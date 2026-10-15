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

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import org.netbeans.modules.java.j2seplatform.queries.DefaultClassPathProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.DataOutputStream;
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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import junit.framework.TestSuite;
import org.netbeans.api.annotations.common.NonNull;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.ProjectGroup;
import org.netbeans.api.project.ui.ProjectGroupChangeListener;



import org.netbeans.core.startup.layers.ArchiveURLMapper;
import org.netbeans.junit.MockServices;



import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.masterfs.MasterURLMapper;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;

import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.explorer.ExplorerManager;


import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.Utilities;


/**
 *
 * @author  tom
 */
public class DefaultClassPathProviderTest extends NbTestCase {
    
    private static final int FILE_IN_PACKAGE = 0;
    private static final int FILE_IN_BAD_PACKAGE = 1;
    private static final int FILE_IN_DEFAULT_PACKAGE = 2;
    private static final int DELAY = 2500;

    private static final byte[] CLASS_FILE_DATA = {
        (byte)0xca, (byte)0xfe, (byte)0xba, (byte)0xbe, 0x00, 0x00, 0x00, 0x2e, 0x00, 0x0d, 0x0a, 0x00, 0x03, 0x00, 0x0a, 0x07, 0x00, 0x0b, 0x07, 0x00,
        0x0c, 0x01, 0x00, 0x06, 0x3c, 0x69, 0x6e, 0x69, 0x74, 0x3e, 0x01, 0x00, 0x03, 0x28, 0x29, 0x56, 0x01, 0x00, 0x04, 0x43,
        0x6f, 0x64, 0x65, 0x01, 0x00, 0x0f, 0x4c, 0x69, 0x6e, 0x65, 0x4e, 0x75, 0x6d, 0x62, 0x65, 0x72, 0x54, 0x61, 0x62, 0x6c,
        0x65, 0x01, 0x00, 0x0a, 0x53, 0x6f, 0x75, 0x72, 0x63, 0x65, 0x46, 0x69, 0x6c, 0x65, 0x01, 0x00, 0x09, 0x54, 0x65, 0x73,
        0x74, 0x2e, 0x6a, 0x61, 0x76, 0x61, 0x0c, 0x00, 0x04, 0x00, 0x05, 0x01, 0x00, 0x09, 0x74, 0x65, 0x73, 0x74, 0x2f, 0x54,
        0x65, 0x73, 0x74, 0x01, 0x00, 0x10, 0x6a, 0x61, 0x76, 0x61, 0x2f, 0x6c, 0x61, 0x6e, 0x67, 0x2f, 0x4f, 0x62, 0x6a, 0x65,
        0x63, 0x74, 0x00, 0x21, 0x00, 0x02, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x04, 0x00, 0x05,
        0x00, 0x01, 0x00, 0x06, 0x00, 0x00, 0x00, 0x1d, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x2a, (byte)0xb7, 0x00, 0x01,
        (byte)0xb1, 0x00, 0x00, 0x00, 0x01, 0x00, 0x07, 0x00, 0x00, 0x00, 0x06, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x00, 0x01, 0x00,
        0x08, 0x00, 0x00, 0x00, 0x02, 0x00, 0x09
    };

    private FileObject srcRoot;
    private FileObject[] srcFile = new FileObject[3];
    private FileObject[] compileRoots;
    private static FileObject[] execRoots;
    private static FileObject[] libSourceRoots;
    private FileObject execTestDir;
    private JavaPlatform j9;

    /** Creates a new instance of DefaultClassPathProviderTest */
    public DefaultClassPathProviderTest (String testName) {
        super (testName);
        MockServices.setServices(
                ArchiveURLMapper.class,
                MasterURLMapper.class,
                JavaPlatformProviderImpl.class,
                SFBQI.class,
                OpenProject.class,
                MockSLQ.class);
    }

