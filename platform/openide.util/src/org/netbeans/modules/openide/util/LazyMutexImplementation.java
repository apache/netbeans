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
package org.netbeans.modules.openide.util;

import java.util.concurrent.Callable;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Parameters;
import org.openide.util.spi.MutexImplementation;

/**
 *
 * @author Tomas Zezula
 */
public final class LazyMutexImplementation implements MutexImplementation {

    private final Callable<? extends MutexImplementation> provider;

    //@GuardedBy("this")
    private MutexImplementation impl;

    public LazyMutexImplementation(final Callable<? extends MutexImplementation> provider) {
        Parameters.notNull("provider", provider);   //NOI18N
        this.provider = provider;
    }

    @Override
    public boolean isReadAccess() {
        return getDelegate().isReadAccess();
    }

    @Override
    public boolean isWriteAccess() {
        return getDelegate().isWriteAccess();
    }

    @Override
    public void writeAccess(Runnable runnable) {
        getDelegate().writeAccess(runnable);
    }

    @Override
    public <T> T writeAccess(Mutex.ExceptionAction<T> action) throws MutexException {
        return getDelegate().writeAccess(action);
    }

    @Override
    public void readAccess(Runnable runnable) {
        getDelegate().readAccess(runnable);
    }

    @Override
    public <T> T readAccess(Mutex.ExceptionAction<T> action) throws MutexException {
        return getDelegate().readAccess(action);
    }

    @Override
    public void postReadRequest(Runnable run) {
        getDelegate().postReadRequest(run);
    }

    @Override
    public void postWriteRequest(Runnable run) {
        getDelegate().postWriteRequest(run);
    }

    private synchronized MutexImplementation getDelegate() {
        if (impl == null) {
            try {
                impl = provider.call();
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            assert impl != null;
        }
        return impl;
    }
}
