/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009-2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.spiimpl.batch;

import org.netbeans.modules.java.hints.spiimpl.batch.TestUtils.File;
import org.netbeans.modules.java.hints.spiimpl.MessageImpl;
import org.netbeans.modules.java.hints.spiimpl.batch.BatchSearchTest.ClassPathProviderImpl;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
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
import org.netbeans.api.java.source.TestUtilities;
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
import org.openide.LifecycleManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.loaders.DataObject;
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
