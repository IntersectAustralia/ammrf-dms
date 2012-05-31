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

package au.org.intersect.dms.instrument.ingest.filewatcher;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.service.IngestionNode;
import au.org.intersect.dms.core.service.dto.CopyParameter;
import au.org.intersect.dms.core.service.dto.GetListParameter;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;

/**
 * File watcher.
 * 
 * @version $Rev: 5 $
 * 
 */
public class FileWatcher
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcher.class);

    private IngestParameter ingestParams;
    private String filter;
    private IngestionNode ingestionNode;

    private InstrumentProfile instrumentProfile;

    private boolean cancelled;

    private OpenConnectionParameter toConnectionParams;

    public FileWatcher(IngestParameter ingestParams, String filter, IngestionNode workerNode,
            InstrumentProfile instrumentProfile, OpenConnectionParameter toConnectionParams)
    {
        super();
        this.ingestParams = ingestParams;
        this.filter = filter;
        this.ingestionNode = workerNode;
        this.instrumentProfile = instrumentProfile;
        this.toConnectionParams = toConnectionParams;
    }

    /**
     * Checks if marker file appeared
     * 
     * @return
     */
    public FileWatcherResult checkMarkerFile()
    {
        LOGGER.trace("Checking marker file...");

        if (cancelled)
        {
            LOGGER.debug("File watcher was cancelled. Stop monitoring.");
            return FileWatcherResult.CANCELLED;
        }

        GetListParameter getListParams = new GetListParameter(ingestParams.getFromConnectionId(), getFromDir());
        // TODO CHECKSTYLE-OFF: IllegalCatch
        try
        {
            List<FileInfo> markerFiles = ingestionNode.getList(getListParams, filter);
            boolean found = !markerFiles.isEmpty();
            if (found)
            {
                LOGGER.debug("Marker file found. Starting ingestion...");
            }
            else
            {
                LOGGER.trace("Marker file not found. Continue monitoring.");
            }
            return found ? FileWatcherResult.FOUND : FileWatcherResult.WAIT;
        }
        catch (Exception e)
        {
            return FileWatcherResult.ERROR;
        }
        // CHECKSTYLE-ON: IllegalCatch
    }

    public String getMonitoredDirectory()
    {
        return getFromDir();
    }

    public Integer getFromConnectionId()
    {
        return ingestParams.getFromConnectionId();
    }

    /**
     * Actual ingestion.
     */
    public void ingest()
    {
        LOGGER.debug("Ingesting directory {}", getMonitoredDirectory());
        List<String> sources = new LinkedList<String>();
        sources.add(getFromDir());

        Integer toConnectionId = ingestionNode.openConnection(toConnectionParams);

        ingestionNode.copyAndOptionallyHarvest(new CopyParameter(ingestParams.getUsername(), ingestParams.getJobId(),
                ingestParams.getFromConnectionId(), sources, toConnectionId, ingestParams.getToDir()),
                instrumentProfile);
    }

    public void abortIngest()
    {
        ingestionNode.stopIngest(ingestParams.getJobId(), JobStatus.ABORTED);
    }

    public void cancelIngest()
    {
        ingestionNode.stopIngest(ingestParams.getJobId(), JobStatus.CANCELLED);
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder("FileWatcher[");
        str.append("dir:").append(getFromDir()).append(", filter:").append(filter).append("]");
        return str.toString();
    }

    public void cancelJob()
    {
        cancelled = true;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }

    private String getFromDir()
    {
        return ingestParams.getFromFiles().get(0);
    }
}
