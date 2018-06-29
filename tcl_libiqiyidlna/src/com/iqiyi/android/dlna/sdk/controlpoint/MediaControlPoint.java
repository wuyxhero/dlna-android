/*******************************************************
 * Copyright (C) 2014 iQIYI.COM - All Rights Reserved
 * 
 * This file is part of {IQIYI_DLAN}.
 * Unauthorized copy of this file, via any medium is strictly prohibited.
 * Proprietary and Confidential.
 * 
 * Author(s): chenjiebin<chenjiebin@qiyi.com> maning<maning@qiyi.com>
                        wanjia<wanjia@qiyi.com> zhaotengfei<zhaotengfei@qiyi.com>
 * 
 *******************************************************/
package com.iqiyi.android.dlna.sdk.controlpoint;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringTokenizer;

import junit.framework.Assert;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.util.Enumeration;
import java.net.NetworkInterface;
import java.net.SocketException;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.NETWORK_STATUS;
import org.cybergarage.upnp.NetworkMonitor;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.device.DeviceChangeListener;
import org.cybergarage.upnp.event.EventListener;
import org.cybergarage.util.Debug;

import android.text.TextUtils;

import com.iqiyi.android.dlna.sdk.SDKVersion;
import com.iqiyi.android.dlna.sdk.controlpoint.qimohttpserver.SimpleWebServer;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.PrivateServiceConstStr;

/*
 * 控制点类，对mediarenderer和mediaserver进行控制
 */
public class MediaControlPoint extends ControlPoint implements DeviceChangeListener, IController
{

	private DeviceChangeListener deviceChangeListener = null;
	private NotifyMessageListener mNotifyMessageListener = null;

	private final long SUBSCRIBED_TIMEOUT = 180;//订阅三分钟

	private static Thread mServerThread;
	private static PipedOutputStream mStdIn = null;
	private int mQimoHttpServerPort = 9090;
	private final static int QIMOHTTPRETRYTIME = 5;
	private String HTTPSTRING = "http://";

	private EventListener mEventListener = new EventListener()
	{
		@Override
		public void eventNotifyReceived(String uuid, long seq, String varName, String value)
		{

			if (varName.equals(PrivateServiceConstStr.A_ARG_TYPE_NOTIFYMSG))
			{
				Debug.message("sub: receive " + uuid + " message [" + value + "]");
				if (mNotifyMessageListener == null)
					return;
				mNotifyMessageListener.onReceiveMessage(getSubscriber(uuid), value);
			}
		}
	};

	public MediaControlPoint()
	{
		Debug.message("SDK VERSION: " + SDKVersion.getSDKVersion());
	}

	public DeviceChangeListener getDeviceChangeListener()
	{
		return deviceChangeListener;
	}

	public NETWORK_STATUS getNetworkStatus()
	{
		return NetworkMonitor.getInstance().getNetworkStatus();
	}

	public void setDeviceChangeListener(DeviceChangeListener deviceChangeListener)
	{
		if (deviceChangeListener == null && this.deviceChangeListener != null)
		{
			removeDeviceChangeListener(this.deviceChangeListener);
			this.deviceChangeListener = deviceChangeListener;
			return;
		}
		if (deviceChangeListener == null)
			return;
		this.deviceChangeListener = deviceChangeListener;
		addDeviceChangeListener(deviceChangeListener);
	}

	@Override
	public void deviceAdded(Device dev)
	{
		if (deviceChangeListener != null)
		{
			/*
			 * 设备增加通知
			 */
			deviceChangeListener.deviceAdded(dev);
		}
	}

	@Override
	public void deviceRemoved(Device dev)
	{
		if (deviceChangeListener != null)
		{
			/*
			 * 设备移除通知
			 */
			deviceChangeListener.deviceRemoved(dev);
		}
	}

