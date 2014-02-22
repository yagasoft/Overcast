
package com.yagasoft.overcast;


import com.yagasoft.overcast.authorisation.Authorisation;
import com.yagasoft.overcast.container.LocalFolder;
import com.yagasoft.overcast.container.RemoteFolder;


public abstract class CSP
{
	
	protected Authorisation		authorisation;
	protected LocalFolder		localFileTree;
	protected RemoteFolder<?>	remoteFileTree;
	protected boolean			fullLocalTreeLoaded;
	protected boolean			fullRemoteTreeLoaded;
	protected long				localFreeSpace;
	protected long				remoteFreeSpace;
	
	public long calculateLocalFreeSpace()
	{
		return localFileTree.calculateSize();
	}
	
	public long calculateRemoteFreeSpace()
	{
		return remoteFileTree.calculateSize();
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	// ======================================================================================
	
	public Authorisation getAuthorisation()
	{
		return this.authorisation;
	}
	
	public void setAuthorisation(Authorisation value)
	{
		this.authorisation = value;
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
		return this.remoteFileTree;
	}
	
	public void setRemoteFileTree(RemoteFolder<?> value)
	{
		this.remoteFileTree = value;
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
