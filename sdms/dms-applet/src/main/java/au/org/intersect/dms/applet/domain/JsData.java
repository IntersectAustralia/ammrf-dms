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

package au.org.intersect.dms.applet.domain;

import org.springframework.roo.addon.javabean.RooJavaBean;

import au.org.intersect.json.JsonObjectSerializer;

/**
 * Data wrapper to hold information about individual tree node (file/directory) 
 */
@RooJavaBean
public class JsData
{
    private String title;
    private String icon;
    
    public JsData()
    {
    }

    public JsData(String title, String icon)
    {
        this.title = title;
        this.icon = icon;
    }


    public String toJson()
    {
        JsonObjectSerializer json = new JsonObjectSerializer();
        json.attribute("title", title);
        json.attribute("icon", icon);
        return json.build();
    }
}
