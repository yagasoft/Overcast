/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.content/IUpdatable.java
 *
 *			Modified: 04-May-2014 (15:19:41)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.update;


import com.yagasoft.overcast.base.container.Container;


/**
 * Classes that can be updated should implement this interface.
 */
public interface IUpdatable
{
	
	/**
	 * Adds an update listener to this container.
	 * 
	 * @param listener
	 *            Listener object.
	 */
	public void addUpdateListener(IUpdateListener listener);
	
	/**
	 * Removes the operation listener.
	 * 
	 * @param listener
	 *            Listener object to be removed.
	 */
	public void removeUpdateListener(IUpdateListener listener);
	
	/**
	 * Notify listeners of the update.
	 * This is used for changes to the container itself,
	 * like when the {@link Container#setSourceObject(Object)} is called.
	 */
	public void notifyUpdateListeners();
	
	/**
	 * Clear all listeners for updates.
	 */
	public void clearUpdateListeners();
}
