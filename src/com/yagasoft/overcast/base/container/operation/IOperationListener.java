/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.container.operation/IOperationListener.java
 *
 *			Modified: 27-Mar-2014 (16:11:01)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
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
	 * Progress has changed.
	 * 
	 * @param event
	 *            the event object containing info about the change.
	 */
	public void operationProgressChanged(OperationEvent event);
	
}
