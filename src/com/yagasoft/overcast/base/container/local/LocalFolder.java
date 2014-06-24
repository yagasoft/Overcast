/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.local/LocalFolder.java
 *
 *			Modified: 20-Jun-2014 (20:50:41)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.local;


import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.Container;
import com.yagasoft.overcast.base.container.Folder;
import com.yagasoft.overcast.base.container.local.FolderHelper.TreeCopier;
import com.yagasoft.overcast.base.container.local.FolderHelper.TreeDeleter;
import com.yagasoft.overcast.base.container.local.FolderHelper.TreeMover;
import com.yagasoft.overcast.base.container.operation.IOperationListener;
import com.yagasoft.overcast.base.container.operation.Operation;
import com.yagasoft.overcast.base.container.operation.OperationState;
import com.yagasoft.overcast.base.container.remote.RemoteFolder;
import com.yagasoft.overcast.base.container.transfer.UploadJob;
import com.yagasoft.overcast.base.container.transfer.event.ITransferProgressListener;
import com.yagasoft.overcast.base.csp.CSP;
import com.yagasoft.overcast.base.csp.LocalCSP;
import com.yagasoft.overcast.exception.AccessException;
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.OperationException;
import com.yagasoft.overcast.exception.TransferException;


// TODO implement a local factory! (stop being lazy!!!)

/**
 * A class representing folders stored locally.
 */
public class LocalFolder extends Folder<Path>
{

	/** The {@link RemoteFolder} corresponding to this local folder if applicable. */
	protected RemoteFolder<?>	remoteMapping;
	protected long				localFreeSpace;

	/**
	 * Instantiates a new local folder.
	 */
	public LocalFolder()
	{
		setExecutor(Executors.newFixedThreadPool(10));
		csp = new LocalCSP();
	}

	/**
	 * Instantiates a new local folder.
	 *
	 * @param file
	 *            Java library File object.
	 * @throws OperationException
	 */
	public LocalFolder(Path file) throws OperationException
	{
		this();
		sourceObject = file;
		updateFromSource(false, false);		// updating the info locally costs nothing, so do it automatically.
	}

