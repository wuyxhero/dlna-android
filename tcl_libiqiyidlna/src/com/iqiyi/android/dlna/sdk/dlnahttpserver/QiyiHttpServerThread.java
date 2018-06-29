package com.iqiyi.android.dlna.sdk.dlnahttpserver;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import org.cybergarage.http.HTTPRequest;
import org.cybergarage.http.HTTPSocket;
import org.cybergarage.util.Debug;

public class QiyiHttpServerThread extends Thread
{

	private QiyiHttpServer httpServer;
	private Socket sock;

	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public QiyiHttpServerThread(QiyiHttpServer httpServer, Socket sock)
	{
		super("iqiyi.QuicklyHTTPServerThread");
		this.httpServer = httpServer;
		this.sock = sock;
	}

	////////////////////////////////////////////////
	//	run	
	////////////////////////////////////////////////

	public static int ConnectCount = 0;//连接的数，这个用来提供给TV，可以知道有哪些设备连接过来

	public void run()
	{
		if (sock == null)
		{
			Debug.message("[QiyiHttpServerThread] [Error] Thread exit...[sock == null]");
			return;
		}

		HTTPSocket httpSock = new HTTPSocket(sock);
		if (httpSock.open() == false)
		{
			Debug.message("[QiyiHttpServerThread] [Error] Thread exit...[httpSock.open() == false]");
			return;
		}

		Debug.message("[QiyiHttpServerThread] Thread start...ClientAddr=" + sock.getRemoteSocketAddress());

		String client_ip = sock.getInetAddress().getHostAddress();
		httpServer.addClient(client_ip);
		Debug.message("client_ip: " + client_ip);

		HTTPRequest httpReq = new HTTPRequest();
		httpReq.setSocket(httpSock);
		InputStream in = httpSock.getInputStream();// 这样会好一些，效率高一些
		InputStreamReader isr = new InputStreamReader(in);
		BufferedReader reader = new BufferedReader(isr);
		synchronized (QiyiHttpServerThread.class)
		{
			if (ConnectCount == 0)// 说明是第一次
			{
				httpServer.performControlPointConnectRendererListener(true);// 有连接
			}
			ConnectCount++;
		}
		while (httpServer.getHttpServerThread() != null)
		{
			httpServer.performControlPointConnectRendererListener(true);
			if (httpReq.readQuickly(reader) == false)// 快速读取
			{
				Debug.message("[QiyiHttpServerThread] Exit thread [httpReq.read() == false]...ClientAddr="
						+ sock.getRemoteSocketAddress());
				break;
			}
			// 这条消息，是否要向上抛，是取决于这条消息是否有最先到达的
			if (httpReq.getIsKeepAlive() == true)
			{
				httpServer.addClient(client_ip);
				continue;
			} else if (httpReq.getIsSingleSend() == true)
			{
				httpServer.performRequestListener(httpReq);
			} else
			{
				if (RacingStrategy.isMessageOk(httpReq) == true)
				{
					httpServer.performRequestListener(httpReq);
				}
			}
		}

		Debug.message("[QiyiHttpServerThread] Thread exit...ClientAddr=" + sock.getRemoteSocketAddress());
		httpServer.removeClient(client_ip);
		httpSock.close();
		httpSock = null;
		synchronized (QiyiHttpServerThread.class)
		{
			ConnectCount--;
			if (ConnectCount <= 0)// 连接数为<=0，则可以向上回调，说明没有连接设备
			{
				httpServer.performControlPointConnectRendererListener(false);
			}
		}
	}
}
