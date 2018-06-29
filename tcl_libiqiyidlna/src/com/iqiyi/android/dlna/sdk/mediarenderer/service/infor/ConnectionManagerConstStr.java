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

public class ConnectionManagerConstStr
{

	// //////////////////////////////////////////////
	// Constants
	// //////////////////////////////////////////////

	private final static String SERVICE_NAME = "ConnectionManager";
	public final static String SERVICE_TYPE = "urn:schemas-upnp-org:service:" + SERVICE_NAME + ":1";
	public final static String SERVICE_ID = "urn:upnp-org:serviceId:" + SERVICE_NAME;

	public final static String SCPDURL = "_urn:schemas-upnp-org:service:" + SERVICE_NAME + "_scpd.xml";
	public final static String CONTROL_URL = "_urn:schemas-upnp-org:service:" + SERVICE_NAME + "_control";
	public final static String EVENTSUB_URL = "_urn:schemas-upnp-org:service:" + SERVICE_NAME + "_event";

	// Browse Action

	public final static String HTTP_GET = "http-get";

	public final static String SOURCEPROTOCOLINFO = "SourceProtocolInfo";
	public final static String SINKPROTOCOLINFO = "SinkProtocolInfo";
	public final static String CURRENTCONNECTIONIDS = "CurrentConnectionIDs";
	public final static String GETPROTOCOLINFO = "GetProtocolInfo";
	public final static String SOURCE = "Source";
	public final static String SINK = "Sink";
	public final static String PREPAREFORCONNECTION = "PrepareForConnection";
	public final static String REMOTEPROTOCOLINFO = "RemoteProtocolInfo";
	public final static String PEERCONNECTIONMANAGER = "PeerConnectionManager";
	public final static String PEERCONNECTIONID = "PeerConnectionID";
	public final static String DIRECTION = "Direction";
	public final static String CONNECTIONID = "ConnectionID";
	public final static String AVTRANSPORTID = "AVTransportID";
	public final static String RCSID = "RcsID";
	public final static String CONNECTIONCOMPLETE = "ConnectionComplete";
	public final static String GETCURRENTCONNECTIONIDS = "GetCurrentConnectionIDs";
	public final static String CONNECTIONIDS = "ConnectionIDs";
	public final static String GETCURRENTCONNECTIONINFO = "GetCurrentConnectionInfo";
	public final static String PROTOCOLINFO = "ProtocolInfo";
	public final static String STATUS = "Status";

	public final static String OK = "OK";
	public final static String CONTENTFORMATMISMATCH = "ContentFormatMismatch";
	public final static String INSUFFICIENTBANDWIDTH = "InsufficientBandwidth";
	public final static String UNRELIABLECHANNEL = "UnreliableChannel";
	public final static String UNKNOWN = "Unknown";
	public final static String INPUT = "Input";
	public final static String OUTPUT = "Output";

