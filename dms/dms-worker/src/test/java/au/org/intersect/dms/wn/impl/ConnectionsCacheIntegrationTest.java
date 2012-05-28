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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.org.intersect.dms.wn.ConnectionParams;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:META-INF/spring/applicationContext*.xml")
public class ConnectionsCacheIntegrationTest
{
    @Autowired
    private ActiveConnectionsCache activeConnectionsCache;

    private ConnectionParams connectionParams = new ConnectionParams("ftp", "test", "username", "password");
    
    @Test
    public void testAvailableWithinIdlePeriod() throws InterruptedException
    {
        Integer existingKey = 1000;
        // the test.cache.properties defines an idle period of 5 secs
        activeConnectionsCache.put(existingKey, connectionParams);
        assertEquals("Connection not available 0 secs", connectionParams, activeConnectionsCache.get(existingKey));
        Thread.sleep(4000);
        assertEquals("Connection not available 4 secs", connectionParams, activeConnectionsCache.get(existingKey));
        Thread.sleep(4000);
        assertEquals("Connection not available 8 secs", connectionParams, activeConnectionsCache.get(existingKey));
    }

    @Test
    public void testNotAvailableAfterIdlePeriod() throws InterruptedException
    {
        Integer existingKey = 1001;
        // the test.cache.properties defines an idle period of 5 secs
        activeConnectionsCache.put(existingKey, connectionParams);
        assertEquals("Connection not available 0 secs", connectionParams, activeConnectionsCache.get(existingKey));
        Thread.sleep(4000);
        assertEquals("Connection not available 4 secs", connectionParams, activeConnectionsCache.get(existingKey));
        Thread.sleep(6000);
        assertNull("Connection available after 5 secs of innactivity", activeConnectionsCache.get(existingKey));
    }

}
