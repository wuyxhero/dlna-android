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

public class QPlayServiceConstStr
{

	// //////////////////////////////////////////////
	// Constants
	// //////////////////////////////////////////////

	public final static String SERVICE_NAME = "QPlay";
	public final static String SERVICE_TYPE = "urn:schemas-tencent-com:service:QPlay:1";
	public final static String SERVICE_ID = "urn:tencent-com:serviceId:QPlay";

	public final static String SCPDURL = "_urn-schemas-upnp-org-service-QPlay_scpd.xml";
	public final static String CONTROL_URL = "_urn-schemas-upnp-org-service-QPlay_control";
	public final static String EVENTSUB_URL = "_urn-schemas-upnp-org-service-QPlay_event";

	// Browse Action

	public final static String SetNetwork = "SetNetwork";
	public final static String SSID = "SSID";
	public final static String Key = "Key";
	public final static String AuthAlgo = "AuthAlgo";
	public final static String CipherAlgo = "CipherAlgo";

	public final static String QPlayAuth = "QPlayAuth";
	public final static String Seed = "Seed";
	public final static String Code = "Code";
	public final static String MID = "MID";
	public final static String DID = "DID";

	public final static String InsertTracks = "InsertTracks";
	public final static String QueueID = "QueueID";
	public final static String StartingIndex = "StartingIndex";
	public final static String TracksMetaData = "TracksMetaData";
	public final static String NumberOfSuccess = "NumberOfSuccess";

	public final static String RemoveTracks = "RemoveTracks";
	//	public final static String QueueID = "QueueID";
	//	public final static String StartingIndex = "StartingIndex";
	//	public final static String TracksMetaData = "TracksMetaData";
	//	public final static String NumberOfSuccess = "NumberOfSuccess";

	public final static String GetTracksInfo = "GetTracksInfo";
	//	public final static String StartingIndex = "StartingIndex";
	public final static String NumberOfTracks = "NumberOfTracks";
	//	public final static String TracksMetaData = "TracksMetaData";

	public final static String SetTracksInfo = "SetTracksInfo";
	//	public final static String QueueID = "QueueID";
	//	public final static String StartingIndex = "StartingIndex";
	public final static String NextIndex = "NextIndex";
	//	public final static String TracksMetaData = "TracksMetaData";
	//	public final static String NumberOfSuccess = "NumberOfSuccess";	

	public final static String GetTracksCount = "GetTracksCount";
	public final static String NrTracks = "NrTracks";

	public final static String GetMaxTracks = "GetMaxTracks";
	public final static String MaxTracks = "MaxTracks";

	public final static String GetLyricSupportType = "GetLyricSupportType";
	public final static String LyricType = "LyricType";

	public final static String SetLyric = "SetLyric";
	public final static String SongID = "SongID";
	public final static String Lyric = "Lyric";

	public final static String RemoveAllTracks = "RemoveAllTracks";
	public final static String UpdateID = "UpdateID";

