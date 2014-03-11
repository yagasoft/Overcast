/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 */

package com.yagasoft.overcast.container.remote;


import java.net.URL;

import com.yagasoft.overcast.container.Folder;
import com.yagasoft.overcast.container.local.LocalFolder;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.exception.TransferException;


/**
 * A class representing the folders on the server.
 * 
 * @param <T>
 *            The source folder type (folder type from the original CSP API) must be passed to this class.
 */
public abstract class RemoteFolder<T> extends Folder<T> implements IRemote
{
	
	/** Pointless for most CSPs! */
	protected URL			link;
	
	/** The {@link RemoteFolder} corresponding to this remote folder if applicable. */
	protected LocalFolder	localMapping;
	
	/**
	 * @see com.yagasoft.overcast.container.remote.IRemote#download(com.yagasoft.overcast.container.local.LocalFolder, boolean,
	 *      com.yagasoft.overcast.container.transfer.ITransferProgressListener, java.lang.Object)
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
