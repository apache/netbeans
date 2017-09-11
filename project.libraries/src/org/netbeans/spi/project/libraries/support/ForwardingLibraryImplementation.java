/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
