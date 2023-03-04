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
package org.netbeans.modules.parsing.impl.indexing.implspi;

import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Indexing context provider.
 * Finds a user context for source root or source files indexing.
 * @since 9.2
 * @author Tomas Zezula
 */
public abstract class ContextProvider {

    private static final AtomicReference<Lookup.Result<ContextProvider>> impls = new AtomicReference<>();

    /**
     * Returns the context for given {@link FileObject} indexing.
     * @param file the {@link FileObject} to get indexing context for
     * @return the context represented by {@link Lookup}
     */
    @NonNull
    public static Lookup getContext(@NonNull FileObject file) {
        Parameters.notNull("file", file);   //NOI18N
        for (ContextProvider cp : getImpls()) {
            final Lookup res = cp.findContext(file);
            if (res != null) {
                return res;
            }
        }
        throw new IllegalStateException("Missing DefaultContextProvider");  //NOI18N
    }

    /**
     * Returns the context for given {@link URL} indexing.
     * @param url the {@link URL} to get indexing context for
     * @return the context represented by {@link Lookup}
     */
    @NonNull
    public static Lookup getContext(@NonNull URL url) {
        Parameters.notNull("url", url); //NOI18N
        for (ContextProvider cp : getImpls()) {
            final Lookup res = cp.findContext(url);
            if (res != null) {
                return res;
            }
        }
        throw new IllegalStateException("Missing DefaultContextProvider");  //NOI18N
    }

    /**
     * Finds the context for given {@link FileObject} indexing.
     * @param file the {@link FileObject} to find indexing context for
     * @return the context represented by {@link Lookup} or null
     */
    @CheckForNull
    protected abstract Lookup findContext(@NonNull FileObject file);

    /**
     * Finds the context for given {@link URL} indexing.
     * @param url the {@link URL} to find indexing context for
     * @return the context represented by {@link Lookup} or null
     */
    @CheckForNull
    protected abstract Lookup findContext(@NonNull URL url);

    @NonNull
    private static Iterable<? extends ContextProvider> getImpls() {
        Lookup.Result<ContextProvider> res = impls.get();
        if (res == null) {
            final Lookup lkp = new ProxyLookup(
                Lookup.getDefault(),
                Lookups.singleton(new DefaultContextProvider()));
            res = lkp.lookupResult(ContextProvider.class);
            if (!impls.compareAndSet(null, res)) {
                res = impls.get();
            }
        }
        return res.allInstances();
    }

    private static final class DefaultContextProvider extends ContextProvider {

        @Override
        @CheckForNull
        protected Lookup findContext(@NonNull final FileObject file) {
            return Lookup.getDefault();
        }

        @Override
        @CheckForNull
        protected Lookup findContext(@NonNull final URL url) {
            return Lookup.getDefault();
        }
    }
}
