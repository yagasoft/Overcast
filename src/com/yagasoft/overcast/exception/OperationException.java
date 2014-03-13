
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
