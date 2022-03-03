package com.hikdsj.hikdsj;

import android.util.Log;

public class HikLog
{
	final static String TAG = "zhbDebug";
	private HikLog()
	{
		/* cannot be instantiated */
		throw new UnsupportedOperationException("cannot be instantiated");
	}
	
	public static boolean isDebug = true;
	public static boolean isError = true;
	public static void d(String msg)
	{
		if (isDebug)
		{
			Log.d(TAG, msg);
		}
	}
	public static void e(String msg)
	{
		if (isError)
		{
			Log.e(TAG, msg);
		}
	}
}
