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

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.logging.Level;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.test.TestFileUtils;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.SpecificationVersion;
import org.openide.util.test.MockLookup;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Test build-impl.xml functionality.
 * Large portion of this class was copied from JavaAntLoggerTest.
 * @author Jesse Glick, David Konecny, Tomas Zezula
 */
public final class BuildImplTest extends NbTestCase {

    public BuildImplTest(String name) {
        super(name);
    }

    private File junitJar;
    private File testNGJar;
    private File jcommanderJar;

    @Override protected Level logLevel() {
        return Level.FINE;
    }

    @Override protected String logRoot() {
        return "org.apache.tools.ant.module";
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        synchronized (output) {
            output.clear();
            outputPosition = 0;
        }
        String junitJarProp = System.getProperty("test.junit.jar");
        assertNotNull("must set test.junit.jar", junitJarProp);
        junitJar = new File(junitJarProp);
        assertTrue("file " + junitJar + " exists", junitJar.isFile());
        String tngJarProp = System.getProperty("test.testng.jar");
        assertNotNull("must set test.testng.jar", tngJarProp);
        testNGJar = new File(tngJarProp);
        assertTrue("file " + testNGJar + " exists", testNGJar.isFile());
        String jcommanderJarProp = System.getProperty("test.jcommander.jar");
        assertNotNull("must set test.jcommander.jar", jcommanderJarProp);
        jcommanderJar = new File(jcommanderJarProp);
        assertTrue("file " + jcommanderJar + " exists", jcommanderJar.isFile());
        MockLookup.setLayersAndInstances(new IOP(), new IFL());
    }

    private AntProjectHelper setupProject(String subFolder, int numberOfSourceFiles, boolean generateTests) throws Exception {
        File proj = getWorkDir();
        if (subFolder != null) {
            proj = new File(getWorkDir(), subFolder);
        }
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.6"));   //NOI18N
        AntProjectHelper aph = J2SEProjectGenerator.createProject(proj, subFolder != null ? subFolder : getName(), (String)null, (String)null, null, false);
        EditableProperties ep = aph.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.put(ProjectProperties.DO_DEPEND, "true"); // to avoid too many changes in tests from issue #118079
        aph.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(ProjectManager.getDefault().findProject(aph.getProjectDirectory()));
        J2SEProjectGenerator.setDefaultSourceLevel(null);
        FileObject root = aph.getProjectDirectory();
        for (int i=0; i<numberOfSourceFiles; i++) {
            generateJava(root, "src/pkg/Source" + i + ".java", false);
            if (generateTests) {
                generateJava(root, "test/pkg/Source" + i + "Test.java", true);
            }
        }
        return aph;
    }

    private AntProjectHelper setupProject(int numberOfSourceFiles, boolean generateTests) throws Exception {
        return setupProject(null, numberOfSourceFiles, generateTests);
    }

    private void generateJava(FileObject root, String path, boolean test) throws Exception {
        String name = path.replaceFirst("^.+/", "").replaceFirst("\\..+$", "");
        if (test) {
            TestFileUtils.writeFile(root, path,
                "package pkg;\n" +
                "import junit.framework.TestCase;\n" +
                "public class " + name + " extends TestCase {\n" +
                "public " + name + "() { }\n"+
                "public void testDoSomething() { System.out.println(\"" + name + " test executed\"); }\n" +
                "}\n");
        } else {
            TestFileUtils.writeFile(root, path,
                "package pkg;\n" +
                "public class " + name + " {\n" +
                "public boolean doSomething() { return true; }\n" +
                "public static void main(String[] args) { System.err.println(\"" + name + " main class executed\"); }\n" +
                "}\n");
        }
    }

    private Properties getProperties() {
        Properties p = new Properties();
        p.setProperty("libs.junit.classpath", testNGJar.getAbsolutePath()  + ":" + junitJar.getAbsolutePath()+ ":" + jcommanderJar.getAbsolutePath());
        return p;
    }

