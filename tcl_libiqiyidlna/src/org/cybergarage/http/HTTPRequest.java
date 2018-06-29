/******************************************************************
 *
 *	CyberHTTP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2004
 *
 *	File: HTTPRequest.java
 *
 *	Revision;
 *
 *	11/18/02
 *		- first revision.
 *	05/23/03
 *		- Giordano Sassaroli <sassarol@cefriel.it>
 *		- Add a relative URL check to setURI().
 *	09/02/03
 *		- Giordano Sassaroli <sassarol@cefriel.it>
 *		- Problem : Devices whose description use absolute urls receive wrong http requests
 *		- Error : the presence of a base url is not mandatory, the API code makes the assumption that control and event subscription urls are relative
 *		- Description: The method setURI should be changed as follows
 *	02/01/04
 *		- Added URI parameter methods.
 *	03/16/04
 *		- Removed setVersion() because the method is added to the super class.
 *		- Changed getVersion() to return the version when the first line string has the length.
 *	05/19/04
 *		- Changed post(HTTPResponse *) to close the socket stream from the server.
 *	08/19/04
 *		- Fixed getFirstLineString() and getHTTPVersion() no to return "HTTP/HTTP/version".
 *	08/25/04
 *		- Added isHeadRequest().
 *	08/26/04
 *		- Changed post(HTTPResponse) not to close the connection.
 *		- Changed post(String, int) to add a connection header to close.
 *	08/27/04
 *		- Changed post(String, int) to support the persistent connection.
 *	08/28/04
 *		- Added isKeepAlive().
 *	10/26/04
 *		- Brent Hills <bhills@openshores.com>
 *		- Added a fix to post() when the last position of Content-Range header is 0.
 *		- Added a Content-Range header to the response in post().
 *		- Changed the status code for the Content-Range request in post().
 *		- Added to check the range of Content-Range request in post().
 *	03/02/05
 *		- Changed post() to suppot chunked stream.
 *	06/10/05
 *		- Changed post() to add a HOST headedr before the posting.
 *	07/07/05
 *		- Lee Peik Feng <pflee@users.sourceforge.net>
 *		- Fixed post() to output the chunk size as a hex string.
 *
 ******************************************************************/

package org.cybergarage.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import org.cybergarage.util.Debug;

/**
 * 
 * This class rappresnet an HTTP <b>request</b>, and act as HTTP client when it sends the request<br>
 * 
 * @author Satoshi "skonno" Konno
 * @author Stefano "Kismet" Lenzi
 * @version 1.8
 * 
 */
