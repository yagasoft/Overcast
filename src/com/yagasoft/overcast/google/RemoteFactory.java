
package com.yagasoft.overcast.google;


import com.google.api.services.drive.model.File;


public class RemoteFactory extends com.yagasoft.overcast.container.remote.RemoteFactory<File, RemoteFolder, File, RemoteFile>
{
	
	public RemoteFactory(Google google)
	{
		super(RemoteFolder.class, RemoteFile.class);
		csp = google;
	}
	
}
