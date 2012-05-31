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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import au.org.intersect.dms.wn.ConnectionParams;

@RunWith(MockitoJUnitRunner.class)
public class ActiveConnectionsCacheTest
{
    @Mock
    private Ehcache cache;

    @InjectMocks
    private ActiveConnectionsCache activeConnectionsCache = new ActiveConnectionsCache(cache);

    private Integer existingKey = 1000;

    private ConnectionParams connectionParams = new ConnectionParams("ftp", "test", "username", "password");

    @Before
    public void setUp() throws Exception
    {
        when(cache.get(existingKey)).thenReturn(new Element(existingKey, connectionParams));
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testPutSuccess()
    {
        activeConnectionsCache.put(existingKey, connectionParams);
        assertEquals("Same connection should be returned", connectionParams, activeConnectionsCache.get(existingKey));
    }

    @Test
    public void testGetSuccess()
    {
        assertEquals("Existing connection should be returned", connectionParams,
                activeConnectionsCache.get(existingKey));
    }

    @Test
    public void testGetFail()
    {
        assertNull("No connection should be returned", activeConnectionsCache.get(9999));
    }

}
