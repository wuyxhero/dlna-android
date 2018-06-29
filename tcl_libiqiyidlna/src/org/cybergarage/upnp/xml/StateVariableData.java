/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File:StateVariableData.java
 *
 *	Revision;
 *
 *	02/05/03
 *		- first revision.
 *	01/06/04
 *		- Added setQueryListener() and getQueryListener().
 *
 ******************************************************************/

package org.cybergarage.upnp.xml;

import org.cybergarage.upnp.control.*;

public class StateVariableData extends NodeData
{
	////////////////////////////////////////////////
	// value
	////////////////////////////////////////////////

	private String mValue = "";

	private String mValue_ext = "";

	public String getValue()
	{
		return mValue;
	}

	public void setValue(String value)
	{
		mValue = value;
	}

	public String getValue_ext()
	{
		return mValue_ext;
	}

	public void setValue_ext(String value)
	{
		mValue_ext = value;
	}

	////////////////////////////////////////////////
	// QueryListener
	////////////////////////////////////////////////

	private QueryListener queryListener = null;

	public QueryListener getQueryListener()
	{
		return queryListener;
	}

	public void setQueryListener(QueryListener queryListener)
	{
		this.queryListener = queryListener;
	}

	////////////////////////////////////////////////
	// QueryResponse
	////////////////////////////////////////////////

	private QueryResponse queryRes = null;

	public QueryResponse getQueryResponse()
	{
		return queryRes;
	}

	public void setQueryResponse(QueryResponse res)
	{
		queryRes = res;
	}

}
