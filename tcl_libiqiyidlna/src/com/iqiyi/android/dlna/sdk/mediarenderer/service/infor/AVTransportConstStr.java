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

public class AVTransportConstStr
{
	// //////////////////////////////////////////////
	// Constants
	// //////////////////////////////////////////////
	public final static String SERVICE_NAME = "AVTransport";
	public final static String SERVICE_TYPE = "urn:schemas-upnp-org:service:" + SERVICE_NAME + ":1";
	public final static String SERVICE_ID = "urn:upnp-org:serviceId:" + SERVICE_NAME;
	public final static String SCPDURL = "_urn:schemas-upnp-org:service:" + SERVICE_NAME + "_scpd.xml";
	public final static String CONTROL_URL = "_urn:schemas-upnp-org:service:" + SERVICE_NAME + "_control";
	public final static String EVENTSUB_URL = "_urn:schemas-upnp-org:service:" + SERVICE_NAME + "_event";

	// Browse Action
	public final static String TRANSPORTSTATE = "TransportState";
	public final static String TRANSPORTSTATUS = "TransportStatus";
	public final static String PLAYBACKSTORAGEMEDIUM = "PlaybackStorageMedium";
	public final static String RECORDSTORAGEMEDIUM = "RecordStorageMedium";
	public final static String POSSIBLEPLAYBACKSTORAGEMEDIA = "PossiblePlaybackStorageMedia";
	public final static String POSSIBLERECORDSTORAGEMEDIA = "PossibleRecordStorageMedia";
	public final static String CURRENTPLAYMODE = "CurrentPlayMode";
	public final static String TRANSPORTPLAYSPEED = "TransportPlaySpeed";
	public final static String RECORDMEDIUMWRITESTATUS = "RecordMediumWriteStatus";
	public final static String CURRENTRECORDQUALITYMODE = "CurrentRecordQualityMode";
	public final static String POSSIBLERECORDQUALITYMODES = "PossibleRecordQualityModes";
	public final static String NUMBEROFTRACKS = "NumberOfTracks";
	public final static String CURRENTTRACK = "CurrentTrack";
	public final static String CURRENTTRACKDURATION = "CurrentTrackDuration";
	public final static String CURRENTMEDIADURATION = "CurrentMediaDuration";
	public final static String CURRENTTRACKMETADATA = "CurrentTrackMetaData";
	public final static String CURRENTTRACKURI = "CurrentTrackURI";
	public final static String AVTRANSPORTURI = "AVTransportURI";
	public final static String AVTRANSPORTURIMETADATA = "AVTransportURIMetaData";
	public final static String NEXTAVTRANSPORTURI = "NextAVTransportURI";
	public final static String NEXTAVTRANSPORTURIMETADATA = "NextAVTransportURIMetaData";
	public final static String RELATIVETIMEPOSITION = "RelativeTimePosition";
	public final static String ABSOLUTETIMEPOSITION = "AbsoluteTimePosition";
	public final static String RELATIVECOUNTERPOSITION = "RelativeCounterPosition";
	public final static String ABSOLUTECOUNTERPOSITION = "AbsoluteCounterPosition";
	public final static String CURRENTTRANSPORTACTIONS = "CurrentTransportActions";
	public final static String LASTCHANGE = "LastChange";
	public final static String SETAVTRANSPORTURI = "SetAVTransportURI";
	public final static String INSTANCEID = "InstanceID";
	public final static String CURRENTURI = "CurrentURI";
	public final static String CURRENTURIMETADATA = "CurrentURIMetaData";
	public final static String SETNEXTAVTRANSPORTURI = "SetNextAVTransportURI";
	public final static String NEXTURI = "NextURI";
	public final static String NEXTURIMETADATA = "NextURIMetaData";
	public final static String GETMEDIAINFO = "GetMediaInfo";
	public final static String NRTRACKS = "NrTracks";
	public final static String MEDIADURATION = "MediaDuration";
	public final static String PLAYMEDIUM = "PlayMedium";
	public final static String RECORDMEDIUM = "RecordMedium";
	public final static String WRITESTATUS = "WriteStatus";
	public final static String GETTRANSPORTINFO = "GetTransportInfo";
	public final static String CURRENTTRANSPORTSTATE = "CurrentTransportState";
	public final static String CURRENTTRANSPORTSTATUS = "CurrentTransportStatus";
	public final static String CURRENTSPEED = "CurrentSpeed";
	public final static String GETPOSITIONINFO = "GetPositionInfo";
	public final static String TRACK = "Track";
	public final static String TRACKDURATION = "TrackDuration";
	public final static String TRACKMETADATA = "TrackMetaData";
	public final static String TRACKURI = "TrackURI";
	public final static String RELTIME = "RelTime";
	public final static String ABSTIME = "AbsTime";
	public final static String RELCOUNT = "RelCount";
	public final static String ABSCOUNT = "AbsCount";
	public final static String GETDEVICECAPABILITIES = "GetDeviceCapabilities";
	public final static String PLAYMEDIA = "PlayMedia";
	public final static String RECMEDIA = "RecMedia";
	public final static String RECQUALITYMODES = "RecQualityModes";
	public final static String GETTRANSPORTSETTINGS = "GetTransportSettings";
	public final static String PLAYMODE = "PlayMode";
	public final static String RECQUALITYMODE = "RecQualityMode";
	public final static String STOP = "Stop";
	public final static String PLAY = "Play";
	public final static String SPEED = "Speed";
	public final static String PAUSE = "Pause";
	public final static String RECORD = "Record";
	public final static String SEEK = "Seek";
	public final static String UNIT = "Unit";
	public final static String TARGET = "Target";
	public final static String NEXT = "Next";
	public final static String PREVIOUS = "Previous";
	public final static String SETPLAYMODE = "SetPlayMode";
	public final static String NEWPLAYMODE = "NewPlayMode";
	public final static String SETRECORDQUALITYMODE = "SetRecordQualityMode";
	public final static String NEWRECORDQUALITYMODE = "NewRecordQualityMode";
	public final static String GETCURRENTTRANSPORTACTIONS = "GetCurrentTransportActions";
	public final static String ACTIONS = "Actions";

