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
 * The BookingGatewayService interface.
 * 
 * @author carlos
 */
public interface BookingGatewayInterface
{

    /**
     * Returns true if the username/password combination is valid within the booking system. Supports feature: UI will
     * confirm username and password. Implementation should go to ags_users table, not relying on web app.
     * 
     * @param username
     *            username in ags_user
     * @param password
     *            password for this username
     * @return true if password is valid
     */
    boolean checkPassword(String username, String password);

    /**
     * Returns bookings for a user in a given range. Supports feature: UI will present all bookings available for the
     * user. Implementation should use ags_users and ez_bookings. The UI can use the fromDate and toDate to allow user
     * to properly select relevant booking.
     * 
     * @param username
     *            username in ags_user
     * @param instrument
     *            instrument id in ez_objects
     * @param fromDate
     *            date range, lower bound inclusive
     * @param toDate
     *            date range, upper bound inclusive
     * @return the bookings for this user or empty array if none
     */
    Booking[] getBookingsRange(String username, long instrument, Date fromDate, Date toDate);
    
    /**
     * Returns booking for given user, instrument and time
     * @param username
     * @param instrument
     * @param date
     * @return booking or null if no bookings for the given params where found
     */
    Booking getBooking(String username, long instrument, Date date);

    /**
     * Returns projects associated with a user. Supports feature: UI will present all projects available to the user.
     * 
     * @param username
     *            username in ags_user table
     * @return array of participating projects (can be empty, never null)
     */
    ProjectParticipation[] getProjectParticipations(String username);

    /**
     * Returns all metadata available in bookings system.
     * 
     * @param projectCode
     *            the project id
     * @param bookingId
     *            the booking id
     * @return the project details metadata
     */
    ProjectDetails getProjectDetails(long projectCode, long bookingId);

    /**
     * Returns details for the given user. Null if not found.
     * 
     * @param username
     * @return
     */
    UserDetails getUserDetails(String username);
    
    /**
     * Returns projects of this user from the booking system.
     * 
     * @param username
     *            name used in the booking system
     * @return
     */
    Project[] getProjects(String username);

}
