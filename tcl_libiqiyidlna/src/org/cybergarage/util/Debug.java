/******************************************************************
 *
 *	CyberUtil for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: Debug.java
 *
 *	Revision;
 *
 *	11/18/02
 *		- first revision.
 *
 ******************************************************************/

package org.cybergarage.util;

import java.io.PrintStream;

import android.util.Log;
import android.os.Build;

public final class Debug
{
	public static Debug debug = new Debug();

	private PrintStream out = System.out;

	private static final String TAG = "DLNA";

	private static boolean DEBUG()
	{
		if (Build.TYPE.equals("eng")/* || Build.TYPE.equals("userdebug") */)
			return true;
		return Log.isLoggable(TAG, Log.DEBUG);
	}

	public Debug()
	{

	}

	public synchronized PrintStream getOut()
	{
		return out;
	}

	public synchronized void setOut(PrintStream out)
	{
		this.out = out;
	}

	public static boolean enabled = false;

	public static Debug getDebug()
	{
		return Debug.debug;
	}

	public static final void on()
	{
		enabled = true;
	}

	public static final void off()
	{
		enabled = false;
	}

	public static boolean isOn()
	{
		return enabled || DEBUG();
	}

	private static final String LOG_TAG = "TVGuoDLNA: ";

	public static final void message(String s)
	{
		if (enabled == true || DEBUG())
		{
			Debug.debug.getOut().println(LOG_TAG + s);
		}
	}

	public static final void message(String tag, String s)
	{
		if (enabled == true || DEBUG())
		{
			Log.i(LOG_TAG, tag + ": " + s);
		}
	}

	public static final void warning(String s)
	{
		if (enabled == true || DEBUG())
			Debug.debug.getOut().println(LOG_TAG + "warning: " + s);
	}

	public static final void warning(String m, Exception e)
	{
		if (e.getMessage() == null)
		{
			Debug.debug.getOut().println(LOG_TAG + "warning : " + m + " START");
			e.printStackTrace(Debug.debug.getOut());
			Debug.debug.getOut().println(LOG_TAG + "warning : " + m + " END");
		} else
		{
			Debug.debug.getOut().println(LOG_TAG + "warning : " + m + " (" + e.getMessage() + ")");
			e.printStackTrace(Debug.debug.getOut());
		}
	}

	public static final void warning(Exception e)
	{
		warning(e.getMessage());
		e.printStackTrace(Debug.debug.getOut());
	}
}
