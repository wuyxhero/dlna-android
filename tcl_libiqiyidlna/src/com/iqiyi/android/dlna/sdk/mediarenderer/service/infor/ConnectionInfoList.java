/******************************************************************
 *
 *	MediaServer for CyberLink
 *
 *	Copyright (C) Satoshi Konno 2003-2004
 *
 *	File: ConnectionInfoList.java
 *
 *	Revision;
 *
 *	02/22/08
 *		- first revision.
 *
 ******************************************************************/

package com.iqiyi.android.dlna.sdk.mediarenderer.service.infor;

import java.util.*;

public class ConnectionInfoList extends Vector<ConnectionInfo>
{
	private static final long serialVersionUID = 1L;

	public ConnectionInfo getConnectionInfo(int n)
	{
		return (ConnectionInfo) get(n);
	}
}
