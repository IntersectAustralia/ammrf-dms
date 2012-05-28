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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.org.intersect.dms.core.service.BasicConnectionDetails;
import au.org.intersect.dms.core.service.WorkerEventListener;
import au.org.intersect.dms.wn.ConnectionParams;

/**
 * A class that calls trigger methods depending on its nature.
 * 
 * @author carlos
 * 
 */
public class TriggerHelper
{
    private static final Map<String, Method> METHODS = new HashMap<String, Method>();

    private List<WorkerEventListener> listeners = new ArrayList<WorkerEventListener>();

    static
    {
        for (Method meth : WorkerEventListener.class.getMethods())
        {
            METHODS.put(meth.getName(), meth);
        }
    }

    public void addEventListener(WorkerEventListener listener)
    {
        synchronized (listeners)
        {
            if (!listeners.contains(listener))
            {
                listeners.add(listener);
            }
        }
    }

    public void removeEventListener(WorkerEventListener listener)
    {
        synchronized (listeners)
        {
            listeners.remove(listener);
        }
    }

    public void createDirectory(BasicConnectionDetails connParams, String parentPath, String name)
    {
        triggerMethod("createDirectory", connParams, parentPath, name);
    }

    public void rename(BasicConnectionDetails connParams, String parentPath, String oldName,
            String newName)
    {
        triggerMethod("rename", connParams, parentPath, oldName, newName);
    }

    public void delete(ConnectionParams connParams, String path)
    {
        triggerMethod("delete", connParams, path);
    }

    public void copyFromTo(BasicConnectionDetails connParamsFrom, String fromPath,
            BasicConnectionDetails connParamsTo, String toPath)
    {
        triggerMethod("copyFromTo", connParamsFrom, fromPath, connParamsTo, toPath);
    }

    private void triggerMethod(String method, Object... params)
    {
        Method meth = METHODS.get(method);
        synchronized (listeners)
        {
            for (WorkerEventListener listener : listeners)
            {
                try
                {
                    meth.invoke(listener, params);
                }
                catch (IllegalArgumentException e)
                {
                    throw new RuntimeException(e);
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
                catch (InvocationTargetException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

    }

}
