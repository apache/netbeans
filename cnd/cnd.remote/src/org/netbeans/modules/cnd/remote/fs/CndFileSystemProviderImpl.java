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

package org.netbeans.modules.cnd.remote.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.EnvUtils;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemCacheProvider;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.spi.FileSystemProvider.FileSystemProblemListener;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=CndFileSystemProvider.class)
public class CndFileSystemProviderImpl extends CndFileSystemProvider {

   /** just to speed it up, since Utilities.isWindows will get string property, test equals, etc */
   private static final boolean isWindows = Utilities.isWindows();
   private String cachePrefix;

    public CndFileSystemProviderImpl() {
    }

    @Override
    protected FileObject toFileObjectImpl(CharSequence absPath) {
        FileSystemAndString p = getFileSystemAndRemotePath(absPath);
        if (p == null) {
            return FileSystemProvider.urlToFileObject(absPath.toString());
        } else {
            return p.getFileObject();
        }
    }

    @Override
    protected FileObject toFileObjectImpl(File file) {
        return FileSystemProvider.fileToFileObject(file);
    }

    @Override
    protected CharSequence fileObjectToUrlImpl(FileObject fileObject) {
        return FileSystemProvider.fileObjectToUrl(fileObject);
    }

    @Override
    protected CharSequence toUrlImpl(FSPath fsPath) {
        return FileSystemProvider.toUrl(fsPath.getFileSystem(), fsPath.getPath());
    }

    @Override
    protected CharSequence toUrlImpl(FileSystem fileSystem, CharSequence absPath) {
        return FileSystemProvider.toUrl(fileSystem, absPath.toString());
    }

    @Override
    protected CharSequence getCanonicalPathImpl(FileSystem fileSystem, CharSequence absPath) throws IOException {
        return FileSystemProvider.getCanonicalPath(fileSystem, absPath.toString());
    }

    @Override
    protected FileObject getCanonicalFileObjectImpl(FileObject fo) throws IOException {
        return FileSystemProvider.getCanonicalFileObject(fo);
    }

    @Override
    protected String getCanonicalPathImpl(FileObject fo) throws IOException {
        return FileSystemProvider.getCanonicalPath(fo);
    }

    @Override
    protected String normalizeAbsolutePathImpl(FileSystem fs, String absPath) {
        return FileSystemProvider.normalizeAbsolutePath(absPath, fs);
    }

    @Override
    protected FileObject urlToFileObjectImpl(CharSequence url) {
        // That's legacy: an url can be a path to RFS cache file.
        FileSystemAndString p = getFileSystemAndRemotePath(url);
        if (p == null) {
            return FileSystemProvider.urlToFileObject(url.toString());
        } else {
            return p.getFileObject();
        }
    }

    @Override
    protected FileSystem urlToFileSystemImpl(CharSequence url) {
        // That's legacy: an url can be a path to RFS cache file.
        FileSystemAndString p = getFileSystemAndRemotePath(url);
        if (p == null) {
            return FileSystemProvider.urlToFileSystem(url.toString());
        } else {
            return p.getFileSystem();
        }
    }

    @Override
    protected FileInfo[] getChildInfoImpl(CharSequence path) {
        FileSystemAndString p = getFileSystemAndRemotePath(path);
        if (p != null) {
            CndUtils.assertNotNull(p.fileSystem, "null file system"); //NOI18N
            CndUtils.assertNotNull(p.remotePath, "null remote path"); //NOI18N
            FileObject dirFO = p.getFileObject();
            if (dirFO == null) {
                return new FileInfo[0];
            }
            FileObject[] children = dirFO.getChildren();
            FileInfo[] result = new FileInfo[children.length];
            for (int i = 0; i < children.length; i++) {
                result[i] = new FileInfo(path.toString() + '/' + children[i].getNameExt(), children[i].isFolder(), children[i].isData());
            }
            return result;
        }
        return null;
    }

    @Override
    @org.netbeans.api.annotations.common.SuppressWarnings("NP") // three state
    protected Boolean canReadImpl(CharSequence path) {
        FileSystemAndString p = getFileSystemAndRemotePath(path);
        if (p != null) {
            CndUtils.assertNotNull(p.fileSystem, "null file system"); //NOI18N
            CndUtils.assertNotNull(p.remotePath, "null remote path"); //NOI18N
            FileObject fo = p.getFileObject();
            return (fo != null && fo.isValid() && fo.canRead());
        }
        return null;
    }

    @Override
    @org.netbeans.api.annotations.common.SuppressWarnings("NP") // three state
    protected Boolean existsImpl(CharSequence path) {
        FileSystemAndString p = getFileSystemAndRemotePath(path);
        if (p != null) {
            CndUtils.assertNotNull(p.fileSystem, "null file system"); //NOI18N
            CndUtils.assertNotNull(p.remotePath, "null remote path"); //NOI18N
            FileObject fo = p.getFileObject();
            return (fo != null && fo.isValid());
        }
        return null;
    }

