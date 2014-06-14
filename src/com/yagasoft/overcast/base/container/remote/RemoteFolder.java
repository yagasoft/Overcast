/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.remote/RemoteFolder.java
 * 
 *			Modified: Apr 15, 2014 (9:21:10 AM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.remote;


import java.net.URL;

import com.yagasoft.overcast.base.container.Folder;
import com.yagasoft.overcast.base.container.local.LocalFolder;
import com.yagasoft.overcast.base.container.transfer.DownloadJob;
import com.yagasoft.overcast.base.container.transfer.event.ITransferProgressListener;
import com.yagasoft.overcast.base.csp.CSP;
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.OperationException;
import com.yagasoft.overcast.exception.TransferException;


/**
 * A class representing the folders on the server.<br />
 * All methods in this class must be synchronised in the implementation.
 * 
 * @param <T>
 *            The source folder type (folder type from the original CSP API) must be passed to this class.
 */
public abstract class RemoteFolder<T> extends Folder<T>
{
	
	/** Pointless for most CSPs! Could be used to display to the user. */
	protected URL			link;
	
	/** The {@link RemoteFolder} corresponding to this remote folder if applicable. */
	protected LocalFolder	localMapping;
	
	/**
	 * Download the container (passed) from the server.<br />
	 * This should just call the one in {@link CSP}.
	 * 
	 * @param parent
	 *            The local folder to download to. Must pass a {@link LocalFolder} with the path initialised in it.
	 * @param overwrite
	 *            Whether to overwrite existing container on the local disk or not.
	 * @param listener
	 *            Object listening to the changes in the transfer state.
	 * @return the download jobs
	 * @throws TransferException
	 *             A problem occurred during the transfer of the container.
	 * @throws OperationException
	 *             the operation exception
	 * @throws CreationException
	 *             the creation exception
	 */
	public DownloadJob<?>[] download(LocalFolder parent, boolean overwrite, ITransferProgressListener listener)
			throws TransferException, OperationException, CreationException
	{
		return csp.download(this, parent, overwrite, listener);
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
