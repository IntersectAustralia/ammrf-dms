/**
 * Project: Platforms for Collaboration at the AMMRF
 *
 * Copyright (c) Intersect Pty Ltd, 2011
 *
 * @see http://www.ammrf.org.au
 * @see http://www.intersect.org.au
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * This program contains open source third party libraries from a number of
 * sources, please read the THIRD_PARTY.txt file for more details.
 */

package au.org.intersect.dms.wn.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.errors.PathNotFoundException;
import au.org.intersect.dms.core.errors.TransportException;
import au.org.intersect.dms.core.instrument.FileHarvester;
import au.org.intersect.dms.core.instrument.InstrumentHarvester;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.wn.CopyStrategy;
import au.org.intersect.dms.wn.TransportConnection;
import au.org.intersect.dms.wn.transports.impl.NullConnection;
import au.org.intersect.dms.wn.transports.impl.PathUtils;

/**
 * Does the copy.
 * 
 * @version $Rev: 29 $
 */
public class WorkerNodeCopyStrategy implements CopyStrategy
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerNodeCopyStrategy.class);

    /**
     * Buffer size
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 8; // 8 Kb

    private int bufferSize = DEFAULT_BUFFER_SIZE;

    public void setBufferSize(int bufferSize)
    {
        this.bufferSize = bufferSize;
    }

    public JobTracker doScope(JobTracker tracker, TransportConnection conn, List<String> fromFiles, String to)
        throws IOException
    {
        tracker.scopeStarted();
        Set<String> addedParents = new HashSet<String>();
        for (String from : fromFiles)
        {
            if (addedParents.contains(from))
            {
                continue;
            }
            doScope(conn, addedParents, from, to, tracker);
        }
        tracker.scopeDone();
        return tracker;
    }

    private void doScope(TransportConnection conn, Set<String> addedParents, String from, String to, JobTracker tracker)
        throws IOException
    {
        FileInfo info = conn.getInfo(from);
        if (info.getFileType() == FileType.DIRECTORY)
        {
            if (!addedParents.contains(from))
            {
                tracker.addDirectory(from, PathUtils.joinPath(to, info.getName()), info.getSize());
                addedParents.add(from);
                for (FileInfo child : conn.getList(from))
                {
                    doScope(conn, addedParents, child.getAbsolutePath(), PathUtils.joinPath(to, info.getName()),
                            tracker);
                }
            }
        }
        else
        {
            tracker.addFile(from, PathUtils.joinPath(to, info.getName()), info.getSize());
        }
    }

    public void copy(final JobTracker tracker, final TransportConnection fromConn, final TransportConnection toConn,
            final InstrumentHarvester instrument) throws IOException
    {
        final TransportStreamTemplate tst = new TransportStreamTemplate();

        for (final FromToData fromTo : tracker.taskList())
        {
            if (fromTo.getType() == FileType.DIRECTORY)
            {
                if (!checkDirectoryExists(toConn, fromTo))
                {
                    String parent = PathUtils.getParent(fromTo.getTo());
                    String name = PathUtils.getName(fromTo.getTo());
                    if (!toConn.createDir(parent, name))
                    {
                        throw new TransportException("Job " + tracker.getJobId() + ": failed to create " + parent + "/"
                                + name);
                    }
                }
                tracker.directoryEnd(fromTo.getTo());
                if (instrument != null)
                {
                    instrument.harvestDirectory(fromTo.getTo());
                }
            }
            else
            {
                StreamCallback<InputStream> inputCallback = new StreamCallback<InputStream>()
                {
                    @Override
                    public void withStream(final InputStream is) throws IOException
                    {
                        StreamCallback<OutputStream> outputCallback = makeOutputStreamCallback(is, tracker,
                                instrument != null ? instrument.getHarvesterFor(fromTo.getFrom()) : null,
                                fromTo.getTo());
                        tst.withOutputStream(toConn, outputCallback, fromTo.getTo(), fromTo.getSize());
                    }
                };
                tst.withInputStream(fromConn, inputCallback, fromTo.getFrom());
                tracker.fileEnd(fromTo.getTo());
                if (instrument != null)
                {
                    instrument.harvestFile(fromTo.getTo(), fromTo.getSize());
                }
            }
        }

        if (instrument != null)
        {
            instrument.harvestEnd();
            tracker.jobFinished(instrument.getMetadata());
        }
        else
        {
            tracker.jobFinished();
        }
    }

    public void harvest(final IngestParameter ingestParams, final InstrumentHarvester instrument,
            final JobTracker tracker, final TransportConnection fromConn)
    {
        instrument.harvestStart(ingestParams.getToDir());

        for (final FromToData fromTo : tracker.taskList())
        {
            switch (fromTo.getType())
            {
                case DIRECTORY:
                    tracker.directoryEnd(fromTo.getTo());
                    instrument.harvestDirectory(fromTo.getTo());
                    break;
                case FILE:
                    final FileHarvester harvester = instrument.getHarvesterFor(fromTo.getFrom());
                    if (harvester != null)
                    {
                        final TransportStreamTemplate tst = new TransportStreamTemplate();
                        StreamCallback<InputStream> inputCallback = new StreamCallback<InputStream>()
                        {
                            @Override
                            public void withStream(final InputStream is) throws IOException
                            {
                                StreamCallback<OutputStream> outputCallback = makeOutputStreamCallback(is, tracker,
                                        harvester, fromTo.getTo());
                                tst.withOutputStream(new NullConnection(), outputCallback, fromTo.getTo(),
                                        fromTo.getSize());
                            }
                        };
                        tst.withInputStream(fromConn, inputCallback, fromTo.getFrom());
                    }
                    else
                    {
                        tracker.progressFile(fromTo.getSize());
                    }

                    instrument.harvestFile(fromTo.getTo(), fromTo.getSize());
                    tracker.fileEnd(fromTo.getTo());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown file type " + fromTo.getType());
            }
        }

        instrument.harvestEnd();
        tracker.jobFinished(instrument.getMetadata());
    }

    private boolean checkDirectoryExists(final TransportConnection toConn, final FromToData fromTo)
    {
        try
        {
            FileInfo dirInfo = getDirInfo(toConn, fromTo.getTo());
            if (dirInfo.getFileType() == FileType.DIRECTORY)
            {
                return true;
            }
            else
            {
                throw new TransportException("File " + fromTo.getTo() + " found, but is not a directory");
            }
        }
        catch (PathNotFoundException e)
        {
            return false;
        }
    }

    private FileInfo getDirInfo(TransportConnection toConn, String to)
    {
        try
        {
            return toConn.getInfo(to);
        }
        catch (IOException e)
        {
            throw new PathNotFoundException("Couldn't retrieve information for '" + to + "'");
        }
    }

    private StreamCallback<OutputStream> makeOutputStreamCallback(final InputStream is, final JobTracker tracker,
            final FileHarvester harvester, final String toPath)
    {
        return new StreamCallback<OutputStream>()
        {
            @Override
            public void withStream(OutputStream os) throws IOException
            {
                OutputStream harvestOS = harvester != null ? harvester.getSink() : null;
                byte[] buffer = new byte[bufferSize];
                boolean ok = false;
                try
                {
                    streamCopy(buffer, is, tracker, os, harvestOS);
                    ok = true;
                }
                catch (IOException e)
                {
                    throw e;
                }
                finally
                {
                    attachMetadata(harvester, harvestOS, ok);
                }
            }
        };
    }

    protected void attachMetadata(FileHarvester harvester, OutputStream harvestOS, boolean ok)
    {
        if (harvester != null)
        {
            harvester.closeSink(harvestOS, ok);
        }
    }

    private void streamCopy(byte[] buffer, final InputStream is, final JobTracker tracker, OutputStream os,
            OutputStream harvestOS) throws IOException
    {
        int readBytes = is.read(buffer);
        while (readBytes > 0)
        {
            os.write(buffer, 0, readBytes);
            if (harvestOS != null)
            {
                harvestOS.write(buffer, 0, readBytes);
            }
            tracker.progressFile(readBytes);
            readBytes = is.read(buffer);
        }
    }

}