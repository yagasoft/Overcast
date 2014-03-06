
package com.yagasoft.overcast.exception;


public class TransferException extends Exception
{

	private static final long	serialVersionUID	= 6247277610637029361L;

	/**
	 *
	 */
	public TransferException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public TransferException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public TransferException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TransferException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public TransferException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
