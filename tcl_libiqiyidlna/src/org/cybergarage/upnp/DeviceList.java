/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: DeviceList.java
 *
 *	Revision;
 *
 *	12/04/02
 *		- first revision.
 *
 ******************************************************************/

package org.cybergarage.upnp;

import java.util.Vector;

public class DeviceList extends Vector<Device>
{
	private static final long serialVersionUID = 1L;

	public final static String ELEM_NAME = "deviceList";

	public DeviceList()
	{
	}

	public Device getDevice(int n)
	{
		Device device = null;
		try
		{
			device = get(n);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return device;
	}
}
