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

package org.netbeans.modules.hudson.php.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.filesystems.FileObject;

public final class BuildXmlUtils {

    static final Charset XML_CHARSET = StandardCharsets.UTF_8;

    private static final String PROJECT_NAME_LINE = "<project name=\"%s\" default=\"build\">"; // NOI18N
    private static final String DIR_LINE = "${basedir}/%s"; // NOI18N

    private BuildXmlUtils() {
    }

    public static void processBuildXml(PhpModule phpModule, Path projectPhpUnitConfig) throws IOException {
        List<String> newLines = processBuildXmlLines(phpModule.getDisplayName(), phpModule.getProjectDirectory(), phpModule.getSourceDirectory(),
                phpModule.getTestDirectories(), Files.readAllLines(projectPhpUnitConfig, XML_CHARSET));
        Files.write(projectPhpUnitConfig, newLines, XML_CHARSET);
    }

    static List<String> processBuildXmlLines(String projectName, FileObject projectDir, FileObject srcDir, List<FileObject> testDirs, List<String> currentLines) {
        List<String> newLines = new ArrayList<>(currentLines.size() * 2);
        String projectNameLine = String.format(PROJECT_NAME_LINE, "name-of-project"); // NOI18N
        String srcLine = String.format(DIR_LINE, "src"); // NOI18N
        String testLine = String.format(DIR_LINE, "tests"); // NOI18N
        for (String line : currentLines) {
            if (line.contains(projectNameLine)) {
                newLines.add(line.replace(projectNameLine, String.format(PROJECT_NAME_LINE, projectName)));
                continue;
            }
            if (line.contains(srcLine)) {
                newLines.add(line.replace(srcLine, String.format(DIR_LINE, FileUtils.relativizePath(projectDir, srcDir))));
                continue;
            }
            if (line.contains(testLine)) {
                for (FileObject testDir : testDirs) {
                    // simply take only the first one, sorry
                    newLines.add(line.replace(testLine, String.format(DIR_LINE, FileUtils.relativizePath(projectDir, testDir))));
                    break;
                }
                continue;
            }
            newLines.add(line);
        }
        return newLines;
    }

}
