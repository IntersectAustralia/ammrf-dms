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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import au.org.intersect.dms.util.DateFormatter;

/**
 * olympus tif image file parser
 * 
 */
public class TirfTifParser
{
    private static final StringTemplateGroup ST_HARVESTERS = new StringTemplateGroup("harvesters");
    private static final Logger LOGGER = LoggerFactory.getLogger(TirfTifParser.class);

    private String templateFilePath;
    private final Set<String> fields = new TreeSet<String>();
    private final Map<String, String> types = new HashMap<String, String>();
    private final DateTimeFormatter instrumentDateFormat;

    public TirfTifParser(String fieldsFilePath, String typesFilePath, String templateFilePath,
            DateTimeFormatter instrumentDateFormat)
    {
        this.templateFilePath = templateFilePath;
        this.instrumentDateFormat = instrumentDateFormat;
        loadFields(fieldsFilePath);
        loadTypes(typesFilePath);
    }

    /**
     * Parses a tif file using the metadata-extractor library
     * 
     * @param input
     *            stream for a tif image file
     * @return parsed metadata
     * @throws IOException
     */
    public String parse(File tempFile) throws IOException
    {
        StringTemplate template = ST_HARVESTERS.getInstanceOf(templateFilePath);
        Map<String, Object> properties = new HashMap<String, Object>();


        Metadata metadata = null;
        try
        {
            metadata = ImageMetadataReader.readMetadata(tempFile);
        }
        catch (ImageProcessingException e)
        {
            throw new RuntimeException(
                    "There was an error while processing the metadata contained in the tif file. {}", e);
        }

        for (Directory directory : metadata.getDirectories())
        {
            for (Tag tag : directory.getTags())
            {
                if (tag != null)
                {                   
                    Map<String, Object> requiredField = parseField(
                            tag.getTagName().trim(), tag.getDescription().trim());
                    
                    properties.putAll(requiredField);

                }
            }
        }

        template.setAttribute("properties", properties);
        return template.toString();
    }

    private Map<String, Object> parseField(String name, String value)
    {
        Map<String, Object> tagMap = new HashMap<String, Object>();
        if (fields.contains(name))
        {
            String convertedValue = convertPropertyValue(name, value);
            if (value != null)
            {
                tagMap.put(AbstractHarvester.toXML(name), AbstractHarvester.toXML(convertedValue));
            }
        }

        return tagMap;
    }

    private void loadFields(String filePath)
    {
        assert filePath != null;
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    TirfTifHarvester.class.getResourceAsStream(filePath)));
            String line = reader.readLine();
            while (line != null)
            {
                fields.add(line.trim());
                line = reader.readLine();
            }
            reader.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void loadTypes(String filePath)
    {
        try
        {
            if (filePath != null)
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        TirfTifHarvester.class.getResourceAsStream(filePath)));
                String line = reader.readLine();
                while (line != null)
                {
                    String[] keyValue = line.trim().split("=");
                    types.put(keyValue[0], keyValue[1]);
                    line = reader.readLine();
                }
                reader.close();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String convertPropertyValue(String name, String value)
    {
        if (types.containsKey(name))
        {
            if ("date".equals(types.get(name)))
            {
                return convertDate2UTC(value);
            }
        }
        return value;
    }

    /**
     * Converts date string in Olympus Tirf .tif file format (EST) to xml UTC standard string
     * 
     * @param dateString
     * @return
     */
    private String convertDate2UTC(String dateString)
    {
        DateTime estDate = instrumentDateFormat.parseDateTime(dateString);
        return DateFormatter.formatDate2UTC(estDate);
    }
}
