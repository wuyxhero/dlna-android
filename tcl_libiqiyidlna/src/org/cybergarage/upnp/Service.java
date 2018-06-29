/******************************************************************
 *
 *	CyberLink for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File: Service.java
 *
 *	Revision;
 *
 *	11/28/02
 *		- first revision.
 *	04/12/02
 *		- Holmes, Arran C <acholm@essex.ac.uk>
 *		- Fixed SERVICE_ID constant instead of "serviceId".
 *	06/17/03
 *		- Added notifyAllStateVariables().
 *	09/03/03
 *		- Giordano Sassaroli <sassarol@cefriel.it>
 *		- Problem : The device does not accepts request for services when control or subscription urls are absolute
 *		- Error : device methods, when requests are received, search for services that have a controlUrl (or eventSubUrl) equal to the request URI
 *		          but request URI must be relative, so they cannot equal absolute urls
 *	09/03/03
 *		- Steven Yen
 *		- description: to retrieve service information based on information in URLBase and SCPDURL
 *		- problem: not able to retrieve service information when URLBase is missing and SCPDURL is relative
 *		- fix: modify to retrieve host information from Header's Location (required) field and update the
 *		       BaseURL tag in the xml so subsequent information retrieval can be done (Steven Yen, 8.27.2003)
 *		- note: 1. in the case that Header's Location field combine with SCPDURL is not able to retrieve proper 
 *		          information, updating BaseURL would not hurt, since exception will be thrown with or without update.
 *		        2. this problem was discovered when using PC running MS win XP with ICS enabled (gateway). 
 *		          It seems that  root device xml file does not have BaseURL and SCPDURL are all relative.
 *		        3. UPnP device architecture states that BaseURL is optional and SCPDURL may be relative as 
 *		          specified by UPnP vendor, so MS does not seem to violate the rule.
 *	10/22/03
 *		- Added setActionListener().
 *	01/04/04
 *		- Changed about new QueryListener interface.
 *	01/06/04
 *		- Moved the following methods to StateVariable class.
 *		  getQueryListener() 
 *		  setQueryListener() 
 *		  performQueryListener()
 *		- Added new setQueryListener() to set a listner to all state variables.
 *	07/02/04
 *		- Added serviceSearchResponse().
 *		- Deleted getLocationURL().
 *		- Fixed announce() to set the root device URL to the LOCATION field.
 *	07/31/04
 *		- Changed notify() to remove the expired subscribers and not to remove the invalid response subscribers for NMPR.
 *	10/29/04
 *		- Fixed a bug when notify() removes the expired devices().
 *	03/23/05
 *		- Added loadSCPD() to load the description from memory.
 *	03/30/05
 *		- Added isSCPDURL().
 *		- Removed setDescriptionURL() and getDescriptionURL()
 *	03/31/05
 *		- Added getSCPDData().
 * 	04/25/05
 *		- Thanks for Mikael Hakman <mhakman@dkab.net>
 * 		- Changed getSCPDData() to add a XML declaration at first line.
 *	06/21/05
 *		- Changed notify() to continue when the subscriber is null.
 *	04/12/06
 *		- Added setUserData() and getUserData() to set a user original data object.
 *	09/18/2010 Robin V. <robinsp@gmail.com>
 *		- Fixed getSCPDNode() not to occur recursive http get requests.
 *
 ******************************************************************/

package org.cybergarage.upnp;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import org.cybergarage.http.HTTP;
import org.cybergarage.upnp.control.ActionListener;
import org.cybergarage.upnp.control.QueryListener;
import org.cybergarage.upnp.device.InvalidDescriptionException;
import org.cybergarage.upnp.device.NTS;
import org.cybergarage.upnp.device.ST;
import org.cybergarage.upnp.event.Subscriber;
import org.cybergarage.upnp.event.SubscriberList;
import org.cybergarage.upnp.ssdp.SSDPNotifyRequest;
import org.cybergarage.upnp.ssdp.SSDPNotifySocket;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import org.cybergarage.upnp.xml.ServiceData;
import org.cybergarage.util.Debug;
import org.cybergarage.util.StringUtil;
import org.cybergarage.xml.Node;
import org.cybergarage.xml.Parser;
import org.cybergarage.xml.ParserException;

