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
package org.netbeans.modules.java.api.common.classpath;

import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.util.Parameters;

/**
 * The base class for implementation of {@link ClassPathProvider}.
 * @author Tomas Zezula
 * @since 1.99
 */
public abstract class AbstractClassPathProvider implements ClassPathProvider {
    private final List<ClassPathsChangeListener> listeners;

    /**
     * The event describing a change of project's {@link ClassPath} set.
     */
    public static final class ClassPathsChangeEvent extends EventObject {
        private final Collection<? extends String> classPathTypes;

        private ClassPathsChangeEvent(
                @NonNull final AbstractClassPathProvider source,
                @NonNull final Collection<? extends String> classPathTypes) {
            super(source);
            this.classPathTypes = classPathTypes;
        }

        /**
         * Returns the types of changed {@link ClassPath} sets.
         * @return the ids of changed {@link ClassPath}s.
         */
        @NonNull
        public Collection<? extends String> getChangedClassPathTypes() {
            return this.classPathTypes;
        }
    }

    /**
     * Event listener interface for being notified of changes in the set of
     * projects paths.
     */
    public static interface ClassPathsChangeListener extends EventListener {
        /**
         * Called when project's {@link ClassPath} set changes.
         * @param event the event describing the change.
         */
        void classPathsChange(@NonNull final ClassPathsChangeEvent event);
    }

    /**
     * Initializes the {@link AbstractClassPathProvider}.
     */
    protected AbstractClassPathProvider() {
        this.listeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Returns {@link ClassPath}s of given type owned by the project.
     * @param type the {@link ClassPath} type
     * @return the project's {@link ClassPath}s
     */
    @CheckForNull
    public abstract ClassPath[] getProjectClassPaths(@NonNull final String type);

    /**
     * Returns the names of path properties storing the path type for given {@link SourceGroup}.
     * @param sg the {@link SourceGroup} to return the properties for
     * @param type the {@link ClassPath} type
     * @return the property names or null when the type is not supported or given {@link SourceGroup} is unknown.
     * @since 1.113
     */
    @CheckForNull
    public abstract String[] getPropertyName (@NonNull SourceGroup sg, @NonNull String type);

    /**
     * Adds the {@link ClassPathsChangeListener}.
     * The {@link ClassPathsChangeListener} is notified when a set of project {@link ClassPath}s is changed.
     * @param listener the listener to add.
     */
    public final void addClassPathsChangeListener(@NonNull final ClassPathsChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.listeners.add(listener);
    }

    /**
     * Removes the {@link ClassPathsChangeListener}.
     * @param listener the listener to remove.
     */
    public final void removeClassPathsChangeListener(@NonNull final ClassPathsChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.listeners.remove(listener);
    }

    /**
     * Fires the project's {@link ClassPath}s change.
     * @param changedClassPathTypes the changed {@link ClassPath} types
     */
    protected final void fireClassPathsChange(@NonNull final Collection<? extends String> changedClassPathTypes) {
        Parameters.notNull("changedClassPathTypes", changedClassPathTypes); //NOI18N
        if (listeners.isEmpty()) {
            return;
        }
        final ClassPathsChangeEvent evt = new ClassPathsChangeEvent(this, Collections.unmodifiableCollection(changedClassPathTypes));
        for (ClassPathsChangeListener l : listeners) {
            l.classPathsChange(evt);
        }
    }
}
