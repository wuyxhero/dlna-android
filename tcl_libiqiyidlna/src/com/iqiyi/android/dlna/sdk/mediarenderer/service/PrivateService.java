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
package com.iqiyi.android.dlna.sdk.mediarenderer.service;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.ServiceInterface;
import org.cybergarage.upnp.StateVariable;
import org.cybergarage.upnp.control.ActionListener;
import org.cybergarage.upnp.control.QueryListener;
import org.cybergarage.upnp.device.InvalidDescriptionException;

import com.iqiyi.android.dlna.sdk.mediarenderer.MediaRenderer;
import com.iqiyi.android.dlna.sdk.mediarenderer.QiyiDLNAListener;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.PrivateServiceConstStr;

public class PrivateService extends Service implements ActionListener, QueryListener, ServiceInterface
{

	public PrivateService(MediaRenderer render)
	{
		setMediaRenderer(render);
		initService();
		setActionListener(this);
	}

	private QiyiDLNAListener qiyiDLNAListener = null;

	public QiyiDLNAListener getQiyiDLNAListener()
	{
		return qiyiDLNAListener;
	}

	public void setQiyiDLNAListener(QiyiDLNAListener qiyiDLNAListener)
	{
		this.qiyiDLNAListener = qiyiDLNAListener;
	}

	////////////////////////////////////////////////
	// MediaRender
	////////////////////////////////////////////////

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
	// ActionListener
	// //////////////////////////////////////////////

	public boolean actionControlReceived(Action action)
	{
		boolean isActionSuccess;

		String actionName = action.getName();

		if (actionName == null)
			return false;
		isActionSuccess = false;
		MediaRenderer dmr = getMediaRenderer();

		/*
		 * 发送私有消息
		 */
		if (actionName.equals(PrivateServiceConstStr.SEND_MESSAGE) == true)
		{
			int instanceID = action.getArgument(PrivateServiceConstStr.INSTANCE_ID).getIntegerValue();
			String infor = action.getArgument(PrivateServiceConstStr.INFOR).getValue();
			isActionSuccess = true;

			StringBuffer outResult = new StringBuffer();
			if (dmr != null)
			{
				QiyiDLNAListener qiyiDLNAListener = dmr.getQiyiDLNAListener();
				if (qiyiDLNAListener != null)
				{
					qiyiDLNAListener.onReceiveSendMessage(instanceID, infor, outResult);
				}
			}
			action.getArgument(PrivateServiceConstStr.RESULT).setValue(outResult.toString());
		}

		if (dmr != null)
		{
			ActionListener listener = dmr.getActionListener();
			if (listener != null)
				listener.actionControlReceived(action);
		}

		return isActionSuccess;
	}

	// //////////////////////////////////////////////
	// QueryListener
	// //////////////////////////////////////////////

	public boolean queryControlReceived(StateVariable stateVar)
	{
		return false;
	}

	@Override
	public void initService()
	{
		// 设置服务的描述地址，控制地址，订阅地址
		setServiceType(PrivateServiceConstStr.SERVICE_TYPE);
		setServiceID(PrivateServiceConstStr.SERVICE_ID);
		setControlURL(PrivateServiceConstStr.CONTROL_URL);
		setSCPDURL(PrivateServiceConstStr.SCPDURL);
		setEventSubURL(PrivateServiceConstStr.EVENTSUB_URL);

		try
		{
			loadSCPD(PrivateServiceConstStr.SCPD);
		} catch (InvalidDescriptionException e)
		{
			e.printStackTrace();
		}
	}

}
