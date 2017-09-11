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
