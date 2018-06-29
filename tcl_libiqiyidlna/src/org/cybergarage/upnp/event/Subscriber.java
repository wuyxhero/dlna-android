/******************************************************************
 *
 *	CyberUPnP for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: Subscriber.java
 *
 *	Revision;
 *
 *	01/29/03
 *		- first revision.
 *	07/31/04
 *		- Added isExpired().
 *	10/26/04
 *		- Oliver Newell <newell@media-rush.com>
 *		- Added support the intinite time and fixed a bug in isExpired().
 *	
 ******************************************************************/

package org.cybergarage.upnp.event;

import java.net.*;

import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.StateVariable;
import org.cybergarage.util.Debug;
import org.cybergarage.util.ThreadCore;
import org.cybergarage.xml.Node;

public class Subscriber extends ThreadCore
{
	////////////////////////////////////////////////
	//	Constructor
	////////////////////////////////////////////////

	public Subscriber()
	{
		renew();
	}

	////////////////////////////////////////////////
	//	SID
	////////////////////////////////////////////////

	private String SID = null;

	public String getSID()
	{
		return SID;
	}

	public void setSID(String sid)
	{
		SID = sid;
	}

	////////////////////////////////////////////////
	//	deliveryURL
	////////////////////////////////////////////////

	private String ifAddr = "";

	public void setInterfaceAddress(String addr)
	{
		ifAddr = addr;
	}

	public String getInterfaceAddress()
	{
		return ifAddr;
	}

	////////////////////////////////////////////////
	//	deliveryURL
	////////////////////////////////////////////////

	private String deliveryURL = "";

	public String getDeliveryURL()
	{
		return deliveryURL;
	}

	public void setDeliveryURL(String deliveryURL)
	{
		this.deliveryURL = deliveryURL;
		try
		{
			URL url = new URL(deliveryURL);
			deliveryHost = url.getHost();
			deliveryPath = url.getPath();
			deliveryPort = url.getPort();
		} catch (Exception e)
		{
		}
	}

	private String deliveryHost = "";
	private String deliveryPath = "";
	private int deliveryPort = 0;

	public String getDeliveryHost()
	{
		return deliveryHost;
	}

	public String getDeliveryPath()
	{
		return deliveryPath;
	}

	public int getDeliveryPort()
	{
		return deliveryPort;
	}

	////////////////////////////////////////////////
	//	Timeout
	////////////////////////////////////////////////

	private long timeOut = 0;

	public long getTimeOut()
	{
		return timeOut;
	}

	public void setTimeOut(long value)
	{
		timeOut = value;
	}

	public boolean isExpired()
	{
		long currTime = System.nanoTime();

		// Thanks for Oliver Newell (10/26/04)
		if (timeOut == Subscription.INFINITE_VALUE)
			return false;

		// Thanks for Oliver Newell (10/26/04)
		long expiredTime = getSubscriptionTime() + (getTimeOut() * 1000000000);
		if (expiredTime < currTime)
			return true;

		return false;
	}

	////////////////////////////////////////////////
	//	SubscriptionTIme
	////////////////////////////////////////////////

	private long subscriptionTime = 0;

	public long getSubscriptionTime()
	{
		return subscriptionTime;
	}

	public void setSubscriptionTime(long time)
	{
		subscriptionTime = time;
	}

	////////////////////////////////////////////////
	//	SEQ
	////////////////////////////////////////////////

	private long notifyCount = 0;

	public long getNotifyCount()
	{
		return notifyCount;
	}

	public void setNotifyCount(int cnt)
	{
		notifyCount = cnt;
	}

	public void incrementNotifyCount()
	{
		if (notifyCount == Long.MAX_VALUE)
		{
			notifyCount = 1;
			return;
		}
		notifyCount++;
	}

	////////////////////////////////////////////////
	//	renew
	////////////////////////////////////////////////

	public void renew()
	{
		setSubscriptionTime(System.nanoTime());
		setNotifyCount(0);
	}

	private boolean mTvguo = false;
	private Service mService = null;

	private String mValue = "";
	private SubscriberList mList = null;

	public void initThreadParams(Node serviceNode, boolean tvguo)
	{
		mTvguo = tvguo;
		mService = new Service(serviceNode);
		mList = (mTvguo == true) ? mService.getSubscriberList_tvguo() : mService.getSubscriberList_dlna();

		//		StateVariable var = mService.getStateVar(mTvguo);
		//		if (var != null)
		//			mValue = (mTvguo == true) ? var.getValue_tvguo() : var.getValue_dlna();
	}

	public void run()
	{
		Debug.message("[Subscriber] start...[" + deliveryURL + "]");

		int retryCount = 0;
		NotifyRequest notifyReq = new NotifyRequest();

		while (isRunnable() == true)
		{
			synchronized (mList)
			{
				try
				{
					String tmpValue = "";
					StateVariable var = mService.getStateVar(mTvguo);
					if (var != null)
						tmpValue = (mTvguo == true) ? var.getValue_tvguo() : var.getValue_dlna();

					if (mValue.equals(tmpValue) || retryCount >= 10)
					{
						// 状态没有发生变化等待唤醒
						Debug.message("[Subscriber] sleep...[" + deliveryURL + "]");
						mList.wait();
						retryCount = 0;
						Debug.message("[Subscriber] wakeup...[" + deliveryURL + "]");
					}
				} catch (InterruptedException e)
				{
					Debug.message("[Subscriber] interrupted 1...[" + deliveryURL + "]");
					break;
				}
			}

			// 确认当前Subscriber还在列表中,否则退出线程
			if (mService.getSubscriber(SID, mTvguo) == null)
			{
				Debug.message("[Subscriber] expired...[" + deliveryURL + "]");
				break;
			}

			// 尝试连接Subscriber并发送Notify信息
			StateVariable stateVar = mService.getStateVar(mTvguo);
			if (stateVar == null)
			{
				Debug.message("[Subscriber] mad world continue...[" + deliveryURL + "]");
				continue;
			}
			String name = stateVar.getName();
			String value = (mTvguo == true) ? stateVar.getValue_tvguo() : stateVar.getValue_dlna();

			Debug.message("[Subscriber] notify [" + deliveryURL + "][" + name + ":" + value + "]");
			notifyReq.setRequest(this, name, value);

			boolean ret = notifyReq.post(deliveryHost, deliveryPort, true).isSuccessful();

			if (ret)
			{
				Debug.message("[Subscriber] notify success [" + deliveryURL + "][" + name + ":" + value + "]");
				mValue = value;
				incrementNotifyCount();
			} else
			{
				retryCount++;
				Debug.message("[Subscriber] notify failure [" + deliveryURL + "][" + name + ":" + value + "] retryCount="
						+ retryCount);
				if (retryCount >= 2)
				{
					Debug.message("[Subscriber] notify failure [Give up!!!] [" + deliveryURL + "][" + name + ":" + value
							+ "] retryCount=" + retryCount);
				}
				try
				{
					Thread.sleep(2000); // ２秒后重试
				} catch (InterruptedException e)
				{
					Debug.message("[Subscriber] interrupted 2...[" + deliveryURL + "]");
					break;
				}
			}
		}

		notifyReq.closeHostSocket();
		Debug.message("[Subscriber] exit...[" + deliveryURL + "]");
	}
}