import com.iqiyi.android.dlna.sdk.controlpoint.TVGuoDescription;
import com.iqiyi.android.sdk.dlna.keeper.DmcInforKeeper;
import com.iqiyi.android.sdk.dlna.keeper.DmrInfor;

public class Service
{
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////

	public final static String ELEM_NAME = "service";

	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	private Node serviceNode;

	public Node getServiceNode()
	{
		return serviceNode;
	}

	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	public static final String SCPD_ROOTNODE = "scpd";
	public static final String SCPD_ROOTNODE_NS = "urn:schemas-upnp-org:service-1-0";

	public static final String SPEC_VERSION = "specVersion";
	public static final String MAJOR = "major";
	public static final String MAJOR_VALUE = "1";
	public static final String MINOR = "minor";
	public static final String MINOR_VALUE = "0";

	public Service()
	{
		this(new Node(ELEM_NAME));

		Node sp = new Node(SPEC_VERSION);

		Node M = new Node(MAJOR);
		M.setValue(MAJOR_VALUE);
		sp.addNode(M);

		Node m = new Node(MINOR);
		m.setValue(MINOR_VALUE);
		sp.addNode(m);

		//Node scpd = new Node(SCPD_ROOTNODE,SCPD_ROOTNODE_NS); wrong!
		Node scpd = new Node(SCPD_ROOTNODE); // better (twa)
		scpd.addAttribute("xmlns", SCPD_ROOTNODE_NS); // better (twa)
		scpd.addNode(sp);
		getServiceData().setSCPDNode(scpd);
	}

	public Service(Node node)
	{
		serviceNode = node;
	}

	private String descriptionXmlContent = "";//设备描述文件缓存

	public String getDescriptionXmlContent()
	{
		return descriptionXmlContent;
	}

	public void setDescriptionXmlContent(String descriptionXmlContent)
	{
		this.descriptionXmlContent = descriptionXmlContent;
	}

	////////////////////////////////////////////////
	// AcionListener，监听的事件通过这个接口返回
	////////////////////////////////////////////////

	public void setActionListener(ActionListener listener)
	{
		ActionList actionList = getActionList();
		int nActions = actionList.size();
		for (int n = 0; n < nActions; n++)
		{
			Action action = actionList.getAction(n);
			action.setActionListener(listener);
		}
	}

	////////////////////////////////////////////////
	//	isServiceNode
	////////////////////////////////////////////////

	public static boolean isServiceNode(Node node)
	{
		return Service.ELEM_NAME.equals(node.getName());
	}

	////////////////////////////////////////////////
	//	Device/Root Node
	////////////////////////////////////////////////

	private Node getDeviceNode()
	{
		Node node = getServiceNode().getParentNode();
		if (node == null)
			return null;
		return node.getParentNode();
	}

	private Node getRootNode()
	{
		return getServiceNode().getRootNode();
	}

	////////////////////////////////////////////////
	//	Device
	////////////////////////////////////////////////

	public Device getDevice()
	{
		return new Device(getRootNode(), getDeviceNode());
	}

	public Device getRootDevice()
	{
		return getDevice().getRootDevice();
	}

	////////////////////////////////////////////////
	//	serviceType
	////////////////////////////////////////////////

	private final static String SERVICE_TYPE = "serviceType";

	public void setServiceType(String value)
	{
		getServiceNode().setNode(SERVICE_TYPE, value);
	}

	public String getServiceType()
	{
		return getServiceNode().getNodeValue(SERVICE_TYPE);
	}

	////////////////////////////////////////////////
	//	serviceID
	////////////////////////////////////////////////

	private final static String SERVICE_ID = "serviceId";

	public void setServiceID(String value)
	{
		getServiceNode().setNode(SERVICE_ID, value);
	}

	public String getServiceID()
	{
		return getServiceNode().getNodeValue(SERVICE_ID);
	}

	////////////////////////////////////////////////
	//	isURL
	////////////////////////////////////////////////

	// Thanks for Giordano Sassaroli <sassarol@cefriel.it> (09/03/03)
	private boolean isURL(String referenceUrl, String url)
	{
		if (referenceUrl == null || url == null)
			return false;
		boolean ret = url.equals(referenceUrl);
		if (ret == true)
			return true;
		String relativeRefUrl = HTTP.toRelativeURL(referenceUrl, false);
		ret = url.equals(relativeRefUrl);
		if (ret == true)
			return true;
		return false;
	}

