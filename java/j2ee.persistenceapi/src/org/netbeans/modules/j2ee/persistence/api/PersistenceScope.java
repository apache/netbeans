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

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopeProvider;
import org.netbeans.modules.j2ee.persistenceapi.PersistenceScopeAccessor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Describes a persistence scope. A persistence scope is composed of
 * a persistence.xml file and the classpath for this persistence.xml file (which
 * contains all entity classes and JAR files referenced by the persistence units
 * in the persistence.xml file).
 *
 * @author Andrei Badea
 */
public final class PersistenceScope {

    // XXX remove getClassPath(), not needed anymore

    private static final Lookup.Result<PersistenceScopeProvider> providers =
            Lookup.getDefault().lookupResult(PersistenceScopeProvider.class);

    private final PersistenceScopeImplementation impl;

    static {
        PersistenceScopeAccessor.DEFAULT = new PersistenceScopeAccessor() {
            public PersistenceScope createPersistenceScope(PersistenceScopeImplementation impl) {
                return new PersistenceScope(impl);
            }
        };
    }

    /**
     * Returns the persistence scope for the given file.
     *
     * @param  fo the file for which to find the persistence scope; cannot be null.
     *
     * @return the persistence scope for the given file or null if there is no
     *         persistence scope.
     *
     * @throws NullPointerException if the fo parameter was null.
     */
    public static PersistenceScope getPersistenceScope(FileObject fo) {
        Parameters.notNull("fo", fo); // NOI18N
        for (PersistenceScopeProvider provider : providers.allInstances()) {
            PersistenceScope persistenceScope = provider.findPersistenceScope(fo);
            if (persistenceScope != null) {
                return persistenceScope;
            }
        }
        return null;
    }

    private PersistenceScope(PersistenceScopeImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the persistence.xml file of this persistence scope.
     *
     * @return the persistence.xml file or null if it the persistence.xml file does
     * not exist.
     */
    public FileObject getPersistenceXml() {
        return impl.getPersistenceXml();
    }

    /**
     * Returns a model of entity classes for the specified persistence unit.
     * 
     * @param  persistenceUnitName the persistence unit name; cannot be null.
     * 
     * @return an entity class model or null if the given persistence scope does
     *         not exist.
     */
    public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(String persistenceUnitName) {
        return impl.getEntityMappingsModel(persistenceUnitName);
    }

    /**
     * Provides the classpath of this persistence scope, which covers the sources
     * of the entity classes referenced by the persistence.xml file, as well
     * as the referenced JAR files.
     *
     * @return the persistence scope classpath; never null.
     */
    public ClassPath getClassPath() {
        return impl.getClassPath();
    }
}
