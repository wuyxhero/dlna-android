/******************************************************************
 *
 *	CyberHTTP for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: HTTP.java
 *
 *	Revision:
 *
 *	11/18/02
 *		- first revision.
 *	08/30/03
 *		- Giordano Sassaroli <sassarol@cefriel.it>
 *		- Problem : the method getPort should return the default http port 80 when a port is not specified
 *		- Description : the method is used in ControlRequest.setRequestHost() and in SubscriptionRequest.setService(). maybe the default port check could be done in these methods.
 *	09/03/02
 *		- Added getRequestHostURL().
 *	03/11/04
 *		- Added the following methods to send big content stream.
 *		  post(HTTPResponse, byte[])
 *		  post(HTTPResponse, InputStream)
 *	05/26/04
 *		- Added NO_CATCH and MAX_AGE.
 *	10/20/04 
 *		- Brent Hills <bhills@openshores.com>
 *		- Added Range and MYNAME;
 *	
 ******************************************************************/

package org.cybergarage.http;

import java.net.URL;

import android.text.TextUtils;

public class HTTP
{
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////

	/*
	 * 扩展的http报头 
	 */
	public static final String REPLY = "REPLY";//是否回复

	public static final String MAXDELAYTIME = "MAXDELAYTIME";//最大的容忍延时时间
	public static final String DMCTIME = "DMCTIME";//DMC的当前时间
	public static final String DMRTIME = "DMRTIME";//DMR的当前时间
	public static final String DIFFTIME = "DIFFTIME";//DMC-DMR的时间差值
	public static final String HEAD_SIZE = "HEAD-SIZE";//HTTP header的大小，字节为单位

	public static final String HOST = "HOST";

	public static final String VERSION = "1.1";
	public static final String VERSION_10 = "1.0";
	public static final String VERSION_11 = "1.1";

	public static final String CRLF = "\r\n";
	public static final byte CR = '\r';
	public static final byte LF = '\n';
	public static final String TAB = "\t";

	public static final String SOAP_ACTION = "SOAPACTION";

	public static final String M_SEARCH = "M-SEARCH";
	public static final String NOTIFY = "NOTIFY";
	public static final String POST = "POST";
	public static final String GET = "GET";
	public static final String HEAD = "HEAD";
	public static final String SUBSCRIBE = "SUBSCRIBE";
	public static final String UNSUBSCRIBE = "UNSUBSCRIBE";

	public static final String DATE = "Date";
	public static final String CACHE_CONTROL = "Cache-Control";
	public static final String NO_CACHE = "no-cache";
	public static final String MAX_AGE = "max-age";
	public static final String CONNECTION = "Connection";
	public static final String CLOSE = "close";
	public static final String KEEP_ALIVE = "Keep-Alive";
	public static final String IQIYICONNECTION = "IQIYIConnection";
	public static final String IQIYIPORT = "IQIYIPORT";//爱奇艺的端口
	public static final String IQIYIUDPPORT = "IQIYIUDPPORT";//udp端口
	public static final String IQIYIDEVICE = "IQIYIDEVICE"; //设备类型
	public static final String IQIYIVERSION = "IQIYIVERSION"; //协议版本
	public static final String DEVICEVERSION = "DEVICEVERSION"; //硬件版本
	public static final String TVGUOFEATUREBITMAP = "TVGUOFEATUREBITMAP"; //电视果支持功能列表
	public static final String TVGUOMARKETCHANNEL = "TVGUOMARKETCHANNEL";

	public static final String CONTENT_TYPE = "Content-Type";
	public static final String CHARSET = "charset";
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String CONTENT_RANGE = "Content-Range";
	public static final String CONTENT_RANGE_BYTES = "bytes";
	// Thanks for Brent Hills (10/20/04)
	public static final String RANGE = "Range";
	public static final String TRANSFER_ENCODING = "Transfer-Encoding";
	public static final String CHUNKED = "Chunked";
	public static final String LOCATION = "Location";
	public static final String SERVER = "Server";

	public static final String ST = "ST";
	public static final String MX = "MX";
	public static final String MAN = "MAN";
	public static final String NT = "NT";
	public static final String NTS = "NTS";
	public static final String USN = "USN";
	public static final String EXT = "EXT";
	public static final String SID = "SID";
	public static final String SEQ = "SEQ";
	public final static String CALLBACK = "CALLBACK";
	public final static String TIMEOUT = "TIMEOUT";
	// Thanks for Brent Hills (10/20/04)
	public final static String MYNAME = "MYNAME";

