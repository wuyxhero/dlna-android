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
package com.iqiyi.android.sdk.dlna.keeper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.cybergarage.util.Debug;

import android.os.Environment;

public class DmcInforKeeper
{
	//add by tengfei; support android 6.0, need setExternalFilesDir 
	//private final static String logFileName=Environment.getExternalStorageDirectory().getPath()+"/iqiyi"+"/dlna/"+"dmckeeper.log";
	private static String logFileName = Environment.getExternalStorageDirectory().getPath() + "/iqiyi" + "/dlna/"
			+ "dmckeeper.log";
	private static DmcInforKeeper instance = null;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private Map<String, DmrInfor> dmcInforListMap = new HashMap<String, DmrInfor>();

	public DmcInforKeeper()
	{

	}

	//add by tengfei; support android 6.0, need setExternalFilesDir 	 
	public void setExternalFilesDir(String ExternalFilesDir)
	{
		if (ExternalFilesDir != null)
		{
			logFileName = ExternalFilesDir + "/dlna/" + "dmckeeper.log";
			Debug.message("DmcInforKeeper changelogFile logFileName:" + logFileName);
		} else
		{
			Debug.message("DmcInforKeeper changelogFile path error!!!!!!");
		}
	}

	public static DmcInforKeeper getInstance()
	{
		if (instance == null)
		{
			instance = new DmcInforKeeper();
		}
		return instance;
	}

	public void SaveDmrInfor(DmrInfor infor)
	{
		//保存进来？先判断是是否存在，如果存在的话，则直接进行更新，然后再保存
		if (dmcInforListMap == null)
		{
			dmcInforListMap = new HashMap<String, DmrInfor>();
		}
		dmcInforListMap.put(infor.getUuid(), infor);//由于是hashmap会进行自动替换的
		saveAll();//保存序列化

	}

	public DmrInfor getDmrInfor(String udn)
	{
		readAll();
		if (dmcInforListMap == null)
			return null;

		if (dmcInforListMap.containsKey(udn) == true)
		{
			return dmcInforListMap.get(udn);
		}
		return null;
	}

	//读取序列化数据
	@SuppressWarnings("unchecked")
	private void readAll()
	{
		ObjectInputStream ois = null;

		lock.readLock().lock();// 读取加锁

		try
		{
			//Debug.message("DmcInforKeeper readAll logFileName: " + logFileName); 
			ois = new ObjectInputStream(new FileInputStream(logFileName));
			dmcInforListMap = (Map<String, DmrInfor>) (ois.readObject());
		} catch (Exception e)
		{
			e.printStackTrace();
			dmcInforListMap = null;
		} finally
		{
			if (ois != null)
			{
				try
				{
					ois.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			lock.readLock().unlock();// 读锁解除
		}
	}

	// 存储序列化
	private void saveAll()
	{
		if (dmcInforListMap == null)
		{
			dmcInforListMap = new HashMap<String, DmrInfor>();
			return;
		}
		//Debug.message("DmcInforKeeper saveAll logFileName: " + logFileName);
		// 写文件时候，加锁
		lock.writeLock().lock();// 写锁

		File file = new File(logFileName);
		if (file.exists() == false)// 不存在，则进行创建目录
		{
			//先创建目录
			File parent = file.getParentFile();
			if (parent != null && parent.exists() == false)
			{
				parent.mkdirs();
			}
			try
			{
				file.createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		ObjectOutputStream oos = null;
		try
		{
			oos = new ObjectOutputStream(new FileOutputStream(logFileName));
			oos.writeObject(dmcInforListMap);

		} catch (Exception e)
		{
			e.printStackTrace();
			dmcInforListMap = null;
		} finally
		{
			if (oos != null)
			{
				try
				{
					oos.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			lock.writeLock().unlock();// 解锁
		}
	}
}