    public void testDefaultTargets() throws Exception {
        AntProjectHelper aph = setupProject(1, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        assertBuildSuccess(ActionUtils.runTarget(buildXml, null, p));
        assertTrue("Default target must test project", output.contains("test:"));
        assertTrue("Default target must jar project", output.contains("jar:"));
        assertTrue("Default target must build javadoc", output.contains("javadoc:"));

        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source0.class must exist", fo.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull("build/test/classes/pkg/Source0Test.class must exist", fo.getFileObject("build/test/classes/pkg/Source0Test.class"));
        assertNotNull("dist/testDefaultTargets.jar must exist", fo.getFileObject("dist/testDefaultTargets.jar"));
        assertNotNull("dist/javadoc/index.html must exist", fo.getFileObject("dist/javadoc/index.html"));
    }

    public void testCompile() throws Exception {
        AntProjectHelper aph = setupProject(2, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"compile"}, p));
        assertTrue("compile target was not executed", output.contains("compile:"));

        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source0.class must exist", fo.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull("build/classes/pkg/Source1.class must exist", fo.getFileObject("build/classes/pkg/Source1.class"));
        assertNull("build/test folder should not be created", fo.getFileObject("build/test"));
        assertNull("dist folder should not be created", fo.getFileObject("dist"));
    }

    public void testCompileSingle() throws Exception {
        AntProjectHelper aph = setupProject(3, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        p.setProperty("javac.includes", "pkg/Source2.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"compile-single"}, p));
        assertTrue("compile-single target was not executed", output.contains("compile-single:"));

        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source2.class must exist", fo.getFileObject("build/classes/pkg/Source2.class"));
        assertEquals("Only one class should be compiled", 1, fo.getFileObject("build/classes/pkg").getChildren().length);
        assertNull("build/test folder should not be created", fo.getFileObject("build/test"));
        assertNull("dist folder should not be created", fo.getFileObject("dist"));
    }

    public void testCompileSingleWithoutDependencyAnalysis() throws Exception { // #85707
        AntProjectHelper aph = setupProject(0, false);
        FileObject root = aph.getProjectDirectory();
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        Properties p = getProperties();
        FileObject s1 = TestFileUtils.writeFile(root, "src/pack/age/Source1.java", "package pack.age; class Source1 {}");
        // Oddly, "class Source2 {Source1 s;}" does not trigger a dep Source2 -> Source1
        // ...which is technically correct (contents of Source1 cannot affect Source2's compilability)
        // but is <depend> really this clever?
        TestFileUtils.writeFile(root, "src/pack/age/Source2.java", "package pack.age; class Source2 {{new Source1();}}");
        p.setProperty("javac.includes", "pack/age/Source1.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-single"}, p));
        File classes = new File(new File(getWorkDir(), "build"), "classes");
        assertOutput("Compiling 1 source file to " + classes);
        File classesPackage = new File(new File(classes, "pack"), "age");
        assertEquals(1, classesPackage.list().length);
        p.setProperty("javac.includes", "pack/age/Source2.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-single"}, p));
        assertOutput("Compiling 1 source file to " + classes);
        assertEquals(2, classesPackage.list().length);
        // Compiling an already-compiled file forces it to be recompiled:
        p.setProperty("javac.includes", "pack/age/Source1.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-single"}, p));
        assertOutput("Compiling 1 source file to " + classes);
        assertEquals(2, classesPackage.list().length);
        // Can compile several at once:
        p.setProperty("javac.includes", "pack/age/Source1.java,pack/age/Source2.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-single"}, p));
        assertOutput("Compiling 2 source files to " + classes);
        assertEquals(2, classesPackage.list().length);
        // But <depend> is not run:
        TestFileUtils.touch(s1, null);
        p.setProperty("javac.includes", "pack/age/Source1.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-single"}, p));
        assertOutput("Compiling 1 source file to " + classes);
        assertEquals(2, classesPackage.list().length);
        // Same for tests:
        FileObject t1 = TestFileUtils.writeFile(root, "test/pack/age/Test1.java", "package pack.age; class Test1 {}");
        TestFileUtils.writeFile(root, "test/pack/age/Test2.java", "package pack.age; class Test2 {{new Test1();}}");
        p.setProperty("javac.includes", "pack/age/Test1.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-test-single"}, p));
        classes = new File(new File(new File(getWorkDir(), "build"), "test"), "classes");
        assertOutput("Compiling 1 source file to " + classes);
        classesPackage = new File(new File(classes, "pack"), "age");
        assertEquals(1, classesPackage.list().length);
        p.setProperty("javac.includes", "pack/age/Test2.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-test-single"}, p));
        assertOutput("Compiling 1 source file to " + classes);
        assertEquals(2, classesPackage.list().length);
        p.setProperty("javac.includes", "pack/age/Test1.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-test-single"}, p));
        assertOutput("Compiling 1 source file to " + classes);
        assertEquals(2, classesPackage.list().length);
        p.setProperty("javac.includes", "pack/age/Test1.java,pack/age/Test2.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-test-single"}, p));
        assertOutput("Compiling 2 source files to " + classes);
        assertEquals(2, classesPackage.list().length);
        TestFileUtils.touch(t1, null);
        p.setProperty("javac.includes", "pack/age/Test1.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-test-single"}, p));
        assertOutput("Compiling 1 source file to " + classes);
        assertEquals(2, classesPackage.list().length);
    }

    public void testCompileSingleTransitive() throws Exception { // #115918
        AntProjectHelper aph = setupProject(0, false);
        FileObject root = aph.getProjectDirectory();
        FileObject a = TestFileUtils.writeFile(root, "src/p/A.java", "package p; class A {}");
        TestFileUtils.writeFile(root, "src/p/B.java", "package p; class B {A a;}");
        TestFileUtils.writeFile(root, "src/p/C.java", "package p; class C {}");
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        Properties p = getProperties();
        p.setProperty("javac.includes", "p/B.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-single"}, p));
        File classes = new File(new File(getWorkDir(), "build"), "classes");
        assertOutput("Compiling 1 source file to " + classes);
        assertNotNull(root.getFileObject("build/classes/p/A.class"));
        assertNotNull(root.getFileObject("build/classes/p/B.class"));
        assertNull(root.getFileObject("build/classes/p/C.class"));
        TestFileUtils.writeFile(root, "src/p/A.java", "BROKEN");
        TestFileUtils.touch(a, root.getFileObject("build/classes/p/A.class"));
        assertBuildFailure(ActionUtils.runTarget(buildXml, new String[] {"compile-single"}, p));
        TestFileUtils.touch(a, root.getFileObject("build/classes/p/A.class"));
        TestFileUtils.writeFile(root, "src/p/A.java", "package p; class A {}");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-single"}, p));
        // XXX test same in test dir
        // XXX test alternate src/test dirs
    }

    @RandomlyFails // NB-Core-Build #8012: in writeFile, RefersToExcluded1b.java is "not valid" (should convert to java.io.File)
    public void testIncludesExcludes() throws Exception {
        AntProjectHelper aph = setupProject(12, true);
        EditableProperties ep = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(ProjectProperties.INCLUDES, "**/*1*");
        ep.setProperty(ProjectProperties.EXCLUDES, "**/*0*");
        aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveAllProjects();
        FileObject dir = aph.getProjectDirectory();
        FileUtil.createData(dir, "src/data0.xml");
        FileUtil.createData(dir, "src/data1.xml");
        FileUtil.createData(dir, "src/data2.xml");
        FileUtil.createData(dir, "src/data10.xml");
        FileUtil.createData(dir, "src/data11.xml");
        generateJava(dir, "test/pkg/Utils1.java", true);
        FileObject buildXml = dir.getFileObject("build.xml");
        assertNotNull(buildXml);
        Properties p = getProperties();
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"test"}, p));
        assertNull(dir.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull(dir.getFileObject("build/classes/pkg/Source1.class"));
        assertNull(dir.getFileObject("build/classes/pkg/Source2.class"));
        assertNull(dir.getFileObject("build/classes/pkg/Source10.class"));
        assertNotNull(dir.getFileObject("build/classes/pkg/Source11.class"));
        assertNull(dir.getFileObject("build/classes/data0.xml"));
        assertNotNull(dir.getFileObject("build/classes/data1.xml"));
        assertNull(dir.getFileObject("build/classes/data2.xml"));
        assertNull(dir.getFileObject("build/classes/data10.xml"));
        assertNotNull(dir.getFileObject("build/classes/data11.xml"));
        assertNull(dir.getFileObject("build/test/classes/pkg/Source0Test.class"));
        assertNotNull(dir.getFileObject("build/test/classes/pkg/Source1Test.class"));
        assertNull(dir.getFileObject("build/test/classes/pkg/Source2Test.class"));
        assertNull(dir.getFileObject("build/test/classes/pkg/Source10Test.class"));
        assertNotNull(dir.getFileObject("build/test/classes/pkg/Source11Test.class"));
        assertNotNull(dir.getFileObject("build/test/classes/pkg/Utils1.class"));
        assertFalse(output.contains("Source0Test test executed"));
        assertTrue(output.contains("Source1Test test executed"));
        assertFalse(output.contains("Source2Test test executed"));
        assertFalse(output.contains("Source10Test test executed"));
        assertTrue(output.contains("Source11Test test executed"));
        assertFalse(output.contains("Utils1 test executed"));
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"javadoc"}, p));
        assertNull(dir.getFileObject("dist/javadoc/pkg/Source0.html"));
        assertNotNull(dir.getFileObject("dist/javadoc/pkg/Source1.html"));
        assertNull(dir.getFileObject("dist/javadoc/pkg/Source2.html"));
        assertNull(dir.getFileObject("dist/javadoc/pkg/Source10.html"));
        assertNotNull(dir.getFileObject("dist/javadoc/pkg/Source11.html"));
        p.setProperty("javac.includes", "pkg/Source4.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-single"}, p));
        assertNotNull(dir.getFileObject("build/classes/pkg/Source4.class"));
        p.setProperty("javac.includes", "pkg/Source4Test.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-test-single"}, p));
        assertNotNull(dir.getFileObject("build/test/classes/pkg/Source4Test.class"));
        p.setProperty("javac.includes", "pkg/Source7Test.java");
        p.setProperty("test.includes", "pkg/Source7Test.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"test-single"}, p));
        assertNotNull(dir.getFileObject("build/test/classes/pkg/Source7Test.class"));
        assertTrue(output.contains("Source7Test test executed"));
        TestFileUtils.writeFile(dir, "src/RefersToExcluded1a.java", "class RefersToExcluded1a {{new pkg.Source11();}}");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile"}, p));
        TestFileUtils.writeFile(dir, "src/RefersToExcluded1b.java", "class RefersToExcluded1b {{new pkg.Source10();}}");
        assertBuildFailure(ActionUtils.runTarget(buildXml, new String[] {"compile"}, p));
    }

    /** @see "issue #36033" */
    @RandomlyFails // assertNotNull failed in NB-Core-Build #2418:
                   // java.lang.IllegalStateException: WARNING(please REPORT):  Externally created file: <workdir>/build/classes/p/Y.class  (For additional information see: http://wiki.netbeans.org/wiki/view/FileSystems)
                   //         at org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory.printWarning(FileObjectFactory.java:243)
                   //         at org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory.checkCacheState(FileObjectFactory.java:226)
                   //         at org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory.issueIfExist(FileObjectFactory.java:328)
                   //         at org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory.getFileObject(FileObjectFactory.java:193)
                   //         at org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory.getValidFileObject(FileObjectFactory.java:630)
                   //         at org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.getFileObject(FolderObj.java:110)
                   //         at org.netbeans.modules.java.j2seproject.BuildImplTest.testCompileWithDependencyAnalysis(BuildImplTest.java:376)
    public void testCompileWithDependencyAnalysis() throws Exception {
        AntProjectHelper aph = setupProject(0, false);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        FileObject d = aph.getProjectDirectory();
        FileObject x = TestFileUtils.writeFile(d, "src/p/X.java", "package p; public class X {static {Y.y1();}}");
        FileObject y = TestFileUtils.writeFile(d, "src/p/Y.java", "package p; public class Y {static void y1() {}}");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile"}, getProperties()));
        TestFileUtils.writeFile(d, "src/p/Y.java", "package p; public class Y {static void y2() {}}");
        TestFileUtils.touch(y, d.getFileObject("build/classes/p/Y.class"));
        assertBuildFailure(ActionUtils.runTarget(buildXml, new String[] {"compile"}, getProperties()));
        TestFileUtils.writeFile(d, "src/p/X.java", "package p; public class X {static {Y.y2();}}");
        TestFileUtils.touch(x, null);
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile"}, getProperties()));
        FileObject yt = TestFileUtils.writeFile(d, "test/p/YTest.java", "package p; public class YTest extends junit.framework.TestCase {public void testY() {Y.y2();}}");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-test"}, getProperties()));
        TestFileUtils.writeFile(d, "src/p/X.java", "package p; public class X {static {Y.y1();}}");
        TestFileUtils.touch(x, d.getFileObject("build/classes/p/X.class"));
        TestFileUtils.writeFile(d, "src/p/Y.java", "package p; public class Y {static void y1() {}}");
        assertNotNull(d.getFileObject("build/classes/p/Y.class"));
        TestFileUtils.touch(y, d.getFileObject("build/classes/p/Y.class"));
        assertBuildFailure(ActionUtils.runTarget(buildXml, new String[] {"compile-test"}, getProperties()));
        TestFileUtils.writeFile(d, "test/p/YTest.java", "package p; public class YTest extends junit.framework.TestCase {public void testY() {Y.y1();}}");
        TestFileUtils.touch(yt, null);
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile-test"}, getProperties()));
    }

    public void testRun() throws Exception {
        AntProjectHelper aph = setupProject(3, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        p.setProperty("main.class", "pkg.Source1");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"run"}, p));
        assertTrue("compile target was not executed", output.contains("compile:"));
        assertTrue("run target was not executed", output.contains("run:"));
        assertTrue("main class was not executed", output.contains("Source1 main class executed"));

        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source0.class must exist", fo.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull("build/classes/pkg/Source1.class must exist", fo.getFileObject("build/classes/pkg/Source1.class"));
        assertNotNull("build/classes/pkg/Source2.class must exist", fo.getFileObject("build/classes/pkg/Source2.class"));
        assertNull("build/test folder should not be created", fo.getFileObject("build/test"));
        assertNull("dist folder should not be created", fo.getFileObject("dist"));
    }

    public void testRunSingle() throws Exception {
        AntProjectHelper aph = setupProject(3, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        p.setProperty("main.class", "pkg.Source0");
        p.setProperty("javac.includes", "pkg/Source2.java");
        p.setProperty("run.class", "pkg.Source2");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"run-single"}, p));
        assertTrue("compile-single target was not executed", output.contains("compile-single:"));
        assertTrue("run target was not executed", output.contains("run-single:"));
        assertTrue("main class was not executed", output.contains("Source2 main class executed"));

        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source2.class must exist", fo.getFileObject("build/classes/pkg/Source2.class"));
        assertEquals("Only one class should be compiled", 1, fo.getFileObject("build/classes/pkg").getChildren().length);
        assertNull("build/test folder should not be created", fo.getFileObject("build/test"));
        assertNull("dist folder should not be created", fo.getFileObject("dist"));
    }

    public void testJar() throws Exception {
        AntProjectHelper aph = setupProject(2, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"jar"}, p));
        assertTrue("compile target was not executed", output.contains("compile:"));
        assertTrue("jar target was not executed", output.contains("jar:"));

        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source0.class must exist", fo.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull("build/classes/pkg/Source1.class must exist", fo.getFileObject("build/classes/pkg/Source1.class"));
        assertNull("build/test folder should not be created", fo.getFileObject("build/test"));
        assertNotNull("dist/testJar.jar must exist", fo.getFileObject("dist/testJar.jar"));
        assertNull("dist/javadoc fodler should not be created", fo.getFileObject("dist/javadoc"));
        Attributes mf = getJarManifest(fo.getFileObject("dist/testJar.jar"));
        assertNull("Main-class was not set", mf.getValue("Main-class"));

        // set a manifest

        TestFileUtils.writeFile(aph.getProjectDirectory(), "manifest/manifest.mf",
            "Manifest-Version: 1.0\n" +
            "Something: s.o.m.e\n\n");
        p.setProperty("manifest.file", "manifest/manifest.mf");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"jar"}, p));
        assertTrue("compile target was not executed", output.contains("compile:"));
        assertTrue("jar target was not executed", output.contains("jar:"));
        assertNull("build/test folder should not be created", fo.getFileObject("build/test"));
        assertNotNull("dist/testJar.jar must exist", fo.getFileObject("dist/testJar.jar"));
        assertNull("dist/javadoc fodler should not be created", fo.getFileObject("dist/javadoc"));
        mf = getJarManifest(fo.getFileObject("dist/testJar.jar"));
        assertEquals("Specified manifest was not used", "s.o.m.e", mf.getValue("Something"));
        assertNull("Main-class was not set", mf.getValue("Main-class"));

        // set a mainclass

        p.setProperty("main.class", "some.clazz.Main");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"jar"}, p));
        assertTrue("compile target was not executed", output.contains("compile:"));
        assertTrue("jar target was not executed", output.contains("jar:"));
        assertNull("build/test folder should not be created", fo.getFileObject("build/test"));
        assertNotNull("dist/testJar.jar must exist", fo.getFileObject("dist/testJar.jar"));
        assertNull("dist/javadoc fodler should not be created", fo.getFileObject("dist/javadoc"));
        mf = getJarManifest(fo.getFileObject("dist/testJar.jar"));
        assertEquals("Specified manifest was not used", "s.o.m.e", mf.getValue("Something"));
        assertEquals("Main-class was not set", "some.clazz.Main", mf.getValue("Main-class"));
    }

    public void testJavadoc() throws Exception {
        AntProjectHelper aph = setupProject(3, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"javadoc"}, p));
        assertTrue("javadoc target was not executed", output.contains("javadoc:"));

        FileObject fo = aph.getProjectDirectory();
        assertNull("build folder should not be created", fo.getFileObject("build"));
        assertNull("dist/testJavadoc.jar should not exist", fo.getFileObject("dist/testJavadoc.jar"));
        assertNotNull("dist/javadoc/index.html must exist", fo.getFileObject("dist/javadoc/index.html"));
    }
    public void testJavadocPackagesDocumented() throws Exception {
        AntProjectHelper aph = setupProject(0, false);
        TestFileUtils.writeFile(aph.getProjectDirectory(), "src/pkg1/A.java", "package pkg1; public class A {}");
        TestFileUtils.writeFile(aph.getProjectDirectory(), "src/pkg1/package.html", "<html><body>Floopy blint.</body></html>");
        TestFileUtils.writeFile(aph.getProjectDirectory(), "src/pkg2/B.java", "package pkg2; public class B {}");
        TestFileUtils.writeFile(aph.getProjectDirectory(), "src/pkg2/package-info.java", "/** Floppy blunt. */ package pkg2;");
        assertBuildSuccess(ActionUtils.runTarget(aph.getProjectDirectory().getFileObject("build.xml"), new String[] {"javadoc"}, getProperties()));
        TestFileUtils.assertContains(aph.resolveFileObject("dist/javadoc/pkg1/package-summary.html"), "Floopy blint.");
        TestFileUtils.assertContains(aph.resolveFileObject("dist/javadoc/pkg2/package-summary.html"), "Floppy blunt.");
    }
    public void testJavadocExcludedClassesAndPackagesNotDocumented() throws Exception { // part of #49026
        AntProjectHelper aph = setupProject(0, false);
        TestFileUtils.writeFile(aph.getProjectDirectory(), "src/incl/A.java", "package incl; public class A {}");
        TestFileUtils.writeFile(aph.getProjectDirectory(), "src/incl/A_.java", "package incl; public class A_ {}");
        TestFileUtils.writeFile(aph.getProjectDirectory(), "src/excl/B.java", "package excl; public class B {}");
        TestFileUtils.writeFile(aph.getProjectDirectory(), "src/other/C.java", "package other; public class C {}");
        EditableProperties ep = aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("includes", "*cl/");
        ep.setProperty("excludes", "excl/,**/*_*");
        aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(ProjectManager.getDefault().findProject(aph.getProjectDirectory()));
        assertBuildSuccess(ActionUtils.runTarget(aph.getProjectDirectory().getFileObject("build.xml"), new String[] {"javadoc"}, getProperties()));
        assertNotNull(aph.resolveFileObject("dist/javadoc/incl/A.html"));
        assertNull(aph.resolveFileObject("dist/javadoc/incl/A_.html"));
        assertNull(aph.resolveFileObject("dist/javadoc/excl"));
        assertNull(aph.resolveFileObject("dist/javadoc/other"));
    }
    // XXX cannot be made to work without breaking something else:
