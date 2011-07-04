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

package au.org.intersect.dms.instrument.atomprobe;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.errors.TransportError;
import au.org.intersect.dms.core.service.AtomProbeService;
import au.org.intersect.dms.core.service.ConfigurationService;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;

@RunWith(PowerMockRunner.class)
public class AtomProbeJobCreatorTest
{
    private static final String FILENAME_PREFIX = "test";

    private static final String INSTRUMENT_PROTOCOL = "local";

    private static final String SLASH = "/";

    // private String repoProtocol = "ftp";

    // private String repoHost = "localhost";

    // private String repoUsername = "AtomProbeUser";

    // private String repoPassword = "password";

    private String defaultUsername = "AtomProbeUser";

    private String fileExtention = ".RHIT";

    @InjectMocks
    private AtomProbeJobCreator jobCreator = new AtomProbeJobCreator();

    @Mock
    private AtomProbeService atomProbeService;
    
    @Mock
    private ConfigurationService configurationService;

    @Mock
    private DmsService dmsService;

    private InstrumentProfile instrumentProfile = InstrumentProfile.ATOM_PROBE;

    private Integer fromConnectionId = 1000;

    private Integer toConnectionId = 1001;

    private String targetRootPath = "/atomProbeTest";

    private Long instrumentId = 1L;

    private Long repositoryId = 2L;

    private OpenConnectionParameter instrumentParams = new OpenConnectionParameter("local", "atomProbe", "atom", "pwd");
    private OpenConnectionParameter repoParams = new OpenConnectionParameter("ftp", "localhost", "AtomProbeUser",
            "password");

    private void initProperties(AtomProbeJobCreator jobCreator)
    {
        jobCreator.setInstrument(instrumentId);
        jobCreator.setRepository(repositoryId);
        jobCreator.setDefaultUsername(defaultUsername);
        jobCreator.setFileExtension(fileExtention);
        jobCreator.setTargetRootPath(targetRootPath);
    }

    @Before
    public void setUp() throws Exception
    {

        when(configurationService.getServerConnectionParameters(instrumentId)).thenReturn(instrumentParams);
        when(configurationService.getServerConnectionParameters(repositoryId)).thenReturn(repoParams);

        when(
                dmsService.openConnection(instrumentParams.getProtocol(), instrumentParams.getServer(),
                        instrumentParams.getUsername(), instrumentParams.getPassword())).thenReturn(fromConnectionId);
        when(
                dmsService.openConnection(repoParams.getProtocol(), repoParams.getServer(), repoParams.getUsername(),
                        repoParams.getPassword())).thenReturn(toConnectionId);

        initProperties(jobCreator);
    }

    @After
    public void tearDown()
    {
        verifyNoMoreInteractions(dmsService);
        verifyNoMoreInteractions(atomProbeService);
    }

    private void verifyGetServerConnectionParams()
    {
        verify(configurationService).getServerConnectionParameters(instrumentId);
        verify(configurationService).getServerConnectionParameters(repositoryId);
    }

    @Test
    public void testCreateJobsUsernameNull()
    {
        runTestForUser(null, defaultUsername);
        verifyGetServerConnectionParams();
    }

    @Test
    public void testCreateJobsUsernameNoMatching()
    {
        String username = "user1";

        when(atomProbeService.getBookingSystemUsername(instrumentId, username)).thenReturn(null);
        runTestForUser(username, defaultUsername);
        verify(atomProbeService).getBookingSystemUsername(instrumentId, username);
        verifyGetServerConnectionParams();
    }

    @Test
    public void testCreateJobsUsernameMatched()
    {
        String username = "user1";
        String bsUsername = "bs-" + username;

        when(atomProbeService.getBookingSystemUsername(instrumentId, username)).thenReturn(bsUsername);

        runTestForUser(username, bsUsername);
        verify(atomProbeService).getBookingSystemUsername(instrumentId, username);
        verifyGetServerConnectionParams();
    }

