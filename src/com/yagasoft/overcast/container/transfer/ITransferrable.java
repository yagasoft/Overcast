/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 */

package com.yagasoft.overcast.container.transfer;


/**
 * An interface collecting common methods for any container that can be transferred to/from a CSP.
 */
public interface ITransferrable
{

	/**
	 * Adds a progress listener to the transfer of this container (upload/download).
	 *
	 * @param listener Listener object.
	 * @param object Object passed by the initialiser to be passed back on state change. It can be used as a kind of "call-back" or
	 *            something; the sender of this object can cast it back and use it as seen fit.
	 */
	public void addProgressListener(ITransferProgressListener listener, Object object);

	/**
	 * Removes the progress listener.
	 *
	 * @param listener Listener object to be removed.
	 */
	public void removeProgressListener(ITransferProgressListener listener);

	/**
	 * Notify listeners of the state (stated, cancelled, ...etc.) of the transfer, and the progress in the range from 0 to 1.
	 *
	 * @param state State of the transfer.
	 * @param progress Progress between 0 and 1.
	 */
	public void notifyListeners(TransferState state, float progress);

	/**
	 * Clear all listeners to the transfer.
	 */
	public void clearListeners();
}




