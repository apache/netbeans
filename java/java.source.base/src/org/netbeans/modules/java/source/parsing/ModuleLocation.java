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
package org.netbeans.modules.java.source.parsing;

import java.net.URL;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;

/**
 *
 * @author Tomas Zezula
 */
class ModuleLocation implements Location {

    private final Location base;
    private final String moduleName;
    private final Collection<? extends URL> moduleRoots;

    ModuleLocation(
            @NonNull final Location base,
            @NonNull final String moduleName,
            @NonNull final Collection<? extends URL> moduleRoots) {
        assert base != null;
        assert moduleName != null;
        assert moduleRoots != null;
        this.base = base;
        this.moduleName = moduleName;
        this.moduleRoots = moduleRoots;
    }

    @Override
    @NonNull
    public String getName() {
        return moduleRoots.toString();
    }

    @Override
    public boolean isOutputLocation() {
        return base == StandardLocation.CLASS_OUTPUT;
    }

    @Override
    public String toString() {
        return getName();
    }

    @NonNull
    String getModuleName() {
        return moduleName;
    }

    @NonNull
    Collection<? extends URL> getModuleRoots() {
        return moduleRoots;
    }

    @NonNull
    Location getBaseLocation() {
        return base;
    }

    @NonNull
    static ModuleLocation cast(@NonNull final Location l) {
        if (!isInstance(l)) {
            throw new IllegalArgumentException (String.valueOf(l));
        }
        return (ModuleLocation) l;
    }

    static boolean isInstance(final Location l) {
        return l instanceof ModuleLocation;
    }

    @NonNull
    static ModuleLocation create(
            @NonNull final Location base,
            @NonNull final Collection<? extends URL> moduleRoots,
            @NonNull final String moduleName) {
        return new ModuleLocation(
                base,
                moduleName,
                moduleRoots);
    }

    static final class WithExcludes extends ModuleLocation {

        private final Collection<? extends ClassPath.Entry> moduleEntries;

        private WithExcludes(Location base, String moduleName, Collection<? extends ClassPath.Entry> moduleEntries) {
            super(base, moduleName, moduleEntries.stream().map(entry -> entry.getURL()).collect(Collectors.toSet()));
            this.moduleEntries = moduleEntries;
        }

        @NonNull
        Collection<? extends ClassPath.Entry> getModuleEntries() {
            return moduleEntries;
        }

        @NonNull
        static WithExcludes cast(@NonNull final Location l) {
            if (!isInstance(l)) {
                throw new IllegalArgumentException (String.valueOf(l));
            }
            return (WithExcludes) l;
        }

        static boolean isInstance(final Location l) {
            return l instanceof WithExcludes;
        }

        @NonNull
        static WithExcludes createExcludes(
                @NonNull final Location base,
                @NonNull final Collection<? extends ClassPath.Entry> moduleEntries,
                @NonNull final String moduleName) {
            return new WithExcludes(
                    base,
                    moduleName,
                    moduleEntries);
        }
    }
}