	/**
	 * Instantiates a new local folder.
	 *
	 * @param path
	 *            Path to the folder.
	 * @throws OperationException
	 */
	public LocalFolder(String path) throws OperationException
	{
		this(Paths.get(path));		// get the folder object and pass it to the other constructor.
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
	 * @see com.yagasoft.overcast.container.Folder#create(Folder<?>, IOperationListener)
	 */
	@Override
	public synchronized void create(Folder<?> parent, IOperationListener listener) throws CreationException
	{
		create(parent.getPath(), listener);		// extract path and call overloaded string function.
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#create(java.lang.String, IOperationListener)
	 */
	@Override
	public synchronized void create(String parentPath, IOperationListener listener) throws CreationException
	{
		Logger.info("OVERCAST: LOCALFOLDER: creating folder from path: " + parentPath + "/" + name);

		addOperationListener(listener, Operation.CREATE);

		try
		{
			Files.createDirectories(Paths.get(parentPath, name));
			updateFromSource();
			notifyOperationListeners(Operation.CREATE, OperationState.COMPLETED, 1.0f);

			Logger.info("OVERCAST: LOCALFOLDER: finished creating folder: " + path);
		}
		catch (IOException | OperationException e)
		{
			Logger.error("OVERCAST: LOCALFOLDER: failed to create folder: " + parentPath + "/" + name);
			Logger.except(e);
			e.printStackTrace();

			notifyOperationListeners(Operation.CREATE, OperationState.FAILED, 0f);
			throw new CreationException("Couldn't create folder! " + e.getMessage());
		}
		finally
		{
			clearOperationListeners(Operation.CREATE);
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#isExist()
	 */
	@Override
	public synchronized boolean isExist() throws AccessException
	{
		Logger.info("OVERCAST: LOCALFOLDER: checking existence: " + path);

		// if the Java library says the folder doesn't exist, and at same time it says the folder doesn't 'not exist', then ...
		// obviously a problem.
		if ( !Files.exists(sourceObject) && !Files.notExists(sourceObject))
		{
			Logger.error("OVERCAST: LOCALFOLDER: failed to determine if folder exists or not: " + path);
			throw new AccessException("Can't determine if folder exists or not!");
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
	 * @see com.yagasoft.overcast.base.container.Folder#buildTreeProcess(int, java.util.List)
	 */
	@Override
	public synchronized void buildTreeProcess(final int numberOfLevels, List<Container<?>> childrenArray)
			throws OperationException
	{
		ArrayList<Path> paths = new ArrayList<Path>();		// will be used to store children read from disk.
		ArrayList<String> pathsString = new ArrayList<String>();	// will be used to filter children.

		// read children of the folder from the disk.
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceObject))
		{
			stream.forEach(file ->
			{
				paths.add(file);
				pathsString.add(file.toAbsolutePath().toString());
			});
		}
		catch (IOException | DirectoryIteratorException e)
		{
			throw new OperationException(e.getMessage());
		}

		// filter!
		removeObsolete(pathsString, true);

//		HashMap<String, Folder<?>> newFolders = new HashMap<String, Folder<?>>();
		// HashMap<String, File<?>> newFiles = new HashMap<String, File<?>>();

		// add new files and folders to this folder's list, and build recursively if required.
		paths.stream()
				.forEach(path ->
				{
					// if ( !folders.containsKey(path.toString()))
					// {
					// if ( !files.containsKey(path.toString()))
					// {
						try
						{
							if (Files.isDirectory(path))
							{
								childrenArray.add(new LocalFolder(path));
							}
							else
							{
								childrenArray.add(new LocalFile(path));
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						// }
						// else
						// {
						// newFiles.put(path.toString(), files.get(path.toString()));
						// }
						// }
						// else
						// {
						// Folder<?> folder = folders.get(path.toString());
						// folder.buildTree(numberOfLevels - 1);
						// newFolders.put(folder.id, folder);
						// }
					});

		//
		// folders = newFolders;
		// files = newFiles;

//		for (Container<?> container : getChildrenArray())
		// {
		// System.out.println(container.getPath());
		// }
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#calculateSize()
	 */
	@Override
	public synchronized long calculateSize() throws OperationException
	{
		Logger.info("OVERCAST: LOCALFOLDER: calculating size: " + path);

		try
		{
			size = FolderHelper.getSize(path);		// this will calculate the whole folder size, including sub-folders.

			Logger.info("OVERCAST: LOCALFOLDER: finished calc size: " + path);
		}
		catch (IOException e)
		{
			Logger.error("OVERCAST: LOCALFOLDER: failed to calc size: " + path);
			Logger.except(e);
			e.printStackTrace();

			size = 0;
			throw new OperationException("Couldn't determine size! " + e.getMessage());
		}

		return size;
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#updateInfo()
	 */
	@Override
	public synchronized void updateInfo()
	{
		if (name == null)
		{
			name = "";
		}

		path = (((parent == null) || parent.getPath().equals("/")) ? "/" : (parent.getPath() + "/")) + name;
		
		generateId();
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#updateFromSource(boolean, boolean)
	 */
	@Override
	public synchronized void updateFromSource(boolean folderContents, boolean recursively) throws OperationException
	{
		// TODO re-write this method

		if (folderContents)
		{
			buildTree(false);
		}

		name = sourceObject.getFileName().toString();
		path = sourceObject.toAbsolutePath().toString();
		localFreeSpace = calculateLocalFreeSpace();

		String parentString = sourceObject.getParent().toString();

		if (parentString.equals(sourceObject.getRoot().toString()))
		{
			parent = new LocalFolder();
			parent.setName("root");
		}
		else
		{
			parent = new LocalFolder(parentString);
		}


		try
		{
			date = Files.getLastModifiedTime(sourceObject).toMillis();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		updateInfo();
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#updateFromSource()
	 */
	@Override
	public void updateFromSource() throws OperationException
	{
		updateFromSource(true, false);
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
			// call Oracle's copier.
			TreeCopier treeCopier = new TreeCopier(sourceObject, (Path) destination.getSourceObject(), true, true);
			Files.walkFileTree(sourceObject, treeCopier);
			return new LocalFolder(((Path) destination.getSourceObject()).resolve(sourceObject.getFileName()));
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
			// call my modification to Oracle's copier.
			TreeMover treeMover = new TreeMover(sourceObject, (Path) destination.getSourceObject(), true);
			Files.walkFileTree(sourceObject, treeMover);
			return ((Path) destination.getSourceObject()).resolve(sourceObject.getFileName());
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
			TreeDeleter treeDeleter = new TreeDeleter();
			Files.walkFileTree(sourceObject, treeDeleter);		// delete the content first recursively (must!).
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
	 * @return the upload jobs
	 * @throws TransferException
	 *             A problem occurred during the transfer of the container.
	 * @throws CreationException
	 * @throws OperationException
	 */
	public synchronized UploadJob<?, ?>[] upload(RemoteFolder<?> parent, boolean overwrite, ITransferProgressListener listener)
			throws TransferException, OperationException, CreationException
	{
		return parent.getCsp().upload(this, parent, overwrite, listener);
	}

	/**
	 * Calculate local free space available on the local disk (the one the root resides on).
	 *
	 * @return the free space in bytes
	 * @throws OperationException
	 */
	public synchronized long calculateLocalFreeSpace() throws OperationException
	{
		Logger.info("OVERCAST: LOCALFOLDER: calculating local free space");

		try
		{
			return localFreeSpace = Files.getFileStore(sourceObject.getRoot()).getUnallocatedSpace();
		}
		catch (IOException e)
		{
			Logger.error("OVERCAST: LOCALFOLDER: failed to calculate local free space");
			Logger.except(e);
			e.printStackTrace();

			throw new OperationException("Couldn't determine free space! " + e.getMessage());
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	// ======================================================================================

	/**
	 * @return the remoteMapping
	 */
	public RemoteFolder<?> getRemoteMapping()
	{
		return remoteMapping;
	}

	/**
	 * @param remoteMapping
	 *            the remoteMapping to set
	 */
	public void setRemoteMapping(RemoteFolder<?> remoteMapping)
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

	/**
	 * @return the localFreeSpace
	 * @throws OperationException
	 */
	public long getLocalFreeSpace() throws OperationException
	{
		if (localFreeSpace == 0)
		{
			calculateLocalFreeSpace();
		}

		return localFreeSpace;
	}

	/**
	 * @param localFreeSpace
	 *            the localFreeSpace to set
	 */
	public void setLocalFreeSpace(long localFreeSpace)
	{
		this.localFreeSpace = localFreeSpace;
	}

	// ======================================================================================
	// #endregion Getters and setters.
	// //////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected Path createProcess(Folder<?> parent) throws CreationException
	{
		return null;
	}

	@Override
	public void setPath(String value)
	{
		path = value;
		setPathPrefix("");
	}
}
