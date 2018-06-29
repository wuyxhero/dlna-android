package org.cybergarage.upnp;

public interface NetworkStatusListener
{

	public void OnNetworkStatusChanged(NETWORK_STATUS status);

	public void OnResponseTimeGot(long responseTime);

}
