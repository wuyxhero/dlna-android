/******************************************************************
 *
 *	CyberHTTP for Java
 *
 *	Copyright (C) Satoshi Konno 2002
 *
 *	File: HTTPHeader.java
 *
 *	Revision;
 *
 *	11/19/02
 *		- first revision.
 *	05/26/04
 *		- Jan Newmarch <jan.newmarch@infotech.monash.edu.au> (05/26/04)
 *		- Fixed getValue() to compare using String::equals() instead of String::startWidth().
 *	
 ******************************************************************/

package org.cybergarage.http;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.cybergarage.util.Debug;

public class HTTPHeader
{
	private static int MAX_LENGTH = 1024;
	private String name;
	private String value;

	public HTTPHeader(String name, String value)
	{
		setName(name);
		setValue(value);
	}

	public HTTPHeader(String lineStr)
	{
		setName("");
		setValue("");
		if (lineStr == null)
			return;
		int colonIdx = lineStr.indexOf(':');
		if (colonIdx < 0)
			return;

		//		String name = new String(lineStr.getBytes(), 0, colonIdx);				
		//		String value = new String(lineStr.getBytes(), colonIdx+1, lineStr.length()-colonIdx-1);	
		//		setName(name.trim());
		//		setValue(value.trim());

		/**
		 * new String() 是比较耗时的操作，改成subString()
		 */
		String name = lineStr.substring(0, colonIdx);
		String value = lineStr.substring(colonIdx + 1, lineStr.length());
		setName(name.trim());
		setValue(value.trim());
	}

	////////////////////////////////////////////////
	//	Member
	////////////////////////////////////////////////

	public void setName(String name)
	{
		this.name = name;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String getName()
	{
		return name;
	}

	public String getValue()
	{
		return value;
	}

	public boolean hasName()
	{
		if (name == null || name.length() <= 0)
			return false;
		return true;
	}

	/**
	 * 从sting line获取和name相匹配的的value,比较搞笑的做法，替代原来的耗时操作。 原来平均4~5ms，现在基本0~1ms
	 * 
	 * @param line
	 * @param name
	 * @return
	 */
	private static String getValueFromLine(String line, String name)
	{
		if (line == null)
		{
			return "";
		}
		int index = line.indexOf(':');
		if (index < 0)
		{
			return "";
		}
		String key = line.substring(0, index).toUpperCase(Locale.getDefault()).trim();
		String bigName = name.toUpperCase(Locale.getDefault()).trim();
		if (!key.equals(bigName))
		{
			return "";
		}
		return line.substring(index + 1, line.length()).trim();
	}

	////////////////////////////////////////////////
	//	static methods
	////////////////////////////////////////////////

	public final static String getValue(LineNumberReader reader, String name)
	{
		//		String bigName = name.toUpperCase();
		try
		{
			String lineStr = reader.readLine();
			while (lineStr != null && 0 < lineStr.length())
			{

				//so time consuming, replace with getValueFromLine()
				/*HTTPHeader header = new HTTPHeader(lineStr);
				if (header.hasName() == false) {
					 lineStr = reader.readLine();
					continue;
				}
				String bigLineHeaderName = header.getName().toUpperCase();
				// Thanks for Jan Newmarch <jan.newmarch@infotech.monash.edu.au> (05/26/04)
				if (bigLineHeaderName.equals(bigName) == false) {
					 lineStr = reader.readLine();
					 continue;
				}
				Profiling.i("value: " + header.getValue());
				p.end("getValue");
				return header.getValue();*/
				String result = getValueFromLine(lineStr, name);
				if (result.equals(""))
				{
					lineStr = reader.readLine();
					continue;
				}
				return result;

			}
		} catch (IOException e)
		{
			Debug.warning(e);
			return "";
		}
		return "";
	}

	public final static String getValue(String data, String name)
	{
		/* Thanks for Stephan Mehlhase (2010-10-26) */
		StringReader strReader = new StringReader(data);
		LineNumberReader lineReader = new LineNumberReader(strReader, Math.min(data.length(), MAX_LENGTH));
		return getValue(lineReader, name);
	}

	public final static String getValue(byte[] data, String name)
	{
		return getValue(new String(data), name);
	}

	public final static int getIntegerValue(String data, String name)
	{
		try
		{
			return Integer.parseInt(getValue(data, name));
		} catch (Exception e)
		{
			return 0;
		}
	}

	public final static int getIntegerValue(byte[] data, String name)
	{
		try
		{
			return Integer.parseInt(getValue(data, name));
		} catch (Exception e)
		{
			return 0;
		}
	}

	public static final String HEADERCAT = "HEADERCAT";
	public static Map<String, String> getAllValues(byte[] buf) {
		Map<String, String> mHeaderMap = new HashMap<String, String>();
		mHeaderMap.put(HEADERCAT, "");
		String bufStr = new String(buf);
		StringReader strReader = new StringReader(bufStr);
		LineNumberReader lineReader = new LineNumberReader(strReader, Math.min(bufStr.length(), MAX_LENGTH));

		try
		{
			String lineStr = lineReader.readLine();
			while (lineStr != null && 0 < lineStr.length())
			{
				int index = lineStr.indexOf(':');
				if (index > 0)
				{
					String key = lineStr.substring(0, index).toUpperCase(Locale.getDefault()).trim();
					String value =  lineStr.substring(index + 1, lineStr.length()).trim();
					mHeaderMap.put(key, value);

					String tmp = mHeaderMap.get(HEADERCAT);
					mHeaderMap.put(HEADERCAT, tmp + key);
				}
				lineStr = lineReader.readLine();
			}
		} catch (IOException e)
		{
			Debug.warning(e);
		}

		return mHeaderMap;
	}
}
