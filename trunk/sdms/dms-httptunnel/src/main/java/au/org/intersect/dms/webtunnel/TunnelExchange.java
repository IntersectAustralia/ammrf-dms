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

package au.org.intersect.dms.webtunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.tunnel.HddConstants;

/**
 * Exchange in the tunnel controller between threads
 * 
 * @version $Rev: 29 $
 */
public class TunnelExchange
{
    private static final int BUFFER_SIZE = 4096;
    private static final Logger LOGGER = LoggerFactory.getLogger(TunnelExchange.class);
    
    private CyclicBarrier phase1 = new CyclicBarrier(2);
    private CyclicBarrier phase2 = new CyclicBarrier(2);
    private long id;
    private String method;
    private InputStream inputStream;
    private OutputStream outputStream;
    private long bytesUp;
    private long bytesDown;
    private TunnelJobTracker jobTracker;
        
    public TunnelExchange(TunnelJobTracker tunnelJobTracker, long id, String method, InputStream inputStream,
            OutputStream outputStream)
    {
        this.jobTracker = tunnelJobTracker;
        this.id = id;
        this.method = method;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        LOGGER.info("new TunnelExchange " + id + "/" + method);
    }

    public void awaitForApp(String who) throws IOException
    {
        LOGGER.info(who + " waits (Step 1), req#" + id + "/" + method + " [" + this.hashCode() + "]");
        await(phase1);
    }

    public void awaitForReply(String who) throws IOException
    {
        LOGGER.info(who + " waits (Step 2), req#" + id + "/" + method + " [" + this.hashCode() + "]");
        await(phase2);
    }

    public long getId()
    {
        return id;
    }

    public void copyRequest(HttpServletResponse response) throws IOException
    {
    	if (inputStream == null)
    	{
    		LOGGER.debug("NO INPUT-STREAM");
    		response.getOutputStream().close();
    		return;
    	}
        byte[] buffer = new byte[BUFFER_SIZE];
        try
        {
            ServletOutputStream os = response.getOutputStream();
            int len = inputStream.read(buffer);
            while (len > 0)
            {
                LOGGER.debug("copyRequest");
                os.write(buffer, 0, len);
                bytesUp += len;
                len = inputStream.read(buffer);
            }
            inputStream.close();
            os.flush();
            os.close();
        }
        catch (IOException e)
        {
            breakBarrier(e);
            throw e;
        }
    }

	public void copyRequestHeaders(HttpServletResponse response) throws IOException
    {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(HddConstants.REQID_HEADER, jobTracker.encryptString(Long.toString(id)));
        response.setHeader(HddConstants.JOB_HEADER, jobTracker.getJobId());
        response.setHeader(HddConstants.METHOD_HEADER, jobTracker.encryptString(method));
    }

    public void copyReply(ServletInputStream is) throws IOException
    {
        byte[] buffer = new byte[BUFFER_SIZE];
        try
        {
            int len = is.read(buffer);
            while (len > 0)
            {
                LOGGER.debug("copyReply");
                outputStream.write(buffer, 0, len);
                bytesDown += len;
                len = is.read(buffer);
            }
            outputStream.close();
            is.close();
        }
        catch (IOException e)
        {
            breakBarrier(e);
            throw e;
        }
    }

    public long getBytesUp()
    {
        return bytesUp;
    }

    public long getBytesDown()
    {
        return bytesDown;
    }

    private void await(CyclicBarrier phase) throws IOException
    {
        try
        {
            if (phase != null)
            {
            	long curBytesUp = bytesUp;
            	long curBytesDown = bytesDown;
                boolean failed = false;
            	while (true)
            	{
                    // LOGGER.info("curBytesUp:"+curBytesUp+" curBytesDown:"+curBytesDown+", waiting:"+phase.getNumberWaiting());
                    try {
						phase.await(30, TimeUnit.SECONDS);
						return;
					} catch (TimeoutException e) {
	                    if (bytesUp == curBytesUp && bytesDown == curBytesDown) throw new IOException("IO Stalled communication",e);
	                    // else, wait again - must reset because Timeout also breaks barrier
                            // Note: there is a chance of a race condition here between the two threads using the barrier phase
                            // as one thread may break the barrier due to timeout and the other one may enter this loop at the same
                            // time and see the broken barrier condition. We just added a catch below this one as an attempt to fix this.
                            phase.reset();
                            failed = false;
			} catch(BrokenBarrierException e) {
                            if (failed) throw new IOException("Broken IO",e);
                            try { Thread.currentThread().sleep(5000); } catch (InterruptedException e2) { throw new IOException(e2); }
                            failed = true;
                        }
	             curBytesUp = bytesUp;
	             curBytesDown = bytesDown;
            	}
            }
            else
            {
                throw new IOException("Other party failed");
            }
        }
        catch (Exception e)
        {
        	breakBarrier(e);
            throw new IOException(e);
        }
    }
    
    /**
     * If any thread waiting, it will break; if not, then it will break entering the exchange.
     */
    private void breakBarrier(Exception e)
    {
    	jobTracker.stopJob();
        phase1.reset();
        phase2.reset();
    }

    public void sendOk(HttpServletResponse response) throws IOException
    {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        response.getOutputStream().close();
    }

}
