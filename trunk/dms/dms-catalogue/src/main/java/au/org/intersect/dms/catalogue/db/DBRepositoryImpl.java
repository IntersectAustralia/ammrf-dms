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

package au.org.intersect.dms.catalogue.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.dms.catalogue.Dataset;
import au.org.intersect.dms.catalogue.DatasetNotFoundException;
import au.org.intersect.dms.catalogue.DatasetSearchResult;
import au.org.intersect.dms.catalogue.MetadataItem;
import au.org.intersect.dms.catalogue.MetadataRepository;
import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.core.errors.IngestionFailedException;
import au.org.intersect.dms.util.DateFormatter;

/**
 * DB based Metadata repository.
 * 
 */
public class DBRepositoryImpl implements MetadataRepository
{

    private static final Logger LOGGER = LoggerFactory.getLogger(DBRepositoryImpl.class);

    @Autowired
    private SolrIndexFacade solrIndex;

    @Override
    @Transactional(readOnly = true, value = "catalogue")
    public Dataset findDatasetByUrl(String url)
    {
        List<DbDataset> datasets = DbDataset.findDbDatasetsByUrl(url).getResultList();
        if (datasets != null && !datasets.isEmpty())
        {
            return fromDbDataset(datasets.get(0));
        }
        throw new DatasetNotFoundException("No dataset for url " + url + " found");
    }

