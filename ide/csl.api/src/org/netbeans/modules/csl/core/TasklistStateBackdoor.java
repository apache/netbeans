/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.csl.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.util.NbBundle;

import static org.netbeans.modules.csl.core.Bundle.*;

/**
 * This class provides access to tasklist settings. The settings are only available
 * to push scanners, so this 'push scanner' is a singleton, which can be queried for 
 * settings, but really does not push any tasks to the tasklist. 
 * <p/>
 * Be aware that the scope can change to null at any time.
 * @author sdedic
 */
@NbBundle.Messages({
    "DN_tlIndexerName=Hints-based tasks",
    "DESC_tlIndexerName=Tasks provided by language hints"
})
class TasklistStateBackdoor extends PushTaskScanner {
    private static final TasklistStateBackdoor INSTANCE = new TasklistStateBackdoor();
    
    private volatile TaskScanningScope scope;
    private volatile Callback callback;
    private volatile boolean seenByTlIndexer = true;
    private boolean wasScanning;
    
    TasklistStateBackdoor() {
        super(DN_tlIndexerName(), DESC_tlIndexerName(), null);
    }
    
    static TasklistStateBackdoor getInstance() {
        return INSTANCE;
    }
    
    boolean isCurrentEditorScope() {
        Callback c = this.callback;
        seenByTlIndexer = true;
        return c != null && c.isCurrentEditorScope();
    }
    
    boolean isObserved() {
        Callback c = this.callback;
        seenByTlIndexer = true;
        return c != null && c.isObserved();
    }
    
    TaskScanningScope getScope() {
        return scope;
    }
    
    @Override
    public void setScope(TaskScanningScope scope, Callback callback) {
        this.callback = callback;
        if (scope == null) {
            // ignore; for example project switch when ctx menu is displayed
            // sets scope to null, then back to the project scope to force refresh/reload
            return;
        }
        synchronized (this) {
            boolean newScanning = !callback.isCurrentEditorScope();
            if (!callback.isObserved()) {
                scope = null;
                newScanning = false;
            }
            this.scope = scope;
            if (!callback.isObserved() || callback.isCurrentEditorScope() || 
                    !newScanning || wasScanning == newScanning || !seenByTlIndexer) {
                wasScanning = newScanning;
                seenByTlIndexer = false;
                return;
            }
            wasScanning = newScanning;
            seenByTlIndexer = false;
            IndexingManager.getDefault().refreshAllIndices(TLIndexerFactory.INDEXER_NAME);
        }
    }
}
