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
package org.netbeans.modules.java.classpath;

import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.spi.java.classpath.GlobalPathRegistryImplementation;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public abstract class SPIAccessor {
    private static volatile SPIAccessor _instance;

    public static void setInstance(@NonNull final SPIAccessor instance) {
        Parameters.notNull("instance", instance);   //NOI18N
        _instance = instance;
    }

    @NonNull
    public static SPIAccessor getInstance() {
        SPIAccessor res = _instance;
        if (res == null) {
            try {
                Class.forName(
                    GlobalPathRegistryImplementation.class.getName(),
                    true,
                    SPIAccessor.class.getClassLoader());
                res = _instance;
                assert res != null;
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return res;
    }

    @NonNull
    public abstract Set<ClassPath> getPaths(
        @NonNull GlobalPathRegistryImplementation impl,
        @NonNull String id);


    @NonNull
    public abstract Set<ClassPath> register(
        @NonNull GlobalPathRegistryImplementation impl,
        @NonNull String id,
        @NonNull ClassPath[] paths);


    @NonNull
    public abstract Set<ClassPath> unregister(
        @NonNull GlobalPathRegistryImplementation impl,
        @NonNull String id,
        @NonNull ClassPath[] paths) throws IllegalArgumentException;


    @NonNull
    public abstract Set<ClassPath> clear(@NonNull GlobalPathRegistryImplementation impl);

    public abstract void attachAPI(
        @NonNull GlobalPathRegistryImplementation impl,
        @NonNull GlobalPathRegistry api);
}
