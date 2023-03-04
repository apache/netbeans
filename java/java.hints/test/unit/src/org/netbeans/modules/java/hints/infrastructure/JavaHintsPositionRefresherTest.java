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

package org.netbeans.modules.java.hints.infrastructure;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.hints.spiimpl.JavaHintsPositionRefresher;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
import org.netbeans.modules.java.source.parsing.JavacParserFactory;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.editor.hints.Context;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.PositionRefresher;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Test class for JavaHints implementation of Editor Hints {@link PositionRefresher}
 * @author Max Sauer
 */
public class JavaHintsPositionRefresherTest extends NbTestCase {

    private FileObject sourceRoot;
    private CompilationInfo info;
    private Document doc;
    private static File cache;
    private static FileObject cacheFO;

    public JavaHintsPositionRefresherTest(String name) {
        super(name);
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
        SourceUtilsTestUtil.prepareTest(new String[]{"org/netbeans/modules/java/editor/resources/layer.xml", "org/netbeans/modules/java/hints/resources/layer.xml", "META-INF/generated-layer.xml"},
                new Object[]{JavaDataLoader.class,
                new MimeDataProvider() {
                public Lookup getLookup(MimePath mimePath) {
                    return Lookups.fixed(new Object[] {
                        new JavaKit(), new JavacParserFactory(), new JavaCustomIndexer.Factory()
                    });
                }
            },
            new LanguageProvider() {
                public Language<?> findLanguage(String mimePath) {
                    return JavaTokenId.language();
                }

                public LanguageEmbedding<?> findLanguageEmbedding(Token<?> token,
                        LanguagePath languagePath,
                        InputAttributes inputAttributes) {
                    return null;
                }
            }
        });

        clearWorkDir();

        if (cache == null) {
            cache = FileUtil.normalizeFile(new File(getWorkDir(), "cache"));
            cacheFO = FileUtil.createFolder(cache);

            IndexUtil.setCacheFolder(cache);

            TestUtilities.analyzeBinaries(SourceUtilsTestUtil.getBootClassPath());
        }
    }

    private void prepareTest(String fileName, String code) throws Exception {
        FileObject workFO = FileUtil.toFileObject(getWorkDir());

        assertNotNull(workFO);

        sourceRoot = workFO.createFolder("src");

        FileObject buildRoot  = workFO.createFolder("build");

        FileObject data = FileUtil.createData(sourceRoot, fileName);
        File dataFile = FileUtil.toFile(data);

        assertNotNull(dataFile);

        TestUtilities.copyStringToFile(dataFile, code);

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cacheFO);

        DataObject od = DataObject.find(data);
        EditorCookie ec = od.getCookie(EditorCookie.class);

        assertNotNull(ec);

        doc = ec.openDocument();
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");

        //XXX: takes a long time
        //re-index, in order to find classes-living-elsewhere
        IndexingManager.getDefault().refreshIndexAndWait(sourceRoot.getURL(), null);

        JavaSource js = JavaSource.forFileObject(data);

        assertNotNull(js);

        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);

        assertNotNull(info);
    }

    private Context prepareContext(int position) {
        Context ctx = null;
        try {
            Constructor constructor = Context.class.getDeclaredConstructor(int.class, AtomicBoolean.class);
            constructor.setAccessible(true);
            ctx = (Context) constructor.newInstance(new Integer(position), new AtomicBoolean());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ctx;
    }

    public void testEmpty() throws Exception {
        performTest("test/Test.java", "package test; public class Test {\n public void foo() {while(true)|;}}", new String[] {"1:20-1:32:verifier:Empty statement after 'while'"});
    }

    public void testErrorHint0() throws Exception {
        performTest("test/Test.java", "package test; public class Test {public void foo() {\n| new Foo();}}", new String[] {"1:5-1:8:error:cannot find symbol\n  symbol  : class Foo\n  location: class Test"});
    }

    public void testWrongpackage() throws Exception {
        performTest("test/Test.java", "|\npublic class Test {\n }", new String[] {"0:0-1:0:error:Incorrect Package"});
    }

    public void testHintCount173282() throws Exception {
        performTest("test/Test.java", "class Test { static int statField; int field; public void method() { \n|String field = \"\"; \nSystem.out.println(field); Integer.parseInt(\"1\"); if(\"\"== \"\") { System.out.println(\"ok\"); } this.statField = 23; } }",
                new String[] {"2:53-2:60:verifier:Comparing Strings using == or !=", "1:7-1:12:verifier:Local variable hides a field", "2:97-2:106:verifier:AS0statField"});
    }

    public void testEmptyStatement() throws Exception {
        performTest("test/Test.java", "class Test { public void method() {\n |; \n} }",
                new String[] {"1:1-1:2:verifier:Empty statement"});
    }

    public void testPatternBasedHint() throws Exception {
        performTest("test/Test.java", "class Test { public void method(String g) {\n java.util.|logging.Logger.global.fine(g + g); \n} }",
                new String[] {"1:38-1:43:verifier:Inefficient use of string concatenation in logger"});
    }

    private void performTest(String fileName , String code, String[] expected) throws Exception {
        int[] caretPosition = new int[1];
        code = org.netbeans.modules.java.hints.spiimpl.TestUtilities.detectOffsets(code, caretPosition);
        prepareTest(fileName, code);
        final Context ctx = prepareContext(caretPosition[0]);
        final Map<String, List<ErrorDescription>> errorDescriptionsAt = new HashMap<String, List<ErrorDescription>>();

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Map<String, List<ErrorDescription>> edsAt = new JavaHintsPositionRefresher().getErrorDescriptionsAt(ctx, doc);
                errorDescriptionsAt.putAll(edsAt);
            }
        });

        Set<String> eds = new HashSet<String>();
        for (Entry<String, List<ErrorDescription>> e : errorDescriptionsAt.entrySet()) {
            for (ErrorDescription ed : e.getValue()) {
                eds.add(ed.toString().replace(":  ", "  :"));
            }
        }
        assertTrue("Provided error messages differ. " + eds, eds.containsAll(Arrays.asList(expected)));
    }

    static {
        NbBundle.setBranding("test");
    }
    
}
