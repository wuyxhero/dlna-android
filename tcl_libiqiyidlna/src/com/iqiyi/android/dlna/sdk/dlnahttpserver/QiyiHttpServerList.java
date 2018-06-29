/*******************************************************
 * Copyright (C) 2014 iQIYI.COM - All Rights Reserved
 * 
 * This file is part of {IQIYI_DLAN}.
 * Unauthorized copy of this file, via any medium is strictly prohibited.
 * Proprietary and Confidential.
 * 
 * Author(s): chenjiebin<chenjiebin@qiyi.com> maning<maning@qiyi.com>
 * 
 *******************************************************/
package com.iqiyi.android.dlna.sdk.dlnahttpserver;

import java.net.InetAddress;
import java.util.Vector;

import org.cybergarage.http.HTTPRequestListener;
import org.cybergarage.http.HTTPServer;
import org.cybergarage.net.HostInterface;
import org.cybergarage.upnp.Device;
import org.cybergarage.util.Debug;

import com.iqiyi.android.dlna.sdk.mediarenderer.ControlPointConnectRendererListener;

public class QiyiHttpServerList extends Vector<QiyiHttpServer>
{
	private static final long serialVersionUID = 1L;

	private InetAddress[] binds = null;
	private int port = Device.HTTP_DEFAULT_PORT;

	public QiyiHttpServerList()
	{
	}

	public QiyiHttpServerList(InetAddress[] list, int port)
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
			QiyiHttpServer server = getHTTPServer(n);
			server.addRequestListener(listener);
		}
	}

	public void addControlPointConnectListener(ControlPointConnectRendererListener listener)
	{
		int nServers = size();
		for (int n = 0; n < nServers; n++)
		{
			QiyiHttpServer server = getHTTPServer(n);
			server.addControlPointConnectRendererListener(listener);
		}
	}

	public QiyiHttpServer getHTTPServer(int n)
	{
		return (QiyiHttpServer) get(n);
	}

	////////////////////////////////////////////////
	//	open/close
	////////////////////////////////////////////////

	public void close()
	{
		int nServers = size();
		for (int n = 0; n < nServers; n++)
		{
			QiyiHttpServer server = getHTTPServer(n);
			server.close();
		}
	}

	public boolean open()
	{
		Debug.message("[HTTPServerList] open server...port=" + port);
		QiyiHttpServer httpServer = new QiyiHttpServer();
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
			QiyiHttpServer server = getHTTPServer(n);
			server.start();
		}
	}

	public void stop()
	{
		int nServers = size();
		for (int n = 0; n < nServers; n++)
		{
			QiyiHttpServer server = getHTTPServer(n);
			server.stop();
		}
	}
}
