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

package org.netbeans.modules.j2ee.persistence.api;

import java.beans.PropertyChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesImplementation;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesProvider;
import org.netbeans.modules.j2ee.persistenceapi.PersistenceScopesAccessor;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Describes a list of persistence scopes and allows listening on this list.
 *
 * @author Andrei Badea
 */
public final class PersistenceScopes {

    /**
     * The property corresponding to {@link #getPersistenceScopes}.
     */
    public static final String PROP_PERSISTENCE_SCOPES = "persistenceScopes"; // NOI18N

    private final PersistenceScopesImplementation impl;

    static {
        PersistenceScopesAccessor.DEFAULT = new PersistenceScopesAccessor() {
            public PersistenceScopes createPersistenceScopes(PersistenceScopesImplementation impl) {
                return new PersistenceScopes(impl);
            }
        };
    }

    /**
     * Returns an instance of <code>PersistenceScopes</code> for the given
     * project.
     *
     * @return an instance of <code>PersistenceScopes</code> or null if the
     *         project doesn't provide a list of persistence scopes.
     * @throws NullPointerException if <code>project</code> was null.
     */
    public static PersistenceScopes getPersistenceScopes(Project project) {
        Parameters.notNull("project", project); // NOI18N
        PersistenceScopesProvider provider = project.getLookup().lookup(PersistenceScopesProvider.class);
        if (provider != null) {
            return provider.getPersistenceScopes();
        }
        return null;
    }

    /**
     * Returns an instance of <code>PersistenceScopes</code> for the given
     * project's root.
     *
     * @return an instance of <code>PersistenceScopes</code> or null if the
     *         project doesn't provide a list of persistence scopes.
     * @throws NullPointerException if <code>project</code> was null.
     * @since 1.37
     */
    public static PersistenceScopes getPersistenceScopes(Project project, FileObject fo) {
        Parameters.notNull("project", project); // NOI18N
        PersistenceScopesProvider provider = project.getLookup().lookup(PersistenceScopesProvider.class);
        if (provider != null) {
            return provider.getPersistenceScopes(fo);
        }
        return null;
    }

    private PersistenceScopes(PersistenceScopesImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the persistence scopes contained in this instance.
     *
     * @return an array of <code>PersistenceScope</code> instances; never null.
     */
    public PersistenceScope[] getPersistenceScopes() {
        return impl.getPersistenceScopes();
    }

    /**
     * Adds a property change listener, allowing to listen on properties, e.g.
     * {@link #PROP_PERSISTENCE_SCOPES}.
     *
     * @param  listener the listener to add; can be null.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        impl.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener.
     *
     * @param  listener the listener to remove; can be null.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        impl.removePropertyChangeListener(listener);
    }
}
