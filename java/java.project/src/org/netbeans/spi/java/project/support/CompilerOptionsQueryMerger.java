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
