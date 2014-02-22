
package com.yagasoft.overcast.google;


import java.io.IOException;
import java.util.ArrayList;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.drive.Drive.Children;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.yagasoft.overcast.container.Container;
import com.yagasoft.overcast.container.Folder;


public class RemoteFolder extends com.yagasoft.overcast.container.RemoteFolder<File> implements IRemote
{

	public RemoteFolder()
	{
		super();
	}

	public RemoteFolder(File folder)
	{
		super(folder);
	}

	/**
	 * @see com.yagasoft.overcast.container.Folder#create()
	 */
	@Override
	public void create()
	{
		
	}

	/**
	 * @see com.yagasoft.overcast.container.Container#isExist()
	 */
	@Override
	public boolean isExist()
	{
		return false;
	}

	/**
	 * @see com.yagasoft.overcast.container.Folder#buildTree(int)
	 */
	@Override
	public void buildTree(final int numberOfLevels)
	{
		if (numberOfLevels < 0)
		{
			return;
		}

		try
		{
			Children.List request = Google.driveService.children().list(id);
			request.setQ("trashed=false");

			ArrayList<String> childrenIds = new ArrayList<String>();

			do
			{
				try
				{
					ChildList children = request.execute();

					for (ChildReference child : children.getItems())
					{
						childrenIds.add(child.getId());
					}

					request.setPageToken(children.getNextPageToken());
				}
				catch (IOException e)
				{
					System.out.println("An error occurred: " + e);
					request.setPageToken(null);
				}
			} while ((request.getPageToken() != null) && (request.getPageToken().length() > 0));

			removeObsolete(childrenIds, true);

			if (!childrenIds.isEmpty())
			{
				BatchRequest batch = Google.driveService.batch();

				JsonBatchCallback<File> callback = new JsonBatchCallback<File>()
				{

					@Override
					public void onSuccess(File remote, HttpHeaders responseHeaders)
					{
						if (remote.getMimeType().indexOf("folder") >= 0)
						{
							RemoteFolder folder = new RemoteFolder(remote);
							add(folder);

							System.out.println("Folder: " + folder.parent.getName() + "\\" + folder.name + " => " + folder.id);

							folder.buildTree(numberOfLevels - 1);
						}
						else
						{
							RemoteFile file = new RemoteFile(remote);
							add(file);

							System.out.println("File: " + name + "\\" + file.getName() + " => " + file.getId());
						}
					}

					@Override
					public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders)
					{
						System.out.println("Error Message: " + e.getMessage());
					}
				};

				for (String childId : childrenIds)
				{
					Google.driveService.files().get(childId).queue(batch, callback);
				}

				batch.execute();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
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
		id = sourceObject.getId();
		name = sourceObject.getTitle();

//		try
//		{
//			link= new URL(sourceObject.getDownloadUrl());
//		}
//		catch (MalformedURLException e)
//		{
//			e.printStackTrace();
//		}
	}

	/**
	 * @see com.yagasoft.overcast.container.Folder#updateFromSource(boolean,
	 *      boolean)
	 */
	@Override
	public void updateFromSource(boolean folderContents, final boolean recursively)
	{
		if (folderContents)
		{
			buildTree(recursively);
		}
	}

	/**
	 * @see com.yagasoft.overcast.container.Container#copy(com.yagasoft.overcast.container.Folder,
	 *      boolean)
	 */
	@Override
	public Container<?> copy(Folder<?> destination, boolean overwrite)
	{
		return null;
	}

	/**
	 * @see com.yagasoft.overcast.container.Container#move(com.yagasoft.overcast.container.Folder,
	 *      boolean)
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

	/**
	 * @see com.yagasoft.overcast.container.Container#updateInfo()
	 */
	@Override
	public void updateInfo()
	{}

	/**
	 * @see com.yagasoft.overcast.container.Container#updateFromSource()
	 */
	@Override
	public void updateFromSource()
	{}
}
