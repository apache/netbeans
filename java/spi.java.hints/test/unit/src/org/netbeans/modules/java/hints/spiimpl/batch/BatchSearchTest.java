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
package org.netbeans.modules.java.hints.spiimpl.batch;

import org.netbeans.modules.java.hints.spiimpl.batch.TestUtils.File;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import junit.framework.TestSuite;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.GlobalPathRegistryEvent;
import org.netbeans.api.java.classpath.GlobalPathRegistryListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.lib.nbjavac.services.NBAttr;
import org.netbeans.modules.java.file.launcher.queries.MultiSourceRootProvider;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.BatchResult;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Folder;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Resource;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.VerifiedSpansCallBack;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.MimeTypes;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.lucene.support.LowMemoryWatcherAccessor;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.util.lookup.ServiceProvider;

import static org.netbeans.modules.java.hints.spiimpl.batch.TestUtils.writeFilesAndWaitForScan;
import static org.netbeans.modules.java.hints.spiimpl.batch.TestUtils.prepareHints;

/**
 *
 * @author lahvac
 */
public class BatchSearchTest extends NbTestCase {

    public BatchSearchTest(String name) {
        super(name);
    }

    public static TestSuite suite() {
        TestSuite result = new NbTestSuite();

        result.addTestSuite(BatchSearchTest.class);
//        result.addTest(new BatchSearchTest("testBatchSearchFolderRemoteIndex"));

        return result;
    }

