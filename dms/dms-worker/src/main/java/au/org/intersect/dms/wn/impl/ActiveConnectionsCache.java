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

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import au.org.intersect.dms.wn.CacheWrapper;
import au.org.intersect.dms.wn.ConnectionParams;

/**
 * Cache based on Ehcache to store active connections. Uses expiration algorithm to delete inactive connections (should
 * be configured in spring context).
 * 
 */
public class ActiveConnectionsCache implements CacheWrapper<Integer, ConnectionParams>
{
    private final Ehcache cache;

    public ActiveConnectionsCache(Ehcache cache)
    {
        this.cache = cache;
        //this.cache = cacheFactoryBean.getObject();
    }

    @Override
    public void put(Integer key, ConnectionParams value)
    {
        cache.put(new Element(key, value));
    }

    /**
     * Returns ConnectionParams object or null if it is not in cache.
     */
    @Override
    public ConnectionParams get(Integer key)
    {
        Element element = cache.get(key);
        if (element != null)
        {
            return (ConnectionParams) element.getValue();
        }
        return null;
    }
    
}
