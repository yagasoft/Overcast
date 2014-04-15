/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.implement.dropbox.transfer/UploadJob.java
 * 
 *			Modified: Apr 15, 2014 (1:50:16 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.dropbox.transfer;


import com.dropbox.core.DbxEntry.File;
import com.yagasoft.overcast.base.container.local.LocalFile;
import com.yagasoft.overcast.implement.dropbox.RemoteFile;
import com.yagasoft.overcast.implement.dropbox.RemoteFolder;


/**
 * @see com.yagasoft.overcast.base.container.transfer.UploadJob
 */
public class UploadJob extends com.yagasoft.overcast.base.container.transfer.UploadJob<Uploader, File>
{
	
	/** The canceller used to cancel the transfer. */
	Uploader	canceller;
	
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
			, Uploader cspTransferer, Uploader canceller)
	{
		super(localFile, remoteFile, parent, overwrite, cspTransferer);
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
	public Uploader getCanceller()
	{
		return canceller;
	}
	
	/**
	 * @param canceller
	 *            the canceller to set
	 */
	public void setCanceller(Uploader canceller)
	{
		this.canceller = canceller;
	}
	
}
