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
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fs.DirEntry;
import org.netbeans.modules.remote.impl.fs.RemoteDirectory;
import org.netbeans.modules.remote.impl.fs.RemoteExceptions;
import org.netbeans.modules.remote.impl.fs.RemoteFileObject;
import org.netbeans.modules.remote.impl.fs.RemoteFileObjectBase;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystem;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemManager;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemTransport;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemUtils;
import org.netbeans.modules.remote.impl.fs.RemoteFileUrlMapper;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.spi.RemoteServerListProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Static methods that are need for RemoteVcsSupportImpl
 */
public class RemoteVcsSupportUtil {

    private RemoteVcsSupportUtil() {        
    }

    /** deprecated: use USE_FS instead */
    private static final boolean USE_CACHE;
    static {
        String text = System.getProperty("rfs.vcs.cache");
        USE_CACHE = (text == null) ? true : Boolean.parseBoolean(text);
    }
    
    public static final boolean USE_FS = RemoteFileSystemUtils.getBoolean("rfs.vcs.use.fs", true);
    
    public static boolean isSymbolicLink(FileSystem fileSystem, String path) {
        if (fileSystem instanceof RemoteFileSystem) {
            RemoteFileSystem rfs = (RemoteFileSystem) fileSystem;
            if (USE_CACHE) {
                Boolean res = rfs.vcsSafeIsSymbolicLink(path);
                if (res != null) {
                    return res.booleanValue();
                }
            }            
            final ExecutionEnvironment env = rfs.getExecutionEnvironment();
            if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                return false;
            }
            try {
                DirEntry entry = RemoteFileSystemTransport.lstat(env, path);
                return entry.isLink();
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
            
        } else {
            return false;
        }
    }
    
    @org.netbeans.api.annotations.common.SuppressWarnings("NP") // Three state
    public static Boolean isDirectoryFast(FileSystem fs, String path) throws IOException {
        if (fs instanceof RemoteFileSystem) {
            return ((RemoteFileSystem) fs).vcsSafeIsDirectory(path);
        }
        return null;
    }

    public static String readSymbolicLinkPath(FileSystem fileSystem, String path) throws IOException {
        if (fileSystem instanceof RemoteFileSystem) {
            FileObject fo = fileSystem.findResource(path);
            if (fo != null) {
                return fo.readSymbolicLinkPath();
            }
        }
        return null;
    }


    /** returns fully resolved canonical path or NULL if this is not a symbolic link */
    public static String getCanonicalPath(FileSystem fileSystem, String path) throws IOException {
        if (fileSystem instanceof RemoteFileSystem) {
            return getCanonicalPathImpl((RemoteFileSystem) fileSystem, path);
        } else {
            return null;
        }
    }
    
    /** returns fully resolved canonical path or NULL if this is not a symbolic link */
    private static String getCanonicalPathImpl(RemoteFileSystem fs, String path) throws IOException {
        Boolean isLink = fs.vcsSafeCanonicalPathDiffers(path);
        if (isLink != null && !isLink.booleanValue()) {
            return null;
        }
        ExecutionEnvironment env = fs.getExecutionEnvironment();
        if (!ConnectionManager.getInstance().isConnectedTo(env)) {
            throw RemoteExceptions.createConnectException(RemoteFileSystemUtils.getConnectExceptionMessage(env));
        }
        try {
            DirEntry entry = RemoteFileSystemTransport.lstat(env, path);
            if (entry.isLink()) {
                String target = entry.getLinkTarget();
                if (!target.startsWith("/")) { //NOI18N
                    target = PathUtilities.normalizeUnixPath(path + "/" + target); // NOI18N
                }
                String nextTarget = getCanonicalPathImpl(fs, target);
                return (nextTarget == null) ? target : nextTarget;
            } else {
                return null;
            }
        } catch (TimeoutException ex) {
            throw new IOException(ex);
        } catch (InterruptedException ex) {
            throw new InterruptedIOException();
        } catch (ExecutionException ex) {
            if (RemoteFileSystemUtils.isFileNotFoundException(ex)) {
                final FileNotFoundException fnfe = new FileNotFoundException();
                fnfe.initCause(ex);
                throw fnfe; // TODO: think over whether this is correct
            }
            throw new IOException(ex);
        }
    }
    
