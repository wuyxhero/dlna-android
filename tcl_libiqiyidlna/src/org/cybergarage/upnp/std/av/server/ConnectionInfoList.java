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
 *	06/19/04
 *		- first revision.
 *
 ******************************************************************/

package org.cybergarage.upnp.std.av.server;

import java.util.*;

public class ConnectionInfoList extends Vector<ConnectionInfo>
{
	private static final long serialVersionUID = 1L;

	public ConnectionInfoList()
	{
	}

	////////////////////////////////////////////////
	// getConnectionInfo
	////////////////////////////////////////////////

	public ConnectionInfo getConnectionInfo(int n)
	{
		return (ConnectionInfo) get(n);
	}

}
