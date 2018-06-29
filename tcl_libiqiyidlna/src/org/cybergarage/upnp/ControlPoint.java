/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2004
 *
 *	File: ControlPoint.java
 *
 *	Revision:
 *
 *	11/18/02
 *		- first revision.
 *	05/13/03
 *		- Changed to create socket threads each local interfaces.
 *		  (HTTP, SSDPNotiry, SSDPSerachResponse)
 *	05/28/03
 *		- Changed to send m-serach packets from SSDPSearchResponseSocket.
 *		  The socket doesn't bind interface address.
 *		- SSDPSearchResponsSocketList that binds a port and a interface can't
 *		  send m-serch packets of IPv6 on J2SE v 1.4.1_02 and Redhat 9.
 *	07/23/03
 *		- Suzan Foster (suislief)
 *		- Fixed a bug. HOST field was missing.
 *	07/29/03
 *		- Synchronized when a device is added by the ssdp message.
 *	09/08/03
 *		- Giordano Sassaroli <sassarol@cefriel.it>
 *		- Problem : when an event notification message is received and the message
 *		            contains updates on more than one variable, only the first variable update
 *		            is notified.
 *		- Error :  the other xml nodes of the message are ignored
 *		- Fix : add two methods to the NotifyRequest for extracting the property array
 *                and modify the httpRequestRecieved method in ControlPoint
 *	12/12/03
 *		- Added a static() to initialize UPnP class.
 *	01/06/04
 *		- Added the following methods to remove expired devices automatically
 *		  removeExpiredDevices()
 *		  setExpiredDeviceMonitoringInterval()/getExpiredDeviceMonitoringInterval()
 *		  setDeviceDisposer()/getDeviceDisposer()
 *	04/20/04
 *		- Added the following methods.
 *		  start(String target, int mx) and start(String target).
 *	06/23/04
 *		- Added setNMPRMode() and isNMPRMode().
 *	07/08/04
 *		- Added renewSubscriberService().
 *		- Changed start() to create renew subscriber thread when the NMPR mode is true.
 *	08/17/04
 *		- Fixed removeExpiredDevices() to remove using the device array.
 *	10/16/04
 *		- Oliver Newell <newell@media-rush.com>
 *		- Added this class to allow ControlPoint applications to be notified when 
 *		  the ControlPoint base class adds/removes a UPnP device
 *	03/30/05
 *		- Changed addDevice() to use Parser::parse(URL).
 *	04/12/06
 *		- Added setUserData() and getUserData() to set a user original data object.
 *
 *******************************************************************/

package org.cybergarage.upnp;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.cybergarage.http.HTTPRequest;
import org.cybergarage.http.HTTPRequestListener;
import org.cybergarage.http.HTTPServerList;
import org.cybergarage.net.HostInterface;
import org.cybergarage.upnp.control.RenewSubscriber;
import org.cybergarage.upnp.device.DeviceChangeListener;
import org.cybergarage.upnp.device.Disposer;
import org.cybergarage.upnp.device.NotifyListener;
import org.cybergarage.upnp.device.ST;
import org.cybergarage.upnp.device.SearchResponseListener;
import org.cybergarage.upnp.device.USN;
import org.cybergarage.upnp.event.EventListener;
import org.cybergarage.upnp.event.NotifyRequest;
import org.cybergarage.upnp.event.Property;
import org.cybergarage.upnp.event.PropertyList;
import org.cybergarage.upnp.event.Subscription;
import org.cybergarage.upnp.event.SubscriptionRequest;
import org.cybergarage.upnp.event.SubscriptionResponse;
import org.cybergarage.upnp.ssdp.SSDP;
import org.cybergarage.upnp.ssdp.SSDPNotifySocketList;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import org.cybergarage.upnp.ssdp.SSDPSearchRequest;
import org.cybergarage.upnp.ssdp.SSDPSearchResponseSocketList;
import org.cybergarage.util.Debug;
import org.cybergarage.util.ListenerList;
import org.cybergarage.util.Mutex;
import org.cybergarage.xml.Node;
import org.cybergarage.xml.NodeList;
import org.cybergarage.xml.Parser;
import org.cybergarage.xml.ParserException;

import com.iqiyi.android.dlna.sdk.SDKVersion;
import com.iqiyi.android.dlna.sdk.controlpoint.DeviceType;
import com.iqiyi.android.dlna.sdk.controlpoint.TVGuoDescription;
import com.iqiyi.android.sdk.dlna.keeper.ControlPointKeeper;
import com.iqiyi.android.sdk.dlna.keeper.DmcInforKeeper;
import com.iqiyi.android.sdk.dlna.keeper.DmrInfor;
import com.iqiyi.android.dlna.sdk.DeviceName;

public class ControlPoint implements HTTPRequestListener
{
	private final static int DEFAULT_EVENTSUB_PORT = 8058;
	private final static int DEFAULT_SSDP_PORT = 53204;//8008
	//private final static String DLNA_DEVICE_TYPE = "urn:schemas-upnp-org:device:MediaRenderer:1";
	private final static String DLNA_URN = "urn:schemas-upnp-org:device:";
	private final static String DLNA_MEDIARENDERER = "MediaRenderer";
	private final static String DLNA_MEDIASERVER = "MediaServer";
	private final static int DEFAULT_EXPIRED_DEVICE_MONITORING_INTERVAL = 5;//以10S来检测一次，改成了5S进行心跳检查一次

	private final static String DEFAULT_EVENTSUB_URI = "/evetSub";

	private DeviceType findDeviceType = DeviceType.MEDIA_RENDERER;//默认为所有的设备类型

	//public Device currentControlDevice = null;//当前被控制的设备

	public boolean isAppSleep = false;//表示应用程序是否锁屏了

	public static long maxDelayTime = -1;//最大的延时时间，如果为-1，则表示不进行限制
	public static boolean isOpenRealTime = false;//是否开启实时体验，实时地代价就是将不实时地消息给干掉....默认是不开启的

	public boolean mLongforKeepAlive = true;

	private long subTimeout = -1;

	public boolean mExternalGID = false;
	private final static String EXTERNAL_VALUE = "external";

	public void setExternalApp(boolean external)
	{
		mExternalGID = external;
	}

	public boolean getExternalApp()
	{
		return mExternalGID;
	}

	public void setSubscriberTimeout(long time)
	{
		subTimeout = time;
	}

	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	public DeviceType getFindDeviceType()
	{
		return findDeviceType;
	}

