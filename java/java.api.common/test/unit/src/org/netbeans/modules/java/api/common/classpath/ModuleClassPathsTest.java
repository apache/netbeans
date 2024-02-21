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
package org.netbeans.modules.java.api.common.classpath;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import javax.lang.model.element.ModuleElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.TestJavaPlatform;
import org.netbeans.modules.java.api.common.TestProject;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.RequestProcessor;
import org.openide.util.test.MockLookup;

/**
 * Tests for ModuleClassPaths.
 * Todo: add -Xmodule test, add requires transitive test
 * @author Tomas Zezula
 */
public class ModuleClassPathsTest extends NbTestCase {
    
    private static final Comparator<Object> LEX_COMPARATOR =
            (a,b) -> a.toString().compareTo(b.toString());
    private static final Predicate<ModuleElement> NON_JAVA_PUB = (e) ->
                           !e.getQualifiedName().toString().startsWith("java.") &&  //NOI18N
                            e.getDirectives().stream()
                                .filter((d) -> d.getKind() == ModuleElement.DirectiveKind.EXPORTS)
                                .map((d) -> (ModuleElement.ExportsDirective)d)
                                .anyMatch((ed) -> ed.getTargetModules() == null);
    
    private ClassPath src;
    private ClassPath testSrc;
    private ClassPath systemModules;
    private FileObject automaticModuleRoot;
    private FileObject jarFileRoot;
    private FileObject target;
    private TestProject tp;
    
