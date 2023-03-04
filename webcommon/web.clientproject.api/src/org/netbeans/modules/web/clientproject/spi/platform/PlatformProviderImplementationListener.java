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
package org.netbeans.modules.web.clientproject.spi.platform;

import java.beans.PropertyChangeEvent;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.openide.util.Parameters;

/**
 * Defines an object which listens for changes in platform provider.
 * @since 1.68
 */
public interface PlatformProviderImplementationListener extends EventListener {

    /**
     * This method is called when property of the given platform provider changes.
     * @param project project which properties has changed, can be {@code null} if the property is not project specific
     * @param platformProvider platform provider which properties has changed
     * @param event information about property change
     */
    void propertyChanged(@NullAllowed Project project, @NonNull PlatformProviderImplementation platformProvider, @NonNull PropertyChangeEvent event);

    /**
     * Support class for working with {@link PlatformProviderImplementationListener}.
     * <p>
     * This class is thread safe.
     */
    final class Support {

        private final List<PlatformProviderImplementationListener> listeners = new CopyOnWriteArrayList<>();

        /**
         * Add a {@code PlatformProviderImplementationsListener} to the listener list. The same
         * listener object may be added more than once, and will be called
         * as many times as it is added. If {@code listener} is {@code null},
         * no exception is thrown and no action is taken.
         * @param listener the {@code PlatformProviderImplementationsListener} to be added, can be {@code null}
         */
        public void addPlatformProviderImplementationsListener(@NullAllowed PlatformProviderImplementationListener listener) {
            if (listener == null) {
                return;
            }
            listeners.add(listener);
        }

        /**
         * Removes a {@code PlatformProviderImplementationsListener} from the listener list.
         * If {@code listener} was added more than once,
         * it will be notified one less time after being removed.
         * If {@code listener} is {@code null}, or was never added, no exception is
         * thrown and no action is taken.
         * @param listener the {@code PlatformProviderImplementationListener} to be removed, can be {@code null}
         */
        public void removePlatformProviderImplementationsListener(@NullAllowed PlatformProviderImplementationListener listener) {
            if (listener == null) {
                return;
            }
            listeners.remove(listener);
        }

        /**
         * Fire an event in property of the given platform provider.
         * @param project project which properties has changed, can be {@code null} if the property is not project specific
         * @param platformProvider platform provider which properties has changed
         * @param event information about property change
         */
        public void firePropertyChanged(@NullAllowed Project project, @NonNull PlatformProviderImplementation platformProvider, @NonNull PropertyChangeEvent event) {
            Parameters.notNull("platformProvider", platformProvider); // NOI18N
            Parameters.notNull("event", event); // NOI18N
            for (PlatformProviderImplementationListener listener : listeners) {
                listener.propertyChanged(project, platformProvider, event);
            }
        }

        /**
         * Check if there are any listeners registered to this {@code Support}.
         * @return {@code true} if there are one or more listeners, {@code false} otherwise.
         */
        public boolean hasListeners() {
            return !listeners.isEmpty();
        }

    }

}
