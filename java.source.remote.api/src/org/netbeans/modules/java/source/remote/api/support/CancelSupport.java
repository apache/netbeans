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
package org.netbeans.modules.java.source.remote.api.support;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author lahvac
 */
public final class CancelSupport {
    private final AtomicBoolean cancel = new AtomicBoolean();
    private final AtomicReference<Future<?>> future = new AtomicReference<>();

    public void cancel() {
        cancel.set(true);
        Future<?> f = future.get();
        if (f != null)
            f.cancel(true);
    }

    public void resume() {
        cancel.set(false);
        future.set(null);
    }

    public boolean isCancelled() {
        return cancel.get();
    }

    public AtomicBoolean getCancelBoolean() {
        return cancel;
    }

    public void setFuture(Future<?> f) {
        future.set(f);
        if (cancel.get() && f != null) {
            f.cancel(true);
        }
    }
}
