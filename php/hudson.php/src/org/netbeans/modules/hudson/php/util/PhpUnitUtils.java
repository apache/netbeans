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
import java.util.List;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.filesystems.FileObject;

public final class PhpUnitUtils {

    static final Charset XML_CHARSET = StandardCharsets.UTF_8;

    private static final String BOOTSTRAP_LINE = "bootstrap=\"tests/bootstrap.php\""; // NOI18N
    private static final String PROJECT_NAME_LINE = "<testsuite name=\"%s\">"; // NOI18N
    private static final String SRC_DIR_LINE = "<directory suffix=\".php\">%s</directory>"; // NOI18N
    private static final String TESTS_DIR_LINE = "<directory suffix=\"Test.php\">%s</directory>"; // NOI18N
    private static final String TITLE_LINE = "title=\"%s\""; // NOI18N


    private PhpUnitUtils() {
    }

    public static void processPhpUnitConfig(PhpModule phpModule, Path projectPhpUnitConfig) throws IOException {
        List<String> newLines = processPhpUnitConfigLines(phpModule.getDisplayName(), phpModule.getProjectDirectory(), phpModule.getSourceDirectory(),
                phpModule.getTestDirectories(), Files.readAllLines(projectPhpUnitConfig, XML_CHARSET));
        Files.write(projectPhpUnitConfig, newLines, XML_CHARSET);
    }

    static List<String> processPhpUnitConfigLines(String projectName, FileObject projectDir, FileObject srcDir, List<FileObject> testDirs, List<String> currentLines) {
        List<String> newLines = new ArrayList<>(currentLines.size() * 2);
        String projectNameLine = String.format(PROJECT_NAME_LINE, "ProjectName"); // NOI18N
        String unitTestsLine = String.format(TESTS_DIR_LINE, "tests/unit/"); // NOI18N
        String integrationTestsLine = String.format(TESTS_DIR_LINE, "tests/integration/"); // NOI18N
        String titleLine = String.format(TITLE_LINE, "BankAccount"); // NOI18N
        String srcLine = String.format(SRC_DIR_LINE, "src"); // NOI18N
        for (String line : currentLines) {
            if (line.contains(BOOTSTRAP_LINE)) {
                newLines.add(line.replace(BOOTSTRAP_LINE, "")); // NOI18N
                continue;
            }
            if (line.contains(projectNameLine)) {
                newLines.add(line.replace(projectNameLine, String.format(PROJECT_NAME_LINE, projectName)));
                continue;
            }
            if (line.contains(unitTestsLine)) {
                for (FileObject testDir : testDirs) {
                    newLines.add(line.replace(unitTestsLine, String.format(TESTS_DIR_LINE, FileUtils.relativizePath(projectDir, testDir))));
                }
                continue;
            }
            if (line.contains(integrationTestsLine)) {
                continue;
            }
            if (line.contains(titleLine)) {
                newLines.add(line.replace(titleLine, String.format(TITLE_LINE, projectName)));
                continue;
            }
            if (line.contains(srcLine)) {
                newLines.add(line.replace(srcLine, String.format(SRC_DIR_LINE, FileUtils.relativizePath(projectDir, srcDir))));
                continue;
            }
            newLines.add(line);
        }
        return newLines;
    }

}
