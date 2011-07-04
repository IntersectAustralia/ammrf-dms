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

package au.org.intersect.dms.wn.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import au.org.intersect.dms.core.errors.TransportException;
import au.org.intersect.dms.wn.TransportConnection;

/**
 * Template pattern for Input/Output streams grabbed from a connection
 * 
 * @version $Rev: 29 $
 */
public class TransportStreamTemplate
{
    public void withInputStream(TransportConnection conn, StreamCallback<InputStream> callback, String from)
    {
        withStream(true, conn, callback, from, 0L);
    }

    public void withOutputStream(TransportConnection conn, StreamCallback<OutputStream> callback, String to, long size)
    {
        withStream(false, conn, callback, to, size);
    }

    private static <S> void withStream(boolean mode, TransportConnection conn, StreamCallback<S> callbak,
            String target, long size)
    {
        Object is = null;
        try
        {
            is = mode ? conn.openInputStream(target) : conn.openOutputStream(target, size);
            callbak.withStream((S) is);
        }
        catch (IOException e)
        {
            throw new TransportException("Fail stream, input=" + mode, e);
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    if (mode)
                    {
                        conn.closeInputStream(target, (InputStream) is);
                    }
                    else
                    {
                        conn.closeOutputStream(target, (OutputStream) is);
                    }
                }
                catch (IOException e)
                {
                    throw new TransportException("Fail closing stream, input=" + mode, e);
                }
            }
        }
    }

}
