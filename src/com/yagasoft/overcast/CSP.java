
package com.yagasoft.overcast;


import java.util.LinkedList;
import java.util.Queue;

import com.yagasoft.overcast.authorisation.Authorisation;
import com.yagasoft.overcast.container.local.LocalFile;
import com.yagasoft.overcast.container.local.LocalFolder;
import com.yagasoft.overcast.container.remote.RemoteFile;
import com.yagasoft.overcast.container.remote.RemoteFolder;
import com.yagasoft.overcast.container.transfer.DownloadJob;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.container.transfer.UploadJob;
import com.yagasoft.overcast.exception.TransferException;


public abstract class CSP
{

	protected Authorisation			authorisation;
	protected LocalFolder			localFileTree;
	protected RemoteFolder<?>		remoteFileTree;
	protected boolean				fullLocalTreeLoaded;
	protected boolean				fullRemoteTreeLoaded;
	protected long					localFreeSpace;
	protected long					remoteFreeSpace;
	protected Queue<DownloadJob<?>>	downloadQueue	= new LinkedList<DownloadJob<?>>();
	protected DownloadJob<?>		currentDownloadJob;
	protected Queue<UploadJob<?>>	uploadQueue		= new LinkedList<UploadJob<?>>();
	protected UploadJob<?>			currentUploadJob;

	// add a RemoteFactory object in the subclass.

	public abstract void initTree();

	public abstract void buildFileTree(boolean recursively);

	public long calculateLocalFreeSpace()
	{
		return localFileTree.calculateSize();
	}

	public long calculateRemoteFreeSpace()
	{
		return remoteFileTree.calculateSize();
	}

	public abstract void download(RemoteFolder<?> folder, LocalFolder parent, boolean overwrite,
	ITransferProgressListener listener, Object object);

	public abstract void download(RemoteFile<?> file, LocalFolder parent, boolean overwrite, ITransferProgressListener listener,
			Object object)
			throws TransferException;

	public abstract void nextDownloadJob();

	public abstract void upload(LocalFolder folder, RemoteFolder<?> parent, boolean overwrite,
	ITransferProgressListener listener, Object object);

	public abstract void upload(LocalFile file, RemoteFolder<?> parent, boolean overwrite, ITransferProgressListener listener,
			Object object)
			throws TransferException;

	public abstract void nextUploadJob();

	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	// ======================================================================================

	public Authorisation getAuthorisation()
	{
		return authorisation;
	}

	public void setAuthorisation(Authorisation value)
	{
		authorisation = value;
	}

	/**
	 * @return the localFileTree
	 */
	public LocalFolder getLocalFileTree()
	{
		return localFileTree;
	}

	/**
	 * @param localFileTree
	 *            the localFileTree to set
	 */
	public void setLocalFileTree(LocalFolder localFileTree)
	{
		this.localFileTree = localFileTree;
	}

	public RemoteFolder<?> getRemoteFileTree()
	{
		return remoteFileTree;
	}

	public void setRemoteFileTree(RemoteFolder<?> value)
	{
		remoteFileTree = value;
	}

	/**
	 * @return the fullLocalTreeLoaded
	 */
	public boolean isFullLocalTreeLoaded()
	{
		return fullLocalTreeLoaded;
	}

	/**
	 * @param fullLocalTreeLoaded
	 *            the fullLocalTreeLoaded to set
	 */
	public void setFullLocalTreeLoaded(boolean fullLocalTreeLoaded)
	{
		this.fullLocalTreeLoaded = fullLocalTreeLoaded;
	}

	/**
	 * @return the fullRemoteTreeLoaded
	 */
	public boolean isFullRemoteTreeLoaded()
	{
		return fullRemoteTreeLoaded;
	}

	/**
	 * @param fullRemoteTreeLoaded
	 *            the fullRemoteTreeLoaded to set
	 */
	public void setFullRemoteTreeLoaded(boolean fullRemoteTreeLoaded)
	{
		this.fullRemoteTreeLoaded = fullRemoteTreeLoaded;
	}

	/**
	 * @return the localFreeSpace
	 */
	public long getLocalFreeSpace()
	{
		return localFreeSpace;
	}

	/**
	 * @param localFreeSpace
	 *            the localFreeSpace to set
	 */
	public void setLocalFreeSpace(long localFreeSpace)
	{
		this.localFreeSpace = localFreeSpace;
	}

	/**
	 * @return the remoteFreeSpace
	 */
	public long getRemoteFreeSpace()
	{
		return remoteFreeSpace;
	}

	/**
	 * @param remoteFreeSpace
	 *            the remoteFreeSpace to set
	 */
	public void setRemoteFreeSpace(long remoteFreeSpace)
	{
		this.remoteFreeSpace = remoteFreeSpace;
	}

	// ======================================================================================
	// #endregion Getters and setters.
	// //////////////////////////////////////////////////////////////////////////////////////

}
