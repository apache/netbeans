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
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.WorkingCopyTest;
import org.netbeans.junit.NbTestSuite;

/**
 * @author Pavel Flaska
 */
public class TypeAnnotationTest extends GeneratorTestBase {

    /** Creates a new instance of ClassMemberTest */
    public TypeAnnotationTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(TypeAnnotationTest.class);
//        suite.addTest(new ConstructorRenameTest("testAnnotationRename1"));
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
    

    public void testAddRemoveTypeAnnotation() throws Exception {
        performAlterTypeAnnotationTest("@A @C @D", new AlterAnnotations() {
            @Override public List<? extends AnnotationTree> alter(WorkingCopy copy, List<? extends AnnotationTree> annotations) {
                List<AnnotationTree> result = new ArrayList<AnnotationTree>(annotations);
                result.remove(1);
                return result;
            }
        });
        performAlterTypeAnnotationTest("@A @D", new AlterAnnotations() {
            @Override public List<? extends AnnotationTree> alter(WorkingCopy copy, List<? extends AnnotationTree> annotations) {
                List<AnnotationTree> result = new ArrayList<AnnotationTree>(annotations);
                result.remove(1);
                result.remove(1);
                return result;
            }
        });
        performAlterTypeAnnotationTest("@B @C @D", new AlterAnnotations() {
            @Override public List<? extends AnnotationTree> alter(WorkingCopy copy, List<? extends AnnotationTree> annotations) {
                List<AnnotationTree> result = new ArrayList<AnnotationTree>(annotations);
                result.remove(0);
                return result;
            }
        });
        performAlterTypeAnnotationTest("@A @B @C", new AlterAnnotations() {
            @Override public List<? extends AnnotationTree> alter(WorkingCopy copy, List<? extends AnnotationTree> annotations) {
                List<AnnotationTree> result = new ArrayList<AnnotationTree>(annotations);
                result.remove(3);
                return result;
            }
        });
        performAlterTypeAnnotationTest("@E @A @B @C @D", new AlterAnnotations() {
            @Override public List<? extends AnnotationTree> alter(WorkingCopy copy, List<? extends AnnotationTree> annotations) {
                List<AnnotationTree> result = new ArrayList<AnnotationTree>(annotations);
                TreeMaker make = copy.getTreeMaker();
                result.add(0, copy.getTreeMaker().TypeAnnotation(make.Identifier("E"), Collections.<ExpressionTree>emptyList()));
                return result;
            }
        });
        performAlterTypeAnnotationTest("@A @E @B @C @D", new AlterAnnotations() {
            @Override public List<? extends AnnotationTree> alter(WorkingCopy copy, List<? extends AnnotationTree> annotations) {
                List<AnnotationTree> result = new ArrayList<AnnotationTree>(annotations);
                TreeMaker make = copy.getTreeMaker();
                result.add(1, copy.getTreeMaker().TypeAnnotation(make.Identifier("E"), Collections.<ExpressionTree>emptyList()));
                return result;
            }
        });
        performAlterTypeAnnotationTest("@A @B @C @D @E", new AlterAnnotations() {
            @Override public List<? extends AnnotationTree> alter(WorkingCopy copy, List<? extends AnnotationTree> annotations) {
                List<AnnotationTree> result = new ArrayList<AnnotationTree>(annotations);
                TreeMaker make = copy.getTreeMaker();
                result.add(copy.getTreeMaker().TypeAnnotation(make.Identifier("E"), Collections.<ExpressionTree>emptyList()));
                return result;
            }
        });
    }
    
    public void testChangeTypeAnnotation() throws Exception {
        performAlterTypeAnnotationTest("@A @E @C @D", new AlterAnnotations() {
            @Override public List<? extends AnnotationTree> alter(WorkingCopy copy, List<? extends AnnotationTree> annotations) {
                List<AnnotationTree> result = new ArrayList<AnnotationTree>(annotations);
                TreeMaker make = copy.getTreeMaker();
                result.remove(1);
                result.add(1, copy.getTreeMaker().TypeAnnotation(make.Identifier("E"), Collections.<ExpressionTree>emptyList()));
                return result;
            }
        });
    }
    
