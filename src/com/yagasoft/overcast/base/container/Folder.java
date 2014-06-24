/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container/Folder.java
 *
 *			Modified: 28-May-2014 (00:05:30)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.operation.IOperationListener;
import com.yagasoft.overcast.base.container.operation.Operation;
import com.yagasoft.overcast.base.container.operation.OperationState;
import com.yagasoft.overcast.base.container.remote.RemoteFolder;
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.OperationException;


/**
 * Class representing the commonalities between all folder types (remote/local).
 *
 * @param <T>
 *            the type of the folder in the original API of the CSP.
 */
@SuppressWarnings("unchecked")
public abstract class Folder<T> extends Container<T>
{

	/** Folders inside this folder mapped by ID (tree implementation). */
	protected Map<String, Folder<?>>	folders		= new HashMap<String, Folder<?>>();

	/** Files inside this folder mapped by ID. */
	protected Map<String, File<?>>		files		= new HashMap<String, File<?>>();

	/** Thread executor to be used to load sub-folders in the tree. */
	protected static ExecutorService	executor	= Executors.newCachedThreadPool();

	/**
	 * Number of slots to be used to load folder tree, fixed and low to reduce the load on the server; we don't want to be
	 * throttled!
	 */
	protected static Semaphore			slots		= new Semaphore(2);

	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Create folder.
	// ======================================================================================

	/**
	 * Creates the folder at the source with the name set as a field and the parent path passed.
	 *
	 * @param parentPath
	 *            Parent path.
	 * @param listener
	 *            the listener
	 * @throws CreationException
	 *             problem with creating the folder
	 */
	@SuppressWarnings("rawtypes")
	public void create(String parentPath, IOperationListener listener) throws CreationException
	{
		Logger.info(csp.getName() + ": OVERCAST: FOLDER: CREATING from path: " + parentPath + "/" + name);

		// split the parent path into nodes.
		ArrayList<String> splitPath = new ArrayList<String>(Arrays.asList(parentPath.split("/")));

		// remove the first node as it's the root
		if ( !splitPath.isEmpty() && splitPath.get(0).equals(""))
		{
			splitPath.remove(0);
		}

		// start from the root.
		RemoteFolder parent = csp.getRemoteFileTree();

		// if there're sub-folders in the path
		if ( !splitPath.isEmpty())
		{
			List<Container<?>> result;

			// search for the first sub-folder.
			result = parent.searchByName(splitPath.get(0), false, false);

			// if it's found, and there're more sub-folders ...
			// (if it's found but not sub-folders, then it's the parent we want to create in)
			while ( !result.isEmpty() && !splitPath.isEmpty())
			{
				// this it the intended parent for now ...
				parent = (RemoteFolder) result.get(0);
				splitPath.remove(0);		// don't need it anymore in the node's list.

				// more sub-folders?
				if ( !splitPath.isEmpty())
				{
					result = parent.searchByName(splitPath.get(0), false, false);
				}
				else
				{	// no more sub-folders, this is the parent for now.
					break;
				}
			}
		}

		// couldn't find a node in the first iteration, so start creating the folders in the rest of the path.
		while ( !splitPath.isEmpty())
		{
			RemoteFolder tempFolder = csp.getAbstractFactory().createFolder();
			tempFolder.setName(splitPath.remove(0));
			tempFolder.create(parent, listener);
			parent = tempFolder;		// new parent is the newly created folder.

			Logger.info(csp.getName() + ": OVERCAST: FOLDER: CREATED mid folder: " + parent.path);
		}

		// done with creating/traversing the path, now search if this folder exists in the last node ...
		List<Container<?>> result = parent.searchByName(name, false, false);

		// ... if so, then it already exists.
		if ( !result.isEmpty() && result.get(0).isFolder())
		{
			Logger.error(csp.getName() + ": OVERCAST: FOLDER: CREATING nodes to reach desired folder: " + parentPath + "/" + name);

			throw new CreationException("Folder already exists!");
		}
		else
		{
			create(parent, listener);		// create the folder in the reached parent.
		}
	}

