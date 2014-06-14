/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.remote/RemoteFactory.java
 *
 *			Modified: 06-May-2014 (04:10:43)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.remote;


import com.yagasoft.logger.Logger;
import com.yagasoft.overcast.base.container.Container;
import com.yagasoft.overcast.base.csp.CSP;
import com.yagasoft.overcast.exception.CreationException;
import com.yagasoft.overcast.exception.OperationException;


/**
 * A factory for creating remote files and folders objects.
 *
 * @param <FolderSourceType>
 *            The source folder type (folder type from the original CspType API) must be passed to this class.<br />
 *            It's needed to assist in creating the {@link RemoteFile}.
 * @param <FolderType>
 *            The folder type (folder type from this API) must be passed to this class.<br />
 *            It's needed to assist in creating the {@link RemoteFile}.
 * @param <FileSourceType>
 *            The source file type (file type from the original CspType API) must be passed to this class.<br />
 *            It's needed to assist in creating the {@link RemoteFile}.
 * @param <FileType>
 *            The file type (file type from this API) must be passed to this class.<br />
 *            It's needed to assist in creating the {@link RemoteFile}.
 */
@SuppressWarnings("rawtypes")
public class RemoteFactory<FolderSourceType, FolderType extends RemoteFolder<FolderSourceType>, FileSourceType, FileType extends RemoteFile<FileSourceType>, CspType extends CSP>
{
	
	/** CspType object to be passed to created files and folders. */
	protected CspType			csp;
	
	/** Path prefix, which will be used to clean-up the path sent by the CspType -- for API path standardisation. */
	protected String			pathPrefix;
	
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
	 * @param csp
	 *            CspType using this factory.
	 * @param folderType
	 *            Folder type from this API.
	 * @param fileType
	 *            File type from this API.
	 * @param pathPrefix
	 *            Path prefix.
	 */
	public RemoteFactory(CspType csp, Class<FolderType> folderType, Class<FileType> fileType, String pathPrefix)
	{
		this.csp = csp;
		this.folderType = folderType;
		this.fileType = fileType;
		this.pathPrefix = pathPrefix;
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
		container.setPathPrefix(pathPrefix);
	}
	
	// #endregion Create basic.
	// --------------------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------------------
	// #region Create out of sent sourceObject.
	
	/**
	 * Creates a new remote folder object.
	 *
	 * @param sourceObject
	 *            the folder object given by the original CspType API.
	 * @param fetchInfoOnline
	 *            the fetch info online instead of from memory.
	 * @return the folder object
	 * @throws CreationException
	 */
	public FolderType createFolder(FolderSourceType sourceObject, boolean fetchInfoOnline) throws CreationException
	{
		FolderType folder = createFolder();		// get the basic folder object
		folder.setSourceObject(sourceObject);	// add the CspType folder object to it
		updateContainer(folder, fetchInfoOnline);		// update folder meta info
		
		return folder;
	}
	
	/**
	 * Creates a new remote file object.
	 *
	 * @param sourceObject
	 *            the file object given by the original CspType API.
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
			}
		}
	}
	
	// #endregion Create out of sent sourceObject.
	// --------------------------------------------------------------------------------------
	
	// //--------------------------------------------------------------------------------------
	// // #region Create using the sent ID.
	//
	// public FolderType createFolderById(String id)
	// {
	// FolderType folder = createFolder();
	// postObjectCreationById(folder, id);
	//
	// return folder;
	// }
	//
	// public FileType createFileById(String id)
	// {
	// FileType file = createFile();
	// postObjectCreationById(file, id);
	//
	// return file;
	// }
	//
	// protected void postObjectCreationById(Container<?> container, String id)
	// {
	// container.setId(id);
	// }
	//
	//
	// // #endregion Create using the sent ID.
	// //--------------------------------------------------------------------------------------
	
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
			FolderType container = folderType.newInstance();	// new folder()
			
			// post creation stuff, and make sure it doesn't already exist
			return (FolderType) postObjectCreationByPath(container, path);
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
			FileType container = fileType.newInstance();	// new file()
			
			// post creation stuff, and make sure it doesn't already exist
			return (FileType) postObjectCreationByPath(container, path);
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
	protected Container<?> postObjectCreationByPath(Container<?> container, String path) throws OperationException
	{
		Container<?> result;
		
		// look for the container in file tree.
		if (container instanceof RemoteFolder)
		{
			result = csp.searchFolderByPath(path);
		}
		else if (container instanceof RemoteFile)
		{
			result = csp.searchFileByPath(path);
		}
		else
		{
			return container;
		}
		
		return result != null ? result : container;
	}
	
	// #endregion Create using the sent path.
	// --------------------------------------------------------------------------------------
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	// ======================================================================================
	
	/**
	 * @return the csp
	 */
	public CspType getCsp()
	{
		return csp;
	}
	
	/**
	 * @param csp
	 *            the csp to set
	 */
	public void setCsp(CspType csp)
	{
		this.csp = csp;
	}
	
	/**
	 * @return the pathPrefix
	 */
	public String getPathPrefix()
	{
		return pathPrefix;
	}
	
	/**
	 * @param pathPrefix
	 *            the pathPrefix to set
	 */
	public void setPathPrefix(String pathPrefix)
	{
		this.pathPrefix = pathPrefix;
	}
	
	/**
	 * @return the folderType
	 */
	public Class<FolderType> getFolderType()
	{
		return folderType;
	}
	
	/**
	 * @param folderType
	 *            the folderType to set
	 */
	public void setFolderType(Class<FolderType> folderType)
	{
		this.folderType = folderType;
	}
	
	/**
	 * @return the fileType
	 */
	public Class<FileType> getFileType()
	{
		return fileType;
	}
	
	/**
	 * @param fileType
	 *            the fileType to set
	 */
	public void setFileType(Class<FileType> fileType)
	{
		this.fileType = fileType;
	}
	
	// ======================================================================================
	// #endregion Getters and setters.
	// //////////////////////////////////////////////////////////////////////////////////////
	
}
