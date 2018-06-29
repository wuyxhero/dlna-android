/******************************************************************
 *
 *	MediaServer for CyberLink
 *
 *	Copyright (C) Satoshi Konno 2003
 *
 *	File : RenderingControl.java
 *
 *	Revision:
 *
 *	02/22/08
 *		- first revision.
 *
 ******************************************************************/

package com.iqiyi.android.dlna.sdk.mediarenderer.service;

import org.cybergarage.http.HTTPHeader;
import org.cybergarage.util.*;
import org.cybergarage.upnp.*;
import org.cybergarage.upnp.control.*;
import org.cybergarage.upnp.device.InvalidDescriptionException;

import com.iqiyi.android.dlna.sdk.mediarenderer.MediaRenderer;
import com.iqiyi.android.dlna.sdk.mediarenderer.StandardDLNAListener;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.RenderingControlConstStr;

public class RenderingControl extends Service implements ActionListener, QueryListener, ServiceInterface
{
	// //////////////////////////////////////////////
	// Constructor
	// //////////////////////////////////////////////
	private StateVariable lastChangenotifyStateVariable = null;

	public RenderingControl(MediaRenderer render)
	{
		setMediaRenderer(render);
		initService();
		setActionListener(this);
	}

	// //////////////////////////////////////////////
	// MediaRender
	// //////////////////////////////////////////////

	private MediaRenderer mediaRenderer;

	private void setMediaRenderer(MediaRenderer render)
	{
		mediaRenderer = render;
	}

	public MediaRenderer getMediaRenderer()
	{
		return mediaRenderer;
	}

	// //////////////////////////////////////////////
	// Mutex
	// //////////////////////////////////////////////

	private Mutex mutex = new Mutex();

	public void lock()
	{
		mutex.lock();
	}

	public void unlock()
	{
		mutex.unlock();
	}

	private int mSohuVolume = 0;

	// //////////////////////////////////////////////
	// ActionListener
	// //////////////////////////////////////////////

	public boolean actionControlReceived(Action action)
	{
		boolean isActionSuccess = false;

		String actionName = action.getName();
		if (actionName == null)
		{
			Debug.message("[Error] RC action: actionName == null");
			action.setStatus(UPnPStatus.INVALID_ACTION);
			return false;
		}

		MediaRenderer dmr = getMediaRenderer();
		StandardDLNAListener standardDLNAListener = null;
		if (dmr != null)
		{
			standardDLNAListener = dmr.getStandardDLNAListener();
		}

		if (actionName.equals(RenderingControlConstStr.GETMUTE) == true)
		{
			// 获取声音状态
			Debug.message("Process RC actionControlReceived() action: " + actionName);
			int instanceID = action.getArgument(RenderingControlConstStr.INSTANCEID).getIntegerValue();
			String channel = action.getArgument(RenderingControlConstStr.CHANNEL).getValue();

			Boolean outCurrentMute = false;
			if (standardDLNAListener != null)
			{
				outCurrentMute = standardDLNAListener.GetMute(instanceID, channel);
			}

			action.getArgument(RenderingControlConstStr.CURRENTMUTE).setValue(outCurrentMute == true ? 1 : 0);

			isActionSuccess = true;
		} else if (actionName.equals(RenderingControlConstStr.GETVOLUME) == true)
		{
			// 获取音量
			Debug.message("Process RC actionControlReceived() action: " + actionName);
			int instanceID = action.getArgument(RenderingControlConstStr.INSTANCEID).getIntegerValue();
			String channel = action.getArgument(RenderingControlConstStr.CHANNEL).getValue();

			Integer outCurrentVolume = 0;
			if (standardDLNAListener != null)
			{
				outCurrentVolume = standardDLNAListener.GetVolume(instanceID, channel);
			}

			action.getArgument(RenderingControlConstStr.CURRENTVOLUME).setValue(outCurrentVolume);

			isActionSuccess = true;
		} else if (actionName.equals(RenderingControlConstStr.SETMUTE) == true)
		{
			// 设置声音状态
			Debug.message("Process RC actionControlReceived() action: " + actionName);
			int instanceID = action.getArgument(RenderingControlConstStr.INSTANCEID).getIntegerValue();
			String channel = action.getArgument(RenderingControlConstStr.CHANNEL).getValue();
			int desireMute = action.getArgument(RenderingControlConstStr.DESIREDMUTE).getIntegerValue();

			if (standardDLNAListener != null)
			{
				standardDLNAListener.SetMute(instanceID, channel, desireMute == 0 ? false : true);
			}

			isActionSuccess = true;
		} else if (actionName.equals(RenderingControlConstStr.SETVOLUME) == true)
		{
			// 设置声音音量
			Debug.message("Process RC actionControlReceived() action: " + actionName);
			int instanceID = action.getArgument(RenderingControlConstStr.INSTANCEID).getIntegerValue();
			String channel = action.getArgument(RenderingControlConstStr.CHANNEL).getValue();
			int desiredVolume = action.getArgument(RenderingControlConstStr.DESIREDVOLUME).getIntegerValue();

			if (standardDLNAListener != null)
			{
				HTTPHeader reqAgent = action.getCurActionReq().getHeader("User-Agent");
				if(reqAgent != null && reqAgent.getValue().contains("SOHUVideo")) {
					if (desiredVolume == 0) {
						mSohuVolume = 0;
					}
					if (mSohuVolume < desiredVolume) {
						Debug.message("Sohu volumeUp");
						standardDLNAListener.SetVolume(instanceID, "volumeUp", desiredVolume);
					} else if (mSohuVolume > desiredVolume) {
						Debug.message("Sohu volumeDown");
						standardDLNAListener.SetVolume(instanceID, "volumeDown", desiredVolume);
					}
					mSohuVolume = desiredVolume;
				} else {
					standardDLNAListener.SetVolume(instanceID, channel, desiredVolume);
				}
			}

			isActionSuccess = true;
		} else
		{
			Debug.message("Unknown RC actionControlReceived() action: " + actionName);
			action.setStatus(UPnPStatus.INVALID_ACTION);
		}

		if (dmr != null)
		{
			ActionListener listener = dmr.getActionListener();
			if (listener != null)
				listener.actionControlReceived(action);
		}

		return isActionSuccess;
	}