	/**
	 * Creates the folder at the source with the info set (class attributes).
	 *
	 * @param parent
	 *            Parent folder to create in.
	 * @param listener
	 *            the listener
	 * @throws CreationException
	 *             problem with creating the folder
	 */
	public synchronized void create(Folder<?> parent, IOperationListener listener) throws CreationException
	{
		try
		{
			initCreate(parent, listener);
			setSourceObject(createProcess(parent));
			postCreate(parent);
		}
		catch (CreationException e)
		{
			Logger.error(csp.getName() + ": OVERCAST: FOLDER: FAILED in creating folder: " + parent.getPath() + "/" + name);
			Logger.except(e);
			e.printStackTrace();

			throw new CreationException("Couldn't create folder! " + e.getMessage());
		}
		finally
		{
			removeTempOperationListener(listener, Operation.CREATE);
		}
	}

	/**
	 * Initialises the folder creation process.
	 */
	protected void initCreate(Folder<?> parent, IOperationListener listener) throws CreationException
	{
		Logger.info(csp.getName() + ": OVERCAST: FOLDER: CREATING folder: " + parent.getPath() + "/" + name);

		addTempOperationListener(listener, Operation.CREATE);

		// check if the folder exists in the parent ...
		List<Container<?>> result = parent.searchByName(name, false, false);

		// if it exists, problem!
		if ((result.size() >= 1) && result.get(0).isFolder())
		{
			Logger.error(csp.getName() + ": OVERCAST: FOLDER: FAILED in creating folder -- already exists: " + parent.getPath() + "/" + name);
			throw new CreationException("Folder already Exists!");
		}
	}

	/**
	 * Process of creating the folder. It should create the folder at the CSP,
	 * and return an object representing the created folder in the type used by the CSP.
	 *
	 * @param parent
	 *            Parent.
	 * @return Source object of the created folder.
	 * @throws CreationException
	 *             the creation exception
	 */
	protected abstract T createProcess(Folder<?> parent) throws CreationException;

	/**
	 * Post folder creation.
	 */
	protected void postCreate(Folder<?> parent) throws CreationException
	{
		parent.add(this);
		notifyOperationListeners(Operation.CREATE, OperationState.COMPLETED, 1.0f);

		Logger.info(csp.getName() + ": OVERCAST: FOLDER: FINISHED creating folder: " + path);
	}

	// ======================================================================================
	// #endregion Create folder.
	// //////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Adds the container passed to the list of containers in this folder.
	 *
	 * @param container
	 *            Container to add.
	 */
	public void add(Container<?> container)
	{
		if (container.isFolder())
		{
			folders.put(container.id, (Folder<?>) container);		// extract the ID of the folder and use it to map the folder passed.
		}
		else
		{
			files.put(container.id, (File<?>) container);
		}

		container.setParent(this);				// set the parent of the container passed as this folder.
		notifyOperationListeners(Operation.ADD, container);

		Logger.info(csp.getName() + ": OVERCAST: FOLDER: ADDED: " + container.path + ", to parent: " + path);
	}

	/**
	 * Removes the container from the list of containers in this folder.
	 *
	 * @param container
	 *            Container to remove.
	 */
	public void remove(Container<?> container)
	{
		// remove container, and if it existed, then remove the parent (this) pointer from it as well.
		if ((container.isFolder() && (folders.remove(container.id) != null))
				|| ( !container.isFolder() && (files.remove(container.id) != null)))
		{
			container.setParent(null);
			notifyOperationListeners(Operation.REMOVE, container);

			// the container is an orphan and not needed, so remove its listeners.
			container.clearAllListeners();

			Logger.info(csp.getName() + ": OVERCAST: FOLDER: REMOVED: " + container.path + ", from parent: " + path);
		}
	}

