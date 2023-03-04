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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Comment.Style;
import static org.netbeans.modules.java.source.save.PositionEstimator.*;
import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.query.CommentSet;
import org.netbeans.modules.java.source.query.CommentSet.RelativePosition;
import org.netbeans.modules.java.source.save.CasualDiff;
import org.netbeans.modules.java.source.save.PositionEstimator;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
/**
 *
 * @author Pavel Flaska
 */
public class CommentsTest extends GeneratorTestBase {

    /** Creates a new instance of CommentsTest */
    public CommentsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        CasualDiff.noInvalidCopyTos = true;
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(CommentsTest.class);
//        suite.addTest(new CommentsTest("testMoveMethod171345"));
        return suite;
    }
    
    public void testReplaceExpression() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile
                , "package t;\n"
                + "public class A {\n"
                + "    public int getRating() {\n"
                + "        return (false) ? 2 : 1;\n"
                + "    }\n"
                + "    private int numberOfLateDeliveries = 6;\n"
                + "}"
        );
        String golden
                = "package t;\n"
                + "public class A {\n"
                + "    public int getRating() {\n"
                + "        return ( // Comment\n"
                + "        numberOfLateDeliveries > 5) ? 2 : 1;\n"
                + "    }\n"
                + "    private int numberOfLateDeliveries = 6;\n"
                + "}";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ReturnTree returnStatement = (ReturnTree) method.getBody().getStatements().get(0);
                ConditionalExpressionTree condExp = (ConditionalExpressionTree) returnStatement.getExpression();
                ExpressionTree condition = ((ParenthesizedTree)condExp.getCondition()).getExpression();
                BinaryTree newCond = make.Binary(Tree.Kind.GREATER_THAN, make.Identifier("numberOfLateDeliveries"), make.Literal(5));
                make.addComment(newCond, Comment.create(Style.LINE, "Comment"), true);
                workingCopy.rewrite(condition, newCond);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddStatement() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "        // test\n" +
            "        int a;\n" +
            "        /**\n" +
            "         * becko\n" +
            "         */\n" +
            "        int b; //NOI18N\n" +
            "        // cecko\n" +
            "        int c; // trail\n" +
            "    }\n" +
            "\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                String bodyText = 
                        "{\n" +
                        "    // test\n" +
                        "    int a;\n" +
                        "    /**\n" +
                        "     * becko\n" +
                        "     */\n" +
                        "    int b; //NOI18N\n" +
                        "    // cecko\n" +
                        "    int c; // trail\n" +
                        "}";
                BlockTree block = make.createMethodBody(method, bodyText);
                workingCopy.rewrite(method.getBody(), block);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testGetComment1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "        // preceding comment\n" +
            "        int a; // trailing comment\n" +
            "        // what's up?" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
