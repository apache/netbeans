/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.parsing.nb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.spi.editor.document.EditorMimeTypesImplementation;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service = EditorMimeTypesImplementation.class)
public final class EditorMimeTypesImpl implements EditorMimeTypesImplementation {

    private final EditorSettings es;
    private final PropertyChangeSupport listeners;
    private final PropertyChangeListener listener;

    public EditorMimeTypesImpl() {
        this.es = EditorSettings.getDefault();
        this.listeners = new PropertyChangeSupport(this);
        this.listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(@NonNull final PropertyChangeEvent evt) {
                if (evt.getPropertyName() == null || EditorSettings.PROP_MIME_TYPES.equals(evt.getPropertyName())) {
                    listeners.firePropertyChange(PROP_SUPPORTED_MIME_TYPES, null, null);
                }
            }
        };
        this.es.addPropertyChangeListener(WeakListeners.propertyChange(listener, this.es));
    }

    @Override
    public Set<String> getSupportedMimeTypes() {
        return es.getAllMimeTypes();
    }

    @Override
    public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        listeners.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        listeners.removePropertyChangeListener(listener);
    }
}
