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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.util.NamedThreadFactory;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.org.intersect.dms.applet.ConnectionRunner;
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
import au.org.intersect.dms.wn.transports.impl.HddConnection;

//TODO CHECKSTYLE-OFF: ClassFanOutComplexity
//TODO CHECKSTYLE-OFF: ClassDataAbstractionCoupling
public class TunnelTest
{
	private static final Logger LOGGER = LoggerFactory.getLogger(TunnelTest.class);
    private ExecutorService appletExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("APPLET"));
    private ExecutorService workerExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("WORKER"));
    private ExecutorService serverExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("SERVER"));
	private File tmpDir;
	private Server server;
	private static int nextId = 10;
    
    @Before
    public void setup() throws Exception
    {
        System.setProperty("DEBUG", "true");
        server = new Server(8777);
        server.setHandler(createServletContext());
        server.setThreadPool(new ExecutorThreadPool(serverExecutor));
        server.setStopAtShutdown(true);
        server.start();
        tmpDir = createTempDirectory();
    }
    
    @After
    public void tearDown() throws Exception
    {
    	server.stop();
    	server.join();	
    }

    @Test
    public void testGetListAndFinish() throws Exception
    {
        String jobId = nextId();
        final String path = tmpDir.getAbsolutePath();
        RunnableTestAction<List<FileInfo>> action = new RunnableTestAction<List<FileInfo>>()
        		{
        	
		        	@Override
		        	public List<FileInfo> performWith(TransportConnection conn) throws IOException
		        	{
		        		return conn.getList(path);
		        	}
		        	
		        	public void check()
		        	{
		    		    check = resp != null && resp.size() == 2;
		        	}
        	
        		};
        		
        connectFromWorker(jobId, path, action);
        ConnectionRunner appletConnection = connectFromApplet(jobId);
        waitFor(10000);
        assertTrue("getList didn't work", action.getCheck());
        assertNull("error in applet", appletConnection.getError());
    }

    @Test
    public void testGetListWithoutFinish() throws Exception
    {
        String jobId = nextId();
        final String path = tmpDir.getAbsolutePath();
        RunnableTestAction<List<FileInfo>> action = new RunnableTestAction<List<FileInfo>>()
        		{
        	
		        	@Override
		        	public List<FileInfo> performWith(TransportConnection conn) throws IOException
		        	{
		        		List<FileInfo> temp = conn.getList(path);
		        		waitFor(10000); // the queue timeout is 5000 after first request succeeds
		        		return temp; 
		        	}
		        	
		        	public void check()
		        	{
		    		    check = resp != null && resp.size() == 2;
		        	}
        	
        		};
        		
        connectFromWorker(jobId, tmpDir.getAbsolutePath(), action);
        ConnectionRunner appletConnection = connectFromApplet(jobId);
        waitFor(10000);
        assertFalse("getList didn't work", action.getCheck());
        assertNotNull("no error in applet", appletConnection.getError());
    }

    @Test
    public void testDelete() throws Exception
    {
        String jobId = nextId();
        final String path = tmpDir.getAbsolutePath();
    	new FileOutputStream(new File(tmpDir, "text-del.txt")).close();
        RunnableTestAction<Boolean> action = new RunnableTestAction<Boolean>()
        		{
        	
		        	@Override
		        	public Boolean performWith(TransportConnection conn) throws IOException
		        	{
		        		FileInfo fileInfo = conn.getInfo(path + "/text-del.txt");
		        		return fileInfo != null && fileInfo.getName().equals("text-del.txt") && conn.delete(fileInfo);
		        	}

					@Override
					public void check() {
						check = resp != null && resp.booleanValue(); 
					}
        	
        		};
        		
        connectFromWorker(jobId, tmpDir.getAbsolutePath(), action);
        ConnectionRunner appletConnection = connectFromApplet(jobId);
        waitFor(10000);
        assertTrue("delete didn't work", action.getCheck());
        assertNull("error in applet", appletConnection.getError());
    }

    @Test
    public void testCopyToApplet() throws Exception
    {
        String jobId = nextId();
        final String path = tmpDir.getAbsolutePath();
        RunnableTestAction<Boolean> action = new RunnableTestAction<Boolean>()
        		{
        	
        			int chunks = 20;
        			
		        	@Override
		        	public Boolean performWith(TransportConnection conn) throws IOException
		        	{
		        		String fname = path + "/text-up.txt";
		        		OutputStream os = conn.openOutputStream(fname, chunks*1000);
		        		copyBytes(os,chunks,1000,100);
		        		conn.closeOutputStream(fname, os);
		        		return true;
		        	}

					@Override
					public void check() {
						try {
							FileInputStream is = new FileInputStream(path + "/text-up.txt");
							check = verifyBytes(is,chunks,1000,100);
							is.close();
							// new File(path + "/text-up.txt").delete();
						} catch (Exception e) {
							e.printStackTrace();
							check = false;
						}
					}
        	
        		};
        		
        connectFromWorker(jobId, tmpDir.getAbsolutePath(), action);
        ConnectionRunner appletConnection = connectFromApplet(jobId);
        waitFor(10000);
        assertTrue("write to applet didn't work", action.getCheck());
        assertNull("error in applet", appletConnection.getError());
    }
    
    @Test
    public void testReadFromApplet() throws Exception
    {
        String jobId = nextId();
        final String path = tmpDir.getAbsolutePath();
        RunnableTestAction<Boolean> action = new RunnableTestAction<Boolean>()
        		{
        	
        			int chunks = 20;
        			String fname = path + "/text-down.txt";
        			
		        	@Override
		        	public Boolean performWith(TransportConnection conn) throws IOException
		        	{
		        		OutputStream os = new FileOutputStream(fname);
		        		copyBytes(os,chunks,1000,100);
		        		os.close();
		        		InputStream is = conn.openInputStream(fname);
		        		check = verifyBytes(is,chunks,1000,100);
		        		conn.closeInputStream(fname, is);
		        		return true;
		        	}

					@Override
					public void check() {
						new File(fname).delete();
					}
        	
        		};
        		
        connectFromWorker(jobId, tmpDir.getAbsolutePath(), action);
        ConnectionRunner appletConnection = connectFromApplet(jobId);
        waitFor(10000);
        assertTrue("read from applet didn't work", action.getCheck());
        assertNull("error in applet", appletConnection.getError());
    }

    @Test
    public void testReadLongFromApplet() throws Exception
    {
        final String jobId = nextId();
        final String path = tmpDir.getAbsolutePath();
        RunnableTestAction<Boolean> action = new RunnableTestAction<Boolean>()
        		{
        	
        			String fname1 = path + "/text-down-1.txt";
        			String fname2 = path + "/text-down-2.txt";

		        	private void copy1(TransportConnection conn, String fname, int chunks, int delay) throws IOException
                                {
		        		OutputStream os = new FileOutputStream(fname);
		        		copyBytes(os,chunks,1000,10);
		        		os.close();
		        		InputStream is = conn.openInputStream(fname);
		        		check = verifyBytes(is,chunks,1000,delay);
		        		conn.closeInputStream(fname, is);
                                        LOGGER.info("Finished " + fname);
                                }
        			
		        	@Override
		        	public Boolean performWith(TransportConnection conn) throws IOException
		        	{
                                        copy1(conn, fname1, 100, 1000);
                                        if (check)
                                        {
                                            copy1(conn, fname2, 20, 100);
                                        }
		        		return true;
		        	}

					@Override
					public void check() {
						new File(fname1).delete();
						new File(fname2).delete();
					}
        	
        		};
        		
        		
        connectFromWorker(jobId, tmpDir.getAbsolutePath(), action);
        ConnectionRunner appletConnection = connectFromApplet(jobId);
        LOGGER.info("Wait for test");
        waitFor(10000);
        assertTrue("read from applet didn't work", action.getCheck());
        assertNull("error in applet", appletConnection.getError());
    }
    

    
    @Test
    public void testCreateDir() throws Exception
    {
        String jobId = nextId();
        final String path = tmpDir.getAbsolutePath();
        RunnableTestAction<Boolean> action = new RunnableTestAction<Boolean>()
        		{
        	
		        	@Override
		        	public Boolean performWith(TransportConnection conn) throws IOException
		        	{
		        		check = conn.createDir(path, "new-dir");
		        		return check;
		        	}

					@Override
					public void check() {
						File fdir = new File(path,"new-dir");
						check = check && fdir.exists() && fdir.isDirectory();
						fdir.delete();
					}
        	
        		};
        		
        connectFromWorker(jobId, tmpDir.getAbsolutePath(), action);
        ConnectionRunner appletConnection = connectFromApplet(jobId);
        waitFor(10000);
        assertTrue("createDir didn't work", action.getCheck());
        assertNull("error in applet", appletConnection.getError());
    }
    
    @Test
    public void testRename() throws Exception
    {
        String jobId = nextId();
        final String path = tmpDir.getAbsolutePath();
    	new FileOutputStream(new File(tmpDir, "text-rename.txt")).close();
        RunnableTestAction<Boolean> action = new RunnableTestAction<Boolean>()
        		{
        	
		        	@Override
		        	public Boolean performWith(TransportConnection conn) throws IOException
		        	{
		        		check = conn.rename(path, "text-rename.txt","text-new-name.txt");
		        		return check;
		        	}

					@Override
					public void check() {
						File file = new File(path,"text-new-name.txt");
						check = check && file.exists() && file.isFile();
						file.delete();
					}
        	
        		};
        		
        connectFromWorker(jobId, tmpDir.getAbsolutePath(), action);
        ConnectionRunner appletConnection = connectFromApplet(jobId);
        waitFor(10000);
        assertTrue("rename didn't work", action.getCheck());
        assertNull("error in applet", appletConnection.getError());
    }
    
    private String nextId()
    {
    	return Integer.toString(++nextId);
    }

    /**
     * copy a number of bytes into an output stream
     * @param os
     * @param number
     * @param k 
     * @param j 
     * @throws IOException 
     */
    private static void copyBytes(OutputStream os, int chunks, int size, int delayMillis) throws IOException
    {
    	byte[] chars = new byte[size];
    	for (int i=0; i < chunks; i++)
    	{
    		for (int j=0; j < size; j++)
    		{
    			chars[j] = (byte)Character.forDigit(j % 16, 16);
    		}
    		os.write(chars,0,size);
    		try {
				Thread.sleep(delayMillis);
			} catch (InterruptedException e) {
				throw new IOException(e);
			}
    	}
        LOGGER.info("copyBytes: done");
    }
    
    private static boolean verifyBytes(InputStream is, int chunks, int size, int delayMillis) throws IOException
    {
    	boolean resp = true;
    	byte[] chars = new byte[size];
    	for (int i=0; resp && i < chunks; i++)
    	{
                LOGGER.info("verifyBytes: chunk"+i);
    		int t = 0;
    		for (int offset = 0; offset < size && (t = is.read(chars, offset, size - offset)) > 0; offset += t)
                {
    		    try {
			Thread.sleep(delayMillis);
		    } catch (InterruptedException e) {
			throw new IOException(e);
	            }
                }
    		for (int j=0; resp && j < size; j++)
    		{
    			resp = chars[j] == (byte)Character.forDigit(j % 16, 16);
    		}
    	}
    	return resp;
    }

	private ConnectionRunner connectFromApplet(final String jobId) throws Exception
    {
        return appletExecutor.submit(new Callable<ConnectionRunner>()
        {
        	List<ConnectionRunner> runners = new ArrayList<ConnectionRunner>();
        	String tunnelUrl = "http://localhost:8777/dms-tunnel-test/app";

            @Override
            public ConnectionRunner call()
            {
				try {
					ConnectionRunner runner = new ConnectionRunner(runners, tunnelUrl, getEncryptedJobId(jobId));
					runner.run();
	                return runner;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
            }
        }).get();
    }

    private String getEncryptedJobId(String jobId) throws Exception
    {
        // THIS HAPPENS IN THE WEB APP AND IT'S SENT TO THE APPLET
        PublicEncryptAgent agent = new PublicEncryptAgent("classpath:/keys/pubTunnelWorker.der");
        byte[] encjobId = agent.process((jobId.toString()).getBytes());
        String jobIdAsHexString = HddUtil.convertByteToHexString(encjobId);
        return jobIdAsHexString;
    }

    private void connectFromWorker(final String jobId, final String path, RunnableTestAction runnable)
    {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                "/META-INF/spring/context-hdd.xml");
        final TransportConnectionTemplate template = ctx.getBean("transportTemplate",
                TransportConnectionTemplate.class);
        ConnectionParams key = new ConnectionParams("hdd", "upload", jobId, null);
        runnable.setup(template, key);
		workerExecutor.submit(runnable);
    }

	private static void waitFor(int waitFor)
	{
		try {
			Thread.currentThread().sleep(waitFor);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

    private ServletContextHandler createServletContext()
    {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("/META-INF/spring/context.xml");
        TunnelController controller = ctx.getBean("tunnelController", TunnelController.class);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/dms-tunnel-test");
        context.addServlet(new ServletHolder(new TunnelServlet(controller)), "/app");
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
        new FileOutputStream(new File(temp, "text-b.txt")).close();
        return temp;
    }
    
    private static abstract class RunnableTestAction<T> implements Runnable, TransportConnectionCallback<T> {
		private TransportConnectionTemplate template;
		private ConnectionParams key;
		protected T resp;
		protected boolean check = false;
		
		public void setup(TransportConnectionTemplate template, ConnectionParams key)
		{
			this.template = template;
			this.key = key;			
		}
		
		@Override
		public void run()
		{
		    resp = template.execute(key, this);
		    template.purgeKey(key);
		    check();
		}
		
		public abstract void check();

		public boolean getCheck()
		{
			return check;
		}
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

        public TunnelServlet(TunnelController controller)
        {
            this.controller = controller;
        }

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
        {
            try
            {
                controller.app(req, resp);
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
                throw new RuntimeException(e);
            }
        }

    }

}
