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

package au.org.intersect.dms.core.domain;

import java.util.Date;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.serializable.RooSerializable;

import au.org.intersect.json.JsonArraySerializer;
import au.org.intersect.json.JsonObjectSerializer;

/**
 * Wrapper to represent metadata of the file (name, size, etc.)
 */
@RooJavaBean
@RooSerializable
public class FileInfo implements java.io.Serializable
{
    private static final String ROOT = "/";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("dd/MM/yyyy h:mm:ss a");

    private FileType fileType;
    private String absolutePath;

    private String name;
    private Long size;
    private String modificationDate;

    public FileInfo()
    {
    }

    public FileInfo(FileType fileType, String absolutePath, String name, Long size, Date modificationDate)
    {
        if (!absolutePath.startsWith(ROOT))
        {
            throw new IllegalArgumentException(absolutePath + " must begin with '/'");
        }
        this.fileType = fileType;
        this.absolutePath = absolutePath;
        this.name = name;
        this.size = size;
        setModificationDate(modificationDate);
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = DATE_TIME_FORMATTER.print(modificationDate.getTime());
    }

    public void setAbsolutePath(String absolutePath)
    {
        if (!absolutePath.startsWith(ROOT))
        {
            throw new IllegalArgumentException(absolutePath + " must begin with '/'");
        }
        this.absolutePath = absolutePath;
    }

    public static FileInfo createRootFileInfo()
    {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileType(FileType.DIRECTORY);
        fileInfo.setAbsolutePath(ROOT);
        fileInfo.setName(ROOT);
        fileInfo.setSize(0L);
        return fileInfo;
    }

    public String toJson()
    {
        JsonObjectSerializer json = new JsonObjectSerializer();
        json.attribute("fileType", fileType.toString());
        json.attribute("absolutePath", absolutePath);
        json.attribute("name", name);
        json.attribute("size", size);
        json.attribute("modificationDate", modificationDate);
        return json.build();
    }

    public static String toJson(FileInfo[] infos)
    {
        JsonArraySerializer json = new JsonArraySerializer();
        for (int i = 0; i < infos.length; i++)
        {
            json.element(infos[i] != null ? infos[i].toJson() : null);
        }
        return json.build();
    }

}