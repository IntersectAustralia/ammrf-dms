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

package au.org.intersect.dms.wn.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.pool.KeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.errors.ConnectionClosedError;
import au.org.intersect.dms.core.errors.UnknownProtocolError;
import au.org.intersect.dms.core.service.JobListener;
import au.org.intersect.dms.core.service.WorkerEventListener;
import au.org.intersect.dms.core.service.WorkerNode;
import au.org.intersect.dms.core.service.dto.CopyParameter;
import au.org.intersect.dms.core.service.dto.CreateDirectoryParameter;
import au.org.intersect.dms.core.service.dto.DeleteParameter;
import au.org.intersect.dms.core.service.dto.GetFileInfoParameter;
import au.org.intersect.dms.core.service.dto.GetListParameter;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;
import au.org.intersect.dms.core.service.dto.RenameParameter;
import au.org.intersect.dms.wn.CacheWrapper;
import au.org.intersect.dms.wn.ConnectionParams;
import au.org.intersect.dms.wn.CopyStrategy;
import au.org.intersect.dms.wn.TransportConnection;

/**
 * Implementation of WorkerNode abstraction.
 * 
 */
// TODO CHECKSTYLE-OFF: ClassFanOutComplexity
// TODO CHECKSTYLE-OFF: ClassDataAbstractionCoupling
// TODO Refactor to fix ClassFanOutComplexity violation
public class WorkerNodeImpl implements WorkerNode
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerNodeImpl.class);

    private static final int FIRST_CONNECTION_ID = 1000;
    
    private static final long CHECKER_FREQUENCY = 60000L;

    private int nextId = FIRST_CONNECTION_ID;

    private CacheWrapper<Integer, ConnectionParams> activeConnectionsCache; // configured in ehcache.xml

    private TransportConnectionTemplate transportTemplate = new TransportConnectionTemplate();

    private final Map<Long, JobTracker> trackers = new HashMap<Long, JobTracker>();

    @Autowired
    private JobListener jobListener;

    private TriggerHelper trigger = new TriggerHelper();
    
    private Timer timer = new Timer();

    private CopyStrategy copier;
    
    public WorkerNodeImpl()
    {
    	timer.scheduleAtFixedRate(makeChecker(), CHECKER_FREQUENCY, CHECKER_FREQUENCY);
    }

    private TimerTask makeChecker() {
		return new TimerTask()
		{
			public void run()
			{
				for(JobTracker tracker : trackers.values())
				{
					if (!tracker.progressed())
					{
                                                LOGGER.info("Worker checker: killing blocked job " + tracker.getJobId());
						tracker.getThread().interrupt();
					}
					else
					{
						tracker.stampProgress();
					}
				}
			}

		};
	}

	public void setProtoMapping(Map<String, KeyedObjectPool> protoMapping)
    {
        transportTemplate.setProtoMapping(protoMapping);
    }

    @Required
    public void setCopyStrategy(CopyStrategy copier)
    {
        this.copier = copier;
    }

    public void setActiveConnectionsCache(
            CacheWrapper<Integer, ConnectionParams> activeConnectionsCache)
    {
        this.activeConnectionsCache = activeConnectionsCache;
    }

    @Override
    public Integer openConnection(OpenConnectionParameter openConnectionParams)
    {
        Map<String, KeyedObjectPool> protoMapping = transportTemplate.getProtoMapping();
        if (protoMapping == null || protoMapping.get(openConnectionParams.getProtocol()) == null)
        {
            throw new UnknownProtocolError(openConnectionParams.getProtocol());
        }
        final ConnectionParams key = new ConnectionParams(openConnectionParams.getProtocol(),
                openConnectionParams.getServer(), openConnectionParams.getUsername(),
                openConnectionParams.getPassword());
        TransportConnectionCallback<Integer> action = new TransportConnectionCallback<Integer>()
        {

            @Override
            public Integer performWith(TransportConnection conn)
            {
                return newSessionId(key);
            }

        };
        return transportTemplate.execute(key, action);
    }
    
    @Override
    public void closeConnection(Integer connectionId)
    {
    	ConnectionParams key = activeConnectionsCache.get(connectionId);
    	try
    	{
    	    activeConnectionsCache.remove(connectionId);
    	}
    	finally 
    	{
            transportTemplate.purgeKey(key);
    	}
    }

    @Override
    public List<FileInfo> getList(final GetListParameter getListParams)
    {
        return getList(getListParams, null);
    }

    @Override
    public List<FileInfo> getList(final GetListParameter getListParams, final String filter)
    {
        TransportConnectionCallback<List<FileInfo>> action = new TransportConnectionCallback<List<FileInfo>>()
        {

            @Override
            public List<FileInfo> performWith(TransportConnection conn) throws IOException
            {
                if (filter == null || filter.isEmpty())
                {
                    return conn.getList(getListParams.getAbsolutePath());
                }
                else
                {
                    return conn.getList(getListParams.getAbsolutePath(), filter);
                }
            }

        };
        return transportTemplate.execute(
                activeConnectionsCache.get(getListParams.getConnectionId()), action);
    }

    @Override
    public FileInfo getFileInfo(final GetFileInfoParameter getFileInfoParams)
    {
        TransportConnectionCallback<FileInfo> action = new TransportConnectionCallback<FileInfo>()
        {

            @Override
            public FileInfo performWith(TransportConnection conn) throws IOException
            {
                return conn.getInfo(getFileInfoParams.getAbsolutePath());
            }

        };
        return transportTemplate.execute(
                activeConnectionsCache.get(getFileInfoParams.getConnectionId()), action);
    }

    @Override
    public boolean rename(RenameParameter renameParams)
    {
        final Integer connectionId = renameParams.getConnectionId();
        final String to = renameParams.getTo();
        if (!renameParams.getFrom().startsWith("/"))
        {
            throw new IllegalArgumentException(renameParams.getFrom() + " must begin with '/'");
        }
        int parentDirEnd = renameParams.getFrom().lastIndexOf('/');
        final String parentDirectory = renameParams.getFrom().substring(0,
                parentDirEnd == 0 ? 1 : parentDirEnd);
        final String oldName = renameParams.getFrom().substring(parentDirEnd + 1,
                renameParams.getFrom().length());
        final ConnectionParams connParams = activeConnectionsCache.get(connectionId);
        TransportConnectionCallback<Boolean> action = new TransportConnectionCallback<Boolean>()
        {

            @Override
            public Boolean performWith(TransportConnection conn) throws IOException
            {
                if (conn.rename(parentDirectory, oldName, to))
                {
                    trigger.rename(connParams, parentDirectory, oldName, to);
                    return true;
                }
                return false;
            }

        };
        return transportTemplate.execute(connParams, action);
    }

    @Override
    public boolean delete(final DeleteParameter deleteParams)
    {
        final ConnectionParams connParams = activeConnectionsCache.get(deleteParams
                .getConnectionId());
        // TODO CHECKSTYLE-OFF: AnonInnerLength
        TransportConnectionCallback<Boolean> action = new TransportConnectionCallback<Boolean>()
        {
            @Override
            public Boolean performWith(TransportConnection conn) throws IOException
            {
                return delete(conn, deleteParams.getFiles());
            }

            private boolean delete(TransportConnection conn, List<FileInfo> subList)
                throws IOException
            {
                boolean success = subList.size() > 0 ? false : true;
                for (FileInfo file : subList)
                {
                    if (FileType.DIRECTORY == file.getFileType())
                    {
                        List<FileInfo> listing = conn.getList(file.getAbsolutePath());
                        if (listing.size() > 0)
                        {
                            success = delete(conn, listing);
                        }
                    }
                    success = conn.delete(file);
                    trigger.delete(connParams, file.getAbsolutePath());
                    if (!success)
                    {
                        LOGGER.error("Failed to delete file {}", file.getAbsolutePath());
                    }
                }
                return success;
            }
        };
        // CHECKSTYLE-ON: AnonInnerLength
        return transportTemplate.execute(
                activeConnectionsCache.get(deleteParams.getConnectionId()), action);
    }

    private synchronized Integer newSessionId(ConnectionParams key)
    {
        activeConnectionsCache.put(nextId, key);
        Integer resp = nextId;
        nextId++;
        return resp;
    }

    @Override
    public boolean createDir(final CreateDirectoryParameter createDirectoryparams)
    {
        final ConnectionParams connParams = activeConnectionsCache.get(createDirectoryparams
                .getConnectionId());
        TransportConnectionCallback<Boolean> action = new TransportConnectionCallback<Boolean>()
        {

            @Override
            public Boolean performWith(TransportConnection conn) throws IOException
            {
                if (conn.createDir(createDirectoryparams.getParent(),
                        createDirectoryparams.getName()))
                {
                    trigger.createDirectory(connParams, createDirectoryparams.getParent(),
                            createDirectoryparams.getName());
                    return true;
                }
                return false;
            }

        };
        return transportTemplate.execute(connParams, action);
    }

    @Override
    public void copy(final CopyParameter copyParams)
    {
        final JobTracker tracker = new JobTracker(jobListener, copyParams.getJobId(), Thread.currentThread());
        storeTracker(copyParams.getJobId(), tracker);
        // TODO CHECKSTYLE-OFF: IllegalCatch
        try
        {
            final ConnectionParams fromConnParams = activeConnectionsCache.get(copyParams
                    .getFromConnectionId());
            TransportConnectionCallback<Void> action = new TransportConnectionCallback<Void>()
            {
                @Override
                public Void performWith(final TransportConnection fromConn) throws IOException
                {

                    copier.doScope(tracker, fromConn, copyParams.getFromFiles(),
                            copyParams.getToDir());
                    final ConnectionParams toConnParams = activeConnectionsCache.get(copyParams
                            .getToConnectionId());
                    TransportConnectionCallback<Void> subAction = new TransportConnectionCallback<Void>()
                    {
                        @Override
                        public Void performWith(final TransportConnection toConn)
                            throws IOException
                        {
                            copier.copy(tracker, fromConn, toConn, trigger);
                            return null;
                        }
                    };
                    return transportTemplate.execute(toConnParams, subAction);
                }
            };

            LOGGER.debug(
                    "Starting copy: jobId={}, fromConnection={}, fromList={}, toConnection={}, toDir={}",
                    new Object[] {copyParams.getJobId(), copyParams.getFromConnectionId(),
                        copyParams.getFromFiles(), copyParams.getToConnectionId(),
                        copyParams.getToDir()});

            transportTemplate.execute(fromConnParams, action);
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to execute copy job " + copyParams.getJobId(), e);
            tracker.jobException(e);
        }
        finally
        {
            removeTracker(copyParams.getJobId());
        }
        // CHECKSTYLE-ON: IllegalCatch
    }

    private void storeTracker(Long jobId, JobTracker tracker)
    {
        synchronized (trackers)
        {
            trackers.put(jobId, tracker);
        }
    }

    private void removeTracker(Long jobId)
    {
        synchronized (trackers)
        {
            trackers.remove(jobId);
        }
    }

    @Override
    public OpenConnectionParameter getConnectionDetails(Integer connectionId)
    {
        if (HDD_CONNECTION_ID.equals(connectionId))
        {
            return new OpenConnectionParameter("file", "/", null, null);
        }
        ConnectionParams connParams = activeConnectionsCache.get(connectionId);
        if (connParams != null)
        {
            return new OpenConnectionParameter(connParams.getProtocol(), connParams.getHostname(),
                    connParams.getUsername(), connParams.getPassword());
        }
        else
        {
            throw new ConnectionClosedError(connectionId + " is not active");
        }
    }

    @Override
    public boolean stopJob(Long jobId)
    {
        boolean resp = false;
        synchronized (trackers)
        {
            JobTracker tracker = trackers.get(jobId);
            if (tracker != null)
            {
            	Thread thread = tracker.getThread();
            	LOGGER.info("Stopping copy thread: " + thread.getName());
            	thread.interrupt();
                resp = true;
            }
        }
        return resp;
    }

    @Override
    public void addEventListener(WorkerEventListener listener)
    {
        trigger.addEventListener(listener);
    }

    public void removeEventListener(WorkerEventListener listener)
    {
        trigger.removeEventListener(listener);
    }

}
