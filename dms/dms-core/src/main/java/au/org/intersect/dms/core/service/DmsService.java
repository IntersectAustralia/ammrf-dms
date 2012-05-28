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
import au.org.intersect.dms.core.domain.JobItem;
import au.org.intersect.dms.core.domain.JobSearchResult;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;

/**
 * Client to DMS API.
 */
public interface DmsService
{

    /**
     * Return connection identifier for the given request
     * 
     * @param protocol
     * @param server
     * @param username
     * @param password
     * 
     * @return connectionId
     */
    Integer openConnection(String protocol, String server, String username, String password);

    /**
     * List directory.
     * 
     * @param connectionId
     * @param absolutePath
     * @return
     */
    List<FileInfo> getList(Integer connectionId, String absolutePath);

    /**
     * List info of a file or directory
     * 
     * @param connectionId
     * @param absolutePath
     * @return
     */
    FileInfo getFileInfo(Integer connectionId, String absolutePath);
    
    /**
     * Renames file/directory.
     * 
     * @param connectionId
     *            connection ID
     * @param from
     *            absolute path to the file/directory
     * @param to
     *            new name (relative)
     * @return true if rename was successful or false otherwise
     */
    boolean rename(Integer connectionId, String from, String to);

    /**
     * Creates directory.
     * 
     * @param connectionId
     *            connection ID
     * @param parent
     *            absolute path to the parent directory
     * @param name
     *            name of the new directory
     * @return true if creation was successful or false otherwise
     */
    boolean createDir(Integer connectionId, String parent, String name);

    /**
     * Deletes files and directories
     * 
     * @param connectionId
     *            connection ID
     * @param files
     *            files and directories to delete
     * @return true if deleted successfully, false otherwise
     */
    boolean delete(Integer connectionId, List<FileInfo> files);

    /**
     * Starts ingestion of the directory into DMS system (storage server and catalogue)
     * 
     * @param username
     *            login name of the logged-in user
     * @param projectCode
     *            project code this dataset will be linked to
     * @param metadata
     *            metadata xml to associate with the dataset
     * @param parameters
     *            ingestion params:
     *            <ul>
     *            <li>sourceConnectionId - source connection (instrument)</li>
     *            <li>sourceDir - absolute path of the source directory</li>
     *            <li>destinationConnectionId - destination connection</li>
     *            <li>targetDir - absolute path of the target directory (storage server)</li>
     *            </ul>
     * @return job ID of the created ingestion job
     */
    Long ingest(String username, Long projectCode, String metadata, IngestParameter parameters);

    /**
     * Starts copy of files and directories
     * 
     * @param sourceConnectionId
     *            source connection
     * @param sources
     *            list of absolute paths to copy
     * @param destinationConnectionId
     *            destination connection
     * @param targetDir
     *            absolute path of the target directory
     * @return job ID of the created copy job
     */
    Long copy(String username, Integer sourceConnectionId, List<String> sources, Integer destinationConnectionId,
            String targetDir);

    /**
     * Returns all jobs for a user, ordered by ID in descending order
     * 
     * @param username
     * @param startIndex
     * @param pageSize
     * @return
     */
    JobSearchResult getJobs(String username, int startIndex, int pageSize);

    /**
     * Queries a job associated with given user
     * 
     * @param username
     * @param jobId
     * @return
     */
    JobItem getJobStatus(String username, long jobId);

    /**
     * Returns a URL that would represent the given path in a connectionId
     * 
     * @param connectionId
     * @param path
     * @return
     */
    String getUrl(Integer connectionId, String path);

    /**
     * Stops a running job, returns true if job stops or false otherwise (already finished or non-existent job)
     * 
     * @param jobId
     * @return
     */
    boolean stopJob(String username, Long jobId);

    /**
     * checks if there is a worker that can complete the job that the user is attempting.
     * 
     * @param logic
     * @return
     */

    boolean checkForValidRouting(Integer connectionIdFrom, Integer connectionIdTo);
    
    /**
     * Returns the parameters used to create the connection 
     * @param connectionId
     * @return
     */
    OpenConnectionParameter getConnectionParameters(Integer connectionId);
    
}
