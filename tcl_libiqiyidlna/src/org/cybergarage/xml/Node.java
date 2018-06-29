/******************************************************************
 *
 *	CyberXML for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: Element.java
 *
 *	Revision;
 *
 *	11/27/02
 *		- first revision.
 *	11/01/03
 *		- Terje Bakken
 *		- fixed missing escaping of reserved XML characters
 *	11/19/04
 *		- Theo Beisch <theo.beisch@gmx.de>
 *		- Added "&" and "\"" "\\" to toXMLString().
 *	11/19/04
 *		- Theo Beisch <theo.beisch@gmx.de>
 *		- Changed XML::output() to use short notation when the tag value is null.
 *	12/02/04
 *		- Brian Owens <brian@b-owens.com>
 *		- Fixed toXMLString() to convert from "'" to "&apos;" instead of "\".
 *	11/07/05
 *		- Changed toString() to return as utf-8 string.
 *	02/08/08
 *		- Added addValue().
 *
 ******************************************************************/

package org.cybergarage.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

public class Node
{

	/**
	 * Create a Node with empty UserData and no Parent Node
	 * 
	 */
	public Node()
	{
		setUserData(null);
		setParentNode(null);
	}

	public Node(String name)
	{
		this();
		setName(name);
	}

	public Node(String ns, String name)
	{
		this();
		setName(ns, name);
	}

	////////////////////////////////////////////////
	//	parent node
	////////////////////////////////////////////////

	private Node parentNode = null;

	public void setParentNode(Node node)
	{
		parentNode = node;
	}

	public Node getParentNode()
	{
		return parentNode;
	}

	////////////////////////////////////////////////
	//	root node
	////////////////////////////////////////////////

	public Node getRootNode()
	{
		Node rootNode = null;
		Node parentNode = getParentNode();
		while (parentNode != null)
		{
			rootNode = parentNode;
			parentNode = rootNode.getParentNode();
		}
		return rootNode;
	}

	////////////////////////////////////////////////
	//	name
	////////////////////////////////////////////////

	private String name = new String();

	public void setName(String name)
	{
		this.name = name;
	}

	public void setName(String ns, String name)
	{
		this.name = ns + ":" + name;
	}

	public String getName()
	{
		return name;
	}

	public boolean isName(String value)
	{
		return name.equals(value);
	}

	////////////////////////////////////////////////
	//	value
	////////////////////////////////////////////////

	private String value = new String();

	public void setValue(String value)
	{
		this.value = value;
	}

	public void setValue(int value)
	{
		setValue(Integer.toString(value));
	}

	public void addValue(String value)
	{
		if (this.value == null)
		{
			this.value = value;
			return;
		}
		if (value != null)
			this.value += value;
	}

	public String getValue()
	{
		return value;
	}

	////////////////////////////////////////////////
	//	Attribute (Basic)
	////////////////////////////////////////////////

	private AttributeList attrList = new AttributeList();

	public int getNAttributes()
	{
		return attrList.size();
	}

	public Attribute getAttribute(int index)
	{
		return attrList.getAttribute(index);
	}

	public Attribute getAttribute(String name)
	{
		return attrList.getAttribute(name);
	}

	public void addAttribute(Attribute attr)
	{
		attrList.add(attr);
	}

	public void insertAttributeAt(Attribute attr, int index)
	{
		attrList.insertElementAt(attr, index);
	}

	public void addAttribute(String name, String value)
	{
		Attribute attr = new Attribute(name, value);
		addAttribute(attr);
	}

	public boolean removeAttribute(Attribute attr)
	{
		return attrList.remove(attr);
	}

	public boolean removeAttribute(String name)
	{
		return removeAttribute(getAttribute(name));
	}

	public boolean hasAttributes()
	{
		if (0 < getNAttributes())
			return true;
		return false;
	}

	////////////////////////////////////////////////
	//	Attribute (Extention)
	////////////////////////////////////////////////

	public void setAttribute(String name, String value)
	{
		Attribute attr = getAttribute(name);
		if (attr != null)
		{
			attr.setValue(value);
			return;
		}
		attr = new Attribute(name, value);
		addAttribute(attr);
	}

	public void setAttribute(String name, int value)
	{
		setAttribute(name, Integer.toString(value));
	}

