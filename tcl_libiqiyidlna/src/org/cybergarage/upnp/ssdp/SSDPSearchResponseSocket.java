/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: SSDPSearchResponseSocket.java
 *
 *	Revision;
 *
 *	11/20/02
 *		- first revision.
 *	05/28/03
 *		- Added post() to send a SSDPSearchRequest.
 *	01/31/08
 *		- Changed start() not to abort when the interface infomation is null on Android m3-rc37a.
 *	
 ******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.net.DatagramSocket;
import java.net.InetAddress;

import org.cybergarage.upnp.*;
import org.cybergarage.util.Debug;

public class SSDPSearchResponseSocket extends HTTPUSocket implements Runnable
{
	private static SSDPSearchResponseSocket sock = null;

	public static SSDPSearchResponseSocket getInstance() {
		if (sock == null)
			sock =  new SSDPSearchResponseSocket();

		return sock;
	}
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public SSDPSearchResponseSocket()
	{
		setControlPoint(null);
	}

	public SSDPSearchResponseSocket(String bindAddr, int port)
	{
		super(bindAddr, port);
		setControlPoint(null);
	}

	////////////////////////////////////////////////
	//	ControlPoint	
	////////////////////////////////////////////////

	private ControlPoint controlPoint = null;

	public void setControlPoint(ControlPoint ctrlp)
	{
		this.controlPoint = ctrlp;
	}

	public ControlPoint getControlPoint()
	{
		return controlPoint;
	}

	////////////////////////////////////////////////
	//	run	
	////////////////////////////////////////////////

	private Thread deviceSearchResponseThread = null;

	public void run()
	{
		Thread thisThread = Thread.currentThread();

		ControlPoint ctrlPoint = getControlPoint();

		while (deviceSearchResponseThread == thisThread)
		{
			Thread.yield();
			SSDPPacket packet = receive();
			if (packet == null)
				break;
			if (ctrlPoint != null)
			{
				//remove by tengfei remove the packet toString log
				//Debug.message("++++++++searchResponseReceived packet: " + packet.toString());
				Debug.message("++++++++searchResponseReceived get: " + packet.getLocation());
				ctrlPoint.searchResponseReceived(packet);
			}
		}
	}

	public void start()
	{

		StringBuffer name = new StringBuffer("iqiyi.SSDPSearchResponseSocket/");
		DatagramSocket s = getDatagramSocket();
		// localAddr is null on Android m3-rc37a (01/30/08)
		if (s == null)
			;//这边s可能会为null，当有两个控制点同时存在时，就会存在这个问题。

		InetAddress localAddr = s.getLocalAddress();
		if (localAddr != null)
		{
			name.append(s.getLocalAddress()).append(':');
			name.append(s.getLocalPort());
		}
		deviceSearchResponseThread = new Thread(this, name.toString());
		deviceSearchResponseThread.start();
	}

	public void stop()
	{
		deviceSearchResponseThread = null;
	}

	////////////////////////////////////////////////
	//	post
	////////////////////////////////////////////////

	public boolean post(String addr, int port, SSDPSearchResponse res)
	{
		return post(addr, port, res.getHeader());
	}

	////////////////////////////////////////////////
	//	post
	////////////////////////////////////////////////

	public boolean post(String addr, int port, SSDPSearchRequest req)
	{
		return post(addr, port, req.toString());
	}
}
