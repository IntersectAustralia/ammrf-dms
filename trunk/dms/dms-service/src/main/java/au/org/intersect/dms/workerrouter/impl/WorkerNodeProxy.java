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

package au.org.intersect.dms.workerrouter.impl;

import java.util.List;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.service.WorkerNode;
import au.org.intersect.dms.core.service.dto.CopyParameter;
import au.org.intersect.dms.core.service.dto.CreateDirectoryParameter;
import au.org.intersect.dms.core.service.dto.DeleteParameter;
import au.org.intersect.dms.core.service.dto.GetListParameter;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;
import au.org.intersect.dms.core.service.dto.RenameParameter;

/**
 * A wrapper that intercepts the calls to the WorkerNode and changes the connectionId properly.
 *
 * @version $Rev: 29 $
 */
public class WorkerNodeProxy implements WorkerNode
{
    private static final int BITS = 8;
    private static final int MAX_WORKERS = 1 << BITS;
    private static final int BIT_MASK = 0x0ff;

    private int workerId;
    private List<WorkerRoute> routes;
    private WorkerNode proxy;
    private String queue;

    public WorkerNodeProxy(int i, List<WorkerRoute> routes, String queue)
    {
        if (i >= MAX_WORKERS)
        {
            throw new RuntimeException("Maximum " + MAX_WORKERS + " allowed");
        }
        this.workerId = i;
        this.routes = routes;
        this.queue = queue;
    }

    @Override
    public Integer openConnection(OpenConnectionParameter openConnectionParams)
    {
        return toExternal(proxy.openConnection(openConnectionParams));
    }

    @Override
    public OpenConnectionParameter getConnectionDetails(Integer connectionId)
    {
        return proxy.getConnectionDetails(fromExternal(connectionId));
    }

    @Override
    public List<FileInfo> getList(GetListParameter oldParams)
    {
        GetListParameter params = new GetListParameter(fromExternal(oldParams.getConnectionId()),
                oldParams.getAbsolutePath());
        return proxy.getList(params);
    }

    @Override
    public boolean rename(RenameParameter oldParams)
    {
        RenameParameter params = new RenameParameter(fromExternal(oldParams.getConnectionId()), oldParams.getFrom(),
                oldParams.getTo());
        return proxy.rename(params);
    }

    @Override
    public boolean delete(DeleteParameter oldParams)
    {
        DeleteParameter params = new DeleteParameter(fromExternal(oldParams.getConnectionId()), oldParams.getFiles());
        return proxy.delete(params);
    }

    @Override
    public boolean createDir(CreateDirectoryParameter oldParams)
    {
        CreateDirectoryParameter params = new CreateDirectoryParameter(fromExternal(oldParams.getConnectionId()),
                oldParams.getParent(), oldParams.getName());
        return proxy.createDir(params);
    }

    @Override
    public void copy(CopyParameter oldParams)
    {
        CopyParameter params = new CopyParameter(oldParams.getUsername(), oldParams.getJobId(),
                fromExternal(oldParams.getFromConnectionId()), oldParams.getFromFiles(),
                fromExternal(oldParams.getToConnectionId()), oldParams.getToDir());
        proxy.copy(params);
    }

    @Override
    public void ingest(IngestParameter oldParams)
    {
        IngestParameter params = new IngestParameter(oldParams.getUsername(), oldParams.getJobId(),
                fromExternal(oldParams.getFromConnectionId()), oldParams.getFromFiles(),
                fromExternal(oldParams.getToConnectionId()), oldParams.getToDir(), oldParams.getInstrumentProfile());
        params.setCopyToWorkstation(oldParams.isCopyToWorkstation());
        proxy.ingest(params);
    }

    @Override
    public boolean stopJob(Long jobId)
    {
        return proxy.stopJob(jobId);
    }

    public boolean canHandle(String protocol, String server)
    {
        for (WorkerRoute route : routes)
        {
            if (route.canHandle(protocol, server))
            {
                return true;
            }
        }
        return false;
    }

    public int getWorkerId()
    {
        return workerId;
    }
    
    @Override
    public String toString()
    {
        return "WorkerProxy #" + workerId + " on " + queue;
    }

    public void setProxy(WorkerNode proxy)
    {
        this.proxy = proxy;
    }

    private Integer fromExternal(Integer connectionId)
    {
        return Integer.valueOf(connectionId >> BITS);
    }

    private Integer toExternal(Integer connectionId)
    {
        return Integer.valueOf((connectionId << BITS) + workerId);
    }

    public static Integer getWorkerId(Integer connectionId)
    {
        return Integer.valueOf(connectionId & BIT_MASK);
    }

}
