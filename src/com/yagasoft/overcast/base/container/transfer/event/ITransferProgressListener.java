/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.transfer/ITransferProgressListener.java
 *
 *			Modified: 26-May-2014 (21:30:53)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.transfer.event;


/**
 * The listener interface for receiving ITransferProgress events.<br />
 * The class that is interested in processing a ITransferProgress event implements this interface.<br />
 * To listen to transfer events, the listener must register in the file object itself.
 *
 * @see TransferEvent
 */
@FunctionalInterface
public interface ITransferProgressListener
{

	/**
	 * Progress has changed. Can also notify of success or failure.
	 *
	 * @param event
	 *            the event object containing info about the change.
	 */
	public void transferProgressChanged(TransferEvent event);
}
