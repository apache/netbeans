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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import org.netbeans.modules.parsing.impl.indexing.lucene.DocumentBasedIndexManager;
import org.netbeans.modules.parsing.impl.indexing.lucene.LuceneIndexFactory;
import org.openide.modules.OnStart;
import org.openide.modules.OnStop;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sdedic
 */
public class IndexingModule {
    private static final String STOP_HOOKS_PATH = "Parsing/Indexing/Stop";   //NOI18N
    private static volatile boolean closed;

    /**
     * Initialization part of the former IndexerModule / ModuleInstall
     *
     * @author sdedic
     */
    @OnStart
    public static class Startup implements Runnable {
        @Override
        public void run() {
            RepositoryUpdater.getDefault().start(false);
        }
    }

    @OnStop
    public static class Shutdown implements Runnable, Callable<Boolean> {

        @Override
        public void run() {
            closed = true;
            final Runnable postTask = new Runnable() {
                @Override
                public void run() {
                    callStopHooks();
                    LuceneIndexFactory.getDefault().close();
                    DocumentBasedIndexManager.getDefault().close();
                }
            };
            try {
                RepositoryUpdater.getDefault().stop(postTask);
            } catch (TimeoutException | IllegalStateException e) {
                //Timeout or already closed
                postTask.run();
            }
        }

        @Override
        public Boolean call() throws Exception {
            LogContext.notifyClosing();
            return true;
        }
    }

    public static boolean isClosed() {
        return closed;
    }

    private static void callStopHooks() {
        for (Runnable r : Lookups.forPath(STOP_HOOKS_PATH).lookupAll(Runnable.class)) {
            try {
                r.run();
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath) t;
                } else {
                    Exceptions.printStackTrace(t);
                }
            }
        }

    }

}
