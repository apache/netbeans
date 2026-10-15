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
package org.netbeans.api.java.source.gen;

import com.sun.source.doctree.AttributeTree;
import com.sun.source.doctree.AuthorTree;
import com.sun.source.doctree.CommentTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.IdentifierTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.UnknownInlineTagTree;
import com.sun.source.doctree.ValueTree;
import com.sun.source.doctree.VersionTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.DocTreeScanner;
import com.sun.source.util.DocTrees;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import static org.netbeans.api.java.source.gen.GeneratorTestBase.getJavaSource;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Ralph Benjamin Ruijs
 */
public class DoctreeTest extends GeneratorTestBase {

    /**
     * Creates a new instance of DoctreeTest
     */
    public DoctreeTest(String name) {
        super(name);
    }
    
    @Override
    String getGoldenPckg() {
        return "";
    }

    @Override
    String getSourcePckg() {
        return"";
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(DoctreeTest.class);
        return suite;
    }
    
    public void testAddDocComment() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * Test method\n" +
            "     * @param test\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        ParamTree param = make.Param(false, make.DocIdentifier("test"), new LinkedList<DocTree>());
                        DocCommentTree newDoc = make.DocComment(
                                Collections.singletonList(make.Text("Test method")),
                                Collections.EMPTY_LIST,
                                Collections.singletonList(param));
                        wc.rewrite(mt, null, newDoc);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test232353 () throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    private void test() {\n"
                + "    }\n"
                + "\n"
                + "    public static class Inner {\n"
                + "        public Inner(String message) {\n"
                + "        }\n"
                + "    }\n"
                + "}\n");
        String golden
                = "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method\n"
                + "     * @param test\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "\n"
                + "    public static class Inner {\n"
                + "\n"
                + "        /**\n"
                + "         *\n"
                + "         * @param message\n"
                + "         */\n"
                + "        public Inner(String message) {\n"
                + "        }\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        if("test".contentEquals(mt.getName())) {
                            ParamTree param = make.Param(false, make.DocIdentifier("test"), new LinkedList<DocTree>());
                            DocCommentTree newDoc = make.DocComment(
                                    Collections.singletonList(make.Text("Test method")),
                                    Collections.EMPTY_LIST,
                                    Collections.singletonList(param));
                            wc.rewrite(mt, null, newDoc);
                        } else {
                            ParamTree param = make.Param(false, make.DocIdentifier("message"), new LinkedList<DocTree>());
                            DocCommentTree newDoc = make.DocComment(
                                    Collections.singletonList(make.Text("")),
                                    Collections.EMPTY_LIST,
                                    Collections.singletonList(param));
                            wc.rewrite(mt, null, newDoc);
                        }
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddDocCommentTagA() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * Test method\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * Test method\n" +
            "     * @param test\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitDocComment(DocCommentTree docComment, Void p) {
                                ParamTree param = make.Param(false, make.DocIdentifier("test"), new LinkedList<DocTree>());
                                DocCommentTree newDoc = make.DocComment(
                                        docComment.getFirstSentence(),
                                        docComment.getBody(),
                                        Collections.singletonList(param));
                                wc.rewrite(mt, docComment, newDoc);
                                return super.visitDocComment(docComment, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddDocCommentTagB() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * @param test\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitDocComment(DocCommentTree docComment, Void p) {
                                ParamTree param = make.Param(false, make.DocIdentifier("test"), new LinkedList<DocTree>());
                                DocCommentTree newDoc = make.DocComment(
                                        docComment.getFirstSentence(),
                                        docComment.getBody(),
                                        Collections.singletonList(param));
                                wc.rewrite(mt, docComment, newDoc);
                                return super.visitDocComment(docComment, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddDocCommentTagC() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * \n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * \n" +
            "     * @param test\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitDocComment(DocCommentTree docComment, Void p) {
                                ParamTree param = make.Param(false, make.DocIdentifier("test"), new LinkedList<DocTree>());
                                DocCommentTree newDoc = make.DocComment(
                                        docComment.getFirstSentence(),
                                        docComment.getBody(),
                                        Collections.singletonList(param));
                                wc.rewrite(mt, docComment, newDoc);
                                return super.visitDocComment(docComment, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddTypeParamEndPos() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * @param <T> \n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * @param <T> \n" +
            "     * @param test \n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitDocComment(DocCommentTree docComment, Void p) {
                                List<DocTree> params = new ArrayList<DocTree>(docComment.getBlockTags());
                                params.add(make.Param(false, make.DocIdentifier("test"), new LinkedList<DocTree>()));
                                DocCommentTree newDoc = make.DocComment(
                                        docComment.getFirstSentence(),
                                        docComment.getBody(),
                                        params);
                                wc.rewrite(mt, docComment, newDoc);
                                return super.visitDocComment(docComment, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddTypeParamEndPosB() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * @param <T> some param\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * @param <T> some param\n" +
            "     * @param test\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitDocComment(DocCommentTree docComment, Void p) {
                                List<DocTree> params = new ArrayList<DocTree>(docComment.getBlockTags());
                                params.add(make.Param(false, make.DocIdentifier("test"), new LinkedList<DocTree>()));
                                DocCommentTree newDoc = make.DocComment(
                                        docComment.getFirstSentence(),
                                        docComment.getBody(),
                                        params);
                                wc.rewrite(mt, docComment, newDoc);
                                return super.visitDocComment(docComment, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveDocCommentTag() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * Test method\n" +
            "     * \n" +
            "     * @param test\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * Test method\n" +
            "     * \n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitDocComment(DocCommentTree docComment, Void p) {
                                DocCommentTree newDoc = make.DocComment(
                                        docComment.getFirstSentence(),
                                        docComment.getBody(),
                                        new LinkedList<DocTree>());
                                wc.rewrite(mt, docComment, newDoc);
                                return super.visitDocComment(docComment, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveInlineDocTag() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method\n"
                + "     * \n"
                + "     * @param test {@link #test}\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method\n"
                + "     * \n"
                + "     * @param test\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitParam(ParamTree node, Void p) {
                                List<DocTree> description = new LinkedList<DocTree>();
                                for (DocTree t : node.getDescription()) {
                                    if (t.getKind() != DocTree.Kind.LINK) {
                                        description.add(t);
                                    }
                                }
                                ParamTree param = make.Param(node.isTypeParameter(), node.getName(), description);
                                wc.rewrite(mt, node, param);
                                return super.visitParam(node, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeInlineDocTag() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method\n"
                + "     * \n"
                + "     * @param test {@link #test foo}\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method\n"
                + "     * \n"
                + "     * @param test {@link #test test method}\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitLink(LinkTree node, Void p) {
                                List<DocTree> text = Collections.singletonList((DocTree) make.Text("test method"));
                                LinkTree newLink = make.Link(node.getReference(), text);
                                wc.rewrite(mt, node, newLink);
                                return super.visitLink(node, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testCreateAll() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * <!-- comment -->{@docRoot}</a>&a;{@inheritDoc}{@link H#H(H, H) H}{@literal H}<a a/>{@a H}{@value H#H(H, H)}\n"
                + "     * @author H\n"
                + "     * @deprecated H\n"
                + "     * @param a H\n"
                + "     * @param <A> H\n"
                + "     * @return H\n"
                + "     * @see H#H(H, H)\n"
                + "     * @serialData H\n"
                + "     * @serialField a H#H(H, H) H\n"
                + "     * @serial H\n"
                + "     * @since H\n"
                + "     * @throws H#H(H, H) H\n"
                + "     * @H H\n"
                + "     * @version H\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        List<DocTree> firstSentence = new LinkedList<DocTree>();
                        List<DocTree> tags = new LinkedList<DocTree>();
                        
                        List<TextTree> text = Collections.singletonList(make.Text("H"));
                        IdentifierTree ident = make.DocIdentifier("a");
                        ReferenceTree reference = make.Reference((ExpressionTree)make.Type("H"), "H", Collections.<ExpressionTree>nCopies(2, (ExpressionTree)make.Type("H")));
                        AttributeTree attribute = make.Attribute("a", AttributeTree.ValueKind.EMPTY, null);

                        firstSentence.add(make.Comment("<!-- comment -->"));
                        firstSentence.add(make.DocRoot());
                        firstSentence.add(make.EndElement("a"));
                        firstSentence.add(make.Entity("a"));
                        firstSentence.add(make.InheritDoc());
                        firstSentence.add(make.Link(reference, text));
                        firstSentence.add(make.DocLiteral(text.get(0)));
                        firstSentence.add(make.StartElement("a", Collections.singletonList(attribute), true));
                        firstSentence.add(make.UnknownInlineTag("a", text));
                        firstSentence.add(make.Value(reference));

                        tags.add(make.Author(text));
                        tags.add(make.Deprecated(text));
                        tags.add(make.Param(false, ident, text));
                        tags.add(make.Param(true, make.DocIdentifier("A"), text));
                        tags.add(make.DocReturn(text));
                        tags.add(make.See(Collections.singletonList(reference)));
                        tags.add(make.SerialData(text));
                        tags.add(make.SerialField(ident, reference, text));
                        tags.add(make.Serial(text));
                        tags.add(make.Since(text));
                        tags.add(make.Throws(reference, text));
                        tags.add(make.UnknownBlockTag("H", text));
                        tags.add(make.Version(text));
                        
                        DocCommentTree newDoc = make.DocComment(firstSentence, Collections.EMPTY_LIST, tags);
                        wc.rewrite(mt, docTree, newDoc);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testChangeAttribute() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method\n"
                + "     * \n"
                + "     * @param test {@link #test foo}\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method\n"
                + "     * \n"
                + "     * @param test {@link #test test method}\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitLink(LinkTree node, Void p) {
                                List<DocTree> text = Collections.singletonList((DocTree) make.Text("test method"));
                                LinkTree newLink = make.Link(node.getReference(), text);
                                wc.rewrite(mt, node, newLink);
                                return super.visitLink(node, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeAuthor() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @author\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @author Ralph Benjamin Ruijs\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitAuthor(AuthorTree node, Void p) {
                                List<DocTree> text = Collections.singletonList((DocTree) make.Text("Ralph Benjamin Ruijs"));
                                AuthorTree newTree = make.Author(text);
                                wc.rewrite(mt, node, newTree);
                                return null;
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeComment() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * <!-- &pound;33,- -->\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * <!-- &euro;33,- -->\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitComment(CommentTree node, Void p) {
                                CommentTree newTree = make.Comment("<!-- &euro;33,- -->");
                                wc.rewrite(mt, node, newTree);
                                return null;
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeDeprecated() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @deprecated\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @deprecated something\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitDeprecated(DeprecatedTree node, Void p) {
                                List<DocTree> text = Collections.singletonList((DocTree) make.Text("something"));
                                DeprecatedTree newTree = make.Deprecated(text);
                                wc.rewrite(mt, node, newTree);
                                return null;
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeEntity() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * &pound;33,-\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * &euro;33,-\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitEntity(EntityTree node, Void p) {
                                EntityTree newTree = make.Entity("euro");
                                wc.rewrite(mt, node, newTree);
                                return null;
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeLink() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method {@link #test Test}\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method {@link #toString string}\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitLink(LinkTree node, Void p) {
                                LinkTree newTree = make.Link(make.Reference(null, "toString", null), Collections.singletonList(make.Text("string")));
                                wc.rewrite(mt, node, newTree);
                                return null;
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testChangeToLinkPlain() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method {@link #test Test}\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method {@linkplain #toString string}\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitLink(LinkTree node, Void p) {
                                LinkTree newTree = make.LinkPlain(make.Reference(null, "toString", null), Collections.singletonList(make.Text("string")));
                                wc.rewrite(mt, node, newTree);
                                return null;
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeLiteral() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method {@literal Benjamin Ruijs}\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method {@literal NetBeans}\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitLiteral(LiteralTree node, Void p) {
                                LiteralTree newTree = make.DocLiteral(make.Text("NetBeans"));
                                wc.rewrite(mt, node, newTree);
                                return null;
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testChangeCode() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method {@code Benjamin Ruijs}\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * NetBeans{@code NetBeans}\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitText(TextTree node, Void p) {
                                TextTree newTree = make.Text("NetBeans");
                                wc.rewrite(mt, node, newTree);
                                return null;
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testChangeParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @param a\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @param b B\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitParam(ParamTree node, Void p) {
                                ParamTree newTree = make.Param(false, make.DocIdentifier("b"), Collections.singletonList(make.Text("B")));
                                wc.rewrite(mt, node, newTree);
                                return null;
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testChangeTypeParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @param <A>\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @param <B> b\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitParam(ParamTree node, Void p) {
                                ParamTree newTree = make.Param(true, make.DocIdentifier("B"), Collections.singletonList(make.Text("b")));
                                wc.rewrite(mt, node, newTree);
                                return null;
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testChangeTypeParam2() throws Exception { // #237448
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @param <A> a\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @param <A> b\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            AtomicBoolean inParam = new AtomicBoolean(false);
                            
                            @Override
                            public Void visitParam(ParamTree node, Void p) {
                                inParam.set(true);
                                super.visitParam(node, p);
                                inParam.set(false);
                                return null;
                            }

                            @Override
                            public Void visitText(TextTree node, Void p) {
                                if(inParam.get()) {
                                    TextTree newTree = make.Text("b");
                                    wc.rewrite(mt, node, newTree);
                                    return null;
                                } else {
                                    return super.visitText(node, p);
                                }
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeToTypeParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @param a\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @param <B> b\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitParam(ParamTree node, Void p) {
                                ParamTree newTree = make.Param(true, make.DocIdentifier("B"), Collections.singletonList(make.Text("b")));
                                wc.rewrite(mt, node, newTree);
                                return null;
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeReference() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @see Test#test\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @see Some#thing\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitReference(ReferenceTree node, Void p) {
                                ReferenceTree newTree = make.Reference((ExpressionTree)make.Type("Some"), "thing", null);
                                wc.rewrite(mt, node, newTree);
                                return null;
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeReturn() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @return\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @return something\n"
                + "     * \n"
                + "     */\n"
                + "    private String test() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitReturn(ReturnTree node, Void p) {
                                List<DocTree> text = Collections.singletonList((DocTree) make.Text("something"));
                                ReturnTree newTree = make.DocReturn(text);
                                wc.rewrite(mt, node, newTree);
                                return null;
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeSee() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @see Test\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @see #test\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitSee(SeeTree node, Void p) {
                                List<DocTree> text = Collections.singletonList((DocTree) make.Reference(null, "test", null));
                                SeeTree newTree = make.See(text);
                                wc.rewrite(mt, node, newTree);
                                return null;
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeSince() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @since\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @since 33.3\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitSince(SinceTree node, Void p) {
                                List<DocTree> text = Collections.singletonList((DocTree) make.Text("33.3"));
                                SinceTree newTree = make.Since(text);
                                wc.rewrite(mt, node, newTree);
                                return super.visitSince(node, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeStartElement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test <p>method</p>.\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test <p class=\"id\">method</p>.\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            
                            @Override
                            public Void visitStartElement(StartElementTree node, Void p) {
                                List<? extends DocTree> attrs = Collections.singletonList(make.Attribute("class", AttributeTree.ValueKind.DOUBLE, Collections.singletonList(make.Text("id"))));
                                StartElementTree newTree = make.StartElement(node.getName(), attrs, false);
                                wc.rewrite(mt, node, newTree);
                                return super.visitStartElement(node, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testChangeStartEndElement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test <i>method</i>.\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test <b>method</b>.\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitEndElement(EndElementTree node, Void p) {
                                EndElementTree newTree = make.EndElement("b");
                                wc.rewrite(mt, node, newTree);
                                return super.visitEndElement(node, p);
                            }
                            
                            @Override
                            public Void visitStartElement(StartElementTree node, Void p) {
                                StartElementTree newTree = make.StartElement("b", Collections.EMPTY_LIST, false);
                                wc.rewrite(mt, node, newTree);
                                return super.visitStartElement(node, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeThrows() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @throws Exception\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() throws Exception {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @throws Exception exception\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() throws Exception {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitThrows(ThrowsTree node, Void p) {
                                List<DocTree> text = Collections.singletonList((DocTree) make.Text("exception"));
                                ThrowsTree newTree = make.Throws(node.getExceptionName(), text);
                                wc.rewrite(mt, node, newTree);
                                return super.visitThrows(node, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeUnknownBlockTag() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @Ralph Benjamin Ruijs\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method.\n"
                + "     * @Oracle NetBeans\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitUnknownBlockTag(UnknownBlockTagTree node, Void p) {
                                List<DocTree> text = Collections.singletonList((DocTree) make.Text("NetBeans"));
                                UnknownBlockTagTree newVersion = make.UnknownBlockTag("Oracle", text);
                                wc.rewrite(mt, node, newVersion);
                                return super.visitUnknownBlockTag(node, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeUnknownInlineTag() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method {@Ralph Benjamin Ruijs}\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method {@Oracle NetBeans}\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {

                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {

                            @Override
                            public Void visitUnknownInlineTag(UnknownInlineTagTree node, Void p) {
                                List<DocTree> text = Collections.singletonList((DocTree) make.Text("NetBeans"));
                                UnknownInlineTagTree newVersion = make.UnknownInlineTag("Oracle", text);
                                wc.rewrite(mt, node, newVersion);
                                return super.visitUnknownInlineTag(node, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeValue() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method {@value #test}\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method {@value Test}\n"
                + "     * \n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitValue(ValueTree node, Void p) {
                                ReferenceTree ref = make.Reference((ExpressionTree)make.Type("Test"), null, null);
                                ValueTree newVersion = make.Value(ref);
                                wc.rewrite(mt, node, newVersion);
                                return super.visitValue(node, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeVersion() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method\n"
                + "     * \n"
                + "     * @version\n"
                + "     * @version  1\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n");
        String golden =
                "package hierbas.del.litoral;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "    /**\n"
                + "     * Test method\n"
                + "     * \n"
                + "     * @version 1.3\n"
                + "     * @version  1.3\n"
                + "     */\n"
                + "    private void test() {\n"
                + "    }\n"
                + "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitVersion(VersionTree node, Void p) {
                                List<DocTree> text = Collections.singletonList((DocTree) make.Text("1.3"));
                                VersionTree newVersion = make.Version(text);
                                wc.rewrite(mt, node, newVersion);
                                return super.visitVersion(node, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddMarkdownTag() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;

                public class Test {

                    /// Test method
                    ///
                    /// @param p1 param1
                    private void test(int p1, int p2) {
                    }
                }
                """);
        String golden =
                """
                package hierbas.del.litoral;

                public class Test {

                    /// Test method
                    ///
                    /// @param p1 param1
                    /// @param p2 param2
                    private void test(int p1, int p2) {
                    }
                }
                """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        if (docTree != null) {
                            ArrayList<DocTree> blockTags = new ArrayList<>(docTree.getBlockTags());
                            blockTags.add(make.Param(false, make.DocIdentifier("p2"), List.of(make.Text("param2"))));
                            wc.rewrite(mt, docTree, make.DocComment(docTree.getFullBody(), blockTags));
                        }
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeMarkdownParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;

                public class Test {

                    /// Test method
                    ///
                    /// @param p1 param1
                    private void test(int p2) {
                    }
                }
                """);
        String golden =
                """
                package hierbas.del.litoral;

                public class Test {

                    /// Test method
                    ///
                    /// @param p2 param2
                    private void test(int p2) {
                    }
                }
                """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        DocTreeScanner<Void, Void> scanner = new DocTreeScanner<Void, Void>() {
                            @Override
                            public Void visitParam(ParamTree node, Void p) {
                                ParamTree newParam = make.Param(false, make.DocIdentifier("p2"), List.of(make.Text("param2")));
                                wc.rewrite(mt, node, newParam);
                                return super.visitParam(node, p);
                            }
                        };
                        scanner.scan(docTree, null);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveMarkdownParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;

                public class Test {

                    /// Test method
                    ///
                    /// @param p1 param1
                    private void test(int p2) {
                    }
                }
                """);
        String golden =
                """
                package hierbas.del.litoral;

                public class Test {

                    /// Test method
                    ///
                    private void test(int p2) {
                    }
                }
                """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        DocCommentTree docTree = trees.getDocCommentTree(getCurrentPath());
                        if (docTree != null) {
                            wc.rewrite(mt, docTree, make.DocComment(docTree.getFullBody(), List.of()));
                        }
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testNewMarkdownComment() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                """
                package hierbas.del.litoral;

                public class Test {

                    private void test(int p2) {
                    }
                }
                """);
        String golden =
                """
                package hierbas.del.litoral;

                public class Test {

                    /// Test method
                    /// @param p2 param2
                    private void test(int p2) {
                    }
                }
                """;

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            @Override
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                final DocTrees trees = wc.getDocTrees();
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(final MethodTree mt, Void p) {
                        ParamTree param2 = make.Param(false, make.DocIdentifier("p2"), List.of(make.Text("param2")));
                        DocCommentTree newDoc = make.MarkdownDocComment(List.of(make.RawText("Test method")), List.of(param2));
                        wc.rewrite(mt, null, newDoc);
                        return super.visitMethod(mt, p);
                    }
                }.scan(wc.getCompilationUnit(), null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
}
