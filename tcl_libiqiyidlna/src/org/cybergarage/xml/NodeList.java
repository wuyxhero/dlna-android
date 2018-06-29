/******************************************************************
 *
 *	CyberXML for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: NodeList.java
 *
 *	Revision;
 *
 *	11/27/02
 *		- first revision.
 *
 ******************************************************************/

package org.cybergarage.xml;

import java.util.Vector;

public class NodeList extends Vector<Node>
{
	private static final long serialVersionUID = 1L;

	public NodeList()
	{
	}

	public Node getNode(int n)
	{
		if (n >= this.size())//比实际多，肯定不行
		{
			return null;
		}
		return (Node) get(n);
	}

	public Node getNode(String name)
	{
		if (name == null)
			return null;

		int nLists = size();
		for (int n = 0; n < nLists; n++)
		{
			Node node = getNode(n);
			if (node != null)
			{
				String nodeName = node.getName();
				if (name.compareTo(nodeName) == 0)
					return node;
			}
		}
		return null;
	}

	public Node getEndsWith(String name)
	{
		if (name == null)
			return null;

		int nLists = size();
		for (int n = 0; n < nLists; n++)
		{
			Node node = getNode(n);
			if (node != null)
			{
				String nodeName = node.getName();
				if (nodeName == null)
					continue;
				if (nodeName.endsWith(name) == true)
					return node;
			}
		}
		return null;
	}
}
