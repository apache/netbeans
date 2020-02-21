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

package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.netbeans.modules.dlight.libs.common.DLightLibsCommonLogger;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.api.ui.FileObjectBasedFile;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.spi.FileSystemProvider.FileSystemProblemListener;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.spi.FileSystemProviderImplementation;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=org.netbeans.modules.remote.spi.FileSystemProviderImplementation.class, position=150)
public class RemoteFileSystemProvider implements FileSystemProviderImplementation {

    @Override
    public FileSystem getFileSystem(ExecutionEnvironment env, String root) {
        return RemoteFileSystemManager.getInstance().getFileSystem(env);
    }

    @Override
    public String normalizeAbsolutePath(String absPath, ExecutionEnvironment env) {
        return RemoteFileSystemManager.getInstance().getFileSystem(env).normalizeAbsolutePath(absPath);
    }

    @Override
    public String normalizeAbsolutePath(String absPath, FileSystem fileSystem) {
        RemoteLogger.assertTrue(fileSystem instanceof RemoteFileSystem); // see isMine(FileSystem)
        if ((fileSystem instanceof RemoteFileSystem)) { // paranoidal check
            return ((RemoteFileSystem) fileSystem).normalizeAbsolutePath(absPath);
        }
        return PathUtilities.normalizeUnixPath(absPath);
    }

    @Override
    public boolean isAbsolute(String path) {
        return path.isEmpty() || path.startsWith("/"); //NOI18N
    }

