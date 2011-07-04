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

package au.org.intersect.dms.wn.main;

import java.io.PrintStream;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.service.WorkerNode;
import au.org.intersect.dms.core.service.dto.GetListParameter;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;

/**
 * Simple command line utility to test worker
 * @author carlos
 *
 */
public class Main
{
    // TODO CHECKSTYLE-OFF: IllegalCatch|RegexpMultiline|UncommentedMain
    
    private static final String[] CONFIG_FILES = {"classpath*:META-INF/spring/worker-client.xml"};
    
    private static final PrintStream OUT = System.out;

    /**
     * Entry point for simple worker client.
     * @param args arguments: protocol, server, username, password, path
     */
    public static void main(String[] args)
    {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(CONFIG_FILES);
        context.registerShutdownHook();
        context.start();

        try
        {
            String protocol = args[0];
            String server = args[1];
            String username = args[2];
            String password = args[2 + 1];
            String absolutePath = args[2 + 2];
            WorkerNode worker = context.getBean(WorkerNode.class);
            OpenConnectionParameter params = new OpenConnectionParameter(protocol, server, username, password);
            int connId = worker.openConnection(params);
            OUT.println("Directory of " + protocol + "://" + server + absolutePath);
            GetListParameter params2 = new GetListParameter(connId, absolutePath);
            List<FileInfo> list = worker.getList(params2);
            for (int i = 0; i < list.size(); i++)
            {
                FileInfo fInfo = list.get(i); 
                OUT.println(fInfo.getName() + " " + fInfo.getFileType());
            }
            OUT.println("Entries: " + list.size());
        }
        catch (Exception e)
        {
            e.printStackTrace(OUT);
            OUT.println("Usage: <protocol> <server> <user> <passwd> <path>");
        }
        finally
        {
            context.stop();
            System.exit(0);
        }
    }

}
