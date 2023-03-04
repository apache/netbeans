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
package org.netbeans.modules.java.api.common.queries;

import java.net.URL;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;


/**
 * An implementation of {@link MultipleRootsUnitTestForSourceQueryImplementation} for a multi-module project.
 * @author Tomas Zezula
 */
final class MultiModuleUnitTestForSourceQueryImpl implements MultipleRootsUnitTestForSourceQueryImplementation {

    private final MultiModule sourceModules;
    private final MultiModule testModules;

    MultiModuleUnitTestForSourceQueryImpl(
            @NonNull final MultiModule sourceModules,
            @NonNull final MultiModule testModules) {
        Parameters.notNull("sourceModules", sourceModules);     //NOI18N
        Parameters.notNull("testModules", testModules);         //NOI18N
        this.sourceModules = sourceModules;
        this.testModules = testModules;
    }

    @Override
    public URL[] findUnitTests(FileObject source) {
        return map(source, sourceModules, testModules);
    }

    @Override
    public URL[] findSources(FileObject unitTest) {
        return map(unitTest, testModules, sourceModules);
    }

    @CheckForNull
    private static URL[] map(
            @NonNull final FileObject artefact,
            @NonNull final MultiModule from,
            @NonNull final MultiModule to) {
        final String moduleName = from.getModuleName(artefact);
        if (moduleName == null) {
            return null;
        }
        final ClassPath srcPath = to.getModuleSources(moduleName);
        if (srcPath == null) {
            return null;
        }
        return srcPath.entries().stream()
                .map((e) -> e.getURL())
                .toArray((len) -> new URL[len]);
    }
}
