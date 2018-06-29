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
package org.cybergarage.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class InternetInfo
{
	/**
	 * network whether is enable
	 * 
	 * @param ctx
	 * @return
	 */
	public static boolean isEnable(Context ctx)
	{
		ConnectivityManager manager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info == null || !info.isConnected())
			return false;

		if (info.getTypeName().toString().equalsIgnoreCase("ethernet")
				|| info.getTypeName().toString().equalsIgnoreCase("wifi"))
		{
			return true;
		}

		return false;
	}

	private static final String DEFAULT_NAME = "unknow";

	public static String getIpIndentity()
	{
		try
		{
			String ipAddress = getIp();
			if (ipAddress.contains("."))
				ipAddress = ipAddress.substring(ipAddress.lastIndexOf(".") + 1);
			int result = Integer.parseInt(ipAddress);
			return String.valueOf(result);
		} catch (Exception ex)
		{
			return DEFAULT_NAME;
		}
	}

	public static String getIp()
	{
		return DEFAULT_NAME;

		//		Context context = ContextInfo.getInstance().getContext();  //先注释掉一下
		//		if (context == null)
		//		{
		//			return DEFAULT_NAME;
		//		}
		//
		//		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		//		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		//		String ipAddress = null;
		//		if (info.getTypeName().toString().equalsIgnoreCase("ethernet"))
		//		{
		//			ipAddress = getWiredAddress(true);
		//		} else if (info.getTypeName().toString().equalsIgnoreCase("wifi"))
		//		{
		//			ipAddress = getWirelessIpAddress(context);
		//		}
		//
		//		return ipAddress;
	}

	public static String getWirelessIpAddress(Context context)
	{
		WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		int ipAddr = wifiInfo.getIpAddress();
		StringBuffer ipBuf = new StringBuffer();
		ipBuf.append(ipAddr & 0xff).append('.').append((ipAddr >>>= 8) & 0xff).append('.').append((ipAddr >>>= 8) & 0xff)
				.append('.').append((ipAddr >>>= 8) & 0xff);

		return ipBuf.toString();
	}

	public static String getWiredAddress(boolean useIPv4)
	{
		try
		{
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces)
			{
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs)
				{
					if (!addr.isLoopbackAddress())
					{
						String sAddr = addr.getHostAddress().toUpperCase(Locale.getDefault());
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (useIPv4)
						{
							if (isIPv4)
								return sAddr;
						} else
						{
							if (!isIPv4)
							{
								int delim = sAddr.indexOf('%'); // drop ip6 port suffix
								return delim < 0 ? sAddr : sAddr.substring(0, delim);
							}
						}
					}
				}
			}
		} catch (Exception ex)
		{
		} // for now eat exceptions
		return "";
	}
}
