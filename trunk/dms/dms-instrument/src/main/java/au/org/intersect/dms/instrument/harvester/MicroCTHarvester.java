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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.instrument.FileHarvester;
import au.org.intersect.dms.core.instrument.InstrumentHarvester;
import au.org.intersect.dms.core.instrument.UrlCreator;
import au.org.intersect.dms.core.service.dto.MetadataEventItem;

/**
 * Implements the harvest profile for microCT. It harvest a log file and nothing else. The logfile attach metadata
 * to the dataset!
 * @version $Rev: 29 $
 */
public class MicroCTHarvester implements InstrumentHarvester
{
    @Autowired
    @Qualifier("executorService")
    private ExecutorService executorService;
    
    private String toDir;

    private UrlCreator urlCreator;

    private List<MetadataEventItem> metadataItems = new ArrayList<MetadataEventItem>();

    @Override
    public FileHarvester getHarvesterFor(String path)
    {
        if (path.endsWith(".log"))
        {
            // note how we override the path and use the toDir as the target URL
            return new MicroCTLogHarvester(executorService, urlCreator.getUrl(toDir), this);
        }
        else
        {
            return null;
        }
    }

    @Override
    public void harvestStart(String toDir)
    {
        this.toDir = toDir;
    }
    
    @Override
    public void harvestEnd()
    {
    }

    
    @Override
    public List<MetadataEventItem> getMetadata()
    {
        return metadataItems;
    }

    @Override
    public void setUrlCreator(UrlCreator urlCreator)
    {
        this.urlCreator = urlCreator;
    }

    @Override
    public void addMetadataItem(MetadataEventItem item)
    {
        synchronized (metadataItems)
        {
            metadataItems.add(item);
        }
    }

    @Override
    public void harvestDirectory(String to)
    {
        synchronized (metadataItems)
        {
            MetadataEventItem item = new MetadataEventItem(urlCreator.getUrl(to), FileType.DIRECTORY, 0L, null, null);
            metadataItems.add(item);
        }
    }

    @Override
    public void harvestFile(String to, long size)
    {
        synchronized (metadataItems)
        {
            MetadataEventItem item = new MetadataEventItem(urlCreator.getUrl(to), FileType.FILE, size, null, null);
            metadataItems.add(item);
        }
    }

}
