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
