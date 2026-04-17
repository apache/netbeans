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

package org.netbeans.modules.maven.indexer;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.openide.util.Cancellable;

/**
 * Thrown when a task is canceled.
 * Cannot just be a {@link RuntimeException}, since {@link DefaultNexusIndexer#scan} would catch it.
 */
final class Cancellation extends Error {

    public Cancellation() {
        super("canceled");
    }

    private static final Set<Cancellable> cancellables = Collections.newSetFromMap(new WeakHashMap<>());

    public static synchronized void register(Cancellable c) {
        cancellables.add(c);
    }

    public static synchronized boolean cancelAll() {
        boolean ok = true;
        for (Cancellable c : cancellables) {
            boolean result = c.cancel();
            ok &= result;
        }
        return ok;
    }

}
