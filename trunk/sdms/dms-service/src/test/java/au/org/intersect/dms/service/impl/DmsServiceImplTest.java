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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.core.service.WorkerNode;
import au.org.intersect.dms.core.service.dto.CopyParameter;
import au.org.intersect.dms.core.service.dto.JobType;
import au.org.intersect.dms.service.domain.DmsUser;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DmsServiceImpl.class)
@PowerMockIgnore("org.apache.log4j.*")
public class DmsServiceImplTest
{
    private static final String CREATE_JOB_METHOD = "createJob";

    @Mock
    private WorkerNode workerNode;

    private DmsUser user = new DmsUser("test-user");

    @InjectMocks
    @Spy
    private DmsService dmsService = new DmsServiceImpl();

    private List<String> sources;

    private String targetDir = "/test";

    private Integer sourceConnectionId = 1000;
    private Integer targetConnectionId = 1001;

    private Long jobId = 123L;

    @Before
    public void setUp() throws Exception
    {
        sources = new LinkedList<String>();
        sources.add("/home/src/test");
    }

    @Test
    public void redo()
    {
        // THE WHOLE TEST NEEDS TO BE REWRITTEN
    }

    // @Test
    public void copy() throws Exception
    {
        doReturn(jobId).when(dmsService, CREATE_JOB_METHOD, JobType.COPY, user.getUsername(), sourceConnectionId,
                sources, targetConnectionId, targetDir, null);

        Long jobId = dmsService.copy(user.getUsername(), sourceConnectionId, sources, targetConnectionId, targetDir);

        verifyPrivate(dmsService).invoke(CREATE_JOB_METHOD, JobType.COPY, user.getUsername(), sourceConnectionId,
                sources, targetConnectionId, targetDir, null);
        verify(workerNode)
                .copy(new CopyParameter(user.getUsername(), jobId, sourceConnectionId, sources, targetConnectionId,
                        targetDir));
        assertEquals("Wrong job ID", jobId, jobId);
    }

    // @Test
    public void copyDownload() throws Exception
    {
        doReturn(jobId).when(dmsService, CREATE_JOB_METHOD, JobType.COPY, user.getUsername(), sourceConnectionId,
                sources, WorkerNode.HDD_CONNECTION_ID, targetDir, null);

        Long jobId = dmsService.copy(user.getUsername(), sourceConnectionId, sources, WorkerNode.HDD_CONNECTION_ID,
                targetDir);

        verifyPrivate(dmsService).invoke(CREATE_JOB_METHOD, JobType.COPY, user.getUsername(), sourceConnectionId,
                sources, WorkerNode.HDD_CONNECTION_ID, targetDir, null);
        verify(workerNode)
                .copy(new CopyParameter(user.getUsername(), jobId, sourceConnectionId, sources, any(Integer.class),
                        targetDir));
        assertEquals("Wrong job ID", jobId, jobId);
    }

}
