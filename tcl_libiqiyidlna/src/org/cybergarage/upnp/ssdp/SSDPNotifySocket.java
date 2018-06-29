/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File: SSDPNotifySocket.java
 *
 *	Revision;
 *
 *	11/20/02
 *		- first revision.
 *	05/13/03
 *		- Added support for IPv6.
 *	02/20/04
 *		- Inma Marin Lopez <inma@dif.um.es>
 *		- Added a multicast filter using the SSDP pakcet.
 *	04/20/05
 *		- Mikael Hakman <mhakman@dkab.net>
 *		- Handle receive() returning null.
 *		- Added close() in stop().
 *	08/23/07
 *		- Thanks for Kazuyuki Shudo
 * 		- Changed run() to catch IOException of HTTPMUSocket::receive().
 *	01/31/08
 *		- Changed start() not to abort when the interface infomation is null on Android m3-rc37a.
 *	
 ******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.net.*;
import java.util.List;
import java.io.IOException;

import org.cybergarage.net.*;
import org.cybergarage.util.*;
import org.cybergarage.http.*;
import org.cybergarage.upnp.*;

/**
 * 
 * This class identifies a SSDP socket only for <b>notifing packet</b>.<br>
 * 
 * @author Satoshi "skonno" Konno
 * @author Stefano "Kismet" Lenzi
 * @version 1.8
 * 
 */
public class SSDPNotifySocket extends HTTPMUSocket implements Runnable
{
	private boolean useIPv6Address;

	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public SSDPNotifySocket(String bindAddr)
	{
		String ssdpAddr = SSDP.ADDRESS;
		useIPv6Address = false;
		if (HostInterface.isIPv6Address(bindAddr) == true)
		{
			ssdpAddr = SSDP.getIPv6Address();
			useIPv6Address = true;
		}
		open(ssdpAddr, SSDP.PORT, bindAddr);
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

	/**
	 * This method send a {@link SSDPNotifyRequest} over {@link SSDPNotifySocket}
	 * 
	 * @param req
	 *            the {@link SSDPNotifyRequest} to send
	 * @return true if and only if the trasmission succeced<br>
	 *         Because it rely on UDP doesn't mean that it's also recieved
	 */
	public boolean post(SSDPNotifyRequest req)
	{
		String ssdpAddr = SSDP.ADDRESS;
		if (useIPv6Address == true)
			ssdpAddr = SSDP.getIPv6Address();
		req.setHost(ssdpAddr, SSDP.PORT);
		return post((HTTPRequest) req);
	}

	////////////////////////////////////////////////
	//	run	
	////////////////////////////////////////////////
	private Thread deviceNotifyThread = null;
	private Thread deviceNotifyThread2 = null;

	public void run()
	{
		Thread thisThread = Thread.currentThread();

		ControlPoint ctrlPoint = getControlPoint();

		while ((deviceNotifyThread == thisThread) || (deviceNotifyThread2 == thisThread))
		{
			Thread.yield();

			// Thanks for Kazuyuki Shudo (08/23/07)
			SSDPPacket packet = null;

			try
			{
				if (deviceNotifyThread == thisThread)
				{
					packet = receive();
				} else
				{
					packet = receive2();
				}
			} catch (IOException e)
			{
				Debug.message("++++++++SSDPNotifySocket.java deviceNotifyThread exit");
				break;
			}

			try
			{
				// Thanks for Mikael Hakman (04/20/05)
				if (packet == null)
					continue;
	
				// Thanks for Inma (02/20/04)
				InetAddress maddr = getMulticastInetAddress();
				InetAddress pmaddr = packet.getHostInetAddress();
	
				if (maddr == null || maddr.equals(pmaddr) == false)
				{
					Debug.warning("Invalidate Multicast Recieved from IP " + maddr + " on " + pmaddr);
					continue;
				}
	
				// TODO 这里判断packet.getLocation()中的IP和packet.getLocalAddress()中的IP
				// 如果不在一个网段则丢弃，防止订阅时填写的CALLBACK错误
				// SUBSCRIBE /_urn:schemas-upnp-org:service:PrivateServer_event HTTP/1.1
				// Content-Length: 0	GID: external	HOST: 192.168.31.121:39620
				// CALLBACK: <http://10.163.126.162:8058/evetSub>	NT: upnp:event	TIMEOUT: Second-180
				// 其实电视够端端已经修复了这个BUG，不会在Interface上出现别的Interface的IP的SSDP包，但是有旧版本电视果在网路里
	
				String locationStr = packet.getLocation();
				if (packet.isAlive() == true) {
					Debug.message("notifyReceived sender = " + locationStr);
					String tmp = locationStr.replace("http://", "");
					int pos = tmp.contains(":") ? tmp.lastIndexOf(":") : tmp.indexOf("/");
					String locationIp = tmp.substring(0, pos);
					if (!checkSameNetwork (locationIp, packet.getLocalAddress())) {
						Debug.warning("checkSameNetwork false! continue...");
						continue;
					}
				}
	
				// TODO Must be performed on a different Thread in order to prevent UDP packet losses.
				if (ctrlPoint != null)
					ctrlPoint.notifyReceived(packet);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Debug.warning("deviceNotifyThread " + Thread.currentThread().getName() + " exit");
	}

	public static boolean checkSameNetwork(String locationIp, String localIp) {
		Debug.message("checkSameNetwork locationIp = " + locationIp + " localIp = " + localIp);

		try {
			InetAddress ip = InetAddress.getByName(localIp);
			NetworkInterface ni = NetworkInterface.getByInetAddress(ip);// 搜索绑定了指定IP地址的网络接口

			if (ni == null) {
				Debug.message("checkSameNetwork no interface with localIp " + locationIp);
				return false;
			}

			List<InterfaceAddress> list = ni.getInterfaceAddresses();
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getAddress().getHostAddress().equals(localIp)) {
					System.out.println("Cur:" + list.get(i).getAddress().getHostAddress());

					int mask = 0xFFFFFFFF << (32 - list.get(i).getNetworkPrefixLength());

					String[] locationips = locationIp.split("\\.");
					int locationIpAddr = (Integer.parseInt(locationips[0]) << 24) | (Integer.parseInt(locationips[1]) << 16)
							| (Integer.parseInt(locationips[2]) << 8) | Integer.parseInt(locationips[3]);

					String localips[] = localIp.split("\\.");
					int localIpAddr = Integer.valueOf(localips[0]) << 24 | Integer.valueOf(localips[1]) << 16
							| Integer.valueOf(localips[2]) << 8 | Integer.valueOf(localips[3]);

					return (locationIpAddr & mask) == (localIpAddr & mask);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public void start()
	{
		StringBuffer name = new StringBuffer("iqiyi.SSDPNotifySocket/");
		String localAddr = this.getLocalAddress();
		// localAddr is null on Android m3-rc37a (01/30/08)
		if (localAddr != null && 0 < localAddr.length())
		{
			name.append(this.getLocalAddress()).append(':');
			name.append(this.getLocalPort()).append(" -> ");
			name.append(this.getMulticastAddress()).append(':');
			name.append(this.getMulticastPort());
		}
		deviceNotifyThread = new Thread(this, name.toString());
		deviceNotifyThread.start();

		StringBuffer name2 = new StringBuffer("iqiyi.SSDPNotifySocket2/");
		deviceNotifyThread2 = new Thread(this, name2.toString());
		deviceNotifyThread2.start();
	}

	public void stop()
	{
		// Thanks for Mikael Hakman (04/20/05)
		close();
		closeBroadForReceive();

		deviceNotifyThread = null;
		deviceNotifyThread2 = null;
	}
}
