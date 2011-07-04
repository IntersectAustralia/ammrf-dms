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

package au.org.intersect.dms.wn;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * Holds the key for the transport pool. As this object is handed down to the factory object, 
 * we put all the info needed to create the connection to the
 * server.
 * @version $Rev: 29 $
 */
public final class ConnectionParams implements Serializable
{

    private static final long serialVersionUID = 6733033199259181074L;

    private String hostname;

    private String username;

    private String password;

    private String protocol;

    public ConnectionParams(String protocol, String hostname, String username, String password)
    {
        super();
        this.protocol = protocol;
        this.hostname = hostname;
        this.username = username;
        this.password = password;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public String getHostname()
    {
        return hostname;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((protocol == null) ? 0 : protocol.hashCode());
        result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        ConnectionParams other = (ConnectionParams) obj;
        return StringUtils.equals(protocol, other.protocol) && StringUtils.equals(hostname, other.hostname)
                && StringUtils.equals(username, other.username) && StringUtils.equals(password, other.password);
    }

}
