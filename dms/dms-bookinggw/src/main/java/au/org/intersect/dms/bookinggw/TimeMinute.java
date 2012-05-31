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
 * Captures an Hour:Minute representation in the ezthis.bookings table.
 * 
 * @author carlos
 */
public class TimeMinute
{

    /**
     * Hour field.
     */
    private double hour;

    /**
     * Minute field.
     */
    private double minute;

    /**
     * Empty constructor for marshalling.
     */
    public TimeMinute()
    {
        // nothing
    }

    /**
     * Quick constructor for application.
     * 
     * @param hour
     *            the hour
     * @param minute
     *            the minute
     */
    public TimeMinute(double hour, double minute)
    {
        this.hour = hour;
        this.minute = minute;
    }

    /**
     * @return the hour
     */
    public double getHour()
    {
        return this.hour;
    }

    /**
     * @param hour
     *            the hour to set
     */
    public void setHour(double hour)
    {
        this.hour = hour;
    }

    /**
     * @return the minute
     */
    public double getMinute()
    {
        return this.minute;
    }

    /**
     * @param minute
     *            the minute to set
     */
    public void setMinute(double minute)
    {
        this.minute = minute;
    }

}
