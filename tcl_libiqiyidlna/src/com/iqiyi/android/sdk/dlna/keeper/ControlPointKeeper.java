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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.cybergarage.util.Debug;

import android.os.Environment;

public class ControlPointKeeper
{
	//add by tengfei; support android 6.0, need setExternalFilesDir 	
	private static String logFileName = Environment.getExternalStorageDirectory().getPath() + "/iqiyi" + "/dlna/"
			+ "controlpointkeeper.log";
	private static ControlPointKeeper instance = null;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	/*
	 * 保存UUID的值
	 */
	private String controlPointUUID = null;

	public ControlPointKeeper()
	{

	}

	//add by tengfei; support android 6.0, need setExternalFilesDir 
	public void setExternalFilesDir(String ExternalFilesDir)
	{
		if (ExternalFilesDir != null)
		{
			logFileName = ExternalFilesDir + "/dlna/" + "controlpointkeeper.log";
			Debug.message("ControlPointKeeper changelogFile logFileName: " + logFileName);
		} else
		{
			Debug.message("ControlPointKeeper changelogFile path error!!!!!!");
		}
	}

	public static ControlPointKeeper getInstance()
	{
		if (instance == null)
		{
			instance = new ControlPointKeeper();
		}
		return instance;
	}

	public void Save(String uuid)
	{
		controlPointUUID = uuid;
		saveAll();//保存序列化
	}

	public String getUUID()
	{
		readAll();
		return controlPointUUID;
	}

	//读取序列化数据
	private void readAll()
	{
		ObjectInputStream ois = null;
		//Debug.message("ControlPointKeeper readAll logFileName: " + logFileName); 
		lock.readLock().lock();// 读取加锁

		try
		{
			ois = new ObjectInputStream(new FileInputStream(logFileName));
			controlPointUUID = (String) (ois.readObject());

		} catch (StreamCorruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block

		} catch (IOException e)
		{
			// TODO Auto-generated catch block

		} catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block

		} finally
		{
			if (ois != null)
			{
				try
				{
					ois.close();
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			lock.readLock().unlock();// 读锁解除
		}
	}

	// 存储序列化
	private void saveAll()
	{
		if (controlPointUUID == null)
		{
			return;
		}
		//Debug.message("ControlPointKeeper saveAll logFileName: " + logFileName); 
		// 写文件时候，加锁
		lock.writeLock().lock();// 写锁
		//
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ObjectOutputStream oos = null;
		try
		{
			oos = new ObjectOutputStream(new FileOutputStream(logFileName));
			oos.writeObject(controlPointUUID);

		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally
		{
			if (oos != null)
			{
				try
				{
					oos.close();
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			lock.writeLock().unlock();// 解锁
		}
	}
}
