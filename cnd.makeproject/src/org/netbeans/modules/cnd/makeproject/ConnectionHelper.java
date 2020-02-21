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
package org.netbeans.modules.cnd.makeproject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.openide.util.RequestProcessor;

/**
 *
 */
public enum ConnectionHelper implements ConnectionListener {
    INSTANCE;
    
    private final RequestProcessor RP = new RequestProcessor("Check connection", 4); // NOI18N
    private final Set<ExecutionEnvironment> storage = new HashSet<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    private ConnectionHelper(){
        ConnectionManager.getInstance().addConnectionListener(this);
    }
    
    /**
     * Post connection task if host does not connected yet.
     * 
     * @param env 
     */
    public void ensureConnection(final ExecutionEnvironment env) {
        if (env != null && env.isRemote()) {
            ReadLock readLock = lock.readLock();
            try {
                readLock.lock();
                if (storage.contains(env)) {
                    return;
                }
            } finally {
                readLock.unlock();
            }
            WriteLock writeLock = lock.writeLock();
            try {
                writeLock.lock();
                if (storage.contains(env)) {
                    return;
                }
                storage.add(env);
                RP.post(() -> {
                    try {
                        ConnectionManager.getInstance().connectTo(env);
                        ServerRecord rec = ServerList.get(env);
                        if (rec != null) {
                            rec.checkSetupAfterConnection(null);
                        }
                    } catch (IOException ex) {
                        
                    } catch (CancellationException ex) {
                        // don't log CancellationException
                    }
                });
            } finally {
                writeLock.unlock();
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="not public implementation">
    @Override
    public void connected(ExecutionEnvironment env) {
        if (env != null && env.isRemote()) {
            WriteLock writeLock = lock.writeLock();
            try {
                writeLock.lock();
                storage.add(env);
            } finally {
                writeLock.unlock();
            }
        }
    }
    
    @Override
    public void disconnected(ExecutionEnvironment env) {
        if (env != null && env.isRemote()) {
            WriteLock writeLock = lock.writeLock();
            try {
                writeLock.lock();
                storage.remove(env);
            } finally {
                writeLock.unlock();
            }
        }
    }
    //</editor-fold>
}
