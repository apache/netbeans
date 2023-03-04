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

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class BuildXmlUtilsTest extends NbTestCase {

    private static final String PROJECT_NAME = "MyProject1";
    private FileObject projectDir;
    private FileObject srcDir;
    private List<FileObject> testDirs;


    public BuildXmlUtilsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        assertNotNull(workDir);
        projectDir = FileUtil.createFolder(workDir, PROJECT_NAME);
        srcDir = FileUtil.createFolder(projectDir, "mysrc");
        testDirs = Arrays.asList(
                FileUtil.createFolder(srcDir, "mytest1"),
                FileUtil.createFolder(projectDir, "mytest2")
        );
    }

    public void testProcessBuildXmlLines() throws Exception {
        List<String> currentLines = Files.readAllLines(new File(getDataDir(), "build.xml").toPath(), PhpUnitUtils.XML_CHARSET);
        List<String> goldenLines = Files.readAllLines(new File(getDataDir(), "build.golden.xml").toPath(), PhpUnitUtils.XML_CHARSET);
        List<String> newLines = BuildXmlUtils.processBuildXmlLines(PROJECT_NAME, projectDir, srcDir, testDirs, currentLines);
        for (int i = 0; i < newLines.size(); i++) {
            assertEquals(goldenLines.get(i), newLines.get(i));
        }
        assertEquals(goldenLines.size(), newLines.size());
    }

}
