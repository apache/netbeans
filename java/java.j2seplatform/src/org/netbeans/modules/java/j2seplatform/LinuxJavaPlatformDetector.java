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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author lkishalmi
 */
public class LinuxJavaPlatformDetector implements Runnable {

    static final Path LINUX_JAVA_DIR = Paths.get("/usr/lib/jvm"); //NOI18N
    
    /*
     * examples:
     *  java-17-openjdk-amd64 (debian)
     *  java-17-openjdk (arch, manjaro)
     *  java-17-openjdk-17.0.6.0.10-1.fc37.x86_64 (fedora)
     */
    static final String JAVA_DIR_MATCHER = "^java-(\\d+)-openjdk(-.+)?"; //NOI18N

    @Override
    public void run() {

        if (Files.isDirectory(LINUX_JAVA_DIR)) {

            try (Stream<Path> files = Files.list(LINUX_JAVA_DIR)) {

                Pattern pattern = Pattern.compile(JAVA_DIR_MATCHER);
                List<Path> jdks = files.filter(p -> Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS))
                                       .filter(p -> pattern.matcher(p.getFileName().toString()).matches())
                                       .collect(Collectors.toList());

                JDKDetectorUtils.registerJDKs(jdks, path -> getDisplayName(path, pattern));
                
            } catch (IOException ignore) {
                // can't list files
            }
            
        }
    }

    private static String getDisplayName(Path path, Pattern pattern) {
        String folder = path.getFileName().toString();
        Matcher m = pattern.matcher(folder);
        return (m.matches() ? "JDK " + m.group(1) : folder) + " (System)";
    }
}
