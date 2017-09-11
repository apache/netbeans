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
