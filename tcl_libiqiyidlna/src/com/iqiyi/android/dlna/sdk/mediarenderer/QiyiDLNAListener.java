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
 * 奇艺自定义的DLNA的私有接口回调
 */
public interface QiyiDLNAListener
{
	/*
	 * 接收SendMessage的消息
	 */
	public void onReceiveSendMessage(int instance, String infor, StringBuffer outResult);

}
