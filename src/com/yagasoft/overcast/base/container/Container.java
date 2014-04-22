/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container/Container.java
 *
 *			Modified: Apr 14, 2014 (2:21:34 PM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container;


import java.util.HashMap;

import com.yagasoft.overcast.base.container.operation.IOperable;
import com.yagasoft.overcast.base.container.operation.IOperationListener;
import com.yagasoft.overcast.base.container.operation.Operation;
import com.yagasoft.overcast.base.container.operation.OperationEvent;
import com.yagasoft.overcast.base.container.operation.OperationState;
import com.yagasoft.overcast.base.csp.CSP;
import com.yagasoft.overcast.exception.AccessException;
import com.yagasoft.overcast.exception.OperationException;


/**
 * A class representing the common attributes and operation of the files and folders.<br />
 * All methods in this class (or subclasses) must be synchronised in the implementation to prevent conflicting operations.
 * 
 * @param <T>
 *            the type of the file or folder in the original API of the CSP.
 */
public abstract class Container<T> implements IOperable, Comparable<Container<T>>
{
	
	/** Unique identifier for the container -- implementation specific. */
	protected String									id;
	
	/** Name of the container. */
	protected String									name;
	
	/** Path of the container at the source, including its name. */
	protected String									path;
	
	/** Size of the container in bytes. */
	protected long										size;
	
	/** Container object created by the original API of the CSP. */
	protected T											sourceObject;
	
	/** Parent folder containing this container. */
	protected Folder<?>									parent;
	
	/** Listeners to the operations in this container. */
	protected HashMap<IOperationListener, Operation>	operationListeners	= new HashMap<IOperationListener, Operation>();
	
	/** CSP object related to this container, or where the container is stored at. */
	protected CSP<T, ?, ?>								csp;
	
	/**
	 * Generate unique ID for this container.
	 */
	public abstract void generateId();
	
	/**
	 * Checks if the file exists physically or not.
	 * 
	 * @return true, if it exists
	 * @throws AccessException
	 *             Can't access the container to determine its existence.
	 * @throws OperationException
	 *             the operation exception
	 */
	public abstract boolean isExist() throws AccessException, OperationException;
	
	/**
	 * Is this a folder?.
	 * 
	 * @return true, if it is a folder
	 */
	public abstract boolean isFolder();
	
	/**
	 * Update the fields (class attributes) in this file object from the in-memory info (nothing is done outside the program).
	 */
	public abstract void updateInfo();
	
	/**
	 * Update from where the container resides. It reads the meta of the container.<br />
	 * For folders, it reads the children list. It might go online to do both.
	 * 
	 * @throws OperationException
	 *             the operation exception
	 */
	public abstract void updateFromSource() throws OperationException;
	
	/**
	 * Copy this container to the destination folder.
	 * 
	 * @param destination
	 *            Destination folder.
	 * @param overwrite
	 *            Overwrite existing container at the destination.
	 * @param listener
	 *            the listener
	 * @return Container object at the destination.
	 * @throws OperationException
	 *             the operation exception
	 */
	public abstract Container<?> copy(Folder<?> destination, boolean overwrite, IOperationListener listener)
			throws OperationException;
	
	/**
	 * Move this container to the destination folder.
	 * 
	 * @param destination
	 *            Destination folder.
	 * @param overwrite
	 *            Overwrite existing container at the destination.
	 * @param listener
	 *            the listener
	 * @throws OperationException
	 *             the operation exception
	 */
	public abstract void move(Folder<?> destination, boolean overwrite, IOperationListener listener) throws OperationException;
	
	/**
	 * Rename this container.
	 * 
	 * @param newName
	 *            The new name.
	 * @param listener
	 *            the listener
	 * @throws OperationException
	 *             the operation exception
	 */
	public abstract void rename(String newName, IOperationListener listener) throws OperationException;
	
	/**
	 * Delete this container.
	 * 
	 * @param listener
	 *            the listener
	 * @throws OperationException
	 *             the operation exception
	 */
	public abstract void delete(IOperationListener listener) throws OperationException;
	
	/**
	 * @see com.yagasoft.overcast.base.container.operation.IOperable#addOperationListener(com.yagasoft.overcast.base.container.operation.IOperationListener,
	 *      Operation)
	 */
	@Override
	public void addOperationListener(IOperationListener listener, Operation operation)
	{
		operationListeners.put(listener, operation);
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.operation.IOperable#removeOperationListener(com.yagasoft.overcast.base.container.operation.IOperationListener)
	 */
	@Override
	public void removeOperationListener(IOperationListener listener)
	{
		operationListeners.remove(listener);
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.operation.IOperable#notifyOperationListeners(Operation,
	 *      com.yagasoft.overcast.base.container.operation.OperationState, float)
	 */
	@Override
	public void notifyOperationListeners(Operation operation, OperationState state, float progress)
	{
		// go through the listeners' list and notify whoever is concerned with this operation.
		for (IOperationListener listener : operationListeners.keySet())
		{
			if (operationListeners.get(listener) == operation)
			{
				listener.operationProgressChanged(new OperationEvent(this, operation, state, progress));
				
				// if the operation has ended, remove its listener.
				if ((state == OperationState.COMPLETED) || (state == OperationState.FAILED))
				{
					removeOperationListener(listener);
				}
			}
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.base.container.operation.IOperable#clearOperationListeners(com.yagasoft.overcast.base.container.operation.Operation)
	 */
	@Override
	public void clearOperationListeners(Operation operation)
	{
		for (IOperationListener listener : operationListeners.keySet())
		{
			if (operationListeners.get(listener) == operation)
			{
				removeOperationListener(listener);
			}
		}
	}
	
	/**
	 * Checks if the object passed is identical to this one. It checks if it's a container in the first place, and if so, checks
	 * the ID, and as it's unique, there won't be conflicts.
	 * 
	 * @param object
	 *            Object to compare.
	 * @return true, if they're identical
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object)
	{
		return ((object instanceof Container) && (((Container<?>) object).id.equalsIgnoreCase(getId())));
	}
	
	/**
	 * Compares names, used for sorting.
	 * 
	 * @param container
	 *            the container
	 * @return -1 if less than alphabetically, 0 if identical, 1 if higher.
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	@Override
	public int compareTo(Container<T> container)
	{
		return name.compareTo(container.name);
	}
	
	/**
	 * Returns the name of the container.
	 * 
	 * @return the name
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return name;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Getters and setters.
	// ======================================================================================
	
	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(String id)
	{
		this.id = id;
	}
	
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param value
	 *            the new name
	 */
	public void setName(String value)
	{
		this.name = value;
	}
	
	/**
	 * Gets the path.
	 * 
	 * @return the path
	 */
	public String getPath()
	{
		return this.path;
	}
	
	/**
	 * Sets the path.
	 * 
	 * @param value
	 *            the new path
	 */
	public void setPath(String value)
	{
		this.path = value;
	}
	
	/**
	 * Gets the size.
	 * 
	 * @return the size
	 */
	public long getSize()
	{
		return size;
	}
	
	/**
	 * Sets the size.
	 * 
	 * @param size
	 *            the size to set
	 */
	public void setSize(long size)
	{
		this.size = size;
	}
	
	/**
	 * Gets the source object.
	 * 
	 * @return the sourceObject
	 */
	public T getSourceObject()
	{
		return sourceObject;
	}
	
	/**
	 * Sets the source object, and updates the info.
	 * 
	 * @param sourceObject
	 *            the sourceObject to set
	 */
	public void setSourceObject(T sourceObject)
	{
		this.sourceObject = sourceObject;
		updateInfo();
	}
	
	/**
	 * Gets the parent.
	 * 
	 * @return the parent
	 */
	public Folder<?> getParent()
	{
		return parent;
	}
	
	/**
	 * Sets the parent.
	 * 
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(Folder<?> parent)
	{
		this.parent = parent;
	}
	
	/**
	 * Gets the csp.
	 * 
	 * @return the csp
	 */
	public CSP<T, ?, ?> getCsp()
	{
		return csp;
	}
	
	/**
	 * Sets the csp.
	 * 
	 * @param csp
	 *            the csp to set
	 */
	public void setCsp(CSP<T, ?, ?> csp)
	{
		this.csp = csp;
	}
	
	// ======================================================================================
	// #endregion Getters and setters.
	// //////////////////////////////////////////////////////////////////////////////////////
	
}
