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

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.cybergarage.http.HTTPRequest;
import org.cybergarage.http.HTTPRequestListener;
import org.cybergarage.util.Debug;
import org.cybergarage.util.ListenerList;

import com.iqiyi.android.dlna.sdk.mediarenderer.ControlPointConnectRendererListener;

public class QiyiUDPHttpServer implements Runnable
{
	public final static String NAME = "HTTP";
	public final static String VERSION = "1.0";

	public final static int DEFAULT_PORT = 80;

	/**
	 * Default timeout connection for HTTP comunication
	 * 
	 * @since 1.8
	 */
	public final static int DEFAULT_TIMEOUT = 60 * 1000;

	public static String getName()
	{
		String osName = System.getProperty("os.name");
		String osVer = System.getProperty("os.version");
		return osName + "/" + osVer + " " + NAME + "/" + VERSION;
	}

	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public QiyiUDPHttpServer()
	{
		serverSock = null;

	}

	////////////////////////////////////////////////
	//	ServerSocket
	////////////////////////////////////////////////

	private DatagramSocket serverSock = null;//采用udp协议
	private InetAddress bindAddr = null;
	private int bindPort = 0;

	/**
	 * Store the current TCP timeout value The variable should be accessed by getter and setter metho
	 */
	protected int timeout = DEFAULT_TIMEOUT;

	public DatagramSocket getServerSock()
	{
		return serverSock;
	}

	public String getBindAddress()
	{
		if (bindAddr == null)
			return "";
		return bindAddr.toString();
	}

	public int getBindPort()
	{
		return bindPort;
	}

	////////////////////////////////////////////////
	//	open/close
	////////////////////////////////////////////////

	/**
	 * Get the current socket timeout
	 * 
	 * @since 1.8
	 */
	public synchronized int getTimeout()
	{
		return timeout;
	}

	/**
	 * Set the current socket timeout
	 * 
	 * @param longout
	 *            new timeout
	 * @since 1.8
	 */
	public synchronized void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	public boolean open(InetAddress addr, int port)
	{
		if (serverSock != null)
		{
			return true;
		}
		if (addr == null)
		{
			return false;
		}

		try
		{
			InetSocketAddress socketAddress = new InetSocketAddress(addr.getHostAddress(), port);
			serverSock = new DatagramSocket(socketAddress);

		} catch (IOException e)
		{
			return false;
		}
		return true;
	}

	public boolean open(String addr, int port)
	{
		if (serverSock != null)
			return true;
		try
		{
			InetSocketAddress socketAddress = new InetSocketAddress(addr, port);
			bindAddr = InetAddress.getByName(addr);
			bindPort = port;
			serverSock = new DatagramSocket(socketAddress);//启动socket
		} catch (IOException e)
		{
			return false;
		}
		return true;
	}

	public boolean open(int port)
	{
		if (serverSock != null)
			return true;
		try
		{
			InetSocketAddress socketAddress = new InetSocketAddress(port);
			bindAddr = socketAddress.getAddress();
			bindPort = port;
			serverSock = new DatagramSocket(socketAddress);//启动socket
		} catch (IOException e)
		{
			return false;
		}
		return true;
	}

	private boolean shutDown()
	{
		if (serverSock == null)
		{
			return true;
		}
		try
		{
			serverSock.close();
			serverSock = null;
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean close()
	{
		if (serverSock == null)
			return true;
		try
		{
			serverSock.close();
			serverSock = null;
			bindAddr = null;
			bindPort = 0;
		} catch (Exception e)
		{
			Debug.warning(e);
			return false;
		}
		return true;
	}

	public boolean isOpened()
	{
		return (serverSock != null) ? true : false;
	}

	////////////////////////////////////////////////
	//	httpRequest
	////////////////////////////////////////////////

	private ListenerList httpRequestListenerList = new ListenerList();

	public void addRequestListener(HTTPRequestListener listener)
	{
		httpRequestListenerList.add(listener);
	}

	public void removeRequestListener(HTTPRequestListener listener)
	{
		httpRequestListenerList.remove(listener);
	}

	public void performRequestListener(HTTPRequest httpReq)
	{
		int listenerSize = httpRequestListenerList.size();
		for (int n = 0; n < listenerSize; n++)
		{
			HTTPRequestListener listener = (HTTPRequestListener) httpRequestListenerList.get(n);
			listener.httpRequestRecieved(httpReq);
		}
	}

	////////////////////////////////////////////////
	/*
	 * controlpoint是否连接设备的回调函数
	 */
	private ListenerList controlPointListenerList = new ListenerList();

	public void addControlPointConnectRendererListener(ControlPointConnectRendererListener listener)
	{
		controlPointListenerList.add(listener);
	}

	public void removeControlPointConnectRendererListener(ControlPointConnectRendererListener listener)
	{
		controlPointListenerList.remove(listener);
	}

	public void performControlPointConnectRendererListener(boolean isConnect)
	{
		int listenerSize = controlPointListenerList.size();
		for (int n = 0; n < listenerSize; n++)
		{
			ControlPointConnectRendererListener listener = (ControlPointConnectRendererListener) controlPointListenerList
					.get(n);
			if (listener != null)
			{
				listener.onReceiveDeviceConnect(isConnect);
			}
		}
	}

	////////////////////////////////////////////////
	//	run	
	////////////////////////////////////////////////
	private Thread httpServerThread = null;

	/*
	 * 采用了UDP的读取方式
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		if (isOpened() == false)
			return;

		Thread thisThread = Thread.currentThread();

		while (httpServerThread == thisThread)
		{
			Thread.yield();
			while (true)
			{
				HTTPRequest httpReq = new HTTPRequest();
				if (httpReq.readQuickly(serverSock) == false)//快速读取
				{
					//如果server挂掉算了，这需要重建这个socket
					shutDown(); // just shut down, bindAddr and bindPort are still available!
					if (open(bindAddr, bindPort) == false)//再次打开，如果失败的话，就直接挂掉了
					{
						Debug.warning("UDP Quickly Channel Died!");
						break;
					}
				}

				if (httpReq.getIsSingleSend() == true)
				{
					continue;
				}

				//这里判断消息是否可用
				if (RacingStrategy.isMessageOk(httpReq) == true)
				{
					performRequestListener(httpReq);
				}
			}
		}
	}

	public boolean start()
	{
		StringBuffer name = new StringBuffer("iqiyi.QuicklyHTTPServer/");
		name.append(serverSock.getLocalSocketAddress());
		httpServerThread = new Thread(this, name.toString());
		httpServerThread.start();
		return true;
	}

	public boolean stop()
	{
		httpServerThread = null;
		return true;
	}
}
