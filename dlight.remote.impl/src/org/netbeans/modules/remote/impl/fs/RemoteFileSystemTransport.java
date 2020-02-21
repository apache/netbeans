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
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fs.server.FSSTransport;
import org.netbeans.modules.remote.spi.FileSystemProvider;

/**
 *
 */
public abstract class RemoteFileSystemTransport {

    public interface Warmup {
        DirEntryList getAndRemove(String path) throws InterruptedException;
        void remove(String path);
        DirEntryList tryGetAndRemove(String path);
    }
    
    public static Warmup createWarmup(ExecutionEnvironment execEnv, String path) {
        return getInstanceFast(execEnv).createWarmup(path);
    }

    public static boolean needsClientSidePollingRefresh(ExecutionEnvironment execEnv) {
        return getInstanceFast(execEnv).needsClientSidePollingRefresh();
    }
    
    public static boolean canRefreshFast(ExecutionEnvironment execEnv) {
        return getInstanceFast(execEnv).canRefreshFast();
    }

    public static boolean canSetAccessCheckType(ExecutionEnvironment execEnv) {
        return getInstanceFast(execEnv).canSetAccessCheckType();
    }
    
    public static boolean canDeleteOnDisconnect(ExecutionEnvironment execEnv) {
        return getInstanceFast(execEnv).canDeleteOnDisconnect();
    }
    
    public static void deleteOnDisconnect(ExecutionEnvironment execEnv, String... paths)
        throws IOException, InterruptedException, ExecutionException {
        getInstanceFast(execEnv).deleteOnDisconnect(paths);
    }

    public static void setAccessCheckType(ExecutionEnvironment execEnv, FileSystemProvider.AccessCheckType accessCheckType) {
        getInstanceFast(execEnv).setAccessCheckType(accessCheckType);
    }

    static FileSystemProvider.AccessCheckType getAccessCheckType(ExecutionEnvironment execEnv) {
        return getInstanceFast(execEnv).getAccessCheckType();
    }
    
    public static void refreshFast(RemoteDirectory directory, boolean expected) 
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        if (!directory.hasCache()) {
            return;
        }
        String path = directory.getPath();
        if (path.isEmpty()) {
            path = "/"; // NOI18N
        }
        long time = 0;
        int foCount = 0, syncCount = 0;
        if (RemoteLogger.getInstance().isLoggable(Level.FINE)) {
            final RemoteFileSystem fs = directory.getFileSystem();
            time = System.currentTimeMillis();
            foCount = fs.getCachedFileObjectsCount();
            syncCount = fs.getDirSyncCount();
        }

        getInstanceSlow(directory.getExecutionEnvironment()).refreshFast(path,expected);

