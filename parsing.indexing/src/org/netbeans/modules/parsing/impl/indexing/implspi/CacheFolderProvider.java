/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
