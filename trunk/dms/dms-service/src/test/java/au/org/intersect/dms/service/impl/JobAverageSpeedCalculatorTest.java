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

package au.org.intersect.dms.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import au.org.intersect.dms.core.service.dto.JobType;
import au.org.intersect.dms.core.service.dto.JobUpdate;
import au.org.intersect.dms.service.domain.Job;

public class JobAverageSpeedCalculatorTest
{

    private JobAverageSpeedCalculator calculator;

    @Before
    public void setUp() throws Exception
    {
        calculator = new JobAverageSpeedCalculator();
    }
    
    @Test
    public void testCalculateSpeedForNullAverage()
    {
        Job job = new Job(JobType.COPY, null, null, null, null, 0);
        job.setAverageSpeed(null);
        long timeStampAvg = 6000000;
        job.setUpdateTimeStamp(timeStampAvg);
        job.setCurrentBytes(1000L);
        JobUpdate details = new JobUpdate(1L, 1, 1, 2000L, timeStampAvg + 1000L);
        Double resp = calculator.calculateSpeed(job, details);
        assertNotNull(resp);
        assertEquals(1000.0, resp.doubleValue(), 0.0001);
    }

}
