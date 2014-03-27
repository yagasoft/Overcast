/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.exception/AccessException.java
 * 
 *			Modified: 27-Mar-2014 (16:14:05)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.exception;


/**
 * Fired when there's a problem accessing a file.
 */
public class AccessException extends Exception
{
	
	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 7410482488933705949L;
	
	/**
	 * Instantiates a new access exception.
	 */
	public AccessException()
	{
		super();
	}
	
	/**
	 * Instantiates a new access exception.
	 * 
	 * @param message
	 *            the message
	 */
	public AccessException(String message)
	{
		super(message);
	}
	
	/**
	 * Instantiates a new access exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public AccessException(Throwable cause)
	{
		super(cause);
	}
	
	/**
	 * Instantiates a new access exception.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public AccessException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	/**
	 * Instantiates a new access exception.
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
	public AccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
