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

import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.java.ui.FmtOptions;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 * Java formatter tests.
 *
 * @author Miloslav Metelka
 */
public class JavaFormatterArrayInitOrEnumUnitTest extends JavaFormatterUnitTestCase {

    public JavaFormatterArrayInitOrEnumUnitTest(String testMethodName) {
        super(testMethodName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Lookup.getDefault().lookup(ModuleInfo.class);
        Preferences prefs = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
        prefs.putInt(FmtOptions.blankLinesBeforeClass, 0);
        prefs.putInt(FmtOptions.blankLinesBeforeMethods, 0);
        prefs.putInt(FmtOptions.blankLinesAfterClassHeader, 0);
        prefs.putBoolean(FmtOptions.spaceBeforeArrayInitLeftBrace, true);
    }

    public void testReformatIntArray() {
        setLoadDocumentText(
                "public class Test {\n" +
                "void m() {\n" +
                "int[] array = {\n" +
                "1, 2, 3};\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "public class Test {\n" +
                "    void m() {\n" +
                "        int[] array = {\n" +
                "            1, 2, 3};\n" +
                "        f();\n" +
                "    }\n"
        );
    }
    
    public void testReformatStringArray() {
        setLoadDocumentText(
                "public class Test {\n" +
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
                "public class Test {\n" +
                "    void m() {\n" +
                "        String[] array = {\n" +
                "            \"first\",\n" +
                "            \"second\"\n" +
                "        };\n" +
                "        f();\n" +
                "    }\n"
        );
    }
    
    public void testReformatStringArrayExtraComma() {
        setLoadDocumentText(
                "public class Test {\n" +
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
                "public class Test {\n" +
                "    void m() {\n" +
                "        String[] array = {\n" +
                "            \"first\",\n" +
                "            \"second\",};\n" +
                "        f();\n" +
                "    }\n"
        );
        //TODO: original expected text:
//        assertDocumentText("Incorrect multi-array && multi-line reformating",
//                "public class Test {\n" +
//                "    void m() {\n" +
//                "        String[] array = {\n" +
//                "            \"first\",\n" +
//                "            \"second\",\n" +
//                "        };\n" +
//                "        f();\n" +
//                "    }\n"
//        );
    }
    
    public void testReformatStringArrayRBraceOnSameLine() {
        setLoadDocumentText(
                "public class Test {\n" +
                "void m() {\n" +
                "String[] array = {\n" +
                "\"first\",\n" +
                "\"second\"};\n" +
                "f();\n" +
                "}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "public class Test {\n" +
                "    void m() {\n" +
                "        String[] array = {\n" +
                "            \"first\",\n" +
                "            \"second\"};\n" +
                "        f();\n" +
                "    }\n"
        );
    }
    
    public void testReformatNewObjectArray() {
        setLoadDocumentText(
                "public class Test {\n" +
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
                "public class Test {\n" +
                "    void m() {\n" +
                "        Object[] array = {\n" +
                "            new Object(),\n" +
                "            new String(\"second\"),\n" +
                "            new Object()\n" +
                "        };\n" +
                "        f();\n" +
                "    }\n"
        );
    }
    
    public void testReformatNewObjectArrayMultiLine() {
        setLoadDocumentText(
                "public class Test {\n" +
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
                "public class Test {\n" +
                "    void m() {\n" +
                "        Object[] array = {\n" +
                "            new Object(),\n" +
                "            new String(\n" +
                "            \"second\"),\n" +
                "            new Object()\n" +
                "        };\n" +
                "        f();\n" +
                "    }\n"
        );
        //TODO: original expected text:
//        assertDocumentText("Incorrect multi-array && multi-line reformating",
//                "public class Test {\n" +
//                "    void m() {\n" +
//                "        Object[] array = {\n" +
//                "            new Object(),\n" +
//                "            new String(\n" +
//                "                    \"second\"),\n" +
//                "            new Object()\n" +
//                "        };\n" +
//                "        f();\n" +
//                "    }\n"
//        );
    }
    
    public void testReformatStringArrayArgument() {
        setLoadDocumentText(
                "public class Test {\n" +
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
                "public class Test {\n" +
                "    void m() {\n" +
                "        a(new String[] {\n" +
                "            \"first\",\n" +
                "            \"second\"\n" +
                "        });\n" +
                "        f();\n" +
                "    }\n"
        );
    }
    
    public void testReformatObjectArrayArgument() {
        setLoadDocumentText(
                "public class Test {\n" +
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
                "public class Test {\n" +
                "    void m() {\n" +
                "        a(new Object[] {\n" +
                "            new Object(),\n" +
                "            \"second\"\n" +
                "        });\n" +
                "        f();\n" +
                "    }\n"
        );
    }
    
    public void testReformatMultiArray() {
        setLoadDocumentText(
                "public class Test {\n" +
                "static int[][] CONVERT_TABLE={\n" +
                "{1,2},{2,3},\n" +
                "{3,4},{4,5},{5,6},\n" +
                "{6,7},{7,8},{8,9}};\n" +
                "void f() {}\n"
        );
        reformat();
        assertDocumentText("Incorrect multi-array && multi-line reformating",
                "public class Test {\n" +
                "    static int[][] CONVERT_TABLE = {\n" +
                "        {1, 2}, {2, 3},\n" +
                "        {3, 4}, {4, 5}, {5, 6},\n" +
                "        {6, 7}, {7, 8}, {8, 9}};\n" +
                "    void f() {\n" +
                "    }\n"
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

    @Override
    protected BaseDocument createDocument() {
        try {
            FileObject file = FileUtil.createMemoryFileSystem().getRoot().createData("Test", "java");
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec.openDocument();
            doc.putProperty(Language.class, JavaTokenId.language());
            return (BaseDocument) doc;
        } catch (IOException ex) {
            throw new AssertionError("Unexpected: ", ex);
        }
    }

}
