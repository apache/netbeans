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

import com.sun.source.tree.*;
import java.io.File;
import static com.sun.source.tree.Tree.Kind.*;
import com.sun.source.util.TreeScanner;
import java.util.*;
import java.io.IOException;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.junit.NbTestSuite;
import junit.textui.TestRunner;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.source.save.CasualDiff;
import org.openide.filesystems.FileUtil;

/**
 * Tests the method generator.
 *
 * @author  Pavel Flaska
 */
public class ConstructorTest extends GeneratorTestBase {
    
    /** Need to be defined because of JUnit */
    public ConstructorTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(ConstructorTest.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        CasualDiff.noInvalidCopyTos = true;
    }

    public void testAddConstructor() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ConstructorTest.java");

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                // exactly one class in compilation unit
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().iterator().next();
                for (Tree member : topLevel.getMembers()) {
                    // for the first inner class in top level
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(member.getKind())) {

                        ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));

                        List<VariableTree> arguments = new ArrayList<VariableTree>();
                        arguments.add(make.Variable(
                                make.Modifiers(EnumSet.noneOf(Modifier.class)),
                                "a",
                                make.PrimitiveType(TypeKind.BOOLEAN), null)
                        );

                        MethodTree newConstructor = make.Constructor(
                                mods,
                                Collections.<TypeParameterTree>emptyList(),
                                arguments,
                                Collections.<ExpressionTree>emptyList(),
                                make.Block(Collections.<StatementTree>emptyList(), false)
                        );
                        ClassTree newInner = make.addClassMember((ClassTree) member, newConstructor);
                        workingCopy.rewrite(member, newInner);
                    }
                }
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertFiles("testAddConstructor.pass");
    }

    public void testAddConstructor2() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ConstructorTest2.java");

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                // exactly one class in compilation unit
                ClassTree topLevel = (ClassTree) cut.getTypeDecls().iterator().next();

                ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                List<VariableTree> arguments = new ArrayList<VariableTree>();
                arguments.add(make.Variable(
                        make.Modifiers(EnumSet.noneOf(Modifier.class)),
                        "a",
                        make.PrimitiveType(TypeKind.BOOLEAN), null)
                );
                MethodTree newConstructor = make.Constructor(
                        mods,
                        Collections.<TypeParameterTree>emptyList(),
                        arguments,
                        Collections.<ExpressionTree>emptyList(),
                        make.Block(Collections.<StatementTree>emptyList(), false)
                );

                ClassTree newClass = make.addClassMember(topLevel, newConstructor);
                workingCopy.rewrite(topLevel, newClass);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertFiles("testAddConstructor2.pass");
    }

    public void testRemovingReturnType134403() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void    Test() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                MethodTree nueMethod = make.Method(method.getModifiers(), method.getName(), null, method.getTypeParameters(), method.getParameters(), method.getThrows(), method.getBody(), (ExpressionTree) method.getDefaultValue());
                workingCopy.rewrite(method, nueMethod);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testConstructorWithSuper153561a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test extends java.util.ArrayList {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test extends java.util.ArrayList {\n\n" +
            "    public Test(int a) {\n" +
            "        super(a);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mt = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                VariableTree param = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "a", make.Type(workingCopy.getTypes().getPrimitiveType(TypeKind.INT)), null);
                MethodTree nueConstr = make.Constructor(mt, Collections.<TypeParameterTree>emptyList(), Collections.singletonList(param), Collections.<ExpressionTree>emptyList(), "{ super(a); }");
                workingCopy.rewrite(clazz, make.addClassMember(clazz, nueConstr));
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testConstructorWithSuper153561b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test extends java.util.ArrayList {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test extends java.util.ArrayList {\n" +
            "    public Test() {\n" +
            "    }\n\n" +
            "    public Test(int a) {\n" +
            "        super(a);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();

                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree mt = make.Modifiers(EnumSet.of(Modifier.PUBLIC));
                VariableTree param = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "a", make.Type(workingCopy.getTypes().getPrimitiveType(TypeKind.INT)), null);
                MethodTree origConstr = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = make.createMethodBody(origConstr, "{ super(a); }");
                MethodTree nueConstr = make.Constructor(mt, Collections.<TypeParameterTree>emptyList(), Collections.singletonList(param), Collections.<ExpressionTree>emptyList(), body);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, nueConstr));
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testConstructorWithSuper211913a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Test() {\n" +
            "        super(13);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();

                new TreeScanner<Void, Void>() {

                    @Override
                    public Void scan(Tree node, Void p) {
                        return super.scan(node, p);
                    }
                    @Override public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
                        if ("super".equals(node.getMethodSelect().toString())) {
                            workingCopy.rewrite(node, make.addMethodInvocationArgument(node, make.Literal(13)));
                        }
                        return super.visitMethodInvocation(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testConstructorWithSuper211913b() throws Exception {
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
            "public class Test {\n\n" +
            "    public Test() {\n" +
            "        super(13);\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();

                new TreeScanner<Void, Void>() {

                    @Override
                    public Void scan(Tree node, Void p) {
                        return super.scan(node, p);
                    }
                    @Override public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
                        if ("super".equals(node.getMethodSelect().toString())) {
                            workingCopy.rewrite(node, make.addMethodInvocationArgument(node, make.Literal(13)));
                        }
                        return super.visitMethodInvocation(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/ConstructorTest/ConstructorTest/";
    }

}