    @Override
    public FileObject getFileObject(FileObject baseFileObject, String relativeOrAbsolutePath) {
        if (baseFileObject instanceof RemoteFileObject) {
            ExecutionEnvironment execEnv = ((RemoteFileObject) baseFileObject).getExecutionEnvironment();
            if (isAbsolute(relativeOrAbsolutePath)) {
                relativeOrAbsolutePath = RemoteFileSystemManager.getInstance().getFileSystem(execEnv).normalizeAbsolutePath(relativeOrAbsolutePath);
                try {
                    return baseFileObject.getFileSystem().findResource(relativeOrAbsolutePath);
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                // it's RemoteDirectory responsibility to normalize in this case
                return baseFileObject.getFileObject(relativeOrAbsolutePath);
            }
        }
        return null;
    }

    @Override
    public boolean isMine(ExecutionEnvironment env) {
        return env.isRemote();
    }

    @Override
    public boolean isMine(FileObject fileObject) {
        return fileObject instanceof RemoteFileObject;
    }

    @Override
    public boolean isMine(FileSystem fileSystem) {
        return fileSystem instanceof RemoteFileSystem;
    }

    @Override
    public boolean isMine(URI uri) {
        return uri.getScheme().equals(RemoteFileURLStreamHandler.PROTOCOL);
    }

    @Override
    public FileObject getCanonicalFileObject(FileObject fileObject) throws IOException {
        return RemoteFileSystemUtils.getCanonicalFileObject(fileObject);
    }

    @Override
    public String getCanonicalPath(FileObject fileObject) throws IOException {
        return RemoteFileSystemUtils.getCanonicalFileObject(fileObject).getPath();
    }

    @Override
    public String getCanonicalPath(FileSystem fs, String absPath) throws IOException {
        FileObject fo = fs.findResource(absPath);
        if (fo != null) {
            try {
                return getCanonicalFileObject(fo).getPath();
            } catch (FileNotFoundException e) {
                RemoteLogger.finest(e);
            }
        }
        return PathUtilities.normalizeUnixPath(absPath);
    }

    @Override
    public String getCanonicalPath(ExecutionEnvironment env, String absPath) throws IOException {
        RemoteLogger.assertTrueInConsole(env.isRemote(), getClass().getSimpleName() + ".getCanonicalPath is called for LOCAL env: " + env); //NOI18N
        FileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
        return getCanonicalPath(fs, absPath);
    }

    @Override
    public ExecutionEnvironment getExecutionEnvironment(FileSystem fileSystem) {
        if (fileSystem instanceof RemoteFileSystem) {
            return ((RemoteFileSystem) fileSystem).getExecutionEnvironment();
        }
        return ExecutionEnvironmentFactory.getLocal();
    }

    @Override
    public boolean isMine(String absoluteURL) {
        return absoluteURL.startsWith(RemoteFileURLStreamHandler.PROTOCOL_PREFIX);
    }

    @Override
    public boolean waitWrites(ExecutionEnvironment env, Collection<String> failedFiles) throws InterruptedException {
        return true;
    }

    @Override
    public boolean waitWrites(ExecutionEnvironment env, Collection<FileObject> filesToWait, Collection<String> failedFiles) throws InterruptedException {
        return true;
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        assert isMine(uri);
        return getFileSystem(getEnv(uri), ""); //NOI18N
    }

    private ExecutionEnvironment getEnv(URI uri) {
        String host = uri.getHost();
        int port = uri.getPort();
        String user = uri.getUserInfo();
        return ExecutionEnvironmentFactory.createNew(user, host, port);
    }

    /**
     * Returns a FileOblect referenced by the provided URI.
     * Strictly speaking this is not an URL, but rather a string returned by
     * one of the toURL() methods.
     */
    @Override
    public FileObject urlToFileObject(String path) {
        DLightLibsCommonLogger.assertNonUiThreadOnce(Level.INFO);

        if (!path.startsWith(RemoteFileURLStreamHandler.PROTOCOL_PREFIX)) {
            return null;
        }
        
        String url = path.substring(RemoteFileURLStreamHandler.PROTOCOL_PREFIX.length());
        url = PathUtilities.unescapePath(url);
        if (url.startsWith("//")) { // NOI18N
            url = url.substring(2);
        }

        // Make both notations possible (with or without leading "//" and ":" before path)
        // rfs:vk155xxx@71.152.281.291:22:/home/vk155xxx/NetBeansProjects/simple-prj
        // rfs://vk155xxx@71.152.281.291:22/home/vk155xxx/NetBeansProjects/simple-prj
        
        int idx = url.indexOf(":/"); // NOI18N
        if (idx < 0) {
            idx = url.indexOf("/"); // NOI18N
        }

        String envPart;
        String pathPart;
        if (idx < 0) {
            envPart = url;
            pathPart = "/"; // NOI18N
        } else {
            envPart = url.substring(0, idx);
            pathPart = url.substring((url.charAt(idx) == ':') ?  idx + 1 : idx);
        }

        ExecutionEnvironment env = null;
        if (envPart.indexOf('@') < 0) {
            // The magic below is about getting connected environment even
            // when no user is specified ...
            RemoteLogger.assertTrueInConsole(false, "Trying to access remote file system without user name"); // NOI18N
            idx = envPart.lastIndexOf(':');
            String host = (idx < 0) ? envPart : envPart.substring(0, idx);
            env = RemoteFileSystemUtils.getExecutionEnvironment(host, 0);
        }
        if (env == null) {
            env = ExecutionEnvironmentFactory.fromUniqueID(envPart);
        }
        if (env == null) {
            throw new IllegalArgumentException("Invalid path: " + path); //NOI18N
        }

        RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
        return fs.findResource(pathPart);
    }

    @Override
    public FileSystem urlToFileSystem(String path) {
        DLightLibsCommonLogger.assertNonUiThreadOnce(Level.INFO);

        if (!path.startsWith(RemoteFileURLStreamHandler.PROTOCOL_PREFIX)) {
            return null;
        }

        String url = path.substring(RemoteFileURLStreamHandler.PROTOCOL_PREFIX.length());
        url = PathUtilities.unescapePath(url);
        if (url.startsWith("//")) { // NOI18N
            url = url.substring(2);
        }

        int idx = url.indexOf(":/"); // NOI18N
        if (idx < 0) {
            idx = url.indexOf('/');
        }

        String envPart;
        if (idx < 0) {
            envPart = url;
        } else {
            envPart = url.substring(0, idx);
        }

        ExecutionEnvironment env = null;
        if (envPart.indexOf('@') < 0) {
            // The magic below is about getting connected environment even
            // when no user is specified ...
            RemoteLogger.assertTrueInConsole(false, "Trying to access remote file system without user name"); // NOI18N
            idx = envPart.lastIndexOf(':');
            String host = (idx < 0) ? envPart : envPart.substring(0, idx);
            env = RemoteFileSystemUtils.getExecutionEnvironment(host, 0);
        }
        if (env == null) {
            env = ExecutionEnvironmentFactory.fromUniqueID(envPart);
        }
        if (env == null) {
            throw new IllegalArgumentException("Invalid path: " + path); //NOI18N
        }

        RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
        return fs;
    }


    /**
     * Returns an URL-like string for the passed fileObject.
     * Later it could be passed to the urlToFileObject() method to get a 
     * FileObject. 
     */
    @Override
    public String toURL(FileObject fileObject) {
        if (!(fileObject instanceof RemoteFileObject)) {
            return null;
        }

        ExecutionEnvironment env = ((RemoteFileObject) fileObject).getExecutionEnvironment();
        String path = fileObject.getPath();
        if (path == null || path.isEmpty()) {
            path = "/"; // NOI18N
        }
        return RemoteFileURLStreamHandler.PROTOCOL_PREFIX + ExecutionEnvironmentFactory.toUniqueID(env) + /*':' +*/ path;
    }

    @Override
    /**
     * Returns an URL-like string for the passed fileSystem and absPath.
     * Later it could be passed to the urlToFileObject() method to get a 
     * FileObject. 
     */
    public String toURL(FileSystem fileSystem, String absPath) {
        RemoteLogger.assertTrue(isAbsolute(absPath), "Path must be absolute: " + absPath); //NOPI18N        
        if (!(fileSystem instanceof RemoteFileSystem)) {
            throw new IllegalArgumentException("File system should be an istance of " + RemoteFileSystem.class.getName()); //NOI18N
        }

        ExecutionEnvironment env = ((RemoteFileSystem) fileSystem).getExecutionEnvironment();
        return RemoteFileURLStreamHandler.PROTOCOL_PREFIX + ExecutionEnvironmentFactory.toUniqueID(env) + /*':' +*/ absPath;
    }

    @Override
    public FileObject fileToFileObject(File file) {
        if (!(file instanceof FileObjectBasedFile)) {
            return null;
        }
        return ((FileObjectBasedFile) file).getFileObject();
    }

    @Override
    public boolean isMine(File file) {
        return file instanceof FileObjectBasedFile;
    }

    @Override
    public void refresh(FileObject fileObject, boolean recursive) {
        if (recursive) {
            fileObject.refresh();
        } else {
            ((RemoteFileObject)fileObject).nonRecursiveRefresh();
        }
    }

    @Override
    public void scheduleRefresh(FileObject fileObject) {
        if (fileObject instanceof RemoteFileObject) {
            RemoteFileObject fo = (RemoteFileObject) fileObject;
            RemoteFileSystemTransport.scheduleRefresh(fo.getExecutionEnvironment(), Arrays.asList(fo.getPath()));
        } else {
            RemoteLogger.getInstance().log(Level.WARNING, "Unexpected fileObject class: {0}", fileObject.getClass());
        }
    }

    @Override
    public void scheduleRefresh(ExecutionEnvironment env, Collection<String> paths) {
        RemoteFileSystemTransport.scheduleRefresh(env, paths);
    }

    @Override
    public void addRecursiveListener(FileChangeListener listener, FileSystem fileSystem, String absPath) {
        addRecursiveListener(listener, fileSystem, absPath, null, null);
    }

    @Override
    public void addRecursiveListener(FileChangeListener listener, FileSystem fileSystem, String absPath,  FileFilter recurseInto, Callable<Boolean> interrupter) {
        //TODO: use interrupter & filter
        RemoteLogger.assertTrue(fileSystem instanceof RemoteFileSystem, "Unexpected file system class: " + fileSystem); // NOI18N
        FileObject fileObject = fileSystem.findResource(absPath);
        if (fileObject != null) {
            fileObject.addRecursiveListener(listener);
        }
    }

    @Override
    public void removeRecursiveListener(FileChangeListener listener, FileSystem fileSystem, String absPath) {
        RemoteLogger.assertTrue(fileSystem instanceof RemoteFileSystem, "Unexpected file system class: " + fileSystem); // NOI18N
        FileObject fileObject = fileSystem.findResource(absPath);
        if (fileObject != null) {
            fileObject.removeRecursiveListener(listener);
        }
    }

    @Override
    public void addFileChangeListener(FileChangeListener listener, FileSystem fileSystem, String path) {
        RemoteLogger.assertTrue(fileSystem instanceof RemoteFileSystem, "Unexpected file system class: " + fileSystem); // NOI18N
        ((RemoteFileSystem) fileSystem).getFactory().addFileChangeListener(path, listener);

    }

    @Override
    public void addFileChangeListener(FileChangeListener listener, ExecutionEnvironment env, String path) {
        RemoteLogger.assertTrue(env.isRemote(), "Unexpected ExecutionEnvironment: should be remote"); // NOI18N
        RemoteFileSystemManager.getInstance().getFileSystem(env).getFactory().addFileChangeListener(path, listener);
    }

    @Override
    public void addFileChangeListener(FileChangeListener listener) {
        RemoteFileSystemManager.getInstance().addFileChangeListener(listener);
    }

    @Override
    public void removeFileChangeListener(FileChangeListener listener) {
        RemoteFileSystemManager.getInstance().removeFileChangeListener(listener);
    }

    @Override
    public boolean canExecute(FileObject fileObject) {
        RemoteLogger.assertTrue(fileObject instanceof RemoteFileObject, "Unexpected file object class: " + fileObject); // NOI18N
        if (fileObject instanceof RemoteFileObject) {
            return ((RemoteFileObject) fileObject).getImplementor().canExecute();
        }
        return false;
    }

    @Override
    public char getFileSeparatorChar() {
        return '/';
    }

    @Override
    public void addFileSystemProblemListener(FileSystemProblemListener listener, FileSystem fileSystem) {
        ((RemoteFileSystem) fileSystem).addFileSystemProblemListener(listener);
    }

    @Override
    public void addFileSystemProblemListener(FileSystemProblemListener listener) {
        RemoteFileSystem.addGlobalFileSystemProblemListener(listener);
    }

    @Override
    public void removeFileSystemProblemListener(FileSystemProblemListener listener, FileSystem fileSystem) {
        ((RemoteFileSystem) fileSystem).removeFileSystemProblemListener(listener);
    }
    
    @Override
    public void warmup(FileSystemProvider.WarmupMode mode, ExecutionEnvironment env, Collection<String> paths, Collection<String> extensions) {
        switch (mode) {
            case RECURSIVE_LS:
                if (!RemoteFileSystemUtils.getBoolean("remote.warmup.recursive.ls", true)) {
                    return;
                }
                break;
            case FILES_CONTENT:
                if (!RemoteFileSystemUtils.getBoolean("remote.warmup.files.content", true)) {
                    return;
                }
                break;
        }
        RemoteFileSystemManager.getInstance().getFileSystem(env).warmup(paths, mode, extensions);
    }    

    @Override
    public boolean isLink(FileSystem fileSystem, String path) {        
        return isLink(fileSystem.findResource(path));
    }

    @Override
    public boolean isLink(ExecutionEnvironment env, String path) {
        return isLink(getFileSystem(env, "/").findResource(path));
    }

    @Override
    public boolean isLink(FileObject fo) {
        try {
            return fo.isSymbolicLink();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex); // should never be the case, so let's report it that way
            return false; // as in java.nio.file.Files
        }
    }

