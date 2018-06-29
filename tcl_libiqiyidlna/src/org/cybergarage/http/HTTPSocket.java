/******************************************************************
 *
 *	CyberHTTP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2004
 *
 *	File: HTTPSocket.java
 *
 *	Revision;
 *
 *	12/12/02
 *		- first revision.
 *	03/11/04
 *		- Added the following methods about chunk size.
 *		  setChunkSize(), getChunkSize().
 *	08/26/04
 *		- Added a isOnlyHeader to post().
 *	03/02/05
 *		- Changed post() to suppot chunked stream.
 *	06/10/05
 *		- Changed post() to add a Date headedr to the HTTPResponse before the posting.
 *	07/07/05
 *		- Lee Peik Feng <pflee@users.sourceforge.net>
 *		- Fixed post() to output the chunk size as a hex string.
 *	
 ******************************************************************/

package org.cybergarage.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;

import org.cybergarage.util.Debug;

public class HTTPSocket
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public HTTPSocket(Socket socket)
	{
		setSocket(socket);
		open();
	}

	public HTTPSocket(HTTPSocket socket)
	{
		if (socket != null)
		{
			setSocket(socket.getSocket());
			setInputStream(socket.getInputStream());
			setOutputStream(socket.getOutputStream());
		}
	}

	public void finalize()
	{
		close();
	}

	////////////////////////////////////////////////
	//	Socket
	////////////////////////////////////////////////

	private Socket socket = null;

	private void setSocket(Socket socket)
	{
		this.socket = socket;
	}

	public Socket getSocket()
	{
		return socket;
	}

	////////////////////////////////////////////////
	//	local address/port
	////////////////////////////////////////////////

	public String getLocalAddress()
	{
		if (getSocket() != null)
		{
			return getSocket().getLocalAddress().getHostAddress();
		}
		return "";
	}

	public int getLocalPort()
	{
		if (getSocket() != null)
		{
			return getSocket().getLocalPort();
		}
		return -1;
	}

	////////////////////////////////////////////////
	//	in/out
	////////////////////////////////////////////////

	private InputStream sockIn = null;
	private OutputStream sockOut = null;

	private void setInputStream(InputStream in)
	{
		sockIn = in;
	}

	public InputStream getInputStream()
	{
		return sockIn;
	}

	private void setOutputStream(OutputStream out)
	{
		sockOut = out;
	}

	private OutputStream getOutputStream()
	{
		return sockOut;
	}

	////////////////////////////////////////////////
	//	open/close
	////////////////////////////////////////////////

	public boolean open()
	{
		Socket sock = getSocket();
		if (sock == null)//这边需要加这个条件，不然会有BUG的
			return false;

		try
		{
			sockIn = sock.getInputStream();
			sockOut = sock.getOutputStream();
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean close()
	{
		try
		{
			if (sockIn != null)
				sockIn.close();
			if (sockOut != null)
				sockOut.close();
			if (getSocket() != null)
			{
				getSocket().close();
			}
		} catch (Exception e)
		{
			Debug.warning(e);
			return false;
		}
		return true;
	}

	public static byte[] append(byte[] org, byte[] to)
	{
		byte[] newByte = new byte[org.length + to.length];
		System.arraycopy(org, 0, newByte, 0, org.length);
		System.arraycopy(to, 0, newByte, org.length, to.length);

		return newByte;
	}

	public static byte[] copyOfRange(byte[] original, int from, int length)
	{
		byte[] copy = new byte[length];
		System.arraycopy(original, from, copy, 0, length);

		return copy;
	}

	////////////////////////////////////////////////
	//	post
	////////////////////////////////////////////////
	private boolean post(HTTPResponse httpRes, byte content[], long contentOffset, long contentLength, boolean isOnlyHeader)
	{
		//TODO Check for bad HTTP agents, this method may be list for IOInteruptedException and for blacklistening
		httpRes.setDate(Calendar.getInstance());

		OutputStream out = getOutputStream();
		byte[] resp;

		try
		{
			httpRes.setContentLength(contentLength);
			resp = append(httpRes.getHeader().getBytes(), HTTP.CRLF.getBytes());

			if (isOnlyHeader == true)
			{
				out.write(resp);
				out.flush();
				return true;
			}

			boolean isChunkedResponse = httpRes.isChunked();

			if (isChunkedResponse == true)
			{
				// Thanks for Lee Peik Feng <pflee@users.sourceforge.net> (07/07/05)
				String chunSizeBuf = Long.toHexString(contentLength);
				resp = append(resp, (chunSizeBuf + HTTP.CRLF).getBytes());
			}

			resp = append(resp, copyOfRange(content, (int) contentOffset, (int) contentLength));

			if (isChunkedResponse == true)
			{
				resp = append(resp, (HTTP.CRLF + "0" + HTTP.CRLF).getBytes());
			}

			out.write(resp);
			out.flush();
		} catch (Exception e)
		{
			//Debug.warning(e);
			return false;
		}

		return true;
	}

	private boolean post(HTTPResponse httpRes, InputStream in, long contentOffset, long contentLength, boolean isOnlyHeader)
	{
		//TODO Check for bad HTTP agents, this method may be list for IOInteruptedException and for blacklistening
		httpRes.setDate(Calendar.getInstance());

		OutputStream out = getOutputStream();

		try
		{
			httpRes.setContentLength(contentLength);

			out.write(httpRes.getHeader().getBytes());
			out.write(HTTP.CRLF.getBytes());

			if (isOnlyHeader == true)
			{
				out.flush();
				return true;
			}

			boolean isChunkedResponse = httpRes.isChunked();

			if (0 < contentOffset)
				in.skip(contentOffset);

			int chunkSize = HTTP.getChunkSize();
			byte readBuf[] = new byte[chunkSize];
			long readCnt = 0;
			long readSize = (chunkSize < contentLength) ? chunkSize : contentLength;
			int readLen = in.read(readBuf, 0, (int) readSize);
			while (0 < readLen && readCnt < contentLength)
			{
				if (isChunkedResponse == true)
				{
					// Thanks for Lee Peik Feng <pflee@users.sourceforge.net> (07/07/05)
					String chunSizeBuf = Long.toHexString(readLen);
					out.write(chunSizeBuf.getBytes());
					out.write(HTTP.CRLF.getBytes());
				}
				out.write(readBuf, 0, readLen);
				if (isChunkedResponse == true)
					out.write(HTTP.CRLF.getBytes());
				readCnt += readLen;
				readSize = (chunkSize < (contentLength - readCnt)) ? chunkSize : (contentLength - readCnt);
				readLen = in.read(readBuf, 0, (int) readSize);
			}

			if (isChunkedResponse == true)
			{
				out.write("0".getBytes());
				out.write(HTTP.CRLF.getBytes());
			}

			out.flush();
		} catch (Exception e)
		{
			Debug.warning(e);
			return false;
		}

		return true;
	}

	public boolean post(HTTPResponse httpRes, long contentOffset, long contentLength, boolean isOnlyHeader)
	{
		//TODO Close if Connection != keep-alive
		if (httpRes.hasContentInputStream() == true)
			return post(httpRes, httpRes.getContentInputStream(), contentOffset, contentLength, isOnlyHeader);
		return post(httpRes, httpRes.getContent(), contentOffset, contentLength, isOnlyHeader);
	}
}
