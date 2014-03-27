/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.container.transfer/ITransferProgressListener.java
 * 
 *			Modified: 27-Mar-2014 (16:13:36)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.container.transfer;


/**
 * The listener interface for receiving ITransferProgress events.<br />
 * The class that is interested in processing a ITransferProgress event implements this interface.<br />
 * To listen to transfer events, the listener must register in the file object itself.
 * 
 * @see TransferEvent
 */
public interface ITransferProgressListener
{
	
	/**
	 * Progress has changed.
	 * 
	 * @param event
	 *            the event object containing info about the change.
	 */
	public void transferProgressChanged(TransferEvent event);
}