	public final static String NOT_IMPLEMENTED = "NOT_IMPLEMENTED";
	/*
	 * public final static String SCPD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
	 * "<scpd xmlns=\"urn:schemas-upnp-org:service-1-0\">\n" + "   <specVersion>\n" + "      <major>1</major>\n" +
	 * "      <minor>0</minor>\n" + "	</specVersion>\n" + "	<actionList>\n" + "		<action>\n" +
	 * "         <name>GetCurrentConnectionInfo</name>\n" + "         <argumentList>\n" + "            <argument>\n" +
	 * "               <name>ConnectionID</name>\n" + "               <direction>in</direction>\n" +
	 * "               <relatedStateVariable>A_ARG_TYPE_ConnectionID</relatedStateVariable>\n" + "            </argument>\n" +
	 * "            <argument>\n" + "               <name>RcsID</name>\n" + "               <direction>out</direction>\n" +
	 * "               <relatedStateVariable>A_ARG_TYPE_RcsID</relatedStateVariable>\n" + "            </argument>\n" +
	 * "            <argument>\n" + "               <name>AVTransportID</name>\n" +
	 * "               <direction>out</direction>\n" +
	 * "               <relatedStateVariable>A_ARG_TYPE_AVTransportID</relatedStateVariable>\n" + "            </argument>\n" +
	 * "            <argument>\n" + "               <name>ProtocolInfo</name>\n" + "               <direction>out</direction>\n"
	 * + "               <relatedStateVariable>A_ARG_TYPE_ProtocolInfo</relatedStateVariable>\n" + "            </argument>\n" +
	 * "            <argument>\n" + "               <name>PeerConnectionManager</name>\n" +
	 * "               <direction>out</direction>\n" +
	 * "               <relatedStateVariable>A_ARG_TYPE_ConnectionManager</relatedStateVariable>\n" +
	 * "            </argument>\n" + "            <argument>\n" + "               <name>PeerConnectionID</name>\n" +
	 * "               <direction>out</direction>\n" +
	 * "               <relatedStateVariable>A_ARG_TYPE_ConnectionID</relatedStateVariable>\n" + "            </argument>\n" +
	 * "            <argument>\n" + "               <name>Direction</name>\n" + "               <direction>out</direction>\n" +
	 * "               <relatedStateVariable>A_ARG_TYPE_Direction</relatedStateVariable>\n" + "            </argument>\n" +
	 * "            <argument>\n" + "               <name>Status</name>\n" + "               <direction>out</direction>\n" +
	 * "               <relatedStateVariable>A_ARG_TYPE_ConnectionStatus</relatedStateVariable>\n" + "            </argument>\n"
	 * + "         </argumentList>\n" + "      </action>\n" + "      <action>\n" + "         <name>GetProtocolInfo</name>\n" +
	 * "         <argumentList>\n" + "            <argument>\n" + "               <name>Source</name>\n" +
	 * "               <direction>out</direction>\n" +
	 * "               <relatedStateVariable>SourceProtocolInfo</relatedStateVariable>\n" + "            </argument>\n" +
	 * "            <argument>\n" + "               <name>Sink</name>\n" + "               <direction>out</direction>\n" +
	 * "               <relatedStateVariable>SinkProtocolInfo</relatedStateVariable>\n" + "            </argument>\n" +
	 * "         </argumentList>\n" + "      </action>\n" + "      <action>\n" +
	 * "         <name>GetCurrentConnectionIDs</name>\n" + "         <argumentList>\n" + "            <argument>\n" +
	 * "               <name>ConnectionIDs</name>\n" + "               <direction>out</direction>\n" +
	 * "               <relatedStateVariable>CurrentConnectionIDs</relatedStateVariable>\n" + "            </argument>\n" +
	 * "         </argumentList>\n" + "      </action>\n" + "   </actionList>\n" + "   <serviceStateTable>\n" +
	 * "      <stateVariable sendEvents=\"no\">\n" + "         <name>A_ARG_TYPE_ProtocolInfo</name>\n" +
	 * "         <dataType>string</dataType>\n" + "      </stateVariable>\n" + "      <stateVariable sendEvents=\"no\">\n" +
	 * "         <name>A_ARG_TYPE_ConnectionStatus</name>\n" + "         <dataType>string</dataType>\n" +
	 * "         <allowedValueList>\n" + "            <allowedValue>OK</allowedValue>\n" +
	 * "            <allowedValue>ContentFormatMismatch</allowedValue>\n" +
	 * "            <allowedValue>InsufficientBandwidth</allowedValue>\n" +
	 * "            <allowedValue>UnreliableChannel</allowedValue>\n" + "            <allowedValue>Unknown</allowedValue>\n" +
	 * "         </allowedValueList>\n" + "      </stateVariable>\n" + "      <stateVariable sendEvents=\"no\">\n" +
	 * "         <name>A_ARG_TYPE_AVTransportID</name>\n" + "         <dataType>i4</dataType>\n" + "      </stateVariable>\n" +
	 * "      <stateVariable sendEvents=\"no\">\n" + "         <name>A_ARG_TYPE_RcsID</name>\n" +
	 * "         <dataType>i4</dataType>\n" + "      </stateVariable>\n" + "      <stateVariable sendEvents=\"no\">\n" +
	 * "         <name>A_ARG_TYPE_ConnectionID</name>\n" + "         <dataType>i4</dataType>\n" + "      </stateVariable>\n" +
	 * "      <stateVariable sendEvents=\"no\">\n" + "         <name>A_ARG_TYPE_ConnectionManager</name>\n" +
	 * "         <dataType>string</dataType>\n" + "      </stateVariable>\n" + "      <stateVariable sendEvents=\"yes\">\n" +
	 * "         <name>SourceProtocolInfo</name>\n" + "         <dataType>string</dataType>\n" + "      </stateVariable>\n" +
	 * "      <stateVariable sendEvents=\"yes\">\n" + "         <name>SinkProtocolInfo</name>\n" +
	 * "         <dataType>string</dataType>\n" + "      </stateVariable>\n" + "      <stateVariable sendEvents=\"no\">\n" +
	 * "         <name>A_ARG_TYPE_Direction</name>\n" + "         <dataType>string</dataType>\n" +
	 * "         <allowedValueList>\n" + "            <allowedValue>Input</allowedValue>\n" +
	 * "            <allowedValue>Output</allowedValue>\n" + "         </allowedValueList>\n" + "      </stateVariable>\n" +
	 * "      <stateVariable sendEvents=\"yes\">\n" + "         <name>CurrentConnectionIDs</name>\n" +
	 * "         <dataType>string</dataType>\n" + "      </stateVariable>\n" + "   </serviceStateTable>\n" + "</scpd>";
	 */
	public final static String SCPD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
			+ "<scpd xmlns=\"urn:schemas-upnp-org:service-1-0\">\r\n" + "  <specVersion>\r\n" + "    <major>1</major>\r\n"
			+ "    <minor>0</minor>\r\n" + "  </specVersion>\r\n" + "  <actionList>\r\n" + "    <action>\r\n"
			+ "      <name>GetCurrentConnectionInfo</name>\r\n" + "      <argumentList>\r\n" + "        <argument>\r\n"
			+ "          <name>ConnectionID</name>\r\n" + "          <direction>in</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_ConnectionID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>RcsID</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_RcsID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>AVTransportID</name>\r\n"
			+ "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_AVTransportID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>ProtocolInfo</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_ProtocolInfo</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>PeerConnectionManager</name>\r\n"
			+ "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_ConnectionManager</relatedStateVariable>\r\n"
			+ "        </argument>\r\n" + "        <argument>\r\n" + "          <name>PeerConnectionID</name>\r\n"
			+ "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_ConnectionID</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>Direction</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_Direction</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>Status</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>A_ARG_TYPE_ConnectionStatus</relatedStateVariable>\r\n"
			+ "        </argument>\r\n" + "      </argumentList>\r\n" + "    </action>\r\n" + "    <action>\r\n"
			+ "      <name>GetProtocolInfo</name>\r\n" + "      <argumentList>\r\n" + "        <argument>\r\n"
			+ "          <name>Source</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>SourceProtocolInfo</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "        <argument>\r\n" + "          <name>Sink</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>SinkProtocolInfo</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "      </argumentList>\r\n" + "    </action>\r\n" + "    <action>\r\n"
			+ "      <name>GetCurrentConnectionIDs</name>\r\n" + "      <argumentList>\r\n" + "        <argument>\r\n"
			+ "          <name>ConnectionIDs</name>\r\n" + "          <direction>out</direction>\r\n"
			+ "          <relatedStateVariable>CurrentConnectionIDs</relatedStateVariable>\r\n" + "        </argument>\r\n"
			+ "      </argumentList>\r\n" + "    </action>\r\n" + "  </actionList>\r\n" + "  <serviceStateTable>\r\n"
			+ "    <stateVariable sendEvents=\"no\">\r\n" + "      <name>A_ARG_TYPE_ProtocolInfo</name>\r\n"
			+ "      <dataType>string</dataType>\r\n" + "    </stateVariable>\r\n"
			+ "    <stateVariable sendEvents=\"no\">\r\n" + "      <name>A_ARG_TYPE_ConnectionStatus</name>\r\n"
			+ "      <dataType>string</dataType>\r\n" + "      <allowedValueList>\r\n"
			+ "        <allowedValue>OK</allowedValue>\r\n" + "        <allowedValue>ContentFormatMismatch</allowedValue>\r\n"
			+ "        <allowedValue>InsufficientBandwidth</allowedValue>\r\n"
			+ "        <allowedValue>UnreliableChannel</allowedValue>\r\n" + "        <allowedValue>Unknown</allowedValue>\r\n"
			+ "      </allowedValueList>\r\n" + "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>A_ARG_TYPE_AVTransportID</name>\r\n" + "      <dataType>i4</dataType>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>A_ARG_TYPE_RcsID</name>\r\n" + "      <dataType>i4</dataType>\r\n" + "    </stateVariable>\r\n"
			+ "    <stateVariable sendEvents=\"no\">\r\n" + "      <name>A_ARG_TYPE_ConnectionID</name>\r\n"
			+ "      <dataType>i4</dataType>\r\n" + "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"no\">\r\n"
			+ "      <name>A_ARG_TYPE_ConnectionManager</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"yes\">\r\n"
			+ "      <name>SourceProtocolInfo</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"yes\">\r\n"
			+ "      <name>SinkProtocolInfo</name>\r\n" + "      <dataType>string</dataType>\r\n" + "    </stateVariable>\r\n"
			+ "    <stateVariable sendEvents=\"no\">\r\n" + "      <name>A_ARG_TYPE_Direction</name>\r\n"
			+ "      <dataType>string</dataType>\r\n" + "      <allowedValueList>\r\n"
			+ "        <allowedValue>Input</allowedValue>\r\n" + "        <allowedValue>Output</allowedValue>\r\n"
			+ "      </allowedValueList>\r\n" + "    </stateVariable>\r\n" + "    <stateVariable sendEvents=\"yes\">\r\n"
			+ "      <name>CurrentConnectionIDs</name>\r\n" + "      <dataType>string</dataType>\r\n"
			+ "    </stateVariable>\r\n" + "  </serviceStateTable>\r\n" + "</scpd>";
}
