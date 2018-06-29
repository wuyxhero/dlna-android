package org.cybergarage.util;

import android.util.Log;

public class Profiling
{

	private static boolean DEBUG = true;

	private static String TAG = "qiyi_profiling";

	private long mTime = 0;

	public static int i(String msg)
	{
		return Log.i(TAG, msg);
	}

	public void start()
	{
		if (!DEBUG)
		{
			return;
		}
		mTime = System.currentTimeMillis();
	}

	public void end(String msg)
	{
		if (!DEBUG)
		{
			return;
		}
		long time = System.currentTimeMillis() - mTime;
		i(msg + " time: " + time);
	}

}
