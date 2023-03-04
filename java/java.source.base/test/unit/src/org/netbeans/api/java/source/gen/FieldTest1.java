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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.lang.model.type.TypeKind;
import junit.textui.TestRunner;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 * Test the field generator.
 *
 * @author  Pavel Flaska
 */
public class FieldTest1 extends GeneratorTestMDRCompat {
    
    public FieldTest1(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new FieldTest1("testFieldModifiers"));
        suite.addTest(new FieldTest1("testFieldType"));
        suite.addTest(new FieldTest1("testFieldName"));
        suite.addTest(new FieldTest1("testFieldInitialValueText"));
        suite.addTest(new FieldTest1("testFieldInitialValue"));
        suite.addTest(new FieldTest1("testFieldChangeInInitValue"));
        suite.addTest(new FieldTest1("testFieldInitialValueRemoval"));
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "FieldTest1.java");
    }

    /**
     * Removes all the modififers from the field.
     */
    public void testFieldModifiers() throws IOException {
        System.err.println("testFieldModifiers");
        process(
            new Transformer<Void, Object>() {
                public Void visitVariable(VariableTree node, Object p) {
                    super.visitVariable(node, p);
                    if ("modifiersField".contentEquals(node.getName())) {
                        copy.rewrite(node.getModifiers(), make.Modifiers(Collections.EMPTY_SET));
                    }
                    return null;
                }
            }
        );
        assertFiles("testFieldModifiers.pass");
    }
    
    /**
     * Changes long type of the field to short type.
     */
    public void testFieldType() throws IOException {
        System.err.println("testFieldType");
        process(
            new Transformer<Void, Object>() {
                public Void visitVariable(VariableTree node, Object p) {
                    super.visitVariable(node, p);
                    if ("typeField".contentEquals(node.getName())) {
                        PrimitiveTypeTree pt = make.PrimitiveType(TypeKind.SHORT);
                        VariableTree vt = make.Variable(
                                node.getModifiers(),
                                node.getName(),
                                pt,
                                node.getInitializer()
                        );
                        model.setElement(vt, model.getElement(node));
                        model.setType(vt, model.getType(node));
                        model.setPos(vt, model.getPos(node));
                        //copy.rewrite(node.getType(), tree);
                        copy.rewrite(node, vt);
                    }
                    return null;
                }
            }
        );
        assertFiles("testFieldType.pass");
    }
    
    /**
     * Changes field name to thisIsTheNewName.
     */
    public void testFieldName() throws IOException {
        System.err.println("testFieldName");
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                VariableTree var = (VariableTree) clazz.getMembers().get(3);
                workingCopy.rewrite(var, make.setLabel(var, "thisIsTheNewName"));
            }            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testFieldName.pass");
    }
    
    /**
     * copy the initial value text.
     */
    public void testFieldInitialValueText() throws IOException {
        System.err.println("testFieldInitialValueText");
        process(
            new Transformer<Void, Object>() {
                public Void visitVariable(VariableTree node, Object p) {
                    super.visitVariable(node, p);
                    if ("initialValueTextTester".contentEquals(node.getName())) {
                        copy.rewrite(node.getInitializer(), make.Literal("This is a new text."));
                    }
                    return null;
                }
            }
        );
        assertFiles("testFieldInitialValueText.pass");
    }
    
    /**
     * Change whole initial value expression to literal 78.
     */
    public void testFieldInitialValue() throws IOException {
        System.err.println("testFieldInitialValue");
        process(
            new Transformer<Void, Object>() {
                public Void visitVariable(VariableTree node, Object p) {
                    super.visitVariable(node, p);
                    if ("initialValueChanger".contentEquals(node.getName())) {
                        copy.rewrite(node.getInitializer(), make.Literal(Long.valueOf(78)));
                    }
                    return null;
                }
            }
        );
        assertFiles("testFieldInitialValue.pass");
    }
    
    /**
     * copy the initialization 'new String("NetBeers")' to 'new String()'.
     * It tests only change in expression, no expression swap.
     */
    public void testFieldChangeInInitValue() throws IOException {
        System.err.println("testFieldChangeInInitValue");
        process(
            new Transformer<Void, Object>() {
                public Void visitVariable(VariableTree node, Object p) {
                    super.visitVariable(node, p);
                    if ("initialValueReplacer".contentEquals(node.getName())) {
                        if (Tree.Kind.NEW_CLASS.equals(node.getInitializer().getKind())) {
                            NewClassTree nct = (NewClassTree) node.getInitializer();
                            NewClassTree njuNct = make.NewClass(
                                    nct.getEnclosingExpression(), 
                                    (List<ExpressionTree>) nct.getTypeArguments(),
                                    nct.getIdentifier(),
                                    Collections.singletonList(make.Literal("NetBeans")),
                                    nct.getClassBody()
                            );
                            copy.rewrite(nct, njuNct);
                        }
                    }
                    return null;
                }
            }
        );
        assertFiles("testFieldChangeInInitValue.pass");
    }

    /**
     * Removes the inital value from the field.
     */
    public void testFieldInitialValueRemoval() throws IOException {
        System.err.println("testFieldInitialValueRemoval");
        process(
            new Transformer<Void, Object>() {
                public Void visitVariable(VariableTree node, Object p) {
                    super.visitVariable(node, p);
                    if ("removeInitialValueField".contentEquals(node.getName())) {
                        VariableTree vt = make.Variable(
                                node.getModifiers(), 
                                node.getName(),
                                node.getType(),
                                null);
                        copy.rewrite(node, vt);
                    }
                    return null;
                }
            }
        );
        assertFiles("testFieldInitialValueRemoval.pass");
    }
                    
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
        return "org/netbeans/jmi/javamodel/codegen/FieldTest1/FieldTest1/";
    }
}
