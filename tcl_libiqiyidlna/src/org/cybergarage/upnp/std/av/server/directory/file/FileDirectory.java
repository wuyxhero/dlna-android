/******************************************************************
 *
 *	MediaServer for CyberLink
 *
 *	Copyright (C) Satoshi Konno 2004
 *
 *	File : FileDirectory
 *
 *	Revision:
 *
 *	02/10/04
 *		- first revision.
 *
 ******************************************************************/

package org.cybergarage.upnp.std.av.server.directory.file;

import java.io.*;

import org.cybergarage.util.*;
import org.cybergarage.xml.*;
import org.cybergarage.upnp.std.av.server.*;
import org.cybergarage.upnp.std.av.server.object.*;
import org.cybergarage.upnp.std.av.server.object.item.file.*;

import android.os.Environment;

public class FileDirectory extends Directory implements FileNode
{
	////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////

	public FileDirectory(String name, String path)
	{
		super(name);
		setPath(path);
	}

	////////////////////////////////////////////////
	// Path
	////////////////////////////////////////////////

	private String path;

	public void setPath(String value)
	{
		path = value;
	}

	public String getPath()
	{
		return path;
	}

	////////////////////////////////////////////////
	// create/updateItemNode
	////////////////////////////////////////////////

	private boolean updateFileNode(FileNode node, File file)
	{
		if (node instanceof FileDirectory)
		{
			File innerFile = node.getFile();
			FileDirectory dir = (FileDirectory) node;
			dir.setTitle(innerFile.getName());

		} else if (node instanceof FileItemNode)
		{
			Format format = getContentDirectory().getFormat(file);
			if (format == null)
				return false;
			FormatObject formatObj = format.createObject(file);
			FileItemNode itemNode = (FileItemNode) node;
			// File/TimeStamp
			itemNode.setFile(file);

			// Title
			String title = formatObj.getTitle();
			if (0 < title.length())
				itemNode.setTitle(title);

			// Creator
			String creator = formatObj.getCreator();
			if (0 < creator.length())
				itemNode.setCreator(creator);

			// Media Class
			String mediaClass = format.getMediaClass();
			if (0 < mediaClass.length())
				itemNode.setUPnPClass(mediaClass);

			// Date
			long lastModTime = file.lastModified();
			itemNode.setDate(lastModTime);

			// Storatge Used
			try
			{
				long fileSize = file.length();
				itemNode.setStorageUsed(fileSize);
			} catch (Exception e)
			{
				Debug.warning(e);
			}

			String serverRoot = this.getContentDirectory().getMediaServer().getServerRootDir();
			String absServerPath = Environment.getExternalStorageDirectory().getAbsolutePath() + serverRoot;
			String filePath = itemNode.getFile().getAbsolutePath();

			filePath = filePath.substring(absServerPath.length(), filePath.length());

			// ProtocolInfo
			String mimeType = format.getMimeType();
			String protocol = ConnectionManager.HTTP_GET + ":*:" + mimeType + ":*";
			String id = itemNode.getID();
			String url = getContentDirectory().getContentExportURL(filePath, id);
			AttributeList objAttrList = formatObj.getAttributeList();
			itemNode.setResource(url, protocol, objAttrList);
		}
		getContentDirectory().updateSystemUpdateID();

		return true;
	}

	/**
	 * @deprecated
	 * @param itemNode
	 * @param file
	 * @return
	 */
	private boolean updateItemNode(FileItemNode itemNode, File file)
	{
		Format format = getContentDirectory().getFormat(file);
		if (format == null)
			return false;
		FormatObject formatObj = format.createObject(file);

		// File/TimeStamp
		itemNode.setFile(file);

		// Title
		String title = formatObj.getTitle();
		if (0 < title.length())
			itemNode.setTitle(title);

		// Creator
		String creator = formatObj.getCreator();
		if (0 < creator.length())
			itemNode.setCreator(creator);

		// Media Class
		String mediaClass = format.getMediaClass();
		if (0 < mediaClass.length())
			itemNode.setUPnPClass(mediaClass);

		// Date
		long lastModTime = file.lastModified();
		itemNode.setDate(lastModTime);

		// Storatge Used
		try
		{
			long fileSize = file.length();
			itemNode.setStorageUsed(fileSize);
		} catch (Exception e)
		{
			Debug.warning(e);
		}

		String serverRoot = this.getContentDirectory().getMediaServer().getServerRootDir();
		String absServerPath = Environment.getExternalStorageDirectory().getAbsolutePath() + serverRoot;
		String filePath = itemNode.getFile().getAbsolutePath();

		filePath = filePath.substring(absServerPath.length() + 1, filePath.length());

		// ProtocolInfo
		String mimeType = format.getMimeType();
		String protocol = ConnectionManager.HTTP_GET + ":*:" + mimeType + ":*";
		String id = itemNode.getID();
		String url = getContentDirectory().getContentExportURL(filePath, id);
		AttributeList objAttrList = formatObj.getAttributeList();
		itemNode.setResource(url, protocol, objAttrList);

		// Update SystemUpdateID
		getContentDirectory().updateSystemUpdateID();

		return true;
	}

