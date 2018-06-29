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
package com.iqiyi.android.dlna.sdk.controlpoint;

import org.cybergarage.upnp.Device;

public interface NotifyMessageListener
{
	/*
	 * DMC接收DMR消息的回调接口
	 */
	public void onReceiveMessage(Device dev, String msg);
}
