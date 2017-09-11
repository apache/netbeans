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
        System.err.println(res);
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
