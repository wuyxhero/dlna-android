/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: ServiceList.java
 *
 *	Revision;
 *
 *	12/04/02
 *		- first revision.
 *	06/18/03
 *		- Added caching a ArrayIndexOfBound exception.
 *
 ******************************************************************/

package org.cybergarage.upnp;

import java.util.Vector;

public class ServiceList extends Vector<Service>
{
	private static final long serialVersionUID = 1L;

	public final static String ELEM_NAME = "serviceList";

	public ServiceList()
	{
	}

	public Service getService(int n)
	{
		Service service = null;
		try
		{
			service = get(n);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return service;
	}
}
