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
package org.netbeans.modules.java.source.base;

import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.openide.modules.OnStop;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
public final class Module {

    private static final String STOP_HOOKS_PATH = "Parsing/Indexing/Stop";   //NOI18N
    private static volatile boolean closed;

    private Module() {
        throw new IllegalStateException("No instance Allowed");
    }

    @OnStop
    public static final class ModuleStop implements Runnable {
        @Override
        public void run() {
            close();
        }
    }

    @ServiceProvider(service = Runnable.class, path = STOP_HOOKS_PATH)
    public static final class IndexingStop implements Runnable {
        @Override
        public void run() {
            close();
            ClassIndexManager.getDefault().close();
        }
    }


    public static boolean isClosed() {
        return closed;
    }

    private static void close() {
        closed = true;
    }
}
