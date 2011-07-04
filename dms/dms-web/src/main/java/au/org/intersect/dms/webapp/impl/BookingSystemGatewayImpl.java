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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.dms.bookinggw.BookingGatewayInterface;
import au.org.intersect.dms.service.domain.DmsUser;

/**
 * Spring security hook into booking system
 */
public class BookingSystemGatewayImpl implements AuthenticationProvider
{
    @Autowired
    private MessageSourceAccessor messages;

    @Autowired
    private BookingGatewayInterface bookinggw;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
        if (token.getCredentials() == null)
        {
            throw new BadCredentialsException(messages.getMessage("security_login_badCredentials"));
        }
        if (!login(token.getName(), token.getCredentials().toString()))
        {
            throw new BadCredentialsException(messages.getMessage("security_login_badCredentials"));
        }

        DmsUser user = createUserIfNotExists(token.getName());
        List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
        roles.add(new GrantedAuthorityImpl("ROLE_USER"));
        if (user.isAdmin())
        {
            roles.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
        }

        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(token.getName(),
                authentication.getCredentials(), roles);
        result.setDetails(authentication.getDetails());
        return result;
    }

    @Override
    public boolean supports(Class<? extends Object> authentication)
    {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private boolean login(String username, String password)
    {
        return bookinggw.checkPassword(username, password);
    }

    @Transactional("service")
    public DmsUser createUserIfNotExists(String username)
    {
        List<DmsUser> users = DmsUser.findDmsUsersByUsername(username).getResultList();
        if (users.size() == 0)
        {
            DmsUser user = new DmsUser(username);
            user.persist();
            return user;
        }
        else
        {
            return users.get(0);
        }
    }

}
