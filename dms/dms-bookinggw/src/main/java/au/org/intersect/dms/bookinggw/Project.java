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
 * Basic data for a project.
 * 
 * @author carlos
 */
public class Project
{

    /**
     * the project code.
     */
    private long projectCode;

    /**
     * the title.
     */
    private String title;

    /**
     * Empty constructor.
     */
    public Project()
    {
    }

    /**
     * @param projectCode
     *            the code.
     * @param title
     *            the title of the project.
     */
    public Project(long projectCode, String title)
    {
        this.projectCode = projectCode;
        this.title = title;
    }

    /**
     * @return the projectCode
     */
    public long getProjectCode()
    {
        return this.projectCode;
    }

    /**
     * @param projectCode
     *            the projectCode to set
     */
    public void setProjectCode(long projectCode)
    {
        this.projectCode = projectCode;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return this.title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

}
