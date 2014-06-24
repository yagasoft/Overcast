/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.transfer/TransferJob.java
 *
 *			Modified: 24-Jun-2014 (21:32:50)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.transfer;


import java.util.ArrayList;
import java.util.List;

import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.File;
import com.yagasoft.overcast.base.container.Folder;
import com.yagasoft.overcast.base.container.local.LocalFile;
import com.yagasoft.overcast.base.container.transfer.event.ITransferProgressListener;
import com.yagasoft.overcast.base.container.transfer.event.ITransferrable;
import com.yagasoft.overcast.base.container.transfer.event.TransferEvent;
import com.yagasoft.overcast.base.container.transfer.event.TransferState;
import com.yagasoft.overcast.base.csp.CSP;


/**
 * A class representing a job in the upload/download queue.<br />
 * It's needed to contain information vital to complete the transfer process.
 *
 * @param <T>
 *            the type of the object to perform the actual transfer.
 */
public abstract class TransferJob<T> implements ITransferrable
{
	
	/** The local file. */
	protected LocalFile							localFile;
	
	/** The parent. */
	protected Folder<?>							parent;
	
	/** Overwrite existing container. */
	protected boolean							overwrite;
	
	/** The parent. */
	protected CSP<?, ?, ?>						csp;
	
	/** The object to perform the actual transfer. */
	protected T									cspTransferer;
	
	/** Progress listeners to the download or upload of this container. */
	protected List<ITransferProgressListener>	progressListeners	= new ArrayList<ITransferProgressListener>();
	
	/**
	 * Instantiates a new transfer job.
	 *
	 * @param localFile
	 *            the local file
	 * @param parent
	 *            Parent.
	 * @param csp
	 *            the CSP of the remote container.
	 * @param overwrite
	 *            overwrite existing container.
	 * @param cspTransferer
	 *            the object to perform the actual transfer.
	 */
	public TransferJob(LocalFile localFile, Folder<?> parent, CSP<?, ?, ?> csp, boolean overwrite, T cspTransferer)
	{
		this.overwrite = overwrite;
		this.cspTransferer = cspTransferer;
		this.localFile = localFile;
		this.csp = csp;
	}
	
	/**
	 * Checks if this is a download job.
	 *
	 * @return true, if it is download job
	 */
	public abstract boolean isDownloadJob();
	
	/**
	 * Perform actions if transfer is successful.
	 */
	public abstract void success();
	
	/**
	 * Perform actions when progress of transfer changes.
	 */
	public void progress(float progress)
	{
		notifyProgressListeners(TransferState.IN_PROGRESS, progress);
	}
	
	/**
	 * Perform actions if transfer failed.
	 */
	public abstract void failure();
	
	/**
	 * Cancel the transfer if supported. This should be overridden and the procedure implemented.
	 */
	public void cancelTransfer() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("This CSP doesn't support cancelling transfers.");
	}
	
	/**
	 * Gets the source file.
	 *
	 * @return the source file
	 */
	public abstract File<?> getSourceFile();
	
	/**
	 * Gets the destination file.
	 *
	 * @return the destination file
	 */
	public abstract File<?> getDestinationFile();
	
	/**
	 * @see com.yagasoft.overcast.base.container.transfer.event.ITransferrable#addProgressListener(com.yagasoft.overcast.base.container.transfer.event.ITransferProgressListener)
	 */
	@Override
	public void addProgressListener(ITransferProgressListener listener)
	{
		progressListeners.add(listener);
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.transfer.event.ITransferrable#removeProgressListener(com.yagasoft.overcast.base.container.transfer.event.ITransferProgressListener)
	 */
	@Override
	public void removeProgressListener(ITransferProgressListener listener)
	{
		progressListeners.remove(listener);
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.transfer.event.ITransferrable#notifyProgressListeners(com.yagasoft.overcast.base.container.transfer.event.TransferState,
	 *      float)
	 */
	@Override
	public void notifyProgressListeners(TransferState state, float progress)
	{
		Logger.info("OVERCAST: JOB: transfer event: " + state + " => " + progress);
		
		for (ITransferProgressListener listener : progressListeners)
		{
			listener.transferProgressChanged(new TransferEvent(this, state, progress));
		}
		
		if (state == TransferState.COMPLETED)
		{
			clearTransferListeners();
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.transfer.event.ITransferrable#clearTransferListeners()
	 */
	@Override
	public void clearTransferListeners()
	{
		progressListeners.clear();
	}
	
	@Override
	public String toString()
	{
		return getSourceFile().getPath();
	}
	
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
	
	/**
	 * @return the parent
	 */
	public Folder<?> getParent()
	{
		return parent;
	}
	
	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(Folder<?> parent)
	{
		this.parent = parent;
	}
	
	/**
	 * @return the csp
	 */
	public CSP<?, ?, ?> getCsp()
	{
		return csp;
	}
	
	/**
	 * @param csp
	 *            the csp to set
	 */
	public void setCsp(CSP<?, ?, ?> csp)
	{
		this.csp = csp;
	}
	
	// #endregion Getters and setters.
	// --------------------------------------------------------------------------------------
	
}
