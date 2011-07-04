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

package au.org.intersect.dms.webapp.domain;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

/**
 * User (the same as {@link au.org.intersect.dms.service.domain.DmsUser}).
 * Had to create this one because Spring Roo can't handle entiries from different module.
 */
@Entity
@RooJavaBean
@RooToString
@RooEntity(finders = { "findDmsUsersByUsername" }, persistenceUnit = "webPU")
public class DmsUser
{

    @Column(unique = true, nullable = false)
    private String username;
    
    private boolean admin;

    public DmsUser()
    {
    }

    public DmsUser(String username)
    {
        this.username = username;
    }
}
