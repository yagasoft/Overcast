/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.ubuntu/UploadJob.java
 *
 *			Modified: 18-Mar-2014 (19:35:44)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.ubuntu;


import com.ubuntuone.api.files.model.U1File;
import com.ubuntuone.api.files.request.U1UploadListener;
import com.ubuntuone.api.files.util.U1CancelTrigger;
import com.yagasoft.overcast.base.container.local.LocalFile;
import com.yagasoft.overcast.exception.OperationException;


public class UploadJob extends com.yagasoft.overcast.base.container.transfer.UploadJob<U1UploadListener, U1File>
{
	
	U1CancelTrigger	canceller;
	
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
			, U1UploadListener cspTransferer, U1CancelTrigger canceller)
	{
		super(localFile, remoteFile, parent, overwrite, cspTransferer);
		this.canceller = canceller;
	}
	
	@Override
	public void success(U1File file)
	{
		super.success(file);
		
		try
		{
			remoteFile.updateFromSource();
		}
		catch (OperationException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.transfer.TransferJob#cancelTransfer()
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
