/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.container.local/LocalFolder.java
 *
 *			Modified: 27-Mar-2014 (16:12:44)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.local;


import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.Folder;
import com.yagasoft.overcast.base.container.FolderHelper;
import com.yagasoft.overcast.base.container.FolderHelper.TreeCopier;
import com.yagasoft.overcast.base.container.FolderHelper.TreeDeleter;
import com.yagasoft.overcast.base.container.FolderHelper.TreeMover;
import com.yagasoft.overcast.base.container.operation.IOperationListener;
import com.yagasoft.overcast.base.container.operation.Operation;
import com.yagasoft.overcast.base.container.operation.OperationState;
import com.yagasoft.overcast.base.container.remote.RemoteFolder;
import com.yagasoft.overcast.base.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.base.container.transfer.UploadJob;
import com.yagasoft.overcast.base.csp.CSP;
import com.yagasoft.overcast.exception.AccessException;
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.OperationException;
import com.yagasoft.overcast.exception.TransferException;


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
	{}

	/**
	 * Instantiates a new local folder.
	 *
	 * @param file
	 *            Java library File object.
	 * @throws OperationException
	 */
	public LocalFolder(Path file) throws OperationException
	{
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
		addOperationListener(listener, Operation.CREATE);

		try
		{
			Files.createDirectories(Paths.get(parentPath, name));
			notifyOperationListeners(Operation.CREATE, OperationState.COMPLETED, 1.0f);
		}
		catch (IOException e)
		{
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
		// if the Java library says the folder doesn't exist, and at same time it says the folder doesn't 'not exist', then ...
		// obviously a problem.
		if ( !Files.exists(sourceObject) && !Files.notExists(sourceObject))
		{
			throw new AccessException("Can't determine if folder exists or not!");
		}

		return Files.exists(sourceObject);
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#buildTree(int)
	 */
	@Override
	public synchronized void buildTree(int numberOfLevels) throws OperationException
	{
		// stop at the required depth.
		if (numberOfLevels < 0)
		{
			return;
		}

		ArrayList<Path> paths = new ArrayList<Path>();		// will be used to store children read from disk.
		ArrayList<String> pathsString = new ArrayList<String>();	// will be used to filter children.

		// read children of the folder from the disk.
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceObject))
		{
			for (Path file : stream)
			{
				paths.add(file);
				pathsString.add(file.toAbsolutePath().toString());
			}
		}
		catch (IOException | DirectoryIteratorException e)
		{
			e.printStackTrace();
			throw new OperationException("Couldn't build tree! " + e.getMessage());
		}

		// filter!
		removeObsolete(pathsString, true);

//		HashMap<String, Folder<?>> newFolders = new HashMap<String, Folder<?>>();
//		HashMap<String, File<?>> newFiles = new HashMap<String, File<?>>();

		// add new files and folders to this folder's list, and build recursively if required.
		for (Path path : paths)
		{
//			if ( !folders.containsKey(path.toString()))
//			{
//				if ( !files.containsKey(path.toString()))
//				{
			if (Files.isDirectory(path))
			{
				LocalFolder folder = new LocalFolder(path);
				folder.buildTree(numberOfLevels - 1);		// build recursively.
				folders.put(folder.id, folder);
				folder.parent = this;
				Logger.newEntry("Folder: " + folder.getPath());
			}
			else
			{
				LocalFile file = new LocalFile(path);
				files.put(file.getId(), file);
				file.setParent(this);
				Logger.newEntry("File: " + file.getPath());
			}
//				}
//				else
//				{
//					newFiles.put(path.toString(), files.get(path.toString()));
//				}
//			}
//			else
//			{
//				Folder<?> folder = folders.get(path.toString());
//				folder.buildTree(numberOfLevels - 1);
//				newFolders.put(folder.id, folder);
//			}
		}
//
//		folders = newFolders;
//		files = newFiles;

//		for (Container<?> container : getChildrenArray())
//		{
//			System.out.println(container.getPath());
//		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#calculateSize()
	 */
	@Override
	public synchronized long calculateSize() throws OperationException
	{
		try
		{
			size = FolderHelper.getSize(path);		// this will calculate the whole folder size, including sub-folders.
		}
		catch (IOException e)
		{
			size = 0;
			e.printStackTrace();
			throw new OperationException("Couldn't determine size! " + e.getMessage());
		}

		return size;
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#updateInfo(boolean, boolean)
	 */
	@Override
	public synchronized void updateInfo(boolean folderContents, boolean recursively)
	{
		try
		{
			updateFromSource(folderContents, recursively);		// updating the info locally costs nothing, so do it automatically.
		}
		catch (OperationException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#updateFromSource(boolean, boolean)
	 */
	@Override
	public synchronized void updateFromSource(boolean folderContents, boolean recursively) throws OperationException
	{
		// NOTE: re-write this method!!! <<<<<<<<<<<<<<<<<<<<<<<<<<

		if (folderContents)
		{
			buildTree(recursively);
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

		generateId();
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#copy(com.yagasoft.overcast.base.container.Folder, boolean, IOperationListener)
	 */
	@Override
	public synchronized LocalFolder copy(Folder<?> destination, boolean overwrite, IOperationListener listener)
			throws OperationException
	{
		// call Oracle's copier.
		TreeCopier treeCopier = new TreeCopier(sourceObject, (Path) destination.getSourceObject(), !overwrite, true);

		try
		{
			Files.walkFileTree(sourceObject, treeCopier);
			return new LocalFolder(((Path) destination.getSourceObject()).resolve(sourceObject.getFileName()));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new OperationException("Couldn't copy folder! " + e.getMessage());
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#move(com.yagasoft.overcast.base.container.Folder, boolean, IOperationListener)
	 */
	@Override
	public synchronized void move(Folder<?> destination, boolean overwrite, IOperationListener listener)
			throws OperationException
	{
		// call my modification to Oracle's copier.
		TreeMover treeMover = new TreeMover(sourceObject, (Path) destination.getSourceObject(), !overwrite);

		try
		{
			Files.walkFileTree(sourceObject, treeMover);
			sourceObject = ((Path) destination.getSourceObject()).resolve(sourceObject.getFileName());
			updateFromSource();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new OperationException("Couldn't move folder! " + e.getMessage());
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
			throw new OperationException("Couldn't rename folder! " + e.getMessage());
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#delete(IOperationListener)
	 */
	@Override
	public synchronized void delete(IOperationListener listener) throws OperationException
	{
		TreeDeleter treeDeleter = new TreeDeleter();

		try
		{
			Files.walkFileTree(sourceObject, treeDeleter);		// delete the content first recursively (must!).

			// folder is obsolete after delete, so remove from parent.
			if (parent != null)
			{
				parent.remove(this);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new OperationException("Couldn't delete folder! " + e.getMessage());
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
		try
		{
			return localFreeSpace = Files.getFileStore(sourceObject.getRoot()).getUnallocatedSpace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new OperationException("Couldn't determine free space! " + e.getMessage());
		}
	}

	/**
	 * Not supported; DO NOT use!<br />
	 * Use {@link #updateInfo(boolean, boolean)} instead.
	 */
	@Override
	public void updateInfo()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Not supported; DO NOT use!<br />
	 * Use {@link #updateFromSource(boolean, boolean)} instead.
	 */
	@Override
	public void updateFromSource()
	{
		throw new UnsupportedOperationException();
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

}
