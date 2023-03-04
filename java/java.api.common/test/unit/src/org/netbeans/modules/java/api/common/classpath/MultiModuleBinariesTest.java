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

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.impl.ModuleTestUtilities;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;
import org.openide.util.test.MockPropertyChangeListener;

/**
 *
 * @author Tomas Zezula
 */
public final class MultiModuleBinariesTest extends NbTestCase {

    private FileObject src1;
    private FileObject src2;
    private FileObject mod1a;
    private FileObject mod1b;
    private FileObject mod2c;
    private FileObject mod1d;
    private FileObject mod2d;
    private TestProject tp;
    private ModuleTestUtilities mtu;

    public MultiModuleBinariesTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setInstances(TestProject.createProjectType(), new DelegatingB4SQImpl());
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        src1 = wd.createFolder("src1"); //NOI18N
        assertNotNull(src1);
        src2 = wd.createFolder("src2"); //NOI18N
        assertNotNull(src2);
        mod1a = src1.createFolder("lib.common").createFolder("classes");        //NOI18N
        assertNotNull(mod1a);
        mod1b = src1.createFolder("lib.util").createFolder("classes");          //NOI18N
        assertNotNull(mod1b);
        mod2c = src2.createFolder("lib.discovery").createFolder("classes");     //NOI18N
        assertNotNull(mod2c);
        mod2d = src2.createFolder("lib.event").createFolder("classes");         //NOI18N
        assertNotNull(mod2d);
        mod1d = src1.createFolder("lib.event").createFolder("classes");         //NOI18N
        assertNotNull(mod1d);
        final Project prj = TestProject.createMultiModuleProject(wd);
        tp = prj.getLookup().lookup(TestProject.class);
        assertNotNull(tp);
        //Set BUILD_MODULES_DIR used by QuerySupport to BUILD_CLASSES_DIR used by ModuleTestUtilities
        setProperty(
                ProjectProperties.BUILD_MODULES_DIR,
                "${"+ProjectProperties.BUILD_CLASSES_DIR+"}");  //NOI18N
        //Set BUILD_TEST_MODULES_DIR used by testBuildFor
        setProperty(
                ProjectProperties.BUILD_TEST_MODULES_DIR,
                "${"+ProjectProperties.BUILD_TEST_CLASSES_DIR+"}"); //NOI18N
        mtu = ModuleTestUtilities.newInstance(tp);
        assertNotNull(mtu);
    }

    public void testBinaryPath() {
        assertTrue(mtu.updateModuleRoots(false,src1,src2));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod2c, mod1d, mod2d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);

        final BinaryForSourceQueryImplementation impl = QuerySupport.createMultiModuleBinaryForSourceQuery(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                modules,
                sources,
                testModules,
                testSources);
        assertNotNull(impl);
        Lookup.getDefault().lookup(DelegatingB4SQImpl.class).setDelegate(impl);
        final ClassPathImplementation cpImpl = ModuleClassPaths.createMultiModuleBinariesPath(model, true, false);
        assertNotNull(cpImpl);
        final ClassPath cp = ClassPathFactory.createClassPath(cpImpl);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod2c, mod1d, mod2d})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.distFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));
    }

    public void testModulePathChanges() {
        assertTrue(mtu.updateModuleRoots(false,src1));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);

        final BinaryForSourceQueryImplementation impl = QuerySupport.createMultiModuleBinaryForSourceQuery(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                modules,
                sources,
                testModules,
                testSources);
        assertNotNull(impl);
        Lookup.getDefault().lookup(DelegatingB4SQImpl.class).setDelegate(impl);
        final ClassPathImplementation cpImpl = ModuleClassPaths.createMultiModuleBinariesPath(model, true, false);
        assertNotNull(cpImpl);
        final ClassPath cp = ClassPathFactory.createClassPath(cpImpl);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.distFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));

        assertTrue(mtu.updateModuleRoots(false, src1, src2));
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod2c, mod1d, mod2d})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.distFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));

        assertTrue(mtu.updateModuleRoots(false, src2));
        assertEquals(
                Arrays.stream(new FileObject[]{mod2c, mod2d})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.distFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));
    }

    public void testModulePathChangesFires() {
        assertTrue(mtu.updateModuleRoots(false, src1));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);

        final BinaryForSourceQueryImplementation impl = QuerySupport.createMultiModuleBinaryForSourceQuery(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                modules,
                sources,
                testModules,
                testSources);
        assertNotNull(impl);
        Lookup.getDefault().lookup(DelegatingB4SQImpl.class).setDelegate(impl);
        final ClassPathImplementation cpImpl = ModuleClassPaths.createMultiModuleBinariesPath(model, true, false);
        assertNotNull(cpImpl);
        final ClassPath cp = ClassPathFactory.createClassPath(cpImpl);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.distFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));

        final MockPropertyChangeListener l = new MockPropertyChangeListener();
        cp.addPropertyChangeListener(l);
        assertTrue(mtu.updateModuleRoots(false, src1, src2));
        l.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS);

        assertTrue(mtu.updateModuleRoots(false, src2));
        l.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS);
    }

    public void testModuleSetChanges() throws IOException {
        assertTrue(mtu.updateModuleRoots(false, src1));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);

        final BinaryForSourceQueryImplementation impl = QuerySupport.createMultiModuleBinaryForSourceQuery(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                modules,
                sources,
                testModules,
                testSources);
        assertNotNull(impl);
        Lookup.getDefault().lookup(DelegatingB4SQImpl.class).setDelegate(impl);
        final ClassPathImplementation cpImpl = ModuleClassPaths.createMultiModuleBinariesPath(model, true, false);
        assertNotNull(cpImpl);
        final ClassPath cp = ClassPathFactory.createClassPath(cpImpl);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.distFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));

        final FileObject foomodule = src1.createFolder("foomodule").createFolder("classes");    //NOI18N
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d, foomodule})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.distFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));
        foomodule.getParent().delete();
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.distFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));
    }

    public void testModuleSetChangesFires() throws IOException {
        assertTrue(mtu.updateModuleRoots(false, src1));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);

        final BinaryForSourceQueryImplementation impl = QuerySupport.createMultiModuleBinaryForSourceQuery(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                modules,
                sources,
                testModules,
                testSources);
        assertNotNull(impl);
        Lookup.getDefault().lookup(DelegatingB4SQImpl.class).setDelegate(impl);
        final ClassPathImplementation cpImpl = ModuleClassPaths.createMultiModuleBinariesPath(model, true, false);
        assertNotNull(cpImpl);
        final ClassPath cp = ClassPathFactory.createClassPath(cpImpl);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.distFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));

        final MockPropertyChangeListener l = new MockPropertyChangeListener();
        cp.addPropertyChangeListener(l);
        final FileObject foomodule = src1.createFolder("foomodule").createFolder("classes");    //NOI18N
        l.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS);
        foomodule.getParent().delete();
        l.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS);
    }

    public void testDistFolderChanges() {
        assertTrue(mtu.updateModuleRoots(false, src1));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);

        final BinaryForSourceQueryImplementation impl = QuerySupport.createMultiModuleBinaryForSourceQuery(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                modules,
                sources,
                testModules,
                testSources);
        assertNotNull(impl);
        Lookup.getDefault().lookup(DelegatingB4SQImpl.class).setDelegate(impl);
        final ClassPathImplementation cpImpl = ModuleClassPaths.createMultiModuleBinariesPath(model, true, false);
        assertNotNull(cpImpl);
        final ClassPath cp = ClassPathFactory.createClassPath(cpImpl);
        final List<URL> oldExpected = Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                .flatMap((fo) -> Arrays.stream(new URL[]{
                    mtu.distFor(fo.getParent().getNameExt())}))
                .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                .distinct()
                .collect(Collectors.toList());
        assertEquals(
                oldExpected,
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));

        final String oldDist = setProperty(ProjectProperties.DIST_DIR, "release"); //NOI18N
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.distFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));

        setProperty(ProjectProperties.DIST_DIR, oldDist);
        assertEquals(
                oldExpected,
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));
    }

    public void testDistFolderChangesFires() {
        assertTrue(mtu.updateModuleRoots(false, src1));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final MultiModule model = MultiModule.getOrCreate(modules, sources);
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);

        final BinaryForSourceQueryImplementation impl = QuerySupport.createMultiModuleBinaryForSourceQuery(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                modules,
                sources,
                testModules,
                testSources);
        assertNotNull(impl);
        Lookup.getDefault().lookup(DelegatingB4SQImpl.class).setDelegate(impl);
        final ClassPathImplementation cpImpl = ModuleClassPaths.createMultiModuleBinariesPath(model, true, false);
        assertNotNull(cpImpl);
        final ClassPath cp = ClassPathFactory.createClassPath(cpImpl);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod1d})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.distFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));

        final MockPropertyChangeListener l = new MockPropertyChangeListener();
        cp.addPropertyChangeListener(l);
        setProperty(ProjectProperties.DIST_DIR, "release"); //NOI18N
        l.assertEvents(ClassPath.PROP_ENTRIES, ClassPath.PROP_ROOTS);
    }

    public void testUnitTestModules() throws IOException {
        final FileObject mod1aTests = src1.getFileObject("lib.common").createFolder("tests");        //NOI18N
        assertNotNull(mod1aTests);
        final FileObject mod1bTests = src1.getFileObject("lib.util").createFolder("tests");          //NOI18N
        assertNotNull(mod1bTests);
        FileUtil.createData(mod1bTests, "module-info.java");                                         //NOI18N
        final FileObject mod2cTests = src2.getFileObject("lib.discovery").createFolder("tests");     //NOI18N
        assertNotNull(mod2cTests);
        final FileObject mod2dTests = src2.getFileObject("lib.event").createFolder("tests");         //NOI18N
        assertNotNull(mod2dTests);
        FileUtil.createData(mod2dTests, "module-info.java");                                         //NOI18N
        final FileObject mod1dTests = src1.getFileObject("lib.event").createFolder("tests");         //NOI18N
        assertNotNull(mod1dTests);
        assertTrue(mtu.updateModuleRoots(false,src1,src2));
        assertTrue(mtu.updateModuleRoots(true,src1,src2));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod2c, mod1d, mod2d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1aTests, mod1bTests, mod2cTests, mod1dTests, mod2dTests})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(testSources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final BinaryForSourceQueryImplementation impl = QuerySupport.createMultiModuleBinaryForSourceQuery(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                modules,
                sources,
                testModules,
                testSources);
        assertNotNull(impl);
        Lookup.getDefault().lookup(DelegatingB4SQImpl.class).setDelegate(impl);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);
        final ClassPathImplementation cpImpl = ModuleClassPaths.createMultiModuleBinariesPath(testModel, true, true);
        assertNotNull(cpImpl);
        final ClassPath cp = ClassPathFactory.createClassPath(cpImpl);
        assertEquals(
                //Only test modules with module info
                Arrays.stream(new FileObject[]{mod1bTests, mod1dTests, mod2dTests})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.testBuildFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));
    }

    public void testUnitTestModulesModuleInfoCreateDelete() throws IOException {
        final FileObject mod1aTests = src1.getFileObject("lib.common").createFolder("tests");        //NOI18N
        assertNotNull(mod1aTests);
        final FileObject mod1bTests = src1.getFileObject("lib.util").createFolder("tests");          //NOI18N
        assertNotNull(mod1bTests);
        final FileObject mod2cTests = src2.getFileObject("lib.discovery").createFolder("tests");     //NOI18N
        assertNotNull(mod2cTests);
        final FileObject mod2dTests = src2.getFileObject("lib.event").createFolder("tests");         //NOI18N
        assertNotNull(mod2dTests);
        final FileObject mod1dTests = src1.getFileObject("lib.event").createFolder("tests");         //NOI18N
        assertNotNull(mod1dTests);
        assertTrue(mtu.updateModuleRoots(false,src1,src2));
        assertTrue(mtu.updateModuleRoots(true,src1,src2));
        final SourceRoots modules = mtu.newModuleRoots(false);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, modules.getRoots()));
        final SourceRoots sources = mtu.newSourceRoots(false);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1a, mod1b, mod2c, mod1d, mod2d})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(sources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final SourceRoots testModules = mtu.newModuleRoots(true);
        assertTrue(Arrays.equals(new FileObject[]{src1, src2}, testModules.getRoots()));
        final SourceRoots testSources = mtu.newSourceRoots(true);
        assertEquals(
                Arrays.stream(new FileObject[]{mod1aTests, mod1bTests, mod2cTests, mod1dTests, mod2dTests})
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()),
                Arrays.stream(testSources.getRoots())
                    .map((fo) -> fo.getPath())
                    .sorted()
                    .collect(Collectors.toList()));
        final BinaryForSourceQueryImplementation impl = QuerySupport.createMultiModuleBinaryForSourceQuery(
                tp.getUpdateHelper().getAntProjectHelper(),
                tp.getEvaluator(),
                modules,
                sources,
                testModules,
                testSources);
        assertNotNull(impl);
        Lookup.getDefault().lookup(DelegatingB4SQImpl.class).setDelegate(impl);
        final MultiModule testModel = MultiModule.getOrCreate(testModules, testSources);
        final ClassPathImplementation cpImpl = ModuleClassPaths.createMultiModuleBinariesPath(testModel, true, true);
        assertNotNull(cpImpl);
        final ClassPath cp = ClassPathFactory.createClassPath(cpImpl);
        assertTrue(cp.entries().isEmpty());
        FileObject mib = FileUtil.createData(mod1bTests, "module-info.java"); //NOI18N
        assertEquals(
                //Only test modules with module info
                Arrays.stream(new FileObject[]{mod1bTests})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.testBuildFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));
        FileObject mia = FileUtil.createData(mod1aTests, "module-info.java");                                         //NOI18N
        assertEquals(
                //Only test modules with module info
                Arrays.stream(new FileObject[]{mod1aTests, mod1bTests})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.testBuildFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));
        mib.delete();
        assertEquals(
                //Only test modules with module info
                Arrays.stream(new FileObject[]{mod1aTests})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.testBuildFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));
        mia.delete();
        assertTrue(cp.entries().isEmpty());
        FileObject mic = FileUtil.createData(mod2cTests, "module-info.java");                                         //NOI18N
        assertEquals(
                //Only test modules with module info
                Arrays.stream(new FileObject[]{mod2cTests})
                    .flatMap((fo) -> Arrays.stream(new URL[]{
                        mtu.testBuildFor(fo.getParent().getNameExt())}))
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .distinct()
                    .collect(Collectors.toList()),
                cp.entries().stream()
                    .map((e) -> e.getURL())
                    .sorted((u1,u2) -> u1.toString().compareTo(u2.toString()))
                    .collect(Collectors.toList()));
    }

    private String setProperty(final String name, final String value) {
        final String[] res = new String[1];
        ProjectManager.mutex(true, tp).writeAccess(()->{
            final EditableProperties ep = tp.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            res[0] = ep.getProperty(name);
            ep.setProperty(name, value);
            tp.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        });
        return res[0];
    }

    private static final class DelegatingB4SQImpl implements BinaryForSourceQueryImplementation {
        private final AtomicReference<BinaryForSourceQueryImplementation> delegate;

        DelegatingB4SQImpl() {
            delegate = new AtomicReference<>();
        }

        void setDelegate(@NonNull final BinaryForSourceQueryImplementation delegate) {
            this.delegate.set(delegate);
        }

        @Override
        public BinaryForSourceQuery.Result findBinaryRoots(URL sourceRoot) {
            final BinaryForSourceQueryImplementation d = this.delegate.get();
            if (d != null) {
                return d.findBinaryRoots(sourceRoot);
            } else {
                return null;
            }
        }
    }

}
