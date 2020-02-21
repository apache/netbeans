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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.netbeans.modules.dlight.libs.common.DLightLibsCommonLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * A temporary solution until we have an official file system provider in thus module
 */
public final class FileSystemProvider {

    public interface FileSystemProblemListener {
        void problemOccurred(FileSystem fileSystem, String path);
        void recovered(FileSystem fileSystem);
    }

    public static final class Stat {

        public final long inode;
        public final long device;

        private Stat(long device, long inode) {
            this.inode = inode;
            this.device = device;
        }

        public static Stat create(long  device, long inode) {
            return new Stat(device, inode);
        }

        public static Stat createInvalid() {
            return new Stat(-1, -1);
        }

        public boolean isValid() {
            return inode >= 0;
        }

        @Override
        public String toString() {
            return "Stat(" + "dev=" + device + ",ino=" + device + ')'; //NOI18N
        }
    }

    public enum AccessCheckType {
        FAST,
        FULL;
        public String getDisplayName() {
            switch (this) {
                case FAST:
                    return NbBundle.getMessage(FileSystemProvider.class, "AccessType_Fast");
                case FULL:
                    return NbBundle.getMessage(FileSystemProvider.class, "AccessType_Full");
                default:
                    throw new IllegalArgumentException("Unexpected access type: " + this); //NOI18N
            }
        }

        @Override
        public String toString() {
            return getDisplayName();
        }        
    }
    
    // create own copy of lookup to avoid performance issues in ProxyLookup.LazyCollection.iterator()
    private static final  Collection<FileSystemProviderImplementation> ALL_PROVIDERS =
            new ArrayList<FileSystemProviderImplementation>(Lookup.getDefault().lookupAll(FileSystemProviderImplementation.class));

    private FileSystemProvider() {
    }

    public static FileSystem getFileSystem(ExecutionEnvironment env) {
        return getFileSystem(env, "/"); //NOI18N
    }

