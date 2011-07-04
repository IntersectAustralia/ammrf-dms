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

package au.org.intersect.dms.catalogue;

import java.util.List;
import java.util.Map;

import au.org.intersect.dms.core.catalogue.MetadataSchema;

/**
 * Metadata repository.
 * 
 * @version $Rev: 5 $
 * 
 */
public interface MetadataRepository
{
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
    DatasetSearchResult findDatasets(String username, List<Long> userProjects, String query, int startIndex,
            int pageSize);
    
    /**
     * Returns all datasets. This method should be used if logged-in user is admin
     * 
     * @param query
     * @param startIndex
     * @param pageSize
     * @return
     */
    DatasetSearchResult findDatasets(String query, int startIndex, int pageSize);

    /**
     * Returns all public (published) datasets
     * 
     * @return
     */
    DatasetSearchResult findPublicDatasets();
    
    /**
     * Returns all private (not published) datasets
     * 
     * @return
     */
    Map<String,  List<Dataset>> findPrivateDatasets();

    /**
     * Returns dataset associated with this URL. 
     * Throws {@link DatasetNotFoundException} if no matching dataset was found
     * 
     * @param url
     *            dataset URL
     * @return dataset or throws {@link DatasetNotFoundException} if no matching dataset was found
     */
    Dataset findDatasetByUrl(String url);
    
    boolean isUrlCatalogued(String url);

    void ingest(Dataset dataset) throws IngestionFailedException;
    
    void updateDataset(Dataset dataset);

    boolean makePublic(String url);

    boolean makePrivate(String url);

    boolean isPublicSchema(MetadataSchema schema);
    
    boolean isEditableSchema(MetadataSchema schema);
    
    boolean isRequiredSchema(MetadataSchema schema);
}
