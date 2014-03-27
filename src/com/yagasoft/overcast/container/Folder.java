/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.container/Folder.java
 * 
 *			Modified: 27-Mar-2014 (16:12:23)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.container;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.yagasoft.overcast.container.operation.IOperationListener;
import com.yagasoft.overcast.exception.CreationException;


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
	protected HashMap<String, Folder<?>>	folders	= new HashMap<String, Folder<?>>();
	
	/** Files inside this folder mapped by ID. */
	protected HashMap<String, File<?>>		files	= new HashMap<String, File<?>>();
	
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
	 * Creates the folder at the source with the info set (class attributes).
	 * 
	 * @param parentPath
	 *            Parent path.
	 * @param listener
	 *            the listener
	 * @throws CreationException
	 *             problem with creating the folder
	 */
	public abstract void create(String parentPath, IOperationListener listener) throws CreationException;
	
	/**
	 * Adds the folder passed to the list of folder in this folder.
	 * 
	 * @param folder
	 *            Folder to add.
	 */
	public void add(Folder<?> folder)
	{
		folders.put(folder.id, folder);		// extract the ID of the folder and use it to map the folder passed.
		folder.parent = this;				// set the parent of the folder passed as this folder.
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
		file.parent = this;
	}
	
	/**
	 * Removes the folder from the list of folder in this folder.
	 * 
	 * @param folder
	 *            Folder to remove.
	 */
	public void remove(Folder<?> folder)
	{
		folders.remove(folder.id);
	}
	
	/**
	 * Removes the file from the list of files in this folder.
	 * 
	 * @param file
	 *            File to remove.
	 */
	public void remove(File<?> file)
	{
		files.remove(file.id);
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
		folders.remove(id);
		files.remove(id);
	}
	
	/**
	 * Builds the sub-tree of this folder, adding sub-folders and files to the array.
	 * 
	 * @param numberOfLevels
	 *            Depth to go to in sub-folders, with zero as this folder contents only.
	 */
	public abstract void buildTree(int numberOfLevels);
	
	/**
	 * Equivalent to {@link Folder#buildTree(int)} with Integer.MAX_VALUE passed if recursive, or passing zero if not.
	 * 
	 * @param recursively
	 *            Recursively build this tree.
	 */
	public void buildTree(boolean recursively)
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
	 */
	public abstract long calculateSize();
	
	/**
	 * @see com.yagasoft.overcast.container.Container#isFolder()
	 */
	@Override
	public boolean isFolder()
	{
		return true;		// this is a folder!
	}
	
	/**
	 * Update the fields (class attributes) in this folder object from the in-memory info (nothing is done outside the program).
	 * Can update the folder contents' attributes as well, and can do so recursively (tree).
	 * 
	 * @param folderContents
	 *            Update folder contents.
	 * @param recursively
	 *            Recursively or not.
	 */
	public abstract void updateInfo(boolean folderContents, boolean recursively);
	
	/**
	 * Update from where the folder resides. It updates the info of the folder from the source itself
	 * , and can be done recursively (tree).
	 * 
	 * @param folderContents
	 *            Update folder contents.
	 * @param recursively
	 *            Recursive or not.
	 */
	public abstract void updateFromSource(boolean folderContents, boolean recursively);
	
	/**
	 * Checks if this file/folder exists in this folder, and whether to search recursively.
	 * 
	 * @param <S>
	 *            capture the type of the container passed to return it after searching is done.
	 * @param container
	 *            File/folder to look for.
	 * @param recursively
	 *            Recursively or not.
	 * @return The container
	 */
	public <S extends Container<?>> S searchById(S container, boolean recursively)
	{
		return (S) searchById(container.id, recursively);		// capture the ID (as it's unique) and call the appropriate method.
	}
	
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
					return (S) container;
				}
			}
		}
		
		// nothing was found.
		return null;
	}
	
	/**
	 * Checks if the file/folder exists in this folder (searches by name), and whether to do it recursively.
	 * 
	 * @param <S>
	 *            capture the type of the container passed to return it after searching is done.
	 * @param container
	 *            Container to look for.
	 * @param recursively
	 *            Recursively or not.
	 * @return The container
	 */
	public <S extends Container<?>> S searchByName(S container, boolean recursively)
	{
		return (S) searchByName(container.name, recursively);
	}
	
	/**
	 * Search by name.
	 * 
	 * @param <S>
	 *            capture the type of the container passed to return it after searching is done.
	 * @param name
	 *            Name of the container to search for.
	 * @param recursively
	 *            Recursively or not.
	 * @return The container
	 */
	public <S extends Container<?>> S searchByName(String name, boolean recursively)
	{
		// same as 'searchById' ...
		for (Container<?> container : getChildrenArray())
		{
			if (name.equals(container.name))
			{
				return (S) container;
			}
		}
		
		if (recursively)
		{
			for (Folder<?> folder : folders.values())
			{
				Container<?> container = folder.searchByName(name, recursively);
				
				if (container != null)
				{
					return (S) container;
				}
			}
		}
		
		return null;
	}
	
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
			}
		}
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
		ArrayList<String> foldersList = new ArrayList<String>();
		foldersList.addAll(folders.keySet());
		
		for (String folder : foldersList)
		{
			if ( !folderIds.contains(folder))
			{
				folders.remove(id);
				files.remove(id);
			}
		}
		
		if (filter)
		{
			for (String folder : foldersList)
			{
				folderIds.remove(folder);
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
		ArrayList<String> filesList = new ArrayList<String>();
		filesList.addAll(folders.keySet());
		
		for (String file : filesList)
		{
			if ( !fileIds.contains(file))
			{
				folders.remove(id);
				files.remove(id);
			}
		}
		
		if (filter)
		{
			for (String file : filesList)
			{
				fileIds.remove(file);
			}
		}
	}
	
	// --------------------------------------------------------------------------------------
	// #region Children.
	
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
	
	// #endregion Children.
	// --------------------------------------------------------------------------------------
	
}
