/******************************************************************
 *
 *	MediaServer for CyberLink
 *
 *	Copyright (C) Satoshi Konno 2003
 *
 *	File : ConnectionManager.java
 *
 *	Revision:
 *
 *	02/22/08
 *		- first revision.
 *
 ******************************************************************/

package com.iqiyi.android.dlna.sdk.mediarenderer.service;

import org.cybergarage.util.*;
import org.cybergarage.upnp.*;
import org.cybergarage.upnp.control.*;
import org.cybergarage.upnp.device.InvalidDescriptionException;

import com.iqiyi.android.dlna.sdk.mediarenderer.MediaRenderer;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.ConnectionInfo;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.ConnectionInfoList;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.ConnectionManagerConstStr;

public class ConnectionManager extends Service implements ActionListener, QueryListener, ServiceInterface
{

	public ConnectionManager(MediaRenderer render)
	{
		maxConnectionID = 0;
		setMediaRenderer(render);
		initService();
		setActionListener(this);
	}

	private MediaRenderer mediaRenderer;

	private void setMediaRenderer(MediaRenderer render)
	{
		mediaRenderer = render;
	}

	public MediaRenderer getMediaRenderer()
	{
		return mediaRenderer;
	}

	private int maxConnectionID;

	public synchronized int getNextConnectionID()
	{
		maxConnectionID++;
		return maxConnectionID;
	}

	private ConnectionInfoList conInfoList = new ConnectionInfoList();;

	public ConnectionInfoList getConnectionInfoList()
	{
		return conInfoList;
	}

	public ConnectionInfo getConnectionInfo(int id)
	{
		int size = conInfoList.size();
		for (int n = 0; n < size; n++)
		{
			ConnectionInfo info = conInfoList.getConnectionInfo(n);
			if (info.getID() == id)
				return info;
		}
		return null;
	}

	public synchronized void addConnectionInfo(ConnectionInfo info)
	{
		conInfoList.add(info);
	}

	public synchronized void removeConnectionInfo(int id)
	{
		int size = conInfoList.size();
		for (int n = 0; n < size; n++)
		{
			ConnectionInfo info = conInfoList.getConnectionInfo(n);
			if (info.getID() == id)
			{
				conInfoList.remove(info);
				break;
			}
		}
	}

	public synchronized void removeConnectionInfo(ConnectionInfo info)
	{
		conInfoList.remove(info);
	}

	public boolean actionControlReceived(Action action)
	{
		boolean isActionSuccess = false;

		String actionName = action.getName();
		if (actionName == null)
		{
			Debug.message("[Error] CM action: actionName == null");
			action.setStatus(UPnPStatus.INVALID_ACTION);
			return false;
		}

		if (ConnectionManagerConstStr.GETPROTOCOLINFO.equals(actionName))
		{
			Debug.message("Process CM actionControlReceived() action: " + actionName);
			String sinkProtocol = "http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_PRO,http-get:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_SP_G726,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_FULL,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_MED,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_BASE,http-get:*:audio/L16;rate=44100;channels=1:DLNA.ORG_PN=LPCM,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_PAL,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_NTSC,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVHIGH_PRO,http-get:*:audio/L16;rate=44100;channels=2:DLNA.ORG_PN=LPCM,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_SM,http-get:*:video/x-ms-asf:DLNA.ORG_PN=VC1_ASF_AP_L1_WMA,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMDRM_WMABASE,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVHIGH_FULL,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMAFULL,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMABASE,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPLL_BASE,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_NTSC_XAC3,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMDRM_WMVSPLL_BASE,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_BASE,http-get:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_ASP_L5_SO_G726,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_LRG,http-get:*:audio/mpeg:DLNA.ORG_PN=MP3,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_PAL_XAC3,http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMAPRO,http-get:*:video/mpeg:DLNA.ORG_PN=MPEG1,http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN,http-get:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_ASP_L4_SO_G726,http-get:*:audio/L16;rate=48000;channels=2:DLNA.ORG_PN=LPCM,http-get:*:audio/mpeg:DLNA.ORG_PN=MP3X,http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_MP3,http-get:*:video/x-ms-wmv:*,http-get:*:image/png:*,http-get:*:audio/mp3:*,http-get:*:audio/mpeg:*,http-get:*:audio/mpeg3:*,http-get:*:video/mp4:*,http-get:*:video/avi:*";
			action.getArgument(ConnectionManagerConstStr.SINK).setValue(sinkProtocol);

			isActionSuccess = true;
		} else if (ConnectionManagerConstStr.GETCURRENTCONNECTIONIDS.equals(actionName))
		{
			Debug.message("Process CM actionControlReceived() action: " + actionName);
			action.getArgument(ConnectionManagerConstStr.CONNECTIONIDS).setValue("0");

			isActionSuccess = true;
		} else if (ConnectionManagerConstStr.GETCURRENTCONNECTIONINFO.equals(actionName))
		{
			Debug.message("Process CM actionControlReceived() action: " + actionName);
			action.getArgument(ConnectionManagerConstStr.RCSID).setValue("0");
			action.getArgument(ConnectionManagerConstStr.AVTRANSPORTID).setValue("0");
			action.getArgument(ConnectionManagerConstStr.PEERCONNECTIONMANAGER).setValue("");
			action.getArgument(ConnectionManagerConstStr.PEERCONNECTIONID).setValue("-1");
			action.getArgument(ConnectionManagerConstStr.DIRECTION).setValue("Input");
			action.getArgument(ConnectionManagerConstStr.STATUS).setValue("Unknown");

			isActionSuccess = true;
		} else
		{
			Debug.message("Unknown CM actionControlReceived() action: " + actionName);
			action.setStatus(UPnPStatus.INVALID_ACTION);
		}

		MediaRenderer dmr = getMediaRenderer();
		if (dmr != null)
		{
			ActionListener listener = dmr.getActionListener();
			if (listener != null)
				listener.actionControlReceived(action);
		}

		return isActionSuccess;
	}

