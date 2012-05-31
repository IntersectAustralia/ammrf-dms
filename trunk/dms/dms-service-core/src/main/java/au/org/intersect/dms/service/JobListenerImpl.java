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

package au.org.intersect.dms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.dms.core.errors.IngestionFailedException;
import au.org.intersect.dms.core.service.JobListener;
import au.org.intersect.dms.core.service.dto.JobEvent;
import au.org.intersect.dms.core.service.dto.JobFinished;
import au.org.intersect.dms.core.service.dto.JobScoped;
import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.dms.core.service.dto.JobStatusUpdateEvent;
import au.org.intersect.dms.core.service.dto.JobType;
import au.org.intersect.dms.core.service.dto.JobUpdate;
import au.org.intersect.dms.service.domain.Job;

/**
 * Updates job status
 * 
 * @version $Rev: 29 $
 */
public class JobListenerImpl implements JobListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JobListenerImpl.class);

    private JobAverageSpeedCalculator speedCalculator = new JobAverageSpeedCalculator();

    @Autowired(required = false)
    private JobMetadataIngestor metadataIngestor;

    private Job makeCommonUpdate(JobEvent details)
    {
        Job job = Job.findJob(details.getJobId());
        job.setUpdateTimeStamp(details.getTimeStamp());
        return job;
    }

    @Override
    @Transactional("service")
    public void jobStatusUpdate(JobStatusUpdateEvent details)
    {
        LOGGER.debug("Recived event jobStatusUpdate:{}", details);

        Job job = makeCommonUpdate(details);
        job.setStatus(details.getStatus());
        job.merge();
    }

    @Override
    @Transactional("service")
    public void jobBegin(JobScoped details)
    {
        LOGGER.debug("Recived event jobBegin:{}", details);

        Job job = makeCommonUpdate(details);
        job.setTotalNumberOfDirectories(details.getTotalNumberOfDirectories());
        job.setTotalNumberOfFiles(details.getTotalNumberOfFiles());
        job.setTotalBytes(details.getTotalBytes());
        job.setCopyStartedTimeStamp(details.getTimeStamp());
        job.setStatus(JobStatus.COPYING);
        job.merge();
    }

    @Override
    @Transactional("service")
    public void jobProgress(JobUpdate details)
    {
        LOGGER.trace("Recived event jobProgress:{}", details);

        Job job = Job.findJob(details.getJobId());
        if (job.getUpdateTimeStamp() == null || (job.getUpdateTimeStamp() <= details.getTimeStamp()))
        {
            Double newAverageSpeed = speedCalculator.calculateSpeed(job, details);
            job.setCurrentNumberOfDirectories(details.getCurrentNumberOfDirectories());
            job.setCurrentNumberOfFiles(details.getCurrentNumberOfFiles());
            job.setCurrentBytes(details.getCurrentBytes());
            job.setUpdateTimeStamp(details.getTimeStamp());
            job.setAverageSpeed(newAverageSpeed);
            job.merge();
        }
    }

    @Override
    @Transactional("service")
    public void jobEnd(final JobFinished details)
    {
        LOGGER.debug("Recived event jobEnd:{}", details);

        Job job = makeCommonUpdate(details);
        job.setStatus(details.getOk());

        if (JobStatus.FINISHED == details.getOk())
        {
            job.setFinishedTimeStamp(System.currentTimeMillis());

            if (JobType.INGEST == job.getType())
            {
                if (metadataIngestor == null)
                {
                    throw new IllegalStateException(
                            "Metadata ingestor is not configured. Check spring application context configuration.");
                }
                metadataIngestor.saveMetadata(job, details.getMetadata());
                try
                {
                    metadataIngestor.ingest(job);
                }
                catch (IngestionFailedException e)
                {
                    LOGGER.error("Coudn't ingest metadata for job {}. Aborting job.", job.getId());
                    job.setStatus(JobStatus.ABORTED);
                    job.setFinishedTimeStamp(null);
                }
            }

            LOGGER.debug("Job {} finished.", job.getId());
        }

        job.merge();
    }

}
