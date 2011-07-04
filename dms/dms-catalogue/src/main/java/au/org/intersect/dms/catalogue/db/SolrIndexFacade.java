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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import au.org.intersect.dms.catalogue.Dataset;
import au.org.intersect.dms.catalogue.DatasetSearchResult;

/**
 * Facade to query Solr index for data
 *
 * @version $Rev: 29 $
 */
public class SolrIndexFacade
{
    
    public DatasetSearchResult findDatasets(String query, int startIndex, int pageSize)
    {
        return findDatasets("*", null, query, startIndex, pageSize);
    }
    
    /**
     * Returns all datasets owned by the user matching specified full text search query
     * 
     * @param username
     *            username of the owner in DMS system
     * @param userProjects
     *            list of project codes from booking system this username belongs to
     * @param query
     *            full text search query
     * @param startIndex
     *            index of the first dataset to display on the curect page
     * @param pageSize
     *            max number of datasets to display on each page
     * @return datasets of this user (one page)
     */
    public DatasetSearchResult findDatasets(String username, List<Long> projects, String query, int startIndex,
            int pageSize)
    {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setFields("dataset.id_l");
        solrQuery.setStart(startIndex);
        solrQuery.setRows(pageSize);
        StringBuilder queryString = new StringBuilder();
        if (query == null || "".equals(query))
        {
            queryString.append("dataset.metadata_t:*");
        }
        else
        {
            queryString.append(query);
        }
        queryString.append(" AND (dataset.owner_s:").append(username);

        String projectQuery = buildProjectCriteria(projects);
        if (!projectQuery.isEmpty())
        {
            queryString.append(" OR ").append(projectQuery);
        }
        queryString.append(")");

        solrQuery.setQuery(queryString.toString());
        QueryResponse solrResponse = DbDataset.search(solrQuery);
        SolrDocumentList docs = solrResponse.getResults();

        List<Dataset> datasets = new LinkedList<Dataset>();
        if (docs != null)
        {
            for (SolrDocument solrDocument : docs)
            {
                Long datasetId = (Long) solrDocument.getFieldValue("dataset.id_l");
                DbDataset dbDataset = DbDataset.findDbDataset(datasetId);
                if (dbDataset != null)
                {
                    Dataset dataset = new Dataset();
                    dataset.setId(dbDataset.getId().toString());
                    dataset.setUrl(dbDataset.getUrl());
                    dataset.setOwner(dbDataset.getOwner());
                    dataset.setCreationDate(dbDataset.getCreationDate());
                    datasets.add(dataset);
                }
            }
        }
        DatasetSearchResult result = new DatasetSearchResult();
        result.setDatasets(datasets);
        result.setTotalSize(docs != null ? docs.getNumFound() : 0);
        return result;
    }
    
    private String buildProjectCriteria(List<Long> projects)
    {
        StringBuilder query = new StringBuilder();
        if (projects != null && !projects.isEmpty())
        {
            query.append("dataset.project_l:(");
            for (Iterator<Long> iterator = projects.iterator(); iterator.hasNext();)
            {
                Long projectCode = iterator.next();
                query.append(projectCode);
                if (iterator.hasNext())
                {
                    query.append(" OR ");
                }
            }
            query.append(")");
        }
        return query.toString();
    }
    
}
