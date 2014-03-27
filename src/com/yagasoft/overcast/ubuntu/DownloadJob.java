/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.ubuntu/DownloadJob.java
 *
 *			Modified: 18-Mar-2014 (19:35:48)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.ubuntu;


import com.ubuntuone.api.files.request.U1DownloadListener;
import com.ubuntuone.api.files.util.U1CancelTrigger;
import com.yagasoft.overcast.container.local.LocalFolder;


public class DownloadJob extends com.yagasoft.overcast.container.transfer.DownloadJob<U1DownloadListener>
{
	
	U1CancelTrigger	canceller;
	
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
			, U1DownloadListener cspTransferer, U1CancelTrigger canceller)
	{
		super(remoteFile, parent, overwrite, cspTransferer);
		this.canceller = canceller;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.transfer.TransferJob#cancelTransfer()
	 */
	@Override
	public void cancelTransfer()
	{
		canceller.onCancel();
	}
	
	/**
	 * @return the canceller
	 */
	public U1CancelTrigger getCanceller()
	{
		return canceller;
	}
	
	/**
	 * @param canceller
	 *            the canceller to set
	 */
	public void setCanceller(U1CancelTrigger canceller)
	{
		this.canceller = canceller;
	}
	
}
