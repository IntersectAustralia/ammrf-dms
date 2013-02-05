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

/**
 * Constants for the HDD connection
 * 
 * @version $Rev: 29 $
 */
public class HddConstants
{
    public static final String JOB_HEADER = "X-dms-jobId";
    public static final String METHOD_HEADER = "X-dms-method";
    public static final String REQID_HEADER = "X-dms-reqid";
    public static final String KEY_HEADER = "X-key";
    public static final String KEY_ALGORITHM = "DES";

    public static final String METHOD_GETLIST = "getList";
    public static final String METHOD_DELETE = "delete";
    public static final String METHOD_CREATEDIR = "createDir";
    public static final String METHOD_RENAME = "rename";
    public static final String METHOD_OPENOUTPUTSTREAM = "openOutputStream";
    public static final String METHOD_OPENINPUTSTREAM = "openInputStream";
    public static final String METHOD_FINALIZE = "finalize";
}
