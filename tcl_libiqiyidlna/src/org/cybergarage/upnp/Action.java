/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2004
 *
 *	File: Action.java
 *
 *	Revision;
 *
 *	12/05/02
 *		- first revision.
 *	08/30/03
 *		- Gordano Sassaroli <sassarol@cefriel.it>
 *		- Problem    : When invoking an action that has at least one out parameter, an error message is returned
 *		- Error      : The action post method gets the entire list of arguments instead of only the in arguments
 *	01/04/04
 *		- Added UPnP status methods.
 *		- Changed about new ActionListener interface.
 *	01/05/04
 *		- Added clearOutputAgumentValues() to initialize the output values before calling performActionListener().
 *	07/09/04
 *		- Thanks for Dimas <cyberrate@users.sourceforge.net> and Stefano Lenzi <kismet-sl@users.sourceforge.net>
 *		- Changed postControlAction() to set the status code to the UPnPStatus.
 *	04/12/06
 *		- Added setUserData() and getUserData() to set a user original data object.
 *
 ******************************************************************/

package org.cybergarage.upnp;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.cybergarage.http.HTTP;
import org.cybergarage.http.HTTPHeader;
import org.cybergarage.upnp.control.ActionListener;
import org.cybergarage.upnp.control.ActionRequest;
import org.cybergarage.upnp.control.ActionResponse;
import org.cybergarage.upnp.control.ControlResponse;
import org.cybergarage.upnp.xml.ActionData;
import org.cybergarage.util.Debug;
import org.cybergarage.util.Mutex;
import org.cybergarage.xml.Node;

public class Action
{
	public Boolean isKeepAlive = null;//是否支持长连接
	////////////////////////////////////////////////
	//	Constants
	////////////////////////////////////////////////

	public final static String ELEM_NAME = "action";

	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	private Node serviceNode;
	private Node actionNode;

	private Node getServiceNode()
	{
		return serviceNode;
	}

	public Service getService()
	{
		return new Service(getServiceNode());
	}

	void setService(Service s)
	{
		serviceNode = s.getServiceNode();
		/*To ensure integrity of the XML structure*/
		Iterator<Argument> i = getArgumentList().iterator();
		while (i.hasNext())
		{
			Argument arg = (Argument) i.next();
			arg.setService(s);
		}
	}

	public Node getActionNode()
	{
		return actionNode;
	}

	public boolean isKeepAlive()
	{
		return isKeepAlive;
	}

	public void setKeepAlive(boolean keepAlive)
	{
		isKeepAlive = keepAlive;
	}

	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////
	public Action(Node serviceNode)
	{
		//TODO Test
		this.serviceNode = serviceNode;
		this.actionNode = new Node(Action.ELEM_NAME);
	}

	public Action(Node serviceNode, Node actionNode)
	{
		this.serviceNode = serviceNode;
		this.actionNode = actionNode;
	}

