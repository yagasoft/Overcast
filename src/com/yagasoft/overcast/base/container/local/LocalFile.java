/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.container.local/LocalFile.java
 *
 *			Modified: 27-Mar-2014 (16:12:38)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.local;


import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.yagasoft.overcast.base.container.File;
import com.yagasoft.overcast.base.container.Folder;
import com.yagasoft.overcast.base.container.operation.IOperationListener;
import com.yagasoft.overcast.base.container.remote.RemoteFile;
import com.yagasoft.overcast.base.container.remote.RemoteFolder;
import com.yagasoft.overcast.base.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.base.container.transfer.UploadJob;
import com.yagasoft.overcast.base.csp.CSP;
import com.yagasoft.overcast.exception.AccessException;
import com.yagasoft.overcast.exception.OperationException;
import com.yagasoft.overcast.exception.TransferException;


/**
 * A class representing files stored locally.
 */
public class LocalFile extends File<Path>
{
	
	/** The {@link RemoteFile} corresponding to this local file if applicable. */
	protected RemoteFile<?>	remoteMapping;
	
	/**
	 * Instantiates a new local file.
	 */
	public LocalFile()
	{}
	
	/**
	 * Instantiates a new local file.
	 * 
	 * @param file
	 *            Java library File object.
	 */
	public LocalFile(Path file)
	{
		sourceObject = file;
		updateInfo();		// updating the info locally costs nothing, so do it automatically.
	}
	
	/**
	 * Instantiates a new local file.
	 * 
	 * @param path
	 *            Path to the file.
	 */
	public LocalFile(String path)
	{
		this(Paths.get(path));		// get the file object and pass it to the other constructor.
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#generateId()
	 */
	@Override
	public void generateId()
	{
		id = path;
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#isExist()
	 */
	@Override
	public synchronized boolean isExist() throws AccessException
	{
		// if the Java library says the file doesn't exist, and at same time it says the file doesn't 'not exist', then ...
		// obviously a problem.
		if ( !Files.exists(sourceObject) && !Files.notExists(sourceObject))
		{
			throw new AccessException("Can't determine if file exists or not!");
		}
		
		return Files.exists(sourceObject);
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#updateInfo()
	 */
	@Override
	public synchronized void updateInfo()
	{
		try
		{
			updateFromSource();	// updating the info locally costs nothing, so do it automatically.
		}
		catch (OperationException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#updateFromSource()
	 */
	@Override
	public synchronized void updateFromSource() throws OperationException
	{
		name = sourceObject.getFileName().toString();
		path = sourceObject.toAbsolutePath().toString();
		type = URLConnection.guessContentTypeFromName(path);		// guess type of file (MIME)
		
		try
		{
			size = Files.size(sourceObject);
		}
		catch (IOException e)
		{
			size = 0;
			e.printStackTrace();
			throw new OperationException("Couldn't update info! " + e.getMessage());
		}
		
		generateId();
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#copy(com.yagasoft.overcast.base.container.Folder, boolean, IOperationListener)
	 */
	@Override
	public synchronized LocalFile copy(Folder<?> destination, boolean overwrite, IOperationListener listener)
			throws OperationException
	{
		try
		{
			return new LocalFile(Files.copy(
					sourceObject
					// get destination path, and add to it this file's name to form complete path.
					, ((Path) destination.getSourceObject()).resolve(sourceObject.getFileName())
					, overwrite ?
							new CopyOption[] { REPLACE_EXISTING, COPY_ATTRIBUTES }
							:
							new CopyOption[] { COPY_ATTRIBUTES }));
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new OperationException("Failed to copy file! " + e.getMessage());
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#move(com.yagasoft.overcast.base.container.Folder, boolean, IOperationListener)
	 */
	@Override
	public synchronized void move(Folder<?> destination, boolean overwrite, IOperationListener listener)
			throws OperationException
	{
		try
		{
			sourceObject = Files.move(
					sourceObject
					, ((Path) destination.getSourceObject()).resolve(sourceObject.getFileName())
					, overwrite ?
							new CopyOption[] { REPLACE_EXISTING }
							:
							new CopyOption[0]);
			
			updateFromSource();		// need to update new path.
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new OperationException("Failed to move file! " + e.getMessage());
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#rename(java.lang.String, IOperationListener)
	 */
	@Override
	public synchronized void rename(String newName, IOperationListener listener) throws OperationException
	{
		try
		{
			// renaming is effectively moving under a new name.
			sourceObject = Files.move(sourceObject, sourceObject.resolveSibling(newName));
			updateFromSource();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new OperationException("Failed to rename file! " + e.getMessage());
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.Container#delete(IOperationListener)
	 */
	@Override
	public synchronized void delete(IOperationListener listener) throws OperationException
	{
		try
		{
			Files.deleteIfExists(sourceObject);
			
			// file is obsolete after delete, so remove from parent.
			if (parent != null)
			{
				parent.remove(this);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new OperationException("Failed to delete file! " + e.getMessage());
		}
	}
	
	/**
	 * Upload the container to the server.<br />
	 * This should just call the one in {@link CSP}.
	 * 
	 * @param parent
	 *            The remote folder to upload to. Must pass a {@link RemoteFolder} with the path initialised in it.
	 * @param overwrite
	 *            Whether to overwrite existing container on the server or not.
	 * @param listener
	 *            Object listening to the changes in the transfer state.
	 * @return the upload job
	 * @throws TransferException
	 *             A problem occurred during the transfer of the container.
	 * @throws OperationException
	 */
	public synchronized UploadJob<?, ?> upload(RemoteFolder<?> parent, boolean overwrite, ITransferProgressListener listener)
			throws TransferException, OperationException
	{
		return parent.getCsp().upload(this, parent, overwrite, listener);
	}
	
	/**
	 * @return the remoteMapping
	 */
	public RemoteFile<?> getRemoteMapping()
	{
		return remoteMapping;
	}
	
	/**
	 * @param remoteMapping
	 *            the remoteMapping to set
	 */
	public void setRemoteMapping(RemoteFile<?> remoteMapping)
	{
		this.remoteMapping = remoteMapping;
	}
	
	@Override
	public CSP<Path, ?, ?> getCsp()
	{
		throw new UnsupportedOperationException("DO NOT USE!");
	}
	
	@Override
	public void setCsp(CSP<Path, ?, ?> csp)
	{
		throw new UnsupportedOperationException("DO NOT USE!");
	}
}
