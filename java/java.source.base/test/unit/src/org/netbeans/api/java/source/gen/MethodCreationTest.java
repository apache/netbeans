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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.modules.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Pavel Flaska
 */
public class MethodCreationTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of MethodCreationTest */
    public MethodCreationTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(MethodCreationTest.class);
//        suite.addTest(new MethodCreationTest("testAddFirst"));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
//        suite.addTest(new MethodCreationTest(""));
        return suite;
    }

    /*
     * create the method:
     *
     * public <T> void taragui(List menta, Object carqueja, int dulce, boolean compuesta) throws IOException {
     * }
     */
    public void testAddFirst() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test {\n\n" +
            "    public <T> void taragui(List menta, T carqueja, int dulce, boolean compuesta, boolean logrando) throws IOException {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                @Override
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        List<VariableTree> parametersList = new ArrayList<VariableTree>(5);
                        ModifiersTree mods = make.Modifiers(EnumSet.noneOf(Modifier.class));
                        parametersList.add(make.Variable(mods, "menta", make.Identifier("List"), null));
                        parametersList.add(make.Variable(mods, "carqueja", make.Identifier("T"), null));
                        parametersList.add(make.Variable(mods, "dulce", make.PrimitiveType(TypeKind.INT), null));
                        parametersList.add(make.Variable(mods, "compuesta", make.PrimitiveType(TypeKind.BOOLEAN), null));
                        parametersList.add(make.Variable(mods, "logrando", make.PrimitiveType(TypeKind.BOOLEAN), null));
                        MethodTree newMethod = make.Method(
                                make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)), // modifiers - public
                                "taragui",  // name - targui
                                make.PrimitiveType(TypeKind.VOID), // return type - void
                                Collections.<TypeParameterTree>singletonList(make.TypeParameter("T", Collections.<ExpressionTree>emptyList())), // type parameter - <T>
                                parametersList, // parameters
                                Collections.<ExpressionTree>singletonList(make.Identifier("IOException")), // throws
                                make.Block(Collections.<StatementTree>emptyList(), false),
                                null // default value - not applicable
                        ); 
                        ClassTree copy = make.addClassMember(
                            node, newMethod
                        );
                        this.copy.rewrite(node, copy);
                    }
                    return null;
                }
            }
        
        );
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /*
     * create the method:
     *
     * public <T> void taragui(List menta, Object carqueja, int dulce, boolean compuesta) throws IOException {
     * }
     */
    public void testAddFirstWithVarArgs() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test {\n\n" +
            "    public <T> void taragui(List menta, T carqueja, int dulce, boolean compuesta, String... strs) throws IOException {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                @Override
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        List<VariableTree> parametersList = new ArrayList<VariableTree>(5);
                        ModifiersTree mods = make.Modifiers(EnumSet.noneOf(Modifier.class));
                        parametersList.add(make.Variable(mods, "menta", make.Identifier("List"), null));
                        parametersList.add(make.Variable(mods, "carqueja", make.Identifier("T"), null));
                        parametersList.add(make.Variable(mods, "dulce", make.PrimitiveType(TypeKind.INT), null));
                        parametersList.add(make.Variable(mods, "compuesta", make.PrimitiveType(TypeKind.BOOLEAN), null));
//                        mods = make.Modifiers(EnumSet.noneOf(Modifier.class));
                        parametersList.add(make.Variable(mods, "strs", make.ArrayType(make.Identifier("String")), null));                        
//                        parametersList.add(make.Variable(mods, "logrando", make.PrimitiveType(TypeKind.BOOLEAN), null));
                        MethodTree newMethod = make.Method(
                                make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)), // modifiers - public
                                "taragui",  // name - targui
                                make.PrimitiveType(TypeKind.VOID), // return type - void
                                Collections.<TypeParameterTree>singletonList(make.TypeParameter("T", Collections.<ExpressionTree>emptyList())), // type parameter - <T>
                                parametersList, // parameters
                                Collections.<ExpressionTree>singletonList(make.Identifier("IOException")), // throws
                                make.Block(Collections.<StatementTree>emptyList(), false),
                                null, // default value - not applicable
                                true
                        ); 
                        ClassTree copy = make.addClassMember(
                            node, newMethod
                        );
                        this.copy.rewrite(node, copy);
                    }
                    return null;
                }
            }
        
        );
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }
    
    String getSourcePckg() {
        return "";
    }
   
}
