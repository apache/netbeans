/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.exec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable.FileLineParser;
import org.openide.util.Pair;

public class FileLineParserTest extends NbTestCase {

    private File dataDir;
    private File nodeJsSources;
    private FileLineParser fileLineParser;


    public FileLineParserTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dataDir = getDataDir();
        assertTrue(dataDir.isDirectory());
        nodeJsSources = new File(dataDir, "nodejs-sources");
        assertTrue(nodeJsSources.isDirectory());
        List<File> sourceRoots = new ArrayList<>();
        sourceRoots.add(dataDir);
        sourceRoots.add(nodeJsSources);
        fileLineParser = new FileLineParser(sourceRoots);
    }

    public void testFullPaths() {
        File mainJs = new File(dataDir, "main.js");
        assertFilePattern(mainJs.getAbsolutePath() + ":9",
                mainJs, 9);
        assertFilePattern("    at Object.<anonymous> (" + mainJs.getAbsolutePath() + ":15:1)",
                mainJs, 15);
    }

    public void testIncompletePaths() {
        File nodeJs = new File(nodeJsSources, "node.js");
        assertFilePattern("at startup (node.js:119:16)",
                nodeJs, 119);
        assertFilePattern("at node.js:906:3",
                nodeJs, 906);
    }

    public void testIssue253600() {
        Pair<String, Integer> fileNameLine = FileLineParser.getOutputFileNameLine(
                "    at /media/oldhome/gapon/Download/example/node_modules/sails/node_modules/async/lib/async.js:607:21");
        assertNotNull(fileNameLine);
        assertEquals("/media/oldhome/gapon/Download/example/node_modules/sails/node_modules/async/lib/async.js", fileNameLine.first());
        assertEquals(607, fileNameLine.second().intValue());
        fileNameLine = FileLineParser.getOutputFileNameLine(
                "at /media/oldhome/gapon/Download/example/node_modules/sails/node_modules/async/lib/async.js:607:21");
        assertNotNull(fileNameLine);
        assertEquals("/media/oldhome/gapon/Download/example/node_modules/sails/node_modules/async/lib/async.js", fileNameLine.first());
        assertEquals(607, fileNameLine.second().intValue());
    }

    private void assertFilePattern(String input, File file, int line) {
        assertTrue(file.getAbsolutePath(), file.isFile());
        Pair<File, Integer> fileLine = fileLineParser.getOutputFileLine(input);
        assertNotNull(input, fileLine);
        assertEquals(file, fileLine.first());
        assertEquals(line, fileLine.second().intValue());
    }

}
