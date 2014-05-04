/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container/Folder.java
 *
 *			Modified: 04-May-2014 (15:07:03)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.content.Change;
import com.yagasoft.overcast.base.container.content.ChangeEvent;
import com.yagasoft.overcast.base.container.content.IContentListener;
import com.yagasoft.overcast.base.container.content.IContentManager;
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
public abstract class Folder<T> extends Container<T> implements IContentManager
{

	/** Folders inside this folder mapped by ID (tree implementation). */
	protected HashMap<String, Folder<?>>					folders				= new HashMap<String, Folder<?>>();

	/** Files inside this folder mapped by ID. */
	protected HashMap<String, File<?>>						files				= new HashMap<String, File<?>>();

	/** Number of slots to be used to load folder tree, which reduces the load on the server. */
	protected static Semaphore								slots				= new Semaphore(2);

	/** Listeners to the contents changes in this container. */
	protected HashMap<IContentListener, HashSet<Change>>	contentListeners	= new HashMap<IContentListener, HashSet<Change>>();

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
	public abstract void create(Folder<?> parent, IOperationListener listener) throws CreationException;

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
		Logger.info("creating folder from path: " + parentPath + "/" + name);

		// split the parent path into nodes.
		ArrayList<String> splitPath = new ArrayList<String>(Arrays.asList(parentPath.split("/")));

		// remove the first node as it's the root
		if (splitPath.get(0).equals(""))
		{
			splitPath.remove(0);
		}

		// start from the root.
		RemoteFolder parent = csp.getRemoteFileTree();

		// if there're sub-folders in the path
		if (splitPath.size() > 0)
		{
			Container[] result;

			// search for the first sub-folder.
			try
			{
				parent.buildTree(false);		// slows, but removes chance of errors.
				result = parent.searchByName(splitPath.get(0), false);
			}
			catch (OperationException e)
			{
				e.printStackTrace();
				Logger.error("refreshing folder in path: " + parent.getPath());

				notifyOperationListeners(Operation.CREATE, OperationState.FAILED, 0f);
				throw new CreationException("Can't refresh path.");
			}

			// if it's found, and there're more sub-folders ...
			// (if it's found but not sub-folders, then it's the parent we want to create in)
			while ((result.length > 0) && (splitPath.size() > 0))
			{
				// this it the intended parent for now ...
				parent = (RemoteFolder) result[0];
				splitPath.remove(0);		// don't need it anymore in the node's list.

				// more sub-folders?
				if (splitPath.size() > 0)
				{
					// check for the next node in the current parent.
					try
					{
						parent.buildTree(false);
						result = parent.searchByName(splitPath.get(0), false);
					}
					catch (OperationException e)
					{
						e.printStackTrace();
						Logger.error("refreshing folder in path: " + parent.getPath());

						notifyOperationListeners(Operation.CREATE, OperationState.FAILED, 0f);
						throw new CreationException("Can't refresh path.");
					}
				}
				else
				{	// no more sub-folders, this is the parent for now.
					break;
				}
			}
		}

		// couldn't find a node in the first iteration, so start creating the folders in the rest of the path.
		while (splitPath.size() > 0)
		{
			RemoteFolder tempFolder = csp.getAbstractFactory().createFolder();
			tempFolder.setName(splitPath.remove(0));
			tempFolder.create(parent, listener);
			parent = tempFolder;		// new parent is the newly created folder.

			Logger.info("created mid folder: " + parent.path);
		}

		// done with creating/traversing the path, now search if this folder exists in the last node ...
		Container<?>[] result = parent.searchByName(name, false);

