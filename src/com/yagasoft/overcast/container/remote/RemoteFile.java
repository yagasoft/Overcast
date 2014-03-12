/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 */

package com.yagasoft.overcast.container.remote;


import java.net.URL;

import com.yagasoft.overcast.CSP;
import com.yagasoft.overcast.container.File;
import com.yagasoft.overcast.container.local.LocalFile;
import com.yagasoft.overcast.container.local.LocalFolder;
import com.yagasoft.overcast.container.transfer.DownloadJob;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.exception.TransferException;


/**
 * A class representing the files on the server.
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
	 * @param object
	 *            Object passed by the initialiser to be passed back on state change. It can be used as a kind of "call-back" or
	 *            something; the sender of this object can cast it back and use it as seen fit.
	 * @return the download job
	 * @throws TransferException
	 *             A problem occurred during the transfer of the container.
	 */
	public DownloadJob<?> download(LocalFolder parent, boolean overwrite, ITransferProgressListener listener, Object object)
			throws TransferException
	{
		return csp.download(this, parent, overwrite, listener, object);
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
