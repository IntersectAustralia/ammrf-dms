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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.instrument.MetadataAccumulator;

/**
 * takes the inputstream of a mtb file created by the olympus Cell^R instrument and creates a temporary file that is
 * passed to the mtbParser.
 */

public class CellRMtbHarvester extends AbstractHarvester
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CellRMtbHarvester.class);

    private static final String INCLUSIONS_FIELDS = "/META-INF/olympus-cellR/inclusions.txt";
    private static final String TEMPLATE = "META-INF/olympus-cellR/mtbToXml";
    private static final String TYPES = "/META-INF/olympus-cellR/types.txt";

    private static final int BUFFER_SIZE = 512;

    public CellRMtbHarvester(ExecutorService executorService, String targetUrl, MetadataAccumulator accumulator)
    {
        super(executorService, MetadataSchema.OLYMPUS_CELL_R, targetUrl, FileType.DIRECTORY, accumulator);
    }

    @Override
    String process(InputStream input) throws IOException
    {
        CellRMtbParser parser = new CellRMtbParser(INCLUSIONS_FIELDS, TYPES, TEMPLATE);
        //

        String tempFilePrefix = System.currentTimeMillis() + "";
        byte[] buffer = new byte[BUFFER_SIZE];

        File tempFile = File.createTempFile(tempFilePrefix, null);
        LOGGER.trace("Creating temporary DB file: {}", tempFile);

        BufferedOutputStream writeToTempFile = null;
        try
        {
            writeToTempFile = new BufferedOutputStream(new FileOutputStream(tempFile));
            int readBytes = input.read(buffer);
            while (readBytes > 0)
            {
                writeToTempFile.write(buffer);
                readBytes = input.read(buffer);
            }
        }
        finally
        {
            if (writeToTempFile != null)
            {
                writeToTempFile.close();
            }
        }

        String metadata;
        try
        {
            metadata = parser.parse(tempFile);
        }
        finally
        {
            LOGGER.trace("Destroying the temporary DB file: {}", tempFile);
            if (tempFile.exists())
            {

                if (tempFile.delete())
                {
                    LOGGER.trace("Temporary file destroyed successfully.");

                }
                else
                {
                    LOGGER.debug("Could not destroy Temporary file! File still exists in directory: {}", tempFile);
                    tempFile.deleteOnExit();
                }
            }

        }
        return metadata;
    }
}
