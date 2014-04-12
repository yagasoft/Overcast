/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.google/RemoteFactory.java
 * 
 *			Modified: 27-Mar-2014 (16:14:58)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.google;


import com.google.api.services.drive.model.File;


public class RemoteFactory extends com.yagasoft.overcast.base.container.remote.RemoteFactory<File, RemoteFolder, File, RemoteFile>
{
	
	public RemoteFactory(Google google)
	{
		super(RemoteFolder.class, RemoteFile.class);
		csp = google;
	}
	
}
