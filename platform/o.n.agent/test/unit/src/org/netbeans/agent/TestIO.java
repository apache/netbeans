/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.UserPrincipal;
import java.util.Arrays;
import java.util.HashSet;
import org.netbeans.agent.hooks.api.TrackingHooks;
import org.netbeans.agent.hooks.api.TrackingHooks.Hooks;

/**
 *
 * @author lahvac
 */
public class TestIO {
    public static void main(String... args) throws IOException {
        File temp = File.createTempFile("test", "test");
        String tempPath = temp.getPath();
        TrackingHooks.register(new TrackingHooks() {
            @Override
            protected void checkFileWrite(String path) {
                System.err.println("checkFileWrite: " + (path.equals(tempPath) ? "TEMP-FILE" : path));
            }
            @Override
            protected void checkFileRead(String path) {
                System.err.println("checkFileRead: " + (path.equals(tempPath) ? "TEMP-FILE" : path));
            }
            @Override
            protected void checkDelete(String path) {
                System.err.println("checkDelete: " + (path.equals(tempPath) ? "TEMP-FILE" : path));
            }
        }, 0, Hooks.IO);
        System.err.print("going to write using File: "); new FileOutputStream(temp).close();
        System.err.print("File.canExecute: "); temp.canExecute();
        System.err.print("File.canRead: "); temp.canRead();
        System.err.print("File.canWrite: "); temp.canWrite();
        System.err.print("File.createNewFile: "); temp.createNewFile();
        System.err.print("File.isDirectory: "); temp.isDirectory();
        System.err.print("File.isFile: "); temp.isFile();
        System.err.print("File.isHidden: "); temp.isHidden();
        System.err.print("File.length: "); temp.exists();
        System.err.print("File.exists: "); temp.length();
        System.err.print("File.setExecutable(boolean): "); temp.setExecutable(true);
        System.err.print("File.setExecutable(boolean, boolean): "); temp.setExecutable(true, true);
        System.err.print("File.setReadable(boolean): "); temp.setReadable(true);
        System.err.print("File.setReadable(boolean, boolean): "); temp.setReadable(true, true);
        System.err.print("File.setWritable(boolean): "); temp.setWritable(true);
        System.err.print("File.setWritable(boolean, boolean): "); temp.setWritable(true, true);
        System.err.print("File.setReadOnly(): "); temp.setReadOnly();
        System.err.print("File.setLastModified(): "); temp.setLastModified(0);
        System.err.print("going to delete using File: "); temp.delete();
        System.err.print("going to mkdir using File: "); temp.mkdir();
        System.err.print("going to mkdirs using File: "); temp.mkdirs();
        System.err.println("going to list using File: ");
        System.err.print("1: "); temp.list();
        System.err.print("2: "); temp.list((dir, name) -> true);
        System.err.print("3: "); temp.listFiles();
        System.err.print("4: "); temp.listFiles((dir, name) -> true);
        System.err.print("5: "); temp.listFiles(file -> true);
        System.err.print("going to delete using File: "); temp.delete();
        System.err.print("going to mkdirs using File: "); temp.mkdir();
        System.err.print("going to delete using File: "); temp.delete();
        System.err.print("going to write using Path: "); Files.newOutputStream(temp.toPath()).close();
        System.err.print("delete using Path: "); Files.delete(temp.toPath());
        System.err.print("going to mkdir using Path: "); Files.createDirectory(temp.toPath());
        System.err.print("going to mkdirs using Path: "); Files.createDirectories(temp.toPath());
        System.err.println("going to list using Path: ");
        System.err.print("1: "); Files.newDirectoryStream(temp.toPath());
        System.err.print("2: "); Files.newDirectoryStream(temp.toPath(), "*");
        System.err.print("3: "); Files.newDirectoryStream(temp.toPath(), f -> true);
        System.err.print("delete using Path: "); Files.delete(temp.toPath());
        System.err.print("going to write using String: "); new FileOutputStream(temp.getAbsolutePath()).close();
        System.err.print("going to read using File: "); new FileInputStream(temp).close();
        System.err.print("going to read using Path: "); Files.newInputStream(temp.toPath()).close();
        System.err.print("going to read using String: "); new FileInputStream(temp.getAbsolutePath()).close();
        System.err.print("going to open using RandomAccessFile using String: "); new RandomAccessFile(temp.getPath(), "r").close();
        System.err.print("going to open using RandomAccessFile using File: "); new RandomAccessFile(temp, "r").close();
        System.err.println("going to open using newByteChannel: ");
        System.err.print("1a: "); Files.newByteChannel(temp.toPath()).close();
        System.err.print("1b: "); Files.newByteChannel(temp.toPath(), StandardOpenOption.READ).close();
        System.err.print("1c: "); Files.newByteChannel(temp.toPath(), StandardOpenOption.WRITE).close();
        System.err.print("1d: "); Files.newByteChannel(temp.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE).close();
        System.err.print("2a: "); Files.newByteChannel(temp.toPath(), new HashSet<>()).close();
        System.err.print("2b: "); Files.newByteChannel(temp.toPath(), new HashSet<>(Arrays.asList(StandardOpenOption.READ))).close();
        System.err.print("2c: "); Files.newByteChannel(temp.toPath(), new HashSet<>(Arrays.asList(StandardOpenOption.WRITE))).close();
        System.err.print("2d: "); Files.newByteChannel(temp.toPath(), new HashSet<>(Arrays.asList(StandardOpenOption.READ, StandardOpenOption.WRITE))).close();
        System.err.print("Files.readAttributes(Path, Class): "); Files.readAttributes(temp.toPath(), BasicFileAttributes.class);
        System.err.print("Files.readAttributes(Path, String): "); Files.readAttributes(temp.toPath(), "*");
        System.err.print("Files.getAttribute: "); Files.getAttribute(temp.toPath(), "lastModifiedTime");
        System.err.print("Files.getFileAttributeView: "); Files.getFileAttributeView(temp.toPath(), BasicFileAttributeView.class);
        System.err.print("Files.isDirectory: "); Files.isDirectory(temp.toPath());
        System.err.print("Files.isExecutable: "); Files.isExecutable(temp.toPath());
        System.err.print("Files.isHidden: "); Files.isHidden(temp.toPath());
        System.err.print("Files.isReadable: "); Files.isReadable(temp.toPath());
        System.err.print("Files.isRegularFile: "); Files.isRegularFile(temp.toPath());
        System.err.print("Files.isSameFile: "); Files.isSameFile(temp.toPath(), temp.toPath()); //XXX
        System.err.print("Files.isWritable: "); Files.isWritable(temp.toPath());
        System.err.print("Files.getLastModifiedTime: "); FileTime lastModified = Files.getLastModifiedTime(temp.toPath());
        System.err.print("Files.getOwner: "); UserPrincipal owner = Files.getOwner(temp.toPath());
        System.err.print("Files.size: "); Files.size(temp.toPath());
        System.err.print("Files.setAttribute: "); Files.setAttribute(temp.toPath(), "lastModifiedTime", lastModified);
        System.err.print("Files.setLastModifiedTime: "); Files.setLastModifiedTime(temp.toPath(), lastModified);
        System.err.print("Files.setOwner: "); Files.setOwner(temp.toPath(), owner);
    }
}
