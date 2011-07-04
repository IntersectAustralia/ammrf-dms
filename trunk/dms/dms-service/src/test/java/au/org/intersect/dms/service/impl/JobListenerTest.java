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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.spy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import au.org.intersect.dms.core.service.JobListener;
import au.org.intersect.dms.core.service.dto.JobFinished;
import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.dms.core.service.dto.JobType;
import au.org.intersect.dms.service.domain.DmsUser;
import au.org.intersect.dms.service.domain.Job;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Job.class)
@PowerMockIgnore("org.apache.log4j.*")
public class JobListenerTest
{

    @Mock
    private MetadataIngestor metadataIngestor;

    @InjectMocks
    private JobListener jobListener = new JobListenerImpl();

    private Long jobId = 123L;

    private DmsUser user = new DmsUser("testuser");

    @Spy
    private Job job = new Job(JobType.INGEST, user, "ftp://localhost", "/test/dest", "ftp://localhost", 1);

    @Before
    public void setUp() throws Exception
    {
        job.setId(jobId);
        job.setStatus(JobStatus.CREATED);

        spy(Job.class);

        doReturn(job).when(Job.class, "findJob", jobId);
        doReturn(job).when(job).merge();
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void jobException()
    {
        JobFinished details = new JobFinished(jobId, JobStatus.ABORTED, new Exception("Some test exception"));
        doThrow(new RuntimeException("Some test exception")).when(metadataIngestor).saveMetadata(job, null);
        jobListener.jobEnd(details);

        assertEquals("Wrong job status", JobStatus.ABORTED, job.getStatus());
        assertNull("Finished time must be null", job.getFinishedTimeStamp());

    }

}
