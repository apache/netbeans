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
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import javax.lang.model.element.Modifier;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyle.WrapStyle;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.save.Reformatter;
import org.netbeans.modules.java.ui.FmtOptions;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/** Also run SuppressWarningsFixerTest in java.hints
 *
 * @author Pavel Flaska
 */
public class AnnotationTest extends GeneratorTestBase {

    /** Creates a new instance of ClassMemberTest */
    public AnnotationTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(AnnotationTest.class);
//        suite.addTest(new AnnotationTest("testRemoveAnnotationEnumConstant"));
//        suite.addTest(new ConstructorRenameTest("testAnnotationRename2"));
//        suite.addTest(new ConstructorRenameTest("testClassToAnnotation"));
//        suite.addTest(new ConstructorRenameTest("testAddDefaultValue"));
//        suite.addTest(new ConstructorRenameTest("testRemoveDefaultValue"));
//        suite.addTest(new AnnotationTest("testAddArrayInitializer1"));
//        suite.addTest(new AnnotationTest("testAddArrayInitializer2"));
//        suite.addTest(new AnnotationTest("testRenameAttributeFromDefault174552b"));
//        suite.addTest(new AnnotationTest("testAnnotationWrapping1"));
//        suite.addTest(new AnnotationTest("testAnnotationWrapping2"));
        return suite;
    }
    

    public void testAnnotationRename1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public @interface Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public @interface Foo {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree copy = make.setLabel((ClassTree) typeDecl, "Foo");
                        workingCopy.rewrite(typeDecl, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAnnotationRename2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public @interface Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public @interface Foo {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree ct = (ClassTree) typeDecl;
                        ClassTree copy = make.AnnotationType(ct.getModifiers(),"Foo", ct.getMembers());
                        //System.err.println(copy.toString());
                        workingCopy.rewrite(typeDecl, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void XtestClassToAnnotation() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public @interface Foo {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree ct = (ClassTree) typeDecl;
                        ClassTree copy = make.AnnotationType(ct.getModifiers(),"Foo", ct.getMembers());
                        //System.err.println(copy.toString());
                        workingCopy.rewrite(typeDecl, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    
    public void testAddDefaultValue() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package aloisovo;\n" +
            "\n" +
            "public @interface Traktor {\n" +
            "    public void zetorBrno(); \n" +
            "}\n" +
            "enum A {E}");
        String golden =
            "package aloisovo;\n" +
            "\n" +
            "public @interface Traktor {\n" +
            "    public void zetorBrno() default A.E; \n" +
            "}\n" +
            "enum A {E}";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(MethodTree node, Void p) {
                        workingCopy.rewrite(node, workingCopy.getTreeMaker().setInitialValue(node, workingCopy.getTreeMaker().Identifier("A.E")));
                        return super.visitMethod(node, p);
                    }
                }.scan(cut, null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveDefaultValue() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package aloisovo;\n" +
            "\n" +
            "public @interface Traktor {\n" +
            "    public void zetorBrno() default A.E; \n" +
            "}\n" +
            "enum A {E}");
        String golden =
            "package aloisovo;\n" +
            "\n" +
            "public @interface Traktor {\n" +
            "    public void zetorBrno(); \n" +
            "}\n" +
            "enum A {E}";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitMethod(MethodTree node, Void p) {
                        workingCopy.rewrite(node, workingCopy.getTreeMaker().setInitialValue(node, null));
                        return super.visitMethod(node, p);
                    }
                }.scan(cut, null);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddAnnotation123745() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n" +
            "@interface A {\n" +
            "    public String test1();\n" +
            "    public int test2();\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@A(test1 = \"A\", test2 = 42)\n" +
            "public class Test {\n" +
            "}\n" +
            "@interface A {\n" +
            "    public String test1();\n" +
            "    public int test2();\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);
                
                ExpressionTree attr1 = make.Assignment(make.Identifier("test1"), make.Literal("A"));
                ExpressionTree attr2 = make.Assignment(make.Identifier("test2"), make.Literal(42));
                AnnotationTree at = make.Annotation(make.Identifier("A"), Arrays.asList(attr1, attr2));
                ModifiersTree mt = make.Modifiers(ct.getModifiers(), Arrays.asList(at));
                
                workingCopy.rewrite(ct.getModifiers(), mt);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddAnnotation123745b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n" +
            "@interface A {\n" +
            "    public String test1();\n" +
            "    public int test2();\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@A(test1 = \"A\", test2 = 42)\n" +
            "public class Test {\n" +
            "}\n" +
            "@interface A {\n" +
            "    public String test1();\n" +
            "    public int test2();\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);
                
                ExpressionTree attr1 = workingCopy.getTreeUtilities().parseExpression("test1=\"A\"", new SourcePositions[1]);
                ExpressionTree attr2 = workingCopy.getTreeUtilities().parseExpression("test2=42", new SourcePositions[1]);
                AnnotationTree at = make.Annotation(make.Identifier("A"), Arrays.asList(attr1, attr2));
                ModifiersTree mt = make.Modifiers(ct.getModifiers(), Arrays.asList(at));
                
                workingCopy.rewrite(ct.getModifiers(), mt);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddArrayInitializer1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import hierbas.del.litoral.Test.A;\n" +
            "\n" +
            "@A(test={\"first\"})" +
            "public class Test {\n" +
            "    @interface A {\n" +
            "        public String[] test();\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import hierbas.del.litoral.Test.A;\n" +
            "\n" +
            "@A(test={\"first\", \"something\"})" +
            "public class Test {\n" +
            "    @interface A {\n" +
            "        public String[] test();\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);
                AnnotationTree an = ct.getModifiers().getAnnotations().get(0);
                AssignmentTree testArg = (AssignmentTree) an.getArguments().get(0);
                NewArrayTree nat = (NewArrayTree) testArg.getExpression();
                NewArrayTree nueNat = make.addNewArrayInitializer(nat, make.Literal("something"));

                workingCopy.rewrite(nat, nueNat);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddArrayInitializer2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import hierbas.del.litoral.Test.A;\n" +
            "\n" +
            "@A(test={})" +
            "public class Test {\n" +
            "    @interface A {\n" +
            "        public String[] test();\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import hierbas.del.litoral.Test.A;\n" +
            "\n" +
            "@A(test={\"something\"})" +
            "public class Test {\n" +
            "    @interface A {\n" +
            "        public String[] test();\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);
                AnnotationTree an = ct.getModifiers().getAnnotations().get(0);
                AssignmentTree testArg = (AssignmentTree) an.getArguments().get(0);
                NewArrayTree nat = (NewArrayTree) testArg.getExpression();
                NewArrayTree nueNat = make.addNewArrayInitializer(nat, make.Literal("something"));

                workingCopy.rewrite(nat, nueNat);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddAttributeWithDefaultValue() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package aloisovo;\n" +
            "\n" +
            "public @interface Traktor {\n" +
            "}\n");
        String golden =
            "package aloisovo;\n" +
            "\n" +
            "public @interface Traktor {\n" + "\n" +
            "    public String zetorBrno() default \"\";\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);
                TreeMaker make = workingCopy.getTreeMaker();
                MethodTree method = make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                                                "zetorBrno",
                                                make.QualIdent(workingCopy.getElements().getTypeElement("java.lang.String")),
                                                Collections.<TypeParameterTree>emptyList(),
                                                Collections.<VariableTree>emptyList(),
                                                Collections.<ExpressionTree>emptyList(),
                                                (BlockTree) null,
                                                make.Literal(""));
                workingCopy.rewrite(ct, make.addClassMember(ct, method));
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRenameAttributeFromDefault174552a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@A(\"test\")\n" +
            "public class Test {\n" +
            "}\n" +
            "@interface A {\n" +
            "    public String value();\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@A(test = \"test\")\n" +
            "public class Test {\n" +
            "}\n" +
            "@interface A {\n" +
            "    public String value();\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);

                ExpressionTree attr = workingCopy.getTreeUtilities().parseExpression("test=\"test\"", new SourcePositions[1]);

                AnnotationTree at = ct.getModifiers().getAnnotations().get(0);
                AnnotationTree nue = make.addAnnotationAttrValue(make.removeAnnotationAttrValue(at, 0), attr);

                workingCopy.rewrite(at, nue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRenameAttributeFromDefault174552b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@AA(\"test\")\n" +
            "public class Test {\n" +
            "}\n" +
            "@interface AA {\n" +
            "    public String[] value();\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@AA({\"test\", \"test2\"})\n" +
            "public class Test {\n" +
            "}\n" +
            "@interface AA {\n" +
            "    public String[] value();\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);

                AnnotationTree at = ct.getModifiers().getAnnotations().get(0);
                AssignmentTree val = (AssignmentTree) at.getArguments().get(0);
                LiteralTree lit = (LiteralTree) val.getExpression();
                ExpressionTree nueValue = make.NewArray(null, Collections.<ExpressionTree>emptyList(), Arrays.asList(lit, make.Literal("test2")));

                workingCopy.rewrite(val.getExpression(), nueValue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void XtestRenameAttributeFromDefault174552c() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@A(\"test\")\n" +
            "public class Test {\n" +
            "}\n" +
            "@interface AA {\n" +
            "    public String[] value();\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@A({\"test\", \"test2\"})\n" + //adds extra space here.
            "public class Test {\n" +
            "}\n" +
            "@interface AA {\n" +
            "    public String[] value();\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree ct = (ClassTree) cut.getTypeDecls().get(0);

                AnnotationTree at = ct.getModifiers().getAnnotations().get(0);
                AssignmentTree val = (AssignmentTree) at.getArguments().get(0);
                LiteralTree lit = (LiteralTree) val.getExpression();
                ExpressionTree nueValue = make.NewArray(null, Collections.<ExpressionTree>emptyList(), Arrays.asList(lit, make.Literal("test2")));

                workingCopy.rewrite(val.getExpression(), nueValue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAnnotationWrapping1() throws Throwable {
        performAnnotationWrappingTest("@Entity " +
                                      "@Table(name = \"category\") " +
                                      "@NamedQueries({@NamedQuery(name = \"Category.findAll\", query = @Query(\"SELECT c FROM Category c\")), " +
                                      "               @NamedQuery(name = \"Category.findById\", query = @Query(\"SELECT c FROM Category c WHERE c.id = :id\")), " +
                                      "               @NamedQuery(name = \"Category.findByName\", query = @Query(\"SELECT c FROM Category c WHERE c.name = :name\")), " +
                                      "               @NamedQuery(name = \"Category.findByDescription\", query = @Query(\"SELECT c FROM Category c WHERE c.description = :description\")), " +
                                      "               @NamedQuery(name = \"Category.findByImageurl\", query = @Query(\"SELECT c FROM Category c WHERE c.imageurl = :imageurl\"))})");
    }

    public void testAnnotationWrapping2() throws Throwable {
        performAnnotationWrappingTest("@Entity " +
                                      "@Table(name = \"category\") " +
                                      "@NamedQueries({@NamedQuery(name = \"Category.findAll\", query = \"SELECT c FROM Category c\"), " +
                                      "               @NamedQuery(name = \"Category.findById\", query = \"SELECT c FROM Category c WHERE c.id = :id\"), " +
                                      "               @NamedQuery(name = \"Category.findByName\", query = \"SELECT c FROM Category c WHERE c.name = :name\"), " +
                                      "               @NamedQuery(name = \"Category.findByDescription\", query = \"SELECT c FROM Category c WHERE c.description = :description\"), " +
                                      "               @NamedQuery(name = \"Category.findByImageurl\", query = \"SELECT c FROM Category c WHERE c.imageurl = :imageurl\")})");
    }

    private void performAnnotationWrappingTest(final String annotationSpecification) throws Throwable {
        Map<String, List<String>> variables = new HashMap<String, List<String>>();

        variables.put(FmtOptions.wrapAnnotations, Arrays.asList(WrapStyle.WRAP_ALWAYS.name(), WrapStyle.WRAP_IF_LONG.name(), WrapStyle.WRAP_NEVER.name()));
        variables.put(FmtOptions.wrapAnnotationArgs, Arrays.asList(WrapStyle.WRAP_ALWAYS.name(), WrapStyle.WRAP_IF_LONG.name(), WrapStyle.WRAP_NEVER.name()));
        variables.put(FmtOptions.alignMultilineAnnotationArgs, Arrays.asList("false", "true"));
        variables.put(FmtOptions.indentSize, Arrays.asList("4", "5"));
        variables.put(FmtOptions.continuationIndentSize, Arrays.asList("6", "7"));

        List<Map<String, String>> variants = computeVariants(variables);

        for (Map<String, String> settings : variants) {
            try {
                performAnnotationWrappingTest(annotationSpecification, settings);
            } catch (Throwable t) {
                Exceptions.attachMessage(t, settings.toString());
                throw t;
            }
        }
    }

    private static List<Map<String, String>> computeVariants(Map<String, List<String>> variables) {
        List<Map<String, String>> result = new LinkedList<Map<String, String>>();
        Entry<String, List<String>> e = variables.entrySet().iterator().next();

        variables.remove(e.getKey());

        if (!variables.isEmpty()) {
            for (Map<String, String> m : computeVariants(variables)) {
                for (String v : e.getValue()) {
                    Map<String, String> nue = new HashMap<String, String>(m);

                    nue.put(e.getKey(), v);
                    result.add(nue);
                }
            }
        } else {
            for (String v : e.getValue()) {
                result.add(Collections.singletonMap(e.getKey(), v));
            }
        }

        return result;
    }

    private void performAnnotationWrappingTest(final String annotationSpecification, Map<String, String> adjustPreferences) throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        Map<String, String> origValues = new HashMap<String, String>();
        for (String key : adjustPreferences.keySet()) {
            origValues.put(key, preferences.get(key, null));
        }
        setValues(preferences, adjustPreferences);
        testFile = new File(getWorkDir(), "Test.java");
        String code = "package hierbas.del.litoral;\n" +
                      "\n" +
                      "public class Test {\n\n" +
                      "    public Test() {\n" +
                      "    }\n\n" +
                      "    public void test() {\n" +
                      "    }\n" +
                      "}\n";

        code = Reformatter.reformat(code, CodeStyle.getDefault(FileUtil.toFileObject(testFile)));
        TestUtilities.copyStringToFile(testFile, code);

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);

                final TreeMaker make = workingCopy.getTreeMaker();

                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitModifiers(ModifiersTree node, Void p) {
                        String toParse = "new Object() {" + annotationSpecification + " void test() {} }";
                        NewClassTree nct = (NewClassTree) workingCopy.getTreeUtilities().parseExpression(toParse, new SourcePositions[1]);
                        MethodTree method = ((MethodTree) nct.getClassBody().getMembers().get(0));
                        ModifiersTree mt = make.Modifiers(node.getFlags(), method.getModifiers().getAnnotations());
                        workingCopy.rewrite(node, mt);

                        return super.visitModifiers(node, p);
                    }
                    @Override
                    public Void visitClass(ClassTree node, Void p) {
                        String toParse = "new Object() {" + annotationSpecification + " void aa() {} }";
                        NewClassTree nct = (NewClassTree) workingCopy.getTreeUtilities().parseExpression(toParse, new SourcePositions[1]);
                        MethodTree method = ((MethodTree) nct.getClassBody().getMembers().get(0));
                        workingCopy.rewrite(node, make.addClassMember(node, method));
                        return super.visitClass(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        String formattedRes = Reformatter.reformat(res, CodeStyle.getDefault(FileUtil.toFileObject(testFile)));
        //System.err.println(res);
        res = res.replaceAll("\n[ ]*\n", "\n");
        //System.err.println(formattedRes);
        formattedRes = formattedRes.replaceAll("\n[ ]*\n", "\n"); //XXX: workaround for a bug in reformatter
        assertEquals(formattedRes, res);
        setValues(preferences, origValues);
    }

    public void testAnnotation187551a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "\n" +
            "@interface Test {\n" +
            "}\n"
            );
        String golden =
            "\n" +
            "@interface Foo {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        ClassTree copy = make.AnnotationType(clazz.getModifiers(), "Foo", clazz.getMembers());
                        workingCopy.rewrite(typeDecl, copy);
                    }
                }
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAnnotation187551b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "\n" +
            "@interface Test {\n" +
            "}\n"
            );
        String golden =
            "\n" +
            "class Foo {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        ModifiersTree mt = make.Modifiers(EnumSet.noneOf(Modifier.class));
                        //XXX: ideally, TreeMaker.Class should be enough but it is not right now, and it may not be possible to change due to compatibility:
                        ClassTree copy = make.Class(mt, "Foo", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), clazz.getMembers());
                        workingCopy.rewrite(typeDecl, copy);
                    }
                }
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAnnotation187551c() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "\n" +
            "class Test {\n" +
            "}\n"
            );
        String golden =
            "\n" +
            "@interface Foo {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        ClassTree copy = make.AnnotationType(clazz.getModifiers(), "Foo", clazz.getMembers());
                        workingCopy.rewrite(typeDecl, copy);
                    }
                }
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testParameterAnnotations() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String code = "package hierbas.del.litoral;\n" +
                      "\n" +
                      "public class Test {\n\n" +
                      "    public Test() {\n" +
                      "    }\n\n" +
                      "    public void test() {\n" +
                      "    }\n" +
                      "}\n";

        code = Reformatter.reformat(code, CodeStyle.getDefault(FileUtil.toFileObject(testFile)));
        TestUtilities.copyStringToFile(testFile, code);

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);

                NewClassTree nct = (NewClassTree) workingCopy.getTreeUtilities().parseExpression("new Object() { public int a(@Test1(a=1) @Test2(b=2) int i, @Test1 @Test2 int j) { return 0; }", new SourcePositions[1]);
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                workingCopy.rewrite(clazz, workingCopy.getTreeMaker().addClassMember(clazz, nct.getClassBody().getMembers().get(0)));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        String formattedRes = Reformatter.reformat(res, CodeStyle.getDefault(FileUtil.toFileObject(testFile)));
        //System.err.println(res);
        res = res.replaceAll("\n[ ]*\n", "\n");
        //System.err.println(formattedRes);
        formattedRes = formattedRes.replaceAll("\n[ ]*\n", "\n"); //XXX: workaround for a bug in reformatter
        assertEquals(formattedRes, res);
    }

    private void setValues(Preferences p, Map<String, String> values) {
        for (Entry<String, String> e : values.entrySet()) {
            if (e.getValue() != null) {
                p.put(e.getKey(), e.getValue());
            } else {
                p.remove(e.getKey());
            }
        }
    }

    public void test203333() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String code=
            "package hierbas.del.litoral;\n" +
            "\n" +
            "@Entity\n" +
            "@NamedQueries({\n" +
            "    @NamedQuery(name = MyEntity.A, query=\"a\")\n" +
            "    , @NamedQuery(name = MyEntity.B, query=\"b\")\n" +
            "    , @NamedQuery(name = MyEntity.C, query=\"c\")\n" +
            "})\n" +
            "@NamedNativeQuery(name = MyEntity.D, query=\"d\")\n" +
            "public class MyEntity {\n" +
            "}\n";
        TestUtilities.copyStringToFile(testFile, code);
        String golden = code.replace("MyEntity", "MyEntity1");

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.PARSED);
                final TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                        if (node.getName().contentEquals("MyEntity"))
                            workingCopy.rewrite(node, make.Identifier("MyEntity1"));
                        
                        return super.visitIdentifier(node, p);
                    }
                    @Override public Void visitClass(ClassTree node, Void p) {
                        workingCopy.rewrite(node, make.setLabel(node, "MyEntity1"));
                        return super.visitClass(node, p);
                    }
                }.scan(cut, null);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testFirstAnnotationWithImport() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String code = "package hierbas.del.litoral;\n" +
                      "\n" +
                      "public class Test {\n" +
                      "}\n";
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.lang.annotation.Retention;\n" +
            "\n" +
            "@Retention\n" +
            "public class Test {\n" +
            "}\n";

        TestUtilities.copyStringToFile(testFile, code);

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);

                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mods = clazz.getModifiers();
                TreeMaker make = workingCopy.getTreeMaker();
                workingCopy.rewrite(mods,
                                    make.addModifiersAnnotation(mods,
                                                                make.Annotation(make.Type("java.lang.annotation.Retention"),
                                                                                Collections.<ExpressionTree>emptyList())));
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveAnnotationEnumConstant() throws Exception {
        fileModificationTest("package hierbas.del.litoral;\n" +
                             "\n" +
                             "public enum Test {\n" +
                             "    A();\n" +
                             "}\n",
                             workingCopy -> {
                                 ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                                 VariableTree constant = (VariableTree) clazz.getMembers().get(1);
                                 ModifiersTree mods = constant.getModifiers();
                                 TreeMaker make = workingCopy.getTreeMaker();
                                 workingCopy.rewrite(mods,
                                                     make.addModifiersAnnotation(mods, make.Annotation(make.Identifier("Deprecated"), Collections.emptyList())));
                             },
                             "package hierbas.del.litoral;\n" +
                             "\n" +
                             "public enum Test {\n" +
                             "    @Deprecated\n" +
                             "    A();\n" +
                             "}\n");
        fileModificationTest("package hierbas.del.litoral;\n" +
                             "\n" +
                             "public enum Test {\n" +
                             "    @Deprecated\n" +
                             "    A();\n" +
                             "}\n",
                             workingCopy -> {
                                 ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                                 VariableTree constant = (VariableTree) clazz.getMembers().get(1);
                                 ModifiersTree mods = constant.getModifiers();
                                 TreeMaker make = workingCopy.getTreeMaker();
                                 workingCopy.rewrite(mods,
                                                     make.insertModifiersAnnotation(mods, 0, make.Annotation(make.Identifier("A"), Collections.emptyList())));
                             },
                             "package hierbas.del.litoral;\n" +
                             "\n" +
                             "public enum Test {\n" +
                             "    @A\n" +
                             "    @Deprecated\n" +
                             "    A();\n" +
                             "}\n");
        fileModificationTest("package hierbas.del.litoral;\n" +
                             "\n" +
                             "public enum Test {\n" +
                             "    @Deprecated\n" +
                             "    A();\n" +
                             "}\n",
                             workingCopy -> {
                                 ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                                 VariableTree constant = (VariableTree) clazz.getMembers().get(1);
                                 ModifiersTree mods = constant.getModifiers();
                                 TreeMaker make = workingCopy.getTreeMaker();
                                 workingCopy.rewrite(mods,
                                                     make.insertModifiersAnnotation(mods, 1, make.Annotation(make.Identifier("A"), Collections.emptyList())));
                             },
                             "package hierbas.del.litoral;\n" +
                             "\n" +
                             "public enum Test {\n" +
                             "    @Deprecated\n" +
                             "    @A\n" +
                             "    A();\n" +
                             "}\n");
        fileModificationTest("package hierbas.del.litoral;\n" +
                             "\n" +
                             "public enum Test {\n" +
                             "    @A\n" +
                             "    @Deprecated\n" +
                             "    A();\n" +
                             "}\n",
                             workingCopy -> {
                                 ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                                 VariableTree constant = (VariableTree) clazz.getMembers().get(1);
                                 ModifiersTree mods = constant.getModifiers();
                                 TreeMaker make = workingCopy.getTreeMaker();
                                 workingCopy.rewrite(mods,
                                                     make.removeModifiersAnnotation(mods, 0));
                             },
                             "package hierbas.del.litoral;\n" +
                             "\n" +
                             "public enum Test {\n" +
                             "    @Deprecated\n" +
                             "    A();\n" +
                             "}\n");
        fileModificationTest("package hierbas.del.litoral;\n" +
                             "\n" +
                             "public enum Test {\n" +
                             "    @Deprecated\n" +
                             "    @A\n" +
                             "    A();\n" +
                             "}\n",
                             workingCopy -> {
                                 ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                                 VariableTree constant = (VariableTree) clazz.getMembers().get(1);
                                 ModifiersTree mods = constant.getModifiers();
                                 TreeMaker make = workingCopy.getTreeMaker();
                                 workingCopy.rewrite(mods,
                                                     make.removeModifiersAnnotation(mods, 1));
                             },
                             "package hierbas.del.litoral;\n" +
                             "\n" +
                             "public enum Test {\n" +
                             "    @Deprecated\n" +
                             "    A();\n" +
                             "}\n");
        fileModificationTest("package hierbas.del.litoral;\n" +
                             "\n" +
                             "public enum Test {\n" +
                             "    @Deprecated\n" +
                             "    A();\n" +
                             "}\n",
                             workingCopy -> {
                                 ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                                 VariableTree constant = (VariableTree) clazz.getMembers().get(1);
                                 ModifiersTree mods = constant.getModifiers();
                                 TreeMaker make = workingCopy.getTreeMaker();
                                 workingCopy.rewrite(mods,
                                                     make.removeModifiersAnnotation(mods, 0));
                             },
                             "package hierbas.del.litoral;\n" +
                             "\n" +
                             "public enum Test {\n" +
                             "    A();\n" +
                             "}\n");
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
    
}
