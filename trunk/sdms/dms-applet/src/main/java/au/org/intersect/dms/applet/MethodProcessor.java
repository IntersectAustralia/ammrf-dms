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

package au.org.intersect.dms.applet;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.org.intersect.dms.applet.domain.JsConnection;
import au.org.intersect.dms.applet.domain.JsResponse;
import au.org.intersect.dms.applet.domain.JsTreeNode;
import au.org.intersect.dms.core.domain.FileInfo;

/**
 * Parses the URL and calls the appropiate method in the hddImpl.
 * 
 * @version $Rev: 29 $
 */
public class MethodProcessor
{
    private HddAccess hddImpl = new HddAccess();

    public JsResponse perform(String method, String data) throws UnsupportedEncodingException
    {
        if ("connect".equals(method))
        {
            return new JsResponse(connect(data).toJson(), true);
        }
        if (method != null && method.startsWith("list?"))
        {
            Map<String, List<String>> params = parseUrl(method);
            String path = params.get("path").get(0);
            FileInfo[] list = getList(path);
            JsTreeNode[] nodes = JsTreeNode.toJsTreeNodes(list);
            return new JsResponse(JsTreeNode.toJson(nodes), true);
        }
        return new JsResponse("Unknown method", false);
    }

    private Map<String, List<String>> parseUrl(String url) throws UnsupportedEncodingException
    {
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        String[] urlParts = url.split("\\?");
        if (urlParts.length > 1)
        {
            String query = urlParts[1];
            for (String param : query.split("&"))
            {
                String[] pair = param.split("=");
                String key = URLDecoder.decode(pair[0], "UTF-8");
                String value = URLDecoder.decode(pair[1], "UTF-8");
                List<String> values = params.get(key);
                if (values == null)
                {
                    values = new ArrayList<String>();
                    params.put(key, values);
                }
                values.add(value);
            }
        }
        return params;
    }

    private FileInfo[] getList(String path)
    {
        return hddImpl.getList(path);
    }

    private JsConnection connect(String data)
    {
        return new JsConnection("hdd", 1L);
    }

}
