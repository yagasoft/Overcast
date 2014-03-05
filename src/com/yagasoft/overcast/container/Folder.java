
package com.yagasoft.overcast.container;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


@SuppressWarnings("unchecked")
public abstract class Folder<T> extends Container<T>
{
	
	protected HashMap<String, Folder<?>>	folders	= new HashMap<String, Folder<?>>();
	protected HashMap<String, File<?>>		files	= new HashMap<String, File<?>>();
	
	/**
	 * Creates the folder at the source with the info set (class attributes).
	 * 
	 * @throws Exception
	 */
	public abstract void create(Folder<?> parent) throws Exception;
	
	public abstract void create(String parentPath) throws Exception;
	
	public void add(Folder<?> folder)
	{
		folders.put(folder.id, folder);
		folder.parent = this;
	}
	
	public void add(File<?> file)
	{
		files.put(file.id, file);
		file.parent = this;
	}
	
	public void remove(Folder<?> folder)
	{
		folders.remove(folder.id);
	}
	
	public void remove(File<?> file)
	{
		files.remove(file.id);
	}
	
	public void remove(String id)
	{
		folders.remove(id);
		files.remove(id);
	}
	
	/**
	 * Builds the sub-tree of this folder, adding sub-folders and files to the
	 * array.
	 * 
	 * @param numberOfLevels
	 *            Depth to go to in sub-folders, with zero as this folder
	 *            contents only.
	 */
	public abstract void buildTree(int numberOfLevels);
	
	/**
	 * Equivalent to {@link Folder#buildTree(int)} with Integer.MAX_VALUE
	 * passed if recursive, or passing zero if not.
	 * 
	 * @param recursively
	 *            Recursively build this tree.
	 */
	public void buildTree(boolean recursively)
	{
		if (recursively)
		{
			buildTree(Integer.MAX_VALUE);
		}
		else
		{
			buildTree(0);
		}
	}
	
	/**
	 * I chose not to add it here and not in updateFromSource because it's an
	 * intensive operation that should be done manually only.
	 */
	public abstract long calculateSize();
	
	/**
	 * @see com.yagasoft.overcast.container.Container#isFolder()
	 */
	@Override
	public boolean isFolder()
	{
		return true;
	}
	
	/**
	 * Update the fields (class attributes) in this folder object from the
	 * in-memory info (nothing is done outside the program). Can update the
	 * folder contents' attributes as well, and can do so recursively (tree).
	 * 
	 * @param folderContents
	 *            Update folder contents.
	 * @param recursively
	 *            Recursively or not.
	 */
	public abstract void updateInfo(boolean folderContents, boolean recursively);
	
	/**
	 * Update from where the folder resides. It updates the contents (meta only)
	 * of the folder, and can be done recursively (tree).
	 * 
	 * @param folderContents
	 *            Update folder contents.
	 * @param recursively
	 *            Recursively or not.
	 */
	public abstract void updateFromSource(boolean folderContents, boolean recursively);
	
	/**
	 * Checks if this file/folder exists in this folder, and whether to do it
	 * recursively.
	 * 
	 * @param container
	 *            File/folder to look for.
	 * @param recursively
	 *            Recursively or not.
	 * @return true if it exists, and false otherwise.
	 */
	public <S extends Container<?>> S searchById(S container, boolean recursively)
	{
		return (S) searchById(container.id, recursively);
	}
	
	/**
	 * Checks if this file/folder exists in this folder (searches by ID), and whether to do it recursively.
	 * 
	 * @param id
	 *            The ID of the file to look for.
	 * @param recursively
	 *            Recursively or not.
	 * @return true if it exists, and false otherwise.
	 */
	public <S extends Container<?>> S searchById(String id, boolean recursively)
	{
		if (folders.containsKey(id))
		{
			return (S) folders.get(id);
		}
		else if (files.containsKey(id))
		{
			return (S) files.get(id);
		}
		else if (recursively)
		{
			for (Folder<?> folder : folders.values())
			{
				Container<?> container = folder.searchById(id, recursively);
				
				if (container != null)
				{
					return (S) container;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Checks if this file/folder exists in this folder, and whether to do it
	 * recursively.
	 * 
	 * @param container
	 *            File/folder to look for.
	 * @param recursively
	 *            Recursively or not.
	 * @return true if it exists, and false otherwise.
	 */
	public <S extends Container<?>> S searchByName(S container, boolean recursively)
	{
		return (S) searchByName(container.name, recursively);
	}
	
	public <S extends Container<?>> S searchByName(String name, boolean recursively)
	{
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
	 * Removes the obsolete members from the list in this folder using the fresh
	 * list sent as an argument.
	 * 
	 * @param ids
	 *            Fresh list of folders and files.
	 * @param filter
	 *            Filter the sent list from already existing members in this folder (to allow faster parsing of new ones).
	 */
	public void removeObsolete(ArrayList<String> ids, boolean filter)
	{
		ArrayList<String> containers = new ArrayList<String>();
		containers.addAll(folders.keySet());
		containers.addAll(files.keySet());
		
		for (String container : containers)
		{
			if ( !ids.contains(container))
			{
				folders.remove(id);
				files.remove(id);
			}
		}
		
		if (filter)
		{
			for (String container : containers)
			{
				ids.remove(container);
			}
		}
	}
	
	/**
	 * Removes the obsolete folders from the list in this folder using the fresh
	 * list sent as an argument.
	 * 
	 * @param folderIds
	 *            Fresh list of folders.
	 * @param filter
	 *            Filter the list from already existing folders (to allow faster
	 *            parsing of new folders).
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
	 * Removes the obsolete files from the list in this folder using the fresh
	 * list sent as an argument.
	 * 
	 * @param fileIds
	 *            Fresh list of files.
	 * @param filter
	 *            Filter the list from already existing files (to allow faster
	 *            parsing of new files).
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
	// #region children.
	
	/**
	 * Gets the whole tree as a sequential list. First adds the children to a
	 * new list, and then goes through each folder and gets its tree and adds it
	 * recursively.
	 * 
	 * @return the whole tree as a sequential list
	 */
	public List<Container<?>> getWholeTreeList()
	{
		ArrayList<Container<?>> children = new ArrayList<Container<?>>();
		
		children.addAll(folders.values());
		children.addAll(files.values());
		
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
	
	public List<Folder<?>> getFoldersList()
	{
		ArrayList<Folder<?>> foldersList = new ArrayList<Folder<?>>();
		
		foldersList.addAll(folders.values());
		
		return foldersList;
	}
	
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
		
		return children.toArray(new Container<?>[0]);
	}
	
	public Folder<?>[] getFoldersArray()
	{
		ArrayList<Folder<?>> foldersList = new ArrayList<Folder<?>>();
		
		foldersList.addAll(folders.values());
		
		return foldersList.toArray(new Folder<?>[0]);
	}
	
	public File<?>[] getFilesArray()
	{
		ArrayList<File<?>> filesList = new ArrayList<File<?>>();
		
		filesList.addAll(files.values());
		
		return filesList.toArray(new File<?>[0]);
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
	
	public Iterator<Folder<?>> getFoldersIterator()
	{
		return getFoldersList().iterator();
	}
	
	public Iterator<File<?>> getFilesIterator()
	{
		return getFilesList().iterator();
	}
	
	// #endregion children.
	// --------------------------------------------------------------------------------------
	
}
