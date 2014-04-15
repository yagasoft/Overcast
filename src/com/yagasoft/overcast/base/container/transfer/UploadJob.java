/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.transfer/UploadJob.java
 * 
 *			Modified: Apr 15, 2014 (9:32:23 AM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.transfer;


import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.File;
import com.yagasoft.overcast.base.container.Folder;
import com.yagasoft.overcast.base.container.local.LocalFile;
import com.yagasoft.overcast.base.container.remote.RemoteFile;


/**
 * A class representing a job in the upload queue.<br />
 * It's needed to contain information vital to complete the upload process.
 * 
 * @param <T>
 *            the type of the object to perform the actual upload.
 * @param <S>
 *            the type of the file given by the CSP.
 */
public abstract class UploadJob<T, S> extends TransferJob<T>
{
	
	/** The remote file object. */
	protected RemoteFile<S>	remoteFile;
	
	/**
	 * Instantiates a new upload job.
	 * 
	 * @param localFile
	 *            the local file
	 * @param remoteFile
	 *            the remote file
	 * @param parent
	 *            the parent
	 * @param overwrite
	 *            overwrite existing file
	 * @param cspTransferer
	 *            uploader
	 */
	public UploadJob(LocalFile localFile, RemoteFile<S> remoteFile, Folder<?> parent, boolean overwrite, T cspTransferer)
	{
		super(localFile, parent, remoteFile.getCsp(), overwrite, cspTransferer);
		this.parent = parent;
		this.remoteFile = remoteFile;
	}
	
	/**
	 * Do NOT use!<br />
	 * As the CSP API file type can't be determined in this general implementation, so it has to be passed after creating an
	 * object of this job in the CSP.
	 * 
	 * @see com.yagasoft.overcast.base.container.transfer.TransferJob#success()
	 */
	@Override
	public void success()
	{
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Perform stuff on successful upload.
	 * 
	 * @param file
	 *            the file object from the original CSP API.
	 */
	public void success(S file)
	{
		remoteFile.setSourceObject(file);
		remoteFile.updateInfo();
		localFile.setRemoteMapping(remoteFile);
		remoteFile.setLocalMapping(localFile);
		parent.add(remoteFile);
		Logger.newEntry("uploaded successfully " + remoteFile.getPath());
		notifyProgressListeners(TransferState.COMPLETED, 1.0f);
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.transfer.TransferJob#failure()
	 */
	@Override
	public void failure()
	{
		notifyProgressListeners(TransferState.FAILED, 0.0f);
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.transfer.TransferJob#getSourceFile()
	 */
	@Override
	public File<?> getSourceFile()
	{
		return localFile;
	}
	
	// --------------------------------------------------------------------------------------
	// #region Getters and setters.
	
	/**
	 * @return the remoteFile
	 */
	public RemoteFile<S> getRemoteFile()
	{
		return remoteFile;
	}
	
	/**
	 * @param remoteFile
	 *            the remoteFile to set
	 */
	public void setRemoteFile(RemoteFile<S> remoteFile)
	{
		this.remoteFile = remoteFile;
	}
	
	// #endregion Getters and setters.
	// --------------------------------------------------------------------------------------
	
}
