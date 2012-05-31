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

package au.org.intersect.dms.encrypt.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.encrypt.EncryptionAgent;
import au.org.intersect.dms.encrypt.EncryptionAgentException;
import au.org.intersect.dms.tunnel.HddUtil;

/**
 * 
 * @author jake
 * 
 */

abstract class AbstractEncryptImpl implements EncryptionAgent
{
    // PKCS Padding v1.5 value
    private static final int BYTES_FOR_PADDING = 11;
    private static final Logger LOG = LoggerFactory.getLogger(AbstractEncryptImpl.class);
    private static final int BUFFER_SIZE = 128;
    private static final String CLASSPATH_PREFIX = "classpath:";
    private Key key;
    private int cipherMode;
    private Cipher cipher;
    private int blockSize;
    private String agentId;

    protected void init(Key key, int cipherMode) throws NoSuchAlgorithmException, NoSuchPaddingException,
        InvalidKeyException
    {
        this.key = key;
        blockSize = ((RSAKey) key).getModulus().bitLength() / Byte.SIZE
                - (cipherMode == Cipher.ENCRYPT_MODE ? BYTES_FOR_PADDING : 0);
        this.cipherMode = cipherMode;
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(cipherMode, key);
        LOG.info("{} initialized: {}, blockSize:{}", new Object[] {getClass().getSimpleName(),
            cipherMode == Cipher.ENCRYPT_MODE ? "ENCRYPT" : "DECRYPT", Integer.valueOf(blockSize)});
    }

    protected byte[] readBytesFromKeyFile(String keyFileName) throws IOException
    {
        LOG.info("Initializing {} from {}", new Object[] {getClass().getSimpleName(), keyFileName});
        agentId = getClass().getSimpleName() + "[" + keyFileName + "]";
        InputStream streamFromKeyFile;

        if (keyFileName.startsWith(CLASSPATH_PREFIX))
        {
            streamFromKeyFile = getClass().getResourceAsStream(keyFileName.substring(CLASSPATH_PREFIX.length()));
        }
        else
        {
            File keyFile = new File(keyFileName);
            streamFromKeyFile = new FileInputStream(keyFile);
        }
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int num = streamFromKeyFile.read(buffer);
            while (num >= 0)
            {
                bos.write(buffer, 0, num);
                num = streamFromKeyFile.read(buffer);
            }
            bos.close();
            return bos.toByteArray();
        }
        finally
        {
            streamFromKeyFile.close();
        }
    }

    public synchronized byte[] process(byte[] data) throws EncryptionAgentException
    {
        try
        {
            debug(" Input", data);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            cipher.init(cipherMode, key);
            copyStream(data, bos);
            return debug(" Output", bos.toByteArray());
        }
        catch (IOException e)
        {
            throw new EncryptionAgentException(e);
        }
        catch (InvalidKeyException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalBlockSizeException e)
        {
            throw new EncryptionAgentException(e);
        }
        catch (BadPaddingException e)
        {
            throw new EncryptionAgentException(e);
        }

    }

    private byte[] debug(String string, byte[] byteArray)
    {
        if (LOG.isDebugEnabled())
        {
            String what = HddUtil.convertByteToHexString(byteArray);
            LOG.debug(agentId + string + ":" + what);
        }
        return byteArray;
    }

    private void copyStream(byte[] data, OutputStream os) throws IOException, IllegalBlockSizeException,
        BadPaddingException
    {
        int p = 0;
        while (p < data.length)
        {
            int len = data.length - p > blockSize ? blockSize : data.length - p;
            byte[] block = cipher.doFinal(data, p, len);
            os.write(block);
            p += len;
        }
        os.close();
    }

}
