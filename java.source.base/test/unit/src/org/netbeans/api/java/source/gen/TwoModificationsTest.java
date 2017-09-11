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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.util.List;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 * Tests which do two modification in one modification task.
 * 
 * @author Pavel Flaska
 */
public class TwoModificationsTest extends GeneratorTestBase {
    
    /** Creates a new instance of TwoModificationsTest */
    public TwoModificationsTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(TwoModificationsTest.class);
//        suite.addTest(new TwoModificationsTest("testModifySetter"));
        return suite;
    }
    
    /**
     * #91265: Adding annotation to class and method to class members fails
     */
    public void testAddAnnAndMethod() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.*;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {// what a ... problem\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.io.*;\n" +
            "\n" +
            "@javax.jws.WebService\n" +
            "public class Test {\n" +
            "    public void taragui() {// what a ... problem\n" +
            "    }\n\n" +
            "    public void writeExternal(final Object arg0) throws IOException {\n" +
            "        throw new UnsupportedOperationException(\"Not supported yet.\");\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree clazz = (ClassTree) typeDecl;
                        
                        AnnotationTree wsAnnotation = make.Annotation(
                                make.Identifier("javax.jws.WebService"), 
                                //make.QualIdent(element),
                                Collections.<ExpressionTree>emptyList()
                        );
                        
                        ClassTree modClass1 =  make.Class(
                            make.addModifiersAnnotation(clazz.getModifiers(), wsAnnotation),
                            clazz.getSimpleName(),
                            clazz.getTypeParameters(),
                            clazz.getExtendsClause(),
                            (List<ExpressionTree>)clazz.getImplementsClause(),
                            clazz.getMembers());
                        
                        // create method modifier: public and no annotation
                        ModifiersTree methodModifiers = make.Modifiers(
                            Collections.<Modifier>singleton(Modifier.PUBLIC),
                            Collections.<AnnotationTree>emptyList()
                        );
                        
                        // create parameter:
                        // final ObjectOutput arg0
                        VariableTree parameter = make.Variable(
                                make.Modifiers(
                                    Collections.<Modifier>singleton(Modifier.FINAL),
                                    Collections.<AnnotationTree>emptyList()
                                ),
                                "arg0", // name
                                make.Identifier("Object"), // parameter type
                                null // initializer - does not make sense in parameters.
                        );
                        
                        // prepare simple name to throws clause:
                        // 'throws IOException' and its import will be added
                        TypeElement element = workingCopy.getElements().getTypeElement("java.io.IOException");
                        ExpressionTree throwsClause = make.QualIdent(element);
                        
                        // create method. There are two basic options:
                        // 1)
                        // make.Method() with 'BlockTree body' parameter -
                        // body has to be created, here in example code
                        // empty body block commented out
                        // 2)
                        // make.Method() with 'String body' parameter -
                        // body is added as a text. Used in our example.
                        MethodTree newMethod = make.Method(
                            methodModifiers, // public
                            "writeExternal", // writeExternal
                            make.PrimitiveType(TypeKind.VOID), // return type "void"
                            Collections.<TypeParameterTree>emptyList(), // type parameters - none
                            Collections.<VariableTree>singletonList(parameter), // final ObjectOutput arg0
                            Collections.<ExpressionTree>singletonList(throwsClause), // throws 
                            "{ throw new UnsupportedOperationException(\"Not supported yet.\") }", // body text
                            // make.Block(Collections.<StatementTree>emptyList(), false), // empty statement block
                            null // default value - not applicable here, used by annotations
                        );

                        // and in the same way as interface was added to implements clause,
                        // add feature to the class:
                        ClassTree modifiedClazz = make.addClassMember(modClass1, newMethod);
                        workingCopy.rewrite(clazz, modifiedClazz);
                    }
                }
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testModifySetter() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(Class o) {\n" +
            "    }\n" +
            "    \n" +
            "    public String getText() {\n" +
            "    }\n" +
            "    \n" +
            "    public void setText() {\n" +
            "        System.out.println(\"Text\");\n" +
            "    }\n" +
            "    \n" +
            "    public Object method2(Class o) {\n" +
            "    }\n" +
            "    \n" +
            "}\n");
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    private int i;\n" +
            "    public Object method(Class o) {\n" +
            "    }\n" +
            "    \n" +
            "    public String getText() {\n" +
            "    }\n" +
            "    \n" +
            "    public void setText() {\n" +
            "        System.out.println(\"Test\");\n" +
            "        System.out.println(\"Text\");\n" +
            "        System.out.println(\"Test\");\n" +
            "    }\n" +
            "    \n" +
            "    public Object method2(Class o) {\n" +
            "    }\n" +
            "    \n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(org.netbeans.api.java.source.JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree)workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree)clazz.getMembers().get(3);
                BlockTree block = method.getBody();
                VariableTree var = make.Variable(make.Modifiers(EnumSet.of(Modifier.PRIVATE)), "i", make.Type(workingCopy.getTypes().getPrimitiveType(TypeKind.INT)), null);
                ClassTree clazzCopy = make.insertClassMember(clazz, 0, var);
                workingCopy.rewrite(clazz, clazzCopy);
                ExpressionStatementTree est = make.ExpressionStatement(
                    make.MethodInvocation(
                        Collections.<ExpressionTree>emptyList(),
                        make.MemberSelect(
                            make.MemberSelect(
                                make.Identifier("System"),
                                "out"
                            ),
                            "println"
                        ),
                        Collections.<ExpressionTree>singletonList(
                            make.Literal("Test")
                        )
                    )
                );
                BlockTree bt = make.addBlockStatement(block, est);
                bt = make.insertBlockStatement(bt, 0, est);
                workingCopy.rewrite(block, bt);
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRewriteMethodTwoTimes() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(Class o) {\n" +
            "    }\n" +
            "    \n" +
            "    public String getText() {\n" +
            "    }\n" +
            "    \n" +
            "    public void setText() {\n" +
            "        System.out.println(\"Text\");\n" +
            "    }\n" +
            "    \n" +
            "    public Object method2(Class o) {\n" +
            "    }\n" +
            "    \n" +
            "}\n");
         String golden = 
            "package personal;\n" +
            "\n" +
            "public class Test {\n" +
            "    public Object method(Class o) {\n" +
            "    }\n" +
            "    \n" +
            "    public String getText() {\n" +
            "    }\n" +
            "    \n" +
            "    public void textSetter() {\n" +
            "        System.out.println(\"Text\");\n" +
            "    }\n" +
            "    \n" +
            "    public Object method2(Class o) {\n" +
            "    }\n" +
            "    \n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(org.netbeans.api.java.source.JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(3);
                workingCopy.rewrite(method, make.setLabel(method, "nastavText"));
                workingCopy.rewrite(method, make.setLabel(method, "textSetter"));
            }
            
        };
        testSource.runModificationTask(task).commit();
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
