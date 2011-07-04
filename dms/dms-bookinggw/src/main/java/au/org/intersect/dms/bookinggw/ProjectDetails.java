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

import au.org.intersect.dms.util.Clone;

/**
 * @author carlos
 */
public class ProjectDetails extends Project
{

    /**
     * The booking id to use as investigation number.
     */
    private long bookingId;

    /**
     * The instrument name.
     */
    private String instrument;

    /**
     * The outline.
     */
    private String outline;

    /**
     * Investigators and their roles.
     */
    private InvestigatorRole[] investigators;

    /**
     * Required for marshalling.
     */
    public ProjectDetails()
    {
        // nothing
    }

    /**
     * Full constructor.
     * 
     * @param projecCode
     *            the project id
     * @param title
     *            the title of the project
     * @param bookingId
     *            the bookingid
     * @param instrument
     *            the instrument description
     * @param outline
     *            the outline of the project
     * @param investigators
     *            investigators for the project
     */
    public ProjectDetails(long projecCode, String title, long bookingId, String instrument, String outline,
            InvestigatorRole[] investigators)
    {
        super(projecCode, title);
        this.bookingId = bookingId;
        this.instrument = instrument;
        this.outline = outline;
        this.investigators = Clone.clone(investigators);
    }

    /**
     * @return the outline
     */
    public String getOutline()
    {
        return this.outline;
    }

    /**
     * @param outline
     *            the outline to set
     */
    public void setOutline(String outline)
    {
        this.outline = outline;
    }

    /**
     * @return the investigators
     */
    public InvestigatorRole[] getInvestigators()
    {
        return Clone.clone(this.investigators);
    }

    /**
     * @param investigators
     *            the investigators to set
     */
    public void setInvestigators(InvestigatorRole[] investigators)
    {
        this.investigators = Clone.clone(investigators);
    }

    /**
     * @return the bookingId
     */
    public long getBookingId()
    {
        return this.bookingId;
    }

    /**
     * @param bookingId
     *            the bookingId to set
     */
    public void setBookingId(long bookingId)
    {
        this.bookingId = bookingId;
    }

    /**
     * @return the instrument
     */
    public String getInstrument()
    {
        return this.instrument;
    }

    /**
     * @param instrument
     *            the instrument to set
     */
    public void setInstrument(String instrument)
    {
        this.instrument = instrument;
    }

}