    @Test
    public void jobCreationFailed()
    {

        List<Experiment> experiments = new LinkedList<Experiment>();
        for (int i = 1; i <= 3; i++)
        {
            Experiment experiment = new Experiment();
            experiment.setId(Long.valueOf(i));
            experiment.setFileName(FILENAME_PREFIX + i);
            experiments.add(experiment);
            String source = SLASH + experiment.getFileName() + fileExtention;
            IngestParameter ingestParameters = new IngestParameter(defaultUsername, null, fromConnectionId, source,
                    toConnectionId, targetRootPath + SLASH + defaultUsername, instrumentProfile);
            if (i < 3)
            {
                when(dmsService.ingest(defaultUsername, null, null, ingestParameters)).thenReturn(10L + i);
            }
            else
            {
                when(dmsService.ingest(defaultUsername, null, null, ingestParameters)).thenReturn(null);
            }
        }

        jobCreator.createJobs(experiments);

        for (int i = 1; i <= 3; i++)
        {
            Experiment experiment = new Experiment();
            experiment.setId(Long.valueOf(i));
            experiment.setFileName(FILENAME_PREFIX + i);
            experiments.add(experiment);
            String source = SLASH + experiment.getFileName() + fileExtention;

            IngestParameter ingestParameters = new IngestParameter(defaultUsername, null, fromConnectionId, source,
                    toConnectionId, targetRootPath + SLASH + defaultUsername, instrumentProfile);
            verify(dmsService).ingest(defaultUsername, null, null, ingestParameters);
        }
        verify(atomProbeService).setLastProcessedExperiment(instrumentId, 2L);

        verify(dmsService, times(3)).openConnection(instrumentParams.getProtocol(), instrumentParams.getServer(),
                instrumentParams.getUsername(), instrumentParams.getPassword());
        verify(dmsService, times(3)).openConnection(repoParams.getProtocol(), repoParams.getServer(),
                repoParams.getUsername(), repoParams.getPassword());
        verifyGetServerConnectionParams();
    }

    @Test
    public void connectionFailed()
    {

        List<Experiment> experiments = new LinkedList<Experiment>();
        for (int i = 1; i <= 3; i++)
        {
            Experiment experiment = new Experiment();
            experiment.setId(Long.valueOf(i));
            experiment.setFileName(FILENAME_PREFIX + i);
            experiments.add(experiment);
            String source = SLASH + experiment.getFileName() + fileExtention;

            IngestParameter ingestParameters = new IngestParameter(defaultUsername, null, fromConnectionId, source,
                    toConnectionId, targetRootPath + SLASH + defaultUsername, instrumentProfile);
            when(
                    dmsService.openConnection(instrumentParams.getProtocol(), instrumentParams.getServer(),
                            instrumentParams.getUsername(), instrumentParams.getPassword())).thenReturn(
                    fromConnectionId).thenThrow(new TransportError("Can't connect"));
            when(dmsService.ingest(defaultUsername, null, null, ingestParameters)).thenReturn(10L + i);
        }

        jobCreator.createJobs(experiments);

        for (int i = 1; i <= 3; i++)
        {
            Experiment experiment = new Experiment();
            experiment.setId(Long.valueOf(i));
            experiment.setFileName(FILENAME_PREFIX + i);
            experiments.add(experiment);
            String source = SLASH + experiment.getFileName() + fileExtention;

            if (i == 1)
            {
                IngestParameter ingestParameters = new IngestParameter(defaultUsername, null, fromConnectionId, source,
                        toConnectionId, targetRootPath + SLASH + defaultUsername, instrumentProfile);
                verify(dmsService).ingest(defaultUsername, null, null, ingestParameters);
                verify(atomProbeService).setLastProcessedExperiment(instrumentId, experiment.getId());
            }
        }
        verify(dmsService, times(2)).openConnection(instrumentParams.getProtocol(), instrumentParams.getServer(),
                instrumentParams.getUsername(), instrumentParams.getPassword());
        verify(dmsService).openConnection(repoParams.getProtocol(), repoParams.getServer(), repoParams.getUsername(),
                repoParams.getPassword());
        verifyGetServerConnectionParams();
    }

    @Test
    public void noExperiments()
    {
        List<Experiment> experiments = new LinkedList<Experiment>();
        jobCreator.createJobs(experiments);
    }

    private void runTestForUser(String experimentUsername, String bookingSystemUsername)
    {
        Experiment experiment = new Experiment();
        experiment.setId(1L);
        experiment.setFileName("test1");
        experiment.setUsername(experimentUsername);

        List<Experiment> experiments = new LinkedList<Experiment>();
        experiments.add(experiment);

        String source = SLASH + experiment.getFileName() + fileExtention;
        IngestParameter ingestParameters = new IngestParameter(bookingSystemUsername, null, fromConnectionId, source,
                toConnectionId, targetRootPath + SLASH + bookingSystemUsername, instrumentProfile);
        when(dmsService.ingest(bookingSystemUsername, null, null, ingestParameters)).thenReturn(11L);

        jobCreator.createJobs(experiments);

        verify(dmsService).ingest(bookingSystemUsername, null, null, ingestParameters);
        verify(dmsService).openConnection(instrumentParams.getProtocol(), instrumentParams.getServer(),
                instrumentParams.getUsername(), instrumentParams.getPassword());
        verify(dmsService).openConnection(repoParams.getProtocol(), repoParams.getServer(), repoParams.getUsername(),
                repoParams.getPassword());
        verify(atomProbeService).setLastProcessedExperiment(instrumentId, experiment.getId());
    }
}
