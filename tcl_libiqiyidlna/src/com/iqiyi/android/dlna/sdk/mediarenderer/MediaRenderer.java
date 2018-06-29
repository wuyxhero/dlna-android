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

import java.io.*;

import org.cybergarage.net.*;
import org.cybergarage.util.*;
import org.cybergarage.upnp.*;
import org.cybergarage.upnp.control.ActionListener;
import org.cybergarage.upnp.device.*;

import com.iqiyi.android.dlna.sdk.SDKVersion;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.AVTransport;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.ConnectionManager;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.PrivateService;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.QPlayService;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.RenderingControl;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.AVTransportConstStr;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.ConnectionManagerConstStr;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.PrivateServiceConstStr;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.RenderingControlConstStr;

public class MediaRenderer extends Device
{
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////
	public final static String DEVICE_TYPE = "urn:schemas-upnp-org:device:MediaRenderer:1";

	//爱奇艺的独有的设备，要防止别人能够扫描到
	public final static String DEVICE_IQIYIRENDERER_TYPE = "urn:schemas-upnp-org:device:IQIYIMediaRenderer:1";
	public final static String DMR_VERSION = "DMR-1.50";

	public final static int DEFAULT_HTTP_PORT = 39620;
	public final static int DEFAULT_HTTP_PORT_EXTRA = 39640;

	//异步消息通知状态变量
	private final static String notifyStateVar = "A_ARG_TYPE_NOTIFYMSG";
	private StateVariable notifyStateVariable = null;

	//私有服务
	private Service privateServer = null;

	////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////
	public MediaRenderer(String descriptionFileName) throws InvalidDescriptionException
	{
		super(new File(descriptionFileName));
		Debug.message("SDK VERSION: " + SDKVersion.getSDKVersion());
		initialize();
	}

	/*
	 * 初始化版本号
	 */
	public MediaRenderer(int major, int minor)
	{
		super(major, minor);
		clear();
		setDeviceType(DEVICE_TYPE);
		//setDeviceType(DEVICE_IQIYIRENDERER_TYPE);
		setDLNADOC(DMR_VERSION);
	}

	public MediaRenderer()
	{
		super(1, 0);
		clear();
		setDeviceType(DEVICE_TYPE);
		setDLNADOC(DMR_VERSION);
	}

	private LastChangeListener mLastChangeListener = new DMRLastChangeListener();

	public final LastChangeListener getLastChangeListener()
	{
		return mLastChangeListener;
	}

	/*
	 * 标准的DLNA监听接口
	 */
	private StandardDLNAListener mStandardDLNAListener = null;

	public StandardDLNAListener getStandardDLNAListener()
	{
		return mStandardDLNAListener;
	}

	public void setStandardDLNAListener(StandardDLNAListener listener)
	{
		mStandardDLNAListener = listener;
	}

	/*
	 * 奇艺的DLNA监听接口
	 */
	private QiyiDLNAListener mQiyiDLNAListener = null;

	public QiyiDLNAListener getQiyiDLNAListener()
	{
		return mQiyiDLNAListener;
	}

	public void setQiyiDLNAListener(QiyiDLNAListener listener)
	{
		mQiyiDLNAListener = listener;
	}

	public void initialize(String uuid)
	{
		// Networking initialization
		UPnP.setEnable(UPnP.USE_ONLY_IPV4_ADDR);
		String firstIf = HostInterface.getHostAddress(0);
		setInterfaceAddress(firstIf);
		if (getPackageName().length() > 1)
			setHTTPPort(DEFAULT_HTTP_PORT_EXTRA);
		else
			setHTTPPort(DEFAULT_HTTP_PORT);

		if (uuid == null || uuid.isEmpty())
		{
			setUUID(UPnP.createUUID());
			setUDN(getUUID());
		} else
		{
			setUUID(uuid);
			//必须对udn进行处理，不然的话，fileMd5就有问题
			setUDN(getUUID());
		}
		if (hasUDN() == false)
			updateUDN();

		renCon = new RenderingControl(this);
		conMan = new ConnectionManager(this);
		avTrans = new AVTransport(this);
		privateService = new PrivateService(this);
		qplayService = new QPlayService(this);

		// 添加服务
		addService(renCon);
		addService(conMan);
		addService(avTrans);
		addService(privateService);
		addService(qplayService);
	}

	public void initialize()
	{
		// Networking initialization
		UPnP.setEnable(UPnP.USE_ONLY_IPV4_ADDR);
		String firstIf = HostInterface.getHostAddress(0);
		setInterfaceAddress(firstIf);
		if (getPackageName().length() > 1)
			setHTTPPort(DEFAULT_HTTP_PORT_EXTRA);
		else
			setHTTPPort(DEFAULT_HTTP_PORT);

		setUUID(UPnP.createUUID());
		setUDN(getUUID());

		if (hasUDN() == false)
			updateUDN();

		renCon = new RenderingControl(this);
		conMan = new ConnectionManager(this);
		avTrans = new AVTransport(this);
		privateService = new PrivateService(this);
		qplayService = new QPlayService(this);

		//添加服务
		addService(renCon);
		addService(conMan);
		addService(avTrans);
		addService(privateService);
		addService(qplayService);
	}

	////////////////////////////////////////////////
	// Memeber
	////////////////////////////////////////////////
	private ConnectionManager conMan;
	private RenderingControl renCon;
	private AVTransport avTrans;
	private PrivateService privateService;
	private QPlayService qplayService;

	public ConnectionManager getConnectionManager()
	{
		return conMan;
	}

