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
import java.io.FileReader;
import java.io.BufferedReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

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
    private Session session;
    private ChannelSftp channel;
    private ConnectionParams params;

    public SftpConnection(String server, String username, String sshKey, String knownHosts)
    {
        JSch.setLogger(new JschLogger());
        final JSch jsch=new JSch();
        boolean ok = false;
        params = new ConnectionParams("sftp", server, username, null);

        try
        {
            LOGGER.debug("sftp setup");
            jsch.setKnownHosts(knownHosts);
            jsch.addIdentity(sshKey);
            LOGGER.debug("starting ssh session to server:{} using username:{}", new String[] {server, username});
            session = jsch.getSession(username, server);
            session.setUserInfo(new DummyUserInfo());
            session.connect();
            LOGGER.debug("request sftp channel");
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            ok = true;
        }
        catch (Exception e)
        {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            LOGGER.error("SftpConnection cannot establish connection to SFTP server", e);
            throw new TransportError(e);
        }
        finally
        {
            try
            {
                if (!ok && channel != null)
                {
                    channel.disconnect();
                }

            }
            catch (Exception e)
            {
                LOGGER.error("Could not close SFTP connection", e);
            }
        }
    }

    @Override
    public List<FileInfo> getList(String path) throws IOException
    {
        try
        {
            LOGGER.debug("SftpConnection.getList({})", new String[]{path});
            String newPath = PathUtils.isRoot(path) ? "." : path.substring(1);
            
            if (!isDir(newPath)) throw new IOException(path + " not a directory");

            List<FileInfo> resp = new ArrayList<FileInfo>();
            for (Object obj : channel.ls(newPath)) {
                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) obj;
                if (".".equals(entry.getFilename()) || "..".equals(entry.getFilename())) continue;
                resp.add(makeFileInfo(path, entry));
            }

            return resp; 
        }
        catch(SftpException e)
        {
            throw new IOException(e);
        }
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
        try
        {
            String parent = PathUtils.getParent(path);
            String name = PathUtils.getName(path);
            SftpATTRS attrs = channel.stat(path.substring(1));
            return makeFileInfo(parent, name, attrs);
        }
        catch (SftpException e)
        {
            throw new PathNotFoundException("Couldn't retrieve information for '" + path + "'");
        }
    }

    private boolean isDir(String path) throws IOException
    {
        try
        {
            return channel.stat(path).isDir();
        }
        catch (SftpException e)
        {
            throw new IOException("Can't stat " + path + " or doesn't exist"); 
        }
    } 

    private FileInfo makeFileInfo(String parentPath, ChannelSftp.LsEntry info)
    {
        SftpATTRS attrs = info.getAttrs();
        Date date = new Date(attrs.getMTime());
        FileType type = attrs.isDir() ? FileType.DIRECTORY : FileType.FILE;
        FileInfo item = new FileInfo(type, PathUtils.joinPath(parentPath, info.getFilename()), info.getFilename(), attrs.getSize(), date);
        return item;
    }

    private FileInfo makeFileInfo(String parent, String name, SftpATTRS attrs)
    {
        Date date = new Date(attrs.getMTime());
        FileType type = attrs.isDir() ? FileType.DIRECTORY : FileType.FILE;
        FileInfo item = new FileInfo(type, PathUtils.joinPath(parent, name), name, attrs.getSize(), date);
        return item;
    }

    @Override
    public boolean rename(String directory, String from, String to) throws IOException
    {
        try
        {
            String newDir = PathUtils.isRoot(directory) ? "." : directory.substring(1);
            if (!isDir(newDir)) throw new IOException(directory + " not a directory");
            channel.rename(PathUtils.joinPath(newDir, from), PathUtils.joinPath(newDir,to));
            return true;
        }
        catch (SftpException e)
        {
            LOGGER.error("Cannot rename " + directory + "/" + from + " to " + directory + "/" + to, e);
            return false;
        }
    }

    @Override
    public boolean delete(FileInfo file) throws IOException
    {
        String path = file.getAbsolutePath();
        if (PathUtils.isRoot(path)) return false;
        path = path.substring(1);
        try
        {
            SftpATTRS attrs = channel.stat(path);
            if (attrs.isDir()) {
                channel.rmdir(path);
            } else {
                channel.rm(path);
            }
            return true;
        }
        catch (SftpException e)
        {
            LOGGER.error("Cannot delete " + file.getAbsolutePath(), e);
            return false;
        }
    }

    @Override
    public boolean createDir(String parent, String name) throws IOException
    {
        try
        {
            String newDir = PathUtils.isRoot(parent) ? "." : parent.substring(1);
            if (!isDir(newDir)) throw new IOException(parent + " not a directory");
            channel.mkdir(PathUtils.joinPath(newDir, name));
            return true;
        }
        catch (SftpException e)
        {
            LOGGER.error("Cannot mkdir "+ parent + "/" + name, e);
            return false;
        }
    }

    @Override
    public InputStream openInputStream(String from) throws IOException
    {
        try
        {
            if (isDir(from.substring(1))) throw new IOException(from + " is a directory");
            return channel.get(from.substring(1));
        }
        catch (SftpException e)
        {
            throw new IOException("Cannot open input " + from, e);
        }
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
        try
        {
            String parent = PathUtils.getParent(to);
            parent = PathUtils.isRoot(parent) ? "." : parent.substring(1);
            if (!isDir(parent)) throw new IOException(parent + " is not a directory");
            return channel.put(to.substring(1));
        }
        catch (SftpException e)
        {
            throw new IOException("Cannot open output " + to, e);
        }
    }

    @Override
    public boolean closeOutputStream(String to, OutputStream os) throws IOException
    {
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
        channel.disconnect();
        session.disconnect();
    }

    private static class DummyUserInfo implements UserInfo
    {
        public String getPassphrase() { throw new RuntimeException("sftp: requested pass phrase"); }
        public String getPassword() { throw new RuntimeException("sftp: requested password"); }
        public boolean promptPassword(String message) { throw new RuntimeException("sftp: requested prompt password"); }
        public boolean promptPassphrase(String message) { throw new RuntimeException("sftp: requested prompt passphrase"); }
        public boolean promptYesNo(String message) { throw new RuntimeException("stfp: requested yes/no"); }
        public void showMessage(String message) { throw new RuntimeException("sftp: requested show message " + message); }
    }

    private static class JschLogger implements com.jcraft.jsch.Logger
    {
        public boolean isEnabled(int level) {
            switch (level) {
            case com.jcraft.jsch.Logger.DEBUG: return LOGGER.isDebugEnabled();
            case com.jcraft.jsch.Logger.INFO: return LOGGER.isInfoEnabled();
            case com.jcraft.jsch.Logger.WARN: return LOGGER.isWarnEnabled();
            }
            return LOGGER.isErrorEnabled();
        }
        public void log(int level, String message){
            switch (level) {
            case com.jcraft.jsch.Logger.DEBUG: if (LOGGER.isDebugEnabled()) { LOGGER.debug(message); return; }
            case com.jcraft.jsch.Logger.INFO: if (LOGGER.isInfoEnabled()) { LOGGER.info(message); return; }
            case com.jcraft.jsch.Logger.WARN: if (LOGGER.isWarnEnabled()) { LOGGER.warn(message); return; }
            }
            if (LOGGER.isErrorEnabled()) { LOGGER.error(message); return; }
        }
    }

}
