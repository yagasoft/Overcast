/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.base.csp/LocalCSP.java
 * 
 *			Modified: 24-Jun-2014 (20:33:27)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.csp;


import java.nio.file.Path;

import com.yagasoft.overcast.base.container.local.LocalFile;
import com.yagasoft.overcast.base.container.local.LocalFolder;
import com.yagasoft.overcast.base.container.operation.IOperationListener;
import com.yagasoft.overcast.base.container.remote.RemoteFactory;
import com.yagasoft.overcast.base.container.remote.RemoteFile;
import com.yagasoft.overcast.base.container.remote.RemoteFolder;
import com.yagasoft.overcast.base.container.transfer.DownloadJob;
import com.yagasoft.overcast.base.container.transfer.UploadJob;
import com.yagasoft.overcast.exception.AuthorisationException;
import com.yagasoft.overcast.exception.OperationException;
import com.yagasoft.overcast.exception.TransferException;


/**
 * The Class LocalCSP.
 */
public class LocalCSP extends CSP<Path, Object, Object>
{
	
	/**
	 * Instantiates a new local csp.
	 */
	public LocalCSP()
	{
		name = "Local";
	}
	
	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#destroyInstance()
	 */
	@Override
	public void destroyInstance()
	{}
	
	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#initTree(com.yagasoft.overcast.base.container.operation.IOperationListener)
	 */
	@Override
	public void initTree(IOperationListener listener) throws OperationException
	{}
	
	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#resetPermission()
	 */
	@Override
	public void resetPermission() throws AuthorisationException, OperationException
	{}
	
	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#calculateRemoteFreeSpace()
	 */
	@Override
	public long calculateRemoteFreeSpace() throws OperationException
	{
		return 0;
	}
	
	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#initDownload(com.yagasoft.overcast.base.container.remote.RemoteFile,
	 *      com.yagasoft.overcast.base.container.local.LocalFolder, boolean)
	 */
	@Override
	protected DownloadJob<Object> initDownload(RemoteFile<?> file, LocalFolder parent, boolean overwrite)
			throws TransferException
	{
		return null;
	}
	
	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#initiateDownload()
	 */
	@Override
	protected void initiateDownload() throws TransferException
	{}
	
	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#initUpload(com.yagasoft.overcast.base.container.local.LocalFile,
	 *      com.yagasoft.overcast.base.container.remote.RemoteFolder, boolean,
	 *      com.yagasoft.overcast.base.container.remote.RemoteFile)
	 */
	@Override
	protected UploadJob<Object, Path> initUpload(LocalFile file, RemoteFolder<?> parent, boolean overwrite,
			RemoteFile<?> remoteFile) throws TransferException
	{
		return null;
	}
	
	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#initiateUpload()
	 */
	@Override
	protected void initiateUpload() throws TransferException
	{}
	
	/**
	 * @see com.yagasoft.overcast.base.csp.CSP#getAbstractFactory()
	 */
	@Override
	public RemoteFactory<?, ?, ?, ?, ?> getAbstractFactory()
	{
		return null;
	}
	
}
