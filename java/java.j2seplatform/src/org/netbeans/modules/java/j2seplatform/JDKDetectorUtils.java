/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.j2seplatform;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.j2seplatform.platformdefinition.J2SEPlatformImpl;
import org.netbeans.spi.java.platform.JavaPlatformFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author mbien
 */
class JDKDetectorUtils {

    /**
     * Registers new JDKs if they aren't registered yet.
     * @param jdks List of paths to the JDKs.
     * @param namer Converts a Path to the display name of the JDK.
     */
    static void registerJDKs(List<Path> jdks, Function<Path, String> namer) {

        List<JavaPlatform> platforms = Stream.of(JavaPlatformManager.getDefault().getInstalledPlatforms())
                                             .filter(JavaPlatform::isValid)
                                             .collect(Collectors.toList());

        Set<String> registeredJDKNames = platforms.stream()
                                                  .map(JavaPlatform::getDisplayName)
                                                  .collect(Collectors.toSet());

        Set<Path> registeredPaths = platforms.stream()
                                             .flatMap(p -> p.getInstallFolders().stream())
                                             .map(f -> Paths.get(f.getPath()))
                                             .collect(Collectors.toSet());

        for (JavaPlatformFactory.Provider provider : Lookup.getDefault().lookupAll(JavaPlatformFactory.Provider.class)) {

            JavaPlatformFactory platformFactory = provider.forType(J2SEPlatformImpl.PLATFORM_J2SE);
            if (platformFactory != null) {
                for (Path jdk : jdks) {
                    FileObject fo = FileUtil.toFileObject(jdk);
                    if (fo != null) {
                        Object value = fo.getAttribute("J2SEPlatform.displayName"); // NOI18N
                        if (value instanceof String) {
                            // skip; a value here indicates that the JDK was added
                            // (and potentially subsequently removed again) by the user
                            break;
                        }
                    }
                    try {
                        String jdkName = namer.apply(jdk);
                        // don't register if something with the same name or same path exists
                        if (!registeredJDKNames.contains(jdkName) && !registeredPaths.contains(jdk)) {
                            platformFactory.create(FileUtil.toFileObject(jdk), jdkName, true);
                        }
                    } catch (IOException | IllegalArgumentException ignore) {
                        // not valid.. probably
                    }
                }
            }
        }
    }

}
