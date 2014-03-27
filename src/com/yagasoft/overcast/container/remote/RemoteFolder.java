/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.container.remote/RemoteFolder.java
 * 
 *			Modified: 27-Mar-2014 (16:13:20)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.container.remote;


import java.net.URL;

import com.yagasoft.overcast.CSP;
import com.yagasoft.overcast.container.Folder;
import com.yagasoft.overcast.container.local.LocalFolder;
import com.yagasoft.overcast.container.transfer.DownloadJob;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.exception.TransferException;


/**
 * A class representing the folders on the server.
 * 
 * @param <T>
 *            The source folder type (folder type from the original CSP API) must be passed to this class.
 */
public abstract class RemoteFolder<T> extends Folder<T>
{
	
	/** Pointless for most CSPs! */
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
	 * @param object
	 *            Object passed by the initialiser to be passed back on state change. It can be used as a kind of "call-back" or
	 *            something; the sender of this object can cast it back and use it as seen fit.
	 * @return the download jobs
	 * @throws TransferException
	 *             A problem occurred during the transfer of the container.
	 */
	public DownloadJob<?>[] download(LocalFolder parent, boolean overwrite, ITransferProgressListener listener)
			throws TransferException
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
