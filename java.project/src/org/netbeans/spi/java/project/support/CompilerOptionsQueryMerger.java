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
package org.netbeans.spi.java.project.support;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.project.LookupMerger;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
final class CompilerOptionsQueryMerger implements LookupMerger<CompilerOptionsQueryImplementation>{

    CompilerOptionsQueryMerger() {
    }

    @Override
    public Class<CompilerOptionsQueryImplementation> getMergeableClass() {
        return CompilerOptionsQueryImplementation.class;
    }

    @Override
    public CompilerOptionsQueryImplementation merge(Lookup lookup) {
        return new CompilerOptionsQueryImpl(lookup);
    }

    private static final class CompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {
        private final Lookup lookup;
        //@GuardedBy("cache")
        private final Map<FileObject, Reference<ResultImpl>> cache;

        CompilerOptionsQueryImpl(
                @NonNull final Lookup lookup) {
            Parameters.notNull("lookup", lookup);   //NOI18N
            this.lookup = lookup;
            this.cache = Collections.synchronizedMap(new WeakHashMap<FileObject, Reference<ResultImpl>>());
        }

        @CheckForNull
        @Override
        public Result getOptions(@NonNull final FileObject file) {
            final FileObject owner = Optional.ofNullable(ClassPath.getClassPath(file, ClassPath.SOURCE))
                    .map((cp) -> cp.findOwnerRoot(file))
                    .orElse(file);
            ResultImpl res;
            Reference<ResultImpl> resRef = cache.get(owner);
            if (resRef == null || (res = resRef.get()) == null) {
                res = new ResultImpl(owner, lookup);
                if (res.isEmpty()) {
                    return null;
                }
                synchronized (cache) {
                    ResultImpl prev;
                    final Reference<ResultImpl> prevRef = cache.get(owner);
                    if (prevRef == null || (prev = prevRef.get()) == null) {
                        cache.put(owner, new WeakReference<>(res));
                    } else {
                        res = prev;
                    }
                }
            }
            return res;
        }

        private static final class ResultImpl extends Result implements LookupListener, ChangeListener {
            private final FileObject artifact;
            private final Lookup.Result<CompilerOptionsQueryImplementation> providers;
            private final ChangeSupport listeners;
            //@GuardedBy("this")
            private volatile List<Pair<Result,ChangeListener>> currentResults;
            //@GuardedBy("this")
            private volatile List<String> currentArgs;

            ResultImpl(
                    @NonNull final FileObject artifact,
                    @NonNull final Lookup lookup) {
                this.artifact = artifact;
                this.providers = lookup.lookupResult(CompilerOptionsQueryImplementation.class);
                this.listeners = new ChangeSupport(this);
                this.providers.addLookupListener(WeakListeners.create(LookupListener.class, this, providers));
                checkProviders();
            }

            @Override
            public List<? extends String> getArguments() {
                return currentArgs;
            }

            @Override
            public void addChangeListener(@NonNull final ChangeListener listener) {
                listeners.addChangeListener(listener);
            }

            @Override
            public void removeChangeListener(@NonNull final ChangeListener listener) {
                listeners.removeChangeListener(listener);
            }

            @Override
            public void resultChanged(LookupEvent ev) {
                update();
            }

            @Override
            public void stateChanged(ChangeEvent e) {
                update();
            }

            boolean isEmpty() {
                final List<Pair<Result,ChangeListener>> l = currentResults;
                return l == null ? true : l.isEmpty();
            }
            
            private void update() {
                final Runnable resetAction = () -> {
                    checkProviders();
                    listeners.fireChange();
                };
                if (ProjectManager.mutex().isWriteAccess()) {
                    ProjectManager.mutex().postReadRequest(resetAction);
                } else {
                    resetAction.run();
                }
            }

            private void checkProviders() {
                synchronized (this) {
                    if (currentResults != null) {
                        for (Pair<Result,ChangeListener> res : currentResults) {
                            res.first().removeChangeListener(res.second());
                        }
                        currentResults = null;
                    }
                }
                final List<Pair<Result,ChangeListener>> newResults = providers.allInstances().stream()
                        .map((p) -> p.getOptions(artifact))
                        .filter((r) -> r != null)
                        .map((r) -> {
                            final ChangeListener cl = WeakListeners.change(this, r);
                            r.addChangeListener(cl);
                            return Pair.of(r,cl);
                            })
                        .collect(Collectors.toList());
                final List<String> newArgs = newResults.stream()
                        .flatMap((p) -> p.first().getArguments().stream())
                        .collect(Collectors.toList());
                synchronized (this) {
                    currentResults = newResults;
                    currentArgs = Collections.unmodifiableList(newArgs);
                }
            }
        }
    }
}