	//描述文件的md5的值
	public final static String FILEMD5 = "FILEMD5";

	public final static String GID = "GID";

	//notify添加字段
	public final static String LINKEDIP = "LINKEDIP";
	public final static String ELAPSETIME = "ELAPSETIME";
	public final static String TVGUOSN = "TVGUOSN";

	// 设备发现添加字段
	public final static String TVGUOPCBA = "TVGUOPCBA";

	public static final String REQEST_LINE_DELIM = " ";
	public static final String HEADER_LINE_DELIM = " :";
	public static final String STATUS_LINE_DELIM = " ";

	public static final int DEFAULT_PORT = 80;
	public static final int DEFAULT_CHUNK_SIZE = 512 * 1024;
	public static final int DEFAULT_TIMEOUT = 30;

	//socke 缓冲区大小
	public static final int SOCKET_REC_BUFFER_SIZE = 5 * 1024;
	public static final int SOCKET_SEND_BUFFER_SIZE = 5 * 1024;

	////////////////////////////////////////////////
	// URL
	////////////////////////////////////////////////

	public static final boolean isAbsoluteURL(String urlStr)
	{
		try
		{
			new URL(urlStr);
			return true;
		} catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * 原来的实现通过new URL()来实现，整个函数耗时3~4ms，现在直接从string中split得到，耗时降到0.7ms。
	 * 
	 * @param urlStr
	 * @return
	 */
	public static final String getHost(String urlStr)
	{
		if (TextUtils.isEmpty(urlStr))
		{
			return "";
		}

		String[] strs = urlStr.split("/");
		if (strs.length < 3)
		{
			return "";
		}
		String[] host = strs[2].split(":");
		if (host.length < 2)
		{
			return "";
		}
		return host[0];

		/*try {
			URL url = new URL(urlStr);
			return url.getHost();
		}
		catch (Exception e) {
			return "";
		}*/
	}

	/**
	 * 原来的实现通过new URL()来实现，整个函数耗时3~4ms，现在直接从string中split得到，耗时降到0.7ms。
	 * 
	 * @param urlStr
	 * @return
	 */
	public static final int getPort(String urlStr)
	{
		int port = DEFAULT_PORT;
		if (TextUtils.isEmpty(urlStr))
		{
			return port;
		}

		String[] strs = urlStr.split("/");
		if (strs.length < 3)
		{
			return port;
		}
		String[] host = strs[2].split(":");

		if (host.length < 2)
		{
			return port;
		}
		port = Integer.parseInt(host[1]);
		return port;

		/*try {
			URL url = new URL(urlStr);
			// Thanks for Giordano Sassaroli <sassarol@cefriel.it> (08/30/03)
			int port = url.getPort();
			if (port <= 0)
				port = DEFAULT_PORT;
			return port;
		}
		catch (Exception e) {
			return DEFAULT_PORT;
		}*/
	}

	public static final String getRequestHostURL(String host, int port)
	{
		String reqHost = "http://" + host + ":" + port;
		return reqHost;
	}

	public static final String toRelativeURL(String urlStr, boolean withParam)
	{
		String uri = urlStr;
		if (isAbsoluteURL(urlStr) == false)
		{
			if (0 < urlStr.length() && urlStr.charAt(0) != '/')
				uri = "/" + urlStr;
		} else
		{
			try
			{
				URL url = new URL(urlStr);
				uri = url.getPath();
				if (withParam == true)
				{
					String queryStr = url.getQuery();
					if (!queryStr.equals(""))
					{
						uri += "?" + queryStr;
					}
				}
				if (uri.endsWith("/"))
					uri = uri.substring(0, uri.length() - 1);
			} catch (Exception e)
			{
			}
		}
		return uri;
	}

	public static final String toRelativeURL(String urlStr)
	{
		return toRelativeURL(urlStr, true);
	}

	public static final String getAbsoluteURL(String baseURLStr, String relURlStr)
	{
		try
		{
			URL baseURL = new URL(baseURLStr);
			String url = baseURL.getProtocol() + "://" + baseURL.getHost() + ":" + baseURL.getPort() + toRelativeURL(relURlStr);
			return url;
		} catch (Exception e)
		{
			return "";
		}
	}

	////////////////////////////////////////////////
	// Chunk Size
	////////////////////////////////////////////////

	private static int chunkSize = DEFAULT_CHUNK_SIZE;

	public static final void setChunkSize(int size)
	{
		chunkSize = size;
	}

	public static final int getChunkSize()
	{
		return chunkSize;
	}

}
