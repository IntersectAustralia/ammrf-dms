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

package au.org.intersect.dms.tunnel;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Utility class to serialize, de-serialize objects directly on in specials ways. Note: Methods here usually close the
 * input/output stream; except the ones named 'special'
 * 
 * @version $Rev: 29 $
 */
public class HddUtil
{
    private static final int HEXDIGIT_BITS = 4;
    private static final int HEXDIGIT_MASK = 0x0F;
    private static final char[] HEXDIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
        'C', 'D', 'E', 'F'};
    private static final int HEX_BASE = 16;
    private static final int OK_RANGE_MIN = 200;
    private static final int OK_RANGE_MAX = 299;

    public static boolean isStatusOk(int status)
    {
        return OK_RANGE_MIN <= status && status <= OK_RANGE_MAX;
    }
    
    /**
     * Serialize zero or more string parameters, closing the supplied OutputStream.
     * 
     * @param os
     * @param name
     * @throws IOException
     */
    public static void serialize(OutputStream os, String... params) throws IOException
    {
        serializeSpecial(os, params);
        os.close();
    }
    
    public static void serialize(OutputStream os, Serializable obj) throws IOException
    {
        serializeSpecial(os, obj);
        os.close();
    }

    private static void serializeSpecial(OutputStream os, Serializable obj) throws IOException
    {
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(obj);
    }
    /**
     * Serialize zero or more string parameters, but leaves the OutputStream <code>os</code> open for further output.
     * 
     * @param os
     * @param name
     * @throws IOException
     */
    public static void serializeSpecial(OutputStream os, String... param) throws IOException
    {
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(param);
    }

    /**
     * Reads from the supplied InputStream <code>is</code> String[] parameters (the opposite to serialize) closing the
     * stream afterwards.
     * 
     * @param is
     * @return
     * @throws IOException
     */
    public static <T> T deserialize(InputStream is, Class<T> clazz) throws IOException
    {
        T resp = deserializeSpecial(is, clazz);
        is.close();
        return resp;
    }

    /**
     * Like deserialize but leaves the supplied stream open.
     * @param is
     * @return
     * @throws IOException
     */
    public static <T> T deserializeSpecial(InputStream is, Class<T> clazz) throws IOException
    {
        Object obj = "";
        try
        {
            ObjectInputStream ois = new ObjectInputStream(is);
            obj = ois.readObject();
            return clazz.cast(obj);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        catch (ClassCastException e)
        {
            throw new IOException("Expecting " + clazz.getName() + ", found " + obj.getClass());
        }
    }

    public static String convertByteToHexString(byte[] b)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++)
        {
            sb.append(HEXDIGITS[(b[i] >> HEXDIGIT_BITS) & HEXDIGIT_MASK]); 
            sb.append(HEXDIGITS[b[i] & HEXDIGIT_MASK]); 
        }
        return sb.toString();
    }
    
    
    public static byte[] convertHexToByteArray(String hexString)
    {
        if ((hexString.length() % 2) != 0)
        {
            throw new IllegalArgumentException();
        }

        byte[] result = new byte[hexString.length() / 2];
        char[] enc = hexString.toCharArray();
        for (int i = 0; i < enc.length; i += 2)
        {
            StringBuilder curr = new StringBuilder(2);
            curr.append(enc[i]).append(enc[i + 1]);
            result[i / 2] = (byte) Integer.parseInt(curr.toString(), HEX_BASE);
        }
        return result;
    }

}