	public String getAttributeValue(String name)
	{
		Attribute attr = getAttribute(name);
		if (attr != null)
			return attr.getValue();
		return "";
	}

	public int getAttributeIntegerValue(String name)
	{
		String val = getAttributeValue(name);
		try
		{
			return Integer.parseInt(val);
		} catch (Exception e)
		{
		}
		return 0;
	}

	////////////////////////////////////////////////
	//	Attribute (xmlns)
	////////////////////////////////////////////////

	public void setNameSpace(String ns, String value)
	{
		setAttribute("xmlns:" + ns, value);
	}

	public void setNameSpace(String value)
	{
		setAttribute("xmlns", value);
	}

	////////////////////////////////////////////////
	//	Child node
	////////////////////////////////////////////////

	private NodeList nodeList = new NodeList();

	public int getNNodes()
	{
		return nodeList.size();
	}

	public Node getNode(int index)
	{
		return nodeList.getNode(index);
	}

	public Node getNode(String name)
	{
		return nodeList.getNode(name);
	}

	public Node getNodeEndsWith(String name)
	{
		return nodeList.getEndsWith(name);
	}

	public void addNode(Node node)
	{
		node.setParentNode(this);
		//modify
		if (nodeList.contains(node) == false)
		{
			nodeList.add(node);
		}
	}

	public void insertNode(Node node, int index)
	{
		node.setParentNode(this);
		nodeList.insertElementAt(node, index);
	}

	public int getIndex(String name)
	{
		int index = -1;
		for (Iterator<Node> i = nodeList.iterator(); i.hasNext();)
		{
			index++;
			Node n = (Node) i.next();
			if (n.getName().equals(name))
				return index;
		}
		return index;
	}

	public boolean removeNode(Node node)
	{
		node.setParentNode(null);
		return nodeList.remove(node);
	}

	public boolean removeNode(String name)
	{
		return nodeList.remove(getNode(name));
	}

	public void removeAllNodes()
	{
		nodeList.clear();
	}

	public boolean hasNodes()
	{
		if (0 < getNNodes())
			return true;
		return false;
	}

	////////////////////////////////////////////////
	//	Element (Child Node)
	////////////////////////////////////////////////

	public void setNode(String name, String value)
	{
		Node node = getNode(name);
		if (node != null)
		{
			node.setValue(value);
			return;
		}
		node = new Node(name);
		node.setValue(value);
		addNode(node);
	}

	public void setNode(String name, String value, String nameSpace, String nameSpaceValue)
	{
		Node node = getNode(name);
		if (node != null)
		{
			node.setValue(value);
			return;
		}
		node = new Node(name);
		node.setNameSpace(nameSpace, nameSpaceValue);
		node.setValue(value);
		addNode(node);
	}

	public String getNodeValue(String name)
	{
		Node node = getNode(name);
		if (node != null)
			return node.getValue();
		return "";
	}

	////////////////////////////////////////////////
	//	userData
	////////////////////////////////////////////////

	private Object userData = null;

	public void setUserData(Object data)
	{
		userData = data;
	}

	public Object getUserData()
	{
		return userData;
	}

	////////////////////////////////////////////////
	//	toString 
	////////////////////////////////////////////////

	/**
	 * Inovoke {@link #getIndentLevelString(int, String)} with <code>"   "</code> as String
	 * 
	 * @see #getIndentLevelString(int, String)
	 */
	public String getIndentLevelString(int nIndentLevel)
	{
		return getIndentLevelString(nIndentLevel, "   ");
	}

	/**
	 * 
	 * @param nIndentLevel
	 *            the level of indentation to produce
	 * @param space
	 *            the String to use for the intendation
	 * @since 1.8.0
	 * @return an indentation String
	 */
	public String getIndentLevelString(int nIndentLevel, String space)
	{
		StringBuffer indentString = new StringBuffer(nIndentLevel * space.length());
		for (int n = 0; n < nIndentLevel; n++)
		{
			indentString.append(space);
		}
		return indentString.toString();
	}

	public void outputAttributes(PrintWriter ps)
	{
		int nAttributes = getNAttributes();
		for (int n = 0; n < nAttributes; n++)
		{
			Attribute attr = getAttribute(n);
			ps.print(" " + attr.getName() + "=\"" + XML.escapeXMLChars(attr.getValue()) + "\"");
		}
	}

