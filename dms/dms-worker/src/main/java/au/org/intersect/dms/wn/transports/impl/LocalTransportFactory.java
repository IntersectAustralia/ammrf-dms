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

package au.org.intersect.dms.wn.transports.impl;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;

import au.org.intersect.dms.core.errors.NotAuthorizedError;
import au.org.intersect.dms.wn.ConnectionParams;
import au.org.intersect.dms.wn.TransportFactory;

/**
 * Connection pool to local drive.
 */
public class LocalTransportFactory extends BaseKeyedPoolableObjectFactory implements TransportFactory
{

    private static final String PROTOCOL = "local";

    private String username;
    
    private String password;    
    
    private File rootDir;

    public void setRootPath(String local)
    {
        rootDir = new File(local);
        if (!rootDir.exists() || !rootDir.isDirectory())
        {
            throw new RuntimeException("[" + local + "] is wrong root directory for LocalPool");
        }
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Override
    public Object makeObject(Object key) throws Exception
    {
        ConnectionParams details = (ConnectionParams) key;
        if (!StringUtils.defaultString(username).equals(StringUtils.defaultString(details.getUsername())))
        {
            throw new NotAuthorizedError();
        }

        if (!StringUtils.defaultString(password).equals(StringUtils.defaultString(details.getPassword())))
        {
            throw new NotAuthorizedError();
        }

        return new LocalConnection(rootDir, details.getHostname());
    }

    @Override
    public String getSupportedProtocol()
    {
        return PROTOCOL;
    }

}
