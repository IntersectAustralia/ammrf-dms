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

package au.org.intersect.dms.wn;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import au.org.intersect.dms.core.domain.FileInfo;

/**
 * Actual connection to access the data server. Access to active, getFileInfo and getList must be synchronized by
 * clients.
 * 
 * @author user
 */
public interface TransportConnection
{

    /**
     * Gets the file list for the corresponding path. Works only if the path is a directory. If any exception is thrown
     * during retrieval, the implementation must garantee that active will return false in subsequent calls.
     * 
     * @param path
     * @return listing
     * @throws IOException
     *             If an I/O error occurs while either sending a command to the server or receiving a reply from the
     *             server.
     */
    List<FileInfo> getList(String path) throws IOException;
    
    List<FileInfo> getList(String path, String filter) throws IOException;
    
    
    /**
     * Returns info about a single path
     * @param path
     * @return
     * @throws IOException
     */
    FileInfo getInfo(String path) throws IOException;

    /**
     * Renames file/directory.
     * 
     * @param absolute
     *            path to the parent directory
     * @param from
     *            relative path to the file/directory
     * @param to
     *            new name (relative)
     * @return true if rename was successful or false otherwise
     * @throws IOException
     *             If an I/O error occurs while either sending a command to the server or receiving a reply from the
     *             server.
     */
    boolean rename(String directory, String from, String to) throws IOException;

    /**
     * Deletes file/directory. Directory must be empty otherwise method fails.
     * 
     * @param file
     *            file or directory to delete
     * @return true if deleted successfully, false otherwise
     * @throws IOException
     *             If an I/O error occurs while either sending a command to the server or receiving a reply from the
     *             server.
     */
    boolean delete(FileInfo file) throws IOException;

    /**
     * Creates directory
     * 
     * @param parent
     *            absolute path to the parent directory
     * @param name
     *            new directory name
     * @return true if creation was successful, false otherwise
     * @throws IOException
     *             If an I/O error occurs while either sending a command to the server or receiving a reply from the
     *             server.
     */
    boolean createDir(String parent, String name) throws IOException;

    /**
     * 
     * @param from
     * @return
     */
    InputStream openInputStream(String from) throws IOException;
    
    
    /**
     * Finalize the reading on the IS
     * @param from
     * @param is
     * @return
     */
    boolean closeInputStream(String from, InputStream is) throws IOException;

    /**
     * 
     * @param to
     * @return
     */
    OutputStream openOutputStream(String to, long size) throws IOException;
    
    /**
     * 
     * @param to
     * @param os
     * @return
     * @throws IOException
     */
    boolean closeOutputStream(String to, OutputStream os) throws IOException;
}
