/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.remote/RemoteFile.java
 * 
 *			Modified: Apr 15, 2014 (9:20:21 AM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.remote;


import java.net.URL;

import com.yagasoft.overcast.base.container.File;
import com.yagasoft.overcast.base.container.local.LocalFile;
import com.yagasoft.overcast.base.container.local.LocalFolder;
import com.yagasoft.overcast.base.container.transfer.DownloadJob;
import com.yagasoft.overcast.base.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.base.csp.CSP;
import com.yagasoft.overcast.exception.OperationException;
import com.yagasoft.overcast.exception.TransferException;


/**
 * A class representing the files on the server.<br />
 * All methods in this class must be synchronised in the implementation.
 * 
 * @param <T>
 *            The source file type (file type from the original CSP API) must be passed to this class.
 */
public abstract class RemoteFile<T> extends File<T>
{
	
	/** The direct link to the file (can be used to download the file). */
	protected URL		link;
	
	/** The {@link LocalFile} corresponding to this remote file if applicable. */
	protected LocalFile	localMapping;
	
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
	 * @return the download job
	 * @throws TransferException
	 *             A problem occurred during the transfer of the container.
	 * @throws OperationException
	 *             the operation exception
	 */
	public DownloadJob<?> download(LocalFolder parent, boolean overwrite, ITransferProgressListener listener)
			throws TransferException, OperationException
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
