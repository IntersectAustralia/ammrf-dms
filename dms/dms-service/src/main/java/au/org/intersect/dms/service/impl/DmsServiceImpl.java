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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.dms.catalogue.MetadataRepository;
import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.JobItem;
import au.org.intersect.dms.core.domain.JobSearchResult;
import au.org.intersect.dms.core.errors.NotAuthorizedError;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.core.service.WorkerNode;
import au.org.intersect.dms.core.service.dto.CreateDirectoryParameter;
import au.org.intersect.dms.core.service.dto.DeleteParameter;
import au.org.intersect.dms.core.service.dto.GetListParameter;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;
import au.org.intersect.dms.core.service.dto.RenameParameter;
import au.org.intersect.dms.service.domain.DmsUser;
import au.org.intersect.dms.service.domain.Job;
import au.org.intersect.dms.service.domain.JobFrom;
import au.org.intersect.dms.workerrouter.WorkerNotFoundException;
import au.org.intersect.dms.workerrouter.WorkerRouter;

/**
 * Implementation of DMS Service.
 * 
 */
public class DmsServiceImpl implements DmsService
{

    private static final int MILLIS_IN_A_SECOND = 1000;

    @Autowired
    private DmsServiceCopyImpl copyImpl;

    @Autowired
    private WorkerRouter router;
    
    @Autowired
    private MetadataRepository repository;

    @Override
    public Integer openConnection(String protocol, String server, String username, String password)
    {
        WorkerNode workerNode = router.findWorker(protocol, server);
        return workerNode.openConnection(new OpenConnectionParameter(protocol, server, username, password));
    }

    @Override
    public List<FileInfo> getList(Integer connectionId, String absolutePath)
    {
        WorkerNode workerNode = router.findWorker(connectionId);
        return workerNode.getList(new GetListParameter(connectionId, absolutePath));
    }

    @Override
    public boolean rename(Integer connectionId, String from, String to)
    {
        WorkerNode workerNode = router.findWorker(connectionId);
        return workerNode.rename(new RenameParameter(connectionId, from, to));
    }

    @Override
    public boolean delete(Integer connectionId, List<FileInfo> files)
    {
        WorkerNode workerNode = router.findWorker(connectionId);
        return workerNode.delete(new DeleteParameter(connectionId, files));
    }

    @Override
    public Long copy(String username, Integer sourceConnectionId, List<String> sources,
            Integer destinationConnectionId, String targetDir)
    {
        return copyImpl.callCopy(username, sourceConnectionId, sources, destinationConnectionId, targetDir);
    }

    @Override
    public Long ingest(String username, Long projectCode, String metadata, IngestParameter parameters)
    {
        return copyImpl.callIngest(username, projectCode, metadata, parameters);
    }

    private DmsUser findUser(String username)
    {
        List<DmsUser> users = DmsUser.findDmsUsersByUsername(username).getResultList();
        if (users.size() != 1)
        {
            return null;
        }
        return users.get(0);
    }

    @Override
    public boolean createDir(Integer connectionId, String parent, String name)
    {
        WorkerNode workerNode = router.findWorker(connectionId);
        return workerNode.createDir(new CreateDirectoryParameter(connectionId, parent, name));
    }

    @Override
    @Transactional("service")
    public JobSearchResult getJobs(String username, int startIndex, int pageSize)
    {
        DmsUser user = findUser(username);
        List<Job> jobs = user != null ? user.getJobs() : new ArrayList<Job>();
        JobSearchResult resp = new JobSearchResult();
        List<JobItem> jobsResp = new ArrayList<JobItem>(pageSize);
        int maxIndex = startIndex + pageSize;
        for (int i = startIndex; i < maxIndex && i < jobs.size(); i++)
        {
            JobItem jobItem = fromJobToJobItem(jobs.get(i));
            jobsResp.add(jobItem);
        }
        resp.setJobs(jobsResp);
        resp.setTotalSize(jobs.size());
        return resp;
    }

