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

package au.org.intersect.dms.itest.tunnel;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.org.intersect.dms.applet.HddAccess;
import au.org.intersect.dms.applet.transfer.PcConnection;
import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.encrypt.impl.PublicEncryptAgent;
import au.org.intersect.dms.tunnel.HddUtil;
import au.org.intersect.dms.webtunnel.TunnelController;
import au.org.intersect.dms.wn.ConnectionParams;
import au.org.intersect.dms.wn.TransportConnection;
import au.org.intersect.dms.wn.impl.TransportConnectionCallback;
import au.org.intersect.dms.wn.impl.TransportConnectionTemplate;

//TODO CHECKSTYLE-OFF: ClassFanOutComplexity
//TODO CHECKSTYLE-OFF: ClassDataAbstractionCoupling
public class TunnelTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger(TunnelTest.class);

    private ExecutorService executor = Executors.newFixedThreadPool(3);
    private Boolean check = false;

    @Test
    public void integrationTest() throws Exception
    {
        String jobId = "11";
        Server server = new Server(8777);
        server.setHandler(createServletContext());
        server.setThreadPool(new ExecutorThreadPool());
        server.setStopAtShutdown(true);
        server.start();

        File tmpDir = createTempDirectory();
        connectFromWorker(jobId, tmpDir.getAbsolutePath());
        connectFromApplet(jobId);
        stopServer(server);
        server.join();
        assertTrue(check);
    }

    private void connectFromApplet(String jobId) throws Exception
    {
        PublicEncryptAgent agent = new PublicEncryptAgent("classpath:/keys/pubTunnelApplet.der");
        final PcConnection connection = new PcConnection(
                "http://localhost:8777/dms-tunnel-test/app", getEncryptedJobId(jobId),
                new HddAccess(), agent);
        executor.submit(new Runnable()
        {

            @Override
            public void run()
            {
                connection.run();
            }
        });
    }

    private String getEncryptedJobId(String jobId) throws Exception
    {
        // THIS HAPPENS IN THE WEB APP AND IT'S SENT TO THE APPLET
        PublicEncryptAgent agent = new PublicEncryptAgent("classpath:/keys/pubTunnelWorker.der");
        byte[] encjobId = agent.process((jobId.toString()).getBytes());
        String jobIdAsHexString = HddUtil.convertByteToHexString(encjobId);
        return jobIdAsHexString;
    }

    private void connectFromWorker(String jobId, final String path)
    {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "/META-INF/spring/context-hdd.xml");
        final TransportConnectionTemplate template = ctx.getBean("transportTemplate",
                TransportConnectionTemplate.class);
        final ConnectionParams key = new ConnectionParams("hdd", "upload", jobId, null);
        final TransportConnectionCallback<List<FileInfo>> action = new TransportConnectionCallback<List<FileInfo>>()
        {

            @Override
            public List<FileInfo> performWith(TransportConnection conn) throws IOException
            {
                return conn.getList(path);
            }

        };
        executor.submit(new Runnable()
        {

            @Override
            public void run()
            {
                List<FileInfo> resp = template.execute(key, action);
                check = resp != null && resp.size() == 2;
            }
        });
    }

    private void stopServer(Server server) throws Exception
    {
        Thread.currentThread().sleep(5000);
        server.stop();
    }

    private ServletContextHandler createServletContext()
    {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("/META-INF/spring/context.xml");
        TunnelController controller = ctx.getBean("tunnelController", TunnelController.class);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/dms-tunnel-test");
        context.addServlet(new ServletHolder(new TunnelServlet(controller, "hdd")), "/hdd");
        context.addServlet(new ServletHolder(new TunnelServlet(controller, "app")), "/app");
        context.addServlet(new ServletHolder(new TunnelServlet(controller, "keys")), "/keys");
        return context;
    }

    private static File createTempDirectory() throws IOException
    {
        final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
        final File temp = new File(sysTempDir, "temp" + Long.toString(System.nanoTime()));
        if (!(temp.mkdir()))
        {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }
        new FileOutputStream(new File(temp, "text-a.txt")).close();
        new FileOutputStream(new File(temp, "text-b.txt")).close();
        return temp;
    }

    /**
     * A wrapper that invokes the appropiate method on TunnelController. Take note of the very simple implementation and
     * important assumptions in this method regarding method parameters.
     * 
     * @author carlos
     * 
     */
    private class TunnelServlet extends HttpServlet
    {
        private TunnelController controller;
        private Method method;

        public TunnelServlet(TunnelController controller, String methodName)
        {
            this.controller = controller;
            for (Method method : controller.getClass().getMethods())
            {
                if (method.getName().equals(methodName))
                {
                    this.method = method;
                }
            }
            if (method == null)
            {
                throw new RuntimeException("Cannot find " + methodName + " in "
                        + controller.getClass().getSimpleName());
            }
        }

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
        {
            try
            {
                if (method.getParameterTypes().length == 2)
                {
                    // if two parameters, assumes req, response
                    method.invoke(controller, req, resp);
                }
                else
                {
                    // if one parameter, assume response
                    method.invoke(controller, resp);
                }
            }
            catch (IllegalArgumentException e)
            {
                throw new RuntimeException(e);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
            catch (InvocationTargetException e)
            {
                throw new RuntimeException(e);
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
                throw new RuntimeException(e);
            }
        }

    }

}
