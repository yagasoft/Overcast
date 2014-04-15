/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.exception/OperationException.java
 * 
 *			Modified: Apr 14, 2014 (2:08:33 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.exception;


/**
 * Fired when there's a problem in a basic operation of a file or folder.
 */
public class OperationException extends Exception
{
	
	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 5629459836821067729L;
	
	/**
	 * Instantiates a new operation exception.
	 */
	public OperationException()
	{
		super();
	}
	
	/**
	 * Instantiates a new operation exception.
	 * 
	 * @param message
	 *            the message
	 */
	public OperationException(String message)
	{
		super(message);
	}
	
	/**
	 * Instantiates a new operation exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public OperationException(Throwable cause)
	{
		super(cause);
	}
	
	/**
	 * Instantiates a new operation exception.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public OperationException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	/**
	 * Instantiates a new operation exception.
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
	public OperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
