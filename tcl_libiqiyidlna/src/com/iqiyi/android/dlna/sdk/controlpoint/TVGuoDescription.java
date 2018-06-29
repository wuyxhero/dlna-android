package com.iqiyi.android.dlna.sdk.controlpoint;

import org.cybergarage.util.Debug;

public class TVGuoDescription
{
	public static final String TVGuoSCPD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
			+ "<scpd xmlns=\"urn:schemas-upnp-org:service-1-0\">\n" + "   <specVersion>\n" + "      <major>1</major>\n"
			+ "      <minor>0</minor>\n" + "   </specVersion>\n" + "   <serviceStateTable>\n"
			+ "      <stateVariable sendEvents=\"no\">\n" + "         <name>A_ARG_TYPE_InstanceID</name>\n"
			+ "         <dataType>ui4</dataType>\n" + "      </stateVariable>\n" + "      <stateVariable sendEvents=\"yes\">\n"
			+ "         <name>A_ARG_TYPE_NOTIFYMSG</name>\n" + "         <dataType>string</dataType>\n"
			+ "      </stateVariable>\n" + "      <stateVariable sendEvents=\"no\">\n"
			+ "         <name>A_ARG_TYPE_INFOR</name>\n" + "         <dataType>string</dataType>\n"
			+ "      </stateVariable>\n" + "      <stateVariable sendEvents=\"no\">\n"
			+ "         <name>A_ARG_TYPE_SendMessage_Result</name>\n" + "         <dataType>string</dataType>\n"
			+ "      </stateVariable>\n" + "   </serviceStateTable>\n" + "   <actionList>\n" + "      <action>\n"
			+ "         <name>SendMessage</name>\n" + "         <argumentList>\n" + "            <argument>\n"
			+ "               <name>InstanceID</name>\n" + "               <direction>in</direction>\n"
			+ "               <relatedStateVariable>A_ARG_TYPE_InstanceID</relatedStateVariable>\n"
			+ "            </argument>\n" + "            <argument>\n" + "               <name>Infor</name>\n"
			+ "               <direction>in</direction>\n"
			+ "               <relatedStateVariable>A_ARG_TYPE_INFOR</relatedStateVariable>\n" + "            </argument>\n"
			+ "            <argument>\n" + "               <name>Result</name>\n"
			+ "               <direction>out</direction>\n"
			+ "               <relatedStateVariable>A_ARG_TYPE_SendMessage_Result</relatedStateVariable>\n"
			+ "            </argument>\n" + "         </argumentList>\n" + "      </action>\n" + "      <action>\n"
			+ "         <name>NotifyMessage</name>\n" + "         <argumentList>\n" + "            <argument>\n"
			+ "               <name>NotifyMsg</name>\n" + "               <direction>in</direction>\n"
			+ "               <relatedStateVariable>A_ARG_TYPE_NOTIFYMSG</relatedStateVariable>\n"
			+ "            </argument>\n" + "         </argumentList>\n" + "      </action>\n" + "   </actionList>\n"
			+ "</scpd>\n";

	public static final String TVGuoDescription = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
			+ "<root xmlns=\"urn:schemas-upnp-org:device-1-0\" xmlns:dlna=\"urn:schemas-dlna-org:device-1-0\">\n"
			+ "   <specVersion>\n" + "      <major>1</major>\n" + "      <minor>0</minor>\n" + "   </specVersion>\n"
			+ "   <device>\n" + "      <friendlyName></friendlyName>\n"
			+ "      <deviceType>urn:schemas-upnp-org:device:MediaRenderer:1</deviceType>\n"
			+ "      <dlna:X_DLNADOC xmlns:dlna=\"urn:schemas-dlna-org:device-1-0\">DMR-1.50</dlna:X_DLNADOC>\n"
			+ "      <manufacturer>Microsoft Corporation</manufacturer>\n"
			+ "      <manufacturerURL>http://www.microsoft.com</manufacturerURL>\n"
			+ "      <modelDescription>TVGUO Media Renderer</modelDescription>\n"
			+ "      <modelName>Windows Media Player</modelName>\n"
			+ "      <modelURL>http://go.microsoft.com/fwlink/?LinkId=105927</modelURL>\n" + "      <iconList>\n"
			+ "         <icon>\n" + "            <mimetype>image/png</mimetype>\n" + "            <width>94</width>\n"
			+ "            <height>94</height>\n" + "            <depth>8</depth>\n"
			+ "            <url>dongle_icon.png</url>\n" + "         </icon>\n" + "      </iconList>\n"
			+ "      <serviceList>\n" + "         <service>\n"
			+ "            <serviceType>urn:schemas-upnp-org:service:RenderingControl:1</serviceType>\n"
			+ "            <serviceId>urn:upnp-org:serviceId:RenderingControl</serviceId>\n"
			+ "            <controlURL>_urn:schemas-upnp-org:service:RenderingControl_control</controlURL>\n"
			+ "            <SCPDURL>_urn:schemas-upnp-org:service:RenderingControl_scpd.xml</SCPDURL>\n"
			+ "            <eventSubURL>_urn:schemas-upnp-org:service:RenderingControl_event</eventSubURL>\n"
			+ "         </service>\n" + "         <service>\n"
			+ "            <serviceType>urn:schemas-upnp-org:service:ConnectionManager:1</serviceType>\n"
			+ "            <serviceId>urn:upnp-org:serviceId:ConnectionManager</serviceId>\n"
			+ "            <SCPDURL>_urn:schemas-upnp-org:service:ConnectionManager_scpd.xml</SCPDURL>\n"
			+ "            <controlURL>_urn:schemas-upnp-org:service:ConnectionManager_control</controlURL>\n"
			+ "            <eventSubURL>_urn:schemas-upnp-org:service:ConnectionManager_event</eventSubURL>\n"
			+ "         </service>\n" + "         <service>\n"
			+ "            <serviceType>urn:schemas-upnp-org:service:AVTransport:1</serviceType>\n"
			+ "            <serviceId>urn:upnp-org:serviceId:AVTransport</serviceId>\n"
			+ "            <controlURL>_urn:schemas-upnp-org:service:AVTransport_control</controlURL>\n"
			+ "            <SCPDURL>_urn:schemas-upnp-org:service:AVTransport_scpd.xml</SCPDURL>\n"
			+ "            <eventSubURL>_urn:schemas-upnp-org:service:AVTransport_event</eventSubURL>\n"
			+ "         </service>\n" + "         <service>\n"
			+ "            <serviceType>urn:schemas-upnp-org:service:PrivateServer:1</serviceType>\n"
			+ "            <serviceId>urn:upnp-org:serviceId:PrivateServer</serviceId>\n"
			+ "            <controlURL>_urn:schemas-upnp-org:service:PrivateServer_control</controlURL>\n"
			+ "            <SCPDURL>_urn:schemas-upnp-org:service:PrivateServer_scpd.xml</SCPDURL>\n"
			+ "            <eventSubURL>_urn:schemas-upnp-org:service:PrivateServer_event</eventSubURL>\n"
			+ "         </service>\n" + "      </serviceList>\n" + "      <UDN></UDN>\n"
			+ "      <modelNumber>1234</modelNumber>\n" + "      <PackageName></PackageName>\n" + "   </device>\n" + "</root>";

	public static String construct(String friendlyName, String uuid)
	{
		Debug.message("TVGuoDescription construct " + friendlyName + " " + uuid);
		return TVGuoDescription.replace("<friendlyName></friendlyName>", "<friendlyName>" + friendlyName + "</friendlyName>")
				.replace("<UDN></UDN>", "<UDN>" + uuid + "</UDN>");
	}
}
