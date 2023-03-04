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

package org.netbeans.modules.project.libraries.ui;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.project.libraries.ArealLibraryProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation2;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.spi.project.libraries.LibraryStorageArea;

/**
 *
 * @author Tomas Zezula
 */
class ALPUtils {

    private ALPUtils() {
        throw new IllegalStateException("Instance not allowed");    //NOI18N
    }

    /**
     * Type-safe accessor for {@link ArealLibraryProvider#getLibraries}.
     * @throws ClassCastException if the runtime types do not match
     */
    @NonNull
    static LibraryProvider getLibraries(
            @NonNull final ArealLibraryProvider alp,
            @NonNull final LibraryStorageArea area) {
        return getLibraries0((ArealLibraryProvider<?,?>) alp, area);
    }
    private static <A extends LibraryStorageArea> LibraryProvider getLibraries0(ArealLibraryProvider<A,?> alp, LibraryStorageArea area) {
        return alp.getLibraries(alp.areaType().cast(area));
    }

    /**
     * Type-safe accessor for {@link ArealLibraryProvider#remove}.
     * @throws ClassCastException if the runtime types do not match
     */
    static void remove(
            @NonNull final ArealLibraryProvider alp,
            @NonNull final LibraryImplementation2 lib) throws IOException {
        remove0((ArealLibraryProvider<?,?>) alp, lib);
    }
    private static <L extends LibraryImplementation2> void remove0(ArealLibraryProvider<?,L> alp, LibraryImplementation2 lib) throws IOException {
        alp.remove(alp.libraryType().cast(lib));
    }

    /**
     * Type-safe accessor for {@link ArealLibraryProvider#createLibrary}.
     * @throws ClassCastException if the runtime types do not match
     */
    @NonNull
    static LibraryImplementation2 createLibrary(
            @NonNull final ArealLibraryProvider alp,
            @NonNull final String type,
            @NonNull final String name,
            @NonNull final LibraryStorageArea area,
            @NonNull final Map<String,List<URI>> contents) throws IOException {
        return createLibrary0(((ArealLibraryProvider<?,?>) alp), type, name, area, contents);
    }
    private static <A extends LibraryStorageArea> LibraryImplementation2 createLibrary0(ArealLibraryProvider<A,?> alp, String type, String name, LibraryStorageArea area, Map<String,List<URI>> contents) throws IOException {
        return alp.createLibrary(type, name, alp.areaType().cast(area), contents);
    }
}