    @Override
    protected boolean addFileChangeListenerImpl(FileChangeListener listener, FileSystem fileSystem, String path) {
       FileSystemProvider.addFileChangeListener(listener, fileSystem, path);
       return true;
    }

    @Override
    protected boolean removeFileChangeListenerImpl(FileChangeListener listener, FileSystem fileSystem, String path) {
        FileSystemProvider.removeRecursiveListener(listener, fileSystem, path);
        return true;
    }

    @Override
    protected boolean addFileChangeListenerImpl(FileChangeListener listener) {
        FileSystemProvider.addFileChangeListener(listener);
        return true;
    }

    @Override
    protected boolean removeFileChangeListenerImpl(FileChangeListener listener) {
        FileSystemProvider.removeFileChangeListener(listener);
        return true;
    }



    private FileSystemAndString getFileSystemAndRemotePath(CharSequence path) {
        String prefix = getPrefix();
        if (prefix != null) {
            if (isWindows) {
                path = path.toString().replace('\\', '/');
            }
            if (pathStartsWith(path, prefix)) {
                CharSequence rest = path.subSequence(prefix.length(), path.length());
                int slashPos = CharSequenceUtils.indexOf(rest, "/"); // NOI18N
                if (slashPos >= 0) {
                    String envID = rest.subSequence(0, slashPos).toString();
                    CharSequence remotePath = rest.subSequence(slashPos + 1, rest.length());
                    ExecutionEnvironment env = getExecutionEnvironmentByEnvID(envID);
                    if (env != null) {
                        FileSystem fs = FileSystemProvider.getFileSystem(env);
                        return new FileSystemAndString(fs, remotePath);
                    }
                }
            }
        }
        return null;
    }

    private synchronized String getPrefix() {
        if (cachePrefix == null) {
            String cacheRoot = FileSystemCacheProvider.getCacheRoot(ExecutionEnvironmentFactory.getLocal());
            if (cacheRoot != null) {
                String prefix = new File(cacheRoot).getParent();
                if (prefix != null) {
                    prefix= prefix.replace("\\", "/"); //NOI18N
                    if (!prefix.endsWith("/")) { //NOI18N
                        prefix += '/';
                    }
                    cachePrefix = prefix;
                }
            }
        }
        return cachePrefix;
    }

    private boolean pathStartsWith(CharSequence path, CharSequence prefix) {
        if (CndFileUtils.isSystemCaseSensitive()) {
            return CharSequenceUtils.startsWith(path, prefix);
        } else {
            return CharSequenceUtils.startsWithIgnoreCase(path, prefix);
        }
    }

    private static ExecutionEnvironment getExecutionEnvironmentByEnvID(String envID) {
        // envID has form: hostId + '_' + userId
        ExecutionEnvironment result = null;
        for(ExecutionEnvironment env : ServerList.getEnvironments()) {
            String currHostID = EnvUtils.toHostID(env);
            if (envID.startsWith(currHostID)) {
                if (envID.length() > currHostID.length() && envID.charAt(currHostID.length()) == '_') {
                    String user = envID.substring(currHostID.length() + 1);
                    if (user.equals(env.getUser())) {
                        return env;
                    }
                }
            }
        }
        return result;
    }

    private final List<ProblemListenerAdapter> adapters = new ArrayList<>();

    private void cleanDeadListeners() {
        assert Thread.holdsLock(adapters);
        for (Iterator<ProblemListenerAdapter> it = adapters.iterator(); it.hasNext();) {
            ProblemListenerAdapter adapter = it.next();
            if (adapter.listenerRef.get() == null) {
                it.remove();
            }
        }
    }
    
    @Override
    protected void addFileSystemProblemListenerImpl(CndFileSystemProblemListener listener) {
        ProblemListenerAdapter newAdapter = new ProblemListenerAdapter(listener, null);
        synchronized (adapters) {
            cleanDeadListeners();
            adapters.add(newAdapter);
        }
        FileSystemProvider.addFileSystemProblemListener(newAdapter);
    }

    @Override
    protected void fireFileSystemProblemOccurredImpl(FSPath fsPath) {
        List<CndFileSystemProblemListener> listeners = new ArrayList<>();
        synchronized (adapters) {
            for (ProblemListenerAdapter adapter : adapters) {
                CndFileSystemProblemListener l = adapter.listenerRef.get();
                if (l != null) {                    
                    FileSystem listenerFS = (adapter.fileSystemRef == null) ? null : adapter.fileSystemRef.get();
                    if (listenerFS == null || listenerFS.equals(fsPath.getFileSystem())) {
                        listeners.add(l);
                    }
                }
            }
        }
        for (CndFileSystemProblemListener l : listeners) {
            l.problemOccurred(fsPath);
        }
    }

