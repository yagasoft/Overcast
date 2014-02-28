
package com.yagasoft.overcast.google;


import java.util.ArrayList;
import java.util.Arrays;

import com.google.api.services.drive.model.File;
import com.yagasoft.overcast.container.Container;
import com.yagasoft.overcast.container.IRemote;


@SuppressWarnings("unchecked")
public class RemoteFactory extends com.yagasoft.overcast.container.RemoteFactory<File, File>
{
	
	public RemoteFactory(Google google)
	{
		csp = google;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.RemoteFactory#createObject(java.lang.Class)
	 */
	@Override
	public <U extends Container<File> & IRemote> U createObject(Class<U> type)
	{
		try
		{
			U container = type.newInstance();
			container.setCsp(csp);
			
			return container;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.RemoteFactory#createObject(java.lang.Class,
	 *      java.lang.Object)
	 */
	@Override
	public <U extends Container<File> & IRemote> U createObject(Class<U> type, File sourceObject)
	{
		try
		{
			U container = type.newInstance();
			container.setSourceObject(sourceObject);
			container.setCsp(csp);
			container.updateFromSource();
			
			return container;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.RemoteFactory#createObjectById(java.lang.Class,
	 *      java.lang.String)
	 */
	@Override
	public <U extends Container<File> & IRemote> U createObjectById(Class<U> type, String id)
	{
		try
		{
			U container = type.newInstance();
			container.setId(id);
			container.setCsp(csp);
			container.updateFromSource();
			
			return container;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.RemoteFactory#createObjectByPath(java.lang.Class,
	 *      java.lang.String)
	 */
	@Override
	public <U extends Container<File> & IRemote> U createObjectByPath(Class<U> type, String path)
	{
		try
		{
			ArrayList<String> splitPath = new ArrayList<String>(Arrays.asList(path.split("/")));
			
			if (splitPath.get(0).equals(""))
			{
				splitPath.remove(0);
			}
			
			U container = type.newInstance();
			container.setName(splitPath.remove(splitPath.size() - 1));
			container.setCsp(csp);
			
			Container<?> result = searchForContainer(splitPath, container.getName());
			
			if ((result == null)
					|| !(result.isFolder() && (container instanceof RemoteFolder))
					|| !( !result.isFolder() && (container instanceof RemoteFile)))
			{
				return container;
			}
			else
			{
				return (U) result;
			}
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private Container<?> searchForContainer(ArrayList<String> splitPath, String name)
	{
		RemoteFolder result = (RemoteFolder) csp.getRemoteFileTree();
		
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
}
