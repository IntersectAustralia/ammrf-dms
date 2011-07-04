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

package au.org.intersect.dms.instrument.olympus;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.service.ConfigurationService;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;

@RunWith(PowerMockRunner.class)
public class TirfPollerTest
{

    private static final String DATASET1_PATH = "/testuser1/dataset1";

    private static final String DATASET1_URL = "ftp://localhost" + DATASET1_PATH;

    private static final String ROOT_DIR = "/fv1000";

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private DmsService dmsService;

    @InjectMocks
    private TIRFPoller poller = new TIRFPoller();

    private OpenConnectionParameter repoConnectionParams = new OpenConnectionParameter("ftp", "localhost", "repouser",
            "password");
    private Long repositoryId = 3L;

    private Integer connectionId = 1234;

    private List<FileInfo> usersDirs = new LinkedList<FileInfo>();

    private Date date = new Date();

    private FileInfo dataset1 = new FileInfo(FileType.DIRECTORY, DATASET1_PATH, "dataset1", 0L, date);
    private FileInfo dataset2 = new FileInfo(FileType.DIRECTORY, "/testuser1/dataset2", "dataset2", 0L, date);

    private List<FileInfo> datasetFiles = new LinkedList<FileInfo>();

    @Before
    public void setUp() throws Exception
    {
        when(configurationService.getServerConnectionParameters(repositoryId)).thenReturn(repoConnectionParams);
        when(
                dmsService.openConnection(repoConnectionParams.getProtocol(), repoConnectionParams.getServer(),
                        repoConnectionParams.getUsername(), repoConnectionParams.getPassword())).thenReturn(
                connectionId);

        poller.setRepository(repositoryId);
        poller.setRootDirectory(ROOT_DIR);

        for (int i = 0; i <= 3; i++)
        {
            usersDirs.add(new FileInfo(FileType.DIRECTORY, "/testuser" + i, "testuser" + i, 0L, date));
        }

        datasetFiles.add(new FileInfo(FileType.FILE, "/testuser1/dataset1/test1.tif", "test1.tif", 10L, date));
        datasetFiles.add(new FileInfo(FileType.FILE, "/testuser1/dataset1/test2.tif", "test2.tif", 20L, date));
        datasetFiles.add(new FileInfo(FileType.FILE, "/testuser1/dataset1/test3.tif", "test3.tif", 30L, date));
    }

    @After
    public void tearDown() throws Exception
    {
        verify(configurationService).getServerConnectionParameters(repositoryId);
        verifyNoMoreInteractions(configurationService);
    }

    @Test
    public void noNewDatasets()
    {
        when(dmsService.getList(connectionId, ROOT_DIR)).thenReturn(usersDirs);

        List<FileInfo> userFiles = new LinkedList<FileInfo>();
        userFiles.add(dataset1);
        when(dmsService.getList(connectionId, "/testuser1")).thenReturn(userFiles);

        when(dmsService.isUrlCatalogued(DATASET1_URL)).thenReturn(true);
        when(dmsService.getList(connectionId, DATASET1_PATH)).thenReturn(datasetFiles);

        assertReflectionEquals("No new datasets expected", Collections.EMPTY_LIST, poller.getNewDatasets());
    }

    @Test
    public void oneNewDatasetOneUser()
    {
        when(dmsService.getList(connectionId, ROOT_DIR)).thenReturn(usersDirs);

        List<FileInfo> userFiles = new LinkedList<FileInfo>();
        userFiles.add(dataset1);
        userFiles.add(dataset2);
        when(dmsService.getList(connectionId, "/testuser1")).thenReturn(userFiles);

        when(dmsService.isUrlCatalogued(DATASET1_URL)).thenReturn(false);
        when(dmsService.getList(connectionId, DATASET1_PATH)).thenReturn(datasetFiles);

        Collection<DatasetParams> newDatasets = poller.getNewDatasets();

        Collection<DatasetParams> expectedDatasets = new LinkedList<DatasetParams>();
        DatasetParams expectedDataset = new DatasetParams("testuser1", DATASET1_PATH, dataset1.getModificationDate(),
                InstrumentProfile.OLYMPUS_TIRF);
        expectedDatasets.add(expectedDataset);

        assertReflectionEquals(expectedDatasets, newDatasets, ReflectionComparatorMode.LENIENT_ORDER);
    }

}
