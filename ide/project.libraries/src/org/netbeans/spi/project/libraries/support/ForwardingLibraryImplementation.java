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

package org.netbeans.spi.project.libraries.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation2;
import org.netbeans.spi.project.libraries.LibraryImplementation3;
import org.netbeans.spi.project.libraries.NamedLibraryImplementation;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Forwarding {@link LibraryImplementation}.
 * When possible the implementation delegates to given {@link LibraryImplementation},
 * in case that the delegate does not support required contract it throws an {@link UnsupportedOperationException}.
 * @author Tomas Zezula
 * @since 1.48
 */
public class ForwardingLibraryImplementation implements LibraryImplementation,
        NamedLibraryImplementation, LibraryImplementation2, LibraryImplementation3 {

    private final LibraryImplementation delegate;
    private final PropertyChangeSupport support;
    private final PropertyChangeListener listener;

    /**
     * Creates a new {@link ForwardingLibraryImplementation}.
     * @param delegate the delegate
     */
    public ForwardingLibraryImplementation(@NonNull final LibraryImplementation delegate) {
        Parameters.notNull("delegate", delegate);   //NOI18N
        this.delegate = delegate;
        this.support = new PropertyChangeSupport(this);
        this.listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                ForwardingLibraryImplementation.this.support.firePropertyChange(
                    evt.getPropertyName(),
                    evt.getOldValue(),
                    evt.getNewValue());
            }
        };
        this.delegate.addPropertyChangeListener(WeakListeners.propertyChange(
            this.listener,
            this.delegate));
    }

    /**
     * Returns the delegate.
     * @return the delegate
     */
    @NonNull
    public final LibraryImplementation getDelegate() {
        return delegate;
    }

    protected final void firePropertyChange(
            @NonNull final String propName,
            @NullAllowed final Object oldValue,
            @NullAllowed final Object newValue) {
        support.firePropertyChange(propName, oldValue, newValue);
    }

    @Override
    public String getType() {
        return delegate.getType();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public String getLocalizingBundle() {
        return delegate.getLocalizingBundle();
    }

    @Override
    public List<URL> getContent(String volumeType) throws IllegalArgumentException {
        return delegate.getContent(volumeType);
    }

    @Override
    public void setName(String name) {
        delegate.setName(name);
    }

    @Override
    public void setDescription(String text) {
        delegate.setDescription(text);
    }

    @Override
    public void setLocalizingBundle(String resourceName) {
        delegate.setLocalizingBundle(resourceName);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }

    @Override
    public void setContent(String volumeType, List<URL> path) throws IllegalArgumentException {
        delegate.setContent(volumeType, path);
    }

    @Override
    public void setDisplayName(String displayName) {
        if (!LibrariesSupport.supportsDisplayName(delegate)) {
            throw new UnsupportedOperationException(String.format(
                "Delegate: %s does not support displayName",       //NOI18N
                delegate));
        }
        LibrariesSupport.setDisplayName(delegate, displayName);
    }

    @Override
    public String getDisplayName() {
        if (!LibrariesSupport.supportsDisplayName(delegate)) {
            throw new UnsupportedOperationException(String.format(
                "Delegate: %s does not support displayName",       //NOI18N
                delegate));
        }
        return LibrariesSupport.getDisplayName(delegate);
    }

    @Override
    public List<URI> getURIContent(String volumeType) throws IllegalArgumentException {
        if (!LibrariesSupport.supportsURIContent(delegate)) {
            throw new UnsupportedOperationException(String.format(
                "Delegate: %s does not support URI content",       //NOI18N
                delegate));
        }
        return LibrariesSupport.getURIContent(delegate, volumeType, LibrariesSupport.ConversionMode.FAIL);
    }

    @Override
    public void setURIContent(String volumeType, List<URI> path) throws IllegalArgumentException {
        if (!LibrariesSupport.supportsURIContent(delegate)) {
            throw new UnsupportedOperationException(String.format(
                "Delegate: %s does not support URI content",       //NOI18N
                delegate));
        }
        LibrariesSupport.setURIContent(delegate, volumeType, path, LibrariesSupport.ConversionMode.FAIL);
    }

    @Override
    public Map<String, String> getProperties() {
        if (!LibrariesSupport.supportsProperties(delegate)) {
            throw new UnsupportedOperationException(String.format(
                "Delegate: %s does not support properties",       //NOI18N
                delegate));
        }
        return LibrariesSupport.getProperties(delegate);
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        if (!LibrariesSupport.supportsProperties(delegate)) {
            throw new UnsupportedOperationException(String.format(
                "Delegate: %s does not support properties",       //NOI18N
                delegate));
        }
        LibrariesSupport.setProperties(delegate, properties);
    }

}
