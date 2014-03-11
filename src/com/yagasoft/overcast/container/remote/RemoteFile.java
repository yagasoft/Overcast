/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 */

package com.yagasoft.overcast.container.remote;


import java.net.URL;

import com.yagasoft.overcast.container.File;
import com.yagasoft.overcast.container.local.LocalFile;
import com.yagasoft.overcast.container.local.LocalFolder;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.exception.TransferException;


/**
 * A class representing the files on the server.
 * 
 * @param <T>
 *            The source file type (file type from the original CSP API) must be passed to this class.
 */
public abstract class RemoteFile<T> extends File<T> implements IRemote
{
	
	/** The direct link to the file (can be used to download the file). */
	protected URL		link;
	
	/** The {@link LocalFile} corresponding to this remote file if applicable. */
	protected LocalFile	localMapping;
	
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
	public LocalFile getLocalMapping()
	{
		return localMapping;
	}
	
	/**
	 * @param localMapping
	 *            the localMapping to set
	 */
	public void setLocalMapping(LocalFile localMapping)
	{
		this.localMapping = localMapping;
	}
	
	// #endregion Getters and setters.
	// --------------------------------------------------------------------------------------
	
}
