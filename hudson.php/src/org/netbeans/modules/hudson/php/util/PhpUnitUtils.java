/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.php.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.filesystems.FileObject;

public final class PhpUnitUtils {

    static final Charset XML_CHARSET = Charset.forName("UTF-8"); // NOI18N

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
