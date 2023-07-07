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
import java.util.Objects;
import org.netbeans.agent.hooks.api.TrackingHooks;
import org.netbeans.agent.hooks.api.TrackingHooks.Hooks;

/**
 *
 * @author lahvac
 */
public class TestIO {
    private static Output out;

    public static void main(String... args) throws IOException {
        File temp = File.createTempFile("test", "test");
        String tempPath = temp.getPath();
        TrackingHooks.register(new TrackingHooks() {
            @Override
            protected void checkFileWrite(String path) {
                if (path.equals(tempPath)) {
                    out.print("checkFileWrite: TEMP-FILE");
                }
            }
            @Override
            protected void checkFileRead(String path) {
                if (path.equals(tempPath)) {
                    out.print("checkFileRead: TEMP-FILE");
                }
            }
            @Override
            protected void checkDelete(String path) {
                if (path.equals(tempPath)) {
                    out.print("checkDelete: TEMP-FILE");
                }
            }
        }, 0, Hooks.IO);
        runTest("going to write using File: ", () -> new FileOutputStream(temp).close());
        runTest("File.canExecute: ", () -> temp.canExecute());
        runTest("File.canRead: ", () -> temp.canRead());
        runTest("File.canWrite: ", () -> temp.canWrite());
        runTest("File.createNewFile: ", () -> temp.createNewFile());
        runTest("File.isDirectory: ", () -> temp.isDirectory());
        runTest("File.isFile: ", () -> temp.isFile());
        runTest("File.isHidden: ", () -> temp.isHidden());
        runTest("File.length: ", () -> temp.exists());
        runTest("File.exists: ", () -> temp.length());
        runTest("File.lastModified: ", () -> temp.lastModified());
        runTest("File.setExecutable(boolean): ", () -> temp.setExecutable(true));
        runTest("File.setExecutable(boolean, boolean): ", () -> temp.setExecutable(true, true));
        runTest("File.setReadable(boolean): ", () -> temp.setReadable(true));
        runTest("File.setReadable(boolean, boolean): ", () -> temp.setReadable(true, true));
        runTest("File.setWritable(boolean): ", () -> temp.setWritable(true));
        runTest("File.setWritable(boolean, boolean): ", () -> temp.setWritable(true, true));
        runTest("File.setReadOnly(): ", () -> temp.setReadOnly());
        runTest("File.setLastModified(): ", () -> temp.setLastModified(0));
        runTest("going to delete using File: ", () -> temp.delete());
        runTest("going to mkdir using File: ", () -> temp.mkdir());
        runTest("going to mkdirs using File: ", () -> temp.mkdirs());
        System.err.println("going to list using File: ");
        runTest("1: ", () -> temp.list());
        runTest("2: ", () -> temp.list((dir, name) -> true));
        runTest("3: ", () -> temp.listFiles());
        runTest("4: ", () -> temp.listFiles((dir, name) -> true));
        runTest("5: ", () -> temp.listFiles(file -> true));
        runTest("going to delete using File: ", () -> temp.delete());
        runTest("going to mkdirs using File: ", () -> temp.mkdir());
        runTest("going to delete using File: ", () -> temp.delete());
        runTest("going to write using Path: ", () -> Files.newOutputStream(temp.toPath()).close());
        runTest("delete using Path: ", () -> Files.delete(temp.toPath()));
        runTest("going to mkdir using Path: ", () -> Files.createDirectory(temp.toPath()));
        runTest("going to mkdirs using Path: ", () -> Files.createDirectories(temp.toPath()));
        System.err.println("going to list using Path: ");
        runTest("1: ", () -> Files.newDirectoryStream(temp.toPath()));
        runTest("2: ", () -> Files.newDirectoryStream(temp.toPath(), "*"));
        runTest("3: ", () -> Files.newDirectoryStream(temp.toPath(), f -> true));
        runTest("delete using Path: ", () -> Files.delete(temp.toPath()));
        runTest("going to write using String: ", () -> new FileOutputStream(temp.getAbsolutePath()).close());
        runTest("going to read using File: ", () -> new FileInputStream(temp).close());
        runTest("going to read using Path: ", () -> Files.newInputStream(temp.toPath()).close());
        runTest("going to read using String: ", () -> new FileInputStream(temp.getAbsolutePath()).close());
        runTest("going to open using RandomAccessFile using String: ", () -> new RandomAccessFile(temp.getPath(), "r").close());
        runTest("going to open using RandomAccessFile using File: ", () -> new RandomAccessFile(temp, "r").close());
        System.err.println("going to open using newByteChannel: ");
        runTest("1a: ", () -> Files.newByteChannel(temp.toPath()).close());
        runTest("1b: ", () -> Files.newByteChannel(temp.toPath(), StandardOpenOption.READ).close());
        runTest("1c: ", () -> Files.newByteChannel(temp.toPath(), StandardOpenOption.WRITE).close());
        runTest("1d: ", () -> Files.newByteChannel(temp.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE).close());
        runTest("2a: ", () -> Files.newByteChannel(temp.toPath(), new HashSet<>()).close());
        runTest("2b: ", () -> Files.newByteChannel(temp.toPath(), new HashSet<>(Arrays.asList(StandardOpenOption.READ))).close());
        runTest("2c: ", () -> Files.newByteChannel(temp.toPath(), new HashSet<>(Arrays.asList(StandardOpenOption.WRITE))).close());
        runTest("2d: ", () -> Files.newByteChannel(temp.toPath(), new HashSet<>(Arrays.asList(StandardOpenOption.READ, StandardOpenOption.WRITE))).close());
        runTest("Files.readAttributes(Path, Class): ", () -> Files.readAttributes(temp.toPath(), BasicFileAttributes.class));
        runTest("Files.readAttributes(Path, String): ", () -> Files.readAttributes(temp.toPath(), "*"));
        runTest("Files.getAttribute: ", () -> Files.getAttribute(temp.toPath(), "lastModifiedTime"));
        runTest("Files.getFileAttributeView: ", () -> Files.getFileAttributeView(temp.toPath(), BasicFileAttributeView.class));
        runTest("Files.isDirectory: ", () -> Files.isDirectory(temp.toPath()));
        runTest("Files.isExecutable: ", () -> Files.isExecutable(temp.toPath()));
        runTest("Files.isHidden: ", () -> Files.isHidden(temp.toPath()));
        runTest("Files.isReadable: ", () -> Files.isReadable(temp.toPath()));
        runTest("Files.isRegularFile: ", () -> Files.isRegularFile(temp.toPath()));
        runTest("Files.isSameFile: ", () -> Files.isSameFile(temp.toPath(), temp.toPath())); //XXX
        runTest("Files.isWritable: ", () -> Files.isWritable(temp.toPath()));
        FileTime[] lastModified = new FileTime[1];
        runTest("Files.getLastModifiedTime: ", () -> lastModified[0] = Files.getLastModifiedTime(temp.toPath()));
        UserPrincipal[] owner = new UserPrincipal[1];
        runTest("Files.getOwner: ", () -> owner[0] = Files.getOwner(temp.toPath()));
        runTest("Files.size: ", () -> Files.size(temp.toPath()));
        runTest("Files.setAttribute: ", () -> Files.setAttribute(temp.toPath(), "lastModifiedTime", lastModified[0]));
        runTest("Files.setLastModifiedTime: ", () -> Files.setLastModifiedTime(temp.toPath(), lastModified[0]));
        runTest("Files.setOwner: ", () -> Files.setOwner(temp.toPath(), owner[0]));
    }

    private static void runTest(String desc, Test test) throws IOException {
        System.err.print(desc);

        try {
            out = new Output() {
                String last;
                @Override
                public void print(String text) {
                    if (!Objects.equals(last, text)) {
                        last = text;
                        System.err.print(text);
                    }
                }
            };
            test.run();
        } finally {
            out = null;
        }
        System.err.println();
    }

    private static class Dedupe {

    }

    interface Output {
        public void print(String text);
    }

    interface Test {
        public void run() throws IOException;
    }

}
