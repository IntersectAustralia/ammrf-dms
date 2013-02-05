package au.org.intersect.dms.webtunnel;

/**
 * Tunnel Job Queue
 * It is a singleton and shared by worker and web threads.
 * @author carlos
 */
public class TunnelJobQueue extends TunnelMap<TunnelJobTracker>
{
	
	private static TunnelMap<TunnelJobTracker> theInstance = new TunnelMap<TunnelJobTracker>();
	
	public static TunnelMap<TunnelJobTracker> getInstance()
	{
		return theInstance;
	}

}
