/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 */

package com.yagasoft.overcast.container.local;


import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.CSP;
import com.yagasoft.overcast.container.Folder;
import com.yagasoft.overcast.container.FolderHelper;
import com.yagasoft.overcast.container.FolderHelper.TreeCopier;
import com.yagasoft.overcast.container.FolderHelper.TreeDeleter;
import com.yagasoft.overcast.container.FolderHelper.TreeMover;
import com.yagasoft.overcast.container.remote.RemoteFolder;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.container.transfer.UploadJob;
import com.yagasoft.overcast.exception.AccessException;
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
	 */
	public LocalFolder(Path file)
	{
		sourceObject = file;
		updateFromSource(false, false);		// updating the info locally costs nothing, so do it automatically.
	}
	
	/**
	 * Instantiates a new local folder.
	 * 
	 * @param path
	 *            Path to the folder.
	 */
	public LocalFolder(String path)
	{
		this(Paths.get(path));		// get the folder object and pass it to the other constructor.
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#generateId()
	 */
	@Override
	public void generateId()
	{
		id = path;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#create(Folder<?>)
	 */
	@Override
	public void create(Folder<?> parent)
	{
		create(parent.getPath());		// extract path and call overloaded string function.
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#create(java.lang.String)
	 */
	@Override
	public void create(String parentPath)
	{
		try
		{
			Files.createDirectories(Paths.get(parentPath, name));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#isExist()
	 */
	@Override
	public boolean isExist() throws AccessException
	{
		// if the Java library says the folder doesn't exist, and at same time it says the folder doesn't 'not exist', then ...
		// obviously a problem.
		if ( !Files.exists(sourceObject) && !Files.notExists(sourceObject))
		{
			throw new AccessException("Can't determine if folder exists or not.");
		}
		
		return Files.exists(sourceObject);
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#buildTree(int)
	 */
	@Override
	public void buildTree(int numberOfLevels)
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
	 * @see com.yagasoft.overcast.container.Folder#calculateSize()
	 */
	@Override
	public long calculateSize()
	{
		try
		{
			size = FolderHelper.getSize(path);		// this will calculate the whole folder size, including sub-folders.
		}
		catch (IOException e)
		{
			size = 0;
			e.printStackTrace();
		}
		
		return size;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#updateInfo(boolean, boolean)
	 */
	@Override
	public void updateInfo(boolean folderContents, boolean recursively)
	{
		updateFromSource(folderContents, recursively);		// updating the info locally costs nothing, so do it automatically.
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#updateFromSource(boolean, boolean)
	 */
	@Override
	public void updateFromSource(boolean folderContents, boolean recursively)
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
	 * @see com.yagasoft.overcast.container.Container#copy(com.yagasoft.overcast.container.Folder, boolean)
	 */
	@Override
	public LocalFolder copy(Folder<?> destination, boolean overwrite) throws OperationException
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
		}
		
		return null;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#move(com.yagasoft.overcast.container.Folder, boolean)
	 */
	@Override
	public void move(Folder<?> destination, boolean overwrite) throws OperationException
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
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#rename(java.lang.String)
	 */
	@Override
	public void rename(String newName) throws OperationException
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
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#delete()
	 */
	@Override
	public void delete()
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
	 * @param object
	 *            Object passed by the initialiser to be passed back on state change. It can be used as a kind of "call-back" or
	 *            something; the sender of this object can cast it back and use it as seen fit.
	 * @return the upload jobs
	 * @throws TransferException
	 *             A problem occurred during the transfer of the container.
	 */
	public UploadJob<?, ?>[] upload(RemoteFolder<?> parent, boolean overwrite, ITransferProgressListener listener, Object object)
			throws TransferException
	{
		return parent.getCsp().upload(this, parent, overwrite, listener, object);
	}
	
	/**
	 * Calculate local free space available on the local disk (the one the root resides on).
	 * 
	 * @return the free space in bytes
	 */
	public long calculateLocalFreeSpace()
	{
		try
		{
			return localFreeSpace = Files.getFileStore(sourceObject.getRoot()).getUnallocatedSpace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return localFreeSpace;
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
	 */
	public long getLocalFreeSpace()
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