	public final static String SCPD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
			+ "<scpd xmlns=\"urn:schemas-tencent-com:service-1-0\">\n" + "  <specVersion>\n" + "    <major>1</major>\n"
			+ "    <minor>0</minor>\n" + "  </specVersion>\n" + "  <actionList>\n" + "    <action>\n"
			+ "      <name>SetNetwork</name>\n" + "      <argumentList>\n" + "        <argument>\n"
			+ "          <name>SSID</name>\n" + "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_SSID</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>Key</name>\n" + "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_Key</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>AuthAlgo</name>\n" + "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_AuthAlgo</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>CipherAlgo</name>\n" + "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_CipherAlgo</relatedStateVariable>\n" + "        </argument>\n"
			+ "      </argumentList>\n" + "    </action>\n" + "    <action>\n" + "      <name>QPlayAuth</name>\n"
			+ "      <argumentList>\n" + "        <argument>\n" + "          <name>Seed</name>\n"
			+ "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_Seed</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>Code</name>\n" + "          <direction>out</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_Code</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>MID</name>\n" + "          <direction>out</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_MID</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>DID</name>\n" + "          <direction>out</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_DID</relatedStateVariable>\n" + "        </argument>\n"
			+ "      </argumentList>\n" + "    </action>\n" + "    <action>\n" + "      <name>InsertTracks</name>\n"
			+ "      <argumentList>\n" + "        <argument>\n" + "          <name>QueueID</name>\n"
			+ "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_QueueID</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>StartingIndex</name>\n" + "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_StartingIndex</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>TracksMetaData</name>\n" + "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_TracksMetaData</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>NumberOfSuccess</name>\n" + "          <direction>out</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_NumberOfTracks</relatedStateVariable>\n" + "        </argument>\n"
			+ "      </argumentList>\n" + "    </action>\n" + "    <action>\n" + "      <name>RemoveTracks</name>\n"
			+ "      <argumentList>\n" + "        <argument>\n" + "          <name>QueueID</name>\n"
			+ "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_QueueID</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>StartingIndex</name>\n" + "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_StartingIndex</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>NumberOfTracks</name>\n" + "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_NumberOfTracks</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>NumberOfSuccess</name>\n" + "          <direction>out</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_NumberOfTracks</relatedStateVariable>\n" + "        </argument>\n"
			+ "      </argumentList>\n" + "    </action>\n" + "    <action>\n" + "      <name>RemoveAllTracks</name>\n"
			+ "      <argumentList>\n" + "        <argument>\n" + "          <name>QueueID</name>\n"
			+ "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_QueueID</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>UpdateID</name>\n" + "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_StartingIndex</relatedStateVariable>\n" + "        </argument>\n"
			+ "      </argumentList>\n" + "    </action>\n" + "    <action>\n" + "      <name>GetTracksInfo</name>\n"
			+ "      <argumentList>\n" + "        <argument>\n" + "          <name>StartingIndex</name>\n"
			+ "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_StartingIndex</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>NumberOfTracks</name>\n" + "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_NumberOfTracks</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>TracksMetaData</name>\n" + "          <direction>out</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_TracksMetaData</relatedStateVariable>\n" + "        </argument>\n"
			+ "      </argumentList>\n" + "    </action>\n" + "    <action>\n" + "      <name>SetTracksInfo</name>\n"
			+ "      <argumentList>\n" + "        <argument>\n" + "          <name>QueueID</name>\n"
			+ "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_QueueID</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>StartingIndex</name>\n" + "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_StartingIndex</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>NextIndex</name>\n" + "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_NextIndex</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>TracksMetaData</name>\n" + "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_TracksMetaData</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>NumberOfSuccess</name>\n" + "          <direction>out</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_NumberOfTracks</relatedStateVariable>\n" + "        </argument>\n"
			+ "      </argumentList>\n" + "    </action>\n" + "    <action>\n" + "      <name>GetTracksCount</name>\n"
			+ "      <argumentList>\n" + "        <argument>\n" + "          <name>NrTracks</name>\n"
			+ "          <direction>out</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_NumberOfTracks</relatedStateVariable>\n" + "        </argument>\n"
			+ "      </argumentList>\n" + "    </action>\n" + "    <action>\n" + "      <name>GetMaxTracks</name>\n"
			+ "      <argumentList>\n" + "        <argument>\n" + "          <name>MaxTracks</name>\n"
			+ "          <direction>out</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_NumberOfTracks</relatedStateVariable>\n" + "        </argument>\n"
			+ "      </argumentList>\n" + "    </action>\n" + "    <action>\n" + "      <name>GetLyricSupportType</name>\n"
			+ "      <argumentList>\n" + "        <argument>\n" + "          <name>LyricType</name>\n"
			+ "          <direction>out</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_LYRIC_TYPE</relatedStateVariable>\n" + "        </argument>\n"
			+ "      </argumentList>\n" + "    </action>\n" + "    <action>\n" + "      <name>SetLyric</name>\n"
			+ "      <argumentList>\n" + "        <argument>\n" + "          <name>SongID</name>\n"
			+ "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_SONG_ID_TYPE</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>LyricType</name>\n" + "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_LYRIC_TYPE</relatedStateVariable>\n" + "        </argument>\n"
			+ "        <argument>\n" + "          <name>Lyric</name>\n" + "          <direction>in</direction>\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_LYRIC_TEXT_TYPE</relatedStateVariable>\n" + "        </argument>\n"
			+ "      </argumentList>\n" + "    </action>\n" + "  </actionList>\n" + "  <serviceStateTable>\n"
			+ "    <stateVariable sendEvents=\"no\">\n" + "      <name>A_ARG_TYPE_SSID</name>\n"
			+ "      <dataType>string</dataType>\n" + "    </stateVariable>\n" + "    <stateVariable sendEvents=\"no\">\n"
			+ "      <name>A_ARG_TYPE_Key</name>\n" + "      <dataType>string</dataType>\n" + "    </stateVariable>\n"
			+ "    <stateVariable sendEvents=\"no\">\n" + "      <name>A_ARG_TYPE_AuthAlgo</name>\n"
			+ "      <dataType>string</dataType>\n" + "      <allowedValueList>\n"
			+ "        <allowedValue>open</allowedValue>\n" + "        <allowedValue>shared</allowedValue>\n"
			+ "        <allowedValue>WPA</allowedValue>\n" + "        <allowedValue>WPAPSK</allowedValue>\n"
			+ "        <allowedValue>WPA2</allowedValue>\n" + "        <allowedValue>WPA2PSK</allowedValue>\n"
			+ "      </allowedValueList>\n" + "    </stateVariable>\n" + "    <stateVariable sendEvents=\"no\">\n"
			+ "      <name>A_ARG_TYPE_CipherAlgo</name>\n" + "      <dataType>string</dataType>\n"
			+ "      <allowedValueList>\n" + "        <allowedValue>none</allowedValue>\n"
			+ "        <allowedValue>WEP</allowedValue>\n" + "        <allowedValue>TKIP</allowedValue>\n"
			+ "        <allowedValue>AES</allowedValue>\n" + "      </allowedValueList>\n" + "    </stateVariable>\n"
			+ "    <stateVariable sendEvents=\"no\">\n" + "      <name>A_ARG_TYPE_Seed</name>\n"
			+ "      <dataType>string</dataType>\n" + "    </stateVariable>\n" + "    <stateVariable sendEvents=\"no\">\n"
			+ "      <name>A_ARG_TYPE_Code</name>\n" + "      <dataType>string</dataType>\n" + "    </stateVariable>\n"
			+ "    <stateVariable sendEvents=\"no\">\n" + "      <name>A_ARG_TYPE_MID</name>\n"
			+ "      <dataType>string</dataType>\n" + "    </stateVariable>\n" + "    <stateVariable sendEvents=\"no\">\n"
			+ "      <name>A_ARG_TYPE_DID</name>\n" + "      <dataType>string</dataType>\n" + "    </stateVariable>\n"
			+ "    <stateVariable sendEvents=\"no\">\n" + "      <name>A_ARG_TYPE_QueueID</name>\n"
			+ "      <dataType>string</dataType>\n" + "    </stateVariable>\n" + "    <stateVariable sendEvents=\"no\">\n"
			+ "      <name>A_ARG_TYPE_StartingIndex</name>\n" + "      <dataType>string</dataType>\n"
			+ "    </stateVariable>\n" + "    <stateVariable sendEvents=\"no\">\n"
			+ "      <name>A_ARG_TYPE_NextIndex</name>\n" + "      <dataType>string</dataType>\n" + "    </stateVariable>\n"
			+ "    <stateVariable sendEvents=\"no\">\n" + "      <name>A_ARG_TYPE_NumberOfTracks</name>\n"
			+ "      <dataType>string</dataType>\n" + "    </stateVariable>\n" + "    <stateVariable sendEvents=\"no\">\n"
			+ "      <name>A_ARG_TYPE_TracksMetaData</name>\n" + "      <dataType>string</dataType>\n"
			+ "    </stateVariable>\n" + "    <stateVariable sendEvents=\"no\">\n"
			+ "      <name>A_ARG_TYPE_LYRIC_TYPE</name>\n" + "      <dataType>string</dataType>\n"
			+ "      <allowedValueList>\n" + "       <allowedValue>none</allowedValue>\n"
			+ "        <allowedValue>QRC</allowedValue>\n" + "        <allowedValue>LRC</allowedValue>\n"
			+ "      </allowedValueList>\n" + "    </stateVariable>\n" + "    <stateVariable sendEvents=\"no\">\n"
			+ "      <name>A_ARG_TYPE_SONG_ID_TYPE</name>\n" + "      <dataType>string</dataType>\n" + "    </stateVariable>\n"
			+ "    <stateVariable sendEvents=\"no\">\n" + "      <name>A_ARG_TYPE_LYRIC_TEXT_TYPE</name>\n"
			+ "      <dataType>string</dataType>\n" + "    </stateVariable>\n" + "  </serviceStateTable>\n" + "</scpd>";
}
