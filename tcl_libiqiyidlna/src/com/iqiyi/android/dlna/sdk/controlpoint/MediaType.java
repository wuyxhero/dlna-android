/*******************************************************
 * Copyright (C) 2015 iQIYI.COM - All Rights Reserved
 * 
 * This file is part of {IQIYI_DLAN}.
 * Unauthorized copy of this file, via any medium is strictly prohibited.
 * Proprietary and Confidential.
 * 
 * Author(s): wanjia<wanjia@qiyi.com>
 * 
 *******************************************************/
package com.iqiyi.android.dlna.sdk.controlpoint;

public enum MediaType
{

	VIDEO(MediaType.videoType), MUSIC(MediaType.AudioType), IMAGE(MediaType.ImageType);

	private String typeName;
	private static final String videoType = "object.item.videoItem";
	private static final String AudioType = "object.item.audioItem.musicTrack";
	private static final String ImageType = "object.item.imageItem.photo";

	MediaType(String typeName)
	{
		this.typeName = typeName;
	}

	public String getTypeName()
	{
		return typeName;
	}

}
