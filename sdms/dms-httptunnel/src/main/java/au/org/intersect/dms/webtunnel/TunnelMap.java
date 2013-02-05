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

package au.org.intersect.dms.webtunnel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashSet;

/**
 * A map that blocks if key is not there, although we didn't implement the whole map interface.
 *
 * @version $Rev: 29 $
 * @param <T>
 */
public class TunnelMap<T>
{
	
	/**
     * How to create values.
     * @version $Rev: 29 $
     * @param <T>
     */
    public interface ValueFactory<T>
    {
        T create();
    }

    private Map<String, T> map = new HashMap<String, T>();
    
    public Set<Entry<String, T>> keys()
    {
        synchronized (map)
        {
            return new HashSet(map.entrySet());
        }
    }
    
    public T getNoWait(String id)
    {
        synchronized (map)
        {
            return map.get(id);
        }
    }
    
    public T getOrWait(String id)
    {
        synchronized (map)
        {
            while (!map.containsKey(id))
            {
                try
                {
                    map.wait(1000);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
            return map.get(id);
        }
    }
    
    public T put(String key, ValueFactory<T> factory)
    {
        synchronized (map)
        {
            if (!map.containsKey(key))
            {
                map.put(key, factory.create());
                map.notifyAll();
            }
            return map.get(key);
        }
    }

    public void remove(String key)
    {
        synchronized (map)
        {
            map.remove(key);
        }
    }

    public int size()
    {
        synchronized (map)
        {
            return map.size();
        }
    }
    
}
