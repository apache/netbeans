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
package org.netbeans.modules.java.source.parsing;

import com.sun.source.tree.LineMap;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.tools.Diagnostic;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.save.Reindenter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.openide.util.SharedClassObject;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.cookies.EditorCookie;

/**
 *
 * @author jlahoda
 */
public class PartialReparseTest extends NbTestCase {
    public PartialReparseTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        // ensure JavaKit is present, so that NbEditorDocument is eventually created.
        // it handles PositionRefs differently than PlainDocument/PlainEditorKit.
        MockMimeLookup.setInstances(MimePath.get("text/x-java"),
                new Reindenter.Factory(), new JavaKit());
        SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);

        SourceUtilsTestUtil.prepareTest(
                new String[] {
                    "org/netbeans/modules/java/project/ui/layer.xml",
                    "org/netbeans/modules/project/ui/resources/layer.xml"
                },
                new Object[] {loader/*, new VanillaPartialReparser()*/ /*, cpp*/}
        );

        JEditorPane.registerEditorKitForContentType("text/x-java", "org.netbeans.modules.editor.java.JavaKit");
        TestUtil.setupEditorMockServices();
    }

    public void testPartialReparse() throws Exception {
        doRunTest("package test;\n" +
                  "public class Test {\n" +
                  "    private void test() {\n" +
                  "        System.err.println(1);^^\n" +
                  "    }" +
                  "}",
                  "\n        System.err.println(2);");
    }

    public void testIntroduceParseError1() throws Exception {
        doRunTest("package test;\n" +
                  "public class Test {\n" +
                  "    private void test() {\n" +
                  "        System.err.println(1);^^\n" +
                  "    }" +
                  "}",
                  "\n        if (");
    }

    public void testIntroduceParseError2() throws Exception {
        doRunTest("package test;\n" +
                  "public class Test {\n" +
                  "    private void test() {\n" +
                  "        System.err.println(1);^^\n" +
                  "    }" +
                  "}",
                  "\n        if (tr");
    }

    public void testRemoveParseError() throws Exception {
        doRunTest("package test;\n" +
                  "public class Test {\n" +
                  "    private void test() {\n" +
                  "        System.err.println(1);^\n" +
                  "        if (^\n" +
                  "    }" +
                  "}",
                  "");
    }

    public void testResolutionError() throws Exception {
        doRunTest("package test;\n" +
                  "public class Test {\n" +
                  "    private void test() {\n" +
                  "        System.err.println(1);^^\n" +
                  "    }" +
                  "}",
                  "\n        a = 15;");
    }

    public void testRemoveResolution() throws Exception {
        doRunTest("package test;\n" +
                  "public class Test {\n" +
                  "    private void test() {\n" +
                  "        System.err.println(1);^\n" +
                  "        a = 15;^\n" +
                  "    }" +
                  "}",
                  "");
    }

    public void testIntroduceSomeNewErrors() throws Exception {
        doRunTest("package test;\n" +
                  "public class Test {\n" +
                  "    private void test() {\n" +
                  "        System.err.println(1);\n" +
                  "        if (^^\n" +
                  "    }" +
                  "}",
                  "a");
    }

    public void testErrorsRemain() throws Exception {
        doRunTest("package test;\n" +
                  "public class Test {\n" +
                  "    private void test() {\n" +
                  "        System.err.println(1);\n" +
                  "        ^if (^a\n" +
                  "    }" +
                  "}",
                  "if (");
    }

    public void testFlowErrors1() throws Exception {
        doRunTest("package test;\n" +
                  "public class Test {\n" +
                  "    private void test() {\n" +
                  "        final int i = 5;\n" +
                  "        ^^\n" +
                  "        System.err.println(i);\n" +
                  "    }" +
                  "}",
                  "return ;");
    }

    public void testFlowErrors2() throws Exception {
        doRunTest("package test;\n" +
                  "public class Test {\n" +
                  "    private void test() {\n" +
                  "        final int i = 5;\n" +
                  "        ^return ;^\n" +
                  "        System.err.println(i);\n" +
                  "    }" +
                  "}",
                  "");
    }

    public void testAnonymous() throws Exception {
        doRunTest("package test;\n" +
                  "public class Test {\n" +
                  "    private void test() {\n" +
                  "        new Object() {\\n" +
                  "        };" +
                  "        final int i = 5;\n" +
                  "        ^^\n" +
                  "    }" +
                  "}",
                  "final int j = 5;\n");
    }

    public void testAnonymousFullReparse1() throws Exception {
        doVerifyFullReparse("package test;\n" +
                            "public class Test {\n" +
                            "    private void test() {\n" +
                            "        ^new Object() {" +
                            "        };^" +
                            "        final int i = 5;\n" +
                            "    }" +
                            "}",
                            "");
    }

    public void testAnonymousFullReparse2() throws Exception {
        doVerifyFullReparse("package test;\n" +
                            "public class Test {\n" +
                            "    private void test() {\n" +
                            "        new Object() {" +
                            "        };" +
                            "        final int i = 5;\n" +
                            "        ^^\n" +
                            "    }" +
                            "}",
                            "new Object() {}");
    }

    public void testDocComments() throws Exception {
        doRunTest("package test;\n" +
                  "public class Test {\n" +
                  "        /**javadoc1*/" +
                  "    private void test() {\n" +
                  "        new Object() {" +
                  "            /**javadoc2*/" +
                  "            final int i = 5;\n" +
                  "            ^^\n" +
                  "       };\n" +
                  "    }" +
                  "}",
                  "        /**javadoc3*/\n" +
                  "        final int j = 5;\n");
    }

    public void testConstructor1() throws Exception {
        doRunTest("package test;\n" +
                  "public class Test {\n" +
                  "    public Test() {\n" +
                  "        System.err.println(1);\n" +
                  "        ^^\n" +
                  "    }" +
                  "}",
                  "System.err.println(2);");
    }

    public void testConstructor2() throws Exception {
        doRunTest("package test;\n" +
                  "public class Test {\n" +
                  "    public Test() {\n" +
                  "        super();" +
                  "        System.err.println(1);\n" +
                  "        ^^\n" +
                  "    }" +
                  "}",
                  "System.err.println(2);");
    }

    public void testConstructorEnum1() throws Exception {
        doRunTest("package test;\n" +
                  "public enum Test {\n" +
                  "    A(1);\n" +
                  "    public Test(int i) {\n" +
                  "        System.err.println(i);\n" +
                  "        ^^\n" +
                  "    }" +
                  "}",
                  "System.err.println(2);");
    }

    public void testConstructorEnum2() throws Exception {
        doRunTest("package test;\n" +
                  "public enum Test {\n" +
                  "    A(1);\n" +
                  "    public Test(int i) {\n" +
                  "        super();\n" +
                  "        System.err.println(i);\n" +
                  "        ^^\n" +
                  "    }" +
                  "}",
                  "System.err.println(2);");
    }

    private void doRunTest(String code, String inject) throws Exception {
        FileObject srcDir = FileUtil.createMemoryFileSystem().getRoot();
        FileObject src = srcDir.createData("Test.java");
        try (Writer out = new OutputStreamWriter(src.getOutputStream())) {
            out.write(code.replaceFirst("^", "").replaceFirst("^", ""));
        }
        EditorCookie ec = src.getLookup().lookup(EditorCookie.class);
        Document doc = ec.openDocument();
        JavaSource source = JavaSource.forFileObject(src);
        Object[] topLevel = new Object[1];
        source.runUserActionTask(cc -> {
            cc.toPhase(Phase.RESOLVED);
             topLevel[0] = cc.getCompilationUnit();
        }, true);
        int startReplace = code.indexOf('^');
        int endReplace = code.indexOf('^', startReplace + 1) + 1;
        doc.remove(startReplace, endReplace - startReplace);
        doc.insertString(startReplace, inject, null);
        AtomicReference<List<TreeDescription>> actualTree = new AtomicReference<>();
        AtomicReference<List<DiagnosticDescription>> actualDiagnostics = new AtomicReference<>();
        AtomicReference<List<Long>> actualLineMap = new AtomicReference<>();
        source.runUserActionTask(cc -> {
            cc.toPhase(Phase.RESOLVED);
            assertSame(topLevel[0], cc.getCompilationUnit());
            actualTree.set(dumpTree(cc));
            actualDiagnostics.set(dumpDiagnostics(cc));
            actualLineMap.set(dumpLineMap(cc));
        }, true);
        ec.saveDocument();
        ec.close();
        AtomicReference<List<TreeDescription>> expectedTree = new AtomicReference<>();
        AtomicReference<List<DiagnosticDescription>> expectedDiagnostics = new AtomicReference<>();
        AtomicReference<List<Long>> expectedLineMap = new AtomicReference<>();
        source.runUserActionTask(cc -> {
            cc.toPhase(Phase.RESOLVED);
            assertNotSame(topLevel[0], cc.getCompilationUnit());
            expectedTree.set(dumpTree(cc));
            expectedDiagnostics.set(dumpDiagnostics(cc));
            expectedLineMap.set(dumpLineMap(cc));
        }, true);
        assertEquals(expectedTree.get(), actualTree.get());
        assertEquals(expectedDiagnostics.get(), actualDiagnostics.get());
        assertEquals(expectedLineMap.get(), actualLineMap.get());
    }

    private void doVerifyFullReparse(String code, String inject) throws Exception {
        FileObject srcDir = FileUtil.createMemoryFileSystem().getRoot();
        FileObject src = srcDir.createData("Test.java");
        try (Writer out = new OutputStreamWriter(src.getOutputStream())) {
            out.write(code.replaceFirst("^", "").replaceFirst("^", ""));
        }
        EditorCookie ec = src.getLookup().lookup(EditorCookie.class);
        Document doc = ec.openDocument();
        JavaSource source = JavaSource.forFileObject(src);
        Object[] topLevel = new Object[1];
        source.runUserActionTask(cc -> {
            cc.toPhase(Phase.RESOLVED);
             topLevel[0] = cc.getCompilationUnit();
        }, true);
        int startReplace = code.indexOf('^');
        int endReplace = code.indexOf('^', startReplace + 1) - 1;
        doc.remove(startReplace, endReplace - startReplace);
        doc.insertString(startReplace, inject, null);
        source.runUserActionTask(cc -> {
            cc.toPhase(Phase.RESOLVED);
            assertNotSame(topLevel[0], cc.getCompilationUnit());
        }, true);
        ec.saveDocument();
        ec.close();
    }

    private static List<TreeDescription> dumpTree(CompilationInfo info) {
        List<TreeDescription> result = new ArrayList<>();
        new TreePathScanner<Void, Void>() {
            @Override
            public Void scan(Tree tree, Void p) {
                if (tree == null) {
                    result.add(null);
                } else {
                    TreePath tp = new TreePath(getCurrentPath(), tree);
                    Element el = info.getTrees().getElement(tp);
                    StringBuilder elDesc = new StringBuilder();
                    if (el != null) {
                        elDesc.append(el.getKind().name());
                        while (el != null && !SUPPORTED_ELEMENTS.contains(el.getKind())) {
                            elDesc.append(":");
                            elDesc.append(el.getSimpleName());
                            el = el.getEnclosingElement();
                        }
                        if (el != null) {
                            for (String part : SourceUtils.getJVMSignature(ElementHandle.create(el))) {
                                elDesc.append(":");
                                elDesc.append(part);
                            }
                        }
                    }
                    result.add(new TreeDescription(tree.getKind(),
                                                   info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree),
                                                   info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tree),
                                                   elDesc.toString(),
                                                   String.valueOf(info.getTrees().getTypeMirror(tp)),
                                                   info.getTrees().getDocComment(tp)));
                }
                return super.scan(tree, p);
            }
        }.scan(info.getCompilationUnit(), null);
        return result;
    }

    private static final Set<ElementKind> SUPPORTED_ELEMENTS = EnumSet.of(
            ElementKind.PACKAGE, ElementKind.CLASS, ElementKind.INTERFACE,
            ElementKind.ENUM, ElementKind.ANNOTATION_TYPE, ElementKind.METHOD,
            ElementKind.CONSTRUCTOR, ElementKind.INSTANCE_INIT,
            ElementKind.STATIC_INIT, ElementKind.FIELD, ElementKind.ENUM_CONSTANT);

    private static List<DiagnosticDescription> dumpDiagnostics(CompilationInfo info) {
        return info.getDiagnostics().stream().map(d -> new DiagnosticDescription(d)).collect(Collectors.toList());
    }

    private static List<Long> dumpLineMap(CompilationInfo info) {
        LineMap lm = info.getCompilationUnit().getLineMap();
        int len = info.getText().length();
        List<Long> dump = new ArrayList<>();
        for (int p = 0; p <= len + 1; p++) {
            dump.add(lm.getLineNumber(p));
            dump.add(lm.getColumnNumber(p));
            dump.add(lm.getStartPosition(lm.getLineNumber(p)));
            dump.add(lm.getPosition(lm.getLineNumber(p), lm.getColumnNumber(p)));
        }
        return dump;
    }

    private static final class TreeDescription {
        private final Kind kind;
        private final long start;
        private final long end;
        private final String element;
        private final String type;
        private final String docComment;

        public TreeDescription(Kind kind, long start, long end, String element, String type, String docComment) {
            this.kind = kind;
            this.start = start;
            this.end = end;
            this.element = element;
            this.type = type;
            this.docComment = docComment;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 31 * hash + Objects.hashCode(this.kind);
            hash = 31 * hash + (int) (this.start ^ (this.start >>> 32));
            hash = 31 * hash + (int) (this.end ^ (this.end >>> 32));
            hash = 31 * hash + Objects.hashCode(this.element);
            hash = 31 * hash + Objects.hashCode(this.type);
            hash = 31 * hash + Objects.hashCode(this.docComment);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TreeDescription other = (TreeDescription) obj;
            if (this.start != other.start) {
                return false;
            }
            if (this.end != other.end) {
                return false;
            }
            if (!Objects.equals(this.element, other.element)) {
                return false;
            }
            if (!Objects.equals(this.type, other.type)) {
                return false;
            }
            if (!Objects.equals(this.docComment, other.docComment)) {
                return false;
            }
            if (this.kind != other.kind) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "TreeDescription{" + "kind=" + kind + ", start=" + start + ", end=" + end + ", element=" + element + ", type=" + type + ", docComment=" + docComment + '}';
        }


    }

    private static final class DiagnosticDescription {

        private final String code;
        private final String message;
        private final long column;
        private final long endPos;
        private final Diagnostic.Kind kind;
        private final long line;
        private final long pos;
        private final Object source;
        private final long startPos;

        public DiagnosticDescription(Diagnostic diag) {
            this.code = diag.getCode();
            this.message = diag.getMessage(Locale.ROOT);
            this.column = diag.getColumnNumber();
            this.endPos = diag.getEndPosition();
            this.kind = diag.getKind();
            this.line = diag.getLineNumber();
            this.pos = diag.getPosition();
            this.source = diag.getSource();
            this.startPos = diag.getStartPosition();
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + Objects.hashCode(this.code);
            hash = 97 * hash + Objects.hashCode(this.message);
            hash = 97 * hash + (int) (this.column ^ (this.column >>> 32));
            hash = 97 * hash + (int) (this.endPos ^ (this.endPos >>> 32));
            hash = 97 * hash + Objects.hashCode(this.kind);
            hash = 97 * hash + (int) (this.line ^ (this.line >>> 32));
            hash = 97 * hash + (int) (this.pos ^ (this.pos >>> 32));
            hash = 97 * hash + Objects.hashCode(this.source);
            hash = 97 * hash + (int) (this.startPos ^ (this.startPos >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DiagnosticDescription other = (DiagnosticDescription) obj;
            if (this.column != other.column) {
                return false;
            }
            if (this.endPos != other.endPos) {
                return false;
            }
            if (this.line != other.line) {
                return false;
            }
            if (this.pos != other.pos) {
                return false;
            }
            if (this.startPos != other.startPos) {
                return false;
            }
            if (!Objects.equals(this.code, other.code)) {
                return false;
            }
            if (!Objects.equals(this.message, other.message)) {
                return false;
            }
            if (this.kind != other.kind) {
                return false;
            }
            if (!Objects.equals(this.source, other.source)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "DiagnosticDescription{" + "code=" + code + ", message=" + message + ", column=" + column + ", endPos=" + endPos + ", kind=" + kind + ", line=" + line + ", pos=" + pos + ", source=" + source + ", startPos=" + startPos + '}';
        }

    }

    static {
        System.setProperty("java.enable.partial.reparse", "true");
    }
}
