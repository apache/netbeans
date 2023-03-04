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
package org.netbeans.modules.parsing.spi.indexing;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.indexing.SuspendSupport;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.openide.util.Parameters;

/**
 * A service for indexers to check if an indexing is suspended or
 * to park while indexing is suspended.
 * The indexing is suspended by the infrastructure when high priority request
 * like index query or execution of {@link UserTask}, {@link ParserResultTask}
 * is in progress.
 * The instance of the {@link SuspendStatus} can be obtained from
 * {@link Context#getSuspendStatus()}. The infrastructure suspends indexing
 * automatically. The {@link EmbeddingIndexer}s are suspended with a file granularity.
 * The {@link CustomIndexer}s are suspended with a source root granularity.
 * This service can be used for more fine granularity suspending especially for
 * {@link CustomIndexer}s.
 * @author Tomas Zezula
 * @since 1.52
 */
public final class SuspendStatus {
    
    private final SuspendSupport.SuspendStatusImpl impl;
    
    SuspendStatus(@NonNull final SuspendSupport.SuspendStatusImpl impl) {
        Parameters.notNull("impl", impl);   //NOI18N
        this.impl = impl;
    }

    /**
     * Checks if a indexing task supports suspend.
     * @return true if suspend is supported by the active indexing task.
     * @since 1.78
     */
    public boolean isSuspendSupported() {
        return impl.isSuspendSupported();
    }

    /**
     * Checks if an indexing is suspended.
     * @return true if an indexing is suspended.
     */
    public boolean isSuspended() {
        return impl.isSuspended();
    }
    
    /**
     * Parks a current (caller) thread while an indexing is suspended.
     * Threading: The caller should not hold any locks when calling this method.
     * @throws InterruptedException if the caller is interrupted.
     */
    public void parkWhileSuspended() throws InterruptedException {
        impl.parkWhileSuspended();
    }
}
