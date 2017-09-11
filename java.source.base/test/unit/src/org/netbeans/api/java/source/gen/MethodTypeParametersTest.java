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
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.*;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests method type parameters changes.
 * 
 * @author Pavel Flaska
 */
public class MethodTypeParametersTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of MethodParametersTest */
    public MethodTypeParametersTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(MethodTypeParametersTest.class);
//        suite.addTest(new MethodTypeParametersTest("testAddFirst"));
//        suite.addTest(new MethodTypeParametersTest("testAddFirstToExisting"));
//        suite.addTest(new MethodTypeParametersTest("testAddFirstTwo"));
//        suite.addTest(new MethodTypeParametersTest("testAddThirdToExisting"));
//        suite.addTest(new MethodTypeParametersTest("testRemoveAll"));
//        suite.addTest(new MethodTypeParametersTest("testRemoveMid"));
//        suite.addTest(new MethodTypeParametersTest("testRemoveFirst"));
//        suite.addTest(new MethodTypeParametersTest("testRemoveLast"));
//        suite.addTest(new MethodTypeParametersTest("testRemoveJust"));
//        suite.addTest(new MethodTypeParametersTest("testRenameTypePar1"));
//        suite.addTest(new MethodTypeParametersTest("testRenameTypePar2"));
//        suite.addTest(new MethodTypeParametersTest("testRenameTypePar3"));
//        suite.addTest(new MethodTypeParametersTest("testRenameTypePar4"));
//        suite.addTest(new MethodTypeParametersTest("testWhitespaceAfterTypeParamInMethodInvocation170340"));
//        suite.addTest(new MethodTypeParametersTest("testAddTypeParamInMethodInvocation"));
//        suite.addTest(new MethodTypeParametersTest("testRemoveTypeParamInMethodInvocation"));
//        suite.addTest(new MethodTypeParametersTest("testAddTypeParamInMethodInvocationIdent"));
//        suite.addTest(new MethodTypeParametersTest("testRemoveTypeParamInMethodInvocationIdent"));
        return suite;
    }
    
    public void testAddFirst() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T> void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    MethodTree method = (MethodTree) clazz.getMembers().get(1);
                    MethodTree copy = make.addMethodTypeParameter(
                        method, make.TypeParameter("T", Collections.<ExpressionTree>emptyList())
                    );
                    workingCopy.rewrite(method, copy);
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirstTwo() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T, N> void taragui(int b) {\n" +
            "    }\n" +
            "}\n";


        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    MethodTree method = (MethodTree) clazz.getMembers().get(1);
                    MethodTree copy = make.addMethodTypeParameter(
                        method, make.TypeParameter("T", Collections.<ExpressionTree>emptyList())
                    );
                    copy = make.addMethodTypeParameter(
                        copy, make.TypeParameter("N", Collections.<ExpressionTree>emptyList())
                    );
                    workingCopy.rewrite(method, copy);
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveAll() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <E,N,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    MethodTree method = (MethodTree) clazz.getMembers().get(1);
                    MethodTree copy = make.removeMethodTypeParameter(method, 0);
                    copy = make.removeMethodTypeParameter(copy, 0);
                    copy = make.removeMethodTypeParameter(copy, 0);
                    workingCopy.rewrite(method, copy);
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    
    public void testAddThirdToExisting() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T,N>void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T,N, M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    MethodTree method = (MethodTree) clazz.getMembers().get(1);
                    MethodTree copy = make.addMethodTypeParameter(
                        method, make.TypeParameter("M", Collections.<ExpressionTree>emptyList())
                    );
                    workingCopy.rewrite(method, copy);
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirstToExisting() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T,N,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <E, T,N,M>void taragui(int b) {\n" +//XXX
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    MethodTree method = (MethodTree) clazz.getMembers().get(1);
                    MethodTree copy = make.insertMethodTypeParameter(
                        method, 0, make.TypeParameter("E", Collections.<ExpressionTree>emptyList())
                    );
                    workingCopy.rewrite(method, copy);
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveMid() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T,N,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    MethodTree method = (MethodTree) clazz.getMembers().get(1);
                    MethodTree copy = make.removeMethodTypeParameter(method, 1);
                    workingCopy.rewrite(method, copy);
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveFirst() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T,N,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <N,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    MethodTree method = (MethodTree) clazz.getMembers().get(1);
                    MethodTree copy = make.removeMethodTypeParameter(method, 0);
                    workingCopy.rewrite(method, copy);
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveLast() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T,N,M>void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T,N>void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    MethodTree method = (MethodTree) clazz.getMembers().get(1);
                    MethodTree copy = make.removeMethodTypeParameter(method, 2);
                    workingCopy.rewrite(method, copy);
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveJust() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T>void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    MethodTree method = (MethodTree) clazz.getMembers().get(1);
                    MethodTree copy = make.removeMethodTypeParameter(method, 0);
                    workingCopy.rewrite(method, copy);
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRenameTypePar1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T> void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <Tecko> void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    MethodTree method = (MethodTree) clazz.getMembers().get(1);
                    TypeParameterTree tpt = method.getTypeParameters().get(0);
                    workingCopy.rewrite(tpt, make.setLabel(tpt, "Tecko"));
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRenameTypePar2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T, E> void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <Tecko, E> void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    MethodTree method = (MethodTree) clazz.getMembers().get(1);
                    TypeParameterTree tpt = method.getTypeParameters().get(0);
                    workingCopy.rewrite(tpt, make.setLabel(tpt, "Tecko"));
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRenameTypePar3() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <T extends String, E> void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public <Tecko extends String, E> void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    MethodTree method = (MethodTree) clazz.getMembers().get(1);
                    TypeParameterTree tpt = method.getTypeParameters().get(0);
                    workingCopy.rewrite(tpt, make.setLabel(tpt, "Tecko"));
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Reproduces issue #96969.
     */ 
    public void testRenameTypePar4() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "    public <T extends String, E> void taragui(int b) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Test {\n" +
            "    public <T extends Retezec, E> void taragui(int b) {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    MethodTree method = (MethodTree) clazz.getMembers().get(1);
                    TypeParameterTree tpt = method.getTypeParameters().get(0);
                    IdentifierTree ident = (IdentifierTree) tpt.getBounds().get(0);
                    workingCopy.rewrite(ident, make.Identifier("Retezec"));
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testWhitespaceAfterTypeParamInMethodInvocation170340() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "class Test {\n" +
            "    static <T> T foo() { return null; }\n" +
            "    { Test.<String> foo(); }\n" +
            "}");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "class Test {\n" +
            "    static <T> T foo() { return null; }\n" +
            "    { Test.<String> bar(); }\n" +
            "}";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                BlockTree init = (BlockTree) clazz.getMembers().get(2);
                ExpressionStatementTree est = (ExpressionStatementTree) init.getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();

                workingCopy.rewrite(mit.getMethodSelect(), make.setLabel(mit.getMethodSelect(), "bar"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddTypeParamInMethodInvocation() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "class Test {\n" +
            "    static <T> T foo() { return null; }\n" +
            "    { Test.foo(); }\n" +
            "}");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "class Test {\n" +
            "    static <T> T foo() { return null; }\n" +
            "    { Test.<String>foo(); }\n" +
            "}";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                BlockTree init = (BlockTree) clazz.getMembers().get(2);
                ExpressionStatementTree est = (ExpressionStatementTree) init.getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MethodInvocationTree methodInvocation = make.MethodInvocation(Collections.singletonList((ExpressionTree)make.Type("String")), mit.getMethodSelect(), mit.getArguments());

                workingCopy.rewrite(mit, methodInvocation);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveTypeParamInMethodInvocation() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "class Test {\n" +
            "    static <T> T foo() { return null; }\n" +
            "    { Test.<String>foo(); }\n" +
            "}");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "class Test {\n" +
            "    static <T> T foo() { return null; }\n" +
            "    { Test.foo(); }\n" +
            "}";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                BlockTree init = (BlockTree) clazz.getMembers().get(2);
                ExpressionStatementTree est = (ExpressionStatementTree) init.getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MethodInvocationTree methodInvocation = make.MethodInvocation(Collections.EMPTY_LIST, mit.getMethodSelect(), mit.getArguments());

                workingCopy.rewrite(mit, methodInvocation);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
     public void testAddTypeParamInMethodInvocationIdent() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "class Test {\n" +
            "    static <T> T foo() { return null; }\n" +
            "    void main() { foo(); }\n" +
            "}");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "class Test {\n" +
            "    static <T> T foo() { return null; }\n" +
            "    void main() { <String>foo(); }\n" +
            "}";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree init = (MethodTree) clazz.getMembers().get(2);
                ExpressionStatementTree est = (ExpressionStatementTree) init.getBody().getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MethodInvocationTree methodInvocation = make.MethodInvocation(Collections.singletonList((ExpressionTree)make.Type("String")), mit.getMethodSelect(), mit.getArguments());

                workingCopy.rewrite(mit, methodInvocation);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveTypeParamInMethodInvocationIdent() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "class Test {\n" +
            "    <T> T foo() { return null; }\n" +
            "    void main() { this.<String>foo(); }\n" +
            "}");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "class Test {\n" +
            "    <T> T foo() { return null; }\n" +
            "    void main() { this.foo(); }\n" +
            "}";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree init = (MethodTree) clazz.getMembers().get(2);
                ExpressionStatementTree est = (ExpressionStatementTree) init.getBody().getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MethodInvocationTree methodInvocation = make.MethodInvocation(Collections.EMPTY_LIST, mit.getMethodSelect(), mit.getArguments());

                workingCopy.rewrite(mit, methodInvocation);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testToTypeParamInMethodInvocationIdent() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "class Test {\n" +
            "    <T> T foo() { return null; }\n" +
            "    void main() { foo(); }\n" +
            "}");
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "class Test {\n" +
            "    <T> T foo() { return null; }\n" +
            "    void main() { this.<String>foo(); }\n" +
            "}";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree init = (MethodTree) clazz.getMembers().get(2);
                ExpressionStatementTree est = (ExpressionStatementTree) init.getBody().getStatements().get(0);
                MethodInvocationTree mit = (MethodInvocationTree) est.getExpression();
                MethodInvocationTree methodInvocation = make.MethodInvocation(Collections.singletonList(make.Identifier("String")), make.QualIdent("this.foo"), mit.getArguments());

                workingCopy.rewrite(mit, methodInvocation);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "Test.java");
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
    
}
