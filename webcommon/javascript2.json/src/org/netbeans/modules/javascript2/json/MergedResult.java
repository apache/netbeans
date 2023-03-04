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
import java.util.Arrays;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javascript2.json.spi.JsonOptionsQueryImplementation;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Merges {@link JsonOptionsQueryImplementation.Result}.
 * @author Tomas Zezula
 */
final class MergedResult implements JsonOptionsQueryImplementation.Result, PropertyChangeListener {

    private final JsonOptionsQueryImplementation.Result[] delegates;
    private final PropertyChangeSupport listeners;

    MergedResult(@NonNull final JsonOptionsQueryImplementation.Result... delegates) {
        Parameters.notNull("delegates", delegates); //NOI18N
        this.delegates = Arrays.copyOf(delegates, delegates.length);
        this.listeners = new PropertyChangeSupport(this);
        Arrays.stream(this.delegates)
                .forEach((r) -> r.addPropertyChangeListener(WeakListeners.propertyChange(this, r)));
    }

    @CheckForNull
    @Override
    public Boolean isCommentSupported() {
        for (JsonOptionsQueryImplementation.Result delegate : delegates) {
            final Boolean res = delegate.isCommentSupported();
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    @Override
    public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(@NonNull final PropertyChangeEvent evt) {
        this.listeners.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }
}
