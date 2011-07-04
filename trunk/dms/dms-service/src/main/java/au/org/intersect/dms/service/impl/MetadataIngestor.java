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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.dms.catalogue.Dataset;
import au.org.intersect.dms.catalogue.IngestionFailedException;
import au.org.intersect.dms.catalogue.MetadataItem;
import au.org.intersect.dms.catalogue.MetadataRepository;
import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.service.dto.MetadataEventItem;
import au.org.intersect.dms.service.domain.Job;
import au.org.intersect.dms.service.domain.JobDetailMetadata;

/**
 * Stores metadata received from worker node in the DB (temp table) and handles subsequent ingestion into the metadata
 * catalogue.
 * 
 */
public class MetadataIngestor
{

    private static final long SLEEP = 30000L;

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataIngestor.class);

    @Autowired
    private MetadataRepository metadataRepository;

    @Transactional("service")
    public void saveMetadata(Job job, List<MetadataEventItem> metadata)
    {
        LOGGER.debug("Saving metadata for job {}", job.getId());
        List<Map<String, Object>> files = new ArrayList<Map<String, Object>>();
        for (MetadataEventItem item : metadata)
        {
            if (item.getSchema() != null)
            {
                JobDetailMetadata jobMetadata = new JobDetailMetadata();
                jobMetadata.setJob(job);
                jobMetadata.setUrl(item.getUrl());
                MetadataSchema schema = item.getSchema();
                jobMetadata.setMetadataSchema(schema);
                if (MetadataSchema.EMPTY != schema)
                {
                    jobMetadata.setMetadata(item.getMetadata());
                }
                jobMetadata.persist();
            }
            else
            {
                Map<String, Object> itemMap = newItemMap(item.getUrl(), item.getType(), item.getSize());
                files.add(itemMap);
            }
        }
        if (files.size() > 0)
        {
            JobDetailMetadata jobMetadata = new JobDetailMetadata();
            jobMetadata.setJob(job);
            jobMetadata.setUrl(job.getDestinationUrl());
            jobMetadata.setMetadataSchema(MetadataSchema.FILES_LIST);
            jobMetadata.setMetadata(MetadataFileList.makeFileList(files));
            jobMetadata.persist();
        }
    }

    @Transactional(value = "service")
    public void ingest(Job job) throws IngestionFailedException
    {
        LOGGER.info("Injesting metadata for job {}", job.getId());
        metadataRepository.ingest(datasetFromJob(job));

        LOGGER.debug("Metadata for job {} ingested successfully. Deleting temp metadata.", job.getId());
        List<JobDetailMetadata> jobMetadata = JobDetailMetadata.findJobDetailMetadatasByJob(job).getResultList();
        for (JobDetailMetadata jobDetailMetadata : jobMetadata)
        {
            jobDetailMetadata.remove();
        }
    };

    private Dataset datasetFromJob(Job job)
    {
        Dataset dataset = new Dataset();
        dataset.setOwner(job.getDmsUser().getUsername());
        dataset.setUrl(job.getDestinationUrl());
        dataset.setCreationDate(new Date(job.getFinishedTimeStamp()));
        dataset.setProjectCode(job.getProjectCode());
        dataset.setMetadata(buildMetadata(dataset, job));
        return dataset;
    }

    private Map<String, Object> newItemMap(String url, FileType type, Long size)
    {
        Map<String, Object> item = new HashMap<String, Object>();
        item.put("url", url);
        item.put("type", type.toString());
        item.put("size", size != null ? Long.toString(size) : "0");
        return item;
    }

    private List<MetadataItem> buildMetadata(Dataset dataset, Job job)
    {
        List<JobDetailMetadata> jobMetadata = JobDetailMetadata.findJobDetailMetadatasByJob(job).getResultList();
        List<MetadataItem> metadata = new LinkedList<MetadataItem>();
        for (JobDetailMetadata jobDetailMetadata : jobMetadata)
        {
            MetadataItem item = new MetadataItem();
            item.setSchema(jobDetailMetadata.getMetadataSchema());
            item.setMetadata(jobDetailMetadata.getMetadata());
            metadata.add(item);
        }
        return metadata;
    }

}