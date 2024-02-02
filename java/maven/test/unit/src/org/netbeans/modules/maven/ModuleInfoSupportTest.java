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
package org.netbeans.modules.maven;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.tools.ToolProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;

public class ModuleInfoSupportTest extends NbTestCase {

    public ModuleInfoSupportTest(String name) {
        super(name);
    }

    protected @Override
    void setUp() throws Exception {
        clearWorkDir();
    }

    public void testAddRequires() throws Exception {
        System.setProperty("test.load.sync", "true");
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject moduleInfo = TestFileUtils.writeFile(
                workDir,
                "module-info.java",
                "module Mavenproject {\n}"
        );
        ModuleInfoSupport.addRequires(moduleInfo, List.of("test.dummy"));
        assertEquals("module Mavenproject {\n    requires test.dummy;\n}", moduleInfo.asText());
    }

    public void testGetDeclaredModules() throws Exception {
        System.setProperty("test.load.sync", "true");
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        FileObject dummyDir = workDir.createFolder("dummy");
        FileObject dummy2xDir = workDir.createFolder("dummy2x");
        FileObject dummyModuleInfo = TestFileUtils.writeFile(
                dummyDir,
                "module-info.java",
                "module test.dummy {}"
        );
        FileObject dummy2xModuleInfo = TestFileUtils.writeFile(
                dummy2xDir,
                "module-info.java",
                "module test.dummy2x {}"
        );
        FileObject moduleInfo = TestFileUtils.writeFile(workDir, "module-info.java",
                "module Mavenproject {"
                + "    requires test.dummy;\n"
                + "    requires test.dummy2x;\n"
                + "\n}");

        ToolProvider.getSystemJavaCompiler().run(
                System.in,
                System.out,
                System.err,
                "-d", dummyDir.getPath(),
                dummyModuleInfo.getPath()
        );

        ToolProvider.getSystemJavaCompiler().run(
                System.in,
                System.out,
                System.err,
                "-d", dummy2xDir.getPath(),
                dummy2xModuleInfo.getPath()
        );

        // Enforce refresh NB view of filesystem - assumes, that file system for
        // both dummy modules is identical
        workDir.getFileSystem().refresh(true);

        ClassPath bootClasspath = JavaPlatformManager
                .getDefault()
                .getDefaultPlatform()
                .getBootstrapLibraries();

        ClassPath modulePath = ClassPathSupport.createClassPath(
                dummyDir, dummy2xDir
        );

        ClasspathInfo cpi = new ClasspathInfo.Builder(bootClasspath)
                .setModuleCompilePath(modulePath)
                .build();

        JavaSource js = JavaSource.create(cpi, moduleInfo);

        assertEquals(Set.of("java.base", "test.dummy", "test.dummy2x"),
                new HashSet<>(ModuleInfoSupport.getDeclaredModules(js)));
    }
}
