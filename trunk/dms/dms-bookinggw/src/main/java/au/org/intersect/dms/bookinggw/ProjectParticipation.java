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

package au.org.intersect.dms.bookinggw;

/**
 * Participation of a user in a project; from participations and projects table.
 * 
 * @author carlos
 */
public class ProjectParticipation
{

    private Project project;

    private String comments;

    private String supervisorUsername;

    /**
     * Empty for marshalling.
     */
    public ProjectParticipation()
    {
    }

    /**
     * @param project
     *            the project.
     * @param comments
     *            the comments.
     * @param supervisorUsername
     *            the supervisor (as username).
     */
    public ProjectParticipation(Project project, String comments, String supervisorUsername)
    {
        this.project = project;
        this.comments = comments;
        this.supervisorUsername = supervisorUsername;
    }

    /**
     * @return the project
     */
    public Project getProject()
    {
        return this.project;
    }

    /**
     * @param project
     *            the project to set
     */
    public void setProject(Project project)
    {
        this.project = project;
    }

    /**
     * @return the comments
     */
    public String getComments()
    {
        return this.comments;
    }

    /**
     * @param comments
     *            the comments to set
     */
    public void setComments(String comments)
    {
        this.comments = comments;
    }

    /**
     * @return the supervisorUsername
     */
    public String getSupervisorUsername()
    {
        return this.supervisorUsername;
    }

    /**
     * @param supervisorUsername
     *            the supervisorUsername to set
     */
    public void setSupervisorUsername(String supervisorUsername)
    {
        this.supervisorUsername = supervisorUsername;
    }

}
