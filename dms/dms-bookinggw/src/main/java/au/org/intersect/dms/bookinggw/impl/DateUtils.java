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

package au.org.intersect.dms.bookinggw.impl;

import java.util.Date;

import org.joda.time.DateTime;

/**
 * Utility class to work with dates
 *
 */
public class DateUtils
{
    private static final int DAY_END_HOUR = 23;
    private static final int DAY_END_MINUTES = 59;
    private static final int DAY_END_SECONDS = 59;
    
    /**
     * Returns given date with zero time
     * 
     * @param date date to truncate time
     * @return
     */
    public static Date getFromDate(Date date)
    {
        DateTime dateTime = new DateTime(date);
        DateTime fromDate = dateTime.withTime(0, 0, 0, 0);
        return fromDate.toDate();
    }
    
    /**
     * Return given date with time set to 23:59:59
     * 
     * @param date date to round time
     * @return
     */
    public static Date getToDate(Date date)
    {
        DateTime dateTime = new DateTime(date);
        DateTime toDate = dateTime.withTime(DAY_END_HOUR, DAY_END_MINUTES, DAY_END_SECONDS, 0);
        return toDate.toDate();
    }
}
