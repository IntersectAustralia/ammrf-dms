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

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.solr.RooSolrSearchable;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.scheduling.annotation.Async;

/**
 * Dataset stored in DB.
 */
@RooJavaBean
@RooToString
@RooEntity(finders = {"findDbDatasetsByUrl"}, persistenceUnit = "cataloguePU", table = "dataset")
@RooSolrSearchable
public class DbDataset
{

    private static final Logger LOGGER = LoggerFactory.getLogger(DbDataset.class);

    private String owner;

    private Date creationDate;

    @Column(unique = true, nullable = false)
    private String url;

    @Column(nullable = false)
    private int visible;

    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<DbMetadataItem> metadata;

    private Long projectCode;

    @Autowired
    private transient DatasetIndexer indexer;

    public int getVisible()
    {
        return visible;
    }

    public void setVisible(int visible)
    {
        this.visible = visible;
    }

    public static List<DbDataset> findDbDatasetsByOwner(String owner, int firstResult, int maxResults)
    {
        return entityManager()
                .createQuery("select dataset from DbDataset dataset where dataset.owner = :owner", DbDataset.class)
                .setParameter("owner", owner).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public static long countDbDatasetsByOwner(String owner)
    {
        return entityManager()
                .createQuery("select count(dataset) from DbDataset dataset where dataset.owner = :owner", Long.class)
                .setParameter("owner", owner).getSingleResult();
    }

    public static List<DbDataset> findPublicDbDatasets()
    {
        return entityManager().createQuery("select dataset from DbDataset dataset where dataset.visible = 1",
                DbDataset.class).getResultList();
    }

    public static List<DbDataset> findPrivateDbDatasets()
    {
        return entityManager().createQuery("select dataset from DbDataset dataset where dataset.visible = 0",
                DbDataset.class).getResultList();
    }

    @Async
    public static void indexDbDatasets(Collection<DbDataset> datasets)
    {
        LOGGER.debug("Bulk indexing {} datasets", datasets.size());
        new DbDataset().indexer.indexDatasets(datasets);
    }

    @Async
    public static void reindexAllDbDatasets()
    {
        LOGGER.info("Reindexing all datasets");
        try
        {
            solrServer().deleteByQuery("*:*");
            indexDbDatasets(DbDataset.findAllDbDatasets());
        }
        catch (SolrServerException e)
        {
            LOGGER.error("Exception during indexing", e);
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            LOGGER.error("Exception during indexing", e);
            throw new RuntimeException(e);
        }
    }
}
