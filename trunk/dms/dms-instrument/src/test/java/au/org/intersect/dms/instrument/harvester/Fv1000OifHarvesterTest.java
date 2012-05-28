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

public class Fv1000OifHarvesterTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Fv1000OifHarvesterTest.class);
    private static final int BUFFER_SIZE = 512;

    @Test
    public void testHarvest() throws IOException
    {
        final String url = "ftp://blah";
        byte[] buffer = new byte[BUFFER_SIZE];
        MetadataAccumulator accumulator = new MetadataAccumulator()
        {

            @Override
            public void addMetadataItem(MetadataEventItem mdItem)
            {
                assertNotNull(mdItem);
                LOGGER.info("Metadata:\n{}", mdItem.getMetadata());
                assertEquals(url, mdItem.getUrl());
                assertEquals(MetadataSchema.OLYMPUS_FV1000, mdItem.getSchema());
                // test that it contains an expected value there
                assertTrue(mdItem.getMetadata().contains("XYZ"));
                assertTrue(mdItem.getMetadata().contains("PixUnit"));

                // test dates conversion from EST to UTC
                // 2011-01-20 09:38:25 (EST) in UTC
                assertTrue("Dates conversion from EST to UTC is incorrect",
                       mdItem.getMetadata().contains("2011-01-19T22:38:25Z"));

                // test that it does not contain the exclusions
                assertFalse(mdItem.getMetadata().contains("FLUOVIEW FV1000"));
                assertFalse(mdItem.getMetadata().contains("ExcitationDM Position"));
            }
        };
        Fv1000OifHarvester harvester = new Fv1000OifHarvester(Executors.newSingleThreadExecutor(), url, accumulator);
        InputStream is = getClass().getResourceAsStream("/olympus-fv1000/Pea root section tubulin PEM c zstack.oif");
        
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
