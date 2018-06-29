/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: SSDPResponse.java
 *
 *	Revision;
 *
 *	01/14/03
 *		- first revision.
 *	01/23/04
 *		- Oliver Newell
 *		- Overided HTTPResponse::getHeader() for Intel UPnP control points.
 *	03/16/04
 *		- Thanks for Darrell Young
 *		- Fixed to set v1.1 to the HTTP version.
 *	10/20/04 
 *		- Brent Hills <bhills@openshores.com>
 *		- Added setMYNAME() and getMYNAME().
 *	
 ******************************************************************/

package org.cybergarage.upnp.ssdp;

import java.io.InputStream;

import org.cybergarage.http.*;

public class SSDPResponse extends HTTPResponse
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public SSDPResponse()
	{
		setVersion(HTTP.VERSION_11);
	}

	public SSDPResponse(InputStream in)
	{
		super(in);
	}

	////////////////////////////////////////////////
	//	ST (SearchTarget)
	////////////////////////////////////////////////

	public void setST(String value)
	{
		setHeader(HTTP.ST, value);
	}

	public String getST()
	{
		return getHeaderValue(HTTP.ST);
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
	//	MYNAME
	////////////////////////////////////////////////

	public void setMYNAME(String value)
	{
		setHeader(HTTP.MYNAME, value);
	}

	public String getMYNAME()
	{
		return getHeaderValue(HTTP.MYNAME);
	}

	public void setFileMd5(String value)
	{
		setHeader(HTTP.FILEMD5, value);
	}

	public String getFileMd5()
	{
		return getHeaderValue(HTTP.FILEMD5);
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

	public void setIQIYIVERSION(int version)
	{
		setHeader(HTTP.IQIYIVERSION, version);
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

	/*
	 * 设置奇艺的快速发送端口
	 */
	public void setIQIYIPORT(int port)
	{
		setHeader(HTTP.IQIYIPORT, port);
	}

	public void setIQIYIUDPPORT(int port)
	{
		setHeader(HTTP.IQIYIUDPPORT, port);
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
	//	getHeader (Override)
	////////////////////////////////////////////////

	public String getHeader()
	{
		StringBuffer str = new StringBuffer();
		str.append("HTTP/1.1 200 OK");
		str.append(HTTP.CRLF);
		str.append(getHeaderString());
		str.append(HTTP.CRLF); // for Intel UPnP control points.

		return str.toString();
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
