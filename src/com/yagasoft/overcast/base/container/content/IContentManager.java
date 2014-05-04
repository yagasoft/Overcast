/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.content/IContentManager.java
 * 
 *			Modified: May 3, 2014 (8:17:23 AM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.content;


import com.yagasoft.overcast.base.container.Container;


/**
 * Classes that can have contents that could change should implement this interface.
 */
public interface IContentManager
{
	
	/**
	 * Adds all types of change content listener to this container.
	 * 
	 * @param listener
	 *            Listener object.
	 */
	public void addContentListener(IContentListener listener);
	
	/**
	 * Adds a content listener to this container.
	 * 
	 * @param listener
	 *            Listener object.
	 * @param change
	 *            the change type
	 */
	public void addContentListener(IContentListener listener, Change change);
	
	/**
	 * Removes the operation listener.
	 * 
	 * @param listener
	 *            Listener object to be removed.
	 */
	public void removeContentListener(IContentListener listener);
	
	/**
	 * Notify listeners of the change.
	 * 
	 * @param change
	 *            the operation
	 * @param object
	 *            the object of the change
	 */
	public void notifyContentListeners(Change change, Container<?> object);
	
	/**
	 * Clear all listeners of the type of change.
	 * 
	 * @param change
	 *            the change type
	 */
	public void clearContentListeners(Change change);
}
