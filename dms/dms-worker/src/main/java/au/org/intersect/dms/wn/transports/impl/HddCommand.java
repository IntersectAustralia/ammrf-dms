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
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import au.org.intersect.dms.core.errors.TransportError;
import au.org.intersect.dms.tunnel.HddUtil;

/**
 * Encapsulates processing a single HTTP command sent to the tunnel; the client has access to the OutputStream (to send
 * output) or to the InputStream to receive input. The synchronization is performed by performing one step a time via a
 * Call-able: in 'phase1', the connection is created and parameters sent, the client get the output stream as a result
 * of the associated future; the output stream returned is wrapped, so when we get closed the HddCommand issues a second
 * future to grab the input stream; clients doing getInputStream then will wait for that to execute and grab the data.
 * 
 * @version $Rev: 29 $
 */
class HddCommand
{
    private ExecutorService executor;
    private Future<OutputStream> future1;
    private Future<InputStream> future2;
    private HttpURLConnection conn;

    public HddCommand(ExecutorService executor, HttpURLConnection conn, Callable<OutputStream> phase1)
    {
        this.executor = executor;
        this.conn = conn;
        this.future1 = executor.submit(phase1);
    }

    public OutputStream getOutputStream()
    {
        try
        {
            return new HddWrappedOutputStream<HddCommand>(this, future1.get())
            {

                @Override
                public void close() throws IOException
                {
                    super.close();
                    // starts phase2
                    future2 = executor.submit(new Callable<InputStream>()
                    {

                        @Override
                        public InputStream call() throws Exception
                        {
                            int httpCode = conn.getResponseCode();
                            if (HddUtil.isStatusOk(httpCode))
                            {
                                return conn.getInputStream();
                            }
                            else
                            {
                                throw new TransportError("HTTP Tunnel Status " + httpCode);
                            }
                        }
                    });
                }

            };
        }
        catch (InterruptedException e)
        {
            throw new TransportError(e);
        }
        catch (ExecutionException e)
        {
            throw new TransportError(e);
        }
    }

    public InputStream getInputStream() throws InterruptedException, ExecutionException
    {
        return new WrappedInputStream(future2.get());
    }

    public boolean isCancelled()
    {
        return future1.isCancelled();
    }

    /**
     * Wraps an InputStream keeping a reference to enclosing object
     * 
     * @version $Rev: 29 $
     */
    private static class WrappedInputStream extends InputStream
    {

        private InputStream inputStream;

        public WrappedInputStream(InputStream inputStream)
        {
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException
        {
            return inputStream.read();
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
        public void close() throws IOException
        {
            inputStream.close();
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

}