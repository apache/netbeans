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

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
class InjectedTasksSupport {

    private static final Queue<Callable<?>> tasks = new ConcurrentLinkedQueue<Callable<?>>();


    static void enqueueTask(@NonNull final Callable<?> task) {
        Parameters.notNull("task", task);   //NOI18N
        tasks.offer(task);
    }

    static void clear() {
        if (!TransientUpdateSupport.isTransientUpdate()) {
            tasks.clear();
        }
    }

    @CheckForNull
    static Callable<?> nextTask() {
        return TransientUpdateSupport.isTransientUpdate()?
            null:
            tasks.poll();
    }

    static void execute() throws IOException {
        for (Callable<?> task = nextTask(); task != null; task = nextTask()) {
            try {
                task.call();
            } catch (RuntimeException re) {
                throw re;
            } catch (IOException ioe) {
                throw ioe;
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

}