	public Action(Action action)
	{
		this.serviceNode = action.getServiceNode();
		this.actionNode = action.getActionNode();
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
	//	isActionNode
	////////////////////////////////////////////////

	public static boolean isActionNode(Node node)
	{
		return Action.ELEM_NAME.equals(node.getName());
	}

	////////////////////////////////////////////////
	//	name
	////////////////////////////////////////////////

	private final static String NAME = "name";

	public void setName(String value)
	{
		getActionNode().setNode(NAME, value);
	}

	public String getName()
	{
		return getActionNode().getNodeValue(NAME);
	}

	////////////////////////////////////////////////
	//	argumentList
	////////////////////////////////////////////////

	public ArgumentList getArgumentList()
	{
		ArgumentList argumentList = new ArgumentList();
		Node argumentListNode = getActionNode().getNode(ArgumentList.ELEM_NAME);
		if (argumentListNode == null)
			return argumentList;
		int nodeCnt = argumentListNode.getNNodes();
		for (int n = 0; n < nodeCnt; n++)
		{
			Node node = argumentListNode.getNode(n);
			if (Argument.isArgumentNode(node) == false)
				continue;
			Argument argument = new Argument(getServiceNode(), node);
			argumentList.add(argument);
		}
		return argumentList;
	}

	public void setArgumentList(ArgumentList al)
	{
		Node argumentListNode = getActionNode().getNode(ArgumentList.ELEM_NAME);
		if (argumentListNode == null)
		{
			argumentListNode = new Node(ArgumentList.ELEM_NAME);
			getActionNode().addNode(argumentListNode);
		} else
		{
			argumentListNode.removeAllNodes();
		}
		Iterator<Argument> i = al.iterator();
		while (i.hasNext())
		{
			Argument a = (Argument) i.next();
			a.setService(getService());
			argumentListNode.addNode(a.getArgumentNode());
		}

	}

	public ArgumentList getInputArgumentList()
	{
		ArgumentList allArgList = getArgumentList();
		int allArgCnt = allArgList.size();
		ArgumentList argList = new ArgumentList();
		for (int n = 0; n < allArgCnt; n++)
		{
			Argument arg = allArgList.getArgument(n);
			if (arg.isInDirection() == false)
				continue;
			argList.add(arg);
		}
		return argList;
	}

	public ArgumentList getOutputArgumentList()
	{
		ArgumentList allArgList = getArgumentList();
		int allArgCnt = allArgList.size();
		ArgumentList argList = new ArgumentList();
		for (int n = 0; n < allArgCnt; n++)
		{
			Argument arg = allArgList.getArgument(n);
			if (arg.isOutDirection() == false)
				continue;
			argList.add(arg);
		}
		return argList;
	}

	public Argument getArgument(String name)
	{
		ArgumentList argList = getArgumentList();
		int nArgs = argList.size();
		for (int n = 0; n < nArgs; n++)
		{
			Argument arg = argList.getArgument(n);
			String argName = arg.getName();
			if (argName == null)
				continue;
			if (name.equals(argName) == true)
				return arg;
		}
		return null;
	}

	/**
	 * @deprecated You should use one of the following methods instead:<br />
	 *             - {@link #setInArgumentValues(ArgumentList)} <br/>
	 *             - {@link #setOutArgumentValues(ArgumentList)}
	 */
	public void setArgumentValues(ArgumentList argList)
	{
		getArgumentList().set(argList);
	}

	/**
	 * 
	 * @param argList
	 * @since 1.8.0
	 */
	public void setInArgumentValues(ArgumentList argList)
	{
		getArgumentList().setReqArgs(argList);
	}

	/**
	 * 
	 * @param argList
	 * @since 1.8.0
	 */
	public void setOutArgumentValues(ArgumentList argList)
	{
		getArgumentList().setResArgs(argList);
	}

	public void setArgumentValue(String name, String value)
	{
		Argument arg = getArgument(name);
		if (arg == null)
			return;
		arg.setValue(value);
	}

	public void setArgumentValue(String name, int value)
	{
		setArgumentValue(name, Integer.toString(value));
	}

	private void clearOutputAgumentValues()
	{
		ArgumentList allArgList = getArgumentList();
		int allArgCnt = allArgList.size();
		for (int n = 0; n < allArgCnt; n++)
		{
			Argument arg = allArgList.getArgument(n);
			if (arg.isOutDirection() == false)
				continue;
			arg.setValue("");
		}
	}

	public String getArgumentValue(String name)
	{
		Argument arg = getArgument(name);
		if (arg == null)
			return "";
		return arg.getValue();
	}

	public int getArgumentIntegerValue(String name)
	{
		Argument arg = getArgument(name);
		if (arg == null)
			return 0;
		return arg.getIntegerValue();
	}

	////////////////////////////////////////////////
	//	UserData
	////////////////////////////////////////////////

	private ActionData getActionData()
	{
		Node node = getActionNode();
		ActionData userData = (ActionData) node.getUserData();
		if (userData == null)
		{
			userData = new ActionData();
			node.setUserData(userData);
			userData.setNode(node);
		}
		return userData;
	}

	////////////////////////////////////////////////
	//	controlAction
	////////////////////////////////////////////////

	public ActionListener getActionListener()
	{
		return getActionData().getActionListener();
	}

	public void setActionListener(ActionListener listener)
	{
		getActionData().setActionListener(listener);
	}

	private ActionRequest curActionReq = null;

	public ActionRequest getCurActionReq() {
		return curActionReq;
	}

	/*
	 * 接收到Action消息，绝对是否对这个Action进行回复
	 */
	public boolean performActionListener(ActionRequest actionReq)
	{
		ActionListener listener = (ActionListener) getActionListener();
		if (listener == null)
			return false;

		curActionReq = actionReq;

		ActionResponse actionRes = new ActionResponse();
		setStatus(UPnPStatus.INVALID_ACTION);

		// TODO 这里不知道为什么要清理
		// clearOutputAgumentValues();

		/*
		 * 实时体验策略
		 */
		long dmrTimes = System.currentTimeMillis();//当前DMR的时间
		HTTPHeader replyHeader = actionReq.getHeader(HTTP.REPLY);//是否有需要回复的标示

		HTTPHeader maxDelayHeader = actionReq.getHeader(HTTP.MAXDELAYTIME);//有最大延时的时间设置，说明需要实时体验
		if (maxDelayHeader != null)
		{
			HTTPHeader dmcTimeHeader = actionReq.getHeader(HTTP.DMCTIME);//DMC的当前时间
			HTTPHeader diffTimeHeader = actionReq.getHeader(HTTP.DIFFTIME);//DMR与DMC的差值
			if (dmcTimeHeader != null && diffTimeHeader != null)
			{
				String dmcTimeValue = dmcTimeHeader.getValue();
				String diffTimeValue = diffTimeHeader.getValue();
				String maxDelayTimeValue = maxDelayHeader.getValue();
				if (dmcTimeValue != null && diffTimeValue != null && maxDelayTimeValue != null)
				{
					long dmcTimes = Long.parseLong(dmcTimeValue);
					long diffTimes = Long.parseLong(diffTimeValue);
					long maxDelayTimes = Long.parseLong(maxDelayTimeValue);

					Debug.message("dmc:" + dmcTimes);
					Debug.message("diffTimes:" + diffTimes);
					Debug.message("maxDelayTimes:" + maxDelayTimes);
					long absDelayTime = Math.abs(dmrTimes - dmcTimes - diffTimes);
					if (absDelayTime > maxDelayTimes)
					{
						Debug.message("delay times is:" + absDelayTime);
						String replyValue = "";
						if (replyHeader != null)
						{
							replyValue = replyHeader.getValue();
						}
						if (replyValue.compareTo("1") == 0)//如果这个消息需要回复，则还是要给客户端回复，不过为空值
						{
							Debug.message("give up message to DMR!but reply to dmc!");
							actionRes.setResponse(this);
							return actionReq.post(actionRes);//直接回复这个消息，但是不回调给DMR
						}
						Debug.message("give up the message!!");//不会掉，但是还是要给客户端回复
						return true;
					} else
					{
						Debug.message("delay times is:" + absDelayTime);
					}
				}
			}
		}

		if (listener.actionControlReceived(this) == true)
		{
			/*
			 * 判断报头，看这个报头是否有需要回复的
			 */
			if (replyHeader != null)
			{
				String replyValue = replyHeader.getValue();
				if (replyValue != null)
				{
					if (replyValue.compareTo("0") == 0)//不需要回复
					{
						Debug.message("DMR不需要回复消息");
						return true;//不需要回复消息，直接成功即可
					} else
					{
						//无论是否有实时要求，都进行发送DMR的当前系统的时间戳值
						//移除HTTP.DMRTIME节省DMC sendmessage返回时间
						//actionRes.setHeader(HTTP.DMRTIME,System.currentTimeMillis());
					}
				}
			}
			actionRes.setResponse(this);
		} else
		{
			UPnPStatus upnpStatus = getStatus();
			actionRes.setFaultResponse(upnpStatus.getCode(), upnpStatus.getDescription());
		}

		if (Debug.isOn() == true)
			actionRes.print();

		//这是发送回复消息
		return actionReq.post(actionRes);
	}

	////////////////////////////////////////////////
	//	ActionControl
	////////////////////////////////////////////////

	private ControlResponse getControlResponse()
	{
		return getActionData().getControlResponse();
	}

	private void setControlResponse(ControlResponse res)
	{
		getActionData().setControlResponse(res);
	}

	public UPnPStatus getControlStatus()
	{
		return getControlResponse().getUPnPError();
	}

	////////////////////////////////////////////////
	//	postControlAction
	////////////////////////////////////////////////

	private long adjustingCount = 0;//表示DMC和DMR之间的进行时间校准的次数
	private long differenceTime = 0;//DMC和DMR的时间差值
	private final long MIN_ADJUST_TIMNE_COUNT = 20;//最小的校准时间次数，默认是10次，最少要校准20次，才能使用
	private final long MAX_ADJUST_TIME_COUNT = 100;//100此够了

	private static final long TIMER_INTERVAL = 500;//间隔

	//	private static class Timer extends CountDownTimer
	//	{
	//		private boolean isTimeoutTriggered = false;
	//		
	//		public Timer(long millisInFuture, long countDownInterval) {
	//			super(millisInFuture, countDownInterval);
	//			isTimeoutTriggered = false;
	//		}
	//
	//		@Override
	//		public void onTick(long millisUntilFinished) {
	//			long elapsedTime = TIMER_TOTAL_TIME - millisUntilFinished;
	//			if(elapsedTime == 0)
	//			{
	//				return;
	//			}
	//			
	//			if(elapsedTime == NetworkMonitor.BAD_RESPONSE_TIME || elapsedTime % NetworkMonitor.SUPER_BAD_RESPONSE_TIME == 0)
	//			{
	//				NetworkMonitor.getInstance().notifyResponeTime(NetworkMonitor.BAD_RESPONSE_TIME);
	//				isTimeoutTriggered = true;
	//			}
	//		}
	//
	//		@Override
	//		public void onFinish() {
	//			this.cancel();
	//			NetworkMonitor.getInstance().notifyResponeTime(NetworkMonitor.BAD_RESPONSE_TIME);
	//			isTimeoutTriggered = true;
	//		}
	//		
	//		public boolean isTimeoutTriggered()
	//		{
	//			return isTimeoutTriggered;
	//		}
	//	}

	private boolean isTimeoutTriggered = false;
	private long mTimerBeginTime;
	private Timer mTimer = null;
	private TimerTask mTimerTask = null;

	private void setRealTimeStrategy(ActionRequest ctrlReq)//在消息报头设置实时信息,越用越准确
	{
		if (ControlPoint.isOpenRealTime == true)
		{
			if (adjustingCount >= MIN_ADJUST_TIMNE_COUNT)
			{
				if (ctrlReq != null)
				{
					ctrlReq.setHeader(HTTP.MAXDELAYTIME, ControlPoint.maxDelayTime);//最大的延时时间
					ctrlReq.setHeader(HTTP.DMCTIME, System.currentTimeMillis());//当前DMC时间
					ctrlReq.setHeader(HTTP.DIFFTIME, differenceTime);//DMC-DMR的时间差值
				}
			}
		}
	}

	public boolean postControlAction()
	{
		if (isKeepAlive == null)
			isKeepAlive = false;

		// Thanks for Giordano Sassaroli <sassarol@cefriel.it> (08/30/03)
		ArgumentList actionArgList = getArgumentList();
		ArgumentList actionInputArgList = getInputArgumentList();

		ActionRequest ctrlReq = getActionRequest();
		ctrlReq.setHeader(HTTP.REPLY, "1");//增加报头，这个报头指出是否需要服务端回复这个消息  1表示回复
		ctrlReq.setRequest(this, actionInputArgList);

		if (Debug.isOn() == true)
			ctrlReq.print();
		/*
		 * 实时体验策略
		 */
		setRealTimeStrategy(ctrlReq);//设置实时策略

		long beginsendTime = System.currentTimeMillis();

		resetTimer();

		ActionResponse ctrlRes = ctrlReq.post(true, isKeepAlive); //需要回复的

		if (Debug.isOn() == true)
			ctrlRes.print();

		setControlResponse(ctrlRes);

		int statCode = ctrlRes.getStatusCode();
		setStatus(statCode);

		long receiveTime = System.currentTimeMillis();//接收到消息的时间
		long responeTime = receiveTime - beginsendTime;//得到了从DMC到DMR的时间差值

		if (!isTimeoutTriggered)
		{
			NetworkMonitor.getInstance().notifyResponseTime(responeTime);
		}

		stopTimer();

		Debug.message("responseTime:" + responeTime);
		if (ctrlRes.isSuccessful() == false)
		{

			return false;
		}
		//必须是返回成功才行
		//提取DMR的当前时间
		HTTPHeader dmrTimeHeader = ctrlRes.getHeader(HTTP.DMRTIME);
		if (dmrTimeHeader != null && responeTime < 1000)//只有这个往返时间小于1000ms，这会进入迭代考虑，这样才会越来越准确
		{
			String dmrTimeStr = dmrTimeHeader.getValue();
			if (dmrTimeStr != null)
			{
				long dmrTime = Long.parseLong(dmrTimeStr);
				long sendTime = (long) (0.5 * responeTime);//发送时间和应答时间的比例是5:5
				long tmptime = dmrTime - beginsendTime - sendTime;
				long tmptime1 = receiveTime - dmrTime - sendTime;
				Debug.message("dmrTime:" + dmrTime);
				Debug.message("sendTime:" + sendTime);
				Debug.message("tmpTime:" + tmptime);//从dmr到dmc的差值
				if (Math.abs(Math.abs(tmptime) - Math.abs(tmptime1)) < 100)//两者相差100s，才是可信的
				{
					if (differenceTime == 0)//第一次赋值
					{
						adjustingCount = 1;//第一次
						differenceTime = tmptime;
					} else
					{
						if (adjustingCount < MAX_ADJUST_TIME_COUNT)//只算100次即可，这个值够精确了,找到100次内最小的值
						{
							if (differenceTime > tmptime)
							{
								differenceTime = tmptime;
							}
							adjustingCount++;
						}
					}
					Debug.message("DMC diff DMR Time is:" + differenceTime);
				}
			}
		}

		ArgumentList outArgList = ctrlRes.getResponse();
		try
		{
			actionArgList.setResArgs(outArgList);
		} catch (IllegalArgumentException ex)
		{
			setStatus(UPnPStatus.INVALID_ARGS, "Action succesfully delivered but invalid arguments returned.");
			return false;
		}
		return true;
	}

	private void stopTimer()
	{
		if (mTimer != null)
		{
			if (mTimerTask != null)
			{
				mTimerTask.cancel();
				mTimerTask = null;
			}
			mTimer.cancel();
			mTimer = null;
		}
	}

	private void resetTimer()
	{
		stopTimer();
		isTimeoutTriggered = false;
		mTimer = new Timer();
		mTimerTask = new TimerTask()
		{

			@Override
			public void run()
			{
				long elapsedTime = System.currentTimeMillis() - mTimerBeginTime;
				if (elapsedTime <= 0)
				{
					return;
				} else if (elapsedTime >= NetworkMonitor.BAD_RESPONSE_TIME
						&& elapsedTime < NetworkMonitor.SUPER_BAD_RESPONSE_TIME)
				{
					if (!isTimeoutTriggered)
					{
						NetworkMonitor.getInstance().notifyResponseTime(NetworkMonitor.BAD_RESPONSE_TIME);
						isTimeoutTriggered = true;
					}
				} else if (elapsedTime % NetworkMonitor.SUPER_BAD_RESPONSE_TIME < 50
						|| elapsedTime % NetworkMonitor.SUPER_BAD_RESPONSE_TIME > NetworkMonitor.SUPER_BAD_RESPONSE_TIME - 50)
				{
					NetworkMonitor.getInstance().notifyResponseTime(NetworkMonitor.SUPER_BAD_RESPONSE_TIME);
					isTimeoutTriggered = true;
				}
			}
		};
		mTimerBeginTime = System.currentTimeMillis();
		mTimer.schedule(mTimerTask, TIMER_INTERVAL, TIMER_INTERVAL);
	}

	private ActionRequest actionRequest = null;

	public ActionRequest getActionRequest()
	{
		if (actionRequest == null)
		{
			actionRequest = new ActionRequest();
			actionRequest.setVersion(HTTP.VERSION_11);
		}
		return actionRequest;

	}

	/*
	 * 不需要读取回复的值
	 */
	public boolean postControlActionNoReply()
	{
		if (isKeepAlive == null)
			isKeepAlive = false;

		ArgumentList actionInputArgList = getInputArgumentList();
		/*
		 * 这里需要修改一个BUG，这样才能正确使用长连接策略
		 */
		ActionRequest ctrlReq = getActionRequest();

		ctrlReq.setHeader(HTTP.REPLY, "0");//不需要回复   0表示不需要回复

		ctrlReq.setRequest(this, actionInputArgList);

		if (Debug.isOn() == true)
			ctrlReq.print();

		setRealTimeStrategy(ctrlReq);//设置实时策略

		if (ctrlReq.post(false, isKeepAlive) == null)//不需要回复
		{
			return false;
		}
		return true;
	}

	////////////////////////////////////////////////
	//	Debug
	////////////////////////////////////////////////

	public void print()
	{
		System.out.println("Action : " + getName());
		ArgumentList argList = getArgumentList();
		int nArgs = argList.size();
		for (int n = 0; n < nArgs; n++)
		{
			Argument arg = argList.getArgument(n);
			String name = arg.getName();
			String value = arg.getValue();
			String dir = arg.getDirection();
			System.out.println(" [" + n + "] = " + dir + ", " + name + ", " + value);
		}
	}

	////////////////////////////////////////////////
	//	UPnPStatus
	////////////////////////////////////////////////

	private UPnPStatus upnpStatus = new UPnPStatus();

	public void setStatus(int code, String descr)
	{
		upnpStatus.setCode(code);
		upnpStatus.setDescription(descr);
	}

	public void setStatus(int code)
	{
		setStatus(code, UPnPStatus.code2String(code));
	}

	public UPnPStatus getStatus()
	{
		return upnpStatus;
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
}
