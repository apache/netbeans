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

package org.netbeans.modules.remote.impl.fs.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.netbeans.modules.dlight.libs.common.DLightLibsCommonLogger;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.nativeexecution.api.util.RemoteStatistics;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fs.DirEntry;
import org.netbeans.modules.remote.impl.fs.DirEntryImpl;
import org.netbeans.modules.remote.impl.fs.DirEntryList;
import org.netbeans.modules.remote.impl.fs.RemoteDirectory;
import org.netbeans.modules.remote.impl.fs.RemoteFileObjectBase;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystem;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemManager;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemTransport;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class FSSTransport extends RemoteFileSystemTransport implements ConnectionListener {

    private static final Map<ExecutionEnvironment, FSSTransport> instances = new HashMap<>();
    private static final Object instancesLock = new Object();

    public static final boolean USE_FS_SERVER = RemoteFileSystemUtils.getBoolean("remote.fs_server", true);
    public static final boolean VERBOSE_RESPONSE = Boolean.getBoolean("remote.fs_server.verbose.response");

    /** Default timeout; zero means forever */
    public static final int DEFAULT_TIMEOUT = Integer.getInteger("remote.fs_server.default.timeout", 15*1000); //NOI18N

    private final ExecutionEnvironment env;

    private final FSSDispatcher dispatcher;

    private final AtomicInteger dirReadCnt = new AtomicInteger(0);
    private final AtomicInteger warmupCnt = new AtomicInteger(0);

    public static FSSTransport getInstance(ExecutionEnvironment env) {
        if (!USE_FS_SERVER) {
            return null;
        }
        synchronized (instancesLock) {
            FSSTransport instance = instances.get(env);
            if (instance == null) {
                instance = new FSSTransport(env);
                instances.put(env, instance);
                ConnectionManager.getInstance().addConnectionListener(instance);
            }
            return instance;
        }
    }

    public static FSSTransport removeInstance(ExecutionEnvironment env) {
        synchronized (instancesLock) {
            FSSTransport instance = instances.remove(env);
            if (instance != null) {
                ConnectionManager.getInstance().removeConnectionListener(instance);
            }
            return instance;
        }
    }

    private FSSTransport(ExecutionEnvironment env) {
        this.env = env;
        this.dispatcher = FSSDispatcher.getInstance(env);
    }

    @Override
    public boolean isValidFast() {
        return dispatcher.isValidFast();
    }

    @Override
    protected boolean isValidSlow()
            throws ConnectException, InterruptedException {
        if (!dispatcher.isValidFast()) {
            return false;
        }
        return dispatcher.isValidSlow();
    }

    @Override
    protected DirEntry stat(String path)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        try {
            return stat_or_lstat(path, false, 0);
        } catch (TimeoutException ex) {
            RemoteFileSystemUtils.reportUnexpectedTimeout(ex, path);
            throw ex;
        }
    }

    @Override
    protected DirEntry stat(String path, int timeoutMillis)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        return stat_or_lstat(path, false, timeoutMillis);
    }

    @Override
    protected DirEntry lstat(String path)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        try {
            return stat_or_lstat(path, true, 0);
        } catch (TimeoutException ex) {
            RemoteFileSystemUtils.reportUnexpectedTimeout(ex, path);
            throw ex;
        }
    }

    @Override
    protected DirEntry lstat(String path, int timeoutMillis)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        return stat_or_lstat(path, true, timeoutMillis);
    }

    private DirEntry stat_or_lstat(String path, boolean lstat, int timeoutMillis)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {

        if (path.isEmpty()) {
            path = "/"; // NOI18N
        }

        FSSRequestKind requestKind = lstat ? FSSRequestKind.FS_REQ_LSTAT : FSSRequestKind.FS_REQ_STAT;
        FSSRequest request = new FSSRequest(requestKind, path);
        long time = System.currentTimeMillis();
        RemoteStatistics.ActivityID activityID = RemoteStatistics.startChannelActivity(
                lstat ? "fs_server_lstat" : "fs_server_stat", path); // NOI18N
        FSSResponse response = null;
        try {
            RemoteLogger.finest("Sending stat/lstat request #{0} for {1} to fs_server",
                    request.getId(), path);
            response = dispatcher.dispatch(request);
            FSSResponse.Package pkg = response.getNextPackage(timeoutMillis);
            if (pkg.getKind() == FSSResponseKind.FS_RSP_ENTRY) {
                return createDirEntry(pkg, request.getId(), env);
            } else if (pkg.getKind() == FSSResponseKind.FS_RSP_ERROR) {
                IOException ioe = createIOException(pkg);
                throw new ExecutionException(ioe);
            } else {
                throw new IllegalStateException("wrong response: " + pkg); //NOI18N
            }

        } finally {
            if (response != null) {
                response.dispose();
            }
            RemoteStatistics.stopChannelActivity(activityID, 0);
            RemoteLogger.finest("Getting stat/lstat #{0} from fs_server for {1} took {2} ms",
                    request.getId(), path, System.currentTimeMillis() - time);
        }

    }

    private String timeoutMessage(String operation, int timeoutMillis, String path) {
        return String.format("Timeout %d ms when %s for %s:%s", //NOI18N
                timeoutMillis, operation, env, path); //NOI18N
    }

    @Override
    protected boolean canCopy(String from, String to) {
        return true;
    }

    @Override
    protected DirEntryList copy(String from, String to,
            Collection<IOException> subdirectoryExceptions)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {

        FSSRequest request = new FSSRequest(FSSRequestKind.FS_REQ_COPY, from, to);
        FSSResponse response = null;
        RemoteStatistics.ActivityID activityID = RemoteStatistics.startChannelActivity("fs_server_copy", from, to); // NOI18N
        long time = System.currentTimeMillis();
        try {
            RemoteLogger.finest("Sending request #{0} for copying {1} to {2} to fs_server", request.getId(), from, to);
            response = dispatcher.dispatch(request);
            FSSResponse.Package pkg = response.getNextPackage();
            while (pkg.getKind() == FSSResponseKind.FS_RSP_ERROR) {
                IOException ex = createIOException(pkg);
                subdirectoryExceptions.add(ex);
                pkg = response.getNextPackage();
            }
            if (pkg.getKind() == FSSResponseKind.FS_RSP_END) {
                // no ls info => throw an exception
                if (subdirectoryExceptions.isEmpty()) {
                    throw new IOException("Unexpected package list end"); // NOI18N
                } else {
                    throw subdirectoryExceptions.iterator().next();
                }
            } else if (pkg.getKind() == FSSResponseKind.FS_RSP_LS) {
                return readEntries(response, to, request.getId(), dirReadCnt);
            } else {
                throw new IOException("Unexpected package kind: " + pkg.getKind()); // NOI18N
            }
        } catch (ExecutionException ex) {
            if (RemoteFileSystemUtils.isFileNotFoundException(ex)) {
                throw new FileNotFoundException(from + " or " + to); //NOI18N
            } else {
                throw new IOException(ex);
            }
        } finally {
            RemoteStatistics.stopChannelActivity(activityID, 0);
            RemoteLogger.finest("Communication #{0} with fs_server for copying {1} to {2} took {3} ms",
                    request.getId(), from, to, System.currentTimeMillis() - time);
            if (response != null) {
                response.dispose();
            }
        }
    }

    @Override
    protected boolean canMove(String from, String to) {
        return true;
    }

    @Override
    protected MoveInfo move(String from, String to)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        Future<FileInfoProvider.StatInfo> f = FileInfoProvider.move(env, from, to);
        f.get();
        String fromParent = PathUtilities.getDirName(from);
        DirEntryList fromList = readDirectory(fromParent == null ? "/" : fromParent); // NOI18N
        String toParent = PathUtilities.getDirName(to);
        DirEntryList toList = readDirectory(toParent == null ? "/" : toParent); // NOI18N
        return new MoveInfo(fromList, toList);
    }

    private IOException createIOException(FSSResponse.Package pkg) {
        Buffer buf = pkg.getBuffer();
        buf.getChar(); // skip kind
        buf.getInt(); // unused
        int errno = buf.getInt();
        String emsg = buf.getRest();
        IOException ioe = FSSUtil.createIOException(errno, emsg, env);
        return ioe;
    }

    @Override
    protected DirEntryList readDirectory(String path)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        if (path.isEmpty()) {
            path = "/"; // NOI18N
        }
        FSSRequest request = new FSSRequest(FSSRequestKind.FS_REQ_LS, path);
        long time = System.currentTimeMillis();
        RemoteStatistics.ActivityID activityID = RemoteStatistics.startChannelActivity("fs_server_ls", path); // NOI18N
        AtomicInteger realCnt = new AtomicInteger(0);
        FSSResponse response = null;
        try {
            RemoteLogger.finest("Sending request #{0} for directory {1} to fs_server",
                    request.getId(), path);
            // XXX: a temporary simplistic solution
            response = dispatcher.dispatch(request);
            FSSResponse.Package pkg = response.getNextPackage();
            assert pkg.getKind() == FSSResponseKind.FS_RSP_LS : "Wrong package kind: " + pkg.getKind();
            Buffer buf = pkg.getBuffer();
            buf.getChar();
            int respId = buf.getInt();
            assert respId == request.getId();
            String serverPath = buf.getString();
            if (!serverPath.equals(path)) {
                DLightLibsCommonLogger.assertTrue(false, "Unexpected path in response: \"" + //NOI18N
                        serverPath + "\" expected \"" + path + "\""); //NOI18N
            }
            return readEntries(response, path, request.getId(), realCnt);
        } finally {
            dirReadCnt.incrementAndGet();
            if (response != null) {
                response.dispose();
            }
            RemoteStatistics.stopChannelActivity(activityID, 0);
            RemoteLogger.finest("Communication #{0} with fs_server for directory {1} ({2} entries read) took {3} ms",
                    request.getId(), path, realCnt.get(), System.currentTimeMillis() - time);
        }
    }

    private DirEntryList readEntries(FSSResponse response, String path, long reqId, AtomicInteger cnt)
            throws TimeoutException, IOException, InterruptedException, ExecutionException {
        RemoteLogger.finest("Reading response #{0} from fs_server for directory {1})",
                reqId, path);
        List<FSSResponse.Package> packages = new ArrayList<>();
        for (FSSResponse.Package pkg = response.getNextPackage();
                pkg.getKind() != FSSResponseKind.FS_RSP_END;
                pkg = response.getNextPackage()) {
            if (pkg.getKind() == FSSResponseKind.FS_RSP_END) {
                break;
            }
            if (pkg.getKind() == FSSResponseKind.FS_RSP_ERROR) {
                throw createIOException(pkg);
            }
            cnt.incrementAndGet();
            if (VERBOSE_RESPONSE) {
                RemoteLogger.finest("\tfs_server response #{0}: [{1}] {2}",
                        reqId, cnt.get(), pkg.getData());
            }
            packages.add(pkg);
        }
        RemoteLogger.finest("Processing response #{0} from fs_server for directory {1}",
                reqId, path);
        List<DirEntry> result = new ArrayList<>();
        for (FSSResponse.Package pkg : packages) {
            try {
                assert pkg != null;
                DirEntry entry = createDirEntry(pkg, reqId, env);
                // TODO: windows names
                result.add(entry);
            } catch (Throwable thr) {
                thr.printStackTrace(System.err);
            }
        }
        return new DirEntryList(result, System.currentTimeMillis());
    }

    private DirEntry createDirEntry(FSSResponse.Package pkg, long reqId, ExecutionEnvironment env) {
        try {
            Buffer buf = pkg.getBuffer();
            char kindChar = buf.getChar();
            assert kindChar == pkg.getKind().getChar();
            assert pkg.getKind() == FSSResponseKind.FS_RSP_ENTRY;
            int id = buf.getInt();
            assert id == reqId;
            //        name       type size  date          acc dev  ino lnk
            // e 1 10 lost+found d   16384 1398697954000  --- 2049 11  0
            String name = buf.getString();

            char type = buf.getChar();
            long size = buf.getLong();
            long mtime = buf.getLong() / 1000 * 1000; // to be consistent with jsch sftp

            char r = buf.getChar();
            char w = buf.getChar();
            char x = buf.getChar();
            buf.getChar(); // space

            if ((r  != 'r' && r != '-') || (w != 'w' && w != '-') || (x != 'x' && x != '-')) {
                throw new IllegalStateException("Wrong file access format: " + buf); //NO18N // NOI18N
            }
            boolean canRead = r == 'r';
            boolean canWrite = w == 'w';
            boolean canExec = x == 'x';

            long device = buf.getLong();
            long inode = buf.getLong();

            String linkTarget = buf.getString();
            if (linkTarget.isEmpty()) {
                linkTarget = null;
            }

            return DirEntryImpl.create(name, size, mtime, canRead, canWrite, canExec,
                    type, device, inode, linkTarget);
        } catch (Throwable thr) {
            throw new IllegalArgumentException("Error processing response " + pkg, thr); // NOI18N
        }
    }

    @Override
    public void connected(ExecutionEnvironment env) {
        if (env.equals(this.env)) {
            dispatcher.connected();
        }
    }

    @Override
    public void disconnected(ExecutionEnvironment env) {
    }

    public final void testSetCleanupUponStart(boolean cleanup) {
        dispatcher.setCleanupUponStart(cleanup);
    }

    public static final void testDumpInstances(PrintStream ps) {
        Collection<FSSTransport> transports;
        synchronized (instancesLock) {
            transports = instances.values();
        }
        for (FSSTransport tr : transports) {
            tr.testDump(ps);
        }
    }

    protected void testDump(PrintStream ps) {
        this.dispatcher.testDump(ps);
    }

    @Override
    protected boolean needsClientSidePollingRefresh() {
        return false;
    }

    @Override
    protected void registerDirectoryImpl(RemoteDirectory directory) {
        if (ConnectionManager.getInstance().isConnectedTo(env)) {
            requestRefreshCycle(directory.getPath());
        }
    }

    @Override
    protected void unregisterDirectoryImpl(String path) {

    }

    @Override
    protected void onConnect() {
        // nothing: see ConnectTask
    }

    @Override
    protected void onFocusGained() {
        requestRefreshCycle("/"); //NOI18N
    }

    @Override
    protected void scheduleRefresh(Collection<String> paths) {
        if (!dispatcher.isRefreshing()) {
            for (String path : paths) {
                dispatcher.requestRefreshCycle(path.isEmpty() ? "/" : path); // NOI18N
            }
        }
    }

    private void requestRefreshCycle(String path) {
        if (!dispatcher.isRefreshing()) {
            // file system root has empty path
            dispatcher.requestRefreshCycle(path.isEmpty() ? "/" : path); // NOI18N
        }
    }

    @Override
    protected boolean canRefreshFast() {
        return true;
    }

    @Override
    protected void refreshFast(String path, boolean expected)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {

        long time = System.currentTimeMillis();
        RemoteStatistics.ActivityID activityID = RemoteStatistics.startChannelActivity("fs_server_fast_refresh", path); // NOI18N
        FSSResponse response = null;
        FSSRequest request = new FSSRequest(FSSRequestKind.FS_REQ_REFRESH, path, false);
        try {
            RemoteLogger.finest("Sending request #{0} for directory {1} to fs_server", request.getId(), path);
            response = dispatcher.dispatch(request);
            FSSResponse.Package pkg = response.getNextPackage();
            assert pkg.getKind() == FSSResponseKind.FS_RSP_REFRESH;
            Buffer buf = pkg.getBuffer();
            buf.getChar();
            int respId = buf.getInt();
            assert respId == request.getId();
            String serverPath = buf.getString();
            if (!serverPath.equals(path)) {
                DLightLibsCommonLogger.assertTrue(false, "Unexpected path in response: \"" + //NOI18N
                        serverPath + "\" expected \"" + path + "\""); //NOI18N
            }
            IOException ex = null;
            for (pkg = response.getNextPackage();
                    pkg.getKind() != FSSResponseKind.FS_RSP_END;
                    pkg = response.getNextPackage()) {
                if (pkg.getKind() == FSSResponseKind.FS_RSP_ERROR) {
                    ex = createIOException(pkg);
                }
                if (pkg.getKind() != FSSResponseKind.FS_RSP_CHANGE) {
                    new IllegalArgumentException("Wrong response kind: " + response).printStackTrace(System.err); // NOI18N
                    continue;
                }
                buf = pkg.getBuffer(); // e.g. "c 5 19 /tmp/tmp.BTFx185bJs"
                buf.getChar();
                buf.getInt();
                String changedPath = buf.getString();
                if (!changedPath.startsWith(path)) {
                    new IllegalArgumentException("Unexpected changed path: " + response).printStackTrace(System.err); // NOI18N
                    continue;
                }
                final RemoteFileSystem fs = RemoteFileSystemManager.getInstance().getFileSystem(env);
                fs.getRefreshManager().removeFromRefresh(changedPath);
                RemoteFileObjectBase fo = fs.getFactory().getCachedFileObject(changedPath);
                if (fo != null) {
                    fo.refreshImpl(false, null, expected, RemoteFileObjectBase.RefreshMode.DEFAULT);
                }
                // TODO: should we proceed with other directories in the case of exception?
            }
            if (ex != null) {
                throw ex;
            }
        } finally {
            if (response != null) {
                response.dispose();
            }
            RemoteStatistics.stopChannelActivity(activityID, 0);
            RemoteLogger.finest("Fast refresh #{0} of {1} took {2} ms",
                    request.getId(), path, System.currentTimeMillis() - time);
        }
    }

    @Override
    protected Warmup createWarmup(String path) {
        WarmupImpl warmup = new WarmupImpl(path);
        warmup.start();
        return warmup;
    }

    @Override
    protected DirEntryList delete(String path, boolean directory) throws TimeoutException, ConnectException, IOException {
        FSSRequest request = new FSSRequest(FSSRequestKind.FS_REQ_DELETE, path);
        FSSResponse response = null;
        RemoteStatistics.ActivityID activityID = RemoteStatistics.startChannelActivity("fs_server_delete", path); // NOI18N
        AtomicInteger cnt = new AtomicInteger(0);
        long time = System.currentTimeMillis();
        try {
            RemoteLogger.finest("Sending request #{0} for removing {1} to fs_server", request.getId(), path);
            response = dispatcher.dispatch(request);
            FSSResponse.Package pkg = response.getNextPackage();
            if (pkg.getKind() == FSSResponseKind.FS_RSP_ERROR) {
                throw createIOException(pkg);
            } else {
                assert pkg.getKind() == FSSResponseKind.FS_RSP_LS;
            }
            return readEntries(response, path, request.getId(), dirReadCnt);
        } catch (ConnectException | InterruptedException | ExecutionException ex) {
            throw new IOException(ex);
        } finally {
            RemoteStatistics.stopChannelActivity(activityID, 0);
            RemoteLogger.finest("Communication #{0} with fs_server for removing {1} ({2} entries read) took {3} ms",
                    request.getId(), path, cnt.get(), System.currentTimeMillis() - time);
            if (response != null) {
                response.dispose();
            }
        }
    }

    @Override
    protected DirEntry uploadAndRename(File srcFile, String pathToUpload, String pathToRename)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException, InterruptedException {

        CommonTasksSupport.UploadParameters params = new CommonTasksSupport.UploadParameters(
                srcFile, env, pathToUpload, null, -1, false, null, false);
        Future<CommonTasksSupport.UploadStatus> task = CommonTasksSupport.uploadFile(params);
        CommonTasksSupport.UploadStatus uploadStatus = task.get();
        if (uploadStatus.isOK()) {
            RemoteLogger.getInstance().log(Level.FINEST, "Uploading to {0} succeeded", pathToUpload);
            if(pathToRename == null) { // possible if parent directory is r/o
                return lstat(pathToUpload);
            } else {
                return renameAfterUpload(pathToUpload, pathToRename);
            }
        } else {
            RemoteLogger.getInstance().log(Level.FINEST, "Uploading to {0} failed", pathToUpload);
            throw new IOException(uploadStatus.getError() + " " + uploadStatus.getExitCode()); //NOI18N
        }
    }

    private DirEntry renameAfterUpload(String pathToUpload, String pathToRename)
            throws IOException, InterruptedException, ExecutionException {

        FSSRequest request = new FSSRequest(FSSRequestKind.FS_REQ_MOVE, pathToUpload, pathToRename);
        FSSResponse response = null;
        RemoteStatistics.ActivityID activityID = RemoteStatistics.startChannelActivity("fs_server_rename_afre_upload", pathToUpload, pathToRename); // NOI18N
        long time = System.currentTimeMillis();
        try {
            RemoteLogger.finest("Sending request #{0} for renaming {1} to {2} to fs_server", request.getId(), pathToUpload, pathToRename);
            response = dispatcher.dispatch(request);
            FSSResponse.Package pkg = response.getNextPackage();
            if (pkg.getKind() == FSSResponseKind.FS_RSP_ERROR) {
                IOException ex = createIOException(pkg);
                throw ex;
            } else if (pkg.getKind() == FSSResponseKind.FS_RSP_ENTRY) {
                return createDirEntry(pkg, request.getId(), env);
            } else {
                throw new IOException("Unexpected package kind: " + pkg.getKind()); // NOI18N
            }
        } catch (ConnectException | TimeoutException ex) {
            throw new IOException(ex);
        } finally {
            RemoteStatistics.stopChannelActivity(activityID, 0);
            RemoteLogger.finest("Communication #{0} with fs_server for renaming {1} to {2} took {3} ms",
                    request.getId(), pathToUpload, pathToRename, System.currentTimeMillis() - time);
            if (response != null) {
                response.dispose();
            }
        }
    }


    @Override
    protected boolean canSetAccessCheckType() {
        return true;
    }

    @Override
    protected void setAccessCheckType(FileSystemProvider.AccessCheckType accessCheckType) {
        dispatcher.setAccessCheckType(accessCheckType);
    }

    @Override
    protected FileSystemProvider.AccessCheckType getAccessCheckType() {
        return dispatcher.getAccessCheckType();
    }
    
    @Override
    protected boolean canDeleteOnDisconnect() {
        return RemoteFileSystemUtils.getBoolean("remote.native.delete.on.exit", true);
    }

    @Override
    protected void deleteOnDisconnect(String[] paths) 
        throws IOException, InterruptedException, ExecutionException {
        for (String p : paths) {
            FSSRequest request = new FSSRequest(FSSRequestKind.FS_REQ_DELETE_ON_DISCONNECT, p, true);
            dispatcher.dispatch(request);
        }
    }    

    @Override
    protected void shutdown() {
        dispatcher.shutdown();
    }

    private class WarmupImpl implements Warmup, FSSResponse.Listener, Runnable {

        private final String path;
        private final Map<String, DirEntryList> cache = new HashMap<>();
        private final Object lock = new Object();
        private FSSResponse response;

        private final boolean useListener = RemoteFileSystemUtils.getBoolean("remote.warmup.listener", false);
        private final RequestProcessor rp;

        public WarmupImpl(String path) {
            this.path = path.isEmpty() ? "/" : path; //NOI18N
            rp = useListener ? null : new RequestProcessor("Warming Up " + env + ':' + this.path, 1); //NOI18N
        }

        public void start() {
            if (useListener) {
                try {
                    sendRequest();
                } catch (IOException | ExecutionException ex) {
                    ex.printStackTrace(System.err);
                } catch (InterruptedException ex) {
                    // don't log InterruptedException
                }
            } else {
                rp.post(this);
            }
        }

        @Override
        public void packageAdded(FSSResponse.Package pkg) {
            if (useListener) {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        }

        @Override
        public DirEntryList getAndRemove(String path) throws InterruptedException {
            while (true) {
                DirEntryList l = tryGetAndRemove(path);
                if (l != null) {
                    return l;
                }
                synchronized (lock) {
                    lock.wait(1000);
                }
            }
        }


        @Override
        public DirEntryList tryGetAndRemove(String path) {
            synchronized (lock) {
                DirEntryList entries = cache.remove(path);
                if (entries != null) {
                    RemoteLogger.fine("Warming up: got entries for {0}; {1} cached entry lists remain", path, cache.size());
                    return entries;
                }
            }
            return null;
        }

        @Override
        public void remove(String path){
            synchronized (lock) {
                cache.remove(path);
            }
        }


        @Override
        public void run() {
            long time = System.currentTimeMillis();
            RemoteStatistics.ActivityID activityID = RemoteStatistics.startChannelActivity("fs_server_warmup", path); // NOI18N
            try {
                RemoteLogger.fine("Warming up fs_server for {0}", path);
                warmapImpl();
            } catch (IOException | TimeoutException ex) {
                ex.printStackTrace(System.err);
            } catch (InterruptedException ex) {
                // don't report InterruptedException
            } finally {
                warmupCnt.incrementAndGet();
                RemoteStatistics.stopChannelActivity(activityID, 0);
                RemoteLogger.fine("Warming up fs_server for {0} took {1} ms",
                        path, System.currentTimeMillis() - time);
            }
        }

        private FSSResponse sendRequest()
                throws IOException, ConnectException, ExecutionException, InterruptedException {

            FSSRequest request = new FSSRequest(FSSRequestKind.FS_REQ_RECURSIVE_LS, path);
                RemoteLogger.finest("Sending recursive request #{0} for directory {1} to fs_server",
                        request.getId(), path);
            return dispatcher.dispatch(request, this);
        }

        private void warmapImpl() throws TimeoutException, IOException, InterruptedException {
            long time = System.currentTimeMillis();
            AtomicInteger realCnt = new AtomicInteger(0);
            try {
                synchronized (lock) {
                    response = sendRequest();
                }

                while (true) {
                    FSSResponse.Package pkg = response.getNextPackage();
                    if (pkg.getKind() == FSSResponseKind.FS_RSP_END) {
                        break;
                    }
                    Buffer buf = pkg.getBuffer();
                    char respKind = buf.getChar();
                    assert respKind == FSSResponseKind.FS_RSP_RECURSIVE_LS.getChar();
                    int respId = buf.getInt();
                    assert respId == response.getId();
                    String serverPath = buf.getString();
                    DirEntryList entries = readEntries(response, serverPath, response.getId(), realCnt);
                    synchronized (lock) {
                        cache.put(serverPath, entries);
                        lock.notifyAll();
                    }
                }
            } catch (ConnectException | ExecutionException | TimeoutException ex) {
                ex.printStackTrace(System.err);
            } finally {
                FSSResponse r;
                synchronized (lock) {
                    r = response;
                }
                if (r != null) {
                    r.dispose();
                }
                RemoteLogger.finest("Warming up directory {1}:{2}: ({3} entries) took {4} ms",
                        env, path, realCnt.get(), System.currentTimeMillis() - time);
            }
        }
    }
}
