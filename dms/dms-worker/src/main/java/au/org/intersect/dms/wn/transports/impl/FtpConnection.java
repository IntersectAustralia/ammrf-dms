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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.errors.NotAuthorizedError;
import au.org.intersect.dms.core.errors.PathNotFoundException;
import au.org.intersect.dms.core.errors.TransportError;
import au.org.intersect.dms.core.errors.TransportException;
import au.org.intersect.dms.wn.TransportConnection;

/**
 * Connection to FTP servers.
 */
public class FtpConnection implements TransportConnection
{

    private static final Logger LOGGER = LoggerFactory.getLogger(FtpConnection.class);

    private FTPClient client;

    protected FtpConnection()
    {
    }

    public FtpConnection(String server, String username, String password)
    {
        LOGGER.debug("ftp connect to server:{} using username:{} and password:{}", new String[] {server, username,
            password});
        boolean ok = false;
        try
        {
            client = new FTPClient();
            client.connect(server);
            client.enterLocalPassiveMode();
            if (!FTPReply.isPositiveCompletion(client.getReplyCode()))
            {
                throw new TransportError();
            }
            if (!client.login(username, password))
            {
                throw new NotAuthorizedError();
            }
            client.setFileType(FTP.BINARY_FILE_TYPE);
            ok = true;
        }
        catch (SocketException e)
        {
            throw new TransportError(e);
        }
        catch (IOException e)
        {
            throw new TransportError(e);
        }
        finally
        {
            if (!ok && client != null)
            {
                try
                {
                    client.disconnect();
                }
                catch (IOException e)
                {
                    // swallows exception
                }
            }
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
        String namePart = PathUtils.getName(path);
        changeWorkingDirectory(parentPath);
        for (FTPFile file : client.listFiles())
        {
            if (file.getName().equals(namePart))
            {
                return makeFileInfo(parentPath, file);
            }
        }
        throw new PathNotFoundException("Couldn't retrieve information for '" + path + "'");
    }
    
    @Override
    public List<FileInfo> getList(String path) throws IOException
    {
        assert client != null;

        List<FileInfo> resp = new ArrayList<FileInfo>();
        changeWorkingDirectory(path);
        for (FTPFile info : client.listFiles())
        {
            resp.add(makeFileInfo(path, info));
        }
        return resp;

    }
    
    @Override
    public List<FileInfo> getList(String path, String filter) throws IOException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean rename(String directory, String from, String to) throws IOException
    {
        changeWorkingDirectory(directory);
        return client.rename(from, to);
    }

    private void changeWorkingDirectory(String path) throws IOException
    {
        if (!client.changeWorkingDirectory(path))
        {
            throw new PathNotFoundException("Path " + path + " not found");
        }
    }

    @Override
    public boolean delete(FileInfo file) throws IOException
    {
        if (FileType.FILE == file.getFileType())
        {
            LOGGER.debug("Deleting file", file.getAbsolutePath());
            return client.deleteFile(file.getAbsolutePath());
        }
        else
        {
            LOGGER.debug("Deleting directory", file.getAbsolutePath());
            return client.removeDirectory(file.getAbsolutePath());
        }
    }

    public void close() throws IOException
    {
        client.disconnect();
        client = null;
    }

    public void noop() throws IOException
    {
        client.noop();
    }

    @Override
    public boolean createDir(String parent, String name) throws IOException
    {
        changeWorkingDirectory(parent);
        return client.makeDirectory(name);

    }

    @Override
    public InputStream openInputStream(String from) throws IOException
    {
        InputStream is = client.retrieveFileStream(from);
        if (is == null)
        {
            throw new TransportException("FTP create read stream '" + from + "' failed");
        }
        return is;
    }

    @Override
    public OutputStream openOutputStream(String to, long size) throws IOException
    {
        OutputStream os = client.storeFileStream(to);
        if (os == null)
        {
            throw new TransportException("FTP create write stream '" + to + "' failed");
        }
        return os;
    }

    @Override
    public boolean closeInputStream(String from, InputStream is) throws IOException
    {
        is.close();
        return client.completePendingCommand();
    }

    @Override
    public boolean closeOutputStream(String to, OutputStream os) throws IOException
    {
        os.close();
        return client.completePendingCommand();
    }
    
    private FileInfo makeFileInfo(String parentPath, FTPFile info)
    {
        Date date = info.getTimestamp().getTime();
        int ftpType = info.getType();
        if (ftpType == FTPFile.SYMBOLIC_LINK_TYPE)
        {
            try
            {
                changeWorkingDirectory(PathUtils.joinPath(parentPath, info.getName()));
                ftpType = FTPFile.DIRECTORY_TYPE;
            }
            catch (IOException e)
            {
                throw new TransportException("Cannot get list directory (" + info.getName() + ")");
            }
            catch (PathNotFoundException e)
            {
                ftpType = FTPFile.FILE_TYPE;
            }
        }
        FileType type = ftpType == FTPFile.DIRECTORY_TYPE ? FileType.DIRECTORY : FileType.FILE;
        FileInfo item = new FileInfo(type, PathUtils.joinPath(parentPath, info.getName()),
                info.getName(), info.getSize(), date);
        return item;
    }

}
