
package com.yagasoft.overcast.container;


import java.util.ArrayList;
import java.util.Arrays;

import com.yagasoft.overcast.CSP;


public abstract class RemoteFactory<FolderSourceType, FolderType extends RemoteFolder<FolderSourceType>, FileSourceType, FileType extends RemoteFile<FileSourceType>>
{

	protected CSP		csp;

	protected Class<FolderType>	folderType;
	protected Class<FileType>	fileType;

	public RemoteFactory(Class<FolderType> folderType, Class<FileType> fileType)
	{
		this.folderType = folderType;
		this.fileType = fileType;
	}

	//--------------------------------------------------------------------------------------
	// #region Create basic.

	public FolderType createFolder()
	{
		try
		{
			FolderType folder = folderType.newInstance();
			postObjectCreation(folder);

			return folder;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public FileType createFile()
	{
		try
		{
			FileType file = fileType.newInstance();
			postObjectCreation(file);

			return file;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	protected void postObjectCreation(Container<?> container)
	{
		container.setCsp(csp);
	}

	// #endregion Create basic.
	//--------------------------------------------------------------------------------------


	//--------------------------------------------------------------------------------------
	// #region Create out of sent sourceObject.

	public FolderType createFolder(FolderSourceType sourceObject, boolean fetchInfoOnline)
	{
		FolderType folder = createFolder();
		folder.setSourceObject(sourceObject);
		updateContainer(folder, fetchInfoOnline);

		return folder;
	}

	public FileType createFile(FileSourceType sourceObject, boolean fetchInfoOnline)
	{
		FileType file = createFile();
		file.setSourceObject(sourceObject);
		updateContainer(file, fetchInfoOnline);

		return file;
	}

	protected void updateContainer(Container<?> container, boolean online)
	{
		if (online)
		{
			container.updateFromSource();
		}
		else
		{
			container.updateInfo();
		}
	}


	// #endregion Create out of sent sourceObject.
	//--------------------------------------------------------------------------------------


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


	//--------------------------------------------------------------------------------------
	// #region Create using the sent path.

	@SuppressWarnings("unchecked")
	public FolderType createFolderByPath(String path)
	{
		try
		{
			ArrayList<String> splitPath = splitPath(path);

			FolderType container = folderType.newInstance();

			return (FolderType) postObjectCreationByPath(container, splitPath);
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public FileType createFileByPath(String path)
	{
		try
		{
			ArrayList<String> splitPath = splitPath(path);

			FileType container = fileType.newInstance();

			return (FileType) postObjectCreationByPath(container, splitPath);
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	protected Container<?> postObjectCreationByPath(Container<?> container, ArrayList<String> splitPath)
	{
		container.setName(splitPath.remove(splitPath.size() - 1));

		Container<?> result = searchForContainer(splitPath, container.getName());

		if ((result == null)
				|| !(result.isFolder() && (container instanceof RemoteFolder))
				|| !( !result.isFolder() && (container instanceof RemoteFile)))
		{
			return container;
		}
		else
		{
			return result;
		}
	}

	protected ArrayList<String> splitPath(String path)
	{
		ArrayList<String> splitPath = new ArrayList<String>(Arrays.asList(path.split("/")));

		if (splitPath.get(0).equals(""))
		{
			splitPath.remove(0);
		}

		return splitPath;
	}

	protected Container<?> searchForContainer(ArrayList<String> splitPath, String name)
	{
		RemoteFolder<?> result = csp.getRemoteFileTree();

		while ((result != null) && (splitPath.size() > 0))
		{
			result.updateFromSource(true, false);
			result = result.searchByName(splitPath.remove(0), false);
		}

		if ((splitPath.size() > 0) || (result == null))
		{
			return null;
		}
		else
		{
			return result.searchByName(name, false);
		}

	}


	// #endregion Create using the sent path.
	//--------------------------------------------------------------------------------------


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
