/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File: HTTPServerList.java
 *
 *	Revision;
 *
 *	05/08/03
 *		- first revision.
 *	24/03/06
 *		- Stefano Lenzi:added debug information as request by Stephen More
 *
 ******************************************************************/

package org.cybergarage.http;

import java.net.InetAddress;
import java.util.Vector;

import org.cybergarage.upnp.Device;
import org.cybergarage.util.Debug;

public class HTTPServerList extends Vector<HTTPServer>
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private InetAddress[] binds = null;
	private int port = Device.HTTP_DEFAULT_PORT;

	public HTTPServerList()
	{
	}

	public HTTPServerList(InetAddress[] list, int port)
	{
		this.binds = list;
		this.port = port;
	}

	////////////////////////////////////////////////
	//	Methods
	////////////////////////////////////////////////

	public void addRequestListener(HTTPRequestListener listener)
	{
		int nServers = size();
		for (int n = 0; n < nServers; n++)
		{
			HTTPServer server = getHTTPServer(n);
			server.addRequestListener(listener);
		}
	}

	public HTTPServer getHTTPServer(int n)
	{
		return (HTTPServer) get(n);
	}

	////////////////////////////////////////////////
	//	open/close
	////////////////////////////////////////////////

	public void close()
	{
		int nServers = size();
		for (int n = 0; n < nServers; n++)
		{
			HTTPServer server = getHTTPServer(n);
			server.close();
		}
	}

	public boolean open()
	{
		Debug.message("[HTTPServerList] open server...port=" + port);
		HTTPServer httpServer = new HTTPServer();
		if (httpServer.open(port) == false)
		{
			Debug.message("[HTTPServerList] open server failed...port=" + port);
			close();
			clear();
			return false;
		} else
		{
			Debug.message("[HTTPServerList] open server succeed...port=" + port);
			add(httpServer);
			return true;
		}
	}

	public boolean open(int port)
	{
		this.port = port;
		return open();
	}

	////////////////////////////////////////////////
	//	start/stop
	////////////////////////////////////////////////

	public void start()
	{
		int nServers = size();
		for (int n = 0; n < nServers; n++)
		{
			HTTPServer server = getHTTPServer(n);
			server.start();
		}
	}

	public void stop()
	{
		int nServers = size();
		for (int n = 0; n < nServers; n++)
		{
			HTTPServer server = getHTTPServer(n);
			server.stop();
		}
	}

}
