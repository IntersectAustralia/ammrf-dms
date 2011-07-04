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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool.KeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.core.errors.ConnectionClosedError;
import au.org.intersect.dms.core.errors.TransportError;
import au.org.intersect.dms.core.errors.TransportException;
import au.org.intersect.dms.wn.ConnectionParams;
import au.org.intersect.dms.wn.TransportConnection;

/**
 * Template pattern to handle keyed pools
 * 
 * @version $Rev: 29 $
 */
public class TransportConnectionTemplate
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportConnectionTemplate.class);

    private Map<String, KeyedObjectPool> protoMapping = new HashMap<String, KeyedObjectPool>();

    public void setProtoMapping(Map<String, KeyedObjectPool> protoMapping)
    {
        this.protoMapping = protoMapping;
    }

    public Map<String, KeyedObjectPool> getProtoMapping()
    {
        return protoMapping;
    }

    /**
     * Runs action with transport connection from pool and does proper pool handling.
     * 
     * @param <T>
     * @param key
     * @param action
     * @return
     */
    public <T> T execute(ConnectionParams key, TransportConnectionCallback<T> action)
    {
        if (key == null)
        {
            throw new ConnectionClosedError("The connection to this server has timed out. "
                    + "Please close the current tab and re-establish the connection");
        }
        // TODO CHECKSTYLE-OFF: IllegalCatch
        KeyedObjectPool pool = protoMapping.get(key.getProtocol());
        Object conn = null;
        try
        {
            conn = pool.borrowObject(key);
            return action.performWith((TransportConnection) conn);
        }
        catch (TransportException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to perform operation. Exception:", e);
            if (conn != null)
            {
                try
                {
                    pool.invalidateObject(key, conn);
                }
                catch (Exception e1)
                {
                    LOGGER.error("Failed to remove closed connection from the pool. Exception:", e1);
                }
            }
            conn = null;
            throw new TransportError("This server has stopped responding. Please close the current tab and" 
                    + " re-establish the connection. " 
                    + "If the problem persists please contact your system administrator.");
        }
        finally
        {
            if (conn != null)
            {
                try
                {
                    pool.returnObject(key, conn);
                }
                catch (Exception e)
                {
                    LOGGER.error("Failed to return connection to the pool. Exception:", e);
                }
            }
        }
        // CHECKSTYLE-ON: IllegalCatch
    }

}
