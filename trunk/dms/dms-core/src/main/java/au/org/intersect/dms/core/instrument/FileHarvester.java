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

package au.org.intersect.dms.core.instrument;

import java.io.OutputStream;

/**
 * Creates the output stream to do processing of data being transfered in parallel. The Copy will call getSink() to
 * get an OutputStream in which to copy bytes to, and it will call the same MetadataHarvester::closeSink() with
 * the same object and letting it know whether the copy was ok or not. The harvester should use this information
 * to send an appropriate catalog event.
 * Note: implementations should typically use a different thread to grab the data from the OutputStream, otherwise
 * attempting to read from it (via piped streams for example) will block the current copying thread, producing a
 * deadlock.
 * @version $Rev: 29 $
 */
public interface FileHarvester
{

    /**
     * Return OutputStream to copy the bytes into
     * @param toPath 
     * @param jobId 
     * @return
     */
    OutputStream getSink();

    /**
     * Copier will call this at the end of copying the file, letting the harvester know if the copying was sucessful or
     * not.
     * @param harvester
     * @param ok
     */
    void closeSink(OutputStream harvester, boolean ok);

}
