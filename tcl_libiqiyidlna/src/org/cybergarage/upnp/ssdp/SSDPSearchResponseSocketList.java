/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File: SSDPSearchResponseSocketList.java
 *
 *	Revision;
 *
 *	05/08/03
 *		- first revision.
 *	05/28/03
 *		- Added post() to send a SSDPSearchRequest.
 *
 ******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.net.InetAddress;
import java.util.*;

import org.cybergarage.net.*;

import org.cybergarage.upnp.*;
import org.cybergarage.util.Debug;

public class SSDPSearchResponseSocketList extends Vector<SSDPSearchResponseSocket>
{
	private static final long serialVersionUID = 1L;

	private InetAddress[] binds = null;

	public SSDPSearchResponseSocketList()
	{
	}

	/**
	 * 
	 * @param binds
	 *            The host to bind.Use <tt>null</tt> for the default behavior
	 */
	public SSDPSearchResponseSocketList(InetAddress[] binds)
	{
		this.binds = binds;
	}

	////////////////////////////////////////////////
	//	ControlPoint

	////////////////////////////////////////////////
	//	ControlPoint
	////////////////////////////////////////////////

	public void setControlPoint(ControlPoint ctrlPoint)
	{
		int nSockets = size();
		for (int n = 0; n < nSockets; n++)
		{
			SSDPSearchResponseSocket sock = getSSDPSearchResponseSocket(n);
			sock.setControlPoint(ctrlPoint);
		}
	}

	////////////////////////////////////////////////
	//	get
	////////////////////////////////////////////////

	public SSDPSearchResponseSocket getSSDPSearchResponseSocket(int n)
	{
		return (SSDPSearchResponseSocket) get(n);
	}

	////////////////////////////////////////////////
	//	Methods
	////////////////////////////////////////////////

	public boolean open(int port)
	{
		InetAddress[] binds = this.binds;
		String[] bindAddresses;
		if (binds != null)
		{
			bindAddresses = new String[binds.length];
			for (int i = 0; i < binds.length; i++)
			{
				bindAddresses[i] = binds[i].getHostAddress();
				Debug.message("getNHostAddresses=" + bindAddresses[i] + "; n=" + i);
			}
		} else
		{
			int nHostAddrs = HostInterface.getNHostAddresses();
			bindAddresses = new String[nHostAddrs];
			for (int n = 0; n < nHostAddrs; n++)
			{
				bindAddresses[n] = HostInterface.getHostAddress(n);
				Debug.message("getNHostAddresses=" + bindAddresses[n] + "; n=" + n);
			}
		}
		try
		{
			for (int j = 0; j < bindAddresses.length; j++)
			{
				//这边端口是可能出现BUG，这里要进行一次判定，因为如果有一个控制点在运行，再运行一个控制点就崩溃了
				//SSDPSearchResponseSocket socket = new SSDPSearchResponseSocket(bindAddresses[j], port);
				//Bug修复
				SSDPSearchResponseSocket socket = new SSDPSearchResponseSocket();
				if (HostInterface.isIPv6Address(bindAddresses[j]) == true)
					continue; //Ignore IPV6
				if (socket.open(bindAddresses[j], port) == false)//修复这个BUG
				{
					Debug.message("getNHostAddresses=" + bindAddresses[j] + "; j=" + j + "; port=" + port);
					stop();
					close();
					clear();
					return false;
				} else
					Debug.message("getNHostAddresses open success" + bindAddresses[j] + "; j=" + j + "; port=" + port);

				add(socket);
			}
		} catch (Exception e)
		{
			stop();
			close();
			clear();
			return false;
		}
		return true;
	}

	public boolean open()
	{
		return open(SSDP.PORT);
	}

	public void close()
	{
		int nSockets = size();
		for (int n = 0; n < nSockets; n++)
		{
			SSDPSearchResponseSocket sock = getSSDPSearchResponseSocket(n);
			sock.close();
		}
		clear();
	}

	////////////////////////////////////////////////
	//	Methods
	////////////////////////////////////////////////

	public void start()
	{
		int nSockets = size();
		for (int n = 0; n < nSockets; n++)
		{
			SSDPSearchResponseSocket sock = getSSDPSearchResponseSocket(n);
			sock.start();
		}
	}

	public void stop()
	{
		int nSockets = size();
		for (int n = 0; n < nSockets; n++)
		{
			SSDPSearchResponseSocket sock = getSSDPSearchResponseSocket(n);
			sock.stop();
		}
	}

	////////////////////////////////////////////////
	//	Methods
	////////////////////////////////////////////////

	public boolean post(SSDPSearchRequest req)
	{
		boolean ret = true;
		int nSockets = size();
		for (int n = 0; n < nSockets; n++)
		{
			SSDPSearchResponseSocket sock = null;
			try
			{
				sock = getSSDPSearchResponseSocket(n);
			} catch (Exception e)
			{
				Debug.message("++++Do search when stop, exception happened!!!");
				e.printStackTrace();
				return false;
			}
			String bindAddr = sock.getLocalAddress();
			req.setLocalAddress(bindAddr);
			String ssdpAddr = SSDP.ADDRESS;
			if (HostInterface.isIPv6Address(bindAddr) == true)
			{
				ssdpAddr = SSDP.getIPv6Address();//略过IPV6,
				continue;
			}
			//sock.joinGroup(ssdpAddr, SSDP.PORT, bindAddr);
			if (sock.post(ssdpAddr, SSDP.PORT, req) == false)
				ret = false;
			//sock.leaveGroup(ssdpAddr, SSDP.PORT, bindAddr);
		}
		return ret;
	}

}
