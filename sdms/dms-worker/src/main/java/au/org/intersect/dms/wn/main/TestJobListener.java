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

package au.org.intersect.dms.wn.main;

import au.org.intersect.dms.core.service.JobListener;
import au.org.intersect.dms.core.service.dto.JobFinished;
import au.org.intersect.dms.core.service.dto.JobScoped;
import au.org.intersect.dms.core.service.dto.JobStatusUpdateEvent;
import au.org.intersect.dms.core.service.dto.JobUpdate;

public class TestJobListener implements JobListener
{
    long jobId;
    boolean begin = false;
    boolean end = false;

    public void jobStatusUpdate(JobStatusUpdateEvent details) {
        if (details.getJobId() != jobId) return;
    }

    public void jobBegin(JobScoped details) {
        if (details.getJobId() == jobId) begin = true;
    }

    public void jobProgress(JobUpdate details) {
        if (details.getJobId() != jobId) return;
    }

    public void jobEnd(JobFinished details) {
        if (details.getJobId() == jobId) end = true;
    }

}
