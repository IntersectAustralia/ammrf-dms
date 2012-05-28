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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.dms.core.catalogue.MetadataPopulator;
import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.core.service.dto.JobType;
import au.org.intersect.dms.service.JobService;
import au.org.intersect.dms.service.domain.DmsUser;
import au.org.intersect.dms.service.domain.Job;
import au.org.intersect.dms.service.domain.JobDetailMetadata;

/**
 * Job service
 * 
 */
@Transactional("service")
public class JobServiceImpl implements JobService
{

    @Autowired(required = false)
    private MetadataPopulator metadataPopulator;

    @Override
    public Job createJob(JobType type, String username, Long projectCode, String sourceDetails, List<String> sources,
            String destinationDetails, String targetDir)
    {
        DmsUser user = findOrCreate(username);
        Job job = new Job(type, user, sourceDetails, destinationDetails, targetDir, 0);
        job.setProjectCode(projectCode);
        job.addSourceDirs(sources);
        job.persist();
        return job;
    }

    @Override
    public void storeMetadata(Long jobId, String metadata)
    {
        Job job = findJob(jobId);

        String url = job.getDestinationUrl();

        String metadataXML = metadata;
        if (metadata == null || metadata.isEmpty())
        {
            if (metadataPopulator == null)
            {
                throw new IllegalStateException(
                        "Metadata populator is not configured. Check spring application context configuration.");
            }
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("url", url);
            metadataXML = metadataPopulator.getMetadata(params);
        }

        JobDetailMetadata jdm = new JobDetailMetadata();
        jdm.setJob(job);
        jdm.setUrl(url);
        jdm.setMetadataSchema(MetadataSchema.RIF_CS);
        jdm.setMetadata(metadataXML);

        jdm.persist();
        job.merge();
    }

    private Job findJob(Long jobId)
    {
        Job job = Job.findJob(jobId);
        if (job == null)
        {
            throw new IllegalArgumentException("Job with ID " + jobId + " not found");
        }
        return job;
    }

    private DmsUser findOrCreate(String username)
    {
        List<DmsUser> users = DmsUser.findDmsUsersByUsername(username).getResultList();
        if (users.size() == 0)
        {
            return createUser(username);
        }
        return users.get(0);
    }

    private DmsUser createUser(String username)
    {
        DmsUser user = new DmsUser(username);
        user.persist();
        return user;
    }

}
