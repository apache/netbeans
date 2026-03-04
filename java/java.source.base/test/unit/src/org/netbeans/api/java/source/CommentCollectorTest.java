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

import com.sun.source.tree.*;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.CommentSetImpl;
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.query.CommentSet;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenHierarchy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.netbeans.modules.java.source.usages.IndexUtil;

/**
 * @author Rastislav Komara (<a href="mailto:moonko@netbeans.orgm">RKo</a>)
 * @todo documentation
 */
@SuppressWarnings({"unchecked"})
public class CommentCollectorTest extends NbTestCase {


    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(CommentCollectorTest.class);
//        suite.addTest(new CommentCollectorTest("testMethod2"));
        return suite;
    }

    private File work;
    
    @Override
    protected void setUp() throws Exception {
        File wd = getWorkDir();
        FileObject cache = FileUtil.createFolder(new File(wd, "cache"));
        IndexUtil.setCacheFolder(FileUtil.toFile(cache));
        work = new File(wd, "work");
        work.mkdirs();
        
        super.setUp();
    }

    /**
     * Constructs a test case with the given name.
     *
     * @param name name of the testcase
     */
    public CommentCollectorTest(String name) {
        super(name);
    }

    static JavaSource getJavaSource(File aFile) throws IOException {
        FileObject testSourceFO = FileUtil.toFileObject(aFile);
        assertNotNull(testSourceFO);
        return JavaSource.forFileObject(testSourceFO);
    }
    
    public void testCollectEmptyComments199756() throws Exception {
        File testFile = new File(work, "Test.java");
        final String origin = "public class Test {\n" +
        "        /* comment before */\n" +
        "        public void m() {}\n" +
        "        /* comment after */\n\n" +
        "        /** comment before 2 */\n" +
        "        public void m2() {\n" +
        "        }\n" +
        "        /* comment after 2 */\n\n" +
        "        /**\n" +
        "        doc comment before 3\n" +
        "        */\n" +
        "        // line comment before 3\n" +
        "        public void m3() {\n" +
        "            \n" +
        "            // line comment in 3\n" +
        "        }\n" +
        "        /* block comment after 3 */\n\n" +
        "        \n" +
        "        public void m4() {\n" +
        "            \n" +
        "        }\n" +
        "        // line comment after 4\n" +
        "    }";
        TestUtilities.copyStringToFile(testFile, origin);
        
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            protected CommentHandlerService service;

            public void run(WorkingCopy workingCopy) throws Exception {
//                CommentCollector cc = CommentCollector.getInstance();
                workingCopy.toPhase(JavaSource.Phase.PARSED);
//                cc.collect(workingCopy);
                CompilationUnitTree cu = workingCopy.getCompilationUnit();
                GeneratorUtilities.get(workingCopy).importComments(cu, cu);

                service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());
                CommentPrinter printer = new CommentPrinter(service);
                cu.accept(printer, null);


                TreeVisitor<Void, Void> w = new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(MethodTree node, Void aVoid) {
                        switch (node.getName().toString()) {
                            case "m":
                                verify(node, CommentSet.RelativePosition.PRECEDING, service, 
                                        "/* comment before */");
                                verify(node, CommentSet.RelativePosition.TRAILING, service, 
                                        "/* comment after */");
                                break;
                            case "m2":
                                verify(node, CommentSet.RelativePosition.PRECEDING, service, 
                                        "/** comment before 2 */");
                                verify(node, CommentSet.RelativePosition.TRAILING, service, 
                                        "/* comment after 2 */");
                                break;
                            case "m3":
                                verify(node, CommentSet.RelativePosition.PRECEDING, service, 
                                        "/**\n        doc comment before 3\n        */", "// line comment before 3");
                                verify(node.getBody(), CommentSet.RelativePosition.INNER, service,
                                        "",
                                        "// line comment in 3");
                                verify(node, CommentSet.RelativePosition.TRAILING, service, 
                                        "/* block comment after 3 */");
                                break;
                            case "m4":
                                verify(node, CommentSet.RelativePosition.PRECEDING, service);
                                verify(node.getBody(), CommentSet.RelativePosition.INNER, service,
                                        "");
                                verify(node, CommentSet.RelativePosition.TRAILING, service, 
                                        "// line comment after 4");
                                break;
                        }
                        return super.visitMethod(node, aVoid);
                    }
                };
                cu.accept(w, null);

            }


        };
        src.runModificationTask(task);
    }

    @Test
    public void testCollector() throws Exception {
        File testFile = new File(work, "Test.java");
        final String origin = "/** (COMM1) This comment belongs before class */\n" +
                "public class Clazz {\n" +
                "\n\n\n//TODO: (COMM2) This is just right under class (inside)" +
                "\n\n\n\n\n\n" +
                "/** (COMM3) This belongs to encapsulate field */\n" +
                "public int field = 9;\n\n //TODO: (COMM4) This is inside the class comment\n" +
                "/** (COMM5) method which do something */\n" +
                "public void method() {\n" +
                "\t//TODO: (COMM6) Implement this method to do something \n" +
                "}\n" +
                "private String str = \"string object\" //(COMM7) NOI18N\n" +
                "}\n";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            protected CommentHandlerService service;

            public void run(WorkingCopy workingCopy) throws Exception {
//                CommentCollector cc = CommentCollector.getInstance();
                workingCopy.toPhase(JavaSource.Phase.PARSED);
//                cc.collect(workingCopy);
                CompilationUnitTree cu = workingCopy.getCompilationUnit();
                GeneratorUtilities.get(workingCopy).importComments(cu, cu);

                service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());
                CommentPrinter printer = new CommentPrinter(service);
                cu.accept(printer, null);


                TreeVisitor<Void, Void> w = new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void aVoid) {
                        verify(node, CommentSet.RelativePosition.PRECEDING, service, "/** (COMM1) This comment belongs before class */");
                        return super.visitClass(node, aVoid);
                    }

                    @Override
                    public Void visitVariable(VariableTree node, Void aVoid) {
                        if (node.toString().contains("field")) {
                            verify(node, CommentSet.RelativePosition.PRECEDING, service, 
                                    "//TODO: (COMM2) This is just right under class (inside)", 
                                    "/** (COMM3) This belongs to encapsulate field */");
                        }
                        return super.visitVariable(node, aVoid);
                    }

                    @Override
                    public Void visitLiteral(LiteralTree node, Void aVoid) {
                        if (node.toString().contains("string")) {
                            verify(node, CommentSet.RelativePosition.INLINE, service, "//(COMM7) NOI18N");
                        }
                        return super.visitLiteral(node, aVoid);
                    }

                    @Override
                    public Void visitMethod(MethodTree node, Void aVoid) {
                        if (node.toString().contains("method")) {
                            verify(node, CommentSet.RelativePosition.PRECEDING
                                    , service, "//TODO: (COMM4) This is inside the class comment"
                                    , "/** (COMM5) method which do something */");
                            verify(node.getBody(), CommentSet.RelativePosition.INNER, service, /*XXX:*/"\n\t".trim(), "//TODO: (COMM6) Implement this method to do something");
                        }
                        return super.visitMethod(node, aVoid);
                    }
                };
                cu.accept(w, null);

            }


        };
        src.runModificationTask(task);

    }


    public void testMethod() throws Exception {
        File testFile = new File(work, "Test.java");
        String origin = "\n" +
                "import java.io.File;\n" +
                "\n" +
                "public class Test {\n" +
                "\n" +
                "    void method() {\n" +
                "        // Test\n" +
                "        System.out.println(\"Slepitchka\");\n" +
                "    }\n" +
                "\n" +
                "}\n";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            protected CommentHandlerService service;

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                CompilationUnitTree cu = workingCopy.getCompilationUnit();
                GeneratorUtilities.get(workingCopy).importComments(cu, cu);

                service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());
                CommentPrinter printer = new CommentPrinter(service);
                cu.accept(printer, null);

                JCTree.JCClassDecl clazz = (JCTree.JCClassDecl) cu.getTypeDecls().get(0);
                final boolean[] processed = new boolean[1];
                TreeVisitor<Void, Void> w = new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitExpressionStatement(ExpressionStatementTree node, Void p) {
                        verify(node, CommentSet.RelativePosition.PRECEDING, service, "// Test");
                        processed[0] = true;
                        return super.visitExpressionStatement(node, p);
                    }
                };
                clazz.accept(w, null);
                if (!processed[0]) {
                    fail("Tree has not been processed!");
                }
            }


        };
        src.runModificationTask(task);

    }

    public void testMethod2() throws Exception {
        File testFile = new File(work, "Test.java");
        final String origin =
                "public class Origin {\n" +
                        "    /** * comment * @return 1 */\n" +
                        "    int method() {\n" +
                        "        // TODO: Process the button click action. Return value is a navigation\n" +
                        "        // case name where null will return to the same page.\n" +
                        "        return 1;\n" +
                        "    }\n" +
                        "}\n";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            protected CommentHandlerService service;

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                CompilationUnitTree cu = workingCopy.getCompilationUnit();
                GeneratorUtilities.get(workingCopy).importComments(cu, cu);

                service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());
                CommentPrinter printer = new CommentPrinter(service);
                cu.accept(printer, null);

                JCTree.JCClassDecl clazz = (JCTree.JCClassDecl) cu.getTypeDecls().get(0);
                TreeVisitor<Void, Void> w = new ErrorAwareTreeScanner<Void, Void>() {

                    @Override
                    public Void visitReturn(ReturnTree node, Void aVoid) {
                        verify(node, CommentSet.RelativePosition.PRECEDING, service,
                                "// TODO: Process the button click action. Return value is a navigation",
                                "// case name where null will return to the same page."
                        );
                        return super.visitReturn(node, aVoid);
                    }

                    @Override
                    public Void visitMethod(MethodTree node, Void aVoid) {
                        if (node.getName().contentEquals("method")) {
                            verify(node, CommentSet.RelativePosition.PRECEDING, service,
                                    "/** * comment * @return 1 */"
                            );
                        }
                        return super.visitMethod(node, aVoid);
                    }


                };
                clazz.accept(w, null);
            }


        };
        src.runModificationTask(task);

    }

    public void testVariable() throws Exception {
        File testFile = new File(work, "Test.java");
        final String origin =
                       "package test;\n" +
                       "public abstract class Test {\n" +
                       "    public Test() {\n" +
                       "        //nabytek\n" +
                       "        FileInputStream fis = new FileInputStream(new File(\"\"));//NOI18N\n" +
                       "        //foo\n" +
                       "        \n" +
                       "        fis = null;\n" +
                       "    }\n" +
                       "}\n";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                final CommentHandlerService service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());

                ErrorAwareTreeScanner<Void, Void> w = new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitVariable(VariableTree node, Void p) {
                        GeneratorUtilities.get(workingCopy).importComments(node, workingCopy.getCompilationUnit());
                        verify(node, CommentSet.RelativePosition.PRECEDING, service,
                                "//nabytek"
                        );
                        verify(node, CommentSet.RelativePosition.INLINE, service,
                                "//NOI18N"
                        );
                        verify(node, CommentSet.RelativePosition.TRAILING, service,
                                "//foo"
                        );

                        return super.visitVariable(node, p);
                    }
                };
                w.scan(workingCopy.getCompilationUnit(), null);
            }


        };
        src.runModificationTask(task);

    }

    public void test186176a() throws Exception {
        File testFile = new File(work, "Test.java");
        final String origin =
                       "package test;\n" +
                       "public abstract class Test {\n" +
                       "    private void m2() {\n" +
                       "    }\n" +
                       "\n" +
                       "    /**\n" +
                       "     *test1*\n" +
                       "     */\n" +
                       "    /**\n" +
                       "     *test2*\n" +
                       "     */\n" +
                       "    private void m3() {\n" +
                       "    }\n\n" +
                       "}\n";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                final CommentHandlerService service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());

                ErrorAwareTreeScanner<Void, Void> w = new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(MethodTree node, Void p) {
                        if (node.getName().contentEquals("m3")) {
                            verify(node, CommentSet.RelativePosition.PRECEDING, service,
                                    "/**\n     *test1*\n     */", "/**\n     *test2*\n     */"
                            );
                        }
                        return super.visitMethod(node, p);
                    }
                    @Override
                    public Void visitClass(ClassTree node, Void p) {
                        GeneratorUtilities.get(workingCopy).importComments(node, workingCopy.getCompilationUnit());
                        return super.visitClass(node, p);
                    }
                };
                w.scan(workingCopy.getCompilationUnit(), null);
            }


        };
        src.runModificationTask(task);

    }

    public void test186176b() throws Exception {
        File testFile = new File(work, "Test.java");
        final String origin =
                       "package test;\n" +
                       "public abstract class Test {\n public void test() {\n /*zbytek*/\nSystem.out.println(\"\"); /*obycej*/\n/*bystry*/\nnew java.io.FileInputStream(\"\");\n/*bylina*/\n} \n }\n";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                final CommentHandlerService service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());

                ErrorAwareTreeScanner<Void, Void> w = new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(MethodTree node, Void p) {
                        GeneratorUtilities.get(workingCopy).importComments(node, workingCopy.getCompilationUnit());
                        
                        if (node.getName().contentEquals("test")) {
                            StatementTree first = node.getBody().getStatements().get(0);
                            verify(first, CommentSet.RelativePosition.PRECEDING, service,
                                    "/*zbytek*/"
                            );
                            verify(first, CommentSet.RelativePosition.INLINE, service,
                                    "/*obycej*/"
                            );
                            StatementTree second = node.getBody().getStatements().get(1);
                            verify(second, CommentSet.RelativePosition.PRECEDING, service,
                                    "/*bystry*/"
                            );
                            verify(second, CommentSet.RelativePosition.TRAILING, service,
                                    "/*bylina*/"
                            );
                        }
                        return super.visitMethod(node, p);
                    }
                };
                w.scan(workingCopy.getCompilationUnit(), null);
            }


        };
        src.runModificationTask(task);

    }

    public void test179202() throws Exception {
        File testFile = new File(work, "Test.java");
        final String origin =
                       "package test;\n" +
                       "public class Test {\n public void test() {\n int x = 0; // some comment\n System.currentTimeMillis();\n }\n }\n";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                final CommentHandlerService service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());

                ErrorAwareTreeScanner<Void, Void> w = new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(MethodTree node, Void p) {
                        if (node.getName().contentEquals("test")) {
                            StatementTree second = node.getBody().getStatements().get(1);
                            GeneratorUtilities.get(workingCopy).importComments(second, workingCopy.getCompilationUnit());
                            verify(second, CommentSet.RelativePosition.PRECEDING, service);
                            verify(second, CommentSet.RelativePosition.INLINE, service);
                            verify(second, CommentSet.RelativePosition.TRAILING, service);
                        }
                        return super.visitMethod(node, p);
                    }
                };
                w.scan(workingCopy.getCompilationUnit(), null);
            }


        };
        src.runModificationTask(task);

    }

    public void test186754() throws Exception {
        File testFile = new File(work, "Test.java");
        final String origin =
                       "package test;\n" +
                       "public class Test {\n public void test() {\n } //test\n /**foo*/\n private void t() {\n}\n }\n";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                final CommentHandlerService service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());

                ErrorAwareTreeScanner<Void, Void> w = new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void p) {
                        GeneratorUtilities.get(workingCopy).importComments(node, workingCopy.getCompilationUnit());
                        new CommentPrinter(service).scan(node, null);
                        return super.visitClass(node, p);
                    }
                    @Override
                    public Void visitMethod(MethodTree node, Void p) {
                        if (node.getName().contentEquals("test")) {
                            verify(node.getBody(), CommentSet.RelativePosition.PRECEDING, service);
                            verify(node.getBody(), CommentSet.RelativePosition.INLINE, service, "//test");
                            verify(node.getBody(), CommentSet.RelativePosition.TRAILING, service);
                        }
                        if (node.getName().contentEquals("t")) {
                            verify(node, CommentSet.RelativePosition.PRECEDING, service, "/**foo*/");
                            verify(node, CommentSet.RelativePosition.INLINE, service);
                            verify(node, CommentSet.RelativePosition.TRAILING, service);
                        }
                        return super.visitMethod(node, p);
                    }
                };
                w.scan(workingCopy.getCompilationUnit(), null);
            }


        };
        src.runModificationTask(task);

    }

    public void test197057() throws Exception {
        File testFile = new File(work, "Test.java");
        final String origin =
                       "package test;\n" +
                       "public class Test {\n public void aa() {\n//aa\n } public void bb() {\n//bb\n } }\n";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree a = (MethodTree) clazz.getMembers().get(0);
                MethodTree b = (MethodTree) clazz.getMembers().get(1);

                a = GeneratorUtilities.get(workingCopy).importComments(a, workingCopy.getCompilationUnit());
                b = GeneratorUtilities.get(workingCopy).importComments(b, workingCopy.getCompilationUnit());

                final CommentHandlerService service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());

                verify(a.getBody(), CommentSet.RelativePosition.INNER, service, "", "//aa");
                verify(b.getBody(), CommentSet.RelativePosition.INNER, service, "", "//bb");
            }
        };
        src.runModificationTask(task);

    }

    public void test205525() throws Exception {
        File testFile = new File(work, "Test.java");
        final String origin =
                       "package test;\n" +
                       "public class Test {\n public void aa() {\n//aa\n } public void bb() {\n//bb\n } }\n";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);

                clazz = GeneratorUtilities.get(workingCopy).importComments(clazz, workingCopy.getCompilationUnit());

                final CommentHandlerService service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());

                assertTrue(service.getComments(clazz).areCommentsMapped());

                verify(clazz, CommentSet.RelativePosition.INNER, service);
            }
        };
        src.runModificationTask(task);

    }

    public void testImportCommentsIdempotent206200() throws Exception {
        File testFile = new File(work, "Test.java");
        final String origin =
                       "package test;\n" +
                       "public class Test {\n /**prec*/\npublic void aa() {\n//aa\n int ii = 0;} }\n";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree mt = (MethodTree) clazz.getMembers().get(0);
                VariableTree var = (VariableTree) mt.getBody().getStatements().get(0);
                
                GeneratorUtilities.get(workingCopy).importComments(var, workingCopy.getCompilationUnit());
                GeneratorUtilities.get(workingCopy).importComments(var, workingCopy.getCompilationUnit());

                final CommentHandlerService service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());

                verify(var, CommentSet.RelativePosition.PRECEDING, service, "//aa");

                GeneratorUtilities.get(workingCopy).importComments(clazz, workingCopy.getCompilationUnit());

                verify(var, CommentSet.RelativePosition.PRECEDING, service, "//aa");
                verify(mt, CommentSet.RelativePosition.PRECEDING, service, "/**prec*/");
                verify(clazz, CommentSet.RelativePosition.PRECEDING, service);
            }
        };
        src.runModificationTask(task);

    }
    
    public void testImportLeadingComment() throws Exception {
        File testFile = new File(work, "Test.java");
        final String origin =
                       "/*leading*/\n" +
                       "package test;\n" +
                       "public class Test { }\n";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                GeneratorUtilities.get(workingCopy).importComments(workingCopy.getCompilationUnit(), workingCopy.getCompilationUnit());

                final CommentHandlerService service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());

                verify(workingCopy.getCompilationUnit(), CommentSet.RelativePosition.PRECEDING, service, "/*leading*/");
            }
        };
        src.runModificationTask(task);
    }
    
    public void test224577() throws Exception {
        File testFile = new File(work, "Test.java");
        final String origin =
                       "package test;\n" +
                       "public class Test {\n" +
                       "    private void test() { synchronized(this)";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                GeneratorUtilities.get(workingCopy).importComments(workingCopy.getCompilationUnit(), workingCopy.getCompilationUnit());
            }
        };
        src.runModificationTask(task);
    }
    
    public void test223701() throws Exception {
        File testFile = new File(work, "Test.java");
        final String origin =
                       "/*a*/\npackage test;\n" +
                       "class Test {\n" +
                       "}";
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                ClassTree ct = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ((JCClassDecl) ct).pos = 1;//"great" positions apparently produced by some code-generating tools (read: Lombok)
                GeneratorUtilities.get(workingCopy).importComments(ct, workingCopy.getCompilationUnit());
            }
        };
        src.runModificationTask(task);
    }

    void verify(Tree tree, CommentSet.RelativePosition position, CommentHandler service, String... comments) {
        assertNotNull("Comments handler service not null", service);
        CommentSet set = service.getComments(tree);
        java.util.List<Comment> cl = set.getComments(position);
        assertEquals("Unexpected size of " + tree.getKind() + " "
                + position.toString().toLowerCase() +
                " comments", comments.length, cl.size());
        Arrays.sort(comments);
        for (Comment comment : cl) {
            String text = comment.getText().trim();
            if (Arrays.binarySearch(comments, text) < 0) {
                fail("There is no occurence of " + comment + " within list of required comments");
            }
        }
    }

    private static class CommentPrinter extends ErrorAwareTreeScanner<Void, Void> {
        private CommentHandlerService service;

        CommentPrinter(CommentHandlerService service) {
            this.service = service;
        }

        @Override
        public Void scan(Tree node, Void aVoid) {
            defaultAction(node, aVoid);
            return super.scan(node, aVoid);
        }

        protected Void defaultAction(Tree node, Void aVoid) {
            CommentSetImpl comments = service.getComments(node);
            if (comments.hasComments()) {
                String s = node.toString();
                System.out.println(node.getKind()
                        + ": '"
                        + s.substring(0, Math.min(20, s.length() - 1)).replace("\n", "\\n").replace("\n\r", "\\n")
                        + "' |> " + comments + "\n\n");
            }
            return aVoid;
        }

        @Override
        public Void visitCompilationUnit(CompilationUnitTree node, Void aVoid) {
            defaultAction(node, aVoid);
            return super.visitCompilationUnit(node, aVoid);
        }
    }

    public void testImproperlyMappedCommentsMembersStartFromModifiers() throws Exception {
        File testFile = new File(work, "Test.java");
        final String origin =
                """
                public class Test {
                    static final int I = 1; //trailing

                    //preceding
                    @Deprecated
                    void test() {
                    }

                    Test() {}
                }
                """;
        TestUtilities.copyStringToFile(testFile, origin);
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                final CommentHandlerService service = CommentHandlerService.instance(workingCopy.impl.getJavacTask().getContext());

                ErrorAwareTreeScanner<Void, Void> w = new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void p) {
                        super.visitClass(node, p);

                        //verify the comments for the field I are not mapped:
                        Tree fieldMember = node.getMembers().get(0);

                        assertEquals(Tree.Kind.VARIABLE, fieldMember.getKind());
                        assertEquals("I", ((VariableTree) fieldMember).getName().toString());

                        CommentSetImpl fieldCommentSet = service.getComments(fieldMember);

                        assertFalse(fieldCommentSet.areCommentsMapped());
                        verify(fieldMember, CommentSet.RelativePosition.PRECEDING, service);
                        verify(fieldMember, CommentSet.RelativePosition.INLINE, service);
                        verify(fieldMember, CommentSet.RelativePosition.TRAILING, service);
                        return null;
                    }

                    @Override
                    public Void visitMethod(MethodTree node, Void p) {
                        if (node.getName().contentEquals("test")) {
                            workingCopy.getTreeUtilities().getComments(node.getModifiers(), true);
                            verify(node, CommentSet.RelativePosition.PRECEDING, service, "//preceding");
                            verify(node, CommentSet.RelativePosition.INLINE, service);
                            verify(node, CommentSet.RelativePosition.TRAILING, service);
                        }
                        return super.visitMethod(node, p);
                    }
                };
                w.scan(workingCopy.getCompilationUnit(), null);
            }
        };

        src.runModificationTask(task);
    }
}