    public ModuleClassPathsTest(@NonNull final String name) {
        super(name);
    }
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final FileObject workDir = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        MockLookup.setInstances(TestProject.createProjectType(), new MockCompilerOptions(), new MockClassPathProvider());
        final FileObject prjDir = FileUtil.createFolder(workDir, "TestProject");    //NOI18N
        assertNotNull(prjDir);
        final FileObject srcDir = FileUtil.createFolder(prjDir, "src");    //NOI18N
        assertNotNull(srcDir);
        final Project prj = TestProject.createProject(prjDir, srcDir, null);
        assertNotNull(prj);
        setSourceLevel(prj, "9");   //NOI18N
        tp = prj.getLookup().lookup(TestProject.class);
        assertNotNull(tp);
        src = ClassPathFactory.createClassPath(ClassPathSupportFactory.createSourcePathImplementation (
                            tp.getSourceRoots(),
                            tp.getUpdateHelper().getAntProjectHelper(),
                            tp.getEvaluator()));
        final FileObject testDir = FileUtil.createFolder(prjDir, "test");    //NOI18N
        assertNotNull(testDir);
        testSrc = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(testDir);
        target = FileUtil.createFolder(prjDir, "build");    //NOI18N
        assertNotNull(target);
        systemModules = Optional.ofNullable(TestUtilities.getJava9Home())
                .map((jh) -> TestJavaPlatform.createModularPlatform(jh))
                .map((jp) -> jp.getBootstrapLibraries())
                .orElse(null);
        final FileObject libs = FileUtil.createFolder(workDir, "libs"); //NOI18N
        automaticModuleRoot = createJar(libs,"automatic.jar");  //NOI18N
        assertNotNull(automaticModuleRoot);
        jarFileRoot = createJar(libs,"legacy.jar"); //NOI18N
        assertNotNull(jarFileRoot);
    }
    
   
    public void testModuleInfoBasedCp_SystemModules_in_UnnamedModule() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                src,
                systemModules,
                ClassPath.EMPTY,
                null,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = reads(
                systemModules,
                NamePredicate.create("java.se").or(NON_JAVA_PUB));  //NOI18N
        assertEquals(expectedURLs, resURLs);
    }


    public void testModuleInfoBasedCp_SystemModules_in_NamedEmptyModule() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        createModuleInfo(src, "Modle"); //NOI18N
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                src,
                systemModules,
                ClassPath.EMPTY,
                null,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = reads(systemModules, NamePredicate.create("java.base"));  //NOI18N
        assertEquals(expectedURLs, resURLs);
    }

    public void DISABLEDtestModuleInfoInJDK8Project() throws IOException {
        assertNotNull(src);
        createModuleInfo(src, "ModuleInfoDebris"); //NOI18N
        setSourceLevel(tp, "1.8");   //NOI18N
        final ClassPath base = systemModules == null ? ClassPath.EMPTY : systemModules;
        final ClassPathImplementation mcp = ModuleClassPaths.createModuleInfoBasedPath(
            base,
            src,
            base,
            ClassPath.EMPTY,
            null,
            null
        );
        List<? extends PathResourceImplementation> resources = mcp.getResources();
        assertEquals("No resources found as module-info.java is ignored: " + resources, 0, resources.size());
    }

    public void testModuleInfoInJDK11Project() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }

        assertNotNull(src);
        createModuleInfo(src, "ModuleInfoUsed"); //NOI18N
        final ClassPath base = systemModules;
        final ClassPathImplementation mcp = ModuleClassPaths.createModuleInfoBasedPath(
            base,
            src,
            base,
            ClassPath.EMPTY,
            null,
            null
        );
        List<? extends PathResourceImplementation> one = mcp.getResources();
        assertEquals("One resource found as module-info.java is used: " + one, 1, one.size());
    }

    public void testModuleInfoBasedCp_SystemModules_in_NamedModule() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        createModuleInfo(src, "Modle", "java.logging"); //NOI18N
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                src,
                systemModules,
                ClassPath.EMPTY,
                null,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = reads(systemModules, NamePredicate.create("java.logging"));  //NOI18N
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testModuleInfoBasedCp_UserModules_in_UnnamedModule() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(automaticModuleRoot);
        final ClassPath legacyCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(jarFileRoot);
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                userModules,
                src,
                systemModules,
                userModules,
                legacyCp,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = legacyCp.entries().stream()
                .map((e) -> e.getURL())
                .collect(Collectors.toList());
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testModuleInfoBasedCp_UserModules_in_UnnamedModule_AddMods() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(automaticModuleRoot);
        final ClassPath legacyCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(jarFileRoot);
        final MockCompilerOptions opts = MockCompilerOptions.getInstance();
        assertNotNull("No MockCompilerOptions in Lookup", opts);
        opts.forRoot(src.getRoots()[0])
                .apply("--add-modules")    //NOI18N
                .apply(SourceUtils.getModuleName(automaticModuleRoot.toURL()));
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                userModules,
                src,
                systemModules,
                userModules,
                legacyCp,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        //Modules from add-modules + cp
        final Collection<URL> expectedURLs = new ArrayList<>();
        expectedURLs.add(automaticModuleRoot.toURL());
        legacyCp.entries().stream()
                .map((e) -> e.getURL())
                .forEach(expectedURLs::add);
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testModuleInfoBasedCp_UserModules_in_NamedModule() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(automaticModuleRoot);
        final ClassPath legacyCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(jarFileRoot);
        createModuleInfo(src, "Modle", "java.logging",SourceUtils.getModuleName(automaticModuleRoot.toURL())); //NOI18N
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                userModules,
                src,
                systemModules,
                userModules,
                legacyCp,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        //Modules from declared dependencies
        final Collection<URL> expectedURLs = new ArrayList<>();
        expectedURLs.add(automaticModuleRoot.toURL());
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testModuleInfoBasedCp_UserModules_in_NamedModule_AddMods() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(automaticModuleRoot);
        final ClassPath legacyCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(jarFileRoot);
        createModuleInfo(src, "Modle", "java.logging"); //NOI18N
        final MockCompilerOptions opts = MockCompilerOptions.getInstance();
        assertNotNull("No MockCompilerOptions in Lookup", opts);
        opts.forRoot(src.getRoots()[0])
                .apply("--add-modules")    //NOI18N
                .apply(SourceUtils.getModuleName(automaticModuleRoot.toURL()));
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                userModules,
                src,
                systemModules,
                userModules,
                legacyCp,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        //Modules from declared dependencies - nothing
        final Collection<URL> expectedURLs = new ArrayList<>();
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testModuleInfoBasedCp_UserModules_in_NamedModule_AddRead() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(automaticModuleRoot);
        final ClassPath legacyCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(jarFileRoot);
        createModuleInfo(src, "Modle", "java.logging"); //NOI18N
        final MockCompilerOptions opts = MockCompilerOptions.getInstance();
        assertNotNull("No MockCompilerOptions in Lookup", opts);
        opts.forRoot(src.getRoots()[0])
                .apply("--add-reads")    //NOI18N
                .apply("Modle="+SourceUtils.getModuleName(automaticModuleRoot.toURL()))    //NOI18N
                .apply("--add-modules") //NOI18N
                .apply(SourceUtils.getModuleName(automaticModuleRoot.toURL()));
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                userModules,
                src,
                systemModules,
                userModules,
                legacyCp,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        //Modules from declared dependencies + add-reads
        final Collection<URL> expectedURLs = new ArrayList<>();
        expectedURLs.add(automaticModuleRoot.toURL());
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testModuleInfoBasedCp_UserModules_in_NamedModule_AddReadAllUnnamed() throws IOException {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(automaticModuleRoot);
        final ClassPath legacyCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(jarFileRoot);
        createModuleInfo(src, "Modle", "java.logging"); //NOI18N
        final MockCompilerOptions opts = MockCompilerOptions.getInstance();
        assertNotNull("No MockCompilerOptions in Lookup", opts);
        opts.forRoot(src.getRoots()[0])
                .apply("--add-reads")    //NOI18N
                .apply("Modle=ALL-UNNAMED");    //NOI18N
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                userModules,
                src,
                systemModules,
                userModules,
                legacyCp,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        //Modules from declared dependencies + add-reads
        final Collection<URL> expectedURLs = legacyCp.entries().stream()
                .map((e) -> e.getURL())
                .collect(Collectors.toList());
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testModuleInfoBothSourceAndTest() throws Exception {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        assertNotNull(testSrc);
        createModuleInfo(src, "modle", "java.logging"); //NOI18N
        createModuleInfo(testSrc, "modle", "java.logging", "java.compiler"); //NOI18N
        final MockCompilerOptions opts = MockCompilerOptions.getInstance();
        assertNotNull("No MockCompilerOptions in Lookup", opts);
        opts.forRoot(testSrc.getRoots()[0])
                .apply("--patch-module")    //NOI18N
                .apply(String.format(
                        "modle=%s",  //NOI18N
                        FileUtil.toFile(src.getRoots()[0]).getAbsolutePath()));
        URL buildClasses = this.tp.getProjectDirectory().toURL().toURI().resolve("build/").resolve("classes/").toURL();
        ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(buildClasses));
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                userModules,
                testSrc,
                systemModules,
                userModules,
                null,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = new ArrayList<>();
        expectedURLs.add(buildClasses);
        expectedURLs.addAll(Arrays.asList(BinaryForSourceQuery.findBinaryRoots(src.getRoots()[0].toURL()).getRoots()));
        assertEquals(expectedURLs, resURLs);
    }

    public void testProjectMutexWriteDeadlock() throws Exception {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final FileObject moduleInfo = createModuleInfo(src, "Modle", "java.logging"); //NOI18N
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                src,
                systemModules,
                ClassPath.EMPTY,
                null,
                null));
        final RequestProcessor deadLockMaker = new RequestProcessor("DeadLock Maker", 1);
        final ClasspathInfo info = new ClasspathInfo.Builder(systemModules)
                .build();
        final CountDownLatch startThread = new CountDownLatch(1);
        final CountDownLatch startSelf = new CountDownLatch(1);
        final CountDownLatch endThread = new CountDownLatch(1);
        deadLockMaker.execute(() -> {
            try {
                final JavaSource js = JavaSource.create(info);
                js.runUserActionTask((cc)->{
                        startThread.await();
                        startSelf.countDown();
                        ProjectManager.mutex().readAccess(()->{
                            System.out.println("EXECUTED");
                        });
                    },
                        true);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            } finally {
                endThread.countDown();
            }
        });
        ProjectManager.mutex().writeAccess(()-> {
            try {
                startThread.countDown();
                startSelf.await();
                cp.entries();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });                
        endThread.await();
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = reads(systemModules, NamePredicate.create("java.logging"));  //NOI18N
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testPatchModule() throws Exception {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        createModuleInfo(src, "Modle", "java.logging"); //NOI18N
        final FileObject compact1Patch1 = createPatchFolder("java.logging.patch1");    //NOI18N
        final FileObject compact1Patch2 = createPatchFolder("java.logging.patch2");    //NOI18N
        final MockCompilerOptions opts = MockCompilerOptions.getInstance();
        assertNotNull("No MockCompilerOptions in Lookup", opts);
        opts.forRoot(src.getRoots()[0])
                .apply("--patch-module")    //NOI18N
                .apply(String.format(
                        "java.logging=%s:%s",  //NOI18N
                        FileUtil.toFile(compact1Patch1).getAbsolutePath(),
                        FileUtil.toFile(compact1Patch2).getAbsolutePath()));        
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                src,
                systemModules,
                ClassPath.EMPTY,
                null,
                null));
        final Collection<URL> resURLs = collectEntries(cp);     
        final Collection<URL> expectedURLs = reads(
                systemModules,
                NamePredicate.create("java.logging"),   //NOI18N
                compact1Patch1,
                compact1Patch2);  
        assertEquals(expectedURLs, resURLs);
    }
    
    public void testPatchModuleWithDuplicates() throws Exception {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        createModuleInfo(src, "Modle", "java.logging"); //NOI18N
        final FileObject compact1Patch1 = createPatchFolder("java.logging.patch1");    //NOI18N
        final FileObject compact1Patch2 = createPatchFolder("java.logging.patch2");    //NOI18N
        final MockCompilerOptions opts = MockCompilerOptions.getInstance();
        assertNotNull("No MockCompilerOptions in Lookup", opts);
        opts.forRoot(src.getRoots()[0])
                .apply("--patch-module")    //NOI18N
                .apply(String.format(
                        "java.logging=%s",//NOI18N
                        FileUtil.toFile(compact1Patch1).getAbsolutePath()))
                .apply("--patch-module")    //NOI18N
                .apply(String.format("java.logging=%s",  //NOI18N
                        FileUtil.toFile(compact1Patch2).getAbsolutePath()));
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                src,
                systemModules,
                ClassPath.EMPTY,
                null,
                null));
        final Collection<URL> resURLs = collectEntries(cp);     
        final Collection<URL> expectedURLs = reads(
                systemModules,
                NamePredicate.create("java.logging"),   //NOI18N
                compact1Patch1);  
        assertEquals(expectedURLs, resURLs);
    }

    public void testPatchModuleWithSourcePatch_SysModules() throws Exception {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(tp);
        assertNotNull(src);
        createModuleInfo(src, "Modle", "java.logging"); //NOI18N
        final FileObject tests = tp.getProjectDirectory().createFolder("tests");
        final ClassPath testSourcePath = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(tests);
        final ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(BinaryForSourceQuery.findBinaryRoots(src.entries().get(0).getURL()).getRoots()[1]);
        MockCompilerOptions.getInstance().forRoot(tests)
                .apply("--patch-module")    //NOI18N
                .apply(String.format("Modle=%s", FileUtil.toFile(tests).getAbsolutePath())) //NOI18N
                .apply("--add-modules") //NOI18N
                .apply("Modle") //NOI18N
                .apply("--add-reads")   //NOI18N
                .apply("Modle=ALL-UNNAMED"); //NOI18N
        for (ClassPath.Entry e : src.entries()) {
            MockClassPathProvider.getInstance().forRoot(e.getRoot())
                    .apply(JavaClassPathConstants.MODULE_BOOT_PATH, systemModules)
                    .apply(ClassPath.BOOT, systemModules);
            IndexingManager.getDefault().refreshIndexAndWait(e.getURL(), null, true);
        }
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                testSourcePath,
                systemModules,
                userModules,
                ClassPath.EMPTY,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = reads(
                systemModules,
                NamePredicate.create("java.logging"));   //NOI18N
        assertEquals(expectedURLs, resURLs);
    }

    public void testPatchModuleWithSourcePatch_UserModules() throws Exception {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(tp);
        assertNotNull(src);
        createModuleInfo(src, "Modle", "java.logging"); //NOI18N
        final FileObject tests = tp.getProjectDirectory().createFolder("tests");
        final ClassPath testSourcePath = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(tests);
        final URL dist = BinaryForSourceQuery.findBinaryRoots(src.entries().get(0).getURL()).getRoots()[1];
        final ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(dist);
        MockCompilerOptions.getInstance().forRoot(tests)
                .apply("--patch-module")    //NOI18N
                .apply(String.format("Modle=%s", FileUtil.toFile(tests).getAbsolutePath())) //NOI18N
                .apply("--add-modules") //NOI18N
                .apply("Modle") //NOI18N
                .apply("--add-reads")   //NOI18N
                .apply("Modle=ALL-UNNAMED"); //NOI18N
        for (ClassPath.Entry e : src.entries()) {
            MockClassPathProvider.getInstance().forRoot(e.getRoot())
                    .apply(JavaClassPathConstants.MODULE_BOOT_PATH, systemModules)
                    .apply(ClassPath.BOOT, systemModules);
            IndexingManager.getDefault().refreshIndexAndWait(e.getURL(), null, true);
        }
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                userModules,
                testSourcePath,
                systemModules,
                userModules,
                ClassPath.EMPTY,
                null));
        assertEquals(
                Collections.singletonList(dist),
                collectEntries(cp));
    }

    public void testPatchModuleWithSourcePatch_UnscannedBaseModule() throws Exception {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(tp);
        assertNotNull(src);
        createModuleInfo(src, "Modle", "java.logging"); //NOI18N
        final FileObject tests = tp.getProjectDirectory().createFolder("tests");
        final ClassPath testSourcePath = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(tests);
        final URL dist = BinaryForSourceQuery.findBinaryRoots(src.entries().get(0).getURL()).getRoots()[1];
        final ClassPath userModules = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(dist);
        MockCompilerOptions.getInstance().forRoot(tests)
                .apply("--patch-module")    //NOI18N
                .apply(String.format("Modle=%s", FileUtil.toFile(tests).getAbsolutePath())) //NOI18N
                .apply("--add-modules") //NOI18N
                .apply("Modle") //NOI18N
                .apply("--add-reads")   //NOI18N
                .apply("Modle=ALL-UNNAMED"); //NOI18N
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                userModules,
                testSourcePath,
                systemModules,
                userModules,
                ClassPath.EMPTY,
                null));
        cp.entries();
        assertEquals(
                Collections.singletonList(dist),
                collectEntries(cp));
    }

    public void testDuplicateSourceDirsNETBEANS_817() throws Exception {
        if (systemModules == null) {
            System.out.println("No jdk 9 home configured.");    //NOI18N
            return;
        }
        assertNotNull(src);
        final FileObject moduleInfo = createModuleInfo(src, "Modle", "java.logging"); //NOI18N
        final ClassPath cp = ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                systemModules,
                org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(src, src),
                systemModules,
                ClassPath.EMPTY,
                null,
                null));
        final Collection<URL> resURLs = collectEntries(cp);
        final Collection<URL> expectedURLs = reads(systemModules, NamePredicate.create("java.logging"));  //NOI18N
        assertEquals(expectedURLs, resURLs);
    }

    private static void setSourceLevel(
            @NonNull final Project prj,
            @NonNull final String sourceLevel) throws IOException {
        try {
            final TestProject tprj = prj.getLookup().lookup(TestProject.class);
            if (tprj == null) {
                throw new IllegalStateException("No TestProject instance: " + prj); //NOI18N
            }
            ProjectManager.mutex().writeAccess((Mutex.ExceptionAction<Void>)()->{
                final EditableProperties ep = tprj.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                ep.setProperty(ProjectProperties.JAVAC_SOURCE, sourceLevel);
                ep.setProperty(ProjectProperties.JAVAC_TARGET, sourceLevel);
                tprj.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(prj);
                return null;
            });
        } catch (MutexException e) {
            throw e.getCause() instanceof IOException ?
                    (IOException) e.getCause() :
                    new IOException(e.getCause());
        }
    }
    
    @NonNull
    private static FileObject createModuleInfo(
            @NonNull final ClassPath src,
            @NonNull final String moduleName,
            @NonNull final String... requiredModules) throws IOException {
        final FileObject[] roots = src.getRoots();
        if (roots.length == 0) {
            throw new IOException("No source roots");   //NOI18N
        }
        final FileObject[] res = new FileObject[1];
        FileUtil.runAtomicAction((FileSystem.AtomicAction)() -> {
                res[0] = FileUtil.createData(roots[0], "module-info.java");    //NOI18N
                final StringBuilder module = new StringBuilder("module ").append(moduleName).append(" {");    //NOI18N
                for (String mod : requiredModules) {
                    module.append("requires ").append(mod).append(";");
                }
                module.append("}"); //NOI18N
                final FileLock lck = res[0].lock();
                try (OutputStream out = res[0].getOutputStream(lck);
                        InputStream in = new ByteArrayInputStream(module.toString().getBytes(FileEncodingQuery.getEncoding(res[0])))) {
                    FileUtil.copy(in, out);
                } finally {
                    lck.releaseLock();
                }
        });
        return res[0];
    }

    private FileObject createPatchFolder(final String name) throws IOException {
        return FileUtil.createFolder(FileUtil.normalizeFile(
                new File(getWorkDir(),name)));
    }
    
    private Collection<URL> collectEntries(@NonNull final ClassPath cp) {
        final List<URL> res = new ArrayList<>();
        for (ClassPath.Entry e : cp.entries()) {
            res.add(e.getURL());
        }
        res.sort(LEX_COMPARATOR);
        return res;
    }

    static Collection<URL> unnamedReads(@NonNull final ClassPath base) throws IOException {
        return reads(base, NamePredicate.create("java.se").or(NON_JAVA_PUB));  //NOI18N
    }

    private static Collection<URL> reads(
            @NonNull final ClassPath base,
            @NonNull final Predicate<ModuleElement> predicate,
            @NonNull final FileObject... additionalRoots) throws IOException {
        final ClasspathInfo info = new ClasspathInfo.Builder(base)
                .setModuleBootPath(base)
                .build();
        final Set<String> moduleNames = new HashSet<>();
        JavaSource.create(info).runUserActionTask((cc)-> {
                final Set<ModuleElement> rootModules = base.entries().stream()
                        .map((e) -> SourceUtils.getModuleName(e.getURL()))
                        .filter((n) -> n != null)
                        .map(cc.getElements()::getModuleElement)
                        .filter((m) -> m != null)
                        .filter(predicate)
                        .collect(Collectors.toSet());
                for (ModuleElement rootModule : rootModules) {
                    collectDeps(rootModule, moduleNames);
                }
            },
            true);
        final List<URL> l = new ArrayList<>();
        base.entries().stream()
                .map((e) -> e.getURL())
                .filter((url) -> moduleNames.contains(SourceUtils.getModuleName(url)))
                .forEach(l::add);
        Arrays.stream(additionalRoots)
                .map((fo) -> fo.toURL())
                .forEach(l::add);
        l.sort(LEX_COMPARATOR);
        return l;
    }
    
    private static void collectDeps(
        @NullAllowed final ModuleElement me,
        @NonNull final Collection<? super String> modNames) {
        if (me != null) {
            final String mn = me.getQualifiedName().toString();
            if (!modNames.contains(mn)) {
                modNames.add(mn);
                for (ModuleElement.Directive d : me.getDirectives()) {
                    if (d.getKind() == ModuleElement.DirectiveKind.REQUIRES) {
                        final ModuleElement.RequiresDirective rd = (ModuleElement.RequiresDirective)d;
                        if (rd.isTransitive()|| isMandated(rd)) {
                            collectDeps(rd.getDependency(), modNames);
                        }
                    }
                }
            }
        }
    }
    
    private static boolean isMandated(ModuleElement.RequiresDirective rd) {
        return Optional.ofNullable(rd.getDependency())
                .map((me) -> me.getQualifiedName().toString())
                .map((mn) -> "java.base".equals(mn))
                .orElse(Boolean.FALSE);
    }

    private static FileObject createJar(
            @NonNull final FileObject folder,
            @NonNull final String name,
            @NonNull final FileObject... content) throws IOException {
           final File f = FileUtil.toFile(folder);
           if (f == null) {
               throw new IOException("The " + FileUtil.getFileDisplayName(folder) +" is not local");
           }
           final File res = new File(f,name);
           try(JarOutputStream out = new JarOutputStream(new FileOutputStream(res))) {
               for (FileObject c : content) {
                   pack(out, c, c);
               }
           }
           return FileUtil.getArchiveRoot(FileUtil.toFileObject(res));
    }

    private static void pack(
            JarOutputStream out,
            FileObject f,
            FileObject root) throws IOException {
        if (f.isFolder()) {
            for (FileObject c : f.getChildren()) {
                pack(out, c, root);
            }
        } else {
            String path = FileUtil.getRelativePath(root, f);
            if (path.isEmpty()) {
                path = f.getNameExt();
            }
            out.putNextEntry(new ZipEntry(path));
            FileUtil.copy(f.getInputStream(), out);
            out.closeEntry();
        }
    }

    private static boolean compileModule(
            @NonNull final File src,
            @NonNull final File dest,
            @NonNull final File output) throws IOException, InterruptedException {
        final File f = TestUtilities.getJava9Home();
        if (f == null) {
            return false;
        }
        final FileObject jdkHome = FileUtil.toFileObject(f);
        if (jdkHome == null) {
            throw new IOException("Cannot find: " + f.getAbsolutePath());
        }
        final FileObject javac = Util.findTool("javac", Collections.singleton(jdkHome));
        if (javac == null) {
            throw new IOException("No javac in: " + f.getAbsolutePath());
        }
        final List<String> toCompile = Files.walk(src.toPath())
                .filter((p) -> p.getFileName().toString().endsWith(".java"))
                .map((p) -> p.toAbsolutePath().toString())
                .collect(Collectors.toList());
        if (toCompile.isEmpty()) {
            return true;
        }
        final List<String> cmd = new ArrayList<>(toCompile.size() + 7);
        cmd.add(FileUtil.toFile(javac).getAbsolutePath());
        cmd.add("-s");  //NOI18N
        cmd.add(src.getAbsolutePath());
        cmd.add("-d");  //NOI18N
        cmd.add(dest.getAbsolutePath());
        cmd.add("-source"); //NOI18N
        cmd.add("9");   //NOI18N
        cmd.addAll(toCompile);
        final ProcessBuilder pb = new ProcessBuilder(cmd)
            .redirectErrorStream(true)
            .redirectOutput(output);
        final Process p = pb.start();
        return p.waitFor() == 0;
    }

    private static final class NamePredicate implements Predicate<ModuleElement> {
        private final String name;
                
        private NamePredicate(@NonNull final String name) {
            this.name = name;
        }

        @Override
        public boolean test(ModuleElement t) {
            return name.equals(t.getQualifiedName().toString());
        }       
        
        @NonNull
        static Predicate<ModuleElement> create(@NonNull final String name) {
            return new NamePredicate(name);
        }
    } 
    
    private static final class MockCompilerOptions implements CompilerOptionsQueryImplementation {
        private final Map<FileObject,List<String>> options = new HashMap<>();
        
        @NonNull
        <T extends Function<String, T>> Function<String,T> forRoot(@NonNull final FileObject root) {
            final List<String> args = options.computeIfAbsent(root, (k) -> new ArrayList<>());
            args.clear();
            return new Function<String,T> () {
                @Override
                public T apply(String t) {
                    args.add(t);
                    return (T) this;
                }
            };
        }

        @Override
        @NonNull
        public Result getOptions(FileObject file) {
            for (Map.Entry<FileObject,List<String>> option : options.entrySet()) {
                if (isArtifact(option.getKey(), file)) {
                    return new R(option.getValue());
                }
            }
            return null;
        }
        
        @CheckForNull
        static MockCompilerOptions getInstance() {
            return Lookup.getDefault().lookup(MockCompilerOptions.class);
        }
        
        private static boolean isArtifact(
                @NonNull final FileObject root,
                @NonNull final FileObject file) {
            return root.equals(file) || FileUtil.isParentOf(root, file);
        }
        
        private static final class R extends Result {
            private final List<String> args;
            
            R(@NonNull final List<String> args) {
                this.args = args;
            }

            @Override
            @NonNull
            public List<? extends String> getArguments() {
                return Collections.unmodifiableList(args);
            }

            @Override
            public void addChangeListener(ChangeListener listener) {
            }

            @Override
            public void removeChangeListener(ChangeListener listener) {
            }            
        }        
    }

    private static final class MockClassPathProvider implements ClassPathProvider {
        private final Map<FileObject,Map<String,ClassPath>> cps;
        MockClassPathProvider() {
            cps = new HashMap<>();
        }

        <T extends BiFunction<String,ClassPath,T>> T forRoot(@NonNull final FileObject root) {
            final Map<String,ClassPath> entry = new HashMap<>();
            cps.put(root, entry);
            final BiFunction<String,ClassPath,T> res = new BiFunction<String,ClassPath,T>() {
                @Override
                public T apply(String id, ClassPath cp) {
                    entry.put(id, cp);
                    return (T) this;
                }
            };
            return (T) res;
        }

        @CheckForNull
        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            for (Map.Entry<FileObject,Map<String,ClassPath>> e : cps.entrySet()) {
                if (e.getKey().equals(file) || FileUtil.isParentOf(e.getKey(), file)) {
                    return e.getValue().get(type);
                }
            }
            return null;
        }

        @CheckForNull
        static MockClassPathProvider getInstance() {
            return Lookup.getDefault().lookup(MockClassPathProvider.class);
        }
    }
}
