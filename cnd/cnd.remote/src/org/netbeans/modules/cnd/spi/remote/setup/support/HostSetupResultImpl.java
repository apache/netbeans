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
package org.netbeans.modules.cnd.spi.remote.setup.support;

import org.netbeans.modules.cnd.spi.remote.setup.*;
import org.netbeans.modules.cnd.spi.remote.*;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * A trivial HostSetupWorker.Result implementation
 */
public class HostSetupResultImpl implements HostSetupWorker.Result {

    private ExecutionEnvironment executionEnvironment;
    private String displayName;
    private RemoteSyncFactory syncFactory;
    private Runnable runOnFinish;
    private static ExecutionEnvironment lastExecutionEnvironment;

    public HostSetupResultImpl() {
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public ExecutionEnvironment getExecutionEnvironment() {
        return executionEnvironment;
    }


    @Override
    public RemoteSyncFactory getSyncFactory() {
        return syncFactory;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("ST")
    public void setExecutionEnvironment(ExecutionEnvironment executionEnvironment) {
        this.executionEnvironment = lastExecutionEnvironment = executionEnvironment;
    }

    public static ExecutionEnvironment getLastExecutionEnvironment() {
        return lastExecutionEnvironment;
    }

    public void setSyncFactory(RemoteSyncFactory syncFactory) {
        this.syncFactory = syncFactory;
    }

    @Override
    public Runnable getRunOnFinish() {
        return runOnFinish;
    }

    public void setRunOnFinish(Runnable runOnFinish) {
        this.runOnFinish = runOnFinish;
    }
}
