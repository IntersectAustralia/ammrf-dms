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

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.instrument.MetadataAccumulator;

/**
 * Harvests an olympus TIRF .tif file. called by TirfHarvester.
 */

public class TirfTifHarvester extends AbstractHarvester
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TirfTifHarvester.class);

    private static final String INCLUSIONS_FIELDS = "/META-INF/olympus-tirf/inclusions.txt";
    private static final String TEMPLATE = "META-INF/olympus-tirf/tifToXml";
    private static final String TYPES = "/META-INF/olympus-tirf/types.txt";
    
    private static final DateTimeFormatter TIRF_DATE_TIME_FORMATTER = DateTimeFormat
    .forPattern("yyyy:MM:dd HH:mm:ss");
    
    private static final int BUFFER_SIZE = 512;

    public TirfTifHarvester(ExecutorService executorService, String targetUrl, MetadataAccumulator accumulator)
    {
        super(executorService, MetadataSchema.OLYMPUS_TIRF, targetUrl, FileType.DIRECTORY, accumulator);
    }

    @Override
    String process(InputStream input) throws IOException
    {
        TirfTifParser parser = new TirfTifParser(INCLUSIONS_FIELDS, TYPES, TEMPLATE, TIRF_DATE_TIME_FORMATTER);       
        //return parser.parse(input);
        
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
