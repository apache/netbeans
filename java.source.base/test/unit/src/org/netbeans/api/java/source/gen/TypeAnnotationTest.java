/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import com.sun.source.util.TreePathScanner;
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

                new TreePathScanner<Void, Void>(){
                    @Override public Void visitAnnotatedType(AnnotatedTypeTree node, Void p) {
                        workingCopy.rewrite(node, WorkingCopyTest.MakeAnnotatedTypeTemp(make, node.getUnderlyingType(), alter.alter(workingCopy, node.getAnnotations())));
                        return null;
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
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

                new TreePathScanner<Void, Void>(){
                    @Override public Void visitAnnotatedType(AnnotatedTypeTree node, Void p) {
                        workingCopy.rewrite(node, WorkingCopyTest.MakeAnnotatedTypeTemp(make, make.Identifier("String"), node.getAnnotations()));
                        return null;
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
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

                new TreePathScanner<Void, Void>(){
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
        System.err.println(res);
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

                new TreePathScanner<Void, Void>(){
                    @Override public Void visitAnnotatedType(AnnotatedTypeTree node, Void p) {
                        workingCopy.rewrite(node, node.getUnderlyingType());
                        return super.visitAnnotatedType(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
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
