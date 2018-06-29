package org.cybergarage.upnp;

import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

public class NetworkMonitor
{

	private static NetworkMonitor sInstance = null;

	private static final long GOOD_RESPONSE_TIME = 200;
	private static final long NORMAL_RESPONSE_TIME = 1000;
	public static final long BAD_RESPONSE_TIME = 2000;
	public static final long SUPER_BAD_RESPONSE_TIME = 4000;

	private static final int BAD_QUEUE_LENGTH = 1;
	private static final int NORMAL_QUEUE_LENGTH = 1;

	private LinkedBlockingQueue<Long> mBadQueue = null;
	private LinkedBlockingQueue<Long> mNotBadQueue = null;
	private NETWORK_STATUS mNetStatus = NETWORK_STATUS.OK;
	private long mLastResponseTime = 0;
	private Vector<NetworkStatusListener> mListeners = null;

	public static NetworkMonitor getInstance()
	{
		if (sInstance == null)
		{
			sInstance = new NetworkMonitor();
		}

		return sInstance;
	}

	private NetworkMonitor()
	{

		if (mListeners == null)
		{
			mListeners = new Vector<NetworkStatusListener>();
		} else
		{
			mListeners.clear();
		}

		mNetStatus = NETWORK_STATUS.OK;
		mLastResponseTime = 0;

		checkQueuesAvailable();
	}

	public NETWORK_STATUS getNetworkStatus()
	{
		return mNetStatus;
	}

	public long getLastResponseTime()
	{
		return mLastResponseTime;
	}

	private void checkQueuesAvailable()
	{
		if (mBadQueue == null)
		{
			mBadQueue = new LinkedBlockingQueue<Long>(BAD_QUEUE_LENGTH);
		}

		if (mNotBadQueue == null)
		{
			mNotBadQueue = new LinkedBlockingQueue<Long>(NORMAL_QUEUE_LENGTH);
		}

	}

	public void release()
	{
		if (mBadQueue != null)
		{
			mBadQueue.clear();
			mBadQueue = null;
		}

		if (mNotBadQueue != null)
		{
			mNotBadQueue.clear();
			mNotBadQueue = null;
		}

		if (mListeners != null)
		{
			mListeners.clear();
			mListeners = null;
		}

		mLastResponseTime = 0;

		sInstance = null;
	}

	void notifyResponseTime(long responseTime)
	{
		if (responseTime <= 0)
		{
			return; //invalid Value
		}

		mLastResponseTime = responseTime;

		if (mListeners != null && !mListeners.isEmpty())
		{
			for (NetworkStatusListener listener : mListeners)
			{
				listener.OnResponseTimeGot(responseTime);
			}
		}

		if (responseTime <= NORMAL_RESPONSE_TIME)
		{
			notifyNotBad(responseTime);
		} else if (responseTime >= BAD_RESPONSE_TIME)
		{
			notifyBad(responseTime);
		} else
		{
			notifyOther(responseTime);
		}
	}

	private void notifyNotBad(long responseTime)
	{
		checkQueuesAvailable();

		boolean offerResult = mNotBadQueue.offer(Long.valueOf(responseTime));

		if (offerResult == false || responseTime <= GOOD_RESPONSE_TIME) //normal queue is already full
		{
			mBadQueue.clear();
			setStatus(NETWORK_STATUS.OK, responseTime);
		}
	}

	private void notifyBad(long responseTime)
	{
		checkQueuesAvailable();

		boolean offerResult = mBadQueue.offer(Long.valueOf(responseTime));

		if (offerResult == false || responseTime >= SUPER_BAD_RESPONSE_TIME)
		{
			mNotBadQueue.clear();
			setStatus(NETWORK_STATUS.BAD, responseTime);
		}
	}

	private void notifyOther(long responseTime)
	{
		//Buffer, keep last netWork Status,just do nothing.
	}

	private void setStatus(NETWORK_STATUS status, long responseTime)
	{

		if (status == null || status == mNetStatus)
		{
			return;
		}

		mNetStatus = status;

		if (mListeners != null && !mListeners.isEmpty())
		{
			for (NetworkStatusListener listener : mListeners)
			{
				listener.OnNetworkStatusChanged(status);
			}
		}
	}

	public boolean addNetworkStatusListener(NetworkStatusListener listener)
	{
		if (listener == null)
		{
			return false;
		}

		if (mListeners == null)
		{
			mListeners = new Vector<NetworkStatusListener>();
		}

		return mListeners.add(listener);
	}

	public boolean removeNetworkStatusListener(NetworkStatusListener listener)
	{
		if (listener == null || mListeners == null || mListeners.isEmpty())
		{
			return false;
		}

		return mListeners.remove(listener);
	}

}
