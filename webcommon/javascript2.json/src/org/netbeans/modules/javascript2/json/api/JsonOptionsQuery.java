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
package org.netbeans.modules.javascript2.json.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javascript2.json.spi.JsonOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
public class JsonOptionsQuery {

    private JsonOptionsQuery() {
        throw new IllegalStateException("No instance allowed"); //NOI18N
    }

    @NonNull
    public static Result getOptions(@NonNull final FileObject file) {
        Parameters.notNull("file", file);
        final Deque<JsonOptionsQueryImplementation.Result> results = new ArrayDeque<>();
        for (JsonOptionsQueryImplementation impl : Lookup.getDefault().lookupAll(JsonOptionsQueryImplementation.class)) {
            final JsonOptionsQueryImplementation.Result res = impl.getOptions(file);
            if (res != null) {
                results.offer(res);
            }
        }
        return new Result(results);
    }

    public static final class Result {

        public static final String PROP_COMMENT_SUPPORTED = "commentSupported"; //NOI18N
        private final Collection<? extends JsonOptionsQueryImplementation.Result> delegates;
        private final PropertyChangeSupport listeners;
        private final PropertyChangeListener pcl;
        private final AtomicBoolean listens = new AtomicBoolean();

        private Result(@NonNull final Collection<? extends JsonOptionsQueryImplementation.Result> delegates) {
            Parameters.notNull("delegates", delegates); //NOI18N
            this.delegates = delegates;
            this.listeners = new PropertyChangeSupport(this);
            this.pcl = (evt) -> listeners.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }

        public boolean isCommentSupported() {
            for (JsonOptionsQueryImplementation.Result delegate : delegates) {
                final Boolean res = delegate.isCommentSupported();
                if (res != null) {
                    return res;
                }
            }
            return false;
        }

        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            if (!listens.get() && listens.compareAndSet(false, true)) {
                this.delegates
                    .forEach((r) -> r.addPropertyChangeListener(WeakListeners.propertyChange(pcl, r)));
            }
            listeners.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            listeners.removePropertyChangeListener(listener);
        }
    }
}
