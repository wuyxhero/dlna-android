/******************************************************************
 *
 *	CyberLink for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2004
 *
 *	File: Device.java
 *
 *	Revision:
 *
 *	11/28/02
 *		- first revision.
 *	02/26/03
 *		- URLBase is updated automatically.
 * 		- Description of a root device is returned from the XML node tree.
 *	05/13/03
 *		- URLBase is updated when the request is received.
 *		- Changed to create socket threads each local interfaces.
 *		  (HTTP, SSDPSearch)
 *	06/17/03
 *		- Added notify all state variables when a new subscription is received.
 *	06/18/03
 *		- Fixed a announce bug when the bind address is null on J2SE v 1.4.1_02 and Redhat 9.
 *	09/02/03
 *		- Giordano Sassaroli <sassarol@cefriel.it>
 *		- Problem : bad request response sent even with successful subscriptions
 *		- Error : a return statement is missing in the httpRequestRecieved method
 *	10/21/03
 *		- Updated a udn field by a original uuid.
 *	10/22/03
 *		- Added setActionListener().
 *		- Added setQueryListener().
 *	12/12/03
 *		- Added a static() to initialize UPnP class.
 *	12/25/03
 *		- Added advertiser functions.
 *	01/05/04
 *		- Added isExpired().
 *	03/23/04
 *		- Oliver Newell <newell@media-rush.com>
 *		- Changed to update the UDN only when the field is null.
 *	04/21/04
 *		- Added isDeviceType().
 *	06/18/04
 *		- Added setNMPRMode() and isNMPRMode().
 *		- Changed getDescriptionData() to update only when the NMPR mode is false.
 *	06/21/04
 *		- Changed start() to send a bye-bye before the announce.
 *		- Changed annouce(), byebye() and deviceSearchReceived() to send the SSDP
 *		  messsage four times when the NMPR and the Wireless mode are true.
 *	07/02/04
 *		- Fixed announce() and byebye() to send the upnp::rootdevice message despite embedded devices.
 *		- Fixed getRootNode() to return the root node when the device is embedded.
 *	07/24/04
 *		- Thanks for Stefano Lenzi <kismet-sl@users.sourceforge.net>
 *		- Added getParentDevice().
 *	10/20/04 
 *		- Brent Hills <bhills@openshores.com>
 *		- Changed postSearchResponse() to add MYNAME header.
 *	11/19/04
 *		- Theo Beisch <theo.beisch@gmx.de>
 *		- Added getStateVariable(String serviceType, String name).
 *	03/22/05
 *		- Changed httpPostRequestRecieved() to return the bad request when the post request isn't the soap action.
 *	03/23/05
 *		- Added loadDescription(String) to load the description from memory.
 *	03/30/05
 *		- Added getDeviceByDescriptionURI().
 *		- Added getServiceBySCPDURL().
 *	03/31/05
 *		- Changed httpGetRequestRecieved() to return the description stream using
 *		  Device::getDescriptionData() and Service::getSCPDData() at first.
 *	04/25/05
 *		- Thanks for Mikael Hakman <mhakman@dkab.net>
 *		  Changed announce() and byebye() to close the socket after the posting.
 *	04/25/05
 *		- Thanks for Mikael Hakman <mhakman@dkab.net>
 *		  Changed deviceSearchResponse() answer with USN:UDN::<device-type> when request ST is device type.
 * 	04/25/05
 *		- Thanks for Mikael Hakman <mhakman@dkab.net>
 * 		- Changed getDescriptionData() to add a XML declaration at first line.
 * 	04/25/05
 *		- Thanks for Mikael Hakman <mhakman@dkab.net>
 *		- Added a new setActionListener() and serQueryListner() to include the sub devices. 
 *	07/24/05
 *		- Thanks for Stefano Lenzi <kismet-sl@users.sourceforge.net>
 *		- Fixed a bug of getParentDevice() to return the parent device normally.
 *	02/21/06
 *		- Changed httpRequestRecieved() not to ignore HEAD requests.
 *	04/12/06
 *		- Added setUserData() and getUserData() to set a user original data object.
 *	03/29/08
 *		- Added isRunning() to know whether the device is running.
 * 
 ******************************************************************/

package org.cybergarage.upnp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Vector;

import org.cybergarage.http.HTTP;
import org.cybergarage.http.HTTPRequest;
import org.cybergarage.http.HTTPResponse;
import org.cybergarage.http.HTTPServerList;
import org.cybergarage.http.HTTPStatus;
import org.cybergarage.net.HostInterface;
import org.cybergarage.soap.SOAPResponse;
import org.cybergarage.upnp.control.ActionListener;
import org.cybergarage.upnp.control.ActionRequest;
import org.cybergarage.upnp.control.ActionResponse;
import org.cybergarage.upnp.control.ControlRequest;
import org.cybergarage.upnp.control.ControlResponse;
import org.cybergarage.upnp.control.QueryListener;
import org.cybergarage.upnp.control.QueryRequest;
import org.cybergarage.upnp.device.Advertiser;
import org.cybergarage.upnp.device.Description;
import org.cybergarage.upnp.device.InvalidDescriptionException;
import org.cybergarage.upnp.device.NTS;
import org.cybergarage.upnp.device.ST;
import org.cybergarage.upnp.device.SearchListener;
import org.cybergarage.upnp.device.USN;
import org.cybergarage.upnp.event.Subscriber;
import org.cybergarage.upnp.event.Subscription;
import org.cybergarage.upnp.event.SubscriptionRequest;
import org.cybergarage.upnp.event.SubscriptionResponse;
import org.cybergarage.upnp.ssdp.SSDPNotifyRequest;
import org.cybergarage.upnp.ssdp.SSDPNotifySocket;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import org.cybergarage.upnp.ssdp.SSDPSearchResponse;
import org.cybergarage.upnp.ssdp.SSDPSearchResponseSocket;
import org.cybergarage.upnp.ssdp.SSDPSearchSocketList;
import org.cybergarage.upnp.xml.DeviceData;
import org.cybergarage.util.Debug;
import org.cybergarage.util.FileUtil;
import org.cybergarage.util.MD5Util;
import org.cybergarage.util.Mutex;
import org.cybergarage.util.TimerUtil;
import org.cybergarage.xml.Node;
import org.cybergarage.xml.Parser;
import org.cybergarage.xml.ParserException;
import org.cybergarage.xml.XML;

import android.text.TextUtils;

import com.iqiyi.android.dlna.sdk.dlnahttpserver.QiyiHttpServerList;
import com.iqiyi.android.dlna.sdk.dlnahttpserver.QiyiUDPHttpServer;
import com.iqiyi.android.dlna.sdk.dlnahttpserver.QiyiHttpServer;
import com.iqiyi.android.dlna.sdk.mediarenderer.ControlPointConnectRendererListener;
import com.iqiyi.android.dlna.sdk.mediarenderer.QuicklySendMessageListener;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.PrivateServiceConstStr;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.RenderingControlConstStr;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.RenderingControl;
import com.iqiyi.android.dlna.sdk.mediarenderer.service.infor.AVTransportConstStr;
import com.iqiyi.android.dlna.sdk.DeviceName;

public class Device implements org.cybergarage.http.HTTPRequestListener, SearchListener
{
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////

	public final static String ELEM_NAME = "device";
	public final static String UPNP_ROOTDEVICE = "upnp:rootdevice";
	public final static String IQIYI_DEVICE = "IQIYI";
	//add by tengfei, update IQIYIVERSION from 2 to 3
	//20160121 for 弱网优化 and skipinfonew
	public final static int IQIYI_VERSION = 3;

	public final static int DEFAULT_STARTUP_WAIT_TIME = 1000;
	public final static int DEFAULT_DISCOVERY_WAIT_TIME = 100;

	public final static int DEFAULT_LEASE_TIME = 30; //相当于30S一次
	public final static int DLNA_SEARCH_LEASE_TIME = 30;
	public final static int DEFAULT_EXPIRED_DEVICE_TIME = 10;//10S检查一次

	public final static int HTTP_DEFAULT_PORT = 4004;

	public final static String DEFAULT_DESCRIPTION_URI = "/description.xml"; //默认描述文件的地址 description.xml

	public int bindssdpPort = 16968;//默认从16868开始

	/*
	 * 加入hash表中，如果这个连接没有在hash表中，则加入Hashmap里，如果hashmap有的话，则直接去取
	 */
	private Map<String, byte[]> cacheMap = new HashMap<String, byte[]>();

	private String mIconPath;

	// 用来区分是什么设备
	private int mDeviceName = DeviceName.IQIYI_BOX;

	// 用来区分硬件版本
	private int mDeviceVersion = DeviceName.IQIYI_DONGLE_V1;

	// 电视果功能表
	private int mTVGuoFeatureBitmap = 0;

	// 电视果销售渠道
	private long mTVGuoMarketChannel = 0;

	private static final String UUID = "uuid:";

	//电视果设备的SN编号
	private String mSN = null;

	public void setTvguoSN(String sn)
	{
		mSN = sn;
	}

	public String getTvguoSN() {
		return mSN;
	}

	// 电视果设备的SN编号
	private String mPCBA = null;

	public void setTvguoPCBA(String pcba) {
		mPCBA = pcba;
	}

	public String getTvguoPCBA() {
		return mPCBA;
	}

	//通过Smart Config为该设备配置网络的设备的IP地址
	private String mLinkedIp = null;
	//设备联网时间
	private long mLinkedIpTime = 0;

	public void setLinkedIp(String ip, long time)
	{
		Debug.message("setLinkedIp: ip=" + ip + " time=" + time);
		mLinkedIp = ip;
		mLinkedIpTime = time;
	}

	public String getLinkedIp()
	{
		return mLinkedIp;
	}

	public long getLinkedIpTime()
	{
		return mLinkedIpTime;
	}

	/*
	 * 是否支持快速发送
	 */
	private boolean quicklySend = false;

	public boolean isQuicklySend()
	{
		return quicklySend;
	}

	/*
	 * 设置快速发送通道
	 */
	public void setQuicklySend(boolean quicklySend)
	{
		this.quicklySend = quicklySend;
	}

	public void setIconPath(String iconPath)
	{
		this.mIconPath = iconPath;
	}

	/*
	 * 快速发送的消息回调接口
	 */
	private QuicklySendMessageListener quicklySendMessageListener = null;

	public QuicklySendMessageListener getQuicklySendMessageListener()
	{
		return quicklySendMessageListener;
	}

	public void setQuicklySendMessageListener(QuicklySendMessageListener quicklySendMessageListener)
	{
		this.quicklySendMessageListener = quicklySendMessageListener;
	}

	//controlpoint是否连接设备的回调函数
	private ControlPointConnectRendererListener controlPointConnectRendererListener = null;

	public ControlPointConnectRendererListener getControlPointConnectRendererListener()
	{
		return controlPointConnectRendererListener;
	}

	public void setControlPointConnectRendererListener(ControlPointConnectRendererListener controlPointConnectRendererListener)
	{
		this.controlPointConnectRendererListener = controlPointConnectRendererListener;
	}

	/*
	 * 优化服务的选择和动作查找，使得每次调用的时候，不需要重新查找一次
	 * 这样可以减少查找的次数，使得发送消息的时候，效率提供
	 */
	private Service privateServer = null;
	private Action sendMessageAction = null;

	private Boolean isSupperKeepAlive = null;//默认是不支持的,是否支持长连接

	public boolean getDeviceIsSupperKeepAlive()
	{
		if (isSupperKeepAlive == null)
		{
			if (getSSDPPacket() != null)
			{
				isSupperKeepAlive = getSSDPPacket().isSupperConnectKeepAlive();
			} else
			{
				isSupperKeepAlive = false;
			}
		}
		return isSupperKeepAlive;
	}

	/*
	 * 获取sendmessage的action
	 */
	public synchronized void clearSendMessageAction()
	{
		if (sendMessageAction != null)
		{
			sendMessageAction.getActionRequest().closeHostSocket();
			sendMessageAction = null;
		}

		if (quicklyHttpRequest != null)
		{
			quicklyHttpRequest.closeHostQuickly();
			quicklyHttpRequest = null;
		}
	}

	public synchronized Action getSendMessageAction(boolean iskeepalive)
	{
		if (iskeepalive == true)
		{
			if (sendMessageAction == null)
			{
				Service privateService = getPrivateServer();
				if (privateService != null)
				{
					sendMessageAction = privateService.getAction(PrivateServiceConstStr.SEND_MESSAGE);
				}
			}
			return sendMessageAction;
		}
		/*
		 * 支持短连接的方式
		 */
		Service privateService = getPrivateServer();
		Action action = null;
		if (privateService != null)
		{
			action = privateService.getAction(PrivateServiceConstStr.SEND_MESSAGE);
		}
		return action;
	}

	//keep frequent messages connection
	//GetPositionInfo, GetTransportInfo
	private Action GetPositionInfoAction = null;
	private Action GetTransportInfoAction = null;

	public synchronized void clearDLNAAction()
	{
		GetPositionInfoAction = null;
		GetTransportInfoAction = null;
	}

	public synchronized Action getGetTransportInfoAction()
	{
		if (GetTransportInfoAction == null)
		{
			Service AVTransport = getService(AVTransportConstStr.SERVICE_ID);
			if (AVTransport != null)
			{
				GetTransportInfoAction = AVTransport.getAction(AVTransportConstStr.GETTRANSPORTINFO);
			}
		}
		return GetTransportInfoAction;
	}