	//	private synchronized boolean getCurrentConnectionIDs(Action action)
	//	{
	//		String conIDs = "";
	//
	//		int size = conInfoList.size();
	//		for (int n = 0; n < size; n++)
	//		{
	//			ConnectionInfo info = conInfoList.getConnectionInfo(n);
	//			if (0 < n)
	//				conIDs += ",";
	//			conIDs += Integer.toString(info.getID());
	//		}
	//		action.getArgument(ConnectionManagerConstStr.CONNECTIONIDS).setValue(conIDs);
	//
	//		return true;
	//	}
	//
	//	private synchronized boolean getCurrentConnectionInfo(Action action)
	//	{
	//		int id = action.getArgument(ConnectionManagerConstStr.RCSID).getIntegerValue();
	//		ConnectionInfo info = getConnectionInfo(id);
	//		if (info != null)
	//		{
	//			action.getArgument(ConnectionManagerConstStr.RCSID).setValue(info.getRcsID());
	//			action.getArgument(ConnectionManagerConstStr.AVTRANSPORTID).setValue(info.getAVTransportID());
	//			action.getArgument(ConnectionManagerConstStr.PEERCONNECTIONMANAGER).setValue(info.getPeerConnectionManager());
	//			action.getArgument(ConnectionManagerConstStr.PEERCONNECTIONID).setValue(info.getPeerConnectionID());
	//			action.getArgument(ConnectionManagerConstStr.DIRECTION).setValue(info.getDirection());
	//			action.getArgument(ConnectionManagerConstStr.STATUS).setValue(info.getStatus());
	//		} else
	//		{
	//			action.getArgument(ConnectionManagerConstStr.RCSID).setValue(-1);
	//			action.getArgument(ConnectionManagerConstStr.AVTRANSPORTID).setValue(-1);
	//			action.getArgument(ConnectionManagerConstStr.PEERCONNECTIONMANAGER).setValue("");
	//			action.getArgument(ConnectionManagerConstStr.PEERCONNECTIONID).setValue(-1);
	//			action.getArgument(ConnectionManagerConstStr.DIRECTION).setValue(ConnectionInfo.OUTPUT);
	//			action.getArgument(ConnectionManagerConstStr.STATUS).setValue(ConnectionInfo.UNKNOWN);
	//		}
	//
	//		return true;
	//	}

	public boolean queryControlReceived(StateVariable stateVar)
	{
		return false;
	}

	@Override
	public void initService()
	{
		setServiceType(ConnectionManagerConstStr.SERVICE_TYPE);
		setServiceID(ConnectionManagerConstStr.SERVICE_ID);
		setSCPDURL(ConnectionManagerConstStr.SCPDURL);
		setControlURL(ConnectionManagerConstStr.CONTROL_URL);
		setEventSubURL(ConnectionManagerConstStr.EVENTSUB_URL);
		try
		{
			loadSCPD(ConnectionManagerConstStr.SCPD);
		} catch (InvalidDescriptionException e)
		{
			e.printStackTrace();
		}
	}
}
