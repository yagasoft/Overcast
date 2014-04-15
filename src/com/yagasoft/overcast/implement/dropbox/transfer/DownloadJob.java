/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.implement.dropbox.transfer/DownloadJob.java
 * 
 *			Modified: Apr 15, 2014 (1:19:56 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.dropbox.transfer;


import com.yagasoft.overcast.base.container.local.LocalFolder;
import com.yagasoft.overcast.implement.dropbox.RemoteFile;


/**
 * @see com.yagasoft.overcast.base.container.transfer.DownloadJob
 */
public class DownloadJob extends com.yagasoft.overcast.base.container.transfer.DownloadJob<Downloader>
{
	
	/** The canceller, which is used to cancel the transfer. */
	Downloader	canceller;
	
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
			, Downloader cspTransferer, Downloader canceller)
	{
		super(remoteFile, parent, overwrite, cspTransferer);
		this.canceller = canceller;
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.transfer.TransferJob#cancelTransfer()
	 */
	@Override
	public void cancelTransfer()
	{
		canceller.cancel();
	}
	
	/**
	 * @return the canceller
	 */
	public Downloader getCanceller()
	{
		return canceller;
	}
	
	/**
	 * @param canceller
	 *            the canceller to set
	 */
	public void setCanceller(Downloader canceller)
	{
		this.canceller = canceller;
	}
	
}
