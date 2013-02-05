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

package au.org.intersect.json;

/**
 * Transforms basic types to json.
 *
 * @version $Rev: 29 $
 */
public class JsonTransformer
{

    private static final int ZERO_BITS_IN_MASK = 12;
    private static final int TOPBITS_MASK = 0xf000;
    private static final int BITS_PER_HEXDIGIT = 4;
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    private static final char[] SPECIAL_CHARS = new char[]{'"', '\\', '\b', '\f', '\n', '\r', '\t'};
    private static final String[] SPECIAL_REPLACE = new String[]{"\\\"", "\\\\", "\\b", "\\f", "\\n", "\\r", "\\t"};

    public static String transform(String s)
    {
        if (s == null)
        {
            return "null";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("\"");
        char[] chars = new char[s.length()];
        s.getChars(0, s.length(), chars, 0);
        for (int i = 0; i < chars.length; i++)
        {
            char c = chars[i];
            int p = getPositionIfSpecial(c);
            if (p >= 0)
            {
                sb.append(SPECIAL_REPLACE[p]);
            }
            else if (Character.isISOControl(c))
            {
                unicode(sb, c);
            }
            else
            {
                sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }
    
    private static int getPositionIfSpecial(char c)
    {
        for (int i = 0; i < SPECIAL_CHARS.length; i++)
        {
            if (c == SPECIAL_CHARS[i])
            {
                return i;
            }
        }
        return -1;
    }

    public static String transform(long v)
    {
        return Long.toString(v);
    }

    public static String transform(int v)
    {
        return Integer.toString(v);
    }

    private static void unicode(StringBuffer sb, char c)
    {
        sb.append("\\u");
        int n = c;
        for (int i = 0; i < BITS_PER_HEXDIGIT; ++i)
        {
            int digit = (n & TOPBITS_MASK) >> ZERO_BITS_IN_MASK;
            sb.append(String.valueOf(HEX[digit]));
            n <<= BITS_PER_HEXDIGIT;
        }
    }

}
