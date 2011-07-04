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

package au.org.intersect.dms.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.service.JobListener;
import au.org.intersect.dms.core.service.WorkerNode;
import au.org.intersect.dms.core.service.dto.CopyParameter;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.core.service.dto.JobFinished;
import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.dms.core.service.dto.JobType;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;
import au.org.intersect.dms.service.JobService;
import au.org.intersect.dms.service.domain.Job;
import au.org.intersect.dms.workerrouter.WorkerRouter;

/**
 * Helper to create jobs and call the worker copy or ingest and flag job as aborted on problems
 * 
 * @version $Rev: 29 $
 */
public class DmsServiceCopyImpl
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DmsServiceCopyImpl.class);

    @Autowired
    private JobListener jobListener;

    @Autowired
    private WorkerRouter router;

    @Autowired
    private JobService jobService;

    public Long callCopy(String username, Integer sourceConnectionId, List<String> sources,
            Integer destinationConnectionId, String targetDir)
    {
        Job job = jobService.createJob(JobType.COPY, username, null, makeDescriptor(router, sourceConnectionId),
                sources, makeDescriptor(router, destinationConnectionId), targetDir);
        try
        {
            WorkerAndNewConnections details = resolveCommonWorker(router, sourceConnectionId, destinationConnectionId,
                    job.getId().toString());
            WorkerNode workerNode = details.getWorkerNode();
            Integer sourceToUse = details.getSourceId();
            Integer destinationToUse = details.getDestinationId();
            jobService.setWorker(job.getId(), router.getWorkerId(sourceToUse));
            workerNode
                    .copy(new CopyParameter(username, job.getId(), sourceToUse, sources, destinationToUse, targetDir));
        }
        // TODO CHECKSTYLE-OFF: IllegalCatch
        catch (Exception e)
        {
            LOGGER.error("Exception during copy", e);
            jobListener.jobEnd(new JobFinished(job.getId(), JobStatus.ABORTED, e));
        }
        return job.getId();
    }

    public Long callIngest(String username, Long projectCode, String metadata, IngestParameter parameters)
    {
        Integer sourceConnectionId = parameters.getFromConnectionId();
        Integer destinationConnectionId = parameters.getToConnectionId();
        String targetDir = parameters.getToDir();
        InstrumentProfile instrumentProfile = parameters.getInstrumentProfile();

        Job job = jobService.createJob(JobType.INGEST, username, projectCode,
                makeDescriptor(router, sourceConnectionId), parameters.getFromFiles(),
                makeDescriptor(router, destinationConnectionId), targetDir);
        try
        {
            WorkerAndNewConnections details = resolveCommonWorker(router, sourceConnectionId, destinationConnectionId,
                    job.getId().toString());
            WorkerNode workerNode = details.getWorkerNode();
            Integer newSourceId = details.getSourceId();
            Integer newdestinationId = details.getDestinationId();
            jobService.setWorker(job.getId(), router.getWorkerId(newSourceId));

            jobService.storeMetadata(job.getId(), metadata);

            IngestParameter ingestParams = new IngestParameter(username, job.getId(), newSourceId,
                    parameters.getFromFiles(), newdestinationId, targetDir, instrumentProfile);
            ingestParams.setCopyToWorkstation(parameters.isCopyToWorkstation());
            workerNode.ingest(ingestParams);
        }
        // TODO CHECKSTYLE-OFF: IllegalCatch
        catch (Exception e)
        {
            LOGGER.error("Exception during ingestion", e);
            jobListener.jobEnd(new JobFinished(job.getId(), JobStatus.ABORTED, e));
        }
        return job.getId();
    }

    private String makeDescriptor(WorkerRouter router, Integer connectionId)
    {
        if (connectionId != null)
        {
            if (connectionId == -1)
            {
                return "PC:/";
            }
            WorkerNode workerNode = router.findWorker(connectionId);
            OpenConnectionParameter details = workerNode.getConnectionDetails(connectionId);
            return details.getProtocol() + "://" + details.getServer();
        }
        else
        {
            return null;
        }
    }

    @Transactional("service")
    public void forceStopJob(Long jobId)
    {
        jobListener.jobEnd(new JobFinished(jobId, JobStatus.CANCELLED, null));
    }

    private WorkerAndNewConnections resolveCommonWorker(WorkerRouter router, Integer sourceConnectionId,
            Integer destinationConnectionId, String jobId)
    {
        WorkerNode source = isApplet(sourceConnectionId) ? null : router.findWorker(sourceConnectionId);
        WorkerNode destination = isApplet(destinationConnectionId) ? null : router.findWorker(destinationConnectionId);
        WorkerNode workerNode;
        Integer newSourceId;
        Integer newDestinationId;
        if (source != destination)
        {
            OpenConnectionParameter sourceDetails = isApplet(sourceConnectionId) ? new OpenConnectionParameter("hdd",
                    "upload", jobId, null) : source.getConnectionDetails(sourceConnectionId);
            OpenConnectionParameter destinationDetails = isApplet(destinationConnectionId)
                ? new OpenConnectionParameter("hdd", "download", jobId, null)
                : destination.getConnectionDetails(destinationConnectionId);

            workerNode = router.findCommonWorker(sourceDetails.getProtocol(), sourceDetails.getServer(),
                    destinationDetails.getProtocol(), destinationDetails.getServer());
            newSourceId = workerNode.openConnection(sourceDetails);
            newDestinationId = workerNode.openConnection(destinationDetails);
        }
        else
        {
            workerNode = source;
            newSourceId = sourceConnectionId;
            newDestinationId = destinationConnectionId;
        }
        return new WorkerAndNewConnections(workerNode, newSourceId, newDestinationId);
    }

    private boolean isApplet(Integer connectionId)
    {
        return WorkerNode.HDD_CONNECTION_ID.equals(connectionId);
    }

    /**
     * Just a wrapper class to return the three values from resolveCommonWorker
     * 
     * @version $Rev: 29 $
     */
    private static class WorkerAndNewConnections
    {

        private WorkerNode workerNode;
        private Integer sourceId;
        private Integer destinationId;

        public WorkerAndNewConnections(WorkerNode workerNode, Integer newSourceId, Integer newDestinationId)
        {
            this.workerNode = workerNode;
            this.sourceId = newSourceId;
            this.destinationId = newDestinationId;
        }

        public WorkerNode getWorkerNode()
        {
            return workerNode;
        }

        public Integer getSourceId()
        {
            return sourceId;
        }

        public Integer getDestinationId()
        {
            return destinationId;
        }

    }

    @Transactional("service")
    public boolean stopJob(WorkerRouter router, Job job)
    {
        WorkerNode workerNode = router.findWorkerById(job.getWorkerId());
        boolean resp = workerNode.stopJob(job.getId());
        if (!resp)
        {
            forceStopJob(job.getId());
        }
        return true;
    }

}
