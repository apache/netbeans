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
package org.netbeans.modules.javascript.cdnjs;

import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;

/**
 * Defines an object which listens for changes in CDNJS libraries.
 */
public interface LibraryListener extends EventListener {

    /**
     * This method is called when libraries in a project change.
     * @param project project which libraries has changed
     */
    void librariesChanged(@NonNull Project project);

    //~ Inner classes

    /**
     * Support class for working with {@link LibraryListener}.
     * <p>
     * This class is thread safe.
     */
    final class Support {

        private final List<LibraryListener> listeners = new CopyOnWriteArrayList<>();


        /**
         * Adds a {@link LibraryListener} to the listener list. The same
         * listener object may be added more than once, and will be called
         * as many times as it is added. If {@code listener} is {@code null},
         * no exception is thrown and no action is taken.
         * @param listener the {@link LibraryListener} to be added, can be {@code null}
         */
        public void addLibraryListener(@NullAllowed LibraryListener listener) {
            if (listener == null) {
                return;
            }
            listeners.add(listener);
        }

        /**
         * Removes a {@link LibraryListener} from the listener list.
         * If {@code listener} was added more than once,
         * it will be notified one less time after being removed.
         * If {@code listener} is {@code null}, or was never added, no exception is
         * thrown and no action is taken.
         * @param listener the {@link LibraryListener} to be removed, can be {@code null}
         */
        public void removeLibraryListener(@NullAllowed LibraryListener listener) {
            if (listener == null) {
                return;
            }
            listeners.remove(listener);
        }

        /**
         * Fires an event in libraries of the given project.
         * @param project project which libraries have changed
         */
        public void fireLibrariesChanged(@NonNull Project project) {
            assert project != null;
            for (LibraryListener listener : listeners) {
                listener.librariesChanged(project);
            }
        }

        /**
         * Checks if there are any listeners registered to this {@code Support}.
         * @return {@code true} if there are one or more listeners, {@code false} otherwise.
         */
        public boolean hasListeners() {
            return !listeners.isEmpty();
        }

    }

}
