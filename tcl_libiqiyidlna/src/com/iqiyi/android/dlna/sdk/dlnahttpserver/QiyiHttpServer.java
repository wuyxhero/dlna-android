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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Vector;

import org.cybergarage.http.HTTPRequest;
import org.cybergarage.http.HTTPRequestListener;
import org.cybergarage.util.Debug;
import org.cybergarage.util.ListenerList;

import com.iqiyi.android.dlna.sdk.mediarenderer.ControlPointConnectRendererListener;

public class QiyiHttpServer implements Runnable
{

	public final static String NAME = "HTTP";
	public final static String VERSION = "1.0";

	public final static int DEFAULT_PORT = 80;
	public Vector<String> client = null;
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

	public QiyiHttpServer()
	{
		serverSock = null;
		client = new Vector<String>();
	}

	public synchronized void addClient(String ip)
	{
		if (ip == null || ip.length() < 7)
			return;
		try
		{
			for (int n = 0; n < client.size(); n++)
			{
				if (ip.equals(client.get(n)))
					return;
			}
			client.add(ip);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public synchronized void removeClient(String ip)
	{
		try
		{
			for (int n = 0; n < client.size(); n++)
			{
				if (ip.equals(client.get(n)))
					client.remove(n);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public synchronized Vector<String> getClientList()
	{
		Vector<String> clientList = new Vector<String>();
		for (int n = 0; n < client.size(); n++)
		{
			String client_ip = (String) client.get(n);
			clientList.add(client_ip);
		}
		return clientList;
	}

	////////////////////////////////////////////////
	//	ServerSocket
	////////////////////////////////////////////////

	private ServerSocket serverSock = null;
	private InetAddress bindAddr = null;
	private int bindPort = 0;
	/**
	 * Store the current TCP timeout value The variable should be accessed by getter and setter metho
	 */
	protected int timeout = DEFAULT_TIMEOUT;

	public ServerSocket getServerSock()
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
	 * @param timeout
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
			return true;
		try
		{
			serverSock = new ServerSocket(bindPort);
			serverSock.setPerformancePreferences(2, 3, 1);// 延时的优先性最高
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
			bindAddr = InetAddress.getByName(addr);
			bindPort = port;
			serverSock = new ServerSocket(bindPort);
			serverSock.setPerformancePreferences(2, 3, 1);//延时的优先性最高
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
			bindAddr = null;
			bindPort = port;
			serverSock = new ServerSocket(bindPort);
			serverSock.setPerformancePreferences(2, 3, 1);// 延时的优先性最高
		} catch (IOException e)
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

	public Socket accept()
	{
		if (serverSock == null)
			return null;
		try
		{
			Socket sock = serverSock.accept();
			sock.setOOBInline(false);//不接受紧急数据
			sock.setTrafficClass(0x10);//最小延时
			sock.setTcpNoDelay(true);
			sock.setPerformancePreferences(2, 3, 1);//延时的优先性最高
			sock.setKeepAlive(true);
			return sock;
		} catch (Exception e)
		{
			Debug.warning(e);
			return null;
		}
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

	public Thread getHttpServerThread()
	{
		return httpServerThread;
	}

	public void run()
	{
		if (isOpened() == false)
		{
			Debug.message("[QiyiHttpServer] [Error] Thread exit...[serverSock == null]");
			return;
		}

		SocketAddress addr = serverSock.getLocalSocketAddress();

		Debug.message("[QiyiHttpServer] Thread start...ServerAddr=" + addr);

		Vector<Socket> clients = new Vector<Socket>();

		Thread thisThread = Thread.currentThread();

		while (httpServerThread == thisThread)
		{
			Thread.yield();
			Socket sock;
			try
			{
				Debug.message("[QiyiHttpServer] Wait for connecting...HTTPServer=" + addr);
				sock = accept();
				if (sock != null)
				{
					Debug.message("[QiyiHttpServer] Remote client connected...ClientAddr=" + sock.getRemoteSocketAddress());
					clients.add(sock);
				} else
				{
					Debug.message("[QiyiHttpServer] [Error] Accept() failure...[sock == null]");
					break;
				}
			} catch (Exception e)
			{
				e.printStackTrace();
				sock = null;
				break;
			}

			Debug.message("[QiyiHttpServer] Create thread to handle connection...ClientAddr=" + sock.getRemoteSocketAddress());
			QiyiHttpServerThread httpServThread = new QiyiHttpServerThread(this, sock);
			httpServThread.start();
		}

		Iterator<Socket> it = clients.iterator();
		while (it.hasNext())
		{
			Socket sock = it.next();
			try
			{
				sock.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		Debug.message("[QiyiHttpServer] Thread exit...ServerAddr=" + addr);
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
		if (client != null)
			client.clear();
		httpServerThread = null;
		return true;
	}
}
