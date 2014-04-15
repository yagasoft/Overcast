/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.implement.google/UploadJob.java
 * 
 *			Modified: Apr 15, 2014 (1:54:35 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.google;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.yagasoft.overcast.base.container.local.LocalFile;
import com.yagasoft.overcast.base.container.transfer.TransferState;


/**
 * @see com.yagasoft.overcast.base.container.transfer.UploadJob
 */
public class UploadJob extends com.yagasoft.overcast.base.container.transfer.UploadJob<Drive.Files.Insert, File>
{

	/** The canceller to cancel this transfer. */
	Thread	canceller;

	/**
	 * Instantiates a new upload job.
	 *
	 * @param localFile
	 *            Local file.
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
	public UploadJob(LocalFile localFile, RemoteFile remoteFile, RemoteFolder parent, boolean overwrite
			, Drive.Files.Insert cspTransferer, Thread canceller)
	{
		super(localFile, remoteFile, parent, overwrite, cspTransferer);
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
		remoteFile.getCsp().resetUpload();
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
