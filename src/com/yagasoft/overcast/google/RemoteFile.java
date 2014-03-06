
package com.yagasoft.overcast.google;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.api.services.drive.model.File;
import com.yagasoft.overcast.container.Container;
import com.yagasoft.overcast.container.Folder;


public class RemoteFile extends com.yagasoft.overcast.container.remote.RemoteFile<File>
{
	
	/**
	 * Better use the factory in Google class.
	 */
	public RemoteFile()
	{}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#isExist()
	 */
	@Override
	public boolean isExist()
	{
		return false;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#updateInfo()
	 */
	@Override
	public void updateInfo()
	{
		id = sourceObject.getId();
		name = sourceObject.getTitle();
		type = sourceObject.getMimeType();
		
		try
		{
			link = new URL(sourceObject.getDownloadUrl());
		}
		catch (MalformedURLException e)
		{
			link = null;
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#updateFromSource()
	 */
	@Override
	public void updateFromSource()
	{
		try
		{
			sourceObject = Google.driveService.files().get((sourceObject == null) ? id : sourceObject.getId()).execute();
			updateInfo();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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
	{
		try
		{
			Google.getDriveService().children().delete(parent.getId(), id).execute();
			parent.remove(this);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
}