    public static boolean canReadImpl(RemoteFileSystem fileSystem, String path) {        
        try {
            ExecutionEnvironment env = fileSystem.getExecutionEnvironment();
            DirEntry entry = RemoteFileSystemTransport.stat(env, path);
            return entry.canRead();
        } catch (ConnectException ex) {
            RemoteLogger.finest(ex);
        } catch (InterruptedException | IOException | ExecutionException | TimeoutException ex) {
            RemoteLogger.finest(ex);
        }    
        return false; // TODO: is this correct?
    }
    
    public static boolean canRead(FileSystem fileSystem, String path) {
        if (fileSystem instanceof RemoteFileSystem) {
            return canReadImpl((RemoteFileSystem) fileSystem, path);
        } else {
            return false;
        }        
    }
    
    public static long getSizeImpl(RemoteFileSystem fileSystem, String path) {
        try {
            ExecutionEnvironment env = fileSystem.getExecutionEnvironment();
            DirEntry entry = RemoteFileSystemTransport.stat(env, path);
            return entry.getSize();
        } catch (ConnectException ex) {
            RemoteLogger.finest(ex);
            return 0; // TODO: is this correct?
        } catch (InterruptedException | IOException | ExecutionException | TimeoutException ex) {
            RemoteLogger.finest(ex);
            return 0; // TODO: is this correct?
        }   
    }

    public static long getSize(FileSystem fileSystem, String path) {
        if (fileSystem instanceof RemoteFileSystem) {
            return getSizeImpl((RemoteFileSystem) fileSystem, path);
        } else {
            return 0; // TODO: should it be -1?
        }
    }

    public static OutputStream getOutputStream(FileSystem fileSystem, String path) throws IOException {            
        FileObject fo = getOrCreateFileObject(fileSystem, path);
        return fo.getOutputStream();
    }

    private static FileObject getOrCreateFileObject(FileSystem fileSystem, String path) throws IOException {
        FileObject fo = getFileObject(fileSystem, path);
        if (fo == null) {
            fo = FileUtil.createData(fileSystem.getRoot(), path);
        }
        return fo;
    }
    
    private static FileObject getFileObject(FileSystem fileSystem, String path) throws IOException {
        return getFileObject(fileSystem, path, null);
    }

    private static FileObject getFileObject(FileSystem fileSystem, String path, AtomicBoolean refreshed) throws IOException {
        if (fileSystem instanceof RemoteFileSystem) {
            RemoteFileObjectBase cachedFileObject = ((RemoteFileSystem) fileSystem).getFactory().getCachedFileObject(path);
            if (cachedFileObject != null && cachedFileObject.isValid()) {
                return cachedFileObject.getOwnerFileObject();
            }
        }
        FileObject fo = fileSystem.findResource(path);
        if (fo == null)  {
            String parentPath = PathUtilities.getDirName(path);
            FileObject parentFO = (parentPath == null) ? fileSystem.getRoot() : fileSystem.findResource(parentPath);
            while (parentFO == null) {
                parentPath = PathUtilities.getDirName(parentPath);
                parentFO = (parentPath == null) ? fileSystem.getRoot() : fileSystem.findResource(parentPath);
            }
            parentFO.refresh();
            if (refreshed != null) {
                refreshed.set(true);
            }
            fo = fileSystem.findResource(path);
        }
        return fo;
    }

    private static void deleteExternally(ExecutionEnvironment env, String path) {
        final ExitStatus res = ProcessUtils.execute(env, "rm", "-rf", path); // NOI18N
        if (!res.isOK()) {
            RemoteLogger.info("Error deleting {0}:{1} rc={2} {3}", env, path, res.exitCode, res.getErrorString()); //NOI18N
        }
    }

