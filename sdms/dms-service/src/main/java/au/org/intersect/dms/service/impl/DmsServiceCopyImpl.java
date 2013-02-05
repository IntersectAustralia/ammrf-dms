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

import au.org.intersect.dms.core.service.JobListener;
import au.org.intersect.dms.core.service.WorkerNode;
import au.org.intersect.dms.core.service.dto.CopyParameter;
import au.org.intersect.dms.core.service.dto.JobFinished;
import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.dms.core.service.dto.JobType;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;
import au.org.intersect.dms.service.JobService;
import au.org.intersect.dms.service.domain.Job;

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
    private WorkerNode workerNode;

    @Autowired
    private JobService jobService;

    public Long callCopy(final String username, final Integer sourceConnectionId, final List<String> sources,
            final Integer destinationConnectionId, final String targetDir)
    {
        final Job job = jobService.createJob(JobType.COPY, username, null, makeDescriptor(sourceConnectionId), sources,
                makeDescriptor(destinationConnectionId), targetDir);

        new Thread(new Runnable() {
        	public void run() {
        		LOGGER.info("Job " + job.getId() + ": making connections");
		        NewConnections newConnections = openHddConnections(sourceConnectionId, destinationConnectionId, job.getId());
		        try
		        {
		            workerNode.copy(new CopyParameter(username, job.getId(), newConnections.getSourceId(), sources,
		                    newConnections.getDestinationId(), targetDir));
		        }
		        // TODO CHECKSTYLE-OFF: IllegalCatch
		        catch (Exception e)
		        {
		            LOGGER.error("Exception during copy", e);
		            jobListener.jobEnd(new JobFinished(job.getId(), JobStatus.ABORTED, e));
		        }
		        finally
		        {
		        	closeHddConnections(sourceConnectionId, destinationConnectionId, newConnections);
		        }
		        }
        	}).start();
        return job.getId();
    }

    private String makeDescriptor(Integer connectionId)
    {
        if (connectionId != null)
        {
            if (WorkerNode.HDD_CONNECTION_ID.equals(connectionId))
            {
                return "PC:/";
            }
            OpenConnectionParameter details = workerNode.getConnectionDetails(connectionId);
            return details.getProtocol() + "://" + details.getServer();
        }
        else
        {
            return null;
        }
    }

    @Transactional("serviceTM")
    public void forceStopJob(Long jobId)
    {
        jobListener.jobEnd(new JobFinished(jobId, JobStatus.CANCELLED, null));
    }

    @Transactional("serviceTM")
    public boolean stopJob(Job job)
    {
        boolean resp = workerNode.stopJob(job.getId());
        if (!resp)
        {
            forceStopJob(job.getId());
        }
        return true;
    }

    private NewConnections openHddConnections(Integer sourceConnectionId, Integer destinationConnectionId, Long jobId)
    {
        Integer newSourceId = sourceConnectionId;
        Integer newDestinationId = destinationConnectionId;

        if (isApplet(sourceConnectionId))
        {
            newSourceId = workerNode
                    .openConnection(new OpenConnectionParameter("hdd", "upload", jobId.toString(), null));
        }
        if (isApplet(destinationConnectionId))
        {
            newDestinationId = workerNode.openConnection(new OpenConnectionParameter("hdd", "download", jobId
                    .toString(), null));
        }

        return new NewConnections(newSourceId, newDestinationId);
    }
    
    private void closeHddConnections(Integer sourceConnectionId, Integer destinationConnectionId, NewConnections newConnections)
    {
        if (isApplet(sourceConnectionId))
        {
            workerNode.closeConnection(newConnections.sourceId);
        }
        if (isApplet(destinationConnectionId))
        {
            workerNode.closeConnection(newConnections.destinationId);
        }    	
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
    private static class NewConnections
    {

        private Integer sourceId;
        private Integer destinationId;

        public NewConnections(Integer newSourceId, Integer newDestinationId)
        {
            this.sourceId = newSourceId;
            this.destinationId = newDestinationId;
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

}
