/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.preprocessorbridge.api;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * Finds Compile On Save performer for given source root.
 * @author Tomas Zezula
 * @since 1.41
 */
public final class CompileOnSaveActionQuery {
    private static final Lookup.Result<CompileOnSaveAction.Provider> instances
            = Lookup.getDefault().lookupResult(CompileOnSaveAction.Provider.class);
    //Normalization Cache
    //@GuardedBy("u2a")
    private static final Map<URL,Reference<CompileOnSaveAction>> u2a = new WeakHashMap<>();
    //@GuardedBy("u2a")
    private static final Map<CompileOnSaveAction,URL> a2u = new WeakHashMap<>();

    private CompileOnSaveActionQuery() {
        throw new IllegalStateException("No instance allowed.");    //NOI18N
    }
    
    /**
     * Finds Compile On Save performer for given source root.
     * @param sourceRoot the source root to find the performer for.
     * @return the {@link CompileOnSaveAction} for performing compile on save or
     * null in case when the root is not recognized.
     */
    @CheckForNull
    public static CompileOnSaveAction getAction(@NonNull final URL sourceRoot) {
        CompileOnSaveAction res;
        synchronized (u2a) {
            final Reference<CompileOnSaveAction> ref = u2a.get(sourceRoot);
            res = ref != null ?
                    ref.get() :
                    null;
        }
        if (res == null) {
            final Collection<CompileOnSaveAction> actions = findAll(sourceRoot);
            res = actions.isEmpty() ?
                null :
                new ProxyAction(sourceRoot, actions, instances);
            synchronized (u2a) {
                final Reference<CompileOnSaveAction> ref = u2a.get(sourceRoot);
                CompileOnSaveAction tmpRes;
                if (ref == null || (tmpRes = ref.get()) == null) {
                    u2a.put(sourceRoot, new WeakReference<>(res));
                    a2u.put(res, sourceRoot);
                } else {
                    res = tmpRes;
                }
            }
        }        
        return res;
    }
    
    private static Collection<CompileOnSaveAction> findAll(URL root) {
        return instances.allInstances().stream()
                .map((p) -> p.forRoot(root))
                .filter((a) -> a != null)
                .collect(Collectors.toList());        
    }
    
    private static final class ProxyAction implements CompileOnSaveAction, LookupListener, ChangeListener {
        private static Predicate<CompileOnSaveAction> ALL = (a) -> true;
        private static Predicate<CompileOnSaveAction> ACTIVE = (a) -> a.isEnabled();
        private final URL root;
        private final AtomicReference<Collection<CompileOnSaveAction>> active;
        private final ChangeSupport listeners;
        
        ProxyAction(
                @NonNull final URL root,
                @NonNull final Collection<CompileOnSaveAction> current,
                @NonNull final Lookup.Result<CompileOnSaveAction.Provider> eventSource) {
            this.root = root;
            this.active = new AtomicReference<>(current);
            this.listeners = new ChangeSupport(this);
            instances.addLookupListener(WeakListeners.create(
                    LookupListener.class,
                    this,
                    instances));
            getActions(ALL)
                    .forEach((a) -> a.addChangeListener(WeakListeners.change(this, a)));
        }

        @Override
        public Boolean performAction(Context ctx) throws IOException {
            return getActions(ACTIVE)
                    .findFirst()
                    .map((a) -> {
                        try {
                            return a.performAction(ctx);
                        } catch (IOException ioe) {
                            return null;
                        }
                    })
                    .orElse(null);
        }
        
        public boolean isEnabled() {
            return getActions(ACTIVE)
                    .findAny()
                    .isPresent();
        }

        @Override
        public boolean isUpdateResources() {
            return getActions(ACTIVE)
                    .findFirst()
                    .map((a) -> a.isUpdateResources())
                    .orElse(Boolean.FALSE);
        }

        @Override
        public boolean isUpdateClasses() {
            return getActions(ACTIVE)
                    .findFirst()
                    .map((a) -> a.isUpdateClasses())
                    .orElse(Boolean.FALSE);
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener l) {
            this.listeners.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener l) {
            this.listeners.removeChangeListener(l);
        }

        @Override
        public void resultChanged(@NonNull final LookupEvent ev) {
            reset();
        }
        
        @Override
        public void stateChanged(ChangeEvent e) {
            reset();
        }
        
        private void reset() {
            this.active.set(null);
            listeners.fireChange();
        }
        
        @NonNull
        private Stream<CompileOnSaveAction> getActions(@NonNull final Predicate<CompileOnSaveAction> filter) {
            Collection<CompileOnSaveAction> res = this.active.get();
            if (res == null) {
                res = findAll(root);
                this.active.compareAndSet(null, res);
            }
            return res.stream().filter(filter);
        }        
    }
}