    public static TestSuite suite() {
        final NbTestSuite suite = new NbTestSuite();
        suite.addTest(new DefaultClassPathProviderTest("testFindClassPath"));   //NOI18N
        suite.addTest(new DefaultClassPathProviderTest("testCycle"));           //NOI18N
        suite.addTest(new DefaultClassPathProviderTest("testEvents"));          //NOI18N
        suite.addTest(new DefaultClassPathProviderTest("testModularSources_SystemModules"));  //NOI18N
        suite.addTest(new DefaultClassPathProviderTest("testJavaPlatformCaching"));  //NOI18N
        suite.addTest(new DefaultClassPathProviderTest("testLRUCaching"));  //NOI18N
        suite.addTest(new DefaultClassPathProviderTest("testJPMChanges"));  //NOI18N
        suite.addTest(new DefaultClassPathProviderTest("testModularSources_UserModules"));  //NOI18N
        return suite;
    }
    
    
    @Override
    protected void tearDown () throws Exception {
        this.srcRoot = null;
        this.compileRoots = null;
        if (j9 != null) {
            Lookup.getDefault().lookup(JavaPlatformProviderImpl.class).removePlatform(j9);
        }
        super.tearDown();
    }
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        final RunnableFuture<Project[]> dummyFuture = new FutureTask<>(new Callable<Project[]>() {
            @Override
            public Project[] call() throws Exception {
                return new Project[0];
            }
        });
        dummyFuture.run();
        assertTrue(dummyFuture.isDone());
        OpenProject.future = dummyFuture;
        FileObject workDir = FileUtil.toFileObject(this.getWorkDir());
        assertNotNull("MasterFS is not configured.", workDir);
        this.srcRoot = workDir.createFolder("src");
        this.compileRoots = new FileObject[3];
        for (int i=0; i< this.compileRoots.length; i++) {
            this.compileRoots[i] = workDir.createFolder("lib_"+Integer.toString(i));
        }
        ClassPath cp = ClassPathSupport.createClassPath(this.compileRoots);
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] {cp});
       this.execRoots = new FileObject[2];
        this.execRoots[0] = this.compileRoots[2];
        this.execRoots[1] = workDir.createFolder("lib_OnlyExec");
        cp = ClassPathSupport.createClassPath(this.execRoots);
        GlobalPathRegistry.getDefault().register (ClassPath.EXECUTE, new ClassPath[]{cp});
        this.libSourceRoots = new FileObject[2];
        for (int i=0; i< libSourceRoots.length; i++) {
            this.libSourceRoots[i] = workDir.createFolder ("libSrc_"+Integer.toString(i));
        }
        cp = ClassPathSupport.createClassPath (this.libSourceRoots);
        GlobalPathRegistry.getDefault().register (ClassPath.SOURCE, new ClassPath[]{cp});
        execTestDir = workDir.createFolder("exec");

        FileObject java9 = FileUtil.createFolder(workDir, "java9"); //NOI18N
        FileObject javaBase = FileUtil.createFolder(java9, "modules/java.base"); //NOI18N
        j9 = new J9(java9, javaBase);
        Lookup.getDefault().lookup(JavaPlatformProviderImpl.class).addPlatform(j9);
        Lookup.getDefault().lookup(MockSLQ.class).reset();
    }
    
    
    
    public void testFindClassPath () throws IOException {
        FileObject artefact = getSourceFile (FILE_IN_PACKAGE);
        ClassPathProvider cpp = new DefaultClassPathProvider ();
        ClassPath cp = cpp.findClassPath(artefact, ClassPath.SOURCE);
        assertNull ("DefaultClassPathProvider returned not null for SOURCES",cp);
//        assertNotNull ("DefaultClassPathProvider returned null for SOURCES",cp);
//        assertEquals("Invalid length of classpath for SOURCE",1,cp.getRoots().length);
//        assertRootsEquals ("Invalid classpath roots for SOURCE", cp, new FileObject[] {this.srcRoot});
        cp = cpp.findClassPath(artefact, ClassPath.COMPILE);        
        assertNotNull ("DefaultClassPathProvider returned null for COMPILE",cp);
        assertEquals("Invalid length of classpath for COMPILE",this.compileRoots.length + 1, cp.getRoots().length);
        FileObject[] resRoots = new FileObject[this.compileRoots.length + 1];
        System.arraycopy(this.compileRoots,0,resRoots,0,this.compileRoots.length);
        resRoots[this.compileRoots.length] = this.execRoots[1];
        assertRootsEquals ("Invalid classpath roots for COMPILE", cp, resRoots);
        cp = cpp.findClassPath(artefact, ClassPath.BOOT);
        assertNotNull ("DefaultClassPathProvider returned null for BOOT",cp);
        JavaPlatform dp = JavaPlatformManager.getDefault().getDefaultPlatform();
        assertEquals("Invalid length of classpath for BOOT",dp.getBootstrapLibraries().getRoots().length, cp.getRoots().length);
        assertRootsEquals ("Invalid classpath roots for BOOT", cp, dp.getBootstrapLibraries().getRoots());

        artefact = getSourceFile (FILE_IN_DEFAULT_PACKAGE);
        cp = cpp.findClassPath(artefact, ClassPath.SOURCE);
        assertNull ("DefaultClassPathProvider returned not null for SOURCES",cp);
//        assertNotNull ("DefaultClassPathProvider returned null for SOURCES",cp);
//        assertEquals("Invalid length of classpath for SOURCE",1,cp.getRoots().length);
//        assertRootsEquals ("Invalid classpath roots for SOURCE", cp, new FileObject[] {this.srcRoot});
        
        artefact = getSourceFile (FILE_IN_BAD_PACKAGE);
        cp = cpp.findClassPath(artefact, ClassPath.SOURCE);
        assertNull ("DefaultClassPathProvider returned not null for SOURCES",cp);
//        assertNotNull ("DefaultClassPathProvider returned null for SOURCES",cp);
//        assertEquals("Invalid length of classpath for SOURCE",1,cp.getRoots().length);
//        FileObject badRoot = this.srcRoot.getFileObject ("test");
//        assertRootsEquals ("Invalid classpath roots for SOURCE", cp, new FileObject[] {badRoot});      //ERROR
        FileObject classFile = getClassFile();
        cp = cpp.findClassPath(classFile, ClassPath.EXECUTE);
        assertNotNull ("DefaultClassPathProvider returned null for EXECUTE",cp);
        assertEquals("Invalid length of classpath for EXECUTE",1,cp.getRoots().length);
        assertEquals("Illegal classpath for EXECUTE: ",cp.getRoots()[0],this.execTestDir);
    }
    
    public void testCycle () throws Exception {
        GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
        Set<ClassPath> toCleanUp = regs.getPaths(ClassPath.COMPILE);        
        regs.unregister(ClassPath.COMPILE, toCleanUp.toArray(new ClassPath[0]));
        toCleanUp = regs.getPaths(ClassPath.EXECUTE);        
        regs.unregister(ClassPath.EXECUTE, toCleanUp.toArray(new ClassPath[0]));
        File wdf = getWorkDir();
        FileObject wd = FileUtil.toFileObject(wdf);
        FileObject root1 = wd.createFolder("root1");
        FileObject root2 = wd.createFolder("root2");
        ClassPathProvider cpp = new DefaultClassPathProvider ();
        ClassPath dcp = cpp.findClassPath(root2, ClassPath.COMPILE);
        ClassPath cp = ClassPathSupport.createClassPath(new FileObject[] {root1});
        regs.register(ClassPath.COMPILE, new ClassPath[] {cp});        
        assertNotNull(dcp);
        FileObject[] roots = dcp.getRoots();
        assertEquals(1, roots.length);
        assertEquals(root1, roots[0]);
        
        regs.register(ClassPath.COMPILE, new ClassPath[] {dcp});
        roots = dcp.getRoots();
        assertEquals(1, roots.length);        
    }

    /**
     * Checks that single event is fired during opening (closing) of projects
     */
    public void testEvents() throws Exception {
        final File wd = getWorkDir();
        final File root1 = FileUtil.normalizeFile(new File (wd,"src1"));    //NOI18N
        final File root2 = FileUtil.normalizeFile(new File (wd,"src2"));    //NOI18N
        root1.mkdir();
        root2.mkdir();
        final ClassPathProvider cpp = new DefaultClassPathProvider ();
        final ClassPath defaultCompile = cpp.findClassPath(FileUtil.toFileObject(wd), ClassPath.COMPILE);
        assertNotNull(defaultCompile);
        assertFalse(contains(defaultCompile, Utilities.toURI(root1).toURL()));
        assertFalse(contains(defaultCompile, Utilities.toURI(root2).toURL()));
        final Listener listener = new Listener();
        defaultCompile.addPropertyChangeListener(listener);
        final GlobalPathRegistry regs = GlobalPathRegistry.getDefault();
        final ClassPath compileCpProject1 = ClassPathSupport.createClassPath(Utilities.toURI(root1).toURL());
        final ClassPath compileCpProject2 = ClassPathSupport.createClassPath(Utilities.toURI(root2).toURL());
        
        //Simulate projects open
        RunnableFuture<Project[]> barrier = new FutureTask<Project[]>(new Callable<Project[]>() {
            @Override
            public Project[] call() throws Exception {
                return new Project[0];
            }
        });
        OpenProject.future = barrier;

        regs.register(ClassPath.COMPILE, new ClassPath[]{compileCpProject1});
        assertFalse(contains(defaultCompile, Utilities.toURI(root1).toURL()));
        assertFalse(listener.awaitEvent());
        assertEquals(0, listener.get());
        regs.register(ClassPath.COMPILE, new ClassPath[]{compileCpProject2});
        assertFalse(contains(defaultCompile, Utilities.toURI(root2).toURL()));
        assertFalse(listener.awaitEvent());
        assertEquals(0, listener.get());
        barrier.run();
        assertTrue(listener.awaitEvent());
        assertEquals(1, listener.get());
        assertTrue(contains(defaultCompile, Utilities.toURI(root1).toURL()));
        assertTrue(contains(defaultCompile, Utilities.toURI(root2).toURL()));

        //Simulate projects close
        barrier = new FutureTask<Project[]>(new Callable<Project[]>() {
            @Override
            public Project[] call() throws Exception {
                return new Project[0];
            }
        });
        OpenProject.future = barrier;
        listener.reset();
        regs.unregister(ClassPath.COMPILE, new ClassPath[]{compileCpProject1});
        assertTrue(contains(defaultCompile, Utilities.toURI(root1).toURL()));
        assertFalse(listener.awaitEvent());
        assertEquals(0, listener.get());
        regs.unregister(ClassPath.COMPILE, new ClassPath[]{compileCpProject2});
        assertTrue(contains(defaultCompile, Utilities.toURI(root2).toURL()));
        assertFalse(listener.awaitEvent());
        assertEquals(0, listener.get());
        barrier.run();
        assertTrue(listener.awaitEvent());
        assertEquals(1, listener.get());
        assertFalse(contains(defaultCompile, Utilities.toURI(root1).toURL()));
        assertFalse(contains(defaultCompile, Utilities.toURI(root2).toURL()));
    }

    public void testModularSources_SystemModules() throws Exception {
        final FileObject artefact = getSourceFile (FILE_IN_PACKAGE);
        Lookup.getDefault().lookup(MockSLQ.class).register(this.srcRoot, new SpecificationVersion("1.8"));  //NOI18N
        assertEquals("1.8", SourceLevelQuery.getSourceLevel(artefact)); //NOI18N
        ClassPathProvider cpp = new DefaultClassPathProvider ();
        ClassPath cp = cpp.findClassPath(artefact, JavaClassPathConstants.MODULE_BOOT_PATH);
        assertNull ("DefaultClassPathProvider returned not null for MODULE_BOOT_PATH with source level 8",cp);
        cp = cpp.findClassPath(artefact, ClassPath.BOOT);
        assertEquals("DefaultClassPathProvider returned invalid classpath for BOOT with source level 8",
                JavaPlatform.getDefault().getBootstrapLibraries(),
                cp);
        Lookup.getDefault().lookup(MockSLQ.class).register(this.srcRoot, new SpecificationVersion("9"));  //NOI18N
        assertEquals("9", SourceLevelQuery.getSourceLevel(artefact)); //NOI18N
        cpp = new DefaultClassPathProvider ();
        cp = cpp.findClassPath(artefact, JavaClassPathConstants.MODULE_BOOT_PATH);
        assertEquals ("DefaultClassPathProvider returned invalid classpath for MODULE_BOOT_PATH with source level 9",
                j9.getBootstrapLibraries(),
                cp);
        cp = cpp.findClassPath(artefact, ClassPath.BOOT);
        assertEquals("DefaultClassPathProvider returned invalid classpath for BOOT with source level 9",
                j9.getBootstrapLibraries(),
                cp);
    }

    public void testJavaPlatformCaching() throws IOException {
        FileObject artefact = getSourceFile (FILE_IN_PACKAGE);
        Lookup.getDefault().lookup(MockSLQ.class).register(this.srcRoot, new SpecificationVersion("9"));  //NOI18N
        assertEquals("9", SourceLevelQuery.getSourceLevel(artefact)); //NOI18N
        ClassPathProvider cpp = new DefaultClassPathProvider ();
        final Logger log = Logger.getLogger(DefaultClassPathProvider.class.getName());
        final H h = new H();
        final Level origLevel = log.getLevel();
        log.setLevel(Level.FINE);
        log.addHandler(h);
        try {
            ClassPath cp = cpp.findClassPath(artefact, JavaClassPathConstants.MODULE_BOOT_PATH);
            assertEquals ("DefaultClassPathProvider returned invalid classpath for MODULE_BOOT_PATH with source level 9",
                    j9.getBootstrapLibraries(),
                    cp);
            List<Optional<JavaPlatform>> plts = h.getCachedPlatforms();
            assertEquals(1, plts.size());
            assertEquals(j9, plts.get(0).get());
            h.reset();
            artefact = getSourceFile (FILE_IN_PACKAGE);
            cp = cpp.findClassPath(artefact, JavaClassPathConstants.MODULE_BOOT_PATH);
            assertEquals ("DefaultClassPathProvider returned invalid classpath for MODULE_BOOT_PATH with source level 9",
                    j9.getBootstrapLibraries(),
                    cp);
            plts = h.getCachedPlatforms();
            assertEquals(0, plts.size());
        } finally {
            log.removeHandler(h);
            log.setLevel(origLevel);
        }
    }

    public void testLRUCaching() throws IOException {
        FileObject artefact = getSourceFile (FILE_IN_PACKAGE);
        Lookup.getDefault().lookup(MockSLQ.class).register(this.srcRoot, new SpecificationVersion("9"));  //NOI18N
        assertEquals("9", SourceLevelQuery.getSourceLevel(artefact)); //NOI18N
        ClassPathProvider cpp = new DefaultClassPathProvider ();
        final Logger log = Logger.getLogger(DefaultClassPathProvider.class.getName());
        final H h = new H();
        final Level origLevel = log.getLevel();
        log.setLevel(Level.FINE);
        log.addHandler(h);
        try {
            ClassPath cp = cpp.findClassPath(artefact, JavaClassPathConstants.MODULE_BOOT_PATH);
            assertEquals ("DefaultClassPathProvider returned invalid classpath for MODULE_BOOT_PATH with source level 9",
                    j9.getBootstrapLibraries(),
                    cp);
            List<JavaPlatform> plts = h.getLRU();
            assertEquals(1, plts.size());
            assertEquals(j9, plts.get(0));
            h.reset();
            artefact = getSourceFile (FILE_IN_PACKAGE);
            cp = cpp.findClassPath(artefact, JavaClassPathConstants.MODULE_BOOT_PATH);
            assertEquals ("DefaultClassPathProvider returned invalid classpath for MODULE_BOOT_PATH with source level 9",
                    j9.getBootstrapLibraries(),
                    cp);
            plts = h.getLRU();
            assertEquals(0, plts.size());
        } finally {
            log.removeHandler(h);
            log.setLevel(origLevel);
        }
    }

    public void testJPMChanges() throws IOException {
        final FileObject artefact = getSourceFile (FILE_IN_PACKAGE);
        Lookup.getDefault().lookup(MockSLQ.class).register(this.srcRoot, new SpecificationVersion("9"));  //NOI18N
        assertEquals("9", SourceLevelQuery.getSourceLevel(artefact)); //NOI18N
        final JavaPlatform j9Orig = Arrays.stream(JavaPlatformManager.getDefault().getInstalledPlatforms())
                .filter((jp) -> "j2se".equals(jp.getSpecification().getName())   //NOI18N
                        && new SpecificationVersion("9").equals(jp.getSpecification().getVersion()))
                .findFirst()
                .get();
        ClassPathProvider cpp = new DefaultClassPathProvider ();
        final Logger log = Logger.getLogger(DefaultClassPathProvider.class.getName());
        final H h = new H();
        final Level origLevel = log.getLevel();
        log.setLevel(Level.FINE);
        log.addHandler(h);
        try {
            ClassPath cp = cpp.findClassPath(artefact, JavaClassPathConstants.MODULE_BOOT_PATH);
            assertEquals ("DefaultClassPathProvider returned invalid classpath for MODULE_BOOT_PATH with source level 9",
                    j9Orig.getBootstrapLibraries(),
                    cp);
            List<Optional<JavaPlatform>> plts = h.getCachedPlatforms();
            assertEquals(1, plts.size());
            assertEquals(j9Orig, plts.get(0).get());
            List<JavaPlatform> lru = h.getLRU();
            assertEquals(1, lru.size());
            assertEquals(j9Orig, lru.get(0));
            h.reset();
            Lookup.getDefault().lookup(JavaPlatformProviderImpl.class).removePlatform(j9Orig);
            cp = cpp.findClassPath(artefact, JavaClassPathConstants.MODULE_BOOT_PATH);
            assertEquals ("DefaultClassPathProvider returned invalid classpath for MODULE_BOOT_PATH with source level 9",
                    null,
                    cp);
            plts = h.getCachedPlatforms();
            assertEquals(1, plts.size());
            assertFalse(plts.get(0).isPresent());
            lru = h.getLRU();
            assertEquals(1, lru.size());
            assertNull(lru.get(0));
            h.reset();
            Lookup.getDefault().lookup(JavaPlatformProviderImpl.class).addPlatform(j9Orig);
            cp = cpp.findClassPath(artefact, JavaClassPathConstants.MODULE_BOOT_PATH);
            assertEquals ("DefaultClassPathProvider returned invalid classpath for MODULE_BOOT_PATH with source level 9",
                    j9Orig.getBootstrapLibraries(),
                    cp);
            plts = h.getCachedPlatforms();
            assertEquals(1, plts.size());
            assertEquals(j9Orig, plts.get(0).get());
            lru = h.getLRU();
            assertEquals(1, lru.size());
            assertEquals(j9Orig, lru.get(0));
        } finally {
            log.removeHandler(h);
            log.setLevel(origLevel);
        }
    }

    public void testModularSources_UserModules() throws Exception {
        final FileObject workDir = FileUtil.toFileObject(FileUtil.normalizeFile(this.getWorkDir()));
        final FileObject modulesFolder = FileUtil.createFolder(workDir, "modules");
        final FileObject[] modules = new FileObject[3];
        for (int i = 0; i < modules.length; i++) {
            modules[i] = FileUtil.createFolder(modulesFolder, "module" + (char) ('a'+i));
        }
        final ClassPath mp = ClassPathSupport.createClassPath(modules);
        GlobalPathRegistry.getDefault().register(JavaClassPathConstants.MODULE_COMPILE_PATH, new ClassPath[]{mp});
        try {
            final FileObject artefact = libSourceRoots[0];
            Lookup.getDefault().lookup(MockSLQ.class).register(artefact, new SpecificationVersion("1.8"));  //NOI18N
            assertEquals("1.8", SourceLevelQuery.getSourceLevel(artefact)); //NOI18N
            ClassPathProvider cpp = new DefaultClassPathProvider ();
            ClassPath cp = cpp.findClassPath(artefact, JavaClassPathConstants.MODULE_COMPILE_PATH);
            assertNull ("DefaultClassPathProvider returned not null for MODULE_COMPILE_PATH with source level 8",cp);
            Lookup.getDefault().lookup(MockSLQ.class).register(artefact, new SpecificationVersion("9"));  //NOI18N
            assertEquals("9", SourceLevelQuery.getSourceLevel(artefact)); //NOI18N
            cpp = new DefaultClassPathProvider ();
            cp = cpp.findClassPath(artefact, JavaClassPathConstants.MODULE_COMPILE_PATH);
            assertEquals(
                    mp.entries().stream()
                            .map((e) -> e.getRoot())
                            .sorted((a,b) -> a.getPath().compareTo(b.getPath()))
                            .collect(Collectors.toList()),
                    cp.entries().stream()
                            .map((e) -> e.getRoot())
                            .sorted((a,b) -> a.getPath().compareTo(b.getPath()))
                            .collect(Collectors.toList()));
        } finally {
            GlobalPathRegistry.getDefault().unregister(JavaClassPathConstants.MODULE_COMPILE_PATH, new ClassPath[]{mp});
        }
    }

    private static boolean contains (final ClassPath cp, final URL url) {
        for (ClassPath.Entry entry : cp.entries()) {
            if (entry.getURL().equals(url)) {
                return true;
            }
        }
        return false;
    }
    
    
    private static void assertRootsEquals (String message, ClassPath cp, FileObject[] roots) {
        Set<FileObject> cpRoots = new HashSet<FileObject>(Arrays.asList(cp.getRoots()));
        assertEquals(message, cpRoots.size(), roots.length);
        for (FileObject root : roots) {
            if (!cpRoots.contains(root)) {
                assertTrue(message, false);
            }
        }
    }
    
    private synchronized FileObject getSourceFile (int type) throws IOException {
        if (this.srcFile[type]==null) {
            assertNotNull (this.srcRoot);
            switch (type) {
                case FILE_IN_PACKAGE:
                    this.srcFile[type] = createFile (this.srcRoot,"test","Test","package test;\npublic class Test {}");                    
                    break;
                case FILE_IN_DEFAULT_PACKAGE:
                    this.srcFile[type] = createFile (this.srcRoot,null,"DefaultTest","public class DefaultTest {}");                    
                    break;
                case FILE_IN_BAD_PACKAGE:
                    this.srcFile[type] = createFile (this.srcRoot,"test","BadTest","package bad;\npublic class BadTest {}");                    
                    break;
                default:
                    throw new IllegalArgumentException ();
            }
        }
        return this.srcFile[type];
    }

    private synchronized FileObject getClassFile () throws IOException {
        FileObject fo = this.execTestDir.getFileObject("test/Test.class");
        if (fo == null) {
            fo = execTestDir.createFolder("test");
            fo = fo.createData("Test","class");
            FileLock lock = fo.lock();
            try {
                DataOutputStream out = new DataOutputStream (fo.getOutputStream(lock));
                try {
                    out.write(CLASS_FILE_DATA);
                    out.flush();
                } finally {
                    out.close();
                }
            } finally {
                lock.releaseLock();
            }
        }
        return fo;
    }

    private static FileObject createFile (FileObject root, String folderName, String name, String body) throws IOException {
        if (folderName != null) {
            FileObject tmp = root.getFileObject(folderName,null);
            if (tmp == null) {
                tmp = root.createFolder (folderName);
            }
            root = tmp;
        }
        FileObject file = root.createData (name,"java");
        FileLock lock = file.lock();
        try {
            PrintWriter out = new PrintWriter ( new OutputStreamWriter (file.getOutputStream(lock)));
            try {
                out.println (body);
            } finally {
                out.close ();
            }
        } finally {
            lock.releaseLock();
        }
        return file;
    }
    
    
    
    public static class SFBQI implements SourceForBinaryQueryImplementation {
        
        
        public SFBQI () {
        }
        
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            for (int i = 0; i < execRoots.length; i++) {
                final URL url = execRoots[i].toURL ();
                if (url.equals (binaryRoot)) {
                    return new SourceForBinaryQuery.Result () {

                        public FileObject[] getRoots () {
                            return libSourceRoots;
                        }

                        public void addChangeListener (ChangeListener l) {
                        }

                        public void removeChangeListener (ChangeListener l) {
                        }
                    };
                }
            }
            return null;
        }
    }

    public static class OpenProject implements  OpenProjectsTrampoline {

        static Future<Project[]> future;

        @Override
        public Project[] getOpenProjectsAPI() {
            return new Project[0];
        }

        @Override
        public void openAPI(Project[] projects, boolean openRequiredProjects, boolean showProgress) {
        }

        @Override
        public void closeAPI(Project[] projects) {
        }

        @Override
        public Project getMainProject() {
            return null;
        }

        @Override
        public void setMainProject(Project project) {
        }

        @Override
        public void addPropertyChangeListenerAPI(PropertyChangeListener listener, Object source) {
        }

        @Override
        public void removePropertyChangeListenerAPI(PropertyChangeListener listener) {
        }

        @Override
        public Future<Project[]> openProjectsAPI() {
            return future;
        }

        @Override
        public ProjectGroup getActiveProjectGroupAPI() {
            return null;
        }

        @Override
        public void addProjectGroupChangeListenerAPI(ProjectGroupChangeListener listener) {
        }

        @Override
        public void removeProjectGroupChangeListenerAPI(ProjectGroupChangeListener listener) {
        }

        @Override
        public ExplorerManager createLogicalView() {
            return new ExplorerManager();
        }

        @Override
        public ExplorerManager createPhysicalView() {
            return new ExplorerManager();
        }

    }

    private static class Listener implements PropertyChangeListener {

        private volatile int count;
        private final Lock lock = new ReentrantLock();
        private final Condition cnd = lock.newCondition();

        public void reset() {
            lock.lock();
            try {
                count = 0;
            } finally {
                lock.unlock();
            }
        }

        public int get() {
            return count;
        }

        public boolean awaitEvent() throws InterruptedException {
            lock.lock();
            try {
                while (count == 0) {
                    if (!cnd.await(DELAY, TimeUnit.MILLISECONDS)) {
                        return false;
                    }
                }
            } finally {
                lock.unlock();
            }
            return true;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ClassPath.PROP_ENTRIES.equals(evt.getPropertyName())) {
                lock.lock();
                try {
                    count = count + 1;
                    cnd.signal();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    private static final class J9 extends JavaPlatform {
        private final FileObject installFolder;
        private final ClassPath boot;

        public J9(
                @NonNull final FileObject installFolder,
                @NonNull final FileObject javaBase) {
            assert installFolder != null;
            assert javaBase != null;
            this.installFolder = installFolder;
            this.boot = ClassPathSupport.createClassPath(javaBase);
        }

        @Override
        public String getDisplayName() {
            return installFolder.getNameExt();
        }

        @Override
        public ClassPath getBootstrapLibraries() {
            return boot;
        }

        @Override
        public ClassPath getStandardLibraries() {
            return ClassPath.EMPTY;
        }

        @Override
        public ClassPath getSourceFolders() {
            return ClassPath.EMPTY;
        }

        @Override
        public List<URL> getJavadocFolders() {
            return Collections.emptyList();
        }

        @Override
        public String getVendor() {
            return "Oracle";    //NOI18N
        }

        @Override
        public Collection<FileObject> getInstallFolders() {
            return Collections.singleton(installFolder);
        }

        @Override
        public FileObject findTool(String toolName) {
            return null;
        }

        @Override
        public Map<String, String> getProperties() {
            return Collections.emptyMap();
        }

        @Override
        public Specification getSpecification() {
            return new Specification("j2se", new SpecificationVersion("9"));    //NOI18N
        }
    }

    public static final class MockSLQ implements SourceLevelQueryImplementation2 {
        private Map<FileObject,R> levels;

        public MockSLQ() {
            this.levels = new HashMap<>();
        }

        void register(
                @NonNull final FileObject root,
                @NonNull final SpecificationVersion sl) {
            assert root != null;
            levels.put(root, new R(sl));
        }

        void reset() {
            levels.clear();
        }

        @Override
        public Result getSourceLevel(FileObject javaFile) {
            for (Map.Entry<FileObject,R> e : this.levels.entrySet()) {
                final FileObject root = e.getKey();
                if (root.equals(javaFile) || FileUtil.isParentOf(root, javaFile)) {
                    return e.getValue();
                }
            }
            return null;
        }

        private static final class R implements Result {
            private final SpecificationVersion sl;

            R(@NonNull final SpecificationVersion sl) {
                assert sl != null;
                this.sl = sl;
            }

            @Override
            public String getSourceLevel() {
                return sl.toString();
            }

            @Override
            public void addChangeListener(ChangeListener listener) {
            }

            @Override
            public void removeChangeListener(ChangeListener listener) {
            }
        }
    }

    private static final class H extends Handler {

        private final List<Optional<JavaPlatform>> cachedPlatforms = new ArrayList<>();
        private final List<JavaPlatform> lru = new ArrayList<>();

        H() {
        }

        void reset() {
            cachedPlatforms.clear();
            lru.clear();
        }

        @NonNull
        List<Optional<JavaPlatform>> getCachedPlatforms() {
            return new ArrayList<>(cachedPlatforms);
        }

        @NonNull
        List<JavaPlatform> getLRU() {
            return new ArrayList<>(lru);
        }

        @Override
        public void publish(LogRecord record) {
            final String msg = record.getMessage();
            if (msg != null) {
                switch (msg) {
                    case "platformCache updated: {0}":
                        cachedPlatforms.add((Optional<JavaPlatform>)record.getParameters()[0]);
                        break;
                    case "lru updated: {0}":
                        lru.add((JavaPlatform)record.getParameters()[0]);
                        break;
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