	private FileItemNode createCompareItemNode(File file)
	{
		Format format = getContentDirectory().getFormat(file);
		if (format == null)
			return null;
		FileItemNode itemNode = new FileItemNode();
		itemNode.setFile(file);
		return itemNode;
	}

	////////////////////////////////////////////////
	// FileList
	////////////////////////////////////////////////

	private int getDirectoryFileNodeList(File dirFile, FileNodeList fileNodeList)
	{
		File childFile[] = dirFile.listFiles();
		int fileCnt = childFile.length;
		for (int n = 0; n < fileCnt; ++n)
		{
			File file = childFile[n];
			if (file.isDirectory())
			{
				FileDirectory directory = new FileDirectory(file.getName(), file.getAbsolutePath());
				directory.setChildCount(file.listFiles().length);
				directory.setFile(file);
				directory.setContentDirectory(this.getContentDirectory());
				fileNodeList.add(directory);
			} else if (file.isFile())
			{
				FileNode itemNode = createCompareItemNode(file);
				if (itemNode == null)
				{
					continue;
				}
				fileNodeList.add(itemNode);
			}

		}
		return fileNodeList.size();
	}

	private int getDirectoryItemNodeList(File dirFile, FileItemNodeList itemNodeList)
	{
		File childFile[] = dirFile.listFiles();
		int fileCnt = childFile.length;
		for (int n = 0; n < fileCnt; n++)
		{
			File file = childFile[n];
			if (file.isDirectory() == true)
			{
				getDirectoryItemNodeList(file, itemNodeList);
				continue;
			}
			if (file.isFile() == true)
			{
				FileItemNode itemNode = createCompareItemNode(file);
				if (itemNode == null)
					continue;
				itemNodeList.add(itemNode);
			}
		}
		return itemNodeList.size();
	}

	private FileNodeList getCurrentDirectoryFileNodeList()
	{
		FileNodeList nodeList = new FileNodeList();
		String path = getPath();
		File pathFile = new File(path);
		getDirectoryFileNodeList(pathFile, nodeList);
		return nodeList;

	}

	/**
	 * @deprecated
	 * @return
	 */
	private FileItemNodeList getCurrentDirectoryItemNodeList()
	{
		FileItemNodeList itemNodeList = new FileItemNodeList();
		String path = getPath();
		File pathFile = new File(path);
		getDirectoryItemNodeList(pathFile, itemNodeList);
		return itemNodeList;
	}

	////////////////////////////////////////////////
	// updateItemNodeList
	////////////////////////////////////////////////

	private FileNode getFileNode(File file)
	{
		int nContents = getNContentNodes();
		for (int i = 0; i < nContents; ++i)
		{
			ContentNode cnode = getContentNode(i);
			FileNode fileNode = (FileNode) cnode;
			if (fileNode.equals(file))
			{
				return fileNode;
			}
		}
		return null;
	}

	/**
	 * @deprecated
	 * @param file
	 * @return
	 */
	private FileItemNode getItemNode(File file)
	{
		int nContents = getNContentNodes();
		for (int n = 0; n < nContents; n++)
		{
			ContentNode cnode = getContentNode(n);
			if ((cnode instanceof FileItemNode) == false)
				continue;
			FileItemNode itemNode = (FileItemNode) cnode;
			if (itemNode.equals(file) == true)
				return itemNode;
		}
		return null;
	}

	private void addItemNode(FileItemNode itemNode)
	{
		addContentNode(itemNode);
	}

	private void addFileNode(FileNode node)
	{
		addContentNode((ContentNode) node);
	}

	private boolean updateFileNodeList(FileNode newFileNode)
	{
		File newNodeFile = newFileNode.getFile();
		FileNode currentNode = getFileNode(newNodeFile);
		if (currentNode == null)
		{
			int newItemID = -1;
			if (newFileNode instanceof FileDirectory)
			{
				newItemID = getContentDirectory().getNextContainerID();
			} else if (newFileNode instanceof FileItemNode)
			{
				newItemID = getContentDirectory().getNextItemID();
			}
			((ContentNode) newFileNode).setID(newItemID);
			updateFileNode(newFileNode, newNodeFile);
			addFileNode(newFileNode);
			return true;

		}

		long currTimeStamp = newFileNode.getFileTimeStamp();
		long newTimeStamp = newFileNode.getFileTimeStamp();
		if (currTimeStamp == newTimeStamp)
			return false;

		updateFileNode(currentNode, newNodeFile);

		return true;
	}

