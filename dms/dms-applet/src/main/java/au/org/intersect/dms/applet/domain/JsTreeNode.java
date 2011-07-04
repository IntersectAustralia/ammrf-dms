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

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.json.JsonArraySerializer;
import au.org.intersect.json.JsonObjectSerializer;

/**
 * JSON object to represent node in the tree widget (directory or file). Holds all necessary information including
 * metadata.
 * 
 */
@RooJavaBean
public class JsTreeNode
{
    private JsData data;
    private JsAttribute attr;
    private FileInfo metadata;
    private String state;

    public JsTreeNode()
    {
    }

    public JsTreeNode(FileInfo fileInfo)
    {
        data = new JsData(fileInfo.getName(), fileInfo.getFileType().toString().toLowerCase());
        if (FileType.DIRECTORY == fileInfo.getFileType())
        {
            state = "closed";
        }
        attr = new JsAttribute("n" + fileInfo.getAbsolutePath().replaceAll("\\W", "_"));
        metadata = fileInfo;
    }

    public String toJson()
    {
        JsonObjectSerializer json = new JsonObjectSerializer();
        json.attributeJson("data", data.toJson());
        json.attributeJson("attr", attr.toJson());
        json.attributeJson("metadata", metadata.toJson());
        json.attribute("state", state);
        return json.build();
    }

    public static String toJson(JsTreeNode[] nodes)
    {
        JsonArraySerializer json = new JsonArraySerializer();
        for (int i = 0; i < nodes.length; i++)
        {
            json.element(nodes[i] != null ? nodes[i].toJson() : null);
        }
        return json.build();
    }

    public static JsTreeNode[] toJsTreeNodes(FileInfo[] infos)
    {
        JsTreeNode[] resp = new JsTreeNode[infos.length];
        for (int i = 0; i < infos.length; i++)
        {
            resp[i] = new JsTreeNode(infos[i]);
        }
        return resp;
    }

}
