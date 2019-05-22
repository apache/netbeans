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

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
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
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;

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
//        dataDir = SourceUtilsTestUtil.makeScratchDir(this);
//        FileObject dataTargetPackage = FileUtil.createFolder(dataDir, getSourcePckg());
//        assertNotNull(dataTargetPackage);
//        FileObject dataSourceFolder = FileUtil.toFileObject(getDataDir()).getFileObject(getSourcePckg());
//        assertNotNull(dataSourceFolder);
//        deepCopy(dataSourceFolder, dataTargetPackage);
//        ClassPathProvider cpp = new ClassPathProvider() {
//            public ClassPath findClassPath(FileObject file, String type) {
//                if (type == ClassPath.SOURCE)
//                    return ClassPathSupport.createClassPath(new FileObject[] {dataDir});
//                    if (type == ClassPath.COMPILE)
//                        return ClassPathSupport.createClassPath(new FileObject[0]);
//                    if (type == ClassPath.BOOT)
//                        return BootClassPathUtil.getBootClassPath();
//                    return null;
//            }
//        };
        SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);
        
        SourceUtilsTestUtil.prepareTest(
                new String[] {
                    "org/netbeans/modules/java/project/ui/layer.xml", 
                    "org/netbeans/modules/project/ui/resources/layer.xml"
                },
                new Object[] {loader/*, new VanillaPartialReparser()*/ /*, cpp*/}
        );
        
        JEditorPane.registerEditorKitForContentType("text/x-java", "org.netbeans.modules.editor.java.JavaKit");
//        File cacheFolder = new File(getWorkDir(), "var/cache/index");
//        cacheFolder.mkdirs();
//        IndexUtil.setCacheFolder(cacheFolder);
//        ensureRootValid(dataDir.getURL());
        TestUtil.setupEditorMockServices();
    }
    
    public void testPartialReparse() throws Exception {
        doRunTest("package test;\n" +
                  "public class Test {\n" +
                  "    private void test() {\n" +
                  "        System.err.println(1);//\n" +
                  "    }" +
                  "}",
                  "\n        System.err.println(2);");
    }
    
    private void doRunTest(String code, String inject) throws Exception {
        FileObject srcDir = FileUtil.createMemoryFileSystem().getRoot();
        FileObject src = srcDir.createData("Test.java");
        try (Writer out = new OutputStreamWriter(src.getOutputStream())) {
            out.write(code.replace("//", ""));
        }
        EditorCookie ec = src.getLookup().lookup(EditorCookie.class);
        Document doc = ec.openDocument();
        JavaSource source = JavaSource.forFileObject(src);
        Object[] topLevel = new Object[1];
        source.runUserActionTask(cc -> {
            cc.toPhase(Phase.RESOLVED);
            topLevel[0] = cc.getCompilationUnit();
        }, true);
        doc.insertString(code.indexOf("//"), inject, null);
        AtomicReference<List<TreeDescription>> actualTree = new AtomicReference<>();
        AtomicReference<List<DiagnosticDescription>> actualDiagnostics = new AtomicReference<>();
        source.runUserActionTask(cc -> {
            cc.toPhase(Phase.RESOLVED);
            assertSame(topLevel[0], cc.getCompilationUnit());
            actualTree.set(dumpTree(cc));
            actualDiagnostics.set(dumpDiagnostics(cc));
        }, true);
        FileObject srcDir2 = FileUtil.createMemoryFileSystem().getRoot();
        FileObject src2 = srcDir2.createData("Test.java");
        try (Writer out = new OutputStreamWriter(src2.getOutputStream())) {
            out.write(code.replace("//", inject));
        }
        JavaSource source2 = JavaSource.forFileObject(src);
        AtomicReference<List<TreeDescription>> expectedTree = new AtomicReference<>();
        AtomicReference<List<DiagnosticDescription>> expectedDiagnostics = new AtomicReference<>();
        source2.runUserActionTask(cc -> {
            cc.toPhase(Phase.RESOLVED);
            expectedTree.set(dumpTree(cc));
            expectedDiagnostics.set(dumpDiagnostics(cc));
        }, true);
        assertEquals(expectedTree.get(), actualTree.get());
        assertEquals(expectedDiagnostics.get(), actualDiagnostics.get());
    }

    private static List<TreeDescription> dumpTree(CompilationInfo info) {
        List<TreeDescription> result = new ArrayList<>();
        new TreePathScanner<Void, Void>() {
            @Override
            public Void scan(Tree tree, Void p) {
                if (tree == null) {
                    result.add(null);
                } else {
                    result.add(new TreeDescription(tree.getKind(),
                                                   info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree), 
                                                   info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tree)));
                }
                return super.scan(tree, p);
            }
        }.scan(info.getCompilationUnit(), null);
        return result;
    }

    private static List<DiagnosticDescription> dumpDiagnostics(CompilationInfo info) {
        return info.getDiagnostics().stream().map(d -> new DiagnosticDescription(d)).collect(Collectors.toList());
    }

    private static final class TreeDescription {
        private final Kind kind;
        private final long start;
        private final long end;
        //TODO: element, type

        public TreeDescription(Kind kind, long start, long end) {
            this.kind = kind;
            this.start = start;
            this.end = end;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + Objects.hashCode(this.kind);
            hash = 29 * hash + (int) (this.start ^ (this.start >>> 32));
            hash = 29 * hash + (int) (this.end ^ (this.end >>> 32));
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
            if (this.kind != other.kind) {
                return false;
            }
            return true;
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
        
    }
}
