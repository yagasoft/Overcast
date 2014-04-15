/* 
 * Copyright (C) 2011-2014 by Ahmed Osama el-Sawalhy
 * 
 *		The Modified MIT Licence (GPL v3 compatible)
 * 			License terms are in a separate file (LICENCE.md)
 * 
 *		Project/File: Overcast/com.yagasoft.overcast.base.container/FolderHelper.java
 * 
 *			Modified: Apr 15, 2014 (8:06:38 AM)
 *			   Using: Eclipse J-EE / JDK 7 / Windows 8.1 x64
 */

package com.yagasoft.overcast.base.container;


/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * (Please note: I've modified this code for Overcast from Oracle's original)
 */

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Methods that perform operations related to folders.
 */
public abstract class FolderHelper
{
	
	// --------------------------------------------------------------------------------------
	// #region copy.
	
	/**
	 * Copy source file to target location. If {@code prompt} is true then prompt user to overwrite target if it exists. The
	 * {@code preserve} parameter determines if file attributes should be copied/preserved.
	 */
	private static void copyFile(Path source, Path target, boolean prompt, boolean preserve)
	{
		CopyOption[] options = (preserve) ?
				new CopyOption[] { COPY_ATTRIBUTES, REPLACE_EXISTING } :
				new CopyOption[] { REPLACE_EXISTING };
		if ( !prompt || Files.notExists(target)) // || okayToOverwrite(target))
		{
			try
			{
				Files.copy(source, target, options);
			}
			catch (IOException x)
			{
				System.err.format("Unable to copy: %s: %s%n", source, x);
			}
		}
	}
	
	/**
	 * A {@code FileVisitor} that copies a file-tree ("cp -r")
	 */
	public static class TreeCopier implements FileVisitor<Path>
	{
		
		private final Path		source;
		private final Path		target;
		private final boolean	prompt;
		private final boolean	preserve;
		
		public TreeCopier(Path source, Path target, boolean prompt, boolean preserve)
		{
			this.source = source;
			this.target = target;
			this.prompt = prompt;
			this.preserve = preserve;
		}
		
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
		{
			// before visiting entries in a directory we copy the directory
			// (okay if directory already exists).
			CopyOption[] options = (preserve) ?
					new CopyOption[] { COPY_ATTRIBUTES } : new CopyOption[0];
			
			Path newdir = target.resolve(source.relativize(dir));
			try
			{
				Files.copy(dir, newdir, options);
			}
			catch (FileAlreadyExistsException x)
			{
				// ignore
			}
			catch (IOException x)
			{
				System.err.format("Unable to create: %s: %s%n", newdir, x);
				return SKIP_SUBTREE;
			}
			return CONTINUE;
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		{
			copyFile(file, target.resolve(source.relativize(file)),
					prompt, preserve);
			return CONTINUE;
		}
		
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc)
		{
			// fix up modification time of directory when done
			if ((exc == null) && preserve)
			{
				Path newdir = target.resolve(source.relativize(dir));
				try
				{
					FileTime time = Files.getLastModifiedTime(dir);
					Files.setLastModifiedTime(newdir, time);
				}
				catch (IOException x)
				{
					System.err.format("Unable to copy all attributes to: %s: %s%n", newdir, x);
				}
			}
			return CONTINUE;
		}
		
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc)
		{
			if (exc instanceof FileSystemLoopException)
			{
				System.err.println("cycle detected: " + file);
			}
			else
			{
				System.err.format("Unable to copy: %s: %s%n", file, exc);
			}
			return CONTINUE;
		}
	}
	
	// #endregion copy.
	// --------------------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------------------
	// #region move.
	
	private static void moveFile(Path source, Path target, boolean prompt)
	{
		CopyOption[] options = new CopyOption[] { REPLACE_EXISTING };
		
		if ( !prompt || Files.notExists(target)) // || okayToOverwrite(target))
		{
			try
			{
				Files.move(source, target, options);
			}
			catch (IOException x)
			{
				System.err.format("Unable to copy: %s: %s%n", source, x);
			}
		}
	}
	
	public static class TreeMover implements FileVisitor<Path>
	{
		
		private final Path		source;
		private final Path		target;
		private final boolean	prompt;
		
		public TreeMover(Path source, Path target, boolean prompt)
		{
			this.source = source;
			this.target = target;
			this.prompt = prompt;
		}
		
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
		{
			Path newdir = target.resolve(source.relativize(dir));
			
			try
			{
				Files.copy(dir, newdir);
			}
			catch (FileAlreadyExistsException x)
			{
				// ignore
			}
			catch (IOException x)
			{
				System.err.format("Unable to create: %s: %s%n", newdir, x);
				return SKIP_SUBTREE;
			}
			return CONTINUE;
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		{
			moveFile(file, target.resolve(source.relativize(file)), prompt);
			return CONTINUE;
		}
		
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc)
		{
			// fix up modification time of directory when done
			if ((exc == null))
			{
				Path newdir = target.resolve(source.relativize(dir));
				
				try
				{
					FileTime time = Files.getLastModifiedTime(dir);
					Files.setLastModifiedTime(newdir, time);
					
					if (source.toAbsolutePath().equals(newdir.toAbsolutePath()))
					{
						source.resolve(newdir.toAbsolutePath());
					}
					
					Files.deleteIfExists(dir);
				}
				catch (IOException x)
				{
					System.err.format("Unable to copy all attributes to: %s: %s%n", newdir, x);
				}
			}
			
			return CONTINUE;
		}
		
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc)
		{
			if (exc instanceof FileSystemLoopException)
			{
				System.err.println("cycle detected: " + file);
			}
			else
			{
				System.err.format("Unable to copy: %s: %s%n", file, exc);
			}
			
			return CONTINUE;
		}
	}
	
	// #endregion move.
	// --------------------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------------------
	// #region delete.
	
	private static void deleteFile(Path file)
	{
		try
		{
			Files.deleteIfExists(file);
		}
		catch (IOException x)
		{
			System.err.format("Unable to copy: %s: %s%n", file, x);
		}
	}
	
	public static class TreeDeleter implements FileVisitor<Path>
	{
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		{
			deleteFile(file);
			return CONTINUE;
		}
		
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc)
		{
			// fix up modification time of directory when done
			if ((exc == null))
			{
				try
				{
					Files.deleteIfExists(dir);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			
			return CONTINUE;
		}
		
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc)
		{
			if (exc instanceof FileSystemLoopException)
			{
				System.err.println("cycle detected: " + file);
			}
			else
			{
				System.err.format("Unable to copy: %s: %s%n", file, exc);
			}
			
			return CONTINUE;
		}
		
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
		{
			return CONTINUE;
		}
	}
	
	// #endregion delete.
	// --------------------------------------------------------------------------------------
	
	/**
	 * Gets the size.<br />
	 * <br/>
	 * Credit: Aksel Willgert at StackOverFlow
	 * 
	 * @param startPath
	 *            Folder to get size for.
	 * @return the size of the folder passed
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static long getSize(String startPath) throws IOException
	{
		final AtomicLong size = new AtomicLong(0);
		Path path = Paths.get(startPath);
		
		Files.walkFileTree(path, new SimpleFileVisitor<Path>()
		{
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				size.addAndGet(attrs.size());
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
			{
				// Skip folders that can't be traversed
				System.out.println("skipped: " + file + "e=" + exc);
				return FileVisitResult.CONTINUE;
			}
		});
		
		return size.get();
	}
}