    @Override
    public String resolveLink(FileObject fo) throws IOException {
        return fo.readSymbolicLinkPath();
    }

    @Override
    public InputStream getInputStream(FileObject fo, int maxSize) throws IOException {
        // instance check is in isMine
        return ((RemoteFileObject) fo).getInputStream(maxSize);
    }
    
    @Override
    public boolean canSetAccessCheckType(ExecutionEnvironment execEnv) {
        return RemoteFileSystemTransport.canSetAccessCheckType(execEnv);
    }

    @Override
    public void setAccessCheckType(ExecutionEnvironment execEnv, FileSystemProvider.AccessCheckType accessCheckType) {
        RemoteFileSystemTransport.setAccessCheckType(execEnv, accessCheckType);
    }
    
    @Override
    public FileSystemProvider.AccessCheckType getAccessCheckType(ExecutionEnvironment execEnv) {
        return RemoteFileSystemTransport.getAccessCheckType(execEnv);
    }

    @Override
    public FileSystemProvider.Stat getStat(FileObject fo) {
        if (fo instanceof RemoteFileObject) {
            RemoteFileObjectBase rfo = ((RemoteFileObject) fo).getImplementor();
            return rfo.getStat();
        }
        return FileSystemProvider.Stat.createInvalid();
    }

    @Override
    public void uploadAndUnzip(InputStream zipStream, final FileObject targetFolder) 
            throws FileNotFoundException, IOException, InterruptedException, ConnectException {
        if (targetFolder instanceof RemoteFileObject) {
            RemoteFileObjectBase impl = ((RemoteFileObject) targetFolder).getImplementor();            
            while (impl instanceof RemoteLinkBase) {
                RemoteFileObjectBase delegate = ((RemoteLinkBase) impl).getCanonicalDelegate();
                if (delegate != null) {
                    impl = delegate;
                }
            }
            if (impl instanceof RemoteDirectory) {
                ((RemoteDirectory) impl).uploadAndUnzip(zipStream);
            } else {
                throw new IOException("Unexpected file object class for " + impl + //NOI18N
                        ", expected " + RemoteDirectory.class.getSimpleName() + //NOI18N
                        " initial FileObject " + targetFolder); //NOI18N
            }
        }
    }

