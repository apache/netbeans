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
package org.netbeans.modules.javascript2.json;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript2.json.spi.JsonOptionsQueryImplementation;
import org.netbeans.modules.javascript2.json.spi.support.JsonPreferences;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * An implementation of the {@link JsonOptionsQueryImplementation} which delegates
 * to the {@link Project} lookup.
 * @author Tomas Zezula
 */
@ServiceProvider(service = JsonOptionsQueryImplementation.class, position = 1_000)
public final class ProjectJsonOptionsQueryImpl implements JsonOptionsQueryImplementation {

    private final Map</*@GuardedBy("normCache")*/Project,Reference<Result>> normCache;

    public ProjectJsonOptionsQueryImpl() {
        normCache = new WeakHashMap<>();
    }

    @CheckForNull
    @Override
    public Result getOptions(@NonNull final FileObject file) {
        final Project p = FileOwnerQuery.getOwner(file);
        if (p == null) {
            return null;
        }
        final JsonOptionsQueryImplementation impl = p.getLookup().lookup(JsonOptionsQueryImplementation.class);
        final Result overrideRes = impl == null ?
                null :
                impl.getOptions(file);
        final Result defaultRes = createDefaultResult(p);
        return overrideRes == null ?
                defaultRes :
                new MergedResult(overrideRes, defaultRes);
    }

    private Result createDefaultResult(@NonNull final Project p) {
        synchronized (normCache) {
            final Reference<Result> ref = normCache.get(p);
            Result res = ref == null ? null : ref.get();
            if (res == null) {
                res = new DefaultProjectResult(p);
                normCache.put(p, new WeakReference<>(res));
            }
            return res;
        }
    }

    private static final class DefaultProjectResult implements Result, PropertyChangeListener {
        private final JsonPreferences jsonPrefs;
        private final PropertyChangeSupport listeners;
        private final AtomicBoolean listens = new AtomicBoolean();

        DefaultProjectResult(@NonNull final Project project) {
            this.jsonPrefs = JsonPreferences.forProject(project);
            this.listeners = new PropertyChangeSupport(this);
        }

        @CheckForNull
        @Override
        public Boolean isCommentSupported() {
            return jsonPrefs.isCommentSupported() ?
                    Boolean.TRUE :
                    null;
        }

        @Override
        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            if (!listens.get() && listens.compareAndSet(false, true)) {
                jsonPrefs.addPropertyChangeListener(this);
            }
            listeners.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            listeners.removePropertyChangeListener(listener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            listeners.firePropertyChange(PROP_COMMENT_SUPPORTED, null, null);
        }
    }
}
