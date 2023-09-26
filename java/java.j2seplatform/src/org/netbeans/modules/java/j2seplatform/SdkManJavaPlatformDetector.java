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
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author lkishalmi
 */
public class SdkManJavaPlatformDetector implements Runnable {

    static final Path SDKMAN_JAVA_DIR = Paths.get(System.getProperty("user.home"), ".sdkman", "candidates", "java"); //NOI18N

    @Override
    public void run() {

        if (Files.isDirectory(SDKMAN_JAVA_DIR)) {
            try (Stream<Path> files = Files.list(SDKMAN_JAVA_DIR)) {

                List<Path> jdks = files.filter(p -> Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS))
                                       .filter(p -> !p.getFileName().toString().equals("current"))
                                       .collect(Collectors.toList());

                JDKDetectorUtils.registerJDKs(jdks, jdk -> getDisplayName(jdk));
            } catch (IOException ignore) {
                // can't list files
            }
        }

    }

    private static String getDisplayName(Path path) {
        return "JDK " + path.getFileName().toString() + " (SDKMAN)"; //NOI18
    }
}