    @Override
    public void suspendWritesUpload(FileObject folder) throws IOException {
        if (folder instanceof RemoteFileObject) {
            RemoteFileObjectBase impl = ((RemoteFileObject) folder).getImplementor();            
            while (impl instanceof RemoteLinkBase) {
                RemoteFileObjectBase delegate = ((RemoteLinkBase) impl).getCanonicalDelegate();
                if (delegate != null) {
                    impl = delegate;
                }
            }
            if (impl instanceof RemoteDirectory) {
                ((RemoteDirectory) impl).suspendWritesUpload();
            } else {
                throw new IOException("Unexpected file object class for " + impl + //NOI18N
                        ", expected " + RemoteDirectory.class.getSimpleName() + //NOI18N
                        " initial FileObject " + folder); //NOI18N
            }
        }
    }

    @Override
    public void resumeWritesUpload(FileObject folder) 
        throws IOException, InterruptedException, ConnectException {
        if (folder instanceof RemoteFileObject) {
            RemoteFileObjectBase impl = ((RemoteFileObject) folder).getImplementor();            
            while (impl instanceof RemoteLinkBase) {
                RemoteFileObjectBase delegate = ((RemoteLinkBase) impl).getCanonicalDelegate();
                if (delegate != null) {
                    impl = delegate;
                }
            }
            if (impl instanceof RemoteDirectory) {
                ((RemoteDirectory) impl).resumeWritesUpload();
            } else {
                throw new IOException("Unexpected file object class for " + impl + //NOI18N
                        ", expected " + RemoteDirectory.class.getSimpleName() + //NOI18N
                        " initial FileObject " + folder); //NOI18N
            }
        }
    }
}
