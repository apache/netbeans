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
package org.netbeans.modules.editor.java;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lsp.Completion;
import org.netbeans.api.lsp.Completion.Context;
import org.netbeans.api.lsp.Completion.TriggerKind;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class JavaCompletionCollectorTest extends NbTestCase {

    public JavaCompletionCollectorTest(String name) {
        super(name);
    }

    public void testStaticMembersAndImports() throws Exception {
        AtomicBoolean found = new AtomicBoolean();
        runJavaCollector(List.of(new FileDescription("test/Test.java",
                                                     """
                                                     package test;
                                                     public class Test {
                                                         public void test() {
                                                             if (call(|)) {
                                                             }
                                                         }
                                                         private boolean call(test.other.E e) {
                                                             return false;
                                                         }
                                                     }
                                                     """),
                                 new FileDescription("test/other/E.java",
                                                     """
                                                     package test.other;
                                                     public enum E {
                                                         A, B, C;
                                                     }
                                                     """)),
                         completions -> {
                             for (Completion completion : completions) {
                                 if (completion.getLabel().equals("E.A")) {
                                     assertEquals("14-14:\\nimport test.other.E;\\n\\n",
                                                  completion.getAdditionalTextEdits()
                                                            .get()
                                                            .stream()
                                                            .map(JavaCompletionCollectorTest::textEdit2String)
                                                            .collect(Collectors.joining(", ")));
                                     assertEquals("E.A",
                                                  completion.getInsertText());
                                     found.set(true);
                                 }
                             }
                         });
        assertTrue(found.get());
    }

    public void testStaticMembersAndNoImports() throws Exception {
        AtomicBoolean found = new AtomicBoolean();
        runJavaCollector(List.of(new FileDescription("test/Test.java",
                                                     """
                                                     package test;
                                                     public class Test {
                                                         public void test() {
                                                             if (call(|)) {
                                                             }
                                                         }
                                                         private boolean call(Outter.E e) {
                                                             return false;
                                                         }
                                                     }
                                                     class Outter {
                                                         public enum E {
                                                             A, B, C;
                                                         }
                                                     }
                                                     """)),
                         completions -> {
                             for (Completion completion : completions) {
                                 if (completion.getLabel().equals("E.A")) {
                                     assertNull(completion.getAdditionalTextEdits());
                                     assertEquals("Outter.E.A",
                                                  completion.getInsertText());
                                     found.set(true);
                                 }
                             }
                         });
        assertTrue(found.get());
    }

    public void testTypeFromIndex1() throws Exception {
        AtomicBoolean found = new AtomicBoolean();
        runJavaCollector(List.of(new FileDescription("test/Test.java",
                                                     """
                                                     package test;
                                                     public class Test {
                                                         public void test() {
                                                             EEE|
                                                         }
                                                     }
                                                     """),
                                 new FileDescription("test/other/EEE.java",
                                                     """
                                                     package test.other;
                                                     public class EEE {
                                                     }
                                                     """)),
                         completions -> {
                             for (Completion completion : completions) {
                                 if (completion.getLabel().equals("EEE")) {
                                     assertEquals("14-14:\\nimport test.other.EEE;\\n\\n",
                                                  completion.getAdditionalTextEdits()
                                                            .get()
                                                            .stream()
                                                            .map(JavaCompletionCollectorTest::textEdit2String)
                                                            .collect(Collectors.joining(", ")));
                                     assertEquals("EEE",
                                                  completion.getInsertText());
                                     found.set(true);
                                 }
                             }
                         });
        assertTrue(found.get());
    }

    public void testTypeFromIndex2() throws Exception {
        AtomicBoolean found = new AtomicBoolean();
        runJavaCollector(List.of(new FileDescription("test/Test.java",
                                                     """
                                                     package test;
                                                     public class Test {
                                                         public void test() {
                                                             EEE|
                                                         }
                                                         interface EEE {}
                                                     }
                                                     """),
                                 new FileDescription("test/other/EEE.java",
                                                     """
                                                     package test.other;
                                                     public class EEE {
                                                     }
                                                     """)),
                         completions -> {
                             for (Completion completion : completions) {
                                 if (completion.getLabel().equals("EEE") &&
                                     completion.getKind() == Completion.Kind.Class) {
                                     assertEquals("67-67:test.other.",
                                                  completion.getAdditionalTextEdits()
                                                            .get()
                                                            .stream()
                                                            .map(JavaCompletionCollectorTest::textEdit2String)
                                                            .collect(Collectors.joining(", ")));
                                     assertEquals("EEE",
                                                  completion.getInsertText());
                                     found.set(true);
                                 }
                             }
                         });
        assertTrue(found.get());
    }

    private void runJavaCollector(List<FileDescription> files, Validator<List<Completion>> validator) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[]{"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[]{new MIMEResolverImpl(), new MIMEDataProvider()});

        FileObject scratch = SourceUtilsTestUtil.makeScratchDir(this);
        FileObject cache = scratch.createFolder("cache");
        FileObject src = scratch.createFolder("src");
        FileObject mainFile = null;
        int caretPosition = -1;

        for (FileDescription testFile : files) {
            FileObject testFO = FileUtil.createData(src, testFile.fileName);
            String code = testFile.code;

            if (mainFile == null) {
                mainFile = testFO;
                caretPosition = code.indexOf('|');

                assertTrue(caretPosition >= 0);

                code = code.substring(0, caretPosition) + code.substring(caretPosition + 1);
            }

            TestUtilities.copyStringToFile(testFO, code);
        }

        assertNotNull(mainFile);

        if (sourceLevel != null) {
            SourceUtilsTestUtil.setSourceLevel(mainFile, sourceLevel);
        }

        SourceUtilsTestUtil.prepareTest(src, FileUtil.createFolder(scratch, "test-build"), cache);
        SourceUtilsTestUtil.compileRecursively(src);

        EditorCookie ec = mainFile.getLookup().lookup(EditorCookie.class);
        Document doc = ec.openDocument();
        JavaCompletionCollector collector = new JavaCompletionCollector();
        Context ctx = new Context(TriggerKind.Invoked, null);
        List<Completion> completions = new ArrayList<>();

        JavaSource.forDocument(doc).runUserActionTask(cc -> {
            cc.toPhase(JavaSource.Phase.RESOLVED);
        }, true);
        collector.collectCompletions(doc, caretPosition, ctx, completions::add);
        validator.validate(completions);
    }

    private static String textEdit2String(TextEdit te) {
        return te.getStartOffset() + "-" + te.getEndOffset() + ":" + te.getNewText().replace("\n", "\\n");
    }

    private String sourceLevel;

    private final void setSourceLevel(String sourceLevel) {
        this.sourceLevel = sourceLevel;
    }

    interface Validator<T> {
        public void validate(T t) throws Exception;
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

    static class MIMEDataProvider implements MimeDataProvider {

        @Override
        public Lookup getLookup(MimePath mimePath) {
            if ("text/x-java".equals(mimePath.getPath())) {
                return Lookups.fixed(JavaTokenId.language());
            }
            return null;
        }

    }

    private static final class FileDescription {

        final String fileName;
        final String code;

        public FileDescription(String fileName, String code) {
            this.fileName = fileName;
            this.code = code;
        }
    }
}
