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
package org.netbeans.modules.java.editor.base.embedding;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.EntityCatalog;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author jlahoda
 */
public class EmbeddingProviderImplTest extends NbTestCase {

    public EmbeddingProviderImplTest(String name) {
        super(name);
    }

    public void testFromAnnotationMethodParameter() throws Exception {
        runTest("""
                public class Test {
                    public void t() {
                        a(\"""
                          <html|>
                          \""");
                    }
                    public void a(@Language("test") String test) {}
                    @interface Language { public String value(); }
                }
                """,
                "<html>\n");
    }

    public void testFromAnnotationVariable() throws Exception {
        runTest("""
                public class Test {
                    public void t() {
                        @Language("test")
                        String a =
                          \"""
                          <html|>
                          \""");
                    }
                    @interface Language { public String value(); }
                }
                """,
                "<html>\n");
    }

    public void testFromAnnotationConstructorParameter() throws Exception {
        runTest("""
                public class Test {
                    public void t() {
                        new Test(\"""
                          <html|>
                          \""");
                    }
                    public Test(@Language("test") String test) {}
                    @interface Language { public String value(); }
                }
                """,
                "<html>\n");
    }

    public void testEscapes() throws Exception {
        runTest("""
                public class Test {
                    public void t() {
                        new Test(\"""
                          a\\"a|
                          \""");
                    }
                    public Test(@Language("test") String test) {}
                    @interface Language { public String value(); }
                }
                """,
                "a\"a\n");
    }

    private void runTest(@org.netbeans.api.annotations.common.Language("Java") String code,
                         String snippet) throws Exception {
        String fileName = "Test";
        int caretPos = code.indexOf('|');

        assertTrue(caretPos >= (-1));
        code = code.substring(0, caretPos) + code.substring(caretPos + 1);

        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml",
                                                      "META-INF/generated-layer.xml"},
                                        new Object[] {new MIMEResolverImpl()});

	FileObject scratch = SourceUtilsTestUtil.makeScratchDir(this);
	FileObject cache   = scratch.createFolder("cache");

        File wd         = getWorkDir();
        File testSource = new File(wd, "test/" + fileName + ".java");

        testSource.getParentFile().mkdirs();
        TestUtilities.copyStringToFile(testSource, code);

        FileObject testSourceFO = FileUtil.toFileObject(testSource);

        assertNotNull(testSourceFO);

        SourceUtilsTestUtil.setSourceLevel(testSourceFO, "21");

        File testBuildTo = new File(wd, "test-build");

        testBuildTo.mkdirs();

        FileObject srcRoot = FileUtil.toFileObject(testSource.getParentFile());
        SourceUtilsTestUtil.prepareTest(srcRoot,FileUtil.toFileObject(testBuildTo), cache);

        final Document doc = getDocument(testSourceFO);
        AtomicBoolean invoked = new AtomicBoolean();

        ParserManager.parse(Collections.singleton(Source.create(doc)), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                invoked.set(true);
                Result result = resultIterator.getParserResult(caretPos);
                assertTrue(result instanceof TestParser.TestParserResult);
                assertEquals(snippet, result.getSnapshot().getText().toString());
            }
        });

        assertTrue(invoked.get());
    }

    static class MIMEResolverImpl extends MIMEResolver {
        public String findMIMEType(FileObject fo) {
            if ("java".equals(fo.getExt())) {
                return "text/x-java";
            } else {
                return null;
            }
        }
    }

    protected final Document getDocument(FileObject file) throws IOException {
        DataObject od = DataObject.find(file);
        EditorCookie ec = (EditorCookie) od.getCookie(EditorCookie.class);

        if (ec != null) {
            Document doc = ec.openDocument();

            doc.putProperty(Language.class, JavaTokenId.language());
            doc.putProperty("mimeType", "text/x-java");

            return doc;
        } else {
            return null;
        }
    }

    public static final class TestParser extends Parser {

        private Snapshot snapshot;

        @Override
        public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
            this.snapshot = snapshot;
        }

        @Override
        public Result getResult(Task task) throws ParseException {
            return new TestParserResult(snapshot);
        }

        @Override
        public void addChangeListener(ChangeListener changeListener) {
        }

        @Override
        public void removeChangeListener(ChangeListener changeListener) {
        }

        @MimeRegistration(service=ParserFactory.class, mimeType="text/x-test") //TODO: isolate layers
        public static final class FactoryImpl extends ParserFactory {

            @Override
            public Parser createParser(Collection<Snapshot> snapshots) {
                return new TestParser();
            }

        }

        public static class TestParserResult extends Result {

            public TestParserResult(Snapshot _snapshot) {
                super(_snapshot);
            }

            @Override
            protected void invalidate() {
            }

        }
    }

    public enum TestTokenId implements TokenId {
        TOKEN;

        @Override
        public String primaryCategory() {
            return "text";
        }

        private static final Language<TestTokenId> language = new LanguageHierarchy<TestTokenId>() {

            @Override
            protected Collection<TestTokenId> createTokenIds() {
                return EnumSet.allOf(TestTokenId.class);
            }

            @Override
            protected Lexer<TestTokenId> createLexer(LexerRestartInfo<TestTokenId> info) {
                LexerInput input = info.input();
                TokenFactory<TestTokenId> factory = info.tokenFactory();
                return new Lexer<TestTokenId>() {
                    @Override
                    public Token<TestTokenId> nextToken() {
                        while (input.read() != LexerInput.EOF)
                            ;
                        if (input.readLength() == 0) return null;
                        return factory.createToken(TestTokenId.TOKEN);
                    }

                    @Override
                    public Object state() {
                        return null;
                    }

                    @Override
                    public void release() {
                    }
                };
            }

            @Override
            protected String mimeType() {
                return "text/x-test";
            }

        }.language();

        @MimeRegistration(mimeType="text/x-test", service=Language.class)
        public static Language<TestTokenId> language() {
            return language;
        }
    }

    @ServiceProvider(service=EntityCatalog.class)
    public static final class TestEntityCatalogImpl extends EntityCatalog {

        @Override
        public InputSource resolveEntity(String publicID, String systemID) throws SAXException, IOException {
            switch (publicID) {
                case "-//NetBeans//DTD Editor KeyBindings settings 1.1//EN":
                    return new InputSource(TestEntityCatalogImpl.class.getResourceAsStream("/org/netbeans/modules/editor/settings/storage/keybindings/EditorKeyBindings-1_1.dtd"));
                case "-//NetBeans//DTD Editor Preferences 1.0//EN":
                    return new InputSource(TestEntityCatalogImpl.class.getResourceAsStream("/org/netbeans/modules/editor/settings/storage/preferences/EditorPreferences-1_0.dtd"));
                case "-//NetBeans//DTD Editor Fonts and Colors settings 1.1//EN":
                    return new InputSource(TestEntityCatalogImpl.class.getResourceAsStream("/org/netbeans/modules/editor/settings/storage/fontscolors/EditorFontsColors-1_1.dtd"));
            }
            return null;
        }

    }
}
