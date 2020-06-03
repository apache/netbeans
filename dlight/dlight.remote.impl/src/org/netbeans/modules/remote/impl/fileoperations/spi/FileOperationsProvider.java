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
package org.netbeans.modules.remote.impl.fileoperations.spi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import org.netbeans.api.extexecution.ProcessBuilder;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fs.DirEntry;
import org.netbeans.modules.remote.impl.fs.RemoteDirectory;
import org.netbeans.modules.remote.impl.fs.RemoteExceptions;
import org.netbeans.modules.remote.impl.fs.RemoteFileObject;
import org.netbeans.modules.remote.impl.fs.RemoteFileObjectBase;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystem;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystem.FileInfo;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemManager;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemTransport;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemUtils;
import org.netbeans.modules.remote.impl.fs.RemoteFileUrlMapper;
import org.netbeans.modules.remote.impl.fs.RemotePlainFile;
import org.netbeans.spi.extexecution.ProcessBuilderFactory;
import org.netbeans.spi.extexecution.ProcessBuilderImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;

/**
 *
 */
abstract public class FileOperationsProvider {
    public static final String ATTRIBUTE = "FileProxyOperations"; // NOI18N        
    private static FileOperationsProvider defaultProvider;

    protected FileOperationsProvider() {
    }

    abstract public FileOperations getFileOperations(FileSystem fs);

    abstract public static class FileOperations {

        private final ExecutionEnvironment env;
        private final RemoteFileSystem fileSystem;
        //private final RequestProcessor RP;
        
        private static final boolean USE_CACHE;
        static {
            String text = System.getProperty("rfs.vcs.cache");
            USE_CACHE = (text == null) ? true : Boolean.parseBoolean(text);
        }

        protected FileOperations(FileSystem fs) {
            FileObject root = fs.getRoot();
            if (root instanceof RemoteFileObject) {
                env = ((RemoteFileObject)root).getExecutionEnvironment();
                fileSystem = (RemoteFileSystem) fs;
                //RP = new RequestProcessor("Refresh for "+env); //NOI18N
            } else {
                throw new IllegalArgumentException();
            }
        }

        protected String getName(FileProxyO file) {
            return PathUtilities.getBaseName(file.getPath());
        }

        protected String getDir(FileProxyO file) {
            return PathUtilities.getDirName(file.getPath());
        }
        
        protected String normalizeUnixPath(FileProxyO file) {
            String path = PathUtilities.normalizeUnixPath(file.getPath());
            // TODO resolve inconsistency of PathUtilities && FileUtils.
            if (path.isEmpty() && file.getPath().startsWith("/") || //NOI18N
                path.equals("/..")) { //NOI18N
                return "/"; //NOI18N
            }
            return path;
        }

        private RemoteFileObject getFileObject(FileProxyO file) {
            String path = PathUtilities.normalizeUnixPath(file.getPath());
            RemoteFileObjectBase cached = fileSystem.getFactory().getCachedFileObject(path);
            RemoteFileObject fo;
            if (cached != null && cached.isValid()) {
                fo = cached.getOwnerFileObject();
            } else {
                fo = fileSystem.findResource(path);
            }
            return fo;
        }
        
        protected boolean isDirectory(FileProxyO file) {
            if (RemoteVcsSupportUtil.USE_FS && !fileSystem.isInsideVCS()) {
                RemoteFileObject fo = getFileObject(file);
                return (fo == null || ! fo.isValid()) ? false : fo.isFolder();
            }
            if (USE_CACHE) {
                Boolean res = fileSystem.vcsSafeIsDirectory(file.getPath());
                if (res != null) {
                    return res.booleanValue();
                }
            }
            FileInfo beingCreated = fileSystem.getBeingCreated();
            if (beingCreated != null) {
                if (beingCreated.getPath().equals(file.getPath())) {
                    if (beingCreated.getType() == FileType.Regular) {
                        return false;
                    } else if (beingCreated.getType() == FileType.Directory) {
                        return true;
                    }
                }
            }
            
            if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                return false;
            }
            try {
                DirEntry entry = RemoteFileSystemTransport.stat(env, file.getPath());
                return entry.isDirectory(); 
            } catch (ConnectException ex) {
                RemoteLogger.finest(ex);
            } catch (InterruptedException | IOException | TimeoutException ex) {
                RemoteLogger.finest(ex);
            } catch (ExecutionException ex) {
                if (RemoteFileSystemUtils.isFileNotFoundException(ex)) {
                    return false;
                }
                ex.printStackTrace(System.err);
            }
            return false;
        }
        
