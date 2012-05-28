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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.pool.KeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.annotation.Async;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.errors.ConnectionClosedError;
import au.org.intersect.dms.core.errors.UnknownProtocolError;
import au.org.intersect.dms.core.instrument.InstrumentHarvester;
import au.org.intersect.dms.core.instrument.InstrumentHarvesterFactory;
import au.org.intersect.dms.core.instrument.UrlCreator;
import au.org.intersect.dms.core.service.IngestionNode;
import au.org.intersect.dms.core.service.Ingestor;
import au.org.intersect.dms.core.service.JobListener;
import au.org.intersect.dms.core.service.WorkerEventListener;
import au.org.intersect.dms.core.service.WorkerNode;
import au.org.intersect.dms.core.service.dto.CopyParameter;
import au.org.intersect.dms.core.service.dto.CreateDirectoryParameter;
import au.org.intersect.dms.core.service.dto.DeleteParameter;
import au.org.intersect.dms.core.service.dto.GetFileInfoParameter;
import au.org.intersect.dms.core.service.dto.GetListParameter;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.core.service.dto.JobFinished;
import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;
import au.org.intersect.dms.core.service.dto.RenameParameter;
import au.org.intersect.dms.wn.CacheWrapper;
import au.org.intersect.dms.wn.ConnectionParams;
import au.org.intersect.dms.wn.CopyStrategy;
import au.org.intersect.dms.wn.TransportConnection;

/**
 * Implementation of WorkerNode abstraction.
 * 
 */
