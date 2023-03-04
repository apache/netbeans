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
package org.netbeans.spi.java.classpath;

import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.java.classpath.SPIAccessor;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * The SPI interface for the {@link GlobalPathRegistry}.
 * Allows different implementations of the {@link GlobalPathRegistry}.
 * The first SPI instance registered in the global {@link Lookup} is used
 * by the {@link GlobalPathRegistry}.
 * Threading: The implementations don't need to be thread safe,
 * synchronization is done by the {@link GlobalPathRegistry}.
 * @author Tomas Zezula
 * @since 1.48
 */
public abstract class GlobalPathRegistryImplementation {

    static {
        SPIAccessor.setInstance(new AccessorImpl());
    }

    private volatile GlobalPathRegistry owner;

    /**
     * Find all paths of a certain type.
     * @param id a classpath type, e.g. {@link ClassPath#SOURCE}
     * @return an immutable set of all registered {@link ClassPath}s of that type (may be empty but not null)
     */
    @NonNull
    protected abstract Set<ClassPath> getPaths(@NonNull String id);

    /**
     * Register some classpaths of a certain type.
     * @param id a classpath type, e.g. {@link ClassPath#SOURCE}
     * @param paths a list of classpaths to add to the registry
     * @return the added classpaths
     */
    @NonNull
    protected abstract Set<ClassPath> register(@NonNull String id, @NonNull ClassPath[] paths);

    /**
     * Unregister some classpaths of a certain type.
     * @param id a classpath type, e.g. {@link ClassPath#SOURCE}
     * @param paths a list of classpaths to remove from the registry
     * @return the removed classpaths
     * @throws IllegalArgumentException if they had not been registered before
     */
    @NonNull
    protected abstract Set<ClassPath> unregister(@NonNull String id, @NonNull ClassPath[] paths) throws IllegalArgumentException;

    /**
     * Removes all known classpaths.
     * @return a set of removed classpaths
     */
    @NonNull
    protected abstract Set<ClassPath> clear();

    private static final class AccessorImpl extends SPIAccessor {

        @Override
        @NonNull
        public Set<ClassPath> getPaths(
                @NonNull final GlobalPathRegistryImplementation impl,
                @NonNull final String id) {
            return impl.getPaths(id);
        }

        @Override
        @NonNull
        public Set<ClassPath> register(
                @NonNull final GlobalPathRegistryImplementation impl,
                @NonNull final String id,
                @NonNull final ClassPath[] paths) {
            return impl.register(id, paths);
        }

        @Override
        @NonNull
        public Set<ClassPath> unregister(
                @NonNull final GlobalPathRegistryImplementation impl,
                @NonNull final String id,
                @NonNull final ClassPath[] paths) throws IllegalArgumentException {
            return impl.unregister(id, paths);
        }

        @Override
        @NonNull
        public Set<ClassPath> clear(@NonNull final GlobalPathRegistryImplementation impl) {
            return impl.clear();
        }

        @Override
        @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
        public void attachAPI(
                @NonNull final GlobalPathRegistryImplementation impl,
                @NonNull final GlobalPathRegistry api) {
            Parameters.notNull("api", api); //NOI18N
            impl.owner = api;
        }
    }
}
