/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.implement.google/RemoteFactory.java
 * 
 *			Modified: Apr 15, 2014 (1:55:21 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.google;


import com.google.api.services.drive.model.File;


/**
 * @see com.yagasoft.overcast.base.container.remote.RemoteFactory
 */
public class RemoteFactory extends com.yagasoft.overcast.base.container.remote.RemoteFactory<File, RemoteFolder, File, RemoteFile>
{
	
	public RemoteFactory(Google google)
	{
		super(google, RemoteFolder.class, RemoteFile.class, "");
	}
	
}
