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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

/**
 * Stores number of incorrect logins into cookie.
 * 
 * @author Andrey Chernyshov
 * 
 */
public class DmsAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler
{

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException
    {
        updateFailedLoginsCount(request, response);
        super.onAuthenticationFailure(request, response, exception);
    }

    private void updateFailedLoginsCount(HttpServletRequest request, HttpServletResponse response)
    {
        Cookie[] cookies = request.getCookies();
        int newValue = 1;
        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if (DmsCookies.FAILED_LOGINS.getKey().equals(cookie.getName()))
                {
                    int oldValue = Integer.parseInt(cookie.getValue());
                    newValue = oldValue + 1;
                }
            }
        }
        logger.error("Invalid login " + request.getParameter("j_username") + ", attempt #" + newValue);
        Cookie cookie = new Cookie(DmsCookies.FAILED_LOGINS.getKey(), String.valueOf(newValue));
        cookie.setPath(request.getContextPath());
        response.addCookie(cookie);
    }
}
