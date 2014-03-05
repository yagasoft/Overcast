
package com.yagasoft.overcast.ubuntu;


import com.ubuntuone.api.files.model.U1Directory;
import com.ubuntuone.api.files.model.U1File;


public class RemoteFactory extends com.yagasoft.overcast.container.RemoteFactory<U1Directory, RemoteFolder, U1File, RemoteFile>
{

	public RemoteFactory(Ubuntu ubuntu)
	{
		super(RemoteFolder.class, RemoteFile.class);
		csp = ubuntu;
	}

}
