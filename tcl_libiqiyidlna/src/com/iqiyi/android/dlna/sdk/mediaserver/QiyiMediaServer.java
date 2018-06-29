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
package com.iqiyi.android.dlna.sdk.mediaserver;

import java.io.File;
import java.io.IOException;

import org.cybergarage.upnp.std.av.server.MediaServer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;

/**
 * 
 * @author QIYI
 * 
 */
public abstract class QiyiMediaServer extends Service
{
	private MediaServer mMediaServer = null;

	private LocalBinder mLocalBinder = new LocalBinder();

	private String mDefaultRootDir = "/http";

	private WifiStateReceiver mWifiStateListener = new WifiStateReceiver();

	@Override
	public IBinder onBind(Intent arg0)
	{
		// TODO Auto-generated method stub
		return mLocalBinder;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		try
		{
			mediaServerInit();
		} catch (IOException e)
		{
			e.printStackTrace();
			stopSelf();
			return;
		}

		mediaServerStart();
		registerWifiStateReceiver();

	}

	private void mediaServerInit() throws IOException
	{
		String rootDir = getRootDir();
		if (TextUtils.isEmpty(rootDir))
		{
			rootDir = mDefaultRootDir;
		}

		String path = Environment.getExternalStorageDirectory() + rootDir;
		File file = new File(path);
		if (!file.exists())
		{
			file.mkdir();
		} else if (file.isFile())
		{
			throw new IOException("file exists, and it's not a directory! ");
		}

		mMediaServer = new MediaServer();
		mMediaServer.setWorkingState(getWorkingState());
		setupMediaServer(mMediaServer);
		mMediaServer.setServerRootDir(rootDir);
	}

	private void registerWifiStateReceiver()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
		registerReceiver(mWifiStateListener, filter);
	}

	private void unregisterWifiStateReveiver()
	{
		this.unregisterReceiver(mWifiStateListener);
	}

	/**
	 * 返回所指定的DMS web server在本地的根目录，非绝对目录，而是"/sdcard"下的目录。如，实际的服务目录为"/sdcard/yourapplication", 那么就返回"/yourapplication".
	 * 
	 * @return 如 "/yourapplication"
	 */

	abstract protected String getRootDir();

	/**
	 * 这里提供一个设置DMS个性信息的机会。如:<br>
	 * protected void setupMediaServer(MediaServer mediaServer) { <br>
	 * mediaServer.setFriendlyName("IQIYI_TV_(24:CF:21:C0:00:20)");<br>
	 * mediaServer.setManufacture("iqiyi");<br>
	 * mediaServer.setManufactureURL("http://www.iqiyi.com");<br>
	 * mediaServer.setModelDescription("QiYi AV Media Server Device");<br>
	 * mediaServer.setModelName("IQIYI AV Media Server Device");<br>
	 * mediaServer.setModelNumber("1234");<br>
	 * mediaServer.setModelURL("http://www.iqiyi.com/qiyi");<br>
	 * 
	 * }<br>
	 * 
	 * @param mediaServer
	 */
	abstract protected void setupMediaServer(MediaServer mediaServer);

	/**
	 * 
	 * @return
	 */
	abstract protected int getWorkingState();

	private void mediaServerStart()
	{
		Thread th = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				mMediaServer.start();
			}
		});
		th.start();
	}

	private void mediaServerRestart()
	{
		Thread th = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				mMediaServer.restart();
			}
		});
		th.start();
	}

	private void mediaServerStop()
	{
		Thread th = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				mMediaServer.stop();

			}
		});

		th.start();
	}

	public MediaServer getMeidaServer()
	{
		return mMediaServer;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		mediaServerStop();
		unregisterWifiStateReveiver();
	}

	@Override
	public void onRebind(Intent intent)
	{
		super.onRebind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		return super.onUnbind(intent);
	}

	class LocalBinder extends Binder implements IMediaServerBinder
	{

		@Override
		public QiyiMediaServer getDigitalMediaServer()
		{
			return QiyiMediaServer.this;
		}

	}

	class WifiStateReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();

			if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action))
			{
				NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if (info != null)
				{
					State netState = info.getState();

					if (netState == NetworkInfo.State.DISCONNECTING || netState == NetworkInfo.State.DISCONNECTED)
					{
						mediaServerStop();

					} else if (netState == NetworkInfo.State.CONNECTED)
					{
						mediaServerRestart();
					}

				}

			}

		}

	}

}
