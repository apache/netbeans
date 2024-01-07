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
package org.netbeans.api.java.source;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.Symbols;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.java.file.launcher.queries.MultiSourceRootProvider;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.ClassIndexManagerEvent;
import org.netbeans.modules.java.source.usages.ClassIndexManagerListener;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public class ClassIndexTest extends NbTestCase {

    private static FileObject srcRoot;
    private static FileObject srcRoot2;
    private static FileObject binRoot;
    private static FileObject binRoot2;
    private static FileObject libSrc2;
    private static ClassPath sourcePath;
    private static ClassPath compilePath;
    private static ClassPath bootPath;
    private static MutableCp spiCp;
    private static MutableCp spiSrc;

    public ClassIndexTest (String name) {
        super (name);
    }

    public static NbTestSuite suite() {
        final NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ClassIndexTest("testEvents"));    //NOI18N
        suite.addTest(new ClassIndexTest("testholdsWriteLock"));    //NOI18N
        suite.addTest(new ClassIndexTest("testGetElementsScopes"));    //NOI18N
        suite.addTest(new ClassIndexTest("testGetDeclaredTypesScopes"));    //NOI18N
        suite.addTest(new ClassIndexTest("testPackageUsages"));    //NOI18N
        suite.addTest(new ClassIndexTest("testNullRootPassedToClassIndexEvent"));    //NOI18N
        suite.addTest(new ClassIndexTest("testFindSymbols"));    //NOI18N
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        File cache = new File(getWorkDir(), "cache");       //NOI18N
        cache.mkdirs();
        IndexUtil.setCacheFolder(cache);
        File src = new File(getWorkDir(), "src");           //NOI18N
        src.mkdirs();
        srcRoot = FileUtil.toFileObject(src);
        srcRoot.createFolder("foo");                        //NOI18N
        src = new File(getWorkDir(), "src2");               //NOI18N
        src.mkdirs();
        srcRoot2 = FileUtil.toFileObject(src);
        src = new File(getWorkDir(), "lib");               //NOI18N
        src.mkdirs();
        binRoot = FileUtil.toFileObject(src);
        src = new File(getWorkDir(), "lib2");               //NOI18N
        src.mkdirs();
        binRoot2 = FileUtil.toFileObject(src);
        src = new File(getWorkDir(), "lib2Src");            //NOI18N
        src.mkdirs();
        libSrc2 = FileUtil.toFileObject(src);
        spiSrc = new MutableCp (Collections.singletonList(ClassPathSupport.createResource(srcRoot.getURL())));
        sourcePath = ClassPathFactory.createClassPath(spiSrc);
        spiCp = new MutableCp ();
        compilePath = ClassPathFactory.createClassPath(spiCp);
        bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        MockServices.setServices(ClassPathProviderImpl.class, SFBQ.class);
    }

    @Override
    protected void tearDown() throws Exception {
        MockServices.setServices();
    }

    @RandomlyFails
    public void testEvents () throws Exception {
        GlobalPathRegistry.getDefault().register(ClassPath.BOOT, new ClassPath[] {bootPath});
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] {compilePath});
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {sourcePath});
        IndexingManager.getDefault().refreshIndexAndWait(srcRoot.getURL(), null);
        final ClasspathInfo cpi = ClasspathInfo.create(srcRoot);
        final ClassIndex index = cpi.getClassIndex();
        index.getPackageNames("org", true, EnumSet.of(ClassIndex.SearchScope.SOURCE));
        final CIL testListener = new CIL ();
        index.addClassIndexListener(testListener);

        Set<EventType> et = EnumSet.of(EventType.TYPES_ADDED);
        testListener.setExpectedEvents (et);
        createFile ("foo/A.java", "package foo;\n public class A {}");
        assertTrue("TestListener returned false instead of true.", testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());        
        
        
        et = EnumSet.of(EventType.TYPES_CHANGED);
        testListener.setExpectedEvents (et);
        createFile ("foo/A.java", "package foo;\n public class A extends Object {}");
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());
        
        
        et = EnumSet.of(EventType.TYPES_REMOVED);
        testListener.setExpectedEvents (et);
        deleteFile("foo/A.java");
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());
        
        
        et = EnumSet.of (EventType.ROOTS_ADDED);
        testListener.setExpectedEvents(et);
        List<PathResourceImplementation> impls = new ArrayList<PathResourceImplementation>();
        impls.add (ClassPathSupport.createResource(srcRoot.getURL()));
        impls.add (ClassPathSupport.createResource(srcRoot2.getURL()));
        spiSrc.setImpls(impls);
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());

        Thread.sleep(500);

        et = EnumSet.of (EventType.ROOTS_REMOVED);
        testListener.setExpectedEvents(et);
        impls = new ArrayList<PathResourceImplementation>();
        impls.add (ClassPathSupport.createResource(srcRoot.getURL()));
        spiSrc.setImpls(impls);
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());
        
        et = EnumSet.of (EventType.ROOTS_ADDED);
        testListener.setExpectedEvents(et);
        impls = new ArrayList<PathResourceImplementation>();
        impls.add (ClassPathSupport.createResource(binRoot.getURL()));
        spiCp.setImpls(impls);
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());

        et = EnumSet.of (EventType.ROOTS_REMOVED);
        testListener.setExpectedEvents(et);
        spiCp.setImpls(Collections.<PathResourceImplementation>emptyList());
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());
        
        et = EnumSet.of (EventType.ROOTS_ADDED);
        testListener.setExpectedEvents(et);
        impls = new ArrayList<PathResourceImplementation>();
        impls.add (ClassPathSupport.createResource(binRoot2.getURL()));
        spiCp.setImpls(impls);
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());

        et = EnumSet.of (EventType.ROOTS_REMOVED);
        testListener.setExpectedEvents(et);
        spiCp.setImpls(Collections.<PathResourceImplementation>emptyList());
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());
        
        //Wait until RU processes remove event (otherwise it may be colapsed with re-add)
        RepositoryUpdater.getDefault().waitUntilFinished(-1);


        //Root Added should NOT be fired by registration of new source root
        //outside these ClassPaths, but should be fired by other ClassIndex
        et = EnumSet.noneOf (EventType.class);
        testListener.setExpectedEvents(et);
        ClassPath srcPath2 = ClassPathSupport.createClassPath(new FileObject[]{libSrc2});
        ClasspathInfo cpInfo2 = ClasspathInfo.create(ClassPathSupport.createClassPath(new URL[0]),
                ClassPathSupport.createClassPath(new URL[0]), srcPath2);
        ClassIndex ci2 = cpInfo2.getClassIndex();
        CIL testListener2 = new CIL ();
        ci2.addClassIndexListener(testListener2);
        EnumSet<EventType> et2 = EnumSet.of (EventType.ROOTS_ADDED);
        testListener2.setExpectedEvents(et2);

        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[]{srcPath2});
        assertTrue(testListener2.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et2, testListener2.getEventLog());
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());
        ci2.removeClassIndexListener(testListener2);
        ci2 = null;
        
        //Root Added should be fired by registration of new binary root pointing to already registered source root
        et = EnumSet.of (EventType.ROOTS_ADDED);
        testListener.setExpectedEvents(et);
        impls = new ArrayList<PathResourceImplementation>();
        impls.add (ClassPathSupport.createResource(binRoot2.getURL()));
        spiCp.setImpls(impls);
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());

        //Root Removed should be called on ClassIndex 1
        et = EnumSet.of (EventType.ROOTS_REMOVED);
        testListener.setExpectedEvents(et);
        spiCp.setImpls(Collections.<PathResourceImplementation>emptyList());
        assertTrue(testListener.awaitEvent(10, TimeUnit.SECONDS));
        assertExpectedEvents (et, testListener.getEventLog());
    }
    
    @SuppressWarnings("deprecation")
    public void testholdsWriteLock () throws Exception {
        //Test basics
        IndexManager.readAccess(new IndexManager.Action<Void>() {
            public Void run() throws IOException, InterruptedException {
                assertFalse(IndexManager.holdsWriteLock());
                return null;
            }
        });
        //Test nesting of [write|read] lock in write lock
        //the opposite is forbidden
        IndexManager.writeAccess(new IndexManager.Action<Void>() {
            public Void run() throws IOException, InterruptedException {                           
                assertTrue(IndexManager.holdsWriteLock());
                IndexManager.readAccess(new IndexManager.Action<Void>() {
                    public Void run() throws IOException, InterruptedException {                
                        assertTrue(IndexManager.holdsWriteLock());
                        return null;
                    }
                });
                assertTrue(IndexManager.holdsWriteLock());
                IndexManager.writeAccess(new IndexManager.Action<Void>() {
                    public Void run() throws IOException, InterruptedException {                
                        assertTrue(IndexManager.holdsWriteLock());
                        return null;
                    }
                });                
                assertTrue(IndexManager.holdsWriteLock());
                return null;
            }
        });
    }

    public void testGetElementsScopes() throws Exception {
        createJavaFile (srcRoot,"org.me.base","Base", "package org.me.base;\npublic class Base {}\n");
        createJavaFile (srcRoot,"org.me.pkg1","Class1", "package org.me.pkg1;\npublic class Class1 extends org.me.base.Base {}\n");
        createJavaFile (srcRoot,"org.me.pkg2","Class2", "package org.me.pkg2;\npublic class Class2 extends org.me.base.Base {}\n");
        IndexingManager.getDefault().refreshIndexAndWait(srcRoot.getURL(), null, true);
        final ClassPath scp = ClassPathSupport.createClassPath(srcRoot);
        final ClasspathInfo cpInfo = ClasspathInfo.create(
            ClassPathSupport.createClassPath(new URL[0]),
            ClassPathSupport.createClassPath(new URL[0]),
            scp);
        final ClassIndex ci = cpInfo.getClassIndex();
        Set<ElementHandle<TypeElement>> r = ci.getElements(
            ElementHandle.createTypeElementHandle(ElementKind.CLASS, "org.me.base.Base"),
            EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
            EnumSet.of(ClassIndex.SearchScope.SOURCE));
        assertElementHandles(new String[]{"org.me.pkg1.Class1","org.me.pkg2.Class2"}, r);

        r = ci.getElements(
            ElementHandle.createTypeElementHandle(ElementKind.CLASS, "org.me.base.Base"),
            EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
            Collections.singleton(
                ClassIndex.createPackageSearchScope(
                    ClassIndex.SearchScope.SOURCE,
                    "org.me.pkg1",
                    "org.me.pkg2")));
        assertElementHandles(new String[]{"org.me.pkg1.Class1","org.me.pkg2.Class2"}, r);

        Set<ClassIndex.SearchScopeType> scopes = new HashSet<ClassIndex.SearchScopeType>();
        scopes.add(ClassIndex.createPackageSearchScope(
            ClassIndex.SearchScope.SOURCE,
            "org.me.pkg1"));
        scopes.add(ClassIndex.createPackageSearchScope(
            ClassIndex.SearchScope.SOURCE,
            "org.me.pkg2"));
        r = ci.getElements(
            ElementHandle.createTypeElementHandle(ElementKind.CLASS, "org.me.base.Base"),
            EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
            scopes);
        assertElementHandles(new String[]{"org.me.pkg1.Class1","org.me.pkg2.Class2"}, r);

        r = ci.getElements(
            ElementHandle.createTypeElementHandle(ElementKind.CLASS, "org.me.base.Base"),
            EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
            Collections.singleton(
                new ClassIndex.SearchScopeType() {
                    @Override
                    public Set<? extends String> getPackages() {
                        return null;
                    }

                    @Override
                    public boolean isSources() {
                        return true;
                    }

                    @Override
                    public boolean isDependencies() {
                        return false;
                    }
                }));
        assertElementHandles(new String[]{"org.me.pkg1.Class1","org.me.pkg2.Class2"}, r);

        r = ci.getElements(
            ElementHandle.createTypeElementHandle(ElementKind.CLASS, "org.me.base.Base"),
            EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
            Collections.singleton(
                ClassIndex.createPackageSearchScope(
                    ClassIndex.SearchScope.SOURCE,
                    "org.me.pkg1")));
        assertElementHandles(new String[]{"org.me.pkg1.Class1"}, r);

        r = ci.getElements(
            ElementHandle.createTypeElementHandle(ElementKind.CLASS, "org.me.base.Base"),
            EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),
            Collections.singleton(
                ClassIndex.createPackageSearchScope(
                    ClassIndex.SearchScope.SOURCE)));
        assertElementHandles(new String[]{}, r);

        r = ci.getElements(
            ElementHandle.createTypeElementHandle(ElementKind.CLASS, "java.lang.Object"),
            EnumSet.allOf(ClassIndex.SearchKind.class),
            EnumSet.of(ClassIndex.SearchScope.SOURCE));
        assertElementHandles(new String[]{"org.me.base.Base", "org.me.pkg1.Class1","org.me.pkg2.Class2"}, r);

        r = ci.getElements(
            ElementHandle.createTypeElementHandle(ElementKind.CLASS, "java.lang.Object"),
            EnumSet.allOf(ClassIndex.SearchKind.class),
            Collections.singleton(ClassIndex.createPackageSearchScope(ClassIndex.SearchScope.SOURCE, "org.me.pkg1")));
        assertElementHandles(new String[]{"org.me.pkg1.Class1"}, r);
    }

    public void testGetDeclaredTypesScopes() throws Exception {
        createJavaFile (srcRoot,"org.me.pkg1","Class1", "package org.me.pkg1;\npublic class Class1 {}\n");
        createJavaFile (srcRoot,"org.me.pkg2","Class2", "package org.me.pkg2;\npublic class Class2 {}\n");
        IndexingManager.getDefault().refreshIndexAndWait(srcRoot.getURL(), null, true);
        final ClassPath scp = ClassPathSupport.createClassPath(srcRoot);
        final ClasspathInfo cpInfo = ClasspathInfo.create(
            ClassPathSupport.createClassPath(new URL[0]),
            ClassPathSupport.createClassPath(new URL[0]),
            scp);
        final ClassIndex ci = cpInfo.getClassIndex();
        Set<ElementHandle<TypeElement>> r = ci.getDeclaredTypes(
            "",
            ClassIndex.NameKind.PREFIX,
            EnumSet.of(ClassIndex.SearchScope.SOURCE));
        assertElementHandles(new String[]{"org.me.pkg1.Class1","org.me.pkg2.Class2"}, r);

        r = ci.getDeclaredTypes(
            "",
            ClassIndex.NameKind.PREFIX,
            Collections.singleton(
                ClassIndex.createPackageSearchScope(
                    ClassIndex.SearchScope.SOURCE,
                    "org.me.pkg1",
                    "org.me.pkg2")));
        assertElementHandles(new String[]{"org.me.pkg1.Class1","org.me.pkg2.Class2"}, r);

        Set<ClassIndex.SearchScopeType> scopes = new HashSet<ClassIndex.SearchScopeType>();
        scopes.add(ClassIndex.createPackageSearchScope(
            ClassIndex.SearchScope.SOURCE,
            "org.me.pkg1"));
        scopes.add(ClassIndex.createPackageSearchScope(
            ClassIndex.SearchScope.SOURCE,
            "org.me.pkg2"));
        r = ci.getDeclaredTypes(
            "",
            ClassIndex.NameKind.PREFIX,
            scopes);
        assertElementHandles(new String[]{"org.me.pkg1.Class1","org.me.pkg2.Class2"}, r);
        r = ci.getDeclaredTypes(
            "",
            ClassIndex.NameKind.PREFIX,
            Collections.singleton(
                new ClassIndex.SearchScopeType() {
                    @Override
                    public Set<? extends String> getPackages() {
                        return null;
                    }

                    @Override
                    public boolean isSources() {
                        return true;
                    }

                    @Override
                    public boolean isDependencies() {
                        return false;
                    }
                }));
        assertElementHandles(new String[]{"org.me.pkg1.Class1","org.me.pkg2.Class2"}, r);

        r = ci.getDeclaredTypes(
            "",
            ClassIndex.NameKind.PREFIX,
            Collections.singleton(
                ClassIndex.createPackageSearchScope(
                    ClassIndex.SearchScope.SOURCE,
                    "org.me.pkg1")));
        assertElementHandles(new String[]{"org.me.pkg1.Class1"}, r);

        r = ci.getDeclaredTypes(
            "",
            ClassIndex.NameKind.PREFIX,
            Collections.singleton(
                ClassIndex.createPackageSearchScope(
                    ClassIndex.SearchScope.SOURCE)));
        assertElementHandles(new String[]{}, r);
    }

    @RandomlyFails
    public void testPackageUsages() throws Exception {
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject root = FileUtil.createFolder(wd,"src");    //NOI18N
        sourcePath = ClassPathSupport.createClassPath(root);
        compilePath = ClassPathSupport.createClassPath(new URL[0]);
        bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        final FileObject t1 = createJavaFile(
                root,
                "org.me.test",                                          //NOI18N
                "T1",                                                   //NOI18N
                "package org.me.test;\n"+                               //NOI18N
                "public class T1 extends java.util.ArrayList {}");      //NOI18N
        final FileObject t2 = createJavaFile(
                root,
                "org.me.test",                                          //NOI18N
                "T2",                                                   //NOI18N
                "package org.me.test;\n"+                               //NOI18N
                "public class T2 {\n"+                                  //NOI18N
                "   private java.util.Map m;"+                          //NOI18N
                "}");                                                   //NOI18N
        final FileObject t3 = createJavaFile(
                root,
                "org.me.test",                                          //NOI18N
                "T3",                                                   //NOI18N
                "package org.me.test;\n"+                               //NOI18N
                "public class T3 {\n"+                                  //NOI18N
                "   private java.io.File f;"+                           //NOI18N
                "}");                                                   //NOI18N
        final FileObject t4 = createJavaFile(
                root,
                "org.me.test",                                          //NOI18N
                "T4",                                                   //NOI18N
                "package org.me.test;\n"+                               //NOI18N
                "import u.*;\n"+                                        //NOI18N
                "public class T4 {}");                                  //NOI18N
        final FileObject dummy = createJavaFile(
                root,
                "u",                                                    //NOI18N
                "D",                                                    //NOI18N
                "package u;\n"+                                         //NOI18N
                "public class D {\n"+                                   //NOI18N
                "}");
        IndexingManager.getDefault().refreshIndexAndWait(root.getURL(), null);
        final ClassIndex ci = ClasspathInfo.create(bootPath, compilePath, sourcePath).getClassIndex();
        assertNotNull(ci);
        Set<FileObject> result = ci.getResourcesForPackage(
            ElementHandle.createPackageElementHandle("java.util"),    //NOI18N
            EnumSet.<ClassIndex.SearchKind>of(ClassIndex.SearchKind.IMPLEMENTORS),
            EnumSet.<ClassIndex.SearchScope>of(ClassIndex.SearchScope.SOURCE));
        assertNotNull(result);
        assertFiles(Collections.singleton(t1),result);
        result = ci.getResourcesForPackage(
            ElementHandle.createPackageElementHandle("java.util"),    //NOI18N
            EnumSet.<ClassIndex.SearchKind>allOf(ClassIndex.SearchKind.class),
            EnumSet.<ClassIndex.SearchScope>of(ClassIndex.SearchScope.SOURCE));
        assertNotNull(result);
        assertFiles(Arrays.asList(t1,t2),result);
        result = ci.getResourcesForPackage(
            ElementHandle.createPackageElementHandle("java.io"),      //NOI18N
            EnumSet.<ClassIndex.SearchKind>allOf(ClassIndex.SearchKind.class),
            EnumSet.<ClassIndex.SearchScope>of(ClassIndex.SearchScope.SOURCE));
        assertNotNull(result);
        assertFiles(Collections.singleton(t3),result);
        result = ci.getResourcesForPackage(
            ElementHandle.createPackageElementHandle("java.util.concurrent"), //NOI18N
            EnumSet.<ClassIndex.SearchKind>allOf(ClassIndex.SearchKind.class),
            EnumSet.<ClassIndex.SearchScope>of(ClassIndex.SearchScope.SOURCE));
        assertNotNull(result);
        assertFiles(Collections.<FileObject>emptySet(),result);
        result = ci.getResourcesForPackage(
            ElementHandle.createPackageElementHandle("u"),    //NOI18N
            EnumSet.<ClassIndex.SearchKind>of(ClassIndex.SearchKind.TYPE_REFERENCES),
            EnumSet.<ClassIndex.SearchScope>of(ClassIndex.SearchScope.SOURCE));
        assertNotNull(result);
        assertFiles(Arrays.asList(dummy, t4),result);
    }
    
    public void testNullRootPassedToClassIndexEvent() throws Exception {
        
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {sourcePath});
        IndexingManager.getDefault().refreshIndexAndWait(srcRoot.toURL(), null);
        final ClassIndexManagerListenerImpl testListener = new ClassIndexManagerListenerImpl();
        ClassIndexManager.getDefault().addClassIndexManagerListener(testListener);
        testListener.expect(EventType.ROOTS_REMOVED);
        GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, new ClassPath[] {sourcePath});
        assertTrue(testListener.await(10000));
        assertTrue(testListener.getAdded().isEmpty());
    }

    public void testFindSymbols() throws Exception {
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject root = FileUtil.createFolder(wd,"src");    //NOI18N
        sourcePath = ClassPathSupport.createClassPath(root);
        compilePath = ClassPathSupport.createClassPath(new URL[0]);
        bootPath = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        final FileObject t1 = createJavaFile(
                root,
                "test",                                          //NOI18N
                "Test",                                          //NOI18N
                "package test;\n"+                               //NOI18N
                "public class Test { private void foo() { } }"); //NOI18N
        final FileObject t2 = createJavaFile(
                root,
                "test",                                          //NOI18N
                "foo",                                           //NOI18N
                "package test;\n"+                               //NOI18N
                "public class foo {\n"+                          //NOI18N
                "}");                                            //NOI18N
        IndexingManager.getDefault().refreshIndexAndWait(root.toURL(), null);
        final ClassIndex ci = ClasspathInfo.create(bootPath, compilePath, sourcePath).getClassIndex();
        assertNotNull(ci);
        Iterable<Symbols> result = ci.getDeclaredSymbols("foo", NameKind.SIMPLE_NAME,
            EnumSet.<ClassIndex.SearchScope>of(ClassIndex.SearchScope.SOURCE));
        Set<String> actualResult = new HashSet<String>();
        for (Symbols s : result) {
            actualResult.add(s.getEnclosingType().getQualifiedName() + ":" + s.getSymbols().toString());
        }
        assertEquals(new HashSet<String>(Arrays.asList("test.foo:[foo]", "test.Test:[foo]")), actualResult);
    }
    
    private FileObject createJavaFile (
            final FileObject root,
            final String pkg,
            final String name,
            final String content) throws IOException {
            final FileObject file = FileUtil.createData(
                root,
                String.format("%s/%s.java",
                    FileObjects.convertPackage2Folder(pkg),
                    name));
            final FileLock lck = file.lock();
            try {
                final PrintWriter out = new PrintWriter (new OutputStreamWriter(file.getOutputStream(lck)));
                try {
                    out.print(content);
                } finally {
                    out.close();
                }
            } finally {
                lck.releaseLock();
            }
            return file;
    }

    private void assertElementHandles(final String[] expected, final Set<ElementHandle<TypeElement>> result) {
        final Set<String> expSet = new HashSet<>(Arrays.asList(expected));
        for (ElementHandle<TypeElement> handle : result) {
            if (!expSet.remove(handle.getQualifiedName())) {
                throw new AssertionError("Expected: " + Arrays.toString(expected) +" Result: " + result);
            }
        }
        if (!expSet.isEmpty()) {
            throw new AssertionError("Expected: " + Arrays.toString(expected) +" Result: " + result);
        }
    }

    private void assertFiles(final Collection<FileObject> expected, final Collection<FileObject> result) {
        final Set<FileObject> expSet = new HashSet<FileObject>(expected);
        for (FileObject fo : result) {
            if (!expSet.remove(fo)) {
                throw new AssertionError("Expected: " + expected +" Result: " + result);
            }
        }
        if (!expSet.isEmpty()) {
            throw new AssertionError("Expected: " + expected +" Result: " + result);
        }
    }

    private static void assertExpectedEvents (final Set<EventType> et, final List<? extends EventRecord> eventLog) {
        assert et != null;
        assert eventLog != null;
        for (Iterator<? extends EventRecord> it = eventLog.iterator(); it.hasNext(); ) {
            EventRecord rec = it.next();
            if (et.remove(rec.type)) {
                it.remove();
            }
        }
        assertTrue (et.isEmpty());
        assertTrue (eventLog.isEmpty());
    }
    
    private static void createFile (final String path, final String content) throws IOException {
        assert srcRoot != null && srcRoot.isValid();
        srcRoot.getFileSystem().runAtomicAction(new FileSystem.AtomicAction () {
            public void run () throws IOException {
                final FileObject data = FileUtil.createData(srcRoot, path);                
                assert data != null;
                final FileLock lock = data.lock();
                try {
                    PrintWriter out = new PrintWriter (new OutputStreamWriter (data.getOutputStream(lock)));
                    try {
                        out.print (content);
                    } finally {
                        out.close ();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        });                
    }
    
    private static void deleteFile (final String path) throws IOException {
        assert srcRoot != null && srcRoot.isValid();
        final FileObject data  = srcRoot.getFileObject(path);        
        if (data != null) {
            //Workaround of issue #126367
            final FileObject parent = data.getParent();
            data.delete();
        }
    }
       

    static {
        MultiSourceRootProvider.DISABLE_MULTI_SOURCE_ROOT = true;
    }

    public static class ClassPathProviderImpl implements ClassPathProvider {

        public ClassPath findClassPath(final FileObject file, final String type) {
            final FileObject[] roots = sourcePath.getRoots();
            for (FileObject root : roots) {
                if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                    if (type == ClassPath.SOURCE) {
                        return sourcePath;
                    }
                    if (type == ClassPath.COMPILE) {
                        return compilePath;
                    }
                    if (type == ClassPath.BOOT) {
                        return bootPath;
                    }
                }
            }
            if (libSrc2.equals(file) || FileUtil.isParentOf(libSrc2, file)) {
                if (type == ClassPath.SOURCE) {
                        return ClassPathSupport.createClassPath(new FileObject[]{libSrc2});
                    }
                    if (type == ClassPath.COMPILE) {
                        return ClassPathSupport.createClassPath(new URL[0]);
                    }
                    if (type == ClassPath.BOOT) {
                        return bootPath;
                    }
            }
            return null;
        }        
    }
    
    public static class SFBQ implements SourceForBinaryQueryImplementation {

        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) { 
            try {
                if (binaryRoot.equals(binRoot2.getURL())) {
                    return new SourceForBinaryQuery.Result () {

                        public FileObject[] getRoots() {
                            return new FileObject[] {libSrc2};
                        }

                        public void addChangeListener(ChangeListener l) {
                        }

                        public void removeChangeListener(ChangeListener l) {
                        }

                    };
                }
            } catch (FileStateInvalidException e) {}
            return null;
        }
        
    }
    
    private static enum EventType {
        TYPES_ADDED,
        TYPES_REMOVED,
        TYPES_CHANGED,
        ROOTS_ADDED,
        ROOTS_REMOVED
    }
    
    private static final class EventRecord {
        
        private final EventType type;
        private final TypesEvent typesEvent;
        private final RootsEvent rootsEvent;
        
        public EventRecord (final EventType type, final TypesEvent event) {
            assert type == EventType.TYPES_ADDED || type == EventType.TYPES_REMOVED || type == EventType.TYPES_CHANGED;
            this.type = type;
            this.typesEvent = event;
            this.rootsEvent = null;
        }
        
        public EventRecord (final EventType type, final RootsEvent event) {
            assert type == EventType.ROOTS_ADDED || type == EventType.ROOTS_REMOVED;
            this.type = type;
            this.typesEvent = null;
            this.rootsEvent = event;
        }
        
        @Override
        public String toString() {
            return "[" + type +"]";
        }
                
    }
    
    private static final class CIL implements ClassIndexListener {
        
        private CountDownLatch latch; 
        private Set<EventType> expectedEvents;
        private final List<EventRecord> eventsLog = new LinkedList<EventRecord> ();

        public void typesAdded(final TypesEvent event) {
            eventsLog.add(new EventRecord (EventType.TYPES_ADDED, event));
            if (expectedEvents.remove(EventType.TYPES_ADDED)) {
                latch.countDown();
            }
        }

        public void typesRemoved(final TypesEvent event) {
            eventsLog.add (new EventRecord (EventType.TYPES_REMOVED, event));
            if (expectedEvents.remove(EventType.TYPES_REMOVED)) {
                latch.countDown();
            }
        }

        public void typesChanged(final TypesEvent event) {
            eventsLog.add (new EventRecord (EventType.TYPES_CHANGED, event));
            if (expectedEvents.remove(EventType.TYPES_CHANGED)) {
                latch.countDown();
            }
        }

        public void rootsAdded(final RootsEvent event) {
            eventsLog.add (new EventRecord (EventType.ROOTS_ADDED, event));
            if (expectedEvents.remove(EventType.ROOTS_ADDED)) {
                latch.countDown();
            }
        }

        public void rootsRemoved(final RootsEvent event) {
            eventsLog.add (new EventRecord (EventType.ROOTS_REMOVED, event));
            if (expectedEvents.remove(EventType.ROOTS_REMOVED)) {
                latch.countDown();
            }
        }
                
        public List<? extends EventRecord> getEventLog () {
            return new LinkedList<EventRecord> (this.eventsLog);
        }
        
        public void setExpectedEvents (final Set<EventType> et) {
            assert et != null;
            assert this.latch == null;
            this.expectedEvents = EnumSet.copyOf(et);            
            this.eventsLog.clear();
            latch = new CountDownLatch (this.expectedEvents.size());            
        }
        
        public boolean awaitEvent (int timeout, TimeUnit tu) throws InterruptedException {            
            assert this.latch != null;
            final boolean res = latch.await(timeout, tu);
            this.latch = null;
            return res;
        }
        
    }
    
    private static class ClassIndexManagerListenerImpl implements ClassIndexManagerListener {
        
        private Set<? extends EventType> expectedEvents; 
        private final List<ClassIndexManagerEvent> added = new ArrayList<ClassIndexManagerEvent>();
        private final List<ClassIndexManagerEvent> removed = new ArrayList<ClassIndexManagerEvent>();
        
        synchronized void expect(final EventType... events) {
            expectedEvents = new HashSet<EventType>(Arrays.<EventType>asList(events));
            added.clear();
            removed.clear();
        }
        
        synchronized boolean await(int millis) throws InterruptedException {
            final long st = System.currentTimeMillis();
            while (!expectedEvents.isEmpty()) {
                if (System.currentTimeMillis() - st >= millis) {
                    return false;
                }
                wait(millis);
            }
            return true;
        }
        
        public synchronized List<? extends ClassIndexManagerEvent> getAdded() {
            return Collections.<ClassIndexManagerEvent>unmodifiableList(added);
        }
        
        public synchronized List<? extends ClassIndexManagerEvent> getRemoved() {
            return Collections.<ClassIndexManagerEvent>unmodifiableList(removed);
        }

        @Override
        public synchronized void classIndexAdded(ClassIndexManagerEvent event) {
            added.add(event);
            if (expectedEvents.remove(EventType.ROOTS_ADDED)) {
                notifyAll();
            }
        }

        @Override
        public synchronized void classIndexRemoved(ClassIndexManagerEvent event) {
            removed.add(event);
            if (expectedEvents.remove(EventType.ROOTS_REMOVED)) {
                notifyAll();
            }
        }

    }
    
    
    private static final class MutableCp implements ClassPathImplementation {
        
        private final PropertyChangeSupport support;
        private List<? extends PathResourceImplementation> impls;
        
        
        public MutableCp () {
             this (Collections.<PathResourceImplementation>emptyList());
        }
        
        public MutableCp (final List<? extends PathResourceImplementation> impls) {
            assert impls != null;
            support = new PropertyChangeSupport (this);
            this.impls =impls;
        }

        public List<? extends PathResourceImplementation> getResources() {
            return impls;
        }
                
        public void addPropertyChangeListener(final PropertyChangeListener listener) {
            assert listener != null;
            this.support.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(final PropertyChangeListener listener) {
            assert listener != null;
            this.support.removePropertyChangeListener(listener);
        }
        
        
        void setImpls (final List<? extends PathResourceImplementation> impls) {
            assert impls != null;
            this.impls = impls;
            this.support.firePropertyChange(PROP_RESOURCES, null, null);
        }
        
    }
}
