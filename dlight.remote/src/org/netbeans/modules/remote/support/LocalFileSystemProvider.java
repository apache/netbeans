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

package org.netbeans.modules.remote.support;

import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.dlight.libs.common.DLightLibsCommonLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;import org.netbeans.modules.remote.api.RemoteFile;
;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.spi.FileSystemProvider.FileSystemProblemListener;
import org.netbeans.modules.remote.spi.FileSystemProvider.WarmupMode;
import org.netbeans.modules.remote.spi.FileSystemProviderImplementation;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 */
@ServiceProvider(service = org.netbeans.modules.remote.spi.FileSystemProviderImplementation.class, position=100)
public final class LocalFileSystemProvider implements FileSystemProviderImplementation {
    private static final String FILE_PROTOCOL = "file"; // NOI18N
    private static final String FILE_PROTOCOL_PREFIX = "file:"; // NOI18N
    private static final Path ROOT_PATH = Paths.get("/"); // NOI18N

    private FileSystem rootFileSystem = null;
    private final Map<String, LocalFileSystem> nonRootFileSystems = new HashMap<String, LocalFileSystem>();
    private final boolean isWindows = Utilities.isWindows();
    private static final RequestProcessor RP = new RequestProcessor(LocalFileSystemProvider.class.getSimpleName());
    private static volatile RequestProcessor.Task lastRefreshTask;

    @Override
    public String normalizeAbsolutePath(String absPath, ExecutionEnvironment env) {
        return FileUtil.normalizePath(absPath);
    }

    @Override
    public String normalizeAbsolutePath(String absPath, FileSystem fileSystem) {
        return FileUtil.normalizePath(absPath);
    }

    @Override
    public boolean isAbsolute(String path) {
        return new File(path).isAbsolute();
    }
    
    @Override
    public FileObject getFileObject(FileObject baseFileObject, String relativeOrAbsolutePath) {
        String absPath;
        if (FileSystemProvider.isAbsolute(relativeOrAbsolutePath)) {
            absPath = relativeOrAbsolutePath;
            
        } else {
            absPath = baseFileObject.getPath() + File.separatorChar + relativeOrAbsolutePath.toString();
        }
        return FileUtil.toFileObject(new File(FileUtil.normalizePath(absPath)));
    }

    private FileSystem getRootFileSystem() {
        if (rootFileSystem == null) {
            File tmpFile = null;
            try {
                tmpFile = File.createTempFile("NetBeans", ".tmp"); //NOI18N
                tmpFile = FileUtil.normalizeFile(tmpFile);
                FileObject fo = FileUtil.toFileObject(tmpFile);
                rootFileSystem = fo.getFileSystem();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (tmpFile != null) {
                    tmpFile.delete();
                }
            }
        }
        return rootFileSystem;
    }