        if (RemoteLogger.getInstance().isLoggable(Level.FINE)) {
            final RemoteFileSystem fs = directory.getFileSystem();
            time = System.currentTimeMillis() - time;
            foCount = fs.getCachedFileObjectsCount() - foCount;
            syncCount = fs.getDirSyncCount() - syncCount;            
            RemoteLogger.fine("Fast refresh {0} [{1} fo, {2} syncs, {3} new fo] took {4} ms",  //NOI18N
                    directory.getDisplayName(), fs.getCachedFileObjectsCount(),
                    syncCount, foCount, time);
        }
    }

    public static void scheduleRefresh(ExecutionEnvironment env, Collection<String> paths) {
        getInstanceFast(env).scheduleRefresh(paths);
    }

    static void onFocusGained(ExecutionEnvironment execEnv) {
        getInstanceFast(execEnv).onFocusGained();
    }

    static void onConnect(ExecutionEnvironment execEnv) {
        getInstanceFast(execEnv).onConnect();
    }
        
    public static void registerDirectory(RemoteDirectory directory) {
        getInstanceFast(directory.getExecutionEnvironment()).registerDirectoryImpl(directory);
    }

    public static void unregisterDirectory(ExecutionEnvironment execEnv, String path) {
        getInstanceFast(execEnv).unregisterDirectoryImpl(path);
    }

    public static DirEntryList readDirectory(ExecutionEnvironment execEnv, String path) 
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {

        DirEntryList entries = null;
        RemoteFileSystemTransport transport = FSSTransport.getInstance(execEnv);
        if (transport != null && transport.isValidFast()) {
            try {
                entries = transport.readDirectory(path);
                // The agreement is as follows: if a fatal error occurs
                // (so we suppose fs_server can't work)
                // DirectoryReaderFS throws ExecutionException or IOException 
                // (InterruptedException and CancellationException don't mean server failed)
                // and DirectoryReaderFS.isValid()  is set to false.
                // In this case we need to fallback to the default (sftp) implementation
                // TODO: consider redesign?
            } catch (ExecutionException | IOException ex) {
                if (transport.isValidFast()) {
                    throw ex; // process as usual
                } // else fall back to sftp implementation
            }
        }
        if (entries == null) {
            if (RemoteFileSystemUtils.isUnitTestMode()) {
                // no fallback for unit tests!
                throw new ExecutionException("Can not get valid transport for " + execEnv, null); //NOI18N
            }            
            RemoteFileSystemTransport directoryReader = SftpTransport.getInstance(execEnv);
            entries = directoryReader.readDirectory(path);
        }            
        return entries;
    }
    
    public static DirEntry stat(ExecutionEnvironment execEnv, String path)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {

        return getInstanceSlow(execEnv).stat(path);
    }

    public static DirEntry stat(ExecutionEnvironment execEnv, String path, int timeoutMillis)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {

        return getInstanceSlow(execEnv).stat(path, timeoutMillis);
    }

    public static DirEntry lstat(ExecutionEnvironment execEnv, String path)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {

        return getInstanceSlow(execEnv).lstat(path);
     }

    public static DirEntry lstat(ExecutionEnvironment execEnv, String path, int timeoutMillis)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {

        return getInstanceSlow(execEnv).lstat(path, timeoutMillis);
     }

    public static DirEntryList delete(ExecutionEnvironment execEnv, String path, boolean directory) 
            throws ConnectException, IOException {
        try {
            return getInstanceSlow(execEnv).delete(path, directory);
        } catch (InterruptedException ex) {
            InterruptedIOException ioe = new InterruptedIOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } catch (TimeoutException ex) {
            throw new IOException(ex);
        }
    }

    public static boolean canCopy(ExecutionEnvironment execEnv, String from, String to) {
        return getInstanceFast(execEnv).canCopy(from, to);
    }

    /**
     * Copies the file, returns new parent directory content. Returning parent
     * directory content is for the sake of optimization. For example,
     * fs_server, can do copy and return refreshed content in one call. It can
     * return null if there is no way of doing that more effective than just
     * calling RemoteFileSystemTransport.readDirectory
     * @param  subdirectoryExceptions if we are copying a directory, 
     * problems that occur inside are to be re reported via this list;
     * if we are unable to create directory itself, then exception is thrown.
     * Can be null.
     *
     * @return parent directory content (can be null - see above)
     */
    public static DirEntryList copy(ExecutionEnvironment execEnv, String from, String to, 
            Collection<IOException> subdirectoryExceptions)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        return getInstanceSlow(execEnv).copy(from, to, subdirectoryExceptions);
    }

    public static class MoveInfo {
        public final DirEntryList from;
        public final DirEntryList to;
        public MoveInfo(DirEntryList src, DirEntryList dst) {
            this.from = src;
            this.to = dst;
        }
    }

    public static boolean canMove(ExecutionEnvironment execEnv, String from, String to) {
        return getInstanceFast(execEnv).canMove(from, to);
    }

    public static MoveInfo move(ExecutionEnvironment execEnv, String from, String to)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException {
        return getInstanceSlow(execEnv).move(from, to);
    }
    
    public static DirEntry uploadAndRename(ExecutionEnvironment execEnv, File src, 
            String pathToUpload, String pathToRename) 
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException, InterruptedException {
        return getInstanceSlow(execEnv).uploadAndRename(src, pathToUpload, pathToRename);
    }
    
    static void shutdown(ExecutionEnvironment execEnv) {
        RemoteFileSystemTransport transport = FSSTransport.removeInstance(execEnv);
        if (transport != null) {
            transport.shutdown();
        }
    }

    private static RemoteFileSystemTransport getInstanceFast(ExecutionEnvironment execEnv) {
        RemoteFileSystemTransport transport = FSSTransport.getInstance(execEnv);
        if (transport == null || ! transport.isValidFast()) {
            transport = SftpTransport.getInstance(execEnv);
        }
        return transport;
    }

    private static RemoteFileSystemTransport getInstanceSlow(ExecutionEnvironment execEnv)
            throws ConnectException, InterruptedException {
        RemoteFileSystemTransport transport = FSSTransport.getInstance(execEnv);
        if (transport == null || ! transport.isValidSlow()) {
            transport = SftpTransport.getInstance(execEnv);
        }
        return transport;
    }

    protected abstract boolean canCopy(String from, String to);

    protected abstract DirEntryList copy(String from, String to, 
            Collection<IOException> subdirectoryExceptions)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException;

    protected abstract boolean canMove(String from, String to);

    protected abstract MoveInfo move(String from, String to)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException;

    protected abstract DirEntry stat(String path)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException;

    // TODO: should only versions with timeout be kept?
    // Should other methods have a timeout parameter? (or probably there should be a static default?)
    // It seems to be right idea, but it needs to many changes and we are too close to thr release

    protected abstract DirEntry stat(String path, int timeoutMillis)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException;

    protected abstract DirEntry lstat(String path)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException;

    protected abstract DirEntry lstat(String path, int timeoutMillis)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException;

    protected abstract DirEntryList readDirectory(String path) 
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException;

    /**
     * Fast validity check - returns true if no problem (yet?) occurred
     * (for example, fs_server might be just not yet started).
     * For reliable and slow check, call checkValid()
     */
    protected abstract boolean isValidFast();

    /**
     * Slow validity check - includes launching (if needed) of remote tools, etc.
     * It can be slow on first call within a session or after reconncet.
     * It should be fast in other cases.
     *
     * It should throw one of declared exceptions
     * if the check is not possible (for example, the connection to host is lost).
     * Such exception will be rethrown to client.
     * Otherwise it should just return true or false.
     */
    protected abstract boolean isValidSlow()
            throws ConnectException, InterruptedException;

    protected abstract boolean needsClientSidePollingRefresh();

    protected abstract boolean canRefreshFast();
    
    protected abstract void refreshFast(String path, boolean expected)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException;
    
    protected abstract void registerDirectoryImpl(RemoteDirectory directory);

    protected abstract void unregisterDirectoryImpl(String path);

    protected abstract void scheduleRefresh(Collection<String> paths);
    
    protected abstract DirEntry uploadAndRename(File srcFile, String pathToUpload, String pathToRename)
            throws TimeoutException, ConnectException, IOException, InterruptedException, ExecutionException, InterruptedException;

    protected abstract boolean canSetAccessCheckType();

    protected abstract boolean canDeleteOnDisconnect();

    protected abstract void deleteOnDisconnect(String[] paths) 
            throws IOException, InterruptedException, ExecutionException;

    protected abstract void setAccessCheckType(FileSystemProvider.AccessCheckType accessCheckType);

    /** can be null */
    protected abstract FileSystemProvider.AccessCheckType getAccessCheckType();

    /** 
     * Deletes the file, returns parent directory content.
     * Returning parent directory content is for the sake of optimization.
     * For example, fs_server, can do remove and return refreshed content in one call.
     * It can return null if there is no way of doing that more effective than
     * just calling RemoteFileSystemTransport.readDirectory
     * @return parent directory content (can be null - see above)
     */
    protected abstract DirEntryList delete(String path, boolean directory) throws TimeoutException, ConnectException, IOException;

    protected void onConnect() {
    }

    protected void onFocusGained() {
    }
    
    protected Warmup createWarmup(String path) {
        return null;
    }
    
    protected void shutdown() {        
    }
}
