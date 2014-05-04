/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.local/LocalFolder.java
 *
 *			Modified: 22-Apr-2014 (15:24:00)
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
import java.util.concurrent.Semaphore;

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
	{
		setSemaphore(new Semaphore(10));
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
		Logger.info("creating folder from path: " + parentPath + "/" + name);

		addOperationListener(listener, Operation.CREATE);

		try
		{
			Files.createDirectories(Paths.get(parentPath, name));
			updateFromSource();
			notifyOperationListeners(Operation.CREATE, OperationState.COMPLETED, 1.0f);

			Logger.info("finished creating folder: " + path);
		}
		catch (IOException | OperationException e)
		{
			Logger.error("creating folder: " + parentPath + "/" + name);
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
		Logger.info("checking existence: " + path);

		// if the Java library says the folder doesn't exist, and at same time it says the folder doesn't 'not exist', then ...
		// obviously a problem.
		if ( !Files.exists(sourceObject) && !Files.notExists(sourceObject))
		{
			Logger.error("can't determine if folder exists or not: " + path);
			throw new AccessException("Can't determine if folder exists or not!");
		}

		return Files.exists(sourceObject);
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#buildTree(int)
	 */
	@Override
	public synchronized void buildTree(final int numberOfLevels) throws OperationException
	{
		// stop at the required depth.
		if (numberOfLevels < 0)
		{
			return;
		}

		Logger.info("building folder tree: " + path);

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
			Logger.error("reading children: " + path);
			Logger.except(e);
			e.printStackTrace();

			throw new OperationException("couldn't build tree! " + e.getMessage());
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
				final LocalFolder folder = new LocalFolder(path);

				try
				{
					new Thread(new Runnable()
					{

						@Override
						public void run()
						{
							try
							{
								slots.acquire();
								folder.buildTree(numberOfLevels - 1);		// build recursively.
								slots.release();
							}
							catch (InterruptedException | OperationException e)
							{
								e.printStackTrace();
							}
						}
					}).start();
				}
				catch (RuntimeException e)
				{
					Logger.error("building folder tree: " + folder.path);
					Logger.except(e);

					throw new OperationException(e.getMessage());
				}

				folders.put(folder.id, folder);
				folder.parent = this;

				Logger.info("found folder: " + folder.path);
			}
			else
			{
				LocalFile file = new LocalFile(path);
				files.put(file.getId(), file);
				file.setParent(this);

				Logger.info("found file: " + file.getPath());
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

		Logger.info("finished building tree: " + path);
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
		Logger.info("calculating size: " + path);

		try
		{
			size = FolderHelper.getSize(path);		// this will calculate the whole folder size, including sub-folders.

			Logger.info("finished calc size: " + path);
		}
		catch (IOException e)
		{
			Logger.error("calc size: " + path);
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

		path = ((parent == null || parent.getPath().equals("/")) ? "/" : (parent.getPath() + "/")) + name;
		
		Logger.info("updated info: " + path);
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Folder#updateFromSource(boolean, boolean)
	 */
	@Override
	public synchronized void updateFromSource(boolean folderContents, boolean recursively) throws OperationException
	{
		// TODO re-write this method

		Logger.info("updating info from source: " + path);

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

		generateId();

		Logger.info("finished updating info from source: " + path);
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
	 * @see com.yagasoft.overcast.base.container.Container#copy(com.yagasoft.overcast.base.container.Folder, boolean,
	 *      IOperationListener)
	 */
	@Override
	public synchronized LocalFolder copy(Folder<?> destination, boolean overwrite, IOperationListener listener)
			throws OperationException
	{
		Logger.info("copying folder: " + path);

		// call Oracle's copier.
		TreeCopier treeCopier = new TreeCopier(sourceObject, (Path) destination.getSourceObject(), !overwrite, true);

		try
		{
			Files.walkFileTree(sourceObject, treeCopier);
			Logger.info("finished copying to: " + destination.getPath());
			return new LocalFolder(((Path) destination.getSourceObject()).resolve(sourceObject.getFileName()));
		}
		catch (IOException e)
		{
			Logger.error("copying folder: " + path);
			Logger.except(e);
			e.printStackTrace();

			throw new OperationException("Couldn't copy folder! " + e.getMessage());
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#move(com.yagasoft.overcast.base.container.Folder, boolean,
	 *      IOperationListener)
	 */
	@Override
	public synchronized void move(Folder<?> destination, boolean overwrite, IOperationListener listener)
			throws OperationException
	{
		Logger.info("moving folder: " + path);

		// call my modification to Oracle's copier.
		TreeMover treeMover = new TreeMover(sourceObject, (Path) destination.getSourceObject(), !overwrite);

		try
		{
			Files.walkFileTree(sourceObject, treeMover);
			sourceObject = ((Path) destination.getSourceObject()).resolve(sourceObject.getFileName());
			updateFromSource();

			Logger.info("finished moving to: " + destination.getPath());
		}
		catch (IOException e)
		{
			Logger.error("moving folder: " + path);
			Logger.except(e);
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
		Logger.info("renaming folder: " + path);

		try
		{
			// renaming is effectively moving under a new name.
			sourceObject = Files.move(sourceObject, sourceObject.resolveSibling(newName));
			updateFromSource();

			Logger.info("finished renaming folder: " + path);
		}
		catch (IOException e)
		{
			Logger.error("renaming folder: " + path);
			Logger.except(e);
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
		Logger.info("deleting folder: " + path);

		TreeDeleter treeDeleter = new TreeDeleter();

		try
		{
			Files.walkFileTree(sourceObject, treeDeleter);		// delete the content first recursively (must!).

			// folder is obsolete after delete, so remove from parent.
			if (parent != null)
			{
				parent.remove(this);
			}

			Logger.info("finished deleting folder: " + path);
		}
		catch (IOException e)
		{
			Logger.error("deleting folder: " + path);
			Logger.except(e);
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
		Logger.info("calculating local free space");

		try
		{
			return localFreeSpace = Files.getFileStore(sourceObject.getRoot()).getUnallocatedSpace();
		}
		catch (IOException e)
		{
			Logger.error("calculating local free space");
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

}
