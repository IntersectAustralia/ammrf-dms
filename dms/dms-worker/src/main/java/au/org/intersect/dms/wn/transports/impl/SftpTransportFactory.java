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

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;

import au.org.intersect.dms.wn.ConnectionParams;
import au.org.intersect.dms.wn.TransportFactory;

/**
 * Connection pool to sftp servers.
 */
public class SftpTransportFactory extends BaseKeyedPoolableObjectFactory implements TransportFactory
{
    private static final String PROTOCOL = "sftp";
    
    private String sshKey;

    public void setSshKey(String sshKey) 
    {
        this.sshKey = sshKey;
    }

    @Override
    public String getSupportedProtocol()
    {
        return PROTOCOL;
    }

    @Override
    public Object makeObject(Object key) throws Exception
    {
        ConnectionParams details = (ConnectionParams) key;
        return new SftpConnection(details.getHostname(), details.getUsername(), sshKey);
    }

    @Override
    public void destroyObject(Object key, Object obj) throws Exception
    {
        ((SftpConnection) obj).close();
    }

}
