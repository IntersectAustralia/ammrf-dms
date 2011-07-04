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

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.instrument.MetadataAccumulator;
import au.org.intersect.dms.instrument.harvester.WindowsIniLogParser.Mode;

/**
 * Harvest microCT log file
 * 
 * @version $Rev: 29 $
 */
public class MicroCTLogHarvester extends AbstractHarvester
{
    private static final String TEMPLATE = "META-INF/micro-ct/logToXml";

    private static final String TYPES = "/META-INF/micro-ct/types.txt";

    private static final String EXCLUSIONS_FIELDS = "/META-INF/micro-ct/exclusions.txt";

    private static final DateTimeFormatter MICROCT_DATE_TIME_FORMATTER = DateTimeFormat
            .forPattern("MMMM dd, yyyy  HH:mm:ss");

    public MicroCTLogHarvester(ExecutorService executorService, String targetUrl, MetadataAccumulator acumulator)
    {
        super(executorService, MetadataSchema.MICRO_CT, targetUrl, FileType.DIRECTORY, acumulator);
    }

    @Override
    String process(InputStream input) throws IOException
    {
        WindowsIniLogParser parser = new WindowsIniLogParser(Mode.EXCLUSION, EXCLUSIONS_FIELDS, TYPES, TEMPLATE,
                MICROCT_DATE_TIME_FORMATTER);
        return parser.parse(input);
    }

}
