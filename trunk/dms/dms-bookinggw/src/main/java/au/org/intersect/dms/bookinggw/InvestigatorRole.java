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

import java.io.Serializable;

/**
 * Investigator role relationship.
 * 
 * @author carlos
 */
public class InvestigatorRole implements Serializable
{

    /**
     * The userId.
     */
    private long userId;

    /**
     * From users.title.
     */
    private String title;

    /**
     * Calculated from user_fname and user_lname.
     */
    private String initials;

    /**
     * Calculated from user_fname.
     */
    private String firstName;

    /**
     * Calculated from user_fname.
     */
    private String middleName;

    /**
     * From user_lname.
     */
    private String lastName;

    /**
     * Organisation name.
     */
    private String affiliation;

    /**
     * Organisation id.
     */
    private String affiliationId;

    /**
     * Default.
     */
    public InvestigatorRole()
    {
    }

    /**
     * Full constructor.
     * 
     * @param userId
     *            the userid
     * @param nameParts
     *            the parts of the name as array
     * @param affiliation
     *            the organization name
     * @param affiliationId
     *            the organization id
     */
    public InvestigatorRole(long userId, String[] nameParts, String affiliation,
            String affiliationId)
    {
        this.userId = userId;
        int i = 0;
        this.title = nameParts[i++];
        this.firstName = nameParts[i++];
        this.middleName = nameParts[i++];
        this.lastName = nameParts[i++];
        this.initials = nameParts[i++];
        this.affiliation = affiliation;
        this.affiliationId = affiliationId;
    }

    /**
     * @return the userId
     */
    public long getUserId()
    {
        return userId;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(long userId)
    {
        this.userId = userId;
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

    /**
     * @return the initials
     */
    public String getInitials()
    {
        return this.initials;
    }

    /**
     * @param initials
     *            the initials to set
     */
    public void setInitials(String initials)
    {
        this.initials = initials;
    }

    /**
     * @return the firstName
     */
    public String getFirstName()
    {
        return this.firstName;
    }

    /**
     * @param firstName
     *            the firstName to set
     */
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    /**
     * @return the middleName
     */
    public String getMiddleName()
    {
        return this.middleName;
    }

    /**
     * @param middleName
     *            the middleName to set
     */
    public void setMiddleName(String middleName)
    {
        this.middleName = middleName;
    }

    /**
     * @return the lastName
     */
    public String getLastName()
    {
        return this.lastName;
    }

    /**
     * @param lastName
     *            the lastName to set
     */
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    /**
     * @return the affiliation
     */
    public String getAffiliation()
    {
        return this.affiliation;
    }

    /**
     * @param affiliation
     *            the affiliation to set
     */
    public void setAffiliation(String affiliation)
    {
        this.affiliation = affiliation;
    }

    /**
     * @return the affiliationId
     */
    public String getAffiliationId()
    {
        return this.affiliationId;
    }

    /**
     * @param affiliationId
     *            the affiliationId to set
     */
    public void setAffiliationId(String affiliationId)
    {
        this.affiliationId = affiliationId;
    }

}
