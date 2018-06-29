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
package com.iqiyi.android.sdk.dlna.keeper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/*
 * 该类用于存储 dmr的信息
 * 目前只存储uuid的值
 */
public class DmrInfor implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String uuid = "";//为uuid或者为udn 在dmr中是uuid，在dmc这边是udn
	private String fileMd5 = "";//dmr设备描述文件的filemd5的值
	private Map<String, String> serverMap = new HashMap<String, String>();//服务描述文件的Hash值
	private String descriptionFileXml = "";

	public DmrInfor()
	{
		uuid = "";
		fileMd5 = "";
		serverMap = new HashMap<String, String>();
		descriptionFileXml = "";
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	/////////////////////////////////////
	public String getDescriptionFileXml()
	{
		return descriptionFileXml;
	}

	public void setDescriptionFileXml(String descriptionFileXml)
	{
		this.descriptionFileXml = descriptionFileXml;
	}

	public String getFileMd5()
	{
		return fileMd5;
	}

	public void setFileMd5(String fileMd5)
	{
		this.fileMd5 = fileMd5;
	}

	public Map<String, String> getServerMap()
	{
		return serverMap;
	}

	public void setServerMap(Map<String, String> serverMap)
	{
		this.serverMap = serverMap;
	}

}
