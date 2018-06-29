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
 * 标准的DLNA的DMR回调消息
 */
public interface StandardDLNAListener
{
	/*
	 * avtransport的服务
	 */
	// 下一个视频
	public void Next(int instanceID);

	// 暂停视频
	public void Pause(int instanceID);

	// 播放视频
	public void Play(int instanceID, String speed);

	// 上一个视频
	public void Previous(int instanceID);

	// 快进
	public void Seek(int instanceID, int seekMode, String target);

	// 设置播放地址
	public void SetAVTransportURI(int instanceID, String currentURI, DlnaMediaModel currentURIMetaData);

	// 停止播放
	public void Stop(int instanceID);

	// 设置下一个播放地址
	public void SetNextAVTransportURI(int instanceID, String nextURI, DlnaMediaModel nextAVTransportURIMetaData);

	// 设置播放模式
	public void SetPlayMode(int instanceID, String newPlayMode);

	/*
	 * renderer control 服务
	 */
	// 获取视频的声音状态
	public boolean GetMute(int instanceID, String channel);

	// 获取视频的音量
	public int GetVolume(int instanceID, String channel);

	// 设置声音状态
	public void SetMute(int instanceID, String channel, boolean desireMute);

	// 设置声音的音量
	public void SetVolume(int instanceID, String channel, int desireVolume);
}
