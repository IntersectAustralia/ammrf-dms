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

package au.org.intersect.dms.core.domain;

import java.util.List;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import au.org.intersect.dms.core.service.dto.JobType;
import au.org.intersect.dms.util.DateFormatter;

/**
 * Job item in job result set
 * 
 * @version $Rev: 29 $
 */
@RooJavaBean
@RooToString
public class JobItem
{
    private static final int PERCENTAGE_MULTIPLIER = 100;

    private Long jobId;
    private String source;
    private List<String> sourceDirs;
    private String destination;
    private String destinationDir;
    private String createdTime;
    private String copyStartedTime;
    private String finishedTime;
    private Integer currentNumberOfFiles;
    private Integer totalNumberOfFiles;
    private Double percentage;
    private String status;
    private JobType type;
    private Double averageSpeed;
    private Double estimatedTimeRemaining;
    private Double displayedAverageSpeed;

    /**
     * Converts raw representation of percentage (0.xxxxx) into proper one and rounds to two decimal places
     * 
     * @param percentage
     *            raw percentage as (current number) / total
     */
    public void setPercentage(Double percentage)
    {
        double result = percentage * PERCENTAGE_MULTIPLIER * PERCENTAGE_MULTIPLIER;
        result = Math.round(result);
        result = result / PERCENTAGE_MULTIPLIER;
        this.percentage = result;
    }

    public void setAverageSpeed(Double averageSpeed)
    {
        this.averageSpeed = averageSpeed != null ? (double) Math.round(averageSpeed) : null;
    }

    public void setEstimatedTimeRemaining(Double estimatedTimeRemaining)
    {
        this.estimatedTimeRemaining = (double) Math.round(estimatedTimeRemaining);
    }

    public void setDisplayedAverageSpeed(Double displayedAverageSpeed)
    {
        this.displayedAverageSpeed = (double) Math.round(displayedAverageSpeed);
    }

    public void setCreatedTime(Long createdTime)
    {
        this.createdTime = DateFormatter.formatDate2EST(createdTime);

    }

    public void setCopyStartedTime(Long copyStartedTime)
    {
        this.copyStartedTime = DateFormatter.formatDate2EST(copyStartedTime);
    }

    public void setFinishedTime(Long finishedTime)
    {
        this.finishedTime = DateFormatter.formatDate2EST(finishedTime);
    }

}