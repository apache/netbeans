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
package org.netbeans.api.editor.document;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.editor.document.EditorMimeTypesImplementation;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * The editor mime types.
 * @since 1.1
 * @author Tomas Zezula
 */
public final class EditorMimeTypes {

    /**
     * The name of the "supportedMimeTypes" property.
     */
    public static final String PROP_SUPPORTED_MIME_TYPES = "supportedMimeTypes";    //NOI18N

    //@GuardedBy("EditorMimeTypes.class")
    private static EditorMimeTypes instance;

    private final EditorMimeTypesImplementation impl;
    private final PropertyChangeSupport listeners;
    private final PropertyChangeListener listener;

    private EditorMimeTypes() {
        impl = Lookup.getDefault().lookup(EditorMimeTypesImplementation.class);
        if (impl == null) {
            throw new IllegalStateException(String.format(
                "No %s instance in the default Lookup.",    //NOI18N
                EditorMimeTypesImplementation.class));
        }
        listeners = new PropertyChangeSupport(this);
        listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (EditorMimeTypesImplementation.PROP_SUPPORTED_MIME_TYPES.equals(evt.getPropertyName())) {
                    listeners.firePropertyChange(PROP_SUPPORTED_MIME_TYPES, null, null);
                }
            }
        };
        impl.addPropertyChangeListener(WeakListeners.propertyChange(listener, impl));
    }

    /**
     * Returns a set of the supported mime types.
     * @return the supported mime types.
     */
    @NonNull
    public Set<String> getSupportedMimeTypes() {
        return impl.getSupportedMimeTypes();
    }

    /**
     * Adds a {@link PropertyChangeListener}.
     * @param listener the listener to be added.
     */
    public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.listeners.addPropertyChangeListener(listener);
    }

    /**
     * Removes a {@link PropertyChangeListener}.
     * @param listener the listener to be removed.
     */
    public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.listeners.removePropertyChangeListener(listener);
    }

    /**
     * Returns a {@link EditorMimeTypes} instance.
     * @return the {@link EditorMimeTypes} instance
     */
    @NonNull
    public static synchronized EditorMimeTypes getDefault() {
        if (instance == null) {
            instance = new EditorMimeTypes();
        }
        return instance;
    }
}
