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

package org.netbeans.modules.cnd.remote.sync;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.spi.remote.setup.support.RemoteSyncNotifier;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
/*package-local*/ class FullRemoteSyncWorker implements RemoteSyncWorker {

    private final ExecutionEnvironment executionEnvironment;
    private final FileSystem sourceFileSystem;
    private final PrintWriter out;
    
    private final PrintWriter err;
    private static final RequestProcessor RP = new RequestProcessor("FullRemoteSyncWoker", 1); // NOI18N
    
    public FullRemoteSyncWorker(ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, List<FSPath> files) {
        this.executionEnvironment = executionEnvironment;
        this.sourceFileSystem = (files.isEmpty()) ? FileSystemProvider.getFileSystem(ExecutionEnvironmentFactory.getLocal()) : files.get(0).getFileSystem();
        this.out = out;
        this.err = err;
    }

    @Override
    public boolean startup(Map<String, String> env2add) {

        if (SyncUtils.isDoubleRemote(executionEnvironment, sourceFileSystem)) {
            RemoteSyncNotifier.getInstance().warnDoubleRemote(executionEnvironment, sourceFileSystem);
            return false;
        }

        try {
            // call FileSystemProvider.waitWrites
            // and print "waiting..." message in the case it does not return within half a second

            final String hostName = RemoteUtil.getDisplayName(executionEnvironment);
            final Object lock = new Object();
            final AtomicReference<Boolean> waiting = new AtomicReference<>(true);
            if (out != null) {
                RequestProcessor.Task task = RP.create(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (lock) {
                            if (waiting.get().booleanValue()) {                                
                                out.println(NbBundle.getMessage(getClass(), "FULL_Synchronizing_Message", hostName));
                            }
                        }
                    }
                });
                task.schedule(500);
            }
            List<String> failedList = new ArrayList<>();
            boolean written = FileSystemProvider.waitWrites(executionEnvironment, failedList);
            synchronized (lock) {
                waiting.set(false);
            }
            if( written ) {
                return true;
            } else {
                if (err != null) {
                    StringBuilder failedText = new StringBuilder();
                    for (String file : failedList) {
                        if (failedText.length() > 0) {
                            failedText.append('\n');
                        }
                        failedText.append(file);
                    }
                    err.println(NbBundle.getMessage(getClass(), "FULL_Failed_Message", hostName, failedText.toString()));
                }
                return false;
            }
        } catch (InterruptedException ex) {
            // don't report InterruptedException
            return false;
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean cancel() {
        return false;
    }
}
