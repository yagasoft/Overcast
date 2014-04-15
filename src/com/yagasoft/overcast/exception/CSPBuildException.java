/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.exception/CSPBuildException.java
 * 
 *			Modified: Apr 14, 2014 (2:06:58 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.exception;


/**
 * Fired when there's a problem initialising a CSP object.
 */
public class CSPBuildException extends Exception
{
	
	private static final long	serialVersionUID	= 8259353575922782057L;
	
	/**
	 * Instantiates a new CSP build exception.
	 */
	public CSPBuildException()
	{
		super();
	}
	
	/**
	 * Instantiates a new CSP build exception.
	 * 
	 * @param message
	 *            the message
	 */
	public CSPBuildException(String message)
	{
		super(message);
	}
	
	/**
	 * Instantiates a new CSP build exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public CSPBuildException(Throwable cause)
	{
		super(cause);
	}
	
	/**
	 * Instantiates a new CSP build exception.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public CSPBuildException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	/**
	 * Instantiates a new CSP build exception.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 * @param enableSuppression
	 *            the enable suppression
	 * @param writableStackTrace
	 *            the writable stack trace
	 */
	public CSPBuildException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
