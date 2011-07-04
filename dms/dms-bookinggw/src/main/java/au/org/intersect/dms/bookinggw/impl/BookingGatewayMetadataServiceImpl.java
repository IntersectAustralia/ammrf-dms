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

package au.org.intersect.dms.bookinggw.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import au.org.intersect.dms.bookinggw.BookingGatewayInterface;
import au.org.intersect.dms.bookinggw.BookingGatewayMetadataService;
import au.org.intersect.dms.bookinggw.ProjectDetails;

/**
 * Helper class to implement metadata related methods.
 * 
 */
public class BookingGatewayMetadataServiceImpl implements BookingGatewayMetadataService
{
    /**
     * Template engine
     */
    private static final StringTemplateGroup ST_GROUP = new StringTemplateGroup("metadata");

    /**
     * Path (in classpath) to the metadata template StringTemplate file (see
     * http://www.antlr.org/wiki/display/ST/Defining+Templates)
     */
    private String metadataTemplatePath;

    @Autowired
    private BookingGatewayInterface bookingGatewayInterface;
    
    @Required
    public void setMetadataTemplatePath(String metadataTemplatePath)
    {
        this.metadataTemplatePath = metadataTemplatePath;
    }

    @Override
    public String getMetadata(Long projectCode, Long bookingId, String destinationURL)
    {
        StringTemplate template = getMetadataTemplate();
        template.setAttribute("url", destinationURL);

        if (projectCode != null && bookingId != null)
        {
            ProjectDetails projectDetails = bookingGatewayInterface.getProjectDetails(projectCode, bookingId);
            template.setAttribute("md_title", projectDetails.getTitle());
            template.setAttribute("md_description", projectDetails.getOutline());
        }

        return template.toString();
    }

    @Override
    public String getMetadata(Map<String, Object> params)
    {
        StringTemplate template = getMetadataTemplate();
        Set<Entry<String, Object>> entrySet = params.entrySet();
        for (Entry<String, Object> param : entrySet)
        {
            template.setAttribute(param.getKey(), param.getValue());
        }
        return template.toString();
    }

    private StringTemplate getMetadataTemplate()
    {
        StringTemplate template = ST_GROUP.getInstanceOf(metadataTemplatePath);
        UUID id = UUID.randomUUID();
        template.setAttribute("id", id);
        return template;
    }

}
