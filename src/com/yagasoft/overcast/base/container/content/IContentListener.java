/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.content/IContentListener.java
 * 
 *			Modified: May 3, 2014 (8:19:33 AM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.content;


/**
 * The listener interface for receiving IOperation events.
 * The class that is interested in processing a IOperation
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addIOperationListener<code> method. When
 * the IOperation event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see IOperationEvent
 */
public interface IContentListener
{
	
	/**
	 * Contents have changed.
	 * 
	 * @param event
	 *            the event object containing info about the change.
	 */
	public void contentsChanged(ChangeEvent event);
	
}
