
package com.yagasoft.overcast.exception;


public class AccessException extends Exception
{
	
	private static final long	serialVersionUID	= 7410482488933705949L;
	
	/**
	 *
	 */
	public AccessException()
	{
		super();
	}
	
	/**
	 * @param message
	 */
	public AccessException(String message)
	{
		super(message);
	}
	
	/**
	 * @param cause
	 */
	public AccessException(Throwable cause)
	{
		super(cause);
	}
	
	/**
	 * @param message
	 * @param cause
	 */
	public AccessException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public AccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
