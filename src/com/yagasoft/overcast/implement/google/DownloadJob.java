/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.implement.google/DownloadJob.java
 * 
 *			Modified: Apr 15, 2014 (1:53:38 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.google;


import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.yagasoft.overcast.base.container.local.LocalFolder;
import com.yagasoft.overcast.base.container.transfer.event.TransferState;


/**
 * @see com.yagasoft.overcast.base.container.transfer.DownloadJob
 */
public class DownloadJob extends com.yagasoft.overcast.base.container.transfer.DownloadJob<MediaHttpDownloader>
{
	
	/** The canceller of this transfer. */
	Thread	canceller;
	
	/**
	 * Instantiates a new download job.
	 * 
	 * @param remoteFile
	 *            Remote file.
	 * @param parent
	 *            Parent.
	 * @param overwrite
	 *            Overwrite.
	 * @param cspTransferer
	 *            Csp transferer.
	 * @param canceller
	 *            Canceller.
	 */
	public DownloadJob(RemoteFile remoteFile, LocalFolder parent, boolean overwrite
			, MediaHttpDownloader cspTransferer, Thread canceller)
	{
		super(remoteFile, parent, overwrite, cspTransferer);
		this.canceller = canceller;
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.transfer.TransferJob#cancelTransfer()
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void cancelTransfer()
	{
		// Google doesn't have a cancel method, so I had to improvise.
		canceller.stop();
		notifyProgressListeners(TransferState.CANCELLED, 0.0f);
		remoteFile.getCsp().resetDownload();
	}
	
	/**
	 * @return the canceller
	 */
	public Thread getCanceller()
	{
		return canceller;
	}
	
	/**
	 * @param canceller
	 *            the canceller to set
	 */
	public void setCanceller(Thread canceller)
	{
		this.canceller = canceller;
	}
	
}