    private void performAlterTypeAnnotationTest(String goldenAnnotations, final AlterAnnotations alter) throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "import java.util.List;\n" +
            "public class Test {\n" +
            "     private List<@A @B @C @D Integer> i;\n" +
            "}\n" +
            "@interface A {}\n" +
            "@interface B {}\n" +
            "@interface C {}\n" +
            "@interface D {}\n" +
            "@interface E {}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.util.List;\n" +
            "public class Test {\n" +
            "     private List<" + goldenAnnotations + " Integer> i;\n" +
            "}\n" +
            "@interface A {}\n" +
            "@interface B {}\n" +
            "@interface C {}\n" +
            "@interface D {}\n" +
            "@interface E {}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();

                new ErrorAwareTreePathScanner<Void, Void>(){
                    @Override public Void visitAnnotatedType(AnnotatedTypeTree node, Void p) {
                        workingCopy.rewrite(node, WorkingCopyTest.MakeAnnotatedTypeTemp(make, node.getUnderlyingType(), alter.alter(workingCopy, node.getAnnotations())));
                        return null;
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testModifyUnderlyingType() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "import java.util.List;\n" +
            "public class Test {\n" +
            "     private List<@A @B @C @D Integer> i;\n" +
            "}\n" +
            "@interface A {}\n" +
            "@interface B {}\n" +
            "@interface C {}\n" +
            "@interface D {}\n" +
            "@interface E {}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.util.List;\n" +
            "public class Test {\n" +
            "     private List<@A @B @C @D String> i;\n" +
            "}\n" +
            "@interface A {}\n" +
            "@interface B {}\n" +
            "@interface C {}\n" +
            "@interface D {}\n" +
            "@interface E {}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();

                new ErrorAwareTreePathScanner<Void, Void>(){
                    @Override public Void visitAnnotatedType(AnnotatedTypeTree node, Void p) {
                        workingCopy.rewrite(node, WorkingCopyTest.MakeAnnotatedTypeTemp(make, make.Identifier("String"), node.getAnnotations()));
                        return null;
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testOrdinary2Annotated() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "import java.util.List;\n" +
            "public class Test {\n" +
            "     private List<Integer> i;\n" +
            "}\n" +
            "@interface A {}\n" +
            "@interface B {}\n" +
            "@interface C {}\n" +
            "@interface D {}\n" +
            "@interface E {}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.util.List;\n" +
            "public class Test {\n" +
            "     private List<@A Integer> i;\n" +
            "}\n" +
            "@interface A {}\n" +
            "@interface B {}\n" +
            "@interface C {}\n" +
            "@interface D {}\n" +
            "@interface E {}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();

                new ErrorAwareTreePathScanner<Void, Void>(){
                    @Override public Void visitParameterizedType(ParameterizedTypeTree node, Void p) {
                        List<AnnotationTree> annotations = Collections.singletonList(workingCopy.getTreeMaker().TypeAnnotation(make.Identifier("A"), Collections.<ExpressionTree>emptyList()));
                        Tree orig = node.getTypeArguments().get(0);
                        workingCopy.rewrite(orig, WorkingCopyTest.MakeAnnotatedTypeTemp(make, orig, annotations));
                        return super.visitParameterizedType(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAnnotated2Ordinary() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "import java.util.List;\n" +
            "public class Test {\n" +
            "     private List<@A Integer> i;\n" +
            "}\n" +
            "@interface A {}\n" +
            "@interface B {}\n" +
            "@interface C {}\n" +
            "@interface D {}\n" +
            "@interface E {}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.util.List;\n" +
            "public class Test {\n" +
            "     private List<Integer> i;\n" +
            "}\n" +
            "@interface A {}\n" +
            "@interface B {}\n" +
            "@interface C {}\n" +
            "@interface D {}\n" +
            "@interface E {}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();

                new ErrorAwareTreePathScanner<Void, Void>(){
                    @Override public Void visitAnnotatedType(AnnotatedTypeTree node, Void p) {
                        workingCopy.rewrite(node, node.getUnderlyingType());
                        return super.visitAnnotatedType(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public interface AlterAnnotations {
        public List<? extends AnnotationTree> alter(WorkingCopy copy, List<? extends AnnotationTree> annotations);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
    
}