    public static void delete(FileSystem fs, String path) {
        RemoteLogger.assertTrue(fs instanceof RemoteFileSystem, "" + fs + " not an instance of RemoteFileSystem"); //NOI18N
        if (fs instanceof RemoteFileSystem) {
            final RemoteFileSystem rfs = (RemoteFileSystem) fs;
            final ExecutionEnvironment env = rfs.getExecutionEnvironment();
            if (rfs.isInsideVCS()) {
                deleteExternally(env, path);
            } else {
                try {
                    FileObject fo = getFileObject(fs, path);
                    if (fo != null) {
                        fo.delete();
                    } else {
                        RemoteLogger.info("Can not delete inexistent file {0}:{1}", env, path);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
    }

    /**
     * Deletes on disconnect
     * @param path file to delete
     */
    public static void deleteOnExit(FileSystem fs, String path) {
        RemoteLogger.assertTrue(fs instanceof RemoteFileSystem, "" + fs + " not an instance of RemoteFileSystem"); //NOI18N
        if (fs instanceof RemoteFileSystem) {
            final RemoteFileSystem rfs = (RemoteFileSystem) fs;
            rfs.deleteOnDisconnect(path);
        }
    }

    public static void deleteExternally(FileSystem fs, String path) {
        RemoteLogger.assertTrue(fs instanceof RemoteFileSystem, "" + fs + " not an instance of RemoteFileSystem"); //NOI18N
        if (fs instanceof RemoteFileSystem) {
            deleteExternally(((RemoteFileSystem) fs).getExecutionEnvironment(), path);
            String parentPath = PathUtilities.getDirName(path);
            try {
                refreshFor(fs, (parentPath == null) ? "/" : parentPath); //NOI18N
            } catch (IOException ex) {
                RemoteLogger.fine(ex);
            }
        }
    }

    public static void setLastModified(FileSystem fs, String path, String referenceFile) {
        RemoteLogger.assertTrue(fs instanceof RemoteFileSystem, "" + fs + " not an instance of RemoteFileSystem"); //NOI18N
        RemoteLogger.assertTrue(path.startsWith("/")); //NOI18N
        RemoteLogger.assertTrue(referenceFile.startsWith("/")); //NOI18N
        if (fs instanceof RemoteFileSystem) {
            final RemoteFileSystem rfs = (RemoteFileSystem) fs;
            final ExecutionEnvironment env = rfs.getExecutionEnvironment();
            final ExitStatus res = ProcessUtils.execute(env, "touch", "-r", path, referenceFile); // NOI18N
            if (res.isOK()) {
                try {
                    String base1 = PathUtilities.getDirName(path);
                    String base2 = PathUtilities.getDirName(referenceFile);
                    FileObject baseFO1 = (base1 == null) ? fs.getRoot() : getFileObject(fs, base1);
                    FileObject baseFO2 = (base2 == null) ? fs.getRoot() : getFileObject(fs, base2);
                    if (baseFO1 instanceof RemoteFileObject) {
                        ((RemoteFileObject) baseFO1).nonRecursiveRefresh();
                    }
                    if (baseFO2 instanceof RemoteFileObject && ! baseFO2.equals(baseFO1)) {
                        ((RemoteFileObject) baseFO2).nonRecursiveRefresh();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            } else {
                RemoteLogger.info("Error setting timestamp for {0}:{1} from {2} rc={3} {4}", //NOI18N
                        env, path, env, referenceFile, res.exitCode, res.getErrorString());
            }
        }
    }

    public static FileSystem[] getConnectedFileSystems() {
        List<FileSystem> connected = new ArrayList<>();
        RemoteServerListProvider provider = Lookup.getDefault().lookup(RemoteServerListProvider.class);
        if (provider == null) {
            for (RemoteFileSystem fs : RemoteFileSystemManager.getInstance().getAllFileSystems()) {
                if (ConnectionManager.getInstance().isConnectedTo(fs.getExecutionEnvironment())) {
                    connected.add(fs);
                }
            }
        } else {
            for (ExecutionEnvironment env : provider.getRemoteServers()) {
                if (ConnectionManager.getInstance().isConnectedTo(env)) {
                    // just ensure that file systems are instantiated 
                    // for all remote servers that have been set up
                    RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
                    connected.add(fs);
                }
            }
        }
        return connected.toArray(new FileSystem[connected.size()]);
    }
    
    public static FileSystem getDefaultFileSystem() {
        RemoteServerListProvider provider = Lookup.getDefault().lookup(RemoteServerListProvider.class);
        if (provider == null) {
            return null;
        }
        ExecutionEnvironment def = provider.getDefailtServer();
        if (def == null || !ConnectionManager.getInstance().isConnectedTo(def)) {
            return null;
        }
        if (def.isLocal()) {
            return null;
        }
        return FileSystemProvider.getFileSystem(def);
    }

    public static void refreshFor(FileSystem fs, String... paths) throws ConnectException, IOException {
        RemoteLogger.assertTrue(fs instanceof RemoteFileSystem, "" + fs + " not an instance of RemoteFileSystem"); //NOI18N
        for (String p : paths) {
            RemoteLogger.assertTrue(p != null, "Path should not be null"); //NOI18N
            RemoteLogger.assertTrue(p.isEmpty() || p.startsWith("/"), "Path should be absolute: {0}", paths); //NOI18N
        }
        RemoteFileSystem rfs = (RemoteFileSystem) fs;
        AtomicBoolean refreshed = new AtomicBoolean(false);
        Set<RemoteDirectory> refreshSet = new HashSet<>();
        for (String p : paths) {
            if (p.isEmpty()) {
                p = "/"; //NOI18N
            }
            FileObject fo = getFileObject(rfs, p, refreshed);            
            if (fo != null && !refreshed.get()) {
                RemoteFileObjectBase impl = ((RemoteFileObject) fo).getImplementor();
                if (impl.isFolder()) {
                    // for folder, add itself (canonicalized)
                    impl = RemoteFileSystemUtils.getCanonicalFileObject(impl);
                    if (impl instanceof RemoteDirectory) {
                        refreshSet.add((RemoteDirectory) impl);
                    } else {
                        RemoteLogger.info("Unexpected file object instance, expected RemoteDirectory: {0}", impl); //NOI18N
                        impl.refresh();
                    }
                } else {
                    // for not folder, add canonical paenr
                    refreshSet.add(RemoteFileSystemUtils.getCanonicalParent(impl));
                }
            }
        }
        if (RemoteFileSystemTransport.canRefreshFast(rfs.getExecutionEnvironment())) {
            for (RemoteDirectory impl : refreshSet) {
                try {
                    RemoteFileSystemTransport.refreshFast(impl, false);
                } catch (InterruptedException | TimeoutException ex) {
                    InterruptedIOException ie = new InterruptedIOException(ex.getMessage());
                    ie.initCause(ex);
                    throw ie;
                } catch (ExecutionException ex) {
                    throw new IOException(ex.getMessage(), ex);
                }
            }
        } else {
            for (RemoteDirectory fo : refreshSet) {
                fo.refresh();
            }
        }
    }
    
    private static boolean isForbiddenFolderImpl(RemoteFileSystem rfs, String path) {
        if (path.isEmpty()) {
            return true;
        } else if (rfs.isAutoMount(path)) {
            return true;
        } else if(rfs.isProhibitedToEnter(path)) {
            return true;
        }
        return false;
    }

    public static boolean isForbiddenFolder(FileSystem fs, String path) {
        if (fs instanceof RemoteFileSystem) {
            if (path.isEmpty() || path.equals("/tmp") && RemoteFileSystemUtils.isUnitTestMode()) { // NOI18N
                return false;
            }            
            RemoteFileSystem rfs = (RemoteFileSystem) fs;
            if (isForbiddenFolderImpl(rfs, path)) {
                return true;
            }
            int pos = path.lastIndexOf('/');
            if (pos >= 0) {
                String parent = path.substring(0, pos);
                // if we decide to remove this check, then at least return "true" for /tmp
                // (except for unit tests!)
                if (isForbiddenFolderImpl(rfs, parent)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }
    
    public static URI toURI(FileSystem fs, String path) {
        if (fs instanceof RemoteFileSystem) {
            RemoteFileSystem rfs = (RemoteFileSystem) fs;
            ExecutionEnvironment env = rfs.getExecutionEnvironment();
            try {
                Boolean folder = isDirectoryFast(fs, path);
                return RemoteFileUrlMapper.toURI(env, path, folder == null ? false : folder);
            } catch (URISyntaxException | IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
        return null;
    }

    public static URL toURL(FileSystem fs, String path) {
        if (fs instanceof RemoteFileSystem) {
            RemoteFileSystem rfs = (RemoteFileSystem) fs;
            ExecutionEnvironment env = rfs.getExecutionEnvironment();
            try {
                Boolean folder = isDirectoryFast(fs, path);
                return RemoteFileUrlMapper.toURL(env, path, folder == null ? false : folder);
            } catch (IOException /*|MalformedURLException*/ ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
        return null;        
    }     
}