	@Override
	public void deviceUpdated(Device dev)
	{
		if (deviceChangeListener != null)
		{
			/*
			 * 设备更新通知
			 */
			deviceChangeListener.deviceUpdated(dev);
		}
	}

	public String getControlDeviceAddress(Device dev)
	{
		String addr = null;
		try
		{
			if (dev != null)
				addr = dev.getSSDPPacket().getRemoteAddress();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return addr;
	}

	/*
	 * 最新必杀技
	 * 是否开启实时体验功能
	 */
	public void setOpenRealTimeFunction(boolean isOpen)
	{
		Debug.message("ERROR！！！！！！！！！！！！！！！！！！！not use setOpenRealTimeFunction: isOpen = " + isOpen);
		isOpenRealTime = isOpen;
	}

	/*
	 * 设置实时发送的最大延时容忍值
	 * 单位为毫秒，最小值为10ms，最大值不限制
	 */
	public void setMaxDelayTolerateTime(long maxTimes)
	{
		Debug.message("ERROR！！！！！！！！！！！！！！！！！！！not use setMaxDelayTolerateTime: maxTimes = " + maxTimes);
		if (maxTimes < 10)
			maxTimes = 10;
		maxDelayTime = maxTimes;
	}

	/*
	 * 应用层通知dmc，当前系统已经锁屏，这个时候dmc，不对心跳进行处理，直接将心跳检查跳过
	 */
	public void NotifyDmcSleep(boolean isSleep)
	{
		Debug.message("NotifyDmcSleep: isSleep = " + isSleep);
		this.isAppSleep = isSleep;//设置app处于锁屏状态
	}

	// Keep the sendMessage connection
	public void SetSendMessageForLongAsKeepLive(boolean isKeepAlive)
	{
		mLongforKeepAlive = isKeepAlive;
	}

	/*
	 * DMC给DMR发送消息,DMR会给出回复的消息，拉片的消息可以从返回值里得到
	 * 是否需要回复
	 */
	public String sendMessage(String infor, boolean isNeedReply, Device device)
	{
		if (device == null)
			return null;

		try
		{
			//找到相关服务
			boolean iskeepAlive = mLongforKeepAlive;

			Action sendMessageAction = device.getSendMessageAction(iskeepAlive);
			if (sendMessageAction == null)
				return null;

			sendMessageAction.setKeepAlive(iskeepAlive);
			sendMessageAction.setArgumentValue(PrivateServiceConstStr.INSTANCE_ID, "0");
			sendMessageAction.setArgumentValue(PrivateServiceConstStr.INFOR, infor);
			if (isNeedReply == true) // 需要回复消息
			{
				if (sendMessageAction.postControlAction() == true)
				{
					// 取出返回的值
					String result = sendMessageAction.getArgumentValue(PrivateServiceConstStr.RESULT);
					return result;// 默认至少是空，但不是null
				} else
				// 发送失败
				{
					if (sendMessageAction.getStatus().getCode() == 0)
					{
						Debug.message("sendMessage [" + infor + "] fail, retry...");
						if (sendMessageAction.postControlAction() == true)
						{
							// 取出返回的值
							String result = sendMessageAction.getArgumentValue(PrivateServiceConstStr.RESULT);
							return result;// 默认至少是空，但不是null
						}
					}
					// 移除设备
					Debug.message("sendMessage [" + infor + "] fail, remove device..." + device.getUUID() + " status="
							+ sendMessageAction.getStatus().getCode());
					removeDevice(getDevice(device.getRootNode()));
					return null;
				}
			} else // 不需要回复消息
			{
				if (sendMessageAction.postControlActionNoReply() || sendMessageAction.postControlActionNoReply())
					return "";
				return null;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		// 发送失败
		return null;
	}

	/*
	 * 发送一个字节的数据，快速发送通道，这里要采用了互斥方法
	 */
	public boolean sendMessage(byte data, Device device)
	{
		try
		{
			if (device != null)
			{
				String mydata = getConstructionData(data);
				device.quicklySendUDPMessage(mydata);
				return device.quicklySendTCPMessage(mydata);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * 发送单字节，是通过UDP通道发送
	 */
	public boolean sendUDPMessage(byte data, Device device)
	{
		try
		{
			if (device != null)
			{
				String mydata = getConstructionData(data);
				return device.quicklySendUDPMessage(mydata);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * 通过紧急指针发送
	 */
	public boolean sendMessageBySingle(byte data, Device device)
	{
		try
		{
			if (device != null)
			{
				return device.quicklySendMessage(data);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * 混合型的发送函数
	 */
	public String sendMessage(String infor, Byte data, boolean isNeedReply, Device device)
	{
		if (device == null)
			return null;

		try
		{
			//支持奇艺的快速通道发送
			if (device.getIsSuperQuicklySend() == true)
			{
				if (data != null)//说明可以用快速通道发送
				{
					if (sendMessage(data, device) == false)//发送失败
					{
						return null;
					}
					return "";//返回空即可
				}
				/*
				 * data 为空，则说明需要走DLNA通道
				 */
				return sendMessage(infor, isNeedReply, device);

			} else
			//不支持奇艺的快速发送通道，一律走DLNA通道
			{
				return sendMessage(infor, isNeedReply, device);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public boolean unsubscribePrivateService(Device device)
	{
		if (device == null)
			return false;

		try
		{
			Service privateServer = device.getPrivateServer();
			if (privateServer == null)
				return false;

			if (isSubscribed(privateServer) == true)
			{
				if (unsubscribe(privateServer) == false)
				{
					Debug.message("sub: " + device.getUUID() + " unsubscribe failed");
					return false;
				}
			}

			Debug.message("sub: " + device.getUUID() + " unsubscribe succeed");
			return true;
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public boolean unsubscribePrivateService(String uuid)
	{
		String udn = "uuid:" + uuid;
		Device dev = getDevice(udn);

		if (dev == null)
			return false;

		try
		{
			Service privateServer = dev.getPrivateServer();
			if (privateServer == null)
				return false;

			if (isSubscribed(privateServer) == true)
			{
				Debug.message("sub: unsub currentDev SID: " + privateServer.getSID());
				if (unsubscribe(privateServer) == false)
				{
					Debug.message("sub: " + uuid + " unsubscribe failed");
					return false;
				}
			}

			Debug.message("sub: " + uuid + " unsubscribe succeed");
			return true;
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public boolean subscribePrivateService(Device device)
	{
		if (device == null)
			return false;
		try
		{
			Service privateServer = device.getPrivateServer();
			if (privateServer == null)
				return false;

			if (isSubscribed(privateServer) == false)
			{
				if (subscribe(privateServer, SUBSCRIBED_TIMEOUT) == false)
				{
					Debug.message("sub: " + device.getUUID() + " subscribe failed");
					return false;
				}
			}

			Debug.message("sub: " + device.getUUID() + " subscribe succeed SID: " + privateServer.getSID());
			return true;
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * 设置是否接受DMR的通知消息，该函数调用在选中某个盒子进行控制之后
	 */
	//	private boolean setIsRecevieNotifyMessage(boolean isRecevied, Device device)
	//	{
	//		Debug.message("setIsRecevieNotifyMessage: isRecevied = " + isRecevied);
	//
	//		if (device == null)
	//			return false;
	//
	//		if (isRecevied == true)//需要接收消息
	//		{
	//			Service privateServer = device.getPrivateServer();
	//			if (privateServer != null)//这个服务已经获取到了，不要重新获取，可以省下很多操作
	//			{
	//				if (isSubscribed(privateServer) == false)//如果已经订阅了这个服务，则不需要再订阅，否则要继续订阅	
	//				{
	//					//开始订阅这个服务
	//					if (subscribe(privateServer, SUBSCRIBED_TIMEOUT) == false)
	//					{
	//						Debug.message("DMC set receive dmr message failure");
	//						return false;
	//					}
	//				}
	//				return true;
	//			}
	//		} else
	//		{
	//			Service privateServer = device.getPrivateServer();
	//			if (privateServer != null)
	//			{
	//				if (isSubscribed(privateServer) == true)//已经订阅，则需要取消这个订阅
	//				{
	//					if (unsubscribe(privateServer) == false)
	//					{
	//						Debug.message("DMC set no receive dmr message");
	//						return false;
	//					}
	//				}
	//				return true;
	//			}
	//		}
	//
	//		return false;
	//	}

	/*
	 * 接收DMR的通知消息
	 */
	public void setReceiveNotifyMessageListener(final NotifyMessageListener notifyMessageListener)
	{
		if (notifyMessageListener == null)
		{
			removeEventListener(mEventListener);
			mNotifyMessageListener = null;
			return;
		}
		mNotifyMessageListener = notifyMessageListener;
		addEventListener(mEventListener);
	}

	public String getLocalIpAddress()
	{
		try
		{
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
			{
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
				{
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address))
					{
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex)
		{
			Debug.message(ex.toString());
		}
		return null;
	}

	private boolean QimoHttpServerPort(int port)
	{
		ServerSocket serverSock = null;
		String addr = getLocalIpAddress();
		InetAddress bindAddr = null;
		int bindPort = 0;
		if (addr == null || addr.length() < 1)
			return false;
		Debug.message("++++QimoHttpServerPort addr: " + addr);
		try
		{
			bindAddr = InetAddress.getByName(addr);
			bindPort = port;
			serverSock = new ServerSocket(bindPort, 0, bindAddr);
		} catch (IOException e)
		{
			Debug.warning(e);
			return false;
		}

		try
		{
			serverSock.close();
			serverSock = null;
			bindAddr = null;
			bindPort = 0;
		} catch (Exception e)
		{
			Debug.warning(e);
			return false;
		}

		return true;
	}

	public boolean StartQimoWebServer()
	{
		int retryCnt = 0;
		int qimoPort = mQimoHttpServerPort;
		boolean ret;
		ret = QimoHttpServerPort(mQimoHttpServerPort);
		while (!ret)
		{
			retryCnt++;
			if (QIMOHTTPRETRYTIME < retryCnt)
				return false;
			qimoPort = mQimoHttpServerPort + 1; //getHTTPPort();
			ret = QimoHttpServerPort(qimoPort);
			Debug.message("++++StartQimoWebServer try port: " + qimoPort + " ret: " + ret);
		}
		mQimoHttpServerPort = qimoPort;

		Debug.message("++++StartQimoWebServer port: " + mQimoHttpServerPort);
		if (mQimoHttpServerPort == -1)
		{
			Debug.message("++++StartQimoWebServer failed");
			return false;
		}

		mStdIn = new PipedOutputStream();

		try
		{
			System.setIn(new PipedInputStream(mStdIn));
			mServerThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					//File root = Environment.getExternalStorageDirectory();
					String[] args =
					{ "--port", String.valueOf(mQimoHttpServerPort), "--dir", "/" };
					SimpleWebServer.main(args);
				}
			});

			mServerThread.start();
			Thread.sleep(100);
		} catch (Exception e)
		{
			Debug.message("----Exception in StartQimoWebServer");
			e.printStackTrace();
			return false;
		}
		Debug.message("----StartQimoWebServer");
		return true;
	}

	public boolean StopQimoWebServer()
	{
		Debug.message("++++StopQimoWebServer");
		try
		{
			if (null != mStdIn)
			{
				mStdIn.write("\n\n".getBytes());
				mServerThread.join(2000);
				Assert.assertFalse(mServerThread.isAlive());
			}
		} catch (Exception e)
		{
			Debug.message("----Exception in StopQimoWebServer");
			e.printStackTrace();
			return false;
		}
		Debug.message("----StopQimoWebServer");
		return true;
	}

	/**
	 * URL-encodes everything between "/"-characters. Encodes spaces as '%20' instead of '+'.
	 */
	public String encodeURL(String uri)
	{
		String newUri = "";
		StringTokenizer st = new StringTokenizer(uri, "/ ", true);
		while (st.hasMoreTokens())
		{
			String tok = st.nextToken();
			if (tok.equals("/"))
			{
				newUri += "/";
			} else if (tok.equals(" "))
			{
				newUri += "%20";
			} else
			{
				try
				{
					newUri += URLEncoder.encode(tok, "UTF-8");
				} catch (UnsupportedEncodingException ignored)
				{
					Debug.message("++++encodeURL UnsupportedEncodingException");
				}
			}
		}
		return newUri;
	}

	public String GetQimoFileAddress(String LocalWIFIIP, String FilePath)
	{
		String Port = String.valueOf(mQimoHttpServerPort); //itos(); mQimoHttpServerPort;
		StringBuffer mQimoFileAddress = new StringBuffer();
		if (FilePath != null)
		{
			String mFilePath = encodeURL(FilePath);
			mQimoFileAddress = mQimoFileAddress.append(HTTPSTRING + LocalWIFIIP + ":" + Port + mFilePath);
		}
		Debug.message("++++GetQimoFileAddress" + mQimoFileAddress.toString());
		return mQimoFileAddress.toString();
	}

	public String GetQimoFileAddress(String FilePath)
	{
		String Port = String.valueOf(mQimoHttpServerPort); //itos(); mQimoHttpServerPort;
		StringBuffer mQimoFileAddress = new StringBuffer();
		String addr = getLocalIpAddress();
		if (FilePath != null)
		{
			String mFilePath = encodeURL(FilePath);
			mQimoFileAddress = mQimoFileAddress.append(HTTPSTRING + addr + ":" + Port + mFilePath);
		}
		Debug.message("++++GetQimoFileAddress" + mQimoFileAddress.toString());
		return mQimoFileAddress.toString();
	}

	@Override
	public boolean start()
	{
		boolean ret = false;
		setSubscriberTimeout(SUBSCRIBED_TIMEOUT);
		ret = super.start();
		return ret;
	}

	@Override
	public boolean stop()
	{
		boolean ret = super.stop();
		return ret;
	}

	private static final String AVTransport = "urn:schemas-upnp-org:service:AVTransport:1";
	private static final String SetAVTransportURI = "SetAVTransportURI";
	private static final String RenderingControl = "urn:schemas-upnp-org:service:RenderingControl:1";
	private static final String Play = "Play";
	private String mMediaDuration;
	private Device dlnaControlDevice = null;

	public void setDLNACurrentDevice(Device currentDevice)
	{
		if (dlnaControlDevice != null)
			dlnaControlDevice.clearDLNAAction();
		dlnaControlDevice = currentDevice;
	}

	private String getMetaData(String MediaType, String title)
	{
		String MetaData = "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\""
				+ "xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\"" + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\""
				+ "xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\">" + "<item id=\"" + title
				+ "\" parentID=\"-1\" restricted=\"1\">" + "<upnp:genre>Unknown</upnp:genre>" + "<upnp:class>" + MediaType
				+ "</upnp:class>" + "<dc:title>" + title + "</dc:title>" + "</item></DIDL-Lite>";
		return MetaData;
	}

	@Override
	public boolean play(String path, String title, MediaType type)
	{
		if (dlnaControlDevice == null)
			return false;

		Service service = dlnaControlDevice.getService(AVTransport);
		Debug.message("Standard DLNA play path: " + path + " title: " + title);

		if (service == null)
		{
			return false;
		}

		final Action action = service.getAction(SetAVTransportURI);
		if (action == null)
		{
			return false;
		}

		final Action playAction = service.getAction(Play);
		if (playAction == null)
		{
			return false;
		}

		if (TextUtils.isEmpty(path))
		{
			return false;
		}

		action.setArgumentValue("InstanceID", 0);
		action.setArgumentValue("CurrentURI", path);
		String metaData = getMetaData(type.getTypeName(), title);
		action.setArgumentValue("CurrentURIMetaData", metaData);
		Debug.message("Standard DLNA SetAVTransportURI: " + path);
		if (!action.postControlAction())
		{
			return false;
		}

		playAction.setArgumentValue("InstanceID", 0);
		playAction.setArgumentValue("Speed", "1");
		return playAction.postControlAction();
	}

	@Override
	public boolean goon(String pausePosition)
	{
		if (dlnaControlDevice == null)
			return false;

		Service localService = dlnaControlDevice.getService(AVTransport);
		if (localService == null)
			return false;

		final Action localAction = localService.getAction("Seek");
		if (localAction == null)
			return false;
		localAction.setArgumentValue("InstanceID", "0");
		localAction.setArgumentValue("Unit", "ABS_TIME");
		localAction.setArgumentValue("Target", pausePosition);
		localAction.postControlAction();

		Action playAction = localService.getAction("Play");
		if (playAction == null)
		{
			return false;
		}

		playAction.setArgumentValue("InstanceID", 0);
		playAction.setArgumentValue("Speed", "1");
		return playAction.postControlAction();
	}

	@Override
	public String getTransportState()
	{
		if (dlnaControlDevice == null)
			return null;
		/*
		Service localService = dlnaControlDevice.getService(AVTransport);
		if (localService == null) {
		return null;
		}

		final Action localAction = localService.getAction("GetTransportInfo");
		*/
		final Action localAction = dlnaControlDevice.getGetTransportInfoAction();
		if (localAction == null)
		{
			return null;
		}

		localAction.setArgumentValue("InstanceID", "0");

		if (localAction.postControlAction())
		{
			return localAction.getArgumentValue("CurrentTransportState");
		} else
		{
			return null;
		}
	}

	public String getVolumeDbRange(String argument)
	{
		if (dlnaControlDevice == null)
			return null;

		Service localService = dlnaControlDevice.getService(RenderingControl);
		if (localService == null)
		{
			return null;
		}
		Action localAction = localService.getAction("GetVolumeDBRange");
		if (localAction == null)
		{
			return null;
		}
		localAction.setArgumentValue("InstanceID", "0");
		localAction.setArgumentValue("Channel", "Master");
		if (!localAction.postControlAction())
		{
			return null;
		} else
		{
			return localAction.getArgumentValue(argument);
		}
	}

	@Override
	public int getMinVolumeValue()
	{
		String minValue = getVolumeDbRange("MinValue");
		if (TextUtils.isEmpty(minValue))
		{
			return 0;
		}
		return Integer.parseInt(minValue);
	}

	@Override
	public int getMaxVolumeValue()
	{
		String maxValue = getVolumeDbRange("MaxValue");
		if (TextUtils.isEmpty(maxValue))
		{
			return 100;
		}
		return Integer.parseInt(maxValue);
	}

	@Override
	public boolean seek(String targetPosition)
	{
		if (dlnaControlDevice == null)
			return false;
		Service localService = dlnaControlDevice.getService(AVTransport);
		if (localService == null)
			return false;

		Action localAction = localService.getAction("Seek");
		if (localAction == null)
		{
			return false;
		}
		localAction.setArgumentValue("InstanceID", "0");
		localAction.setArgumentValue("Unit", "ABS_TIME");
		localAction.setArgumentValue("Target", targetPosition);
		boolean postControlAction = localAction.postControlAction();
		if (!postControlAction)
		{
			localAction.setArgumentValue("Unit", "REL_TIME");
			localAction.setArgumentValue("Target", targetPosition);
			return localAction.postControlAction();
		} else
		{
			return postControlAction;
		}

	}

	@Override
	public String getPositionInfo()
	{
		if (dlnaControlDevice == null)
			return null;
		/*
		Service localService = dlnaControlDevice.getService(AVTransport);

		if (localService == null)
		return null;

		final Action localAction = localService.getAction("GetPositionInfo");
		*/
		final Action localAction = dlnaControlDevice.getGetPositionInfoAction();
		if (localAction == null)
		{
			return null;
		}

		localAction.setArgumentValue("InstanceID", "0");
		boolean isSuccess = localAction.postControlAction();
		if (isSuccess)
		{
			mMediaDuration = localAction.getArgumentValue("TrackDuration");
			return localAction.getArgumentValue("AbsTime");
		} else
		{
			return null;
		}
	}

	@Override
	public String getMediaDuration()
	{
		return mMediaDuration;
	}

	@Override
	public boolean setMute(String targetValue)
	{
		if (dlnaControlDevice == null)
			return false;
		Service service = dlnaControlDevice.getService(RenderingControl);
		if (service == null)
		{
			return false;
		}
		final Action action = service.getAction("SetMute");
		if (action == null)
		{
			return false;
		}

		action.setArgumentValue("InstanceID", "0");
		action.setArgumentValue("Channel", "Master");
		action.setArgumentValue("DesiredMute", targetValue);
		return action.postControlAction();
	}

	@Override
	public String getMute()
	{
		if (dlnaControlDevice == null)
			return null;
		Service service = dlnaControlDevice.getService(RenderingControl);
		if (service == null)
		{
			return null;
		}

		final Action getAction = service.getAction("GetMute");
		if (getAction == null)
		{
			return null;
		}
		getAction.setArgumentValue("InstanceID", "0");
		getAction.setArgumentValue("Channel", "Master");
		getAction.postControlAction();
		return getAction.getArgumentValue("CurrentMute");
	}

	@Override
	public boolean setVoice(int value)
	{
		if (dlnaControlDevice == null)
			return false;
		Service service = dlnaControlDevice.getService(RenderingControl);
		if (service == null)
		{
			return false;
		}

		final Action action = service.getAction("SetVolume");
		if (action == null)
		{
			return false;
		}

		action.setArgumentValue("InstanceID", "0");
		action.setArgumentValue("Channel", "Master");
		action.setArgumentValue("DesiredVolume", value);
		return action.postControlAction();

	}

	@Override
	public int getVoice()
	{
		if (dlnaControlDevice == null)
			return -1;
		Service service = dlnaControlDevice.getService(RenderingControl);
		if (service == null)
		{
			return -1;
		}

		final Action getAction = service.getAction("GetVolume");
		if (getAction == null)
		{
			return -1;
		}
		getAction.setArgumentValue("InstanceID", "0");
		getAction.setArgumentValue("Channel", "Master");
		if (getAction.postControlAction())
		{
			return getAction.getArgumentIntegerValue("CurrentVolume");
		} else
		{
			return -1;
		}

	}

	@Override
	public boolean stopplaying()
	{
		if (dlnaControlDevice == null)
			return false;
		Service service = dlnaControlDevice.getService(AVTransport);

		if (service == null)
		{
			return false;
		}
		final Action stopAction = service.getAction("Stop");
		if (stopAction == null)
		{
			return false;
		}

		stopAction.setArgumentValue("InstanceID", 0);
		return stopAction.postControlAction();

	}

	@Override
	public boolean pause()
	{
		if (dlnaControlDevice == null)
			return false;
		Service service = dlnaControlDevice.getService(AVTransport);
		if (service == null)
		{
			return false;
		}
		final Action pauseAction = service.getAction("Pause");
		if (pauseAction == null)
		{
			return false;
		}
		pauseAction.setArgumentValue("InstanceID", 0);
		return pauseAction.postControlAction();
	}

}
