/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
