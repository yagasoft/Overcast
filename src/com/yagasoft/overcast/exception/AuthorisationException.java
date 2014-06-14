/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.exception/AuthorisationException.java
 * 
 *			Modified: Apr 14, 2014 (2:06:09 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.exception;


/**
 * Fired when there's a problem in the authorisation process.
 */
public class AuthorisationException extends Exception
{
	
	private static final long	serialVersionUID	= -4400694569620144264L;
	
	/**
	 * Instantiates a new authorisation exception.
	 */
	public AuthorisationException()
	{
		super();
	}
	
	/**
	 * Instantiates a new authorisation exception.
	 * 
	 * @param message
	 *            the message
	 */
	public AuthorisationException(String message)
	{
		super(message);
	}
	
	/**
	 * Instantiates a new authorisation exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public AuthorisationException(Throwable cause)
	{
		super(cause);
	}
	
	/**
	 * Instantiates a new authorisation exception.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public AuthorisationException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	/**
	 * Instantiates a new authorisation exception.
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
	public AuthorisationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
