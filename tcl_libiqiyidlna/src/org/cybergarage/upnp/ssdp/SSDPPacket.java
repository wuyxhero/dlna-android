/******************************************************************
 *
 *	CyberLink for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File: SSDPPacket.java
 *
 *	Revision;
 *
 *	11/18/02
 *		- first revision.
 *	05/13/03
 *		- Added getLocalAddress().
 *	11/01/04
 *		- Theo Beisch <theo.beisch@gmx.de>
 *		- Fixed isRootDevice() to check the ST header.
 *	11/19/04
 *		- Theo Beisch <theo.beisch@gmx.de>
 *		- Changed getRemoteAddress() to return the adresss instead of the host name.
 *
 ******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.net.*;
import java.util.Locale;
import java.util.Map;

import org.cybergarage.http.*;
import org.cybergarage.upnp.device.*;
import org.cybergarage.util.Debug;

public class SSDPPacket
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public SSDPPacket(byte[] buf, int length)
	{
		dgmPacket = new DatagramPacket(buf, length);
		mHeaderMap = HTTPHeader.getAllValues(buf);
	}

	public SSDPPacket(byte[] buf) {
		dgmPacket = new DatagramPacket(buf, buf.length);
	}

	public void updateHeaderMap() {
		mHeaderMap = HTTPHeader.getAllValues(dgmPacket.getData());
	}

	private Map<String, String> mHeaderMap = null;

	////////////////////////////////////////////////
	//	DatagramPacket
	////////////////////////////////////////////////

	private DatagramPacket dgmPacket = null;

	public DatagramPacket getDatagramPacket()
	{
		return dgmPacket;
	}

	////////////////////////////////////////////////
	//	addr
	////////////////////////////////////////////////

	private String localAddr = "";

	public void setLocalAddress(String addr)
	{
		localAddr = addr;
	}

	public String getLocalAddress()
	{
		return localAddr;
	}

	////////////////////////////////////////////////
	//	Time
	////////////////////////////////////////////////

	private long timeStamp;

	public void setTimeStamp(long value)
	{
		timeStamp = value;
	}

	public long getTimeStamp()
	{
		return timeStamp;
	}

	////////////////////////////////////////////////
	//	Remote host
	////////////////////////////////////////////////

	public InetAddress getRemoteInetAddress()
	{
		return getDatagramPacket().getAddress();
	}

	public String getRemoteAddress()
	{
		// Thanks for Theo Beisch (11/09/04)
		return getDatagramPacket().getAddress().getHostAddress();
	}

	public int getRemotePort()
	{
		return getDatagramPacket().getPort();
	}

	/**
	 * 对从数据报中获取http header的函数增加缓存。<br>
	 * 优化后getXXX()调用从之前的8.6ms,下降到2.9ms.
	 * 
	 * @param data
	 * @param name
	 * @return
	 */
	private String getValue(String name)
	{
		String value = mHeaderMap.get(name.toUpperCase(Locale.getDefault()));

		return value;
	}

	public String getHost()
	{
		return getValue(HTTP.HOST);
	}

	public String getCacheControl()
	{
		return getValue(HTTP.CACHE_CONTROL);
	}

	public String getLocation()
	{
		return getValue(HTTP.LOCATION);
	}

	public String getMAN()
	{
		return getValue(HTTP.MAN);
	}

	public String getST()
	{
		return getValue(HTTP.ST);
	}

	public String getNT()
	{
		return getValue(HTTP.NT);
	}

	public String getNTS()
	{
		return getValue(HTTP.NTS);
	}

	public String getMyName()
	{
		return getValue(HTTP.MYNAME);
	}

	public String getFileMd5()
	{
		return getValue(HTTP.FILEMD5);
	}

	public String getFriendlyName()
	{
		return getValue(HTTP.MYNAME);
	}

	public int getQiyiHttpPort()
	{
		String port = getValue(HTTP.IQIYIPORT);
		try
		{
			return Integer.parseInt(port);
		} catch (Exception e)
		{
			return 39521;
		}
	}

	public int getQiyiUDPHttpPort()
	{
		String port = getValue(HTTP.IQIYIUDPPORT);
		int retport = 39522;
		try
		{
			retport = Integer.parseInt(port);
		} catch (NumberFormatException e)
		{
			Debug.message("Invalid port = " + port);
		}
		return retport;
	}

	public int getQiyiVersion()
	{
		String version = getValue(HTTP.IQIYIVERSION);
		try
		{
			return Integer.parseInt(version);
		} catch (Exception e)
		{
			return -1;
		}
	}

	public int getQiyiDeviceType()
	{
		String type = getValue(HTTP.IQIYIDEVICE);
		try
		{
			return Integer.parseInt(type);
		} catch (Exception e)
		{
			return 0;
		}
	}

	public int getQiyiDeviceVersion()
	{
		String version = getValue(HTTP.DEVICEVERSION);
		try
		{
			return Integer.parseInt(version);
		} catch (Exception e)
		{
			return 0;
		}
	}

	public int getTvguoFeatureBitmap()
	{
		String map = getValue(HTTP.TVGUOFEATUREBITMAP);
		try
		{
			return Integer.parseInt(map);
		} catch (Exception e)
		{
			return 0;
		}
	}

	public long getTvguoMarketChannel()
	{
		String map = getValue(HTTP.TVGUOMARKETCHANNEL);
		try
		{
			return Long.parseLong(map);
		} catch (Exception e)
		{
			return 0;
		}
	}

	public String getServer()
	{
		return getValue(HTTP.SERVER);
	}

	public String getQIYIConnect()
	{
		return getValue(HTTP.IQIYICONNECTION);
	}

	/*
	 * 是否为奇艺的盒子
	 */
	public boolean isQiyiServer()
	{
		if (getServer() != null)
		{
			if (getServer().toLowerCase(Locale.getDefault()).contains("iqiyi") == true)
			{
				return true;
			}
		}
		return false;
	}

	/*
	 * 是否为路由器
	 */
	public boolean isRounterServer()
	{
		if (getServer() != null)
		{
			if (getServer().toLowerCase(Locale.getDefault()).contains("router") == true)//为路由器
			{
				return true;
			}
		}
		return false;
	}

	/*
	 * 是否为微软默认的UPNP设备，这些设备应该过滤掉
	 */
	public boolean isMicrosoftServer()
	{
		if (getServer() != null)
		{
			if (getServer().contains("Microsoft-Windows") == true)//为windows的设备
			{
				return true;
			}
		}
		return false;
	}

	/*
	 * 判断是否支持长连接
	 */
	public boolean isSupperConnectKeepAlive()
	{
		String qiyiConnect = getQIYIConnect();
		if (qiyiConnect != null && qiyiConnect.length() > 0)
		{
			if (qiyiConnect.contains(HTTP.KEEP_ALIVE) == true)
				return true;

			if (qiyiConnect.contains(HTTP.CLOSE) == true)
				return false;
		}
		return false;
	}

	public String getUSN()
	{
		return getValue(HTTP.USN);
	}

	public int getMX()
	{
		try
		{
			return Integer.parseInt(getValue(HTTP.MX));
		} catch (Exception e)
		{
			return 0;
		}
	}

	public String getHEADERCAT()
	{
		return getValue(HTTPHeader.HEADERCAT);
	}

	////////////////////////////////////////////////
	//	Access Methods
	////////////////////////////////////////////////

	public InetAddress getHostInetAddress()
	{
		String addrStr = "127.0.0.1";
		String host = getHost();
		int canmaIdx = host.lastIndexOf(":");
		if (0 <= canmaIdx)
		{
			addrStr = host.substring(0, canmaIdx);
			if (addrStr.charAt(0) == '[')
				addrStr = addrStr.substring(1, addrStr.length());
			if (addrStr.charAt(addrStr.length() - 1) == ']')
				addrStr = addrStr.substring(0, addrStr.length() - 1);
		}
		InetSocketAddress isockaddr = new InetSocketAddress(addrStr, 0);
		return isockaddr.getAddress();
	}

	////////////////////////////////////////////////
	//	Access Methods (Extension)
	////////////////////////////////////////////////

	public boolean isRootDevice()
	{
		if (NT.isRootDevice(getNT()) == true)
			return true;
		// Thanks for Theo Beisch (11/01/04)
		if (ST.isRootDevice(getST()) == true)
			return true;
		return USN.isRootDevice(getUSN());
	}

	public boolean isDiscover()
	{
		return MAN.isDiscover(getMAN());
	}

	public boolean isAlive()
	{
		return NTS.isAlive(getNTS());
	}

	public boolean isByeBye()
	{
		return NTS.isByeBye(getNTS());
	}

	public int getLeaseTime()
	{
		return SSDP.getLeaseTime(getCacheControl());
	}

	public long getElapseTime()
	{
		String elapseTime = getValue(HTTP.ELAPSETIME);
		try
		{
			return Long.parseLong(elapseTime);
		} catch (Exception e)
		{
			return -1;
		}
	}

	public String getLinkedIp()
	{
		return getValue(HTTP.LINKEDIP);
	}

	public String getSN()
	{
		return getValue(HTTP.TVGUOSN);
	}

	public String getPCBA() {
		return getValue(HTTP.TVGUOPCBA);
	}
	////////////////////////////////////////////////
	//	toString
	////////////////////////////////////////////////

	public String toString()
	{
		return new String(dgmPacket.getData());
	}
}