	public void setFindDeviceType(DeviceType findDeviceType)
	{
		if (findDeviceType == null)
			findDeviceType = DeviceType.MEDIA_ALL;//所有的设备类型
		this.findDeviceType = findDeviceType;
	}

	private SSDPNotifySocketList ssdpNotifySocketList;
	private SSDPSearchResponseSocketList ssdpSearchResponseSocketList;

	private SSDPNotifySocketList getSSDPNotifySocketList()
	{
		return ssdpNotifySocketList;
	}

	private SSDPSearchResponseSocketList getSSDPSearchResponseSocketList()
	{
		return ssdpSearchResponseSocketList;
	}

	////////////////////////////////////////////////
	//	Initialize
	////////////////////////////////////////////////

	static
	{
		UPnP.initialize();
	}

	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public ControlPoint(int ssdpPort, int httpPort, InetAddress[] binds)
	{
		ssdpNotifySocketList = new SSDPNotifySocketList(binds);
		ssdpSearchResponseSocketList = new SSDPSearchResponseSocketList(binds);

		setSSDPPort(ssdpPort);
		setHTTPPort(httpPort);

		setDeviceDisposer(null);
		setExpiredDeviceMonitoringInterval(DEFAULT_EXPIRED_DEVICE_MONITORING_INTERVAL);

		setRenewSubscriber(null);

		setNMPRMode(false);
		setRenewSubscriber(null);
	}

	public ControlPoint(int ssdpPort, int httpPort)
	{
		this(ssdpPort, httpPort, null);
	}

	public ControlPoint()
	{
		this(DEFAULT_SSDP_PORT, DEFAULT_EVENTSUB_PORT);
	}

	public void finalize()
	{
		stop();
	}

	//add by tengfei; support android 6.0, need setExternalFilesDir 
	public void setExternalFilesDir(String ExternalFilesDir)
	{
		ControlPointKeeper.getInstance().setExternalFilesDir(ExternalFilesDir);
		DmcInforKeeper.getInstance().setExternalFilesDir(ExternalFilesDir);
		Debug.message("setExternalFilesDir" + ExternalFilesDir);
	}

	/*
	 * 设备白名单，只有拥有这些标示的设备，才能被发现进来
	 */
	private List<String> whiteList = null;

	private List<String> getwhiteList()
	{
		if (whiteList == null)
		{
			whiteList = new ArrayList<String>();
			whiteList.add("IQIYIDLNA");
			whiteList.add("CyberLinkJava");
			whiteList.add("NewDLNA");
		}
		return whiteList;
	}