	public RenderingControl getRenderingControl()
	{
		return renCon;
	}

	public AVTransport getAVTransport()
	{
		return avTrans;
	}

	public PrivateService getPrivateService()
	{
		return privateService;
	}

	public QPlayService getQPlayService()
	{
		return qplayService;
	}

	////////////////////////////////////////////////
	// HostAddress
	////////////////////////////////////////////////
	public void setInterfaceAddress(String ifaddr)
	{
		HostInterface.setInterface(ifaddr);
	}

	public String getInterfaceAddress()
	{
		return HostInterface.getInterface();
	}

	////////////////////////////////////////////////
	// Action Listener
	////////////////////////////////////////////////
	private ActionListener actionListener;

	public void setActionListener(ActionListener listener)
	{
		actionListener = listener;
	}

	public ActionListener getActionListener()
	{
		return actionListener;
	}

	public synchronized boolean start()
	{
		Debug.message("MediaRenderer start SDK VERSION: " + SDKVersion.getSDKVersion());
		boolean ret = super.start();
		Debug.message("MediaRenderer start SDK VERSION: " + SDKVersion.getSDKVersion() + " [Done] ret=" + ret);
		return ret;
	}

	public synchronized boolean stop()
	{
		Debug.message("MediaRenderer stop SDK VERSION: " + SDKVersion.getSDKVersion());
		boolean ret = super.stop();

		/*
		 * 关闭订阅通知线程
		 */
		if (privateService != null)
		{
			privateService.stopNotifyThreads();
		}

		if (qplayService != null)
		{
			qplayService.stopNotifyThreads();
		}

		if (avTrans != null)
		{
			avTrans.stopNotifyThreads();
		}

		if (conMan != null)
		{
			conMan.stopNotifyThreads();
		}

		if (renCon != null)
		{
			renCon.stopNotifyThreads();
		}

		Debug.message("MediaRenderer stop SDK VERSION: " + SDKVersion.getSDKVersion() + " [Done] ret=" + ret);
		return ret;
	}

	/*
	 * DMR调用这个接口
	 * 给DMC端通知消息
	 * notifyMsg为需要通知的内容
	 */
	public void NotifyMessage(String notifyMsg)
	{
		if (notifyStateVariable == null)
		{
			privateServer = getService(PrivateServiceConstStr.SERVICE_TYPE);
			if (privateServer != null)
			{
				ServiceStateTable stateTable = privateServer.getServiceStateTable();
				int tableSize = stateTable.size();
				for (int n = 0; n < tableSize; n++)
				{
					StateVariable var = stateTable.getStateVariable(n);
					//找到对应的状态列表
					if (var.getStateVariableNode().getNodeValue("name").compareTo(notifyStateVar) == 0)
					{
						notifyStateVariable = var;
						Debug.message("NofityMessage sub send: " + notifyMsg);
						var.setValue(notifyMsg, false);
						break;
					}
				}
			}
		} else
		//直接通知即可
		{
			notifyStateVariable.setValue(notifyMsg, false);
		}
	}

	public void NotifyMessage(String notifyMsg, boolean external)
	{
		if (notifyStateVariable == null)
		{
			privateServer = getService(PrivateServiceConstStr.SERVICE_TYPE);
			if (privateServer != null)
			{
				ServiceStateTable stateTable = privateServer.getServiceStateTable();
				int tableSize = stateTable.size();
				for (int n = 0; n < tableSize; n++)
				{
					StateVariable var = stateTable.getStateVariable(n);
					//找到对应的状态列表
					if (var.getStateVariableNode().getNodeValue("name").compareTo(notifyStateVar) == 0)
					{
						notifyStateVariable = var;
						Debug.message("NofityMessage sub send: " + notifyMsg);
						var.setValue(notifyMsg, external);
						break;
					}
				}
			}
		} else
		{
			//直接通知即可
			notifyStateVariable.setValue(notifyMsg, external);
		}
	}

	final class DMRLastChangeListener implements LastChangeListener
	{

		@Override
		public void lastChange(String service, String name, Object value)
		{
			Debug.message("lastChange() service: " + service + " name: " + name + " value: " + value);

			if (isAVTransportService(service))
			{
				AVTransport transportService = getAVTransport();
				transportService.getStateVariable(name).setValue((String) value, false);

			} else if (isConnectionManagerService(service))
			{
				ConnectionManager conneManager = getConnectionManager();
				conneManager.getStateVariable(name).setValue((String) value, false);

			} else if (isRenderingControlService(service))
			{
				RenderingControl renderControl = getRenderingControl();
				renderControl.getStateVariable(name).setValue((String) value, false);
			}
		}

	}

	private boolean isAVTransportService(String service)
	{
		return (AVTransportConstStr.SERVICE_TYPE.equals(service));
	}

	private boolean isConnectionManagerService(String service)
	{
		return (ConnectionManagerConstStr.SERVICE_TYPE.equals(service));
	}

	private boolean isRenderingControlService(String service)
	{
		return (RenderingControlConstStr.SERVICE_TYPE.equals(service));
	}

	/**
	 * client notify the DMR of it's params changing.
	 * 
	 * @author QIYI
	 * 
	 */
	public interface LastChangeListener
	{

		public void lastChange(String service, String name, Object value);

	}

	private QPlayListener qplayListener = null;

	public QPlayListener getQPlayListener()
	{
		return qplayListener;
	}

	public void setQPlayListener(QPlayListener listener)
	{
		qplayListener = listener;
	}
}
