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
package org.netbeans.modules.java.openjdk.jtreg;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Assert;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.Utilities.TestLookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class ClassPathProviderImplTest extends NbTestCase {

    public ClassPathProviderImplTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();

        ((TestLookup) Lookup.getDefault()).setLookupsImpl(Lookups.metaInfServices(ClassPathProviderImplTest.class.getClassLoader()));
    }

    public void testSimple() throws Exception {
        File workDir = getWorkDir();

        FileUtil.createFolder(new File(workDir, "src/share/classes"));
        FileObject testRoot = createData("test/TEST.ROOT", "");
        FileObject testFile = FileUtil.createData(new File(workDir, "test/feature/Test.java"));
        ClassPath sourceCP = new ClassPathProviderImpl().findClassPath(testFile, ClassPath.SOURCE);

        Assert.assertArrayEquals(new FileObject[] {testFile.getParent()}, sourceCP.getRoots());
    }

    public void testJTRegLibrary() throws Exception {
        File workDir = getWorkDir();

        FileUtil.createFolder(new File(workDir, "src/share/classes"));
        FileObject testRoot = createData("test/TEST.ROOT", "");
        FileObject testFile = createData("test/feature/inner/Test.java", "/** @test\n * @library ../lib /lib2\n */");
        FileObject testLib = FileUtil.createData(new File(workDir, "test/feature/lib/Lib.java"));
        FileObject testLib2 = FileUtil.createData(new File(workDir, "test/lib2/Lib2.java"));
        ClassPath sourceCP = new ClassPathProviderImpl().findClassPath(testFile, ClassPath.SOURCE);

        Assert.assertEquals(new HashSet<>(Arrays.asList(testFile.getParent(), testLib.getParent(), testLib2.getParent())),
                            new HashSet<>(Arrays.asList(sourceCP.getRoots())));
    }

    public void testTestProperties() throws Exception {
        File workDir = getWorkDir();

        FileUtil.createFolder(new File(workDir, "src/share/classes"));
        FileObject testRoot = createData("test/TEST.ROOT", "");
        FileObject testUse = FileUtil.createData(new File(workDir, "test/dir/use/org/Use.java"));
        FileObject testLib = FileUtil.createData(new File(workDir, "test/dir/lib/org/Lib.java"));
        FileObject testProperties = createData("test/dir/use/TEST.properties", "lib.dirs=../lib");
        ClassPath sourceCP = new ClassPathProviderImpl().findClassPath(testUse, ClassPath.SOURCE);

        Assert.assertEquals(new HashSet<>(Arrays.asList(testUse.getParent().getParent(), testLib.getParent().getParent())),
                            new HashSet<>(Arrays.asList(sourceCP.getRoots())));
    }

    public void testLangtoolsCP() throws Exception {
        File workDir = new File(getWorkDir(), "langtools");

        FileUtil.createData(new File(workDir, "src/share/classes/com/sun/tools/javac/main/Main.java"));
        FileObject testRoot = FileUtil.createData(new File(workDir, "test/TEST.ROOT"));
        FileObject testTest = FileUtil.createData(new File(workDir, "test/feature/Test.java"));
        FileObject buildClasses = FileUtil.createFolder(new File(workDir, "build/classes"));
        ClassPath bootCP = new ClassPathProviderImpl().findClassPath(testTest, ClassPath.BOOT);
        ClassPath compileCP = new ClassPathProviderImpl().findClassPath(testTest, ClassPath.COMPILE);

        Assert.assertEquals(buildClasses, bootCP.getRoots()[0]);
        Assert.assertTrue(compileCP.entries().isEmpty());
    }

    public void testExternalLibRoots() throws Exception {
        File workDir = getWorkDir();

        FileUtil.createFolder(new File(workDir, "src/share/classes"));
        FileObject testRoot = createData("test/TEST.ROOT", "external.lib.roots=../lib1 ../lib2\t../lib3");
        FileObject testUse = createData("test/use/Use.java", "/**@test\n@library /lib0/0 /1 /2 /3\n*/");
        FileObject testLib0 = FileUtil.createData(new File(workDir, "test/lib0/0/Lib.java"));
        FileObject testLib1 = FileUtil.createData(new File(workDir, "lib1/1/Lib.java"));
        FileObject testLib2 = FileUtil.createData(new File(workDir, "lib2/2/Lib.java"));
        FileObject testLib3 = FileUtil.createData(new File(workDir, "lib3/3/Lib.java"));
        ClassPath sourceCP = new ClassPathProviderImpl().findClassPath(testUse, ClassPath.SOURCE);

        Assert.assertEquals(new HashSet<>(Arrays.asList(testUse.getParent(), testLib0.getParent(), testLib1.getParent(), testLib2.getParent(), testLib3.getParent())),
                            new HashSet<>(Arrays.asList(sourceCP.getRoots())));
    }

    public void testInPackage() throws Exception {
        File workDir = getWorkDir();

        FileUtil.createFolder(new File(workDir, "src/share/classes"));
        FileObject testRoot = createData("test/TEST.ROOT", "");
        FileObject testFile = FileUtil.createData(new File(workDir, "test/feature/pack/Test.java"));
        writeContent(testFile,
                     """
                     /* package wrong.package.clause; */
                     @Ann("package another.wrong.package.clause")
                     package /**/ //
                     feature.
                     pack;
                     """);
        ClassPath sourceCP = new ClassPathProviderImpl().findClassPath(testFile, ClassPath.SOURCE);

        Assert.assertArrayEquals(new FileObject[] {testFile.getParent().getParent().getParent()},
                                 sourceCP.getRoots());
    }

    public void testInFolder() throws Exception {
        File workDir = getWorkDir();

        FileUtil.createFolder(new File(workDir, "src/share/classes"));
        FileObject testRoot = createData("test/jdk/TEST.ROOT", "");
        FileObject testFile = FileUtil.createData(new File(workDir, "test/lib/feature/pack/Test.java"));
        writeContent(testFile,
                     """
                     /* package wrong.package.clause; */
                     @Ann("package another.wrong.package.clause")
                     package /**/ //
                     feature.
                     pack;
                     """);
        ClassPath sourceCP = new ClassPathProviderImpl().findClassPath(testFile, ClassPath.SOURCE);

        Assert.assertArrayEquals(new FileObject[] {testFile.getParent().getParent().getParent()},
                                 sourceCP.getRoots());
    }

    public void testWrongPackageClause() throws Exception {
        File workDir = getWorkDir();

        FileUtil.createFolder(new File(workDir, "src/share/classes"));
        FileObject testRoot = createData("test/jdk/TEST.ROOT", "");
        FileObject testFile = createData("test/jdk/testpkg/nested/Test.java",
                                         """
                                         package other.nested;
                                         /** @test */
                                         public class Test {}
                                         """);
        ClassPath sourceCP = new ClassPathProviderImpl().findClassPath(testFile, ClassPath.SOURCE);

        Assert.assertArrayEquals(new FileObject[] {testFile.getParent()},
                                 sourceCP.getRoots());
    }

    public void testPackageClausePointsOutOfRoot() throws Exception {
        File workDir = getWorkDir();

        FileUtil.createFolder(new File(workDir, "src/share/classes"));
        FileObject testRoot = createData("test/jdk/TEST.ROOT", "");
        FileObject testFile = createData("test/jdk/testpkg/nested/Test.java",
                                         """
                                         package test.jdk.testpkg.nested;
                                         /** @test */
                                         public class Test {}
                                         """);
        ClassPath sourceCP = new ClassPathProviderImpl().findClassPath(testFile, ClassPath.SOURCE);

        Assert.assertArrayEquals(new FileObject[] {testFile.getParent()},
                                 sourceCP.getRoots());
    }

    private FileObject createData(String relPath, String content) throws IOException {
        File workDir = getWorkDir();
        FileObject file = FileUtil.createData(new File(workDir, relPath));

        writeContent(file, content);

        return file;
    }

    private void writeContent(FileObject file, String content) throws IOException {
        try (Writer w = new OutputStreamWriter(file.getOutputStream())) {
            w.write(content);
        }
    }

}
