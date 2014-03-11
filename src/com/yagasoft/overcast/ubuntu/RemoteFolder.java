
package com.yagasoft.overcast.ubuntu;


import java.util.ArrayList;
import java.util.HashMap;

import com.ubuntuone.api.files.model.U1Directory;
import com.ubuntuone.api.files.model.U1File;
import com.ubuntuone.api.files.model.U1Node;
import com.ubuntuone.api.files.request.U1NodeListener;
import com.ubuntuone.api.files.util.U1Failure;
import com.yagasoft.overcast.container.Container;
import com.yagasoft.overcast.container.Folder;
import com.yagasoft.overcast.exception.AccessException;
import com.yagasoft.overcast.exception.CreationException;


public class RemoteFolder extends com.yagasoft.overcast.container.remote.RemoteFolder<U1Directory>
{

	/**
	 * Better use the factory in Ubuntu class.
	 */
	public RemoteFolder()
	{}

	/**
	 * @see com.yagasoft.overcast.container.Container#generateId()
	 */
	@Override
	public void generateId()
	{
		
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#create(com.yagasoft.overcast.container.Folder)
	 */
	@Override
	public void create(Folder<?> parent) throws CreationException
	{}

	/**
	 * @see com.yagasoft.overcast.container.Folder#create(java.lang.String)
	 */
	@Override
	public void create(String parentPath) throws CreationException
	{}

	/**
	 * @see com.yagasoft.overcast.container.Container#isExist()
	 */
	@Override
	public boolean isExist() throws AccessException
	{
		return false;
	}

	/**
	 * @see com.yagasoft.overcast.container.Folder#buildTree(int)
	 */
	@Override
	public void buildTree(int numberOfLevels)
	{
		if ((numberOfLevels < 0) || !sourceObject.hasChildren())
		{
			return;
		}

		final HashMap<String, U1Node> childrenAsU1Node = new HashMap<String, U1Node>();

		Ubuntu.ubuntuService.listDirectory(path, new U1NodeListener()
		{

			@Override
			public void onSuccess(U1Node result)
			{
				childrenAsU1Node.put(result.getKey(), result);
			}

			@Override
			public void onUbuntuOneFailure(U1Failure failure)
			{
				System.err.println("Ubuntu One failure: " + failure);
			}

			@Override
			public void onFailure(U1Failure failure)
			{
				System.err.println("General failure: " + failure);
			}
		});

		ArrayList<String> childrenIds = new ArrayList<String>(childrenAsU1Node.keySet());

		removeObsolete(childrenIds, true);

		for (String child : childrenIds)
		{
			U1Node childAsU1Node = childrenAsU1Node.get(child);

			if (childAsU1Node.getKind().toString().equals("directory"))
			{
				RemoteFolder folder = Ubuntu.factory.createFolder((U1Directory) childAsU1Node, false);
				add(folder);

				System.out.println("Folder: " + folder.parent.getName() + "\\" + folder.name + " => " + folder.id);

				folder.buildTree(numberOfLevels - 1);
			}
			else
			{
				RemoteFile file = Ubuntu.factory.createFile((U1File) childAsU1Node, false);
				add(file);

				System.out.println("File: " + name + "\\" + file.getName() + " => " + file.getId());
				System.out.println(file.getSourceObject().getPath());
			}
		}
	}

	/**
	 * @see com.yagasoft.overcast.container.Folder#calculateSize()
	 */
	@Override
	public long calculateSize()
	{
		return 0;
	}

	/**
	 * @see com.yagasoft.overcast.container.Folder#updateInfo(boolean, boolean)
	 */
	@Override
	public void updateInfo(boolean folderContents, boolean recursively)
	{
		id = sourceObject.getKey();
		name = sourceObject.getName();
		path = sourceObject.getResourcePath();
	}

	/**
	 * @see com.yagasoft.overcast.container.Folder#updateFromSource(boolean, boolean)
	 */
	@Override
	public void updateFromSource(boolean folderContents, boolean recursively)
	{
		if (folderContents)
		{
			buildTree(recursively);
		}

		Ubuntu.ubuntuService.getNode((sourceObject == null) ? path : sourceObject.getResourcePath(), new U1NodeListener()
		{

			@Override
			public void onSuccess(U1Node node)
			{
				sourceObject = (U1Directory) node;
			}

			@Override
			public void onUbuntuOneFailure(U1Failure failure)
			{
				System.err.println("Ubuntu One error: " + failure.getMessage());
			}

			@Override
			public void onFailure(U1Failure failure)
			{
				System.err.println("General error: " + failure.getMessage());
			}
		});

		updateInfo();
	}

	/**
	 * @see com.yagasoft.overcast.container.Container#updateInfo()
	 */
	@Override
	public void updateInfo()
	{
		updateInfo(false, false);
	}

	/**
	 * @see com.yagasoft.overcast.container.Container#updateFromSource()
	 */
	@Override
	public void updateFromSource()
	{
		updateFromSource(false, false);
	}

	/**
	 * @see com.yagasoft.overcast.container.Container#copy(com.yagasoft.overcast.container.Folder, boolean)
	 */
	@Override
	public Container<?> copy(Folder<?> destination, boolean overwrite)
	{
		return null;
	}

	/**
	 * @see com.yagasoft.overcast.container.Container#move(com.yagasoft.overcast.container.Folder, boolean)
	 */
	@Override
	public void move(Folder<?> destination, boolean overwrite)
	{}

	/**
	 * @see com.yagasoft.overcast.container.Container#rename(java.lang.String)
	 */
	@Override
	public void rename(String newName)
	{}

	/**
	 * @see com.yagasoft.overcast.container.Container#delete()
	 */
	@Override
	public void delete()
	{}

}
