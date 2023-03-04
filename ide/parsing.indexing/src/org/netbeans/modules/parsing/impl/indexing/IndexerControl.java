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

package org.netbeans.modules.parsing.impl.indexing;

import org.netbeans.modules.parsing.impl.IndexerBridge;
import org.netbeans.modules.parsing.lucene.spi.ScanSuspendImplementation;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author sdedic
 */
@ServiceProviders({
    @ServiceProvider(service = ScanSuspendImplementation.class),
    @ServiceProvider(service = IndexerBridge.class)
})
public class IndexerControl implements ScanSuspendImplementation, IndexerBridge{

    @Override
    public void suspend() {
        RepositoryUpdater.getDefault().suspend();
    }

    @Override
    public void resume() {
        RepositoryUpdater.getDefault().resume();
    }

    @Override
    public boolean isIndexing() {
        // need to go through IndexingUtils, so test can set a temporary state
        return !IndexingUtils.getIndexingState().isEmpty();
    }

    @Override
    public boolean ownsProtectedMode() {
        return RepositoryUpdater.getDefault().isProtectedModeOwner(Thread.currentThread()); 
    }
}
