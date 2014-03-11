/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 */

package com.yagasoft.overcast.exception;


/**
 * Fired when there's a problem transferring a file to/from a CSP.
 */
public class TransferException extends Exception
{

	/** The Constant serialVersionUID. */
	private static final long	serialVersionUID	= 6247277610637029361L;

	/**
	 * Instantiates a new transfer exception.
	 */
	public TransferException()
	{
		super();
	}

	/**
	 * Instantiates a new transfer exception.
	 * 
	 * @param message
	 *            the message
	 */
	public TransferException(String message)
	{
		super(message);
	}

	/**
	 * Instantiates a new transfer exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public TransferException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * Instantiates a new transfer exception.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public TransferException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Instantiates a new transfer exception.
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
	public TransferException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
