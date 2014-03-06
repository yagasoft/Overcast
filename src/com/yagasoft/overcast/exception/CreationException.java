
package com.yagasoft.overcast.exception;




public class CreationException extends Exception
{

	private static final long	serialVersionUID	= 6741643227201955184L;

	/**
	 * 
	 */
	public CreationException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public CreationException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public CreationException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CreationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public CreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}	
	
	
}




