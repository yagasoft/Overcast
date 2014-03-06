
package com.yagasoft.overcast.container.transfer;


import java.nio.file.Paths;

import com.yagasoft.overcast.container.local.LocalFile;
import com.yagasoft.overcast.container.local.LocalFolder;
import com.yagasoft.overcast.container.remote.RemoteFile;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener.TransferState;


public final class DownloadJob<T> extends TransferJob<T>
{

	protected LocalFolder	parent;
	protected RemoteFile<?>	remoteFile;

	public DownloadJob(RemoteFile<?> remoteFile, LocalFolder parent, boolean overwrite, T cspTransferer)
	{
		super(new LocalFile(), overwrite, cspTransferer);
		this.parent = parent;
		this.remoteFile = remoteFile;

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
	}

	public void success(String path)
	{

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
