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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import au.org.intersect.dms.util.DateFormatter;

/**
 * olympus mtb file parser
 * 
 */
public class CellRMtbParser
{
    private static final StringTemplateGroup ST_HARVESTERS = new StringTemplateGroup("harvesters");
    private static final Logger LOGGER = LoggerFactory.getLogger(CellRMtbParser.class);

    private String templateFilePath;
    private final Set<String> fields = new TreeSet<String>();
    private final Map<String, String> types = new HashMap<String, String>();

    private Set<String> recordNames = new TreeSet<String>();
    private Set<String> positions = new TreeSet<String>();
    
    private Pattern pattern = Pattern.compile("(\\d*)\\s?(\\w+)");

    public CellRMtbParser(String fieldsFilePath, String typesFilePath, String templateFilePath)
    {
        this.templateFilePath = templateFilePath;
        loadFields(fieldsFilePath);
        loadTypes(typesFilePath);
    }

    /**
     * Parses a mtb file using the jackcess library
     * 
     * @param mtb file to parse
     * @return parsed metadata
     * @throws IOException
     */
    public String parse(File tempFile) throws IOException
    {
        StringTemplate template = ST_HARVESTERS.getInstanceOf(templateFilePath);
        Map<String, Object> properties = new HashMap<String, Object>();

        Database dbDatabase = Database.open(tempFile, true);
        Table table = dbDatabase.getTable("Images");

        if (table != null)
        {
            boolean metadataRowProcessed = false;
            
            for (int i = 0; i < table.getRowCount(); i++)
            {
                Map<String, Object> row = table.getNextRow();
                if (row.get("Image Type") == null)
                {
                    continue;
                }

                for (Entry<String, Object> field : row.entrySet())
                {
                    if ("Record Name".equals(field.getKey()))
                    {
                        String value = field.getValue().toString();
                        Matcher matcher = pattern.matcher(value);
                        if (matcher.matches())
                        {
                            positions.add(matcher.group(1));
                            recordNames.add(matcher.group(2));
                        }
                    }
                    
                    if (!metadataRowProcessed)
                    {
                        Map<String, Object> requiredField = parseField(field, row);
                        properties.putAll(requiredField);
                    }
                }
                metadataRowProcessed = true;
            }
            properties.put("Image type(s)", getRecordNames(recordNames));
            properties.put("Number of Positions", positions.size());
            
        }
        else
        {
            throw new RuntimeException("No Image Could Be Found");
        }

        template.setAttribute("properties", properties);
        return template.toString();
    }

    private String getRecordNames(Set<String> recordNames)
    {
        StringBuilder result = new StringBuilder();
        for (Iterator<String> recordNamesIterator = recordNames.iterator(); recordNamesIterator.hasNext();)
        {
            String recordName = recordNamesIterator.next();
            result.append(recordName);
            if (recordNamesIterator.hasNext())
            {
                result.append(", ");
            }
            
        }
        return result.toString();
    }

    private Map<String, Object> parseField(Entry<String, Object> entry, Map<String, Object> row)
    {

        Map<String, Object> fieldMap = new HashMap<String, Object>();
        if (fields.contains(entry.getKey().trim()))
        {
            String name = entry.getKey();
            Object value = entry.getValue();
            String convertedValue = convertPropertyValue(name, value, row);
            if (value != null)
            {
                fieldMap.put(AbstractHarvester.toXML(name), AbstractHarvester.toXML(convertedValue));
            }
        }

        return fieldMap;
    }

    private void loadFields(String filePath)
    {
        assert filePath != null;
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    CellRMtbHarvester.class.getResourceAsStream(filePath)));
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
                        CellRMtbHarvester.class.getResourceAsStream(filePath)));
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

    private String convertPropertyValue(String name, Object value, Map<String, Object> row)
    {
        if (types.containsKey(name))
        {
            if ("date".equals(types.get(name)))
            {
                MutableDateTime date = new MutableDateTime(value);
                Object timeValue = row.get("Document Creation Time");
                LOGGER.trace("Converting date. Day: {}, time: {}", value, timeValue);
                DateTime time = new DateTime(timeValue);
                date.setTime(time);
                LOGGER.trace("Date converted to: {}", date);
                return convertDate2UTC(date.toDateTime());
            }
        }

        return value.toString();
    }

    /**
     * Converts date string in Olympus mtb file format (EST) to UTC standard string
     * 
     * @param dateString
     * @return
     */
    private String convertDate2UTC(DateTime estDate)
    {
        return DateFormatter.formatDate2UTC(estDate);
    }
}
