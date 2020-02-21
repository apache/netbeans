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
package org.netbeans.modules.cnd.modelimpl.cache.impl;

import java.lang.ref.WeakReference;
import org.netbeans.modules.cnd.api.model.CsmValidable;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 */
public final class WeakContainer<T> {

    private volatile WeakReference<T> weakContainer = TraceFlags.USE_WEAK_MEMORY_CACHE ? new WeakReference<T>(null) : null;
    private int preventMultiplyDiagnosticExceptionsSorage = 0;
    private final CsmValidable stateOwner;
    private final Key storageKey;

    public WeakContainer(CsmValidable stateOwner, Key storageKey) {
        assert storageKey != null;
        this.stateOwner = stateOwner;
        this.storageKey = storageKey;
    }

    public void clear() {
        synchronized (this) {
            if (TraceFlags.USE_WEAK_MEMORY_CACHE) {
                weakContainer.clear();
            }
        }
    }

    public Key getKey() {
        return storageKey;
    }
    
    @SuppressWarnings("unchecked")
    public T getContainer() {
        T container = getFromRef();
        if (container != null) {
            return container;
        }
        synchronized (this) {
            container = getFromRef();
            if (container != null) {
                return container;
            }
            container = (T) RepositoryUtils.get(storageKey);
            if (container == null && stateOwner.isValid() && preventMultiplyDiagnosticExceptionsSorage < DiagnosticExceptoins.LimitMultiplyDiagnosticExceptions) {
                T container2 = (T) RepositoryUtils.get(storageKey);
                String postfix = ""; // NOI18N
                if (CndUtils.isDebugMode() || CndUtils.isUnitTestMode()) {
                    if (container2 == null) {
                        postfix = " TWICE";// NOI18N
                    } else {
                        postfix = " second attempt OK";// NOI18N
                    }
                }
                DiagnosticExceptoins.registerIllegalRepositoryStateException("Failed to get container sorage by key " + postfix, storageKey); // NOI18N
                preventMultiplyDiagnosticExceptionsSorage++;
//            } else{
//                System.err.printf("OK %s\n", storageKey);
            }
            if (TraceFlags.USE_WEAK_MEMORY_CACHE && container != null && weakContainer != null) {
//                WeakReference<T> weak = new WeakReference<T>(container);
//                // assign only when object is completely created
                weakContainer = new WeakReference<>(container);
            }
            return container;
        }
    }

    private T getFromRef() {
        if (TraceFlags.USE_WEAK_MEMORY_CACHE && stateOwner.isValid()) {
            WeakReference<T> weak = weakContainer;
            if (weak != null) {
                return weak.get();
            }
        }
        return null;
    }
}
