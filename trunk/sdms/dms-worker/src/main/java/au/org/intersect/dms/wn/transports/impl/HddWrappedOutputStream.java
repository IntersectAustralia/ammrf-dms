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
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Wrapped OutputStream that keeps a reference to another object that can be recovered later.
 *
 * @version $Rev: 29 $
 * @param <T> the type of the reference
 */
class HddWrappedOutputStream<T> extends OutputStream
{
    private final T reference;
    private OutputStream outputStream;
	private Future<?> monitor;

    public HddWrappedOutputStream(T reference, OutputStream outputStream, Future<?> monitor)
    {
        this.reference = reference;
        this.outputStream = outputStream;
        this.monitor = monitor;
    }

    @Override
    public void write(int b) throws IOException
    {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        outputStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException
    {
        outputStream.flush();
    }
    
    public T getReference() throws IOException
    {
    	try {
    		monitor.get();
	        return reference;
		} catch (Exception e) {
			throw new IOException(e);
		}
    }

	@Override
    public void close() throws IOException
    {
        outputStream.flush();
        outputStream.close();
    }

}