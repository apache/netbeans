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
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.filesystems.FileObject;

public final class BuildXmlUtils {

    static final Charset XML_CHARSET = Charset.forName("UTF-8"); // NOI18N

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