		// ... if so, then it already exists.
		if (result.length > 0 && result[0].isFolder())
		{
			Logger.error("creating nodes to reach desired folder: " + parentPath + "/" + name);

			notifyOperationListeners(Operation.CREATE, OperationState.FAILED, 0f);
			throw new CreationException("Folder already exists!");
		}
		else
		{
			create(parent, listener);		// create the folder in the reached parent.
		}
	}

	/**
	 * Adds the folder passed to the list of folder in this folder.
	 *
	 * @param folder
	 *            Folder to add.
	 */
	public void add(Folder<?> folder)
	{
		folders.put(folder.id, folder);		// extract the ID of the folder and use it to map the folder passed.
		folder.setParent(this);				// set the parent of the folder passed as this folder.
		notifyContentListeners(Change.ADD, folder);

		Logger.info("added folder: " + folder.path + ", to parent: " + path);
	}

	/**
	 * Adds the file passed to the list of files in this folder.
	 *
	 * @param file
	 *            File to add.
	 */
	public void add(File<?> file)
	{
		files.put(file.id, file);
		file.setParent(this);
		notifyContentListeners(Change.ADD, file);

		Logger.info("added file: " + file.path + ", to parent: " + path);
	}

	/**
	 * Removes the folder from the list of folder in this folder.
	 *
	 * @param folder
	 *            Folder to remove.
	 */
	public void remove(Folder<?> folder)
	{
		// remove folder, and if it existed, then remove the parent (this) pointer from it as well.
		if (folders.remove(folder.id) != null)
		{
			folder.setParent(null);
			notifyContentListeners(Change.REMOVE, folder);

			// the folder is an orphan and not needed, so remove its listeners.
			folder.clearAllListeners();
		}

		Logger.info("removed folder: " + folder.path + ", from parent: " + path);
	}

	/**
	 * Removes the file from the list of files in this folder.
	 *
	 * @param file
	 *            File to remove.
	 */
	public void remove(File<?> file)
	{
		// remove file, and if it existed, then remove the parent (this) pointer from it as well.
		if (files.remove(file.id) != null)
		{
			file.setParent(null);
			notifyContentListeners(Change.REMOVE, file);

			// the file is an orphan and not needed, so remove its listeners.
			file.clearAllListeners();
		}

		Logger.info("removed file: " + file.path + ", from parent: " + path);
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
		Folder<?> folder = folders.get(id);
		File<?> file = files.get(id);

		if (folder != null)
		{
			remove(folder);
		}
		else if (file != null)
		{
			remove(file);
		}

		Logger.info("removed file/folder: " + id);
	}

	/**
	 * Builds the sub-tree of this folder, adding sub-folders and files to the map.<br />
	 * Should check the levels reached on each recursion.
	 *
	 * @param numberOfLevels
	 *            Depth to go to in sub-folders, with zero as this folder's contents only.
	 * @throws OperationException
	 *             the operation exception
	 */
	public abstract void buildTree(int numberOfLevels) throws OperationException;

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
	// #region Events.
	// ======================================================================================

	/**
	 * @see com.yagasoft.overcast.base.container.content.IContentManager#addContentListener(com.yagasoft.overcast.base.container.content.IContentListener)
	 */
	@Override
	public void addContentListener(IContentListener listener)
	{
		addContentListener(listener, Change.ADD);
		addContentListener(listener, Change.REMOVE);
	}

	/**
	 * @see com.yagasoft.overcast.base.container.content.IContentManager#addContentListener(com.yagasoft.overcast.base.container.content.IContentListener,
	 *      com.yagasoft.overcast.base.container.content.Change)
	 */
	@Override
	public void addContentListener(IContentListener listener, Change change)
	{
		if ( !contentListeners.containsKey(listener))
		{
			contentListeners.put(listener, new HashSet<Change>());
		}

		contentListeners.get(listener).add(change);
	}

	/**
	 * @see com.yagasoft.overcast.base.container.content.IContentManager#notifyContentListeners(com.yagasoft.overcast.base.container.content.Change,
	 *      com.yagasoft.overcast.base.container.Container)
	 */
	@Override
	public void notifyContentListeners(Change change, Container<?> object)
	{
		// go through the listeners' list and notify whoever is concerned with this change.
		for (IContentListener listener : contentListeners.keySet())
		{
			if (contentListeners.get(listener).contains(change))
			{
				listener.contentsChanged(new ChangeEvent(this, change, object));
			}
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.content.IContentManager#removeContentListener(com.yagasoft.overcast.base.container.content.IContentListener)
	 */
	@Override
	public void removeContentListener(IContentListener listener)
	{
		contentListeners.remove(listener);
	}

	/**
	 * @see com.yagasoft.overcast.base.container.content.IContentManager#clearContentListeners(com.yagasoft.overcast.base.container.content.Change)
	 */
	@Override
	public void clearContentListeners(Change change)
	{
		for (IContentListener listener : contentListeners.keySet())
		{
			if (contentListeners.get(listener).contains(change))
			{
				contentListeners.get(listener).remove(change);

				if (contentListeners.get(listener).isEmpty())
				{
					removeContentListener(listener);
				}
			}
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.Container#clearAllListeners()
	 */
	@Override
	public void clearAllListeners()
	{
		super.clearAllListeners();
		contentListeners.clear();
	}

	// ======================================================================================
	// #endregion Events.
	// //////////////////////////////////////////////////////////////////////////////////////

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
		Logger.info("searching: " + id + ", in: " + path);

		try
		{
			updateFromSource(true, false);
		}
		catch (OperationException e)
		{
			Logger.error("failed to update from source " + id + ", searching from memory ...");
			Logger.except(e);
			e.printStackTrace();
		}

		// if the folders list contains the passed ID (mapped), return it.
		if (folders.containsKey(id))
		{
			return (S) folders.get(id);
		}
		else if (files.containsKey(id))
		{	// else, check the files list.
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
					Logger.info("found: " + container.path + ", in: " + path);
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
	public Container<?>[] searchByName(String name, boolean recursively)
	{
		Logger.info("searching " + name + " in " + path);

		try
		{
			updateFromSource(true, false);
		}
		catch (OperationException e)
		{
			Logger.error("failed to update from source " + id + ", searching from memory ...");
			Logger.except(e);
			e.printStackTrace();
		}

		ArrayList<Container<?>> result = new ArrayList<Container<?>>();

		// same as 'searchById' ...
		for (Container<?> container : getChildrenArray())
		{
			if (name.equals(container.name))
			{
				Logger.info("found: " + container.path + ", in: " + path);
				result.add(container);
			}
		}

		if (recursively)
		{
			for (Folder<?> folder : folders.values())
			{
				result.addAll(Arrays.asList(folder.searchByName(name, recursively)));
			}
		}

		return result.toArray(new Container<?>[result.size()]);
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
	public void removeObsolete(ArrayList<String> ids, boolean filter)
	{
		Logger.info("removing obsolete containers: " + path);

		// combine lists in this folder (IDs only, not containers themselves).
		ArrayList<String> containers = new ArrayList<String>();
		containers.addAll(folders.keySet());
		containers.addAll(files.keySet());

		// go through the combined list.
		for (String container : containers)
		{
			// if the list sent contains any of the existing containers in this folder ...
			if ( !ids.contains(container))
			{
				// remove it.
				folders.remove(id);
				files.remove(id);

				Logger.info("removed obsolete: " + container);
			}
		}

		// if it's required to filter the sent list as well ...
		if (filter)
		{
			// go through the combined list again.
			for (String container : containers)
			{
				// this time just remove from the sent list that exist in this folder.
				ids.remove(container);

				Logger.info("filtering existing: " + container);
			}
		}

		Logger.info("finished removing obsolete containers: " + path);
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
	public void removeObsoleteFolders(ArrayList<String> folderIds, boolean filter)
	{
		Logger.info("removing obsolete folders: " + path);

		ArrayList<String> foldersList = new ArrayList<String>();
		foldersList.addAll(folders.keySet());

		for (String folder : foldersList)
		{
			if ( !folderIds.contains(folder))
			{
				folders.remove(id);
				files.remove(id);

				Logger.info("removed obsolete: " + folder);
			}
		}

		if (filter)
		{
			for (String folder : foldersList)
			{
				folderIds.remove(folder);

				Logger.info("filtering existing: " + folder);
			}
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
	public void removeObsoleteFiles(ArrayList<String> fileIds, boolean filter)
	{
		Logger.info("removing obsolete files: " + path);

		ArrayList<String> filesList = new ArrayList<String>();
		filesList.addAll(folders.keySet());

		for (String file : filesList)
		{
			if ( !fileIds.contains(file))
			{
				folders.remove(id);
				files.remove(id);

				Logger.info("removed obsolete: " + file);
			}
		}

		if (filter)
		{
			for (String file : filesList)
			{
				fileIds.remove(file);

				Logger.info("filtering existing: " + file);
			}
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
		ArrayList<Container<?>> children = new ArrayList<Container<?>>();
		children.addAll(folders.values());
		children.addAll(files.values());

		// do it recursively.
		for (Folder<?> folder : getFoldersList())
		{
			children.addAll(folder.getWholeTreeList());
		}

		return children;
	}

	/**
	 * Gets the children as a list, including folders and files.
	 *
	 * @return the children list
	 */
	public List<Container<?>> getChildrenList()
	{
		ArrayList<Container<?>> children = new ArrayList<Container<?>>();
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
		ArrayList<Folder<?>> foldersList = new ArrayList<Folder<?>>();
		foldersList.addAll(folders.values());

		return foldersList;
	}

	/**
	 * Gets the files list only.
	 *
	 * @return the files list
	 */
	public List<File<?>> getFilesList()
	{
		ArrayList<File<?>> filesList = new ArrayList<File<?>>();
		filesList.addAll(files.values());

		return filesList;
	}

	/**
	 * Gets the children as an array, including folders and files.
	 *
	 * @return the children array
	 */
	public Container<?>[] getChildrenArray()
	{
		ArrayList<Container<?>> children = new ArrayList<Container<?>>();
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
		ArrayList<Folder<?>> foldersList = new ArrayList<Folder<?>>();
		foldersList.addAll(folders.values());

		return foldersList.toArray(new Folder<?>[foldersList.size()]);
	}

	/**
	 * Gets the files array.
	 *
	 * @return the files array
	 */
	public File<?>[] getFilesArray()
	{
		ArrayList<File<?>> filesList = new ArrayList<File<?>>();
		filesList.addAll(files.values());

		return filesList.toArray(new File<?>[filesList.size()]);
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
	 * @return the slots
	 */
	public static Semaphore getSemaphore()
	{
		return slots;
	}

	/**
	 * @param slots
	 *            the slots to set
	 */
	public static void setSemaphore(Semaphore slots)
	{
		Folder.slots = slots;
	}

}
