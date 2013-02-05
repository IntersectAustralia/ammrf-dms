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

package au.org.intersect.dms.core.service.dto;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.serializable.RooSerializable;
import org.springframework.roo.addon.tostring.RooToString;

import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.core.domain.FileType;

/**
 * One metadata item to log in the DMS.
 * @version $Rev: 5 $
 *
 */
@RooJavaBean(settersByDefault = false)
@RooSerializable
@RooToString
public class MetadataEventItem
{
    private String url;
    private FileType type;
    private MetadataSchema schema;
    private String metadata;
    private Long size;
    
    public MetadataEventItem(String url, FileType type, Long size, MetadataSchema schema, String metadata)
    {
        this.url = url;
        this.type = type;
        this.size = size;
        this.schema = schema;
        this.metadata = metadata;
    }
}
