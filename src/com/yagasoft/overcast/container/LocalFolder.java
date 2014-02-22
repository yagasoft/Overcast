
package com.yagasoft.overcast.container;


import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class LocalFolder extends Folder<Path>
{
	
	public LocalFolder()
	{}
	
	public LocalFolder(Path file)
	{
		sourceObject = file;
		updateFromSource(false, false);
	}
	
	public LocalFolder(String path)
	{
		sourceObject = Paths.get(path);
		updateFromSource(false, false);
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#create()
	 */
	@Override
	public void create()
	{
		try
		{
			Files.createDirectories(sourceObject);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#isExist()
	 */
	@Override
	public boolean isExist()
	{
		if ( !Files.exists(sourceObject) && Files.notExists(sourceObject))
		{
			try
			{
				throw new Exception("Can't determine if folder exists or not.");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return Files.exists(sourceObject);
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#buildTree(int)
	 */
	@Override
	public void buildTree(int numberOfLevels)
	{
		if (numberOfLevels < 0)
		{
			return;
		}
		
		ArrayList<Path> paths = new ArrayList<Path>();
		ArrayList<String> pathsString = new ArrayList<String>();
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceObject))
		{
			for (Path file : stream)
			{
				paths.add(file);
				pathsString.add(file.toAbsolutePath().toString());
			}
		}
		catch (IOException | DirectoryIteratorException e)
		{
			e.printStackTrace();
		}
		
		removeObsolete(pathsString, true);
		
//		HashMap<String, Folder<?>> newFolders = new HashMap<String, Folder<?>>();
//		HashMap<String, File<?>> newFiles = new HashMap<String, File<?>>();
//
		for (Path path : paths)
		{
//			if ( !folders.containsKey(path.toString()))
//			{
//				if ( !files.containsKey(path.toString()))
//				{
			if (Files.isDirectory(path))
			{
				LocalFolder folder = new LocalFolder(path);
				folder.buildTree(numberOfLevels - 1);
				folders.put(folder.id, folder);
				folder.parent = this;
			}
			else
			{
				LocalFile file = new LocalFile(path);
				files.put(file.id, file);
				file.parent = this;
			}
//				}
//				else
//				{
//					newFiles.put(path.toString(), files.get(path.toString()));
//				}
//			}
//			else
//			{
//				Folder<?> folder = folders.get(path.toString());
//				folder.buildTree(numberOfLevels - 1);
//				newFolders.put(folder.id, folder);
//			}
		}
//		
//		folders = newFolders;
//		files = newFiles;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#calculateSize()
	 */
	@Override
	public long calculateSize()
	{
		try
		{
			this.size = FolderHelper.getSize(path);
		}
		catch (IOException e)
		{
			size = 0;
			e.printStackTrace();
		}
		
		return size;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#updateInfo(boolean, boolean)
	 */
	@Override
	public void updateInfo(boolean folderContents, boolean recursively)
	{}
	
	/**
	 * @see com.yagasoft.overcast.container.Folder#updateFromSource(boolean,
	 *      boolean)
	 */
	@Override
	public void updateFromSource(boolean folderContents, boolean recursively)
	{
		if (folderContents)
		{
			buildTree(recursively);
		}
		
		name = sourceObject.getFileName().toString();
		path = sourceObject.toAbsolutePath().toString();
		parent = new LocalFolder(sourceObject.getParent().toString());
		generateId();
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#copy(com.yagasoft.overcast.container.Folder,
	 *      boolean)
	 */
	@Override
	public LocalFolder copy(Folder<?> destination, boolean overwrite)
	{
		FolderHelper.TreeCopier treeCopier = new FolderHelper.TreeCopier(sourceObject, (Path) destination.sourceObject, !overwrite, true);
		
		try
		{
			Files.walkFileTree(sourceObject, treeCopier);
			return new LocalFolder(((Path) destination.sourceObject).resolve(sourceObject.getFileName()));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#move(com.yagasoft.overcast.container.Folder,
	 *      boolean)
	 */
	@Override
	public void move(Folder<?> destination, boolean overwrite)
	{
		FolderHelper.TreeMover treeMover = new FolderHelper.TreeMover(sourceObject, (Path) destination.sourceObject, !overwrite);
		
		try
		{
			Files.walkFileTree(sourceObject, treeMover);
			sourceObject = ((Path) destination.sourceObject).resolve(sourceObject.getFileName());
			updateFromSource();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#rename(java.lang.String)
	 */
	@Override
	public void rename(String newName)
	{
		try
		{
			sourceObject = Files.move(sourceObject, sourceObject.resolveSibling(newName));
			updateFromSource();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#delete()
	 */
	@Override
	public void delete()
	{
		FolderHelper.TreeDeleter treeDeleter = new FolderHelper.TreeDeleter();
		
		try
		{
			Files.walkFileTree(sourceObject, treeDeleter);
			
			if (parent != null)
			{
				parent.remove(this);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#updateInfo()
	 */
	@Override
	public void updateInfo()
	{
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see com.yagasoft.overcast.container.Container#updateFromSource()
	 */
	@Override
	public void updateFromSource()
	{
		throw new UnsupportedOperationException();
	}
	
}
