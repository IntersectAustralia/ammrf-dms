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
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

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
    private ServletInputStream is;
    private HttpServletResponse response;
    private long bytesUp;
    private long bytesDown;
    private TunnelJobTracker jobTracker;
        
    public TunnelExchange(TunnelJobTracker tunnelJobTracker, long id, String method, ServletInputStream inputStream,
            HttpServletResponse response)
    {
        this.jobTracker = tunnelJobTracker;
        this.id = id;
        this.method = method;
        this.is = inputStream;
        this.response = response;
        LOGGER.info(Thread.currentThread().getName() + " : new TunnelExchange " + id + "/" + method);
    }

    public void awaitForApp(String who) throws IOException
    {
        LOGGER.info(Thread.currentThread().getName() + " " + who + " waits (Step 1), req#" + id);
        await(phase1);
    }

    public void awaitForReply(String who) throws IOException
    {
        LOGGER.info(Thread.currentThread().getName() + " " + who + " waits (Step 2), req#" + id);
        await(phase2);
    }

    public long getId()
    {
        return id;
    }

    public void copyRequest(HttpServletResponse response) throws IOException
    {
        byte[] buffer = new byte[BUFFER_SIZE];
        try
        {
            ServletOutputStream os = response.getOutputStream();
            int len = is.read(buffer);
            while (len > 0)
            {
                os.write(buffer, 0, len);
                bytesUp += len;
                len = is.read(buffer);
            }
            os.close();
        }
        catch (IOException e)
        {
            LOGGER.error("copyRequest error will break barrier", e);
            breakBarrier();
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

    public void copyReplyHeaders()
    {
        response.setStatus(HttpServletResponse.SC_OK);
    }

    public void copyReply(ServletInputStream is) throws IOException
    {
        byte[] buffer = new byte[BUFFER_SIZE];
        try
        {
            ServletOutputStream os = response.getOutputStream();
            int len = is.read(buffer);
            while (len > 0)
            {
                os.write(buffer, 0, len);
                bytesDown += len;
                len = is.read(buffer);
            }
            os.close();
            is.close();
            // TODO !!! write "OK" to request.getOutputStream()
        }
        catch (IOException e)
        {
            LOGGER.error("copyReply error will break barrier", e);
            breakBarrier();
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
                phase.await();
            }
            else
            {
                throw new IOException("Other party failed");
            }
        }
        catch (InterruptedException e)
        {
            throw new IOException(e);
        }
        catch (BrokenBarrierException e)
        {
            throw new IOException(e);
        }
    }
    
    /**
     * If any thread waiting, it will break; if not, then it will break entering the exchange.
     */
    private void breakBarrier()
    {
        phase1.reset();
        phase2.reset();
        phase1 = null;
        phase2 = null;
    }

    public void sendOk(HttpServletResponse response) throws IOException
    {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        response.getOutputStream().close();
    }

}
