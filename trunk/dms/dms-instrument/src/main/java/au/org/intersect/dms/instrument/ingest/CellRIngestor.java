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

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.service.ConfigurationService;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.core.service.IngestionNode;
import au.org.intersect.dms.core.service.Ingestor;
import au.org.intersect.dms.core.service.dto.CopyParameter;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;

/**
 * Ingests datasets from Olympus Cell^R with optional copy to workstation
 * 
 * @version $Rev: 29 $
 */
public class CellRIngestor implements Ingestor
{
    private static final String SLASH = "/";

    private static final Logger LOGGER = LoggerFactory.getLogger(CellRIngestor.class);

    private Long workstationId;

    private String workstationTargetDir;

    private Long repositoryId;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private DmsService dmsService;

    @Required
    public void setWorkstationId(Long workstationId)
    {
        this.workstationId = workstationId;
    }

    @Required
    public void setWorkstationTargetDir(String workstationTargetDir)
    {
        this.workstationTargetDir = workstationTargetDir;
    }

    @Required
    public void setRepositoryId(Long repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    @Override
    public void ingest(IngestParameter ingestParams, IngestionNode workerNode, InstrumentProfile instrumentProfile)
    {
        LOGGER.info("Ingesting to main server...");
        workerNode.copyAndOptionallyHarvest(new CopyParameter(ingestParams.getUsername(), ingestParams.getJobId(),
                ingestParams.getFromConnectionId(), ingestParams.getFromFiles(), ingestParams.getToConnectionId(),
                ingestParams.getToDir()), instrumentProfile);

        if (ingestParams.isCopyToWorkstation())
        {
            LOGGER.info("Finished. Coping to workstation...");
            OpenConnectionParameter repoConnectionParams = configurationService
                    .getServerConnectionParameters(repositoryId);
            Integer fromConnectionId = dmsService.openConnection(repoConnectionParams.getProtocol(),
                    repoConnectionParams.getServer(), repoConnectionParams.getUsername(),
                    repoConnectionParams.getPassword());

            OpenConnectionParameter workstationConnectionParams = configurationService
                    .getServerConnectionParameters(workstationId);
            Integer toConnectionId = dmsService.openConnection(workstationConnectionParams.getProtocol(),
                    workstationConnectionParams.getServer(), workstationConnectionParams.getUsername(),
                    workstationConnectionParams.getPassword());

            dmsService.copy(ingestParams.getUsername(), fromConnectionId, createCopyFromList(ingestParams),
                    toConnectionId, workstationTargetDir);
        }
        LOGGER.info("Finished.");
    }

    List<String> createCopyFromList(IngestParameter ingestParams)
    {
        StringBuffer from = new StringBuffer(ingestParams.getToDir());
        String fromDir = ingestParams.getFromFiles().get(0); // For CellR we ingest always one directory
        if (ingestParams.getToDir().endsWith(SLASH))
        {
            from.append(fromDir.substring(1)); // removing leading slash
        }
        else
        {
            from.append(fromDir);
        }
        return Arrays.asList(from.toString());
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
