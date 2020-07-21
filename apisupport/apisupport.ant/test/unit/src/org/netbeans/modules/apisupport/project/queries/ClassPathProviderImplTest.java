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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Manifest;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.TestAntLogger;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.universe.ClusterUtils;
import org.netbeans.modules.apisupport.project.ModuleDependency;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.test.TestFileUtils;

// XXX test GPR usage

/**
 * Test functionality of {@link ClassPathProviderImpl}.
 * @author Jesse Glick
 */
public class ClassPathProviderImplTest extends TestBase {

    public ClassPathProviderImplTest(String name) {
        super(name);
    }
    
    private File copyOfSuite2;
    private FileObject copyOfMiscDir;
    private NbModuleProject copyOfMiscProject;
    private ProjectXMLManager copyOfMiscXMLManager;
    private File fooJar;
    private File barJar;
    private File libsJunitJar;
    
    private static final Logger LOG = Logger.getLogger(ClassPathProviderImplTest.class.getName());
    
    protected @Override void setUp() throws Exception {
        noDataDir = true;
        clearWorkDir();
        super.setUp();
        copyOfSuite2 = copyFolder(resolveEEPFile("/suite2"));
        File miscF = new File(copyOfSuite2, "misc-project");
        copyOfMiscDir = FileUtil.toFileObject(miscF);
        copyOfMiscProject = (NbModuleProject) ProjectManager.getDefault().findProject(copyOfMiscDir);
        assertNotNull(copyOfMiscProject);
        copyOfMiscXMLManager = new ProjectXMLManager(copyOfMiscProject);
        // make sure its platform-private.properties is correct:
        Project copyOfSuite2P = ProjectManager.getDefault().findProject(FileUtil.toFileObject(copyOfSuite2));
        ((SuiteProject) copyOfSuite2P).open();
    }
    
    private String urlForJar(String path) {
        File fp = new File(path);
        fp = fp.isAbsolute() ? fp : PropertyUtils.resolveFile(nbRootFile(), path);
        return FileUtil.urlForArchiveOrDir(fp).toExternalForm();
    }
    
    private String urlForDir(String path) {
        return FileUtil.urlForArchiveOrDir(file(path)).toExternalForm();
    }
    
    private Set<String> urlsOfCp(ClassPath cp) {
        Set<String> s = new TreeSet<String>();
        for (ClassPath.Entry entry : cp.entries()) {
            s.add(entry.getURL().toExternalForm());
        }
        return s;
    }
    
    /**
     * Asserts when (sorted) sequences are not equal
     * and prints CP entries.
     */
    private <T> void assertEquals(String msg, Iterable<T> expSeq, Iterable<T> actSeq) {
        int i = 0, cnt = 0;
        boolean dif = false;
        Iterator<T> it1 = expSeq.iterator(), it2 = actSeq.iterator();
        StringBuilder sb1 = new StringBuilder(), sb2 = new StringBuilder();
        
        while (it1.hasNext() || it2.hasNext()) {
            T t1 = null, t2 = null;
            if (it1.hasNext()) {
                t1 = it1.next();
                sb1.append("\t").append(t1.toString()).append("\n");
            } else {
                dif = true;
            }
            if (it2.hasNext()) {
                t2 = it2.next();
                sb2.append("\t").append(t2.toString()).append("\n");
            } else {
                dif = true;
            }
            if (!dif && !t1.equals(t2)) {
                dif = true;
            }
            if (!dif) {
                i++;
            }
            cnt++;
        }
        if (!dif) {
            return;
        }
        String failMsg = String.format("Comparison failed: %1$s\nClasspaths differ at entry #%4$d.\nExpected:\n%2$sActual:\n%3$s",
                msg, sb1, sb2, i);
        fail(failMsg);
    }
    
    private static final Set<String> TESTLIBS = new HashSet<String>(/* no more default test libs after issue #171616 */);
    
    private Set<String> urlsOfCp4Tests(ClassPath cp) throws Exception {
        Set<String> s = new TreeSet<String>();
        for (ClassPath.Entry entry : cp.entries()) {
            String url = entry.getURL().toExternalForm();
            if (url.indexOf("$%7B") != -1) {
                // Unevaluated Ant reference (after octet escaping), so skip.
                continue;
            }
            String simplifiedJarName = url.replaceFirst("^.+/([^/]+?)[0-9_.-]*\\.jar!/$", "$1.jar");
            if (TESTLIBS.contains(simplifiedJarName)) {
                s.add(simplifiedJarName);
            } else {
                // XXX String relativeJarName = url.replace(getWorkDir().toURL().toExternalForm(), "");
                s.add(url);
            }
        }
        return s;
    }

//    XXX: failing test, fix or delete
//    public void testMainClasspath() throws Exception {
//        FileObject src = nbRoot().getFileObject("o.apache.tools.ant.module/src");
//        assertNotNull("have o.apache.tools.ant.module/src", src);
//        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
//        assertNotNull("have a COMPILE classpath", cp);
//        Set<String> expectedRoots = new TreeSet<String>();
//        // Keep up to date w/ changes in o.apache.tools.ant.module/nbproject/project.{xml,properties}:
//        // ${module.classpath}:
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-api-xml.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-modules.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-awt.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-text.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-actions.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-execution.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-io.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-explorer.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-spi-navigator.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-jdesktop-layout.jar"));
//        expectedRoots.add(urlForJar("o.jdesktop.layout/external/swing-layout-1.0.3.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-options-api.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-api-progress.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-projectapi.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-projectuiapi.jar"));
//        assertEquals("right COMPILE classpath for o.apache.tools.ant.module/src", expectedRoots, urlsOfCp(cp));
//        cp = ClassPath.getClassPath(src, ClassPath.EXECUTE);
//        assertNotNull("have an EXECUTE classpath", cp);
//        // #48099: need to include build/classes here too
//        expectedRoots.add(urlForDir("o.apache.tools.ant.module/build/classes"));
//        // And #70206: transitive runtime deps too.
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-queries.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-editor-mimelookup.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-swing-outline.jar"));
//        assertEquals("right EXECUTE classpath (COMPILE plus classes)", expectedRoots, urlsOfCp(cp));
//        cp = ClassPath.getClassPath(src, ClassPath.SOURCE);
//        assertNotNull("have a SOURCE classpath", cp);
//        assertEquals("right SOURCE classpath", Collections.singleton(src), new HashSet<FileObject>(Arrays.asList(cp.getRoots())));
//    }

