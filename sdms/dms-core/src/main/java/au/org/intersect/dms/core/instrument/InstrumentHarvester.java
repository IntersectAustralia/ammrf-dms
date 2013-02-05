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

import java.util.List;

import au.org.intersect.dms.core.service.dto.MetadataEventItem;

/**
 * Class that encapsulates basic knowledge on how to extract metadata for an instrument
 *
 * @version $Rev: 29 $
 */
public interface InstrumentHarvester extends MetadataAccumulator
{
    /**
     * Gets a MD harvester for a <b>file</b> path. It may return null if there is no MD known for that file
     * @param relativePath
     * @return
     */
    FileHarvester getHarvesterFor(String fromPath);

    /**
     * Tells the harvester, ingestion is about to start.
     * @param toDir
     */
    void harvestStart(String toDir);
    
    /**
     * Tells the harvester, ingestion finished.
     */
    void harvestEnd();

    /**
     * Returns metadata collected by this harvester.
     * 
     * @return metadata
     */
    List<MetadataEventItem> getMetadata();
    
    /**
     * Configure harvester with URL generator
     * @param urlCreator
     */
    void setUrlCreator(UrlCreator urlCreator);

    /**
     * Tells harvester a file has been successfully copied
     * @param to
     * @param size
     */
    void harvestFile(String to, long size);

    /**
     * Tells harvester a file has been successfully created
     * @param to
     */
    void harvestDirectory(String to);
}
