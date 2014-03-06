
package com.yagasoft.overcast.container.remote;


import java.net.URL;

import com.yagasoft.overcast.container.File;
import com.yagasoft.overcast.container.local.LocalFolder;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.exception.TransferException;


public abstract class RemoteFile<T> extends File<T> implements IRemote
{
	
	protected URL			link;
	protected LocalFolder	localMapping;
	
	/**
	 * @see com.yagasoft.overcast.container.remote.IRemote#download(com.yagasoft.overcast.container.local.LocalFolder, boolean, com.yagasoft.overcast.container.transfer.ITransferProgressListener, java.lang.Object)
	 */
	@Override
	public void download(LocalFolder parent, boolean overwrite, ITransferProgressListener listener, Object object)
			throws TransferException
	{
		csp.download(this, parent, overwrite, listener, object);
	}
	
	// --------------------------------------------------------------------------------------
	// #region Getters and setters.
	
	/**
	 * @return the link
	 */
	public URL getLink()
	{
		return link;
	}
	
	/**
	 * @param link
	 *            the link to set
	 */
	public void setLink(URL link)
	{
		this.link = link;
	}
	
	/**
	 * @return the localMapping
	 */
	public LocalFolder getLocalMapping()
	{
		return localMapping;
	}
	
	/**
	 * @param localMapping
	 *            the localMapping to set
	 */
	public void setLocalMapping(LocalFolder localMapping)
	{
		this.localMapping = localMapping;
	}
	
	// #endregion Getters and setters.
	// --------------------------------------------------------------------------------------
	
}
