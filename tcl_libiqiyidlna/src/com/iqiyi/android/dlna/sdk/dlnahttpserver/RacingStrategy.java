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

import java.util.HashMap;
import java.util.Map;

import org.cybergarage.http.HTTPRequest;
import org.cybergarage.util.Debug;

public class RacingStrategy
{

	public static Map<String, MessageData> dataHashMap = new HashMap<String, MessageData>();

	/*
	 * 判断这条消息是否可用
	 * 数据规则 uuid+"#"+time+"#"+data
	 */
	public static synchronized boolean isMessageOk(HTTPRequest httpReq)
	{
		String dataStr = httpReq.getTempContent();
		String[] dataSplit = dataStr.split("#");//分割数据
		if (dataSplit.length != 3)
		{
			return false;
		}
		String uuidKey = dataSplit[0].trim();
		/*
		try 
		{
			Thread.sleep(5);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
		}
		*/

		if (dataHashMap.containsKey(uuidKey) == true)//命中了
		{
			MessageData msgData = dataHashMap.get(uuidKey);
			//如果当前的消息的时间和之前的时间一样，说明两条消息是一样，丢弃
			long time = Long.parseLong(dataSplit[1]);
			if (msgData.getTime() == time)
			{
				Debug.message("repeat, uuid:" + dataSplit[0] + " time:" + dataSplit[1] + " data:" + dataSplit[2]);
				return false;
			} else if (time < msgData.getTime())//如果当前包的时间，小于以前收到的包的时间，说明这个包延时了
			{
				Debug.message("later, uuid:" + dataSplit[0] + " time:" + dataSplit[1] + " data:" + dataSplit[2]);
				return false;
			}
			//同时更新时间
			msgData.setTime(time);
			return true;
		} else
		//则将这条消息加入到hashmap中去
		{
			Debug.message("uuid 没有命中");
			//将先到的消息，将上1ms，这样后到的消息，就肯定可以去掉
			MessageData msgData = new MessageData(uuidKey, Long.parseLong(dataSplit[1]), dataSplit[2].getBytes()[0]);
			dataHashMap.put(uuidKey, msgData);
			return true;
		}
	}
}
