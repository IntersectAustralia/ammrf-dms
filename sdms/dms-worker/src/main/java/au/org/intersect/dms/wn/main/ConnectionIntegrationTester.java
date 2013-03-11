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

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.service.WorkerNode;
import au.org.intersect.dms.core.service.JobListener;
import au.org.intersect.dms.core.service.dto.*;
import au.org.intersect.dms.wn.ConnectionParams;
import au.org.intersect.dms.wn.TransportConnection;
import au.org.intersect.dms.wn.TransportFactory;

public class ConnectionIntegrationTester
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionIntegrationTester.class);

    private static final String[] CONFIG_FILES = {"classpath*:META-INF/spring/applicationContext-workernode*.xml","classpath:applicationContext-extra.xml"};

    private static class URL {
        String protocol = "";
        String userInfo = "";
        String host = "";
        Integer port = 0;
        String path = "/";
        URL(String url) {
           String copy = url + "";
           int i = url.indexOf("://");
           protocol = url.substring(0,i);
           url = url.substring(i+3);
           i = url.indexOf('@');
           if (i >= 0) {
              userInfo = url.substring(0,i);
              url = url.substring(i+1);
           }
           i = url.indexOf(':');
           if (i >= 0) {
              int j = url.substring(i+1).indexOf('/');
              if (j >= 0) {
                 port = Integer.parseInt(url.substring(i+1,j));
                 url = url.substring(0,i-1) + url.substring(i+i+j);
              } else {
                 port = Integer.parseInt(url.substring(i+1));
                 url = url.substring(0,i-1);
              }
           }
           i = url.indexOf('/');
           if (i >= 0) {
              path = url.substring(i);
              host = url.substring(0,i);
           } else {
              host = url;
           }
           System.out.println(copy + " > " + protocol + ", " + userInfo + ", " + host + ", [" + port + "], " + path);
        }
    }

    public static void main(String[] args)
    {

        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(CONFIG_FILES);
        context.registerShutdownHook();
        context.start();
        final WorkerNode wn = context.getBean("dmsWorkerNode", WorkerNode.class);
        Integer conn = null;
        Integer conn2 = null;

        try
        {
            System.out.println("running");
            URL url = new URL(args[1]);
            conn = wn.openConnection(makeParam(url));

            if ("getList".equals(args[0])) {

                for(FileInfo info: wn.getList(new GetListParameter(conn, url.path))) {
                    System.out.println(info.getName() + " " + info.getSize() + " " + info.getFileType());
                }
                return;

            }

            if ("getFileInfo".equals(args[0])) {

                FileInfo info = wn.getFileInfo(new GetFileInfoParameter(conn, url.path));
                System.out.println(info.getName() + " " + info.getSize() + " " + info.getFileType());
                return;

            }

            if ("rename".equals(args[0])) {

                System.out.println("R: " + wn.rename(new RenameParameter(conn, url.path, args[2])));
                return;

            }

            if ("delete".equals(args[0])) {

                List<FileInfo> infos = Arrays.asList(new FileInfo[]{wn.getFileInfo(new GetFileInfoParameter(conn, url.path))});
                System.out.println("R: " + wn.delete(new DeleteParameter(conn, infos)));
                return;

            }


            if ("copy".equals(args[0])) {

                URL url2 = new URL(args[2]);
                conn2 = wn.openConnection(makeParam(url2));
                List<String> infos = mapToString(wn.getList(new GetListParameter(conn, url.path)));
                TestJobListener tjl =  (TestJobListener) context.getBean("testJobListener", JobListener.class);
                final CopyParameter param = new CopyParameter("dummy", 1L, conn, infos, conn2, url2.path);
                tjl.jobId = 1;
                (new Thread() {
                    public void run() {
                        wn.copy(param);
                    }}).start();
                while(!tjl.begin) {
                    Thread.currentThread().sleep(100);
                }
                System.out.println("Copying started");
                while(!tjl.end) {
                    Thread.currentThread().sleep(100);
                }
                System.out.println("Copying finished");
                return;

            }

            throw new RuntimeException("unknown " + args[0]);

        }
        catch (Exception e)
        {
            LOGGER.error("Failed", e);
        }
        finally
        {
            if (conn != null) wn.closeConnection(conn);
            if (conn2 != null) wn.closeConnection(conn2);
            context.stop();
            System.exit(0);
        }
    }

    private static List<String> mapToString(List<FileInfo> infos) {
        List<String> resp = new ArrayList<String>();
        for (FileInfo info: infos) {
            resp.add(info.getAbsolutePath());
        }
        return resp;
    } 

    private static OpenConnectionParameter makeParam(URL url) throws Exception
    {
        String user = itOrEmpty(url.userInfo);
        String passw = "";
        if (user.indexOf(':') != -1)
        {
            String[] parts = user.split(":");
            user = parts[0];
            passw = parts[1];
        }
        return new OpenConnectionParameter(url.protocol, url.host, user, passw);
    }

    private static String itOrEmpty(String val)
    {
        if (val == null) return "";
        return val;
    }

}
