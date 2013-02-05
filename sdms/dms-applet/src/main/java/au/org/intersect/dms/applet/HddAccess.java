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

package au.org.intersect.dms.applet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.FileType;

/**
 * HDD access applet
 */
public class HddAccess
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HddAccess.class);
    
    private static final String ROOT_PATH = "/";
    private static final int BUFFER_SIZE = 4096;
    
    private boolean stopped;
    private long totalBytes;
    private long oldBytes = -1;

    public FileInfo[] getList(final String path)
    {
        FileInfo[] resp;
        if (path == null || "".equals(path) || ROOT_PATH.equals(path))
        {
            resp = getRoots();
        }
        else
        {
            resp = getListDir(path);
        }
        
        LOGGER.info("Resp size:{}", resp.length);
        
        return resp;
    }

    public boolean createDir(final String parent, final String name)
    {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>()
        {

            @Override
            public Boolean run()
            {
                LOGGER.info("createDir: '{}', '{}'", parent, name);
                File parentFile = new File(parent);
                File file = new File(parentFile, name);
                return file.mkdir();
            }

        });
    }

    public boolean rename(final String parent, final String from, final String to)
    {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>()
        {

            @Override
            public Boolean run()
            {
                LOGGER.info("rename: '{}', '{}', '{}'", parent, new String[]{from, to});
                File parentFile = new File(parent);
                File file = new File(parentFile, from);
                return file.renameTo(new File(parentFile, to));
            }

        });
    }

    public boolean delete(final String path)
    {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>()
        {

            @Override
            public Boolean run()
            {
                LOGGER.info("delete: '{}'", path);
                File file = new File(path);
                return file.delete();
            }

        });
    }

    public boolean writeStream(final String path, final long size, final InputStream is)
    {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>()
        {

            @Override
            public Boolean run()
            {
                try
                {
                    LOGGER.info("writeStream: '{}', size:{}", path, size);
                    File file = new File(path);
                    FileOutputStream fos = new FileOutputStream(file);
                    return copy(is, fos) == size;
                }
                catch (FileNotFoundException e)
                {
                    throw new RuntimeException(e);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    public boolean readStream(final String path, final OutputStream os)
    {

        return AccessController.doPrivileged(new PrivilegedAction<Boolean>()
        {

            @Override
            public Boolean run()
            {
                try
                {
                    LOGGER.info("readStream: '{}'", path);
                    File file = new File(path);
                    FileInputStream fis = new FileInputStream(file);
                    copy(fis, os);
                    return true;
                }
                catch (FileNotFoundException e)
                {
                    throw new RuntimeException(e);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    private synchronized FileInfo[] getRoots()
    {
        return AccessController.doPrivileged(new PrivilegedAction<FileInfo[]>()
        {

            @Override
            public FileInfo[] run()
            {
                File[] fsvRoots = attemptFileSystemRoots();
                if (fsvRoots == null || (fsvRoots.length == 1 && ROOT_PATH.equals(fsvRoots[0].getName())))
                {
                    return getListDir(ROOT_PATH);
                }

                LOGGER.info("getRoots");
                FileInfo[] resp = new FileInfo[fsvRoots.length];
                for (int i = 0; i < fsvRoots.length; i++)
                {
                    String rootPath = fsvRoots[i].getAbsolutePath();
                    rootPath = rootPath.replace(File.separatorChar, '/');
                    if (!rootPath.startsWith(ROOT_PATH))
                    {
                        rootPath = ROOT_PATH + rootPath;
                    }
                    resp[i] = fromFile(fsvRoots[i], rootPath);
                }
                return resp;
            }
        });
    }
    
    private File[] attemptFileSystemRoots()
    {
        File[] fsvRoots = File.listRoots();
        if (fsvRoots == null)
        {
            return null;
        }
        if (fsvRoots.length == 1 || ROOT_PATH.equals(fsvRoots[0].getName()))
        {
            fsvRoots = FileSystemView.getFileSystemView().getRoots();
        }
        return fsvRoots;
    }

    private FileInfo[] getListDir(final String path)
    {
        return AccessController.doPrivileged(new PrivilegedAction<FileInfo[]>()
        {

            @Override
            public FileInfo[] run()
            {
                LOGGER.info("getListDir: '{}'", path);
                File dir = new File(path);
                if (!dir.exists() || !dir.isDirectory())
                {
                    throw new RuntimeException("Path does not exist");
                }
                List<FileInfo> resp = new ArrayList<FileInfo>();
                File[] files = dir.listFiles();
                if (files == null)
                {
                    return new FileInfo[0];
                }
                for (File file : files)
                {
                    if (".".equals(file.getName()) || "..".equals(file.getName()))
                    {
                        continue;
                    }
                    resp.add(fromFile(file, path + ROOT_PATH));
                }
                return resp.toArray(new FileInfo[resp.size()]);
            }
        });

    }

    private FileInfo fromFile(File file, String parent)
    {
        FileInfo ref = new FileInfo();
        String parentWithSlash = !parent.endsWith("/") ? parent + ROOT_PATH : parent;
        ref.setAbsolutePath(parentWithSlash + file.getName());
        ref.setFileType(file.isDirectory() ? FileType.DIRECTORY : FileType.FILE);
        String name = file.getName();
        ref.setName(name == null || name.length() == 0 ? file.getAbsolutePath() : name);
        ref.setSize(file.length());
        Date date = new Date(file.lastModified());
        ref.setModificationDate(date);
        return ref;
    }

    private long copy(InputStream is, OutputStream os) throws IOException
    {
        long numBytes = 0L;
        byte[] buffer = new byte[BUFFER_SIZE];
        int l = is.read(buffer);
        LOGGER.debug("COPYING");
        while (l > 0 && !stopped)
        {
            os.write(buffer, 0, l);
            totalBytes += l;
            numBytes += l;
            LOGGER.debug("COPYING {} bytes",numBytes);
            l = is.read(buffer);
        }
        os.close();
        is.close();
        LOGGER.info("copy {} done (stopped: {})", numBytes,stopped);
        
        return numBytes;
    }
    
    public void stop()
    {
        LOGGER.info("Stopped!");
        stopped = true;
    }

	public void stampProgress() {
		oldBytes = totalBytes;
	}

	public boolean progressed() {
		return oldBytes != totalBytes;
	}

}
