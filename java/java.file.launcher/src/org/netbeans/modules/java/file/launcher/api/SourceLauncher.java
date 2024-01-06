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
package org.netbeans.modules.java.file.launcher.api;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.netbeans.modules.java.file.launcher.SingleSourceFileUtil;
import org.netbeans.modules.java.file.launcher.queries.MultiSourceRootProvider;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;

/**Holds utilities for source file launcher.
 *
 */
public final class SourceLauncher {

    private static final String ENABLE_PREVIEW = "--enable-preview";
    private static final String SOURCE = "--source";
    private static final String CLASS_PATH = "--class-path";
    private static final String CLASSPATH = "-classpath";
    private static final String CP = "-cp";
    private static final String MODULE_PATH = "--module-path";
    private static final String P = "-p";

    /**Returns {@code true} if and only if the given file is known as a
     * file that is handled by a source file launcher. This, in particular,
     * typically means the file is outside of a project.
     *
     * @param file the file to test
     * @return {@code true} if and only if the file is known as a file handled by the
     *         source launcher. {@code false} otherwise.
     */
    public static boolean isSourceLauncherFile(FileObject file) {
        MultiSourceRootProvider msrp = Lookup.getDefault().lookup(MultiSourceRootProvider.class);
        return msrp != null && msrp.isSourceLauncher(file);
    }

    public static String joinCommandLines(Iterable<? extends String> inputLines) {
        Map<String, String> joinedOptions = new HashMap<>();

        for (String value : inputLines) {
            List<String> args = SingleSourceFileUtil.parseLine(value);

            for (int i = 0; i < args.size(); i++) {
                switch (args.get(i)) {
                    case ENABLE_PREVIEW:
                        joinedOptions.put(ENABLE_PREVIEW, null);
                        break;
                    case CLASSPATH: case CLASS_PATH: case CP:
                        if (i + 1 < args.size()) {
                            joinedOptions.put(CLASS_PATH, mergePaths(joinedOptions.get(CLASS_PATH), args.get(i + 1)));
                            i++;
                        }
                        break;
                    case MODULE_PATH: case P:
                        if (i + 1 < args.size()) {
                            joinedOptions.put(MODULE_PATH, mergePaths(joinedOptions.get(MODULE_PATH), args.get(i + 1)));
                            i++;
                        }
                        break;
                    case SOURCE:
                        if (i + 1 < args.size()) {
                            String version = args.get(i + 1);
                            String testVersion = version;
                            if (testVersion.startsWith("1.")) {
                                testVersion = testVersion.substring(2);
                            }
                            String existingVersion = joinedOptions.get(SOURCE);
                            if (existingVersion != null) {
                                if (existingVersion.startsWith("1.")) {
                                    existingVersion = existingVersion.substring(2);
                                }
                                if (new SpecificationVersion(testVersion).compareTo(new SpecificationVersion(existingVersion)) > 0) {
                                    joinedOptions.put(SOURCE, version);
                                }
                            } else {
                                joinedOptions.put(SOURCE, version);
                            }
                            i++;
                        }
                        break;
                }
            }
        }

        return joinedOptions.entrySet().stream().map(e -> e.getKey() + (e.getValue() != null ? " " + e.getValue() : "")).collect(Collectors.joining(" "));
    }

    private static String mergePaths(String oldPath, String newPath) {
        if (oldPath == null) {
            return newPath;
        }
        if (newPath == null) {
            return oldPath;
        }
        return oldPath + File.pathSeparator + newPath;
    }
}