//                CommentHandler comments = workingCopy.getCommentHandler();
//                CommentSet s = comments.getComments(method.getBody().getStatements().get(0));
//                System.err.println(s);
            }

        };
        src.runModificationTask(task).commit();
    }
    
    public void testAddJavaDocAndAnnotationToRemovedMethod() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    Test() {\n" +
            "    }\n" +
            "\n" +
            "    public void nuevoMetodo(String s) {\n" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    Test() {\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * Comentario\n" +
            "     */\n" +
            "    @Deprecated\n" +
            "    @Deprecated\n" +
            "    public void nuevoMetodo(String s) {\n" +
            "    }\n" +
            "\n" +
            "}\n";
        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(final WorkingCopy copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);
                
                TreeMaker make = copy.getTreeMaker();
                ClassTree node = (ClassTree) copy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree modifiers = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                AnnotationTree annotation = make.Annotation(make.Identifier("Deprecated"), Collections.EMPTY_LIST); //NOI18N
                modifiers = make.addModifiersAnnotation(modifiers, annotation);
                modifiers = make.addModifiersAnnotation(modifiers, annotation);

                MethodTree method = make.Method(
                        modifiers,
                        "nuevoMetodo",
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.singletonList(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "s", make.Type("String"), null)),
                        Collections.<ExpressionTree>emptyList(),
                        make.Block(Collections.EMPTY_LIST, false),
                        null
                );
                make.addComment(method, Comment.create(
                        Comment.Style.JAVADOC, 
                        NOPOS, 
                        NOPOS, 
                        1, // to ensure indentation
                        "Comentario"), 
                        true
                );
                ClassTree clazz = make.removeClassMember(node, 1);
                clazz = make.insertClassMember(clazz, 1, method);
//                ClassTree clazz = make.addClassMember(node, method);
                copy.rewrite(node, clazz);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // #99329
    public void testAddJavaDocToMethod() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    Test() {\n" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    Test() {\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * Comentario\n" +
            "     */\n" +
            "    public void nuevoMetodo() {\n" +
            "    }\n" +
            "\n" +
            "}\n";
        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(final WorkingCopy copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);
                
                TreeMaker make = copy.getTreeMaker();
                ClassTree node = (ClassTree) copy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = make.Method(
                        make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                        "nuevoMetodo",
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        "{ }",
                        null
                );
                make.addComment(method, Comment.create(
                        Comment.Style.JAVADOC, 
                        NOPOS, 
                        NOPOS, 
                        1, // to ensure indentation
                        "Comentario"), 
                        true
                );
                ClassTree clazz = make.addClassMember(node, method);
                copy.rewrite(node, clazz);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddJavaDocToExistingMethod() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    public void test(int a) {\n" +
            "    }\n\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    \n" +
            "    /**\n" +
            "     * Comentario\n" +
            "     */\n" +
            "    public void test(int a) {\n" +
            "    }\n\n" +
            "}\n";
        
        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree node = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) node.getMembers().get(1);
                MethodTree copy = make.Method(method.getModifiers(),
                        method.getName(),
                        method.getReturnType(),
                        method.getTypeParameters(),
                        method.getParameters(),
                        method.getThrows(),
                        method.getBody(),
                        (ExpressionTree) method.getDefaultValue()
                );
                make.addComment(copy, Comment.create(
                        Comment.Style.JAVADOC, 
                        NOPOS, 
                        NOPOS, 
                        NOPOS, // to ensure indentation
                        "Comentario"),
                        true
                );
                workingCopy.rewrite(method, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddTwoEndLineCommments() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "        // TODO: Process the button click action. Return value is a navigation\n" +
            "        // case name where null will return to the same page.\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                String bodyText = "{ \n" +
                    "        // TODO: Process the button click action. Return value is a navigation\n" +
                    "        // case name where null will return to the same page.\n" +
                    "        return null; }";
                BlockTree block = make.createMethodBody(method, bodyText);
                workingCopy.rewrite(method.getBody(), block);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // issue #100829
    public void testCopyMethodWithCommments() throws Exception {
        testFile = new File(getWorkDir(), "Origin.java");
        TestUtilities.copyStringToFile(testFile, 
            "public class Origin {\n" +
            "    /**\n" +
            "     * comment\n" +
            "     * @return 1\n" +
            "     */\n" +
            "    int method() {\n" +
            "        // TODO: Process the button click action. Return value is a navigation\n" +
            "        // case name where null will return to the same page.\n" +
            "        return 1;\n" +
            "    }\n" +
            "}\n"
            );
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "import java.io.File;\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "import java.io.File;\n" +
            "public class Test {\n" +
            "\n" +
            "    /**\n" +
            "     * comment\n" +
            "     * @return 1\n" +
            "     */\n" +
            "    int method() {\n" +
            "        // TODO: Process the button click action. Return value is a navigation\n" +
            "        // case name where null will return to the same page.\n" +
            "        return 1;\n" +
            "    }\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) throws IOException {                
                wc.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = wc.getCompilationUnit();
                TreeMaker make = wc.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                
                TypeElement originClass = wc.getElements().getTypeElement("Origin");
                assertNotNull(originClass);
                
                ClassTree origClassTree = wc.getTrees().getTree(originClass);
                Tree method = origClassTree.getMembers().get(1);
                assertNotNull(method);
                method = GeneratorUtilities.get(wc).importComments(method, wc.getTrees().getPath(originClass).getCompilationUnit());
                wc.rewrite(clazz, make.addClassMember(clazz, method));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    

    public void testAddStatementWithEmptyLine() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "        // test\n" +
            "        int a;\n" +
            "        /*\n" +
            "         * Test\n" +
            "         * Test2\n" +
            "         */\n" +
            "        int b;\n" +
            "    }\n" +
            "\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                String bodyText = 
                        "{\n" +
                        "    \n" +
                        "    // test\n" +
                        "    int a;\n" +
                        "    \n" +
                        "    /*\n" +
                        "     * Test\n" +
                        "     * Test2\n" +
                        "     */\n" +
                        "    int b;\n" +
                        "}";
                BlockTree block = make.createMethodBody(method, bodyText);
                workingCopy.rewrite(method.getBody(), block);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddJavaDoctoEnum() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public enum Test {\n" +
            "\n" +
            "    SOME\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public enum Test {\n" +
            "\n" +
            "    SOME,\n" +
            "    /**\n" +
            "     * What's up?\n" +
            "     */\n" +
            "    THING\n" +
            "\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                int mods =  1<<14;
                ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());
                VariableTree var = make.Variable(modifiers, "THING", make.Identifier("Test"), null);
                int no = PositionEstimator.NOPOS;
                make.addComment(var, Comment.create(Style.JAVADOC, no, no, no, "What's up?\n"), true);
                ClassTree copy = make.addClassMember(clazz, var);
                workingCopy.rewrite(clazz, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /*
     * http://www.netbeans.org/issues/show_bug.cgi?id=113315
     */
    public void testAddJavaDoc113315() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "    }\n" +
            "\n" +
            "    /**\n" +
            "     * What's up?\n" +
            "     */\n" +
            "    void methoda() {\n" +
            "    }\n" +
            "\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = make.Method(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "methoda",
                        make.Identifier("void"),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        "{}",
                        null
                );
                int no = PositionEstimator.NOPOS;
                make.addComment(method, Comment.create(Style.JAVADOC, no, no, no, "What's up?\n"), true);
                ClassTree copy = make.addClassMember(clazz, method);
                workingCopy.rewrite(clazz, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /*
     * http://www.netbeans.org/issues/show_bug.cgi?id=100829
     */
    public void testCopyDoc100829_1() throws Exception {
        File secondFile = new File(getWorkDir(), "Test2.java");
        TestUtilities.copyStringToFile(secondFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test2 {\n" +
            "}\n"
            );
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "        // Test\n" +
            "        System.out.println(\"Slepitchka\");\n" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test2 {\n" +
            "\n" +
            "    void method() {\n" +
            "        // Test\n" +
            "        System.out.println(\"Slepitchka\");\n" +
            "    }\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(secondFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                Element e = workingCopy.getElements().getTypeElement("Test");
                ClassTree newClazz = (ClassTree) workingCopy.getTrees().getTree(e);
                CompilationUnitTree secondCut = workingCopy.getTrees().getPath(e).getCompilationUnit();
                newClazz = make.addClassMember(clazz, GeneratorUtilities.get(workingCopy).importComments(newClazz.getMembers().get(1), secondCut));
                workingCopy.rewrite(clazz, newClazz);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(secondFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /*
     * http://www.netbeans.org/issues/show_bug.cgi?id=100829
     */
    public void testCopyDoc100829_2() throws Exception {
        File secondFile = new File(getWorkDir(), "Test2.java");
        TestUtilities.copyStringToFile(secondFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test2 {\n" +
            "}\n"
            );
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    void method() {\n" +
            "        // Test\n" +
            "        int a = 0;\n" +
            "    }\n" +
            "\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test2 {\n" +
            "\n" +
            "    void method() {\n" +
            "        // Test\n" +
            "        int a = 0;\n" +
            "    }\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(secondFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                Element e = workingCopy.getElements().getTypeElement("Test");
                ClassTree newClazz = (ClassTree) workingCopy.getTrees().getTree(e);
                CompilationUnitTree secondCut = workingCopy.getTrees().getPath(e).getCompilationUnit();
                newClazz = make.addClassMember(
                        clazz, 
                        GeneratorUtilities.get(workingCopy).importComments(newClazz.getMembers().get(1), secondCut)
                );
                workingCopy.rewrite(clazz, newClazz);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(secondFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * http://www.netbeans.org/issues/show_bug.cgi?id=121898
     */
    public void testRemoveMethodWithComment() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "\n" +
            "\n" +
            "/*\n" +
            " * To change this template, choose Tools | Templates\n" +
            " * and open the template in the editor.\n" +
            " */\n" +
            "\n" +
            "package javaapplication11;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "\n" +
            "/**\n" +
            " *\n" +
            " * @author jp159440\n" +
            " */\n" +
            "public class Class1 extends ClassA{\n" +
            "                \n" +
            "    /**\n" +
            "     * a\n" +
            "     * @param x b\n" +
            "     * @return c\n" +
            "     * @throws java.io.IOException d\n" +
            "     */\n" +
            "    public int method(int x) throws IOException {\n" +
            "        \n" +
            "        return 1;        \n" +
            "    }\n" +
            "\n" +
            "    \n" +
            "    \n" +
            "}\n" +
            "\n"
            );
        String golden =
            "\n" +
            "\n" +
            "/*\n" +
            " * To change this template, choose Tools | Templates\n" +
            " * and open the template in the editor.\n" +
            " */\n" +
            "\n" +
            "package javaapplication11;\n" +
            "\n" +
            "import java.io.IOException;\n" +
            "\n" +
            "/**\n" +
            " *\n" +
            " * @author jp159440\n" +
            " */\n" +
            "public class Class1 extends ClassA{\n" +
            "}\n" +
            "\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                workingCopy.rewrite(clazz, make.removeClassMember(clazz, 1));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMethodFromString171043() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication11;\n" +
            "public class Class1 {\n" +
            "}\n");
        String golden =
            "package javaapplication11;\n" +
            "public class Class1 {\n\n" +
            "    public boolean equals(Object object) {\n" +
            "        // TODO: Warning - this method won't work in the case the id fields are not set\n" +
            "        if (!(object instanceof MyEntity)) {\n" +
            "            return false;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ModifiersTree empty = make.Modifiers(EnumSet.noneOf(Modifier.class));
                ModifiersTree pub = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                MethodTree mt = make.Method(pub,
                            "equals",
                            make.Type(workingCopy.getTypes().getPrimitiveType(TypeKind.BOOLEAN)),
                            Collections.<TypeParameterTree>emptyList(),
                            Collections.singletonList(make.Variable(empty, "object", make.QualIdent(workingCopy.getElements().getTypeElement("java.lang.Object")), null)),
                            Collections.<ExpressionTree>emptyList(),
                            "    {\n" +
                            "        // TODO: Warning - this method won't work in the case the id fields are not set\n" +
                            "        if (!(object instanceof MyEntity)) {\n" +
                            "            return false;\n" +
                            "        }\n" +
                            "    }",
                            null);
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, mt));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMethodFromString171043b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication11;\n" +
            "public class Class1 {\n" +
            "}\n");
        String golden =
            "package javaapplication11;\n" +
            "public class Class1 {\n\n" +
            "    public boolean equals(Object object) {\n" +
            "        // TODO: Warning - this method won't work in the case the id fields are not set\n" +
            "        if (!(object instanceof MyEntity)) {\n" +
            "            return false;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ModifiersTree empty = make.Modifiers(EnumSet.noneOf(Modifier.class));
                ModifiersTree pub = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                MethodTree mt = make.Method(pub,
                            "equals",
                            make.Type(workingCopy.getTypes().getPrimitiveType(TypeKind.BOOLEAN)),
                            Collections.<TypeParameterTree>emptyList(),
                            Collections.singletonList(make.Variable(empty, "object", make.QualIdent(workingCopy.getElements().getTypeElement("java.lang.Object")), null)),
                            Collections.<ExpressionTree>emptyList(),
                            "{" +
                            "// TODO: Warning - this method won't work in the case the id fields are not set\n" +
                            "if (!(object instanceof MyEntity)) {" +
                            "return false;" +
                            "}" +
                            "}",
                            null);
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, mt));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMoveMethod171345() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication11;\n" +
            "public class Class1 {\n" +
            "     public class Test {\n" +
            "         /*bflmpsvz*/\n" +
            "         private void test() {\n" +
            "             //byt\n" +
            "             System.err.println();\n" +
            "             //bydlet\n\n" +
            "             if (true) {\n" +
            "                 //obyvatel\n" +
            "             }\n" +
            "         }\n" +
            "         //Pribyslav\n" +
            "     }\n" +
            "}\n");
        String golden =
            "package javaapplication11;\n" +
            "public class Class1 {\n" +
            "     public class Test {\n" +
            "     }\n\n" +
            "     /*bflmpsvz*/\n" +
            "     private void test() {\n" +
            "         //byt\n" +
            "         System.err.println();\n" +
            "         //bydlet\n" +
            (CasualDiff.OLD_TREES_VERBATIM ? "         \n" : "") +//XXX
            "         if (true) {\n" +
            "             //obyvatel\n" +
            "         }\n" +
            "     }\n" +
            "     //Pribyslav\n" +
            "}\n";
        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                GeneratorUtilities gu = GeneratorUtilities.get(workingCopy);
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree inner = (ClassTree) topLevel.getMembers().get(1);
                MethodTree mt = (MethodTree) inner.getMembers().get(1);
                MethodTree nue = gu.importComments(mt, cut);
                workingCopy.rewrite(topLevel, make.addClassMember(topLevel, nue));
                workingCopy.rewrite(inner, make.removeClassMember(inner, mt));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testDuplicatedComment171262() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package test;\n" +
            "import java.io.File;\n" +
            "import java.io.FileInputStream;\n" +
            "import java.io.FileNotFoundException;\n" +
            "public abstract class Test {\n" +
            "    public Test() {\n" +
            "        //pribytek\n" +
            "        FileInputStream fis = new FileInputStream(new File(\"\"));\n" +
            "    }\n" +
            "}\n");
        String golden = "package test;\n" +
                        "import java.io.File;\n" +
                        "import java.io.FileInputStream;\n" +
                        "import java.io.FileNotFoundException;\n" +
                        "public abstract class Test {\n" +
                        "    public Test() {\n" +
                        "        try {\n" +
                        "            //pribytek\n" +
                        "            FileInputStream fis = new FileInputStream(new File(\"\"));\n" +
                        "        } catch (FileNotFoundException ex) {\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                GeneratorUtilities gu = GeneratorUtilities.get(workingCopy);
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree constr = (MethodTree) topLevel.getMembers().get(0);
                StatementTree toSurround = constr.getBody().getStatements().get(1);
                toSurround = gu.importComments(toSurround, cut);
                ModifiersTree mt = make.Modifiers(EnumSet.noneOf(Modifier.class));
                VariableTree vt = make.Variable(mt, "ex", make.Identifier("FileNotFoundException"), null);
                BlockTree empty = make.Block(Collections.<StatementTree>emptyList(), false);
                TryTree tt = make.Try(make.Block(Collections.singletonList(toSurround), false), Collections.singletonList(make.Catch(vt, empty)), null);
                workingCopy.rewrite(toSurround, tt);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testLostJavadoc172386() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package test;\n" +
            "import java.io.File;\n" +
            "import java.io.FileInputStream;\n" +
            "import java.io.FileNotFoundException;\n" +
            "public abstract class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    public void method() {\n" +
            "        \n" +
            "        new Runnable() {\n" +
            "\n" +
            "            /**\n" +
            "             * x\n" +
            "             */\n" +
            "            int x;\n" +
            "            \n" +
            "            /**\n" +
            "             * f\n" +
            "             */\n" +
            "            int f;\n" +
            "\n" +
            "            /**\n" +
            "             * run\n" +
            "             */\n" +
            "            public void run() {\n" +
            "            }\n" +
            "        };\n" +
            "    }\n" +
            "}\n");
        String golden = "package test;\n" +
                        "import java.io.File;\n" +
                        "import java.io.FileInputStream;\n" +
                        "import java.io.FileNotFoundException;\n" +
                        "public abstract class Test {\n" +
                        "    public Test() {\n" +
                        "    }\n" +
                        "    public void method() {\n" +
                        "    }\n" +
                        "\n" +
                        "    class R {\n" +
                        "\n" +
                        "        /**\n" +
                        "         * x\n" +
                        "         */\n" +
                        "        int x;\n" +
//                        "        \n" +
                        "        /**\n" +
                        "         * f\n" +
                        "         */\n" +
                        "        int f;\n" +
                        "\n" +
                        "        /**\n" +
                        "         * run\n" +
                        "         */\n" +
                        "        public void run() {\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                GeneratorUtilities gu = GeneratorUtilities.get(workingCopy);
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) topLevel.getMembers().get(1);
                NewClassTree toMove = (NewClassTree) ((ExpressionStatementTree) method.getBody().getStatements().get(0)).getExpression();
                ModifiersTree mt = make.Modifiers(EnumSet.noneOf(Modifier.class));
                ClassTree toMoveClass = toMove.getClassBody();
                toMoveClass = gu.importComments(toMoveClass, cut);
                ClassTree nue = make.Class(mt, "R", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), toMoveClass.getMembers());

                workingCopy.rewrite(method.getBody(), make.removeBlockStatement(method.getBody(), 0));
                workingCopy.rewrite(topLevel, make.addClassMember(topLevel, nue));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testMoveMethod171345b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        File class1 = new File(getWorkDir(), "Class1.java");
        TestUtilities.copyStringToFile(testFile,
            "public class Test {\n" +
            "}\n");
        TestUtilities.copyStringToFile(class1,
            "public class Class1 {\n" +
            "    public class Test {\n" +
            "         /**\n" +
            "         * @param args the command line arguments\n" +
            "         */\n" +
            "        public void ren3(String[] args) {\n" +
            "            //inline coment\n" +
            "            int aaa;\n" +
            "            /*\n" +
            "             * block comment\n" +
            "             */\n" +
            "            System.out.println(\"\"); //comment 2\n" +
            "        }\n" +
            "    }\n" +
            "}\n");

        String golden =
            "public class Test {\n\n" +
            "    /**\n" +
            "     * @param args the command line arguments\n" +
            "     */\n" +
            "    public void ren3(String[] args) {\n" +
            "        //inline coment\n" +
            "        int aaa;\n" +
            "        /*\n" +
            "         * block comment\n" +
            "         */\n" +
            "        System.out.println(\"\"); //comment 2\n" +
            "    }\n" +
            "}\n";
        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TypeElement t = workingCopy.getElements().getTypeElement("Class1.Test");
                assertNotNull(t);
                ExecutableElement ee = (ExecutableElement) t.getEnclosedElements().get(1);
                TreePath method = workingCopy.getTrees().getPath(ee);
                assertNotNull(method);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                GeneratorUtilities gu = GeneratorUtilities.get(workingCopy);
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree nue = (MethodTree) gu.importComments(method.getLeaf(), method.getCompilationUnit());
                workingCopy.rewrite(topLevel, make.addClassMember(topLevel, nue));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testDuplicatedComment170213a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package test;\n" +
            "import java.io.File;\n" +
            "import java.io.FileInputStream;\n" +
            "import java.io.FileNotFoundException;\n" +
            "public abstract class Test {\n" +
            "    public void test() {\n" +
            "        //t1\n" +
            "        String allianceString = new String(\"[]\");\n" +
            "        //t2\n" +
            "        allianceString += \"\";\n" +
            "        //t3\n" +
            "    }\n" +
            "}\n");
        String golden = "package test;\n" +
                        "import java.io.File;\n" +
                        "import java.io.FileInputStream;\n" +
                        "import java.io.FileNotFoundException;\n" +
                        "public abstract class Test {\n" +
                        "    public void test() {\n" +
                        "        //t1\n" +
                        "        //t2\n" +
                        "        //t3\n" +
                        "        \n" +
                        "        name();\n" +
                        "    }\n" +
                        "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                GeneratorUtilities gu = GeneratorUtilities.get(workingCopy);
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) topLevel.getMembers().get(1);
                BlockTree bt = gu.importComments(method.getBody(), cut);
                ExpressionStatementTree est = make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier("name"), Collections.<ExpressionTree>emptyList()));
                BlockTree nue = make.Block(Collections.singletonList(est), false);
                workingCopy.rewrite(bt, nue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testDuplicatedComment170213b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package test;\n" +
            "import java.io.File;\n" +
            "import java.io.FileInputStream;\n" +
            "import java.io.FileNotFoundException;\n" +
            "public abstract class Test {\n" +
            "    public void test() {\n" +
            "        //t1\n" +
            "        String allianceString = new String(\"[]\");\n" +
            "        //t2\n" +
            "        allianceString += \"\";\n" +
            "        //t3\n" +
            "        //t4\n" +
            "    }\n" +
            "}\n");
        String golden = "package test;\n" +
                        "import java.io.File;\n" +
                        "import java.io.FileInputStream;\n" +
                        "import java.io.FileNotFoundException;\n" +
                        "public abstract class Test {\n" +
                        "    public void test() {\n" +
                        "        //t1\n" +
                        "        //t2\n" +
                        "        //t3\n" +
                        "        //t4\n" +
                        "        \n" +
                        "        name();\n" +
                        "    }\n" +
                        "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                GeneratorUtilities gu = GeneratorUtilities.get(workingCopy);
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) topLevel.getMembers().get(1);
                BlockTree bt = gu.importComments(method.getBody(), cut);
                ExpressionStatementTree est = make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier("name"), Collections.<ExpressionTree>emptyList()));
                BlockTree nue = make.Block(Collections.singletonList(est), false);
                workingCopy.rewrite(bt, nue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testDuplicatedComment170213c() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package test;\n" +
            "import java.io.File;\n" +
            "import java.io.FileInputStream;\n" +
            "import java.io.FileNotFoundException;\n" +
            "public abstract class Test {\n" +
            "    public void test() {\n" +
            "        //t1\n" +
            "        String allianceString = new String(\"[]\");\n" +
            "        //t2\n" +
            "        allianceString += \"\";//test1\n" +
            "        //t3\n" +
            "        //t4\n" +
            "    }\n" +
            "}\n");
        String golden = "package test;\n" +
                        "import java.io.File;\n" +
                        "import java.io.FileInputStream;\n" +
                        "import java.io.FileNotFoundException;\n" +
                        "public abstract class Test {\n" +
                        "    public void test() {\n" +
                        "        //t1\n" +
                        "        //t2\n" +
                        "        //test1\n" +
                        "        //t3\n" +
                        "        //t4\n" +
                        "        \n" +
                        "        name();\n" +
                        "    }\n" +
                        "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                GeneratorUtilities gu = GeneratorUtilities.get(workingCopy);
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) topLevel.getMembers().get(1);
                BlockTree bt = gu.importComments(method.getBody(), cut);
                ExpressionStatementTree est = make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier("name"), Collections.<ExpressionTree>emptyList()));
                BlockTree nue = make.Block(Collections.singletonList(est), false);
                workingCopy.rewrite(bt, nue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testDuplicatedComment170213d() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package test;\n" +
            "import java.io.File;\n" +
            "import java.io.FileInputStream;\n" +
            "import java.io.FileNotFoundException;\n" +
            "public abstract class Test {\n" +
            "    public void test() {\n" +
            "        //nabytek\n" +
            "        FileInputStream fis = new FileInputStream(new File(\"\"));//NOI18N\n" +
            "        //foo\n" +
            "        \n" +
            "        fis.read();\n" +
            "    }\n" +
            "}\n");
        String golden = "package test;\n" +
                        "import java.io.File;\n" +
                        "import java.io.FileInputStream;\n" +
                        "import java.io.FileNotFoundException;\n" +
                        "public abstract class Test {\n" +
                        "    public void test() {\n" +
                        "        //nabytek\n" +
                        "        FileInputStream fis;\n" +
                        "        fis = new FileInputStream(new File(\"\")); //NOI18N\n" +
                        "        //foo\n" +
                        "        \n" +
                        "        fis.read();\n" +
                        "    }\n" +
                        "}\n";

        JavaSource src = JavaSource.forFileObject(FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();

                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitVariable(VariableTree node, Void p) {
                        GeneratorUtilities gu = GeneratorUtilities.get(workingCopy);
                        gu.importComments(node, workingCopy.getCompilationUnit());
                        StatementTree assignment = make.ExpressionStatement(make.Assignment(make.Identifier(node.getName()), node.getInitializer()));
                        StatementTree declaration = make.Variable(node.getModifiers(), node.getName(), node.getType(), null);//XXX: mask out final

                        gu.copyComments(node, declaration, true);
                        gu.copyComments(node, assignment, false);
                        
                        List<StatementTree> nueStatements = new LinkedList<StatementTree>();

                        BlockTree bt = (BlockTree) getCurrentPath().getParentPath().getLeaf();
                        int index = bt.getStatements().indexOf(node);

                        assertTrue(index != (-1));

                        nueStatements.addAll(bt.getStatements().subList(0, index));
                        nueStatements.add(declaration);
                        nueStatements.add(assignment);
                        nueStatements.addAll(bt.getStatements().subList(index + 1, bt.getStatements().size()));

                        workingCopy.rewrite(bt, make.Block(nueStatements, false));

                        return super.visitVariable(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testComments175889a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "/**test\n" +
            " * test\n" +
            " */\n" +
            "package hierbas.del.litoral;\n" +
            "\n" +
            "/**test\n" +
            " * test\n" +
            " */\n" +
            "public class Test {\n" +
            "}\n");
        String golden =
            "/**test\n" +
            " * test\n" +
            " */\n" +
            "package hierbas.del.litoral;\n" +
            "\n" +
            "/**test\n" +
            " * test\n" +
            " */\n" +
            "public class Test {\n\n" +
            "    public void test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = make.Method(make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                                                "test",
                                                make.Type(workingCopy.getTypes().getNoType(TypeKind.VOID)),
                                                Collections.<TypeParameterTree>emptyList(),
                                                Collections.<VariableTree>emptyList(),
                                                Collections.<ExpressionTree>emptyList(),
                                                "{}",
                                                null);
                String commentText = "TESTTTT";
                Comment comment = Comment.create(Comment.Style.JAVADOC, 0, 0, 0, commentText);
                make.addComment(clazz, comment, true);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, method));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddBraces() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    private void test() {\n" +
            "        if (true) //NOI18N\n" +
            "            //a\n" +
            "            System.out.println(0); //b\n" +
            "            //c\n" +
            "        else\n" +
            "            //NOI18N\n" +
            "            //d\n" +
            "            System.out.println(1);//e\n" +
            "            //f\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    private void test() {\n" +
            "        if (true) { //NOI18N\n" +
            "            //a\n" +
            "            System.out.println(0); //b\n" +
            "            //c\n" +
            "        } else {\n" +
            "            //NOI18N\n" +
            "            //d\n" +
            "            System.out.println(1);//e\n" +
            "            //f\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final CompilationUnitTree cut = workingCopy.getCompilationUnit();
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitIf(IfTree node, Void p) {
                        node = GeneratorUtilities.get(workingCopy).importComments(node, cut);
                        workingCopy.rewrite(node.getThenStatement(), make.Block(Collections.singletonList(node.getThenStatement()), false));
                        workingCopy.rewrite(node.getElseStatement(), make.Block(Collections.singletonList(node.getElseStatement()), false));
                        return super.visitIf(node, p);
                    }
                }.scan(cut, null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    // modification - no comment on a separate line
    public void testAddBraces241836() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    private void test() {\n" +
            "        if (true) //NOI18N\n" +
            "            //a\n" +
            "            System.out.println(0); //b\n" +
            "            //c\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    private void test() {\n" +
            "        if (true) { //NOI18N\n" +
            "            //a\n" +
            "            System.out.println(0); //b\n" +
            "            //c\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final CompilationUnitTree cut = workingCopy.getCompilationUnit();
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitIf(IfTree node, Void p) {
                        node = GeneratorUtilities.get(workingCopy).importComments(node, cut);
                        workingCopy.rewrite(node.getThenStatement(), make.Block(Collections.singletonList(node.getThenStatement()), false));
                        return super.visitIf(node, p);
                    }
                }.scan(cut, null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testComments175889b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "/**test\n" +
            " * test\n" +
            " */\n" +
            "package hierbas.del.litoral;\n" +
            "\n" +
            "/**test\n" +
            " * test\n" +
            " */\n" +
            "public class Test {\n" +
            "}\n");
        String golden =
            "/**test\n" +
            " * test\n" +
            " */\n" +
            "package hierbas.del.litoral;\n" +
            "\n" +
            "/**test\n" +
            " * test\n" +
            " */\n" +
            "public class Test {\n\n" +
            "    public void test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = make.Method(make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                                                "test",
                                                make.Type(workingCopy.getTypes().getNoType(TypeKind.VOID)),
                                                Collections.<TypeParameterTree>emptyList(),
                                                Collections.<VariableTree>emptyList(),
                                                Collections.<ExpressionTree>emptyList(),
                                                "{}",
                                                null);
                String commentText = "TESTTTT";
                Comment comment = Comment.create(Comment.Style.JAVADOC, -1, -1, -1, commentText);
                make.addComment(clazz, comment, true);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, method));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveComment186017() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "/**test\n" +
            " * test\n" +
            " */\n" +
            "public class Test {\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n\n" +
            "public class Test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                GeneratorUtilities.get(workingCopy).importComments(clazz, cut);
                ClassTree newClazz = make.setLabel(clazz, clazz.getSimpleName());
                make.removeComment(newClazz, 0, true);
                workingCopy.rewrite(clazz, newClazz);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveComment186176a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    /*test1\n" +
            "     * test\n" +
            "     */\n" +
            "    /*test2\n" +
            "     * test\n" +
            "     */\n" +
            "    private void test() {} \n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    /*test1\n" +
            "     * test\n" +
            "     */\n" +
            "    private void test() {} \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree mt = (MethodTree) clazz.getMembers().get(1);
