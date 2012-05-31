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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.encrypt.Encrypter;
import au.org.intersect.dms.encrypt.EncryptionAgentException;
import au.org.intersect.dms.tunnel.HddUtil;
import au.org.intersect.dms.encrypt.impl.DesEncrypter;

/**
 * JobTracker allows interchange of request/replies between HDD and Applet. It does that by handling a TunnelExchange
 * (TE) object between the threads. There is a timeout for the first request, and subsequent request must be processed
 * faster or they timeout.
 * 
 * @version $Rev: 29 $
 */
public class TunnelJobTracker
{
    private static final int DEFAULT_INITIAL_TIMEOUT = 60000;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TunnelJobTracker.class);
    
    private int initialTimeout = DEFAULT_INITIAL_TIMEOUT;
    
    private int normalTimeout;
    
    private BlockingQueue<TunnelExchange> queue;
    private String jobId;
    private long reqId = 1;
    private long timeout = initialTimeout;
    private TunnelMap<TunnelExchange> replies;
    private long totalBytesUp;
    private long totalBytesDown;
    private Encrypter encrypter;

    public TunnelJobTracker(int size, String jobId)
    {
        this.jobId = jobId;
        queue = new ArrayBlockingQueue<TunnelExchange>(size);
        replies = new TunnelMap<TunnelExchange>();
    }
    
    public synchronized String getStatusString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[Req queue:" + queue.size());
        sb.append(", Reply pend:" + replies.size());
        sb.append(", replies#:" + (reqId - 1));
        sb.append(", bytes up#:" + totalBytesUp);
        sb.append(", bytes down#:" + totalBytesDown);
        sb.append("]");
        return sb.toString();
    }

    /**
     * Process a request for the HDD side. First, it puts the TE in the queue, where it is going the be
     * picked up by the /app thread. Then it puts the same object in a map by replyId, which
     * will be used by the reply )a new thread). Then waits for App (this will consume the input stream)
     * and then for Reply (this will pass the IS from the reply to our response.OutputStream) 
     * @param method
     * @param is
     * @param response
     * @throws IOException
     */
    public void processRequest(String method, ServletInputStream is, HttpServletResponse response) throws IOException
    {
        final TunnelExchange exch = new TunnelExchange(this, newRequestId(), method, is, response);
        try
        {
            if (!queue.offer(exch, getTimeout(), TimeUnit.MILLISECONDS))
            {
                throw new IOException("Job queue #" + jobId + " timeout");
            }
            replies.put(Long.toString(exch.getId()), new TunnelMap.ValueFactory<TunnelExchange>()
            {

                @Override
                public TunnelExchange create()
                {
                    return exch;
                }
            });
            exch.awaitForApp("HDD");
            exch.awaitForReply("HDD");
            incrementTotalBytes(exch);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            replies.remove(Long.toString(exch.getId()));
            LOGGER.info("Removed req#" + exch.getId());
            decreaseTimeout();
        }
    }

    private synchronized void incrementTotalBytes(TunnelExchange exch)
    {
        totalBytesUp += exch.getBytesUp();
        totalBytesDown += exch.getBytesDown();
    }

    /**
     * The /app thread calls this to copy 
     * @param response
     * @throws IOException 
     */
    public void processAppPhase(HttpServletResponse response) throws IOException
    {
        try
        {
            TunnelExchange exch = queue.poll(getTimeout(), TimeUnit.MILLISECONDS);
            exch.copyRequestHeaders(response);
            exch.copyRequest(response);
            exch.awaitForApp("APP");
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void processReplyPhase(String reqId, ServletInputStream inputStream, HttpServletResponse response)
        throws IOException
    {
        TunnelExchange exch = replies.get(reqId);
        exch.copyReplyHeaders();
        exch.copyReply(inputStream);
        exch.sendOk(response);
        exch.awaitForReply("APP");
    }

    private synchronized long newRequestId()
    {
        return reqId++;
    }
    
    private synchronized long getTimeout()
    {
        if (timeout <= 0)
        {
            timeout = DEFAULT_INITIAL_TIMEOUT;
        }
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(jobId + ", timeout=" + timeout);
        }
        return timeout;
    }

    private synchronized void decreaseTimeout()
    {
//        if (normalTimeout <= 0)
//        {
//            normalTimeout = DEFAULT_NORMAL_TIMEOUT;
//        }
//        timeout = normalTimeout;
    }

    public void setKey(SecretKey key) throws IOException
    {
        if (encrypter == null)
        {
            try
            {
                encrypter = new DesEncrypter(key);
            }
            catch (EncryptionAgentException e)
            {
                throw new IOException(e);
            }
        }
    }

    public String getJobId()
    {
        return jobId;
    }

    public String encryptString(String string) throws IOException
    {
        try
        {
            return HddUtil.convertByteToHexString(encrypter.encrypt(string.getBytes()));
        }
        catch (EncryptionAgentException e)
        {
            throw new IOException(e);
        }
    }

}