    @Override
    protected void addFileSystemProblemListenerImpl(CndFileSystemProblemListener listener, FileSystem fileSystem) {
        ProblemListenerAdapter newAdapter = new ProblemListenerAdapter(listener, fileSystem);
        synchronized (adapters) {
            cleanDeadListeners();
            adapters.add(newAdapter);
        }
        FileSystemProvider.addFileSystemProblemListener(newAdapter, fileSystem);
    }

    @Override
    protected boolean isAbsoluteImpl(FileSystem fs, String path) {
        return FileSystemProvider.isAbsolute(fs, path);
    }

    @Override
    protected void removeFileSystemProblemListenerImpl(CndFileSystemProblemListener listener, FileSystem fileSystem) {
        synchronized (adapters) {
            for (Iterator<ProblemListenerAdapter> it = adapters.iterator(); it.hasNext(); ) {
                ProblemListenerAdapter adapter = it.next();
                CndFileSystemProblemListener l = adapter.listenerRef.get();
                if (l == null) {
                    it.remove();
                } else if (l == listener) {
                    FileSystemProvider.removeFileSystemProblemListener(adapter, fileSystem);
                    it.remove();
                }
            }
        }
    }

    private static HostInfo getHostInfoIfAvailable(ExecutionEnvironment env) {
        if (HostInfoUtils.isHostInfoAvailable(env)) {
            try {
                return HostInfoUtils.getHostInfo(env);
            } catch (IOException | ConnectionManager.CancellationException ex) {
                Exceptions.printStackTrace(ex); // should not be the case since since we checked isHostInfoAvailable
            }
        }
        return null;
    }

    @Override
    protected boolean isMacOSImpl(FileSystem fs) {
       ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(fs);
       if (env.isLocal()) {
           return Utilities.isMac();
       } else {
           HostInfo hostInfo = getHostInfoIfAvailable(env);
           if (hostInfo != null) {
               return hostInfo.getOSFamily() == HostInfo.OSFamily.MACOSX;
           } else {
               return false; // if no host info available we suppose that remote is Linux or Solaris
           }
       }
    }

    @Override
    protected boolean isWindowsImpl(FileSystem fs) {
       ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(fs);
       if (env.isLocal()) {
           return Utilities.isWindows();
       } else {
           HostInfo hostInfo = getHostInfoIfAvailable(env);
           if (hostInfo != null) {
               return hostInfo.getOSFamily() == HostInfo.OSFamily.WINDOWS;
           } else {
               return false; // remote is not windows
           }
       }
    }

    @Override
    protected boolean isRemoteImpl(FileSystem fs) {
        return FileSystemProvider.getExecutionEnvironment(fs).isRemote();
    }

    @Override
    protected CndStatInfo getStatInfoImpl(FileObject fo) {
       FileSystemProvider.Stat stat = FileSystemProvider.getStat(fo);
       return stat.isValid() ? CndStatInfo.create(stat.device, stat.inode) : CndStatInfo.createInvalid();
    }

    @Override
    protected InputStream getInputStreamImpl(FileObject fo, int maxSize) throws IOException {
        return FileSystemProvider.getInputStream(fo, maxSize);
    }

    private static class ProblemListenerAdapter implements FileSystemProblemListener {

        private final WeakReference<CndFileSystemProblemListener> listenerRef;
        private final WeakReference<FileSystem> fileSystemRef;

        public ProblemListenerAdapter(CndFileSystemProblemListener listener, FileSystem fileSystem) {
            listenerRef = new WeakReference<>(listener);
            fileSystemRef = (fileSystem == null) ? null : new WeakReference<>(fileSystem);
        }

        @Override
        public void problemOccurred(FileSystem fileSystem, String path) {
            checkFileSystem(fileSystem);
            CndFileSystemProblemListener listener = listenerRef.get();
            if (listener != null) {
                listener.problemOccurred(new FSPath(fileSystem, path));
            }
        }

        @Override
        public void recovered(FileSystem fileSystem) {
            checkFileSystem(fileSystem);
            CndFileSystemProblemListener listener = listenerRef.get();
            if (listener != null) {
                listener.recovered(fileSystem);
            }
        }
        
        private void checkFileSystem(FileSystem fileSystem) {
            if (fileSystemRef != null && CndUtils.isDebugMode()) {
                FileSystem fs = fileSystemRef.get();
                if (fs != null) {
                    CndUtils.assertTrue(fs.equals(fileSystem), "Unexpected file system: " + fileSystem + " , expected " + fs); //NOI18N
                }
            }
        }
    }

    private static class FileSystemAndString {

        public final FileSystem fileSystem;
        public final CharSequence remotePath;

        public FileSystemAndString(FileSystem fs, CharSequence path) {
            this.fileSystem = fs;
            this.remotePath = path;
        }

        public FileObject getFileObject() {
            return fileSystem.findResource(remotePath.toString());
        }

        public FileSystem getFileSystem() {
            return fileSystem;
        }
    }
}