	public Action getGetPositionInfoAction()
	{
		if (GetPositionInfoAction == null)
		{
			Service AVTransport = getService(AVTransportConstStr.SERVICE_ID);
			if (AVTransport != null)
			{
				GetPositionInfoAction = AVTransport.getAction(AVTransportConstStr.GETPOSITIONINFO);
			}
		}
		return GetPositionInfoAction;
	}

	/*
	 * 获取快速发送通道
	 */
	private HTTPRequest quicklyHttpRequest = null;

	private HTTPRequest getQuicklyHttpRequest()
	{
		if (quicklyHttpRequest == null)
		{
			quicklyHttpRequest = new HTTPRequest();
		}
		return quicklyHttpRequest;
	}

	/*
	 * 快速地将数据发送出去
	 */
	private int qiyiTCPPort = 0;

	public boolean quicklySendTCPMessage(String data)
	{
		HTTPRequest httpRequest = getQuicklyHttpRequest();
		if (httpRequest != null)
		{
			int port = getQiyiHTTPPortFromSSDP();
			if (qiyiTCPPort != 0 && port != qiyiTCPPort)
			{
				//说明端口发生了变更，要重新弄一次
				httpRequest.closeHostQuickly();
				httpRequest = null;
				httpRequest = getQuicklyHttpRequest();
				Debug.message("port change!!!");
			}
			if (port == 0)//说明是不支持的
				return false;
			String host = getQiyiHostFromSSDP();

			qiyiTCPPort = port;

			return httpRequest.quicklyTCPPost(host, port, data);
		}
		return false;
	}

	public void setServerIP(String serverIP)
	{
		HostInterface.setInterface(serverIP);
	}

	/*
	 * 快速地将数据发送出去，采用了UDP的方式
	 */
	private int qiyiUDPPort = 0;

	public boolean quicklySendUDPMessage(String data)
	{
		HTTPRequest httpRequest = getQuicklyHttpRequest();

		if (httpRequest != null)
		{
			int port = getQiyiUDPHTTPPortFromSSDP();//从组播消息中获取到udp端口号
			if (qiyiUDPPort != 0 && qiyiUDPPort != port)
			{
				//说明端口发生了变更，要重新弄一次
				httpRequest.closeHostQuickly();
				httpRequest = null;
				httpRequest = getQuicklyHttpRequest();
				Debug.message("port change!!!");
			}
			if (port == 0)//说明是不支持的
				return false;
			String host = getQiyiHostFromSSDP();
			qiyiUDPPort = port;
			return httpRequest.quicklyUDPPost(host, port, data);
		}
		return false;
	}

	/*
	 * 通过TCP的紧急指针发送的
	 */
	public boolean quicklySendMessage(byte data)
	{
		HTTPRequest httpRequest = getQuicklyHttpRequest();
		if (httpRequest != null)
		{
			int port = getQiyiHTTPPortFromSSDP();
			if (port == 0)//说明是不支持的
				return false;
			String host = getQiyiHostFromSSDP();
			Debug.message("++++++++quicklySendMessage host " + host + "port " + port);
			return httpRequest.quicklyPost(host, port, data);
		}
		return false;
	}

