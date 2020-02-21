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

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.actions.FastPasteAction;
import org.netbeans.modules.remote.api.ConnectionNotifier;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fileoperations.spi.AnnotationProvider;
import org.netbeans.modules.remote.impl.fileoperations.spi.FileOperationsProvider;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider.FilesystemInterceptor;
import org.netbeans.modules.remote.spi.FileSystemCacheProvider;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.spi.FileSystemProvider.FileSystemProblemListener;
import org.netbeans.modules.remote.spi.RemoteFileSystemHintsProvider;
import org.openide.actions.FileSystemRefreshAction;
import org.openide.filesystems.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.ImageDecorator;
import org.openide.filesystems.StatusDecorator;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.io.NbObjectInputStream;
import org.openide.windows.WindowManager;

/**
 * Remote file system:
 * gets files on demand from a remote host.
 * It is read-only
 * 
 */
@org.netbeans.api.annotations.common.SuppressWarnings("Se") // is it ever serialized?
public final class RemoteFileSystem extends FileSystem implements ConnectionListener {

    /*package*/ static final boolean COLLECT_STATSISTICS = Boolean.getBoolean("remote.fs.statsistics");
    private static final boolean ATTR_STATS = Boolean.getBoolean("remote.attr.stats");

    public static final String ATTRIBUTES_FILE_NAME = ".rfs_attr"; // NOI18N
    public static final String CACHE_FILE_NAME = ".rfs_cache"; // NOI18N
    // TODO: consider moving cache names to RemoteDirectory
    protected static final String CACHE_ZIP_FILE_NAME = ".rfs_zip.zip"; // NOI18N
    protected static final String CACHE_ZIP_PART_NAME = ".rfs_zip.part"; // NOI18N
    protected static final String TEMP_ZIP_PREFIX = ".rfs_tmp_"; // NOI18N
    public static final String RESERVED_PREFIX = ".rfs_"; // NOI18N
    public static final String RESERVED_PREFIX_ESCAPED = "._rfs_"; // NOI18N
    
    private static final String READONLY_ATTRIBUTES = "readOnlyAttrs"; //NOI18N
    private final ExecutionEnvironment execEnv;
    private final String filePrefix;
    private final RemoteFileObject root;
    private final RemoteDirectory rootDelegate;
    private final RemoteFileSupport remoteFileSupport;
    private final RefreshManager refreshManager;
    private final File cache;
    private final RemoteFileObjectFactory factory;
    /** File transfer statistics */
    private final AtomicInteger fileCopyCount = new AtomicInteger(0);
    /** Directory synchronization statistics */
    private final AtomicInteger dirSyncCount = new AtomicInteger(0);
    private final RemoteLockSupport lockSupport = new RemoteLockSupport();
    private final AtomicBoolean readOnlyConnectNotification = new AtomicBoolean(false);
    private static final List<FileSystemProblemListener> globalProblemListeners = new CopyOnWriteArrayList<>();
    private final List<FileSystemProblemListener> problemListeners =
            new ArrayList<>(globalProblemListeners);
    transient private final StatusImpl status = new StatusImpl();
    private final DeleteOnExitSupport deleteOnExitSupport;
    private final ThreadLocal<RemoteFileObjectBase> beingRemoved = new ThreadLocal<>();
    private final ThreadLocal<FileInfo> beingCreated = new ThreadLocal<>();
    private final ThreadLocal<RemoteFileObjectBase> externallyRemoved = new ThreadLocal<>();
    private final RemoteFileZipper remoteFileZipper;
    private final ThreadLocal<Integer> isInsideVCS = new ThreadLocal<>();
    private final ThreadLocal<Boolean> isGettingDirectoryStorage = new ThreadLocal<>();

    private final Map<RemoteDirectory, SuspendInfo> suspendInfo = new HashMap<>();

    private final RequestProcessor.Task connectionTask;

    /** 
     * ConnectionTaskLock is now scheduled not only upon connection change, but from ctor as well
     * (in order to get auto mounts). 
     * The connectionChanged distinguishes these two cases
     */
    private volatile boolean connectionChanged;

    /**
     * @guarded by self
     * Also guards autoMountsAnalyzed
     */
    private final List<String> autoMounts;

    /** 
     * True if auto mounts are analyzed, otherwise false.
     * Access must be synchronized by autoMounts, if you need to wait until it becomes true,
     * wait on autoMounts either
     * @guarded by autoMounts 
     */
    private boolean autoMountsAnalyzed = false;
    private volatile boolean disposed = false;

