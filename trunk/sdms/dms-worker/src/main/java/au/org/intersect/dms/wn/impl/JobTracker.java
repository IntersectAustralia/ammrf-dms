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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.service.JobListener;
import au.org.intersect.dms.core.service.dto.JobFinished;
import au.org.intersect.dms.core.service.dto.JobScoped;
import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.dms.core.service.dto.JobStatusUpdateEvent;
import au.org.intersect.dms.core.service.dto.JobUpdate;
import au.org.intersect.dms.core.service.dto.MetadataEventItem;

/**
 * Class to hold the scope and track progress of a copy job. We cancel a job by throwing an exception from within this
 * object (via an aspect advice on some methods).<br/>
 * When multi-threading gets introduced, this class needs to be reviewed. At the moment, the only field accessed
 * from several thread is cancelJob - but it's not critical, so we don't synchronize methods reading it (although some
 * care was put into jobException as noted below).
 * 
 * @version $Rev: 29 $
 */
public class JobTracker
{
    private static final int UPDATE_WINDOW_MSECS = 500;

    private static final Logger LOGGER = LoggerFactory.getLogger(JobTracker.class);

    private List<FromToData> fromToList = new ArrayList<FromToData>();
    private int totalNumberOfFiles;
    private int totalNumberOfDirectories;
    private long totalBytes;
    private long currentBytes;
    private int currentNumberOfFiles;
    private int currentNumberOfDirectories;
    private Long jobId;
    private boolean newData;
    // these values will ensure first call to progressed is true
    private long deltaBytes = -1L;
    private long deltaNumberOfFiles = -1L;
    private long deltaNumberOfDirectories = -1L;

    private JobListener jobListener;

    private long timeStamp;

	private Thread thread;

    public JobTracker(JobListener jobListener, Long jobId, Thread thread)
    {
        this.jobListener = jobListener;
        this.jobId = jobId;
        this.thread = thread;
        if (this.jobListener == null)
        {
            this.jobListener = new DummyJobListener();
        }
    }

    public Long getJobId()
    {
        return jobId;
    }

    public void addFile(String pathFrom, String pathTo, long numBytes)
    {
        LOGGER.info("scope addFile:" + pathFrom + " -> " + pathTo);
        fromToList.add(new FromToData(FileType.FILE, pathFrom, pathTo, numBytes));
        totalBytes += numBytes;
        totalNumberOfFiles++;
    }

    public void addDirectory(String pathFrom, String pathTo, long size)
    {
        LOGGER.info("scope addDir:" + pathFrom + " -> " + pathTo);
        fromToList.add(new FromToData(FileType.DIRECTORY, pathFrom, pathTo, size));
        totalNumberOfDirectories++;
    }

    public void progressFile(long numBytes)
    {
        currentBytes += numBytes;
        sendUpdate();
    }

    public Iterable<FromToData> taskList()
    {
        return fromToList;
    }

    public void scopeStarted()
    {
        LOGGER.info("Scoping for job {} started.", jobId);
        jobListener.jobStatusUpdate(new JobStatusUpdateEvent(jobId, JobStatus.SCOPING));
    }

    public void scopeDone()
    {
        this.timeStamp = System.currentTimeMillis();

        jobListener.jobBegin(new JobScoped(jobId, totalNumberOfDirectories, totalNumberOfFiles, totalBytes));
    }

    public void sendUpdate()
    {
        long ts = System.currentTimeMillis();
        if (ts < timeStamp + UPDATE_WINDOW_MSECS)
        {
            newData = true;
            return;
        }
        timeStamp = ts;
        jobListener
                .jobProgress(new JobUpdate(jobId, currentNumberOfDirectories, currentNumberOfFiles, currentBytes, ts));
        newData = false;
    }
    
    public void jobFinished()
    {
        jobFinished(null);
    }

    public void jobFinished(List<MetadataEventItem> metadata)
    {
        if (newData)
        {
            jobListener
                    .jobProgress(new JobUpdate(jobId, currentNumberOfDirectories, currentNumberOfFiles, currentBytes));
            newData = false;
        }
        jobListener.jobEnd(new JobFinished(jobId, JobStatus.FINISHED, metadata, null));
    }

    public void jobException(Exception e)
    {
        if (newData)
        {
            jobListener
                    .jobProgress(new JobUpdate(jobId, currentNumberOfDirectories, currentNumberOfFiles, currentBytes));
            newData = false;
        }
        // cheap solution to avoid the synchronization overhead over cancelJob but still submitting a consistent event
        boolean canceled = e instanceof InterruptedException;
        jobListener.jobEnd(new JobFinished(jobId, !canceled ? JobStatus.ABORTED : JobStatus.CANCELLED,
                 canceled ? null : e));
    }

    public void fileEnd(String path)
    {
        currentNumberOfFiles++;
        sendUpdate();
    }

    public void directoryEnd(String path)
    {
        currentNumberOfDirectories++;
        sendUpdate();
    }

	public Thread getThread() {
		return thread;
	}

	public void stampProgress() {
	    deltaBytes = totalBytes - currentBytes;
	    deltaNumberOfFiles = totalNumberOfFiles - currentNumberOfFiles;
	    deltaNumberOfDirectories = totalNumberOfDirectories - currentNumberOfDirectories;
	}

	public boolean progressed() {
		return deltaBytes != (totalBytes - currentBytes) || 
				deltaNumberOfFiles != (totalNumberOfFiles - currentNumberOfFiles) || 
						deltaNumberOfDirectories != (totalNumberOfDirectories - currentNumberOfDirectories);
	}

    private static class DummyJobListener implements JobListener
    {
        public void jobStatusUpdate(JobStatusUpdateEvent details) { }

        public void jobBegin(JobScoped details) { }

        public void jobProgress(JobUpdate details) { }

        public void jobEnd(JobFinished details) { }
    }

}
