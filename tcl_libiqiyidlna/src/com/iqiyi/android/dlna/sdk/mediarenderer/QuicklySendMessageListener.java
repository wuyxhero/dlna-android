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
package com.iqiyi.android.dlna.sdk.mediarenderer;

/*
 * 快速通道的回调信息
 */
public interface QuicklySendMessageListener
{
	/*
	 * 通过快速通道，接收到DMC给DMR得发送的消息
	 */
	public void onQuicklySendMessageRecieved(byte data);
}
