package au.org.intersect.dms.applet;

import java.io.IOException;
import java.util.List;

import au.org.intersect.dms.applet.transfer.PcConnection;
import au.org.intersect.dms.encrypt.impl.PublicEncryptAgent;

/**
 * Thread to run the HTTP requests
 * 
 * @author carlos
 * 
 */
public class ConnectionRunner extends Thread
{
	private String encryptedJobId;
	private PcConnection connection;
	private List<ConnectionRunner> runners;
	private String tunnelUrl;
	
	/**
	 * @param downloadApplet
	 */
	public ConnectionRunner(List<ConnectionRunner> runners, String tunnelUrl,String encryptedJobId) {
		this.runners = runners;
		this.encryptedJobId = encryptedJobId;
		this.tunnelUrl = tunnelUrl;
	}

    @Override
    public void run()
    {
        try
        {
            PublicEncryptAgent agent = new PublicEncryptAgent("classpath:/keys/pubTunnelApplet.der");
            connection = new PcConnection(tunnelUrl, encryptedJobId, new HddAccess(), agent);
            connection.run();
        }
        catch (IOException e)
        {
            BrowseApplet.LOGGER.error("Exception during download", e);
        }
        // TODO CHECKSTYLE-OFF: IllegalCatch
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
        	runners.remove(this);
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

	public void stampProgress() {
		connection.stampProgress();
	}

	public boolean progressed() {
		return connection.progressed();
	}

	public Object getError() {
		return connection.getError();
	}

}