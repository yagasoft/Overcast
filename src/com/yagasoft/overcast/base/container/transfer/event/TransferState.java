/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.transfer/TransferState.java
 * 
 *			Modified: Apr 15, 2014 (9:32:04 AM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.transfer.event;


/**
 * Transfer state names.
 */
public enum TransferState
{
	INITIALISED,
	IN_PROGRESS,
	CANCELLED,
	FAILED,
	COMPLETED
}