    @Override
    // TODO Otimze method. Create method in DbDataset to check in SQL
    public boolean isUrlCatalogued(String url)
    {
        List<DbDataset> datasets = DbDataset.findDbDatasetsByUrl(url).getResultList();
        if (datasets == null || datasets.isEmpty())
        {
            return false;
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true, value = "catalogue")
    public DatasetSearchResult findDatasets(String username, List<Long> projects, String query, int startIndex,
            int pageSize)
    {
        return solrIndex.findDatasets(username, projects, query, startIndex, pageSize);
    }

    @Override
    @Transactional(readOnly = true, value = "catalogue")
    public DatasetSearchResult findDatasets(String query, int startIndex, int pageSize)
    {
        return solrIndex.findDatasets(query, startIndex, pageSize);
    }

    @Override
    @Transactional(readOnly = true, value = "catalogue")
    public DatasetSearchResult findPublicDatasets()
    {
        List<DbDataset> datasets = DbDataset.findPublicDbDatasets();
        List<Dataset> resp = new LinkedList<Dataset>();
        for (int i = 0; i < datasets.size(); i++)
        {
            resp.add(fromDbDataset(datasets.get(i)));
        }
        DatasetSearchResult result = new DatasetSearchResult();
        result.setDatasets(resp);
        result.setTotalSize(resp.size());
        return result;
    }

    @Override
    public Map<String, List<Dataset>> findPrivateDatasets()
    {
        List<DbDataset> datasets = DbDataset.findPrivateDbDatasets();

        Map<String, List<Dataset>> result = new HashMap<String, List<Dataset>>();
        for (DbDataset dbDataset : datasets)
        {
            Dataset dataset = fromDbDataset(dbDataset);
            String owner = dataset.getOwner();
            if (result.containsKey(owner))
            {
                result.get(owner).add(dataset);
            }
            else
            {
                List<Dataset> userDatasets = new LinkedList<Dataset>();
                userDatasets.add(dataset);
                result.put(owner, userDatasets);
            }
        }
        return result;
    }

    @Override
    @Transactional(value = "catalogue", propagation = Propagation.REQUIRES_NEW, 
            rollbackFor = IngestionFailedException.class)
    public void ingest(Dataset dataset) throws IngestionFailedException
    {
        LOGGER.debug("Ingesting dataset for URL {}", dataset.getUrl());

        // TODO CHECKSTYLE-OFF: IllegalCatch
        try
        {
            checkDatasetValid(dataset);

            DbDataset dbDataset = new DbDataset();
            dbDataset.setOwner(dataset.getOwner());
            dbDataset.setUrl(dataset.getUrl());
            dbDataset.setCreationDate(DateFormatter.parseDateTime(dataset.getCreationDate()).toDate());
            dbDataset.setProjectCode(dataset.getProjectCode());
            dbDataset.setMetadata(buildMetadata(dbDataset, dataset));
            dbDataset.persist();

            LOGGER.debug("Ingested dataset {}", dbDataset.getId());
        }
        catch (Exception e)
        {
            LOGGER.error("Exception during ingest", e);
            throw new IngestionFailedException(e.getCause());
        }
        // TODO CHECKSTYLE-ON: IllegalCatch

    }

    @Override
    @Transactional("catalogue")
    public void updateDataset(Dataset dataset)
    {
        LOGGER.debug("Updating dataset {} for URL {}", dataset.getId(), dataset.getUrl());
        DbDataset dbDataset = DbDataset.findDbDataset(Long.valueOf(dataset.getId()));
        for (DbMetadataItem dbMetadataItem : dbDataset.getMetadata())
        {
            MetadataSchema schema = dbMetadataItem.getMetadataSchema();
            if (isEditableSchema(schema))
            {
                dbMetadataItem.setMetadata(dataset.getMetadata(schema));
            }
        }
        dbDataset.setProjectCode(dataset.getProjectCode());
        dbDataset.setOwner(dataset.getOwner());
        dbDataset.merge();
        DbDataset.indexDbDataset(dbDataset);
    }

    private Dataset fromDbDataset(DbDataset dbDataset)
    {
        Dataset dataset = new Dataset();
        dataset.setId(dbDataset.getId().toString());
        dataset.setUrl(dbDataset.getUrl());
        dataset.setCreationDate(dbDataset.getCreationDate());
        dataset.setOwner(dbDataset.getOwner());
        dataset.setProjectCode(dbDataset.getProjectCode());
        dataset.setVisible(dbDataset.getVisible() == 1);

        List<MetadataItem> metadata = new LinkedList<MetadataItem>();
        for (DbMetadataItem dbMetadataItem : dbDataset.getMetadata())
        {
            MetadataItem metadataItem = new MetadataItem();
            metadataItem.setSchema(dbMetadataItem.getMetadataSchema());
            metadataItem.setMetadata(dbMetadataItem.getMetadata());
            metadata.add(metadataItem);
        }
        dataset.setMetadata(metadata);
        return dataset;
    }

    private List<DbMetadataItem> buildMetadata(DbDataset dbDataset, Dataset dataset)
    {
        List<DbMetadataItem> metadata = new LinkedList<DbMetadataItem>();
        List<MetadataItem> items = dataset.getMetadata();
        for (MetadataItem item : items)
        {
            DbMetadataItem dbItem = new DbMetadataItem();
            dbItem.setDataset(dbDataset);
            dbItem.setUrl(dbDataset.getUrl());
            dbItem.setMetadataSchema(item.getSchema());
            dbItem.setMetadata(item.getMetadata());
            metadata.add(dbItem);
        }
        return metadata;
    }

    @Override
    @Transactional(value = "catalogue")
    public boolean makePublic(String url)
    {
        changeVisible(url, 1);
        return true;
    }

    @Override
    @Transactional(value = "catalogue")
    public boolean makePrivate(String url)
    {
        changeVisible(url, 0);
        return true;
    }

    private void changeVisible(String url, int value)
    {
        DbDataset dbDataset = DbDataset.findDbDatasetsByUrl(url).getSingleResult();
        dbDataset.setVisible(value);
        dbDataset.merge();
    }

    @Override
    public boolean isPublicSchema(MetadataSchema schema)
    {
        return MetadataSchema.RIF_CS.equals(schema);
    }

    @Override
    public boolean isEditableSchema(MetadataSchema schema)
    {
        return MetadataSchema.RIF_CS == schema;
    }

    @Override
    public boolean isRequiredSchema(MetadataSchema schema)
    {
        return MetadataSchema.RIF_CS == schema;
    }

    private void checkDatasetValid(Dataset dataset)
    {
        Set<MetadataSchema> requiredSchemas = getRequiredSchemas();
        Set<MetadataSchema> datasetSchemas = dataset.getSchemas();
        requiredSchemas.removeAll(datasetSchemas);
        if (!requiredSchemas.isEmpty())
        {
            throw new IllegalArgumentException("Dataset " + dataset.getUrl()
                    + " doesn't contain metadata for required schemas " + requiredSchemas);
        }
    }

    private Set<MetadataSchema> getRequiredSchemas()
    {
        Set<MetadataSchema> requiredSchemas = new HashSet<MetadataSchema>();
        for (MetadataSchema schema : MetadataSchema.values())
        {
            if (isRequiredSchema(schema))
            {
                requiredSchemas.add(schema);
            }
        }
        return requiredSchemas;
    }

}
