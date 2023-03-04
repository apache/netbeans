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

package org.netbeans.modules.j2ee.metadata.model.api;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;

/**
 *
 * @author Andrei Badea
 */
public class SimpleMetadataModelImpl<T> implements MetadataModelImplementation<T> {

    private final boolean ready;
    
    public SimpleMetadataModelImpl() {
        this(true);
    }

    public SimpleMetadataModelImpl(boolean ready) {
        this.ready = ready;
    }

    public <R> R runReadAction(MetadataModelAction<T, R> action) throws IOException {
        try {
            return action.run(null);
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            } else {
                throw new MetadataModelException(t);
            }
        }
    }

    public boolean isReady() {
        return ready;
    }

    public <R> Future<R> runReadActionWhenReady(MetadataModelAction<T, R> action) throws IOException {
        if (ready) {
            try {
                return new SimpleFuture<R>(action.run(null), null);
            } catch (Throwable t) {
                if (t instanceof RuntimeException) {
                    throw (RuntimeException)t;
                } else {
                    throw new MetadataModelException(t);
                }
            }
        } else {
            R result = null;
            ExecutionException executionException = null;
            try {
                result = action.run(null);
            } catch (Throwable t) {
                executionException = new ExecutionException(t);
            }
            return new SimpleFuture<R>(result, executionException);
        }
    }

    private static final class SimpleFuture<R> implements Future<R> {

        private final R result;
        private final ExecutionException executionException;

        public SimpleFuture(R result, ExecutionException executionException) {
            this.result = result;
            this.executionException = executionException;
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        public boolean isCancelled() {
            return false;
        }

        public boolean isDone() {
            return true;
        }

        public R get() throws InterruptedException, ExecutionException {
            if (executionException != null) {
                throw executionException;
            }
            return result;
        }

        public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }
}
