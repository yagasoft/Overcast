/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 */

package com.yagasoft.overcast.container.transfer;


import java.nio.file.Paths;

import com.yagasoft.overcast.container.local.LocalFile;
import com.yagasoft.overcast.container.local.LocalFolder;
import com.yagasoft.overcast.container.remote.RemoteFile;
import com.yagasoft.overcast.container.transfer.TransferState;


/**
 * A class representing a job in the download queue.<br />
 * It's needed to contain information vital to complete the download process.
 *
 * @param <T>
 *            the type of the object to perform the actual download.
 */
public final class DownloadJob<T> extends TransferJob<T>
{

	/** The parent. */
	protected LocalFolder	parent;

	/** The remote file to download. */
	protected RemoteFile<?>	remoteFile;

	/**
	 * Instantiates a new download job.
	 *
	 * @param remoteFile
	 *            the remote file
	 * @param parent
	 *            the parent
	 * @param overwrite
	 *            overwrite already existing file.
	 * @param cspTransferer
	 *            downloader
	 */
	public DownloadJob(RemoteFile<?> remoteFile, LocalFolder parent, boolean overwrite, T cspTransferer)
	{
		super(new LocalFile(), overwrite, cspTransferer);
		this.parent = parent;
		this.remoteFile = remoteFile;

		// prepare the local file object
		localFile.setSourceObject(Paths.get(parent.getPath(), remoteFile.getName()));
		localFile.setPath(localFile.getSourceObject().toString());
	}

	/**
	 * @see com.yagasoft.overcast.container.transfer.TransferJob#success()
	 */
	@Override
	public void success()
	{
		localFile.updateInfo();
		localFile.setRemoteMapping(remoteFile);
		remoteFile.setLocalMapping(localFile);
		parent.add(localFile);
		notifyListeners(TransferState.COMPLETED, 1.0f);
	}

	/**
	 * ...
	 *
	 * @param path
	 *            the path
	 */
	public void success(String path)
	{

	}

	/**
	 * @see com.yagasoft.overcast.container.transfer.TransferJob#failure()
	 */
	@Override
	public void failure()
	{
		notifyListeners(TransferState.FAILED, 0.0f);
	}

	/**
	 * @see com.yagasoft.overcast.container.transfer.TransferJob#notifyListeners(com.yagasoft.overcast.container.transfer.ITransferProgressListener.TransferState, float)
	 */
	@Override
	public void notifyListeners(TransferState state, float progress)
	{
		remoteFile.notifyListeners(state, progress);
	}

	// --------------------------------------------------------------------------------------
	// #region Getters and setters.

	/**
	 * @return the remoteFile
	 */
	public RemoteFile<?> getRemoteFile()
	{
		return remoteFile;
	}


	/**
	 * @param remoteFile the remoteFile to set
	 */
	public void setRemoteFile(RemoteFile<?> remoteFile)
	{
		this.remoteFile = remoteFile;
	}


	/**
	 * @return the parent
	 */
	public LocalFolder getParent()
	{
		return parent;
	}


	/**
	 * @param parent the parent to set
	 */
	public void setParent(LocalFolder parent)
	{
		this.parent = parent;
	}

	// #endregion Getters and setters.
	// --------------------------------------------------------------------------------------

}