	////////////////////////////////////////////////
	//	SCPDURL
	////////////////////////////////////////////////

	private final static String SCPDURL = "SCPDURL";

	public void setSCPDURL(String value)
	{
		getServiceNode().setNode(SCPDURL, value);
	}

	public String getSCPDURL()
	{
		return getServiceNode().getNodeValue(SCPDURL);
	}

	public boolean isSCPDURL(String url)
	{
		return isURL(getSCPDURL(), url);
	}

	////////////////////////////////////////////////
	//	controlURL
	////////////////////////////////////////////////

	private final static String CONTROL_URL = "controlURL";

	public void setControlURL(String value)
	{
		getServiceNode().setNode(CONTROL_URL, value);
	}

	public String getControlURL()
	{
		return getServiceNode().getNodeValue(CONTROL_URL);
	}

	public boolean isControlURL(String url)
	{
		return isURL(getControlURL(), url);
	}

	////////////////////////////////////////////////
	//	eventSubURL
	////////////////////////////////////////////////

	private final static String EVENT_SUB_URL = "eventSubURL";

	public void setEventSubURL(String value)
	{
		getServiceNode().setNode(EVENT_SUB_URL, value);
	}

	public String getEventSubURL()
	{
		return getServiceNode().getNodeValue(EVENT_SUB_URL);
	}

	public boolean isEventSubURL(String url)
	{
		return isURL(getEventSubURL(), url);
	}

	////////////////////////////////////////////////
	//	SCPD node
	////////////////////////////////////////////////

	public boolean loadSCPD(String scpdStr) throws InvalidDescriptionException
	{
		try
		{
			Parser parser = UPnP.getXMLParser();
			Node scpdNode = parser.parse(scpdStr);
			if (scpdNode == null)
				return false;
			ServiceData data = getServiceData();
			data.setSCPDNode(scpdNode);
			scpdNode.addAttribute("xmlns", SCPD_ROOTNODE_NS);
		} catch (ParserException e)
		{
			throw new InvalidDescriptionException(e);
		}
		return true;
	}

	public boolean loadSCPD(File file) throws ParserException
	{
		Parser parser = UPnP.getXMLParser();
		Node scpdNode = parser.parse(file);
		if (scpdNode == null)
			return false;
		ServiceData data = getServiceData();
		data.setSCPDNode(scpdNode);
		return true;
	}

	/**
	 * @since 1.8.0
	 */
	public boolean loadSCPD(InputStream input) throws ParserException
	{
		Parser parser = UPnP.getXMLParser();
		Node scpdNode = parser.parse(input);
		if (scpdNode == null)
			return false;
		ServiceData data = getServiceData();
		data.setSCPDNode(scpdNode);
		return true;
	}

	public void setDescriptionURL(String value)
	{
		getServiceData().setDescriptionURL(value);
	}

	public String getDescriptionURL()
	{
		return getServiceData().getDescriptionURL();
	}

	private Node getSCPDNode(URL scpdUrl) throws ParserException
	{
		Parser parser = UPnP.getXMLParser();
		return parser.parse(scpdUrl);
	}

	private Node getSCPDNode(File scpdFile) throws ParserException
	{
		Parser parser = UPnP.getXMLParser();
		return parser.parse(scpdFile);
	}

	/*
	 * 通过设备描述文件区直接构造
	 */
	private Node getSCPDNode(String scpdDocument) throws ParserException
	{
		Parser parser = UPnP.getXMLParser();
		return parser.parse(scpdDocument);
	}

