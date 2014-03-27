/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.container.operation/IOperable.java
 * 
 *			Modified: 27-Mar-2014 (16:11:10)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.container.operation;


/**
 * The Interface IOperable.
 */
public interface IOperable
{
	
	/**
	 * Adds a operation listener to the operations in this container.
	 * 
	 * @param listener
	 *            Listener object.
	 * @param operation
	 *            the operation
	 */
	public void addOperationListener(IOperationListener listener, Operation operation);
	
	/**
	 * Removes the operation listener.
	 * 
	 * @param listener
	 *            Listener object to be removed.
	 */
	public void removeOperationListener(IOperationListener listener);
	
	/**
	 * Notify listeners of the state (stated, cancelled, ...etc.) of the operation, and the progress in the range from 0 to 1.
	 * 
	 * @param operation
	 *            TODO
	 * @param state
	 *            State of the operation.
	 * @param progress
	 *            the progress
	 */
	public void notifyOperationListeners(Operation operation, OperationState state, float progress);
	
	/**
	 * Clear all listeners to the operations.
	 * 
	 * @param operation
	 *            the operation
	 */
	public void clearOperationListeners(Operation operation);
}
