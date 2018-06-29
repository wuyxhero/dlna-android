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
 * 是否有设备连接到TV端的回调函数
 * 当有设备和TV连接时候，回调，只有连接数大于1时，回调
 * 当有设备断开和TV连接时，回调，只有连接数为0时回调
 */
public interface ControlPointConnectRendererListener
{

	//isConnect为true时，表示连接，为false时表示没有设备连接
	public void onReceiveDeviceConnect(boolean isConnect);

}
