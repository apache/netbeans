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
package org.netbeans.modules.cnd.toolchain.ui.impl;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.cnd.spi.toolchain.CSMInitializationTaskRunner;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.api.ConnectionNotifier;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = CSMInitializationTaskRunner.class, position = 100)
public class CSMIntializationTaskRunnerImpl extends CSMInitializationTaskRunner {

    private final List<Runnable> tasks = new ArrayList<Runnable>();

    @Override
    public void runTask(ExecutionEnvironment executionEnvironment, boolean isComplete, NamedRunnable task) {
        if (executionEnvironment.isLocal() || isComplete) {
            task.run();
        } else {
            tasks.add(task);
            final ServerRecord record = ServerList.get(executionEnvironment);
            if (record.isOffline()) {
                ConnectionNotifier.addTask(executionEnvironment, new ConnectionNotifier.NamedRunnable(task.getName()) {
                    @Override
                    protected void runImpl() {
                        record.checkSetupAfterConnection(new Runnable() {
                            @Override
                            public void run() {
                                ToolsCacheManager cacheManager = ToolsCacheManager.createInstance(true);
                                CompilerSetManager csm = cacheManager.getCompilerSetManagerCopy(record.getExecutionEnvironment(), false);
                                csm.initialize(false, true, null);
                                cacheManager.applyChanges();
                            }
                        });
                    }
                });

            }
        }
    }

    @Override
    public void runTasks() {
        for (Runnable task : tasks) {
            task.run();
        }
        tasks.clear();
    }
}

