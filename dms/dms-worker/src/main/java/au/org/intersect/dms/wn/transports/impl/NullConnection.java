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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.errors.TransportException;
import au.org.intersect.dms.core.service.BasicConnectionDetails;
import au.org.intersect.dms.wn.ConnectionParams;
import au.org.intersect.dms.wn.TransportConnection;

/**
 * Connection that just ignores everything
 *
 */
public class NullConnection implements TransportConnection
{

    private static final Logger LOGGER = LoggerFactory.getLogger(NullConnection.class);
    
    @Override
    public List<FileInfo> getList(String path) throws IOException
    {
        throw new TransportException("NULL::getList not implemented");
    }

    @Override
    public List<FileInfo> getList(String path, String filter) throws IOException
    {
        throw new TransportException("NULL::getList not implemented");
    }

    @Override
    public FileInfo getInfo(String path) throws IOException
    {
        throw new TransportException("NULL::getInfo not implemented");
    }

    @Override
    public boolean rename(String directory, String from, String to) throws IOException
    {
        throw new TransportException("NULL::rename not implemented");
    }

    @Override
    public boolean delete(FileInfo file) throws IOException
    {
        throw new TransportException("NULL::delete not implemented");
    }

    @Override
    public boolean createDir(String parent, String name) throws IOException
    {
        throw new TransportException("NULL::createDir not implemented");
    }

    @Override
    public InputStream openInputStream(String from) throws IOException
    {
        throw new TransportException("NULL::openInputStream not implemented");
    }

    @Override
    public boolean closeInputStream(String from, InputStream is) throws IOException
    {
        throw new TransportException("NULL::closeInputStream not implemented");
    }

    @Override
    public OutputStream openOutputStream(String to, long size) throws IOException
    {
        LOGGER.debug("Null OutputStream for: {}", to);
        return NullOutputStream.NULL_OUTPUT_STREAM;
    }

    @Override
    public boolean closeOutputStream(String to, OutputStream os) throws IOException
    {
        os.close();
        return true;
    }

    @Override
    public BasicConnectionDetails getBasicConnectionDetails()
    {
        return new ConnectionParams("null", "", "?", null);
    }

}