	/**
	 * 为了提高效率，替换原来的outputAttributes
	 * 
	 * @param out
	 */
	public void outputAttributes(OutputStream out)
	{

		int nAttributes = getNAttributes();

		for (int n = 0; n < nAttributes; n++)
		{
			Attribute attr = getAttribute(n);
			try
			{
				out.write((" " + attr.getName() + "=\"" + XML.escapeXMLChars(attr.getValue()) + "\"").getBytes());
			} catch (IOException e)
			{
				e.printStackTrace();
			}

		}
	}

	public void output(PrintWriter ps, int indentLevel, boolean hasChildNode)
	{
		String indentString = getIndentLevelString(indentLevel);

		String name = getName();
		String value = getValue();

		if (hasNodes() == false || hasChildNode == false)
		{
			ps.print(indentString + "<" + name);
			outputAttributes(ps);
			// Thnaks for Tho Beisch (11/09/04)
			if (value == null || value.length() == 0)
			{
				// Not using the short notation <node /> because it cause compatibility trouble
				ps.println("></" + name + ">");
			} else
			{
				ps.println(">" + XML.escapeXMLChars(value) + "</" + name + ">");
			}

			return;
		}

		ps.print(indentString + "<" + name);
		outputAttributes(ps);
		ps.println(">");

		int nChildNodes = getNNodes();
		for (int n = 0; n < nChildNodes; n++)
		{
			Node cnode = getNode(n);
			cnode.output(ps, indentLevel + 1, true);
		}

		ps.println(indentString + "</" + name + ">");
	}

	/**
	 * 为了提高效率，替换原来的output(), <br>
	 * 原来耗时2~3ms <br>
	 * 现在耗时1ms
	 * 
	 * @param out
	 * @param indentLevel
	 * @param hasChildNode
	 */
	public void output(OutputStream out, int indentLevel, boolean hasChildNode)
	{
		// String indentString = getIndentLevelString(indentLevel);
		String indentString = "";
		String name = getName();
		String value = getValue();

		try
		{
			if (hasNodes() == false || hasChildNode == false)
			{
				out.write((indentString + "<" + name).getBytes());
				outputAttributes(out);

				if (value == null || value.length() == 0)
				{
					out.write(("></" + name + ">").getBytes());
				} else
				{
					out.write((">" + XML.escapeXMLChars(value) + "</" + name + ">").getBytes());
				}
			} else {
					out.write((indentString + "<" + name).getBytes());
					outputAttributes(out);
					out.write((">").getBytes());

					int nChildNodes = getNNodes();
					for (int n = 0; n < nChildNodes; n++)
					{
						Node cnode = getNode(n);
						cnode.output(out, indentLevel + 1, true);
					}
					out.write((indentString + "</" + name + ">").getBytes());
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	// private PrintWriter pr = new PrintWriter(byteOut);

	public String toString(String enc, boolean hasChildNode)
	{
		try
		{
			byteOut.reset();
			output(byteOut, 0, hasChildNode);
			byteOut.flush();
			if (enc != null && 0 < enc.length())
				return byteOut.toString(enc);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return byteOut.toString();
	}

	public String toString()
	{
		return toString(XML.CHARSET_UTF8, true);
	}

	public String toXMLString(boolean hasChildNode)
	{
		String xmlStr = toString();
		xmlStr = xmlStr.replaceAll("<", "&lt;");
		xmlStr = xmlStr.replaceAll(">", "&gt;");
		// Thanks for Theo Beisch (11/09/04)
		xmlStr = xmlStr.replaceAll("&", "&amp;");
		xmlStr = xmlStr.replaceAll("\"", "&quot;");
		// Thanks for Brian Owens (12/02/04)
		xmlStr = xmlStr.replaceAll("'", "&apos;");
		return xmlStr;
	}

	public String toXMLString()
	{
		return toXMLString(true);
	}

	public void print(boolean hasChildNode)
	{
		PrintWriter pr = new PrintWriter(System.out);
		output(pr, 0, hasChildNode);
		pr.flush();
	}

	public void print()
	{
		print(true);
	}
}
