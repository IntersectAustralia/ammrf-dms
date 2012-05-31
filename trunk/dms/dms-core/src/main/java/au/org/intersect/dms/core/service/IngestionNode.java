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

package au.org.intersect.dms.core.service;

import java.util.List;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.service.dto.CopyParameter;
import au.org.intersect.dms.core.service.dto.GetListParameter;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;

/**
 * Internal interface to explicitly copy and harvest
 * 
 * @version $Rev: 29 $
 */
public interface IngestionNode
{
    /**
     * Needed to be able to monitor a storage
     * 
     * @param getListParams
     * @param filter
     * @return
     */
    List<FileInfo> getList(GetListParameter getListParams, String filter);

    /**
     * Return the give connection details (except password) for a connectionId
     * 
     * @param connectionId
     * @return
     */
    OpenConnectionParameter getConnectionDetails(Integer connectionId);

    /**
     * Actual task of ingesting data
     * 
     * @param copyParams
     * @param instrumentProfile
     */
    void copyAndOptionallyHarvest(final CopyParameter copyParams, final InstrumentProfile instrumentProfile);

    /**
     * Ingest in place without copy
     * 
     * @param ingestParams
     * @param instrumentProfile
     */
    void harvestWithoutCopy(final IngestParameter ingestParams, final InstrumentProfile instrumentProfile);

    /**
     * Abort an ingest from the file-watcher
     * 
     * @param jobId
     * @param aborted
     */
    void stopIngest(Long jobId, JobStatus aborted);

    /**
     * Opens a connection to the given server, using the protocol identified by protocol, with username and password as
     * described. Returns a connection identifier that must be used for subsequent requests.
     * 
     * @param openConnectionParams
     *            TODO
     * 
     * @return
     * 
     * @throws ConnectionOpenException
     */
    Integer openConnection(OpenConnectionParameter openConnectionParams);

}