	//给Qplay调节声音同步作的notify反馈
	public void setVolumeState(int volume, boolean mute)
	{
		SendLastChangeEvent(volume, mute);
	}

	//RenderingControl(RCS)实现lastChange这一块,之前在AVtransport里面实现,Qplay音量调节会判断是否从RenderingControl发出
	private void SendLastChangeEvent(int volume, boolean mute)
	{
		Debug.message("SendLastChangeEvent: " + "setVolumeState");
		ServiceStateTable stateTable = this.getServiceStateTable();
		String lastChangeExpected = null;
		String mMute = mute ? "1" : "0";
		String mVolume = String.valueOf(volume);
		lastChangeExpected = "<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/RCS/\">" + "<InstanceID val=\"0\">"
				+ "<Volume channel=\"" + "Master\"" + " val=\"" + mVolume + "\"/>" + "<Mute channel=\"" + "Master\""
				+ " val=\"" + mMute + "\"/>" + "<PresetNameList val=\"" + "FactoryDefaults" + "\"/>"
				+ "<A_ARG_TYPE_PresetName val=\"" + "FactoryDefaults" + "\"/>" + "</InstanceID>" + "</Event>";
		if (lastChangenotifyStateVariable == null)
		{
			int tableSize = stateTable.size();
			for (int n = 0; n < tableSize; n++)
			{
				StateVariable var = stateTable.getStateVariable(n);
				if (var.getStateVariableNode().getNodeValue("name").equals(RenderingControlConstStr.LASTCHANGE))
				{
					lastChangenotifyStateVariable = var;
					lastChangenotifyStateVariable.setValue(lastChangeExpected, false);
					break;
				}
			}
		} else
		{
			lastChangenotifyStateVariable.setValue(lastChangeExpected, false);
		}
	}

	public boolean queryControlReceived(StateVariable stateVar)
	{
		return false;
	}

	@Override
	public void initService()
	{
		// 设置服务的描述地址，控制地址，订阅地址
		setServiceType(RenderingControlConstStr.SERVICE_TYPE);
		setServiceID(RenderingControlConstStr.SERVICE_ID);
		setControlURL(RenderingControlConstStr.CONTROL_URL);
		setSCPDURL(RenderingControlConstStr.SCPDURL);
		setEventSubURL(RenderingControlConstStr.EVENTSUB_URL);

		try
		{
			loadSCPD(RenderingControlConstStr.SCPD);
		} catch (InvalidDescriptionException e)
		{
			e.printStackTrace();
		}
	}
}
