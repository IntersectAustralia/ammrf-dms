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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import au.org.intersect.dms.util.DateFormatter;

/**
 * Windows INI files parser
 * 
 */
public class WindowsIniLogParser
{
    private static final StringTemplateGroup ST_HARVESTERS = new StringTemplateGroup("harvesters");

    private Mode mode;
    private String templateFilePath;
    private String encoding;
    private SectionChecker sectionChecker;
    /**
     * Exclusion or inclusion fields (based on mode)
     */
    private final Set<String> fields = new HashSet<String>();
    private final Map<String, String> types = new HashMap<String, String>();
    private final DateTimeFormatter instrumentDateFormat;

    public WindowsIniLogParser(Mode mode, String fieldsFilePath, String typesFilePath, String templateFilePath,
            DateTimeFormatter instrumentDateFormat)
    {
        this.mode = mode;
        this.templateFilePath = templateFilePath;
        this.instrumentDateFormat = instrumentDateFormat;
        loadFields(fieldsFilePath);
        loadTypes(typesFilePath);
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public void setSectionChecker(SectionChecker sectionChecker)
    {
        this.sectionChecker = sectionChecker;
    }

    /**
     * Parses win INI file
     * 
     * @param inputStream
     *            input stream of win ini file to parse
     * @return parsed metadata
     */
    public String parse(InputStream inputStream)
    {
        HierarchicalINIConfiguration iniConf = new HierarchicalINIConfiguration();
        try
        {
            iniConf.load(inputStream, encoding);
        }
        catch (ConfigurationException e)
        {
            throw new RuntimeException(e);
        }
        return parseLogIniFile(iniConf);
    }

    private String parseLogIniFile(HierarchicalINIConfiguration iniConf)
    {
        StringTemplate template = ST_HARVESTERS.getInstanceOf(templateFilePath);
        Map<String, Map<String, Object>> values = new HashMap<String, Map<String, Object>>();
        for (String sectionName : (Set<String>) iniConf.getSections())
        {
            Map<String, Object> sectionMap = new HashMap<String, Object>();
            SubnodeConfiguration section = iniConf.getSection(sectionName);
            if (sectionChecker == null || sectionChecker.includeSection(section))
            {
                for (Iterator it = section.getRootNode().getChildren().iterator(); it.hasNext();)
                {
                    ConfigurationNode node = (ConfigurationNode) it.next();
                    sectionMap = parseFields(sectionName, sectionMap, node);
                }
                if (!sectionMap.isEmpty())
                {
                    values.put(AbstractHarvester.toXML(sectionName), sectionMap);
                }
            }
        }
        template.setAttribute("sections", values);
        return template.toString();
    }

    private Map<String, Object> parseFields(String sectionName, Map<String, Object> sectionMap, ConfigurationNode node)
    {
        if (node.getReference() == null && node.getChildrenCount() == 0)
        {
            String sectionProperty = sectionName + "." + node.getName();

            if (mode == Mode.EXCLUSION)
            {
                if (!fields.contains(sectionProperty.trim()))
                {
                    String name = node.getName();
                    String value = node.getValue().toString();
                    value = convertPropertyValue(sectionProperty, value);
                    sectionMap.put(AbstractHarvester.toXML(name), AbstractHarvester.toXML(value));
                }
            }
            else if (mode == Mode.INCLUSION)
            {
                if (findMatch(sectionProperty.trim()))

                {
                    String name = node.getName();
                    String value = node.getValue().toString();
                    value = convertPropertyValue(sectionProperty, value);
                    sectionMap.put(AbstractHarvester.toXML(name), AbstractHarvester.toXML(value));
                }
            }
        }
        return sectionMap;
    }

    private boolean findMatch(String property)
    {
        for (String regex : fields)
        {
            if (property.matches(regex))
            {
                return true;
            }
        }

        return false;
    }

    private void loadFields(String filePath)
    {
        assert filePath != null;
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    MicroCTLogHarvester.class.getResourceAsStream(filePath)));
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
                        MicroCTLogHarvester.class.getResourceAsStream(filePath)));
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
     * Converts date string in Win INI log file format (EST) to xml UTC standard string
     * 
     * @param dateString
     * @return
     */
    private String convertDate2UTC(String dateString)
    {
        DateTime estDate = instrumentDateFormat.parseDateTime(dateString);
        return DateFormatter.formatDate2UTC(estDate);
    }

    /**
     * Parsing mode
     */
    public enum Mode
    {
        /**
         * All fields except indicated should be included
         */
        EXCLUSION,

        /**
         * Only indicated fields should be included
         */
        INCLUSION
    }

    /**
     * Intreface to do extra (dynamic) check on sections
     * 
     */
    public static interface SectionChecker
    {
        /**
         * Checks if section should be included or skipped
         * 
         * @param section
         * @return true if section should be included, false otherwise
         */
        boolean includeSection(SubnodeConfiguration section);
    }
}
