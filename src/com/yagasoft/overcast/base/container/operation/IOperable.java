/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container.operation/IOperable.java
 *
 *			Modified: 28-Jun-2014 (22:09:31)
 *			   Using: Eclipse J-EE / JDK 8 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container.operation;


import java.util.Arrays;

import com.yagasoft.overcast.base.container.Container;


/**
 * Classes that can have operations performed on them should implement this interface.
 * Out of all the 'notify' methods, only {@link #notifyOperationListeners(Operation, OperationState, float, Container)} should be
 * implemented, the rest have a default suitable behaviour.
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
	void addOperationListener(IOperationListener listener, Operation operation);

	/**
	 * Adds the operation listener for all possible operations.
	 *
	 * @param listener
	 *            Listener.
	 */
	default void addOperationListener(IOperationListener listener)
	{
		Arrays.stream(Operation.values()).forEach(operation -> addOperationListener(listener, operation));
	}

	/**
	 * Adds the operation listeners for all possible operations.
	 *
	 * @param listeners
	 *            Listeners.
	 */
	default void addOperationListeners(IOperationListener... listeners)
	{
		Arrays.stream(listeners).forEach(this::addOperationListener);
	}

	/**
	 * Adds the operation listeners for this operation.
	 *
	 * @param operation
	 *            Operation.
	 * @param listeners
	 *            Listeners.
	 */
	default void addOperationListeners(Operation operation, IOperationListener... listeners)
	{
		Arrays.stream(listeners).forEach(listener -> addOperationListener(listener, operation));
	}

	/**
	 * Removes the operation listener.
	 *
	 * @param listener
	 *            Listener object to be removed.
	 */
	void removeOperationListener(IOperationListener listener);

	/**
	 * Removes the operation from the listener list.
	 *
	 * @param listener
	 *            Listener object to be removed.
	 * @param operation
	 *            Operation.
	 */
	void removeOperationListener(IOperationListener listener, Operation operation);

	/**
	 * Adds a temp operation listener to the operations in this container.
	 *
	 * @param operation
	 *            the operation
	 * @param listeners
	 *            Listeners.
	 */
	void addTempOperationListeners(Operation operation, IOperationListener... listeners);

	/**
	 * Removes the temp operation listener.
	 *
	 * @param listeners
	 *            Listeners.
	 */
	void removeTempOperationListeners(IOperationListener... listeners);

	/**
	 * Removes the operation from the temp listener list.
	 *
	 * @param operation
	 *            Operation.
	 * @param listeners
	 *            Listeners.
	 */
	void removeTempOperationListeners(Operation operation, IOperationListener... listeners);

	/**
	 * Notify operation listeners.
	 */
	default void notifyOperationListeners()
	{
		notifyOperationListeners(Operation.UPDATE, null);
	}

	/**
	 * Notify operation listeners.
	 *
	 * @param operation
	 *            Operation.
	 * @param object
	 *            Object.
	 */
	default void notifyOperationListeners(Operation operation, Container<?> object)
	{
		notifyOperationListeners(operation, null, 1.0f, object);
	}

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
	default void notifyOperationListeners(Operation operation, OperationState state, float progress)
	{
		notifyOperationListeners(operation, state, progress, null);
	}

	/**
	 * Notify operation listeners.
	 *
	 * @param operation
	 *            Operation.
	 * @param state
	 *            State.
	 * @param progress
	 *            Progress.
	 * @param object
	 *            Object.
	 */
	void notifyOperationListeners(Operation operation, OperationState state, float progress, Container<?> object);

	/**
	 * Clear all listeners to the operations.
	 *
	 * @param operation
	 *            the operation
	 */
	void clearOperationListeners(Operation operation);
}
