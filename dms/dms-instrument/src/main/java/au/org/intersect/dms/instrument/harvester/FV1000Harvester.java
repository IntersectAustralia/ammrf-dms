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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.instrument.FileHarvester;
import au.org.intersect.dms.core.instrument.InstrumentHarvester;
import au.org.intersect.dms.core.instrument.UrlCreator;
import au.org.intersect.dms.core.service.dto.MetadataEventItem;

/**
 * Implements the harvest profile for olympus FV1000. It harvests FV1000 DB. Dataset is several files.
 * 
 * <p>
 * <b>WARNING:</b> 
 * This harvester is statefull so be careful not to call getHarvetserFor method twice for the same file 
 */
public class FV1000Harvester implements InstrumentHarvester
{
    private static final String ACQUISITION_PARAMETERS_COMMON_SECTION 
        = "<section name=\"Acquisition Parameters Common\">";

    private static final Logger LOGGER = LoggerFactory.getLogger(FV1000Harvester.class);

    @Autowired
    @Qualifier("executorService")
    private ExecutorService executorService;

    private String toDir;

    private UrlCreator urlCreator;

    private List<MetadataEventItem> metadataItems = new ArrayList<MetadataEventItem>();

    private List<MetadataEventItem> tempMetadataItemList = new ArrayList<MetadataEventItem>();

    private boolean ptyHarvested;
    
    /**
     * This harvester is statefull so be careful not to call this method twice for the same file
     */
    @Override
    public FileHarvester getHarvesterFor(String path)
    {
        if (path.endsWith(".oif"))
        {
            // note how we override the path and use the toDir as the target URL
            return new Fv1000OifHarvester(executorService, urlCreator.getUrl(toDir), this);

        }
        else if (!ptyHarvested && path.endsWith(".pty"))
        {
            ptyHarvested = true;
            // note how we override the path and use the toDir as the target URL
            return new Fv1000PtyHarvester(executorService, urlCreator.getUrl(toDir), this);

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
        StringBuilder metadata = new StringBuilder();
        String ptyMetadata = "";
        for (MetadataEventItem metadataItem : tempMetadataItemList)
        {
            if (MetadataSchema.OLYMPUS_FV1000 == metadataItem.getSchema())
            {
                metadata.append(metadataItem.getMetadata());
            }
            else
            {
                ptyMetadata = "\n" + metadataItem.getMetadata();
            }
        }
        metadata.insert(metadata.lastIndexOf(ACQUISITION_PARAMETERS_COMMON_SECTION)
                + ACQUISITION_PARAMETERS_COMMON_SECTION.length(), ptyMetadata);

        synchronized (metadataItems)
        {
            MetadataEventItem item = new MetadataEventItem(urlCreator.getUrl(toDir), FileType.DIRECTORY, 0L,
                    MetadataSchema.OLYMPUS_FV1000, metadata.toString());
            metadataItems.add(item);
        }
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
        synchronized (tempMetadataItemList)
        {
            tempMetadataItemList.add(item);
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
