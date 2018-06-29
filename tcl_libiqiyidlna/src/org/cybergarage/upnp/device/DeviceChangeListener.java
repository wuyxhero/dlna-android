/******************************************************************
 *
 *	CyberLink for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2004
 *
 *	File: DeviceChangeListener.java
 *
 *	Revision;
 *
 *	09/12/04
 *		- Oliver Newell <newell@media-rush.com>
 *		- Added this class to allow ControlPoint applications to 
 *         be notified when the ControlPoint base class adds/removes
 *         a UPnP device
 *	
 ******************************************************************/

package org.cybergarage.upnp.device;

import org.cybergarage.upnp.Device;

public interface DeviceChangeListener
{
	//设备添加消息
	public void deviceAdded(Device dev);

	//设备离线,这个只有对当前的控制设备有意义
	//public void deviceOffline(Device dev);
	//设备退出消息
	public void deviceRemoved(Device dev);

	//设备更新消息
	public void deviceUpdated(Device dev);
}