	public final static String STOPPED = "STOPPED";
	public final static String PLAYING = "PLAYING";
	public final static String PAUSED_PLAYBACK = "PAUSED_PLAYBACK";
	public final static String PAUSED_RECORDING = "PAUSED_RECORDING";
	public final static String OK = "OK";
	public final static String ERROR_OCCURRED = "ERROR_OCCURRED";
	public final static String NORMAL = "NORMAL";
	public final static String TRACK_NR = "TRACK_NR";
	public final static String NOT_IMPLEMENTED = "NOT_IMPLEMENTED";
	public final static String NO_MEDIA_PRESENT = "NO_MEDIA_PRESENT";
	public final static String TRANSPORTING = "TRANSPORTING";

	public final static String CurrentTransportActions = "Play,Pause,Stop,Seek,Next,Previous";

	public final static String DEFAULT_LASTCHANGE = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\">" +
			"<InstanceID val=\"0\"><CurrentPlayMode val=\"NORMAL\"/><RecordStorageMedium val=\"NOT_IMPLEMENTED\"/>" +
			"<CurrentTrackURI val=\"\"/><CurrentTrackDuration val=\"00:00:00\"/><CurrentRecordQualityMode val=\"NOT_IMPLEMENTED\"/>" +
			"<CurrentMediaDuration val=\"00:00:00\"/><AVTransportURI val=\"\"/><TransportState val=\"NO_MEDIA_PRESENT\"/>" +
			"<CurrentTrackMetaData val=\"\"/><NextAVTransportURI val=\"NOT_IMPLEMENTED\"/>" +
			"<PossibleRecordQualityModes val=\"NOT_IMPLEMENTED\"/><CurrentTrack val=\"0\"/>" +
			"<NextAVTransportURIMetaData val=\"NOT_IMPLEMENTED\"/><PlaybackStorageMedium val=\"NONE\"/>" +
			"<CurrentTransportActions val=\"Play,Pause,Stop,Seek,Next,Previous\"/><RecordMediumWriteStatus val=\"NOT_IMPLEMENTED\"/>" +
			"<PossiblePlaybackStorageMedia val=\"NONE,NETWORK,HDD,CD-DA,UNKNOWN\"/><AVTransportURIMetaData val=\"\"/>" +
			"<NumberOfTracks val=\"0\"/><PossibleRecordStorageMedia val=\"NOT_IMPLEMENTED\"/>" +
			"<TransportStatus val=\"OK\"/><TransportPlaySpeed val=\"1\"/></InstanceID></Event>";

