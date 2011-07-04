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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.service.IngestionNode;
import au.org.intersect.dms.core.service.dto.CopyParameter;
import au.org.intersect.dms.core.service.dto.GetListParameter;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;
import au.org.intersect.dms.instrument.ingest.filewatcher.FilePoller;
import au.org.intersect.dms.instrument.ingest.filewatcher.FileWatcher;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:/META-INF/spring/applicationContext-workernode*.xml")
public class CancelPollerTest
{
    private static final String SHOULDN_T_BE_CALLED_MESSAGE = "shouldn't be called";

    @Autowired
    private FilePoller poller;

    private boolean stopped;

    /**
     * Test that a configuration for MICRO_CT exists and that the finder (a service factory) actually delivers different
     * objects in each request.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testCancelPoller() throws InterruptedException
    {
        IngestParameter ingestParams = new IngestParameter("testuser", 1L, 1, "from", 2, "to", null);
        IngestionNode ingestor = buildIngestor();
        FileWatcher fileWatcher = new FileWatcher(ingestParams, "*.log", ingestor, null, null);
        poller.setPeriod(TimeUnit.SECONDS.toMillis(2));
        poller.start(fileWatcher);
        Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        fileWatcher.cancelJob();
        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        assertTrue("File poller should be stopped", stopped);

    }

    private IngestionNode buildIngestor()
    {
        return new DummyIngestionNode();
    }

    private final class DummyIngestionNode implements IngestionNode
    {
        @Override
        public void stopIngest(Long jobId, JobStatus aborted)
        {
            stopped = true;
        }

        @Override
        public List<FileInfo> getList(GetListParameter getListParams, String filter)
        {
            return new ArrayList<FileInfo>();
        }

        public void copyAndOptionallyHarvest(CopyParameter copyParams,
                au.org.intersect.dms.core.domain.InstrumentProfile instrumentProfile)
        {
            throw new RuntimeException(SHOULDN_T_BE_CALLED_MESSAGE);
        };

        @Override
        public Integer openConnection(OpenConnectionParameter openConnectionParams)
        {
            return null;
        }

        public void harvestWithoutCopy(IngestParameter ingestParams,
                au.org.intersect.dms.core.domain.InstrumentProfile instrumentProfile)
        {
            throw new RuntimeException(SHOULDN_T_BE_CALLED_MESSAGE);
        };

        @Override
        public OpenConnectionParameter getConnectionDetails(Integer connectionId)
        {
            throw new RuntimeException(SHOULDN_T_BE_CALLED_MESSAGE);
        }
    }

}
