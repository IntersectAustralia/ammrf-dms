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

package au.org.intersect.dms.instrument.ingest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.service.IngestionNode;
import au.org.intersect.dms.core.service.Ingestor;
import au.org.intersect.dms.core.service.JobListener;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.dms.core.service.dto.JobStatusUpdateEvent;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;
import au.org.intersect.dms.instrument.ingest.filewatcher.FilePoller;
import au.org.intersect.dms.instrument.ingest.filewatcher.FileWatcher;

/**
 * Ingests datasets from MicroCT
 * 
 */
public class MicroCTIngestor implements Ingestor
{

    @Autowired
    private FilePoller filePoller;

    @Autowired
    private JobListener jobListener;

    private Map<Long, FileWatcher> watchers = new HashMap<Long, FileWatcher>();

    @Override
    public void ingest(final IngestParameter ingestParams, final IngestionNode workerNode,
            final InstrumentProfile instrumentProfile)
    {

        OpenConnectionParameter toConnectionParams = workerNode.getConnectionDetails(ingestParams.getToConnectionId());
        FileWatcher fileWatcher = new FileWatcher(ingestParams, ".*[.]log$", workerNode, instrumentProfile,
                toConnectionParams);
        storeWatcher(ingestParams.getJobId(), fileWatcher);
        filePoller.start(fileWatcher);
        jobListener.jobStatusUpdate(new JobStatusUpdateEvent(ingestParams.getJobId(), JobStatus.MONITORING));

    }

    @Override
    public void stopIngest(Long jobId)
    {
        FileWatcher watcher = watchers.get(jobId);
        if (watcher != null)
        {
            watcher.cancelJob();
            watchers.remove(jobId);
        }
    }

    @Override
    public boolean startIngest(Long jobId)
    {
        boolean resp = true;
        synchronized (watchers)
        {
            FileWatcher found = watchers.remove(jobId);
            if (found != null && found.isCancelled())
            {
                resp = false;
            }
        }
        return resp;
    }

    private void storeWatcher(Long jobId, FileWatcher watcher)
    {
        synchronized (watchers)
        {
            watchers.put(jobId, watcher);
        }
    }
}
