/******************************************************************
 *
 *	MediaServer for CyberLink
 *
 *	Copyright (C) Satoshi Konno 2003
 *
 *	File : FileItemNode.java
 *
 *	Revision:
 *
 *	02/12/04
 *		- first revision.
 *
 ******************************************************************/

package org.cybergarage.upnp.std.av.server.object.item.file;

import java.io.*;

import org.cybergarage.util.*;
import org.cybergarage.upnp.std.av.server.*;
import org.cybergarage.upnp.std.av.server.object.*;
import org.cybergarage.upnp.std.av.server.object.item.*;

public class FileItemNode extends ItemNode implements FileNode
{
	////////////////////////////////////////////////
	// Constroctor
	////////////////////////////////////////////////

	public FileItemNode()
	{
		setFile(null);
	}

	////////////////////////////////////////////////
	// File/TimeStamp
	////////////////////////////////////////////////

	private File itemFile;

	@Override
	public void setFile(File file)
	{
		itemFile = file;
	}

	@Override
	public File getFile()
	{
		return itemFile;
	}

	@Override
	public long getFileTimeStamp()
	{
		long itemFileTimeStamp = 0;
		if (itemFile != null)
		{
			try
			{
				itemFileTimeStamp = itemFile.lastModified();
			} catch (Exception e)
			{
				Debug.warning(e);
			}
		}
		return itemFileTimeStamp;
	}

	@Override
	public boolean equals(File file)
	{
		if (itemFile == null)
			return false;
		return itemFile.equals(file);
	}

	////////////////////////////////////////////////
	// Abstract methods
	////////////////////////////////////////////////

	@Override
	public byte[] getContent()
	{
		byte fileByte[] = new byte[0];
		try
		{
			fileByte = FileUtil.load(itemFile);
		} catch (Exception e)
		{
		}
		return fileByte;
	}

	@Override
	public long getContentLength()
	{
		return itemFile.length();
	}

	@Override
	public InputStream getContentInputStream()
	{
		try
		{
			return new FileInputStream(itemFile);
		} catch (Exception e)
		{
			Debug.warning(e);
		}
		return null;
	}

	public String getMimeType()
	{
		ContentDirectory cdir = getContentDirectory();
		File itemFile = getFile();
		Format itemFormat = cdir.getFormat(itemFile);
		if (itemFormat == null)
		{
			return "*/*";
		}
		return itemFormat.getMimeType();
	}
}