	/**
	 * Removes the file/folder from either list in this folder based on ID -- should search both lists.
	 *
	 * @param id
	 *            ID of the container to remove.
	 */
	public void remove(String id)
	{
		// try to remove from both lists, it will fail quietly if it doesn't exist in either.
		if ((folders.remove(id) != null) || (files.remove(id) != null))
		{
			Logger.info(csp.getName() + ": OVERCAST: FOLDER: REMOVED file/folder: " + id);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Tree operations.
	// ======================================================================================

	/**
	 * Builds the sub-tree of this folder, adding sub-folders and files to the map.<br />
	 * Should check the levels reached on each recursion.
	 *
	 * @param numberOfLevels
	 *            Depth to go to in sub-folders, with zero as this folder's contents only.
	 * @throws OperationException
	 *             the operation exception
	 */
	public synchronized void buildTree(int numberOfLevels) throws OperationException
	{
		Logger.info(csp.getName() + ": OVERCAST: FOLDER: PROCESSING folder: " + path);

		// no more levels to check.
		if (numberOfLevels < 0)
		{
			return;
		}

		ArrayList<Container<?>> childrenArray = new ArrayList<Container<?>>();

		try
		{
			initBuildTree();
			buildTreeProcess(numberOfLevels, childrenArray);
			postBuildTree(numberOfLevels, childrenArray);

			Logger.info(csp.getName() + ": OVERCAST: FOLDER: PROCESSED folder: " + path);
		}
		catch (OperationException e)
		{
			Logger.error(csp.getName() + ": OVERCAST: FOLDER: FAILED in building folder tree: " + path);
			Logger.except(e);
			e.printStackTrace();

			throw new OperationException("Failed to build tree! " + e.getMessage());
		}
	}

	/**
	 * Equivalent to {@link Folder#buildTree(int)} with Integer.MAX_VALUE passed if recursive, or passing zero if not.
	 *
	 * @param recursively
	 *            Recursively build this tree.
	 * @throws OperationException
	 *             the operation exception
	 */
	public void buildTree(boolean recursively) throws OperationException
	{
		if (recursively)
		{
			buildTree(Integer.MAX_VALUE);		// if recursive, then pass a very huge number as the number of levels to build.
		}
		else
		{
			buildTree(0);		// if not recursive, then build only the first level.
		}
	}

	/**
	 * Initialises the build tree process.
	 *
	 * @throws OperationException
	 *             the operation exception
	 */
	protected void initBuildTree() throws OperationException
	{
		Logger.info(csp.getName() + ": OVERCAST: FOLDER: BUILDING folder tree: " + path);

		// going to work on a branch, so grab a thread.
		try
		{
			slots.acquire();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Builds the tree. It should fetch the children list from the server, remove obsolete using member methods in here,
	 * create a container object using the factory methods from the CSP, and then add those to the childrenArray.
	 *
	 * @param numberOfLevels
	 *            Number of levels to build.
	 * @param childrenArray
	 *            Children array to contain the resulting containers.
	 * @throws OperationException
	 *             the operation exception
	 */
	protected abstract void buildTreeProcess(int numberOfLevels, List<Container<?>> childrenArray) throws OperationException;

	/**
	 * Post build tree.
	 *
	 * @param numberOfLevels
	 *            Number of levels.
	 * @param childrenArray
	 *            Children array.
	 * @throws OperationException
	 *             the operation exception
	 */
	protected void postBuildTree(final int numberOfLevels, List<Container<?>> childrenArray) throws OperationException
	{
		slots.release();
		
		// add the resulting children to this folder's list
		childrenArray.stream().forEach(container -> add(container));

		// use a service to 'join' threads and not return before finishing the whole tree build.
		CompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(executor);
		int jobs = 0;		// count the submissions to know what to wait for.

		for (final Folder<?> folder : getFoldersArray())
		{
			try
			{
				service.submit(() ->
				{
					try
					{
						folder.buildTree(numberOfLevels - 1);		// build recursively.
						return true;
					}
					catch (OperationException e)
					{
						e.printStackTrace();
						throw new RuntimeException(e.getMessage());
					}
				});

				jobs++;		// job submitted, count!
			}
			catch (RuntimeException e)
			{
				throw new OperationException(e.getMessage());
			}
		}

		// join threads ...
		try
		{
			for (; jobs > 0; jobs--)
			{
				service.take();
			}
		}
		catch (InterruptedException e1)
		{
			e1.printStackTrace();
		}
	}

	// ======================================================================================
	// #endregion Tree operations.
	// //////////////////////////////////////////////////////////////////////////////////////

	/**
	 * I chose to add it here and not in updateFromSource because it's an intensive operation that should be done manually only.
	 *
	 * @return Size in bytes.
	 * @throws OperationException
	 *             the operation exception
	 */
	public abstract long calculateSize() throws OperationException;

	/**
	 * @see com.yagasoft.overcast.base.container.Container#isFolder()
	 */
	@Override
	public boolean isFolder()
	{
		return true;		// this is a folder!
	}

	/**
	 * Update from where the folder resides. It updates the info of the folder from the source itself
	 * , and can be done recursively (tree). It refreshes the children list if 'contents' flag is true.
	 *
	 * @param folderContents
	 *            Update children list.
	 * @param recursively
	 *            Recursive or not.
	 * @throws OperationException
	 *             the operation exception
	 */
	public abstract void updateFromSource(boolean folderContents, boolean recursively) throws OperationException;

	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Searching.
	// ======================================================================================

	/**
	 * Checks if this file/folder exists in this folder (searches by ID), and whether to do it recursively.
	 *
	 * @param <S>
	 *            capture the type of the container passed to return it after searching is done.
	 * @param id
	 *            The ID of the container to look for.
	 * @param recursively
	 *            Recursively or not.
	 * @return The container
	 */
	public <S extends Container<?>> S searchById(String id, boolean recursively)
	{
		Logger.info(csp.getName() + ": OVERCAST: FOLDER: searching: " + id + ", in: " + path);

		try
		{
			// save some time if this folder was already loaded before.
			if (getChildrenList().isEmpty())
			{
				updateFromSource(true, false);
			}
		}
		catch (OperationException e)
		{
			Logger.error(csp.getName() + ": OVERCAST: FOLDER: SEARCH: FAILED to update from source " + id + ", searching from memory ...");
			Logger.except(e);
			e.printStackTrace();
		}

		// if the folders list contains the passed ID (mapped), return it.
		if (folders.containsKey(id))
		{
			Logger.info(csp.getName() + ": OVERCAST: FOLDER: SEARCH: FOUND: " + folders.get(id).path + ", in: " + path);
			return (S) folders.get(id);
		}
		else if (files.containsKey(id))
		{	// else, check the files list.
			Logger.info(csp.getName() + ": OVERCAST: FOLDER: SEARCH: FOUND: " + files.get(id).path + ", in: " + path);
			return (S) files.get(id);
		}
		else if (recursively)
		{	// else, if recursive search is wanted, check sub-folders.
			for (Folder<?> folder : folders.values())	// get the sub-folders from the list.
			{
				// search each by calling this method repeatedly for each.
				Container<?> container = folder.searchById(id, recursively);

				// if something was returned at any point, then return it and end.
				if (container != null)
				{
					return (S) container;
				}
			}
		}

		// nothing was found.
		return null;
	}

	/**
	 * Search by name.
	 *
	 * @param name
	 *            Name of the container to search for.
	 * @param recursively
	 *            Recursively or not.
	 * @return The container list found
	 */
	public List<Container<?>> searchByName(String name, boolean partial, boolean recursively)
	{
		Logger.info(csp.getName() + ": OVERCAST: FOLDER: searching " + name + " in " + path);

		try
		{
			// save some time if this folder was already loaded before.
			if (getChildrenList().isEmpty())
			{
				updateFromSource(true, false);
			}
		}
		catch (OperationException e)
		{
			Logger.error(csp.getName() + ": OVERCAST: FOLDER: SEARCH: FAILED to update from source " + id + ", searching from memory ...");
			Logger.except(e);
			e.printStackTrace();
		}

		List<Container<?>> result = new ArrayList<Container<?>>();

		// same as 'searchById', in THIS folder only ...
		result.addAll(getChildrenList().parallelStream()		// get all children as a stream
				.filter(container ->
				{
					if (partial)
					{
						// keep containers with a similar name
						return container.name.toLowerCase().contains(name.toLowerCase());
					}
					else
					{
						// keep only containers with a matching name
						return container.name.equalsIgnoreCase(name);
					}
				})
				.collect(Collectors.toList()));		// convert filtered result to an array

		if ( !result.isEmpty())
		{
			Logger.info(csp.getName() + ": OVERCAST: FOLDER: SEARCH: FOUND " + result.get(0) + " in " + path);
		}

		// search sub-folders
		if (recursively)
		{
			result.addAll(folders.values().parallelStream()		// get sub-folders as a stream
					// replace each folder with a stream containing its children that match the name
					.flatMap(folder -> folder.searchByName(name, partial, recursively).parallelStream())
					.collect(Collectors.toList()));	// convert to an array
		}

		return result;
	}

	// ======================================================================================
	// #endregion Searching.
	// //////////////////////////////////////////////////////////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Filtering.
	// ======================================================================================

	/**
	 * Removes the obsolete members from the list in this folder using the fresh list sent as an argument.
	 *
	 * @param ids
	 *            Fresh list of folders and files IDs.
	 * @param filter
	 *            Filter the sent list from already existing members in this folder (to allow faster parsing of new ones). This
	 *            removes the duplicates in the list SENT when compared to this folder's list.
	 */
	public void removeObsolete(List<String> ids, boolean filter)
	{
		Logger.info(csp.getName() + ": OVERCAST: FOLDER: REMOVING OBSOLETE containers: " + path);

		// combine lists in this folder (IDs only, not containers themselves).
		List<String> containers = new ArrayList<String>();
		containers.addAll(folders.keySet());
		containers.addAll(files.keySet());

		// go through the combined list.
		containers.stream()
				// keep only the containers in the list sent (not deleted on server)
				.filter(container -> !ids.contains(container))
				.forEach(container ->
				{
					// remove it.
						folders.remove(container);
						files.remove(container);

						Logger.info(csp.getName() + ": OVERCAST: FOLDER: REMOVED OBSOLETE: " + container);
					});

		// if it's required to filter (remove commons) the sent list as well ...
		if (filter)
		{
			// go through the combined list again, but this time just remove from the sent list that exist in this folder.
			containers.stream()
					.forEach(container ->
					{
						ids.remove(container);

						Logger.info(csp.getName() + ": OVERCAST: FOLDER: FILTERING existing: " + container);
					});
		}

		Logger.info(csp.getName() + ": OVERCAST: FOLDER: FINISHED removing OBSOLETE containers: " + path);
	}

	/**
	 * Removes the obsolete folders from the list in this folder using the fresh list sent as an argument.
	 *
	 * @param folderIds
	 *            Fresh list of folders IDs.
	 * @param filter
	 *            Filter the sent list from already existing members in this folder (to allow faster parsing of new ones). This
	 *            removes the duplicates in the list SENT when compared to this folder's list.
	 */
	public void removeObsoleteFolders(List<String> folderIds, boolean filter)
	{
		Logger.info(csp.getName() + ": OVERCAST: FOLDER: removing obsolete folders: " + path);

		List<String> foldersList = new ArrayList<String>();
		foldersList.addAll(folders.keySet());

		foldersList.stream()
				.filter(folder -> !folderIds.contains(folder))
				.forEach(folder ->
				{
					folders.remove(folder);

					Logger.info(csp.getName() + ": OVERCAST: FOLDER: REMOVED OBSOLETE: " + folder);
				});

		if (filter)
		{
			foldersList.stream()
					.forEach(folder ->
					{
						folderIds.remove(folder);

						Logger.info(csp.getName() + ": OVERCAST: FOLDER: FILTERING existing: " + folder);
					});
		}
	}

	/**
	 * Removes the obsolete files from the list in this folder using the fresh list sent as an argument.
	 *
	 * @param fileIds
	 *            Fresh list of files IDs.
	 * @param filter
	 *            Filter the sent list from already existing members in this folder (to allow faster parsing of new ones). This
	 *            removes the duplicates in the list SENT when compared to this folder's list.
	 */
	public void removeObsoleteFiles(List<String> fileIds, boolean filter)
	{
		Logger.info(csp.getName() + ": OVERCAST: FOLDER: removing obsolete files: " + path);

		List<String> filesList = new ArrayList<String>();
		filesList.addAll(folders.keySet());

		filesList.stream()
				.filter(file -> !fileIds.contains(file))
				.forEach(file ->
				{
					files.remove(file);

					Logger.info(csp.getName() + ": OVERCAST: FOLDER: REMOVED OBSOLETE: " + file);
				});

		if (filter)
		{
			filesList.stream()
					.forEach(file ->
					{
						fileIds.remove(file);

						Logger.info(csp.getName() + ": OVERCAST: FOLDER: FILTERING existing: " + file);
					});
		}
	}

	// ======================================================================================
	// #endregion Filtering.
	// //////////////////////////////////////////////////////////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Children listing.
	// ======================================================================================

	/**
	 * Gets the whole tree as a sequential list. First adds the children to a new list, and then goes through each folder and gets
	 * its tree and adds it recursively.
	 *
	 * @return the whole tree as a sequential list
	 */
	public List<Container<?>> getWholeTreeList()
	{
		// combine the lists in this folder.
		List<Container<?>> children = new ArrayList<Container<?>>();
		children.addAll(folders.values());
		children.addAll(files.values());

		// do it recursively.
		getFoldersList().stream()
				.forEach(folder -> children.addAll(folder.getWholeTreeList()));

		return children;
	}

	/**
	 * Gets the children as a list, including folders and files.
	 *
	 * @return the children list
	 */
	public List<Container<?>> getChildrenList()
	{
		List<Container<?>> children = new ArrayList<Container<?>>();
		children.addAll(folders.values());
		children.addAll(files.values());

		return children;
	}

	/**
	 * Gets the folders list only.
	 *
	 * @return the folders list
	 */
	public List<Folder<?>> getFoldersList()
	{
		return new ArrayList<Folder<?>>(folders.values());
	}

	/**
	 * Gets the files list only.
	 *
	 * @return the files list
	 */
	public List<File<?>> getFilesList()
	{
		return new ArrayList<File<?>>(files.values());
	}

	/**
	 * Gets the children as an array, including folders and files.
	 *
	 * @return the children array
	 */
	public Container<?>[] getChildrenArray()
	{
		List<Container<?>> children = new ArrayList<Container<?>>();
		children.addAll(folders.values());
		children.addAll(files.values());

		return children.toArray(new Container<?>[children.size()]);		// convert to array before returning.
	}

	/**
	 * Gets the folders array.
	 *
	 * @return the folders array
	 */
	public Folder<?>[] getFoldersArray()
	{
		return folders.values().toArray(new Folder<?>[folders.size()]);
	}

	/**
	 * Gets the files array.
	 *
	 * @return the files array
	 */
	public File<?>[] getFilesArray()
	{
		return files.values().toArray(new File<?>[files.size()]);
	}

	/**
	 * Gets the children iterator, including folders and files.
	 *
	 * @return the children iterator
	 */
	public Iterator<Container<?>> getChildrenIterator()
	{
		return getChildrenList().iterator();
	}

	/**
	 * Gets the folders iterator.
	 *
	 * @return the folders iterator
	 */
	public Iterator<Folder<?>> getFoldersIterator()
	{
		return getFoldersList().iterator();
	}

	/**
	 * Gets the files iterator.
	 *
	 * @return the files iterator
	 */
	public Iterator<File<?>> getFilesIterator()
	{
		return getFilesList().iterator();
	}

	// ======================================================================================
	// #endregion Children listing.
	// //////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @return the executor
	 */
	public static ExecutorService getExecutor()
	{
		return executor;
	}

	/**
	 * @param executor
	 *            the executor to set
	 */
	public static void setExecutor(ExecutorService executor)
	{
		Folder.executor = executor;
	}

}
