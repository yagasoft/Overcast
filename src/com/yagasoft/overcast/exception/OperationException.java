/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.exception/OperationException.java
 * 
 *			Modified: 27-Mar-2014 (16:14:26)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.exception;


public class OperationException extends Exception
{
	
	private static final long	serialVersionUID	= 5629459836821067729L;
	
	/**
	 *
	 */
	public OperationException()
	{
		super();
	}
	
	/**
	 * @param message
	 */
	public OperationException(String message)
	{
		super(message);
	}
	
	/**
	 * @param cause
	 */
	public OperationException(Throwable cause)
	{
		super(cause);
	}
	
	/**
	 * @param message
	 * @param cause
	 */
	public OperationException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public OperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
