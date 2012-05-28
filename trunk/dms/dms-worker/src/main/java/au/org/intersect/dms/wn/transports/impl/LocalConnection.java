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

package au.org.intersect.dms.wn.transports.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.errors.PathNotFoundException;
import au.org.intersect.dms.core.errors.TransportError;
import au.org.intersect.dms.core.errors.TransportException;
import au.org.intersect.dms.core.service.BasicConnectionDetails;
import au.org.intersect.dms.wn.ConnectionParams;
import au.org.intersect.dms.wn.TransportConnection;

/**
 * Connection to local drive
 */
public class LocalConnection implements TransportConnection
{

    private static final String ROOT_PATH = "/";

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalConnection.class);

    private File dirBase;

    private ConnectionParams params;

    public LocalConnection(File rootDir, String path, String username)
    {
        params = new ConnectionParams("local", path, username, null);
        dirBase = new File(rootDir, path);
        if (!dirBase.exists())
        {
            LOGGER.error("{}/{} doesn't exist", rootDir.getAbsolutePath(), path);
            throw new TransportError("Unable to access requested path - doesn't exist");
        }
        else if (!dirBase.isDirectory())
        {
            LOGGER.error("{}/{} is not a directory", rootDir.getAbsolutePath(), path);
            throw new TransportError("Unable to access requested path - not a directory");
        }
    }

    @Override
    public FileInfo getInfo(String path) throws IOException
    {
        if (PathUtils.isRoot(path))
        {
            return FileInfo.createRootFileInfo();
        }
        String parentPath = PathUtils.getParent(path);
        File file = new File(dirBase, PathUtils.getRelative(path));
        if (file.exists())
        {
            return makeFileInfo(parentPath, file);
        }
        else
        {
            throw new PathNotFoundException(file.getAbsolutePath());
        }
    }

    @Override
    public synchronized List<FileInfo> getList(String path)
    {
        return getList(path, null);
    }

    @Override
    public List<FileInfo> getList(String path, final String filter)
    {
        File dir = PathUtils.isRoot(path) ? dirBase : new File(dirBase, PathUtils.getRelative(path));
        if (!dir.exists() || !dir.isDirectory())
        {
            throw new PathNotFoundException(path + " [using: " + dir.getAbsolutePath() + "]");
        }
        List<FileInfo> resp = new ArrayList<FileInfo>();
        File[] listing;
        if (filter == null || filter.isEmpty())
        {
            listing = dir.listFiles();
        }
        else
        {
            LOGGER.info("checking " + dir.getAbsolutePath() + " for " + filter);
            listing = dir.listFiles(new FilenameFilter()
            {

                @Override
                public boolean accept(File dir, String name)
                {
                    return name.matches(filter);
                }
            });
        }
        for (File file : listing)
        {
            resp.add(makeFileInfo(path, file));
        }
        return resp;
    }

    @Override
    public boolean rename(String directory, String from, String to)
    {
        String relPathFrom = normalizePath(directory + ROOT_PATH + from);
        File fileFrom = new File(dirBase, relPathFrom);
        if (!fileFrom.exists())
        {
            throw new PathNotFoundException();
        }
        String relPathTo = normalizePath(directory + ROOT_PATH + to);
        File fileTo = new File(dirBase, relPathTo);
        if (fileTo.exists())
        {
            throw new TransportException("Cannot rename to existing file");
        }
        return fileFrom.renameTo(fileTo);
    }

    @Override
    public boolean delete(FileInfo file)
    {
        String relPathFrom = normalizePath(file.getAbsolutePath());
        File fileOs = new File(dirBase, relPathFrom);
        if (!fileOs.exists())
        {
            throw new PathNotFoundException();
        }
        return deleteRecursive(fileOs);
    }

    private boolean deleteRecursive(File aFile)
    {
        if (aFile.isDirectory())
        {
            for (File child : aFile.listFiles())
            {
                if (".".equals(child.getName()) || "..".equals(child.getName()) || !child.exists())
                {
                    continue;
                }
                if (!deleteRecursive(child))
                {
                    return false;
                }
            }
        }
        return aFile.delete();
    }

    @Override
    public boolean createDir(String parent, String name)
    {
        String relPath = normalizePath(parent);
        File file = new File(dirBase, relPath);
        if (!file.exists())
        {
            throw new PathNotFoundException();
        }
        if (!file.isDirectory())
        {
            throw new TransportException("proposed parent is not a directory");
        }
        String relPathNew = normalizePath(parent + ROOT_PATH + name);
        File fileNew = new File(dirBase, relPathNew);
        if (fileNew.exists())
        {
            throw new TransportException("Cannot create as there is a file with same name");
        }
        return fileNew.mkdir();
    }

    /**
     * Normalize path parameter to remove leading and trailing '/'. The path must be absolute anyway, and that's checked
     * for here.
     * 
     * @param path
     * @return
     */
    private String normalizePath(String path)
    {
        if (path == null || path.length() == 0 || path.charAt(0) != '/')
        {
            throw new PathNotFoundException();
        }
        // normalize path removing trailing / if any
        String normalizedPath = path.charAt(path.length() - 1) == '/' ? path.substring(0, path.length() - 1) : path;
        return normalizedPath.length() > 0 ? normalizedPath.substring(1) : normalizedPath;
    }

    @Override
    public InputStream openInputStream(String from) throws IOException
    {
        File file = new File(dirBase, PathUtils.getRelative(from));
        LOGGER.debug("InputStream: {}", file.getAbsolutePath());
        return new FileInputStream(file);
    }

    @Override
    public boolean closeInputStream(String from, InputStream is)
    {
        try
        {
            is.close();
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    @Override
    public OutputStream openOutputStream(String to, long size) throws IOException
    {
        File file = new File(dirBase, PathUtils.getRelative(to));
        LOGGER.debug("OutputStream: {}", file.getAbsolutePath());
        return new FileOutputStream(file);
    }

    @Override
    public boolean closeOutputStream(String to, OutputStream os) throws IOException
    {
        try
        {
            os.close();
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    private FileInfo makeFileInfo(String parentPath, File file)
    {
        if (!file.exists())
        {
            throw new PathNotFoundException(file.getAbsolutePath());
        }
        FileType type = file.isDirectory() ? FileType.DIRECTORY : FileType.FILE;
        Long size = file.length();
        Date lastModified = new Date(file.lastModified());
        FileInfo info = new FileInfo(type, PathUtils.joinPath(parentPath, file.getName()), file.getName(), size,
                lastModified);
        return info;
    }

    @Override
    public BasicConnectionDetails getBasicConnectionDetails()
    {
        return params;
    }

}
