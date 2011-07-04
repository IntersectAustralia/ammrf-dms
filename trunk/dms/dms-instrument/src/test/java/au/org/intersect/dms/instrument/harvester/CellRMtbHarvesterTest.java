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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.core.instrument.MetadataAccumulator;
import au.org.intersect.dms.core.service.dto.MetadataEventItem;

public class CellRMtbHarvesterTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CellRMtbHarvesterTest.class);
    private static final int BUFFER_SIZE = 512;
    private byte[] buffer = new byte[BUFFER_SIZE];
    private final String url = "ftp://blah";

    @Test
    public void firstSinglePositionHarvest() throws IOException
    {
        String fileName = "/olympus-cellR/single position 1.mtb";
        
        MetadataAccumulator accumulator = testMetadata();

        createHarvester(url, buffer, accumulator, fileName);
    }

    @Test
    public void secondSinglePositionHarvest() throws IOException
    {
        String fileName = "/olympus-cellR/single position 2.mtb";
        
        MetadataAccumulator accumulator = testMetadata();

        createHarvester(url, buffer, accumulator, fileName);
    }

    @Test
    public void multiPositionHarvest() throws IOException
    {

        String fileName = "/olympus-cellR/multiple positions.mtb";
        
        MetadataAccumulator accumulator = testMetadata();

        createHarvester(url, buffer, accumulator, fileName);
    }

    private void createHarvester(final String url, byte[] buffer, MetadataAccumulator accumulator, String fileName)
        throws IOException
    {
        CellRMtbHarvester harvester = new CellRMtbHarvester(Executors.newSingleThreadExecutor(), url, accumulator);
        InputStream is = getClass().getResourceAsStream(fileName);
        OutputStream os = harvester.getSink();
        assertNotNull(os);
        int readBytes = is.read(buffer);
        while (readBytes > 0)
        {
            os.write(buffer);
            readBytes = is.read(buffer);
        }
        is.close();
        harvester.closeSink(os, true);
    }

    private MetadataAccumulator testMetadata()
    {
        MetadataAccumulator accumulator = new MetadataAccumulator()
        {

            @Override
            public void addMetadataItem(MetadataEventItem mdItem)
            {
                assertNotNull(mdItem);
                LOGGER.info("Metadata:\n{}", mdItem.getMetadata());
                assertEquals(url, mdItem.getUrl());
                assertEquals(MetadataSchema.OLYMPUS_CELL_R, mdItem.getSchema());
                // test that it contains an expected value there
                assertTrue(mdItem.getMetadata().contains("Time Frames"));
                assertTrue(mdItem.getMetadata().contains("Z-Step Width"));

                assertFalse(mdItem.getMetadata().contains("Sample / Preparation"));
                assertFalse(mdItem.getMetadata().contains("Graph Comment"));
            }
        };
        return accumulator;
    }
}
