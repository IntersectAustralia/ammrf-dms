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

package au.org.intersect.dms.service;

import java.util.List;

import au.org.intersect.dms.service.domain.Job;
import au.org.intersect.dms.service.domain.JobDetailMetadata;

/**
 * Converts metadata stored in the ingest job into format suitable for ingestion into metadata repository.
 * 
 * @version $Rev: 5 $
 * 
 */
public interface MetadataBuilder
{
    /**
     * Returns dataset XML derived from metadata stored in the job
     * 
     * @param job
     *            copy job
     * @param metadata
     *            metadata for this job
     * @param datasetId
     *            ID to assign to this dataset
     * @return XML in the format ready to ingest into repository
     */
    String buildMetadata(Job job, List<JobDetailMetadata> metadata, String datasetId);
}
