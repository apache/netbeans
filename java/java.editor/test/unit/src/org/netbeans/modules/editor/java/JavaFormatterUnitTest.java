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

package org.netbeans.modules.editor.java;

import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
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
public class JavaFormatterUnitTest extends JavaFormatterUnitTestCase {

    public JavaFormatterUnitTest(String testMethodName) {
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
    }

    // indent new line tests
    
    public void testJavadocEnterNothingAfterCaret() {
        setLoadDocumentText(
                "/**\n"
                + " * text|\n"
                + " */\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "/**\n"
                + " * text\n"
                + " * |\n"
                + " */\n"
                );
        
    }
    
    public void testJavadocEnterTextAfterCaret() {
        setLoadDocumentText(
                "/**\n"
                + " * break|text\n"
                + " */\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "/**\n"
                + " * break\n"
                + " * |text\n"
                + " */\n"
                );
        
    }
    
    public void testJavadocEnterStarAfterCaret() {
        setLoadDocumentText(
                "/**\n"
                + " * text|*/\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "/**\n"
                + " * text\n"
                + " |*/\n"
                );
        
    }
    
    public void testEnterInMultiLineSystemOutPrintln() {
        setLoadDocumentText(
                "public class Test {\n" +
                "void m() {\n"
                + "    System.out.println(|\n"
                + "\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "public class Test {\n" +
                "void m() {\n"
                + "    System.out.println(\n"
                + "            |\n"
                + "\n"
                );
        
    }
    
    public void testEnterInMultiLineSystemOutPrintlnLineThree() {
        setLoadDocumentText(
                "public class Test {\n" +
                "void m() {\n"
                + "    System.out.println(\n"
                + "            \"haf\"|\n"
                + "\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "public class Test {\n" +
                "void m() {\n"
                + "    System.out.println(\n"
                + "            \"haf\"\n"
                + "            |\n"
                + "\n"
                );
        
    }
    
    public void testEnterInMultiLineSystemOutPrintlnAfterSemiColon() {
        setLoadDocumentText(
                "public class Test {\n" +
                "void m() {\n"
                + "    System.out.println(\n"
                + "            \"haf\");|\n"
                + "\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "public class Test {\n" +
                "void m() {\n"
                + "    System.out.println(\n"
                + "            \"haf\");\n"
                + "    |\n"
                + "\n"
                );
        
    }
    
    public void testEnterInMultiLineClassDeclaration() {
        setLoadDocumentText(
                "public class C\n"
                + "        extends Exception\n"
                + "        implements Runnable {|\n"
                + "}\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "public class C\n"
                + "        extends Exception\n"
                + "        implements Runnable {\n"
                + "    |\n"
                + "}\n"
                );
        
    }
    
    public void testEnterAfterIf() {
        setLoadDocumentText(
                "public class Test {\n" +
                "    void test() {\n" +
                "if (true)|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "public class Test {\n" +
                "    void test() {\n" +
                "if (true)\n" +
                "    |\n"
                );
    }
    
    public void testEnterAfterFor() {
        setLoadDocumentText(
                "public class Test {\n" +
                "    void test() {\n" +
                "for (int i = 0; i < 10; i++)|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "public class Test {\n" +
                "    void test() {\n" +
                "for (int i = 0; i < 10; i++)\n" +
                "    |\n"
                );
    }
    
    public void testEnterAfterWhile() {
        setLoadDocumentText(
                "public class Test {\n" +
                "    void test() {\n" +
                "while (true)|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "public class Test {\n" +
                "    void test() {\n" +
                "while (true)\n" +
                "    |\n"
                );
    }
    
    public void testEnterAfterDo() {
        setLoadDocumentText(
                "public class Test {\n" +
                "    void test() {\n" +
                "        do|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "public class Test {\n" +
                "    void test() {\n" +
                "        do\n" +
                "            |\n"
                );
    }
    
    
    public void testEnterAfterIfStmt() {
        setLoadDocumentText(
                "public class Test {\n" +
                "    void test() {\n" +
                "        if (true)\n" +
                "            stmt;|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "public class Test {\n" +
                "    void test() {\n" +
                "        if (true)\n" +
                "            stmt;\n" +
                "        |\n"
                );
    }
    
    public void testEnterAfterIfElse() {
        setLoadDocumentText(
                "public class Test {\n" +
                "    void test() {\n" +
                "        if (true)\n" +
                "            stmt;\n" +
                "        else|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "public class Test {\n" +
                "    void test() {\n" +
                "        if (true)\n" +
                "            stmt;\n" +
                "        else\n" +
                "            |\n"
                );
    }
    
    public void testEnterAfterIfElseStmt() {
        setLoadDocumentText(
                "public class Test {\n" +
                "    void test() {\n" +
                "        if (true)\n" +
                "            stmt;\n" +
                "        else\n" +
                "            stmt;|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "public class Test {\n" +
                "    void test() {\n" +
                "        if (true)\n" +
                "            stmt;\n" +
                "        else\n" +
                "            stmt;\n" +
                "        |\n"
                );
    }
    
    public void testEnterAfterIfMultiLine() {
        setLoadDocumentText(
                "public class Test {\n" +
                "    void test() {\n" +
                "if (1 < 5|\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "public class Test {\n" +
                "    void test() {\n" +
                "if (1 < 5\n" +
                "        |\n"
                );
    }
    
    public void testEnterAfterIfMultiLine2() {
        setLoadDocumentText(
                "public class Test {\n" +
                "    void test() {\n" +
                "if (1 < 5|)\n"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line indent",
                "public class Test {\n" +
                "    void test() {\n" +
                "if (1 < 5\n" +
                "        |)\n"
                );
    }
    
    // -------- Reformat tests -----------
    
    public void testReformatMultiLineSystemOutPrintln() {
        setLoadDocumentText(
                "public class Test {\n" +
                "void m() {\n"
                + "    System.out.println(\n"
                + "    \"haf\");\n"
                + "}\n"
                + "}\n"
                );
        reformat();
        assertDocumentText("Incorrect new-line indent",
                "public class Test {\n" +
                "    void m() {\n"
                + "        System.out.println(\n"
                + "                \"haf\");\n"
                + "    }\n"
                + "}\n"
                );
        
    }
    
    public void testReformatMultiLineClassDeclaration() {
        setLoadDocumentText(
                "public class C\n"
                + "extends Exception\n"
                + "implements Runnable {|\n"
                + "{ System.out.println(\"haf\"); }\n"
                + "}\n"
                );
        reformat();
        assertDocumentText("Incorrect new-line indent",
                "public class C\n"
                + "        extends Exception\n"
                + "        implements Runnable {\n"
                + "    {\n"
                + "        System.out.println(\"haf\");\n"
                + "    }\n"
                + "}\n"
                );
        
    }
    
    // tests for regressions
    
    /**
     * Tests reformatting of new on two lines
     * @see http://www.netbeans.org/issues/show_bug.cgi?id6065
     */
    public void testReformatNewOnTwoLines() {
        setLoadDocumentText(
                "public class Test {\n" +
                "    void test() {\n" +
                "javax.swing.JPanel =\n" +
                "new javax.swing.JPanel();\n" +
                "    }\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect new on two lines reformating",
                "public class Test {\n" +
                "    void test() {\n" +
                "        javax.swing.JPanel\n" +
                "                = new javax.swing.JPanel();\n" +
                "    }\n" +
                "}\n");
    }
    
    /**
     * Tests reformatting of ternary conditional operators on multiple lines
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=23508
     */
    public void testReformatTernaryConditionalOperator() {
        setLoadDocumentText(
                "public class Test {\n" +
                "    void test() {\n" +
                "something = (someComplicatedExpression != null) ?\n" +
                "(aComplexCalculation) :\n" +
                "(anotherComplexCalculation);\n" +
                "    }\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect ternary conditional operator reformatting",
                "public class Test {\n" +
                "    void test() {\n" +
                "        something = (someComplicatedExpression != null)\n" +
                "                ? (aComplexCalculation)\n" +
                "                : (anotherComplexCalculation);\n" +
                "    }\n" +
                "}\n");
    }
    
    
    /**
     * Test reformatting of array initializer with newlines on
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=47069
     */
    public void testReformatArrayInitializerWithNewline() {
        Preferences prefs = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
        String originalPlacement = prefs.get(FmtOptions.methodDeclBracePlacement, FmtOptions.getDefaultAsString(FmtOptions.methodDeclBracePlacement));
        assertTrue(!originalPlacement.equals(CodeStyle.BracePlacement.NEW_LINE.toString()));
        prefs.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE.toString());
        boolean originalSpaceBeforeInit = prefs.getBoolean(FmtOptions.spaceBeforeArrayInitLeftBrace, FmtOptions.getDefaultAsBoolean(FmtOptions.spaceBeforeArrayInitLeftBrace));
        prefs.putBoolean(FmtOptions.spaceBeforeArrayInitLeftBrace, true);
        setLoadDocumentText(
                "public class Test {\n" +
                "    void test() {\n" +
                "int[] foo = new int[] {1, 2, 3};\n" +
                "    }\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect array initializer with newline reformatting",
                "public class Test {\n" +
                "    void test()\n" +
                "    {\n" +
                "        int[] foo = new int[] {1, 2, 3};\n" +
                "    }\n" +
                "}\n");
        prefs.put(FmtOptions.methodDeclBracePlacement, originalPlacement);
        prefs.putBoolean(FmtOptions.spaceBeforeArrayInitLeftBrace, originalSpaceBeforeInit);
    }
    
    /**
     * Test reformatting of newline braces to normal ones
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=48926
     */
    public void testReformatNewlineBracesToNormalOnes() {
        setLoadDocumentText(
                "public class Test {\n" +
                "    void test() {\n" +
                "try\n" +
                "{\n" +
                "System.out.println(\"test\");\n" +
                "}\n" +
                "catch (ClassCastException e)\n" +
                "{\n" +
                "System.err.println(\"exception\");\n" +
                "}\n" +
                "    }\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect array initializer with newline reformatting",
                "public class Test {\n" +
                "    void test() {\n" +
                "        try {\n" +
                "            System.out.println(\"test\");\n" +
                "        } catch (ClassCastException e) {\n" +
                "            System.err.println(\"exception\");\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
    }
    
    /**
     * Test reformatting of multiline constructors
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=49450
     */
    public void testReformatMultilineConstructor() {
        setLoadDocumentText(
                "public class Test {\n" +
                "public Test(int one,\n" +
                "int two,\n" +
                "int three,\n" +
                "int four) {\n" +
                "this.one = one;\n" +
                "}\n" +
                "}");
        reformat();
        assertDocumentText("Incorrect multiline constructor reformatting",
                "public class Test {\n" +
                "    public Test(int one,\n" +
                "            int two,\n" +
                "            int three,\n" +
                "            int four) {\n" +
                "        this.one = one;\n" +
                "    }\n" +
                "}\n");
    }
    
    /**
     * Test reformatting of if else without brackets
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=50523
     */
    public void testReformatIfElseWithoutBrackets() {
        setLoadDocumentText(
                "public class Test {\n" +
                "    void test() {\n" +
                "if (count == 0)\n" +
                "return 0.0f;\n" +
                "else\n" +
                "return performanceSum / getCount();\n" +
                "    }\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect reformatting of if-else without brackets",
                "public class Test {\n" +
                "    void test() {\n" +
                "        if (count == 0) {\n" +
                "            return 0.0f;\n" +
                "        } else {\n" +
                "            return performanceSum / getCount();\n" +
                "        }\n" +
                "    }\n" +
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
