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
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearchTest.ClassPathProviderImpl;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.BatchResult;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearch.Folder;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

import static org.netbeans.modules.java.hints.spiimpl.batch.TestUtils.writeFilesAndWaitForScan;
import static org.netbeans.modules.java.hints.spiimpl.batch.TestUtils.prepareHints;

import org.netbeans.modules.parsing.impl.indexing.MimeTypes;

/**
 *
 * @author lahvac
 */
public class BatchUtilitiesTest extends NbTestCase {

    public BatchUtilitiesTest(String name) {
        super(name);
    }

    //XXX: copied from CustomIndexerImplTest:
    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/source/resources/layer.xml", "org/netbeans/lib/java/lexer/layer.xml"}, new Object[0]);
        Main.initializeURLFactory();
        org.netbeans.api.project.ui.OpenProjects.getDefault().getOpenProjects();
        prepareTest();
        MimeTypes.setAllMimeTypes(Collections.singleton("text/x-java"));
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] {ClassPathSupport.createClassPath(src1, src2)});
        RepositoryUpdater.getDefault().start(true);
        super.setUp();
    }

    public void testBatchSearchNotIndexed() throws Exception {
        writeFilesAndWaitForScan(src1,
                                 new File("test/Test1.java", "package test; public class Test1 { private void test() { java.io.File f = null; f.isDirectory(); } }"),
                                 new File("test/Test2.java", "package test; public class Test2 { private void test() { new javax.swing.ImageIcon(null); } }"));
        writeFilesAndWaitForScan(src3,
                                 new File("test/Test1.java", "package test; public class Test1 { private void test() { java.io.File f = null; f.isDirectory(); } }"),
                                 new File("test/Test2.java", "package test; public class Test2 { public boolean isDirectory() {return false} }"));

        Iterable<? extends HintDescription> hints = prepareHints("$1.isDirectory() => !$1.isFile()", "$1", "java.io.File");
        BatchResult result = BatchSearch.findOccurrences(hints, Scopes.specifiedFoldersScope(Folder.convert(src1, src3, empty)));
        List<MessageImpl> problems = new LinkedList<MessageImpl>();
        Collection<? extends ModificationResult> changes = BatchUtilities.applyFixes(result, new ProgressHandleWrapper(100), new AtomicBoolean(), problems);

        assertTrue(problems.toString(), problems.isEmpty());

        Map<FileObject, String> file2New = new HashMap<FileObject, String>();

        for (ModificationResult mr : changes) {
            for (FileObject file : mr.getModifiedFileObjects()) {
                assertNull(file2New.put(file, mr.getResultingSource(file)));
            }
        }

        FileObject src1Test1 = src1.getFileObject("test/Test1.java");
        String src1Test1Real = file2New.remove(src1Test1);
        String src1Test1Golden = "package test; public class Test1 { private void test() { java.io.File f = null; !f.isFile(); } }";

        assertEquals(src1Test1Golden, src1Test1Real);

        FileObject src3Test1 = src3.getFileObject("test/Test1.java");
        String src3Test1Real = file2New.remove(src3Test1);
        String src3Test1Golden = "package test; public class Test1 { private void test() { java.io.File f = null; !f.isFile(); } }";

        assertEquals(src3Test1Golden, src3Test1Real);

        assertTrue(file2New.toString(), file2New.isEmpty());
    }

    public void testMultipleWithErrors() throws Exception {
        writeFilesAndWaitForScan(src1,
                                 new File("test/Test1.java", "package test; public class Test1 { public class A1 extends Test2.A2 { public int t(Unknown u) { return this.doesNotExist(u); } } public class B1 {} private void x(java.io.File f) { boolean b = f.isDirectory(); } }"),
                                 new File("test/Test2.java", "package test; public class Test2 { public class A2 {} public class B2 extends Test1.B1 { public int t(Unknown u) { return this.doesNotExist(u); } } private void x(java.io.File f) { boolean b = f.isDirectory(); } }"));

        Iterable<? extends HintDescription> hints = prepareHints("$1.isDirectory() => !$1.isDirectory()", "$1", "java.io.File");
        BatchResult result = BatchSearch.findOccurrences(hints, Scopes.specifiedFoldersScope(Folder.convert(src1, src3, empty)));
        List<MessageImpl> problems = new LinkedList<MessageImpl>();

        BatchUtilities.applyFixes(result, new ProgressHandleWrapper(100), new AtomicBoolean(), new ArrayList<>(), new HashMap<>(), problems);

        assertTrue(problems.toString(), problems.isEmpty());
    }

//    public void testRemoveUnusedImports() throws Exception {
//        writeFilesAndWaitForScan(src1,
//                                 new File("test/Test1.java", "package test;\n import java.util.List;\n public class Test1 { }"));
//        writeFilesAndWaitForScan(src2,
//                                 new File("test/Test2.java", "package test;\n import java.util.LinkedList;\n public class Test2 { }"));
//
//        FileObject test1 = src1.getFileObject("test/Test1.java");
//        FileObject test2 = src2.getFileObject("test/Test2.java");
//
//        System.err.println(DataObject.find(test1).getClass());
//        BatchUtilities.removeUnusedImports(Arrays.asList(test1, test2));
//
//        LifecycleManager.getDefault().saveAll();
//
//        assertEquals("package test;\n public class Test1 { }", TestUtilities.copyFileToString(FileUtil.toFile(test1)));
//        assertEquals("package test;\n public class Test2 { }", TestUtilities.copyFileToString(FileUtil.toFile(test2)));
//    }

    private FileObject src1;
    private FileObject src2;
    private FileObject src3;
    private FileObject empty;

    private void prepareTest() throws Exception {
        FileObject workdir = SourceUtilsTestUtil.makeScratchDir(this);

        src1 = FileUtil.createFolder(workdir, "src1");
        src2 = FileUtil.createFolder(workdir, "src2");
        src3 = FileUtil.createFolder(workdir, "src3");
        empty = FileUtil.createFolder(workdir, "empty");

        ClassPathProviderImpl.setSourceRoots(Arrays.asList(src1, src2, src3));

        FileObject cache = FileUtil.createFolder(workdir, "cache");

        CacheFolder.setCacheFolder(cache);
    }

    @ServiceProvider(service=MimeDataProvider.class)
    public static final class JavaLexerProvider implements MimeDataProvider {

        private Lookup javaLookup = Lookups.fixed(JavaTokenId.language());

        @Override
        public Lookup getLookup(MimePath mimePath) {
            if (mimePath.getPath().endsWith(JavacParser.MIME_TYPE)) {
                return javaLookup;
            }

            return Lookup.EMPTY;
        }

    }

    @ServiceProvider(service=LanguageProvider.class)
    public static final class JavaLanguageProvider extends LanguageProvider {

        @Override
        public Language<?> findLanguage(String mimeType) {
            if ("text/x-java".equals(mimeType)) {
                return JavaTokenId.language();
            }

            return null;
        }

        @Override
        public LanguageEmbedding<?> findLanguageEmbedding(Token<?> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null;
        }

    }
}
