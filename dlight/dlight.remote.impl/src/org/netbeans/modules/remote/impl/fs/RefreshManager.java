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

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class RefreshManager {

    private final ExecutionEnvironment env;
    private final RemoteFileObjectFactory factory;
    private final RequestProcessor.Task updateTask;

    /** one the task was scheduled, this should be true */
    private volatile long updateTaskScheduled = 0;

    private final LinkedList<String> queue = new LinkedList<>();
    private final Set<String> set = new HashSet<>();
    private final Object queueLock = new Object();

    private static final boolean REFRESH_ON_FOCUS = RemoteFileSystemUtils.getBoolean("cnd.remote.refresh.on.focus", true); //NOI18N
    public static final boolean REFRESH_ON_CONNECT = RemoteFileSystemUtils.getBoolean("cnd.remote.refresh.on.connect", true); //NOI18N
    private static final int REPORT_LONG_REFRESH = Integer.getInteger("cnd.remote.report.long.refresh", 5000); //NOI18N

    private final class RefreshWorker implements Runnable {
        private final boolean expected;
        private RefreshWorker(boolean expected) {
            this.expected = expected;
        }
        @Override
        public void run() {
            long time = System.currentTimeMillis();
            int cnt = 0;
                while (true) {
                    RemoteFileObjectBase fo;
                    String path;
                    synchronized (queueLock) {
                        path = queue.poll();
                        if (path == null) {
                            break;
                        }
                        set.remove(path);
                        fo = factory.getCachedFileObject(path);
                        cnt++;
                    }
                    if (fo == null) {
                        RemoteLogger.finest("RefreshManager: skipping dead file object {0} @ {1}", path, env);
                        continue;
                    }
                    String oldThreadName = Thread.currentThread().getName();
                    Thread.currentThread().setName("Remote File System RefreshManager: refreshing " + fo); //NOI18N
                    long time2 = System.currentTimeMillis();
                    try {
                        fo.refreshImpl(false, null, expected, RemoteFileObjectBase.RefreshMode.DEFAULT);
                    } catch (ConnectException ex) {
                        clear();
                        break;
                    } catch (InterruptedException ex) {
                        RemoteLogger.finest(ex, fo);
                        break;
                    } catch (IOException ex) {
                        ex.printStackTrace(System.err);
                    } catch (ExecutionException ex) {
                        if (!permissionDenied(ex)) {
                        System.err.println("Exception on file "+fo.getPath());
                            ex.printStackTrace(System.err);
                        }
                    } finally {
                        Thread.currentThread().setName(oldThreadName);
                        time2 = System.currentTimeMillis() - time2;
                        if (time2 > REPORT_LONG_REFRESH) {
                            RemoteLogger.getInstance().log(Level.WARNING, "Refreshing {0} took {1} ms\n", new Object[]{fo, time2});
                        }
                    }
                }
                time = System.currentTimeMillis() - time;
                if (cnt > 0) {
                    RemoteLogger.getInstance().log(Level.FINEST, "RefreshManager: refreshing {0} directories took {1} ms on {2}", new Object[] {cnt, time, env});
                }
            }
        }

    private boolean permissionDenied(ExecutionException e) {
        Throwable ex = e;
        while (ex != null) {
            if (ex instanceof FileInfoProvider.SftpIOException) {
                switch(((FileInfoProvider.SftpIOException)ex).getId()) {
                    case FileInfoProvider.SftpIOException.SSH_FX_PERMISSION_DENIED:
                        return true;
                }
                break;
            }
            ex = ex.getCause();
        }
        return false;
    }


    private void clear() {
        synchronized (queueLock) {
            queue.clear();
            set.clear();
        }
    }

    public RefreshManager(ExecutionEnvironment env, RemoteFileObjectFactory factory) {
        this.env = env;
        this.factory = factory;
        updateTask = new RequestProcessor("Remote File System RefreshManager " + env.getDisplayName(), 1).create(new RefreshWorker(false)); //NOI18N
    }

    public void scheduleRefreshOnFocusGained() {
        if (REFRESH_ON_FOCUS) {
            if (RemoteFileSystemTransport.needsClientSidePollingRefresh(env)) {
                Collection<RemoteFileObjectBase> fileObjects = factory.getCachedFileObjects();
                RemoteLogger.getInstance().log(Level.FINE, "Refresh on focus gained schedulled for {0} directories on {1}", new Object[]{fileObjects.size(), env});
                scheduleRefreshImpl(filterDirectories(fileObjects), false);
            } else {
                RemoteFileSystemTransport.onFocusGained(env);
            }
        }
    }

    public void scheduleRefreshOnConnect() {
        if (REFRESH_ON_CONNECT && RemoteFileSystemTransport.needsClientSidePollingRefresh(env)) {
            Collection<RemoteFileObjectBase> fileObjects = factory.getCachedFileObjects();
            RemoteLogger.getInstance().log(Level.FINE, "Refresh on connect schedulled for {0} directories on {1}", new Object[]{fileObjects.size(), env});
            scheduleRefreshImpl(filterDirectories(fileObjects), false);
        } else {
            RemoteFileSystemTransport.onConnect(env);
        }
    }

    private Collection<RemoteFileObjectBase> filterDirectories(Collection<RemoteFileObjectBase> fileObjects) {
        Collection<RemoteFileObjectBase> result = new TreeSet<>(new PathComparator(true));
        for (RemoteFileObjectBase fo : fileObjects) {
            // Don't call isValid() or isFolder() - they might be SLOW!
            if (isDirectory(fo)) {
                result.add(fo);
            }
        }
        return result;
    }

    private static boolean isDirectory(RemoteFileObjectBase fo) {
        return fo != null && (/*(fo instanceof RemoteLinkBase) || */ (fo instanceof RemoteDirectory));
    }

    private static class PathComparator implements Comparator<RemoteFileObjectBase>  {
        private final boolean childrenFirst;
        public PathComparator(boolean childrenFirst) {
            this.childrenFirst = childrenFirst;
        }
        @Override
        public int compare(RemoteFileObjectBase o1, RemoteFileObjectBase o2) {
            int result = o1.getPath().compareTo(o2.getPath());
            return childrenFirst ? -result : result;
        }
    }

    public void scheduleRefreshExistent(Collection<String> paths, boolean addExistingChildren) {
        Collection<RemoteFileObjectBase> fileObjects = new ArrayList<>(paths.size());
        for (String path : paths) {
            RemoteFileObjectBase fo = factory.getCachedFileObject(path);
            if (fo != null) {
                fileObjects.add(fo);
            }
        }
        scheduleRefresh(fileObjects, addExistingChildren);
        }

    public void scheduleRefresh(Collection<RemoteFileObjectBase> fileObjects, boolean addExistingChildren) {
        if (addExistingChildren) {
            Collection<RemoteFileObjectBase> toRefresh = new TreeSet<>(new PathComparator(false));
            for (RemoteFileObjectBase fo : fileObjects) {
                addExistingChildren(fo, toRefresh);
            }
            scheduleRefreshImpl(toRefresh, true);
        } else {
            scheduleRefreshImpl(fileObjects, true);
        }
    }

    public void removeFromRefresh(String path) {
        synchronized (queueLock) {
            if (set.contains(path)) {
                set.remove(path);
                queue.remove(path);
            }
        }
    }

    private void addExistingChildren(RemoteFileObjectBase fo, Collection<RemoteFileObjectBase> bag) {
        if (isDirectory(fo)) {
            bag.add(fo);
            for (RemoteFileObjectBase child : fo.getExistentChildren()) {
                addExistingChildren(child, bag);
            }
        }
    }

    private void scheduleRefreshImpl(Collection<RemoteFileObjectBase> fileObjects, boolean toTheHead) {
        if ( ! ConnectionManager.getInstance().isConnectedTo(env)) {
            RemoteLogger.getInstance().warning("scheduleRefresh(Collection<FileObject>) is called while host is not connected");
        }
        if (fileObjects.isEmpty()) {
            return;
        }
        synchronized (queueLock) {
            for (RemoteFileObjectBase fo : fileObjects) {
                String path = fo.getPath();
                if (set.contains(path)) {
                    queue.remove(path);
                } else {
                    set.add(path);
                }
                queue.add(toTheHead ? 0 : queue.size(), path);
            }
        }
        updateTask.schedule(0);
        updateTaskScheduled = System.currentTimeMillis();
    }

    @SuppressWarnings("SleepWhileInLoop")
    /*package*/ void testWaitLastRefreshFinished(long time) {
        if (updateTaskScheduled < time) {
            // sleep up to 10 seconds, awaking each 0.1 second and checking whether update was scheduled
            for (int i = 0; i < 100 && updateTaskScheduled < time; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
        }
        if (updateTaskScheduled > 0) {
            updateTask.waitFinished();
        }
    }
}