	/**
	 * @deprecated
	 * @param newItemNode
	 * @return
	 */
	private boolean updateItemNodeList(FileItemNode newItemNode)
	{
		File newItemNodeFile = newItemNode.getFile();
		FileItemNode currItemNode = getItemNode(newItemNodeFile);
		if (currItemNode == null)
		{
			int newItemID = getContentDirectory().getNextItemID();
			newItemNode.setID(newItemID);
			updateItemNode(newItemNode, newItemNodeFile);
			addItemNode(newItemNode);
			return true;
		}

		long currTimeStamp = currItemNode.getFileTimeStamp();
		long newTimeStamp = newItemNode.getFileTimeStamp();
		if (currTimeStamp == newTimeStamp)
			return false;

		updateItemNode(currItemNode, newItemNodeFile);

		return true;
	}

	/**
	 * @deprecated
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean updateItemNodeList()
	{
		boolean updateFlag = false;

		// Checking Deleted Items
		int nContents = getNContentNodes();
		ContentNode cnode[] = new ContentNode[nContents];
		for (int n = 0; n < nContents; n++)
			cnode[n] = getContentNode(n);
		for (int n = 0; n < nContents; n++)
		{
			if ((cnode[n] instanceof FileItemNode) == false)
				continue;
			FileItemNode itemNode = (FileItemNode) cnode[n];
			File itemFile = itemNode.getFile();
			if (itemFile == null)
				continue;
			if (itemFile.exists() == false)
			{
				removeContentNode(cnode[n]);
				updateFlag = true;
			}
		}

		// Checking Added or Updated Items
		FileItemNodeList itemNodeList = getCurrentDirectoryItemNodeList();
		int itemNodeCnt = itemNodeList.size();
		for (int n = 0; n < itemNodeCnt; n++)
		{
			FileItemNode itemNode = itemNodeList.getFileItemNode(n);
			if (updateItemNodeList(itemNode) == true)
				updateFlag = true;
		}

		return updateFlag;
	}

	private boolean updateFileNodeList()
	{
		boolean updateFlag = false;

		// Checking Deleted Items
		int nContents = getNContentNodes();
		ContentNode cnode[] = new ContentNode[nContents];
		for (int n = 0; n < nContents; n++)
			cnode[n] = getContentNode(n);
		for (int n = 0; n < nContents; n++)
		{

			ContentNode subNode = cnode[n];

			if (subNode instanceof FileNode)
			{
				File tfile = ((FileNode) subNode).getFile();
				if (!tfile.exists())
				{
					removeContentNode(cnode[n]);
					updateFlag = true;
				}
			} else
			{
				continue;
			}

			/*if ((cnode[n] instanceof FileItemNode) == false)
				continue;
			FileItemNode itemNode = (FileItemNode)cnode[n];
			File itemFile = itemNode.getFile();
			if (itemFile == null)
				continue;
			if (itemFile.exists() == false) {
				removeContentNode(cnode[n]);
				updateFlag = true;
			}*/
		}

		FileNodeList nodeList = getCurrentDirectoryFileNodeList();
		int count = nodeList.size();
		for (int i = 0; i < count; ++i)
		{
			FileNode fnode = nodeList.getFileNode(i);
			updateFlag = updateFileNodeList(fnode);

			boolean flag = false;
			if (fnode instanceof FileDirectory)
			{
				flag = ((FileDirectory) fnode).update();
			}

			updateFlag |= flag;

			//		    if (fnode instanceof FileDirectory) {
			//		    	updateFlag = ((FileDirectory) fnode).update();
			//		    } else if (fnode instanceof FileItemNode) {
			//		    	if (updateFileNodeList((FileItemNode)fnode) == true) {
			//		    		updateFlag = true;
			//		    	}
			//		    }
		}

		return updateFlag;

		// Checking Added or Updated Items
		/*FileItemNodeList itemNodeList = getCurrentDirectoryItemNodeList();
		int itemNodeCnt = itemNodeList.size();
		for (int n=0; n<itemNodeCnt; n++) {
			FileItemNode itemNode = itemNodeList.getFileItemNode(n);
			if (updateItemNodeList(itemNode) == true)
				updateFlag = true;
		}
		
		return updateFlag;*/
	}

	////////////////////////////////////////////////
	// update
	////////////////////////////////////////////////

	public boolean update()
	{
		return updateFileNodeList();
	}

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
		return null;
	}

	@Override
	public long getContentLength()
	{
		return 0;
	}

	@Override
	public InputStream getContentInputStream()
	{
		return null;
	}
}
