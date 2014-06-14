/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.operation/IOperationListener.java
 *
 *			Modified: 28-May-2014 (00:05:49)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.operation;


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
public interface IOperationListener
{
	
	/**
	 * Progress has changed. It might also be invoked in case of success or failure.
	 *
	 * @param event
	 *            the event object containing info about the change.
	 */
	public void operationChange(OperationEvent event);
	
}