	private Node getSCPDNode()
	{
		ServiceData data = getServiceData();
		Node scpdNode = data.getSCPDNode();
		if (scpdNode != null)
			return scpdNode;

		// Thanks for Jaap (Sep 18, 2010)
		Device rootDev = getRootDevice();
		if (rootDev == null)
			return null;

		String scpdURLStr = getSCPDURL();

		/*
		 * 由于设备描述在盒子没有更新的时候，设备描述不会被更新。服务与描述文件需要同步
		 * 先到本地的cache去查找，看是否有变更
		 * 这里依然是只针对奇艺的盒子，其他的盒子对这个策略不适用
		 */
		if (rootDev.getSSDPPacket() != null && rootDev.getSSDPPacket().isQiyiServer() == true)//是奇艺的盒子
		{
			DmrInfor dmrInfor = DmcInforKeeper.getInstance().getDmrInfor(rootDev.getUDN());
			if (dmrInfor != null)//这个是必然滴，因为在获取设备描述的时候，已经保存下来了
			{
				if (dmrInfor.getServerMap().containsKey(scpdURLStr) == true)//命中，则取出来
				{
					String serverXml = dmrInfor.getServerMap().get(scpdURLStr);
					try
					{
						scpdNode = getSCPDNode(serverXml);
					} catch (ParserException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (scpdNode != null)
					{
						data.setSCPDNode(scpdNode);
						return scpdNode;
					}
				} else
				{
					String serverXml = TVGuoDescription.TVGuoSCPD;
					try
					{
						scpdNode = getSCPDNode(serverXml);
					} catch (ParserException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (scpdNode != null)
					{
						data.setSCPDNode(scpdNode);
						return scpdNode;
					}
				}
			}
		}

		// Thanks for Robin V. (Sep 18, 2010)
		String rootDevPath = rootDev.getDescriptionFilePath();
		if (rootDevPath != null)
		{
			File f;
			f = new File(rootDevPath.concat(scpdURLStr));

			if (f.exists())
			{
				try
				{
					scpdNode = getSCPDNode(f);
				} catch (ParserException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (scpdNode != null)
				{
					data.setSCPDNode(scpdNode);
					//保存到缓存中去
					saveServiceToCache(rootDev, scpdURLStr, scpdNode);
					return scpdNode;
				}
			}
		}

		try
		{
			URL scpdUrl = new URL(rootDev.getAbsoluteURL(scpdURLStr));
			scpdNode = getSCPDNode(scpdUrl);
			if (scpdNode != null)
			{
				data.setSCPDNode(scpdNode);
				//保存到缓存中去
				saveServiceToCache(rootDev, scpdURLStr, scpdNode);
				return scpdNode;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		String newScpdURLStr = rootDev.getDescriptionFilePath() + HTTP.toRelativeURL(scpdURLStr);
		try
		{
			scpdNode = getSCPDNode(new File(newScpdURLStr));
			//保存到缓存中去
			saveServiceToCache(rootDev, scpdURLStr, scpdNode);

			return scpdNode;
		} catch (Exception e)
		{
			Debug.warning(e);
		}

		return null;
	}

	/*
	 * 将服务描述文件保存到本地的cache中去
	 */
	private void saveServiceToCache(Device rootDev, String scpdURLStr, Node scpdNode)
	{
		//
		if (rootDev.getSSDPPacket() != null && rootDev.getSSDPPacket().isQiyiServer() == true)
		{
			DmrInfor dmrInfor = DmcInforKeeper.getInstance().getDmrInfor(rootDev.getUDN());
			if (dmrInfor != null)//这个是必然滴，因为在获取设备描述的时候，已经保存下来了
			{
				if (dmrInfor.getServerMap().containsKey(scpdURLStr) == false)//没有命中
				{
					dmrInfor.getServerMap().put(scpdURLStr, scpdNode.toString());
					//保存下来
					DmcInforKeeper.getInstance().SaveDmrInfor(dmrInfor);
				}
			}
		}
	}

	public byte[] getSCPDData()
	{
		if (getDescriptionXmlContent() == "")
		{
			Node scpdNode = getSCPDNode();
			if (scpdNode == null)
				return new byte[0];
			// Thanks for Mikael Hakman (04/25/05)
			String desc = new String();
			desc += UPnP.XML_DECLARATION;
			desc += "\n";
			desc += scpdNode.toString();
			setDescriptionXmlContent(desc);
		}
		return getDescriptionXmlContent().getBytes();
	}

	////////////////////////////////////////////////
	//	actionList
	////////////////////////////////////////////////

	public ActionList getActionList()
	{
		ActionList actionList = new ActionList();
		Node scdpNode = getSCPDNode();
		if (scdpNode == null)
			return actionList;
		Node actionListNode = scdpNode.getNode(ActionList.ELEM_NAME);
		if (actionListNode == null)
			return actionList;
		int nNode = actionListNode.getNNodes();
		for (int n = 0; n < nNode; n++)
		{
			Node node = actionListNode.getNode(n);
			if (Action.isActionNode(node) == false)
				continue;
			Action action = new Action(serviceNode, node);
			actionList.add(action);
		}
		return actionList;
	}

	public Action getAction(String actionName)
	{
		if (actionName == null || actionName == "")
			return null;
		ActionList actionList = getActionList();
		int nActions = actionList.size();
		for (int n = 0; n < nActions; n++)
		{
			Action action = actionList.getAction(n);
			String name = action.getName();
			if (name == null)
				continue;
			if (name.equals(actionName) == true)
				return action;
		}
		return null;
	}

	public void addAction(Action a)
	{
		if (a == null)
			return;
		Iterator<Argument> i = a.getArgumentList().iterator();
		while (i.hasNext())
		{
			Argument arg = (Argument) i.next();
			arg.setService(this);
		}

		Node scdpNode = getSCPDNode();
		Node actionListNode = scdpNode.getNode(ActionList.ELEM_NAME);
		if (actionListNode == null)
		{
			actionListNode = new Node(ActionList.ELEM_NAME);
			scdpNode.addNode(actionListNode);
		}
		actionListNode.addNode(a.getActionNode());
	}

	////////////////////////////////////////////////
	//	serviceStateTable
	////////////////////////////////////////////////

	public ServiceStateTable getServiceStateTable()
	{
		ServiceStateTable stateTable = new ServiceStateTable();
		Node stateTableNode = getSCPDNode().getNode(ServiceStateTable.ELEM_NAME);
		if (stateTableNode == null)
			return stateTable;
		Node serviceNode = getServiceNode();
		int nNode = stateTableNode.getNNodes();
		for (int n = 0; n < nNode; n++)
		{
			Node node = stateTableNode.getNode(n);
			if (StateVariable.isStateVariableNode(node) == false)
				continue;
			StateVariable serviceVar = new StateVariable(serviceNode, node);
			stateTable.add(serviceVar);
		}
		return stateTable;
	}

	public StateVariable getStateVariable(String name)
	{
		if (name == null || name == "")
			return null;

		ServiceStateTable stateTable = getServiceStateTable();
		int tableSize = stateTable.size();
		for (int n = 0; n < tableSize; n++)
		{
			StateVariable var = stateTable.getStateVariable(n);
			String varName = var.getName();
			if (varName == null)
				continue;
			if (varName.equals(name) == true)
				return var;
		}
		return null;
	}

	public boolean hasStateVariable(String name)
	{
		return (getStateVariable(name) != null) ? true : false;
	}

	////////////////////////////////////////////////
	//	UserData
	////////////////////////////////////////////////

	public boolean isService(String name)
	{
		if (name == null)
			return false;
		if (name.endsWith(getServiceType()) == true)
			return true;
		if (name.endsWith(getServiceID()) == true)
			return true;
		return false;
	}

	////////////////////////////////////////////////
	//	UserData
	////////////////////////////////////////////////

	private ServiceData getServiceData()
	{
		Node node = getServiceNode();
		ServiceData userData = (ServiceData) node.getUserData();
		if (userData == null)
		{
			userData = new ServiceData();
			node.setUserData(userData);
			userData.setNode(node);
		}
		return userData;
	}

	////////////////////////////////////////////////
	//	Notify
	////////////////////////////////////////////////

	private String getNotifyServiceTypeNT()
	{
		return getServiceType();
	}

	private String getNotifyServiceTypeUSN()
	{
		return getDevice().getUDN() + "::" + getServiceType();
	}

	public void announce(String bindAddr)
	{
		// uuid:device-UUID::urn:schemas-upnp-org:service:serviceType:v 
		Device rootDev = getRootDevice();
		String devLocation = rootDev.getLocationURL(bindAddr);
		String serviceNT = getNotifyServiceTypeNT();
		String serviceUSN = getNotifyServiceTypeUSN();

		Device dev = getDevice();

		SSDPNotifyRequest ssdpReq = new SSDPNotifyRequest();
		ssdpReq.setServer(UPnP.getServerName());
		ssdpReq.setLeaseTime(dev.getLeaseTime());
		ssdpReq.setLocation(devLocation);
		ssdpReq.setNTS(NTS.ALIVE);
		ssdpReq.setNT(serviceNT);
		ssdpReq.setUSN(serviceUSN);
		/*
		 * 在server中也增加了是否支持长连接的报文
		 */
		ssdpReq.setConnect(true);

		SSDPNotifySocket ssdpSock = new SSDPNotifySocket(bindAddr);
		Device.notifyWait();
		ssdpSock.post(ssdpReq);
	}

	public void byebye(String bindAddr)
	{
		// uuid:device-UUID::urn:schemas-upnp-org:service:serviceType:v 

		String devNT = getNotifyServiceTypeNT();
		String devUSN = getNotifyServiceTypeUSN();

		SSDPNotifyRequest ssdpReq = new SSDPNotifyRequest();
		ssdpReq.setNTS(NTS.BYEBYE);
		ssdpReq.setNT(devNT);
		ssdpReq.setUSN(devUSN);

		SSDPNotifySocket ssdpSock = new SSDPNotifySocket(bindAddr);
		Device.notifyWait();
		ssdpSock.post(ssdpReq);
	}

	public boolean serviceSearchResponse(SSDPPacket ssdpPacket)
	{
		String ssdpST = ssdpPacket.getST();

		if (ssdpST == null)
			return false;

		Device dev = getDevice();

		String serviceNT = getNotifyServiceTypeNT();
		String serviceUSN = getNotifyServiceTypeUSN();

		if (ST.isAllDevice(ssdpST) == true)
		{
			dev.postSearchResponse(ssdpPacket, serviceNT, serviceUSN);
		} else if (ST.isURNService(ssdpST) == true)
		{
			String serviceType = getServiceType();
			if (ssdpST.equals(serviceType) == true)
				dev.postSearchResponse(ssdpPacket, serviceType, serviceUSN);
		}

		return true;
	}

	////////////////////////////////////////////////
	// QueryListener
	////////////////////////////////////////////////

	public void setQueryListener(QueryListener queryListener)
	{
		ServiceStateTable stateTable = getServiceStateTable();
		int tableSize = stateTable.size();
		for (int n = 0; n < tableSize; n++)
		{
			StateVariable var = stateTable.getStateVariable(n);
			var.setQueryListener(queryListener);
		}
	}

	////////////////////////////////////////////////
	//	Subscription
	////////////////////////////////////////////////

	public SubscriberList getSubscriberList_dlna()
	{
		return getServiceData().getSubscriberList_dlna();
	}

	public SubscriberList getSubscriberList_tvguo()
	{
		return getServiceData().getSubscriberList_tvguo();
	}

	public synchronized void addSubscriber(Subscriber sub, boolean tvguo)
	{
		String tmp = tvguo ? "TVGupApp" : "DLNA";
		Debug.message("sub: " + getServiceID() + " addSubscriber for " + tmp + ": " + sub.getSID() + "  "
				+ sub.getDeliveryURL());

		SubscriberList subList;
		if (tvguo == false)
			subList = getSubscriberList_dlna();
		else
			subList = getSubscriberList_tvguo();

		synchronized (subList)
		{
			Iterator<Subscriber> it = subList.iterator();
			while (it.hasNext())
			{
				Subscriber curSub = it.next();
				if (sub.getDeliveryHost().equals(curSub.getDeliveryHost()) && sub.getDeliveryPort() == curSub.getDeliveryPort())
				{
					Debug.message("sub: Remove duplicated subscriber: " + curSub.getSID() + "  " + curSub.getDeliveryHost()
							+ "  " + curSub.getDeliveryPort() + "  TVGuoApp=" + tvguo);
					curSub.stop();
					it.remove();
				}
			}
			subList.add(sub);
		}

		sub.initThreadParams(getServiceNode(), tvguo);
		sub.start("NotifySubscriberThread[" + sub.getDeliveryURL() + "]");
	}

	public synchronized void removeSubscriber(Subscriber sub, boolean tvguo)
	{
		SubscriberList subList;
		if (tvguo == false)
		{
			Debug.message("sub: removeSubscriber TVGuoApp " + sub.getSID());
			subList = getSubscriberList_dlna();
			synchronized (subList)
			{
				subList.remove(sub);
			}
			sub.stop();
		} else
		{
			Debug.message("sub: removeSubscriber DLNA " + sub.getSID());
			subList = getSubscriberList_tvguo();
			synchronized (subList)
			{
				subList.remove(sub);
			}
			sub.stop();
		}
	}

	public Subscriber getSubscriber(String name, boolean external)
	{
		SubscriberList subList;
		if (external == false)
			subList = getSubscriberList_dlna();
		else
			subList = getSubscriberList_tvguo();
		int subListCnt = subList.size();
		for (int n = 0; n < subListCnt; n++)
		{
			Subscriber sub = subList.getSubscriber(n);
			if (sub == null)
				continue;
			String sid = sub.getSID();
			if (sid == null)
				continue;
			if (sid.equals(name) == true)
				return sub;
		}
		return null;
	}

	//	private boolean notify(Subscriber sub, StateVariable stateVar, boolean external)
	//	{
	//		String varName = stateVar.getName();
	//		String value;
	//		if (external == true)
	//			value = stateVar.getValue_tvguo();
	//		else
	//			value = stateVar.getValue_dlna();
	//
	//		String host = sub.getDeliveryHost();
	//		int port = sub.getDeliveryPort();
	//
	//		NotifyRequest notifyReq = new NotifyRequest();
	//		notifyReq.setRequest(sub, varName, value);
	//
	//		int retry = 0;
	//		while (notifyReq.post(host, port).isSuccessful() == false)
	//		{
	//			if (++retry >= NOTIFY_RETRY_NUM)
	//			{
	//				Debug.message("sub: notify failure [" + host + ":" + port + "][" + varName + ":" + value + "][Give Up!]");
	//				return false;
	//			}
	//			Debug.message("sub: notify failure [" + host + ":" + port + "][" + varName + ":" + value + "][Retry:" + retry + "]");
	//		}
	//
	//		Debug.message("sub: notify success [" + host + ":" + port + "][" + varName + ":" + value + "]");
	//		sub.incrementNotifyCount();
	//		return true;
	//	}

	public StateVariable getStateVar(final boolean forTVGuoApp)
	{
		if (forTVGuoApp)
			return getServiceData().getStateVar_external();
		else
			return getServiceData().getStateVar_internal();
	}

	public void setStateVar(StateVariable var, final boolean tvguo)
	{
		if (tvguo)
			getServiceData().setStateVar_external(var);
		else
			getServiceData().setStateVar_internal(var);
	}

	public void notifySubscribers(StateVariable var, final boolean tvguo)
	{
		if (tvguo)
		{
			synchronized (getSubscriberList_tvguo())
			{
				Debug.message("sub: wake up TVGuoApp NotifySubsriberThreads");
				setStateVar(var, tvguo);
				getSubscriberList_tvguo().notifyAll();
			}
		} else
		{
			synchronized (getSubscriberList_dlna())
			{
				Debug.message("sub: wake up DLNA NotifySubsriberThreads");
				setStateVar(var, tvguo);
				getSubscriberList_dlna().notifyAll();
			}
		}
	}

	public synchronized void stopNotifyThreads()
	{
		Debug.message("sub: stop NotifySubsriberThreads " + this.getServiceID());
		SubscriberList subList;
		int subListCnt;

		subList = getSubscriberList_dlna();
		synchronized (subList)
		{
			subListCnt = subList.size();
			Debug.message("sub: stop TVGuoApp NotifySubsriberThreads Count=" + subListCnt);
			for (int n = 0; n < subListCnt; n++)
			{
				Subscriber curSub = subList.getSubscriber(n);
				curSub.stop();
			}
			subList.clear();
		}

		subList = getSubscriberList_tvguo();
		synchronized (subList)
		{
			subListCnt = subList.size();
			Debug.message("sub: stop DLNA NotifySubsriberThreads Count=" + subListCnt);
			for (int n = 0; n < subListCnt; n++)
			{
				Subscriber curSub = subList.getSubscriber(n);
				curSub.stop();
			}
			subList.clear();
		}
	}

	public void notify(final StateVariable stateVar, final boolean forTVGuoApp)
	{
		if (forTVGuoApp)
		{
			Debug.message("sub: notify TVGuoApp [" + stateVar.getName() + "][" + stateVar.getValue_tvguo() + "]");
			if (stateVar.getValue_tvguo() == null || stateVar.getValue_tvguo().equals("")) {
				Debug.message("sub: skip notify TVGuoApp [" + stateVar.getName() + "][" + stateVar.getValue_tvguo() + "]");
				return;
			}
		} else
		{
			Debug.message("sub: notify DLNA [" + stateVar.getName() + "][" + stateVar.getValue_dlna() + "]");
			if (stateVar.getValue_dlna() == null || stateVar.getValue_dlna().equals("")) {
				Debug.message("sub: skip notify TVGuoApp [" + stateVar.getName() + "][" + stateVar.getValue_dlna() + "]");
				return;
			}
		}

		SubscriberList subList;
		if (forTVGuoApp == false)
			subList = getSubscriberList_dlna();
		else
			subList = getSubscriberList_tvguo();

		int subListCnt;
		Subscriber subs[];

		// Remove expired subscribers.
		subListCnt = subList.size();
		if (subListCnt == 0)
		{
			if (forTVGuoApp)
				Debug.message("sub: TVGuoApp list empty...");
			else
				Debug.message("sub: DLNA list empty...");
			return;
		}
		subs = new Subscriber[subListCnt];
		for (int n = 0; n < subListCnt; n++)
			subs[n] = subList.getSubscriber(n);
		for (int n = 0; n < subListCnt; n++)
		{
			Subscriber sub = subs[n];
			if (sub == null)
				continue;
			if (sub.isExpired() == true)
			{
				Debug.message("sub: removesubscriber..." + sub.getDeliveryURL());
				removeSubscriber(sub, forTVGuoApp);
			}
		}

		// 唤醒等待中的Notify进程
		notifySubscribers(stateVar, forTVGuoApp);
	}

	public void notifyAllStateVariables(boolean external)
	{
		ServiceStateTable stateTable = getServiceStateTable();
		int tableSize = stateTable.size();
		for (int n = 0; n < tableSize; n++)
		{
			StateVariable var = stateTable.getStateVariable(n);
			if (var.isSendEvents() == true)
				notify(var, external);
		}
	}

	////////////////////////////////////////////////
	// SID
	////////////////////////////////////////////////

	public String getSID()
	{
		return getServiceData().getSID();
	}

	public void setSID(String id)
	{
		getServiceData().setSID(id);
	}

	public void clearSID()
	{
		setSID("");
		setTimeout(0);
	}

	public boolean hasSID()
	{
		return StringUtil.hasData(getSID());
	}

	public boolean isSubscribed()
	{
		return hasSID();
	}

	////////////////////////////////////////////////
	// Timeout
	////////////////////////////////////////////////

	public long getTimeout()
	{
		return getServiceData().getTimeout();
	}

	public void setTimeout(long value)
	{
		getServiceData().setTimeout(value);
	}

	/**
	 * Add the StateVariable to the service.<br>
	 * <br>
	 * Note: This method should be used to create a dynamic<br>
	 * Device withtout writing any XML that describe the device<br>
	 * . <br>
	 * Note: that no control for duplicate StateVariable is done.
	 * 
	 * @param var
	 *            StateVariable that will be added
	 * 
	 * @author Stefano "Kismet" Lenzi - kismet-sl@users.sourceforge.net - 2005
	 */
	public void addStateVariable(StateVariable var)
	{
		//TODO Some test are done not stable
		Node stateTableNode = getSCPDNode().getNode(ServiceStateTable.ELEM_NAME);
		if (stateTableNode == null)
		{
			stateTableNode = new Node(ServiceStateTable.ELEM_NAME);
			/*
			 * Force the node <serviceStateTable> to be the first node inside <scpd>
			 */
			//getSCPDNode().insertNode(stateTableNode,0);
			getSCPDNode().addNode(stateTableNode);
		}
		var.setServiceNode(getServiceNode());
		stateTableNode.addNode(var.getStateVariableNode());
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

	//	private Map<String, Object> mStateVariableCache = new HashMap<String, Object>();
	//	
	//	public void setStateVariable(String name, String value) {
	//		
	//	}

}
