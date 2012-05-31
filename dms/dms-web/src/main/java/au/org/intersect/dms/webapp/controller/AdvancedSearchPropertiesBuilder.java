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

package au.org.intersect.dms.webapp.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.xml.transform.ResourceSource;

import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.integration.search.Mapping;
import au.org.intersect.dms.integration.search.Property;

/**
 * Helper class to build advanced search filter
 */
public class AdvancedSearchPropertiesBuilder
{
    @Qualifier("searchUnmarshaller")
    @Autowired
    private Unmarshaller unmarshaller;

    @Autowired
    private ApplicationContext applicationContext;

    private Map<MetadataSchema, String> mapping;
    
    @Required
    public void setMapping(Map<MetadataSchema, String> mapping)
    {
        this.mapping = mapping;
    }

    public Map<MetadataSchema, List<Property>> getProperties()
    {
        Map<MetadataSchema, List<Property>> properties = new TreeMap<MetadataSchema, List<Property>>();
        Set<Entry<MetadataSchema, String>> entrySet = mapping.entrySet();
        try
        {
            for (Entry<MetadataSchema, String> mappingEntry : entrySet)
            {
                MetadataSchema schema = mappingEntry.getKey();
                Mapping mapping = (Mapping) unmarshaller.unmarshal(new ResourceSource(applicationContext
                        .getResource(mappingEntry.getValue())));
                properties.put(schema, mapping.getProperty());
            }
        }
        catch (XmlMappingException e)
        {
            throw new RuntimeException("Couldn't parse properties mapping", e.getCause());
        }
        catch (IOException e)
        {
            throw new RuntimeException("Couldn't access properties mapping file", e.getCause());
        }
        return properties;
    }
}
