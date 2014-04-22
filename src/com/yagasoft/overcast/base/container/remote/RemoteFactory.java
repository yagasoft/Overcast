/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.remote/RemoteFactory.java
 *
 *			Modified: Apr 15, 2014 (9:14:17 AM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.remote;


import java.util.ArrayList;
import java.util.Arrays;

import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.Container;
import com.yagasoft.overcast.base.csp.CSP;
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.OperationException;


/**
 * A factory for creating remote files and folders objects.
 *
 * @param <FolderSourceType>
 *            The source folder type (folder type from the original CSP API) must be passed to this class.<br />
 *            It's needed to assist in creating the {@link RemoteFile}.
 * @param <FolderType>
 *            The folder type (folder type from this API) must be passed to this class.<br />
 *            It's needed to assist in creating the {@link RemoteFile}.
 * @param <FileSourceType>
 *            The source file type (file type from the original CSP API) must be passed to this class.<br />
 *            It's needed to assist in creating the {@link RemoteFile}.
 * @param <FileType>
 *            The file type (file type from this API) must be passed to this class.<br />
 *            It's needed to assist in creating the {@link RemoteFile}.
 */
@SuppressWarnings("rawtypes")
public abstract class RemoteFactory<FolderSourceType, FolderType extends RemoteFolder<FolderSourceType>, FileSourceType, FileType extends RemoteFile<FileSourceType>>
{

	/** CSP object to be passed to created files and folders. */
	protected CSP				csp;

	/**
	 * Folder type as passed during creation of this factory.<br />
	 * Will be used to create folders -- set their type.
	 */
	protected Class<FolderType>	folderType;

	/**
	 * File type as passed during creation of this factory.<br />
	 * Will be used to create files -- set their type.
	 */
	protected Class<FileType>	fileType;

	/**
	 * Instantiates a new remote factory.
	 *
	 * @param folderType
	 *            Folder type from this API.
	 * @param fileType
	 *            File type from this API.
	 */
	public RemoteFactory(Class<FolderType> folderType, Class<FileType> fileType)
	{
		this.folderType = folderType;
		this.fileType = fileType;
	}

	// --------------------------------------------------------------------------------------
	// #region Create basic.

	/**
	 * Creates a new remote folder object.
	 *
	 * @return the folder object
	 * @throws CreationException
	 */
	public FolderType createFolder() throws CreationException
	{
		try
		{
			FolderType folder = folderType.newInstance();	// new RemoteFolder()
			postObjectCreation(folder);		// init object.

			return folder;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			Logger.error("creating folder object");
			Logger.except(e);
			e.printStackTrace();

			throw new CreationException("Couldn't create object in factory.");
		}
	}

	/**
	 * Creates a new remote file object.
	 *
	 * @return the file object
	 * @throws CreationException
	 */
	public FileType createFile() throws CreationException
	{
		try
		{
			FileType file = fileType.newInstance();		// new RemoteFile()
			postObjectCreation(file);		// init object.

			return file;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			Logger.error("creating file object");
			Logger.except(e);
			e.printStackTrace();

			throw new CreationException("Couldn't create object in factory.");
		}
	}

	/**
	 * Do stuff after object creation.
	 *
	 * @param container
	 *            the container created
	 */
	@SuppressWarnings("unchecked")
	protected void postObjectCreation(Container<?> container)
	{
		container.setCsp(csp);
	}

	// #endregion Create basic.
	// --------------------------------------------------------------------------------------

	// --------------------------------------------------------------------------------------
	// #region Create out of sent sourceObject.

	/**
	 * Creates a new remote folder object.
	 *
	 * @param sourceObject
	 *            the folder object given by the original CSP API.
	 * @param fetchInfoOnline
	 *            the fetch info online instead of from memory.
	 * @return the folder object
	 * @throws CreationException
	 */
	public FolderType createFolder(FolderSourceType sourceObject, boolean fetchInfoOnline) throws CreationException
	{
		FolderType folder = createFolder();		// get the basic folder object
		folder.setSourceObject(sourceObject);	// add the CSP folder object to it
		updateContainer(folder, fetchInfoOnline);		// update folder meta info

		return folder;
	}

	/**
	 * Creates a new remote file object.
	 *
	 * @param sourceObject
	 *            the file object given by the original CSP API.
	 * @param fetchInfoOnline
	 *            the fetch info online instead of from memory.
	 * @return the file object
	 * @throws CreationException
	 */
	public FileType createFile(FileSourceType sourceObject, boolean fetchInfoOnline) throws CreationException
	{
		FileType file = createFile();
		file.setSourceObject(sourceObject);
		updateContainer(file, fetchInfoOnline);

		return file;
	}

	/**
	 * Update container's meta info.
	 *
	 * @param container
	 *            the container
	 * @param online
	 *            update meta from source (online)
	 */
	protected void updateContainer(Container<?> container, boolean online)
	{
		if (online)
		{
			try
			{
				container.updateFromSource();
			}
			catch (OperationException e)
			{
				e.printStackTrace();
				container.updateInfo();
			}
		}
		else
		{
			container.updateInfo();
		}
	}

	// #endregion Create out of sent sourceObject.
	// --------------------------------------------------------------------------------------

//	//--------------------------------------------------------------------------------------
//	// #region Create using the sent ID.
//
//	public FolderType createFolderById(String id)
//	{
//		FolderType folder = createFolder();
//		postObjectCreationById(folder, id);
//
//		return folder;
//	}
//
//	public FileType createFileById(String id)
//	{
//		FileType file = createFile();
//		postObjectCreationById(file, id);
//
//		return file;
//	}
//
//	protected void postObjectCreationById(Container<?> container, String id)
//	{
//		container.setId(id);
//	}
//
//
//	// #endregion Create using the sent ID.
//	//--------------------------------------------------------------------------------------

	// --------------------------------------------------------------------------------------
	// #region Create using the sent path.

	/**
	 * Creates a new remote folder object.
	 *
	 * @param path
	 *            the path to the folder on the server including its name
	 * @return the folder object
	 * @throws OperationException
	 */
	@SuppressWarnings("unchecked")
	public FolderType createFolderByPath(String path) throws OperationException
	{
		try
		{
			ArrayList<String> splitPath = splitPath(path);		// get each node in the path alone.

			FolderType container = folderType.newInstance();	// new folder()

			// post creation stuff, and make sure it doesn't already exist
			return (FolderType) postObjectCreationByPath(container, splitPath);
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			Logger.error("creating folder object: " + path);
			Logger.except(e);
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Creates a new remote file object.
	 *
	 * @param path
	 *            the path to the file on the server including its name
	 * @return the file object
	 * @throws OperationException
	 */
	@SuppressWarnings("unchecked")
	public FileType createFileByPath(String path) throws OperationException
	{
		try
		{
			ArrayList<String> splitPath = splitPath(path);

			FileType container = fileType.newInstance();

			return (FileType) postObjectCreationByPath(container, splitPath);
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			Logger.error("creating file object: " + path);
			Logger.except(e);
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Do stuff after creating the component.
	 *
	 * @param container
	 *            the container
	 * @param splitPath
	 *            the path as individual nodes.
	 * @return the container. It might be the same one passed, or one created previously if found in the file tree
	 * @throws OperationException
	 */
	protected Container<?> postObjectCreationByPath(Container<?> container, ArrayList<String> splitPath) throws OperationException
	{
		container.setName(splitPath.remove(splitPath.size() - 1));		// get the name from the last entry in the path

		// look for the container in file tree.
		Container<?> result = searchForContainer(splitPath, container.getName());

		// if nothing is found, or the result has a type doesn't match with the one passed ...
		if ((result == null)
				|| !(result.isFolder() && (container instanceof RemoteFolder))
				|| !( !result.isFolder() && (container instanceof RemoteFile)))
		{
			return container;		// ... then just return the new one.
		}
		else
		{
			return result;			// ... else, return the old one.
		}
	}

	/**
	 * Split the path into individual nodes.
	 *
	 * @param path
	 *            the path
	 * @return the array list of nodes in the path
	 */
	protected ArrayList<String> splitPath(String path)
	{
		ArrayList<String> splitPath = new ArrayList<String>(Arrays.asList(path.split("/")));

		// if the path starts with '/' it will cause the first entry to be empty!
		if (splitPath.get(0).equals(""))
		{
			splitPath.remove(0);
		}

		return splitPath;
	}

	/**
	 * Search for the container in the file tree in-memory.
	 *
	 * @param splitPath
	 *            the path divided into separate nodes.
	 * @param name
	 *            the name of the file.
	 * @return the container found, or null if nothing is found.
	 * @throws OperationException
	 */
	protected Container<?> searchForContainer(ArrayList<String> splitPath, String name) throws OperationException
	{
		RemoteFolder<?> result = csp.getRemoteFileTree();		// search the file tree.

		// search for each entry in the path ...
		while ((result != null) && (splitPath.size() > 0))
		{
			result.updateFromSource(true, false);
			result = result.searchByName(splitPath.remove(0), false);
		} // ..., until either it reaches the end of the path successfully, or not.

		// if part of the path is not found ...
		if ((splitPath.size() > 0) || (result == null))
		{
			return null;		// ... return nothing.
		}
		else
		{	// ... or search for the file in the end node. Might return null.
			return result.searchByName(name, false);
		}

	}

	// #endregion Create using the sent path.
	// --------------------------------------------------------------------------------------

	/**
	 * @return the csp
	 */
	public CSP getCsp()
	{
		return csp;
	}

	/**
	 * @param csp
	 *            the csp to set
	 */
	public void setCsp(CSP csp)
	{
		this.csp = csp;
	}

}
