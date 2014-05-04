/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.content/UpdateEvent.java
 * 
 *			Modified: 04-May-2014 (15:22:34)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.update;


import com.yagasoft.overcast.base.container.Container;
import com.yagasoft.overcast.base.container.Event;


/**
 * An event class containing all the vital information of an update event.
 */
public class UpdateEvent extends Event
{
	
	/**
	 * Instantiates a new update event.
	 *
	 * @param container Container that has been updated.
	 */
	public UpdateEvent(Container<?> container)
	{
		super(container);
	}
	
}
