/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.transfer/ITransferrable.java
 * 
 *			Modified: Apr 15, 2014 (9:28:11 AM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.transfer.event;


/**
 * An interface collecting common methods for any container that can be transferred to/from a CSP.
 */
public interface ITransferrable
{
	
	/**
	 * Adds a progress listener to the transfer of this container (upload/download).
	 * 
	 * @param listener
	 *            Listener object.
	 */
	public void addProgressListener(ITransferProgressListener listener);
	
	/**
	 * Removes the progress listener.
	 * 
	 * @param listener
	 *            Listener object to be removed.
	 */
	public void removeProgressListener(ITransferProgressListener listener);
	
	/**
	 * Notify listeners of the state (stated, cancelled, ...etc.) of the transfer, and the progress in the range from 0 to 1.
	 * 
	 * @param job
	 *            the job.
	 * @param state
	 *            State of the transfer.
	 * @param progress
	 *            Progress between 0 and 1.
	 */
	public void notifyProgressListeners(TransferState state, float progress);
	
	/**
	 * Clear all listeners to the transfer.
	 */
	public void clearTransferListeners();
}
