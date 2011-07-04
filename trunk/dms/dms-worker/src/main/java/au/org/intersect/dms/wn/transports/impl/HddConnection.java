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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.errors.PathNotFoundException;
import au.org.intersect.dms.core.errors.TransportException;
import au.org.intersect.dms.tunnel.HddConstants;
import au.org.intersect.dms.tunnel.HddUtil;
import au.org.intersect.dms.wn.TransportConnection;
import au.org.intersect.dms.wn.transports.impl.HddHttpClient.HttpMethod;

/**
 * Connection to a HTTP Tunnel to download or upload files from a PC
 * 
 * @version $Rev: 29 $
 */
public class HddConnection implements TransportConnection
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HddConnection.class);

    private HddHttpClient client;
    private String jobId;

    private Map<String, List<FileInfo>> dirs = new HashMap<String, List<FileInfo>>();

    private boolean used;

    public HddConnection(HddHttpClient client, final String jobId)
    {
        this.client = client;
        this.jobId = jobId;
    }

    @Override
    public List<FileInfo> getList(String path) throws IOException
    {
        if (dirs.containsKey(path))
        {
            return dirs.get(path);
        }
        LOGGER.info("job# {}, openInputStream:{}", jobId, path);
        HddCommand future = makeCommand(HddHttpClient.HttpMethod.GET, HddConstants.METHOD_GETLIST);
        HddUtil.serialize(future.getOutputStream(), path);
        InputStream is;
        try
        {
            is = future.getInputStream();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e);
        }
        List<FileInfo> resp = Arrays.asList(HddUtil.deserialize(is, (Class<FileInfo[]>) new FileInfo[0].getClass()));
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Caching '" + path + "' with " + resp.size() + " files");
        }
        dirs.put(path, resp);
        return resp;
    }

    @Override
    public List<FileInfo> getList(String path, String filter) throws IOException
    {
        throw new RuntimeException("HDDConnection:getList(filter) not defined");
    }

    @Override
    public FileInfo getInfo(String path) throws IOException
    {
        if (PathUtils.isRoot(path))
        {
            return FileInfo.createRootFileInfo();
        }
        String parent = PathUtils.getParent(path);
        for (FileInfo info : getList(parent))
        {
            if (path.equals(info.getAbsolutePath()))
            {
                return info;
            }
        }
        throw new PathNotFoundException(path);
    }

    @Override
    public boolean rename(String directory, String from, String to) throws IOException
    {
        throw new TransportException("HDD::rename not implemented");
    }

    @Override
    public boolean delete(FileInfo file) throws IOException
    {
        HddCommand future = makeCommand(HddHttpClient.HttpMethod.GET, HddConstants.METHOD_DELETE);
        OutputStream os = future.getOutputStream();
        HddUtil.serialize(os, file.getAbsolutePath());
        return deserializeFromFuture(future, Boolean.TYPE);
    }

    @Override
    public boolean createDir(String parent, String name) throws IOException
    {
        LOGGER.info("job# {}, createDir:{}/{}", new Object[]{jobId, parent, name});
        HddCommand future = makeCommand(HddHttpClient.HttpMethod.GET, HddConstants.METHOD_CREATEDIR);
        HddUtil.serialize(future.getOutputStream(), parent, name);
        return deserializeFromFuture(future, Boolean.class);
    }

    @Override
    public InputStream openInputStream(String from) throws IOException
    {
        LOGGER.info("job# {}, openInputStream:{}", jobId, from);
        HddCommand future = makeCommand(HddHttpClient.HttpMethod.GET, HddConstants.METHOD_OPENINPUTSTREAM);
        HddUtil.serialize(future.getOutputStream(), from);
        try
        {
            return new HddWrappedInputStream<HddCommand>(future, future.getInputStream());
        }
        catch (InterruptedException e)
        {
            throw new IOException(e);
        }
        catch (ExecutionException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public boolean closeInputStream(String from, InputStream is) throws IOException
    {
        HddWrappedInputStream<HddCommand> wis = (HddWrappedInputStream<HddCommand>) is;
        wis.close();
        return true;
    }

    @Override
    public OutputStream openOutputStream(String path, long size) throws IOException
    {
        LOGGER.info("job# {}, openOutputStream:{}", jobId, path);
        HddCommand future = makeCommand(HddHttpClient.HttpMethod.PUT, HddConstants.METHOD_OPENOUTPUTSTREAM);
        HddUtil.serializeSpecial(future.getOutputStream(), path, Long.toString(size));
        return new HddWrappedOutputStream<HddCommand>(future, future.getOutputStream());
    }

    @Override
    public boolean closeOutputStream(String to, OutputStream os) throws IOException
    {
        HddWrappedOutputStream<HddCommand> wos = (HddWrappedOutputStream<HddCommand>) os;
        HddCommand future = wos.getReference();
        wos.close();
        return deserializeFromFuture(future, Boolean.class);
    }

    public void finishTransfer()
    {
        if (!used)
        {
            return;
        }
        try
        {
            LOGGER.info("job# {}, finishTransfer", jobId);
            HddCommand future = client.makeCommand(HddHttpClient.HttpMethod.GET, jobId, HddConstants.METHOD_FINALIZE);
            future.getOutputStream().close();
            if (!deserializeFromFuture(future, Boolean.class))
            {
                LOGGER.error("FinishTransfer #" + jobId + " unsuccessful");
            }
        }
        catch (IOException e)
        {
            LOGGER.error("FinishTransfer error on #" + jobId, e);
        }
    }

    private HddCommand makeCommand(HttpMethod httpMethod, String method) throws IOException
    {
        used = true;
        HddCommand future = client.makeCommand(httpMethod, jobId, method);
        return future;
    }

    private <T> T deserializeFromFuture(HddCommand future, Class<T> clazz) throws IOException
    {
        InputStream is;
        try
        {
            is = future.getInputStream();
        }
        catch (InterruptedException e)
        {
            throw new TransportException(e);
        }
        catch (ExecutionException e)
        {
            throw new TransportException(e);
        }
        return HddUtil.deserialize(is, clazz);

    }

}
