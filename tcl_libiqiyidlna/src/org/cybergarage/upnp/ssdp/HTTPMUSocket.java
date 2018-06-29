/******************************************************************
 *
 *	CyberLink for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2004
 *
 *	File: HTTPMU.java
 *
 *	Revision;
 *
 *	11/18/02
 *		- first revision.
 *	09/03/03
 *		- Changed to open the socket using setReuseAddress().
 *	12/10/03
 *		- Fixed getLocalAddress() to return a valid interface address.
 *	02/28/04
 *		- Added getMulticastInetAddress(), getMulticastAddress().
 *	11/19/04
 *		- Theo Beisch <theo.beisch@gmx.de>
 *		- Changed send() to set the TTL as 4.
 *	08/23/07
 *		- Thanks for Kazuyuki Shudo
 *		- Changed receive() to throw IOException.
 *	01/10/08
 *		- Changed getLocalAddress() to return a brank string when the ssdpMultiGroup or ssdpMultiIf is null on Android m3-rc37a.
 *	
 ******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.io.IOException;

import org.cybergarage.http.HTTPRequest;
import org.cybergarage.upnp.UPnP;
import org.cybergarage.util.Debug;

// Dummy Class for Android m3-rc37a
// import org.cybergarage.android.MulticastSocket;

public class HTTPMUSocket
{
	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	private InetSocketAddress ssdpMultiGroup = null;
	private MulticastSocket ssdpMultiSock = null;
	private DatagramSocket ssdpboardSock = null;
	public DatagramSocket mDatagramSendBroadSock;
	private NetworkInterface ssdpMultiIf = null;

	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public HTTPMUSocket()
	{
	}

	public HTTPMUSocket(String addr, int port, String bindAddr)
	{
		open(addr, port, bindAddr);
	}

	protected void finalize()
	{
		close();
	}

	////////////////////////////////////////////////
	//	bindAddr
	////////////////////////////////////////////////

	public String getLocalAddress()
	{
		if (ssdpMultiGroup == null || ssdpMultiIf == null)
			return "";
		InetAddress mcastAddr = ssdpMultiGroup.getAddress();
		Enumeration<InetAddress> addrs = ssdpMultiIf.getInetAddresses();
		while (addrs.hasMoreElements())
		{
			InetAddress addr = (InetAddress) addrs.nextElement();
			if (mcastAddr instanceof Inet6Address && addr instanceof Inet6Address)
				return addr.getHostAddress();
			if (mcastAddr instanceof Inet4Address && addr instanceof Inet4Address)
				return addr.getHostAddress();
		}
		return "";
	}

	/**
	 * 
	 * @return the destination port for multicast packet
	 * @since 1.8
	 */
	public int getMulticastPort()
	{
		return ssdpMultiGroup.getPort();
	}

	/**
	 * 
	 * @return the source port for multicast packet
	 * @since 1.8
	 */
	public int getLocalPort()
	{
		return ssdpMultiSock.getLocalPort();
	}

	/**
	 * 
	 * @return the opened {@link MulticastSocket}
	 * @since 1.8
	 */
	public MulticastSocket getSocket()
	{
		return ssdpMultiSock;
	}

	////////////////////////////////////////////////
	//	MulticastAddr
	////////////////////////////////////////////////

	public InetAddress getMulticastInetAddress()
	{
		return ssdpMultiGroup == null ? null : ssdpMultiGroup.getAddress();
	}

	public String getMulticastAddress()
	{
		return getMulticastInetAddress().getHostAddress();
	}

	/**
	 * @param multiAddr
	 *            {@link String} rappresenting the multicast hostname to join into.
	 * @param multiPort
	 *            int rappresenting the port to be use poth as source and destination
	 * @param localAddr
	 *            {@link InetAddress} which identify the hostname of the interface to use for sending and recieving multicast
	 *            packet
	 */
	public boolean open(String multiAddr, int multiPort, InetAddress localAddr)
	{
		try
		{
			ssdpMultiSock = new MulticastSocket(null);
			ssdpMultiSock.setReuseAddress(true);
			InetSocketAddress bindSockAddr = new InetSocketAddress(multiPort);
			ssdpMultiSock.bind(bindSockAddr);
			ssdpMultiGroup = new InetSocketAddress(InetAddress.getByName(multiAddr), multiPort);
			ssdpMultiIf = NetworkInterface.getByInetAddress(localAddr);
			ssdpMultiSock.joinGroup(ssdpMultiGroup, ssdpMultiIf);
			ssdpMultiSock.setNetworkInterface(ssdpMultiIf);
			Debug.message("[openReceive 0] Join Multicast Group " + multiAddr + ":" + multiPort + localAddr);
		} catch (Exception e)
		{
			Debug.message("[Error] Fail to open the SSDP multicast port");
			Debug.warning(e);
			return false;
		}

		return true;
	}

	public boolean openReceive(String multicastAddr, int multicastPort, String localAddr)
	{
		try
		{
			ssdpMultiSock = new MulticastSocket(null);
			ssdpMultiSock.setReuseAddress(true);
			InetSocketAddress bindSockAddr = new InetSocketAddress(multicastPort);
			ssdpMultiSock.bind(bindSockAddr);
			ssdpMultiGroup = new InetSocketAddress(InetAddress.getByName(multicastAddr), multicastPort);
			ssdpMultiIf = NetworkInterface.getByInetAddress(InetAddress.getByName(localAddr));
			ssdpMultiSock.joinGroup(ssdpMultiGroup, ssdpMultiIf);
			Debug.message("[openReceive 1] Join Multicast Group " + multicastAddr + ":" + multicastPort + "/" + localAddr);
		} catch (Exception e)
		{
			Debug.message("[Error] Fail to open the SSDP multicast port");
			Debug.warning(e);
			return false;
		}

		return true;
	}

	public boolean close()
	{
		try
		{
			if (ssdpMultiSock != null)
			{
				ssdpMultiSock.leaveGroup(ssdpMultiGroup, ssdpMultiIf);
				ssdpMultiSock.close();
				ssdpMultiSock = null;
			}
		} catch (Exception e)
		{
			Debug.message("[Error] Fail to close SSDP multicast socket");
			Debug.warning(e);
			return false;
		}

		return true;
	}

	public boolean open(String dstAddr, int dstPort, String bindAddr)
	{
		try
		{
			return open(dstAddr, dstPort, InetAddress.getByName(bindAddr));
		} catch (Exception e)
		{
			Debug.warning(e);
			return false;
		}
	}

	public boolean openBroad(String bindAddr)
	{
		try
		{
			mDatagramSendBroadSock = new DatagramSocket(null);
			mDatagramSendBroadSock.bind(new InetSocketAddress(InetAddress.getByName(bindAddr), 0));
		} catch (Exception e)
		{
			Debug.message("[Error] Fail to open SSDP broadcast socket");
			Debug.warning(e);
			return false;
		}

		return true;
	}

	public boolean closeBroad()
	{
		try
		{
			if (mDatagramSendBroadSock != null)
			{
				mDatagramSendBroadSock.close();
				mDatagramSendBroadSock = null;
			}
		} catch (Exception e)
		{
			Debug.message("[Error] Fail to close SSDP broadcast socket");
			Debug.warning(e);
			return false;
		}

		return true;
	}

	////////////////////////////////////////////////
	//	send
	////////////////////////////////////////////////

	public boolean send(String msg)
	{
		try
		{
			byte[] ms = msg.getBytes();

			Debug.message("Send multicast packet...[239.255.255.250][" + msg.split("\n")[0].trim() + "]["
					+ ssdpMultiSock.getNetworkInterface().getDisplayName() + "]");
			DatagramPacket dgmPacket = new DatagramPacket(ms, ms.length, ssdpMultiGroup);
			ssdpMultiSock.setTimeToLive(UPnP.getTimeToLive()); // 没明白为什么要设TTL
			ssdpMultiSock.send(dgmPacket);

//			String temp = mDatagramSendBroadSock.getLocalAddress().getHostName();
//			int pos = temp.lastIndexOf('.');
//			String broadAddr = temp.subSequence(0, pos).toString() + ".255";
			Debug.message("Send broadcast packet...[255.255.255.255][" + msg.split("\n")[0].trim() + "]");
			DatagramPacket packet = new DatagramPacket(ms, ms.length, InetAddress.getByName("255.255.255.255"), 39390);
			mDatagramSendBroadSock.send(packet);
		} catch (Exception e)
		{
			Debug.warning(e);
			return false;
		}
		return true;
	}

	////////////////////////////////////////////////
	//	post (HTTPRequest)
	////////////////////////////////////////////////

	public boolean post(HTTPRequest req)
	{
		return send(req.toString());
	}

	////////////////////////////////////////////////
	//	reveive
	////////////////////////////////////////////////

	public boolean openBroadForReceive()
	{
		try
		{
			ssdpboardSock = new DatagramSocket(null); // 指定Null很重要，否则Java会自动随机选个可用端口来绑定
			ssdpboardSock.setReuseAddress(true); // 绑定之前先设置Reuse
			ssdpboardSock.bind(new InetSocketAddress(39390)); // 然后再绑定
			Debug.message("openBroadForReceive REUSEADDR is enabled: " + ssdpboardSock.getReuseAddress()); // 返回结果是true，说明才有效
		} catch (Exception e)
		{
			Debug.message("[Error] Fail to open SSDP broadcast socket");
			Debug.warning(e);
			return false;
		}
		return true;
	}

	public boolean closeBroadForReceive()
	{
		try
		{
			if (ssdpboardSock != null)
			{
				ssdpboardSock.close();
				ssdpboardSock = null;
			}
		} catch (Exception e)
		{
			Debug.message("[Error] Fail to close SSDP broad socket");
			Debug.warning(e);
			return false;
		}
		return true;
	}

	public SSDPPacket receive() throws IOException
	{
		byte ssdvRecvBuf[] = new byte[SSDP.RECV_MESSAGE_BUFSIZE];
		SSDPPacket recvPacket = new SSDPPacket(ssdvRecvBuf);
		recvPacket.setLocalAddress(getLocalAddress());

		// Thanks for Kazuyuki Shudo (08/23/07)
		// Thanks for Stephan Mehlhase (2010-10-26)
		if (ssdpMultiSock != null)
		{
			ssdpMultiSock.receive(recvPacket.getDatagramPacket()); // throws IOException
			recvPacket.updateHeaderMap();
			Debug.message("Receive multicast packet...[" + recvPacket.getRemoteAddress() + "] [" + recvPacket.getMAN()
					+ " " + recvPacket.getNTS() + "]");
		} else
			throw new IOException("Multicast socket has already been closed.");

		recvPacket.setTimeStamp(System.currentTimeMillis());

		return recvPacket;
	}

	public SSDPPacket receive2() throws IOException
	{
		byte ssdvRecvBuf[] = new byte[SSDP.RECV_MESSAGE_BUFSIZE];
		SSDPPacket recvPacket = new SSDPPacket(ssdvRecvBuf);
		recvPacket.setLocalAddress(getLocalAddress());

		if (ssdpboardSock != null)
		{
			ssdpboardSock.receive(recvPacket.getDatagramPacket()); // throws IOException
			recvPacket.updateHeaderMap();
			Debug.message("Receive broadcast packet...[" + recvPacket.getRemoteAddress() + "] [" + recvPacket.getMAN()
					+ " " + recvPacket.getNTS() + "]");
		} else
			throw new IOException("Broadcast socket has already been closed.");

		recvPacket.setTimeStamp(System.currentTimeMillis());

		return recvPacket;
	}
}
