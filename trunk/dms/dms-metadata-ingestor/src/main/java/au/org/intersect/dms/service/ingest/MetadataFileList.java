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

package au.org.intersect.dms.service.ingest;

import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.AttributeRenderer;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

/**
 * Creates file list for a metadata item
 * @author carlos
 *
 */
public class MetadataFileList
{
    private static final StringTemplateGroup ST_GROUP = new StringTemplateGroup("metadata-listener");

    public static String makeFileList(List<Map<String, Object>> files)
    {
        StringTemplate template = ST_GROUP.getInstanceOf("META-INF/file-list/files-list");
        template.registerRenderer(String.class, makeRenderer());
        template.setAttribute("list", files);
        return template.toString();
    }
    
    private static AttributeRenderer makeRenderer()
    {
        return new AttributeRenderer()
        {

            @Override
            public String toString(Object o, String formatName)
            {
                return toString(o);
            }

            @Override
            public String toString(Object o)
            {
                return o == null ? "" : o.toString().replace("\"", "\\\"");
            }
        };
    }

    
}