//    public void testJavadocSeeWorksOnPackages() throws Exception { // #57940
//        AntProjectHelper aph = setupProject(0, false);
//        TestFileUtils.writeFile(aph.getProjectDirectory(), "src/pkg1/A.java", "package pkg1; public class A {}");
//        TestFileUtils.writeFile(aph.getProjectDirectory(), "src/pkg2/B.java", "package pkg2; /** @see pkg1 */ public class B {}");
//        assertBuildSuccess(ActionUtils.runTarget(aph.getProjectDirectory().getFileObject("build.xml"), new String[] {"javadoc"}, getProperties()));
//        TestFileUtils.assertContains(aph.resolveFileObject("dist/javadoc/pkg2/B.html"), "../pkg1/package-summary.html");
//    }
    public void testJavadocNoDuplicatedClassNamesInIndex() throws Exception { // #102036
        AntProjectHelper aph = setupProject(0, false);
        TestFileUtils.writeFile(aph.getProjectDirectory(), "src/pkg1/A.java", "package pkg1; public class A {}");
        assertBuildSuccess(ActionUtils.runTarget(aph.getProjectDirectory().getFileObject("build.xml"), new String[] {"javadoc"}, getProperties()));
        String text = aph.resolveFileObject("dist/javadoc/allclasses-frame.html").asText();
        assertTrue(text.matches("(?s).*pkg1/A\\.html.*"));
        assertFalse(text.matches("(?s).*pkg1/A\\.html.*pkg1/A\\.html.*"));
    }

    public void testTest() throws Exception {
        AntProjectHelper aph = setupProject(2, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"test"}, p));
        assertTrue("compile target was not executed", output.contains("compile:"));
        assertTrue("compile-test target was not executed", output.contains("compile-test:"));
        assertTrue("test target was not executed", output.contains("test:"));
        assertTrue("test 0 was not executed: " + output, output.contains("Source0Test test executed"));
        assertTrue("test 1 was not executed", output.contains("Source1Test test executed"));

        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source0.class must exist", fo.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull("build/classes/pkg/Source1.class must exist", fo.getFileObject("build/classes/pkg/Source1.class"));
        assertNotNull("build/test/classes/pkg/Source0Test.class must exist", fo.getFileObject("build/test/classes/pkg/Source0Test.class"));
        assertNotNull("build/test/classes/pkg/Source1Test.class must exist", fo.getFileObject("build/test/classes/pkg/Source1Test.class"));
        assertNull("dist folder should not be created", fo.getFileObject("dist"));
    }

    public void testCompileSingleTest() throws Exception {
        AntProjectHelper aph = setupProject(3, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        p.setProperty("javac.includes", "pkg/Source2Test.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"compile-test-single"}, p));
        assertTrue("compile-single target was not executed", output.contains("compile:"));
        assertTrue("compile-single target was not executed", output.contains("compile-test-single:"));

        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source0.class must exist", fo.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull("build/classes/pkg/Source1.class must exist", fo.getFileObject("build/classes/pkg/Source1.class"));
        assertNotNull("build/classes/pkg/Source2.class must exist", fo.getFileObject("build/classes/pkg/Source2.class"));
        assertNotNull("build/test/classes/pkg/Source2Test.class must exist", fo.getFileObject("build/test/classes/pkg/Source2Test.class"));
        assertEquals("Only one test class should be compiled", 1, fo.getFileObject("build/test/classes/pkg").getChildren().length);
        assertNull("dist folder should not be created", fo.getFileObject("dist"));
    }

    public void testRunSingleTest() throws Exception {
        AntProjectHelper aph = setupProject(3, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        p.setProperty("javac.includes", "pkg/Source2Test.java");
        p.setProperty("test.includes", "pkg/Source2Test.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"test-single"}, p));
        assertTrue("compile target was not executed", output.contains("compile:"));
        assertTrue("run target was not executed", output.contains("compile-test-single:"));
        assertTrue("run target was not executed", output.contains("test-single:"));
        assertTrue("test was not executed", output.contains("Source2Test test executed"));

        FileObject fo = aph.getProjectDirectory();
        assertNotNull("build/classes/pkg/Source0.class must exist", fo.getFileObject("build/classes/pkg/Source0.class"));
        assertNotNull("build/classes/pkg/Source1.class must exist", fo.getFileObject("build/classes/pkg/Source1.class"));
        assertNotNull("build/classes/pkg/Source2.class must exist", fo.getFileObject("build/classes/pkg/Source2.class"));
        assertNotNull("build/test/classes/pkg/Source2Test.class must exist", fo.getFileObject("build/test/classes/pkg/Source2Test.class"));
        /* No longer true as of #97053:
        assertEquals("Only one test class should be compiled", 1, fo.getFileObject("build/test/classes/pkg").getChildren().length);
         */
        assertNull("dist folder should not be created", fo.getFileObject("dist"));
    }

    public void testRunSingleTestWithDep() throws Exception { // #97053
        AntProjectHelper aph = setupProject(1, false);
        FileObject root = aph.getProjectDirectory();
        TestFileUtils.writeFile(root, "test/pkg/TestUtil.java", "package pkg; class TestUtil {}");
        TestFileUtils.writeFile(root, "test/pkg/SomeTest.java", "package pkg; public class SomeTest extends junit.framework.TestCase {public void testX() {new Source0(); new TestUtil();}}");
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        Properties p = getProperties();
        p.setProperty("javac.includes", "pkg/SomeTest.java");
        p.setProperty("test.includes", "pkg/SomeTest.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"test-single"}, p));
        assertTrue("wrong default suite name: " + output, output.contains("testRunSingleTestWithDep"));
        assertTrue("wrong tests executed", output.contains("Total tests run: 1, Failures: 0, Skips: 0"));
    }

    public void testSubprojects() throws Exception {
        AntProjectHelper aph1 = setupProject("p1", 1, false);
        AntProjectHelper aph2 = setupProject("p2", 1, false);
        Project proj1 = ProjectManager.getDefault().findProject(aph1.getProjectDirectory());
        Project proj2 = ProjectManager.getDefault().findProject(aph2.getProjectDirectory());
        ReferenceHelper refHelper = ((J2SEProject)proj1).getReferenceHelper();
        AntArtifactProvider aap = proj2.getLookup().lookup(AntArtifactProvider.class);
        AntArtifact[] aa = aap.getBuildArtifacts();
        assertTrue("Project should have an artifact", aa.length > 0);
        refHelper.addReference(aa[0], aa[0].getArtifactLocations()[0]);
        ProjectManager.getDefault().saveAllProjects();
        FileObject fo = aph1.getProjectDirectory();
        assertNull("build folder cannot exist", fo.getFileObject("build"));
        assertNull("dist folder cannot exist", fo.getFileObject("dist"));
        fo = aph2.getProjectDirectory();
        assertNull("build folder cannot exist", fo.getFileObject("build"));
        assertNull("dist folder cannot exist", fo.getFileObject("dist"));

        FileObject buildXml = aph1.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        p.setProperty("no.dependencies", "true");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"jar"}, p));
        assertTrue("jar target was not executed", output.contains("jar:"));
        output.remove("jar:");
        assertFalse("subproject's jar should not be executed", output.contains("jar:"));
        fo = aph1.getProjectDirectory();
        fo.refresh();
        assertNotNull("build folder must exist", fo.getFileObject("build"));
        assertNotNull("dist folder must exist", fo.getFileObject("dist"));
        fo = aph2.getProjectDirectory();
        assertNull("build folder cannot exist", fo.getFileObject("build"));
        assertNull("dist folder cannot exist", fo.getFileObject("dist"));

        p.setProperty("no.dependencies", "false");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"jar"}, p));
        assertTrue("jar target was not executed", output.contains("jar:"));
        output.remove("jar:");
        assertTrue("subproject's jar target was not executed", output.contains("p2.jar:"));
        fo = aph1.getProjectDirectory();
        fo.refresh();
        assertNotNull("build folder must exist", fo.getFileObject("build"));
        assertNotNull("dist folder must exist", fo.getFileObject("dist"));
        fo = aph2.getProjectDirectory();
        fo.refresh();
        assertNotNull("build folder must exist", fo.getFileObject("build"));
        assertNotNull("dist folder must exist", fo.getFileObject("dist"));

        p.setProperty("no.dependencies", "true");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"clean"}, p));
        assertTrue("clean target was not executed", output.contains("clean:"));
        output.remove("clean:");
        assertFalse("subproject's clean should not be executed", output.contains("p2.clean:"));
        fo = aph1.getProjectDirectory();
        fo.refresh();
        assertNull("build folder cannot exist", fo.getFileObject("build"));
        assertNull("dist folder cannot exist", fo.getFileObject("dist"));
        fo = aph2.getProjectDirectory();
        fo.refresh();
        assertNotNull("build folder must exist", fo.getFileObject("build"));
        assertNotNull("dist folder must exist", fo.getFileObject("dist"));

        p.setProperty("no.dependencies", "false");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[]{"clean"}, p));
        assertTrue("clean target was not executed", output.contains("clean:"));
        output.remove("clean:");
        assertTrue("subproject's clean target was not executed", output.contains("p2.clean:"));
        fo = aph1.getProjectDirectory();
        fo.refresh();
        assertNull("build folder must be removed", fo.getFileObject("build"));
        assertNull("dist folder must be removed", fo.getFileObject("dist"));
        fo = aph2.getProjectDirectory();
        fo.refresh();
        assertNull("build folder must be removed", fo.getFileObject("build"));
        assertNull("dist folder must be removed", fo.getFileObject("dist"));
    }

    public void testDiamondBuilds() throws Exception { // #42683
        // A -> B -> C
        // v \  v \  v
        // D -> E -> F
        // v \  v \  v
        // G -> H -> I
        J2SEProject a = mkprj("a");
        J2SEProject b = mkprj("b");
        J2SEProject c = mkprj("c");
        J2SEProject d = mkprj("d");
        J2SEProject e = mkprj("e");
        J2SEProject f = mkprj("f");
        J2SEProject g = mkprj("g");
        J2SEProject h = mkprj("h");
        J2SEProject i = mkprj("i");
        addDep(a, b);
        addDep(b, c);
        addDep(d, e);
        addDep(e, f);
        addDep(g, h);
        addDep(h, i);
        addDep(a, d);
        addDep(d, g);
        addDep(b, e);
        addDep(e, h);
        addDep(c, f);
        addDep(f, i);
        addDep(a, e);
        addDep(b, f);
        addDep(d, h);
        addDep(e, i);
        ProjectManager.getDefault().saveAllProjects();
        File dir = getWorkDir();
        assertBuildSuccess(ActionUtils.runTarget(a.getProjectDirectory().getFileObject("build.xml"), new String[] {"jar"}, null));
        assertTrue(new File(dir, "a/dist/a.jar").isFile());
        assertTrue(new File(dir, "i/dist/i.jar").isFile());
        assertEquals(9, countOfOutput("jar:"));
        output.clear();
        assertBuildSuccess(ActionUtils.runTarget(a.getProjectDirectory().getFileObject("build.xml"), new String[] {"jar"}, null));
        assertEquals(9, countOfOutput("jar:"));
        output.clear();
        assertBuildSuccess(ActionUtils.runTarget(a.getProjectDirectory().getFileObject("build.xml"), new String[] {"clean"}, null));
        assertFalse(new File(dir, "a/dist/a.jar").isFile());
        assertFalse(new File(dir, "i/dist/i.jar").isFile());
        assertEquals(9, countOfOutput("clean:"));
        output.clear();
        assertBuildSuccess(ActionUtils.runTarget(a.getProjectDirectory().getFileObject("build.xml"), new String[] {"clean", "jar"}, null));
        assertTrue(new File(dir, "a/dist/a.jar").isFile());
        assertTrue(new File(dir, "i/dist/i.jar").isFile());
        assertEquals(9, countOfOutput("clean:"));
        assertEquals(9, countOfOutput("jar:"));
    }

    public void testRecompileDependencyInRunSingle() throws Exception {
        final AntProjectHelper aph = setupProject(0, false);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        FileObject d = aph.getProjectDirectory();
        FileObject x = TestFileUtils.writeFile(d, "src/p/Main.java", "package p; public class Main { public static void main (String[] args) {System.out.println(Test.getMessage());}}");
        FileObject y = TestFileUtils.writeFile(d, "src/p/Test.java", "package p; public class Test { public static String getMessage() {return \"__Test__\";}}");
        Properties p = new Properties(getProperties());
        p.setProperty("run.class", "p.Main");
        p.setProperty("javac.includes","p/Main.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"run-single"}, p));
        boolean found = false;
        synchronized (output) {
            for (String line : output) {
                if ("__Test__".equals(line)) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found);
        Thread.sleep(5000); //Give fs some time to assign higher time stamp (2s on Win)
        y = TestFileUtils.writeFile(d, "src/p/Test.java", "package p; public class Test { public static String getMessage() {return \"__TestNew__\";}}");
        p = new Properties(getProperties());
        p.setProperty("run.class", "p.Main");
        p.setProperty("javac.includes","p/Main.java");
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"run-single"}, p));
        found = false;
        synchronized (output) {
            for (String line : output) {
                if ("__TestNew__".equals(line)) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found);
    }

    public void testCyclicBuildWarnings() throws Exception { // #174799
        AntProjectHelper aph = setupProject(1, true);
        FileObject buildXml = aph.getProjectDirectory().getFileObject("build.xml");
        assertNotNull("Must have build.xml", buildXml);
        Properties p = getProperties();
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"jar", "test"}, p));
        assertOutput("Cycle detected: testCyclicBuildWarnings was already built");
    }

    private J2SEProject mkprj(String name) throws Exception {
        return (J2SEProject) ProjectManager.getDefault().findProject(setupProject(name, 1, false).getProjectDirectory());
    }
    private void addDep(J2SEProject p1, J2SEProject p2) {
        AntArtifact[] aa = p2.getLookup().lookup(AntArtifactProvider.class).getBuildArtifacts();
        p1.getReferenceHelper().addReference(aa[0], aa[0].getArtifactLocations()[0]);
    }
    private int countOfOutput(String expectedLine) {
        int cnt = 0;
        synchronized (output) {
            for (String line : output) {
                if (line.replaceFirst("^.+[.](?=.+:$)", "").equals(expectedLine)) {
                    cnt++;
                }
            }
        }
        return cnt;
    }

    /* XXX: impossible to test currently, because J2SEActionProvider.invokeAction must be called, but that is nonblocking.
    public void testFirstBuildAfterBrokenCleanBuild() throws Exception {
        // #120843. Should really in J2SEActionProviderTest but needs infrastructure of running builds.
        MockLookup.setInstances(new IOP(), new IFL(), new J2SEActionProviderTest.SimplePlatformProvider()); // may need to be in setUp
        AntProjectHelper aph = setupProject(0, false);
        EditableProperties ep = aph.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.put(J2SEProjectProperties.DO_DEPEND, "false");
        ep.put(J2SEProjectProperties.DO_JAR, "false");
        aph.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(ProjectManager.getDefault().findProject(aph.getProjectDirectory()));
        FileObject root = aph.getProjectDirectory();
        FileObject buildXml = root.getFileObject("build.xml");
        Properties p = getProperties();
        FileObject a = TestFileUtils.writeFile(root, "src/A.java", "class A {}");
        FileObject b = TestFileUtils.writeFile(root, "src/B.java", "class B {A v;}broken");
        J2SEActionProvider actionProvider = ProjectManager.getDefault().findProject(root).getLookup().lookup(J2SEActionProvider.class);
        assertEquals("[compile]", Arrays.toString(actionProvider.getTargetNames(ActionProvider.COMMAND_BUILD, Lookup.EMPTY, p)));
        assertEquals(null, p.get("includes"));
        assertBuildFailure(ActionUtils.runTarget(buildXml, new String[] {"compile"}, p));
        assertNull(root.getFileObject("build/classes/A.class"));
        assertNull(root.getFileObject("build/classes/B.class"));
        TestFileUtils.writeFile(root, "src/B.java", "class B {A v;}");
        TestFileUtils.touch(b, null);
        p = getProperties();
        assertEquals("[compile]", Arrays.toString(actionProvider.getTargetNames(ActionProvider.COMMAND_BUILD, Lookup.EMPTY, p)));
        assertEquals(null, p.get("includes"));
        assertBuildSuccess(ActionUtils.runTarget(buildXml, new String[] {"compile"}, p));
    }
     */

    private Attributes getJarManifest(FileObject fo) throws Exception {
        File f = FileUtil.toFile(fo);
        JarFile jf = new JarFile(f);
        Attributes attrs = (Attributes)jf.getManifest().getMainAttributes().clone();
        jf.close();
        return attrs;
    }

    private void assertBuildSuccess(ExecutorTask task) {
        if (task.result() != 0) {
            dumpOutput();
            fail("target failed");
        }
    }

    private void assertBuildFailure(ExecutorTask task) {
        if (task.result() == 0) {
            dumpOutput();
            fail("target failed");
        }
    }

    private void dumpOutput() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        synchronized (output) {
            for (String line : output) {
                System.out.println(line);
            }
        }
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    private void assertOutput(String line) {
        synchronized (output) {
            int newpos = output.size();
            if (!output.subList(outputPosition, newpos).contains(line)) {
                dumpOutput();
                fail("looking for '" + line + "' starting at line #" + (outputPosition + 1));
            }
            outputPosition = newpos;
        }
    }

    @SuppressWarnings("deprecation")
    public static final class IOP extends IOProvider implements InputOutput {

        public IOP() {}

        public InputOutput getIO(String name, boolean newIO) {
            return this;
        }

        public OutputWriter getStdOut() {
            throw new UnsupportedOperationException();
        }

        public OutputWriter getOut() {
            return new OW(false);
        }

        public OutputWriter getErr() {
            return new OW(true);
        }

        public Reader getIn() {
            return new StringReader("");
        }

        public Reader flushReader() {
            return getIn();
        }

        public void closeInputOutput() {}

        public boolean isClosed() {
            return false;
        }

        public boolean isErrSeparated() {
            return false;
        }

        public boolean isFocusTaken() {
            return false;
        }

        public void select() {}

        public void setErrSeparated(boolean value) {}

        public void setErrVisible(boolean value) {}

        public void setFocusTaken(boolean value) {}

        public void setInputVisible(boolean value) {}

        public void setOutputVisible(boolean value) {}

    }

    //@GuardedBy("output")
    private static final List<String> output = Collections.synchronizedList(new ArrayList<String>());
    //@GuardedBy("output")
    private static int outputPosition;

    private static final class OW extends OutputWriter {

        private final boolean err;

        public OW(boolean err) {
            super(new StringWriter());
            this.err = err;
        }

        public void println(String s, OutputListener l) throws IOException {
            message(s, l != null);
        }

        public @Override void println(String x) {
            message(x, false);
        }

        private void message(String msg, boolean hyperlinked) {
            output.add(msg);
        }

        public void reset() throws IOException {}

    }

    /** Copied from AntLoggerTest. */
    public static final class IFL extends InstalledFileLocator {
        public IFL() {}
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if (relativePath.equals("ant/nblib/bridge.jar")) {
                String path = System.getProperty("test.bridge.jar");
                assertNotNull("must set test.bridge.jar", path);
                return new File(path);
            } else if (relativePath.equals("ant")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path);
            } else if (relativePath.startsWith("ant/")) {
                String path = System.getProperty("test.ant.home");
                assertNotNull("must set test.ant.home", path);
                return new File(path, relativePath.substring(4).replace('/', File.separatorChar));
            } else {
                return null;
            }
        }
    }

}
