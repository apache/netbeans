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
package org.netbeans.modules.project.libraries;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.Lookup;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.openide.filesystems.FileObject;
import org.openide.modules.OnStart;

/**
 * Ensures that all {@link LibraryProvider}s are actually loaded.
 * Some of them may perform initialization actions, such as updating
 * $userdir/build.properties with concrete values of some library paths.
 * This needs to happen before any Ant build is run.
 * @author Tomas Zezula
 */
@OnStart
public class LibrariesModule implements Runnable {

    private static final Map<LibraryImplementation,FileObject> sources = Collections.synchronizedMap(
            new WeakHashMap<LibraryImplementation,FileObject>());

    @Override
    public void run() {
        for (LibraryProvider lp : Lookup.getDefault().lookupAll(LibraryProvider.class)) {            
            lp.getLibraries();
        }
    }

    public static void registerSource(
        final @NonNull LibraryImplementation impl,
        final @NonNull FileObject descriptorFile) {
        sources.put(impl, descriptorFile);
    }

    public static FileObject getFile(@NonNull final LibraryImplementation impl) {
        return sources.get(impl);
    }
}
