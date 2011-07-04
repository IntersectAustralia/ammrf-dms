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

package au.org.intersect.dms.webapp;

import org.springframework.security.core.context.SecurityContext;

import au.org.intersect.dms.core.errors.NotAuthorizedError;

/**
 * Facade for spring SecurityContext
 * 
 * @version $Rev: 5 $
 * 
 */
public interface SecurityContextFacade
{
    /**
     * Obtain the current <code>SecurityContext</code>.
     *
     * @return the security context (never <code>null</code>)
     */
    SecurityContext getContext();

    /**
     * Sets new SecurityContext. This method should be used in unit tests only.
     * 
     * @param securityContext
     */
    void setContext(SecurityContext securityContext);
    
    /**
     * Returns username of currenlty logged in user.
     * If no user is logged in then NotAuthorizedError is thrown
     * 
     * @return
     * @throws NotAuthorizedError if there is no logged in user
     */
    String getAuthorizedUsername() throws NotAuthorizedError;

}
