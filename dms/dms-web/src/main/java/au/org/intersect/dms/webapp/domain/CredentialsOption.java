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

package au.org.intersect.dms.webapp.domain;

/**
 * Options for authentication for stock servers.
 * NONE: don't ask for credentials and don't suply them (null values are Ok)
 * DMS: don't ask for them but use current DMS credentials
 * FIXED: don't ask, but use the ones in the database
 * ASK: ask for credentials 
 * @version $Rev: 29 $
 */
public enum CredentialsOption
{
    /**
     * no u/p should be requested, the connect method accepts null.
     */
    NONE,
    /**
     * use the stock server's u/p to connect
     */
    FIXED,
    /**
     * ask the user
     */
    ASK;
}
