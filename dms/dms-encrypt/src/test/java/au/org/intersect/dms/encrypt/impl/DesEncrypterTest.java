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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.junit.Test;

import au.org.intersect.dms.tunnel.HddConstants;

public class DesEncrypterTest
{

    @Test
    public void testDesEncrypter() throws Exception
    {
        createDesEncrypter();
    }

    private DesEncrypter createDesEncrypter() throws Exception
    {
        DESKeySpec keySpec = new DESKeySpec("#a@10.M_+| \\".getBytes());
        SecretKey key = SecretKeyFactory.getInstance(HddConstants.KEY_ALGORITHM).generateSecret(keySpec);
        return new DesEncrypter(key);
    }

    @Test
    public void testEncryptStream() throws Exception
    {
        DesEncrypter des = createDesEncrypter();
        String plain = "THIS IS A PLAIN STRING";
        // encrypt phase
        InputStream is = new ByteArrayInputStream(plain.getBytes());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        des.encrypt(is, os);
        // decrypt phase
        is = new ByteArrayInputStream(os.toByteArray());
        os = new ByteArrayOutputStream();
        des.decrypt(is, os);
        os.close();
        assertEquals(plain, new String(os.toByteArray()));
    }

    @Test
    public void testEncryptByteArray() throws Exception
    {
        DesEncrypter des = createDesEncrypter();
        String plain = "THIS IS ANOTHER PLAIN STRING";
        String resp = new String(des.decrypt(des.encrypt(plain.getBytes())));
        assertEquals(plain, resp);
    }

}
