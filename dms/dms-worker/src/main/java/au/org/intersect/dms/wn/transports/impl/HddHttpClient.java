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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import au.org.intersect.dms.encrypt.EncryptionAgent;
import au.org.intersect.dms.encrypt.EncryptionAgentException;
import au.org.intersect.dms.tunnel.HddConstants;
import au.org.intersect.dms.tunnel.HddUtil;

/**
 * Configures and handles the Apache's HttpClient
 * 
 * @version $Rev: 29 $
 */
public class HddHttpClient
{
    /**
     * methods supported
     * 
     * @version $Rev: 29 $
     */
    public enum HttpMethod
    {
        GET, PUT
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HddHttpClient.class);
    private static final int CONNECTION_SETUP_TIMEOUT = 300000;
    private static final int CHUNK_LENGTH_GET = 512; // get has smaller packages
    private static final int CHUNK_LENGTH_PUT = 4096; // we expect bigger packages here
    
    @Autowired
    @Qualifier("executorService")
    private ExecutorService executor;
    
    @Autowired
    private EncryptionAgent agent;
    
    private URL tunnelUrl;

    public void setTunnelUrl(String tunnelUrl) throws MalformedURLException
    {
        this.tunnelUrl = new URL(tunnelUrl);
    }

    public HddCommand makeCommand(HttpMethod method, final String jobId, String methodName)
        throws IOException
    {
        byte[] encjobId;
        
        try
        {
            encjobId = agent.process(jobId.getBytes());
        }    
        catch (EncryptionAgentException e)
        {
            throw new RuntimeException("The web-app could not encrypt your Job ID correctly; "
                    + "Therefore this job will not be executed.");
        }
        
        String encjobIdAsHexString = HddUtil.convertByteToHexString(encjobId);
        
        final HttpURLConnection conn = (HttpURLConnection) tunnelUrl.openConnection();
        conn.setRequestMethod(method.toString());
        conn.setRequestProperty(HddConstants.JOB_HEADER, encjobIdAsHexString);
        conn.setRequestProperty(HddConstants.METHOD_HEADER, methodName);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setAllowUserInteraction(false);
        conn.setConnectTimeout(CONNECTION_SETUP_TIMEOUT);
        conn.setChunkedStreamingMode(method == HttpMethod.GET ? CHUNK_LENGTH_GET : CHUNK_LENGTH_PUT);
        LOGGER.info("Connecting " + tunnelUrl + " for #" + jobId);
        LOGGER.info("Trusted keystore:{}", System.getProperty("javax.net.ssl.trustStore"));
        conn.connect();
        Callable<OutputStream> proc = new Callable<OutputStream>()
        {
            @Override
            public OutputStream call() throws Exception
            {
                return conn.getOutputStream();
            }
        };
        return new HddCommand(executor, conn, proc);
    }

}
