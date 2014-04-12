/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.container.transfer/TransferState.java
 * 
 *			Modified: 27-Mar-2014 (16:13:51)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.transfer;


/**
 * Transfer states.
 */
public enum TransferState
{
	INITIALISED,
	IN_PROGRESS,
	CANCELLED,
	FAILED,
	COMPLETED
}
