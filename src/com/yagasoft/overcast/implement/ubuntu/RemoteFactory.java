/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.ubuntu/RemoteFactory.java
 *
 *			Modified: 27-Mar-2014 (16:15:31)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.implement.ubuntu;


import com.ubuntuone.api.files.model.U1Directory;
import com.ubuntuone.api.files.model.U1File;


public class RemoteFactory extends
		com.yagasoft.overcast.base.container.remote.RemoteFactory<U1Directory, RemoteFolder, U1File, RemoteFile>
{

	public RemoteFactory(Ubuntu ubuntu)
	{
		super(ubuntu, RemoteFolder.class, RemoteFile.class, "");
	}

}
