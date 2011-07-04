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

package au.org.intersect.dms.webapp.json;

import java.util.HashMap;
import java.util.Map;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.FileType;

/**
 * JSON object to represent node in the tree widget (directory or file).
 * Holds all necessary information including metadata.
 *
 */
public class TreeNode
{
    private Map<String, String> data;
    private Map<String, String> attr;
    private FileInfo metadata;
    private String state;

    public TreeNode(FileInfo fileInfo)
    {
        data = new HashMap<String, String>();
        data.put("title", fileInfo.getName());
        data.put("icon", fileInfo.getFileType().toString().toLowerCase());
        if (FileType.DIRECTORY.equals(fileInfo.getFileType()))
        {
            state = "closed";
        }

        attr = new HashMap<String, String>();
        attr.put("id", "n" + fileInfo.getAbsolutePath().replaceAll("\\W", "_"));
        metadata = fileInfo;
    }

    public Map<String, String> getData()
    {
        return data;
    }

    public FileInfo getMetadata()
    {
        return metadata;
    }

    public String getState()
    {
        return state;
    }

    public Map<String, String> getAttr()
    {
        return attr;
    }

}
