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

package au.org.intersect.dms.applet.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.applet.HddAccess;
import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.encrypt.Encrypter;
import au.org.intersect.dms.encrypt.EncryptionAgent;
import au.org.intersect.dms.encrypt.EncryptionAgentException;
import au.org.intersect.dms.encrypt.impl.DesEncrypter;
import au.org.intersect.dms.tunnel.HddConstants;
import au.org.intersect.dms.tunnel.HddUtil;

/**
 * Connects to the web-app.
 * 
 * TODO / THIS IS A TENTATIVE APPROACH, THIS CLASS NEEDS RE-DESIGN TO MEET FUNCTIONALITY OF APPLET
 * 
 * @version $Rev: 29 $
 */
public class PcConnection
{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PcConnection.class); 

    /**
     * HDD connection handler (download/upload)
     */
    public interface Handler
    {

        void handle(InputStream inputStream) throws IOException;

    }

    private static final int CHUNK_LENGTH_SMALL = 512; // get has smaller packages
    private static final int CHUNK_LENGTH_STREAM = 4096; // we expect bigger packages here
    private static final int CANCEL_RETRY_MS = 1000;
    private static final int RETRY_TIMES = 3;

    private URL url;
    private String encJobId;
    private boolean finished;
    private HddAccess hddAccess;
    private SecretKey secretKey;
    private String secretString;
    private Encrypter encrypter;

    public PcConnection(String tunnelUrl, String encJobId, HddAccess hddAccess, EncryptionAgent agent)
        throws IOException
    {
        LOGGER.info("Connecting:{}", tunnelUrl);
        url = new URL(tunnelUrl);
        this.encJobId = encJobId;
        this.hddAccess = hddAccess;
        try
        {
            KeyGenerator gen = KeyGenerator.getInstance(HddConstants.KEY_ALGORITHM);
            gen.init(new SecureRandom());
            this.secretKey = gen.generateKey();
            this.secretString = HddUtil.convertByteToHexString(agent.process(secretKey.getEncoded()));
            this.encrypter = new DesEncrypter(secretKey);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
        catch (EncryptionAgentException e)
        {
            throw new RuntimeException(e);
        }
        finished = false;
    }

    public void run()
    {
        while (!finished)
        {
            // TODO CHECKSTYLE-OFF: IllegalCatch
            try
            {
                LOGGER.info("{}> ", new Date().toString());
                doWork();
            }
            catch (Exception e)
            {
                LOGGER.error("Exception during work", e);
                finished = true;
            }
        }
    }

    private void doWork() throws IOException
    {
        HttpURLConnection conn = makeCommandRequest();
        if (HddUtil.isStatusOk(conn.getResponseCode()))
        {           
            try
            {
                String method = decryptString(conn.getHeaderField(HddConstants.METHOD_HEADER));
                String reqId = decryptString(conn.getHeaderField(HddConstants.REQID_HEADER));
                Long.parseLong(reqId);
                processRequest(method, reqId, conn.getInputStream());
            }
            catch (EncryptionAgentException e)
            {
                throw new IOException("The request ID associated with a recently executed "
                        + "command was unable to be decrypted correctly by the applet; "
                        + "job will not be executed.");
            }
            catch (NumberFormatException e)
            {
                throw new IOException("The request ID associated with a recently executed "
                        + "command was unable to be decrypted correctly by the applet; "
                        + "job will not be executed.");
            }
        }
        else
        {
            if (conn.getContentType() != null)
            {
                LOGGER.info("--------------------------\n{}--------------\n", conn.getContent().toString());
            }
            conn.disconnect();
            throw new IOException("HTTP Status: " + conn.getResponseCode());
        }
    }
    
    private String decryptString(String string) throws EncryptionAgentException
    {
        // TODO CHECKSTYLE-OFF: IllegalInstantiation
        try
        {
            return new String(encrypter.decrypt(HddUtil.convertHexToByteArray(string)));
        }
        catch (EncryptionAgentException e)
        {
            throw new EncryptionAgentException(e);
        }
    }

    private HttpURLConnection makeCommandRequest() throws IOException
    {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty(HddConstants.KEY_HEADER, secretString);
        conn.setRequestProperty(HddConstants.JOB_HEADER, encJobId);
        return conn;
    }

    private void processRequest(String method, String reqId, InputStream is) throws IOException
    {
        LOGGER.info("PC-Req:{}, #{}", method, reqId);
        
        if (HddConstants.METHOD_GETLIST.equals(method))
        {
            processGetList(reqId, is);
        }
        else if (HddConstants.METHOD_CREATEDIR.equals(method))
        {
            processCreateDir(reqId, is);
        }
        else if (HddConstants.METHOD_DELETE.equals(method))
        {
            processDelete(reqId, is);
        }
        else if (HddConstants.METHOD_OPENOUTPUTSTREAM.equals(method))
        {
            processWriteStream(reqId, is);
        }
        else if (HddConstants.METHOD_OPENINPUTSTREAM.equals(method))
        {
            processReadStream(reqId, is);
        }
        else if (HddConstants.METHOD_FINALIZE.equals(method))
        {
            processFinalize(reqId, is);
        }
        else
        {
            throw new IOException("Invalid request method");
        }
    }

    private void processGetList(String reqId, InputStream is) throws IOException
    {
        String path = HddUtil.deserialize(is, String.class);
        FileInfo[] resp = hddAccess.getList(path);
        doReply(reqId, resp);
    }

    private void processCreateDir(String reqId, InputStream is) throws IOException
    {
        String[] params = new String[0];
        params = HddUtil.deserialize(is, params.getClass());
        Boolean resp = hddAccess.createDir(params[0], params[1]);
        doReply(reqId, resp);
    }

    private void processDelete(String reqId, InputStream is) throws IOException
    {
        String path = "";
        path = HddUtil.deserialize(is, path.getClass());
        Boolean resp = hddAccess.delete(path);
        doReply(reqId, resp);
    }

    private void processWriteStream(String reqId, InputStream is) throws IOException
    {
        String[] params = new String[0];
        params = HddUtil.deserializeSpecial(is, params.getClass());
        Boolean resp = hddAccess.writeStream(params[0], Long.parseLong(params[1]), is);
        doReply(reqId, resp);
    }

    private void processReadStream(String reqId, InputStream is) throws IOException
    {
        String name = "";
        name = HddUtil.deserializeSpecial(is, name.getClass());
        doReplyWithStream(reqId, name);
    }

    private void processFinalize(String reqId, InputStream is) throws IOException
    {
        LOGGER.info("Finalizing...");
        is.close();
        finished = true;
        doReply(reqId, Boolean.TRUE);
    }

    private void doReply(String reqId, Object resp) throws IOException
    {
        HttpURLConnection conn = makeConnection(reqId, CHUNK_LENGTH_SMALL);
        HddUtil.serialize(conn.getOutputStream(), (Serializable) resp);
        finalizeConnection(reqId, conn);
    }

    private void doReplyWithStream(String reqId, String name) throws IOException
    {
        HttpURLConnection conn = makeConnection(reqId, CHUNK_LENGTH_STREAM);
        hddAccess.readStream(name, conn.getOutputStream());
        finalizeConnection(reqId, conn);
    }

    private void finalizeConnection(String reqId, HttpURLConnection conn) throws IOException
    {
        int code = conn.getResponseCode();
        if (!HddUtil.isStatusOk(code))
        {
            throw new IOException("Error, status: " + code);
        }
        else
        {
            LOGGER.info("PC-Reply #{} DONE", reqId);
        }
    }

    private HttpURLConnection makeConnection(String reqId, int chunkLength) throws IOException
    {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty(HddConstants.JOB_HEADER, encJobId);
        conn.setRequestProperty(HddConstants.KEY_HEADER, secretString);
        conn.setRequestProperty(HddConstants.REQID_HEADER, reqId);
        conn.setDoOutput(true);
        conn.setChunkedStreamingMode(chunkLength);
        return conn;
    }

    public void stop()
    {
        finished = true;
        hddAccess.stop();
    }

}