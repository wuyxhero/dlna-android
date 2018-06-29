package org.cybergarage.upnp.std.av.server.object.item.file;

import java.io.File;
import java.util.Vector;

public class FileNodeList extends Vector<FileNode>
{
	private static final long serialVersionUID = 1L;

	public FileNodeList()
	{
	}

	public FileNode getFileNode(int n)
	{
		return (FileNode) get(n);
	}

	public FileNode getFileNode(File file)
	{
		int cnt = size();
		for (int n = 0; n < cnt; n++)
		{
			FileNode node = getFileNode(n);
			File nodeFile = node.getFile();
			if (nodeFile == null)
				continue;
			if (node.equals(file) == true)
				return node;
		}
		return null;
	}
}