	/*
	 * 预先连接，不管有没有用，都先连接再说，这样可以防止，当发送第一条消息的时候，需要连接而导致了延时
	 */
	public void beforeHandConnectHost()
	{
		Debug.message("online beforeHandConnectHost() ");
		/*
		 * 这里必须做这个处理，不然会出现一种情况，当移动端比TV端早启动的时候，就会有图标不显示的问题。
		 */
		if (quicklyHttpRequest != null)
		{
			quicklyHttpRequest.closeHostQuickly();
			quicklyHttpRequest = null;
		}
		HTTPRequest httpRequest = getQuicklyHttpRequest();
		if (httpRequest != null)
		{
			Debug.message("online beforeHandConnectHost() p1 ");
			int port = getQiyiHTTPPortFromSSDP();
			if (port == 0)//说明是不支持的
				return;
			String host = getQiyiHostFromSSDP();
			try
			{
				String location = getSSDPPacket().getLocation();
				URL locationUrl = new URL(location);

				Action sendMessageAction = getSendMessageAction(true);
				ActionRequest ctrlReq = sendMessageAction.getActionRequest();
				ctrlReq.connectHost(locationUrl.getHost(), locationUrl.getPort(), true);

				Debug.message("online beforeHandConnectHost() p2 ");
				httpRequest.connectHostQuickly(host, port);//预先建立起这个长连接
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * reconnect机制会判断是否socket真的已经断开，如果已经断开才会重新连接，否则不做处理。
	 */
	public void reconnectQuicklyHost()
	{
		HTTPRequest httpRequest = getQuicklyHttpRequest();
		String data = null;
		if (httpRequest != null)
		{
			Debug.message("online reconnectQuicklyHost() p1 ");
			int port = getQiyiHTTPPortFromSSDP();
			if (port == 0)//说明是不支持的
				return;
			String host = getQiyiHostFromSSDP();
			try
			{
				httpRequest.closeHostQuickly();
				Debug.message("online reconnectQuicklyHost() p2 ");
				httpRequest.reconnectHostQuickly(host, port, data);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
			}
		}
	}

	public void sendDataToHostTokeepAlive(String data)
	{
		HTTPRequest httpRequest = getQuicklyHttpRequest();
		if (httpRequest != null)
		{
			Debug.message("online sendUrgentData p1 ");
			int port = getQiyiHTTPPortFromSSDP();
			if (port == 0)//说明是不支持的
				return;
			String host = getQiyiHostFromSSDP();
			if (host.length() <= 0)
				return;
			try
			{
				Debug.message("online sendUrgentData p2 data: " + data);
				httpRequest.reconnectHostQuickly(host, port, data);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
			}
		}
	}

	public void closeConnectHost()
	{
		HTTPRequest httpRequest = getQuicklyHttpRequest();
		if (httpRequest != null)
		{
			httpRequest.closeHostQuickly();
		}
	}

	/*
	 * 获取私有服务的接口
	 */
	public Service getPrivateServer()
	{
		if (privateServer == null)
		{
			privateServer = getService(PrivateServiceConstStr.SERVICE_ID);
		}
		return privateServer;
	}

	/*
	 * 回调设备是否有进行了连接
	 */
	private DeviceConnectStatusListener deviceConnectStatusListener = null;

	public DeviceConnectStatusListener getDeviceConnectStatusListener()
	{
		return deviceConnectStatusListener;
	}

	public void setDeviceConnectStatusListener(DeviceConnectStatusListener deviceConnectStatusListener)
	{
		this.deviceConnectStatusListener = deviceConnectStatusListener;
	}

	private String descriptionXmlContent = "";//设备描述文件的内容

	public String getDescriptionXmlContent()
	{
		return descriptionXmlContent;
	}

	public void setDescriptionXmlContent(String descriptionXmlContent)
	{
		this.descriptionXmlContent = descriptionXmlContent;
	}

	/*
	 * upnp的描述文件的MD5值，对upnp的组播消息进行扩展
	 * 目的是为了优化设备发现的过程
	 */
	private String descriptionXmlMd5 = "";

	public String getDescriptionXmlMd5()
	{
		return descriptionXmlMd5;
	}

	public void setDescriptionXmlMd5(String descriptionXmlMd5)
	{
		this.descriptionXmlMd5 = descriptionXmlMd5;
	}

	/*
	 * 获取整个描述文件
	 */
	public synchronized String getDescriptionXml()
	{
		String content = getDescriptionXmlContent();
		if (content == null || content == "")
		{
			Node rootNode = getRootNode();
			if (rootNode == null)
				return "";
			/*
			 * 加上dlna的命名空间
			 */
			rootNode.setNameSpace(RootDescription.ROOT_ELEMENT_NAMESPACE);
			rootNode.setNameSpace(RootDescription.ROOT_ELEMENT_NSDLNA, RootDescription.ROOT_ELEMENT_DLNANAMESPACE);

			String desc = new String();
			desc += UPnP.XML_DECLARATION;
			desc += "\n";
			desc += rootNode.toString();
			//计算完毕之后填充
			setDescriptionXmlContent(desc);

		} else if (content.contains(PrivateServiceConstStr.SERVICE_NAME) == false)//保证server一定是有的
		{
			Node rootNode = getRootNode();
			if (rootNode == null)
				return "";
			/*
			 * 加上dlna的命名空间
			 */
			rootNode.setNameSpace(RootDescription.ROOT_ELEMENT_NAMESPACE);
			rootNode.setNameSpace(RootDescription.ROOT_ELEMENT_NSDLNA, RootDescription.ROOT_ELEMENT_DLNANAMESPACE);

			String desc = new String();
			desc += UPnP.XML_DECLARATION;
			desc += "\n";
			desc += rootNode.toString();
			//计算完毕之后填充
			setDescriptionXmlContent(desc);
		}
		return getDescriptionXmlContent();
	}

	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	private Node rootNode;
	private Node deviceNode;
	private Node specVersionNode;//版本

	public Node getSpecVersionNode()
	{
		return specVersionNode;
	}

	public void setSpecVersionNode(Node specVersionNode)
	{
		this.specVersionNode = specVersionNode;
	}

	public Node getRootNode()
	{
		if (rootNode != null)
			return rootNode;
		if (deviceNode == null)
			return null;
		return deviceNode.getRootNode();
	}

	public Node getDeviceNode()
	{
		return deviceNode;
	}

	public void setRootNode(Node node)
	{
		rootNode = node;
	}

	public void setDeviceNode(Node node)
	{
		deviceNode = node;
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

	public Device(Node root, Node device)
	{
		rootNode = root;
		deviceNode = device;
		//setUUID(UPnP.createUUID());
		setWirelessMode(true);
	}

	/*
	 * 默认的版本为 1.0
	 */
	public Device()
	{
		//this(null, null);

		initDevice(1, 0);
	}

	/*
	 * 设置版本号
	 */
	public Device(int major, int minor)
	{
		initDevice(major, minor);
	}

	public Device(Node device)
	{
		this(null, device);
	}

	public Device(File descriptionFile) throws InvalidDescriptionException
	{
		this(null, null);
		loadDescription(descriptionFile);
	}

	/**
	 * @since 1.8.0
	 */
	public Device(InputStream input) throws InvalidDescriptionException
	{
		this(null, null);
		loadDescription(input);
	}

	public Device(String descriptionFileName) throws InvalidDescriptionException
	{
		this(new File(descriptionFileName));
	}

	/*
	 * 初始化设备
	 */
	public void initDevice(int major, int minor)
	{
		rootNode = new Node("root");
		rootNode.setNameSpace(RootDescription.ROOT_ELEMENT_NAMESPACE);
		rootNode.setNameSpace(RootDescription.ROOT_ELEMENT_NSDLNA, RootDescription.ROOT_ELEMENT_DLNANAMESPACE);

		specVersionNode = new Node(RootDescription.SPECVERSION_ELEMENT);

		Node majorNode = new Node(RootDescription.MAJOR_ELEMENT);
		majorNode.setValue(major);
		specVersionNode.addNode(majorNode);

		Node minorNode = new Node(RootDescription.MINOR_ELEMENT);
		minorNode.setValue(minor);
		specVersionNode.addNode(minorNode);

		rootNode.addNode(specVersionNode);//设备版本节点

		deviceNode = new Node(ELEM_NAME);
		rootNode.addNode(deviceNode);//设备节点

		/*
		 * 这个以后可以加入日志的功能，使得uuid的值，始终只有一个，不会每次启动都会变更
		 */
		/*
		DmrInfor dmrInfor=DmrInforKeeper.getInstance().getDmrInfor();
		if(dmrInfor==null||dmrInfor.getUuid()=="")//不存uuid，这意味着，这个是首次生成
		{
			String  uuid=UPnP.createUUID();
			setUUID(uuid);
			setUDN(getUUID());
			dmrInfor=new DmrInfor();
			dmrInfor.setUuid(uuid);
			DmrInforKeeper.getInstance().SaveDmrInfor(dmrInfor);//保存到日志里去，这样就可以做到uuid的值，每次都一致
		}else
		{
			setUUID(dmrInfor.getUuid());
			//必须对udn进行处理，不然的话，fileMd5就有问题
			setUDN(getUUID());
		}
		*/

		setWirelessMode(true);

		setDescriptionURI(DEFAULT_DESCRIPTION_URI);
		/*
		 * 这里要注意一下
		 */
		/*
		if (hasUDN() == false)
		updateUDN();
		*/

	}

	/*
	 * 初始化设备
	 * 主要对设备的信息是否填写完整进行检查---这个要记得实现啊
	 */
	public void CheckDeviceDes()
	{

		if (getUDN() == null || getUDN() == "")
		{
			setUDN(getUUID());
		}
		if (getFriendlyName() == null || getFriendlyName() == "")
		{
			setInternalFriendlyName("IQIYI_TV");
		}
		if (getManufacture() == null || getManufacture() == "")
		{
			setManufacture("iqiyi");
		}
		if (getManufactureURL() == null || getManufactureURL() == "")
		{
			setManufactureURL("http://www.iqiyi.com");
		}
		if (getModelName() == null || getModelName() == "")
		{
			setModelName("IQIYI AV Media Renderer Device");
		}
		if (getModelNumber() == null || getModelNumber() == "")
		{
			setModelNumber("1234");
		}
		if (getModelURL() == null || getModelURL() == "")
		{
			setModelURL("http://www.iqiyi.com");
		}
		if (getPackageName() == null || getPackageName() == "")
		{
			setPackageName("");
		}
	}

	/*
	 * udp通道开启
	 */
	private QiyiUDPHttpServer qiyiUDPHttpServer = null;

	public boolean start()
	{
		//change true to false, no need send byebye.
		stop(false);//应该是先进行stop,再继续检查

		CheckDeviceDes();//检查一些字段，是否填写完整？如果不完整的话，则进行默认填充
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
			{
				return false;
			}
			setHTTPPort(bindPort + 10);
			bindPort = getHTTPPort();
		}
		httpServerList.addRequestListener(this);
		httpServerList.start();

		/*
		 * 计算描述文件，每次都需要重新计算一下，这个很重要啊，之前我忽略了，犯错了
		 * 因为这里的IP地址可能发生了变更，这个时候xml的文件的组织必然发生了变化，这意味着md5的值也需要改变
		 */
		String desStr = getDescriptionXml();
		//计算设备描述文件的md5的值，这个要对upnp的多播消息进行扩展
		setDescriptionXmlMd5(MD5Util.getMd5(desStr.getBytes(), desStr.length()));

		/*
		 * 快速发送数据通道------------------------------
		 */
		if (isQuicklySend() == true)
		{
			retryCnt = 0;
			QiyiHttpServerList qiyiHttpServerList = getQiyiHttpServerList();
			int qiyibindPort = getQiyiHTTPPort();
			while (qiyiHttpServerList.open(qiyibindPort) == false)
			{
				retryCnt++;
				if (UPnP.SERVER_RETRY_COUNT < retryCnt)
				{
					return false;
				}
				setQiyiHTTPPort(qiyibindPort + 1);
				qiyibindPort = getQiyiHTTPPort();
			}
			qiyiHttpServerList.addRequestListener(this);
			qiyiHttpServerList.addControlPointConnectListener(controlPointConnectRendererListener);
			qiyiHttpServerList.start();
		}

		/*
		 *  快速发送数据通道，UDP策略
		 */
		if (isQuicklySend() == true)
		{
			if (qiyiUDPHttpServer == null)
			{
				qiyiUDPHttpServer = new QiyiUDPHttpServer();

				setUdpQiyiHTTPPort(getQiyiHTTPPort() + 1);
				int UDPqiyibindPort = getUdpQiyiHttpPort();
				retryCnt = 0;
				while (qiyiUDPHttpServer.open(UDPqiyibindPort) == false)
				{
					retryCnt++;
					if (UPnP.SERVER_RETRY_COUNT < retryCnt)
					{
						return false;
					}
					setUdpQiyiHTTPPort(UDPqiyibindPort + 1);
					UDPqiyibindPort = getUdpQiyiHttpPort();
				}
				qiyiUDPHttpServer.addRequestListener(this);
				qiyiUDPHttpServer.addControlPointConnectRendererListener(controlPointConnectRendererListener);
				qiyiUDPHttpServer.start();
			}
		}

		////////////////////////////////////////
		// SSDP Search Socket
		////////////////////////////////////////
		SSDPSearchSocketList ssdpSearchSockList = getSSDPSearchSocketList();
		if (ssdpSearchSockList.open() == false)
		{
			return false;
		}
		ssdpSearchSockList.addSearchListener(this);
		ssdpSearchSockList.start();

		////////////////////////////////////////
		// Announce
		////////////////////////////////////////
		announce();
		////////////////////////////////////////
		// Advertiser
		////////////////////////////////////////
		Advertiser adv = new Advertiser(this);
		setAdvertiser(adv);
		adv.start("Advertiser");

		return true;
	}

	public void clear()
	{
		setInternalFriendlyName("");//清空friendlyname的值
		setDescriptionXmlContent("");//清空描述文件
		setDescriptionXmlMd5("");//清空md5的值
	}

	private boolean stop(boolean doByeBye)
	{
		/*
		 * 先将多播消息给停止了
		 */
		Advertiser adv = getAdvertiser();
		if (adv != null)
		{
			adv.stop();
			setAdvertiser(null);
		}

		if (doByeBye == true)//再发送BYEBYE
			byebye();

		HTTPServerList httpServerList = getHTTPServerList();
		httpServerList.stop();
		httpServerList.close();
		httpServerList.clear();

		/*
		 * 快速发送数据通道关闭，清空资源-----TCP
		 */
		if (isQuicklySend() == true)
		{
			QiyiHttpServerList qiyiHttpServerList = getQiyiHttpServerList();
			qiyiHttpServerList.stop();
			qiyiHttpServerList.close();
			qiyiHttpServerList.clear();
		}

		/*
		 * 关闭udp通道
		 */
		if (isQuicklySend() == true)
		{
			if (qiyiUDPHttpServer != null)
			{
				qiyiUDPHttpServer.removeControlPointConnectRendererListener(controlPointConnectRendererListener);
				qiyiUDPHttpServer.close();
				qiyiUDPHttpServer.stop();
				qiyiUDPHttpServer = null;
			}
		}

		SSDPSearchSocketList ssdpSearchSockList = getSSDPSearchSocketList();
		ssdpSearchSockList.stop();
		ssdpSearchSockList.close();
		ssdpSearchSockList.clear();

		//将网口信息给清除了，不然的话，当发生了网络变更的时候，就可能有问题，这个很重要
		if (getDeviceData() != null)
		{
			getDeviceData().setHTTPBindAddress(null);
		}

		for(String key : mSSDPNotifySockList.keySet()){
			SSDPNotifySocket sock = mSSDPNotifySockList.get(key);
			sock.close();
			sock.closeBroad();
		}
		mSSDPNotifySockList.clear();

		return true;
	}

	public boolean stop()
	{
		return stop(true);
	}

	public boolean isRunning()
	{
		return (getAdvertiser() != null) ? true : false;
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
	//	getAbsoluteURL
	////////////////////////////////////////////////

	public String getAbsoluteURL(String urlString, String baseURLStr, String locationURLStr)
	{
		if ((urlString == null) || (urlString.length() <= 0))
			return "";

		try
		{
			URL url = new URL(urlString);
			return url.toString();
		} catch (Exception e)
		{
		}

		if ((baseURLStr == null) || (baseURLStr.length() <= 0))
		{
			if ((locationURLStr != null) && (0 < locationURLStr.length()))
			{
				if (!locationURLStr.endsWith("/") || !urlString.startsWith("/"))
				{
					String absUrl = locationURLStr + urlString;
					try
					{
						URL url = new URL(absUrl);
						return url.toString();
					} catch (Exception e)
					{
					}
				} else
				{
					String absUrl = locationURLStr + urlString.substring(1);
					try
					{
						URL url = new URL(absUrl);
						return url.toString();
					} catch (Exception e)
					{
					}
				}

				String absUrl = HTTP.getAbsoluteURL(locationURLStr, urlString);
				try
				{
					URL url = new URL(absUrl);
					return url.toString();
				} catch (Exception e)
				{
				}

				// Thanks for Steven Yen (2003/09/03)
				Device rootDev = getRootDevice();
				if (rootDev != null)
				{
					String location = rootDev.getLocation();
					String locationHost = HTTP.getHost(location);
					int locationPort = HTTP.getPort(location);
					baseURLStr = HTTP.getRequestHostURL(locationHost, locationPort);
				}
			}
		}

		if ((baseURLStr != null) && (0 < baseURLStr.length()))
		{
			if (!baseURLStr.endsWith("/") || !urlString.startsWith("/"))
			{
				String absUrl = baseURLStr + urlString;
				try
				{
					URL url = new URL(absUrl);
					return url.toString();
				} catch (Exception e)
				{
				}
			} else
			{
				String absUrl = baseURLStr + urlString.substring(1);
				try
				{
					URL url = new URL(absUrl);
					return url.toString();
				} catch (Exception e)
				{
				}
			}

			String absUrl = HTTP.getAbsoluteURL(baseURLStr, urlString);
			try
			{
				URL url = new URL(absUrl);
				return url.toString();
			} catch (Exception e)
			{
			}
		}

		return urlString;
	}

	public String getAbsoluteURL(String urlString)
	{
		String baseURLStr = null;
		String locationURLStr = null;

		Device rootDev = getRootDevice();
		if (rootDev != null)
		{
			baseURLStr = rootDev.getURLBase();
			locationURLStr = rootDev.getLocation();
			//这里需要过滤一下路径
			int find = locationURLStr.indexOf("/", 10);
			if (find > 0)
			{
				locationURLStr = locationURLStr.substring(0, find);
			}
		}

		return getAbsoluteURL(urlString, baseURLStr, locationURLStr);
	}

	////////////////////////////////////////////////
	//	NMPR
	////////////////////////////////////////////////

	public void setNMPRMode(boolean flag)
	{
		Node devNode = getDeviceNode();
		if (devNode == null)
			return;
		if (flag == true)
		{
			devNode.setNode(UPnP.INMPR03, UPnP.INMPR03_VERSION);
			devNode.removeNode(Device.URLBASE_NAME);
		} else
		{
			devNode.removeNode(UPnP.INMPR03);
		}
	}

	public boolean isNMPRMode()
	{
		Node devNode = getDeviceNode();
		if (devNode == null)
			return false;
		return (devNode.getNode(UPnP.INMPR03) != null) ? true : false;
	}

	////////////////////////////////////////////////
	//	Wireless
	////////////////////////////////////////////////

	private boolean wirelessMode;

	public void setWirelessMode(boolean flag)
	{
		wirelessMode = flag;
	}

	public boolean isWirelessMode()
	{
		return wirelessMode;
	}

	public int getSSDPAnnounceCount()
	{
		if (isNMPRMode() == true && isWirelessMode() == true)
			return UPnP.INMPR03_DISCOVERY_OVER_WIRELESS_COUNT;
		return 1;
	}

	////////////////////////////////////////////////
	//	Device UUID
	////////////////////////////////////////////////

	private String devUUID;

	public void setUUID(String uuid)
	{
		devUUID = uuid;
	}

	public String getUUID()
	{
		if (devUUID != null && devUUID.length() > 16)
			return devUUID;
		String udn = deviceNode.getNodeValue(UDN);
		if (udn.startsWith(UUID) == false)
			return udn;
		String uuid = udn.substring(UUID.length(), udn.length());
		setUUID(uuid);
		return uuid;
	}

	public String getIconUrl()
	{
		//TODO: only get the first icon in iconList
		IconList iconList = getIconList();
		if (iconList.size() == 0)
			return null;
		Icon icon = (Icon) iconList.get(0);
		String iconUrl = icon.getURL();
		//For box that has absolute path in url value
		if (getDeviceName() != DeviceName.IQIYI_DONGLE && getDeviceName() != DeviceName.MEDIA_RENDERER)
			return iconUrl;

		//For dongle
		String absPath = getLocation().substring(0, getLocation().length() - DEFAULT_DESCRIPTION_URI.length());
		String iconAbsUrl = absPath + "/" + iconUrl;
		return iconAbsUrl;
	}

	public void updateUDN()
	{
		setUDN("uuid:" + getUUID());
	}

	////////////////////////////////////////////////
	//	Root Device
	////////////////////////////////////////////////

	public Device getRootDevice()
	{
		Node rootNode = getRootNode();
		if (rootNode == null)
			return null;
		Node devNode = rootNode.getNode(Device.ELEM_NAME);
		if (devNode == null)
			return null;
		return new Device(rootNode, devNode);
	}

	////////////////////////////////////////////////
	//	Parent Device
	////////////////////////////////////////////////

	// Thanks for Stefano Lenzi (07/24/04)

	/**
	 * 
	 * @return A Device that contain this object.<br>
	 *         Return <code>null</code> if this is a root device.
	 * @author Stefano "Kismet" Lenzi
	 */
	public Device getParentDevice()
	{
		if (isRootDevice())
			return null;
		Node devNode = getDeviceNode();
		Node aux = null;
		//<device><deviceList><device>
		aux = devNode.getParentNode().getParentNode();
		return new Device(aux);
	}

	/**
	 * Add a Service to device without checking for duplicate or syntax error
	 * 
	 * @param s
	 *            Add Service s to the Device
	 */
	public void addService(Service s)
	{
		Node serviceListNode = getDeviceNode().getNode(ServiceList.ELEM_NAME);
		if (serviceListNode == null)
		{
			serviceListNode = new Node(ServiceList.ELEM_NAME);
			getDeviceNode().addNode(serviceListNode);
		}
		serviceListNode.addNode(s.getServiceNode());
	}

	/**
	 * Add a Device to device without checking for duplicate or syntax error. This method set or reset the root node of the
	 * Device and itself<br>
	 * <br>
	 * Note: This method should be used to create a dynamic<br>
	 * Device withtout writing any XML that describe the device<br>
	 * .
	 * 
	 * @param d
	 *            Add Device d to the Device
	 * 
	 * @author Stefano "Kismet" Lenzi - kismet-sl@users.sourceforge.net - 2005
	 * 
	 */
	public void addDevice(Device d)
	{
		Node deviceListNode = getDeviceNode().getNode(DeviceList.ELEM_NAME);
		if (deviceListNode == null)
		{
			//deviceListNode = new Node(ServiceList.ELEM_NAME); twa wrong ELEM_NAME;
			deviceListNode = new Node(DeviceList.ELEM_NAME);
			getDeviceNode().addNode(deviceListNode);
		}
		deviceListNode.addNode(d.getDeviceNode());
		d.setRootNode(null);
		if (getRootNode() == null)
		{
			Node root = new Node(RootDescription.ROOT_ELEMENT);
			root.setNameSpace("", RootDescription.ROOT_ELEMENT_NAMESPACE);
			Node spec = new Node(RootDescription.SPECVERSION_ELEMENT);
			Node maj = new Node(RootDescription.MAJOR_ELEMENT);
			maj.setValue("1");
			Node min = new Node(RootDescription.MINOR_ELEMENT);
			min.setValue("0");
			spec.addNode(maj);
			spec.addNode(min);
			root.addNode(spec);
			setRootNode(root);
		}
	}

	////////////////////////////////////////////////
	//	UserData
	////////////////////////////////////////////////

	private DeviceData getDeviceData()
	{
		Node node = getDeviceNode();
		DeviceData userData = (DeviceData) node.getUserData();
		if (userData == null)
		{
			userData = new DeviceData();
			node.setUserData(userData);
			userData.setNode(node);
		}
		return userData;
	}

	////////////////////////////////////////////////
	//	Description
	////////////////////////////////////////////////

	private void setDescriptionFile(File file)
	{
		getDeviceData().setDescriptionFile(file);
	}

	public File getDescriptionFile()
	{
		return getDeviceData().getDescriptionFile();
	}

	private void setDescriptionURI(String uri)
	{
		getDeviceData().setDescriptionURI(uri);
	}

	private String getDescriptionURI()
	{
		return getDeviceData().getDescriptionURI();
	}

	private boolean isDescriptionURI(String uri)
	{
		String descriptionURI = getDescriptionURI();
		if (uri == null || descriptionURI == null)
			return false;
		return descriptionURI.equals(uri);
	}

	public String getDescriptionFilePath()
	{
		File descriptionFile = getDescriptionFile();
		if (descriptionFile == null)
			return "";
		return descriptionFile.getAbsoluteFile().getParent();
	}

	/**
	 * @since 1.8.0
	 */
	public boolean loadDescription(InputStream input) throws InvalidDescriptionException
	{
		try
		{
			Parser parser = UPnP.getXMLParser();
			rootNode = parser.parse(input);
			if (rootNode == null)
				throw new InvalidDescriptionException(Description.NOROOT_EXCEPTION);
			deviceNode = rootNode.getNode(Device.ELEM_NAME);
			if (deviceNode == null)
				throw new InvalidDescriptionException(Description.NOROOTDEVICE_EXCEPTION);
		} catch (ParserException e)
		{
			throw new InvalidDescriptionException(e);
		}

		if (initializeLoadedDescription() == false)
			return false;

		setDescriptionFile(null);

		return true;
	}

	public boolean loadDescription(String descString) throws InvalidDescriptionException
	{
		try
		{
			Parser parser = UPnP.getXMLParser();
			rootNode = parser.parse(descString);
			if (rootNode == null)
				throw new InvalidDescriptionException(Description.NOROOT_EXCEPTION);
			deviceNode = rootNode.getNode(Device.ELEM_NAME);
			if (deviceNode == null)
				throw new InvalidDescriptionException(Description.NOROOTDEVICE_EXCEPTION);
		} catch (ParserException e)
		{
			throw new InvalidDescriptionException(e);
		}

		if (initializeLoadedDescription() == false)
			return false;

		setDescriptionFile(null);

		return true;
	}

	public boolean loadDescription(File file) throws InvalidDescriptionException
	{
		try
		{
			Parser parser = UPnP.getXMLParser();
			rootNode = parser.parse(file);
			if (rootNode == null)
				throw new InvalidDescriptionException(Description.NOROOT_EXCEPTION, file);
			deviceNode = rootNode.getNode(Device.ELEM_NAME);
			if (deviceNode == null)
				throw new InvalidDescriptionException(Description.NOROOTDEVICE_EXCEPTION, file);
		} catch (ParserException e)
		{
			throw new InvalidDescriptionException(e);
		}

		if (initializeLoadedDescription() == false)
			return false;

		setDescriptionFile(file);

		return true;
	}

	private boolean initializeLoadedDescription()
	{
		setDescriptionURI(DEFAULT_DESCRIPTION_URI);
		setLeaseTime(DEFAULT_LEASE_TIME);
		setHTTPPort(HTTP_DEFAULT_PORT);

		// Thanks for Oliver Newell (03/23/04)
		if (hasUDN() == false)
			updateUDN();

		return true;
	}

	////////////////////////////////////////////////
	//	isDeviceNode
	////////////////////////////////////////////////

	public static boolean isDeviceNode(Node node)
	{
		return Device.ELEM_NAME.equals(node.getName());
	}

	////////////////////////////////////////////////
	//	Root Device
	////////////////////////////////////////////////

	public boolean isRootDevice()
	{
		Node rootNode = getRootNode();
		if (rootNode != null)
		{
			Node deviceNode = rootNode.getNode("device");
			if (deviceNode != null)
			{
				String value = deviceNode.getNodeValue(UDN);
				if (value != null)
				{
					return value.equals(getUDN());
				}
			}
		}
		//return getRootNode().getNode("device").getNodeValue(UDN).equals(getUDN());
		return false;
	}

	////////////////////////////////////////////////
	//	Root Device
	////////////////////////////////////////////////

	public void setSSDPPacket(SSDPPacket packet)
	{
		getDeviceData().setSSDPPacket(packet);
	}

	public SSDPPacket getSSDPPacket()
	{
		if (isRootDevice() == false)
			return null;
		return getDeviceData().getSSDPPacket();
	}

	////////////////////////////////////////////////
	//	Location 
	////////////////////////////////////////////////

	public void setLocation(String value)
	{
		getDeviceData().setLocation(value);
	}

	public String getLocation()
	{
		SSDPPacket packet = getSSDPPacket();
		if (packet != null)
			return packet.getLocation();
		return getDeviceData().getLocation();
	}

	////////////////////////////////////////////////
	//	LeaseTime 
	////////////////////////////////////////////////

	public void setLeaseTime(int value)
	{
		getDeviceData().setLeaseTime(value);
		Advertiser adv = getAdvertiser();
		if (adv != null)
		{
			announce();
			adv.restart("Advertiser");
		}
	}

	public int getLeaseTime()
	{
		SSDPPacket packet = getSSDPPacket();
		if (packet != null)
			return packet.getLeaseTime();
		return getDeviceData().getLeaseTime();
	}

	////////////////////////////////////////////////
	//	TimeStamp 
	////////////////////////////////////////////////

	public long getTimeStamp()
	{
		SSDPPacket packet = getSSDPPacket();
		if (packet != null)
			return packet.getTimeStamp();
		return 0;
	}

	public long getElapsedTime()
	{
		return (System.currentTimeMillis() - getTimeStamp()) / 1000;
	}

	public boolean isExpired()
	{
		//		long elipsedTime = getElapsedTime();
		//		long leaseTime = getLeaseTime();
		//
		//		if (leaseTime == 0)
		//			leaseTime = Device.DEFAULT_LEASE_TIME;

		// 这里为了满足产品对快速移除断电设备的要求，把设备超时时间统一设置为３０秒
		// 因为QIMO设计每５秒就会发送SSDP Search,所以30秒足够刷新6次时间戳
		long elipsedTime = getElapsedTime();
		long leaseTime = Device.DEFAULT_LEASE_TIME;

		if (leaseTime < elipsedTime)
			return true;

		return false;
	}

	////////////////////////////////////////////////
	//	URL Base
	////////////////////////////////////////////////

	private final static String URLBASE_NAME = "URLBase";

	private void setURLBase(String value)
	{
		if (isRootDevice() == true)
		{
			Node node = getRootNode().getNode(URLBASE_NAME);
			if (node != null)
			{
				node.setValue(value);
				return;
			}
			node = new Node(URLBASE_NAME);
			node.setValue(value);
			int index = 1;
			if (getRootNode().hasNodes() == false)
				index = 1;
			getRootNode().insertNode(node, index);
		}
	}

	private void updateURLBase(String host)
	{
		String urlBase = HostInterface.getHostURL(host, getHTTPPort(), "");
		setURLBase(urlBase);
	}

	public String getURLBase()
	{
		if (isRootDevice() == true)
		{
			if (getRootNode() != null)
			{
				return getRootNode().getNodeValue(URLBASE_NAME);
			}
		}
		return "";
	}

	////////////////////////////////////////////////
	//	deviceType
	////////////////////////////////////////////////

	private final static String DEVICE_TYPE = "deviceType";

	public void setDeviceType(String value)
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.setNode(DEVICE_TYPE, value);
		}
		//getDeviceNode().setNode(DEVICE_TYPE, value);
	}

	public String getDeviceType()
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			return deviceNode.getNodeValue(DEVICE_TYPE);
		}
		return "";
		//return getDeviceNode().getNodeValue(DEVICE_TYPE);
	}