	/*
	 * 是否在白名单中
	 */
	public boolean isInWhiteList(String serverName)
	{
		if (serverName == null || serverName.length() == 0)
			return false;

		List<String> mylist = getwhiteList();
		for (String name : mylist)
		{
			if (serverName.toLowerCase(Locale.getDefault()) != null)//包含了
			{
				if (serverName.toLowerCase(Locale.getDefault()).contains(name.toLowerCase(Locale.getDefault())) == true)
				{
					//在白名单里面
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * controlpoint引入了uuid的值
	 */
	private String uuid = null;

	public String getUUID()
	{
		if (uuid == null)//第一次肯定为空
		{
			uuid = ControlPointKeeper.getInstance().getUUID();
			if (uuid == null || uuid.length() == 0)
			{
				uuid = UPnP.createUUID();
				ControlPointKeeper.getInstance().Save(uuid);//保存下来
			}
		}
		return uuid;
	}

	/*
	 * 构建值
	 */
	public String getConstructionData(byte data)
	{
		long offset = 0;
		//		if (mTimeAdjust.isInit()) {
		//		    offset = mTimeAdjust.getTimeOffset();
		//		}
		String result = getUUID() + "#" + (System.currentTimeMillis() + offset) + "#" + (char) (data) + "\n";
		return result;
	}

	////////////////////////////////////////////////
	// Mutex
	////////////////////////////////////////////////

	private Mutex mutex = new Mutex();

	public void lock()
	{
		mutex.lock();
	}

	public void unlock()
	{
		mutex.unlock();
	}

	////////////////////////////////////////////////
	//	Port (SSDP)
	////////////////////////////////////////////////

	private int ssdpPort = 0;

	public int getSSDPPort()
	{
		return ssdpPort;
	}

	public void setSSDPPort(int port)
	{
		ssdpPort = port;
	}

	////////////////////////////////////////////////
	//	Port (EventSub)
	////////////////////////////////////////////////

	private int httpPort = 0;

	public int getHTTPPort()
	{
		return httpPort;
	}

	public void setHTTPPort(int port)
	{
		httpPort = port;
	}

	////////////////////////////////////////////////
	//	NMPR
	////////////////////////////////////////////////

	private boolean nmprMode;

	public void setNMPRMode(boolean flag)
	{
		nmprMode = flag;
	}

	public boolean isNMPRMode()
	{
		return nmprMode;
	}

	////////////////////////////////////////////////
	//	Device List
	////////////////////////////////////////////////

	private NodeList devNodeList = new NodeList();

	private void addDevice(Node rootNode)
	{
		synchronized (devNodeList)
		{
			Debug.message("addDevice to [devNodeList]");
			devNodeList.add(rootNode);
		}
	}

	private NodeList allDevNodeList = new NodeList();

	/*
	 * 全部的设备的列表
	 */
	private void addDeviceToAllDeviceList(Node rootNode)
	{
		synchronized (allDevNodeList)
		{
			Debug.message("addDevice to [allDevNodeList]");
			allDevNodeList.add(rootNode);
		}
	}

	//	private boolean isMediaRenderer(Device device)
	//	{
	//		return device.getDeviceType().contains(MediaRenderer.DEVICE_TYPE);
	//	}
	//
	//	private boolean isQIYIMediaRenderer(Device device)
	//	{
	//		return device.getDeviceType().contains(Device.IQIYI_DEVICE);
	//	}

	/*
	 * 将设备加入设备列表，依据搜索的设备类型来判定
	 */
	private void addDeviceByType(Device rootDev, Node rootNode)
	{
		Debug.message("addDeviceByType()");
		/*
		 * 无论是什么类型的设备，都先加入进来再说
		 */
		addDeviceToAllDeviceList(rootNode);

		if (rootDev != null && rootDev.getSSDPPacket().isQiyiServer() == true)
		{
			addDevice(rootNode);
			performAddDeviceListener(rootDev);
		}
	}

	private void addStandardDLNADevice(SSDPPacket ssdpPacket)
	{
		String usn = ssdpPacket.getUSN();
		String udn = USN.getUDN(usn);
		Debug.message(" standard DLNA device udn: " + udn + " usn: " + usn);
		Device dev = getDeviceByAllDeviceList(udn);
		if (dev != null)
		{
			dev.setSSDPPacket(ssdpPacket);
			Device tmpdev = getDevice(udn);
			if (tmpdev != null)
			{
				tmpdev.setSSDPPacket(ssdpPacket);
			}
			return;
		}
		try
		{
			String l = ssdpPacket.getLocation();
			Debug.message("addStandardDLNADevice location: " + l);
			String location = ssdpPacket.getLocation();
			URL locationUrl = new URL(location);
			Parser parser = UPnP.getXMLParser();
			Node rootNode = parser.parse(locationUrl);
			Device rootDev = getDevice(rootNode);
			if (rootDev == null)
				return;
			if (getFindDeviceType() == DeviceType.MEDIA_RENDERER)
			{
				//DMR
				if (!(rootDev.getDeviceType().contains(DLNA_URN) && rootDev.getDeviceType().contains(DLNA_MEDIARENDERER)))
					return;
			} else if (getFindDeviceType() == DeviceType.MEDIA_SERVER)
			{
				//DMP
				if (!(rootDev.getDeviceType().contains(DLNA_URN) && rootDev.getDeviceType().contains(DLNA_MEDIASERVER)))
					return;
			}
			rootDev.setSSDPPacket(ssdpPacket);
			addDeviceToAllDeviceList(rootNode);
			addDevice(rootNode);
			rootDev.setDeviceVersion(Device.IQIYI_VERSION);
			rootDev.setDeviceName(DeviceName.MEDIA_RENDERER);
			performAddDeviceListener(rootDev);
		} catch (MalformedURLException me)
		{
			Debug.warning(ssdpPacket.toString());
			Debug.warning(me);
		} catch (ParserException pe)
		{
			Debug.warning(ssdpPacket.toString());
			Debug.warning(pe);
		}

	}

	/*
	 * 增加设备的时候，要对设备的类型进行判断来是否增加
	 */
	private synchronized void addDevice(SSDPPacket ssdpPacket)
	{
		Debug.message("addDevice() start..." + ssdpPacket.getMyName());
		//如果只需要发现奇艺设备的话，对其他的设备可以一概不理
		if (getFindDeviceType() == DeviceType.MEDIA_QIYI && ssdpPacket.isQiyiServer() == false)
		{
			Debug.message("addDevice() skip 1...");
			return;
		}

		if (ssdpPacket.isRootDevice() == false)
		{
			Debug.message("addDevice() skip 2...");
			return;
		}

		//如果是其他设备的话，则第一个可以过滤掉路由器
		if (ssdpPacket.isRounterServer() == true)
		{
			Debug.message("addDevice() skip 3...");
			return;
		}

		if (isInWhiteList(ssdpPacket.getServer()) == false)//不在白名单中，则不发现该该设备，直接不发现即可
		{
			Debug.message("addDevice() skip 4...");
			if ((getFindDeviceType() == DeviceType.MEDIA_RENDERER) || (getFindDeviceType() == DeviceType.MEDIA_SERVER))
			{
				Debug.message("addDevice() addStandardDLNADevice...");
				addStandardDLNADevice(ssdpPacket);
			}
			return;
		}

		String usn = ssdpPacket.getUSN();
		String udn = USN.getUDN(usn);
		Debug.message("addDevice() name: " + ssdpPacket.getMyName());
		Debug.message("addDevice() udn: " + udn + " usn: " + usn);
		Debug.message("addDevice() location: " + ssdpPacket.getLocation());
		Debug.message("addDevice() linkedIp: " + ssdpPacket.getLinkedIp());
		Debug.message("addDevice() elapseTime: " + ssdpPacket.getElapseTime());

		Device dev = getDevice(udn);
		if (dev != null)
		{
			Debug.message("addDevice() device already exists...");
			//检查IP地址是否发生改变
			if (dev.getLocation() == null || !dev.getLocation().equals(ssdpPacket.getLocation())) {
				Debug.message("addDevice() update device location..." + dev.getLocation() + "=>"
						+ ssdpPacket.getLocation());
				performUpdatedDeviceListener(dev);//设备更新通知
			}

			dev.setSSDPPacket(ssdpPacket);

			//说明friendlyname变更了
			if (ssdpPacket.getFriendlyName() != null && ssdpPacket.getFriendlyName() != "")
			{
				if (dev.getFriendlyName() == null || dev.getFriendlyName().compareTo(ssdpPacket.getFriendlyName()) != 0)
				{
					Debug.message("addDevice() update device friendly name..." + dev.getFriendlyName() + "=>"
							+ ssdpPacket.getFriendlyName());
					dev.setInternalFriendlyName(ssdpPacket.getFriendlyName());
					performUpdatedDeviceListener(dev);//设备更新通知
				}
			}

			Debug.message("addDevice() done..." + ssdpPacket.getMyName());
			return;//直接结束
		}

		try
		{
			/*
			 * 发现到设备之后，要添加设备，这个时候对于奇艺的盒子，通过特殊的字符串，来唯一标示是否为奇艺的盒子，如果是的话，则加入了cache，
			 * 其他盒子如果uuid的每次都在改变，则不适合加入cache，因为这样没有实际的效果
			 * 
			 */
			if (ssdpPacket.isQiyiServer() == true)//server字段中有iqiyi标示
			{
				Debug.message("addDevice() p1");

				DmrInfor dmrInfor = DmcInforKeeper.getInstance().getDmrInfor(udn);

				//可以使用cache策略,到本地缓存去查找，看是否可以命中
				boolean hit = false;
				try
				{
					if (dmrInfor.getFileMd5().compareTo(ssdpPacket.getFileMd5()) == 0)
					{
						hit = true;
					}
				} catch (Exception e)
				{
					Debug.message("addDevice() not hit...");
					e.printStackTrace();
				}

				if (!hit)
				{
					//没有命中，或者md5的值不一致
					Debug.message("addDevice() p2");
					Node rootNode;
					if (ssdpPacket.getQiyiDeviceType() == DeviceName.IQIYI_DONGLE)
					{
						String desXml = TVGuoDescription.construct(ssdpPacket.getFriendlyName(), ssdpPacket.getUSN()
								.split("::")[0]);
						Parser parser = UPnP.getXMLParser();
						rootNode = parser.parse(desXml);
					} else
					{
						String location = ssdpPacket.getLocation();
						URL locationUrl = new URL(location);
						Parser parser = UPnP.getXMLParser();
						rootNode = parser.parse(locationUrl);
					}

					Device rootDev = getDevice(rootNode);
					if (rootDev == null)
					{
						Debug.message("addDevice() [ERROR] rootDev == null");
						return;
					}

					rootDev.setSSDPPacket(ssdpPacket);
					rootDev.setDeviceVersion(ssdpPacket.getQiyiVersion());
					rootDev.setDeviceName(ssdpPacket.getQiyiDeviceType());
					rootDev.setQiyiDeviceVersion(ssdpPacket.getQiyiDeviceVersion());
					rootDev.setInternalFriendlyName(ssdpPacket.getFriendlyName());
					rootDev.setTvguoFeatureBitmap(ssdpPacket.getTvguoFeatureBitmap());
					rootDev.setTvguoMarketChannel(ssdpPacket.getTvguoMarketChannel());

					Debug.message("addDevice() devFriendlyname: " + rootDev.getFriendlyName() + " IQIYIDEVICE:"
							+ rootDev.getDeviceName() + " IQIYIVERSION:" + rootDev.getDeviceVersion() + " DEVICEVERSION:"
							+ rootDev.getQiyiDeviceVersion() + " TVGUOFEATUREBITMAP:" + rootDev.getTvguoFeatureBitmap()
							+ " TVGUOMARKETCHANNEL:" + rootDev.getTvguoMarketChannel());
					addDeviceByType(rootDev, rootNode);

					//加入cache中，只对有iqiyi标识的盒子加入cache，其他盒子不合适
					DmrInfor saveDmrInfor = new DmrInfor();
					saveDmrInfor.setUuid(udn);
					saveDmrInfor.setFileMd5(ssdpPacket.getFileMd5());//设置md5的值
					saveDmrInfor.setDescriptionFileXml(rootDev.getDescriptionXml());
					DmcInforKeeper.getInstance().SaveDmrInfor(saveDmrInfor);
				} else
				//命中而且md5一致
				{
					Debug.message("addDevice() p3");
					String desXml = dmrInfor.getDescriptionFileXml();
					Parser parser = UPnP.getXMLParser();
					Node rootNode = parser.parse(desXml);
					Device rootDev = getDevice(rootNode);
					if (rootDev == null)
					{
						Debug.message("addDevice() [ERROR] rootDev == null");
						return;
					}

					rootDev.setSSDPPacket(ssdpPacket);
					rootDev.setDeviceVersion(ssdpPacket.getQiyiVersion());
					rootDev.setDeviceName(ssdpPacket.getQiyiDeviceType());
					rootDev.setQiyiDeviceVersion(ssdpPacket.getQiyiDeviceVersion());
					rootDev.setInternalFriendlyName(ssdpPacket.getFriendlyName());
					rootDev.setTvguoFeatureBitmap(ssdpPacket.getTvguoFeatureBitmap());
					rootDev.setTvguoMarketChannel(ssdpPacket.getTvguoMarketChannel());

					Debug.message("addDevice() devFriendlyname: " + rootDev.getFriendlyName() + " IQIYIDEVICE:"
							+ rootDev.getDeviceName() + " IQIYIVERSION:" + rootDev.getDeviceVersion() + " DEVICEVERSION:"
							+ rootDev.getQiyiDeviceVersion() + " TVGUOFEATUREBITMAP:" + rootDev.getTvguoFeatureBitmap()
							+ " TVGUOMARKETCHANNEL:" + rootDev.getTvguoMarketChannel());
					addDeviceByType(rootDev, rootNode);
				}
			} else {
				//server字段中没有iqiyi标示
				Debug.message("addDevice() p4");
				String location = ssdpPacket.getLocation();
				URL locationUrl = new URL(location);
				Parser parser = UPnP.getXMLParser();
				Node rootNode = parser.parse(locationUrl);
				Device rootDev = getDevice(rootNode);
				if (rootDev == null)
				{
					Debug.message("addDevice() [ERROR] rootDev == null");
					return;
				}

				rootDev.setSSDPPacket(ssdpPacket);
				addDeviceByType(rootDev, rootNode);
			}

			Debug.message("addDevice() done..." + ssdpPacket.getMyName());
		} catch (Exception e)
		{
			e.printStackTrace();
			Debug.message(ssdpPacket.toString());
		}
	}

	protected Device getDevice(Node rootNode)
	{
		if (rootNode == null)
			return null;
		Node devNode = rootNode.getNode(Device.ELEM_NAME);
		if (devNode == null)
			return null;
		return new Device(rootNode, devNode);
	}

	public DeviceList getDeviceList()
	{
		synchronized (devNodeList)
		{
			DeviceList devList = new DeviceList();
			int nRoots = devNodeList.size();
			for (int n = 0; n < nRoots; n++)
			{
				Node rootNode = devNodeList.getNode(n);
				Device dev = getDevice(rootNode);
				if (dev == null)
					continue;
				devList.add(dev);
			}
			return devList;
		}
	}

	public Device getDevice(String name)
	{
		synchronized (devNodeList)
		{
			int nRoots = devNodeList.size();
			for (int n = 0; n < nRoots; n++)
			{
				Node rootNode = devNodeList.getNode(n);
				Device dev = getDevice(rootNode);
				if (dev == null)
					continue;
				if (dev.isDevice(name) == true)
					return dev;
				Device cdev = dev.getDevice(name);
				if (cdev != null)
					return cdev;
			}
		}
		return null;
	}

	/*
	 * 从所有的设备列表中取设备
	 */
	public Device getDeviceByAllDeviceList(String name)
	{
		synchronized (allDevNodeList)
		{
			int nRoots = allDevNodeList.size();
			for (int n = 0; n < nRoots; n++)
			{
				Node rootNode = allDevNodeList.getNode(n);
				Device dev = getDevice(rootNode);
				if (dev == null)
					continue;
				if (dev.isDevice(name) == true)
					return dev;
				Device cdev = dev.getDevice(name);
				if (cdev != null)
					return cdev;
			}
		}
		return null;
	}

	public boolean hasDevice(String name)
	{
		return (getDevice(name) != null) ? true : false;
	}

	//20150825 code merge, add synchronized in removeDevice
	private synchronized void removeDevice(Node rootNode)
	{
		// Thanks for Oliver Newell (2004/10/16)
		// Invoke device removal listener prior to actual removal so Device node 
		// remains valid for the duration of the listener (application may want
		// to access the node)
		Device dev = getDevice(rootNode);
		Debug.message("removeDevice() start..." + rootNode.getName() + " " + dev.getFriendlyName());

		if (hasDevice(dev.getUDN()) == false)
		{
			Debug.message("removeDevice() skip! " + dev.getFriendlyName() + " already removed.");
			return;
		}

		if (dev != null && dev.isRootDevice())//先通知remove的，再进行删除，这样会导致一个问题，无法刷新列表
		{
			//add by tengfei 2015061201
			Debug.message("removeDevice() " + dev.getFriendlyName() + " " + dev.getLocation() + " " + dev.getUDN());
			synchronized (devNodeList)
			{
				devNodeList.remove(rootNode);
			}
			/*
			 * 对于总列表，也需要进行删除啊
			 */
			synchronized (allDevNodeList)
			{
				allDevNodeList.remove(rootNode);
			}

			performRemoveDeviceListener(dev);
		} else
		{
			//add by tengfei 2015061201
			Debug.message("removeDevice() [ERROR] dev" + dev + " dev.isRootDevice():" + dev.isRootDevice());
			synchronized (devNodeList)
			{
				devNodeList.remove(rootNode);
			}
			/*
			 * 对于总列表，也需要进行删除啊
			 */
			synchronized (allDevNodeList)
			{
				allDevNodeList.remove(rootNode);
			}
		}

		Debug.message("removeDevice() done..." + rootNode.getName() + " " + dev.getFriendlyName());
	}

	public void removeDevice(Device dev)
	{
		if (dev == null)
			return;
		removeDevice(dev.getRootNode());
	}

	protected void removeDevice(String name)
	{
		Device dev = getDevice(name);
		if (dev == null)
		{
			Debug.message("removeDevice: device [" + name + "] not found");
			return;
		}
		removeDevice(dev);
	}

	private synchronized void removeDevice(SSDPPacket packet)
	{
		String usn = packet.getUSN();
		if (usn == null || "".equals(usn))
		{
			Debug.message("++++20150615 removeDevice(SSDPPacket packet) usn == null || usn ==  ");
			return;
		}
		String udn = USN.getUDN(usn);
		if (udn == null || "".equals(udn))
		{
			Debug.message("++++20150615 removeDevice(SSDPPacket packet) udn == null || udn ==  ");
			return;
		}
		removeDevice(udn);
	}

	////////////////////////////////////////////////
	//	Expired Device
	////////////////////////////////////////////////

	private Disposer deviceDisposer;
	private long expiredDeviceMonitoringInterval;

	/*
	 * 判断网络是否连接正常
	 */
	//	private boolean isConnect(String address, int port)
	//	{
	//		Socket socket = null;
	//		try
	//		{
	//			socket = new Socket();
	//			socket.connect(new InetSocketAddress(address, port), 3000);
	//		} catch (Exception e)
	//		{
	//			e.printStackTrace();
	//			return false;
	//		} finally
	//		{
	//			try
	//			{
	//				socket.close();
	//			} catch (Exception e)
	//			{
	//				e.printStackTrace();
	//			}
	//			socket = null;
	//		}
	//		return true;
	//	}

	public void removeExpiredDevices()
	{
		Debug.message("removeExpiredDevices: check expired devices...");

		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		Device dev[] = new Device[devCnt];
		for (int n = 0; n < devCnt; n++)
			dev[n] = devList.getDevice(n);

		for (int n = 0; n < devCnt; n++)
		{
			if (dev[n].isExpired() == true)
			{
				Debug.message("Remove expired device: " + dev[n].getFriendlyName());
				removeDevice(dev[n]);
			}
		}
	}

	public void setExpiredDeviceMonitoringInterval(long interval)
	{
		expiredDeviceMonitoringInterval = interval;
	}

	public long getExpiredDeviceMonitoringInterval()
	{
		return expiredDeviceMonitoringInterval;
	}

	public void setDeviceDisposer(Disposer disposer)
	{
		deviceDisposer = disposer;
	}

	public Disposer getDeviceDisposer()
	{
		return deviceDisposer;
	}

	////////////////////////////////////////////////
	//	Notify
	////////////////////////////////////////////////

	private ListenerList deviceNotifyListenerList = new ListenerList();

	public void addNotifyListener(NotifyListener listener)
	{
		if (listener != null)
		{
			deviceNotifyListenerList.add(listener);
		}
	}

	public void removeNotifyListener(NotifyListener listener)
	{
		if (listener != null)
		{
			deviceNotifyListenerList.remove(listener);
		}
	}

	public void performNotifyListener(SSDPPacket ssdpPacket)
	{
		int listenerSize = deviceNotifyListenerList.size();
		for (int n = 0; n < listenerSize; n++)
		{
			NotifyListener listener = (NotifyListener) deviceNotifyListenerList.get(n);
			try
			{
				listener.deviceNotifyReceived(ssdpPacket);
			} catch (Exception e)
			{
				Debug.warning("NotifyListener returned an error:", e);
			}
		}
	}

	////////////////////////////////////////////////
	//	SearchResponse
	////////////////////////////////////////////////

	private ListenerList deviceSearchResponseListenerList = new ListenerList();

	public void addSearchResponseListener(SearchResponseListener listener)
	{
		deviceSearchResponseListenerList.add(listener);
	}

	public void removeSearchResponseListener(SearchResponseListener listener)
	{
		deviceSearchResponseListenerList.remove(listener);
	}

	public void performSearchResponseListener(SSDPPacket ssdpPacket)
	{
		int listenerSize = deviceSearchResponseListenerList.size();
		for (int n = 0; n < listenerSize; n++)
		{
			SearchResponseListener listener = (SearchResponseListener) deviceSearchResponseListenerList.get(n);
			try
			{
				listener.deviceSearchResponseReceived(ssdpPacket);
			} catch (Exception e)
			{
				Debug.warning("SearchResponseListener returned an error:", e);
			}
		}
	}

	/////////////////////////////////////////////////////////////////////
	// Device status changes (device added or removed) 
	// Applications that support the DeviceChangeListener interface are 
	// notified immediately when a device is added to, or removed from,
	// the control point.
	/////////////////////////////////////////////////////////////////////

	ListenerList deviceChangeListenerList = new ListenerList();

	public void addDeviceChangeListener(DeviceChangeListener listener)
	{
		synchronized (deviceChangeListenerList)
		{
			deviceChangeListenerList.add(listener);
		}
	}

	public void removeDeviceChangeListener(DeviceChangeListener listener)
	{
		synchronized (deviceChangeListenerList)
		{
			deviceChangeListenerList.remove(listener);
		}
	}

	public void performAddDeviceListener(Device dev)
	{
		synchronized (devNodeList)
		{
			Debug.message("performAddDeviceListener(): Device Num=" + devNodeList.size());
			Iterator<Node> it = devNodeList.iterator();
			while (it.hasNext())
			{
				Node curNode = it.next();
				Device mdev = getDevice(curNode);
				Debug.message("performAddDeviceListener(): Dump device: " + mdev.getFriendlyName() + " " + mdev.getLocation()
						+ " " + mdev.getUDN());
			}
		}

		synchronized (deviceChangeListenerList)
		{
			Debug.message("performAddDeviceListener(): DeviceChangeListener Num=" + deviceChangeListenerList.size());
			int listenerSize = deviceChangeListenerList.size();
			for (int n = 0; n < listenerSize; n++)
			{
				DeviceChangeListener listener = (DeviceChangeListener) deviceChangeListenerList.get(n);
				listener.deviceAdded(dev);
			}
		}
	}

	/*
	 * 设备更新的消息
	 */
	public void performUpdatedDeviceListener(Device dev)
	{
		synchronized (deviceChangeListenerList)
		{
			int listenerSize = deviceChangeListenerList.size();
			for (int n = 0; n < listenerSize; n++)
			{
				DeviceChangeListener listener = (DeviceChangeListener) deviceChangeListenerList.get(n);
				listener.deviceUpdated(dev);
			}
		}
	}

	/*
	 * 设备离线消息
	 */
	/*
	public void performOfflineDeviceListener( Device dev )
	{
	int listenerSize = deviceChangeListenerList.size();
	for (int n=0; n<listenerSize; n++) {
		DeviceChangeListener listener = (DeviceChangeListener)deviceChangeListenerList.get(n);
		listener.deviceOffline(dev);
	}
	}
	*/

	public void performRemoveDeviceListener(Device dev)
	{
		synchronized (devNodeList)
		{
			Debug.message("performRemoveDeviceListener(): Device Num=" + devNodeList.size());
			Iterator<Node> it = devNodeList.iterator();
			while (it.hasNext())
			{
				Node curNode = it.next();
				Device mdev = getDevice(curNode);
				Debug.message("performRemoveDeviceListener(): Dump device: " + mdev.getFriendlyName() + " "
						+ mdev.getLocation() + " " + mdev.getUDN());

				if (mdev.getUDN().equals(dev.getUDN()))
				{
					Debug.message("performRemoveDeviceListener(): Remove duplicated device: " + mdev.getFriendlyName() + " "
							+ mdev.getLocation() + " " + mdev.getUDN());

					synchronized (allDevNodeList)
					{
						allDevNodeList.remove(curNode);
					}
					it.remove();
				}
			}
		}

		synchronized (deviceChangeListenerList)
		{
			Debug.message("performRemoveDeviceListener(): DeviceChangeListener Num=" + deviceChangeListenerList.size());
			int listenerSize = deviceChangeListenerList.size();
			for (int n = 0; n < listenerSize; n++)
			{
				DeviceChangeListener listener = (DeviceChangeListener) deviceChangeListenerList.get(n);
				listener.deviceRemoved(dev);
			}
		}
	}

	////////////////////////////////////////////////
	//	SSDPPacket
	////////////////////////////////////////////////

	public void notifyReceived(SSDPPacket packet)
	{
		if (packet.isAlive() == true)
		{
			Debug.message("notifyReceived() " + packet.getMyName() + " " + packet.getNTS());
			addDevice(packet);
		} else if (packet.isByeBye() == true)
		{
			Debug.message("notifyReceived() " + packet.getMyName() + " " + packet.getNTS());
			removeDevice(packet);
		}

		performNotifyListener(packet);
	}

	public void searchResponseReceived(SSDPPacket packet)
	{
		addDevice(packet);
		performSearchResponseListener(packet);
	}

	////////////////////////////////////////////////
	//	M-SEARCH
	////////////////////////////////////////////////

	private int searchMx = SSDP.DEFAULT_MSEARCH_MX;

	public int getSearchMx()
	{
		return searchMx;
	}

	public void setSearchMx(int mx)
	{
		searchMx = mx;
	}

	public void search(String target, int mx)
	{
		SSDPSearchRequest msReq = new SSDPSearchRequest(target, mx);
		SSDPSearchResponseSocketList ssdpSearchResponseSocketList = getSSDPSearchResponseSocketList();
		ssdpSearchResponseSocketList.post(msReq);
	}

	public void search(String target)
	{
		search(target, SSDP.DEFAULT_MSEARCH_MX);
	}

	public synchronized void search()
	{
		Debug.message("++++ControlPoint search");
		search(ST.ROOT_DEVICE, SSDP.DEFAULT_MSEARCH_MX);
		Debug.message("----ControlPoint search");
	}

	////////////////////////////////////////////////
	//	EventSub HTTPServer
	////////////////////////////////////////////////

	private HTTPServerList httpServerList = new HTTPServerList();

	private HTTPServerList getHTTPServerList()
	{
		return httpServerList;
	}

	public void httpRequestRecieved(HTTPRequest httpReq)
	{
		if (Debug.isOn() == true)
			httpReq.print();

		// Thanks for Giordano Sassaroli <sassarol@cefriel.it> (09/08/03)
		if (httpReq.isNotifyRequest() == true)
		{
			NotifyRequest notifyReq = new NotifyRequest(httpReq);
			String uuid = notifyReq.getSID();
			long seq = notifyReq.getSEQ();
			PropertyList props = notifyReq.getPropertyList();
			int propCnt = props.size();
			for (int n = 0; n < propCnt; n++)
			{
				Property prop = props.getProperty(n);
				String varName = prop.getName();
				String varValue = prop.getValue();
				performEventListener(uuid, seq, varName, varValue);
			}
			httpReq.returnOK();
			return;
		}

		httpReq.returnBadRequest();
	}

	////////////////////////////////////////////////
	//	Event Listener 
	////////////////////////////////////////////////

	private ListenerList eventListenerList = new ListenerList();

	public void addEventListener(EventListener listener)
	{
		eventListenerList.add(listener);
	}

	public void removeEventListener(EventListener listener)
	{
		eventListenerList.remove(listener);
	}

	public void performEventListener(String uuid, long seq, String name, String value)
	{
		int listenerSize = eventListenerList.size();
		for (int n = 0; n < listenerSize; n++)
		{
			EventListener listener = (EventListener) eventListenerList.get(n);
			listener.eventNotifyReceived(uuid, seq, name, value);
		}
	}

	////////////////////////////////////////////////
	//	Subscription 
	////////////////////////////////////////////////

	private String eventSubURI = DEFAULT_EVENTSUB_URI;

	public String getEventSubURI()
	{
		return eventSubURI;
	}

	public void setEventSubURI(String url)
	{
		eventSubURI = url;
	}

	private String getEventSubCallbackURL(String host)
	{
		return HostInterface.getHostURL(host, getHTTPPort(), getEventSubURI());
	}

	public boolean subscribe(Service service, long timeout)
	{
		if (service.isSubscribed() == true)
		{
			String sid = service.getSID();
			return subscribe(service, sid, timeout);
		}

		Device rootDev = service.getRootDevice();
		if (rootDev == null)
			return false;
		String ifAddress = rootDev.getInterfaceAddress();
		SubscriptionRequest subReq = new SubscriptionRequest();
		if (getExternalApp())
		{
			Debug.message("sub: sub external for GUOAPP");
			subReq.setGID(EXTERNAL_VALUE);
		}
		subReq.setSubscribeRequest(service, getEventSubCallbackURL(ifAddress), timeout);
		if (Debug.isOn() == true)
			subReq.print();
		SubscriptionResponse subRes = subReq.post();
		if (Debug.isOn() == true)
			subRes.print();
		if (subRes.isSuccessful() == true)
		{
			service.setSID(subRes.getSID());
			service.setTimeout(subRes.getTimeout());
			return true;

		}

		// 订阅失败了,不要清除原先的SID,因为DMR端会继续用这个SID来发送Notify
		// service.clearSID();
		return false;
	}

	public boolean resubscribe(Service service, long timeout)
	{
		Device rootDev = service.getRootDevice();
		if (rootDev == null)
			return false;
		String ifAddress = rootDev.getInterfaceAddress();
		SubscriptionRequest subReq = new SubscriptionRequest();
		if (getExternalApp())
		{
			Debug.message("sub: sub external for GUOAPP");
			subReq.setGID(EXTERNAL_VALUE);
		}
		subReq.setSubscribeRequest(service, getEventSubCallbackURL(ifAddress), timeout);
		if (Debug.isOn() == true)
			subReq.print();
		SubscriptionResponse subRes = subReq.post();
		if (Debug.isOn() == true)
			subRes.print();
		if (subRes.isSuccessful() == true)
		{
			service.setSID(subRes.getSID());
			service.setTimeout(subRes.getTimeout());
			return true;

		}

		// 订阅失败了,不要清除原先的SID,因为DMR端会继续用这个SID来发送Notify
		// service.clearSID();
		return false;
	}

	public boolean subscribe(Service service)
	{
		return subscribe(service, Subscription.INFINITE_VALUE);
	}

	public boolean subscribe(Service service, String sid, long timeout)
	{
		SubscriptionRequest subReq = new SubscriptionRequest();
		if (getExternalApp())
		{
			Debug.message("sub: renew sub external for GUOAPP");
			subReq.setGID(EXTERNAL_VALUE);
		}
		subReq.setRenewRequest(service, sid, timeout);
		if (Debug.isOn() == true)
			subReq.print();
		SubscriptionResponse subRes = subReq.post();
		if (Debug.isOn() == true)
			subRes.print();
		if (subRes.isSuccessful() == true)
		{
			service.setSID(subRes.getSID());
			service.setTimeout(subRes.getTimeout());
			return true;
		}

		// 订阅失败了,不要清除原先的SID,因为DMR端会继续用这个SID来发送Notify
		// service.clearSID();
		return false;
	}

	public boolean subscribe(Service service, String sid)
	{
		return subscribe(service, sid, Subscription.INFINITE_VALUE);
	}

	public boolean isSubscribed(Service service)
	{
		if (service == null)
			return false;
		return service.isSubscribed();
	}

	public boolean unsubscribe(Service service)
	{
		SubscriptionRequest subReq = new SubscriptionRequest();
		if (getExternalApp())
		{
			Debug.message("sub: unsub external for GUOAPP");
			subReq.setGID(EXTERNAL_VALUE);
		}
		subReq.setUnsubscribeRequest(service);
		SubscriptionResponse subRes = subReq.post();
		if (subRes.isSuccessful() == true)
		{
			service.clearSID();
			return true;
		}
		return false;
	}

	public void unsubscribe(Device device)
	{
		ServiceList serviceList = device.getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++)
		{
			Service service = serviceList.getService(n);
			if (service.hasSID() == true)
				unsubscribe(service);
		}

		DeviceList childDevList = device.getDeviceList();
		int childDevCnt = childDevList.size();
		for (int n = 0; n < childDevCnt; n++)
		{
			Device cdev = childDevList.getDevice(n);
			unsubscribe(cdev);
		}
	}

	public void unsubscribe()
	{
		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++)
		{
			Device dev = devList.getDevice(n);
			unsubscribe(dev);
		}
	}

	////////////////////////////////////////////////
	//	getSubscriberService	
	////////////////////////////////////////////////

	public Service getSubscriberService(String uuid)
	{
		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++)
		{
			Device dev = devList.getDevice(n);
			Service service = dev.getSubscriberService(uuid);
			if (service != null)
				return service;
		}
		return null;
	}

	////////////////////////////////////////////////
	//	getSubscriber
	////////////////////////////////////////////////

	public Device getSubscriber(String uuid)
	{
		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++)
		{
			Device dev = devList.getDevice(n);
			Service service = dev.getSubscriberService(uuid);
			if (service != null)
				return dev;
		}
		return null;
	}

