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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.errors.PathNotFoundException;
import au.org.intersect.dms.core.service.BasicConnectionDetails;
import au.org.intersect.dms.tunnel.HddConstants;
import au.org.intersect.dms.tunnel.HddUtil;
import au.org.intersect.dms.webtunnel.TunnelController;
import au.org.intersect.dms.webtunnel.TunnelJobTracker;
import au.org.intersect.dms.wn.ConnectionParams;
import au.org.intersect.dms.wn.TransportConnection;

/**
 * Connection to a HTTP Tunnel to download or upload files from a PC
 * 
 * @version $Rev: 29 $
 */
//TODO CHECKSTYLE-OFF: ClassFanOutComplexity
public class HddConnection implements TransportConnection
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HddConnection.class);

    private String jobId;

    private Map<String, List<FileInfo>> dirs = new HashMap<String, List<FileInfo>>();

    private ConnectionParams params;

	private TunnelJobTracker tracker;

	private ExecutorService executor;

    public HddConnection(TunnelController facade, final String jobId, ExecutorService executor)
    {
        LOGGER.info("HddConnection to job# {}", jobId);
        this.jobId = jobId;
        this.executor = executor;
        params = new ConnectionParams("hdd", "tunnel", jobId, null);
        tracker = facade.createJobQueue(jobId);
    }

    @Override
    public List<FileInfo> getList(String path) throws IOException
    {
        if (dirs.containsKey(path))
        {
            return dirs.get(path);
        }
        LOGGER.info("job# {}, getList:{}", jobId, path);
        ByteArrayInputStream bis = serializeToInputStream(path);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        tracker.processRequest(HddConstants.METHOD_GETLIST, bis, bos);
        List<FileInfo> resp = Arrays.asList(HddUtil.deserialize(new ByteArrayInputStream(bos.toByteArray()), (Class<FileInfo[]>) new FileInfo[0].getClass()));
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Caching '" + path + "' with " + resp.size() + " files");
        }
        dirs.put(path, resp);
        return resp;
    }

    private ByteArrayInputStream serializeToInputStream(String path) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HddUtil.serialize(bos, path);
		return new ByteArrayInputStream(bos.toByteArray());
	}

    private ByteArrayInputStream serializeParamsToInputStream(String[] params) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HddUtil.serialize(bos, params);
		return new ByteArrayInputStream(bos.toByteArray());
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
        LOGGER.info("job# {}, rename in {}, from:{} to:{}", new Object[]{directory, from, to});
        ByteArrayInputStream bis = serializeParamsToInputStream(new String[]{directory, from, to});
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        tracker.processRequest(HddConstants.METHOD_RENAME, bis, bos);
        Boolean resp = HddUtil.deserialize(new ByteArrayInputStream(bos.toByteArray()), Boolean.class);
        return resp != null && resp.booleanValue();
    }

    @Override
    public boolean delete(FileInfo file) throws IOException
    {
        LOGGER.info("job# {}, delete:{}", jobId, file.getAbsolutePath());
        ByteArrayInputStream bis = serializeToInputStream(file.getAbsolutePath());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        tracker.processRequest(HddConstants.METHOD_DELETE, bis, bos);
        Boolean resp = HddUtil.deserialize(new ByteArrayInputStream(bos.toByteArray()), Boolean.class);
        return resp != null && resp.booleanValue();
    }

    @Override
    public boolean createDir(String parent, String name) throws IOException
    {
        LOGGER.info("job# {}, createDir:{}/{}", new Object[]{jobId, parent, name});
        ByteArrayInputStream bis = serializeParamsToInputStream(new String[]{parent,name});
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        tracker.processRequest(HddConstants.METHOD_CREATEDIR, bis, bos);
        Boolean resp = HddUtil.deserialize(new ByteArrayInputStream(bos.toByteArray()), Boolean.class);
        return resp != null && resp.booleanValue();
    }

    @Override
    public InputStream openInputStream(final String from) throws IOException
    {
        LOGGER.info("job# {}, openInputStream:{}", jobId, from);
        final ByteArrayInputStream bis = serializeToInputStream(from);
        final PipedOutputStream pos = new PipedOutputStream();
        final PipedInputStream pis = new PipedInputStream(pos);
        Future<?> mon = executor.submit(new Runnable() {

			@Override
			public void run() {
				try {
					tracker.processRequest(HddConstants.METHOD_OPENINPUTSTREAM, bis, pos);
				} catch (IOException e) {
					LOGGER.error("openInputStream '" + from + "' failed", e);
				}
			}
        });
        return new HddWrappedInputStream<InputStream>(pis, mon);
    }

    @Override
    public boolean closeInputStream(String from, InputStream is) throws IOException
    {
        HddWrappedInputStream<InputStream> wis = (HddWrappedInputStream<InputStream>) is;
        wis.close();
        return true;
    }

    @Override
    public OutputStream openOutputStream(final String path, long size) throws IOException
    {
        LOGGER.info("job# {}, openOutputStream:{}", jobId, path);
        final PipedInputStream pis = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream(pis);
        HddUtil.serializeSpecial(pos, path, Long.toString(size));
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Future<?> mon = executor.submit(new Runnable() {

			@Override
			public void run() {
				try {
					tracker.processRequest(HddConstants.METHOD_OPENOUTPUTSTREAM, pis, bos);
				} catch (IOException e) {
					LOGGER.error("openOutputStream '" + path+ "' failed", e);
				}
			}
        });
        return new HddWrappedOutputStream<ByteArrayOutputStream>(bos, pos, mon);
    }

    @Override
    public boolean closeOutputStream(String to, OutputStream os) throws IOException
    {
    	HddWrappedOutputStream<ByteArrayOutputStream> wos = (HddWrappedOutputStream<ByteArrayOutputStream>) os;
    	// IMPORTANT: close first, so IO finishes and we wait for monitor to give us control
    	// see getReference in HddWrappedOuputStream
    	wos.close(); 
    	ByteArrayOutputStream bos = wos.getReference();
    	Boolean resp = HddUtil.deserialize(new ByteArrayInputStream(bos.toByteArray()), Boolean.class);
        return resp;
    }

    public void finishTransfer()
    {
    	if (tracker.stopped())
    	{
    		return;
    	}
        LOGGER.info("job# {}, finish", jobId);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
			tracker.processRequest(HddConstants.METHOD_FINALIZE, null, bos);
		}
        catch (IOException e)
        {
			LOGGER.error("Error finishing job #" + jobId, e);
		}
        finally
		{
        	tracker.stopJob();
		}
    }
    
    @Override
    public BasicConnectionDetails getBasicConnectionDetails()
    {
        return params;
    }

}
