
package com.yagasoft.overcast.ubuntu;


import java.util.ArrayList;
import java.util.Arrays;

import com.ubuntuone.api.files.model.U1Directory;
import com.ubuntuone.api.files.model.U1File;


public class RemoteFactory extends com.yagasoft.overcast.container.remote.RemoteFactory<U1Directory, RemoteFolder, U1File, RemoteFile>
{

	public RemoteFactory(Ubuntu ubuntu)
	{
		super(RemoteFolder.class, RemoteFile.class);
		csp = ubuntu;
	}

	/**
	 * @see com.yagasoft.overcast.container.remote.RemoteFactory#splitPath(java.lang.String)
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
