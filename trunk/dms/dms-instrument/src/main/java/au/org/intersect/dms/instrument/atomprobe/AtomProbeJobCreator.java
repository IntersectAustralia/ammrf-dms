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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.errors.TransportError;
import au.org.intersect.dms.core.service.AtomProbeService;
import au.org.intersect.dms.core.service.ConfigurationService;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;

/**
 * Creates ingest jobs for atom probe
 */
public class AtomProbeJobCreator
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomProbeJobCreator.class);

    @Autowired
    private DmsService dmsService;

    @Autowired
    private AtomProbeService atomProbeService;
    
    @Autowired
    private ConfigurationService configurationService;

    private Long instrumentId;

    private Long repositoryId;

    private String fileExtension;

    private String defaultUsername;

    private String targetRootPath;

    @Required
    public void setInstrument(Long instrumentId)
    {
        this.instrumentId = instrumentId;
    }

    @Required
    public void setRepository(Long repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    @Required
    public void setDefaultUsername(String defaultUsername)
    {
        this.defaultUsername = defaultUsername;
    }

    @Required
    public void setTargetRootPath(String targetRootPath)
    {
        this.targetRootPath = targetRootPath;
    }

    @Required
    public void setFileExtension(String fileExtension)
    {
        this.fileExtension = fileExtension;
    }

    public void createJobs(List<Experiment> experiments)
    {
        if (experiments.isEmpty())
        {
            return;
        }

        Long lastProcessedExperimentId = null;
        OpenConnectionParameter instrumentConnectionParams = configurationService
                .getServerConnectionParameters(instrumentId);
        OpenConnectionParameter repositoryConnectionParams = configurationService
                .getServerConnectionParameters(repositoryId);

        for (Experiment experiment : experiments)
        {
            try
            {
                LOGGER.info("Creating ingestion job for instrument <{}> and experiment <{}>",
                        instrumentConnectionParams.getServer(), experiment);

                Integer fromConnectionId = dmsService.openConnection(instrumentConnectionParams.getProtocol(),
                        instrumentConnectionParams.getServer(), instrumentConnectionParams.getUsername(),
                        instrumentConnectionParams.getPassword());
                Integer toConnectionId = dmsService.openConnection(repositoryConnectionParams.getProtocol(),
                        repositoryConnectionParams.getServer(), repositoryConnectionParams.getUsername(),
                        repositoryConnectionParams.getPassword());
                String username = getMatchingUsername(experiment.getUsername());
                String sourceDir = "/" + experiment.getFileName() + fileExtension;
                String targetDir = targetRootPath + "/" + username;

                IngestParameter ingestParameters = new IngestParameter(username, null, fromConnectionId, sourceDir,
                        toConnectionId, targetDir, InstrumentProfile.ATOM_PROBE);

                Long jobId = dmsService.ingest(username, null, null, ingestParameters);
                if (jobId != null)
                {
                    lastProcessedExperimentId = experiment.getId();
                    LOGGER.info("Ingestion job with ID {} has been created.", jobId);
                }
                else
                {
                    LOGGER.info("Couldn't create ingestion job for instrument <{}> and experiment <{}>."
                            + " Processing has been stopped.", instrumentConnectionParams.getServer(), experiment);
                    break;
                }
            }
            catch (TransportError e)
            {
                LOGGER.error("Couldn't create ingestion job for instrument <" + instrumentConnectionParams.getServer()
                        + "> and exepriment <" + experiment + ">. Processing has been stopped.", e);
                break;
            }
        }

        if (lastProcessedExperimentId != null)
        {
            atomProbeService.setLastProcessedExperiment(instrumentId, lastProcessedExperimentId);
        }

    }

    private String getMatchingUsername(String username)
    {
        String mappedUserName = defaultUsername;
        if (username != null)
        {
            String bookingUsername = atomProbeService.getBookingSystemUsername(instrumentId, username);
            if (bookingUsername != null)
            {
                mappedUserName = bookingUsername;
            }
        }
        return mappedUserName;

    }
}
