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

import java.io.IOException;

import javax.swing.SwingUtilities;

import au.org.intersect.dms.applet.transfer.PcConnection;
import au.org.intersect.dms.encrypt.impl.PublicEncryptAgent;


/**
 * Applet to download files to HDD
 */
public class DownloadApplet extends DmsApplet
{
    /**
     * Thread to run the HTTP requests
     * 
     * @author carlos
     * 
     */
    private final class ConnectionRunner extends Thread
    {
        private PcConnection connection;

        @Override
        public void run()
        {
            try
            {
                PublicEncryptAgent agent = new PublicEncryptAgent("classpath:/keys/pubTunnelApplet.der");
                connection = new PcConnection(tunnelUrl, getEncryptedJobId(), new HddAccess(), agent);
                connection.run();
            }
            catch (IOException e)
            {
                LOGGER.error("Exception during download", e);
            }
            // TODO CHECKSTYLE-OFF: IllegalCatch
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                panel.finished();
            }
        }

        @Override
        public void interrupt()
        {
            // TODO CHECKSTYLE-OFF: IllegalCatch
            try
            {
                connection.stop();
            }
            catch (Exception e)
            {
                // ignore
            }
            // TODO CHECKSTYLE-ON: IllegalCatch
            super.interrupt();
        }
    }

    private TransferPannel panel;
    private String tunnelUrl;
    private String encJobId;
    private transient Thread runner;

    public DownloadApplet()
    {
    }

    public void init()
    {
        super.init();
        tunnelUrl = getParameter("tunnelUrl");
        // TODO CHECKSTYLE-OFF: IllegalCatch
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run()
                {
                    createGUI();
                }
            });
        }
        catch (Exception e)
        {
            LOGGER.error("Exception during download initialization", e);
        }
        // TODO CHECKSTYLE-ON: IllegalCatch
    }

    @Override
    public void stop()
    {
        stopJob();
    }

    public synchronized void setJob(String jobId, String encJobId)
    {
        this.encJobId = encJobId;
        panel.setJob(jobId);
        runner = new ConnectionRunner();
        runner.start();
    }

    public synchronized String getEncryptedJobId()
    {
        return encJobId;
    }

    private synchronized void stopJob()
    {
        if (runner != null)
        {
            runner.interrupt();
            runner = null;
        }
    }

    private void createGUI()
    {
        panel = new TransferPannel();
        panel.setOpaque(true);
        setContentPane(panel);
    }

    public String[][] getParameterInfo()
    {
        String[][] info = {new String[] {"tunnelUrl", "URL", "Provide URL to connect to DMS Tunnel (required)"}};
        return info;
    }

}
