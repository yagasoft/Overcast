/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.dropbox/UploadJob.java
 *
 *			Modified: 11-Apr-2014 (19:03:02)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.dropbox.transfer;


import com.dropbox.core.DbxEntry.File;
import com.yagasoft.overcast.base.container.local.LocalFile;
import com.yagasoft.overcast.implement.dropbox.RemoteFile;
import com.yagasoft.overcast.implement.dropbox.RemoteFolder;


public class UploadJob extends com.yagasoft.overcast.base.container.transfer.UploadJob<Uploader, File>
{
	
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