	////////////////////////////////////////////////
	//	getSubscriberService	
	////////////////////////////////////////////////

	public void renewSubscriberService(Device dev, long timeout)
	{
		ServiceList serviceList = dev.getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++)
		{
			Service service = serviceList.getService(n);
			if (service.isSubscribed() == false)
				continue;
			String sid = service.getSID();
			boolean isRenewed = subscribe(service, sid, timeout);
			if (isRenewed == false)
				resubscribe(service, timeout);
		}

		DeviceList cdevList = dev.getDeviceList();
		int cdevCnt = cdevList.size();
		for (int n = 0; n < cdevCnt; n++)
		{
			Device cdev = cdevList.getDevice(n);
			renewSubscriberService(cdev, timeout);
		}
	}

	public void renewSubscriberService(long timeout)
	{
		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++)
		{
			Device dev = devList.getDevice(n);
			renewSubscriberService(dev, timeout);
		}
	}

	public void renewSubscriberService()
	{
		renewSubscriberService(Subscription.INFINITE_VALUE);
	}

	////////////////////////////////////////////////
	//	Subscriber
	////////////////////////////////////////////////

	private RenewSubscriber renewSubscriber;

	public void setRenewSubscriber(RenewSubscriber sub)
	{
		renewSubscriber = sub;
	}

	public RenewSubscriber getRenewSubscriber()
	{
		return renewSubscriber;
	}

	////////////////////////////////////////////////
	//	run	
	////////////////////////////////////////////////

	public boolean start(String target, int mx)
	{
		Debug.message("MediaControlPoint start SDK VERSION: " + SDKVersion.getSDKVersion());
		stop();

		////////////////////////////////////////
		// HTTP Server
		////////////////////////////////////////

		int retryCnt = 0;
		int bindPort = getHTTPPort();
		HTTPServerList httpServerList = getHTTPServerList();
		while (httpServerList.open(bindPort) == false)
		{
			retryCnt++;
			if (UPnP.SERVER_RETRY_COUNT < retryCnt)
				return false;
			setHTTPPort(bindPort + 1);
			bindPort = getHTTPPort();
		}
		httpServerList.addRequestListener(this);
		httpServerList.start();

		////////////////////////////////////////
		// Notify Socket
		////////////////////////////////////////

		SSDPNotifySocketList ssdpNotifySocketList = getSSDPNotifySocketList();
		if (ssdpNotifySocketList.open() == false)
			return false;
		ssdpNotifySocketList.setControlPoint(this);
		ssdpNotifySocketList.start();

		////////////////////////////////////////
		// SeachResponse Socket
		////////////////////////////////////////

		int ssdpPort = getSSDPPort();
		retryCnt = 0;
		SSDPSearchResponseSocketList ssdpSearchResponseSocketList = getSSDPSearchResponseSocketList();

		while (ssdpSearchResponseSocketList.open(ssdpPort) == false)
		{
			retryCnt++;
			try
			{
				Thread.sleep(1000);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			Debug.message("ssdpSearchResponseSocketList.open retry" + retryCnt);
			if (UPnP.SERVER_RETRY_COUNT < retryCnt)
				return false;
			//add by tengfei, +10 because the second try will successful, but still not useful
			setSSDPPort(ssdpPort + 10);
			ssdpPort = getSSDPPort();
		}
		ssdpSearchResponseSocketList.setControlPoint(this);
		ssdpSearchResponseSocketList.start();

		////////////////////////////////////////
		// search root devices
		////////////////////////////////////////
		for (int i = 0; i < 3; i++)
			search(target, mx);

		////////////////////////////////////////
		// Disposer
		////////////////////////////////////////

		Disposer disposer = new Disposer(this);
		setDeviceDisposer(disposer);
		disposer.start("Disposer");

		////////////////////////////////////////
		// Subscriber
		////////////////////////////////////////

		if (isNMPRMode() == true)
		{
			RenewSubscriber renewSub = new RenewSubscriber(this);
			setRenewSubscriber(renewSub);
			renewSub.setSubscriberTimeout(subTimeout);
			renewSub.start("RenewSubscriber");
		}

		Debug.message("MediaControlPoint start SDK VERSION [DONE]: " + SDKVersion.getSDKVersion());
		return true;
	}

	public boolean start(String target)
	{
		return start(target, SSDP.DEFAULT_MSEARCH_MX);
	}

	public boolean start()
	{
		return start(ST.ROOT_DEVICE, SSDP.DEFAULT_MSEARCH_MX);
	}

	public boolean stop()
	{
		Debug.message("MediaControlPoint stop SDK VERSION: " + SDKVersion.getSDKVersion());

		SSDPNotifySocketList ssdpNotifySocketList = getSSDPNotifySocketList();
		ssdpNotifySocketList.stop();
		ssdpNotifySocketList.close();
		ssdpNotifySocketList.clear();

		SSDPSearchResponseSocketList ssdpSearchResponseSocketList = getSSDPSearchResponseSocketList();
		ssdpSearchResponseSocketList.stop();
		ssdpSearchResponseSocketList.close();
		ssdpSearchResponseSocketList.clear();

		HTTPServerList httpServerList = getHTTPServerList();
		httpServerList.stop();
		httpServerList.close();
		httpServerList.clear();

		// TODO: closeConnectHost()

		////////////////////////////////////////
		// Disposer
		////////////////////////////////////////

		Disposer disposer = getDeviceDisposer();
		if (disposer != null)
		{
			Debug.message("Stop Disposer Thread...");
			disposer.stop();
			setDeviceDisposer(null);
		}

		////////////////////////////////////////
		// Subscriber
		////////////////////////////////////////

		RenewSubscriber renewSub = getRenewSubscriber();
		if (renewSub != null)
		{
			Debug.message("Stop RenewSubscriber Thread...");
			renewSub.stop();
			setRenewSubscriber(null);
		}

		// 注意这是一个耗时操作,因为要通过TCP进行命令交互
		unsubscribe();

		/*
		 * 清空设备列表
		 */
		synchronized (allDevNodeList)
		{
			allDevNodeList.clear();
		}

		/*
		 * 清空设备列表
		 */
		synchronized (devNodeList)
		{
			devNodeList.clear();
		}

		Debug.message("MediaControlPoint stop SDK VERSION [DONE]: " + SDKVersion.getSDKVersion());

		return true;
	}

	////////////////////////////////////////////////
	//	userData
	////////////////////////////////////////////////

	private Object userData = null;

	public void setUserData(Object data)
	{
		userData = data;
	}

	public Object getUserData()
	{
		return userData;
	}

	////////////////////////////////////////////////
	//	print	
	////////////////////////////////////////////////

	public void print()
	{
		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		Debug.message("Device Num = " + devCnt);
		for (int n = 0; n < devCnt; n++)
		{
			Device dev = devList.getDevice(n);
			Debug.message("[" + n + "] " + dev.getFriendlyName() + ", " + dev.getLeaseTime() + ", " + dev.getElapsedTime());
		}
	}
}