// TODO CHECKSTYLE-OFF: ClassFanOutComplexity
// TODO CHECKSTYLE-OFF: ClassDataAbstractionCoupling
// TODO Refactor to fix ClassFanOutComplexity violation
public class WorkerNodeImpl implements WorkerNode, IngestionNode
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerNodeImpl.class);

    private static final int FIRST_CONNECTION_ID = 1000;

    private int nextId = FIRST_CONNECTION_ID;

    private CacheWrapper<Integer, ConnectionParams> activeConnectionsCache; // configured in ehcache.xml

    private TransportConnectionTemplate transportTemplate = new TransportConnectionTemplate();

    private final Map<Long, JobTracker> trackers = new HashMap<Long, JobTracker>();

    private final Map<Long, Ingestor> jobIngestors = new HashMap<Long, Ingestor>();

    @Autowired
    private JobListener jobListener;

    private TriggerHelper trigger = new TriggerHelper();

    private CopyStrategy copier;

    @Autowired(required = false)
    private InstrumentHarvesterFactory instrumentProfileFinder;

    private Map<InstrumentProfile, Ingestor> ingestors;

    public void setProtoMapping(Map<String, KeyedObjectPool> protoMapping)
    {
        transportTemplate.setProtoMapping(protoMapping);
    }

    public void setIngestors(Map<InstrumentProfile, Ingestor> ingestors)
    {
        this.ingestors = ingestors;
    }

    @Required
    public void setCopyStrategy(CopyStrategy copier)
    {
        this.copier = copier;
    }

    public void setActiveConnectionsCache(
            CacheWrapper<Integer, ConnectionParams> activeConnectionsCache)
    {
        this.activeConnectionsCache = activeConnectionsCache;
    }

    @Override
    public Integer openConnection(OpenConnectionParameter openConnectionParams)
    {
        Map<String, KeyedObjectPool> protoMapping = transportTemplate.getProtoMapping();
        if (protoMapping == null || protoMapping.get(openConnectionParams.getProtocol()) == null)
        {
            throw new UnknownProtocolError(openConnectionParams.getProtocol());
        }
        final ConnectionParams key = new ConnectionParams(openConnectionParams.getProtocol(),
                openConnectionParams.getServer(), openConnectionParams.getUsername(),
                openConnectionParams.getPassword());
        TransportConnectionCallback<Integer> action = new TransportConnectionCallback<Integer>()
        {

            @Override
            public Integer performWith(TransportConnection conn)
            {
                return newSessionId(key);
            }

        };
        return transportTemplate.execute(key, action);
    }

    @Override
    public List<FileInfo> getList(final GetListParameter getListParams)
    {
        return getList(getListParams, null);
    }

    @Override
    public List<FileInfo> getList(final GetListParameter getListParams, final String filter)
    {
        TransportConnectionCallback<List<FileInfo>> action = new TransportConnectionCallback<List<FileInfo>>()
        {

            @Override
            public List<FileInfo> performWith(TransportConnection conn) throws IOException
            {
                if (filter == null || filter.isEmpty())
                {
                    return conn.getList(getListParams.getAbsolutePath());
                }
                else
                {
                    return conn.getList(getListParams.getAbsolutePath(), filter);
                }
            }

        };
        return transportTemplate.execute(
                activeConnectionsCache.get(getListParams.getConnectionId()), action);
    }

    @Override
    public FileInfo getFileInfo(final GetFileInfoParameter getFileInfoParams)
    {
        TransportConnectionCallback<FileInfo> action = new TransportConnectionCallback<FileInfo>()
        {

            @Override
            public FileInfo performWith(TransportConnection conn) throws IOException
            {
                return conn.getInfo(getFileInfoParams.getAbsolutePath());
            }

        };
        return transportTemplate.execute(
                activeConnectionsCache.get(getFileInfoParams.getConnectionId()), action);
    }

    @Override
    public boolean rename(RenameParameter renameParams)
    {
        final Integer connectionId = renameParams.getConnectionId();
        final String to = renameParams.getTo();
        if (!renameParams.getFrom().startsWith("/"))
        {
            throw new IllegalArgumentException(renameParams.getFrom() + " must begin with '/'");
        }
        int parentDirEnd = renameParams.getFrom().lastIndexOf('/');
        final String parentDirectory = renameParams.getFrom().substring(0,
                parentDirEnd == 0 ? 1 : parentDirEnd);
        final String oldName = renameParams.getFrom().substring(parentDirEnd + 1,
                renameParams.getFrom().length());
        final ConnectionParams connParams = activeConnectionsCache.get(connectionId);
        TransportConnectionCallback<Boolean> action = new TransportConnectionCallback<Boolean>()
        {

            @Override
            public Boolean performWith(TransportConnection conn) throws IOException
            {
                if (conn.rename(parentDirectory, oldName, to))
                {
                    trigger.rename(connParams, parentDirectory, oldName, to);
                    return true;
                }
                return false;
            }

        };
        return transportTemplate.execute(connParams, action);
    }

    @Override
    public boolean delete(final DeleteParameter deleteParams)
    {
        final ConnectionParams connParams = activeConnectionsCache.get(deleteParams
                .getConnectionId());
        // TODO CHECKSTYLE-OFF: AnonInnerLength
        TransportConnectionCallback<Boolean> action = new TransportConnectionCallback<Boolean>()
        {
            @Override
            public Boolean performWith(TransportConnection conn) throws IOException
            {
                return delete(conn, deleteParams.getFiles());
            }

            private boolean delete(TransportConnection conn, List<FileInfo> subList)
                throws IOException
            {
                boolean success = subList.size() > 0 ? false : true;
                for (FileInfo file : subList)
                {
                    if (FileType.DIRECTORY == file.getFileType())
                    {
                        List<FileInfo> listing = conn.getList(file.getAbsolutePath());
                        if (listing.size() > 0)
                        {
                            success = delete(conn, listing);
                        }
                    }
                    success = conn.delete(file);
                    trigger.delete(connParams, file.getAbsolutePath());
                    if (!success)
                    {
                        LOGGER.error("Failed to delete file {}", file.getAbsolutePath());
                    }
                }
                return success;
            }
        };
        // CHECKSTYLE-ON: AnonInnerLength
        return transportTemplate.execute(
                activeConnectionsCache.get(deleteParams.getConnectionId()), action);
    }

    private synchronized Integer newSessionId(ConnectionParams key)
    {
        activeConnectionsCache.put(nextId, key);
        Integer resp = nextId;
        nextId++;
        return resp;
    }

    @Override
    public boolean createDir(final CreateDirectoryParameter createDirectoryparams)
    {
        final ConnectionParams connParams = activeConnectionsCache.get(createDirectoryparams
                .getConnectionId());
        TransportConnectionCallback<Boolean> action = new TransportConnectionCallback<Boolean>()
        {

            @Override
            public Boolean performWith(TransportConnection conn) throws IOException
            {
                if (conn.createDir(createDirectoryparams.getParent(),
                        createDirectoryparams.getName()))
                {
                    trigger.createDirectory(connParams, createDirectoryparams.getParent(),
                            createDirectoryparams.getName());
                    return true;
                }
                return false;
            }

        };
        return transportTemplate.execute(connParams, action);
    }

    @Override
    @Async
    public void copy(CopyParameter copyParams)
    {
        copyAndOptionallyHarvest(copyParams, null);
    }

    @Override
    public void copyAndOptionallyHarvest(final CopyParameter copyParams,
            final InstrumentProfile instrumentProfile)
    {
        final JobTracker tracker = new JobTracker(jobListener, copyParams.getJobId());
        storeTracker(copyParams.getJobId(), tracker);

        final InstrumentHarvester instrumentHarverster = getInstrumentHarvester(
                copyParams.getToConnectionId(), instrumentProfile);

        if (instrumentProfile != null)
        {
            Ingestor ingestor = getIngestor(instrumentProfile);
            if (!ingestor.startIngest(copyParams.getJobId()))
            {
                tracker.cancelJob();
            }
        }

        // TODO CHECKSTYLE-OFF: IllegalCatch
        try
        {
            final ConnectionParams fromConnParams = activeConnectionsCache.get(copyParams
                    .getFromConnectionId());
            TransportConnectionCallback<Void> action = new TransportConnectionCallback<Void>()
            {
                @Override
                public Void performWith(final TransportConnection fromConn) throws IOException
                {

                    copier.doScope(tracker, fromConn, copyParams.getFromFiles(),
                            copyParams.getToDir());
                    final ConnectionParams toConnParams = activeConnectionsCache.get(copyParams
                            .getToConnectionId());
                    TransportConnectionCallback<Void> subAction = new TransportConnectionCallback<Void>()
                    {
                        @Override
                        public Void performWith(final TransportConnection toConn)
                            throws IOException
                        {
                            if (instrumentHarverster != null)
                            {
                                instrumentHarverster.harvestStart(copyParams.getToDir());
                            }
                            copier.copy(tracker, fromConn, toConn, instrumentHarverster, trigger);
                            return null;
                        }
                    };
                    return transportTemplate.execute(toConnParams, subAction);
                }
            };

            LOGGER.debug(
                    "Starting copy: jobId={}, fromConnection={}, fromList={}, toConnection={}, toDir={}",
                    new Object[] {copyParams.getJobId(), copyParams.getFromConnectionId(),
                        copyParams.getFromFiles(), copyParams.getToConnectionId(),
                        copyParams.getToDir()});

            transportTemplate.execute(fromConnParams, action);
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to execute copy job " + copyParams.getJobId(), e);
            tracker.jobException(e);
        }
        finally
        {
            removeTracker(copyParams.getJobId());
            removeJobIngestor(copyParams.getJobId());
        }
        // CHECKSTYLE-ON: IllegalCatch

    }

    @Override
    public void harvestWithoutCopy(final IngestParameter ingestParams,
            final InstrumentProfile instrumentProfile)
    {
        final InstrumentHarvester instrumentHarverster = getInstrumentHarvester(
                ingestParams.getToConnectionId(), instrumentProfile);

        final JobTracker tracker = new JobTracker(jobListener, ingestParams.getJobId());
        storeTracker(ingestParams.getJobId(), tracker);

        try
        {
            TransportConnectionCallback<Void> action = new TransportConnectionCallback<Void>()
            {
                @Override
                public Void performWith(final TransportConnection fromConn) throws IOException
                {
                    copier.doScope(tracker, fromConn, ingestParams.getFromFiles(),
                            ingestParams.getToDir());
                    copier.harvest(ingestParams, instrumentHarverster, tracker, fromConn);
                    return null;
                }
            };

            LOGGER.debug("Starting in place ingestion: jobId={}, fromConnection={}, fromList={}",
                    new Object[] {ingestParams.getJobId(), ingestParams.getFromConnectionId(),
                        ingestParams.getFromFiles()});

            transportTemplate.execute(
                    activeConnectionsCache.get(ingestParams.getFromConnectionId()), action);
        }
        finally
        {
            removeTracker(ingestParams.getJobId());
            removeJobIngestor(ingestParams.getJobId());
        }

    }

    private void storeTracker(Long jobId, JobTracker tracker)
    {
        synchronized (trackers)
        {
            trackers.put(jobId, tracker);
        }
    }

    private void removeTracker(Long jobId)
    {
        synchronized (trackers)
        {
            trackers.remove(jobId);
        }
    }

    @Override
    @Async
    public void ingest(final IngestParameter ingestParams)
    {
        InstrumentProfile instrumentProfile = ingestParams.getInstrumentProfile();

        Ingestor ingestor = getIngestor(instrumentProfile);
        storeJobIngestor(ingestParams.getJobId(), ingestor);

        ingestor.ingest(ingestParams, this, instrumentProfile);
    }

    private InstrumentHarvester getInstrumentHarvester(final Integer toConnectionId,
            final InstrumentProfile instrumentProfile)
    {
        InstrumentHarvester instrumentHarverster = instrumentProfile != null ? instrumentProfileFinder
                .makeInstrumentHarvester(instrumentProfile) : null;
        if (instrumentHarverster != null)
        {
            UrlCreator urlCreator = new UrlCreator()
            {
                private ConnectionParams params = activeConnectionsCache.get(toConnectionId);
                private String topUrl = params.getProtocol() + "://" + params.getHostname();

                @Override
                public String getUrl(String path)
                {
                    return topUrl + path;
                }
            };
            instrumentHarverster.setUrlCreator(urlCreator);
        }
        return instrumentHarverster;
    }

    private Ingestor getIngestor(InstrumentProfile instrumentProfile)
    {
        Ingestor ingestor = ingestors.get(instrumentProfile);
        if (ingestor == null)
        {
            throw new IllegalArgumentException("No ingestor found for instrument profile <"
                    + instrumentProfile + ">");
        }
        return ingestor;
    }

    @Override
    public OpenConnectionParameter getConnectionDetails(Integer connectionId)
    {
        if (HDD_CONNECTION_ID.equals(connectionId))
        {
            return new OpenConnectionParameter("file", "/", null, null);
        }
        ConnectionParams connParams = activeConnectionsCache.get(connectionId);
        if (connParams != null)
        {
            return new OpenConnectionParameter(connParams.getProtocol(), connParams.getHostname(),
                    connParams.getUsername(), connParams.getPassword());
        }
        else
        {
            throw new ConnectionClosedError(connectionId + " is not active");
        }
    }

    @Override
    public void stopIngest(Long jobId, JobStatus status)
    {
        stopIngestion(jobId);
        jobListener
                .jobEnd(new JobFinished(jobId, status,
                        status == JobStatus.ABORTED ? new RuntimeException("Job ingestion aborted")
                                : null));
    }

    @Override
    public boolean stopJob(Long jobId)
    {
        boolean resp = false;
        stopIngestion(jobId);

        synchronized (trackers)
        {
            JobTracker tracker = trackers.get(jobId);
            if (tracker != null)
            {
                tracker.cancelJob();
                resp = true;
            }
        }
        return resp;
    }

    private void storeJobIngestor(Long jobId, Ingestor ingestor)
    {
        synchronized (jobIngestors)
        {
            jobIngestors.put(jobId, ingestor);
        }
    }

    private void removeJobIngestor(Long jobId)
    {
        synchronized (jobIngestors)
        {
            jobIngestors.remove(jobId);
        }
    }

    private void stopIngestion(Long jobId)
    {
        synchronized (jobIngestors)
        {
            Ingestor ingestor = jobIngestors.get(jobId);
            if (ingestor != null)
            {
                ingestor.stopIngest(jobId);
                jobIngestors.remove(jobId);
            }
        }
    }

    @Override
    public void addEventListener(WorkerEventListener listener)
    {
        trigger.addEventListener(listener);
    }

    public void removeEventListener(WorkerEventListener listener)
    {
        trigger.removeEventListener(listener);
    }

}
