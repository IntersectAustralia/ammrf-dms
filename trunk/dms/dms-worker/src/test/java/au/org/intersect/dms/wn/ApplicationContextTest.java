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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.instrument.InstrumentHarvester;
import au.org.intersect.dms.instrument.harvester.InstrumentHarvesterFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:META-INF/spring/applicationContext-workernode*.xml")
public class ApplicationContextTest
{
    
    @Autowired
    private InstrumentHarvesterFactory instrumentProfileFinder;

    /**
     * Test that a configuration for MICRO_CT exists and that the finder (a service factory) actually
     * delivers different objects in each request.
     */
    @Test
    public void testHarvesterConfiguration()
    {
        InstrumentProfile profileId = InstrumentProfile.MICRO_CT;
        assertNotNull(instrumentProfileFinder);
        InstrumentHarvester obj1 = instrumentProfileFinder.makeInstrumentHarvester(profileId);
        assertNotNull(obj1);
        InstrumentHarvester obj2 = instrumentProfileFinder.makeInstrumentHarvester(profileId);
        assertNotNull(obj2);
        assertNotSame(obj1, obj2);
    }

}
