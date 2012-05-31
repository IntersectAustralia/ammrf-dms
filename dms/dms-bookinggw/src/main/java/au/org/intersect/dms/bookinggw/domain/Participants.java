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

import java.util.List;

import javax.persistence.Column;
import javax.persistence.TypedQuery;

import org.springframework.roo.addon.dbre.RooDbManaged;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

/**
 * TODO ???
 */
@RooJavaBean
@RooToString
@RooEntity(versionField = "", table = "participants", persistenceUnit = "bookinggwPU")
@RooDbManaged(automaticallyDelete = true)
public class Participants
{

    private static final String PARTICIPATIONS_FOR_USER = "select p from Participants p where p.agsUsers1.userid = ?";
    
    @Column(name = "supervisor")
    private Integer agsUsers;
    
    public static Participants[] findByUser(long userid)
    {
        TypedQuery<Participants> q = entityManager().createQuery(PARTICIPATIONS_FOR_USER, Participants.class);
        q.setParameter(1, userid);
        List<Participants> resp = q.getResultList();
        return resp.toArray(new Participants[resp.size()]);
    }

}
