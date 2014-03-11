/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 */

package com.yagasoft.overcast.container.transfer;


import com.yagasoft.overcast.container.local.LocalFile;


/**
 * A class representing a job in the upload/download queue.<br />
 * It's needed to contain information vital to complete the transfer process.
 * 
 * @param <T>
 *            the type of the object to perform the actual transfer.
 */
public abstract class TransferJob<T>
{
	
	/** The local file. */
	protected LocalFile	localFile;
	
	/** Overwrite existing container. */
	protected boolean	overwrite;
	
	/** The object to perform the actual transfer. */
	protected T			cspTransferer;
	
	/**
	 * Instantiates a new transfer job.
	 * 
	 * @param localFile
	 *            the local file
	 * @param overwrite
	 *            overwrite existing container.
	 * @param cspTransferer
	 *            the object to perform the actual transfer.
	 */
	public TransferJob(LocalFile localFile, boolean overwrite, T cspTransferer)
	{
		this.overwrite = overwrite;
		this.cspTransferer = cspTransferer;
		this.localFile = localFile;
	}
	
	/**
	 * Perform actions if transfer is successful.
	 */
	public abstract void success();
	
	/**
	 * Perform actions when progress of transfer changes.
	 */
	public void progress(float progress)
	{
		notifyListeners(TransferState.IN_PROGRESS, progress);
	}
	
	/**
	 * Perform actions if transfer failed.
	 */
	public abstract void failure();
	
	/**
	 * Notify listeners to the transfer of changes in the transfer state.
	 * 
	 * @param state
	 *            the new state
	 * @param progress
	 *            the progress between 0 and 1.
	 */
	public abstract void notifyListeners(TransferState state, float progress);
	
	// --------------------------------------------------------------------------------------
	// #region Getters and setters.
	
	/**
	 * @return the localFile
	 */
	public LocalFile getLocalFile()
	{
		return localFile;
	}
	
	/**
	 * @param localFile
	 *            the localFile to set
	 */
	public void setLocalFile(LocalFile localFile)
	{
		this.localFile = localFile;
	}
	
	/**
	 * @return the overwrite
	 */
	public boolean isOverwrite()
	{
		return overwrite;
	}
	
	/**
	 * @param overwrite
	 *            the overwrite to set
	 */
	public void setOverwrite(boolean overwrite)
	{
		this.overwrite = overwrite;
	}
	
	/**
	 * @return the cspTransferer
	 */
	public T getCspTransferer()
	{
		return cspTransferer;
	}
	
	/**
	 * @param cspTransferer
	 *            the cspTransferer to set
	 */
	public void setCspTransferer(T cspTransferer)
	{
		this.cspTransferer = cspTransferer;
	}
	
	// #endregion Getters and setters.
	// --------------------------------------------------------------------------------------
	
}
