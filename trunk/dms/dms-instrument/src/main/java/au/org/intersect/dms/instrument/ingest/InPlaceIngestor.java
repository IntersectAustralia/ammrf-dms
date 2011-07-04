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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;

import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.service.IngestionNode;
import au.org.intersect.dms.core.service.Ingestor;
import au.org.intersect.dms.core.service.JobListener;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.dms.core.service.dto.JobStatusUpdateEvent;

/**
 * Ingests datasets in-place without copy.
 * Sutable for instruments which writes directly to main server.
 * 
 * @version $Rev: 29 $
 */
public class InPlaceIngestor implements Ingestor
{

    @Autowired
    private JobListener jobListener;

    @Autowired
    @Qualifier("scheduledExecutorService")
    private ScheduledExecutorService executorService;

    private long delaySeconds;

    @Required
    public void setDelaySeconds(long delaySeconds)
    {
        this.delaySeconds = delaySeconds;
    }
    
    @Override
    public void ingest(final IngestParameter ingestParams, final IngestionNode workerNode,
            final InstrumentProfile instrumentProfile)
    {
        jobListener.jobStatusUpdate(new JobStatusUpdateEvent(ingestParams.getJobId(), JobStatus.MONITORING));
        executorService.schedule(new Runnable()
        {
            @Override
            public void run()
            {
                workerNode.harvestWithoutCopy(ingestParams, instrumentProfile);
            }
        }, delaySeconds, TimeUnit.SECONDS);
    }

    @Override
    public void stopIngest(Long jobId)
    {
    }

    @Override
    public boolean startIngest(Long jobId)
    {
        return true;
    }

}
