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
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.junit.NbTestCase;
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
        FileObject d = FileUtil.toFileObject(getWorkDir());
        TestFileUtils.writeFile(d, "module-info.java",
                "module Mavenproject {\n}");
        FileObject moduleInfo = d.getFileObject("module-info.java");
        ModuleInfoSupport.addRequires(moduleInfo, List.of("test.dummy"));
        assertEquals("module Mavenproject {\n    requires test.dummy;\n}", moduleInfo.asText());
    }

    public void testGetDeclaredModules() throws Exception {
        System.setProperty("test.load.sync", "true");
        FileObject d = FileUtil.toFileObject(getWorkDir());
        FileObject dummy = d.createFolder("dummy");
        FileObject dummy2 = d.createFolder("dummy2");
        TestFileUtils.writeFile(dummy, "module-info.java", "module test.dummy {}");
        TestFileUtils.writeFile(dummy2, "module-info.java", "module test.dummy2x {}");
        TestFileUtils.writeFile(d, "module-info.java",
                "module Mavenproject {"
                + "    requires test.dummy;\n"
                + "    requires test.dummy2x;\n"
                + "\n}");
        FileObject moduleInfo = d.getFileObject("module-info.java");

        javax.tools.ToolProvider.getSystemJavaCompiler().run(
                System.in,
                System.out,
                System.err,
                "-d", dummy.getPath(),
                dummy.getFileObject("module-info.java").getPath()
        );

        javax.tools.ToolProvider.getSystemJavaCompiler().run(
                System.in,
                System.out,
                System.err,
                "-d", dummy2.getPath(),
                dummy2.getFileObject("module-info.java").getPath()
        );

        ClassPath cp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(
                dummy, dummy2
        );

        ClasspathInfo cpi = new ClasspathInfo.Builder(JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries())
                .setModuleCompilePath(cp)
                .build();

        JavaSource js = JavaSource.create(cpi, moduleInfo);

        assertEquals(Set.of("java.base", "test.dummy", "test.dummy2x"),
                new HashSet<>(ModuleInfoSupport.getDeclaredModules(js)));
    }
}
