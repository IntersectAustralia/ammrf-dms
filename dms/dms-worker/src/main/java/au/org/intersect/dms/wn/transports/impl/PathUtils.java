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

/**
 * Path manipulation
 * 
 * @version $Rev: 29 $
 */
public class PathUtils
{

    private static final String ROOT = "/";

    public static String getParent(String path)
    {
        int pos = path.lastIndexOf('/');
        if (pos == -1)
        {
            throw new IllegalArgumentException(path + " must start with leading '/'");
        }
        if (pos == 0)
        {
            return ROOT;
        }
        else
        {
            return path.substring(0, pos);
        }
    }

    public static String getName(String path)
    {
        int pos = path.lastIndexOf('/');
        return path.substring(pos + 1);
    }

    public static String joinPath(String parentPath, String name)
    {
        if (ROOT.equals(parentPath))
        {
            return parentPath + name;
        }
        return parentPath + ROOT + name;
    }

    public static boolean isRoot(String path)
    {
        return ROOT.equals(path);
    }

    public static String getRelative(String path)
    {
        return path.substring(1);
    }

}
