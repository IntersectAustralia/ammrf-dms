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

import java.util.Date;

/**
 * A booking's basic data from ez_bookings table.
 * 
 * @author carlos
 */
public class Booking
{

    /**
     * bookingid column.
     */
    private long bookingId;

    /**
     * booking date.
     */
    private Date bookingDate;

    /**
     * from time and from minute.
     */
    private TimeMinute from;

    /**
     * to time and to minute.
     */
    private TimeMinute to;

    /**
     * comments.
     */
    private String comments;

    /**
     * Empty constructor.
     */
    public Booking()
    {
    }

    /**
     * Full constructor.
     * 
     * @param bookingId
     *            the bookingId
     * @param bookingDate
     *            the bookingDate
     * @param from
     *            the from time
     * @param to
     *            the to time
     * @param comments
     *            the comments
     */
    public Booking(long bookingId, Date bookingDate, TimeMinute from, TimeMinute to, String comments)
    {
        this.bookingId = bookingId;
        this.bookingDate = (Date) bookingDate.clone();
        this.from = from;
        this.to = to;
        this.comments = comments;
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
     * @return the bookingDate
     */
    public Date getBookingDate()
    {
        return (Date) this.bookingDate.clone();
    }

    /**
     * @param bookingDate
     *            the bookingDate to set
     */
    public void setBookingDate(Date bookingDate)
    {
        this.bookingDate = (Date) bookingDate.clone();
    }

    /**
     * @return the from
     */
    public TimeMinute getFrom()
    {
        return this.from;
    }

    /**
     * @param from
     *            the from to set
     */
    public void setFrom(TimeMinute from)
    {
        this.from = from;
    }

    /**
     * @return the to
     */
    public TimeMinute getTo()
    {
        return this.to;
    }

    /**
     * @param to
     *            the to to set
     */
    public void setTo(TimeMinute to)
    {
        this.to = to;
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

}
