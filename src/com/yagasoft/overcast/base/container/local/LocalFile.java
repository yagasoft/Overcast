/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.local/LocalFile.java
 *
 *			Modified: 20-Jun-2014 (20:50:20)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.local;


import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.Container;
import com.yagasoft.overcast.base.container.File;
import com.yagasoft.overcast.base.container.Folder;
import com.yagasoft.overcast.base.container.remote.RemoteFile;
import com.yagasoft.overcast.base.container.remote.RemoteFolder;
import com.yagasoft.overcast.base.container.transfer.UploadJob;
import com.yagasoft.overcast.base.container.transfer.event.ITransferProgressListener;
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
		try
		{
			sourceObject = file;
			updateFromSource();
		}
		catch (OperationException e)
		{
			e.printStackTrace();
		}		// updating the info locally costs nothing, so do it automatically.
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
		Logger.info("OVERCAST: LOCALFILE: checking file existence: " + path);

		// if the Java library says the file doesn't exist, and at same time it says the file doesn't 'not exist', then ...
		// obviously a problem.
		if ( !Files.exists(sourceObject) && !Files.notExists(sourceObject))
		{
			Logger.error("OVERCAST: LOCALFILE: failed to determine if file exists or not: " + path);
			throw new AccessException("Can't determine if file exists or not!");
		}

		return Files.exists(sourceObject);
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#isLocal()
	 */
	@Override
	public boolean isLocal()
	{
		return true;
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#updateInfo()
	 */
	@Override
	public synchronized void updateInfo()
	{
		if (name == null)
		{
			name = "";
		}

		path = (((parent == null) || parent.getPath().equals("/")) ? "/" : (parent.getPath() + "/")) + name;
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

		parent = new LocalFolder(sourceObject.getParent());

		try
		{
			size = Files.size(sourceObject);
			date = Files.getLastModifiedTime(sourceObject).toMillis();
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
	 * @see com.yagasoft.overcast.base.container.Container#copyProcess(com.yagasoft.overcast.base.container.Folder)
	 */
	@Override
	protected Container<?> copyProcess(Folder<?> destination)
			throws OperationException
	{
		try
		{
			return new LocalFile(Files.copy(
					sourceObject
					// get destination path, and add to it this file's name to form complete path.
					, ((Path) destination.getSourceObject()).resolve(sourceObject.getFileName())
					, new CopyOption[] { COPY_ATTRIBUTES }));
		}
		catch (IOException e)
		{
			throw new OperationException(e.getMessage());
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#moveProcess(com.yagasoft.overcast.base.container.Folder)
	 */
	@Override
	protected Path moveProcess(Folder<?> destination)
			throws OperationException
	{
		try
		{
			return Files.move(
					sourceObject
					, ((Path) destination.getSourceObject()).resolve(sourceObject.getFileName())
					, new CopyOption[0]);
		}
		catch (IOException e)
		{
			throw new OperationException(e.getMessage());
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#renameProcess(java.lang.String)
	 */
	@Override
	protected Path renameProcess(String newName) throws OperationException
	{
		try
		{
			return Files.move(sourceObject, sourceObject.resolveSibling(newName));
		}
		catch (IOException e)
		{
			throw new OperationException(e.getMessage());
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#deleteProcess()
	 */
	@Override
	public void deleteProcess() throws OperationException
	{
		try
		{
			Files.deleteIfExists(sourceObject);
		}
		catch (IOException e)
		{
			throw new OperationException(e.getMessage());
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

	@Override
	public void setPath(String value)
	{
		path = value;
		setPathPrefix("");
	}
}
