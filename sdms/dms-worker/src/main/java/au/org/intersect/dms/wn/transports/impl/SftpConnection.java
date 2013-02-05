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
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.errors.PathNotFoundException;
import au.org.intersect.dms.core.errors.TransportError;
import au.org.intersect.dms.core.service.BasicConnectionDetails;
import au.org.intersect.dms.wn.ConnectionParams;
import au.org.intersect.dms.wn.TransportConnection;

/**
 * Connection to SFTP servers.
 */
public class SftpConnection implements TransportConnection
{

    private static final Logger LOGGER = LoggerFactory.getLogger(SftpConnection.class);

    private SFTPClient sftpClient;
    private ConnectionParams params;

    public SftpConnection(String server, String username, String sshKey, String knownHosts)
    {
        LOGGER.debug("sftp connect to server:{} using username:{}", new String[] {server, username});
        final SSHClient ssh = new SSHClient();
        boolean ok = false;
        params = new ConnectionParams("sftp", server, username, null);

        try
        {
            ssh.loadKnownHosts(new File(knownHosts));
            ssh.connect(server);
            ssh.authPublickey(username, sshKey);
            sftpClient = ssh.newSFTPClient();
            ok = true;
        }
        catch (IOException e)
        {
            LOGGER.error("SftpConnection cannot establish connection to SFTP server", e);
            throw new TransportError(e);
        }
        finally
        {
            try
            {
                if (!ok && sftpClient != null)
                {
                    sftpClient.close();
                }
                if (!ok && ssh.isConnected())
                {
                    ssh.disconnect();
                }

            }
            catch (IOException e)
            {
                // Transport exception already thrown
                LOGGER.error("Could not close SFTP connection", e);
            }
        }
    }

    @Override
    public List<FileInfo> getList(String path) throws IOException
    {
        LOGGER.debug("SftpConnection.getList(path)");
        assert sftpClient != null;
        List<FileInfo> resp = new ArrayList<FileInfo>();

        for (RemoteResourceInfo file : sftpClient.ls(path))
        {
            resp.add(makeFileInfo(path, file));
        }
        return resp;
    }

    @Override
    public List<FileInfo> getList(String path, String filter) throws IOException
    {
        LOGGER.debug("SftpConnection.getList(path, filter)");
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public FileInfo getInfo(String path) throws IOException
    {
        LOGGER.debug("getInfo(path)");
        if (PathUtils.isRoot(path))
        {
            return FileInfo.createRootFileInfo();
        }
        String parentPath = PathUtils.getParent(path);
        String namePart = PathUtils.getName(path);
        for (RemoteResourceInfo file : sftpClient.ls(parentPath))
        {
            if (file.getName().equals(namePart))
            {
                return makeFileInfo(parentPath, file);
            }
        }
        throw new PathNotFoundException("Couldn't retrieve information for '" + path + "'");
    }

    private FileInfo makeFileInfo(String parentPath, RemoteResourceInfo info)
    {
        Date date = new Date(info.getAttributes().getMtime());
        FileType type = info.isDirectory() ? FileType.DIRECTORY : FileType.FILE;
        FileInfo item = new FileInfo(type, PathUtils.joinPath(parentPath, info.getName()), info.getName(), info
                .getAttributes().getSize(), date);
        return item;
    }

    @Override
    public boolean rename(String directory, String from, String to) throws IOException
    {
        try
        {
            sftpClient.rename(directory + from, directory + to);
            return true;
        }
        catch (IOException e)
        {
            LOGGER.error("Cannot move {} in directory {} to {}", new String[] {from, directory, to}, e);
            return false;
        }
    }

    @Override
    public boolean delete(FileInfo file) throws IOException
    {
        try
        {
            if (FileType.FILE == file.getFileType())
            {
                LOGGER.debug("Removing sftp file", file.getAbsolutePath());
                sftpClient.rm(file.getAbsolutePath());
            }
            else
            {
                LOGGER.debug("Removing sftp directory", file.getAbsolutePath());
                sftpClient.rmdir(file.getAbsolutePath());
            }
            return true;
        }
        catch (IOException e)
        {
            LOGGER.info("Cannot delete entry " + file.getAbsolutePath(), e);
            return false;
        }
    }

    @Override
    public boolean createDir(String parent, String name) throws IOException
    {
        try
        {
            LOGGER.debug("Creating directory", parent + name);
            sftpClient.mkdir(parent + "/" + name);
            return true;
        }
        catch (IOException e)
        {
            LOGGER.info("Cannot create directory {} under {}", new String[] {parent, name}, e);
            return false;
        }
    }

    @Override
    public InputStream openInputStream(String from) throws IOException
    {
        RemoteFile remoteFile = sftpClient.open(from, EnumSet.of(OpenMode.READ));
        return remoteFile.getInputStream();
    }

    @Override
    public boolean closeInputStream(String from, InputStream is) throws IOException
    {
        is.close();
        return true;
    }

    @Override
    public OutputStream openOutputStream(String to, long size) throws IOException
    {
        RemoteFile remoteFile = sftpClient.open(to, EnumSet.of(OpenMode.CREAT, OpenMode.WRITE));
        return remoteFile.getOutputStream();
    }

    @Override
    public boolean closeOutputStream(String to, OutputStream os) throws IOException
    {
        // TODO Auto-generated method stub
        os.close();
        return true;
    }

    @Override
    public BasicConnectionDetails getBasicConnectionDetails()
    {
        return params;
    }

    public void close() throws IOException
    {
        sftpClient.close();
        sftpClient = null;
    }

}