	public boolean isDeviceType(String value)
	{
		if (value == null)
			return false;
		return value.equals(getDeviceType());
	}

	/*
	 * 设备的基本信息设置
	 * 
	 */
	private final static String FRIENDLY_NAME = "friendlyName";

	public void setFriendlyName(String value)
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.setNode(FRIENDLY_NAME, value);
		}
		//getDeviceNode().setNode(FRIENDLY_NAME, value);
		if (cacheMap != null)
		{
			cacheMap.clear();
		}

		byebye();
	}

	public String getFriendlyName()
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			return deviceNode.getNodeValue(FRIENDLY_NAME);
		}
		return "";
		//return getDeviceNode().getNodeValue(FRIENDLY_NAME);
	}

	void setInternalFriendlyName(String value)
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.setNode(FRIENDLY_NAME, value);
		}
	}

	private final static String MANUFACTURE = "manufacturer";

	public void setManufacture(String value)
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.setNode(MANUFACTURE, value);
		}
		//getDeviceNode().setNode(MANUFACTURE, value);
	}

	public String getManufacture()
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			return deviceNode.getNodeValue(MANUFACTURE);
		}
		return "";
		//return getDeviceNode().getNodeValue(MANUFACTURE);
	}

	private final static String MANUFACTURE_URL = "manufacturerURL";

	public void setManufactureURL(String value)
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.setNode(MANUFACTURE_URL, value);
		}
		//getDeviceNode().setNode(MANUFACTURE_URL, value);
	}

	public String getManufactureURL()
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			return deviceNode.getNodeValue(MANUFACTURE_URL);
		}
		return "";
		//return getDeviceNode().getNodeValue(MANUFACTURE_URL);
	}

	private final static String QPlay_SoftwareCapability = "qq:X_QPlay_SoftwareCapability";

	public void setQPlaySoftwareCapability(String value)
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.setNode(QPlay_SoftwareCapability, value, "qq", "http://www.tencent.com");
		}
	}

	public String getQPlaySoftwareCapability()
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			return deviceNode.getNodeValue(QPlay_SoftwareCapability);
		}
		return "";
	}

	private final static String MODEL_DESCRIPTION = "modelDescription";

	public void setModelDescription(String value)
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.setNode(MODEL_DESCRIPTION, value);
		}
		//getDeviceNode().setNode(MODEL_DESCRIPTION, value);
	}

	public String getModelDescription()
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			return deviceNode.getNodeValue(MODEL_DESCRIPTION);
		}
		return "";
		//return getDeviceNode().getNodeValue(MODEL_DESCRIPTION);
	}

	private final static String MODEL_NAME = "modelName";

	public void setModelName(String value)
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.setNode(MODEL_NAME, value);
		}
		//getDeviceNode().setNode(MODEL_NAME, value);
	}

	public String getModelName()
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			return deviceNode.getNodeValue(MODEL_NAME);
		}
		return "";
		//return getDeviceNode().getNodeValue(MODEL_NAME);
	}

	private final static String MODEL_NUMBER = "modelNumber";

	public void setModelNumber(String value)
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.setNode(MODEL_NUMBER, value);
		}
		//getDeviceNode().setNode(MODEL_NUMBER, value);
	}

	public String getModelNumber()
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			return deviceNode.getNodeValue(MODEL_NUMBER);
		}
		return "";
		//return getDeviceNode().getNodeValue(MODEL_NUMBER);
	}

	private final static String MODEL_URL = "modelURL";

	public void setModelURL(String value)
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.setNode(MODEL_URL, value);
		}
		//getDeviceNode().setNode(MODEL_URL, value);
	}

	public String getModelURL()
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			return deviceNode.getNodeValue(MODEL_URL);
		}
		return "";
		//return getDeviceNode().getNodeValue(MODEL_URL);
	}

	private final static String DLNA_DOC = "dlna:X_DLNADOC";

	public void setDLNADOC(String value)
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			Node node = deviceNode.getNode(DLNA_DOC);
			if (node != null)
			{
				node.setNameSpace(RootDescription.ROOT_ELEMENT_NSDLNA, RootDescription.ROOT_ELEMENT_DLNANAMESPACE);
				node.setValue(value);
				return;
			}
			node = new Node(DLNA_DOC);
			node.setNameSpace(RootDescription.ROOT_ELEMENT_NSDLNA, RootDescription.ROOT_ELEMENT_DLNANAMESPACE);
			node.setValue(value);
			deviceNode.addNode(node);
		}
	}

	public String getDLNADOC()
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			return deviceNode.getNodeValue(DLNA_DOC);
		}
		return "";
		//return getDeviceNode().getNodeValue(DLNA_DOC);
	}

	private final static String SERIAL_NUMBER = "serialNumber";

	public void setSerialNumber(String value)
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.setNode(SERIAL_NUMBER, value);
		}
		//getDeviceNode().setNode(SERIAL_NUMBER, value);
	}

	public String getSerialNumber()
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			return deviceNode.getNodeValue(SERIAL_NUMBER);
		}
		//return getDeviceNode().getNodeValue(SERIAL_NUMBER);
		return "";
	}

	private final static String UDN = "UDN";

	public void setUDN(String value)
	{
		if (value.contains("uuid:") == false)
		{
			value = "uuid:" + value;//将uuid补上
		}
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.setNode(UDN, value);
		}
		//getDeviceNode().setNode(UDN, value);
	}

	public String getUDN()
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			return deviceNode.getNodeValue(UDN);
		}
		//return getDeviceNode().getNodeValue(UDN);
		return "";
	}

	public boolean hasUDN()
	{
		String udn = getUDN();
		if (udn == null || udn.length() <= 0)
			return false;
		return true;
	}

	/////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////
	//	UPC
	////////////////////////////////////////////////

	private final static String UPC = "UPC";

	public void setUPC(String value)
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.setNode(UPC, value);
		}
		//getDeviceNode().setNode(UPC, value);
	}

	public String getUPC()
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			return deviceNode.getNodeValue(UPC);
		}
		return "";
	}

	////////////////////////////////////////////////
	//	presentationURL
	////////////////////////////////////////////////

	private final static String presentationURL = "presentationURL";

	public void setPresentationURL(String value)
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.setNode(presentationURL, value);
		}
		//getDeviceNode().setNode(presentationURL, value);
	}

	public String getPresentationURL()
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			return deviceNode.getNodeValue(presentationURL);
		}
		return "";
		//return getDeviceNode().getNodeValue(presentationURL);
	}

	private final static String PACKAGE_NAME = "PackageName";

	public void setPackageName(String value)
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.setNode(PACKAGE_NAME, value);
		}
	}

	public String getPackageName()
	{
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			return (deviceNode.getNodeValue(PACKAGE_NAME));
		}
		return "";
	}

	////////////////////////////////////////////////
	//	deviceList
	////////////////////////////////////////////////

	public DeviceList getDeviceList()
	{
		DeviceList devList = new DeviceList();

		Node deviceNode = getDeviceNode();
		if (deviceNode == null)
			return devList;

		Node devListNode = deviceNode.getNode(DeviceList.ELEM_NAME);
		if (devListNode == null)
			return devList;

		int nNode = devListNode.getNNodes();
		for (int n = 0; n < nNode; n++)
		{
			Node node = devListNode.getNode(n);
			if (Device.isDeviceNode(node) == false)
				continue;
			Device dev = new Device(node);
			devList.add(dev);
		}
		return devList;
	}

	public boolean isDevice(String name)
	{
		if (name == null)
			return false;
		if (name.endsWith(getUDN()) == true)
			return true;
		if (name.equals(getFriendlyName()) == true)
			return true;
		if (name.endsWith(getDeviceType()) == true)
			return true;
		return false;
	}

	public Device getDevice(String name)
	{
		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++)
		{
			Device dev = devList.getDevice(n);
			if (dev.isDevice(name) == true)
				return dev;
			Device cdev = dev.getDevice(name);
			if (cdev != null)
				return cdev;
		}
		return null;
	}

	public Device getDeviceByDescriptionURI(String uri)
	{
		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++)
		{
			Device dev = devList.getDevice(n);
			if (dev.isDescriptionURI(uri) == true)
				return dev;
			Device cdev = dev.getDeviceByDescriptionURI(uri);
			if (cdev != null)
				return cdev;
		}
		return null;
	}

	////////////////////////////////////////////////
	//	serviceList
	////////////////////////////////////////////////

	public ServiceList getServiceList()
	{
		ServiceList serviceList = new ServiceList();
		Node serviceListNode = getDeviceNode().getNode(ServiceList.ELEM_NAME);
		if (serviceListNode == null)
			return serviceList;
		int nNode = serviceListNode.getNNodes();
		for (int n = 0; n < nNode; n++)
		{
			Node node = serviceListNode.getNode(n);
			if (Service.isServiceNode(node) == false)
				continue;
			Service service = new Service(node);
			serviceList.add(service);
		}
		return serviceList;
	}

	public Service getService(String name)
	{
		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++)
		{
			Service service = serviceList.getService(n);
			if (service.isService(name) == true)
				return service;
		}

		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++)
		{
			Device dev = devList.getDevice(n);
			Service service = dev.getService(name);
			if (service != null)
				return service;
		}

		return null;
	}

	public Service getServiceBySCPDURL(String searchUrl)
	{
		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++)
		{
			Service service = serviceList.getService(n);
			if (service.isSCPDURL(searchUrl) == true)
				return service;
		}

		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++)
		{
			Device dev = devList.getDevice(n);
			Service service = dev.getServiceBySCPDURL(searchUrl);
			if (service != null)
				return service;
		}

		return null;
	}

	public Service getServiceByControlURL(String searchUrl)
	{
		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++)
		{
			Service service = serviceList.getService(n);
			if (service.isControlURL(searchUrl) == true)
				return service;
		}

		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++)
		{
			Device dev = devList.getDevice(n);
			Service service = dev.getServiceByControlURL(searchUrl);
			if (service != null)
				return service;
		}

		return null;
	}

	public Service getServiceByEventSubURL(String searchUrl)
	{
		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++)
		{
			Service service = serviceList.getService(n);
			if (service.isEventSubURL(searchUrl) == true)
				return service;
		}

		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++)
		{
			Device dev = devList.getDevice(n);
			Service service = dev.getServiceByEventSubURL(searchUrl);
			if (service != null)
				return service;
		}

		return null;
	}

	public Service getSubscriberService(String uuid)
	{
		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++)
		{
			Service service = serviceList.getService(n);
			String sid = service.getSID();
			if (uuid.equals(sid) == true)
				return service;
		}

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
	//	StateVariable
	////////////////////////////////////////////////

	public StateVariable getStateVariable(String serviceType, String name)
	{
		if (serviceType == null && name == null)
			return null;

		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++)
		{
			Service service = serviceList.getService(n);
			// Thanks for Theo Beisch (11/09/04)
			if (serviceType != null)
			{
				if (service.getServiceType().equals(serviceType) == false)
					continue;
			}
			StateVariable stateVar = service.getStateVariable(name);
			if (stateVar != null)
				return stateVar;
		}

		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++)
		{
			Device dev = devList.getDevice(n);
			StateVariable stateVar = dev.getStateVariable(serviceType, name);
			if (stateVar != null)
				return stateVar;
		}

		return null;
	}

	public StateVariable getStateVariable(String name)
	{
		return getStateVariable(null, name);
	}

	////////////////////////////////////////////////
	//	Action
	////////////////////////////////////////////////

	public Action getAction(String name)
	{
		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++)
		{
			Service service = serviceList.getService(n);
			ActionList actionList = service.getActionList();
			int actionCnt = actionList.size();
			for (int i = 0; i < actionCnt; i++)
			{
				Action action = (Action) actionList.getAction(i);
				String actionName = action.getName();
				if (actionName == null)
					continue;
				if (actionName.equals(name) == true)
					return action;
			}
		}

		DeviceList devList = getDeviceList();
		int devCnt = devList.size();
		for (int n = 0; n < devCnt; n++)
		{
			Device dev = devList.getDevice(n);
			Action action = dev.getAction(name);
			if (action != null)
				return action;
		}

		return null;
	}

	////////////////////////////////////////////////
	//	iconList
	////////////////////////////////////////////////

	public void setIconList(IconList iconList)
	{
		if (iconList == null)
			return;
		Node iconListNode = getDeviceNode().getNode(IconList.ELEM_NAME);
		if (iconListNode == null)//创建一个iconlist的节点
		{
			iconListNode = new Node(IconList.ELEM_NAME);
		}
		for (int i = 0; i < iconList.size(); i++)
		{
			Icon icon = iconList.getIcon(i);
			iconListNode.addNode(icon.getIconNode());
		}
		Node deviceNode = getDeviceNode();
		if (deviceNode != null)
		{
			deviceNode.addNode(iconListNode);
		}
	}

	public IconList getIconList()
	{
		IconList iconList = new IconList();
		Node iconListNode = getDeviceNode().getNode(IconList.ELEM_NAME);
		if (iconListNode == null)
			return iconList;
		int nNode = iconListNode.getNNodes();
		for (int n = 0; n < nNode; n++)
		{
			Node node = iconListNode.getNode(n);
			if (Icon.isIconNode(node) == false)
				continue;
			Icon icon = new Icon(node);
			iconList.add(icon);
		}
		return iconList;
	}

	public Icon getIcon(int n)
	{
		IconList iconList = getIconList();
		if (n < 0 && (iconList.size() - 1) < n)
			return null;
		return iconList.getIcon(n);
	}

	public Icon getSmallestIcon()
	{
		Icon smallestIcon = null;
		IconList iconList = getIconList();
		int iconCount = iconList.size();
		for (int n = 0; n < iconCount; n++)
		{
			Icon icon = iconList.getIcon(n);
			if (null == smallestIcon)
			{
				smallestIcon = icon;
				continue;
			}
			if (icon.getWidth() < smallestIcon.getWidth())
				smallestIcon = icon;
		}

		return smallestIcon;
	}

	////////////////////////////////////////////////
	//GetClientList
	////////////////////////////////////////////////
	public Vector<String> getClientList()
	{
		Vector<String> result = new Vector<String>();

		QiyiHttpServerList httpServerList = getQiyiHttpServerList();
		int nServers = httpServerList.size();
		for (int n = 0; n < nServers; n++)
		{
			QiyiHttpServer server = httpServerList.getHTTPServer(n);
			result.addAll(server.getClientList());
		}

		return result;
	}

	////////////////////////////////////////////////
	//	Notify
	////////////////////////////////////////////////

	public String getLocationURL(String host)
	{
		return HostInterface.getHostURL(host, getHTTPPort(), getDescriptionURI());
	}

	private String getNotifyDeviceNT()
	{
		if (isRootDevice() == false)
			return getUDN();
		return UPNP_ROOTDEVICE;
	}

	private String getNotifyDeviceUSN()
	{
		if (isRootDevice() == false)
			return getUDN();
		return getUDN() + "::" + UPNP_ROOTDEVICE;
	}

	private String getNotifyDeviceTypeNT()
	{
		return getDeviceType();
	}

	private String getNotifyDeviceTypeUSN()
	{
		return getUDN() + "::" + getDeviceType();
	}

	public final static void notifyWait()
	{
		TimerUtil.waitRandom(DEFAULT_DISCOVERY_WAIT_TIME);
	}

	// private SSDPNotifySocket mSSDPNotifySock = null;

	private Map<String, SSDPNotifySocket> mSSDPNotifySockList = new HashMap<String,SSDPNotifySocket>();

	/*
	 * 宣告在线
	 */
	public void announce(String bindAddr)
	{
		notifyWait();
		String devLocation = getLocationURL(bindAddr);

		SSDPNotifySocket sock = mSSDPNotifySockList.get(bindAddr);
		if (sock == null)
		{
			sock = new SSDPNotifySocket(bindAddr);
			sock.openBroad(bindAddr);

			Debug.message("mSSDPNotifySockList add " + bindAddr + " sock " + sock.toString());
			mSSDPNotifySockList.put(bindAddr, sock);
		}

		SSDPNotifyRequest ssdpReq = SSDPNotifyRequest.getInstance();
		ssdpReq.setServer(UPnP.getServerName());
		ssdpReq.setLocation(devLocation);
		ssdpReq.setNTS(NTS.ALIVE);
		ssdpReq.setLeaseTime(DEFAULT_LEASE_TIME);
		//对ssdp的信息进行扩展，在多播消息中，加入了设备名，这个对于设备发现过程的优化有重要的作用
		ssdpReq.setMYNAME(getFriendlyName());
		//扩展设备描述文件的md5的值，这个值，在多播的时候进行加入
		ssdpReq.setFileMd5(getDescriptionXmlMd5());
		ssdpReq.setConnect(false);
		ssdpReq.setIQIYIDEVICE(mDeviceName);
		ssdpReq.setIQIYIVERSION(IQIYI_VERSION);
		ssdpReq.setDEVICEVERSION(mDeviceVersion);
		ssdpReq.setTVGUOFEATUREBITMAP(mTVGuoFeatureBitmap);
		ssdpReq.setTVGUOMARKETCHANNEL(mTVGuoMarketChannel);

		//扩展设备连接信息和联网时间
		if (getLinkedIp() != null)
		{
			ssdpReq.setLinkedIP(getLinkedIp());
			ssdpReq.setElapseTime((System.nanoTime() - getLinkedIpTime()) / 1000000);
		}  else {
			ssdpReq.setLinkedIP("0.0.0.0");
			ssdpReq.setElapseTime(0);
		}

		//扩展设备SN编号
		if (getTvguoSN() != null)
		{
			ssdpReq.setTvguoSN(getTvguoSN());
		}

		// 扩展设备的pcba
		String pcba = getTvguoPCBA();
		if (!TextUtils.isEmpty(pcba)) {
			ssdpReq.setTvGuoPCBA(pcba);
		}

		if (isQuicklySend() == true)//要开启快速发送通道
		{
			ssdpReq.setIQIYIPORT(getQiyiHTTPPort());
			ssdpReq.setIQIYIUDPPORT(getUdpQiyiHttpPort());
		}

		// uuid:device-UUID(::upnp:rootdevice)* 
		if (isRootDevice() == true)
		{
			String devNT = getNotifyDeviceNT();
			String devUSN = getNotifyDeviceUSN();
			ssdpReq.setNT(devNT);
			ssdpReq.setUSN(devUSN);
			sock.post(ssdpReq);//这里需要改动一下，不然在android出现私有地址的时候，就有问题了

			String devUDN = getUDN();
			ssdpReq.setNT(devUDN);
			ssdpReq.setUSN(devUDN);
			sock.post(ssdpReq); //这里需要改动一下，不然在android出现私有地址的时候，就有问题了
		}

		// uuid:device-UUID::urn:schemas-upnp-org:device:deviceType:v 
		String devNT = getNotifyDeviceTypeNT();
		String devUSN = getNotifyDeviceTypeUSN();
		ssdpReq.setNT(devNT);
		ssdpReq.setUSN(devUSN);
		Debug.message("announce info usn: " + devUSN + " getHTTPPort " + getHTTPPort());
		sock.post(ssdpReq);//这里需要改动一下，不然在android出现私有地址的时候，就有问题了

		// Thanks for Mikael Hakman (04/25/05)
		//		ssdpSock.close();
		//		ssdpSock.closeBroad();

		// 单播Notify和Search Resp
		if (dmcAddrList.size() > 0)
		{
			for (SSDPPacket temp : dmcAddrList)
			{
				deviceSearchResponse(temp);
			}
		}

		//		ServiceList serviceList = getServiceList();
		//		int serviceCnt = serviceList.size();
		//		for (int n=0; n<serviceCnt; n++) 
		//		{
		//			Service service = serviceList.getService(n);
		//			service.announce(bindAddr,bindssdpPort);
		//		}
		//
		//		DeviceList childDeviceList = getDeviceList();
		//		int childDeviceCnt = childDeviceList.size();
		//		for (int n=0; n<childDeviceCnt; n++) {
		//			Device childDevice = childDeviceList.getDevice(n);
		//			childDevice.announce(bindAddr);
		//		}
	}

	public synchronized void announce()
	{
		InetAddress[] binds = getDeviceData().getHTTPBindAddress();
		String[] bindAddresses;

		if (binds != null)
		{
			bindAddresses = new String[binds.length];
			for (int i = 0; i < binds.length; i++)
			{
				bindAddresses[i] = binds[i].getHostAddress();
			}
		} else
		{
			int nHostAddrs = HostInterface.getNHostAddresses();
			bindAddresses = new String[nHostAddrs];
			for (int n = 0; n < nHostAddrs; n++)
			{
				bindAddresses[n] = HostInterface.getHostAddress(n);
			}
		}
		for (int j = 0; j < bindAddresses.length; j++)
		{
			if (bindAddresses[j] == null || bindAddresses[j].length() == 0)
				continue;
			if (HostInterface.isIPv6Address(bindAddresses[j]) == true)
				continue; // Ignore IPV6
			int ssdpCount = getSSDPAnnounceCount();
			for (int i = 0; i < ssdpCount; i++)
			{
				announce(bindAddresses[j]);
			}
		}
	}

	public void byebye(String bindAddr)
	{
		Debug.message("byebye:" + bindAddr);
		SSDPNotifySocket sock = mSSDPNotifySockList.get(bindAddr);
		if (sock == null)
		{
			sock = new SSDPNotifySocket(bindAddr);
			sock.openBroad(bindAddr);
			mSSDPNotifySockList.put(bindAddr, sock);
		}

		SSDPNotifyRequest ssdpReq = SSDPNotifyRequest.getInstance();
		ssdpReq.setNTS(NTS.BYEBYE);
		// uuid:device-UUID(::upnp:rootdevice)* 
		if (isRootDevice() == true)
		{
			String devNT = getNotifyDeviceNT();
			String devUSN = getNotifyDeviceUSN();
			ssdpReq.setNT(devNT);
			ssdpReq.setUSN(devUSN);
			sock.post(ssdpReq);//这里需要改动一下，不然在android出现私有地址的时候，就有问题了

			String devUDN = getUDN();
			ssdpReq.setNT(devUDN);
			ssdpReq.setUSN(devUDN);
			sock.post(ssdpReq); //这里需要改动一下，不然在android出现私有地址的时候，就有问题了
		}

		// uuid:device-UUID::urn:schemas-upnp-org:device:deviceType:v 
		String devNT = getNotifyDeviceTypeNT();
		String devUSN = getNotifyDeviceTypeUSN();
		ssdpReq.setNT(devNT);
		ssdpReq.setUSN(devUSN);
		sock.post(ssdpReq);//这里需要改动一下，不然在android出现私有地址的时候，就有问题了

		// Thanks for Mikael Hakman (04/25/05)
		//		ssdpSock.close();
		//		ssdpSock.closeBroad();

		//		ServiceList serviceList = getServiceList();
		//		int serviceCnt = serviceList.size();
		//		for (int n = 0; n < serviceCnt; n++)
		//		{
		//			Service service = serviceList.getService(n);
		//			service.byebye(bindAddr);
		//		}
		//
		//		DeviceList childDeviceList = getDeviceList();
		//		int childDeviceCnt = childDeviceList.size();
		//		for (int n = 0; n < childDeviceCnt; n++)
		//		{
		//			Device childDevice = childDeviceList.getDevice(n);
		//			childDevice.byebye(bindAddr);
		//		}
	}

	public synchronized void byebye()
	{
		InetAddress[] binds = getDeviceData().getHTTPBindAddress();
		String[] bindAddresses;
		if (binds != null)
		{
			bindAddresses = new String[binds.length];
			for (int i = 0; i < binds.length; i++)
			{
				bindAddresses[i] = binds[i].getHostAddress();
			}
		} else
		{
			int nHostAddrs = HostInterface.getNHostAddresses();
			bindAddresses = new String[nHostAddrs];
			for (int n = 0; n < nHostAddrs; n++)
			{
				bindAddresses[n] = HostInterface.getHostAddress(n);
			}
		}

		for (int j = 0; j < bindAddresses.length; j++)
		{
			if (bindAddresses[j] == null || bindAddresses[j].length() <= 0)
				continue;
			int ssdpCount = getSSDPAnnounceCount();
			for (int i = 0; i < ssdpCount; i++)
				byebye(bindAddresses[j]);
		}
	}

	////////////////////////////////////////////////
	//	Search
	////////////////////////////////////////////////

	private static Calendar cal = Calendar.getInstance();

	public boolean postSearchResponse(SSDPPacket ssdpPacket, String st, String usn)
	{
		String localAddr = ssdpPacket.getLocalAddress();
		Device rootDev = getRootDevice();
		if (rootDev == null)
		{
			Debug.message("Oops, rootDev null");
			return false;
		}
		String rootDevLocation = rootDev.getLocationURL(localAddr);
		String remoteAddr = ssdpPacket.getRemoteAddress();
		if (localAddr.equals(remoteAddr))
			return true;

		SSDPSearchResponse ssdpRes = SSDPSearchResponse.getInstance();
		ssdpRes.setLeaseTime(getLeaseTime());
		ssdpRes.setDate(cal);
		ssdpRes.setST(st);
		ssdpRes.setUSN(usn);
		ssdpRes.setLocation(rootDevLocation);
		ssdpRes.setMYNAME(getFriendlyName());
		ssdpRes.setFileMd5(getDescriptionXmlMd5());//获取设备描述文件的md5值
		ssdpRes.setConnect(false);
		ssdpRes.setIQIYIDEVICE(mDeviceName);
		ssdpRes.setIQIYIVERSION(IQIYI_VERSION);
		ssdpRes.setDEVICEVERSION(mDeviceVersion);
		ssdpRes.setTVGUOFEATUREBITMAP(mTVGuoFeatureBitmap);
		ssdpRes.setTVGUOMARKETCHANNEL(mTVGuoMarketChannel);

		if (getLinkedIp() != null)
		{
			ssdpRes.setLinkedIP(getLinkedIp());
			ssdpRes.setElapseTime((System.nanoTime() - getLinkedIpTime()) / 1000000);
		} else {
			ssdpRes.setLinkedIP("0.0.0.0");
			ssdpRes.setElapseTime(0);
		}

		//扩展设备SN编号
		if (getTvguoSN() != null)
		{
			ssdpRes.setTvguoSN(getTvguoSN());
		}

		// 扩展设备的pcba
		String pcba = getTvguoPCBA();
		if (!TextUtils.isEmpty(pcba)) {
			ssdpRes.setTvGuoPCBA(pcba);
		}

		if (isQuicklySend() == true)//要开启快速发送通道
		{
			ssdpRes.setIQIYIPORT(getQiyiHTTPPort());
			ssdpRes.setIQIYIUDPPORT(getUdpQiyiHttpPort());//udp消息通道
		}

		//int mx = ssdpPacket.getMX();
		//TimerUtil.waitRandom(mx*1000);//现在我们要求是0S

		//String remoteAddr = ssdpPacket.getRemoteAddress();
		int remotePort = ssdpPacket.getRemotePort();
		SSDPSearchResponseSocket ssdpResSock = SSDPSearchResponseSocket.getInstance();
		if (Debug.isOn() == true)
			ssdpRes.print();
		int ssdpCount = getSSDPAnnounceCount();
		for (int i = 0; i < ssdpCount; i++)
		{
			ssdpResSock.post(remoteAddr, remotePort, ssdpRes);
			//TimerUtil.waitRandom(50);//50ms随机一段时间等待
		}

		return true;
	}

	public boolean postSearchResponseSimple(SSDPPacket ssdpPacket, String st, String usn)
	{
		String localAddr = ssdpPacket.getLocalAddress();
		Device rootDev = getRootDevice();
		if (rootDev == null)
		{
			Debug.message("Oops, rootDev null");
			return false;
		}
		String rootDevLocation = rootDev.getLocationURL(localAddr);
		String remoteAddr = ssdpPacket.getRemoteAddress();
		if (localAddr.equals(remoteAddr))
			return true;

		SSDPSearchResponse ssdpRes = null;

		ssdpRes = new SSDPSearchResponse();
		ssdpRes.setHeader(HTTP.SERVER, "Linux/3.4.67 UPnP/1.0 StandardDLNA/1.0");
		ssdpRes.setLeaseTime(DLNA_SEARCH_LEASE_TIME);
		ssdpRes.setDate(cal);
		ssdpRes.setST(st);
		ssdpRes.setUSN(usn);
		ssdpRes.setLocation(rootDevLocation);

		// int mx = ssdpPacket.getMX();
		// TimerUtil.waitRandom(mx*1000);//现在我们要求是0S

		// String remoteAddr = ssdpPacket.getRemoteAddress();
		int remotePort = ssdpPacket.getRemotePort();
		SSDPSearchResponseSocket ssdpResSock = SSDPSearchResponseSocket.getInstance();
		if (Debug.isOn() == true)
			ssdpRes.print();
		int ssdpCount = getSSDPAnnounceCount();
		for (int i = 0; i < ssdpCount; i++)
		{
			ssdpResSock.post(remoteAddr, remotePort, ssdpRes);
			// TimerUtil.waitRandom(100);//100ms随机一段时间等待
		}

		return true;
	}

	public void deviceSearchResponse(SSDPPacket ssdpPacket)
	{
		String ssdpST = ssdpPacket.getST();

		if (ssdpST == null)
			return;

		boolean isRootDevice = isRootDevice();

		String devUSN = getUDN();
		if (isRootDevice == true)
			devUSN += "::" + USN.ROOTDEVICE;

		if (ST.isAllDevice(ssdpST) == true)
		{
			String devNT = getNotifyDeviceNT();
			int repeatCnt = (isRootDevice == true) ? 3 : 2;
			for (int n = 0; n < repeatCnt; n++)
				postSearchResponse(ssdpPacket, devNT, devUSN);
		} else if (ST.isRootDevice(ssdpST) == true)
		{
			if (isRootDevice == true) {
				// Workaround for YOUKU which can not parse our M-Search response with Chinese device name.
				String tmp = ssdpPacket.getHEADERCAT();
				if (ssdpPacket.getMX() == 5 && tmp.contains("MANMXST")) {
					Debug.message("Workaround for YOUKU: MX=5 & MANMXST");
					postSearchResponseSimple(ssdpPacket, ST.ROOT_DEVICE, devUSN);
				} else {
					postSearchResponse(ssdpPacket, ST.ROOT_DEVICE, devUSN);
				}
			}
		} else if (ST.isUUIDDevice(ssdpST) == true)
		{
			String devUDN = getUDN();
			if (ssdpST.equals(devUDN) == true)
				postSearchResponse(ssdpPacket, devUDN, devUSN);
		} else if (ST.isURNDevice(ssdpST) == true)
		{
			String devType = getDeviceType();
			if (ssdpST.equals(devType) == true)
			{
				// Thanks for Mikael Hakman (04/25/05)
				devUSN = getUDN() + "::" + devType;
				postSearchResponseSimple(ssdpPacket, devType, devUSN);
				postSearchResponse(ssdpPacket, devType, devUSN);
			}
		}

		ServiceList serviceList = getServiceList();
		int serviceCnt = serviceList.size();
		for (int n = 0; n < serviceCnt; n++)
		{
			Service service = serviceList.getService(n);
			service.serviceSearchResponse(ssdpPacket);
		}

		DeviceList childDeviceList = getDeviceList();
		int childDeviceCnt = childDeviceList.size();
		for (int n = 0; n < childDeviceCnt; n++)
		{
			Device childDevice = childDeviceList.getDevice(n);
			childDevice.deviceSearchResponse(ssdpPacket);
		}
	}

	private CopyOnWriteArrayList<SSDPPacket> dmcAddrList = new CopyOnWriteArrayList<SSDPPacket>();

	private void addRemoteDmcAddr(SSDPPacket ssdpPacket)
	{
		if (dmcAddrList.isEmpty() == false)
		{
			for (SSDPPacket temp : dmcAddrList)
			{
				if (temp.getRemoteAddress().equals(ssdpPacket.getRemoteAddress())
						&& temp.getRemotePort() == ssdpPacket.getRemotePort())
					return;
			}
			if (dmcAddrList.size() > 2)
			{
				dmcAddrList.remove(0);
			}
		}
		dmcAddrList.add(ssdpPacket);
	}

	public void deviceSearchReceived(SSDPPacket ssdpPacket)
	{
		addRemoteDmcAddr(ssdpPacket);
		deviceSearchResponse(ssdpPacket);
	}

	////////////////////////////////////////////////
	//	HTTP Server	
	////////////////////////////////////////////////

	public void setHTTPPort(int port)
	{
		getDeviceData().setHTTPPort(port);
	}

	public int getHTTPPort()
	{
		return getDeviceData().getHTTPPort();
	}

	//DMR
	public void setIQIYIDEVICE(int deviceName)
	{
		this.mDeviceName = deviceName;
	}

	//DMR
	public void setDONGLEVERSION(int version)
	{
		this.mDeviceVersion = version;
	}

	//DMR
	public void setTVGUOFEATUREBITMAP(int bitmap)
	{
		this.mTVGuoFeatureBitmap = bitmap;
	}

	//DMR
	public void setTVGUOMARKETCHANNEL(long channel)
	{
		this.mTVGuoMarketChannel = channel;
	}

	//DMC
	public void setDeviceName(int deviceName)
	{
		getDeviceData().setQiyiDeviceType(deviceName);
	}

	//DMC
	public int getDeviceName()
	{
		return getDeviceData().getQiyiDeviceType();
	}

	//DMC
	public void setDeviceVersion(int deviceVersion)
	{
		getDeviceData().setQiyiVersion(deviceVersion);
	}

	//DMC
	public int getDeviceVersion()
	{
		return getDeviceData().getQiyiVersion();
	}

	//DMC
	public void setQiyiDeviceVersion(int version)
	{
		getDeviceData().setQiyiDeviceVersion(version);
	}

	//DMC
	public int getQiyiDeviceVersion()
	{
		return getDeviceData().getQiyiDeviceVersion();
	}

	//DMC
	public void setTvguoFeatureBitmap(int bitmap)
	{
		getDeviceData().setTvguoFeatureBitmap(bitmap);
	}

	//DMC
	public int getTvguoFeatureBitmap()
	{
		return getDeviceData().getTvguoFeatureBitmap();
	}

	//DMC
	public void setTvguoMarketChannel(long channel)
	{
		getDeviceData().setTvguoMarketChannel(channel);
	}

	//DMC
	public long getTvguoMarketChannel()
	{
		return getDeviceData().getTvguoMarketChannel();
	}

	/*
	 * 从组播消息中获取tcp httpPort的消息
	 */
	public int getQiyiHTTPPortFromSSDP()
	{
		//		int qiyiPort=getQiyiHTTPPort();//第一次为0
		//		if(qiyiPort==0)
		//		{
		//			SSDPPacket ssdpPacket=getSSDPPacket();
		//			if(ssdpPacket!=null)
		//			{
		//				int tmpPort=ssdpPacket.getQiyiHttpPort();
		//				setQiyiHTTPPort(tmpPort);
		//			}
		//		}else
		//		{
		//			return qiyiPort;
		//		}
		//		qiyiPort=getQiyiHTTPPort();
		//		return qiyiPort;

		//每次都严格从ssdpPacket里面去获取，这样会靠谱一些？
		SSDPPacket ssdpPacket = getSSDPPacket();
		if (ssdpPacket != null)
		{
			int tmpPort = ssdpPacket.getQiyiHttpPort();
			setQiyiHTTPPort(tmpPort);
		}
		return getQiyiHTTPPort();
	}

	/*
	 * 从组播消息中获取udp httpPort的消息
	 */
	public int getQiyiUDPHTTPPortFromSSDP()
	{
		//		int qiyiPort=getUdpQiyiHttpPort();//
		//		if(qiyiPort==0)
		//		{
		//			SSDPPacket ssdpPacket=getSSDPPacket();
		//			if(ssdpPacket!=null)
		//			{
		//				int tmpPort=ssdpPacket.getQiyiUDPHttpPort();
		//				setUdpQiyiHTTPPort(tmpPort);
		//			}
		//		}else
		//		{
		//			return qiyiPort;
		//		}
		//		qiyiPort=getUdpQiyiHttpPort();
		//		return qiyiPort;

		//每次都严格从ssdpPacket里面去获取，这样会靠谱一些？
		SSDPPacket ssdpPacket = getSSDPPacket();
		if (ssdpPacket != null)
		{
			int tmpPort = ssdpPacket.getQiyiUDPHttpPort();
			setUdpQiyiHTTPPort(tmpPort);
		}
		return getUdpQiyiHttpPort();
	}

	/*
	 * 是否支持奇艺的快速通道发送消息
	 */
	public boolean getIsSuperQuicklySend()
	{
		if (getQiyiHTTPPortFromSSDP() == 0)//端口为0，说明是不支持的
			return false;

		return true;
	}

	/*
	 * 从组播消息中获取httpserver的地址
	 */
	public String getQiyiHostFromSSDP()
	{
		SSDPPacket ssdpPacket = getSSDPPacket();
		if (ssdpPacket != null)
		{
			String location = ssdpPacket.getLocation();
			String reqHost = HTTP.getHost(location);
			return reqHost;
		}
		return "";
	}

	/*
	 * 获取奇艺发送的快速端口
	 */
	public int getQiyiHTTPPort()
	{
		return getDeviceData().getQiyihttpPort();
	}

	public void setQiyiHTTPPort(int port)
	{
		getDeviceData().setQiyihttpPort(port);
	}

	/*
	 * udp 的快速发送通道
	 */
	public int getUdpQiyiHttpPort()
	{
		return getDeviceData().getUdpqiyihttpPort();
	}

	public void setUdpQiyiHTTPPort(int port)
	{
		getDeviceData().setUdpqiyihttpPort(port);
	}

	public void setHTTPBindAddress(InetAddress[] inets)
	{
		this.getDeviceData().setHTTPBindAddress(inets);
	}

	public InetAddress[] getHTTPBindAddress()
	{
		return this.getDeviceData().getHTTPBindAddress();
	}

	/**
	 * 
	 * @return
	 * @since 1.8
	 */
	public String getSSDPIPv4MulticastAddress()
	{
		return this.getDeviceData().getMulticastIPv4Address();
	}

	/**
	 * 
	 * @param ip
	 * @since 1.8
	 */
	public void getSSDPIPv4MulticastAddress(String ip)
	{
		this.getDeviceData().setMulticastIPv4Address(ip);
	}

	/**
	 * 
	 * @return
	 * @since 1.8
	 */
	public String getSSDPIPv6MulticastAddress()
	{
		return this.getDeviceData().getMulticastIPv6Address();
	}

	/**
	 * 
	 * @param ip
	 * @since 1.8
	 */
	public void getSSDPIPv6MulticastAddress(String ip)
	{
		this.getDeviceData().setMulticastIPv6Address(ip);
	}

	public void httpRequestRecieved(HTTPRequest httpReq)
	{
		//如果发送的内容只有一个字节，那么说明这个内容是快速通道发送过来
		if (httpReq.isQuicklyRequest() == true)
		{
			if (getQuicklySendMessageListener() != null)
			{
				/*
				 * 必然内容只有一个字节，这个是重要特征，很重要
				 */
				byte[] bArray = httpReq.getContent();
				for (int i = 0; i < bArray.length; ++i)
				{
					Debug.message("content byte [" + i + "] is " + bArray[i]);
				}
				Debug.message("SendMessageReceived done " + httpReq.getContent()[0]);
				quicklySendMessageListener.onQuicklySendMessageRecieved(httpReq.getContent()[0]);
			}
			return;
		}

		if (httpReq.isGetRequest() == true || httpReq.isHeadRequest() == true)
		{
			httpGetRequestRecieved(httpReq);
			return;
		}
		if (httpReq.isPostRequest() == true)
		{
			httpPostRequestRecieved(httpReq);
			return;
		}

		if (httpReq.isSubscribeRequest() == true || httpReq.isUnsubscribeRequest() == true)
		{
			SubscriptionRequest subReq = new SubscriptionRequest(httpReq);
			deviceEventSubscriptionRecieved(subReq);
			return;
		}

		httpReq.returnBadRequest();
	}

	private synchronized byte[] getDescriptionData(String host)
	{
		/*
		 * 这边加入cache策略，减少取设备描述的时候超时
		 */
		if (isNMPRMode() == false)
			updateURLBase(host);

		if (getDescriptionXmlContent() == "")
		{
			Node rootNode = getRootNode();
			if (rootNode == null)
				return new byte[0];
			// Thanks for Mikael Hakman (04/25/05)
			String desc = new String();
			desc += UPnP.XML_DECLARATION;
			desc += "\n";
			desc += rootNode.toString();
			setDescriptionXmlContent(desc);//放进缓存里	
			Debug.message("getDescriptionData new description: " + desc);
			return desc.getBytes();
		}
		return getDescriptionXmlContent().getBytes();//直接读取

	}

	private void httpGetRequestRecieved(HTTPRequest httpReq)
	{
		String uri = httpReq.getURI();
		Debug.message("httpGetRequestRecieved = " + uri);
		if (uri == null)
		{
			httpReq.returnBadRequest();
			return;
		}

		if (uri.startsWith("/description.xml_urn:"))
		{
			uri = uri.replace("/description.xml_urn:", "/_urn:");
			Debug.message("redirect uri = " + uri);
		}

		byte fileByte[] = new byte[0];
		//先从cache中去查找，查找到了,则直接取出
		Object findObject = null;
		try
		{
			findObject = cacheMap.get(uri);
		} catch (Exception e)
		{
		}
		if (findObject != null)
		{
			fileByte = (byte[]) findObject;

		} else
		//没有找到，则需要去取
		{
			Device embDev;
			Service embService;
			//获取设备描述文件
			if (isDescriptionURI(uri) == true)
			{
				String localAddr = httpReq.getLocalAddress();
				if ((localAddr == null) || (localAddr.length() <= 0))
					localAddr = HostInterface.getInterface();
				fileByte = getDescriptionData(localAddr);
				Debug.message("httpGetRequestReceived fresh cacheMap");
				cacheMap.put(uri, fileByte);//加入到cache中去
			}
			//获取嵌入设备的描述文件
			else if ((embDev = getDeviceByDescriptionURI(uri)) != null)
			{
				String localAddr = httpReq.getLocalAddress();
				fileByte = embDev.getDescriptionData(localAddr);
				cacheMap.put(uri, fileByte);//加入到cache中去
			}
			//获取服务的描述文件
			else if ((embService = getServiceBySCPDURL(uri)) != null)
			{
				Debug.message("uri:" + uri);
				fileByte = embService.getSCPDData();
				cacheMap.put(uri, fileByte);//加入到cache中去
			} else if (uri.contains("icon"))
			{
				String iconpath = mIconPath;
				fileByte = FileUtil.load(iconpath);
				cacheMap.put(uri, fileByte);//加入到cache中去
			} else
			{
				httpReq.returnBadRequest();
				return;
			}
		}

		HTTPResponse httpRes = new HTTPResponse();
		if (FileUtil.isXMLFileName(uri) == true)
			httpRes.setContentType(XML.CONTENT_TYPE);
		else
			httpRes.setContentType("image/png");
		httpRes.setStatusCode(HTTPStatus.OK);
		httpRes.setContent(fileByte);
		httpRes.setConnection(HTTP.CLOSE);

		httpReq.post(httpRes);
	}

	private void httpPostRequestRecieved(HTTPRequest httpReq)
	{
		if (httpReq.isSOAPAction() == true)
		{
			//SOAPRequest soapReq = new SOAPRequest(httpReq);
			soapActionRecieved(httpReq);
			return;
		}
		httpReq.returnBadRequest();
	}

	////////////////////////////////////////////////
	//	SOAP
	////////////////////////////////////////////////

	private void soapBadActionRecieved(HTTPRequest soapReq)
	{
		SOAPResponse soapRes = new SOAPResponse();
		soapRes.setStatusCode(HTTPStatus.BAD_REQUEST);
		soapReq.post(soapRes);
	}

	private void soapActionRecieved(HTTPRequest soapReq)
	{
		String uri = soapReq.getURI();
		Service ctlService = getServiceByControlURL(uri);
		if (ctlService != null)
		{
			ActionRequest crlReq = new ActionRequest(soapReq);
			deviceControlRequestRecieved(crlReq, ctlService);
			return;
		}
		soapBadActionRecieved(soapReq);
	}

	////////////////////////////////////////////////
	//	controlAction
	////////////////////////////////////////////////

	private void deviceControlRequestRecieved(ControlRequest ctlReq, Service service)
	{
		if (ctlReq.isQueryControl() == true)
			deviceQueryControlRecieved(new QueryRequest(ctlReq), service);
		else
			deviceActionControlRecieved(new ActionRequest(ctlReq), service);
	}

	private void invalidActionControlRecieved(ControlRequest ctlReq)
	{
		ControlResponse actRes = new ActionResponse();
		actRes.setFaultResponse(UPnPStatus.INVALID_ACTION);
		ctlReq.post(actRes);
	}

	private void invalidArgumentsControlRecieved(ControlRequest ctlReq)
	{
		ControlResponse actRes = new ActionResponse();
		actRes.setFaultResponse(UPnPStatus.INVALID_ARGS);
		ctlReq.post(actRes);
	}

	private synchronized void deviceActionControlRecieved(ActionRequest ctlReq, Service service)
	{
		if (Debug.isOn() == true)
			ctlReq.print();

		String actionName = ctlReq.getActionName();

		if (actionName == null || actionName.equals(""))
		{
			actionName = "";
			Debug.message("Failed to parse the action name...read it from origin data...");
			String[] tempStr = ctlReq.getSOAPAction().split("#");
			if (tempStr.length == 2)
			{
				actionName = tempStr[1];
			}
		}

		Debug.message("Action Name: " + actionName);
		Action action = service.getAction(actionName);
		if (action == null)
		{
			invalidActionControlRecieved(ctlReq);
			return;
		}

		ArgumentList actionArgList = action.getArgumentList();
		ArgumentList reqArgList = ctlReq.getArgumentList();

		if (reqArgList == null)
		{
			Debug.message("[ERROR] deviceActionControlRecieved reqArgList == null");
			invalidArgumentsControlRecieved(ctlReq);
			return;
		}

		try
		{
			actionArgList.setReqArgs(reqArgList);
		} catch (Exception ex)
		{
			Debug.message("[ERROR] deviceActionControlRecieved setReqArgs Exception");
			ex.printStackTrace();
			invalidArgumentsControlRecieved(ctlReq);
			return;
		}

		if (action.performActionListener(ctlReq) == false)
			invalidActionControlRecieved(ctlReq);
	}

	private void deviceQueryControlRecieved(QueryRequest ctlReq, Service service)
	{
		if (Debug.isOn() == true)
			ctlReq.print();
		String varName = ctlReq.getVarName();
		if (service.hasStateVariable(varName) == false)
		{
			invalidActionControlRecieved(ctlReq);
			return;
		}
		StateVariable stateVar = getStateVariable(varName);
		if (stateVar.performQueryListener(ctlReq, false) == false)
			invalidActionControlRecieved(ctlReq);
	}

	////////////////////////////////////////////////
	//	eventSubscribe
	////////////////////////////////////////////////

	private void upnpBadSubscriptionRecieved(SubscriptionRequest subReq, int code)
	{
		SubscriptionResponse subRes = new SubscriptionResponse();
		subRes.setErrorResponse(code);
		subReq.post(subRes);
	}

	private void deviceEventSubscriptionRecieved(SubscriptionRequest subReq)
	{
		String uri = subReq.getURI();
		Service service = getServiceByEventSubURL(uri);
		if (service == null)
		{
			subReq.returnBadRequest();
			return;
		}
		if (subReq.hasCallback() == false && subReq.hasSID() == false)
		{
			upnpBadSubscriptionRecieved(subReq, HTTPStatus.PRECONDITION_FAILED);
			return;
		}

		// UNSUBSCRIBE
		if (subReq.isUnsubscribeRequest() == true)
		{
			Debug.message("sub: receive unsub");
			deviceEventUnsubscriptionRecieved(service, subReq);
			return;
		}

		// SUBSCRIBE (NEW)
		if (subReq.hasCallback() == true)
		{
			Debug.message("sub: receive sub");
			deviceEventNewSubscriptionRecieved(service, subReq);
			return;
		}

		// SUBSCRIBE (RENEW)
		if (subReq.hasSID() == true)
		{
			Debug.message("sub: receive resub");
			deviceEventRenewSubscriptionRecieved(service, subReq);
			return;
		}

		upnpBadSubscriptionRecieved(subReq, HTTPStatus.PRECONDITION_FAILED);
	}

	private void deviceEventNewSubscriptionRecieved(Service service, SubscriptionRequest subReq)
	{
		if (Debug.isOn() == true)
			subReq.print();

		String callback = subReq.getCallback();
		try
		{
			new URL(callback);
		} catch (Exception e)
		{
			upnpBadSubscriptionRecieved(subReq, HTTPStatus.PRECONDITION_FAILED);
			return;
		}

		long timeOut = subReq.getTimeout();
		String sid = Subscription.createSID();

		Subscriber sub = new Subscriber();
		sub.setDeliveryURL(callback);
		sub.setTimeOut(timeOut);
		sub.setSID(sid);
		String gid = subReq.getGID();
		boolean external = false;
		if (gid != null && gid.length() > 0)
		{
			Debug.message("sub: subscribe received for TVGuoApp");
			external = true;
		} else
		{
			Debug.message("sub: renew subscribe received for DLNA");
		}
		service.addSubscriber(sub, external);

		SubscriptionResponse subRes = new SubscriptionResponse();
		subRes.setStatusCode(HTTPStatus.OK);
		subRes.setSID(sid);
		subRes.setTimeout(timeOut);
		subReq.post(subRes);

		if (Debug.isOn() == true)
			subRes.print();

		service.notifyAllStateVariables(external);
	}

	private void deviceEventRenewSubscriptionRecieved(Service service, SubscriptionRequest subReq)
	{
		if (Debug.isOn() == true)
			subReq.print();

		String sid = subReq.getSID();
		String gid = subReq.getGID();
		boolean external = false;
		if (gid != null && gid.length() > 0)
		{
			Debug.message("sub: renew subscribe received for TVGuoApp");
			external = true;
		} else
		{
			Debug.message("sub: renew subscribe received for DLNA");
		}
		Subscriber sub = service.getSubscriber(sid, external);

		if (sub == null)
		{
			upnpBadSubscriptionRecieved(subReq, HTTPStatus.PRECONDITION_FAILED);
			return;
		}

		long timeOut = subReq.getTimeout();
		sub.setTimeOut(timeOut);
		sub.renew();

		SubscriptionResponse subRes = new SubscriptionResponse();
		subRes.setStatusCode(HTTPStatus.OK);
		subRes.setSID(sid);
		subRes.setTimeout(timeOut);
		subReq.post(subRes);

		if (Debug.isOn() == true)
			subRes.print();
	}

	private void deviceEventUnsubscriptionRecieved(Service service, SubscriptionRequest subReq)
	{
		String sid = subReq.getSID();
		String gid = subReq.getGID();
		boolean external = false;
		if (gid != null && gid.length() > 0)
		{
			Debug.message("sub: renew subscribe received with external true");
			external = true;
		}
		Subscriber sub = service.getSubscriber(sid, external);

		if (sub == null)
		{
			upnpBadSubscriptionRecieved(subReq, HTTPStatus.PRECONDITION_FAILED);
			return;
		}

		service.removeSubscriber(sub, external);

		SubscriptionResponse subRes = new SubscriptionResponse();
		subRes.setStatusCode(HTTPStatus.OK);
		subReq.post(subRes);

		if (Debug.isOn() == true)
			subRes.print();
	}

	////////////////////////////////////////////////
	//	Thread	
	////////////////////////////////////////////////

	private HTTPServerList getHTTPServerList()
	{
		return getDeviceData().getHTTPServerList();
	}

	private QiyiHttpServerList getQiyiHttpServerList()
	{
		return getDeviceData().getQiyiHttpServerList();
	}

	/**
	 * 
	 * @param port
	 *            The port to use for binding the SSDP service
	 */
	public void setSSDPPort(int port)
	{
		this.getDeviceData().setSSDPPort(port);
	}

	/**
	 * 
	 * @return The port to use for binding the SSDP service
	 */
	public int getSSDPPort()
	{
		return this.getDeviceData().getSSDPPort();
	}

	/**
	 * 
	 * @param inets
	 *            The IP that will be used for binding the SSDP service. Use <code>null</code> to get the default beahvior
	 */
	public void setSSDPBindAddress(InetAddress[] inets)
	{
		this.getDeviceData().setSSDPBindAddress(inets);
	}

	/**
	 * 
	 * @return inets The IP that will be used for binding the SSDP service. null means the default setted by the class UPnP
	 */
	public InetAddress[] getSSDPBindAddress()
	{
		return this.getDeviceData().getSSDPBindAddress();
	}

	/**
	 * 
	 * @param ip
	 *            The IPv4 address used for Multicast comunication
	 */
	public void setMulticastIPv4Address(String ip)
	{
		this.getDeviceData().setMulticastIPv4Address(ip);
	}

	/**
	 * 
	 * @return The IPv4 address used for Multicast comunication
	 */
	public String getMulticastIPv4Address()
	{
		return this.getDeviceData().getMulticastIPv4Address();
	}

	/**
	 * 
	 * @param ip
	 *            The IPv address used for Multicast comunication
	 */
	public void setMulticastIPv6Address(String ip)
	{
		this.getDeviceData().setMulticastIPv6Address(ip);
	}

	/**
	 * 
	 * @return The IPv address used for Multicast comunication
	 */
	public String getMulticastIPv6Address()
	{
		return this.getDeviceData().getMulticastIPv6Address();
	}

	private SSDPSearchSocketList getSSDPSearchSocketList()
	{
		return getDeviceData().getSSDPSearchSocketList();
	}

	private void setAdvertiser(Advertiser adv)
	{
		getDeviceData().setAdvertiser(adv);
	}

	private Advertiser getAdvertiser()
	{
		return getDeviceData().getAdvertiser();
	}

	////////////////////////////////////////////////
	// Interface Address
	////////////////////////////////////////////////

	public String getInterfaceAddress()
	{
		SSDPPacket ssdpPacket = getSSDPPacket();
		if (ssdpPacket == null)
			return "";
		return ssdpPacket.getLocalAddress();
	}

	////////////////////////////////////////////////
	// Acion/QueryListener
	////////////////////////////////////////////////

	public void setActionListener(ActionListener listener)
	{
		ServiceList serviceList = getServiceList();
		int nServices = serviceList.size();
		for (int n = 0; n < nServices; n++)
		{
			Service service = serviceList.getService(n);
			service.setActionListener(listener);
		}
	}

	public void setQueryListener(QueryListener listener)
	{
		ServiceList serviceList = getServiceList();
		int nServices = serviceList.size();
		for (int n = 0; n < nServices; n++)
		{
			Service service = serviceList.getService(n);
			service.setQueryListener(listener);
		}
	}

	////////////////////////////////////////////////
	// Acion/QueryListener (includeSubDevices)
	////////////////////////////////////////////////

	// Thanks for Mikael Hakman (04/25/05)
	public void setActionListener(ActionListener listener, boolean includeSubDevices)
	{
		setActionListener(listener);
		if (includeSubDevices == true)
		{
			DeviceList devList = getDeviceList();
			int devCnt = devList.size();
			for (int n = 0; n < devCnt; n++)
			{
				Device dev = devList.getDevice(n);
				dev.setActionListener(listener, true);
			}
		}
	}

	// Thanks for Mikael Hakman (04/25/05)
	public void setQueryListener(QueryListener listener, boolean includeSubDevices)
	{
		setQueryListener(listener);
		if (includeSubDevices == true)
		{
			DeviceList devList = getDeviceList();
			int devCnt = devList.size();
			for (int n = 0; n < devCnt; n++)
			{
				Device dev = devList.getDevice(n);
				dev.setQueryListener(listener, true);
			}
		}
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
	//	output
	////////////////////////////////////////////////

	/*
		public void output(PrintWriter ps) 
		{
			ps.println("deviceType = " + getDeviceType());
			ps.println("freindlyName = " + getFriendlyName());
			ps.println("presentationURL = " + getPresentationURL());

			DeviceList devList = getDeviceList();
			ps.println("devList = " + devList.size());

			ServiceList serviceList = getServiceList();
			ps.println("serviceList = " + serviceList.size());

			IconList iconList = getIconList();
			ps.println("iconList = " + iconList.size());
		}

		public void print()
		{
			PrintWriter pr = new PrintWriter(System.out);
			output(pr);
			pr.flush();
		}
	*/

}
