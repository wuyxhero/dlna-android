/******************************************************************
 *
 *	MediaServer for CyberLink
 *
 *	Copyright (C) Satoshi Konno 2003
 **
 *	File: ItemNodeList.java
 *
 *	Revision;
 *
 *	11/11/03
 *		- first revision.
 *
 ******************************************************************/

package org.cybergarage.upnp.std.av.server.object.item;

import java.util.*;

public class ResourceNodeList extends Vector<ResourceNode>
{
	private static final long serialVersionUID = 1L;

	public ResourceNodeList()
	{
	}

	public ResourceNode getResourceNode(int n)
	{
		return (ResourceNode) get(n);
	}
}
