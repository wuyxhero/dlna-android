/******************************************************************
 *
 *	CyberHTTP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File: HTTPServerThread.java
 *
 *	Revision;
 *
 *	10/10/03
 *		- first revision.
 *	
 ******************************************************************/

package org.cybergarage.http;

import java.net.Socket;

import org.cybergarage.util.Debug;
import org.cybergarage.util.Mutex;

public class HTTPServerThread extends Thread
{
	private HTTPServer httpServer;
	private Socket sock;

	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public HTTPServerThread(HTTPServer httpServer, Socket sock)
	{
		super("iqiyi.HTTPServerThread");
		this.httpServer = httpServer;
		this.sock = sock;
	}

	private Mutex mutex = new Mutex();//应该要有一个互斥读取的操作

	public void lock()
	{
		mutex.lock();
	}

	public void unlock()
	{
		mutex.unlock();
	}

	public void run()
	{
		if (sock == null)
		{
			Debug.message("[HTTPServerThread] [Error] Thread exit...[sock == null]");
			return;
		}

		HTTPSocket httpSock = new HTTPSocket(sock);
		if (httpSock.open() == false)
		{
			Debug.message("[HTTPServerThread] [Error] Thread exit...[httpSock.open() == false]");
			return;
		}

		Debug.message("[HTTPServerThread] Thread start...ClientAddr=" + sock.getRemoteSocketAddress());

		HTTPRequest httpReq = new HTTPRequest();
		httpReq.setSocket(httpSock);
		while (httpServer.getHttpServerThread() != null)
		{
			if (httpReq.read() == false)
			{
				Debug.message("[HTTPServerThread] Exit thread [httpReq.read() == false]...ClientAddr="
						+ sock.getRemoteSocketAddress());
				break;
			}

			httpServer.performRequestListener(httpReq);

			if (httpReq.isKeepAlive() == false)//不是长连接则直接结束
			{
				Debug.message("[HTTPServerThread] Exit thread [httpReq.isKeepAlive() == false]...ClientAddr="
						+ sock.getRemoteSocketAddress());
				break;
			}
		}

		Debug.message("[HTTPServerThread] Thread exit...ClientAddr=" + sock.getRemoteSocketAddress());
		httpSock.close();
		httpSock = null;
	}
}
