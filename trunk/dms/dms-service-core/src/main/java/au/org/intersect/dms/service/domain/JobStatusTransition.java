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

package au.org.intersect.dms.service.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.org.intersect.dms.core.service.dto.JobStatus;

/**
 * Job status transition machine:
 * 
 * <ul>
 * <li>CREATED -> MONITORING | SCOPING</li>
 * <li>MONITORING -> SCOPING | ABORTED</li>
 * <li>SCOPING -> COPYING</li>
 * <li>COPYING -> FINISHED | ABORTED</li>
 * </ul>
 * 
 * @version $Rev: 5 $
 * 
 */
public class JobStatusTransition
{
    private static final Map<JobStatus, List<JobStatus>> TRANSITIONS = new HashMap<JobStatus, List<JobStatus>>();

    static
    {
        TRANSITIONS.put(
                JobStatus.CREATED,
                Arrays.asList(new JobStatus[] {JobStatus.MONITORING, JobStatus.SCOPING, JobStatus.ABORTED,
                    JobStatus.CANCELLED}));
        TRANSITIONS.put(JobStatus.MONITORING,
                Arrays.asList(new JobStatus[] {JobStatus.SCOPING, JobStatus.ABORTED, JobStatus.CANCELLED}));
        TRANSITIONS.put(JobStatus.SCOPING,
                Arrays.asList(new JobStatus[] {JobStatus.COPYING, JobStatus.ABORTED, JobStatus.CANCELLED}));
        TRANSITIONS.put(JobStatus.COPYING,
                Arrays.asList(new JobStatus[] {JobStatus.FINISHED, JobStatus.ABORTED, JobStatus.CANCELLED}));
        TRANSITIONS.put(JobStatus.FINISHED, Arrays.asList(new JobStatus[] {JobStatus.ABORTED}));
    }

    /**
     * Checks if transition is valid
     * 
     * @param from
     *            from status
     * @param to
     *            to status
     * @return true if transition is valid, false otherwise
     */
    public static boolean isValidTransition(JobStatus from, JobStatus to)
    {
        List<JobStatus> validTransitions = TRANSITIONS.get(from);
        if (validTransitions == null || validTransitions.isEmpty())
        {
            throw new IllegalArgumentException("No transition specified from state " + from);
        }
        else
        {
            return validTransitions.contains(to);
        }
    }

}
