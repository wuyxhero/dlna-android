/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: SSDPRequest.java
 *
 *	Revision;
 *
 *	01/14/03
 *		- first revision.
 *	03/16/04
 *		- Thanks for Darrell Young
 *		- Fixed to set v1.1 to the HTTP version.;
 *	
 ******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.io.InputStream;

import org.cybergarage.http.*;

public class SSDPRequest extends HTTPRequest
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public SSDPRequest()
	{
		setVersion(HTTP.VERSION_11);
	}

	public SSDPRequest(InputStream in)
	{
		super(in);
	}

	////////////////////////////////////////////////
	//	NT
	////////////////////////////////////////////////

	public void setNT(String value)
	{
		setHeader(HTTP.NT, value);
	}

	public String getNT()
	{
		return getHeaderValue(HTTP.NT);
	}

	////////////////////////////////////////////////
	//	NTS
	////////////////////////////////////////////////

	public void setNTS(String value)
	{
		setHeader(HTTP.NTS, value);
	}

	public String getNTS()
	{
		return getHeaderValue(HTTP.NTS);
	}

	/*
	 * 在多播中，增加Myname的信息，这个有利设备发现的过程的优化
	 */
	public void setMYNAME(String value)
	{
		setHeader(HTTP.MYNAME, value);
	}

	public String getMYNAME()
	{
		return getHeaderValue(HTTP.MYNAME);
	}

	/*
	 * 在多播消息中，增加FILEMD5，携带描述文件的md5的值
	 */
	public void setFileMd5(String value)
	{
		setHeader(HTTP.FILEMD5, value);
	}

	/*
	 * 加上是否支持长连接
	 */
	public void setConnect(boolean keepConnect)
	{
		if (keepConnect == true)
		{
			setHeader(HTTP.IQIYICONNECTION, HTTP.KEEP_ALIVE);
		} else
		{
			setHeader(HTTP.IQIYICONNECTION, HTTP.CLOSE);
		}
	}

	/*
	 * 设置奇艺的快速发送端口
	 */
	public void setIQIYIPORT(int port)
	{
		setHeader(HTTP.IQIYIPORT, port);
	}

	public void setIQIYIVERSION(int version)
	{
		setHeader(HTTP.IQIYIVERSION, version);
	}

	public void setIQIYIUDPPORT(int port)
	{
		setHeader(HTTP.IQIYIUDPPORT, port);
	}

	public void setIQIYIDEVICE(int deviceName)
	{
		setHeader(HTTP.IQIYIDEVICE, deviceName);
	}

	public void setDEVICEVERSION(int version)
	{
		setHeader(HTTP.DEVICEVERSION, version);
	}

	public void setTVGUOFEATUREBITMAP(int bitmap)
	{
		setHeader(HTTP.TVGUOFEATUREBITMAP, bitmap);
	}

	public void setTVGUOMARKETCHANNEL(long channel)
	{
		setHeader(HTTP.TVGUOMARKETCHANNEL, channel);
	}

	public String getFileMd5()
	{
		return getHeaderValue(HTTP.FILEMD5);
	}

	////////////////////////////////////////////////
	//	Location
	////////////////////////////////////////////////

	public void setLocation(String value)
	{
		setHeader(HTTP.LOCATION, value);
	}

	public String getLocation()
	{
		return getHeaderValue(HTTP.LOCATION);
	}

	////////////////////////////////////////////////
	//	USN
	////////////////////////////////////////////////

	public void setUSN(String value)
	{
		setHeader(HTTP.USN, value);
	}

	public String getUSN()
	{
		return getHeaderValue(HTTP.USN);
	}

	////////////////////////////////////////////////
	//	CacheControl
	////////////////////////////////////////////////

	public void setLeaseTime(int len)
	{
		setHeader(HTTP.CACHE_CONTROL, "max-age=" + Integer.toString(len));
	}

	public int getLeaseTime()
	{
		String cacheCtrl = getHeaderValue(HTTP.CACHE_CONTROL);
		return SSDP.getLeaseTime(cacheCtrl);
	}

	////////////////////////////////////////////////
	//LinkedIp and ElapseTime
	///////////////////////////////////////////////

	public void setLinkedIP(String LinkedIp)
	{
		setHeader(HTTP.LINKEDIP, LinkedIp);
	}

	public void setElapseTime(long time)
	{
		setHeader(HTTP.ELAPSETIME, time);
	}

	public void setTvguoSN(String sn)
	{
		setHeader(HTTP.TVGUOSN, sn);
	}

	public void setTvGuoPCBA(String pcba) {
		setHeader(HTTP.TVGUOPCBA, pcba);
	}
}
