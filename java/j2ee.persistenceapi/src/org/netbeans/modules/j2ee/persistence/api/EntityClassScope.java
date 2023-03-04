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

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeImplementation;
import org.netbeans.modules.j2ee.persistence.spi.EntityClassScopeProvider;
import org.netbeans.modules.j2ee.persistenceapi.EntityClassScopeAccessor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Describes an entity class scope, which is basically a bunch of related
 * entity classes on a classpath.
 *
 * @author Andrei Badea
 * @since 1.3
 */
public final class EntityClassScope {

    private static final Lookup.Result<EntityClassScopeProvider> providers =
            Lookup.getDefault().lookupResult(EntityClassScopeProvider.class);

    private final EntityClassScopeImplementation impl;

    static {
        EntityClassScopeAccessor.DEFAULT = new EntityClassScopeAccessor() {
            public EntityClassScope createEntityClassScope(EntityClassScopeImplementation impl) {
                return new EntityClassScope(impl);
            }
        };
    }

    /**
     * Returns the entity class scope for the given file (the entity classes
     * surrounding the given file).
     *
     * @param  fo the file for which to find the entity class scope; cannot be null.
     *
     * @return the entity class scope for the given file or null if there is no
     *         entity class scope.
     *
     * @throws NullPointerException if the fo parameter was null.
     */
    public static EntityClassScope getEntityClassScope(FileObject fo) {
        Parameters.notNull("fo", fo); // NOI18N
        for (EntityClassScopeProvider provider : providers.allInstances()) {
            EntityClassScope entityClassScope = provider.findEntityClassScope(fo);
            if (entityClassScope != null) {
                return entityClassScope;
            }
        }
        return null;
    }

    private EntityClassScope(EntityClassScopeImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the metadata model describing the entity classes in this 
     * entity class scope.
     * 
     * @param  withDeps <code>true</code> if the returned model needs to contain
     *         both the entity classes defined in Java sources and those defined
     *         on the compilation classpath of those sources, <code>false</code>
     *         if the model should only contain the entity classes defined
     *         in Java sources.
     * 
     * @return an entity class model; never null.
     */
    public MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(boolean withDeps) {
        return impl.getEntityMappingsModel(withDeps);
    }
}
