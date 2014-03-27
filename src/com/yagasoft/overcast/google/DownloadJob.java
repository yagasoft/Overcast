/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.google/DownloadJob.java
 * 
 *			Modified: 18-Mar-2014 (19:35:23)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.google;


import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.yagasoft.overcast.container.local.LocalFolder;


public class DownloadJob extends com.yagasoft.overcast.container.transfer.DownloadJob<MediaHttpDownloader>
{
	
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
	 * @see com.yagasoft.overcast.container.transfer.TransferJob#cancelTransfer()
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void cancelTransfer()
	{
		// Google doesn't have a cancel method, so I had to improvise.
		canceller.stop();
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
