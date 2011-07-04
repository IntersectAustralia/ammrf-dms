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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.core.instrument.FileHarvester;
import au.org.intersect.dms.core.instrument.UrlCreator;
import au.org.intersect.dms.core.service.dto.MetadataEventItem;

public class FV1000HarvesterTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FV1000HarvesterTest.class);
    private static final int BUFFER_SIZE = 512;
    
    @Test
    public void testHarvest() throws IOException
    {
        ExecutorService executorService = Executors.newCachedThreadPool();
        final String url = "ftp://blah";
        FV1000Harvester fv1000Harvester = new FV1000Harvester();
        fv1000Harvester.setUrlCreator(new UrlCreator()
        {
            @Override
            public String getUrl(String path)
            {
                return url;
            }
        });
        
        Fv1000OifHarvester oifHarvester = new Fv1000OifHarvester(executorService, url,
                fv1000Harvester);
        streamFile("/olympus-fv1000/Pea root section tubulin PEM c zstack.oif", oifHarvester);
        
        Fv1000PtyHarvester ptyHarvester = new Fv1000PtyHarvester(executorService, url,
                fv1000Harvester);
        streamFile("/olympus-fv1000/Pea root section tubulin PEM c zstack_C001Z008.pty", ptyHarvester);
        
        fv1000Harvester.harvestEnd();
        List<MetadataEventItem> metadata = fv1000Harvester.getMetadata();
        for (MetadataEventItem metadataEventItem : metadata)
        {
            LOGGER.info("Metadata: {}:\n{}", metadataEventItem.getSchema(), metadataEventItem.getMetadata());
            // test that it contains an expected value there
            assertTrue(metadataEventItem.getMetadata().contains("ObjectiveLens Name"));
            assertTrue(metadataEventItem.getMetadata().contains("LaserTransmissivity01"));
        }
        
    }

    private void streamFile(String fileName, FileHarvester harvester) throws IOException
    {
        byte[] buffer = new byte[BUFFER_SIZE];
        InputStream is = getClass().getResourceAsStream(fileName);

        OutputStream os = harvester.getSink();
        assertNotNull(os);
        int readBytes = is.read(buffer);
        while (readBytes > 0)
        {
            os.write(buffer, 0, readBytes);
            readBytes = is.read(buffer);
        }
        is.close();
        harvester.closeSink(os, true);
    }

}
