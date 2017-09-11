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
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import junit.textui.TestRunner;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.modules.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests indentation of newly added elements
 * @author Max Sauer
 */
public class IndentAddedElemTest extends GeneratorTestBase {
    
    public IndentAddedElemTest(String name) {
        super(name);
    }
    
    /**
     * Adds tests to suite
     * @return created suite
     */
    public static NbTestSuite suite() {
        //NbTestSuite suite = new NbTestSuite();
        NbTestSuite suite = new NbTestSuite(IndentAddedElemTest.class);
        //suite.addTest(new IndentMethodTest("testAddMethodToEmpty"));
        return suite;
    }
    
    /**
     * Adding of methods
     */
    public void testAddMethodToEmpty() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package com.max.test.alfa;\n\n" +
                "public class Test {\n" +
                "}\n"
                );
        String golden =
                "package com.max.test.alfa;\n\n" +
                "public class Test {\n" +
                "\n" +
                "    public double Eval(int param) {\n" +
                "    }\n" +
                "}\n";
        
        process(
                new Transformer<Void, Object>() {
            @Override
            public Void visitClass(ClassTree node, Object p) {
                super.visitClass(node, p);
                if ("Test".contentEquals(node.getSimpleName())) {
                    ModifiersTree parMods = make.Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
                    VariableTree param = make.Variable(parMods, "param", make.PrimitiveType(TypeKind.INT), null);
                    List<VariableTree> parList = new ArrayList<VariableTree>(1);
                    parList.add(param);
                    MethodTree member = make.Method(
                            make.Modifiers(
                            Collections.singleton(Modifier.PUBLIC), // modifiers
                            Collections.<AnnotationTree>emptyList() // annotations
                            ), // modifiers and annotations
                            "Eval", // name
                            make.PrimitiveType(TypeKind.DOUBLE), // return type
                            Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                            parList, // parameters
                            Collections.<ExpressionTree>emptyList(), // throws
                            make.Block(Collections.<StatementTree>emptyList(), false), // empty statement block
                            null // default value - not applicable here, used by annotations
                            );
                    
                    ClassTree copy = make.addClassMember(node, member);
                    this.copy.rewrite(node, copy);
                }
                return null;
            }
        }
        
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    /**
     * Adding of fields
     */
    public void testAddFieldEmpty() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package com.max.test.alfa;\n\n" +
                "public class Test {\n" +
                "}\n"
                );
        String golden =
                "package com.max.test.alfa;\n\n" +
                "public class Test {\n" +
                "\n" +
                "    private double value;\n" +
                "}\n";
        
        process(
                new Transformer<Void, Object>() {
            @Override
            public Void visitClass(ClassTree node, Object p) {
                super.visitClass(node, p);
                if ("Test".contentEquals(node.getSimpleName())) {
                    ModifiersTree modTree = make.Modifiers(EnumSet.of(Modifier.PRIVATE));
                    VariableTree member = make.Variable(
                            make.Modifiers(
                                modTree,
                                Collections.<AnnotationTree>emptyList()
                            ),
                            "value", //name
                            make.PrimitiveType(TypeKind.DOUBLE), null
                            );
                    
                    ClassTree copy = make.addClassMember(node, member);
                    this.copy.rewrite(node, copy);
                }
                return null;
            }
        }
        
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void test113413a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package test;\n\n" +
                "public class Test {\n" +
                "  public void test() {\n" +
                "    new Runnable() {\n" +
                "        public void run() {\n" +
                "        }\n" +
                "    }\n" +
                "  }\n" +
                "}\n"
                );
        String golden =
                "package test;\n\n" +
                "public class Test {\n" +
                "  public void test() {\n" +
                "    new Runnable() {\n" +
                "        @Override\n" +
                "        public void run() {\n" +
                "        }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        
        process(
                new Transformer<Void, Object>() {
            @Override
            public Void visitMethod(MethodTree node, Object p) {
                super.visitMethod(node, p);
                if ("run".contentEquals(node.getName())) {
                    AnnotationTree annotation = make.Annotation(make.Identifier("Override"), Collections.<ExpressionTree>emptyList());
                    this.copy.rewrite(node.getModifiers(), make.addModifiersAnnotation(node.getModifiers(), annotation));
                }
                return null;
            }
        }
        
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println("res=" + res);
        assertEquals(golden, res);
    }
    
    public void test113413b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package test;\n\n" +
                "public class Test {\n" +
                "  public void test() {\n" +
                "    new Runnable() {\n" +
                "    }\n" +
                "  }\n" +
                "}\n"
                );
        String golden =
                "package test;\n\n" +
                "public class Test {\n" +
                "  public void test() {\n" +
                "    new Runnable() {\n" +
                "        @Override\n" +
                "        public void run() {\n" +
                "        }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        
        process(
                new Transformer<Void, Object>() {
            @Override
            public Void visitClass(ClassTree node, Object p) {
                super.visitClass(node, p);
                if ("".contentEquals(node.getSimpleName())) {
                    AnnotationTree annotation = make.Annotation(make.Identifier("Override"), Collections.<ExpressionTree>emptyList());
                    ModifiersTree mods = make.Modifiers(EnumSet.of(Modifier.PUBLIC), Collections.<AnnotationTree>singletonList(annotation));
                    MethodTree method = make.Method(mods, "run", make.PrimitiveType(TypeKind.VOID), Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), "{}", null);
                    this.copy.rewrite(node, make.addClassMember(node, method));
                }
                return null;
            }
        }
        
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println("res=" + res);
        assertEquals(golden, res);
    }
    
    public void test113413c() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package test;\n\n" +
                "public class Test {\n" +
                "  public void test() {\n" +
                "    new Runnable() {\n" +
                "      public void run() {\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n"
                );
        String golden =
                "package test;\n\n" +
                "public class Test {\n" +
                "  public void test() {\n" +
                "    new Runnable() {\n" +
                "      @Override\n" +
                "      public void run() {\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        
        process(
                new Transformer<Void, Object>() {
            @Override
            public Void visitMethod(MethodTree node, Object p) {
                super.visitMethod(node, p);
                if ("run".contentEquals(node.getName())) {
                    AnnotationTree annotation = make.Annotation(make.Identifier("Override"), Collections.<ExpressionTree>emptyList());
                    this.copy.rewrite(node.getModifiers(), make.addModifiersAnnotation(node.getModifiers(), annotation));
                }
                return null;
            }
        }
        
        );
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println("res=" + res);
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
        TestRunner.run(suite());
    }
    
}
