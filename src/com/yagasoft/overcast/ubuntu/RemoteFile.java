
package com.yagasoft.overcast.ubuntu;


import java.net.MalformedURLException;
import java.net.URL;

import com.ubuntuone.api.files.model.U1File;
import com.ubuntuone.api.files.model.U1Node;
import com.ubuntuone.api.files.request.U1NodeListener;
import com.ubuntuone.api.files.util.U1Failure;
import com.yagasoft.overcast.container.Container;
import com.yagasoft.overcast.container.Folder;
import com.yagasoft.overcast.exception.AccessException;


public class RemoteFile extends com.yagasoft.overcast.container.remote.RemoteFile<U1File>
{
	
	/**
	 * Better use the factory in Ubuntu class.
	 */
	public RemoteFile()
	{}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#generateId()
	 */
	@Override
	public void generateId()
	{
		
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#isExist()
	 */
	@Override
	public boolean isExist() throws AccessException
	{
		return false;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#updateInfo()
	 */
	@Override
	public void updateInfo()
	{
		id = sourceObject.getKey();
		name = sourceObject.getName();
		path = sourceObject.getResourcePath();
		type = sourceObject.getKind().toString();
		
		try
		{
			link = new URL("https://files.one.ubuntu.com/" + sourceObject.getKey());
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
		Ubuntu.ubuntuService.getNode((sourceObject == null) ? path : sourceObject.getResourcePath(), new U1NodeListener()
		{
			
			@Override
			public void onSuccess(U1Node node)
			{
				sourceObject = (U1File) node;
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
