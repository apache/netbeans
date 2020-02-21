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

package org.netbeans.modules.remote.spi;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.util.Collection;
import java.util.concurrent.Callable;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider.FileSystemProblemListener;
import org.netbeans.modules.remote.spi.FileSystemProvider.WarmupMode;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public interface FileSystemProviderImplementation {
    FileSystem getFileSystem(ExecutionEnvironment env, String root);
    FileSystem getFileSystem(URI uri);
    String normalizeAbsolutePath(String absPath, ExecutionEnvironment env);
    String normalizeAbsolutePath(String absPath, FileSystem fileSystem);
    FileObject getFileObject(FileObject baseFileObject, String relativeOrAbsolutePath);
    FileObject urlToFileObject(String absoluteURL);
    FileSystem urlToFileSystem(String url);
    FileObject fileToFileObject(File file);
    String toURL(FileObject fileObject);
    String toURL(FileSystem fileSystem, String absPath);
    boolean isMine(ExecutionEnvironment env);
    boolean isMine(FileObject fileObject);
    boolean isMine(String absoluteURL);
    boolean isMine(FileSystem fileSystem);
    boolean isMine(File file);
    boolean isMine(URI uri);
    boolean isAbsolute(String path);
    ExecutionEnvironment getExecutionEnvironment(FileSystem fileSystem);
    boolean waitWrites(ExecutionEnvironment env, Collection<String> failedFiles) throws InterruptedException;
    boolean waitWrites(ExecutionEnvironment env, Collection<FileObject> filesToWait, Collection<String> failedFiles) throws InterruptedException;
    FileObject getCanonicalFileObject(FileObject fileObject) throws IOException;
    String getCanonicalPath(FileObject fileObject) throws IOException;
    String getCanonicalPath(FileSystem fs, String absPath) throws IOException;
    String getCanonicalPath(ExecutionEnvironment env, String absPath) throws IOException;
    void refresh(FileObject fileObject, boolean recursive);
    void scheduleRefresh(FileObject fileObject);
    void scheduleRefresh(ExecutionEnvironment env, Collection<String> paths);
    void addRecursiveListener(FileChangeListener listener, FileSystem fileSystem, String absPath);
    void addRecursiveListener(FileChangeListener listener, FileSystem fileSystem, String absPath,  FileFilter recurseInto, Callable<Boolean> interrupter);
    void removeRecursiveListener(FileChangeListener listener, FileSystem fileSystem, String absPath);
    boolean canExecute(FileObject fileObject);
    public void addFileChangeListener(FileChangeListener listener, FileSystem fileSystem, String path);
    public void addFileChangeListener(FileChangeListener listener);
    public void removeFileChangeListener(FileChangeListener listener);
    public void addFileChangeListener(FileChangeListener listener, ExecutionEnvironment env, String path);
    public char getFileSeparatorChar();
    void addFileSystemProblemListener(FileSystemProblemListener listener, FileSystem fileSystem);
    void addFileSystemProblemListener(FileSystemProblemListener listener);
    void removeFileSystemProblemListener(FileSystemProblemListener listener, FileSystem fileSystem);
    void warmup(WarmupMode mode, ExecutionEnvironment env, Collection<String> paths, Collection<String> extensions);
    boolean isLink(FileSystem fileSystem, String path);
    boolean isLink(ExecutionEnvironment env, String path);
    boolean isLink(FileObject fo);
    String resolveLink(FileObject fo) throws IOException;
    InputStream getInputStream(FileObject fo, int maxSize) throws IOException;
    boolean canSetAccessCheckType(ExecutionEnvironment execEnv);
    void setAccessCheckType(ExecutionEnvironment execEnv, FileSystemProvider.AccessCheckType accessCheckType);
    FileSystemProvider.AccessCheckType getAccessCheckType(ExecutionEnvironment execEnv);
    FileSystemProvider.Stat getStat(FileObject fo);
    /** 
     * NB: zip entries time should be in UTC. 
     * To set entry time in UTC use
     * entry.setTime(entryTime - TimeZone.getDefault().getRawOffset()); 
     */
    void uploadAndUnzip(InputStream zipStream, FileObject targetFolder) 
            throws FileNotFoundException, ConnectException, IOException, InterruptedException;
    void suspendWritesUpload(FileObject folder) throws IOException;
    void resumeWritesUpload(FileObject folder) throws IOException, InterruptedException, ConnectException;
}