public class HTTPRequest extends HTTPPacket
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public HTTPRequest()
	{
		setVersion(HTTP.VERSION_11);
	}

	public HTTPRequest(InputStream in)
	{
		super(in);
	}

	public HTTPRequest(HTTPSocket httpSock)
	{
		this(httpSock.getInputStream());
		setSocket(httpSock);
	}

	////////////////////////////////////////////////
	//	Method
	////////////////////////////////////////////////

	private String method = null;

	public void setMethod(String value)
	{
		method = value;
	}

	public String getMethod()
	{
		if (method != null)
			return method;
		return getFirstLineToken(0);
	}

	public boolean isMethod(String method)
	{
		String headerMethod = getMethod();
		if (headerMethod == null)
			return false;
		return headerMethod.equalsIgnoreCase(method);
	}

	public boolean isGetRequest()
	{
		return isMethod(HTTP.GET);
	}

	public boolean isPostRequest()
	{
		return isMethod(HTTP.POST);
	}

	public boolean isHeadRequest()
	{
		return isMethod(HTTP.HEAD);
	}

	public boolean isSubscribeRequest()
	{
		return isMethod(HTTP.SUBSCRIBE);
	}

	public boolean isUnsubscribeRequest()
	{
		return isMethod(HTTP.UNSUBSCRIBE);
	}

	public boolean isNotifyRequest()
	{
		return isMethod(HTTP.NOTIFY);
	}

	/*
	 * 判断是否为快速发送通道，如果是的话，则内容只有一个字节
	 */
	public boolean isQuicklyRequest()
	{
		if (getContent().length == 1)
			return true;
		return false;
	}

	private String tempContent = "";

	public void setTempContent(String tempContent)
	{
		this.tempContent = tempContent;
	}

	public String getTempContent()
	{
		return tempContent;
	}

	////////////////////////////////////////////////
	//	URI
	////////////////////////////////////////////////

	private String uri = null;

	public void setURI(String value, boolean isCheckRelativeURL)
	{
		uri = value;
		if (isCheckRelativeURL == false)
			return;
		// Thanks for Giordano Sassaroli <sassarol@cefriel.it> (09/02/03)
		uri = HTTP.toRelativeURL(uri);
	}

	public void setURI(String value)
	{
		setURI(value, false);
	}

	public String getURI()
	{
		if (uri != null)
			return uri;
		return getFirstLineToken(1);
	}

	////////////////////////////////////////////////
	//	URI Parameter
	////////////////////////////////////////////////

	public ParameterList getParameterList()
	{
		ParameterList paramList = new ParameterList();
		String uri = getURI();
		if (uri == null)
			return paramList;
		int paramIdx = uri.indexOf('?');
		if (paramIdx < 0)
			return paramList;
		while (0 < paramIdx)
		{
			int eqIdx = uri.indexOf('=', (paramIdx + 1));
			String name = uri.substring(paramIdx + 1, eqIdx);
			int nextParamIdx = uri.indexOf('&', (eqIdx + 1));
			String value = uri.substring(eqIdx + 1, (0 < nextParamIdx) ? nextParamIdx : uri.length());
			Parameter param = new Parameter(name, value);
			paramList.add(param);
			paramIdx = nextParamIdx;
		}
		return paramList;
	}

	public String getParameterValue(String name)
	{
		ParameterList paramList = getParameterList();
		return paramList.getValue(name);
	}

	////////////////////////////////////////////////
	//	SOAPAction
	////////////////////////////////////////////////

	public boolean isSOAPAction()
	{
		return hasHeader(HTTP.SOAP_ACTION);
	}

	////////////////////////////////////////////////
	// Host / Port	
	////////////////////////////////////////////////

	private String requestHost = "";

	public void setRequestHost(String host)
	{
		requestHost = host;
	}

	public String getRequestHost()
	{
		return requestHost;
	}

	private int requestPort = -1;

	public void setRequestPort(int host)
	{
		requestPort = host;
	}

	public int getRequestPort()
	{
		return requestPort;
	}

	////////////////////////////////////////////////
	//	Socket
	////////////////////////////////////////////////

	private HTTPSocket httpSocket = null;

	public void setSocket(HTTPSocket value)
	{
		httpSocket = value;
	}

	public HTTPSocket getSocket()
	{
		return httpSocket;
	}

	/////////////////////////// /////////////////////
	//	local address/port
	////////////////////////////////////////////////

	public String getLocalAddress()
	{
		return getSocket().getLocalAddress();
	}

	public int getLocalPort()
	{
		return getSocket().getLocalPort();
	}

	////////////////////////////////////////////////
	//	parseRequest
	////////////////////////////////////////////////

	public boolean parseRequestLine(String lineStr)
	{
		StringTokenizer st = new StringTokenizer(lineStr, HTTP.REQEST_LINE_DELIM);
		if (st.hasMoreTokens() == false)
			return false;
		setMethod(st.nextToken());
		if (st.hasMoreTokens() == false)
			return false;
		setURI(st.nextToken());
		if (st.hasMoreTokens() == false)
			return false;
		setVersion(st.nextToken());
		return true;
	}

	////////////////////////////////////////////////
	//	First Line
	////////////////////////////////////////////////

	public String getHTTPVersion()
	{
		if (hasFirstLine() == true)
			return getFirstLineToken(2);
		return "HTTP/" + super.getVersion();
	}

	public String getFirstLineString()
	{
		return getMethod() + " " + getURI() + " " + getHTTPVersion() + HTTP.CRLF;
	}

	////////////////////////////////////////////////
	//	getHeader
	////////////////////////////////////////////////

	public String getHeader()
	{
		StringBuffer str = new StringBuffer();

		str.append(getFirstLineString());

		String headerString = getHeaderString();
		str.append(headerString);
		return str.toString();
	}

	////////////////////////////////////////////////
	//	isKeepAlive
	////////////////////////////////////////////////

	public boolean isKeepAlive()
	{
		if (isCloseConnection() == true)
			return false;
		if (isKeepAliveConnection() == true)
			return true;
		String httpVer = getHTTPVersion();
		boolean isHTTP10 = (0 < httpVer.indexOf("1.0")) ? true : false;
		if (isHTTP10 == true)
			return false;
		return true;
	}

	////////////////////////////////////////////////
	//	read
	////////////////////////////////////////////////

	public boolean read()
	{
		return super.read(getSocket());
	}

	////////////////////////////////////////////////
	//	POST (Response)
	////////////////////////////////////////////////

	public boolean post(HTTPResponse httpRes)
	{
		HTTPSocket httpSock = getSocket();
		long offset = 0;
		long length = httpRes.getContentLength();
		if (hasContentRange() == true)
		{
			long firstPos = getContentRangeFirstPosition();
			long lastPos = getContentRangeLastPosition();

			// Thanks for Brent Hills (10/26/04)
			if (lastPos <= 0)
				lastPos = length - 1;
			if ((firstPos > length) || (lastPos > length))
				return returnResponse(HTTPStatus.INVALID_RANGE);
			httpRes.setContentRange(firstPos, lastPos, length);
			httpRes.setStatusCode(HTTPStatus.PARTIAL_CONTENT);

			offset = firstPos;
			length = lastPos - firstPos + 1;
		}
		return httpSock.post(httpRes, offset, length, isHeadRequest());
		//httpSock.close();
	}

	////////////////////////////////////////////////
	//	POST (Request)
	////////////////////////////////////////////////

	private Socket postSocket = null;//这样才能利用长连接

	/*
	 * socket的连接函数
	 */
	public void connectHost(String host, int port, boolean isKeepAlive) throws IOException
	{
		/*
		 * 如果是短连接，则每次都要重新建立
		 */
		if (isKeepAlive == false)//短连接方案，每次都要重新连接，connect的方案，效率不高，有时候会很耗损时间
		{
			postSocket = new Socket();
			if (postSocket != null)
			{
				postSocket.setTcpNoDelay(true);
				postSocket.setTrafficClass(0x10);
				postSocket.setPerformancePreferences(2, 3, 1);
				postSocket.setSoTimeout(10000);
				postSocket.connect(new InetSocketAddress(host, port), 5000);//设置连接超时时间
			}
		} else
		{
			if (postSocket == null)
			{
				postSocket = new Socket();
				if (postSocket != null)
				{
					postSocket.setTcpNoDelay(true);
					postSocket.setKeepAlive(true);
					postSocket.setOOBInline(true);//设置要读取这个字节
					postSocket.setTrafficClass(0x10);//低延时
					postSocket.setPerformancePreferences(2, 3, 1);
					postSocket.setSoTimeout(10000);
					postSocket.connect(new InetSocketAddress(host, port), 5000);//设置连接超时时间
				}
			}
		}
	}

	public void closeHostSocket()
	{
		try
		{
			if (postSocket != null)
			{
				Debug.message("clearHostSocket");
				postSocket.close();
				postSocket = null;
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/*
	 * 革命性的思路，采用双通道算法
	 */
	private Socket tcpSocketQuicklyA = null;//双通道算法

	public void connectHostQuickly(String host, int port) throws IOException
	{
		Debug.message("online ConnectHostQuickly()");
		tcpSocketQuicklyA = connectHostTcpQuickly(tcpSocketQuicklyA, host, port);
	}

	public void reconnectHostQuickly(String host, int port, String data) throws IOException
	{
		if (tcpSocketQuicklyA == null || !tcpSocketQuicklyA.isConnected())
		{
			tcpSocketQuicklyA = connectHostTcpQuickly(tcpSocketQuicklyA, host, port);
		} else
		{
			Debug.message("online reconnectHostQuickly() sendUrgentData");
			//tcpSocketQuicklyA.sendUrgentData(0xFF);
			//To send keep alive message
			if (data == null)
				return;
			quicklyTCPPost(host, port, data);
		}
	}

	public void closeHostQuickly()
	{
		Debug.message("online closeHostQuickly() ");
		try
		{
			if (tcpSocketQuicklyA != null)
			{
				tcpSocketQuicklyA.close();
				tcpSocketQuicklyA = null;
			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (udpDsA != null)
		{
			udpDsA.close();
			udpDsA = null;
		}
		if (udpDsB != null)
		{
			udpDsB.close();
			udpDsB = null;
		}
	}

	private Socket connectHostTcpQuickly(Socket socketOne, String host, int port) throws IOException
	{
		if (socketOne == null)
		{
			socketOne = new Socket();
			if (socketOne != null)
			{
				socketOne.setTcpNoDelay(true);
				socketOne.setKeepAlive(true);
				socketOne.setOOBInline(true);//设置要读取这个字节
				socketOne.setTrafficClass(0x10);//低延时
				socketOne.setPerformancePreferences(2, 3, 1);

				socketOne.connect(new InetSocketAddress(host, port), 5000);//设置连接超时时间
			}
		}
		return socketOne;
	}

	/*
	 * 这个方法是变革，竞速的发送机制，TCP
	 */
	public synchronized boolean quicklyTCPPost(final String host, final int port, final String data)
	{
		tcpSocketQuicklyA = postQuickly(tcpSocketQuicklyA, host, port, data);
		if (tcpSocketQuicklyA == null)
		{
			Debug.message("quicklyTCPPost failed");
			return false;
		}
		return true;

	}

	/*
	 * 支持当字节发送的，紧急通道.TCP
	 */
	public synchronized boolean quicklyPost(final String host, final int port, final byte data)
	{
		try
		{
			tcpSocketQuicklyA = connectHostTcpQuickly(tcpSocketQuicklyA, host, port);
			try
			{
				if (tcpSocketQuicklyA != null)//在消息发送之前，进行服务端检测
				{
					tcpSocketQuicklyA.sendUrgentData(data);
				}
				return true;
			} catch (Exception ex)
			{
				if (tcpSocketQuicklyA != null)
				{
					try
					{
						tcpSocketQuicklyA.close();
					} catch (Exception e1)
					{
					}
					tcpSocketQuicklyA = null;
				}
				tcpSocketQuicklyA = connectHostTcpQuickly(tcpSocketQuicklyA, host, port);
			}
		} catch (IOException e)
		{
			if (tcpSocketQuicklyA != null)
			{
				try
				{
					tcpSocketQuicklyA.close();
				} catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				tcpSocketQuicklyA = null;
			}
			return false;
		}
		return true;
	}

	/*
	 * 通过UDP的方式来进行发送
	 */
	private DatagramSocket udpDsA = null;
	private DatagramSocket udpDsB = null;

	private DatagramSocket quicklyUDPHost(DatagramSocket ds)
	{
		if (ds == null)
		{
			try
			{
				ds = new DatagramSocket();
				ds.setTrafficClass(0x10 | 0x04);//高可靠性与低延时
			} catch (SocketException e)
			{
				// TODO Auto-generated catch block
				ds.close();
				ds = null;
			}
		}
		return ds;
	}

	/*
	 * 双通道竞速算法,UDP策略
	 */
	public synchronized boolean quicklyUDPPost(final String host, final int port, final String data)
	{
		boolean retA = false;
		boolean retB = false;
		retA = quicklyUDPPost(udpDsA, host, port, data);//UDP通道A
		if (retA == false)
		{
			retB = quicklyUDPPost(udpDsB, host, port, data);//UDP通道B
			Debug.message("quicklyUDPPost UDP failed, resend ret: " + retB);
			return retB;
		}

		return retA;
	}

	/*
	 * 采用UDP的快速发送通道，为了减少丢包率，这里可以采用，将1个字节，扩展到100个字节，即通过同时发送100个字节，不断地重复，即使丢包也不会有影响
	 */
	public synchronized boolean quicklyUDPPost(DatagramSocket ds, String host, int port, String data)
	{
		ds = quicklyUDPHost(ds);
		if (ds != null)
		{
			try
			{
				DatagramPacket dp = new DatagramPacket(data.getBytes(), data.length(), InetAddress.getByName(host), port);
				ds.send(dp);
				return true;
			} catch (UnknownHostException e)
			{
				e.printStackTrace();
				if (ds != null)
				{
					ds.close();
					ds = null;
				}
				return false;
			} catch (IOException e)
			{
				e.printStackTrace();
				if (ds != null)
				{
					ds.close();
					ds = null;
				}
				return false;
			}
		}
		return false;
	}

	public Socket postQuickly(Socket socket, String host, int port, String data)
	{
		try
		{
			OutputStream out = null;
			socket = connectHostTcpQuickly(socket, host, port);
			try
			{
				if (socket != null)//在消息发送之前，进行服务端检测
				{
					// socket.sendUrgentData(0xFF);
					out = socket.getOutputStream();
					PrintWriter pout = new PrintWriter(out);//自动发送
					pout.print(data);
					pout.flush();
				}
			} catch (Exception ex)
			{
				if (socket != null)
				{
					try
					{
						socket.close();
					} catch (Exception e1)
					{
					}
					socket = null;
				}
				Debug.message("Exception To reconnect and send data : " + ex);
				//Don't call postQuickly here to prevent recursion. If there is no connection, recursion could cause StackOverflowError
				socket = connectHostTcpQuickly(socket, host, port);
			}
		} catch (IOException e)
		{
			if (socket != null)
			{
				try
				{
					socket.close();
				} catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				socket = null;
			}
		}
		return socket;
	}

	/*
	 * 增加一个函数，仅仅是发送，不等待返回值
	 */
	public synchronized boolean justPost(String host, int port, boolean isKeepAlive)
	{
		setHost(host);
		setConnection((isKeepAlive == true) ? HTTP.KEEP_ALIVE : HTTP.CLOSE);

		OutputStream out = null;
		try
		{
			/*
			 * 采用了类似长连接的方案
			 */
			connectHost(host, port, isKeepAlive);

			//需要先判定，服务端是否挂掉了，如果挂掉了就需要重新连接，这样就可以避免这个问题，太好了
			// if(isKeepAlive==true)
			// {
			// try
			// {
			// if(postSocket!=null)//在消息发送之前，进行服务端检测
			// {
			// postSocket.sendUrgentData(0xFF);//发送一个字节，这个字节服务端不处理
			// }
			// }catch(Exception ex)//服务已经关闭了socket
			// {
			// Debug.message("---------socket reconnect----------------");
			// if(postSocket!=null)
			// {
			// try
			// {
			// postSocket.close();
			// } catch (Exception e1) {};
			// postSocket = null;
			// }
			// connectHost(host,port,isKeepAlive);//进行重连处理
			// }
			// }

			out = postSocket.getOutputStream();

			PrintWriter pout = new PrintWriter(out);//自动发送
			pout.print(getHeader());
			pout.print(HTTP.CRLF);

			boolean isChunkedRequest = isChunked();

			String content = getContentString();
			int contentLength = 0;
			if (content != null)
			{
				contentLength = content.length();
			}

			if (0 < contentLength)
			{
				if (isChunkedRequest == true)
				{
					// Thanks for Lee Peik Feng <pflee@users.sourceforge.net> (07/07/05)
					String chunSizeBuf = Long.toHexString(contentLength);
					pout.print(chunSizeBuf);
					pout.print(HTTP.CRLF);
				}
				pout.print(content);
				if (isChunkedRequest == true)
					pout.print(HTTP.CRLF);
			}

			if (isChunkedRequest == true)
			{
				pout.print("0");
				pout.print(HTTP.CRLF);
			}

			pout.flush();

		} catch (SocketException e)
		{
			if (isKeepAlive == true)
			{
				if (postSocket != null)
				{
					try
					{
						postSocket.close();
					} catch (Exception e1)
					{
					}
					postSocket = null;
				}
				Debug.warning(e);
			}
			return false;
		} catch (IOException e)
		{
			if (isKeepAlive == true)
			{
				if (postSocket != null)
				{
					try
					{
						postSocket.close();
					} catch (Exception e1)
					{
					}
					postSocket = null;
				}
				Debug.warning(e);
			}
			return false;
		} finally
		{
			if (isKeepAlive == false)
			{
				if (out != null)
				{
					try
					{
						out.close();
					} catch (Exception e)
					{
					}
					;
				}
				if (postSocket != null)
				{
					try
					{
						postSocket.close();
					} catch (Exception e1)
					{
					}
					postSocket = null;
				}
			}

		}

		return true;
	}

	public HTTPResponse post(String host, int port, boolean isKeepAlive)
	{

		HTTPResponse httpRes = new HTTPResponse();
		setHost(host);
		setConnection((isKeepAlive == true) ? HTTP.KEEP_ALIVE : HTTP.CLOSE);
		setHeader(HTTP.CACHE_CONTROL, HTTP.NO_CACHE);

		boolean isHeaderRequest = isHeadRequest();

		OutputStream out = null;
		InputStream in = null;

		try
		{
			connectHost(host, port, isKeepAlive);
			out = postSocket.getOutputStream();
			PrintWriter pout = new PrintWriter(out);
			pout.print(getHeader());
			pout.print(HTTP.CRLF);

			boolean isChunkedRequest = isChunked();

			String content = getContentString();

			int contentLength = 0;
			if (content != null)
				contentLength = content.length();

			if (0 < contentLength)
			{
				if (isChunkedRequest == true)
				{
					// Thanks for Lee Peik Feng <pflee@users.sourceforge.net> (07/07/05)
					String chunSizeBuf = Long.toHexString(contentLength);
					pout.print(chunSizeBuf);
					pout.print(HTTP.CRLF);
				}
				pout.print(content);
				if (isChunkedRequest == true)
					pout.print(HTTP.CRLF);
			}

			if (isChunkedRequest == true)
			{
				pout.print("0");
				pout.print(HTTP.CRLF);
			}
			pout.flush();

			in = postSocket.getInputStream();
			httpRes.set(in, isHeaderRequest);
			int statuscode = httpRes.getStatusCode();
			if (statuscode == 0)
			{
				Debug.message("DMR server connection has been closed...");
				if (isKeepAlive == true)
				{
					if (postSocket != null)
					{
						if (postSocket.isClosed() == false)
						{
							try
							{
								postSocket.close();
							} catch (Exception e1)
							{
								Debug.warning(e1);
							}
							postSocket = null;
						}
					}
				}
			} else if (httpRes.getConnection().equals("close"))
			{
				Debug.message("DMR server asks me to close connection...");
				if (isKeepAlive == true)
				{
					if (postSocket != null)
					{
						if (postSocket.isClosed() == false)
						{
							try
							{
								postSocket.close();
							} catch (Exception e1)
							{
								Debug.warning(e1);
							}
							postSocket = null;
						}
					}
				}
			}
		} catch (SocketException e)
		{
			httpRes.setStatusCode(HTTPStatus.NOT_FOUND);
			Debug.message("=EXCEPTION: " + e.toString());
			if (isKeepAlive == true)
			{
				if (postSocket != null)
				{
					if (postSocket.isClosed() == false)
					{
						try
						{
							postSocket.close();
						} catch (Exception e1)
						{
						}
						postSocket = null;
					}
				}
				Debug.warning(e);
			}

		} catch (IOException e)
		{
			//Socket create but without connection
			httpRes.setStatusCode(HTTPStatus.INTERNAL_SERVER_ERROR);
			Debug.message("==EXCEPTION: " + e.toString());
			if (isKeepAlive == true)
			{
				if (postSocket != null)
				{
					if (postSocket.isClosed() == false)
					{
						try
						{
							postSocket.close();
						} catch (Exception e1)
						{
						}
						postSocket = null;
					}
				}
				Debug.warning(e);
			}
		} finally
		{
			if (isKeepAlive == false)
			{
				if (in != null)
				{
					try
					{
						in.close();
					} catch (Exception e)
					{
					}
				}
				if (out != null)
				{
					try
					{
						out.close();
					} catch (Exception e)
					{
					}
				}
				if (postSocket != null)
				{
					if (postSocket.isClosed() == false)
					{
						try
						{
							postSocket.close();
						} catch (Exception e1)
						{
						}
						postSocket = null;
					}
				}
			}
		}

		return httpRes;
	}

	/*
	 * 加入post是否需要回复de
	 */
	public HTTPResponse post(String host, int port)
	{
		return post(host, port, false);
	}

	////////////////////////////////////////////////
	//	set
	////////////////////////////////////////////////

	public void set(HTTPRequest httpReq)
	{
		set((HTTPPacket) httpReq);
		setSocket(httpReq.getSocket());
	}

	////////////////////////////////////////////////
	//	OK/BAD_REQUEST
	////////////////////////////////////////////////

	public boolean returnResponse(int statusCode)
	{
		HTTPResponse httpRes = new HTTPResponse();
		httpRes.setStatusCode(statusCode);
		httpRes.setContentLength(0);
		return post(httpRes);
	}

	public boolean returnOK()
	{
		return returnResponse(HTTPStatus.OK);
	}

	public boolean returnBadRequest()
	{
		return returnResponse(HTTPStatus.BAD_REQUEST);
	}

	////////////////////////////////////////////////
	//	toString
	////////////////////////////////////////////////

	public String toString()
	{
		StringBuffer str = new StringBuffer();
		str.append(getFirstLineString());
		str.append(getHeaderString());
		str.append(HTTP.CRLF);
		str.append(getContentString());

		return str.toString();
	}

	public void print()
	{
		System.out.println("------------------------------DUMP HTTPRequest [Start]------------------------------");
		System.out.println(toString().replace(HTTP.CRLF, "	"));
		System.out.println("-------------------------------DUMP HTTPRequest [End]-------------------------------");
	}
}
