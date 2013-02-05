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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import au.org.intersect.dms.encrypt.EncryptionAgent;
import au.org.intersect.dms.encrypt.EncryptionAgentException;
import au.org.intersect.dms.tunnel.HddConstants;
import au.org.intersect.dms.tunnel.HddUtil;

/**
 * Home controller, nothing for now
 */
@RequestMapping("/tunnel/**")
@Controller
public class TunnelController
{
    private static final int STATUS_SERVER_ERROR = 500;
    private static final int JOB_QUEUE_SIZE = 4;
    private static final Logger LOGGER = LoggerFactory.getLogger(TunnelController.class);
    private static final long CLEANER_FREQUENCY = 10000; // 1 min

    @Autowired
    @Qualifier("privAgent")
    private EncryptionAgent privAgent;

    @Autowired
    @Qualifier("privApplet")
    private EncryptionAgent privApplet;
    
    private Timer cleaner = new Timer();
    
    public TunnelController()
    {
    	cleaner.scheduleAtFixedRate(cleanTask(), CLEANER_FREQUENCY, CLEANER_FREQUENCY);
    }
    
    private TimerTask cleanTask() {
		return new TimerTask()
		{
			@Override
			public void run() {
	        	TunnelMap<TunnelJobTracker> jobs = TunnelJobQueue.getInstance();
	        	if (jobs == null) return;
	            for (Entry<String, TunnelJobTracker> entry : jobs.keys())
	            {
	                String key = entry.getKey();
	                TunnelJobTracker jobTracker = entry.getValue();
	                if (jobTracker.stopped())
	                {
	                	if (jobTracker.timeStopped() < System.currentTimeMillis() - CLEANER_FREQUENCY)
	                	{
	                		jobs.remove(key);
	                	}
	                }
	            }
			}
		};
	}

	@RequestMapping(value = "/keys")
    public void keys(HttpServletResponse response)
    {
        response.setContentType("text/plain");
        try
        {
        	TunnelMap<TunnelJobTracker> jobs = TunnelJobQueue.getInstance();
            PrintWriter pw = new PrintWriter(response.getOutputStream());
            Set<Entry<String, TunnelJobTracker>> entries = jobs.keys();
            pw.println("Keys: " + jobs.size());
            for (Entry<String, TunnelJobTracker> entry : entries)
            {
                String key = entry.getKey();
                TunnelJobTracker jobTracker = entry.getValue();
                String status = jobTracker != null ? jobTracker.getStatusString() : " Expired";
                pw.println(key + ":" + status);
            }
            pw.println("------------------------------------");
            pw.close();
        }
        catch (IOException e)
        {
            LOGGER.error("Error /keys", e);
            response.setStatus(STATUS_SERVER_ERROR);
        }
    }
    
    public TunnelJobTracker createJobQueue(String jobId)
    {
    	return getOrCreateJobTracker(jobId);
    }

    @RequestMapping(value = "/app")
    public void app(HttpServletRequest request, HttpServletResponse response) throws IOException,
        EncryptionAgentException
    {
        if (LOGGER.isDebugEnabled())
        {
            debugHeaders("APP", request);
        }

        // TODO CHECKSTYLE-OFF: IllegalInstantiation
        try
        {
            String jobId = decryptJobId(request);
            SecretKey key = getRequestKey(request);
            String id = request.getHeader(HddConstants.REQID_HEADER);
            TunnelJobTracker tracker = getJobTracker(jobId);
            tracker.setKey(key);
            if (id == null)
            {
                tracker.processAppPhase(response);
            }
            else
            {
                tracker.processReplyPhase(id, request.getInputStream(), response);
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            LOGGER.error("Wrong key algorithm", e);
            throw new RuntimeException(e);
        }
        catch (InvalidKeyException e)
        {
            LOGGER.error("Wrong key", e);
            throw new IOException(e);
        }
        catch (InvalidKeySpecException e)
        {
            LOGGER.error("Wrong key", e);
            throw new IOException(e);
        }
    }

    private SecretKey getRequestKey(HttpServletRequest request) throws InvalidKeySpecException,
        NoSuchAlgorithmException, EncryptionAgentException, InvalidKeyException
    {
        byte[] keyBytes = privApplet.process(HddUtil.convertHexToByteArray(request.getHeader(HddConstants.KEY_HEADER)));
        return SecretKeyFactory.getInstance(HddConstants.KEY_ALGORITHM).generateSecret(
                new DESKeySpec(keyBytes));
    }

    private void debugHeaders(String path, HttpServletRequest request)
    {
        LOGGER.debug(path + " " + request.getProtocol() + " [" + request.getMethod() + "]");
        Enumeration enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements())
        {
            String header = (String) enumeration.nextElement();
            LOGGER.debug(path + "| " + header + ": " + request.getHeader(header));
        }
    }

    private TunnelJobTracker getOrCreateJobTracker(final String jobId)
    {
        LOGGER.info("getOrMake #" + jobId);
    	TunnelMap<TunnelJobTracker> jobs = TunnelJobQueue.getInstance();
        return jobs.put(jobId, new TunnelMap.ValueFactory<TunnelJobTracker>()
        {

            @Override
            public TunnelJobTracker create()
            {
                return new TunnelJobTracker(JOB_QUEUE_SIZE, jobId);
            }
        });
    }

    private String decryptJobId(HttpServletRequest request) throws UnsupportedEncodingException,
        EncryptionAgentException
    {
        String resp = new String(privAgent.process(HddUtil.convertHexToByteArray(request
                .getHeader(HddConstants.JOB_HEADER))), "UTF8");
        Long.parseLong(resp);
        return resp;
    }

    private TunnelJobTracker getJobTracker(String jobId)
    {
        LOGGER.info("get #" + jobId);
    	TunnelMap<TunnelJobTracker> jobs = TunnelJobQueue.getInstance();
        return jobs.getOrWait(jobId);
    }

}
