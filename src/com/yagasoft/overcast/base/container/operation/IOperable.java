/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.operation/IOperable.java
 *
 *			Modified: 25-May-2014 (13:03:37)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.operation;


/**
 * Classes that can have operations performed on them should implement this interface.
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
	 * Adds a temp operation listener to the operations in this container.
	 *
	 * @param listener
	 *            Listener object.
	 * @param operation
	 *            the operation
	 */
	public void addTempOperationListener(IOperationListener listener, Operation operation);

	/**
	 * Removes the operation listener.
	 *
	 * @param listener
	 *            Listener object to be removed.
	 */
	public void removeOperationListener(IOperationListener listener);

	/**
	 * Removes the operation from the listener list.
	 *
	 * @param listener            Listener object to be removed.
	 * @param operation Operation.
	 */
	public void removeOperationListener(IOperationListener listener, Operation operation);

	/**
	 * Removes the temp operation listener.
	 *
	 * @param listener
	 *            Listener object to be removed.
	 */
	public void removeTempOperationListener(IOperationListener listener);

	/**
	 * Removes the operation from the temp listener list.
	 *
	 * @param listener            Listener object to be removed.
	 * @param operation Operation.
	 */
	public void removeTempOperationListener(IOperationListener listener, Operation operation);

	/**
	 * Notify listeners of the state (stated, cancelled, ...etc.) of the operation, and the progress in the range from 0 to 1.
	 *
	 * @param operation
	 *            the operation
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
