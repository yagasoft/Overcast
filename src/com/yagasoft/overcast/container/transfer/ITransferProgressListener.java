/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
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
	public void progressChanged(TransferEvent event);
}




