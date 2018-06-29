/*******************************************************
 * Copyright (C) 2014 iQIYI.COM - All Rights Reserved
 * 
 * This file is part of {IQIYI_DLAN}.
 * Unauthorized copy of this file, via any medium is strictly prohibited.
 * Proprietary and Confidential.
 * 
 * Author(s): chenjiebin<chenjiebin@qiyi.com> maning<maning@qiyi.com>
 * 
 *******************************************************/
package com.iqiyi.android.dlna.sdk.dlnahttpserver;

public class MessageData
{

	private String uuid;//controlPoint的uuid的值
	private long time;//如果time为0，说明是第一次
	private byte data;//发送过来的数据

	public MessageData()
	{
		uuid = null;
		time = 0;
		data = 0;
	}

	public MessageData(String uuid, long time, byte data)
	{
		this.uuid = uuid;
		this.time = time;
		this.data = data;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public byte getData()
	{
		return data;
	}

	public void setData(byte data)
	{
		this.data = data;
	}

}
