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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import java.io.*;
import java.util.Collections;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;

/**
 * Some test with type parameters.
 * 
 * @author Pavel Flaska
 */
public class TypeParameterTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of TypeParameterTest */
    public TypeParameterTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(TypeParameterTest.class);
//        suite.addTest(new TypeParameterTest("testRenameTypeParamInClassDecl"));
//        suite.addTest(new TypeParameterTest("testRenameWildCard"));
//        suite.addTest(new TypeParameterTest("testAddGenericImplements"));
//        suite.addTest(new TypeParameterTest("testAddGenericImplementsWithAutoImp"));
        return suite;
    }
    
    /**
     * #92531: rename in type parameter in class decl:
     * public MyList<Ex> extends ArrayList<Ex> implements List<Ex> { ...
     */
    public void testRenameTypeParamInClassDecl() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class MyList<Ex> extends ArrayList<Ex> implements List<Ex> {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class MyList<E> extends ArrayList<E> implements List<E> {\n" +
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
                        // name
                        TypeParameterTree tpt = clazz.getTypeParameters().get(0);
                        workingCopy.rewrite(tpt, make.setLabel(tpt, "E"));
                        // extends
                        ParameterizedTypeTree ptt = (ParameterizedTypeTree) clazz.getExtendsClause();
                        IdentifierTree it = (IdentifierTree) ptt.getTypeArguments().get(0);
                        workingCopy.rewrite(it, make.Identifier("E"));
                        // implements
                        ptt = (ParameterizedTypeTree) clazz.getImplementsClause().get(0);
                        it = (IdentifierTree) ptt.getTypeArguments().get(0);
                        workingCopy.rewrite(it, make.Identifier("E"));
                    }
                }
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     */
    public void testRenameWildCard() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class MyList<Ex> {\n" +
            "    public Object method(Collection<? extends Ex> c) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class MyList<E> {\n" +
            "    public Object method(Collection<? extends E> c) {\n" +
            "    }\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                // name
                TypeParameterTree tpt = clazz.getTypeParameters().get(0);
                workingCopy.rewrite(tpt, make.setLabel(tpt, "E"));
                // method
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree vt = method.getParameters().get(0);
                ParameterizedTypeTree ptt = (ParameterizedTypeTree) vt.getType();
                WildcardTree wct = (WildcardTree) ptt.getTypeArguments().get(0);
                workingCopy.rewrite(wct.getBound(), make.Identifier("E"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     */
    public void testAddGenericImplements() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public class MyList<Ex> {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.*;\n" +
            "\n" +
            "public class MyList<Ex> implements Nothing<String> {\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                ParameterizedTypeTree ptt = make.ParameterizedType(
                        make.Identifier("Nothing"), 
                        Collections.<ExpressionTree>singletonList(make.Identifier("String"))
                );
                // name
                workingCopy.rewrite(clazz, make.addClassImplementsClause(clazz, ptt));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     */
    public void testAddGenericImplementsWithAutoImp() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class MyList<Ex> {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class MyList<Ex> implements List<String> {\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                TypeElement te = workingCopy.getElements().getTypeElement("java.util.List");
                ParameterizedTypeTree ptt = make.ParameterizedType(
                        make.QualIdent(te),
                        Collections.<ExpressionTree>singletonList(make.Identifier("String"))
                );
                // name
                workingCopy.rewrite(clazz, make.addClassImplementsClause(clazz, ptt));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void XtestRemoveTypeParameter123732() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class MyList<Ex> {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class MyList {\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree nue = make.Class(clazz.getModifiers(), clazz.getSimpleName(), Collections.<TypeParameterTree>emptyList(), clazz.getExtendsClause(), clazz.getImplementsClause(), clazz.getMembers());
                // name
                workingCopy.rewrite(clazz, nue);
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
