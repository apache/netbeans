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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Pavel Flaska
 */
public class ArraysTest extends GeneratorTestMDRCompat {

    /** Creates a new instance of ArraysTest */
    public ArraysTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ArraysTest.class);
//        suite.addTest(new ArraysTest("testConstantRename"));
//        suite.addTest(new ArraysTest("testDuplicateMethodWithArrReturn1"));
//        suite.addTest(new ArraysTest("testDuplicateMethodWithArrReturn2"));
//        suite.addTest(new ArraysTest("test120768"));
//        suite.addTest(new ArraysTest("testRewriteType"));
//        suite.addTest(new ArraysTest("test162485a"));
//        suite.addTest(new ArraysTest("test162485b"));
        return suite;
    }

    /**
     * #109267
     */
    public void testConstantRename() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, B, C;\n" +
            "    \n" +
            "    public void enumMethod() {\n" +
            "        int[] a = new int[2];\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, B, C;\n" +
            "    \n" +
            "    public void enumMethod() {\n" +
            "        {\n" +
            "            int[] a = new int[2];\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(4);
                VariableTree vt = (VariableTree) method.getBody().getStatements().get(0);
                BlockTree block = make.Block(Collections.<StatementTree>singletonList(vt), false);
                workingCopy.rewrite(vt, block);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * #114400_1
     */
    public void testDuplicateMethodWithArrReturn1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, B, C;\n" +
            "    \n" +
            "    public int[] enumMethod() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, B, C;\n" +
            "    \n" +
            "    public int[] enumMethod() {\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "class Copy {\n" +
            "\n" +
            "    public int[] enumMethod() {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ModifiersTree empty = make.Modifiers(Collections.<Modifier>emptySet());
                
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(4);
                MethodTree newMethod = make.Method(
                        method.getModifiers(),
                        method.getName(),
                        method.getReturnType(),
                        method.getTypeParameters(),
                        method.getParameters(),
                        method.getThrows(),
                        method.getBody(),
                        null
                        );
                ClassTree copy = make.Class(
                        empty,
                        "Copy",
                        Collections.<TypeParameterTree>emptyList(),
                        null,
                        Collections.<Tree>emptyList(), 
                        Collections.<Tree>singletonList(newMethod)
                );
                workingCopy.rewrite(cut, make.addCompUnitTypeDecl(cut, copy));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #114400_2
     */
    public void testDuplicateMethodWithArrReturn2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, B, C;\n" +
            "    \n" +
            "    public int[] enumMethod() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public enum Test {\n" +
            "    A, B, C;\n" +
            "    \n" +
            "    public int[] enumMethod() {\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "class Copy {\n" +
            "\n" +
            "    public int[] enumMethod() {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ModifiersTree empty = make.Modifiers(Collections.<Modifier>emptySet());
                
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(4);
                ClassTree copy = make.Class(
                        empty,
                        "Copy",
                        Collections.<TypeParameterTree>emptyList(),
                        null,
                        Collections.<Tree>emptyList(), 
                        Collections.<Tree>singletonList(method)
                );
                workingCopy.rewrite(cut, make.addCompUnitTypeDecl(cut, copy));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void test120768() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    byte[] test = new byte[10000];\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    byte[] test = new byte[WHAT_A_F];\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                NewArrayTree nat = (NewArrayTree) var.getInitializer();
                workingCopy.rewrite(nat.getDimensions().get(0), make.Identifier("WHAT_A_F"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test162485a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    Object test = new int[2];\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    Object test = {{1}};\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                NewArrayTree nat = (NewArrayTree) var.getInitializer();
                NewArrayTree dim2 = make.NewArray(null, Collections.<ExpressionTree>emptyList(), Collections.singletonList(make.Literal(Integer.valueOf(1))));
                NewArrayTree newTree = make.NewArray(null, Collections.<ExpressionTree>emptyList(), Collections.<ExpressionTree>singletonList(dim2));
                workingCopy.rewrite(nat, newTree);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test162485b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    Object test = new int[] {};\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    Object test = {{1}};\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                NewArrayTree nat = (NewArrayTree) var.getInitializer();
                NewArrayTree dim2 = make.NewArray(null, Collections.<ExpressionTree>emptyList(), Collections.singletonList(make.Literal(Integer.valueOf(1))));
                NewArrayTree newTree = make.NewArray(null, Collections.<ExpressionTree>emptyList(), Collections.<ExpressionTree>singletonList(dim2));
                workingCopy.rewrite(nat, newTree);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testCLike1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    Integer a[] = null;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    Float a = null;\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(var.getType(), make.Type("Float"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }


    public void testCLike2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int a[][] = null;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int[] a = null;\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(var.getType(), make.Type("int[]"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    
    public void testCLike3() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int[] a[][] = null;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int[] a = null;\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                workingCopy.rewrite(var.getType(), make.Type("int[]"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testCLike4() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int[] a[][] = null;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    int[][] b = null;\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(1);
                //both this:
//                workingCopy.rewrite(var.getType(), make.Type("int[]"));
//                workingCopy.rewrite(var, make.setLabel(var, "b"));
                //and this should get the same result:
                workingCopy.rewrite(var, make.Variable(var.getModifiers(), "b", make.Type("int[][]"), make.Literal(null)));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRewriteType() throws Exception {
        final class ReplaceQualifiedNames extends TreePathScanner<Void, Void> {
            private final WorkingCopy copy;

            public ReplaceQualifiedNames(WorkingCopy copy) {
                this.copy = copy;
            }
            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                if (node.getName().toString().equals("test")) {
                    copy.rewrite(node, copy.getTreeMaker().Identifier("test"));
                    return null;
                }
                
                Element e = copy.getTrees().getElement(getCurrentPath());

                if (e != null) {
                    copy.rewrite(node, copy.getTreeMaker().QualIdent(e));
                    return null;
                }

                return super.visitIdentifier(node, p);
            }

            @Override
            public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
                scan(node.getPackageAnnotations(), p);
                scan(node.getImports(), p);
                scan(node.getTypeDecls(), p);
                return null;
            }
            @Override
            public Void scan(Tree tree, Void p) {
                if (tree != null && copy.getTreeUtilities().isSynthetic(new TreePath(getCurrentPath(), tree)))
                    return null;
                return super.scan(tree, p);
            }
        }

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.net.URL;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void test(URL[] u) {" +
            "        test(new URL[0]);" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.net.URL;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void test(URL[] u) {" +
            "        test(new URL[0]);" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                new ReplaceQualifiedNames(workingCopy).scan(cut, null);
                CompilationUnitTree nue = workingCopy.getTreeMaker().CompilationUnit(cut.getPackageName(), Collections.<ImportTree>emptyList(), cut.getTypeDecls(), cut.getSourceFile());
                workingCopy.rewrite(cut, nue);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
}
