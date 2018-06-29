/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: IconList.java
 *
 *	Revision;
 *
 *	12/04/02
 *		- first revision.
 *
 ******************************************************************/

package org.cybergarage.upnp;

import java.util.Vector;

public class IconList extends Vector<Icon>
{
	private static final long serialVersionUID = 1L;

	public final static String ELEM_NAME = "iconList";

	public IconList()
	{
	}

	public Icon getIcon(int n)
	{
		Icon icon = null;
		try
		{
			icon = get(n);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return icon;
	}
}
