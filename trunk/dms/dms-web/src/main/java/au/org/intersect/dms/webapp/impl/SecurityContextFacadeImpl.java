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

package au.org.intersect.dms.webapp.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import au.org.intersect.dms.core.errors.NotAuthorizedError;
import au.org.intersect.dms.webapp.SecurityContextFacade;

/**
 * Implementation of SecurityContextFacade
 * 
 * @version $Rev: 5 $
 * 
 */
public class SecurityContextFacadeImpl implements SecurityContextFacade
{
    public SecurityContext getContext()
    {
        return SecurityContextHolder.getContext();
    }

    public void setContext(SecurityContext securityContext)
    {
        SecurityContextHolder.setContext(securityContext);
    }

    @Override
    public String getAuthorizedUsername() throws NotAuthorizedError
    {
        Authentication authentication = getContext().getAuthentication();
        if (authentication == null)
        {
            throw new NotAuthorizedError("User is not logged in");
        }
        return authentication.getName();
    }
}
