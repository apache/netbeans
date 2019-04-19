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
package org.netbeans.modules.java.openjdk.project;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.PathConversionMode;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.Utilities.TestLookup;
import org.netbeans.modules.java.openjdk.common.BuildUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class ClassPathProviderImplTest extends NbTestCase {

    public ClassPathProviderImplTest(String name) {
        super(name);
    }

    private FileObject root;

    @Override
    protected void setUp() throws IOException {
        clearWorkDir();

        ((TestLookup) Lookup.getDefault()).setLookupsImpl(Lookups.metaInfServices(ClassPathProviderImplTest.class.getClassLoader()));

        root = FileUtil.toFileObject(getWorkDir());
    }

    public void DISABLEDtestModuleXMLTransitiveDependencies() throws IOException {
        setupModuleXMLJDK(root);
        doTestCompileCP();
    }

    public void testModuleInfoTransitiveDependencies() throws IOException {
        setupModuleInfoJDK(root);
        doTestCompileCP();
    }

    private void doTestCompileCP() {
        File fakeJdkClasses = InstalledFileLocator.getDefault().locate("modules/ext/fakeJdkClasses.zip", "org.netbeans.modules.java.openjdk.project", false);

        checkCompileClassPath("repo/src/test1",
                              "${wd}/jdk/src/java.base/fake-target.jar" +
                              File.pathSeparatorChar +
                              "${wd}/langtools/src/java.compiler/fake-target.jar" +
                              File.pathSeparatorChar +
                              fakeJdkClasses.getAbsolutePath());
        checkCompileClassPath("repo/src/test2",
                              "${wd}/jdk/src/java.base/fake-target.jar" +
                              File.pathSeparatorChar +
                              "${wd}/langtools/src/java.compiler/fake-target.jar" +
                              File.pathSeparatorChar +
                              "${wd}/langtools/src/jdk.compiler/fake-target.jar" +
                              File.pathSeparatorChar +
                              fakeJdkClasses.getAbsolutePath());
        checkCompileClassPath("repo/src/test3",
                              "${wd}/jdk/src/java.base/fake-target.jar" +
                              File.pathSeparatorChar +
                              "${wd}/repo/src/test2/fake-target.jar" +
                              File.pathSeparatorChar +
                              fakeJdkClasses.getAbsolutePath());
    }

    private void checkCompileClassPath(String module, String expected) {
        FileObject prj = BuildUtils.getFileObject(root, module);
        FileObject src = BuildUtils.getFileObject(prj, "share/classes");

        Project project = FileOwnerQuery.getOwner(src);

        assertNotNull(project);

        String actual = ClassPath.getClassPath(src, ClassPath.COMPILE).toString(PathConversionMode.PRINT).replace(getWorkDirPath(), "${wd}");

        assertEquals(expected, actual);
    }

    private void setupModuleXMLJDK(FileObject jdkRoot) throws IOException {
        copyString2File(FileUtil.createData(jdkRoot, "modules.xml"),
                        "<?xml version=\"1.0\" encoding=\"us-ascii\"?>\n" +
                        "<modules>\n" +
                        "  <module>\n" +
                        "    <name>java.base</name>\n" +
                        "    <export>\n" +
                        "      <name>java.lang</name>\n" +
                        "    </export>\n" +
                        "  </module>\n" +
                        "  <module>\n" +
                        "    <name>java.compiler</name>\n" +
                        "    <depend>java.base</depend>\n" +
                        "    <export>\n" +
                        "      <name>javax.lang.model</name>\n" +
                        "    </export>\n" +
                        "  </module>\n" +
                        "  <module>\n" +
                        "    <name>jdk.compiler</name>\n" +
                        "    <depend>java.base</depend>\n" +
                        "    <depend re-exports=\"true\">java.compiler</depend>\n" +
                        "    <export>\n" +
                        "      <name>com.sun.tools.javac</name>\n" +
                        "    </export>\n" +
                        "  </module>\n" +
                        "  <module>\n" +
                        "    <name>test1</name>\n" +
                        "    <depend>java.base</depend>\n" +
                        "    <depend>java.compiler</depend>\n" +
                        "    <export>\n" +
                        "      <name>test1</name>\n" +
                        "    </export>\n" +
                        "  </module>\n" +
                        "  <module>\n" +
                        "    <name>test2</name>\n" +
                        "    <depend>java.base</depend>\n" +
                        "    <depend>jdk.compiler</depend>\n" +
                        "  </module>\n" +
                        "  <module>\n" +
                        "    <name>test3</name>\n" +
                        "    <depend>java.base</depend>\n" +
                        "    <depend>test2</depend>\n" +
                        "  </module>\n" +
                        "</modules>\n");

        setupOrdinaryFiles(jdkRoot);
    }
    
    private void setupModuleInfoJDK(FileObject jdkRoot) throws IOException {
        copyString2File(FileUtil.createData(jdkRoot, "jdk/src/java.base/share/classes/module-info.java"),
                        "module java.base {\n" +
                        "    exports java.lang;\n" +
                        "}\n");
        copyString2File(FileUtil.createData(jdkRoot, "langtools/src/java.compiler/share/classes/module-info.java"),
                        "module java.compiler {\n" +
                        "    requires java.base;\n" +
                        "    exports javax.lang.model;\n" +
                        "}\n");
        copyString2File(FileUtil.createData(jdkRoot, "langtools/src/jdk.compiler/share/classes/module-info.java"),
                        "module jdk.compiler {\n" +
                        "    requires java.base;\n" +
                        "    requires public java.compiler;\n" +
                        "    exports com.sun.tools.javac;\n" +
                        "}\n");
        copyString2File(FileUtil.createData(jdkRoot, "repo/src/test1/share/classes/module-info.java"),
                        "module test1 {\n" +
                        "    requires java.compiler;\n" +
                        "    exports test1;\n" +
                        "}\n");
        copyString2File(FileUtil.createData(jdkRoot, "repo/src/test2/share/classes/module-info.java"),
                        "module test2 {\n" +
                        "    requires java.base;\n" +
                        "    requires jdk.compiler;\n" +
                        "}\n");
        copyString2File(FileUtil.createData(jdkRoot, "repo/src/test3/share/classes/module-info.java"),
                        "module test3 {\n" +
                        "    requires test2;\n" +
                        "}\n");

        setupOrdinaryFiles(jdkRoot);
    }

    private void setupOrdinaryFiles(FileObject jdkRoot) throws IOException {
        copyString2File(FileUtil.createData(jdkRoot, "jdk/src/java.base/share/classes/java/lang/Object.java"),
                        "package java.lang;\n" +
                        "public class Object {}\n");
        copyString2File(FileUtil.createData(jdkRoot, "langtools/src/java.compiler/share/classes/javax/lang/model/SourceVersion.java"),
                        "package javax.lang.model;\n" +
                        "public enum SourceVersion {\n" +
                        "    RELEASE_9;\n" +
                        "}\n");
        copyString2File(FileUtil.createData(jdkRoot, "langtools/src/jdk.compiler/share/classes/com/sun/tools/javac/Main.java"),
                        "package com.sun.tools.javac;\n" +
                        "public class Main {}\n");
        copyString2File(FileUtil.createData(jdkRoot, "repo/src/test1/share/classes/test1/Test.java"),
                        "package test1;\n" +
                        "public class Test {}\n");
        copyString2File(FileUtil.createData(jdkRoot, "repo/src/test2/share/classes/test2/Test.java"),
                        "package test2;\n" +
                        "public class Test {}\n");
        copyString2File(FileUtil.createData(jdkRoot, "repo/src/test3/share/classes/test3/Test.java"),
                        "package test3;\n" +
                        "public class Test {}\n");
    }

    private void copyString2File(FileObject file, String content) throws IOException {
        try (OutputStream out = file.getOutputStream()) {
            out.write(content.getBytes("UTF-8"));
        }
    }

    static {
        System.setProperty("netbeans.dirs", System.getProperty("cluster.path.final", ""));
    }
}
