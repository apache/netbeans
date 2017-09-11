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

package org.netbeans.modules.editor.java;

import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 * Java formatter tests.
 *
 * @autor Miloslav Metelka
 */
public class JavaFormatterArrayInitOrEnumUnitTest extends JavaFormatterUnitTestCase {

    public JavaFormatterArrayInitOrEnumUnitTest(String testMethodName) {
        super(testMethodName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Lookup.getDefault().lookup(ModuleInfo.class);
    }

    public void testReformatIntArray() {
        setLoadDocumentText(
                "void m() {\n" +
                "int[] array = {\n" +
                "1, 2, 3};\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    int[] array = {\n" +
                "        1, 2, 3};\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatStringArray() {
        setLoadDocumentText(
                "void m() {\n" +
                "String[] array = {\n" +
                "\"first\",\n" +
                "\"second\"\n" +
                "};\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    String[] array = {\n" +
                "        \"first\",\n" +
                "        \"second\"\n" +
                "    };\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatStringArrayExtraComma() {
        setLoadDocumentText(
                "void m() {\n" +
                "String[] array = {\n" +
                "\"first\",\n" +
                "\"second\",\n" +
                "};\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    String[] array = {\n" +
                "        \"first\",\n" +
                "        \"second\",\n" +
                "    };\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatStringArrayRBraceOnSameLine() {
        setLoadDocumentText(
                "void m() {\n" +
                "String[] array = {\n" +
                "\"first\",\n" +
                "\"second\"};\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    String[] array = {\n" +
                "        \"first\",\n" +
                "        \"second\"};\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatNewObjectArray() {
        setLoadDocumentText(
                "void m() {\n" +
                "Object[] array = {\n" +
                "new Object(),\n" +
                "new String(\"second\"),\n" +
                "new Object()\n" +
                "};\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    Object[] array = {\n" +
                "        new Object(),\n" +
                "        new String(\"second\"),\n" +
                "        new Object()\n" +
                "    };\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatNewObjectArrayMultiLine() {
        setLoadDocumentText(
                "void m() {\n" +
                "Object[] array = {\n" +
                "new Object(),\n" +
                "new String(\n" +
                "\"second\"),\n" +
                "new Object()\n" +
                "};\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    Object[] array = {\n" +
                "        new Object(),\n" +
                "        new String(\n" +
                "                \"second\"),\n" +
                "        new Object()\n" +
                "    };\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatStringArrayArgument() {
        setLoadDocumentText(
                "void m() {\n" +
                "a(new String[] {\n" +
                "\"first\",\n" +
                "\"second\"\n" +
                "});\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    a(new String[] {\n" +
                "        \"first\",\n" +
                "        \"second\"\n" +
                "    });\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatObjectArrayArgument() {
        setLoadDocumentText(
                "void m() {\n" +
                "a(new Object[] {\n" +
                "new Object(),\n" +
                "\"second\"\n" +
                "});\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "void m() {\n" +
                "    a(new Object[] {\n" +
                "        new Object(),\n" +
                "        \"second\"\n" +
                "    });\n" +
                "    f();\n" +
                "}\n"
        );
    }
    
    public void testReformatMultiArray() {
        setLoadDocumentText(
                "static int[][] CONVERT_TABLE={\n" +
                "{1,2},{2,3},\n" +
                "{3,4},{4,5},{5,6},\n" +
                "{6,7},{7,8},{8,9}};\n" +
                "f();\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "static int[][] CONVERT_TABLE={\n" +
                "    {1,2},{2,3},\n" +
                "    {3,4},{4,5},{5,6},\n" +
                "    {6,7},{7,8},{8,9}};\n" +
                "f();\n"
        );
    }

    
    public void testReformatSimpleEnum() {
        setLoadDocumentText(
                "public enum SimpleEnum {\n" +
                "ONE,\n" +
                "TWO,\n" +
                "THREE\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect simple enum reformating",
                "public enum SimpleEnum {\n" +
                "    ONE,\n" +
                "    TWO,\n" +
                "    THREE\n" +
                "}\n");
    }
    
    public void testReformatNestedEnum() {
        setLoadDocumentText(
                "public enum SimpleEnum {\n" +
                "ONE,\n" +
                "TWO,\n" +
                "THREE;\n" +
                "public enum NestedEnum {\n" +
                "A,\n" +
                "B\n" +
                "}\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect nested enum reformating",
                "public enum SimpleEnum {\n" +
                "    ONE,\n" +
                "    TWO,\n" +
                "    THREE;\n" +
                "    public enum NestedEnum {\n" +
                "        A,\n" +
                "        B\n" +
                "    }\n" +
                "}\n");
    }
    
    public void testReformatComplexEnum() {
        setLoadDocumentText(
                "public enum ComplexEnum {\n" +
                "ONE,\n" +
                "TWO {\n" +
                "public void op(int a,\n" +
                "int b,\n" +
                "int c) {\n" +
                "}\n" +
                "public class Inner {\n" +
                "int a, b,\n" +
                "c,\n" +
                "d;\n" +
                "}\n" +
                "},\n" +
                "THREE\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect complex enum reformating",
                "public enum ComplexEnum {\n" +
                "    ONE,\n" +
                "    TWO {\n" +
                "        public void op(int a,\n" +
                "                int b,\n" +
                "                int c) {\n" +
                "        }\n" +
                "        public class Inner {\n" +
                "            int a, b,\n" +
                "                    c,\n" +
                "                    d;\n" +
                "        }\n" +
                "    },\n" +
                "    THREE\n" +
                "}\n");
    }
    
}
