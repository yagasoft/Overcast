
package com.yagasoft.overcast.container.transfer;


import com.yagasoft.overcast.container.local.LocalFile;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener.TransferState;


public abstract class TransferJob<T>
{
	
	protected LocalFile	localFile;
	protected boolean	overwrite;
	protected T			cspTransferer;
	
	public TransferJob(LocalFile localFile, boolean overwrite, T cspTransferer)
	{
		this.overwrite = overwrite;
		this.cspTransferer = cspTransferer;
		this.localFile = localFile;
	}
	
	public abstract void success();
	
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
