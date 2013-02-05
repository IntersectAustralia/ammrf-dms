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

package au.org.intersect.dms.wn;

/**
 * Werapper that simplifies work with cache
 *
 * @param <K> key type
 * @param <V> type of the objects that will be handled by this cache
 */
public interface CacheWrapper<K, V>
{
    /**
     * Puts object into cache
     * @param key object key
     * @param value object
     */
    void put(K key, V value);
    
    /**
     * Removes from cache
     * @param key
     */
    void remove(K key);
    
    /**
     * Returns object from the cache
     * 
     * @param key object key
     * @return object from the cache
     */
    V get(K key);
}
