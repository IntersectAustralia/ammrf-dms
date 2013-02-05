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
import au.org.intersect.dms.core.service.dto.CopyParameter;
import au.org.intersect.dms.core.service.dto.CreateDirectoryParameter;
import au.org.intersect.dms.core.service.dto.DeleteParameter;
import au.org.intersect.dms.core.service.dto.GetFileInfoParameter;
import au.org.intersect.dms.core.service.dto.GetListParameter;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;
import au.org.intersect.dms.core.service.dto.RenameParameter;

/**
 * Interface to the underlying servers. Abstracts actual data server and protocol. 
 */
public interface WorkerNode
{

    Integer HDD_CONNECTION_ID = -1;
    /**
     * Opens a connection to the given server, using the protocol identified by protocol, with username and password as
     * described. Returns a connection identifier that must be used for subsequent requests.
     * @param openConnectionParams TODO
     * 
     * @return
     * 
     * @throws ConnectionOpenException
     */
    Integer openConnection(OpenConnectionParameter openConnectionParams);

    /**
     * Closes an existing connection in this worker. No other clients can re-use it.
     * @param connectionId
     */
    void closeConnection(Integer connectionId);
    

    /**
     * Return the give connection details (except password) for a connectionId
     * @param connectionId
     * @return
     */
    OpenConnectionParameter getConnectionDetails(Integer connectionId);

    /**
     * Returns FileInfo for given path. All paths are relative to the root directory, i.e. 'dir1/obj1' is equivalent to
     * '/dir1/obj1'.
     * @param getListParams TODO
     * 
     * @return
     * 
     * @throws ConnectionGoneException
     * @throws au.org.intersect.dms.core.errors.TransportError
     * @throws au.org.intersect.dms.core.errors.PathNotFoundException
     * @throws ConnectionOpenException
     */
    List<FileInfo> getList(GetListParameter getListParams);
    
    /**
     * Needed to be able to monitor a storage
     * 
     * @param getListParams
     * @param filter
     * @return
     */
    List<FileInfo> getList(GetListParameter getListParams, String filter);

    /**
     * Returns FileInfo for given path. The path is relative to the root directory, i.e. 'dir1/obj1' is equivalent to
     * '/dir1/obj1'.
     * @param getListParams 
     * 
     * @return
     * 
     * @throws ConnectionGoneException
     * @throws au.org.intersect.dms.core.errors.TransportError
     * @throws au.org.intersect.dms.core.errors.PathNotFoundException
     * @throws ConnectionOpenException
     */
    FileInfo getFileInfo(GetFileInfoParameter getFileInfoParams);
    
    /**
     * Renames file/directory.
     * @param renameParams TODO
     * 
     * @return true if rename was successful or false otherwise
     * 
     * @throws au.org.intersect.dms.core.errors.PathNotFoundException
     * @throws au.org.intersect.dms.core.errors.ConnectionClosedError 
     */
    boolean rename(RenameParameter renameParams);
    
    /**
     * Deletes files and directories
     * @param deleteParams TODO
     * 
     * @return true if deleted successfully, false otherwise
     * 
     * @throws au.org.intersect.dms.core.errors.PathNotFoundException
     * @throws au.org.intersect.dms.core.errors.ConnectionClosedError
     */
    boolean delete(DeleteParameter deleteParams);
    
    /**
     * Creates directory.
     * @param createDirectoryparams TODO
     * 
     * @return true if creation was successful or false otherwise
     * 
     * @throws au.org.intersect.dms.core.errors.PathNotFoundException
     * @throws au.org.intersect.dms.core.errors.ConnectionClosedError
     */
    boolean createDir(CreateDirectoryParameter createDirectoryparams);
    
    /**
     * Starts copy of files and directories
     * @param copyParams TODO
     */
    void copy(CopyParameter copyParams);
    
    /**
     * Stops a running job
     */
    boolean stopJob(Long jobId);
    
    /**
     * Adds a event listener to the queue. Events are sent in the same thread as the IO thread, so implementations
     * should avoid lengthy processes when called.
     * @param listener
     */
    void addEventListener(WorkerEventListener listener);
    
    /**
     * Removes an existing listener; if it is not found returns silently.
     * @param listener
     */
    void removeEventListener(WorkerEventListener listener);
    
}
