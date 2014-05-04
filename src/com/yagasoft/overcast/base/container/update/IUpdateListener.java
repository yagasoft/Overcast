/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.content/IUpdateListener.java
 *
 *			Modified: 04-May-2014 (15:20:49)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.update;




/**
 * The listener interface for receiving IUpdate events.
 * The class that is interested in processing a IUpdate
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addIUpdateListener<code> method. When
 * the IUpdate event occurs, that object's appropriate
 * method is invoked.
 *
 * @see IUpdateEvent
 */
public interface IUpdateListener
{

	/**
	 * Meta-data has changed.
	 *
	 * @param event
	 *            the event object containing info about the change.
	 */
	public void containerUpdated(UpdateEvent event);
}




