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

package au.org.intersect.dms.workerrouter;

import au.org.intersect.dms.core.service.WorkerNode;

/**
 * A worker router helps the DMS to identify a worker to talk to perform a task.
 * Workers are identified by an id (short integer). There is no 
 * @version $Rev: 29 $
 */
public interface WorkerRouter
{
    /**
     * Find a worker able to talk this protocol to a server. If none found, throws WorkerNotFound exception.
     * @param protocol
     * @param server
     * @return
     */
    WorkerNode findWorker(String protocol, String server);

    /**
     * Given a connection, returns the corresponding worker.
     * @param connectionId
     * @return
     */
    WorkerNode findWorker(Integer connectionId);

    /**
     * Finds a common worker to talk to two different servers.
     * @param protocol
     * @param server
     * @param protocol2
     * @param server2
     * @return
     */
    WorkerNode findCommonWorker(String protocol, String server, String protocol2, String server2);

    /**
     * Gets the worker for a given connectionId
     * @param connection
     * @return
     */
    int getWorkerId(Integer connection);

    /**
     * 
     * @param workerId
     * @return
     */
    WorkerNode findWorkerById(int workerId);
    
}
