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

package org.netbeans.modules.maven.queries;

import java.io.File;
import java.util.Arrays;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class UnitTestsCompilerOptionsQueryImplTest extends NbTestCase {

    public UnitTestsCompilerOptionsQueryImplTest(String name) {
        super(name);
    }

    private FileObject wd;

    protected @Override void setUp() throws Exception {
        clearWorkDir();
        File withSpace = new File(getWorkDir(), "path with space");
        wd = FileUtil.createFolder(withSpace);
    }

    public void testNoCompilerPluginSpecified() throws Exception {
        TestFileUtils.writeFile(wd,
                                "pom.xml",
                                "<project>\n" +
                                "<modelVersion>4.0.0</modelVersion>\n" +
                                "<groupId>test</groupId><artifactId>prj</artifactId>\n" +
                                "<packaging>jar</packaging><version>1.0</version>\n" +
                                "<build><plugins><plugin><artifactId>maven-compiler-plugin</artifactId><version>2.1</version>\n" +
                                "<configuration><source>11</source></configuration></plugin></plugins></build>\n" +
                                "</project>\n");
        TestFileUtils.writeFile(wd,
                                "src/main/java/module-info.java",
                                "module test {}\n");
        TestFileUtils.writeFile(wd,
                                "src/main/java/test/API.java",
                                "package test;\n" +
                                "public class API {}\n");
        TestFileUtils.writeFile(wd,
                                "src/test/java/module-info.java",
                                "module test { requires testng; }\n");
        TestFileUtils.writeFile(wd,
                                "target/generated-sources/java/test/Gen.java",
                                "package test;\n" +
                                "public class Gen {}\n");
        FileObject testSource =
        TestFileUtils.writeFile(wd,
                                "src/test/java/test/APITest.java",
                                "package test;\n" +
                                "public class APITest {}\n");
        assertEquals(Arrays.asList("--patch-module",
                                   "test=" + FileUtil.toFile(wd.getFileObject("src/main/java")).toURI().getPath() +
                                             File.pathSeparator +
                                             FileUtil.toFile(wd.getFileObject("target/generated-sources/java")).toURI().getPath()),
                     CompilerOptionsQuery.getOptions(testSource).getArguments());
    }

}
