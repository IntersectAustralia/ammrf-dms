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

package au.org.intersect.dms.wn;

import java.io.IOException;
import java.util.List;

import au.org.intersect.dms.wn.impl.JobTracker;
import au.org.intersect.dms.wn.impl.TriggerHelper;

/**
 * A strategy to copy files and folders between transports, with optional metadata harvesting
 * 
 * @version $Rev: 29 $
 */
public interface CopyStrategy
{

    /**
     * Calculates the scope of a job
     * 
     * @param jobScope
     * @param conn
     * @param fromFiles
     * @param to
     * @return
     * @throws IOException
     */
    JobTracker doScope(JobTracker tracker, TransportConnection conn, List<String> fromFiles,
            String to) throws IOException;

    /**
     * Performs a copy/ingestion job
     * 
     * @param details
     * @param fromConn
     * @param toConn
     * @param instrument
     * @param trigger
     * @param toConnParams
     * @param fromConnParams
     * @throws IOException
     */
    void copy(final JobTracker tracker, final TransportConnection fromConn,
            final TransportConnection toConn, 
            TriggerHelper trigger) throws IOException;
}