        protected long lastModified(FileProxyO file) {
            if (RemoteVcsSupportUtil.USE_FS && !fileSystem.isInsideVCS()) {
                RemoteFileObject fo = getFileObject(file);
                return (fo == null || !fo.isValid()) ? -1 : fo.lastModified().getTime();
            }
            if (USE_CACHE) {
                Long res = fileSystem.vcsSafeLastModified(file.getPath());
                if (res != null) {
                    return res.longValue();
                }
            }
            if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                return -1;
            }
            try {
                DirEntry entry = RemoteFileSystemTransport.stat(env, file.getPath());
                return entry.getLastModified().getTime();
            } catch (ConnectException ex) {
                RemoteLogger.finest(ex);
            } catch (InterruptedException | IOException | TimeoutException ex) {
                RemoteLogger.finest(ex);
            } catch (ExecutionException ex) {
                if (RemoteFileSystemUtils.isFileNotFoundException(ex)) {
                    return -1;
                }
                ex.printStackTrace(System.err);
            }
            return -1;
        }

        protected boolean isFile(FileProxyO file) {
            if (RemoteVcsSupportUtil.USE_FS && !fileSystem.isInsideVCS()) {
                RemoteFileObject fo = getFileObject(file);
                if (fo == null || !fo.isValid()) {
                    if (fileSystem.getFactory().vcsIsUnconfirmedDeletion(file.getPath())) {
                        try {
                            DirEntry e = RemoteFileSystemTransport.lstat(getExecutionEnvironment(), file.getPath());
                            if (e == null) {
                                fileSystem.getFactory().vcsUnregisterUnconfirmedDeletion(file.getPath());
                            } else {
                                return !e.isDirectory();
                            }
                        } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
                            RemoteLogger.finest(e);
                        }
                    }
                    return false;
                } else {
                    return fo.isData();
                }
            }
            if (USE_CACHE) {
                Boolean res = fileSystem.vcsSafeIsFile(file.getPath());
                if (res != null) {
                    return res.booleanValue();
                }
            }
            if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                return false;
            }
            try {
                DirEntry entry = RemoteFileSystemTransport.stat(env, file.getPath());
                return entry.isPlainFile(); 
            } catch (ConnectException ex) {
                RemoteLogger.finest(ex);
            } catch (InterruptedException | IOException | TimeoutException ex) {
                RemoteLogger.finest(ex);
            } catch (ExecutionException ex) {
                if (RemoteFileSystemUtils.isFileNotFoundException(ex)) {
                    return false;
                }
                ex.printStackTrace(System.err);
            }
            return false;
        }
        
        protected boolean canWrite(FileProxyO file) {
            if (RemoteVcsSupportUtil.USE_FS && !fileSystem.isInsideVCS()) {
                RemoteFileObject fo = getFileObject(file);
                return (fo == null || !fo.isValid()) ? false : fo.canWrite();
            }
            if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                return false;
            }
            try {
                DirEntry entry = RemoteFileSystemTransport.stat(env, file.getPath());
                return entry.canWrite();
            } catch (ConnectException ex) {
                RemoteLogger.finest(ex);
            } catch (InterruptedException | IOException | TimeoutException ex) {
                RemoteLogger.finest(ex);
            } catch (ExecutionException ex) {
                if (RemoteFileSystemUtils.isFileNotFoundException(ex)) {
                    return false;
                }
                ex.printStackTrace(System.err);
            }
            return false;
        }
        
        protected FileObject getRoot() {
            RemoteFileSystem fs = getFileSystem();
            return fs.getRoot();
        }
        
        protected RemoteFileSystem getFileSystem() {
            return RemoteFileSystemManager.getInstance().getFileSystem(env);
        }
        
        protected String getPath(FileProxyO file) {
            return file.getPath();
        }

        protected URI toURI(String path, boolean folder) throws URISyntaxException {
            return RemoteFileUrlMapper.toURI(env, path, folder);
        }

        private boolean isKnownSniffingExtension(FileProxyO file) {
            return RemoteFileSystem.isSniffing(file.getPath());
        }

        protected boolean exists(FileProxyO file) {
            if (RemoteVcsSupportUtil.USE_FS && !fileSystem.isInsideVCS()) {
                RemoteFileObject fo = getFileObject(file);
                return fo != null && fo.isValid();
            }
            final String path = file.getPath();
            if (USE_CACHE && (fileSystem.isGettingDirectoryStorage() || isKnownSniffingExtension(file))) {
                Boolean res = fileSystem.vcsSafeExists(path);
                if (res != null) {
                    return res.booleanValue();
                }
            }
            // VCS asks for .git, .hg, etc in a directory that is now being created!
            // we need to filter it out
            FileInfo beingCreated = fileSystem.getBeingCreated();
            if (beingCreated != null) {
                if (path.startsWith(beingCreated.getPath())) {
                    int pos = beingCreated.getPath().length();
                    if (path.length() > pos && path.charAt(pos) == '/') {
                        return false;
                    }
                }
            }
            return existsSafe(file);
        }

        protected boolean existsSafe(FileProxyO file) {
            if (!ConnectionManager.getInstance().isConnectedTo(getExecutionEnvironment())) {
                return false;
            }
            try {
                // shouldn't we use stat instead of lstat?
                DirEntry entry = RemoteFileSystemTransport.lstat(
                        getExecutionEnvironment(), file.getPath());
                return entry != null;
            } catch (ConnectException ex) {
                RemoteLogger.finest(ex);
            } catch (InterruptedException | IOException | TimeoutException ex) {
                RemoteLogger.finest(ex);
            } catch (ExecutionException ex) {
                if (RemoteFileSystemUtils.isFileNotFoundException(ex)) {
                    return false;
                }
                RemoteLogger.finest(ex);
            }
            return false;
        }

        protected FileObject toFileObject(FileProxyO path) {
            RemoteFileObjectBase cachedFileObject = fileSystem.getFactory().getCachedFileObject(path.getPath());
            if (cachedFileObject != null && cachedFileObject.isValid()) {
                return cachedFileObject.getOwnerFileObject();
            }            
            FileObject root = getRoot();
            FileObject fo = root.getFileObject(path.getPath());
            if (fo == null && existsSafe(path)) {
                String parent = path.getPath();
                LinkedList<String> stack = new LinkedList<>();
                while(true) {
                    parent = PathUtilities.getDirName(parent);
                    if (parent == null) {
                        return null;
                    }
                    stack.addLast(parent);
                    FileObject parentFO = root.getFileObject(parent);
                    if (parentFO != null && parentFO.isValid()) {
                        break;
                    }
                }
                while(!stack.isEmpty()) {
                    parent = stack.removeLast();
                    FileObject parentFO = root.getFileObject(parent);
                    if (parentFO != null && parentFO.isValid()) {
                        parentFO.refresh();
                    } else {
                        return null;
                    }
                }
                fo = root.getFileObject(path.getPath());
            }
            return fo;
        }

        protected InputStream getInputStream(FileObject fo, boolean checkLock) throws FileNotFoundException {
            if (fo instanceof RemoteFileObject) {
                return ((RemoteFileObject)fo).getImplementor().getInputStream(checkLock);
            } else {
                return fo.getInputStream();
            }
        }

        protected String[] list(FileProxyO file) {
            if (isDirectory(file)) {
                if (RemoteVcsSupportUtil.USE_FS && !fileSystem.isInsideVCS()) {
                    RemoteFileObject fo = getFileObject(file);
                    if (fo == null) {
                        return null;
                    }
                    RemoteFileObject[] children = fo.getImplementor().getChildren();
                    String[] names = new String[children.length];
                    for (int i = 0; i < children.length; i++) {
                        names[i] = children[i].getNameExt();
                    }
                    return names;
                }                
                Future<FileInfoProvider.StatInfo[]> stat = FileInfoProvider.ls(env, file.getPath());
                try {
                    FileInfoProvider.StatInfo[] statInfo = stat.get();
                    if (statInfo != null) {
                        String[] res = new String[statInfo.length];
                        for (int i = 0; i < statInfo.length; i++) {
                            res[i] = statInfo[i].getName();
                        }
                        return res;
                    }
                } catch (InterruptedException ex) {
                } catch (ExecutionException ex) {
                    if (RemoteFileSystemUtils.isFileNotFoundException(ex)) {
                        return null;
                    }
                    ex.printStackTrace(System.err);
                }
            }
            return null;
        }

        protected ProcessBuilder createProcessBuilder(FileProxyO file) {
            return ProcessBuilderFactory.createProcessBuilder(new ProcessBuilderImplementationImpl(env), "RFS Process Builder"); // NOI18N
        }
        
        protected void refreshFor(FileProxyO ... files) {
            List<RemoteFileObjectBase> roots = new ArrayList<>();
            for(FileProxyO f : files) {
                RemoteFileObjectBase fo = findExistingParent(f.getPath());
                if (fo != null && fo.isValid()) {
                    roots.add(fo);
                }
            }
//            for(RemoteFileObjectBase fo : roots) {                
//                fo.refresh(true);
//            }
            if (!roots.isEmpty()) {
                RemoteFileSystem fs = roots.iterator().next().getFileSystem();
                String[] paths = new String[roots.size()];
                for (int i = 0; i < roots.size(); i++) {
                    paths[i] = roots.get(i).getPath();
                }
                try {
                    RemoteVcsSupportUtil.refreshFor(fs, paths);
                } catch (IOException ex) {
                    RemoteLogger.fine(ex);
                }
            }
        }
        
        private RemoteFileObjectBase findExistingParent(String path) {
            while(true) {
                RemoteFileObject fo = getFileSystem().findResource(path);
                if (fo != null) {
                    return fo.getImplementor();
                }
                path = PathUtilities.getDirName(path);
                if (path == null) {
                    return null;
                }
            }
        }

        private ExecutionEnvironment getExecutionEnvironment() {
            return env;
        }

        @Override
        public String toString() {
            return env.getDisplayName();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + (this.env != null ? this.env.hashCode() : 0);
            return hash;
        }

        @Override
        @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FileOperations other = (FileOperations) obj;
            if (this.env != other.env && (this.env == null || !this.env.equals(other.env))) {
                return false;
            }
            return true;
        }

    }

    public static FileProxyO toFileProxy(final String path) {
        return new FileProxyOImpl(path);
    }

    /**
     * Static method to obtain the provider.
     *
     * @return the provider
     */
    public static FileOperationsProvider getDefault() {
        /*
         * no need for sync synchronized access
         */
        if (defaultProvider != null) {
            return defaultProvider;
        }
        defaultProvider = Lookup.getDefault().lookup(FileOperationsProvider.class);
        return defaultProvider;
    }

    private static final class ProcessBuilderImplementationImpl implements ProcessBuilderImplementation {
        private final ExecutionEnvironment env;
        private ProcessBuilderImplementationImpl(ExecutionEnvironment env) {
            this.env = env;
        }

        @Override
        public Process createProcess(String executable, String workingDirectory, List<String> arguments, List<String> paths, Map<String, String> environment, boolean redirectErrorStream) 
                throws ConnectException, IOException {
            if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(env));
            }
            NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(env);
            pb.setExecutable(executable).setWorkingDirectory(workingDirectory).setArguments(arguments.toArray(new String[arguments.size()]));
            MacroMap mm = MacroMap.forExecEnv(env);
            mm.putAll(environment);
            pb.getEnvironment().putAll(mm);
            for(String path : paths) {
                pb.getEnvironment().appendPathVariable("PATH", path); // NOI18N
            }
            if (redirectErrorStream) {
                pb.redirectError();
            }
            return pb.call();
        }

        @Override
        public String toString() {
            return env.getDisplayName();
        }
    }
    
    public interface FileProxyO {

        String getPath();
    }

    private static final class FileProxyOImpl implements FileProxyO {
        private final String path;
        private FileProxyOImpl(String path) {
            this.path = path;
        }
        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String toString() {
            return path;
        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }

        @Override
        @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FileProxyOImpl other = (FileProxyOImpl) obj;
            if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
                return false;
            }
            return true;
        }
    }   
}
