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

package au.org.intersect.dms.wn.transports.impl;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.wn.ConnectionParams;
import au.org.intersect.dms.wn.TransportConnection;
import au.org.intersect.dms.wn.TransportFactory;

public class ConnectionIntegrationTester
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionIntegrationTester.class);

    private static final String[] CONFIG_FILES = {"classpath*:META-INF/spring/applicationContext-workernode*.xml"};

    public static void main(String[] args)
    {

        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(CONFIG_FILES);
        context.registerShutdownHook();
        context.start();

        try
        {
            System.out.println("running");
            /*
            Callable<Boolean> call1 = new Callable<Boolean>()
            {
                @Override
                public Boolean call() throws Exception
                {
                    TransportFactory factory = context.getBean("hddFactory", TransportFactory.class);
                    ConnectionParams params = new ConnectionParams("hdd", "upload", "25", null);
                    TransportConnection conn = (TransportConnection) factory.makeObject(params);
                    FileInfo info = conn.getInfo("/home");
                    LOGGER.info(info.getAbsolutePath() + ":" + info.getSize());
                    String name = "/home/carlos/Desktop/hdd-dir/sample.xml";
                    InputStream is = conn.openInputStream(name);
                    OutputStream os = new FileOutputStream("/home/carlos/pom.xml");
                    byte[] buffer = new byte[256];
                    int l = is.read(buffer);
                    while (l > 0)
                    {
                        os.write(buffer, 0, l);
                        l = is.read(buffer);
                    }
                    conn.closeInputStream(name, is);
                    os.close();
                    factory.passivateObject(params, conn);
                    LOGGER.info("Got it");
                    return true;
                }
            };
            Future<Boolean> fut1 = executor.submit(call1);
            fut1.get();
            */
        }
        catch (Exception e)
        {
            LOGGER.error("Failed", e);
        }
        finally
        {
            context.stop();
            System.exit(0);
        }
    }

}
