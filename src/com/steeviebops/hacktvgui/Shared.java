/*
 * Copyright (C) 2021 Stephen McGarry
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.steeviebops.hacktvgui;

/**
* Various functions and methods not directly related to the GUI code.
*/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Shared {
    
    public static boolean isNumeric(String strNum) {
	if (strNum == null) {
	    return false;
	}
	try {
	    double d = Double.parseDouble(strNum);
	} catch (NumberFormatException nfe) {
	    return false;
	}
	return true;
    }
    
    public static int wildcardFind(String pathToScan, String startsWith, String endsWith) {
        // Returns the number of files found in a directory with the specified start and end strings
        // Case insensitive, feed it with lowercase filenames
        String fileToFilter;
        File folderToScan = new File(pathToScan);
        File[] listOfFiles = folderToScan.listFiles();
        int c = 0;
        // If the specified directory does not exist, return 0 and stop
        if (!Files.exists(folderToScan.toPath())) return 0;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                fileToFilter = listOfFile.getName();
                // If a file is found, increment c by one
                if (fileToFilter.toLowerCase().startsWith(startsWith)
                        && fileToFilter.toLowerCase().endsWith(endsWith)) {
                    c = c + 1;
                }
            }
        }
        return c;
    }    
    
    public static void deleteFSObject(Path pathToBeDeleted) throws IOException {
        // Deletes the path specified to this method
	Files.walkFileTree(pathToBeDeleted, 
	  new SimpleFileVisitor<Path>() {
	    @Override
	    public FileVisitResult postVisitDirectory(
	      Path dir, IOException exc) throws IOException {
	        Files.delete(dir);
	        return FileVisitResult.CONTINUE;
	        }
	        
	    @Override
	    public FileVisitResult visitFile(
	      Path file, BasicFileAttributes attrs) 
	      throws IOException {
	        Files.delete(file);
	        return FileVisitResult.CONTINUE;
	    }
	});        
    }
    
    public static void copyResource(String res, String dest, Class c) throws IOException {
        InputStream src = c.getResourceAsStream(res);
        Files.copy(src, Paths.get(dest), StandardCopyOption.REPLACE_EXISTING);
    }
    
    public static String stripQuotes(String FilePath) {
        // Bug fix for cases where a path containing quotes is pasted into
        // the file open prompt. This causes Swing to prepend the current
        // directory to the path (with the intended file path including
        // quotes at the end). This can cause things to break badly; if 
        // this path is saved to the preferences store it can prevent the 
        // application from opening! So we check for it and strip the path.
        if (FilePath.contains("\\\"") ) {
            FilePath = FilePath.substring(FilePath.indexOf("\""));
        }
        if (FilePath.startsWith("\"")) FilePath = FilePath.substring(1);
        if (FilePath.endsWith("\"")) FilePath = FilePath.substring(0, FilePath.length() -1);
        return FilePath;
    }
    
    public static Date getLastUpdatedTime(String jarFilePath, String classFilePath) {
        JarFile jar = null;
        try {
            jar = new JarFile(jarFilePath);
            Enumeration<JarEntry> enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry file = (JarEntry) enumEntries.nextElement();
                if (file.getName().equals(classFilePath.substring(1))) {
                    long time=file.getTime();
                    return time==-1?null: new Date(time);
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
     }

    public static void download(String url, String fileName) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.setUseCaches(false);
        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, Paths.get(fileName));  
        }
    }
    
    // Unzip code courtesy of https://www.baeldung.com/java-compress-and-uncompress
    
    public static void UnzipFile(String fileZip, String destination) throws IOException {
        File destDir = new File(destination);
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
             File newFile = newFile(destDir, zipEntry);
             if (zipEntry.isDirectory()) {
                 if (!newFile.isDirectory() && !newFile.mkdirs()) {
                     throw new IOException("Failed to create directory " + newFile);
                 }
             } else {
                 // fix for Windows-created archives
                 File parent = newFile.getParentFile();
                 if (!parent.isDirectory() && !parent.mkdirs()) {
                     throw new IOException("Failed to create directory " + parent);
                 }

                 // write file content
                 FileOutputStream fos = new FileOutputStream(newFile);
                 int len;
                 while ((len = zis.read(buffer)) > 0) {
                     fos.write(buffer, 0, len);
                 }
                 fos.close();
                 // Reset timestamp to original
                 newFile.setLastModified(zipEntry.getTime());
             }
         zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }
    
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}