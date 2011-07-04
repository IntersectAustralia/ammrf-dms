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

import au.org.intersect.dms.core.service.dto.JobType;
import au.org.intersect.dms.service.domain.Job;

/**
 * Service to manipulate jobs. 
 *
 */
public interface JobService
{
    /**
     * Creates new job in DB
     * 
     * @param type
     * @param username
     * @param projectCode
     * @param sourceDetails
     * @param sources
     * @param destinationDetails
     * @param targetDir
     * @return
     */
    Job createJob(JobType type, String username, Long projectCode, String sourceDetails, List<String> sources,
            String destinationDetails, String targetDir);
    
    /**
     * Sets worker in the job.
     * 
     * @param jobId
     * @param workerId
     */
    void setWorker(Long jobId, int workerId);
    
    /**
     * Stores provided metadata (RIF-CS) in the job
     * 
     * @param jobId job ID
     * @param metadata RIF-CS xml
     */
    void storeMetadata(Long jobId, String metadata);
}
