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

package org.netbeans.modules.versioning.indexingbridge;

import java.io.File;
import java.util.concurrent.Callable;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.versioning.util.IndexingBridge.IndexingBridgeProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author vita
 */
@ServiceProvider(service=IndexingBridgeProvider.class)
public final class Bridge implements IndexingBridgeProvider {

    public <T> T runWithoutIndexing(final Callable<T> operation, final File... files) throws Exception {
        return IndexingManager.getDefault().runProtected(new Callable<T>() {
            public T call() throws Exception {
                // Schedule the refresh task, which will then absorb all other tasks generated
                // by filesystem events caused by the operation
                IndexingManager.getDefault().refreshAllIndices(false, false, files);
                return operation.call();
            }
        });
    }

    public boolean isIndexingInProgress() {
        return IndexingManager.getDefault().isIndexing();
    }
}