    private JobItem fromJobToJobItem(Job job)
    {
        JobItem resp = new JobItem();
        resp.setJobId(job.getId());
        resp.setType(job.getType());

        if (job.getTotalBytes() > 0 && job.getAverageSpeed() != null)
        {
            resp.setPercentage(((double) job.getCurrentBytes()) / ((double) job.getTotalBytes()));
            resp.setAverageSpeed(job.getAverageSpeed());
            resp.setEstimatedTimeRemaining(((double) (job.getTotalBytes()) - (job.getCurrentBytes()))
                    / job.getAverageSpeed());
            resp.setDisplayedAverageSpeed(calculateDisplayedSpeed(job));
        }
        else
        {
            if (job.getTotalBytes() == 0 && job.getStatus() == JobStatus.COPYING)
            {
                double percentageUsingNumberOfFiles = (((double) job.getCurrentNumberOfFiles() + job
                        .getCurrentNumberOfDirectories()))
                        / ((double) (job.getTotalNumberOfFiles() + job.getTotalNumberOfDirectories()));
                resp.setPercentage(percentageUsingNumberOfFiles);
            }
            else if (job.getTotalBytes() == 0 && job.getStatus() == JobStatus.FINISHED)
            {
                resp.setPercentage(1.0);
            }
            else
            {
                resp.setPercentage(0.0);
            }
            resp.setAverageSpeed(0.0);
            resp.setEstimatedTimeRemaining(0.0);
            resp.setDisplayedAverageSpeed(0.0);
        }

        resp.setStatus(job.getStatus().toString());
        resp.setDestination(job.getDestination());
        resp.setDestinationDir(job.getDestinationDir());
        resp.setSource(job.getSource());
        List<String> sourceDirs = new ArrayList<String>();
        for (JobFrom jobFrom : job.getSourceDirs())
        {
            sourceDirs.add(jobFrom.getSourceDir());
        }
        resp.setSourceDirs(sourceDirs);
        resp.setTotalNumberOfFiles(job.getTotalNumberOfFiles());
        resp.setCurrentNumberOfFiles(job.getCurrentNumberOfFiles());
        resp.setCreatedTime(job.getCreatedTimeStamp());
        resp.setCopyStartedTime(job.getCopyStartedTimeStamp());
        resp.setFinishedTime(job.getFinishedTimeStamp());
        return resp;
    }

    public Double calculateDisplayedSpeed(Job job)
    {
        double currentSpeed;
        long historicalStamp;
        long currentStamp;
        double timeDifferenceInSeconds;

        if (job.getUpdateTimeStamp() == null)
        {
            return null;
        }

        historicalStamp = job.getCopyStartedTimeStamp(); // the start of the copy
        currentStamp = job.getUpdateTimeStamp(); // now.
        timeDifferenceInSeconds = ((double) (currentStamp - historicalStamp)) / MILLIS_IN_A_SECOND;

        currentSpeed = (double) job.getCurrentBytes() / timeDifferenceInSeconds;

        return currentSpeed;
    }

    @Override
    @Transactional("service")
    public JobItem getJobStatus(String username, long jobId)
    {
        Job job = getJobForUser(username, jobId);
        return fromJobToJobItem(job);
    }

    @Override
    public String getUrl(Integer connectionId, String path)
    {
        WorkerNode workerNode = router.findWorker(connectionId);
        OpenConnectionParameter connDetails = workerNode.getConnectionDetails(connectionId);
        return connDetails.getProtocol() + "://" + connDetails.getServer() + path;
    }

    @Override
    @Transactional("service")
    public boolean stopJob(String username, Long jobId)
    {
        Job job = getJobForUser(username, jobId);
        return copyImpl.stopJob(router, job);
    }

    private Job getJobForUser(String username, Long jobId)
    {
        DmsUser user = findUser(username);
        Job job = Job.findJob(jobId);
        if (user == null || user.getId() == null || job == null || !user.getId().equals(job.getDmsUser().getId()))
        {
            throw new NotAuthorizedError(username + " doesn't own JobID " + jobId);
        }
        return job;
    }

    @Override
    public boolean checkForValidRouting(Integer connectionIdFrom, Integer connectionIdTo)
    {
        try
        {
            OpenConnectionParameter fromDetails = getConnectionDetails(connectionIdFrom);
            OpenConnectionParameter toDetails = getConnectionDetails(connectionIdTo);
            router.findCommonWorker(fromDetails.getProtocol(), fromDetails.getServer(), toDetails.getProtocol(),
                    toDetails.getServer());
            return true;
        }
        catch (WorkerNotFoundException e)
        {
            return false;
        }
    }
    
    @Override
    public boolean isUrlCatalogued(String url)
    {
        return repository.isUrlCatalogued(url);
    }
    
    private OpenConnectionParameter getConnectionDetails(Integer connectionId)
    {
        if (WorkerNode.HDD_CONNECTION_ID.equals(connectionId))
        {
            return new OpenConnectionParameter("hdd", "myPC", null, null);
        }
        return router.findWorker(connectionId).getConnectionDetails(connectionId);
    }
    
}
