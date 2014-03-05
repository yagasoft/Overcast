
package com.yagasoft.overcast.google;


import java.util.ArrayList;
import java.util.Arrays;

import com.google.api.services.drive.model.File;


public class RemoteFactory extends com.yagasoft.overcast.container.RemoteFactory<File, RemoteFolder, File, RemoteFile>
{

	public RemoteFactory(Google google)
	{
		super(RemoteFolder.class, RemoteFile.class);
		csp = google;
	}

	/**
	 * @see com.yagasoft.overcast.container.RemoteFactory#splitPath(java.lang.String)
	 */
	@Override
	protected ArrayList<String> splitPath(String path)
	{
		ArrayList<String> splitPath = new ArrayList<String>(Arrays.asList(path.split("/")));

		if (splitPath.get(0).equals(""))
		{
			splitPath.remove(0);
		}

		if (splitPath.get(0).equals("~"))
		{
			splitPath.remove(0);
		}

		if (splitPath.get(0).equals("Ubuntu One"))
		{
			splitPath.remove(0);
		}

		return splitPath;
	}

}