	public final static String SCPD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
			+ "<scpd xmlns=\"urn:schemas-upnp-org:service-1-0\">\r\n" + "  <specVersion>\r\n" + "    <major>1</major>\r\n"
			+ "    <minor>0</minor>\r\n" + "  </specVersion>\r\n" + "  <actionList>\r\n" + "    <action>\r\n"
			+ "      <name>GetCurrentTransportActions</name>\r\n" + "      <argumentList>\r\n" + "        <argument>\r\n"
			+ "          <name>InstanceID</name>\r\n" + "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>Actions</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>CurrentTransportActions</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "      </argumentList>\r\n" + "    </action>\r\n" + "    <action>\r\n"
			+ "      <name>GetDeviceCapabilities</name>\r\n" + "      <argumentList>\r\n" + "        <argument>\r\n"
			+ "          <name>InstanceID</name>\r\n" + "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>PlayMedia</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>PossiblePlaybackStorageMedia</relatedStateVariable>\r\n"
			+ "        </argument>\r\n" + "        <argument>\r\n" + "          <name>RecMedia</name>\r\n"
			+ "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>PossibleRecordStorageMedia</relatedStateVariable>\r\n"
			+ "        </argument>\r\n" + "        <argument>\r\n" + "          <name>RecQualityModes</name>\r\n"
			+ "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>PossibleRecordQualityModes</relatedStateVariable>\r\n"
			+ "        </argument>\r\n" + "      </argumentList>\r\n" + "    </action>\r\n" + "    <action>\r\n"
			+ "      <name>GetMediaInfo</name>\r\n" + "      <argumentList>\r\n" + "        <argument>\r\n"
			+ "          <name>InstanceID</name>\r\n" + "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>NrTracks</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>NumberOfTracks</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>MediaDuration</name>\r\n"
			+ "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>CurrentMediaDuration</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>CurrentURI</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>AVTransportURI</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>CurrentURIMetaData</name>\r\n"
			+ "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>AVTransportURIMetaData</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>NextURI</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>NextAVTransportURI</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>NextURIMetaData</name>\r\n"
			+ "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>NextAVTransportURIMetaData</relatedStateVariable>\r\n"
			+ "        </argument>\r\n" + "        <argument>\r\n" + "          <name>PlayMedium</name>\r\n"
			+ "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>PlaybackStorageMedium</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>RecordMedium</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>RecordStorageMedium</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>WriteStatus</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>RecordMediumWriteStatus</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "      </argumentList>\r\n" + "    </action>\r\n" + "    <action>\r\n" + "      <name>GetPositionInfo</name>\r\n"
			+ "      <argumentList>\r\n" + "        <argument>\r\n" + "          <name>InstanceID</name>\r\n"
			+ "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>Track</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>CurrentTrack</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>TrackDuration</name>\r\n"
			+ "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>CurrentTrackDuration</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>TrackMetaData</name>\r\n"
			+ "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>CurrentTrackMetaData</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>TrackURI</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>CurrentTrackURI</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>RelTime</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>RelativeTimePosition</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>AbsTime</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>AbsoluteTimePosition</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>RelCount</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>RelativeCounterPosition</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>AbsCount</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>AbsoluteCounterPosition</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "      </argumentList>\r\n" + "    </action>\r\n" + "    <action>\r\n"
			+ "      <name>GetTransportInfo</name>\r\n" + "      <argumentList>\r\n" + "        <argument>\r\n"
			+ "          <name>InstanceID</name>\r\n" + "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>CurrentTransportState</name>\r\n"
			+ "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>TransportState</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>CurrentTransportStatus</name>\r\n"
			+ "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>TransportStatus</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>CurrentSpeed</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>TransportPlaySpeed</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "      </argumentList>\r\n" + "    </action>\r\n" + "    <action>\r\n"
			+ "      <name>GetTransportSettings</name>\r\n" + "      <argumentList>\r\n" + "        <argument>\r\n"
			+ "          <name>InstanceID</name>\r\n" + "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>PlayMode</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>CurrentPlayMode</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>RecQualityMode</name>\r\n"
			+ "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>CurrentRecordQualityMode</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "      </argumentList>\r\n" + "    </action>\r\n" + "    <action>\r\n" + "      <name>Next</name>\r\n"
			+ "      <argumentList>\r\n" + "        <argument>\r\n" + "          <name>InstanceID</name>\r\n"
			+ "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "      </argumentList>\r\n" + "    </action>\r\n" + "    <action>\r\n" + "      <name>Pause</name>\r\n"
			+ "      <argumentList>\r\n" + "        <argument>\r\n" + "          <name>InstanceID</name>\r\n"
			+ "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "      </argumentList>\r\n" + "    </action>\r\n" + "    <action>\r\n" + "      <name>Play</name>\r\n"
			+ "      <argumentList>\r\n" + "        <argument>\r\n" + "          <name>InstanceID</name>\r\n"
			+ "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>Speed</name>\r\n" + "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>TransportPlaySpeed</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "      </argumentList>\r\n" + "    </action>\r\n" + "    <action>\r\n" + "      <name>Previous</name>\r\n"
			+ "      <argumentList>\r\n" + "        <argument>\r\n" + "          <name>InstanceID</name>\r\n"
			+ "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "      </argumentList>\r\n" + "    </action>\r\n" + "    <action>\r\n" + "      <name>Seek</name>\r\n"
			+ "      <argumentList>\r\n" + "        <argument>\r\n" + "          <name>InstanceID</name>\r\n"
			+ "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>Unit</name>\r\n" + "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_SeekMode</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>Target</name>\r\n" + "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_SeekTarget</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "      </argumentList>\r\n" + "    </action>\r\n" + "    <action>\r\n"
			+ "      <name>SetAVTransportURI</name>\r\n" + "      <argumentList>\r\n" + "        <argument>\r\n"
			+ "          <name>InstanceID</name>\r\n" + "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>CurrentURI</name>\r\n" + "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>AVTransportURI</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>CurrentURIMetaData</name>\r\n"
			+ "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>AVTransportURIMetaData</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "      </argumentList>\r\n" + "    </action>\r\n" + "    <action>\r\n" + "      <name>SetPlayMode</name>\r\n"
			+ "      <argumentList>\r\n" + "        <argument>\r\n" + "          <name>InstanceID</name>\r\n"
			+ "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>NewPlayMode</name>\r\n" + "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>CurrentPlayMode</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "      </argumentList>\r\n" + "    </action>\r\n" + "    <action>\r\n" + "      <name>Stop</name>\r\n"
			+ "      <argumentList>\r\n" + "        <argument>\r\n" + "          <name>InstanceID</name>\r\n"
			+ "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "      </argumentList>\r\n" + "    </action>\r\n" + "  </actionList>\r\n" + "  <serviceStateTable>\r\n"
			+ "    <stateVariable sendEvents=\"no\">\r\n" + "      <name>CurrentPlayMode</name>\r\n"
			+ "      <dataType>string</dataType>\r\n" + "      <defaultValue>NORMAL</defaultValue>\r\n"
			+ "      <allowedValueList>\r\n" + "        <allowedValue>NORMAL</allowedValue>\r\n"
			+ "        <allowedValue>REPEAT_ONE</allowedValue>\r\n" + "        <allowedValue>REPEAT_ALL</allowedValue>\r\n"
			+ "        <allowedValue>SHUFFLE</allowedValue>\r\n" + "        <allowedValue>SHUFFLE_NOREPEAT</allowedValue>\r\n"
			+ "      </allowedValueList>\r\n" + "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>RecordStorageMedium</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "      <allowedValueList>\r\n" + "        <allowedValue>NOT_IMPLEMENTED</allowedValue>\r\n"
			+ "      </allowedValueList>\r\n" + "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"yes\">\r\n"
			+ "      <name>LastChange</name>\r\n" + "      <dataType>string</dataType>\r\n" + "    </stateVariable>\r\n"
			+ "    <stateVariable sendEvents=\"no\">\r\n" + "      <name>RelativeTimePosition</name>\r\n"
			+ "      <dataType>string</dataType>\r\n" + "    </stateVariable>\r\n"
			+ "    <stateVariable sendEvents=\"no\">\r\n" + "      <name>CurrentTrackURI</name>\r\n"
			+ "      <dataType>string</dataType>\r\n" + "    </stateVariable>\r\n"
			+ "    <stateVariable sendEvents=\"no\">\r\n" + "      <name>CurrentTrackDuration</name>\r\n"
			+ "      <dataType>string</dataType>\r\n" + "    </stateVariable>\r\n"
			+ "    <stateVariable sendEvents=\"no\">\r\n" + "      <name>CurrentRecordQualityMode</name>\r\n"
			+ "      <dataType>string</dataType>\r\n" + "      <allowedValueList>\r\n"
			+ "        <allowedValue>NOT_IMPLEMENTED</allowedValue>\r\n" + "      </allowedValueList>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>CurrentMediaDuration</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>AbsoluteCounterPosition</name>\r\n" + "      <dataType>i4</dataType>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>RelativeCounterPosition</name>\r\n" + "      <dataType>i4</dataType>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>A_ARG_TYPE_InstanceID</name>\r\n" + "      <dataType>ui4</dataType>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>AVTransportURI</name>\r\n" + "      <dataType>string</dataType>\r\n" + "    </stateVariable>\r\n"
			+ "    <stateVariable sendEvents=\"no\">\r\n" + "      <name>TransportState</name>\r\n"
			+ "      <dataType>string</dataType>\r\n" + "      <allowedValueList>\r\n"
			+ "        <allowedValue>STOPPED</allowedValue>\r\n" + "        <allowedValue>PAUSED_PLAYBACK</allowedValue>\r\n"
			+ "        <allowedValue>PLAYING</allowedValue>\r\n" + "        <allowedValue>TRANSITIONING</allowedValue>\r\n"
			+ "        <allowedValue>NO_MEDIA_PRESENT</allowedValue>\r\n" + "      </allowedValueList>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>CurrentTrackMetaData</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>NextAVTransportURI</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>PossibleRecordQualityModes</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "      <allowedValueList>\r\n" + "        <allowedValue>NOT_IMPLEMENTED</allowedValue>\r\n"
			+ "      </allowedValueList>\r\n" + "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>CurrentTrack</name>\r\n" + "      <dataType>ui4</dataType>\r\n" + "      <allowedValueRange>\r\n"
			+ "        <minimum>0</minimum>\r\n" + "        <maximum>65535</maximum>\r\n" + "        <step>1</step>\r\n"
			+ "      </allowedValueRange>\r\n" + "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>AbsoluteTimePosition</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>NextAVTransportURIMetaData</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>PlaybackStorageMedium</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "      <allowedValueList>\r\n" + "        <allowedValue>NONE</allowedValue>\r\n"
			+ "        <allowedValue>UNKNOWN</allowedValue>\r\n" + "        <allowedValue>CD-DA</allowedValue>\r\n"
			+ "        <allowedValue>HDD</allowedValue>\r\n" + "        <allowedValue>NETWORK</allowedValue>\r\n"
			+ "      </allowedValueList>\r\n" + "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>CurrentTransportActions</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>RecordMediumWriteStatus</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "      <allowedValueList>\r\n" + "        <allowedValue>NOT_IMPLEMENTED</allowedValue>\r\n"
			+ "      </allowedValueList>\r\n" + "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>PossiblePlaybackStorageMedia</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "      <allowedValueList>\r\n" + "        <allowedValue>NONE</allowedValue>\r\n"
			+ "        <allowedValue>UNKNOWN</allowedValue>\r\n" + "        <allowedValue>CD-DA</allowedValue>\r\n"
			+ "        <allowedValue>HDD</allowedValue>\r\n" + "        <allowedValue>NETWORK</allowedValue>\r\n"
			+ "      </allowedValueList>\r\n" + "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>AVTransportURIMetaData</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>NumberOfTracks</name>\r\n" + "      <dataType>ui4</dataType>\r\n" + "      <allowedValueRange>\r\n"
			+ "        <minimum>0</minimum>\r\n" + "        <maximum>65535</maximum>\r\n" + "      </allowedValueRange>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>A_ARG_TYPE_SeekMode</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "      <allowedValueList>\r\n" + "        <allowedValue>REL_TIME</allowedValue>\r\n"
			+ "        <allowedValue>TRACK_NR</allowedValue>\r\n" + "      </allowedValueList>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>A_ARG_TYPE_SeekTarget</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>PossibleRecordStorageMedia</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "      <allowedValueList>\r\n" + "        <allowedValue>NOT_IMPLEMENTED</allowedValue>\r\n"
			+ "      </allowedValueList>\r\n" + "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>TransportStatus</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "      <allowedValueList>\r\n" + "        <allowedValue>OK</allowedValue>\r\n"
			+ "        <allowedValue>ERROR_OCCURRED</allowedValue>\r\n" + "      </allowedValueList>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>TransportPlaySpeed</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "      <allowedValueList>\r\n" + "        <allowedValue>1</allowedValue>\r\n" + "      </allowedValueList>\r\n"
			+ "    </stateVariable>\r\n" + "  </serviceStateTable>\r\n" + "</scpd>";
}
