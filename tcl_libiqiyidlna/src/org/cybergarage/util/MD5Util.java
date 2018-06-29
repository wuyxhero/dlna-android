package org.cybergarage.util;

import java.security.MessageDigest;

public class MD5Util
{

	/*
	 * 将byte值进行md5加密计算
	 */
	public final static String getMd5(byte[] bytes, int count)
	{
		char hexDigits[] =
		{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try
		{
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(bytes, 0, count);
			byte[] md = mdInst.digest();

			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++)
			{
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] byteMerger(byte[] arr_1, byte[] arr_2)
	{
		byte[] arr_3 = new byte[arr_1.length + arr_2.length];
		System.arraycopy(arr_1, 0, arr_3, 0, arr_1.length);
		System.arraycopy(arr_2, 0, arr_3, arr_1.length, arr_2.length);
		return arr_3;
	}

	//比较两个MD5的值是否相同
	public final static boolean isSameMd5(String md51, String md52)
	{
		if (md51 == null && md52 == null)
			return true;

		if (md51 == null || md52 == null)
			return false;

		if (md51.toLowerCase().compareTo(md52.toLowerCase()) == 0)//全部转化成小写进行比较
			return true;
		return false;
	}

}
