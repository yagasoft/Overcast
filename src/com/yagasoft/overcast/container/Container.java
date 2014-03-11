/*
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 *
 *		Modified MIT License (GPL v3 compatible)
 * 			License terms are in a separate file (license.txt)
 *
 */

package com.yagasoft.overcast.container;


import java.util.HashMap;

import com.yagasoft.overcast.CSP;
import com.yagasoft.overcast.container.transfer.ITransferProgressListener;
import com.yagasoft.overcast.container.transfer.TransferState;
import com.yagasoft.overcast.container.transfer.ITransferrable;
import com.yagasoft.overcast.container.transfer.TransferEvent;
import com.yagasoft.overcast.exception.AccessException;


/**
 * A class representing the common attributes and operation of the files and folders.
 *
 * @param <T>
 *            the type of the file or folder in the original API of the CSP.
 */
public abstract class Container<T> implements ITransferrable
{

	/** Unique identifier for the container -- implementation specific. */
	protected String										id;

	/** Name of the container. */
	protected String										name;

	/** Path of the container at the source, including its name. */
	protected String										path;

	/** Size of the container in bytes. */
	protected long											size;

	/** Source object created by the original API of the CSP. */
	protected T												sourceObject;

	/** Parent folder containing this container. */
	protected Folder<?>										parent;

	/** Progress listeners to the download or upload of this container. */
	protected HashMap<ITransferProgressListener, Object>	progressListeners
			= new HashMap<ITransferProgressListener, Object>();

	/** CSP object related to this container, or where the container is stored at. */
	protected CSP<T, ?, ?>									csp;

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
	 */
	public abstract boolean isExist() throws AccessException;

	/**
	 * Is this a folder?
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
	 * It might go online to do it.
	 */
	public abstract void updateFromSource();

	/**
	 * Copy this container to the destination folder.
	 *
	 * @param destination
	 *            Destination folder.
	 * @param overwrite
	 *            Overwrite existing container at the destination.
	 * @return Container object at the destination.
	 */
	public abstract Container<?> copy(Folder<?> destination, boolean overwrite);

	/**
	 * Move this container to the destination folder.
	 *
	 * @param destination
	 *            Destination folder.
	 * @param overwrite
	 *            Overwrite existing container at the destination.
	 */
	public abstract void move(Folder<?> destination, boolean overwrite);

	/**
	 * Rename this container.
	 *
	 * @param newName
	 *            The new name.
	 */
	public abstract void rename(String newName);

	/**
	 * Delete this container.
	 */
	public abstract void delete();

	/**
	 * @see com.yagasoft.overcast.container.transfer.ITransferrable#addProgressListener(
	 * 		com.yagasoft.overcast.container.transfer.ITransferProgressListener,
	 *      java.lang.Object)
	 */
	@Override
	public void addProgressListener(ITransferProgressListener listener, Object object)
	{
		progressListeners.put(listener, object);
	}

	/**
	 * @see com.yagasoft.overcast.container.transfer.ITransferrable#removeProgressListener(
	 * 		com.yagasoft.overcast.container.transfer.ITransferProgressListener)
	 */
	@Override
	public void removeProgressListener(ITransferProgressListener listener)
	{
		progressListeners.remove(listener);
	}

	/**
	 * @see com.yagasoft.overcast.container.transfer.ITransferrable#notifyListeners(
	 * 		com.yagasoft.overcast.container.transfer.TransferState,float)
	 */
	@Override
	public void notifyListeners(TransferState state, float progress)
	{
		for (ITransferProgressListener listener : progressListeners.keySet())
		{
			listener.progressChanged(new TransferEvent(this, state, progress, progressListeners.get(listener)));
		}

		if (state == TransferState.COMPLETED)
		{
			clearListeners();
		}
	}

	/**
	 * @see com.yagasoft.overcast.container.transfer.ITransferrable#clearListeners()
	 */
	@Override
	public void clearListeners()
	{
		progressListeners.clear();
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
	 * Sets the source object.
	 *
	 * @param sourceObject
	 *            the sourceObject to set
	 */
	public void setSourceObject(T sourceObject)
	{
		this.sourceObject = sourceObject;
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
