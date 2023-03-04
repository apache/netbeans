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

package org.netbeans.modules.php.dbgp;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;

/**
 * @author Radek Matous
 */
class BackendLauncher {
    private final Callable<Cancellable> launcher;
    Future<Cancellable> futureCancellable;

    BackendLauncher(Callable<Cancellable> launcher) {
        this.launcher = launcher;
    }

    synchronized void launch() {
        if (futureCancellable == null && launcher != null) {
            futureCancellable = Executors.newSingleThreadExecutor().submit(launcher);
        }
    }

    synchronized void stop() {
        if (futureCancellable != null) {
            try {
                Cancellable cancellable = futureCancellable.get();
                if (cancellable != null) {
                    cancellable.cancel();
                }
            } catch (InterruptedException | ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
