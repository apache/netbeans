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
package org.netbeans.modules.java.api.common.classpath;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;
import javax.swing.event.ChangeListener;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.TestJavaPlatform;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.impl.ModuleTestUtilities;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class MultiModuleClassPathProviderTest extends NbTestCase {

    //Module Source Path
    private FileObject src1;
    private FileObject src2;
    //Source Path
    private FileObject mod1a;   // in src1
    private FileObject mod1b;   // in src1
    private FileObject mod2c;   // in src2
    private FileObject mod1d;   // in src1
    private FileObject mod2d;   // in src2
    //Unit tests
    private FileObject mod1aTests;   // in src1
    private FileObject mod1bTests;   // in src1
    private FileObject mod2cTests;   // in src2
    private FileObject mod1dTests;   // in src1
    private FileObject mod2dTests;   // in src2
    //Module Path
    private FileObject mp;
    private FileObject modliba;
    private FileObject modlibb;
    //ClassPath
    private FileObject cpRoot1;
    private FileObject cpRoot2;

    private TestProject tp;
    private SourceRoots modules;
    private SourceRoots sources;
    private SourceRoots testModules;
    private SourceRoots testSources;
    private ModuleTestUtilities mtu;
    private ClassPath systemModules;

    public MultiModuleClassPathProviderTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        Map<Class<?>,Function<TestProject,Object>> factories = new HashMap<>();
        factories.put(ClassPathProvider.class, (prj) -> {
            return MultiModuleClassPathProvider.Builder.newInstance(
                prj.getUpdateHelper().getAntProjectHelper(),
                prj.getEvaluator(),
                modules,
                sources,
                testModules,
                testSources)
            .build();
        });
        factories.put(BinaryForSourceQueryImplementation.class, (prj) -> {
            return QuerySupport.createMultiModuleBinaryForSourceQuery(
                    prj.getUpdateHelper().getAntProjectHelper(),
                    prj.getEvaluator(),
                    modules,
                    sources,
                    testModules,
                    testSources);
        });
        factories.put(SourceLevelQueryImplementation2.class, (prj) -> {
            return new SourceLevelQueryImplementation2 () {
                private final SourceLevelQueryImplementation2.Result RES = new SourceLevelQueryImplementation2.Result() {
                    @Override
                    public String getSourceLevel() {
                        return "9"; //NOI18N
                    }
                    @Override
                    public void addChangeListener(ChangeListener listener) {
                    }
                    @Override
                    public void removeChangeListener(ChangeListener listener) {
                    }
                };
                @Override
                public SourceLevelQueryImplementation2.Result getSourceLevel(FileObject javaFile) {
                    if (javaFile == prj.getProjectDirectory() || FileUtil.isParentOf(prj.getProjectDirectory(), javaFile)) {
                        return RES;
                    } else {
                        return null;
                    }
                }
            };
        });
        MockLookup.setInstances(TestProject.createProjectType(factories), new MockJPProvider());
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        src1 = wd.createFolder("src1"); //NOI18N
        assertNotNull(src1);
        src2 = wd.createFolder("src2"); //NOI18N
        assertNotNull(src2);
        mod1a = src1.createFolder("lib.common").createFolder("classes");        //NOI18N
        assertNotNull(mod1a);
        createModuleInfo(mod1a, "lib.common", "java.base");
        mod1b = src1.createFolder("lib.util").createFolder("classes");          //NOI18N
        assertNotNull(mod1b);
        createModuleInfo(mod1b, "lib.util", "java.base","java.logging");      //NOI18N
        mod2c = src2.createFolder("lib.discovery").createFolder("classes");     //NOI18N
        assertNotNull(mod2c);
        createModuleInfo(mod2c, "lib.discovery", "java.base","java.compiler");  //NOI18N
        mod2d = src2.createFolder("lib.event").createFolder("classes");         //NOI18N
        assertNotNull(mod2d);
        createModuleInfo(mod2d, "lib.event", "java.base","java.xml");  //NOI18N
        mod1d = src1.createFolder("lib.event").createFolder("classes");         //NOI18N
        assertNotNull(mod1d);
        mod1aTests = src1.getFileObject("lib.common").createFolder("tests");        //NOI18N
        assertNotNull(mod1aTests);
        mod1bTests = src1.getFileObject("lib.util").createFolder("tests");          //NOI18N
        assertNotNull(mod1bTests);
        mod2cTests = src2.getFileObject("lib.discovery").createFolder("tests");     //NOI18N
        assertNotNull(mod2cTests);
        mod1dTests = src1.getFileObject("lib.event").createFolder("tests");         //NOI18N
        assertNotNull(mod1dTests);
        mod2dTests = src2.getFileObject("lib.event").createFolder("tests");         //NOI18N
        assertNotNull(mod2dTests);
        createModuleInfo(mod2dTests, "lib.event.tests");
        mp = wd.createFolder("modules");    //NOI18N
        assertNotNull(mp);
        modliba = createJar(mp, "modliba.jar");   //NOI18N
        assertNotNull(modliba);
        modlibb = createJar(mp, "modlibb.jar");   //NOI18N
        assertNotNull(modlibb);
        final FileObject libs = wd.createFolder("libs");    //NOI18N
        assertNotNull(libs);
        cpRoot1 = libs.createFolder("lib1");    //NOI18N
        assertNotNull(cpRoot1);
        cpRoot2 = libs.createFolder("lib2");    //NOI18N
        assertNotNull(cpRoot2);
        final Project prj = TestProject.createProject(wd, null, null);
        tp = prj.getLookup().lookup(TestProject.class);
        assertNotNull(tp);
        mtu = ModuleTestUtilities.newInstance(tp);
        assertNotNull(mtu);

        //Set module roots
        assertTrue(mtu.updateModuleRoots(false, src1,src2));
        modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, modules.getRoots()));
        sources = mtu.newSourceRoots(false);
        assertTrue(mtu.updateModuleRoots(true, src1,src2));
        testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{src1,src2}, testModules.getRoots()));
        testSources = mtu.newSourceRoots(true);
        //Set classpath, modulepath, build.module.dir, javac.test.classpath
        ProjectManager.mutex(true, prj).writeAccess(() -> {
            final EditableProperties ep = tp.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            ep.setProperty(ProjectProperties.JAVAC_MODULEPATH, org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(mp).toString());
            ep.setProperty(ProjectProperties.JAVAC_CLASSPATH, org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(cpRoot1, cpRoot2).toString());
            ep.setProperty(ProjectProperties.BUILD_MODULES_DIR, "build/modules");   //NOI18N
            ep.setProperty(ProjectProperties.BUILD_TEST_MODULES_DIR, "build/test/modules");   //NOI18N
            ep.setProperty(ProjectProperties.JAVAC_TEST_CLASSPATH, "${"+ProjectProperties.JAVAC_CLASSPATH+"}"); //NOI18N
            tp.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        });
        //Set active platform to Java9 when available
        JavaPlatform java9 = null;
        String java9Name = null;
        for (JavaPlatform jp : JavaPlatformManager.getDefault().getPlatforms(null, new Specification("j2se", new SpecificationVersion("9")))) { //NOI18N
            final String antName = jp.getProperties().get("platform.ant.name");    //NOI18N
            if (antName != null) {
                java9 = jp;
                java9Name = antName;
                break;
            }
        }
        if (java9 != null) {
            final String id = java9Name;
            ProjectManager.mutex(true, prj).writeAccess(() -> {
                final EditableProperties ep = tp.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.setProperty(ProjectProperties.PLATFORM_ACTIVE, id);
                tp.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
            });
            systemModules = java9.getBootstrapLibraries();
        }
    }

    public void testModuleBootPath() {
        final ClassPath mod1aCp = ClassPath.getClassPath(mod1a, JavaClassPathConstants.MODULE_BOOT_PATH);
        assertNotNull(mod1aCp);
        assertTrue(systemModules == null || urls(systemModules).equals(urls(mod1aCp)));
        ClassPath bootCp = ClassPath.getClassPath(mod1b, JavaClassPathConstants.MODULE_BOOT_PATH);
        assertSame(mod1aCp, bootCp);
        bootCp = ClassPath.getClassPath(mod2c, JavaClassPathConstants.MODULE_BOOT_PATH);
        assertSame(mod1aCp, bootCp);
        bootCp = ClassPath.getClassPath(mod1d, JavaClassPathConstants.MODULE_BOOT_PATH);
        assertSame(mod1aCp, bootCp);
        bootCp = ClassPath.getClassPath(mod2d, JavaClassPathConstants.MODULE_BOOT_PATH);
        assertSame(mod1aCp, bootCp);
    }

    public void testModuleSourcePath() {
        final ClassPath mod1aCp = ClassPath.getClassPath(mod1a, JavaClassPathConstants.MODULE_SOURCE_PATH);
        assertEquals(urls(src1,src2), urls(mod1aCp));
        ClassPath msp = ClassPath.getClassPath(mod1b, JavaClassPathConstants.MODULE_SOURCE_PATH);
        assertSame(mod1aCp, msp);
        msp = ClassPath.getClassPath(mod2c, JavaClassPathConstants.MODULE_SOURCE_PATH);
        assertSame(mod1aCp, msp);
        msp = ClassPath.getClassPath(mod1d, JavaClassPathConstants.MODULE_SOURCE_PATH);
        assertSame(mod1aCp, msp);
        msp = ClassPath.getClassPath(mod2d, JavaClassPathConstants.MODULE_SOURCE_PATH);
        assertSame(mod1aCp, msp);
    }

    public void testModuleLegacyClassPath() {
        final ClassPath mod1aCp = ClassPath.getClassPath(mod1a, JavaClassPathConstants.MODULE_CLASS_PATH);
        assertEquals(urls(cpRoot1,cpRoot2), urls(mod1aCp));
        ClassPath mcp = ClassPath.getClassPath(mod1b, JavaClassPathConstants.MODULE_CLASS_PATH);
        assertSame(mod1aCp, mcp);
        mcp = ClassPath.getClassPath(mod2c, JavaClassPathConstants.MODULE_CLASS_PATH);
        assertSame(mod1aCp, mcp);
        mcp = ClassPath.getClassPath(mod1d, JavaClassPathConstants.MODULE_CLASS_PATH);
        assertSame(mod1aCp, mcp);
        mcp = ClassPath.getClassPath(mod2d, JavaClassPathConstants.MODULE_CLASS_PATH);
        assertSame(mod1aCp, mcp);
    }

    public void testModuleCompilePath() {
        final ClassPath mod1aMp = ClassPath.getClassPath(mod1a, JavaClassPathConstants.MODULE_COMPILE_PATH);
        assertEquals(urls(
                modliba.toURL(),
                modlibb.toURL(),
                mtu.distFor("lib.common"),      //NOI18N
                mtu.distFor("lib.util"),        //NOI18N
                mtu.distFor("lib.discovery"),   //NOI18N
                mtu.distFor("lib.event")),      //NOI18N
            urls(mod1aMp));
        ClassPath mp = ClassPath.getClassPath(mod1b, JavaClassPathConstants.MODULE_COMPILE_PATH);
        assertSame(mod1aMp, mp);
        mp = ClassPath.getClassPath(mod2c, JavaClassPathConstants.MODULE_COMPILE_PATH);
        assertSame(mod1aMp, mp);
        mp = ClassPath.getClassPath(mod1d, JavaClassPathConstants.MODULE_COMPILE_PATH);
        assertSame(mod1aMp, mp);
        mp = ClassPath.getClassPath(mod2d, JavaClassPathConstants.MODULE_COMPILE_PATH);
        assertSame(mod1aMp, mp);
    }

    public void testSourcePath() {
        ClassPath scp = ClassPath.getClassPath(mod1a, ClassPath.SOURCE);
        assertEquals(urls(mod1a), urls(scp));
        scp = ClassPath.getClassPath(mod1b, ClassPath.SOURCE);
        assertEquals(urls(mod1b), urls(scp));
        scp = ClassPath.getClassPath(mod2c, ClassPath.SOURCE);
        assertEquals(urls(mod2c), urls(scp));
        scp = ClassPath.getClassPath(mod1d, ClassPath.SOURCE);
        assertEquals(urls(mod1d, mod2d), urls(scp));
        scp = ClassPath.getClassPath(mod2d, ClassPath.SOURCE);
        assertEquals(urls(mod1d, mod2d), urls(scp));
    }

    public void testBootPath() {
        if (systemModules == null) {
            System.out.println("No Java 9, skipping testBootPath"); //NOI18N
            return;
        }
        ClassPath bcp = ClassPath.getClassPath(mod1a, ClassPath.BOOT);
        assertEquals(
                filter(urls(systemModules),"java.base"),    //NOI18N
                urls(bcp));
        bcp = ClassPath.getClassPath(mod1b, ClassPath.BOOT);
        assertEquals(
                filter(urls(systemModules),"java.base","java.logging"),    //NOI18N
                urls(bcp));
        bcp = ClassPath.getClassPath(mod2c, ClassPath.BOOT);
        assertEquals(
                filter(urls(systemModules),"java.base","java.compiler"),    //NOI18N
                urls(bcp));
        bcp = ClassPath.getClassPath(mod1d, ClassPath.BOOT);
        assertEquals(
                filter(urls(systemModules),"java.base","java.xml"),    //NOI18N
                urls(bcp));
        ClassPath bcp2 = ClassPath.getClassPath(mod2d, ClassPath.BOOT);
        assertSame(bcp, bcp2);
    }

    public void testGetProjectClassPaths() throws IOException {
        if (systemModules == null) {
            System.out.println("No Java 9, skipping testBootPath"); //NOI18N
            return;
        }
        ClassPathProvider cpp = tp.getLookup().lookup(ClassPathProvider.class);
        assertTrue(cpp instanceof MultiModuleClassPathProvider);
        MultiModuleClassPathProvider mmcpp = (MultiModuleClassPathProvider) cpp;
        ClassPath[] cps = mmcpp.getProjectClassPaths(JavaClassPathConstants.MODULE_BOOT_PATH);
        assertNotNull(cps);
        assertEquals(1, cps.length);
        assertEquals(urls(systemModules), urls(cps[0]));
        cps = mmcpp.getProjectClassPaths(JavaClassPathConstants.MODULE_SOURCE_PATH);
        assertNotNull(cps);
        assertEquals(2, cps.length);
        assertEquals(urls(src1, src2), urls(cps[0]));
        assertEquals(urls(src1, src2), urls(cps[1]));
        cps = mmcpp.getProjectClassPaths(JavaClassPathConstants.MODULE_CLASS_PATH);
        assertNotNull(cps);
        assertEquals(2, cps.length);
        assertEquals(urls(cpRoot1,cpRoot2), urls(cps[0]));
        assertEquals(urls(cpRoot1,cpRoot2), urls(cps[1]));
        assertNotSame(cps[0], cps[1]);
        cps = mmcpp.getProjectClassPaths(JavaClassPathConstants.MODULE_COMPILE_PATH);
        assertNotNull(cps);
        assertEquals(2, cps.length);
        assertEquals(urls(
                modliba.toURL(),
                modlibb.toURL(),
                mtu.distFor("lib.common"),      //NOI18N
                mtu.distFor("lib.util"),        //NOI18N
                mtu.distFor("lib.discovery"),   //NOI18N
                mtu.distFor("lib.event")),      //NOI18N
            urls(cps[0]));
        assertEquals(urls(
                //Only tests with module-info appear on module-path
                mtu.testBuildFor("lib.event")), //NOI18N
            urls(cps[1]));
        cps = mmcpp.getProjectClassPaths(ClassPath.SOURCE);
        assertNotNull(cps);
        assertEquals(4+4, cps.length);
        Collection<List<URL>> expected = new HashSet<List<URL>>() {
            {
                add(Collections.singletonList(mod1a.toURL()));
                add(Collections.singletonList(mod1b.toURL()));
                add(Collections.singletonList(mod2c.toURL()));
                add(urls(mod1d, mod2d));
                add(Collections.singletonList(mod1aTests.toURL()));
                add(Collections.singletonList(mod1bTests.toURL()));
                add(Collections.singletonList(mod2cTests.toURL()));
                add(urls(mod1dTests, mod2dTests));
            }
        };
        for (ClassPath cp : cps) {
            assertTrue(expected.remove(urls(cp)));
        }
        assertTrue(expected.isEmpty());
        cps = mmcpp.getProjectClassPaths(ClassPath.BOOT);
        assertNotNull(cps);
        assertEquals(4+4, cps.length);
        expected = new ArrayList<List<URL>>() {
            {
                add(filter(urls(systemModules),"java.base"));   //NOI18N
                add(filter(urls(systemModules),"java.base","java.logging"));    //NOI18N
                add(filter(urls(systemModules),"java.base","java.compiler"));   //NOI18N
                add(filter(urls(systemModules),"java.base","java.xml"));    //NOI18N
                //Tests - todo not currect now as there is no CompilerOpsQuery
                add(new ArrayList<>(ModuleClassPathsTest.unnamedReads(systemModules)));
                add(new ArrayList<>(ModuleClassPathsTest.unnamedReads(systemModules)));
                add(new ArrayList<>(ModuleClassPathsTest.unnamedReads(systemModules)));
                add(filter(urls(systemModules),"java.base"));   //NOI18N
            }
        };
        for (ClassPath cp : cps) {
            assertTrue(expected.remove(urls(cp)));
        }
        assertTrue(expected.isEmpty());
        cps = mmcpp.getProjectClassPaths(ClassPath.COMPILE);
        assertNotNull(cps);
        assertEquals(4+4, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.EXECUTE);
        assertNotNull(cps);
        assertEquals(4+4, cps.length);
    }

    public void testEvents() throws IOException {
        ClassPathProvider cpp = tp.getLookup().lookup(ClassPathProvider.class);
        assertTrue(cpp instanceof MultiModuleClassPathProvider);
        MultiModuleClassPathProvider mmcpp = (MultiModuleClassPathProvider) cpp;
        final MockClassPathsChangeListener ml = new MockClassPathsChangeListener();
        mmcpp.addClassPathsChangeListener(ml);
        final FileObject modFoo = src2.createFolder("lib.foo").createFolder("classes"); //NOI18N
        final FileObject fooModInfo = createModuleInfo(modFoo, "lib.foo", "java.base"); //NOI18N
        ml.assertEventCount(2);
        ml.assertChangedTypes(ClassPath.BOOT, ClassPath.COMPILE, ClassPath.SOURCE, ClassPath.EXECUTE);
        ml.reset();
        ClassPath[] cps = mmcpp.getProjectClassPaths(ClassPath.SOURCE);
        assertEquals(10+0, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.BOOT);
        assertEquals(10+0, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.COMPILE);
        assertEquals(10+0, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.EXECUTE);
        assertEquals(10+0, cps.length);
        final FileObject modBoo = src2.createFolder("lib.boo").createFolder("classes"); //NOI18N
        final FileObject booModInfo = createModuleInfo(modBoo, "lib.boo", "java.base"); //NOI18N
        ml.assertEventCount(2);
        ml.assertChangedTypes(ClassPath.BOOT, ClassPath.COMPILE, ClassPath.SOURCE, ClassPath.EXECUTE);
        ml.reset();
        cps = mmcpp.getProjectClassPaths(ClassPath.SOURCE);
        assertEquals(12+0, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.BOOT);
        assertEquals(12+0, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.COMPILE);
        assertEquals(12+0, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.EXECUTE);
        assertEquals(12+0, cps.length);
        modFoo.getParent().delete();
        ml.assertEventCount(2);
        ml.assertChangedTypes(ClassPath.BOOT, ClassPath.COMPILE, ClassPath.SOURCE, ClassPath.EXECUTE);
        ml.reset();
        cps = mmcpp.getProjectClassPaths(ClassPath.SOURCE);
        assertEquals(10+0, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.BOOT);
        assertEquals(10+0, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.COMPILE);
        assertEquals(10+0, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.EXECUTE);
        assertEquals(10+0, cps.length);
        modBoo.getParent().delete();
        ml.assertEventCount(2);
        ml.assertChangedTypes(ClassPath.BOOT, ClassPath.COMPILE, ClassPath.SOURCE, ClassPath.EXECUTE);
        ml.reset();
        cps = mmcpp.getProjectClassPaths(ClassPath.SOURCE);
        assertEquals(8+0, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.BOOT);
        assertEquals(8+0, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.COMPILE);
        assertEquals(8+0, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.EXECUTE);
        assertEquals(8+0, cps.length);
        final FileObject modZoo = src2.createFolder("lib.zoo").createFolder("classes"); //NOI18N
        final FileObject zooModInfo = createModuleInfo(modZoo, "lib.zoo", "java.base"); //NOI18N
        ml.assertEventCount(2);
        ml.assertChangedTypes(ClassPath.BOOT, ClassPath.COMPILE, ClassPath.SOURCE, ClassPath.EXECUTE);
        ml.reset();
        cps = mmcpp.getProjectClassPaths(ClassPath.SOURCE);
        assertEquals(10+0, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.BOOT);
        assertEquals(10+0, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.COMPILE);
        assertEquals(10+0, cps.length);
        cps = mmcpp.getProjectClassPaths(ClassPath.EXECUTE);
        assertEquals(10+0, cps.length);
    }


    @NonNull
    private static List<URL> urls(@NonNull final ClassPath cp) {
        return cp.entries().stream()
                .map(ClassPath.Entry::getURL)
                .sorted((a,b) -> a.toString().compareTo(b.toString()))
                .collect(Collectors.toList());
    }

    @NonNull
    private static List<URL> urls(@NonNull final FileObject... roots) {
        return Arrays.stream(roots)
                .map(FileObject::toURL)
                .sorted((a,b) -> a.toString().compareTo(b.toString()))
                .collect(Collectors.toList());
    }

    @NonNull
    private static List<URL> urls(@NonNull final URL... roots) {
        return Arrays.stream(roots)
                .sorted((a,b) -> a.toString().compareTo(b.toString()))
                .collect(Collectors.toList());
    }

    private static List<URL> filter(
            @NonNull final List<? extends URL> urls,
            @NonNull final String... modules) {
        return urls.stream()
                .filter((u) -> {
                    final String surl = u.toString();
                    for (String name : modules) {
                        if (surl.contains(String.format("/%s/", name))) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

    public void testTestModulePath() {
        ProjectManager.mutex(true, tp).writeAccess(() -> {
            final AntProjectHelper helper = tp.getUpdateHelper().getAntProjectHelper();
            final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            ep.setProperty(ProjectProperties.BUILD_MODULES_DIR, String.format("${%s}/modules", ProjectProperties.BUILD_DIR));   //NOI18N
            ep.setProperty(ProjectProperties.JAVAC_TEST_MODULEPATH, String.format("${%s}", ProjectProperties.JAVAC_MODULEPATH));    //NOI18N
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        });
        ClassPathProvider cpp = tp.getLookup().lookup(ClassPathProvider.class);
        assertTrue(cpp instanceof MultiModuleClassPathProvider);
        MultiModuleClassPathProvider mmcpp = (MultiModuleClassPathProvider) cpp;
        ClassPath[] cps = mmcpp.getProjectClassPaths(JavaClassPathConstants.MODULE_COMPILE_PATH);
        assertNotNull(cps);
        assertEquals(2, cps.length);
        assertEquals(urls(
                    modliba.toURL(),
                    modlibb.toURL(),
                    //Only tests with module-info appear on module-path
                    mtu.testBuildFor("lib.event")),      //NOI18N
                urls(cps[1]));
        //Put "build/modules" on tests module path => all modules should appear on module compile path.
        ProjectManager.mutex(true, tp).writeAccess(() -> {
            final AntProjectHelper helper = tp.getUpdateHelper().getAntProjectHelper();
            final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            ep.setProperty(ProjectProperties.JAVAC_TEST_MODULEPATH, String.format(
                    "${%s}:${%s}",  //NOI18N
                    ProjectProperties.JAVAC_MODULEPATH,
                    ProjectProperties.BUILD_MODULES_DIR));
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        });
        assertEquals(urls(
                    modliba.toURL(),
                    modlibb.toURL(),
                    mtu.testBuildFor("lib.event"),      //NOI18N
                    mtu.distFor("lib.common"),      //NOI18N
                    mtu.distFor("lib.util"),        //NOI18N
                    mtu.distFor("lib.discovery"),   //NOI18N
                    mtu.distFor("lib.event")),
                urls(cps[1]));
    }

    private static FileObject createJar(
            @NonNull final FileObject folder,
            @NonNull final String name) throws IOException {
        final FileObject f = FileUtil.createData(folder, name);
        try (final ZipOutputStream out = new ZipOutputStream(f.getOutputStream())) {
            //write something
        }
        return FileUtil.getArchiveRoot(f);
    }

    @NonNull
    private static FileObject createModuleInfo(
            @NonNull final FileObject root,
            @NonNull final String moduleName,
            @NonNull final String... requiredModules) throws IOException {
        final FileObject moduleInfo = FileUtil.createData(root, "module-info.java");    //NOI18N
        try(PrintWriter out = new PrintWriter(new OutputStreamWriter(moduleInfo.getOutputStream()))) {
            out.printf("module %s {%n", moduleName);    //NOI18N
            for (String requiredModule : requiredModules) {
                out.printf("    requires %s;%n", requiredModule);    //NOI18N
            }
            out.printf("}");    //NOI18N
        }
        return moduleInfo;
    }

    private static final class MockClassPathsChangeListener implements AbstractClassPathProvider.ClassPathsChangeListener {
        private int eventCount = 0;
        private final Set<String> changedCpTypes = new HashSet<>();

        @Override
        public void classPathsChange(AbstractClassPathProvider.ClassPathsChangeEvent event) {
            eventCount++;
            changedCpTypes.addAll(event.getChangedClassPathTypes());
        }

        void assertEventCount(int expectedEventCount) {
            assertEquals(expectedEventCount, eventCount);
        }

        void assertChangedTypes(String... expectedChangedCpTypes) {
            assertEquals(new HashSet<>(Arrays.asList(expectedChangedCpTypes)), changedCpTypes);
        }

        void reset() {
            eventCount = 0;
            changedCpTypes.clear();
        }
    }

    private static final class MockJPProvider implements JavaPlatformProvider {
        private JavaPlatform jp;

        MockJPProvider() throws IOException {
            jp = Optional.ofNullable(TestUtilities.getJava9Home())
                .map((jh) -> TestJavaPlatform.createModularPlatform(jh))
                .orElse(null);
        }

        @Override
        public JavaPlatform[] getInstalledPlatforms() {
            return jp != null ?
                    new JavaPlatform[] {jp} :
                    new JavaPlatform[0];
        }

        @Override
        public JavaPlatform getDefaultPlatform() {
            return jp;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
}