    @Override
    public FileSystem getFileSystem(ExecutionEnvironment env, String root) {
        if (env.isLocal()) {
            synchronized (this) {
                if ("/".equals(root) || "".equals(root)) { // NOI18N
                    return getRootFileSystem();
                } else {
                    LocalFileSystem fs = nonRootFileSystems.get(root);
                    if (fs == null) {
                        fs = new LocalFileSystem();
                        try {
                            fs.setRootDirectory(new File(root));
                            nonRootFileSystems.put(root, fs);
                        } catch (PropertyVetoException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    return fs;
                }
            }
        }
        return null;
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        assert isMine(uri);
        return getFileSystem(ExecutionEnvironmentFactory.getLocal(), uri.getPath());
    }

    @Override
    public FileObject getCanonicalFileObject(FileObject fileObject) throws IOException {
        File file = FileUtil.toFile(fileObject);
        RemoteLogger.assertTrueInConsole(file != null, "null file for fileObject " + fileObject); //NOI18N
        if (file == null) {
            return fileObject;
        } else {
            File canonicalFile = file.getCanonicalFile();
            if (canonicalFile.equals(file)) {
                return fileObject;
            } else {
                FileObject canonicalFileObject = FileUtil.toFileObject(canonicalFile);
                RemoteLogger.assertTrueInConsole(canonicalFileObject != null, "null canonical file object for file " + canonicalFile); //NOI18N
                return (canonicalFileObject == null) ? fileObject : canonicalFileObject;
            }
        }
    }

    @Override
    public String getCanonicalPath(FileObject fileObject) throws IOException {
        return getCanonicalFileObject(fileObject).getPath();
    }

    @Override
    public String getCanonicalPath(FileSystem fs, String absPath) throws IOException {
        return new File(absPath).getCanonicalPath();
    }
    
    @Override
    public String getCanonicalPath(ExecutionEnvironment env, String absPath) throws IOException {
        RemoteLogger.assertTrueInConsole(env.isLocal(), getClass().getSimpleName() + ".getCanonicalPath is called for REMOTE env: " + env); //NOI18N
        return new File(absPath).getCanonicalPath();
    }
    
    @Override
    public boolean isMine(ExecutionEnvironment env) {
        return env.isLocal();
    }

    @Override
    public ExecutionEnvironment getExecutionEnvironment(FileSystem fileSystem) {
        return ExecutionEnvironmentFactory.getLocal();
    }

    @Override
    public boolean isMine(FileObject fileObject) {
        try {
            return isMine(fileObject.getFileSystem());
        } catch (FileStateInvalidException ex) {
            RemoteLogger.getInstance().log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
        return false;
    }

    @Override
    public boolean isMine(FileSystem fileSystem) {
        if (fileSystem instanceof LocalFileSystem) {
            return true;
        } else {
            FileSystem rootFS = getRootFileSystem();
            if (rootFS != null && rootFS.getClass() == fileSystem.getClass()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMine(String absoluteURL) {
        if (absoluteURL.length() == 0) {
            return true;
        }
        if (absoluteURL.startsWith(FILE_PROTOCOL_PREFIX)) {
            return true;
        }
        if (isWindows) {
            return (absoluteURL.length() > 1) && absoluteURL.charAt(1) == ':';
        } else {
            return absoluteURL.startsWith("/"); //NOI18N
        }
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
    public FileObject urlToFileObject(String absoluteURL) {
        DLightLibsCommonLogger.assertNonUiThreadOnce(Level.INFO);
        
        String path = absoluteURL;
        File file;
        if (path.startsWith(FILE_PROTOCOL_PREFIX)) {
            try {
                URL u = new URL(path);
                file = FileUtil.normalizeFile(Utilities.toFile(u.toURI()));
            } catch (IllegalArgumentException ex) {
                RemoteLogger.getInstance().log(Level.WARNING, "LocalFileSystemProvider.urlToFileObject can not convert {0}:\n{1}", new Object[]{absoluteURL, ex.getLocalizedMessage()});
                return null;
            } catch (URISyntaxException ex) {
                RemoteLogger.getInstance().log(Level.WARNING, "LocalFileSystemProvider.urlToFileObject can not convert {0}:\n{1}", new Object[] {absoluteURL, ex.getLocalizedMessage()});
                return null;
            } catch (MalformedURLException ex) {
                RemoteLogger.getInstance().log(Level.WARNING, "LocalFileSystemProvider.urlToFileObject can not convert {0}:\n{1}", new Object[] {absoluteURL, ex.getLocalizedMessage()});
                return null;
            }        
        } else {
            file = new File(FileUtil.normalizePath(path));
        }
        try {
            return FileUtil.toFileObject(file);
        } catch (Throwable ex) {
            RemoteLogger.getInstance().log(Level.WARNING, "LocalFileSystemProvider.urlToFileObject can not convert {0}:\n{1}", new Object[]{absoluteURL, ex.getLocalizedMessage()});
            return null;
        }
    }
    
    @Override
    public FileSystem urlToFileSystem(String rootUrl) {
        if (rootUrl.isEmpty()) {
            return getRootFileSystem();
        } else {
            FileObject root = urlToFileObject(rootUrl);
            if (root != null) {
                try {
                    return root.getFileSystem();
                } catch (FileStateInvalidException ex) {
                    RemoteLogger.getInstance().log(Level.WARNING, "LocalFileSystemProvider.urlToFileSystem can not convert {0}:\n{1}", new Object[]{rootUrl, ex.getLocalizedMessage()});
                    return null;
                }
            }
            return null;
        }
    }

    @Override
    public String toURL(FileObject fileObject) {
        return fileObject.getPath();
    }
    
    @Override
    public String toURL(FileSystem fileSystem, String absPath) {
        return absPath;
    }

    @Override
    public FileObject fileToFileObject(File file) {
        file = FileUtil.normalizeFile(file); // caller can not do this
        return FileUtil.toFileObject(file);
    }

    @Override
    public boolean isMine(File file) {
        return !RemoteFile.class.isAssignableFrom(file.getClass());
    }

    @Override
    public boolean isMine(URI uri) {
        return uri.getScheme().equals(FILE_PROTOCOL);
    }

    @Override
    public void refresh(FileObject fileObject, boolean recursive) {
        fileObject.refresh();
    }

    @Override
    public void scheduleRefresh(FileObject fileObject) {
        final File file = FileUtil.toFile(fileObject);
        scheduleRefresh(file);
    }

    @Override
    public void scheduleRefresh(ExecutionEnvironment env, Collection<String> paths) {
        RemoteLogger.assertTrue(env.isLocal());
        File[] files = new File[paths.size()];
        int pos = 0;
        for (String path : paths) {
            files[pos++] = new File(path);
        }
        scheduleRefresh(files);
    }
    
    private void scheduleRefresh(final File... files) {
        lastRefreshTask = RP.post(new Runnable() {
            @Override
            public void run() {
                FileUtil.refreshFor(files);
            }
        });
    }

    @Override
    public void addRecursiveListener(FileChangeListener listener, FileSystem fileSystem, String absPath) {
        addRecursiveListener(listener, fileSystem, absPath, null, null);
    }
    
    @Override
    public void addRecursiveListener(FileChangeListener listener, FileSystem fileSystem, String absPath,  FileFilter recurseInto, Callable<Boolean> interrupter) {
        File file = new File(absPath);
        FileUtil.addRecursiveListener(listener, file, recurseInto, interrupter);
    }

    @Override
    public void removeRecursiveListener(FileChangeListener listener, FileSystem fileSystem, String absPath) {
        File file = new File(absPath);
        FileUtil.removeRecursiveListener(listener, file);
    }

    @Override
    public boolean canExecute(FileObject fileObject) {
        File file = FileUtil.toFile(fileObject);
        return (file == null) ?  false : file.canExecute();
    }

    @Override
    public void addFileChangeListener(FileChangeListener listener, FileSystem fileSystem, String path) {
        addFileChangeListener(path, listener);
    }

    @Override
    public void addFileChangeListener(FileChangeListener listener, ExecutionEnvironment env, String path) {
        addFileChangeListener(path, listener);
    }
    
    private void addFileChangeListener(String path, FileChangeListener listener) {
        File file = new File(path);
        file = FileUtil.normalizeFile(file);
        FileUtil.addFileChangeListener(listener, file);
    }

    @Override
    public void addFileChangeListener(FileChangeListener listener) {
        FileUtil.addFileChangeListener(listener);
    }

    @Override
    public void removeFileChangeListener(FileChangeListener listener) {
        FileUtil.removeFileChangeListener(listener);
    }

    /** for TEST purposes ONLY */
    public static void testWaitLastRefreshFinished() {
        Task task = lastRefreshTask;
        if (task != null) {
            task.waitFinished();
        }
    }

    @Override
    public char getFileSeparatorChar() {
        return File.separatorChar;
    }

    @Override
    public void addFileSystemProblemListener(FileSystemProblemListener listener, FileSystem fileSystem) {
    }

    @Override
    public void addFileSystemProblemListener(FileSystemProblemListener listener) {
    }

    @Override
    public void removeFileSystemProblemListener(FileSystemProblemListener listener, FileSystem fileSystem) {
    }
    
    @Override
    public void warmup(WarmupMode mode, ExecutionEnvironment env, Collection<String> paths, Collection<String> extensions) {        
    }    

    @Override
    public boolean isLink(FileSystem fileSystem, String path) {
        return isLink(path);
    }

    @Override
    public boolean isLink(ExecutionEnvironment env, String path) {
        return isLink(path);
    }

    @Override
    public boolean isLink(FileObject fo) {
        return isLink(fo.getPath());
    }

    private static boolean isLink(String path) {
        Path filePath = Paths.get(Utilities.toURI(new File(path)));
        return Files.isSymbolicLink(filePath);
    }

    @Override
    public String resolveLink(final FileObject fo) throws IOException {
        final Path filePath = Paths.get(fo.toURI());
        if (Files.isSymbolicLink(filePath)) {
            try {
                return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                    @Override
                    public String run() throws IOException {
                        Path linkPath = Files.readSymbolicLink(filePath);
                        if (!linkPath.isAbsolute()) {
                            linkPath = filePath.getParent().resolve(linkPath).normalize();
                        }
                        return linkPath.toFile().getAbsolutePath();
                    }
                });
            } catch (PrivilegedActionException ex) {
                ex.printStackTrace(System.err);
            }
        }
        return null;
    }

    @Override
    public InputStream getInputStream(FileObject fo, int maxSize) throws IOException {
        return fo.getInputStream();
    }

    @Override
    public boolean canSetAccessCheckType(ExecutionEnvironment execEnv) {
        return false;
    }

    @Override
    public void setAccessCheckType(ExecutionEnvironment execEnv, FileSystemProvider.AccessCheckType accessCheckType) {
    }

    @Override
    public FileSystemProvider.AccessCheckType getAccessCheckType(ExecutionEnvironment execEnv) {
        return null;
    }

    @Override
    public FileSystemProvider.Stat getStat(FileObject fo) {
        if (Utilities.isWindows()) {
            Checksum cs = new Adler32();
            String path = fo.getPath();
            for (int i = 0; i < path.length(); i++) {
                cs.update(path.charAt(i));
            }
            return FileSystemProvider.Stat.create(Long.MIN_VALUE, cs.getValue());
        } else {
            Path path = ROOT_PATH.resolve(fo.getPath());
            if (Files.exists(path)) {
                try {
                    long st_ino = ((Number)Files.getAttribute(path, "unix:ino")).longValue(); //NOI18N
                    long st_dev = ((Number)Files.getAttribute(path, "unix:dev")).longValue(); //NOI18N
                    return FileSystemProvider.Stat.create(st_dev, st_ino);
                } catch (IOException ex) {
                    RemoteLogger.finest(ex);
                }
            }
        }
        return FileSystemProvider.Stat.createInvalid();
    }

    @Override
    public void uploadAndUnzip(InputStream zipStream, FileObject targetFolder) throws FileNotFoundException, IOException {
        try(ZipInputStream zip = new ZipInputStream(zipStream)) {
            ZipEntry ent;
            while ((ent = zip.getNextEntry()) != null) {
                if (ent.isDirectory()) {
                    FileUtil.createFolder(targetFolder, ent.getName());
                } else {
                    FileObject f = FileUtil.createData(targetFolder, ent.getName());
                    try (OutputStream out = f.getOutputStream()) {
                        FileUtil.copy(zip, out);
                    }
                }
            }
        }
    }

    @Override
    public void suspendWritesUpload(FileObject folder) {
    }

    @Override
    public void resumeWritesUpload(FileObject folder) {
        folder.refresh();
    }
}