    //XXX: copied from CustomIndexerImplTest:
    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        Main.initializeURLFactory();
        MultiSourceRootProvider.DISABLE_MULTI_SOURCE_ROOT = true;
        org.netbeans.api.project.ui.OpenProjects.getDefault().getOpenProjects();
        prepareTest();
        MimeTypes.setAllMimeTypes(Collections.singleton("text/x-java"));
        sourceCP = ClassPathSupport.createClassPath(src1, src2);
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {sourceCP});
        RepositoryUpdater.getDefault().start(true);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, new ClassPath[] {sourceCP});
    }

    public void testBatchSearch1() throws Exception {
        writeFilesAndWaitForScan(src1,
                                 new File("test/Test1.java", "package test; public class Test1 { private void test() { java.io.File f = null; f.isDirectory(); } }"),
                                 new File("test/Test2.java", "package test; public class Test2 { private void test() { new javax.swing.ImageIcon(null); } }"));
        writeFilesAndWaitForScan(src2,
                                 new File("test/Test1.java", "package test; public class Test1 { private void test() { java.io.File f = null; f.isDirectory(); } }"),
                                 new File("test/Test2.java", "package test; public class Test2 { private void test() { new javax.swing.ImageIcon(null); } }"));

        Iterable<? extends HintDescription> hints = prepareHints("$1.isDirectory()");
        BatchResult result = BatchSearch.findOccurrences(hints, Scopes.allOpenedProjectsScope());
        Map<String, Iterable<String>> output = new HashMap<String, Iterable<String>>();

        for (Entry<FileObject, Collection<? extends Resource>> e : result.getResourcesWithRoots().entrySet()) {
            Collection<String> resourcesRepr = new LinkedList<String>();

            for (Resource r : e.getValue()) {
                resourcesRepr.add(r.getRelativePath());
            }

            output.put(e.getKey().toURL().toExternalForm(), resourcesRepr);
        }

        Map<String, Iterable<String>> golden = new HashMap<String, Iterable<String>>();

        golden.put(src1.toURL().toExternalForm(), Arrays.asList("test/Test1.java"));
        golden.put(src2.toURL().toExternalForm(), Arrays.asList("test/Test1.java"));

        assertEquals(golden, output);
    }

    public void testBatchSearchSpan() throws Exception {
        String code = "package test;\n" +
                      "public class Test {\n" +
                      "    private void m() {\n" +
                      "        a(c.i().getFileObject());\n" +
                      "        if (span != null && span[0] != (-1) && span[1] != (-1));\n" +
                      "        c.i().getFileObject(\"\");\n" +
                      "    }\n" +
                      "}\n";

        writeFilesAndWaitForScan(src1, new File("test/Test.java", code));

        Iterable<? extends HintDescription> hints = prepareHints("$0.getFileObject($1)");
        BatchResult result = BatchSearch.findOccurrences(hints, Scopes.allOpenedProjectsScope());

        assertEquals(1, result.getResources().size());
        Iterator<? extends Resource> resources = result.getResources().iterator().next().iterator();
        Resource r = resources.next();

        assertFalse(resources.hasNext());

        Set<String> snipets = new HashSet<String>();

        for (int[] span : r.getCandidateSpans()) {
            snipets.add(code.substring(span[0], span[1]));
        }

        Set<String> golden = new HashSet<String>(Arrays.asList("c.i().getFileObject(\"\")"));
        assertEquals(golden, snipets);
    }

    @RandomlyFails
    public void testBatchSearchNotIndexed() throws Exception {
        writeFilesAndWaitForScan(src1,
                                 new File("test/Test1.java", "package test; public class Test1 { private void test() { java.io.File f = null; f.isDirectory(); } }"),
                                 new File("test/Test2.java", "package test; public class Test2 { private void test() { new javax.swing.ImageIcon(null); } }"));
        writeFilesAndWaitForScan(src3,
                                 new File("test/Test1.java", "package test; public class Test1 { private void test() { Test2 f = null; f.isDirectory(); } }"),
                                 new File("test/Test2.java", "package test; public class Test2 { public boolean isDirectory() {return false} }"));

        Iterable<? extends HintDescription> hints = prepareHints("$1.isDirectory()", "$1", "test.Test2");
        BatchResult result = BatchSearch.findOccurrences(hints, Scopes.specifiedFoldersScope(Folder.convert(src1, src3, empty)));
        Map<String, Iterable<String>> output = new HashMap<String, Iterable<String>>();

        for (Entry<FileObject, Collection<? extends Resource>> e : result.getResourcesWithRoots().entrySet()) {
            Collection<String> resourcesRepr = new LinkedList<String>();

            for (Resource r : e.getValue()) {
                resourcesRepr.add(r.getRelativePath());
            }

            output.put(e.getKey().toURL().toExternalForm(), resourcesRepr);
        }

        Map<String, Iterable<String>> golden = new HashMap<String, Iterable<String>>();

        golden.put(src1.toURL().toExternalForm(), Arrays.asList("test/Test1.java"));
        golden.put(src3.toURL().toExternalForm(), Arrays.asList("test/Test1.java"));

        assertEquals(golden, output);

        //check verification:
        Map<String, Map<String, Iterable<String>>> verifiedOutput = verifiedSpans(result, false);
        Map<String, Map<String, Iterable<String>>> verifiedGolden = new HashMap<String, Map<String, Iterable<String>>>();

        verifiedGolden.put(src1.toURL().toExternalForm(), Collections.<String, Iterable<String>>singletonMap("test/Test1.java", Arrays.<String>asList()));
        verifiedGolden.put(src3.toURL().toExternalForm(), Collections.<String, Iterable<String>>singletonMap("test/Test1.java", Arrays.asList("0:75-0:86:verifier:")));

        assertEquals(verifiedGolden, verifiedOutput);
    }

    public void testBatchSearchForceIndexingOfProperDirectory() throws Exception {
        FileObject data = FileUtil.createFolder(workdir, "data");
        FileObject dataSrc1 = FileUtil.createFolder(data, "src1");
        FileObject dataSrc2 = FileUtil.createFolder(data, "src2");
        writeFilesAndWaitForScan(dataSrc1,
                                 new File("test/Test1.java", "package test; public class Test1 { private void test() { java.io.File f = null; f.isDirectory(); } }"),
                                 new File("test/Test2.java", "package test; public class Test2 { private void test() { new javax.swing.ImageIcon(null); } }"));
        writeFilesAndWaitForScan(dataSrc2,
                                 new File("test/Test1.java", "package test; public class Test1 { private void test() { Test2 f = null; f.isDirectory(); } }"),
                                 new File("test/Test2.java", "package test; public class Test2 { public boolean isDirectory() {return false} }"));

        ClassPathProviderImpl.setSourceRoots(Arrays.asList(dataSrc1, dataSrc2));

        Iterable<? extends HintDescription> hints = prepareHints("$1.isDirectory()", "$1", "test.Test2");
        BatchResult result = BatchSearch.findOccurrences(hints, Scopes.specifiedFoldersScope(Folder.convert(data)));
        Map<String, Iterable<String>> output = new HashMap<String, Iterable<String>>();

        for (Entry<FileObject, Collection<? extends Resource>> e : result.getResourcesWithRoots().entrySet()) {
            Collection<String> resourcesRepr = new HashSet<String>();

            for (Resource r : e.getValue()) {
                resourcesRepr.add(r.getRelativePath());
            }

            output.put(e.getKey().toURL().toExternalForm(), resourcesRepr);
        }

        Map<String, Iterable<String>> golden = new HashMap<String, Iterable<String>>();

        golden.put(data.toURL().toExternalForm(), new HashSet<String>(Arrays.asList("src1/test/Test1.java", "src2/test/Test1.java")));

        assertEquals(golden, output);

        //check verification:
        final Set<FileObject> added = new HashSet<FileObject>();
        final Set<FileObject> removed = new HashSet<FileObject>();

        GlobalPathRegistry.getDefault().addGlobalPathRegistryListener(new GlobalPathRegistryListener() {
            @Override
            public void pathsAdded(GlobalPathRegistryEvent event) {
                for (ClassPath cp : event.getChangedPaths()) {
                    added.addAll(Arrays.asList(cp.getRoots()));
                }
            }
            @Override
            public void pathsRemoved(GlobalPathRegistryEvent event) {
                for (ClassPath cp : event.getChangedPaths()) {
                    removed.addAll(Arrays.asList(cp.getRoots()));
                }
            }
        });

//        verifiedGolden.put(data.getURL().toExternalForm(), Arrays.asList("0:75-0:86:verifier:TODO: No display name"));
        Map<String, Map<String, Iterable<String>>> verifiedOutput = verifiedSpans(result, false);
        Map<String, Map<String, Iterable<String>>> verifiedGolden = new HashMap<String, Map<String, Iterable<String>>>();

        Map<String, Iterable<String>> verifiedGoldenPart = new HashMap<String, Iterable<String>>();

        verifiedGoldenPart.put("src1/test/Test1.java", Arrays.<String>asList());
        verifiedGoldenPart.put("src2/test/Test1.java", Arrays.<String>asList("0:75-0:86:verifier:"));

        verifiedGolden.put(data.toURL().toExternalForm(), verifiedGoldenPart);

        assertEquals(verifiedGolden, verifiedOutput);
        assertEquals(new HashSet<FileObject>(Arrays.asList(dataSrc1, dataSrc2)), added);
        assertEquals(new HashSet<FileObject>(Arrays.asList(dataSrc1, dataSrc2)), removed);
    }

    public void testBatchSearchFolderNoIndex() throws Exception {
        FileObject data = FileUtil.createFolder(workdir, "data");
        FileObject dataSrc1 = FileUtil.createFolder(data, "src1");
        FileObject dataSrc2 = FileUtil.createFolder(data, "src2");
        writeFilesAndWaitForScan(dataSrc1,
                                 new File("test/Test1.java", "package test; public class Test1 { private void test() { java.io.File f = null; f.isDirectory(); } }"),
                                 new File("test/Test2.java", "package test; public class Test2 { private void test() { new javax.swing.ImageIcon(null); } }"));
        writeFilesAndWaitForScan(dataSrc2,
                                 new File("test/Test1.java", "package test; public class Test1 { private void test() { Test2 f = null; f.isDirectory(); } }"),
                                 new File("test/Test2.java", "package test; public class Test2 { public boolean isDirectory() {return false} }"));

        Iterable<? extends HintDescription> hints = prepareHints("$1.isDirectory()");
        BatchResult result = BatchSearch.findOccurrences(hints, Scopes.specifiedFoldersScope(Folder.convert(Collections.singleton(data)))); //XXX: should be a no-index variant!
        Map<String, Iterable<String>> output = toDebugOutput(result);
        Map<String, Iterable<String>> golden = new HashMap<String, Iterable<String>>();

        golden.put(data.toURL().toExternalForm(), new HashSet<String>(Arrays.asList("src1/test/Test1.java", "src2/test/Test1.java")));

        assertEquals(golden, output);
    }

    public void testBatchSearchOutOfMemory() throws Exception {
        FileObject data = FileUtil.createFolder(workdir, "data");
        FileObject dataSrc1 = FileUtil.createFolder(data, "src1");
        writeFilesAndWaitForScan(dataSrc1,
                                 new File("test/Test1.java", "package test; public class Test1 { private void test() { java.io.File f = null; f.isDirectory(); } }"),
                                 new File("test/Test2.java", "package test; public class Test2 { private void test() { java.io.File f = null; f.isDirectory(); } }"),
                                 new File("test/Test3.java", "package test; public class Test3 { private void test() { java.io.File f = null; f.isDirectory(); } }"));

        ClassPathProviderImpl.setSourceRoots(Arrays.asList(dataSrc1));

        Iterable<? extends HintDescription> hints = prepareHints("$1.isDirectory()", "$1", "java.io.File");
        BatchResult result = BatchSearch.findOccurrences(hints, Scopes.specifiedFoldersScope(Folder.convert(data)));
        Map<String, Iterable<String>> output = new HashMap<String, Iterable<String>>();

        for (Entry<FileObject, Collection<? extends Resource>> e : result.getResourcesWithRoots().entrySet()) {
            Collection<String> resourcesRepr = new HashSet<String>();

            for (Resource r : e.getValue()) {
                resourcesRepr.add(r.getRelativePath());
            }

            output.put(e.getKey().toURL().toExternalForm(), resourcesRepr);
        }

        Map<String, Iterable<String>> golden = new HashMap<String, Iterable<String>>();

        golden.put(data.toURL().toExternalForm(), new HashSet<String>(Arrays.asList("src1/test/Test1.java", "src1/test/Test2.java", "src1/test/Test3.java")));

        assertEquals(golden, output);

        //check verification:
        Map<String, Map<String, Iterable<String>>> verifiedOutput = verifiedSpans(result, false, c -> new VerifiedSpansCallBack() {
            int callbacks;
            @Override
            public void groupStarted() {
                c.groupStarted();
            }
            @Override
            public boolean spansVerified(CompilationController wc, Resource r, Collection<? extends ErrorDescription> hints) throws Exception {
                if (++callbacks == 1) {
                    LowMemoryWatcherAccessor.setLowMemory(true);
                }
                return c.spansVerified(wc, r, hints);
            }

            @Override
            public void groupFinished() {
                c.groupFinished();
            }

            @Override
            public void cannotVerifySpan(Resource r) {
                c.cannotVerifySpan(r);
            }
        }, errors -> assertEquals(0, errors.size()));
        Map<String, Map<String, Iterable<String>>> verifiedGolden = new HashMap<String, Map<String, Iterable<String>>>();

        Map<String, Iterable<String>> verifiedGoldenPart = new HashMap<String, Iterable<String>>();

        verifiedGoldenPart.put("src1/test/Test1.java", Arrays.<String>asList("0:82-0:93:verifier:"));
        verifiedGoldenPart.put("src1/test/Test2.java", Arrays.<String>asList("0:82-0:93:verifier:"));
        verifiedGoldenPart.put("src1/test/Test3.java", Arrays.<String>asList("0:82-0:93:verifier:"));

        verifiedGolden.put(data.toURL().toExternalForm(), verifiedGoldenPart);

        assertEquals(verifiedGolden, verifiedOutput);
    }

    public void testBatchSearchParserCrashProgressEnsured() throws Exception {
        writeFilesAndWaitForScan(src1,
                                 new File("test/Test1.java", "package test; public class Test1 { private void test() { java.io.File f = null; f.isDirectory(); } }"));

        Iterable<? extends HintDescription> hints = prepareHints("$1.isDirectory()", "$1", "java.io.File");
        BatchResult result = BatchSearch.findOccurrences(hints, Scopes.allOpenedProjectsScope());
        Map<String, Iterable<String>> output = new HashMap<String, Iterable<String>>();

        for (Entry<FileObject, Collection<? extends Resource>> e : result.getResourcesWithRoots().entrySet()) {
            Collection<String> resourcesRepr = new HashSet<String>();

            for (Resource r : e.getValue()) {
                resourcesRepr.add(r.getRelativePath());
            }

            output.put(e.getKey().toURL().toExternalForm(), resourcesRepr);
        }

        Map<String, Iterable<String>> golden = new HashMap<String, Iterable<String>>();

        golden.put(src1.toURL().toExternalForm(), new HashSet<String>(Arrays.asList("test/Test1.java")));

        assertEquals(golden, output);

        //check verification:
        Entry<ClasspathInfo, Collection<FileObject>> cpAndFile = BatchUtilities.sortFiles(Arrays.asList(src1.getFileObject("test/Test1.java"))).entrySet().iterator().next();
        JavaSource js = JavaSource.create(cpAndFile.getKey(), cpAndFile.getValue());

        try {
            js.runUserActionTask(cc -> {
                NBAttr.TEST_DO_SINGLE_FAIL = true; //the upcoming attribution will fail
                cc.toPhase(Phase.RESOLVED);
            }, true);
            fail("Expected exception not thrown!");
        } catch (IllegalStateException ex) {
            //OK
        }

        Map<String, Map<String, Iterable<String>>> verifiedOutput = verifiedSpans(result, false, c -> new VerifiedSpansCallBack() {
            @Override
            public void groupStarted() {
                c.groupStarted();
                NBAttr.TEST_DO_SINGLE_FAIL = true; //the upcoming attribution will fail
            }
            @Override
            public boolean spansVerified(CompilationController wc, Resource r, Collection<? extends ErrorDescription> hints) throws Exception {
                return c.spansVerified(wc, r, hints);
            }

            @Override
            public void groupFinished() {
                c.groupFinished();
            }

            @Override
            public void cannotVerifySpan(Resource r) {
                c.cannotVerifySpan(r);
            }
        }, errors -> {
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).text.contains("IDE log"));
        });

        Map<String, Map<String, Iterable<String>>> verifiedGolden = new HashMap<String, Map<String, Iterable<String>>>();

        assertEquals(verifiedGolden, verifiedOutput);

        //prevent GC:
        js.runUserActionTask(cc -> {
            assertTrue(true);
        }, true);
    }

    private FileObject workdir;
    private FileObject src1;
    private FileObject src2;
    private FileObject src3;
    private FileObject empty;
    private ClassPath sourceCP;

    private void prepareTest() throws Exception {
        workdir = SourceUtilsTestUtil.makeScratchDir(this);

        src1 = FileUtil.createFolder(workdir, "src1");
        src2 = FileUtil.createFolder(workdir, "src2");
        src3 = FileUtil.createFolder(workdir, "src3");
        empty = FileUtil.createFolder(workdir, "empty");

        ClassPathProviderImpl.setSourceRoots(Arrays.asList(src1, src2, src3));

        FileObject cache = FileUtil.createFolder(workdir, "cache");

        CacheFolder.setCacheFolder(cache);
    }

    private Map<String, Iterable<String>> toDebugOutput(BatchResult result) throws Exception {
        Map<String, Iterable<String>> output = new HashMap<String, Iterable<String>>();

        for (Entry<FileObject, Collection<? extends Resource>> e : result.getResourcesWithRoots().entrySet()) {
            Collection<String> resourcesRepr = new HashSet<String>();

            for (Resource r : e.getValue()) {
                resourcesRepr.add(r.getRelativePath());
            }

            output.put(e.getKey().toURL().toExternalForm(), resourcesRepr);
        }

        return output;
    }

    private Map<String, Map<String, Iterable<String>>> verifiedSpans(BatchResult candidates, boolean doNotRegisterClassPath) throws Exception {
        return verifiedSpans(candidates, doNotRegisterClassPath, c -> c, errors -> assertEquals(0, errors.size()));
    }

    private Map<String, Map<String, Iterable<String>>> verifiedSpans(BatchResult candidates, boolean doNotRegisterClassPath, Function<VerifiedSpansCallBack, VerifiedSpansCallBack> wrapper, Consumer<List<MessageImpl>> validateErrors) throws Exception {
        final Map<String, Map<String, Iterable<String>>> result = new HashMap<String, Map<String, Iterable<String>>>();
        List<MessageImpl> errors = new LinkedList<MessageImpl>();
        BatchSearch.getVerifiedSpans(candidates, new ProgressHandleWrapper(1), wrapper.apply(new VerifiedSpansCallBack() {
            @Override
            public void groupStarted() {}
            @Override
            public boolean spansVerified(CompilationController wc, Resource r, Collection<? extends ErrorDescription> hints) throws Exception {
                Map<String, Iterable<String>> files = result.get(r.getRoot().toURL().toExternalForm());

                if (files == null) {
                    result.put(r.getRoot().toURL().toExternalForm(), files = new HashMap<String, Iterable<String>>());
                }

                Collection<String> currentHints = new LinkedList<String>();

                for (ErrorDescription ed : hints) {
                    currentHints.add(ed.toString());
                }

                files.put(r.getRelativePath(), currentHints);

                return true;
            }
            @Override
            public void groupFinished() {}
            @Override
            public void cannotVerifySpan(Resource r) {
                fail("Cannot verify: " +r.getRelativePath());
            }
        }), doNotRegisterClassPath, errors, new AtomicBoolean());

        validateErrors.accept(errors);

        return result;
    }

    @ServiceProvider(service=ClassPathProvider.class)
    public static final class ClassPathProviderImpl implements ClassPathProvider {

        private static Collection<FileObject> sourceRoots;

        public static synchronized void setSourceRoots(Collection<FileObject> sourceRoots) {
            ClassPathProviderImpl.sourceRoots = sourceRoots;
        }

        public static synchronized Collection<FileObject> getSourceRoots() {
            return sourceRoots;
        }

        @Override
        public synchronized ClassPath findClassPath(FileObject file, String type) {
            if (ClassPath.BOOT.equals(type)) {
                return BootClassPathUtil.getBootClassPath();
            }

            if (ClassPath.COMPILE.equals(type)) {
                return ClassPathSupport.createClassPath(new URL[0]);
            }

            if (ClassPath.SOURCE.equals(type) && sourceRoots != null) {
                for (FileObject sr : sourceRoots) {
                    if (file.equals(sr) || FileUtil.isParentOf(sr, file)) {
                        return ClassPathSupport.createClassPath(sr);
                    }
                }
            }

            return null;
        }

    }

}
