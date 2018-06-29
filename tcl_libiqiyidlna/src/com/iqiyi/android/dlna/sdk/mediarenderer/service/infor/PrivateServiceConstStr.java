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
package com.iqiyi.android.dlna.sdk.mediarenderer.service.infor;

public class PrivateServiceConstStr
{

	public final static String SERVICE_NAME = "PrivateServer";
	public final static String SERVICE_TYPE = "urn:schemas-upnp-org:service:" + SERVICE_NAME + ":1";
	public final static String SERVICE_ID = "urn:upnp-org:serviceId:" + SERVICE_NAME;

	public final static String SCPDURL = "_urn:schemas-upnp-org:service:" + SERVICE_NAME + "_scpd.xml";
	public final static String CONTROL_URL = "_urn:schemas-upnp-org:service:" + SERVICE_NAME + "_control";
	public final static String EVENTSUB_URL = "_urn:schemas-upnp-org:service:" + SERVICE_NAME + "_event";

	/*
	 * 默认的action
	 */
	public final static String SEND_MESSAGE = "SendMessage";
	public final static String NOTIFY_MESSAGE = "NotifyMessage";

	/*
	 * 通知消息的内容
	 */
	public final static String NOTIFY_MSG = "NotifyMsg";//通过gena框架来建立起来
	/*
	 * 变量
	 */
	public final static String INSTANCE_ID = "InstanceID";
	public final static String INFOR = "Infor";
	public final static String RESULT = "Result";

	public final static String A_ARG_TYPE_NOTIFYMSG = "A_ARG_TYPE_NOTIFYMSG";

	/*
	 * 描述文档
	 */
	public final static String SCPD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<scpd xmlns=\"urn:schemas-upnp-org:service-1-0\">" + "<specVersion>" + " <major>1</major>" + "<minor>0</minor>"
			+ "</specVersion>" + "<serviceStateTable>" + "<stateVariable sendEvents=\"no\">"
			+ "<name>A_ARG_TYPE_InstanceID</name>" + "<dataType>ui4</dataType>" + "</stateVariable>" +

			"<stateVariable sendEvents=\"yes\">" + "<name>A_ARG_TYPE_NOTIFYMSG</name>" + "<dataType>string</dataType>"
			+ "</stateVariable>" +

			"<stateVariable sendEvents=\"no\">" + "<name>A_ARG_TYPE_INFOR</name>" + "<dataType>string</dataType>"
			+ "</stateVariable>" + "<stateVariable sendEvents=\"no\">" + "<name>A_ARG_TYPE_SendMessage_Result</name>"
			+ "<dataType>string</dataType></stateVariable>" + "</serviceStateTable>" + "<actionList>" + "<action>"
			+ "<name>SendMessage</name>" + "<argumentList>" + "<argument>" + "<name>InstanceID</name>"
			+ "<direction>in</direction>" + "<relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>"
			+ "</argument>" + "<argument>" + "<name>Infor</name>" + "<direction>in</direction>"
			+ "<relatedStateVariable>A_ARG_TYPE_INFOR</relatedStateVariable>" + "</argument>" + "<argument>"
			+ "<name>Result</name>" + "<direction>out</direction>"
			+ "<relatedStateVariable>A_ARG_TYPE_SendMessage_Result</relatedStateVariable>" + "</argument>" + "</argumentList>"
			+ "</action>" +

			"<action>" + "<name>NotifyMessage</name>" + "<argumentList>" + "<argument>" + "<name>NotifyMsg</name>"
			+ "<direction>in</direction>" + "<relatedStateVariable>A_ARG_TYPE_NOTIFYMSG</relatedStateVariable>" + "</argument>"
			+ "</argumentList>" + "</action>" +

			"</actionList>" + "</scpd>";
}
