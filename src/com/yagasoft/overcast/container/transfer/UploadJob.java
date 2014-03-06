
package com.yagasoft.overcast.container.transfer;


import com.yagasoft.overcast.container.local.LocalFile;
import com.yagasoft.overcast.container.remote.RemoteFile;
import com.yagasoft.overcast.container.remote.RemoteFolder;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener.TransferState;


public final class UploadJob<T, S> extends TransferJob<T>
{

	protected RemoteFolder<?>	parent;
	protected RemoteFile<S> remoteFile;

	public UploadJob(LocalFile localFile, RemoteFile<S> remoteFile, RemoteFolder<?> parent, boolean overwrite, T cspTransferer)
	{
		super(localFile, overwrite, cspTransferer);
		this.parent = parent;
		this.remoteFile = remoteFile;
	}

	/**
	 * @see com.yagasoft.overcast.container.transfer.TransferJob#success()
	 */
	@Override
	public void success()
	{
		throw new UnsupportedOperationException();
	}

	public void success(S file)
	{
		remoteFile.setSourceObject(file);
		remoteFile.updateInfo();
		localFile.setRemoteMapping(remoteFile);
		remoteFile.setLocalMapping(localFile);
		parent.add(remoteFile);
	}

	/**
	 * @see com.yagasoft.overcast.container.transfer.TransferJob#notifyListeners(com.yagasoft.overcast.container.transfer.ITransferProgressListener.TransferState, float)
	 */
	@Override
	public void notifyListeners(TransferState state, float progress)
	{
		localFile.notifyListeners(state, progress);
	}

	// --------------------------------------------------------------------------------------
	// #region Getters and setters.

	/**
	 * @return the parent
	 */
	public RemoteFolder<?> getParent()
	{
		return parent;
	}


	/**
	 * @param parent the parent to set
	 */
	public void setParent(RemoteFolder<?> parent)
	{
		this.parent = parent;
	}


	/**
	 * @return the remoteFile
	 */
	public RemoteFile<S> getRemoteFile()
	{
		return remoteFile;
	}


	/**
	 * @param remoteFile the remoteFile to set
	 */
	public void setRemoteFile(RemoteFile<S> remoteFile)
	{
		this.remoteFile = remoteFile;
	}

	// #endregion Getters and setters.
	// --------------------------------------------------------------------------------------

}