//                GeneratorUtilities.get(workingCopy).importComments(clazz, cut);
                MethodTree newMethod = make.setLabel(mt, mt.getName());
                make.removeComment(newMethod, 1, true);
                workingCopy.rewrite(mt, newMethod);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveComment186176b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    /*test1\n" +
            "     * test\n" +
            "     */\n" +
            "    private void test() {} \n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    /*test2*/\n" +
            "    private void test() {} \n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree mt = (MethodTree) clazz.getMembers().get(1);
//                GeneratorUtilities.get(workingCopy).importComments(clazz, cut);
                MethodTree newMethod = make.setLabel(mt, mt.getName());
                make.removeComment(newMethod, 0, true);
                make.addComment(newMethod, Comment.create(Style.BLOCK, "/*test2*/"), true);
                workingCopy.rewrite(mt, newMethod);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveDocCommentAndAddAnnotation186923() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    // start comment\n" +
            "    private void op() {\n" +
            "    }// end comment\n" +
            "    // start comment\n" +
            "    private String name;\n" +
            "    // end comment\n" +
            "\n" +
            "    /**\n" +
            "     * test\n" +
            "     */\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    // start comment\n" +
            "    @Deprecated\n" +
            "    private void op() {\n" +
            "    }// end comment\n" +
            "    // start comment\n" +
            "    private String name;\n" +
            "    // end comment\n" +
            "\n" +
            "    @Deprecated\n" +
            "    private void test() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker tm = wc.getTreeMaker();
                final TreeUtilities tu = wc.getTreeUtilities();
                wc.getCompilationUnit().accept(new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(MethodTree mt, Void p) {
                        Tree nt = tm.setLabel(mt, mt.getName());
                        final List<Comment> comments = tu.getComments(mt, true);
                        int size = comments.size();
                        if (size > 0) {
                            wc.rewrite(mt.getModifiers(), tm.addModifiersAnnotation(
                                mt.getModifiers(), tm.Annotation(tm.QualIdent(
                                wc.getElements().getTypeElement(Deprecated.class.
                                getName())), Collections.<ExpressionTree>emptyList())));
                            for (int i = size - 1; i >= 0; i--) {
                                if (comments.get(i).isDocComment()) {
                                    tm.removeComment(nt, i, true);
                                }
                            }
                            wc.rewrite(mt, nt);
                        }
                        return super.visitMethod(mt, p);
                    }

                }, null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test186923b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    private void op() {\n" +
            "        String s1;/*\n" +
            "*/      String s2;\n" +
            "    }\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    private void op() {\n" +
            "        String s1;/*\n" +
            "*/      s1 = 0;\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker tm = wc.getTreeMaker();
                ClassTree clazz = (ClassTree) wc.getCompilationUnit().getTypeDecls().get(0);
                MethodTree mt = (MethodTree) clazz.getMembers().get(1);
                StatementTree orig = mt.getBody().getStatements().get(1);
                ExpressionStatementTree nue = tm.ExpressionStatement(tm.Assignment(tm.Identifier("s1"), tm.Literal(0)));
                GeneratorUtilities.get(wc).copyComments(orig, nue, true);
                GeneratorUtilities.get(wc).copyComments(orig, nue, false);
                wc.rewrite(orig, nue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testCommentPrinted195048() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n\n" +
            "    /**test*/\n" +
            "    private String t() {\n" +
            "        String s;\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker tm = wc.getTreeMaker();
                MethodTree nue = tm.Method(tm.Modifiers(EnumSet.of(Modifier.PRIVATE)),
                                           "t",
                                           tm.MemberSelect(tm.MemberSelect(tm.Identifier("java"), "lang"), "String"),
                                           Collections.<TypeParameterTree>emptyList(),
                                           Collections.<VariableTree>emptyList(),
                                           Collections.<ExpressionTree>emptyList(),
                                           "{java.lang.String s;}",
                                           null);
                GeneratorUtilities gu = GeneratorUtilities.get(wc);

                tm.addComment(nue, Comment.create(Style.JAVADOC, -2, -2, -2, "/**test*/"), true);
                nue = gu.importFQNs(nue);

                ClassTree clazz = (ClassTree) wc.getCompilationUnit().getTypeDecls().get(0);

                wc.rewrite(clazz, gu.insertClassMember(clazz, nue));
            }

        };
        src.runModificationTask(task).commit();
        //GeneratorUtilities.insertClassMember opens the Document:
        DataObject d = DataObject.find(FileUtil.toFileObject(testFile));
        EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
        ec.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testConstructorCommentsOnClassRename() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    //comment-pref\n" +
            "    public Test() {\n" +
            "    }\n" +
            "    //comment-suff\n" +
            "}\n");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Renamed {\n" +
            "    //comment-pref\n" +
            "    public Renamed() {\n" +
            "    }\n" +
            "    //comment-suff\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy wc) throws IOException {
                wc.toPhase(JavaSource.Phase.RESOLVED);
                final TreeMaker make = wc.getTreeMaker();
                ClassTree clazz = (ClassTree) wc.getCompilationUnit().getTypeDecls().get(0);
                ClassTree renamed = make.setLabel(clazz, "Renamed");
                assertFalse(wc.getTreeUtilities().getComments(renamed.getMembers().get(0), true).isEmpty());
                wc.rewrite(clazz, renamed);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test209357a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        final String content = "package org.netbeans.modules.php.apigen.ui.customizer;\n" +
                               "\n" +
                               "final class ApiGenPanel {\n" +
                               "    private void initComponents() {\n" +
                               "        targetButton.addActionListener(new java.awt.event.ActionListener() {\n" +
                               "            public void actionPerformed(java.awt.event.ActionEvent evt) {\n" +
                               "            }\n" +
                               "        });\n" +
                               "        configButton.addActionListener(new java.awt.event.ActionListener() {\n" +
                               "            public void actionPerformed(java.awt.event.ActionEvent evt) {\n" +
                               "            }\n" +
                               "        });\n" +
                               "\n" +
                               "    }\n" +
                               "\n" +
                               "    private javax.swing.JButton configButton;\n" +
                               "    private javax.swing.JButton targetButton;\n" +
                               "\n" +
                               "}\n";
        final String golden  = "package org.netbeans.modules.php.apigen.ui.customizer;\n" +
                               "\n" +
                               "final class ApiGenPanel {\n" +
                               "    private void initComponents() {\n" +
                               "        System.err.println(1); //NOI18n\n" +
                               "        configButton.addActionListener(new java.awt.event.ActionListener() {\n" +
                               "            public void actionPerformed(java.awt.event.ActionEvent evt) {\n" +
                               "            }\n" +
                               "        });\n" +
                               "\n" +
                               "    }\n" +
                               "\n" +
                               "    private javax.swing.JButton configButton;\n" +
                               "    private javax.swing.JButton targetButton;\n" +
                               "\n" +
                               "}\n";

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, content);

        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);

                MethodTree m = (MethodTree) clazz.getMembers().get(1);
                StatementTree se = workingCopy.getTreeUtilities().parseStatement("System.err.println(1);", new SourcePositions[1]);

                //XXX: need to set the comment with RelativePosition.INLINE, which cannot currently be done through API:
                CommentHandlerService commentHandler = CommentHandlerService.instance(JavaSourceAccessor.getINSTANCE().getJavacTask(workingCopy).getContext());
                CommentSet set = commentHandler.getComments(se);

                assertNotNull(set);
                set.addComment(RelativePosition.INLINE, Comment.create(Style.LINE, "NOI18N"));

                workingCopy.rewrite(m.getBody().getStatements().get(0), se);
            }
        };
        testSource.runModificationTask(task).commit();
        DataObject d = DataObject.find(FileUtil.toFileObject(testFile));
        EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
        ec.saveDocument();

        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(res, 2, res.split("new").length);
    }

    public void test209357b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        final String content = "package org.netbeans.modules.php.apigen.ui.customizer;\n" +
                               "\n" +
                               "final class ApiGenPanel {\n" +
                               "    private void initComponents() {\n" +
                               "        java.lang.System.err.println(\"a\"); //NOI18N\n" +
                               "        java.lang.System.err.println(\"b\"); //NOI18N\n" +
                               "        java.lang.System.err.println(\"c\"); //NOI18N\n" +
                               "        java.lang.System.err.println(\"d\"); //NOI18N\n" +
                               "    }\n" +
                               "}\n";
        final String golden  = "package org.netbeans.modules.php.apigen.ui.customizer;\n" +
                               "\n" +
                               "final class ApiGenPanel {\n" +
                               "    private void initComponents() {\n" +
                               "        System.err.println(\"a\"); //NOI18N\n" +
                               "        System.err.println(\"b\"); //NOI18N\n" +
                               "        System.err.println(\"c\"); //NOI18N\n" +
                               "        System.err.println(\"d\"); //NOI18N\n" +
                               "    }\n" +
                               "}\n";

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, content);

        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                GeneratorUtilities gu = GeneratorUtilities.get(workingCopy);
                workingCopy.rewrite(clazz, gu.importFQNs(gu.importComments(clazz, workingCopy.getCompilationUnit())));
            }
        };
        testSource.runModificationTask(task).commit();
        DataObject d = DataObject.find(FileUtil.toFileObject(testFile));
        EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
        ec.saveDocument();

        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test210760() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        final String content =  "package test;\n" +
                                "import javax.swing.*;\n" +
                                "public class Test {\n" +
                                "    private void initComponents() {\n" +
                                "        jButton1.getAccessibleContext().setAccessibleName(test.Test.getMessage(Test.class, \"NewJPanel1.jButton1.AccessibleContext.accessibleName\")); // NOI18N\n" +
                                "        jButton1.getAccessibleContext().setAccessibleDescription(test.Test.NbBundle.getMessage(Test.class, \"NewJPanel1.jButton1.AccessibleContext.accessibleDescription\")); // NOI18N\n" +
                                "    }\n" +
                                "    private javax.swing.JButton jButton1;\n" +
                                "    private String getMessage(Class c, String k) { return k; } \n" +
                                "}\n";
        final String golden =  "package test;\n" +
                                "import javax.swing.*;\n" +
                                "public class Test {\n" +
                                "    private void initComponents() {\n" +
                                "        jButton1.getAccessibleContext().setAccessibleName(Test.getMessage(Test.class, \"NewJPanel1.jButton1.AccessibleContext.accessibleName\")); // NOI18N\n" +
                                "        jButton1.getAccessibleContext().setAccessibleDescription(Test.NbBundle.getMessage(Test.class, \"NewJPanel1.jButton1.AccessibleContext.accessibleDescription\")); // NOI18N\n" +
                                "    }\n" +
                                "    private JButton jButton1;\n" +
                                "    private String getMessage(Class c, String k) { return k; } \n" +
                                "}\n";

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, content);

        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                GeneratorUtilities gu = GeneratorUtilities.get(workingCopy);
                workingCopy.rewrite(clazz, gu.importFQNs(gu.importComments(clazz, workingCopy.getCompilationUnit())));
            }
        };
        testSource.runModificationTask(task).commit();
        DataObject d = DataObject.find(FileUtil.toFileObject(testFile));
        EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
        ec.saveDocument();

        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test212936() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        final String content =  "package test;\n" +
                                "public class Test {\n" +
                                "    private Test(String str) {\n" +
                                "        str.toString();\n" +
                                "//if (...);" +
                                "    }\n" +
                                "}\n";
        final String golden =  "package test;\n" +
                                "public class Test {\n" +
                                "    private Test(String str) {\n" +
                                "        String nue = str.toString();\n" +
                                "//if (...);" +
                                "    }\n" +
                                "}\n";

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, content);

        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree constr = (MethodTree) clazz.getMembers().get(0);
                ExpressionStatementTree inv = (ExpressionStatementTree) constr.getBody().getStatements().get(1);
                final TreeMaker make = workingCopy.getTreeMaker();
                VariableTree newVar = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "nue", make.Type("java.lang.String"), inv.getExpression());
                inv = GeneratorUtilities.get(workingCopy).importComments(inv, workingCopy.getCompilationUnit());
                GeneratorUtilities.get(workingCopy).copyComments(inv, newVar, false);
                workingCopy.rewrite(inv, newVar);
            }
        };
        testSource.runModificationTask(task).commit();
        DataObject d = DataObject.find(FileUtil.toFileObject(testFile));
        EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
        ec.saveDocument();

        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test206034() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        final String content =  "package test;\n" +
                                "public class Test {\n" +
                                "}\n";
        final String golden =  "package test;\n" +
                                "public class Test {\n" +
                                "    /* This is a comment. */\n" +
                                "    static {\n" +
                                "        ;\n" +
                                "    }\n" +
                                "}\n";

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, content);

        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                BlockTree newBlock = make.Block(Collections.singletonList(make.EmptyStatement()), true);
                Comment comment = Comment.create(Comment.Style.BLOCK, "This is a comment.");

                make.addComment(newBlock, comment, true);

                // updates the class with the code block
                ClassTree modifiedClazz = make.addClassMember(clazz, newBlock);
                workingCopy.rewrite(clazz, modifiedClazz);
            }
        };

        testSource.runModificationTask(task).commit();
        DataObject d = DataObject.find(FileUtil.toFileObject(testFile));
        EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
        ec.saveDocument();

        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return"";
    }

}
