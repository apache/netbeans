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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.modules.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests correct fields addition and matching mechanism.
 * 
 * @author Pavel Flaska
 */
public class Field6Test extends GeneratorTestBase {
    
    /** Creates a new instance of MethodParametersTest */
    public Field6Test(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
//         suite.addTestSuite(Field6Test.class);
        suite.addTest(new Field6Test("testAddFieldToIndex0"));
        suite.addTest(new Field6Test("testRemoveInitialValue"));
        suite.addTest(new Field6Test("testAddFirstParameter"));
        return suite;
    }
    
    public void testAddFieldToIndex0() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "\n" +
            "    int field1;\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    ModifiersTree mods = make.Modifiers(Collections.<Modifier>emptySet());
                    PrimitiveTypeTree type = make.PrimitiveType(TypeKind.INT);
                    VariableTree var = make.Variable(mods, "field1", type, null);
                    ClassTree copy = make.insertClassMember(node, 0, var);
                    this.copy.rewrite(node, copy);
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testRemoveInitialValue() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    int removeInitialValueField = null;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    int removeInitialValueField;\n" +
            "}\n";

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
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirstParameter() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui(int equilibrio) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("taragui".contentEquals(node.getName())) {
                        VariableTree vt = make.Variable(
                                make.Modifiers(Collections.<Modifier>emptySet()),
                                "equilibrio",
                                make.PrimitiveType(TypeKind.INT),
                                null);
                        MethodTree copy = make.addMethodParameter(node, vt);
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