    /**
     * Generates following platfrom:
     * * Binary platform labelled "custom"
     *   * harness
     *     * org-netbeans-modules-nbjunit.jar
     *     * org-netbeans-insane.jar
     *     * org-netbeans-modules-apisupport-harness.jar
     *   * platform
     *     * core/core.jar
     *     * ext/junit-4.5.jar (not a NBM module)
     *     * org-netbeans-libs-junit4.jar (with junit 4.5 on CP)
     *   * somecluster
     *     * foo.jar (CNB foo)
     *     * bar.jar (CNB org.example.bar, runtime dep on foo.jar)
     */
    private void generateTestingPlatform() throws Exception {
        File install = new File(getWorkDir(), "install");
        TestBase.makePlatform(install);
        // MODULE foo
        Manifest mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "foo/1");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION, "1.0");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_IMPLEMENTATION_VERSION, "foo-1");
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE_LOCALIZING_BUNDLE, "foo/Bundle.properties");
        Map<String,String> contents = new HashMap<String,String>();
        contents.put("foo/Bundle.properties", "OpenIDE-Module-Name=Foo Module");
        fooJar = new File(new File(new File(install, "somecluster"), "modules"), "foo.jar");
        TestBase.createJar(fooJar, contents, mani);
        // MODULE bar
        mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "org.example.bar");
        mani.getMainAttributes().putValue("OpenIDE-Module-Module-Dependencies", "foo/1 > 1.0");
        barJar = new File(new File(new File(install, "somecluster"), "modules"), "bar.jar");
        TestBase.createJar(barJar, Collections.<String,String>emptyMap(), mani);
        // add testlibs to platform, so that test CP isn't full of obsolete backward-compatibility entries
        mani = new Manifest();
        File junitJar = new File(install, "platform/modules/ext/junit-4.5.jar");
        TestBase.createJar(junitJar, Collections.<String,String>emptyMap(), mani);
        mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "org.netbeans.libs.junit4");
        mani.getMainAttributes().putValue(ManifestManager.CLASS_PATH, "ext/junit-4.5.jar");
        libsJunitJar = new File(install, "platform/modules/org-netbeans-libs-junit4.jar");
        TestBase.createJar(libsJunitJar, Collections.<String,String>emptyMap(), mani);
        mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "org.netbeans.modules.nbjunit");
        File nbjunitJar = new File(install, "harness/modules/org-netbeans-modules-nbjunit.jar");
        TestBase.createJar(nbjunitJar, Collections.<String,String>emptyMap(), mani);
        mani = new Manifest();
        mani.getMainAttributes().putValue(ManifestManager.OPENIDE_MODULE, "org.netbeans.insane");
        File insaneJar = new File(install, "harness/modules/org-netbeans-insane.jar");
        TestBase.createJar(insaneJar, Collections.<String,String>emptyMap(), mani);
        NbPlatform.addPlatform("custom", install, "custom");
    }

    /**
     * Testing that whole moudule.run.classpath is present on both compile and runtime test CPs
     * @throws Exception
     */
    public void testUnitTestClasspaths165446() throws Exception {
        // PLATFORM SETUP
        generateTestingPlatform();

        // SUITE setup
        SuiteProject suite = TestBase.generateSuite(getWorkDir(), "suite", "custom");
        // MODULE org.example.module1
        NbModuleProject module1 = generateTestingSuiteComponent(suite, "module1",
                "<dependency>\n" +
                "<code-name-base>org.example.bar</code-name-base>" +
                "<build-prerequisite/>\n<compile-dependency/>\n<run-dependency/>\n" +
                "</dependency>", "", "");

        Set<String> expectedRoots = new TreeSet<String>();
        expectedRoots.addAll(TESTLIBS);
        expectedRoots.add(urlForJar(module1.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(fooJar.getAbsolutePath()));
        expectedRoots.add(urlForJar(barJar.getAbsolutePath()));

        ClassPathProvider cpp = module1.getLookup().lookup(ClassPathProvider.class);
        ClassPath cp = cpp.findClassPath(module1.getTestSourceDirectory("unit"), ClassPath.COMPILE);
        // path to compiled tests is a bit tricky, evaluator won't tell us path to testdist dir
        assertEquals("correct UNIT TEST COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));
        assertEquals("UNIT TEST COMPILE cp too big, probably duplicate entries", expectedRoots.size(), cp.entries().size());
        cp = cpp.findClassPath(module1.getTestSourceDirectory("unit"), ClassPath.EXECUTE);
        expectedRoots.add(FileUtil.urlForArchiveOrDir(module1.getTestClassesDirectory("unit")).toExternalForm());
        assertEquals("correct UNIT TEST EXECUTE classpath", expectedRoots, urlsOfCp4Tests(cp));
        assertEquals("UNIT TEST EXECUTE cp too big, probably duplicate entries", expectedRoots.size(), cp.entries().size());
    }

    /**
     * #52354: interpret <class-path-extension>s both in myself and in dependent modules.
     */
    public void testClasspathExtensions() throws Exception {
        SuiteProject suite = generateSuite("testSuite2");
        NbModuleProject prjFoo = generateTestingSuiteComponent(suite, "foo",
                "", "",
                "<class-path-extension>\n" +
                "<runtime-relative-path>ext/foolib.jar</runtime-relative-path>\n" +
                "<binary-origin>external/foolib.jar</binary-origin>\n" +
                "</class-path-extension>");
        File ext = new File(prjFoo.getProjectDirectoryFile(), "external");
        assertTrue(ext.mkdirs());
        Manifest mani = new Manifest();
        File foolibJar = new File(ext, "/foolib.jar");
        TestBase.createJar(foolibJar, Collections.<String,String>emptyMap(), mani);

        NbModuleProject prjBar = generateTestingSuiteComponent(suite,"bar",
                "<dependency>\n" +
                "<code-name-base>org.example.foo</code-name-base>" +
                "<build-prerequisite/>\n<compile-dependency/>\n<run-dependency/>\n" +
                "</dependency>", "", "");

        Set<String> expectedRootsFoo = new TreeSet<String>();
        expectedRootsFoo.add(urlForJar(foolibJar.getPath()));
        Set<String> expectedRootsBar = new TreeSet<String>(expectedRootsFoo);

        ClassPathProvider cpp = prjFoo.getLookup().lookup(ClassPathProvider.class);
        ClassPath cp = cpp.findClassPath(prjFoo.getSourceDirectory(), ClassPath.COMPILE);
        assertEquals("correct COMPILE classpath", expectedRootsFoo, urlsOfCp4Tests(cp));
        assertEquals("COMPILE cp too big, probably duplicate entries", expectedRootsFoo.size(), cp.entries().size());
        expectedRootsFoo.add(FileUtil.urlForArchiveOrDir(prjFoo.getClassesDirectory()).toExternalForm());
        cp = ClassPath.getClassPath(prjFoo.getSourceDirectory(), ClassPath.EXECUTE);
        assertEquals("correct EXECUTE classpath", expectedRootsFoo, urlsOfCp4Tests(cp));
        assertEquals("EXECUTE cp too big, probably duplicate entries", expectedRootsFoo.size(), cp.entries().size());

        expectedRootsBar.add(urlForJar(prjFoo.getModuleJarLocation().getPath()));
        cpp = prjBar.getLookup().lookup(ClassPathProvider.class);
        cp = cpp.findClassPath(prjBar.getSourceDirectory(), ClassPath.COMPILE);
        assertEquals("correct COMPILE classpath", expectedRootsBar, urlsOfCp4Tests(cp));
        assertEquals("COMPILE cp too big, probably duplicate entries", expectedRootsBar.size(), cp.entries().size());
        expectedRootsBar.add(FileUtil.urlForArchiveOrDir(prjBar.getClassesDirectory()).toExternalForm());
        cp = cpp.findClassPath(prjBar.getSourceDirectory(), ClassPath.EXECUTE);
        assertEquals("correct EXECUTE classpath", expectedRootsBar, urlsOfCp4Tests(cp));
        assertEquals("EXECUTE cp too big, probably duplicate entries", expectedRootsBar.size(), cp.entries().size());
    }

    public void testMainClasspathExternalModules() throws Exception {
        FileObject src = resolveEEP("suite3/dummy-project/src");
        assertNotNull("have .../dummy-project/src", src);
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set<String> expectedRoots = new TreeSet<>();
        expectedRoots.add(urlForJar(resolveEEPPath("/suite3/nbplatform/random/modules/random.jar")));
        expectedRoots.add(urlForJar(resolveEEPPath("/suite3/nbplatform/random/modules/ext/stuff.jar")));
        assertEquals("right COMPILE classpath", expectedRoots, urlsOfCp(cp));
    }
    
    /**
     * #52354: interpret <class-path-extension>s both in myself and in dependent modules.
     */
    /* XXX uses obsolete module:
    public void testClasspathExtensions() throws Exception {
        // java/javacore has its own <class-path-extension> and uses others from dependents.
        FileObject src = nbCVSRoot().getFileObject("java/javacore/src");
        assertNotNull("have java/javacore/src", src);
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set<String> expectedRoots = new TreeSet<String>();
        // Keep up to date w/ changes in java/javacore/nbproject/project.xml:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-api-java.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-classfile.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/extra/modules/org-netbeans-jmi-javamodel.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/javax-jmi-reflect.jar"));
        expectedRoots.add(urlForJar("mdr/external/jmi.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/javax-jmi-model.jar"));
        expectedRoots.add(urlForJar("mdr/external/mof.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-api-mdr.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-projectapi.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-api-progress.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-mdr.jar"));
        expectedRoots.add(urlForJar("mdr/dist/mdr.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-jmiutils.jar"));
        expectedRoots.add(urlForJar("mdr/jmiutils/dist/jmiutils.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-modules.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-awt.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-text.jar"));
        expectedRoots.add(urlForJar("java/parser/dist/java-parser.jar"));
        assertEquals("right COMPILE classpath", expectedRoots, urlsOfCp(cp));
        assertTrue("#76341: EXECUTE classpath also has extension",
                urlsOfCp(ClassPath.getClassPath(src, ClassPath.EXECUTE)).contains(urlForJar("java/parser/dist/java-parser.jar")));
    }
     */

    /* XXX would need to include ${ant.core.lib}, never mind:
    public void testExtraCompilationUnits() throws Exception {
        FileObject srcbridge = nbRoot().getFileObject("o.apache.tools.ant.module/src-bridge");
        assertNotNull("have o.apache.tools.ant.module/src-bridge", srcbridge);
        ClassPath cp = ClassPath.getClassPath(srcbridge, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set<String> expectedRoots = new TreeSet<String>();
        // Keep up to date w/ changes in o.apache.tools.ant.module/nbproject/project.{xml,properties}:
        expectedRoots.add(urlForDir("o.apache.tools.ant.module/build/classes"));
        expectedRoots.add(urlForJar("o.apache.tools.ant.module/external/lib/ant.jar"));
        // ${module.classpath}:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-api-xml.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-modules.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-awt.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-text.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-actions.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-execution.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-io.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-explorer.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-spi-navigator.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-jdesktop-layout.jar"));
        expectedRoots.add(urlForJar("o.jdesktop.layout/external/swing-layout-1.0.3.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-options-api.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-api-progress.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-projectapi.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_IDE + "/modules/org-netbeans-modules-projectuiapi.jar"));
        assertEquals("right COMPILE classpath for o.apache.tools.ant.module/src-bridge", expectedRoots, urlsOfCp(cp));
        cp = ClassPath.getClassPath(srcbridge, ClassPath.EXECUTE);
        assertNotNull("have an EXECUTE classpath", cp);
        expectedRoots.add(urlForDir("o.apache.tools.ant.module/build/bridge-classes"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_JAVA + "/ant/nblib/bridge.jar"));
        assertEquals("right EXECUTE classpath (COMPILE plus classes plus JAR)", expectedRoots, urlsOfCp(cp));
        cp = ClassPath.getClassPath(srcbridge, ClassPath.SOURCE);
        assertNotNull("have a SOURCE classpath", cp);
        assertEquals("right SOURCE classpath", Collections.singleton(srcbridge), new HashSet<FileObject>(Arrays.asList(cp.getRoots())));
    }
     */

    private SuiteProject testSuite;
    private NbModuleProject modA;
    private NbModuleProject modB;
    private NbModuleProject modC;
    private NbModuleProject modT;

    public void testSimpleRuntimeTestDependency() throws Exception {
        generateTestingSuite();
        NbModuleProject modD = generateTestingSuiteComponent(testSuite,"d", "",
                "<test-dependency>\n" +
                "<code-name-base>org.example.a</code-name-base>\n" +
//                "<recursive/>\n" +
//                "<compile-dependency/>\n" +
//                "<test/>\n" +
                "</test-dependency>\n", "");

        ClassPathProvider cpp = modD.getLookup().lookup(ClassPathProvider.class);
        ClassPath cp = cpp.findClassPath(modD.getTestSourceDirectory("unit"), ClassPath.COMPILE);
        Set<String> expectedRoots = new TreeSet<String>();
        expectedRoots.addAll(TESTLIBS);
        expectedRoots.add(urlForJar(modD.getModuleJarLocation().getPath()));
        assertEquals("correct TEST COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));

        cp = cpp.findClassPath(modD.getTestSourceDirectory("unit"), ClassPath.EXECUTE);
        expectedRoots = new TreeSet<String>();
        expectedRoots.addAll(TESTLIBS);
        expectedRoots.add(urlForJar(modD.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(modA.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(modD.getTestClassesDirectory("unit").getPath()));
        assertEquals("correct TEST EXECUTE classpath", expectedRoots, urlsOfCp4Tests(cp));
    }

    public void testSimpleCompileTestDependency() throws Exception {
        generateTestingSuite();
        NbModuleProject modD = generateTestingSuiteComponent(testSuite,"d", "",
                "<test-dependency>\n" +
                "<code-name-base>org.example.a</code-name-base>\n" +
//                "<recursive/>\n" +
                "<compile-dependency/>\n" +
//                "<test/>\n" +
                "</test-dependency>\n", "");

        ClassPathProvider cpp = modD.getLookup().lookup(ClassPathProvider.class);
        ClassPath cp = cpp.findClassPath(modD.getTestSourceDirectory("unit"), ClassPath.COMPILE);
        Set<String> expectedRoots = new TreeSet<String>();
        expectedRoots.addAll(TESTLIBS);
        expectedRoots.add(urlForJar(modD.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(modA.getModuleJarLocation().getPath()));
        assertEquals("correct TEST COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));

        cp = cpp.findClassPath(modD.getTestSourceDirectory("unit"), ClassPath.EXECUTE);
        expectedRoots.add(urlForJar(modD.getTestClassesDirectory("unit").getPath()));
        assertEquals("correct TEST EXECUTE classpath", expectedRoots, urlsOfCp4Tests(cp));
    }

    public void testRecursiveRuntimeTestDependency() throws Exception {
        generateTestingSuite();
        NbModuleProject modD = generateTestingSuiteComponent(testSuite,"d", "",
                "<test-dependency>\n" +
                "<code-name-base>org.example.a</code-name-base>\n" +
                "<recursive/>\n" +
//                "<compile-dependency/>\n" +
//                "<test/>\n" +
                "</test-dependency>\n", "");

        ClassPathProvider cpp = modD.getLookup().lookup(ClassPathProvider.class);
        ClassPath cp = cpp.findClassPath(modD.getTestSourceDirectory("unit"), ClassPath.COMPILE);
        Set<String> expectedRoots = new TreeSet<String>();
        expectedRoots.addAll(TESTLIBS);
        expectedRoots.add(urlForJar(modD.getModuleJarLocation().getPath()));
        assertEquals("correct TEST COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));

        cp = cpp.findClassPath(modD.getTestSourceDirectory("unit"), ClassPath.EXECUTE);
        expectedRoots = new TreeSet<String>();
        expectedRoots.addAll(TESTLIBS);
        expectedRoots.add(urlForJar(modD.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(modA.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(modB.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(modC.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(modD.getTestClassesDirectory("unit").getPath()));
        assertEquals("correct TEST EXECUTE classpath", expectedRoots, urlsOfCp4Tests(cp));
    }

    public void testRecursiveCompileTestDependency() throws Exception {
        generateTestingSuite();
        NbModuleProject modD = generateTestingSuiteComponent(testSuite,"d", "",
                "<test-dependency>\n" +
                "<code-name-base>org.example.a</code-name-base>\n" +
                "<recursive/>\n" +
                "<compile-dependency/>\n" +
//                "<test/>\n" +
                "</test-dependency>\n", "");

        ClassPathProvider cpp = modD.getLookup().lookup(ClassPathProvider.class);
        ClassPath cp = cpp.findClassPath(modD.getTestSourceDirectory("unit"), ClassPath.COMPILE);
        Set<String> expectedRoots = new TreeSet<String>();
        expectedRoots.addAll(TESTLIBS);
        expectedRoots.add(urlForJar(modD.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(modA.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(modB.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(modC.getModuleJarLocation().getPath()));
        assertEquals("correct TEST COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));

        cp = cpp.findClassPath(modD.getTestSourceDirectory("unit"), ClassPath.EXECUTE);
        expectedRoots.add(urlForJar(modD.getTestClassesDirectory("unit").getPath()));
        assertEquals("correct TEST EXECUTE classpath", expectedRoots, urlsOfCp4Tests(cp));
    }

    public void testRecursiveCompileTestDependencyWithTests() throws Exception {
        generateTestingSuite();
        NbModuleProject modD = generateTestingSuiteComponent(testSuite,"d", "",
                "<test-dependency>\n" +
                "<code-name-base>org.example.a</code-name-base>\n" +
                "<recursive/>\n" +
                "<compile-dependency/>\n" +
                "<test/>\n" +
                "</test-dependency>\n", "");

        ClassPathProvider cpp = modD.getLookup().lookup(ClassPathProvider.class);
        ClassPath cp = cpp.findClassPath(modD.getTestSourceDirectory("unit"), ClassPath.COMPILE);
        Set<String> expectedRoots = new TreeSet<String>();
        expectedRoots.addAll(TESTLIBS);
        expectedRoots.add(urlForJar(modD.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(modA.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(modB.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(modC.getModuleJarLocation().getPath()));
        // path to compiled tests is a bit tricky, evaluator won't tell us path to testdist dir
        File clusterPath = ClusterUtils.getClusterDirectory(modA);
        String cluster = clusterPath.getName();
        File testDistJar = new File(clusterPath.getParentFile(), "/testdist/unit/" + cluster + "/org-example-a/tests.jar");
        expectedRoots.add(urlForJar(testDistJar.getPath()));
        assertEquals("correct TEST COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));

        cp = cpp.findClassPath(modD.getTestSourceDirectory("unit"), ClassPath.EXECUTE);
        expectedRoots.add(urlForJar(modD.getTestClassesDirectory("unit").getPath()));
        assertEquals("correct TEST EXECUTE classpath", expectedRoots, urlsOfCp4Tests(cp));
    }

    @Override
    protected int timeOut() {
        return 300000;   // testCyclicDependenciesDetected may loop endlessly
    }


    public void testCyclicDependenciesDetected() throws Exception {
        // generate simple A -> B -> C -> A runtime deps cycle (A->B is recursive test dep)
        testSuite = generateSuite("testSuite2");
        modC = generateTestingSuiteComponent(testSuite, "c",
                "<dependency>\n" +
                "<code-name-base>org.example.a</code-name-base>" +
                "<build-prerequisite/>\n<run-dependency/>\n" +
                "</dependency>",
                "", "");
        modB = generateTestingSuiteComponent(testSuite, "b",
                "<dependency>\n" +
                "<code-name-base>org.example.c</code-name-base>" +
                "<build-prerequisite/>\n<run-dependency/>\n" +
                "</dependency>",
                "", "");
        modA = generateTestingSuiteComponent(testSuite, "a",
                "<dependency>\n" +
                "<code-name-base>org.example.b</code-name-base>" +
                "<build-prerequisite/>\n<run-dependency/>\n" +
                "</dependency>",
                "<test-dependency>\n" +
                "<code-name-base>org.example.b</code-name-base>\n" +
                "<recursive/>\n" +
                "<compile-dependency/>\n" +
                "</test-dependency>\n", "");

        ClassPathProvider cpp = modA.getLookup().lookup(ClassPathProvider.class);
        ClassPath cp = cpp.findClassPath(modA.getTestSourceDirectory("unit"), ClassPath.EXECUTE);
        Set<String> expectedRoots = new TreeSet<String>();
        expectedRoots.add(urlForJar(modA.getTestClassesDirectory("unit").getPath()));
        expectedRoots.add(urlForJar(modA.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(modB.getModuleJarLocation().getPath()));
        expectedRoots.add(urlForJar(modC.getModuleJarLocation().getPath()));
        assertEquals("correct TEST COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));
    }

    private class MyHandler extends Handler {
        public int c;
        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().startsWith("computeTestType: processing ")) {
                c++;
            }
        }
        @Override
        public void flush() {
        }
        @Override
        public void close() throws SecurityException {
        }
    }

    // XXX fails without NB sources
    public void testRecursiveScanOptimization() throws Exception {
        FileObject src = nbRoot().getFileObject("apisupport/apisupport.project/test/unit/src");
        assertNotNull("apisupport/apisupport.project/test/unit/src", src);
        Logger logger = Logger.getLogger("org.netbeans.modules.apisupport.project.Evaluator");
        MyHandler h = new MyHandler();
        logger.addHandler(h);
        Level origL = logger.getLevel();
        try {
            logger.setLevel(Level.FINE);
            ClassPath cp = ClassPath.getClassPath(src, ClassPath.EXECUTE);
            assertNotNull("have an EXECUTE classpath", cp);
            Set<String> expectedRoots = new TreeSet<String>();
            // 247 entries & 5848 processed module entries before optimization
            // 142 processed entries with duplicate entries pruned
            assertTrue("Each module entry processed at most once", h.c <= 2 * cp.getRoots().length);
        } finally {
            logger.setLevel(origL);
            logger.removeHandler(h);
        }
    }

    /**
     * Creates testing suite with following modules dependency structure:
     * <pre>
     * org.example.a --(R)--> org.example.b --(R,UT-C)--> org.example.c
     *                              |
     *                          (UT-CRT)
     *                              |
     *                              V
     *                        org.example.t
     * </pre>
     * See harness README for details and expected CP (unit test deps are added 
     * to this structure to check that we're NOT returning them.
     *
     * @return Generated suite project (also stored in #testSuite)
     * @throws java.lang.Exception
     */
    private SuiteProject generateTestingSuite() throws Exception {
        // TestModuleDependencyTest#generateTestingProject() may also be useful
        testSuite = generateSuite("testSuite1");
        modC = generateSuiteComponent(testSuite, "c");
        modT = generateSuiteComponent(testSuite, "t");
        modB = generateTestingSuiteComponent(testSuite,"b",
                "<dependency>\n" +
                "<code-name-base>org.example.c</code-name-base>" +
                "<build-prerequisite/>\n<run-dependency/>\n" +
                "</dependency>",
                "<test-dependency>\n" +
                "<code-name-base>org.example.c</code-name-base>\n" +
                "<compile-dependency/>\n" +
                "</test-dependency>\n" +
                "<test-dependency>\n" +
                "<code-name-base>org.example.t</code-name-base>\n" +
                "<recursive/>\n" +
                "<compile-dependency/>\n" +
                "<test/>\n" +
                "</test-dependency>\n", "");
        modA = generateTestingSuiteComponent(testSuite,"a",
                "<dependency>\n" +
                "<code-name-base>org.example.b</code-name-base>" +
                "<build-prerequisite/>\n<run-dependency/>\n" +
                "</dependency>",
                "", "");
        return testSuite;
    }

    private NbModuleProject generateTestingSuiteComponent(SuiteProject suiteProject,String prjName,
            String depsXMLFragment, String testDepsXMLFragment, String cpExt) throws Exception {
        FileObject prjFO = generateSuiteComponentDirectory(suiteProject, suiteProject.getProjectDirectoryFile(), prjName);

        FileObject projectXMLFO = prjFO.getFileObject("nbproject/project.xml");
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://www.netbeans.org/ns/project/1\">\n" +
                "<type>org.netbeans.modules.apisupport.project</type>\n" +
                "<configuration>\n" +
                "<data xmlns=\"http://www.netbeans.org/ns/nb-module-project/3\">\n" +
                "<code-name-base>org.example." + prjName + "</code-name-base>\n" +
                "<suite-component/>\n" +
                "<module-dependencies>\n" + depsXMLFragment + "</module-dependencies>\n" +
                "<test-dependencies>\n" +
                "<test-type>\n" +
                "<name>unit</name>\n" +
                testDepsXMLFragment +
                "</test-type>\n" +
                "</test-dependencies>\n" +
                "<public-packages/>\n" +
                cpExt +
                "</data>\n" +
                "</configuration>\n" +
                "</project>\n";
        TestBase.dump(projectXMLFO, xml);
        return (NbModuleProject) ProjectManager.getDefault().findProject(prjFO);
    }

//    XXX: failing test, fix or delete
//    public void testUnitTestClasspathsExternalModules() throws Exception {
//        FileObject src = resolveEEP("suite1/support/lib-project/test/unit/src");
//        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
//        assertNotNull("have a COMPILE classpath", cp);
//        Set<String> expectedRoots = new TreeSet<String>();
//        expectedRoots.add(urlForJar(resolveEEPPath("/suite1/build/cluster/modules/org-netbeans-examples-modules-lib.jar")));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-libs-junit4.jar"));
//        expectedRoots.add("junit.jar");
//        expectedRoots.add("org-netbeans-insane.jar");
//        expectedRoots.add("org-netbeans-modules-nbjunit.jar");
//        assertEquals("right COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));
//        // Now test in suite3, where there is no source...
//        src = resolveEEP("suite3/dummy-project/test/unit/src");
//        cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
//        assertNotNull("have a COMPILE classpath", cp);
//        expectedRoots = new TreeSet<String>();
//        expectedRoots.add(urlForJar(resolveEEPPath("/suite3/dummy-project/build/cluster/modules/org-netbeans-examples-modules-dummy.jar")));
//        expectedRoots.add(urlForJar(resolveEEPPath("/suite3/nbplatform/random/modules/random.jar")));
//        expectedRoots.add(urlForJar(resolveEEPPath("/suite3/nbplatform/random/modules/ext/stuff.jar")));
//        expectedRoots.add("junit.jar");
//        expectedRoots.add("nbjunit.jar");
//        expectedRoots.add("org-netbeans-modules-nbjunit.jar");
//        expectedRoots.add("org-netbeans-modules-nbjunit-ide.jar");
//        assertEquals("right COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));
//
//        // test dependencies
//        expectedRoots.clear();
//        src  = resolveEEP("/suite4/module2/test/unit/src");
//        expectedRoots.add(urlForJar(resolveEEPPath("/suite4/build/testdist/unit/cluster/module1/tests.jar")));
//        expectedRoots.add(urlForJar(resolveEEPPath("/suite4/build/cluster/modules/module1.jar")));
//        expectedRoots.add(urlForJar(resolveEEPPath("/suite4/build/cluster/modules/module2.jar")));
//        expectedRoots.add("junit.jar");
//        expectedRoots.add("nbjunit.jar");
//        expectedRoots.add("insanelib.jar");
//        expectedRoots.add("org-netbeans-modules-nbjunit.jar");
//        expectedRoots.add("org-netbeans-modules-nbjunit-ide.jar");
//        cp = ClassPath.getClassPath(src,ClassPath.COMPILE);
//        FileObject roots[] = cp.getRoots();
//        assertEquals("right compileclasspath", expectedRoots, urlsOfCp4Tests(cp));
//    }
    
    /* XXX failing, should be rewritten to use generated module:
    public void testQaFunctionalTestClasspath() throws Exception {
        ....
    }
     */

//    XXX: failing test, fix or delete
//    public void testQaFunctionalTestClasspathExternalModules() throws Exception {
//    }

    /* XXX failing, but what was it supposed to be testing? I cannot decipher this. -jglick
    public void testBuildClassPath () throws Exception {
        FileObject srcRoot = nbCVSRoot().getFileObject("ant/project/src/");
        assertNotNull("have ant/project/src",srcRoot);
        ClassPath ccp = ClassPath.getClassPath(srcRoot, ClassPath.COMPILE);
        assertNotNull("No compile ClassPath for sources",ccp);
        FileObject  buildClasses = nbCVSRoot().getFileObject("ant/project/build/classes/");
        assertNotNull("have ant/project/build/classes",buildClasses);
                
        assertNull ("ClassPath.SOURCE for build must be null",ClassPath.getClassPath(buildClasses, ClassPath.SOURCE));
        assertNull ("ClassPath.COMPILE for build must be null",ClassPath.getClassPath(buildClasses, ClassPath.COMPILE));
        ClassPath cp = ClassPath.getClassPath(buildClasses, ClassPath.EXECUTE);
        assertNotNull("ClassPath.EXECUTE for build must NOT be null",cp);
        ClassPath expectedCp = ClassPathSupport.createProxyClassPath(new ClassPath[] {
                ClassPathSupport.createClassPath(new FileObject[] {buildClasses}),
                ccp
        });
        assertClassPathsHaveTheSameResources(cp, expectedCp);
        
        FileObject testSrcRoot = nbCVSRoot().getFileObject("ant/project/test/unit/src/");
        assertNotNull("have ant/project/test/unit/src/",testSrcRoot);
        ClassPath tccp = ClassPath.getClassPath(testSrcRoot, ClassPath.COMPILE);
        assertNotNull("No compile ClassPath for tests",tccp);
        Project prj = FileOwnerQuery.getOwner(testSrcRoot);
        assertNotNull("No project found",prj);
        assertTrue("Invalid project type", prj instanceof NbModuleProject);
        FileObject testBuildClasses = nbCVSRoot().getFileObject ("ant/project/build/test/unit/classes/");
        if (testBuildClasses == null) {
            // Have to have it, so we can call CP.gCP on it:
            testBuildClasses = FileUtil.createFolder(nbCVSRoot(), "ant/project/build/test/unit/classes");
        }
        assertNull ("ClassPath.SOURCE for build/test must be null",ClassPath.getClassPath(testBuildClasses, ClassPath.SOURCE));
        assertNull ("ClassPath.COMPILE for build/test must be null",ClassPath.getClassPath(testBuildClasses, ClassPath.COMPILE));
        cp = ClassPath.getClassPath(testBuildClasses, ClassPath.EXECUTE);
        
        String path = ((NbModuleProject)prj).evaluator().getProperty("test.unit.run.cp.extra");     //NOI18N
        List<PathResourceImplementation> trExtra = new ArrayList<PathResourceImplementation>();
        if (path != null) {
            String[] pieces = PropertyUtils.tokenizePath(path);
            for (int i = 0; i < pieces.length; i++) {
                File f = ((NbModuleProject)prj).getHelper().resolveFile(pieces[i]);
                URL url = f.toURI().toURL();
                if (FileUtil.isArchiveFile(url)) {
                    url = FileUtil.getArchiveRoot (url);
                }
                else {
                    String stringifiedURL = url.toString ();
                    if (!stringifiedURL.endsWith("/")) {        //NOI18N
                        url = new URL (stringifiedURL+"/");     //NOI18N
                    }
                }
                trExtra.add(ClassPathSupport.createResource(url));
            }
        }        
        assertNotNull("ClassPath.EXECUTE for build/test must NOT be null", cp);
        expectedCp = ClassPathSupport.createProxyClassPath(
                ClassPathSupport.createClassPath(testBuildClasses),
                tccp,
                ClassPathSupport.createClassPath(trExtra),
                ClassPathSupport.createClassPath(new URL(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-modules-masterfs.jar"))));
        assertClassPathsHaveTheSameResources(expectedCp, cp);

        File jarFile = ((NbModuleProject) prj).getModuleJarLocation();
        FileObject jarFO = FileUtil.toFileObject(jarFile);
        assertNotNull("No module jar", jarFO);
        FileObject jarRoot = FileUtil.getArchiveRoot(jarFO);
//        assertEquals(prj, FileOwnerQuery.getOwner(jarRoot));
        assertNull("ClassPath.SOURCE for module jar must be null", ClassPath.getClassPath(jarRoot, ClassPath.SOURCE));
        assertNull("ClassPath.COMPILE for module jar must be null", ClassPath.getClassPath(jarRoot, ClassPath.COMPILE));
        cp = ClassPath.getClassPath(jarRoot, ClassPath.EXECUTE);
        assertNotNull("ClassPath.EXECUTE for module jar must NOT be null", cp);
        expectedCp = ClassPathSupport.createProxyClassPath(ClassPathSupport.createClassPath(jarRoot), ccp);
        assertClassPathsHaveTheSameResources(expectedCp, cp);
    }
     */
    
    public void testCompileClasspathChanges() throws Exception {
        ClassPath cp = ClassPath.getClassPath(copyOfMiscDir.getFileObject("src"), ClassPath.COMPILE);
        Set<String> expectedRoots = new TreeSet<String>();
        assertEquals("right initial COMPILE classpath", expectedRoots, urlsOfCp(cp));
        TestBase.TestPCL l = new TestBase.TestPCL();
        cp.addPropertyChangeListener(l);
        ModuleEntry ioEntry = copyOfMiscProject.getModuleList().getEntry("org.openide.io");
        assertNotNull(ioEntry);
        copyOfMiscXMLManager.addDependencies(Collections.singleton(new ModuleDependency(ioEntry)));
        assertTrue("got changes", l.changed.contains(ClassPath.PROP_ROOTS));
        l.changed.clear();
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-io.jar"));
        assertEquals("right COMPILE classpath after changing project.xml", expectedRoots, urlsOfCp(cp));
        ModuleEntry utilEntry = copyOfMiscProject.getModuleList().getEntry("org.openide.util");
        assertNotNull(utilEntry);
        copyOfMiscXMLManager.addDependencies(Collections.singleton(new ModuleDependency(utilEntry)));
        assertTrue("got changes again", l.changed.contains(ClassPath.PROP_ROOTS));
        l.changed.clear();
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        assertEquals("right COMPILE classpath after changing project.xml again", expectedRoots, urlsOfCp(cp));
    }

    /* XXX cannot be run in binary dist, requires sources, and depends on module dep details; should test against fake platform
    public void testExecuteClasspathChanges() throws Exception {
        ClassPath cp = ClassPath.getClassPath(copyOfMiscDir.getFileObject("src"), ClassPath.EXECUTE);
        Set<String> expectedRoots = new TreeSet<String>();
        expectedRoots.add(FileUtil.urlForArchiveOrDir(file(FileUtil.toFile(copyOfMiscDir), "build/classes")).toExternalForm());
        assertEquals("right initial EXECUTE classpath", expectedRoots, urlsOfCp(cp));
        TestBase.TestPCL l = new TestBase.TestPCL();
        cp.addPropertyChangeListener(l);
        ModuleEntry ioEntry = copyOfMiscProject.getModuleList().getEntry("org.openide.io");
        assertNotNull(ioEntry);
        copyOfMiscXMLManager.addDependencies(Collections.singleton(new ModuleDependency(ioEntry)));
        assertTrue("got changes", l.changed.contains(ClassPath.PROP_ROOTS));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-io.jar"));
        // #70206: transitive deps added too:
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        assertEquals("right EXECUTE classpath after changing project.xml", expectedRoots, urlsOfCp(cp));
    }
     */

    public void testExecuteCPOnClassesDir() throws Exception {
        InstalledFileLocatorImpl.registerDestDir(destDirF);
        TestAntLogger.getDefault().setEnabled(true);
        NbModuleProject prj = TestBase.generateStandaloneModule(getWorkDir(), "testing");
        prj.open();

        FileObject buildScript = prj.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        assertNotNull(buildScript);
        ExecutorTask et = ActionUtils.runTarget(buildScript, new String[]{"compile"}, null);
        et.waitFinished();
        assertEquals("Error during ant ...",0,et.result());
        TestAntLogger.getDefault().setEnabled(false);
        File classesF = prj.getClassesDirectory();
        assertTrue("Classes dir of testing project should exist", classesF.exists());
        FileUtil.refreshFor(classesF); // XXX seems necessary occasionally on deadlock - why?
        FileObject classes = FileUtil.toFileObject(classesF);
        assertNotNull(classes);
        ClassPath cp = ClassPath.getClassPath(classes, ClassPath.EXECUTE);
        assertNotNull("have exec CP for " + classes, cp);
        Set<String> expectedRoots = new TreeSet<String>();
        expectedRoots.add(FileUtil.urlForArchiveOrDir(classesF).toExternalForm());
        assertEquals("right compiled EXECUTE classpath", expectedRoots, urlsOfCp(cp));
    }

//    XXX: failing test, fix or delete
//    public void testUnitTestCompileClasspathChanges() throws Exception {
//        ClassPath cp = ClassPath.getClassPath(copyOfMiscDir.getFileObject("test/unit/src"), ClassPath.COMPILE);
//        Set<String> expectedRoots = new TreeSet<String>();
//        expectedRoots.add(FileUtil.urlForArchiveOrDir(file(copyOfSuite2, "build/cluster/modules/org-netbeans-examples-modules-misc.jar")).toExternalForm());
//        expectedRoots.add("junit.jar");
//        expectedRoots.add("nbjunit.jar");
//        expectedRoots.add("insanelib.jar");
//        expectedRoots.add("org-netbeans-modules-nbjunit.jar");
//        expectedRoots.add("org-netbeans-modules-nbjunit-ide.jar");
//        assertEquals("right initial COMPILE classpath", expectedRoots, urlsOfCp4Tests(cp));
//        TestBase.TestPCL l = new TestBase.TestPCL();
//        cp.addPropertyChangeListener(l);
//        ModuleEntry ioEntry = copyOfMiscProject.getModuleList().getEntry("org.openide.io");
//        assertNotNull(ioEntry);
//        copyOfMiscXMLManager.addDependencies(Collections.singleton(new ModuleDependency(ioEntry)));
//        assertTrue("got changes", l.changed.contains(ClassPath.PROP_ROOTS));
//        l.changed.clear();
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-io.jar"));
//        assertEquals("right COMPILE classpath after changing project.xml", expectedRoots, urlsOfCp4Tests(cp));
//        EditableProperties props = copyOfMiscProject.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//        props.setProperty("test.unit.cp.extra", "${netbeans.dest.dir}/lib/fnord.jar");
//        copyOfMiscProject.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
//        assertTrue("got changes again", l.changed.contains(ClassPath.PROP_ROOTS));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/lib/fnord.jar"));
//        assertEquals("right COMPILE classpath after changing project.properties", expectedRoots, urlsOfCp4Tests(cp));
//    }

    /* XXX no longer available here
    public void testBinaryOriginAbsolutePath() throws Exception {
        File jmfhome = new File(getWorkDir(), "jmfhome");
        File audioFiles = file("platform/samples/audio-files");
        if (!audioFiles.isDirectory()) {
            System.err.println("Skipping testBinaryOriginAbsolutePath since platform not checked out");
            return;
        }
        File audioviewer = copyFolder(audioFiles);
        // Make it a standalone module so we can copy it:
        File pp = new File(audioviewer, "nbproject/private/private.properties".replace('/', File.separatorChar));
        pp.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(pp);
        try {
            Properties p = new Properties();
            p.setProperty("jmf.home", jmfhome.getAbsolutePath());
            p.store(os, null);
        } finally {
            os.close();
        }
        pp = new File(audioviewer, "nbproject/private/platform-private.properties".replace('/', File.separatorChar));
        pp.getParentFile().mkdirs();
        os = new FileOutputStream(pp);
        try {
            Properties p = new Properties();
            p.setProperty(ModuleList.NETBEANS_DEST_DIR, file("nbbuild/netbeans").getAbsolutePath());
            p.store(os, null);
        } finally {
            os.close();
        }
        File px = new File(audioviewer, "nbproject/project.xml".replace('/', File.separatorChar));
        Document doc = XMLUtil.parse(new InputSource(px.toURI().toString()), false, true, null, null);
        NodeList nl = doc.getDocumentElement().getElementsByTagNameNS(NbModuleProjectType.NAMESPACE_SHARED_2, "data");
        assertEquals(1, nl.getLength());
        Element data = (Element) nl.item(0);
        // XXX insert at position 1, between <c-n-b> and <m-d>:
        data.appendChild(doc.createElementNS(NbModuleProjectType.NAMESPACE_SHARED_2, "standalone"));
        os = new FileOutputStream(px);
        try {
            XMLUtil.write(doc, os, "UTF-8");
        } finally {
            os.close();
        }
        FileObject audioviewerFO = FileUtil.toFileObject(audioviewer);
        Project p = ProjectManager.getDefault().findProject(audioviewerFO);
        assertNotNull(p);
        FileObject src = audioviewerFO.getFileObject("src");
        ClassPath cp = ClassPath.getClassPath(src, ClassPath.COMPILE);
        assertNotNull("have a COMPILE classpath", cp);
        Set<String> expectedRoots = new TreeSet<String>();
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-actions.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/core/org-openide-filesystems.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-loaders.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-text.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
        File lib = new File(jmfhome, "lib");
        expectedRoots.add(FileUtil.urlForArchiveOrDir(new File(lib, "jmf.jar")).toExternalForm());
        expectedRoots.add(FileUtil.urlForArchiveOrDir(new File(lib, "mediaplayer.jar")).toExternalForm());
        assertEquals("right COMPILE classpath incl. absolute locations of JARs",
            expectedRoots, urlsOfCp(cp));
    }
    
    private void assertClassPathsHaveTheSameResources(ClassPath actual, ClassPath expected) {
        assertEquals(urlsOfCp(expected).toString(), urlsOfCp(actual).toString());
    }
     */

//    XXX: failing test, fix or delete
//    public void testTransitiveExecuteClasspath() throws Exception { // #70206
//        NbModuleProject p = TestBase.generateStandaloneModule(getWorkDir(), "prj");
//        Util.addDependency(p, "org.openide.windows");
//        ProjectManager.getDefault().saveProject(p);
//        ClassPath cp = ClassPath.getClassPath(p.getSourceDirectory(), ClassPath.EXECUTE);
//        Set<String> expectedRoots = new TreeSet<String>();
//        // What we just added:
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-windows.jar"));
//        // And its transitive deps:
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-dialogs.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-nodes.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-openide-awt.jar"));
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/lib/org-openide-util.jar"));
//        // Dialogs API depends on Progress API
//        expectedRoots.add(urlForJar("nbbuild/netbeans/" + TestBase.CLUSTER_PLATFORM + "/modules/org-netbeans-api-progress.jar"));
//        // And the usual:
//        expectedRoots.add(FileUtil.urlForArchiveOrDir(new File(p.getProjectDirectoryFile(), "build/classes")).toExternalForm());
//        assertEquals("right EXECUTE classpath incl. transitive deps",
//            expectedRoots, urlsOfCp(cp));
//    }
    
    public void testClassPathExtensionChanges() throws Exception { // #179578
        NbModuleProject p = generateStandaloneModule("prj");
        ClassPath cp = ClassPath.getClassPath(p.getSourceDirectory(), ClassPath.COMPILE);
        assertEquals("", cp.toString());
        FileObject xJar = TestFileUtils.writeZipFile(p.getProjectDirectory(), "release/modules/ext/x.jar", "META-INF/MANIFEST.MF:Manifest-Version: 1.0\n\n");
        ProjectXMLManager pxm = new ProjectXMLManager(p);
        pxm.replaceClassPathExtensions(Collections.singletonMap("ext/x.jar", "release/modules/ext/x.jar"));
        ProjectManager.getDefault().saveProject(p);
        assertEquals(FileUtil.toFile(xJar).getAbsolutePath(), cp.toString());
        pxm.replaceClassPathExtensions(Collections.singletonMap("ext/y.jar", (String) null));
        ProjectManager.getDefault().saveProject(p);
        assertEquals(p.getHelper().resolveFile("build/cluster/modules/ext/y.jar").getAbsolutePath(), cp.toString());
    }

    public void testBootClasspath() throws Exception {
        NbModuleProject p = generateStandaloneModule("prj");
        ClassPath boot = ClassPath.getClassPath(p.getSourceDirectory(), ClassPath.BOOT);
        // XXX test that it is sane... although by default, ${nbjdk.home} will be undefined
        FileObject xtra = TestFileUtils.writeZipFile(FileUtil.toFileObject(getWorkDir()), "xtra.jar", "META-INF/MANIFEST.MF:Manifest-Version: 1.0\n\n");
        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put(ClassPathProviderImpl.BOOTCLASSPATH_PREPEND, FileUtil.toFile(xtra).getAbsolutePath());
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(p);
        assertTrue(boot.toString(), Arrays.asList(boot.getRoots()).contains(FileUtil.getArchiveRoot(xtra)));
    }

    public void testSourcePathForWrappedJarSource() throws Exception { // #176983
        NbModuleProject p = generateStandaloneModule("prj");
        String relpath = "release/sources/x.zip";
        FileObject srcZip = TestFileUtils.writeZipFile(p.getProjectDirectory(), relpath, "pkg/C.java:package pkg; public class C {}");
        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put(NbModuleProject.SOURCE_START + "x.jar", relpath);
        // could also create a <class-path-extension> named release/modules/ext/x.jar, but don't have to
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ProjectManager.getDefault().saveProject(p);
        FileObject srcZipRoot = FileUtil.getArchiveRoot(srcZip);
        ClassPath cp = ClassPath.getClassPath(srcZipRoot, ClassPath.SOURCE);
        assertNotNull(cp);
        assertEquals(Collections.singletonList(srcZipRoot), Arrays.asList(cp.getRoots()));
        /* Currently fails; maybe not worth implementing:
        assertEquals(cp, ClassPath.getClassPath(srcZipRoot.getFileObject("pkg/C.java"), ClassPath.SOURCE));
         */
        assertEquals(Collections.singletonList(srcZipRoot),
                Arrays.asList(ClassPath.getClassPath(srcZipRoot.getFileObject("pkg/C.java"), ClassPath.SOURCE).getRoots()));
    }

}
