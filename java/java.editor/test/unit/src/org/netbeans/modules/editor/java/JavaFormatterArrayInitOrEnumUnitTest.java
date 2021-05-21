/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
