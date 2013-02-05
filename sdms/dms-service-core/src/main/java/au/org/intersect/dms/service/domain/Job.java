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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import au.org.intersect.dms.core.service.dto.JobStatus;
import au.org.intersect.dms.core.service.dto.JobType;

/**
 * A copy job
 * 
 * TODO implement state transition check
 */
@Entity
@RooJavaBean
@RooToString
@RooEntity(persistenceUnit = "servicePU")
public class Job
{
    private static final String SLASH = "/";

    @Column(length = 8*1024)
    private String source;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<JobFrom> sourceDirs;

    @Column(length = 8*1024)
    private String destination;
    
    @Column(length = 8*1024)
    private String destinationDir;

    private int totalNumberOfDirectories;
    private int totalNumberOfFiles;
    private long totalBytes;
    private int currentNumberOfDirectories;
    private int currentNumberOfFiles;
    private long currentBytes;
    private long createdTimeStamp;

    @Column(nullable = true)
    private Long updateTimeStamp;

    @Column(nullable = true)
    private Long copyStartedTimeStamp;

    @Column(nullable = true)
    private Long finishedTimeStamp;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Enumerated(EnumType.STRING)
    private JobType type;

    @ManyToOne(optional = false)
    private DmsUser dmsUser;

    private int workerId;

    private Double averageSpeed;
    
    private Long projectCode;

    public Job(JobType type, DmsUser user, String source, String destination, String destinationDir, int workerId)
    {
        this.type = type;
        this.createdTimeStamp = System.currentTimeMillis();
        this.status = JobStatus.CREATED;
        this.dmsUser = user;
        this.source = source;
        this.sourceDirs = new ArrayList<JobFrom>();
        this.destination = destination;
        this.destinationDir = destinationDir;
        this.workerId = workerId;
        this.averageSpeed = null;
    }

    public void addSourceDirs(List<String> sourceDirs)
    {
        for (String sourceDir : sourceDirs)
        {
            this.sourceDirs.add(new JobFrom(this, sourceDir));
        }
    }

    /**
     * Sets new job status according to state machine {@link JobStatusTransition}
     * 
     * @param status
     *            new status
     */
    public void setStatus(JobStatus status)
    {
        if (this.status == status)
        {
            return;
        }
        if (JobStatusTransition.isValidTransition(this.status, status))
        {

            this.status = status;
        }
        else
        {
            throw new IllegalArgumentException("Job status transition from " + this.status + " to " + status
                    + " is not permitted");
        }
    }

    public String getDestinationUrl()
    {
        StringBuilder url = new StringBuilder(destination).append(destinationDir);
        if (sourceDirs != null && !sourceDirs.isEmpty())
        {
            String sourcePath = sourceDirs.get(0).getSourceDir();
            url.append(sourcePath.substring(sourcePath.lastIndexOf(SLASH)
                    + (SLASH.equals(destinationDir) || SLASH.equals(sourcePath) ? 1 : 0)));
        }
        return url.toString();
    }

}
