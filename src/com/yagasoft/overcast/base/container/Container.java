/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			Licence terms are in a separate file (LICENCE.md)
 *
 *		Project/File: Overcast/com.yagasoft.overcast.base.container/Container.java
 *
 *			Modified: 06-May-2014 (04:08:37)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container;


import java.util.HashMap;
import java.util.HashSet;

import com.yagasoft.overcast.base.container.operation.IOperable;
import com.yagasoft.overcast.base.container.operation.IOperationListener;
import com.yagasoft.overcast.base.container.operation.Operation;
import com.yagasoft.overcast.base.container.operation.OperationEvent;
import com.yagasoft.overcast.base.container.operation.OperationState;
import com.yagasoft.overcast.base.container.update.IUpdatable;
import com.yagasoft.overcast.base.container.update.IUpdateListener;
import com.yagasoft.overcast.base.container.update.UpdateEvent;
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
public abstract class Container<T> implements IOperable, IUpdatable, Comparable<Container<T>>
{

	/** Unique identifier for the container -- implementation specific. */
	protected String											id;

	/** Name of the container. */
	protected String											name;

	/** Path of the container at the source, including its name. */
	protected String											path;

	/** Path prefix, which will be used to clean-up the path sent by the CSP -- for API path standardisation. */
	protected String											pathPrefix;

	/** Size of the container in bytes. */
	protected long												size;

	/** Container object created by the original API of the CSP. */
	protected T													sourceObject;

	/** Parent folder containing this container. */
	protected Folder<?>											parent;

	/** Listeners to the operations in this container. */
	protected HashMap<IOperationListener, HashSet<Operation>>	operationListeners	= new HashMap<IOperationListener, HashSet<Operation>>();

	/** Listeners to the meta-data updates of this container. */
	protected HashSet<IUpdateListener>							updateListeners		= new HashSet<IUpdateListener>();

	/** CSP object related to this container, or where the container is stored at. */
	protected CSP<T, ?, ?>										csp;

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
	 * Update the fields (class attributes) in this file object from the in-memory info (nothing is done outside the program). <br />
	 * Set the path, at the end of the method implementation, in this preferred format to make it standardised across the API:
	 *
	 * <pre>
	 * path = ((parent == null || parent.getPath().equals(&quot;/&quot;)) ? &quot;/&quot; : (parent.getPath() + &quot;/&quot;)) + name;
	 * cleanPath();		// remove the prefix.
	 * </pre>
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

	// //////////////////////////////////////////////////////////////////////////////////////
	// #region Listeners.
	// ======================================================================================

	/**
	 * @see com.yagasoft.overcast.base.container.operation.IOperable#addOperationListener(com.yagasoft.overcast.base.container.operation.IOperationListener,
	 *      Operation)
	 */
	@Override
	public void addOperationListener(IOperationListener listener, Operation operation)
	{
		// add operation listener if not already added, which prevents overwriting the value associated with it in the map.
		if ( !operationListeners.containsKey(listener))
		{
			operationListeners.put(listener, new HashSet<Operation>());
		}

		// add the operation to the set associated to the key.
		operationListeners.get(listener).add(operation);
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
			if (operationListeners.get(listener).contains(operation))
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
			// does the set contain this kind of operation.
			if (operationListeners.get(listener).contains(operation))
			{
				// remove the operation from the set.
				operationListeners.get(listener).remove(operation);

				// if the operations set is empty, then remove the listener.
				if (operationListeners.get(listener).isEmpty())
				{
					removeOperationListener(listener);
				}
			}
		}
	}

	/**
	 * Remove all types of listeners that was added to this container before.<br />
	 * Override this in sub-classes if more types of listeners were added.
	 */
	public void clearAllListeners()
	{
		operationListeners.clear();
		updateListeners.clear();
	}

	/**
	 * @see com.yagasoft.overcast.base.container.update.IUpdatable#addUpdateListener(com.yagasoft.overcast.base.container.update.IUpdateListener)
	 */
	@Override
	public void addUpdateListener(IUpdateListener listener)
	{
		updateListeners.add(listener);
	}

	/**
	 * @see com.yagasoft.overcast.base.container.update.IUpdatable#removeUpdateListener(com.yagasoft.overcast.base.container.update.IUpdateListener)
	 */
	@Override
	public void removeUpdateListener(IUpdateListener listener)
	{
		updateListeners.remove(listener);
	}

	/**
	 * @see com.yagasoft.overcast.base.container.update.IUpdatable#notifyUpdateListeners()
	 */
	@Override
	public void notifyUpdateListeners()
	{
		// go through the listeners' list and notify whoever is concerned with this update.
		for (IUpdateListener listener : updateListeners)
		{
			listener.containerUpdated(new UpdateEvent(this));
		}
	}

	/**
	 * @see com.yagasoft.overcast.base.container.update.IUpdatable#clearUpdateListeners()
	 */
	@Override
	public void clearUpdateListeners()
	{
		updateListeners.clear();
	}

	// ======================================================================================
	// #endregion Listeners.
	// //////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Removes the prefix from the path to make it standardised with this API's paths.<br />
	 * It should be added before communicating with the service using its path format.
	 */
	protected void cleanPath()
	{
		if (((path != null) && (path != "/")) && path.startsWith(pathPrefix))
		{
			path.substring(pathPrefix.length(), path.length() - 1);
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
		return path.toLowerCase().compareTo(container.path.toLowerCase());
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
		updateInfo();		// the path is affected.
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
	 * Sets the path. I advise against using this manually.
	 *
	 * @param value
	 *            the new path
	 */
	public void setPath(String value)
	{
		this.path = value;
		cleanPath();
	}

	/**
	 * @return the pathPrefix
	 */
	public String getPathPrefix()
	{
		return pathPrefix;
	}

	/**
	 * @param pathPrefix
	 *            the pathPrefix to set
	 */
	public void setPathPrefix(String pathPrefix)
	{
		this.pathPrefix = pathPrefix;
		cleanPath();
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
		updateInfo();		// all info are affected. (fields)
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
		updateInfo();
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
