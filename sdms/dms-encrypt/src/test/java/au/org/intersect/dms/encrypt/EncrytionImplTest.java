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

package au.org.intersect.dms.encrypt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import au.org.intersect.dms.encrypt.impl.PrivateDecryptAgent;
import au.org.intersect.dms.encrypt.impl.PublicEncryptAgent;

public class EncrytionImplTest
{
    private PublicEncryptAgent pubImpl;
    private PrivateDecryptAgent priImpl;

    public EncrytionImplTest() throws Exception
    {
        pubImpl = new PublicEncryptAgent("classpath:/testPublicKey.der");
        priImpl = new PrivateDecryptAgent("classpath:/testPrivateKey.der");
    }

    @Test
    public void testRsaEncrypt()
    {
        try
        {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 40; i++)
            {
                sb.append("1234567890ABCDEFGHIJKLMNOPQRSTUVXWYZ" + i);
            }
            String jobId = sb.toString();
            byte[] plainText = jobId.getBytes("UTF8");
            byte[] encr = pubImpl.process(plainText);
            assertNotNull("Encrypted to Null!", encr);
            assertTrue("Encrypted data is empty", encr.length > 0);
            String resp = new String(priImpl.process(encr), "UTF8");
            assertEquals(jobId, resp);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String getHexString(byte[] b)
    {
        String result = "";
        for (int i = 0; i < b.length; i++)
        {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

}
