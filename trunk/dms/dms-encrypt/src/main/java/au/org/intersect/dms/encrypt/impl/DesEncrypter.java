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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;

import au.org.intersect.dms.encrypt.Encrypter;
import au.org.intersect.dms.encrypt.EncryptionAgentException;

/**
 * Encrypter implementation using DES
 * 
 * @author carlos
 * 
 */
public class DesEncrypter implements Encrypter
{
    private static final int BUFFER_SIZE = 1024;
    private Cipher ecipher;
    private Cipher dcipher;
    private SecretKey key;

    public DesEncrypter(SecretKey key) throws EncryptionAgentException
    {
        this.key = key;
        try
        {
            ecipher = Cipher.getInstance("DES");
            dcipher = Cipher.getInstance("DES");
            ecipher.init(Cipher.ENCRYPT_MODE, key);
            dcipher.init(Cipher.DECRYPT_MODE, key);
        }
        catch (javax.crypto.NoSuchPaddingException e)
        {
            throw new EncryptionAgentException(e);
        }
        catch (java.security.NoSuchAlgorithmException e)
        {
            throw new EncryptionAgentException(e);
        }
        catch (java.security.InvalidKeyException e)
        {
            throw new EncryptionAgentException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.org.intersect.dms.encrypt.impl.Encrypter#encrypt(java.io.InputStream, java.io.OutputStream)
     */
    @Override
    public void encrypt(InputStream in, OutputStream out) throws EncryptionAgentException
    {
        try
        {
            ecipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] buf = new byte[BUFFER_SIZE];
            OutputStream outEnc = new CipherOutputStream(out, ecipher);
            int numRead = 0;
            while ((numRead = in.read(buf)) >= 0)
            {
                outEnc.write(buf, 0, numRead);
            }
            outEnc.close();
        }
        catch (IOException e)
        {
            throw new EncryptionAgentException(e);
        }
        catch (InvalidKeyException e)
        {
            // shouldn't happen
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.org.intersect.dms.encrypt.impl.Encrypter#decrypt(java.io.InputStream, java.io.OutputStream)
     */
    @Override
    public void decrypt(InputStream in, OutputStream out) throws EncryptionAgentException
    {
        try
        {
            dcipher.init(Cipher.DECRYPT_MODE, key);
            byte[] buf = new byte[BUFFER_SIZE];
            InputStream inEnc = new CipherInputStream(in, dcipher);
            int numRead = 0;
            while ((numRead = inEnc.read(buf)) >= 0)
            {
                out.write(buf, 0, numRead);
            }
            out.close();
        }
        catch (IOException e)
        {
            throw new EncryptionAgentException(e);
        }
        catch (InvalidKeyException e)
        {
            // shouldn't happen
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.org.intersect.dms.encrypt.impl.Encrypter#encrypt(byte[])
     */
    @Override
    public byte[] encrypt(byte[] data) throws EncryptionAgentException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        encrypt(new ByteArrayInputStream(data), os);
        return os.toByteArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.org.intersect.dms.encrypt.impl.Encrypter#decrypt(byte[])
     */
    @Override
    public byte[] decrypt(byte[] data) throws EncryptionAgentException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        decrypt(new ByteArrayInputStream(data), os);
        return os.toByteArray();
    }

}