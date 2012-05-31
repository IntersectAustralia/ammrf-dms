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

package au.org.intersect.json;

/**
 * Basic object json serializer. Use attributeJson to inject an attribute known to be in json format, otherwise use
 * provided methods for known types.
 * 
 * @author carlos
 * 
 */
public class JsonObjectSerializer
{
    private StringBuffer sb;
    private int numFields;
    private String finished;

    public JsonObjectSerializer()
    {
        sb = new StringBuffer();
        sb.append("{");
        numFields = 0;
    }

    public void endObject()
    {
        if (finished != null)
        {
            return;
        }
        sb.append("}");
        finished = sb.toString();
        sb = null;
    }

    public String build()
    {
        endObject();
        return finished;
    }

    public void attribute(String name, String value)
    {
        attribute(name);
        sb.append(JsonTransformer.transform(value));
    }

    public void attribute(String name, long value)
    {
        attribute(name);
        sb.append(JsonTransformer.transform(value));
    }

    /**
     * Use this method only if you know the data string was already in json format
     * 
     * @param name
     * @param data
     */
    public void attributeJson(String name, String data)
    {
        attribute(name);
        sb.append(data);
    }

    private void attribute(String name)
    {
        if (numFields > 0)
        {
            sb.append(',');
        }
        sb.append(JsonTransformer.transform(name));
        sb.append(':');
        numFields++;
    }

}
