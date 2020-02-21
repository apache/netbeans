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

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.impl.fs.HangupEnvList;
import org.netbeans.modules.remote.impl.fs.RemoteFileSystemUtils;
import org.openide.util.NotImplementedException;

/**
 *
 */
/*package*/ final class FSSResponse {

    public static class Package {

        private final FSSResponseKind kind;
        private final String data;

        public Package(FSSResponseKind kind, String data) {
            this.kind = kind;
            this.data = data;
        }

        public FSSResponseKind getKind() {
            return kind;
        }

        public String getData() {
            return data;
        }

        public Buffer getBuffer() {
            return new Buffer(data);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + '[' + kind + ' ' + data + ']';
        }
    }

    public interface Listener {
        void packageAdded(FSSResponse.Package pkg);
    }
    
    private final int requestId;
    private final FSSRequestKind requestKind;
    private final String requestPath;
    private final Disposer<FSSResponse> disposer;
    private final Listener listener;
    private final ExecutionEnvironment env;
    
    private final Object lock = new Object();
    private final LinkedList<Package> packages = new LinkedList<>();
    private ExecutionException exception = null;
    
    public FSSResponse(FSSRequest request, Disposer<FSSResponse> disposer, Listener listener, ExecutionEnvironment env) {
        this.requestId = request.getId();
        this.requestKind = request.getKind();
        this.requestPath = request.getPath();
        this.disposer = disposer;
        this.listener = listener;
        this.env = env;
    }
    
    public boolean hasPackages() {
        synchronized (lock) {
            return ! packages.isEmpty();
        }
    }

    public int getId() {
        return requestId;
    }

    /**
     * @return next package or null in the case timeout occurred
     */
    public Package getNextPackage() throws TimeoutException, InterruptedException, ExecutionException {
        if (RemoteFileSystemUtils.isUnitTestMode()) {
            long timeout = Integer.getInteger("remote.fs_server.timeout", 30000); //NOI18N
            if (HangupEnvList.isHung(env)) {
                throw new IllegalStateException("Rejected: timeout on previous attempt get package from " + env); //NOI18N
            }
            try {
                return getNextPackage(timeout);
            } catch (TimeoutException ex) {
                HangupEnvList.setHung(env);
                final String errText = "Timeout: can't get package at " + env + " for request #" + requestId + // NOI18N
                        ' ' + requestKind.getChar() + ' ' + requestPath + " in " + timeout + " ms"; // NOI18N
                FSSDispatcher.getInstance(env).testDump(System.err); //NOI18N
                throw new IllegalStateException(errText); //NOI18N
            }
        }
        return getNextPackage(0);
    }

    /**
     * @param timeToWait zero means not "forever", but "default timeout"
     * @return next package or null in the case timeout occurred
     */
    public Package getNextPackage(long timeToWait) throws TimeoutException, InterruptedException, ExecutionException {
        if (timeToWait == 0) {
            timeToWait = FSSTransport.DEFAULT_TIMEOUT;
        }
        long timeElapsed = 0;
        while (true) {
            synchronized (lock) {
                if (exception != null) {
                    throw exception;
                }
                if (packages.isEmpty()) {
                    if (timeToWait == 0) {
                        lock.wait();
                    } else {
                        long timeout = timeToWait - timeElapsed;
                        if (timeout <= 0) {
                            throw createTimeoutException(timeToWait); //NOI18N
                        }
                        long curr = System.currentTimeMillis();
                        lock.wait(timeout);
                        timeElapsed += System.currentTimeMillis() - curr;
                    }
                } else {
                    return packages.pollFirst();
                }
            }
        }
    }

    private TimeoutException createTimeoutException(long timeoutMillis) {
        String message = String.format("Timeout %d ms getting responce for %s on %s:%s", //NOI18N
                timeoutMillis, requestKind, env, requestPath); //NOI18N
        return new TimeoutException(message);
    }

    public Package tryGetNextPackage() throws ExecutionException {
        while (true) {
            synchronized (lock) {
                if (exception != null) {
                    throw exception;
                }
                if (!packages.isEmpty()) {
                    return packages.pollFirst();
                }
            }
        }
    }
    
    public void addPackage(FSSResponseKind kind, String data) {
        Package pkg = new Package(kind, data);
        synchronized (lock) {
            packages.addLast(pkg);
            lock.notifyAll();
        }
        if (listener != null) {
            listener.packageAdded(pkg);
        }
    }

    public void failed(ExecutionException exception) {
        synchronized (lock) {
            this.exception = exception;
            lock.notifyAll();
        }
    }

    public void cancel() {
        new NotImplementedException().printStackTrace(System.err);
    }

    void dispose() {
        disposer.dispose(this);
    }

    @Override
    public String toString() {
        return  getClass().getSimpleName() + ' ' + requestKind + " #" + requestId + //NOI18N
                ' ' + requestPath + " pkg.count=" + packages.size(); //NOI18N
    }    
}
