/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.exception/CreationException.java
 * 
 *			Modified: Apr 14, 2014 (2:06:41 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.exception;


/**
 * Fired when there's a problem creating a file/folder.
 */
public class CreationException extends Exception
{
	
	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 6741643227201955184L;
	
	/**
	 * Instantiates a new creation exception.
	 */
	public CreationException()
	{
		super();
	}
	
	/**
	 * Instantiates a new creation exception.
	 * 
	 * @param message
	 *            the message
	 */
	public CreationException(String message)
	{
		super(message);
	}
	
	/**
	 * Instantiates a new creation exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public CreationException(Throwable cause)
	{
		super(cause);
	}
	
	/**
	 * Instantiates a new creation exception.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public CreationException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	/**
	 * Instantiates a new creation exception.
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
	public CreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
