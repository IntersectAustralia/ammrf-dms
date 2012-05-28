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

package au.org.intersect.dms.service;

import au.org.intersect.dms.core.service.dto.JobUpdate;
import au.org.intersect.dms.service.domain.Job;

/**
 * This class implements an algorithm to estimate the average velocity based on the previous estimate and the latest
 * updated transfer values for the job.
 * 
 * @version $Rev: 29 $
 */
public class JobAverageSpeedCalculator
{
    private static final int MILLIS_IN_A_SECOND = 1000;
    /*private static final double DAMP_FACTOR_TIMEFRAME_SECONDS = 60;*/
    private static final double TARGET_DAMPING_FACTOR = 0.92;

    public Double calculateSpeed(Job job, JobUpdate details)
    {
        double currentSpeed;
        long historicalStamp;
        long currentStamp;
        double timeDifferenceInSeconds;
        double weightOfHistoricalAverage;

        if (job.getUpdateTimeStamp() == null)
        {
            return null;
        }

        historicalStamp = job.getUpdateTimeStamp();
        currentStamp = details.getTimeStamp();
        timeDifferenceInSeconds = ((double) (currentStamp - historicalStamp)) / MILLIS_IN_A_SECOND;
        /*weightOfHistoricalAverage = Math.exp(timeDifferenceInSeconds * Math.log(TARGET_DAMPING_FACTOR)
                / DAMP_FACTOR_TIMEFRAME_SECONDS);*/
        
        weightOfHistoricalAverage = Math.pow(TARGET_DAMPING_FACTOR, timeDifferenceInSeconds);

        currentSpeed = ((double) (details.getCurrentBytes() - job.getCurrentBytes())) / timeDifferenceInSeconds;

        if (job.getAverageSpeed() == null)
        {
            return currentSpeed;
        }

        return job.getAverageSpeed() * weightOfHistoricalAverage + currentSpeed * (1 - weightOfHistoricalAverage);
    }

}
