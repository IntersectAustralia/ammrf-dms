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

package au.org.intersect.dms.bookinggw;

import java.util.Map;

/**
 * Metadata related methods
 *
 */
public interface BookingGatewayMetadataService
{

    /**
     * Returns metadata from booking system accociated with the provided project and booking. If projectCode and/or
     * bookingId is null or no metadata for the provided parameters can be found then metadata skeleton (template with
     * empty field values) is returned.
     * 
     * @param projectCode
     *            project code in the booking system
     * @param bookingId
     *            booking ID
     * @param destinationURL
     *            URL of datasets this metadata should be associated with
     * @return xml string of metadata (in RIF-CS format for example)
     */
    public abstract String getMetadata(Long projectCode, Long bookingId, String destinationURL);

    /**
     * Returns metadata populated with the provided parameters
     * 
     * @param params
     *            map of parameters to populate. <b>Key</b> is parameter name (place holder in the template),
     *            <b>Value</b> is parameter value (Can be string or list/array of strings).
     * @return
     */
    public abstract String getMetadata(Map<String, Object> params);

}