    public static ExecutionEnvironment getExecutionEnvironment(FileSystem fileSystem) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                return provider.getExecutionEnvironment(fileSystem);
            }
        }
        return ExecutionEnvironmentFactory.getLocal();
    }

    public static ExecutionEnvironment getExecutionEnvironment(FileObject fileObject) {
        try {
            return getExecutionEnvironment(fileObject.getFileSystem());
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);            
        }
        return ExecutionEnvironmentFactory.getLocal();
    }

    public static FileSystem getFileSystem(ExecutionEnvironment env, String root) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.getFileSystem(env, root);
            }
        }
        noProvidersWarning(env);
        return null;
    }

    public static boolean waitWrites(ExecutionEnvironment env, Collection<FileObject> filesToWait, Collection<String> failedFiles) throws InterruptedException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.waitWrites(env, filesToWait, failedFiles);
            }
        }
        noProvidersWarning(env);
        return true;
    }
    
    public static boolean waitWrites(ExecutionEnvironment env, Collection<String> failedFiles) throws InterruptedException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.waitWrites(env, failedFiles);
            }
        }
        noProvidersWarning(env);
        return true;
    }

    public static String normalizeAbsolutePath(String absPath, ExecutionEnvironment env) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.normalizeAbsolutePath(absPath, env);
            }
        }
        noProvidersWarning(env);
        return FileUtil.normalizePath(absPath); // or should it return just absPath?
    }

    public static String normalizeAbsolutePath(String absPath, FileSystem fileSystem) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                return provider.normalizeAbsolutePath(absPath, fileSystem);
            }
        }
        noProvidersWarning(fileSystem);
        return FileUtil.normalizePath(absPath); // or should it return just absPath?
    }

    /**
     * In many places, standard sequence is as follows:
     *  - convert path to absolute if need
     *  - normalize it
     *  - find file object
     * In the case of non-local file systems we should delegate it to correspondent file systems.
     */
    public static FileObject getFileObject(FileObject baseFileObject, String relativeOrAbsolutePath) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(baseFileObject)) {
                return provider.getFileObject(baseFileObject, relativeOrAbsolutePath);
            }
        }
        noProvidersWarning(baseFileObject);
        if (isAbsolute(relativeOrAbsolutePath)) {
            try {
                return baseFileObject.getFileSystem().findResource(relativeOrAbsolutePath);
            } catch (FileStateInvalidException ex) {
                return null;
            }
        } else {
            return baseFileObject.getFileObject(relativeOrAbsolutePath);
        }
    }
    
    /**
     * Just a convenient shortcut
     */
    public static FileObject getFileObject(ExecutionEnvironment env, String absPath) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.getFileSystem(env, "/").findResource(absPath);
            }
        }
        noProvidersWarning(env);
        return FileUtil.toFileObject(FileUtil.normalizeFile(new File(absPath)));
    }

    public static FileObject getCanonicalFileObject(FileObject fileObject) throws IOException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileObject)) {
                return provider.getCanonicalFileObject(fileObject);
            }
        }
        noProvidersWarning(fileObject);
        return fileObject;
    }
    
    public static String getCanonicalPath(FileObject fileObject) throws IOException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileObject)) {
                return provider.getCanonicalPath(fileObject);
            }
        }
        noProvidersWarning(fileObject);
        return fileObject.getPath();
    }

    public static String getCanonicalPath(FileSystem fileSystem, String absPath) throws IOException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                return provider.getCanonicalPath(fileSystem, absPath);
            }
        }
        noProvidersWarning(fileSystem);
        return absPath;
    }

    public static String getCanonicalPath(ExecutionEnvironment env, String absPath) throws IOException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.getCanonicalPath(env, absPath);
            }
        }
        noProvidersWarning(env);
        return absPath;
    }

    public static boolean isAbsolute(ExecutionEnvironment env,  String path) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.isAbsolute(path);
            }
        }
        return true; // for other file system, let us return true - or should it be false? 
    }
    
    public static boolean isAbsolute(FileSystem fileSystem,  String path) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                return provider.isAbsolute(path);
            }
        }
        return true; // for other file system, let us return true - or should it be false? 
    }
    
    public static boolean isLink(FileSystem fileSystem,  String path) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                return provider.isLink(fileSystem, path);
            }
        }
        return false;
    }

    public static boolean isLink(ExecutionEnvironment env,  String path) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.isLink(env, path);
            }
        }
        return false;
    }

    public static boolean isLink(FileObject fo) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fo)) {
                return provider.isLink(fo);
            }
        }
        return false;
    }

    public static String resolveLink(FileObject fo) throws IOException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fo)) {
                return provider.resolveLink(fo);
            }
        }
        return null;        
    }

    public static boolean isAbsolute(String path) {
        if (path == null || path.length() == 0) {
            return false;
        } else if (path.charAt(0) == '/') {
            return true;
        } else if (path.charAt(0) == '\\') {
            return true;
        } else if (path.indexOf(':') == 1 && Utilities.isWindows()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * JFileChooser works in the term of files.
     * For such "perverted" files FileUtil.toFileObject won't work.
     * @param file
     * @return 
     */
    public static FileObject fileToFileObject(File file) {
        Parameters.notNull("file", file);
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(file)) {
                return provider.fileToFileObject(file);
            }
        }
        noProvidersWarning(file);
        return FileUtil.toFileObject(file);
    }

    public static FileSystem getFileSystem(URI uri) {
        Parameters.notNull("file", uri);
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(uri)) {
                return provider.getFileSystem(uri);
            }
        }
        noProvidersWarning(uri);
        return null;
    }

    public static FileObject urlToFileObject(String url) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(url)) {
                return provider.urlToFileObject(url);
            }
        }
        noProvidersWarning(url);
        return null;
    }
    
    public static FileSystem urlToFileSystem(String url) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(url)) {
                return provider.urlToFileSystem(url);
            }
        }
        noProvidersWarning(url);
        return null;
    }

    public static String toUrl(FileSystem fileSystem, String absPath) {
        Parameters.notNull("fileSystem", fileSystem); //NOI18N
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                return provider.toURL(fileSystem, absPath);
            }
        }
        noProvidersWarning(fileSystem);
        return absPath;        
    }

    public static String fileObjectToUrl(FileObject fileObject) {
        Parameters.notNull("fileObject", fileObject); //NOI18N
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileObject)) {
                return provider.toURL(fileObject);
            }
        }
        noProvidersWarning(fileObject);
        return fileObject.toURL().toExternalForm();
    }

    public static void refresh(FileObject fileObject, boolean recursive) {
        Parameters.notNull("fileObject", fileObject); //NOI18N
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileObject)) {
                provider.refresh(fileObject, recursive);
                return;
            }
        }
        noProvidersWarning(fileObject);
    }

    public static void scheduleRefresh(FileObject fileObject) {
        Parameters.notNull("fileObject", fileObject); //NOI18N
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileObject)) {
                provider.scheduleRefresh(fileObject); 
                return;
            }
        }
        noProvidersWarning(fileObject);
    }
    
    public static void scheduleRefresh(ExecutionEnvironment env, Collection<String> paths) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                provider.scheduleRefresh(env, paths);
                return;
            }
        }
        noProvidersWarning(env);
    }
    
    public static void addRecursiveListener(FileChangeListener listener,  FileSystem fileSystem, String absPath) {
        addRecursiveListener(listener, fileSystem, absPath, null, null);
    }

    public static void addRecursiveListener(FileChangeListener listener,  FileSystem fileSystem, String absPath, FileFilter recurseInto, Callable<Boolean> interrupter) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                absPath = provider.normalizeAbsolutePath(absPath, fileSystem);
                try {
                    provider.addRecursiveListener(listener, fileSystem, absPath, recurseInto, interrupter);
                } catch (Throwable e) {
                    DLightLibsCommonLogger.printStackTraceOnce(e, Level.INFO, true);
                }
                return;
            }
        }
        noProvidersWarning(fileSystem);
    }
    
    public static void removeRecursiveListener(FileChangeListener listener, FileSystem fileSystem, String absPath) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                absPath = provider.normalizeAbsolutePath(absPath, fileSystem);
                try {
                    provider.removeRecursiveListener(listener, fileSystem, absPath);
                } catch (Throwable e) {
                    DLightLibsCommonLogger.printStackTraceOnce(e, Level.INFO, true);
                }
                return;
            }
        }
        noProvidersWarning(fileSystem);
    }

    public static boolean canExecute(FileObject fileObject) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileObject)) {
                return provider.canExecute(fileObject);
            }
        }
        noProvidersWarning(fileObject);
        return true;
    }
    
    public static void addFileChangeListener(FileChangeListener listener, FileSystem fileSystem, String path) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                provider.addFileChangeListener(listener, fileSystem, path);
                return;
            }
        }
        noProvidersWarning(fileSystem);
    }
    
    public static void addFileChangeListener(FileChangeListener listener, ExecutionEnvironment env, String path) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                provider.addFileChangeListener(listener, env, path);
                return;
            }
        }
        noProvidersWarning(env);
    }
    
    public static void addFileChangeListener(FileChangeListener listener) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            provider.addFileChangeListener(listener);
        }
    }
    
    public static void removeFileChangeListener(FileChangeListener listener) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            provider.removeFileChangeListener(listener);
        }
    }
    
    public static void addFileSystemProblemListener(FileSystemProblemListener listener) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            provider.addFileSystemProblemListener(listener);
        }
    }

    public static void addFileSystemProblemListener(FileSystemProblemListener listener, FileSystem fileSystem) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                provider.addFileSystemProblemListener(listener, fileSystem);
            }
        }
    }

    public static void removeFileSystemProblemListener(FileSystemProblemListener listener, FileSystem fileSystem) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                provider.removeFileSystemProblemListener(listener, fileSystem);
            }
        }
    }

    public static char getFileSeparatorChar(FileSystem fileSystem) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileSystem)) {
                return provider.getFileSeparatorChar();
            }
        }
        noProvidersWarning(fileSystem);
        return '/';
    }

    public static char getFileSeparatorChar(ExecutionEnvironment env) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                return provider.getFileSeparatorChar();
            }
        }
        noProvidersWarning(env);
        return '/';
    }

    /** Just a convenient shortcut */
    public static char getFileSeparatorChar(FileObject fileObject) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fileObject)) {
                return provider.getFileSeparatorChar();
            }
        }
        noProvidersWarning(fileObject);
        return '/';
    }
    
    public enum WarmupMode {
        FILES_CONTENT,
        RECURSIVE_LS
    }

    public static void warmup(WarmupMode mode, ExecutionEnvironment env, Collection<String> paths, Collection<String> extensions) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(env)) {
                provider.warmup(mode, env, paths, extensions);
            }
        }
    }

    public static InputStream getInputStream(FileObject fo, int maxSize) throws IOException {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fo)) {
                return provider.getInputStream(fo, maxSize);
            }
        }
        return fo.getInputStream();
    }


    public static boolean canSetAccessCheckType(ExecutionEnvironment execEnv) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(execEnv)) {
                return provider.canSetAccessCheckType(execEnv);
            }
        }
        noProvidersWarning(execEnv);
        return false;
    }

    public static void setAccessCheckType(ExecutionEnvironment execEnv, AccessCheckType accessCheckType) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(execEnv)) {
                provider.setAccessCheckType(execEnv, accessCheckType);
                return;
            }
        }
        noProvidersWarning(execEnv);
    }

    public static Stat getStat(FileObject fo) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(fo)) {
                return provider.getStat(fo);
            }
        }
        noProvidersWarning(fo);
        return Stat.createInvalid();
    }

    /** can be null if provider does not support this or no providers found */
    public static AccessCheckType getAccessCheckType(ExecutionEnvironment execEnv) {
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(execEnv)) {
                return provider.getAccessCheckType(execEnv);
            }
        }
        noProvidersWarning(execEnv);
        return null;
    }

    /**
     * Uploads zip to a temporary file on the remote host, unzips it into the given directory, then removes the uploaded zip.
     * Also unzip its content into remote file system caches
     * NB: zip entries timestamps should be in UTC! 
     * To set entry time in UTC use entry.setTime(entryTime - TimeZone.getDefault().getRawOffset());
     */
    public static void uploadAndUnzip(File zipFile, FileObject targetFolder) 
            throws FileNotFoundException, ConnectException, IOException, InterruptedException {
        uploadAndUnzip(new FileInputStream(zipFile), targetFolder);
    }

    /**
     * Uploads zip to a temporary file on the remote host, unzips it into the given directory, then removes the uploaded zip.
     * Also unzip its content into remote file system caches
     * NB: zip entries timestamps should be in UTC! 
     * To set entry time in UTC use entry.setTime(entryTime - TimeZone.getDefault().getRawOffset());
     */
    public static void uploadAndUnzip(InputStream zipStream, FileObject targetFolder) 
            throws FileNotFoundException, ConnectException, IOException, InterruptedException {
        DLightLibsCommonLogger.assertTrue(targetFolder.isFolder(), "Not a folder: " + targetFolder); //NOI18N
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(targetFolder)) {
                provider.uploadAndUnzip(zipStream, targetFolder);
                return;
            }
        }
        noProvidersWarning(targetFolder);
    }

    /**
     * NB: there are several flaws in the implementation:
     * #1: it does NOT support links inside directory
     * #2: it does NOT file and caches names transformation, they will be exactly the same, 
     * so you can get into trouble it 2 situations:
     *  a) if your local file system is case insensitive and there are files that differ only in case, 
     *  b) if your file name is forbidden on the local file system (like COM1, etc on Windows)
     * #3: it's callers responsibility to call resume in finally block 
     * and to call it on the same directory suspend was called
     * #4: Weird usages such as "suspend and never resume", "suspend twice", "resume twicw" lead to unpredictable results,
     * However, this works well when creating projects - and that was the main goal of introducing this
     */
    public static void suspendWritesUpload(FileObject folder) throws IOException {
        DLightLibsCommonLogger.assertTrue(folder.isFolder(), "Not a folder: " + folder); //NOI18N
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(folder)) {
                provider.suspendWritesUpload(folder);
                return;
            }
        }
        noProvidersWarning(folder);
    }

    /**
     * NB: there are several flaws in the implementation:
     * #1: it does NOT support links inside directory
     * #2: it does NOT file and caches names transformation, they will be exactly the same, 
     * so you can get into trouble it 2 situations:
     *  a) if your local file system is case insensitive and there are files that differ only in case, 
     *  b) if your file name is forbidden on the local file system (like COM1, etc on Windows)
     * #3: it's callers responsibility to call resume in finally block 
     * and to call it on the same directory suspend was called
     * #4: Weird usages such as "suspend and never resume", "suspend twice", "resume twicw" lead to unpredictable results,
     * However, this works well when creating projects - and that was the main goal of introducing this
     */
    public static void resumeWritesUpload(FileObject folder) 
            throws IOException, InterruptedException, ConnectException {
        DLightLibsCommonLogger.assertTrue(folder.isFolder(), "Not a folder: " + folder); //NOI18N
        for (FileSystemProviderImplementation provider : ALL_PROVIDERS) {
            if (provider.isMine(folder)) {
                provider.resumeWritesUpload(folder);
                return;
            }
        }
        noProvidersWarning(folder);
    }

    private static void noProvidersWarning(Object object) {
        if (RemoteLogger.getInstance().isLoggable(Level.FINE)) {        
            if (RemoteLogger.getInstance().isLoggable(Level.FINEST)) {
                String message = "No file system providers for " + object; // NOI18N
                RemoteLogger.getInstance().log( Level.FINEST, message, new Exception(message)); //NOI18N
            } else {
                RemoteLogger.getInstance().log(Level.FINE, "No file system providers for {0}", object); //NOI18N
            }
        }
    }
}
