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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.scheduling.annotation.Scheduled;

import au.org.intersect.dms.catalogue.MetadataRepository;
import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.service.ConfigurationService;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;
import au.org.intersect.dms.util.URLBuilder;

/**
 * Base abstract class to poll directory (recursively) on the main server for new data (FV1000 and TIRF).
 * 
 */
public abstract class RepositoryServerPoller
{
    protected static final Logger LOGGER = LoggerFactory.getLogger(RepositoryServerPoller.class);

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private DmsService dmsService;
    
    @Autowired
    private MetadataRepository metadataRepository;
    
    private InplaceIngestionJobCreator jobCreator;

    /**
     * Stock server of the repository.
     */
    private Long repositoryId;

    private String rootDirectory;

    @Required
    public void setRepository(Long repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    @Required
    public void setRootDirectory(String rootDirectory)
    {
        this.rootDirectory = rootDirectory;
    }
    
    @Required
    public void setJobCreator(InplaceIngestionJobCreator jobCreator)
    {
        this.jobCreator = jobCreator;
    }

    protected DmsService getDmsService()
    {
        return dmsService;
    }
    
    protected MetadataRepository getMetadataRepository()
    {
        return metadataRepository;
    }
    
    @Scheduled(cron = "${dms.olympus.schedule}")
    public void ingestNewDatasets()
    {
        Collection<DatasetParams> newDatasets = getNewDatasets();
        if (!newDatasets.isEmpty())
        {
            jobCreator.createJobs(newDatasets);
        }
    }
    
    protected Collection<DatasetParams> getNewDatasets()
    {
        LOGGER.info("Checking for new datasets in directory {}....", rootDirectory);

        Set<DatasetParams> newDatasets = new HashSet<DatasetParams>();

        OpenConnectionParameter repositoryConnectionParams = configurationService
                .getServerConnectionParameters(repositoryId);

        Integer connectionId = dmsService.openConnection(repositoryConnectionParams.getProtocol(),
                repositoryConnectionParams.getServer(), repositoryConnectionParams.getUsername(),
                repositoryConnectionParams.getPassword());

        List<FileInfo> usersDirs = dmsService.getList(connectionId, rootDirectory);

        for (FileInfo fileInfo : usersDirs)
        {
            String username = fileInfo.getName();
            processDirectory(username, repositoryConnectionParams, connectionId, fileInfo, newDatasets);
        }

        LOGGER.info("Found {} new datasets:{}", newDatasets.size(), newDatasets);

        return newDatasets;
    }

    private void processDirectory(String username, OpenConnectionParameter repositoryConnectionParams,
            Integer connectionId, FileInfo directory, Collection<DatasetParams> newDatasetsAccumulator)
    {
        LOGGER.trace("Checking directory {}", directory.getAbsolutePath());
        List<FileInfo> directoryListing = dmsService.getList(connectionId, directory.getAbsolutePath());
        for (FileInfo fileInfo : directoryListing)
        {
            String url = URLBuilder.buildURL(repositoryConnectionParams, fileInfo);
            if (FileType.FILE == fileInfo.getFileType())
            {
                DatasetParams datasetParams = processFile(username, directory, fileInfo, url);
                if (datasetParams != null)
                {
                    newDatasetsAccumulator.add(datasetParams);
                }
            }
            else if (!fileInfo.getName().toLowerCase().endsWith(".oif.files")
                    && !metadataRepository.isUrlCatalogued(url))
            {
                processDirectory(username, repositoryConnectionParams, connectionId, fileInfo, newDatasetsAccumulator);
            }

        }
    }

    protected abstract DatasetParams processFile(String username, FileInfo directory, FileInfo file, String fileURL);

}
