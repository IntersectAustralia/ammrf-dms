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
import java.io.PipedInputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Wrapped OutputStream that keeps a reference to another object that can be recovered later.
 * 
 * @version $Rev: 29 $
 * @param <T>
 *            the type of the reference
 */
class HddWrappedInputStream<T> extends InputStream
{
    private InputStream inputStream;
	private Future<?> monitor;

    public HddWrappedInputStream(InputStream inputStream, Future<?> mon)
    {
        this.inputStream = inputStream;
        this.monitor = mon;
    }

	@Override
    public int read() throws IOException
    {
        return inputStream.read();
    }

    @Override
    public void close() throws IOException
    {
        try {
			inputStream.close();
			monitor.get();
		} catch (InterruptedException e) {
			throw new IOException(e);
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        return inputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        return inputStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException
    {
        return inputStream.skip(n);
    }

    @Override
    public int available() throws IOException
    {
        return inputStream.available();
    }

    @Override
    public synchronized void mark(int readlimit)
    {
        inputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException
    {
        inputStream.reset();
    }

    @Override
    public boolean markSupported()
    {
        return inputStream.markSupported();
    }

}