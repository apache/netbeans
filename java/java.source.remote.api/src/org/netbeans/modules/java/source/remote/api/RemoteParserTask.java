/**
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
package org.netbeans.modules.java.source.remote.api;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author lahvac
 */
public interface RemoteParserTask<R, I extends CompilationInfo, P> {

    public abstract Future<R> computeResult(I info, P additionalParams) throws IOException;

    public static final class SynchronousFuture<R> implements Future<R> {
        private final ExceptionSupplier<R> compute;
        private final Runnable cancel;
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final AtomicBoolean done = new AtomicBoolean();

        public SynchronousFuture(ExceptionSupplier<R> compute, Runnable cancel) {
            this.compute = compute;
            this.cancel = cancel;
        }
        
        @Override
        public boolean cancel(boolean arg0) {
            cancel.run();
            cancelled.set(true);
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }

        @Override
        public boolean isDone() {
            return done.get();
        }

        @Override
        public R get() throws InterruptedException, ExecutionException {
            try {
                return compute.get();
            } catch (Exception ex) {
                throw new ExecutionException(ex);
            } finally {
                done.set(true);
            }
        }

        @Override
        public R get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public interface ExceptionSupplier<R> {
        public R get() throws Exception;
    }
}
