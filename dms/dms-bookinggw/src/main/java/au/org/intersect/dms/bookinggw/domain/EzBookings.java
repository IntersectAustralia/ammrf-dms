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

package au.org.intersect.dms.bookinggw.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

/**
 * Bookings
 */
@RooJavaBean
@RooToString
@RooEntity(versionField = "", table = "EZ_Bookings", persistenceUnit = "bookinggwPU")
@RooDbManaged(automaticallyDelete = true)
public class EzBookings
{

    private static final String BOOKING_RANGE = "select eb from EzBookings eb where eb.agsUsers.userid = :userid "
            + "and eb.objectid = :objectId and :fromDate <= eb.bookingdate and eb.bookingdate <= :toDate";

    public static EzBookings[] findByObjectAndDateRange(long userId, long objectId, Date from, Date to)
    {
        TypedQuery<EzBookings> q = entityManager().createQuery(BOOKING_RANGE, EzBookings.class);
        q.setParameter("userid", userId);
        q.setParameter("objectId", objectId);
        q.setParameter("fromDate", from);
        q.setParameter("toDate", to);
        List<EzBookings> resp = q.getResultList();
        return resp.toArray(new EzBookings[resp.size()]);
    }
}
