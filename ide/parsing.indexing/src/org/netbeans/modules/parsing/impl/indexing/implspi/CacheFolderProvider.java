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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.impl.indexing.DefaultCacheFolderProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Tomas Zezula
 */
public abstract class CacheFolderProvider {

    private static final AtomicReference<Lookup.Result<CacheFolderProvider>> impls =
            new AtomicReference<>();

    public static enum Kind {
        SOURCES,
        LIBRARIES,
        BINARIES
    }

    public static enum Mode {
        EXISTENT,
        CREATE
    }

    protected CacheFolderProvider() {}

    @CheckForNull
    protected abstract FileObject findCacheFolderForRoot(@NonNull URL root, @NonNull Set<Kind> kinds, @NonNull Mode mode) throws IOException;

    @CheckForNull
    protected abstract URL findRootForCacheFolder(@NonNull FileObject cacheFolder)  throws IOException;

    protected abstract void collectRootsInFolder(@NonNull URL folder, Collection<? super URL> collector) throws IOException;

    @CheckForNull
    public static FileObject getCacheFolderForRoot(
            @NonNull final URL root,
            @NonNull final Set<Kind> kinds,
            @NonNull final Mode mode) throws IOException {
        assert root != null;
        assert kinds != null;
        assert mode != null;
        for (CacheFolderProvider impl : getImpls()) {
            final FileObject result = impl.findCacheFolderForRoot(root, kinds, mode);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @CheckForNull
    public static URL getRootForCacheFolder(@NonNull final FileObject cacheFolder) throws IOException {
        assert cacheFolder != null;
        for (CacheFolderProvider impl : getImpls()) {
            final URL result = impl.findRootForCacheFolder(cacheFolder);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @NonNull
    public static Iterable<? extends URL> getRootsInFolder(@NonNull final URL folder) throws IOException {
        assert folder != null;
        final Collection<URL> resCollector = new ArrayDeque<>();
        for (CacheFolderProvider impl : getImpls()) {
            impl.collectRootsInFolder(folder, resCollector);
        }
        return resCollector;
    }

    @NonNull
    private static Collection<? extends CacheFolderProvider> getImpls() {
        Lookup.Result<CacheFolderProvider> res = impls.get();
        if (res == null) {
            final Lookup lkp = new ProxyLookup(
                // FIXME: the default Lookup instance changes between users; quick fix is to delegate
                // to a dynamic proxy lookup which always delegates to the current default Lookup instance.
                // Proper fix is to probably cache a weak(defaultLookup) -> Lookup.Result map - performance
                // of the lookup.
                Lookups.proxy(new Lookup.Provider() {
                    @Override
                    public Lookup getLookup() {
                        return Lookup.getDefault();
                    }
                }),
                Lookups.singleton(DefaultCacheFolderProvider.getInstance()));
            res = lkp.lookupResult(CacheFolderProvider.class);
            if (!impls.compareAndSet(null, res)) {
                res = impls.get();
            }
        }
        return res.allInstances();
    }
}
