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
package org.netbeans.spi.java.platform;

import java.io.IOException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * A factory for a new {@link JavaPlatform}.
 * @since 1.41
 * @author Tomas Zezula
 */
public interface JavaPlatformFactory {
    /**
     * Creates a new {@link JavaPlatform} located in given folder.
     * @param installFolder the platform folder
     * @param name the name of the newly created platform
     * @param persistent if true the platform is registered in the{@link JavaPlatformManager}
     * @return a newly created {@link JavaPlatform}
     * @throws IOException 
     */
    @NonNull
    JavaPlatform create(@NonNull FileObject installFolder, @NonNull String name, boolean persistent) throws IOException;
    /**
     * Provider of the {@link JavaPlatformFactory}.
     * The provider should be registered in the default {@link Lookup}.
     */
    interface Provider {
        /**
         * Returns a {@link JavaPlatformFactory} for given platform type.
         * @param platformType the required platformType
         * @return a {@link JavaPlatformFactory} or null for unhandled platform type.
         */
        @CheckForNull
        JavaPlatformFactory forType(@NonNull final String platformType);
    }
}
