/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 */

package com.yagasoft.overcast.container.remote;


import com.yagasoft.overcast.CSP;
import com.yagasoft.overcast.container.local.LocalFolder;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.exception.TransferException;


/**
 * An interface for remote files and folders to easily initiate downloading.
 */
public interface IRemote
{
	
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
	 * @throws TransferException
	 *             A problem occurred during the transfer of the container.
	 */
	public void download(LocalFolder parent, boolean overwrite, ITransferProgressListener listener, Object object)
			throws TransferException;
}