    /*package*/ RemoteFileSystem(ExecutionEnvironment execEnv) throws IOException {
        RemoteLogger.assertTrue(execEnv.isRemote());
        this.execEnv = execEnv;
        this.remoteFileSupport = new RemoteFileSupport();
        factory = new RemoteFileObjectFactory(this);
        refreshManager = new RefreshManager(execEnv, factory);
        // FIXUP: it's better than asking a compiler instance... but still a fixup.
        // Should be moved to a proper place
        this.filePrefix = FileSystemCacheProvider.getCacheRoot(execEnv);
        if (filePrefix == null) {
            throw new IllegalStateException("Can not find cache root for remote file system at " + execEnv); //NOI18N
        }
        cache = new File(filePrefix);
        if (!cache.exists() && !cache.mkdirs()) {
            throw new IOException(NbBundle.getMessage(getClass(), "ERR_CreateDir", cache.getAbsolutePath())); // new IOException sic! (ctor)
        }
        deleteOnExitSupport = new DeleteOnExitSupport(execEnv, cache);
        this.rootDelegate = new RootFileObject(this.root = new RemoteFileObject(this), this, execEnv, cache); // NOI18N
        factory.register(rootDelegate);

        final WindowFocusListener windowFocusListener = new WindowFocusListener() {

            @Override
            public void windowGainedFocus(WindowEvent e) {
                if (e.getOppositeWindow() == null) {
                    if (ConnectionManager.getInstance().isConnectedTo(RemoteFileSystem.this.execEnv)) {
                        refreshManager.scheduleRefreshOnFocusGained();
                    }
                }
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
            }
        };
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (!GraphicsEnvironment.isHeadless()) {
                    //WindowManager.getDefault().getMainWindow().addWindowFocusListener(focusListener);
                    WindowManager.getDefault().getMainWindow().addWindowFocusListener(windowFocusListener);
                }
            }
        });
        autoMounts = AutoMountsProvider.restoreAutoMounts(); // before adding connection listeners and schdulling connectionTask!
        if (RemoteLogger.isLoggable(Level.FINE)) {
            RemoteLogger.fine("Restored automount list for {0}:", execEnv);//NOI18N
            for (String path : autoMounts) {
                RemoteLogger.fine("\t{0}", path);
            }
        }
        connectionTask = new RequestProcessor("Connection and R/W change", 1).create(new ConnectionChangeRunnable()); //NOI18N;
        connectionChanged = false; // volatile
        connectionTask.schedule(0);
        ConnectionManager.getInstance().addConnectionListener(RemoteFileSystem.this);
        remoteFileZipper = new RemoteFileZipper(execEnv, this);
    }

    public boolean isAutoMount(String path) {
        synchronized (autoMounts) {
            return autoMounts.contains(path);
        }
    }

    public List<String> getDirsProhibitedToStat(String path) {
        waitAutoMountsAnalyzed();
        synchronized (autoMounts) {
            return Collections.unmodifiableList(autoMounts);
        }
    }

    public boolean isProhibitedToEnter(String path) {
        if (path.equals("/proc") || path.equals("/dev")) { //NOI18N
            return true;
        }
        if (path.equals("/run")) { //NOI18N
        if (HostInfoUtils.isHostInfoAvailable(getExecutionEnvironment())) {
                try {
                    HostInfo hi = HostInfoUtils.getHostInfo(getExecutionEnvironment());
                    if (hi.getOSFamily() == HostInfo.OSFamily.LINUX) {
                        return true;
                    }
                } catch (IOException | ConnectionManager.CancellationException ex) {
                    Exceptions.printStackTrace(ex); // should never be the case if isHostInfoAvailable retured true
                }
            }
        }
        return false;
    }

    public boolean isDirectAutoMountChild(String path) {
        String parent = PathUtilities.getDirName(path);
        if (parent != null && ! parent.isEmpty()) {
            return isAutoMount(parent);
        }
        return false;
    }

    public static boolean isSniffing(String childName) {
        if (childName != null) {
            for (RemoteFileSystemHintsProvider hp : Lookup.getDefault().lookupAll(RemoteFileSystemHintsProvider.class)) {
                if (hp.isSniffing(childName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInsideVCS() {
        Integer currValue = isInsideVCS.get();        
        int level = ((currValue == null) ? 0 : currValue.intValue());
        return level > 0;
    }

    public void setInsideVCS(boolean value) {
        Integer currValue = isInsideVCS.get();
        int newValue = ((currValue == null) ? 0 : currValue.intValue()) + (value ? +1 : -1);
        isInsideVCS.set(newValue);
    }
    
    public boolean isGettingDirectoryStorage() {
        Boolean inside = isGettingDirectoryStorage.get();
        return inside != null && inside.booleanValue();
    }

    public void setGettingDirectoryStorage(boolean inside) {
        isGettingDirectoryStorage.set(inside);
    }

    void warmup(Collection<String> paths, FileSystemProvider.WarmupMode mode, Collection<String> extensions) {
        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            RemoteLogger.fine("Warmup: no connection to host {0}", execEnv); //NOI18N
            return;
        }
        for (String path : paths) {
            // we still do this via RemoteFileObject (and eventually via RemoteDirectory)
            // since we need its own cahche files to be created first
            RemoteFileObject fo = findResource(path);
            if (fo == null) {
                if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                    RemoteLogger.info("Warmup: no connection to host while warmiong up {0} at {1}", path, execEnv); //NOI18N
                    break;
                } else {
                    RemoteLogger.info("Warmup: can't find file object {0} at {1}", path, execEnv); //NOI18N
                }
            } else {
                fo.getImplementor().warmup(mode, extensions);
            }                        
        }
    }

    /*package*/ RemoteFileZipper getZipper() {
        return remoteFileZipper;
    }
    
    /*package*/ void dispose() {
        disposed = true; 
        //RemoteFileSystemTransport.shutdown(execEnv);
        ConnectionManager.getInstance().removeConnectionListener(this);
    }

    private class ConnectionChangeRunnable implements Runnable {

        public ConnectionChangeRunnable() {
        }

        @Override
        public void run() {
            if (ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                maintainAutoMounts();
            }
            if (connectionChanged && !disposed) {
                if (ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                    refreshManager.scheduleRefreshOnConnect();
                }
                for (RemoteFileObjectBase fo : factory.getCachedFileObjects()) {
                    fo.connectionChanged();
                }
            }
            if (!disposed) {
                deleteOnExitSupport.processConnectionChange();
            }
        }

        private void maintainAutoMounts() {
            synchronized (autoMounts) {
                if (autoMountsAnalyzed) {
                    return;
                }
            }
            long time = System.currentTimeMillis();
            try {
                RemoteLogger.fine("Getting automount list for {0}...", execEnv); //NOI18N
                AutoMountsProvider amp = new AutoMountsProvider(execEnv);
                List<String> newAutoMounts = amp.analyze();
                if (newAutoMounts != null) {
                    synchronized (autoMounts) {
                        autoMounts.clear();
                        autoMounts.addAll(newAutoMounts);
                    }
                }
            } finally {
                synchronized (autoMounts) {
                    autoMountsAnalyzed = true;
                    autoMounts.notifyAll();
                }
            }
            if (RemoteLogger.isLoggable(Level.FINE)) {
                synchronized (autoMounts) {
                    RemoteLogger.fine("Getting automount list for {0} took {1} ms", //NOI18N
                            execEnv, System.currentTimeMillis() - time);
                    for (String path : autoMounts) {
                        RemoteLogger.fine("\t{0}", path);
                    }
                }
            }
        }
    }

    private void waitAutoMountsAnalyzed() {
        while (true) {
            synchronized (autoMounts) {
                if (autoMountsAnalyzed) {
                    return;
                }
                try {
                    autoMounts.wait(1000);
                } catch (InterruptedException ex) {
                    RemoteLogger.finest(ex);
                    return;
                }
            }
        }
    }

    @Override
    public void connected(ExecutionEnvironment env) {
        if (execEnv.equals(env)) {
            readOnlyConnectNotification.compareAndSet(true, false);
            connectionChanged = true; // volatile
            deleteOnExitSupport.notifyConnected();
            connectionTask.schedule(0);
        }
    }

    @Override
    public void disconnected(ExecutionEnvironment env) {
        if (execEnv.equals(env)) {
            readOnlyConnectNotification.compareAndSet(true, false);
            connectionChanged = true; // volatile
            connectionTask.schedule(0);
        }
        if (ATTR_STATS) { dumpAttrStat(); }
        if (COLLECT_STATSISTICS) {
            lockSupport.printStatistics(this);
        }
        deleteOnExitSupport.notifyDisconnected();
    }
    
    public ExecutionEnvironment getExecutionEnvironment() {
        return execEnv;
    }

    public RemoteFileObjectFactory getFactory() {
        return factory;
    }
    
    public int getCachedFileObjectsCount() {
        return factory.getCachedFileObjectsCount();
    }

    public RefreshManager getRefreshManager() {
        return refreshManager;
    }
    
    public String normalizeAbsolutePath(String absPath) {
        //BZ#192265 as stated the URI i sused to normilize the path
        //but URI is really very restrictive so let's use another way
        //will use the face that path is absolute and we have Unix like system
        //no special code for Windows
        return PathUtilities.normalizeUnixPath(absPath);
    }

    /*package-local, for testing*/
    File getCache() {
        return cache;
    }

    public RemoteLockSupport getLockSupport() {
        return lockSupport;
    }

    /*package-local test method*/ final void resetStatistic() {
        dirSyncCount.set(0);
        fileCopyCount.set(0);
    }

    /*package-local test method*/ final int getDirSyncCount() {
        return dirSyncCount.get();
    }

    /*package-local test method*/ final int getFileCopyCount() {
        return fileCopyCount.get();
    }

    /*package-local test method*/ final void incrementDirSyncCount() {
        dirSyncCount.incrementAndGet();
    }

    /*package-local test method*/ final void incrementFileCopyCount() {
        fileCopyCount.incrementAndGet();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "RFS_DISPLAY_NAME", execEnv.getDisplayName());
    }

    @Override
    public boolean isReadOnly() {
        return !ConnectionManager.getInstance().isConnectedTo(execEnv);
    }

    @Override
    public RemoteFileObject getRoot() {
        return root;
    }

    @Override
    public RemoteFileObject findResource(String name) {
        return findResource(name, new HashSet<String>());
    }

    public RemoteFileObject findResource(String name, Set<String> antiLoop) {
        if (name.isEmpty() || name.equals("/")) {  // NOI18N
            return getRoot();
        } else {
            return getRoot().getFileObject(name, antiLoop);
        }
    }

    @Override
    public FileObject getTempFolder() throws IOException {
        try {
            String tmpName = HostInfoUtils.getHostInfo(execEnv).getTempDir();
            RemoteFileObject tmpDir = findResource(tmpName);
            if (tmpDir == null) {
                String dirName = PathUtilities.getDirName(tmpName);
                FileObject parentFO = findResource(dirName);
                if (parentFO != null) {
                    parentFO.refresh();
                    tmpDir = findResource(tmpName);
                }                
            }
            if (tmpDir != null && tmpDir.isFolder() && tmpDir.isValid()) {
                return tmpDir;
            } else {
                throw RemoteExceptions.createIOException(
                        NbBundle.getMessage(RemoteFileSystem.class, "EXC_CantFindTemp")); //NOI18N
            }
        } catch (CancellationException ex) {
            throw RemoteExceptions.createIOException(
                    NbBundle.getMessage(RemoteFileSystem.class, "EXC_CantFindTemp", ex)); //NOI18N
        }
    }
    
    @Override
    public FileObject createTempFile(FileObject parent, String prefix, String suffix, boolean deleteOnExit) throws IOException {
        if (parent.isFolder() && parent.isValid()) {
            while(true) {
                File tmpFile = File.createTempFile(prefix, suffix);
                String tmpName = tmpFile.getName();
                tmpFile.delete();
                try {
                    FileObject fo = parent.createData(tmpName);
                    if (fo != null && fo.isData() && fo.isValid()) {
                        if (deleteOnExit) {
                            deleteOnDisconnect(fo.getPath());
                        }
                        return fo;
                    }
                    break;   
                } catch (IOException ex) {
                    FileObject test = parent.getFileObject(tmpName);
                    if (test != null) {
                        continue;
                    }
                    throw ex;
                }
            }
        }
        throw RemoteExceptions.createIOException(
                NbBundle.getMessage(RemoteFileSystem.class, "EXC_CantCantCreateTemp")); // NOI18N
    }
    
    public RemoteFileObjectBase findResourceImpl(String name, @NonNull Set<String> antiloop) {
        if (name.isEmpty() || name.equals("/")) {  // NOI18N
            return getRoot().getImplementor();
        } else {
            RemoteFileObject fo = rootDelegate.getFileObject(name, antiloop);
            return (fo == null) ? null : fo.getImplementor();
        }
    }
    

    public void addPendingFile(RemoteFileObjectBase fo) {
        remoteFileSupport.addPendingFile(fo);
        fireProblemListeners(fo.getPath());
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    /*package*/ void setAttribute(RemoteFileObjectBase file, String attrName, Object value) {
        RemoteFileObjectBase parent = file.getParent();
        boolean hasParent = true;
        if (parent == null) {
            // root
            parent = file;
            hasParent = false;
        }
        File attr = getAttrFile(parent);
        if (attr== null) {
            return;
        }
        Properties table = readProperties(attr);
        String translatedAttributeName = translateAttributeName(file, attrName);
        String encodedValue = encodeValue(value);
        Object oldValue = null;
        if (encodedValue == null) {
            table.remove(translatedAttributeName);
        } else {                
            oldValue = table.setProperty(translatedAttributeName, encodedValue);
        }
        FileOutputStream fileOtputStream = null;
        try {
            fileOtputStream = new FileOutputStream(attr);
            table.store(fileOtputStream, "Set attribute "+attrName); // NOI18N
        } catch (IOException ex) {
            // See issues #254952, #249548: FileNotFoundException: .rfs_attr
            // It can happen that parent  was removed; 
            // if that's the case, then don't report this via Exceptions.printStackTrace
            // If it's not, we'd better report since I think this should never happen
            boolean report = true;
            StringBuilder sb = new StringBuilder();
            sb.append("Can not set attribute for ").append(file) // NOI18N
                    .append("; attr. cache is ").append(attr.getAbsolutePath()); // NOI18N
            if (ex instanceof FileNotFoundException) {
                File attrParentFile = attr.getParentFile();
                if (attrParentFile != null) {
                    boolean parentExists = attrParentFile.exists();
                    sb.append("; attr. cache parent exists ? ").append(parentExists); // NOI18N
                    if (!parentExists) {
                        report = false;
                    }
                }
            }
            IOException ioEx = new IOException(sb.toString(), ex);
            if (report) {
                Exceptions.printStackTrace(ioEx);
            } else {
                ioEx.printStackTrace(System.err);
            }
        } finally {
            if (fileOtputStream != null) {
                try {
                    fileOtputStream.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        if (hasParent) {
            file.fireFileAttributeChangedEvent(file.getListeners(), new FileAttributeEvent(file.getOwnerFileObject(), file.getOwnerFileObject(), attrName, oldValue, value));
            parent.fireFileAttributeChangedEvent(parent.getListeners(), new FileAttributeEvent(parent.getOwnerFileObject(), file.getOwnerFileObject(), attrName, oldValue, value));
        } else {
            file.fireFileAttributeChangedEvent(file.getListeners(), new FileAttributeEvent(file.getOwnerFileObject(), file.getOwnerFileObject(), attrName, oldValue, value));
        }
        logAttrName(attrName, true);
    }

    private static class AttrStat {
        public final String name;
        public int readCount = 0;
        public int writeCount = 0;
        public StackTraceElement[] firstReadStack;
        public StackTraceElement[] firstWriteStack;
        public AttrStat(String name) {
            this.name = name;
        }
    }

    private static final Map<String, AttrStat> attrStats = new TreeMap<> ();

    private static void logAttrName(String name, boolean write) {
        synchronized(attrStats) {
            AttrStat stat  = attrStats.get(name);
            if (stat == null) {
                stat = new AttrStat(name);
                attrStats.put(name, stat);
            }
            if (write) {
                if (stat.writeCount++ == 0) {
                    stat.firstWriteStack = Thread.currentThread().getStackTrace();
                }
            } else {
                if (stat.readCount++ == 0) {
                    stat.firstReadStack = Thread.currentThread().getStackTrace();
                }
            }
        }
        System.out.printf("%sAttribute %s\n", write ? "set" : "get", name); // NOI18N
    }

    /*package*/ void dumpAttrStat() {
        Map<String, AttrStat> toDump;
        synchronized(attrStats) {
            toDump= new TreeMap<>(attrStats);
        }
        System.out.printf("\n\nDumping attributes statistics (%d elements)\n\n", toDump.size()); // NOI18N
        for (Map.Entry<String, AttrStat> entry : toDump.entrySet()) {
            //String name = entry.getKey();
            AttrStat stat = entry.getValue();
            System.out.printf("%s %d %d\n", stat.name, stat.readCount, stat.writeCount); // NOI18N
            if (stat.firstReadStack != null) {
                System.out.printf("\t%s first read stack:\n", stat.name); // NOI18N
                for (StackTraceElement e : stat.firstReadStack) {
                    System.out.printf("\t\t%s\n", e); // NOI18N
                }
            }
            if (stat.firstWriteStack != null) {
                System.out.printf("\t%s first write stack:\n", stat.name); // NOI18N
                for (StackTraceElement e : stat.firstWriteStack) {
                    System.out.printf("\t\t%s\n", e); // NOI18N
                }
            }
        }
    }

    private File getAttrFile(RemoteFileObjectBase parent) {
        File parentCache = parent.getCache();
        if (parentCache == null) {
            RemoteLogger.info(new IllegalArgumentException("Parent cache file is null " + parent)); //NOI18N
            return null;
        }
        File attr = new File(parentCache, ATTRIBUTES_FILE_NAME);
        return attr;
    }

    /*package*/ Object getAttribute(RemoteFileObjectBase file, String attrName) {
        RemoteFileObjectBase parent = file.getParent();
        if (parent == null) {
            // root
            parent = file;
        }
        if (attrName.equals(FileObject.DEFAULT_LINE_SEPARATOR_ATTR)) {
            return "\n"; // NOI18N
        } else if (attrName.equals(FileObject.DEFAULT_PATHNAME_SEPARATOR_ATTR)) {
            return "/"; // NOI18N
        } else if (attrName.equals(READONLY_ATTRIBUTES)) {
            return Boolean.FALSE;
        } else if (attrName.equals("isRemoteAndSlow")) { // NOI18N
            return Boolean.TRUE;
        } else if (attrName.equals("FileSystem.rootPath")) { //NOI18N
            return this.getRoot().getPath();
        } else if (attrName.equals("java.io.File")) { //NOI18N
            return null;
        } else if (attrName.equals("ExistsParentNoPublicAPI")) { //NOI18N
            return true;
        } else if (attrName.startsWith("ProvidedExtensions")) { //NOI18N
            // #158600 - delegate to ProvidedExtensions if attrName starts with ProvidedExtensions prefix
            if (RemoteFileObjectBase.USE_VCS) {
                FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(this);
                if (interceptor != null) {
                    try {
                        setInsideVCS(true);
                        return interceptor.getAttribute(FilesystemInterceptorProvider.toFileProxy(file.getOwnerFileObject()), attrName);
                    } finally {
                        setInsideVCS(false);
                    }
                }
            }
        }
        if (ATTR_STATS) { logAttrName(attrName, false); }
        File attr = getAttrFile(parent);
        if (attr== null) {
            return null;
        }        
        Properties table = readProperties(attr);
        return decodeValue(table.getProperty(translateAttributeName(file, attrName)));
    }

    /*package*/ Enumeration<String> getAttributes(RemoteFileObjectBase file) {
        RemoteFileObjectBase parent = file.getParent();
        if (parent != null) {
            File attr = getAttrFile(parent);
            if (attr == null) {
                return Collections.emptyEnumeration();
            }
            Properties table = readProperties(attr);
            List<String> res = new ArrayList<>();
            Enumeration<Object> keys = table.keys();
            String prefix = file.getNameExt()+"["; // NOI18N
            while(keys.hasMoreElements()) {
                String aKey = keys.nextElement().toString();
                if (aKey.startsWith(prefix)) {
                    aKey = aKey.substring(prefix.length(),aKey.length()-1);
                    res.add(aKey);
                }
            }
            return Collections.enumeration(res);
        }
        return Collections.enumeration(Collections.<String>emptyList());
    }

    private Properties readProperties(File attr) {
        Properties table = new Properties();
        if (attr.exists()) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(attr);
                table.load(fileInputStream);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return table;
    }

    private String translateAttributeName(RemoteFileObjectBase file, String attrName) {
        return file.getNameExt()+"["+attrName+"]"; // NOI18N
    }
    
    /**
     * Creates serialized object, which was encoded in HEX format
     * @param value Encoded serialized object in HEX format
     * @return Created object from encoded HEX format
     * @throws IOException
     */
    private Object decodeValue(String value) {
        if ((value == null) || (value.length() == 0)) {
            return null;
        }

        byte[] bytes = new byte[value.length() / 2];
        int tempI;
        int count = 0;

        for (int i = 0; i < value.length(); i += 2) {
            try {
                tempI = Integer.parseInt(value.substring(i, i + 2), 16);

                if (tempI > 127) {
                    tempI -= 256;
                }

                bytes[count++] = (byte) tempI;
            } catch (NumberFormatException e) {
            }
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes, 0, count);

        try {
            ObjectInputStream ois = new NbObjectInputStream(bis);
            Object ret = ois.readObject();

            return ret;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Encodes Object into String encoded in HEX format
     * @param value Object, which will be encoded
     * @return  serialized Object in String encoded in HEX format
     * @throws IOException
     */
    private String encodeValue(Object value) {
        if (value == null) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.close();
        } catch (Exception e) {
        }

        byte[] bArray = bos.toByteArray();
        StringBuilder strBuff = new StringBuilder(bArray.length * 2);

        for (int i = 0; i < bArray.length; i++) {
            if ((bArray[i] < 16) && (bArray[i] >= 0)) {
                strBuff.append("0"); // NOI18N
            }

            strBuff.append(Integer.toHexString((bArray[i] < 0) ? (bArray[i] + 256) : bArray[i]));
        }

        return strBuff.toString();
    }

    void addReadOnlyConnectNotification(RemoteFileObjectBase fo) {
        if (readOnlyConnectNotification.compareAndSet(false, true)) {
            remoteFileSupport.addPendingFile(fo);
        }
    }

    public static void addGlobalFileSystemProblemListener(FileSystemProblemListener listener) {
        globalProblemListeners.add(listener);
        for (RemoteFileSystem fs : RemoteFileSystemManager.getInstance().getAllFileSystems()) {
            fs.addFileSystemProblemListener(listener);
        }
    }
    
    public void addFileSystemProblemListener(FileSystemProblemListener listener) {
        synchronized (problemListeners) {
            problemListeners.add(listener);
        }
    }

    public void removeFileSystemProblemListener(FileSystemProblemListener listener) {
        synchronized (problemListeners) {
            problemListeners.remove(listener);
        }
    }

    /**
     * @param path if null, this means recover
     */
    private void fireProblemListeners(String path) {
        List<FileSystemProblemListener> listenersCopy;
        synchronized (problemListeners) {
            listenersCopy = new ArrayList<>(problemListeners);
        }
        for (FileSystemProblemListener l : listenersCopy) {
            if (path == null) {
                l.recovered(this);
            } else {
                l.problemOccurred(this, path);
            }
        }
    }

//    @Override -- overrides at runtime
    public final SystemAction[] getActions(final Set<FileObject> foSet) {
        SystemAction[] result = status.getActions (foSet);
        SystemAction refreshAction = isManualRefresh() ? null :  FileSystemRefreshAction.get(FileSystemRefreshAction.class);                 
        if (result == null) {
            result = (refreshAction == null) ? new SystemAction[] {} : new SystemAction[] { refreshAction };
        } else {
            result = (refreshAction == null) ? result : append(result, refreshAction);
        }
        return append(result, FastPasteAction.get(FastPasteAction.class));
    }

    private static boolean isManualRefresh() {
        return NbPreferences.root().node("org/openide/actions/FileSystemRefreshAction").getBoolean("manual", false); // NOI18N
    }

    private static SystemAction[] append(SystemAction[] actions, SystemAction actionToAppend) {
        SystemAction[] result = new SystemAction[actions.length + 1];
        System.arraycopy(actions, 0, result, 0, actions.length);
        result[actions.length] = actionToAppend;
        return result;
    }

    @Override
    public StatusDecorator getDecorator() {
        return status;
    }
    
    public void deleteOnDisconnect(String... paths) {
        if (RemoteFileSystemTransport.canDeleteOnDisconnect(execEnv)) {
            try {
                RemoteFileSystemTransport.deleteOnDisconnect(execEnv, paths);
                return;
            }
            catch (IOException | InterruptedException | ExecutionException ex) {
                ex.printStackTrace(System.err);
            }
        }
        deleteOnExitSupport.deleteOnExit(paths);
    }

    /*package*/ void setBeingRemoved(RemoteFileObjectBase fo) {
        beingRemoved.set(fo);
    }

    /*package*/ void setBeingCreated(FileInfo fo) {
        beingCreated.set(fo);
    }
    
    /** Be very CAUCIOUS when using this FO - it can be in process of VCS operations  */
    public FileInfo getBeingCreated() {
        return beingCreated.get();
    }

    /*package*/ void setExternallyRemoved(RemoteFileObjectBase fo) {
        externallyRemoved.set(fo);
    }

    private RemoteFileObjectBase vcsSafeGetFileObject(String path) {
        RemoteFileObjectBase fo = factory.getCachedFileObject(path);
        if (fo == null) {
            RemoteFileObjectBase removing = beingRemoved.get();
            if (removing != null && removing.getPath().equals(path)) {
                fo = removing;
            }
        }
        return fo;
    }
    
    @org.netbeans.api.annotations.common.SuppressWarnings("NP") // Three state
    public Boolean vcsSafeIsDirectory(String path) {
        path = PathUtilities.normalizeUnixPath(path);
        RemoteFileObjectBase removed = externallyRemoved.get();
        if (removed != null && removed.getPath().equals(path)) {
            // for an object that is just detected as externally removed
            // it doesn't make sense to ask remote host whether it is directory
            return isDirectoryByFileObjectType(removed);
        }
        RemoteFileObjectBase fo = vcsSafeGetFileObject(path);
        if (fo == null) {
            return null;
        } else {
            return fo.isFolder() ? Boolean.TRUE : Boolean.FALSE;
        }            
    }

    private boolean isDirectoryByFileObjectType(RemoteFileObjectBase removed) {
        switch (removed.getType()) {
            case Directory:
                return true;
            case NamedPipe:
            case CharacterSpecial:
            case MultiplexedCharacterSpecial:
            case SpecialNamed:
            case BlockSpecial:
            case MultiplexedBlockSpecial:
            case Regular:
            case NetworkSpecial:
            case SymbolicLink:
            case Shadow:
            case Socket:
            case Door:
            case EventPort:
            case Undefined:
            default:
                return false;
        }
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP") // Three state
    public Boolean vcsSafeIsFile(String path) {
        path = PathUtilities.normalizeUnixPath(path);
        RemoteFileObjectBase removed = externallyRemoved.get();
        if (removed != null && removed.getPath().equals(path)) {
            // for an object that is just detected as externally removed
            // it doesn't make sense to ask remote host whether it is directory
            return !isDirectoryByFileObjectType(removed);
        }
        RemoteFileObjectBase fo = vcsSafeGetFileObject(path);
        if (fo == null) {
            return null;
        } else {
            switch(fo.getType()) {
                // TODO: should we change isData() instead?
                case Regular:
                    return true;
                case SymbolicLink:
                    return fo.isData() ? Boolean.TRUE : Boolean.FALSE;
                default:
                    return false;
            }            
        }            
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP") // Three state
    public Boolean vcsSafeIsSymbolicLink(String path) {
        path = PathUtilities.normalizeUnixPath(path);
        RemoteFileObjectBase removed = externallyRemoved.get();
        if (removed != null && removed.getPath().equals(path)) {
            // for an object that is just detected as externally removed
            // it doesn't make sense to ask remote host whether it is directory
            return removed.getType() == FileInfoProvider.StatInfo.FileType.SymbolicLink;
        }
        RemoteFileObjectBase fo = vcsSafeGetFileObject(path);
        if (fo == null) {
            return null;
        } else {
            return fo.getType() == FileInfoProvider.StatInfo.FileType.SymbolicLink;
        }            
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP") // Three state
    public Boolean vcsSafeCanonicalPathDiffers(String path) {
        path = PathUtilities.normalizeUnixPath(path);
        RemoteFileObjectBase removed = externallyRemoved.get();
        if (removed != null && removed.getPath().equals(path)) {
            // for an object that is just detected as externally removed
            // it doesn't make sense to ask remote host whether it is directory
            return removed instanceof RemoteLinkBase;
        }
        RemoteFileObjectBase fo = vcsSafeGetFileObject(path);
        if (fo == null) {
            return null;
        } else {
            return fo instanceof RemoteLinkBase;
        }            
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP") // Three state
    public Boolean vcsSafeExists(String path) {
        path = PathUtilities.normalizeUnixPath(path);
        RemoteFileObjectBase fo = vcsSafeGetFileObject(path);
        if (fo == null) {
            String parentPath = PathUtilities.getDirName(path);            
            if (parentPath != null) {
                RemoteFileObjectBase parentFO = vcsSafeGetFileObject(parentPath);
                if (parentFO != null) {
                    String childNameExt = PathUtilities.getBaseName(path);
                    RemoteFileObject childFO = parentFO.getFileObject(childNameExt, new HashSet<String>());
                    return (childFO != null && childFO.isValid()) ? Boolean.TRUE : Boolean.FALSE;
                }
            }
            return null;
        } else {
            return fo.isValid() ? Boolean.TRUE : Boolean.FALSE;
        }            
    }

    public Long vcsSafeLastModified(String path) {
        path = PathUtilities.normalizeUnixPath(path);
        RemoteFileObjectBase fo = vcsSafeGetFileObject(path);
        if (fo == null) {
            String parentPath = PathUtilities.getDirName(path);
            if (parentPath != null) {
                RemoteFileObjectBase parentFO = vcsSafeGetFileObject(parentPath);
                if (parentFO != null) {
                    String childNameExt = PathUtilities.getBaseName(path);
                    RemoteFileObject childFO = parentFO.getFileObject(childNameExt, new HashSet<String>());
                    if (childFO != null) {
                        fo = childFO.getImplementor();
                    }
                }
            }
        }
        if (fo == null) {
            return null;
        } else {
            return fo.lastModified().getTime();
        }
    }
    
    /**
     * Gets suspend info for this directory or one of its parents
     */
    /*package*/ SuspendInfo getSuspendInfo(RemoteDirectory dir) {
        for(RemoteDirectory d = dir; d != null; d = d.getParentImpl()) {
            if (d.getFlag(RemoteFileObjectBase.MASK_SUSPEND_WRITES)) {
                synchronized (suspendInfo) {
                    SuspendInfo info = suspendInfo.get(d);
                    if (info == null) {
                        info = new SuspendInfo(d);
                        suspendInfo.put(d, info);
                    }
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * Gets and removes suspend info for exactly this directory
     */
    /*package*/ SuspendInfo removeSuspendInfo(RemoteDirectory dir) {
        synchronized(suspendInfo) {
            return suspendInfo.remove(dir);
        }
    }

    private final class StatusImpl implements StatusDecorator, ImageDecorator, LookupListener, FileStatusListener {

        /** result with providers */
        private final Lookup.Result<AnnotationProvider> annotationProviders;
        private Collection<? extends AnnotationProvider> previousProviders;

        {
            annotationProviders = Lookup.getDefault().lookupResult(AnnotationProvider.class);
            annotationProviders.addLookupListener(this);
            resultChanged(null);
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            Collection<? extends AnnotationProvider> now = annotationProviders.allInstances();
            Collection<? extends AnnotationProvider> add;

            if (previousProviders != null) {
                add = new HashSet<>(now);
                add.removeAll(previousProviders);
                HashSet<AnnotationProvider> toRemove = new HashSet<AnnotationProvider>(previousProviders);
                toRemove.removeAll(now);
                for (AnnotationProvider ap : toRemove) {
                    ap.removeFileStatusListener(this);
                }
            } else {
                add = now;
            }

            for (AnnotationProvider ap : add) {
                try {
                    ap.addFileStatusListener(this);
                } catch (java.util.TooManyListenersException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            previousProviders = now;
        }

        public SystemAction[] getActions(Set<FileObject> foSet) {

            javax.swing.Action[] retVal = null;
            java.util.Iterator<? extends AnnotationProvider> it = annotationProviders.allInstances().iterator();
            while (retVal == null && it.hasNext()) {
                AnnotationProvider ap = it.next();
                retVal = ap.actions(foSet);
            }
            if (retVal != null) {
                // right now we handle just SystemAction, it can be changed if necessary
                SystemAction[] ret = new SystemAction[retVal.length];
                for (int i = 0; i < retVal.length; i++) {
                    if (retVal[i] instanceof SystemAction) {
                        ret[i] = (SystemAction) retVal[i];
                    }
                }
                return ret;
            }
            return null;
        }

        @Override
        public void annotationChanged(FileStatusEvent ev) {
            fireFileStatusChanged(ev);
        }

        @Override
        public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
            for(AnnotationProvider ap : annotationProviders.allInstances()) {
                Image retVal = ap.annotateIcon(icon, iconType, files);
                if (retVal != null) {
                    return retVal;
                }
            }
            return icon;
        }

        @Override
        public String annotateName(String name, Set<? extends FileObject> files) {
            for(AnnotationProvider ap : annotationProviders.allInstances()) {
                String retVal = ap.annotateName(name, files);
                if (retVal != null) {
                    return retVal;
                }
            }
            return name;
        }

        @Override
        public String annotateNameHtml(String name, Set<? extends FileObject> files) {
            for(AnnotationProvider ap : annotationProviders.allInstances()) {
                String retVal = ap.annotateNameHtml(name, files);
                if (retVal != null) {
                    return retVal;
                }
            }
            return null;
        }
    }
    
    private static class RootFileObject extends RemoteDirectory {

        private RootFileObject(RemoteFileObject wrapper, RemoteFileSystem fileSystem, ExecutionEnvironment execEnv, File cache) {
            super(wrapper, fileSystem, execEnv, null, "", cache);
        }

        @Override
        public boolean isRoot() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public Object getAttribute(String attrName) {
            if (FileOperationsProvider.ATTRIBUTE.equals(attrName)) {
                if (USE_VCS) {
                    try {
                        //getFileSystem().setInsideVCS(true);
                        return FileOperationsProvider.getDefault().getFileOperations(getFileSystem());
                    } finally {
                        //getFileSystem().setInsideVCS(false);
                    }
                }
            }
            return super.getAttribute(attrName);
        }
    }
    
    public static final class FileInfo {
        private final String path;
        private final FileType type;

        public FileInfo(RemoteFileObjectBase fo) {
            this.path = fo.getPath();
            this.type = fo.getType();
        }

        public FileInfo(String path, FileType type) {
            this.path = path;
            this.type = type;
        }

        public String getPath() {
            return path;
        }

        public FileType getType() {
            return type;
        }        
    }

    private class RemoteFileSupport extends ConnectionNotifier.NamedRunnable {

        public RemoteFileSupport() {
            super(NbBundle.getMessage(RemoteFileSupport.class, "RemoteDownloadTask.TITLE", execEnv.getDisplayName()));
        }
        
        @Override
        protected void runImpl() {
            try {
                onConnect();
            } catch (ConnectException ex) {
                RemoteLogger.getInstance().log(Level.INFO, NbBundle.getMessage(getClass(), "RemoteFileSystemNotifier.ERROR", execEnv), ex);
                ConnectionNotifier.addTask(execEnv, this);
            } catch (InterruptedException | InterruptedIOException ex) {
                RemoteLogger.finest(ex);
            } catch (IOException ex) {
                RemoteLogger.getInstance().log(Level.INFO, NbBundle.getMessage(getClass(), "RemoteFileSystemNotifier.ERROR", execEnv), ex);
                ConnectionNotifier.addTask(execEnv, this);
            } catch (ExecutionException ex) {
                RemoteLogger.getInstance().log(Level.INFO, NbBundle.getMessage(getClass(), "RemoteFileSystemNotifier.ERROR", execEnv), ex);
                ConnectionNotifier.addTask(execEnv, this);
            }
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(RemoteFileSupport.class, 
                readOnlyConnectNotification.get() ? "RemoteDownloadTask.TEXT_RO" : "RemoteDownloadTask.TEXT", // NOI18N
                execEnv.getDisplayName());
        }

        // NB: it is always called in a specially created thread
        private void onConnect() throws InterruptedException, ConnectException, InterruptedIOException, IOException, ExecutionException {
            fireProblemListeners(null);
        }

        public void addPendingFile(RemoteFileObjectBase fo) {
            RemoteLogger.getInstance().log(Level.FINEST, "Adding notification for {0}:{1}", new Object[]{execEnv, fo.getPath()}); //NOI18N
            ConnectionNotifier.addTask(execEnv, this);
        }
    }
}
