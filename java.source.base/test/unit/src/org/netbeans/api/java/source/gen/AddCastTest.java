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
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.io.IOException;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests correct adding cast to statement.
 *
 * @author Pavel Flaska
 */
public class AddCastTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of AddCastTest */
    public AddCastTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(AddCastTest.class);
        return suite;
    }

    public void testAddCastToDeclStmt() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "        System.err.println(\"taragui() method\");\n" + 
            "        String s = \"Oven.\";\n" +
            "//         line comment\n" + 
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "        System.err.println(\"taragui() method\");\n" + 
            "        String s = (String) \"Oven.\";\n" +
            "//         line comment\n" + 
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(1);
                ExpressionTree init = var.getInitializer();
                ExpressionTree cast = make.TypeCast(make.Identifier("String"), init);
                workingCopy.rewrite(init, cast);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddCastInForWithoutSteps() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void cast() {\n" +
            "        Object o = null;\n" +
            "        for (int i = 0; i < 5; ) {\n" +
            "            String s = o;\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void cast() {\n" +
            "        Object o = null;\n" +
            "        for (int i = 0; i < 5; ) {\n" +
            "            String s = (String) o;\n" +
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
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                ForLoopTree forLoop = (ForLoopTree) method.getBody().getStatements().get(1);
                BlockTree block = (BlockTree) forLoop.getStatement();
                VariableTree vt = (VariableTree) block.getStatements().get(0);
                ExpressionTree init = vt.getInitializer();
                ExpressionTree cast = make.TypeCast(make.Identifier("String"), init);
                workingCopy.rewrite(init, cast);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /*
     * #94324
     */
    public void testAddCast94324() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication3;\n" +
            "\n" +
            "public class Klasa {\n" +
            "    \n" +
            "    static Object method() {\n" +
            "        return null;\n" +
            "    }\n" +
            "    \n" +
            "    public static void main(String[] args) {\n" +
            "        // TODO code application logic here\n" +
            "        String s = Klasa.method();\n" +
            "    }\n" +
            "\n" +
            "}\n"
        );
        String golden =
            "package javaapplication3;\n" +
            "\n" +
            "public class Klasa {\n" +
            "    \n" +
            "    static Object method() {\n" +
            "        return null;\n" +
            "    }\n" +
            "    \n" +
            "    public static void main(String[] args) {\n" +
            "        // TODO code application logic here\n" +
            "        String s = (String) Klasa.method();\n" +
            "    }\n" +
            "\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(2);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                ExpressionTree init = var.getInitializer();
                ExpressionTree cast = make.TypeCast(make.Identifier("String"), init);
                workingCopy.rewrite(init, cast);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddCastLiteral() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package javaapplication3;\n" +
            "\n" +
            "public class Klasa {\n" +
            "    public static void main(String[] args) {\n" +
            "        int i = args.length & 0xFEFE;\n" +
            "    }\n" +
            "\n" +
            "}\n"
        );
        String golden =
            "package javaapplication3;\n" +
            "\n" +
            "public class Klasa {\n" +
            "    public static void main(String[] args) {\n" +
            "        int i = (int) (args.length & 0xFEFE);\n" +
            "    }\n" +
            "\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                ExpressionTree init = var.getInitializer();
                ExpressionTree cast = make.TypeCast(make.PrimitiveType(TypeKind.INT), make.Parenthesized(init));
                workingCopy.rewrite(init, cast);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    /*
     * #94324 - second test. If the moved expression is changed, it is again
     * rewritten by VeryPretty - in this time, i do not see any solution for
     * that.
     *//*
    public void testAddCast94324_2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication3;\n" +
            "\n" +
            "public class Klasa {\n" +
            "    \n" +
            "    static Object method() {\n" +
            "        return null;\n" +
            "    }\n" +
            "    \n" +
            "    public static void main(String[] args) {\n" +
            "        // TODO code application logic here\n" +
            "        String s = Klasa.method();\n" +
            "    }\n" +
            "\n" +
            "}\n"
        );
        String golden =
            "package javaapplication3;\n" +
            "\n" +
            "public class Klasa {\n" +
            "    \n" +
            "    static Object method() {\n" +
            "        return null;\n" +
            "    }\n" +
            "    \n" +
            "    public static void main(String[] args) {\n" +
            "        // TODO code application logic here\n" +
            "        String s = (String) javaapplication3.Klasa.metoda();\n" +
            "    }\n" +
            "\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(2);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                MethodInvocationTree init = (MethodInvocationTree) var.getInitializer();
                MethodInvocationTree initCopy = make.MethodInvocation(
                        (List<ExpressionTree>) init.getTypeArguments(),
                        make.setLabel((MemberSelectTree) init.getMethodSelect(), "metoda"),
                        init.getArguments()
                );
                workingCopy.rewrite(init, initCopy);
                ExpressionTree cast = make.TypeCast(make.Identifier("String"), initCopy);
                workingCopy.rewrite(init, cast);
            }

            public void cancel() {
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }*/
    
    String getGoldenPckg() {
        return "";
    }
    
    String getSourcePckg() {
        return "";
    }
    
    
}
