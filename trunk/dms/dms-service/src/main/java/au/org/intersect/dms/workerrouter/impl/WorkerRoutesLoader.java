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

package au.org.intersect.dms.workerrouter.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Loads configuration of routes from a properties file, the file can live in the classpath (for testing)
 * 
 * @version $Rev: 29 $
 */
public class WorkerRoutesLoader
{
    private static final String CP_PREFIX = "classpath:";

    public List<WorkerDefinition> loadRoutesDefinitions(String fname) throws IOException
    {
        Properties prop = new Properties();
        InputStream is = fname.startsWith(CP_PREFIX) ? getClass().getResourceAsStream(
                fname.substring(CP_PREFIX.length())) : new FileInputStream(fname);
        try
        {
            prop.load(is);
            return parseProperties(prop);
        }
        finally
        {
            is.close();
        }
    }

    private List<WorkerDefinition> parseProperties(Properties prop)
    {
        List<WorkerDefinition> definitions = new ArrayList<WorkerDefinition>();
        for (int num = 1;; num++)
        {
            String workerProperty = "dms.worker." + num;
            String queue = prop.getProperty(workerProperty);
            if (queue == null)
            {
                break;
            }
            WorkerDefinition def = new WorkerDefinition();
            def.setQueue(queue);
            def.setRoutes(loadWorkerDefinition(prop, workerProperty));
            definitions.add(def);
        }
        if (definitions.size() == 0)
        {
            throw new RuntimeException("Not routes found in file (see reference documentation)");
        }
        return definitions;
    }

    private List<WorkerRoute> loadWorkerDefinition(Properties prop, String workerPrefix)
    {
        List<WorkerRoute> routesDef = new ArrayList<WorkerRoute>();
        Enumeration<String> allKeys = (Enumeration<String>) prop.propertyNames();
        while (allKeys.hasMoreElements())
        {
            String key = allKeys.nextElement();
            if (key.startsWith(workerPrefix) && !key.equals(workerPrefix))
            {
                String protocol = key.substring(workerPrefix.length() + 1);
                String pattern = prop.getProperty(key);
                routesDef.add(new WorkerRoute(protocol, pattern));
            }
        }
        return routesDef;
    }

}
