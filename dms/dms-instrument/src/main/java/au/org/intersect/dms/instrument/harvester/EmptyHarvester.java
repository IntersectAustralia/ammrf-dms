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

package au.org.intersect.dms.instrument.harvester;

import java.util.Collections;
import java.util.List;

import au.org.intersect.dms.core.instrument.FileHarvester;
import au.org.intersect.dms.core.instrument.InstrumentHarvester;
import au.org.intersect.dms.core.instrument.UrlCreator;
import au.org.intersect.dms.core.service.dto.MetadataEventItem;

/**
 * Harvester which does no harvesting.
 *
 * @version $Rev: 29 $
 */
public class EmptyHarvester implements InstrumentHarvester
{

    @Override
    public void addMetadataItem(MetadataEventItem item)
    {
    }

    @Override
    public FileHarvester getHarvesterFor(String fromPath)
    {
        return null;
    }

    @Override
    public void harvestStart(String toDir)
    {
    }

    @Override
    public void harvestEnd()
    {
    }

    @Override
    public List<MetadataEventItem> getMetadata()
    {
        return Collections.emptyList();
    }

    @Override
    public void setUrlCreator(UrlCreator urlCreator)
    {
    }

    @Override
    public void harvestFile(String to, long size)
    {
    }

    @Override
    public void harvestDirectory(String to)
    {
    }

}
