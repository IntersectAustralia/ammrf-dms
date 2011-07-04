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

package au.org.intersect.dms.util;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Utility class to format dates
 * 
 */
public class DateFormatter
{
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("dd/MM/yyyy h:mm:ss a");
    private static final DateTimeFormatter UTC_DATE_TIME_FORMATTER = ISODateTimeFormat.dateTimeNoMillis().withZone(
            DateTimeZone.UTC);
    private static final DateTimeFormatter XML_DATE_TIME_FORMATTER = ISODateTimeFormat.dateTimeNoMillis();

    public static DateTime parseDateTime(String text)
    {
        if (text != null && text.length() > 0)
        {
            return DATE_TIME_FORMATTER.parseDateTime(text);
        }
        else
        {
            return null;
        }
    }

    public static String formatDate2UTC(DateTime dateTime)
    {
        return UTC_DATE_TIME_FORMATTER.print(dateTime);
    }
    
    public static String formatDate2UTC(Long timestamp)
    {
        return UTC_DATE_TIME_FORMATTER.print(timestamp);
    }

    public static String formatDate2EST(Date date)
    {
        return formatDate2EST(date != null ? date.getTime() : null);
    }

    public static String formatDate2EST(Long timestamp)
    {
        if (timestamp != null)
        {
            return DATE_TIME_FORMATTER.print(timestamp);
        }
        else
        {
            return null;
        }
    }
    
    public static String formatXMLDate2EST(String xmlDate)
    {
        DateTime date = XML_DATE_TIME_FORMATTER.parseDateTime(xmlDate);
        return DATE_TIME_FORMATTER.print(date);
    }
}
