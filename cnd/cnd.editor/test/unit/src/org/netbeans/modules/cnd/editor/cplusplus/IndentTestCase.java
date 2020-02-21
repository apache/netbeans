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

package org.netbeans.modules.cnd.editor.cplusplus;

import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.options.EditorOptions;

/**
 * Class was taken from java
 * Links point to java IZ.
 * C/C++ specific tests begin from testReformatSimpleClass
 *
 */
public class IndentTestCase extends EditorBase {

    public IndentTestCase(String testMethodName) {
        super(testMethodName);
    }

    // indent new line tests
    
    public void testJavadocEnterNothingAfterCaret() {
        setDefaultsOptions();
        typeCharactersInText(
                "/**\n"
                + " * text|\n"
                + " */\n", // "Incorrect new-line indent",
                "\n",
                "/**\n"
                + " * text\n"
                + " * |\n"
                + " */\n"
                );
        
    }
    
    public void testJavadocEnterTextAfterCaret() {
        setDefaultsOptions();
        typeCharactersInText(
                "/**\n"
                + " * break|text\n"
                + " */\n", // "Incorrect new-line indent",
                "\n",
                "/**\n"
                + " * break\n"
                + " * |text\n"
                + " */\n"
                );
        
    }
    
    public void testJavadocEnterStarAfterCaret() {
        setDefaultsOptions();
        typeCharactersInText(
                "/**\n"
                + " * text|*/\n", // "Incorrect new-line indent",
                "\n",
                "/**\n"
                + " * text\n"
                + " |*/\n"
                );
        
    }

    public void testJavadocStarTyping() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "/**\n" +
                "|\n" +
                " */\n",
                "h",
                "/**\n"
                + " * h|\n"
                + " */\n"
                );
    }
    
    public void testEnterInMultiLineSystemOutPrintln() {
        setDefaultsOptions();
        typeCharactersInText(
                "void m() {\n"
                + "    printf(|\n"
                + "\n", // "Incorrect new-line indent",
                "\n",
                "void m() {\n"
                + "    printf(\n"
                + "            |\n"
                + "\n"
                );
        
    }
    
    public void testEnterInMultiLineSystemOutPrintlnLineThree() {
        setDefaultsOptions();
        typeCharactersInText(
                "void m() {\n"
                + "    printf(\n"
                + "            \"haf\"|\n"
                + "\n", // "Incorrect new-line indent",
                "\n",
                "void m() {\n"
                + "    printf(\n"
                + "            \"haf\"\n"
                + "            |\n"
                + "\n"
                );
        
    }
    
    public void testEnterInMultiLineSystemOutPrintlnAfterSemiColon() {
        setDefaultsOptions();
        typeCharactersInText(
                "void m() {\n"
                + "    printf(\n"
                + "            \"haf\");|\n"
                + "\n", // "Incorrect new-line indent",
                "\n",
                "void m() {\n"
                + "    printf(\n"
                + "            \"haf\");\n"
                + "    |\n"
                + "\n"
                );
        
    }
    
//    public void testEnterInMultiLineClassDeclaration() {
//        typeCharactersInText(
//                "public class C\n"
//                + "        : Runnable\n {|\n"
//                + "}\n"
//                );
//        indentNewLine();
//        assertDocumentTextAndCaret("Incorrect new-line indent",
//                "public class C\n"
//                + "        : Runnable {\n"
//                + "    |\n"
//                + "}\n"
//                );
//        
//    }
    
    public void testEnterAfterIf() {
        setDefaultsOptions();
        typeCharactersInText(
                "if (true)|\n", // "Incorrect new-line indent",
                "\n",
                "if (true)\n"
                + "    |\n"
                );
    }

    public void testEnterAfterIfHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        typeCharactersInText(
                "if (true)|\n", // "Incorrect new-line indent",
                "\n",
                "if (true)\n"
                + "  |\n"
                );
    }

    public void testEnterAfterIfBraceHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        typeCharactersInText(
                "if (true)\n" +
                "  {|\n" +
                "  }\n", // "Incorrect new-line indent",
                "\n",
                "if (true)\n" +
                "  {\n" +
                "    |\n" +
                "  }\n" 
                );
    }

    public void testEnterAfterIfBraceHalf2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        typeCharactersInText(
                "int foo()\n" +
                "{\n" +
                "  if (true)\n" +
                "    {|\n" +
                "    }\n" +
                "}\n", // "Incorrect new-line indent",
                "\n",
                "int foo()\n" +
                "{\n" +
                "  if (true)\n" +
                "    {\n" +
                "      |\n" +
                "    }\n" +
                "}\n"
                );
    }
    
    public void testEnterAfterFor() {
        setDefaultsOptions();
        typeCharactersInText(
                "for (int i = 0; i < 10; i++)|\n", // "Incorrect new-line indent",
                "\n",
                "for (int i = 0; i < 10; i++)\n"
                + "    |\n"
                );
    }

    public void testEnterAfterForHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        typeCharactersInText(
                "for (int i = 0; i < 10; i++)|\n", // "Incorrect new-line indent",
                "\n",
                "for (int i = 0; i < 10; i++)\n"
                + "  |\n"
                );
    }
    
    public void testEnterAfterWhile() {
        setDefaultsOptions();
        typeCharactersInText(
                "while (true)|\n", // "Incorrect new-line indent",
                "\n",
                "while (true)\n"
                + "    |\n"
                );
    }

    public void testEnterAfterWhileHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        typeCharactersInText(
                "while (true)|\n", // "Incorrect new-line indent",
                "\n",
                "while (true)\n" +
                "  |\n"
                );
    }

    public void testEnterAfterDo() {
        setDefaultsOptions();
        typeCharactersInText(
                "do|\n", // "Incorrect new-line indent",
                "\n",
                "do\n"
                + "    |\n"
                );
    }
    
    public void testEnterAfterDoHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        typeCharactersInText(
                "do|\n", // "Incorrect new-line indent",
                "\n",
                "do\n" +
                "  |\n"
                );
    }

    public void testEnterAfterIfStmt() {
        setDefaultsOptions();
        typeCharactersInText(
                "if (true)\n"
                + "    stmt;|\n", // "Incorrect new-line indent",
                "\n",
                "if (true)\n"
                + "    stmt;\n"
                + "|\n"
                );
    }
    
    public void testEnterAfterIfStmtHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        typeCharactersInText(
                "if (true)\n"
                + "  stmt;|\n", // "Incorrect new-line indent",
                "\n",
                "if (true)\n"
                + "  stmt;\n"
                + "|\n"
                );
    }

    public void testEnterAfterIfElse() {
        setDefaultsOptions();
        typeCharactersInText(
                "if (true)\n"
                + "    stmt;\n"
                + "else|\n", // "Incorrect new-line indent",
                "\n",
                "if (true)\n"
                + "    stmt;\n"
                + "else\n"
                + "    |\n"
                );
    }

    public void testEnterAfterIfElseHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        typeCharactersInText(
                "if (true)\n"
                + "  stmt;\n"
                + "else|\n", // "Incorrect new-line indent",
                "\n",
                "if (true)\n"
                + "  stmt;\n"
                + "else\n"
                + "  |\n"
                );
    }
    
    public void testEnterAfterIfElseStmt() {
        setDefaultsOptions();
        typeCharactersInText(
                "if (true)\n"
                + "    stmt;\n"
                + "else\n"
                + "    stmt;|\n", // "Incorrect new-line indent",
                "\n",
                "if (true)\n"
                + "    stmt;\n"
                + "else\n"
                + "    stmt;\n"
                + "|\n"
                );
    }
    
    public void testEnterAfterIfMultiLine() {
        setDefaultsOptions();
        typeCharactersInText(
                "if (1 < 5|\n", // "Incorrect new-line indent",
                "\n",
                "if (1 < 5\n"
                + "        |\n"
                );
    }
    
    public void testEnterAfterIfMultiLine2() {
        setDefaultsOptions();
        typeCharactersInText(
                "if (1 < 5|)\n", // "Incorrect new-line indent",
                "\n",
                "if (1 < 5\n"
                + "        |)\n"
                );
    }
    
    /**
     * Test reformatting of unbalanced braces
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91561
     */
    public void testIdentUnbalancedBraces() {
        setDefaultsOptions();
        typeCharactersInText(
            "void foo() {\n" +
            "#if A\n" +
            "    if (0) {\n" +
            "#else\n" +
            "    if (1) {\n" +
            "#endif|\n" +
            "    }\n" +
            "}\n", // "Incorrect identing of unbalanced braces",
            "\n",
            "void foo() {\n" +
            "#if A\n" +
            "    if (0) {\n" +
            "#else\n" +
            "    if (1) {\n" +
            "#endif\n" +
            "        \n" +
            "    }\n" +
            "}\n");
    }

    /**
     * Test reformatting of unbalanced braces
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91561
     */
    public void testIdentUnbalancedBraces2() {
        setDefaultsOptions();
        typeCharactersInText(
            "void foo() {\n" +
            "#if A\n" +
            "    if (0) {\n" +
            "#else\n" +
            "    if (1) {\n" +
            "#endif\n" +
            "    }|\n" +
            "}\n", // "Incorrect identing of unbalanced braces",
            "\n",
            "void foo() {\n" +
            "#if A\n" +
            "    if (0) {\n" +
            "#else\n" +
            "    if (1) {\n" +
            "#endif\n" +
            "    }\n" +
            "    \n" +
            "}\n");
    }

//    /**
//     * Test reformatting of unbalanced braces
//     * @see http://www.netbeans.org/issues/show_bug.cgi?id=91561
//     */
//    public void testIdentUnbalancedBraces3() {
//        typeCharactersInText(
//            "void foo() {\n" +
//            "#if A\n" +
//            "    if (0) {\n" +
//            "#else\n" +
//            "    if (1) {\n" +
//            "#endif\n" +
//            "    }\n" +
//            "|}\n");
//        indentNewLine();
//        assertDocumentText("Incorrect identing of unbalanced braces",
//            "void foo() {\n" +
//            "#if A\n" +
//            "    if (0) {\n" +
//            "#else\n" +
//            "    if (1) {\n" +
//            "#endif\n" +
//            "    }\n" +
//            "\n" + 
//            "}\n");
//    }
    
    
    public void testIdentMain() {
        setCppEditorKit(false);
        setDefaultsOptions();
        typeCharactersInText(
            "int main() {|\n", // "Incorrect identing of main",
            "\n",
            "int main() {\n" +
            "    |\n" +
            "}\n");
    }

    public void testIdentMainHalf() {
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        typeCharactersInText(
            "int main() {|\n", // "Incorrect identing of main",
            "\n",
            "int main() {\n" +
            "  |\n" +
            "}\n");
    }

    public void testIdentMainHalf2() {
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        typeCharactersInText(
            "int main()|\n", // "Incorrect identing of main",
            "\n",
            "int main()\n" +
            "\n");
    }

    public void testIdentMainHalf3() {
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C, getDocument())).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        typeCharactersInText(
            "int main()\n"+
            "{|\n", // "Incorrect identing of main",
            "\n",
            "int main()\n" +
            "{\n" +
            "  |\n" +
            "}\n");
    }

    public void testIZ101099() {
        setDefaultsOptions();
        typeCharactersInText(
                "template <class T>|\n", // "Incorrect new-line indent IZ101099",
                "\n",
                "template <class T>\n"+
                "|\n"
                );
    }

    public void testIZ122489() {
        setDefaultsOptions();
        typeCharactersInText(
                "Cpu::Cpu(int units) :\n"+
                "   Module(units) {\n"+
                "}|\n", // "Incorrect new-line indent IZ122489",
                "\n",
                "Cpu::Cpu(int units) :\n"+
                "   Module(units) {\n"+
                "}\n"+
                "|\n"
                );
    }

    /**
     * test parameter aligning
     */
    public void testIdentMethodParameters() {
        setCppEditorKit(false);
        setDefaultsOptions();
        typeCharactersInText(
            "int longmain(int a,|\n", // "Incorrect identing of main",
            "\n",
            "int longmain(int a,\n" +
            "        \n");
    }

    /**
     * test parameter aligning
     */
    public void testIdentMethodParameters2() {
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C, getDocument())).
                putBoolean(EditorOptions.alignMultilineMethodParams, true);
        typeCharactersInText(
            "int longmain(int a,|\n", // "Incorrect identing of main",
            "\n",
            "int longmain(int a,\n" +
            "             \n");
    }

    /**
     * test parameter aligning
     */
    public void testIdentCallParameters() {
        setCppEditorKit(false);
        setDefaultsOptions();
        typeCharactersInText(
            "a = longmain(a,|\n", // "Incorrect identing of main",
            "\n",
            "a = longmain(a,\n" +
            "        \n");
    }

    /**
     * test parameter aligning
     */
    public void testIdentCallParameters2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.alignMultilineCallArgs, true);
        typeCharactersInText(
            "a = longmain(a,|\n", // "Incorrect identing of main",
            "\n",
            "a = longmain(a,\n" +
            "             \n");
    }

    public void testIdentNewLineLocalDeclararion() throws Exception {
        setDefaultsOptions("GNU");
        typeCharactersInText(
            "tree\n" +
            "disp(int i){\n" +
            "  int i = |\n" +
            "}", // "Incorrect identing of New Line Local Declararion",
            "\n",
            "tree\n" +
            "disp(int i){\n" +
            "  int i = \n" +
            "  |\n" +
            "}"
            );
    }

    public void testIdentNewLineLocalStatement() throws Exception {
        setDefaultsOptions("GNU");
        typeCharactersInText(
            "tree\n" +
            "disp(int i){\n" +
            "  i = |\n" +
            "}", // "Incorrect identing of New Line Local Statement",
            "\n",
            "tree\n" +
            "disp(int i){\n" +
            "  i = \n" +
            "          |\n" +
            "}"
            );
    }

    public void testIdentNewLineLocalStatement2() throws Exception {
        setDefaultsOptions("GNU");
        typeCharactersInText(
            "tree\n" +
            "disp(int i){\n" +
            "  i = f(i,|)\n" +
            "}", // "Incorrect identing of New Line Local Statement",
            "\n",
            "tree\n" +
            "disp(int i){\n" +
            "  i = f(i,\n" +
            "        |)\n" +
            "}"
            );
    }

    public void testIdentNewLineLocalStatement3() throws Exception {
        setDefaultsOptions("GNU");
        typeCharactersInText(
            "tree\n" +
            "disp(int i){\n" +
            "  i = f(i,\n" +
            "        i+|)\n" +
            "}", // "Incorrect identing of New Line Local Statement",
            "\n",
            "tree\n" +
            "disp(int i){\n" +
            "  i = f(i,\n" +
            "        i+\n" +
            "          |)\n" +
            "}"
            );
    }

    public void testIdentNewLineLocalStatement4() throws Exception {
        setDefaultsOptions("GNU");
        typeCharactersInText(
            "tree\n" +
            "disp(int i){\n" +
            "  i = f(i,\n" +
            "        i+foo(a,|))\n" +
            "}", // "Incorrect identing of New Line Local Statement",
            "\n",
            "tree\n" +
            "disp(int i){\n" +
            "  i = f(i,\n" +
            "        i+foo(a,\n" +
            "              |))\n" +
            "}"
            );
    }

    // IZ#135150:GNU style: wrong indent in 'if else' expression
    public void testIZ135150() throws Exception {
        setDefaultsOptions("GNU");
        typeCharactersInText(
            "int\n" +
            "main()\n" +
            "{\n" +
            "  int i = 0;\n" +
            "  if (i == 0)\n" +
            "    i = 1;\n" +
            "  else\n" +
            "    {|\n", // "IZ#135150:GNU style: wrong indent in 'if else' expression",
            "\n",
            "int\n" +
            "main()\n" +
            "{\n" +
            "  int i = 0;\n" +
            "  if (i == 0)\n" +
            "    i = 1;\n" +
            "  else\n" +
            "    {\n" +
            "      |\n" +
            "    }\n");
    }
    /**
     * test IZ:150788 Slight flaw in apache-style indentation
     */
    public void testIZ150788() {
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C, getDocument())).
                putBoolean(EditorOptions.alignMultilineIfCondition, true);
        typeCharactersInText(
            "if (a &&|)", // "Incorrect identing IZ:150788 Slight flaw in apache-style indentation",
            "\n",
            "if (a &&\n"+
            "    )");
    }
    /**
     * test IZ:150788 Slight flaw in apache-style indentation
     */
    public void testIZ150788_2() {
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C, getDocument())).
                putBoolean(EditorOptions.alignMultilineWhileCondition, true);
        typeCharactersInText(
            "while(a &&|)", // "Incorrect identing IZ:150788 Slight flaw in apache-style indentation",
            "\n",
            "while(a &&\n"+
            "      )");
    }

    /**
     * test IZ:150788 Slight flaw in apache-style indentation
     */
    public void testIZ150788_3() {
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C, getDocument())).
                putBoolean(EditorOptions.alignMultilineFor, true);
        typeCharactersInText(
            "for  (int a = 0;|)", // "Incorrect identing IZ:150788 Slight flaw in apache-style indentation",
            "\n",
            "for  (int a = 0;\n"+
            "      )");
    }

    /**
     * test IZ:150788 Slight flaw in apache-style indentation
     */
    public void testIZ150788_4() {
        setCppEditorKit(false);
        setDefaultsOptions();
        typeCharactersInText(
            "for  (int a = 0;|)", // "Incorrect identing IZ:150788 Slight flaw in apache-style indentation",
            "\n",
            "for  (int a = 0;\n"+
            "        )");
    }
    /**
     * test IZ:150788 Slight flaw in apache-style indentation
     */
    public void testIZ150788_5() {
        setCppEditorKit(false);
        setDefaultsOptions();
        typeCharactersInText(
            "if (a &&|)", // "Incorrect identing IZ:150788 Slight flaw in apache-style indentation",
            "\n",
            "if (a &&\n"+
            "        )");
    }

    public void testIZ161572() {
        setDefaultsOptions();
        typeCharactersInText(
            "enum {\n" +
            "  t1 = 1,|\n" +
            "}", // "Incorrect identing IZ:161572 Wrong indent for multiline code",
            "\n",
            "enum {\n" +
            "  t1 = 1,\n" +
            "  \n" +
            "}");
    }

    public void testIZ161572_1() {
        setDefaultsOptions();
        typeCharactersInText(
            "enum A {\n" +
            "  t1 = 1,|\n" +
            "}", // "Incorrect identing IZ:161572 Wrong indent for multiline code",
            "\n",
            "enum A {\n" +
            "  t1 = 1,\n" +
            "  \n" +
            "}");
    }

    public void testIZ161572_2() {
        setDefaultsOptions();
        typeCharactersInText(
            "enum A {\n" +
            "  t1 = 1,|\n" +
            "}", // "Incorrect identing IZ:161572 Wrong indent for multiline code",
            "\n",
            "enum A {\n" +
            "  t1 = 1,\n" +
            "  \n" +
            "}");
    }

    public void testIZ161572_3() {
        setDefaultsOptions();
        typeCharactersInText(
            "enum A {\n" +
            "  t1,|\n" +
            "}", // "Incorrect identing IZ:161572 Wrong indent for multiline code",
            "\n",
            "enum A {\n" +
            "  t1,\n" +
            "  \n" +
            "}");
    }

    public void testIZ161572_4() {
        setDefaultsOptions();
        typeCharactersInText(
            "class A {\n" +
            "  int a,|\n" +
            "}", // "Incorrect identing IZ:161572 Wrong indent for multiline code",
            "\n",
            "class A {\n" +
            "  int a,\n" +
            "  \n" +
            "}");
    }

    public void testIZ161572_5() {
        setDefaultsOptions();
        typeCharactersInText(
            "class A {\n" +
            "  int b;\n" +
            "  int a,|\n" +
            "}", // "Incorrect identing IZ:161572 Wrong indent for multiline code",
            "\n",
            "class A {\n" +
            "  int b;\n" +
            "  int a,\n" +
            "  \n" +
            "}");
    }

    public void testIZ161572_6() {
        setDefaultsOptions();
        typeCharactersInText(
            "class A {\n" +
            "  int b(int p, int j){}\n" +
            "  int a,|\n" +
            "}", // "Incorrect identing IZ:161572 Wrong indent for multiline code",
            "\n",
            "class A {\n" +
            "  int b(int p, int j){}\n" +
            "  int a,\n" +
            "  \n" +
            "}");
    }

    public void testIZ161572_7() {
        setDefaultsOptions();
        typeCharactersInText(
            "class A {\n" +
            "  int b(int p, int j){\n" +
            "      int a,|\n" +
            "  }\n" +
            "}", // "Incorrect identing IZ:161572 Wrong indent for multiline code",
            "\n",
            "class A {\n" +
            "  int b(int p, int j){\n" +
            "      int a,\n" +
            "              \n" +
            "  }\n" +
            "}");
    }

    public void testIZ168505() {
        setDefaultsOptions();
        typeCharactersInText(
            "std::cout |<< \"Welcome ...\" << std::endl;\n", 
            // "Incorrect identing IZ:168505 cout arrows should be better aligned, like in emacs",
            "\n",
            "std::cout \n" +
            "        |<< \"Welcome ...\" << std::endl;\n"
            );
    }

    /**
     * test IZ:171413 multi-line statement incorrectly tabbed (spaced)
     */
    public void testIZ171413() {
        setCppEditorKit(false);
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.C, getDocument())).
                put(EditorOptions.newLineBeforeBrace, CodeStyle.BracePlacement.NEW_LINE.name());
        typeCharactersInText(
            "for (Protein::bb_torsion_it_t _bbt_it = _prot_gap.Torsions().begin();\n"+
            "        _bbt_it != _prot_gap.Torsions().end(); ++_bbt_it) |{\n"+
            "    cout << _bbt_it->phi.getDihedral() << endl;\n"+
            "}\n", // "Incorrect identing IZ:171413 multi-line statement incorrectly tabbed (spaced)",
            "\n",
            "for (Protein::bb_torsion_it_t _bbt_it = _prot_gap.Torsions().begin();\n"+
            "        _bbt_it != _prot_gap.Torsions().end(); ++_bbt_it) \n"+
            "{\n"+
            "    cout << _bbt_it->phi.getDihedral() << endl;\n"+
            "}\n");
    }

    public void testIZ168369() {
        setDefaultsOptions();
        typeCharactersInText(
                  "namespace A\n" +
                  "{\n" +
                  "/*|", // "Incorrect new-line indent",
                  "\n",
                  "namespace A\n" +
                  "{\n" +
                  "/*\n" +
                  " * |"
                );
    }

    // Bug 176850 -  Impossible to turn off C++ namespace indenting
    public void testIZ176850() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.indentNamespace, false);
        typeCharactersInText(
                  "namespace maths {|", // "Incorrect new-line indent",
                  "\n",
                  "namespace maths {\n" +
                  "|\n" +
                  "}");
    }

    public void testNestedFor() {
        setCppEditorKit(false);
        setDefaultsOptions();
        typeCharactersInText(
            "int main() {\n"+
            "    for (int i=0; i<n; i++)\n"+
            "        for (int j=0; j<n; j++)\n"+
            "            a[i][j]=0;|\n"+
            "}\n", // "Incorrect identing of nested if/for",
            "\n",
            "int main() {\n"+
            "    for (int i=0; i<n; i++)\n"+
            "        for (int j=0; j<n; j++)\n"+
            "            a[i][j]=0;\n"+
            "    |\n"+
            "}\n"
            );
    }

    public void testNestedIf() {
        setCppEditorKit(false);
        setDefaultsOptions();
        typeCharactersInText(
            "int main() {\n"+
            "    if (i==0)\n"+
            "        if (j==0)\n"+
            "            a[i][j]=0;|\n"+
            "}\n", // "Incorrect identing of nested if/for",
            "\n",
            "int main() {\n"+
            "    if (i==0)\n"+
            "        if (j==0)\n"+
            "            a[i][j]=0;\n"+
            "    |\n"+
            "}\n"
            );
    }

    public void testNestedForIf() {
        setCppEditorKit(false);
        setDefaultsOptions();
        typeCharactersInText(
            "int main() {\n"+
            "    for (int i=0; i<n; i++)\n"+
            "        if (j==0)\n"+
            "            a[i][j]=0;|\n"+
            "}\n", // "Incorrect identing of nested if/for",
            "\n",
            "int main() {\n"+
            "    for (int i=0; i<n; i++)\n"+
            "        if (j==0)\n"+
            "            a[i][j]=0;\n"+
            "    |\n"+
            "}\n"
            );
    }

    public void testNestedForIf1() {
        setCppEditorKit(false);
        setDefaultsOptions();
        typeCharactersInText(
            "int main() {\n"+
            "    for (int i=0; i<n; i++)\n"+
            "        if (j==0)|\n"+
            "}\n", // "Incorrect identing of nested if/for",
            "\n",
            "int main() {\n"+
            "    for (int i=0; i<n; i++)\n"+
            "        if (j==0)\n"+
            "            |\n"+
            "}\n"
            );
    }

    public void testNestedIfFor() {
        setCppEditorKit(false);
        setDefaultsOptions();
        typeCharactersInText(
            "int main() {\n"+
            "    if (j==0)\n"+
            "        for (int i=0; i<n; i++)\n"+
            "            a[i][j]=0;|\n"+
            "}\n", // "Incorrect identing of nested if/for",
            "\n",
            "int main() {\n"+
            "    if (j==0)\n"+
            "        for (int i=0; i<n; i++)\n"+
            "            a[i][j]=0;\n"+
            "    |\n"+
            "}\n"
            );
    }

    public void testNestedIfFor1() {
        setCppEditorKit(false);
        setDefaultsOptions();
        typeCharactersInText(
            "int main() {\n"+
            "    if (j==0)\n"+
            "        for (int i=0; i<n; i++)|\n"+
            "}\n", // "Incorrect identing of nested if/for",
            "\n",
            "int main() {\n"+
            "    if (j==0)\n"+
            "        for (int i=0; i<n; i++)\n"+
            "            |\n"+
            "}\n"
            );
    }

    public void testEmptyFor() {
        setCppEditorKit(false);
        setDefaultsOptions();
        typeCharactersInText(
            "int main() {\n"+
            "    for (int i=0; i<n; i++);|\n"+
            "}\n", // "Incorrect identing of empty for",
            "\n",
            "int main() {\n"+
            "    for (int i=0; i<n; i++);\n"+
            "    |\n"+
            "}\n"
            );
    }

    public void testNestedFor2() {
        setCppEditorKit(false);
        setDefaultsOptions();
        typeCharactersInText(
            "int main() {\n"+
            "    for(i=0;i<10;i++)\n"+
            "        for(j=0;j<10;j++)\n"+
            "            for(k=0;k<10;k++)\n"+
            "                {\n"+
            "                    //contents of the last loop\n"+
            "                }|\n"+
            "}\n", // "Incorrect identing of empty for",
            "\n",
            "int main() {\n"+
            "    for(i=0;i<10;i++)\n"+
            "        for(j=0;j<10;j++)\n"+
            "            for(k=0;k<10;k++)\n"+
            "                {\n"+
            "                    //contents of the last loop\n"+
            "                }\n"+
            "    |\n"+
            "}\n"
            );
    }

    public void testPreprocessorIndentTyping() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "#ifdef AAA\n" +
                "    int a;\n" +
                "    |\n",
                "#",
                "#ifdef AAA\n" +
                "    int a;\n" +
                "#|\n"
                );
    }

    public void testPreprocessorIndentTyping2() throws Exception {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.sharpAtStartLine, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.indentPreprocessorDirectives, CodeStyle.PreprocessorIndent.PREPROCESSOR_INDENT.name());
        typeCharactersInText(
                "#ifdef AAA\n" +
                "    int a;\n" +
                "  |\n",
                "#",
                "#ifdef AAA\n" +
                "    int a;\n" +
                "    #|\n"
                );
    }

    public void testPreprocessorIndentTyping3() throws Exception {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                putBoolean(EditorOptions.sharpAtStartLine, false);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.indentPreprocessorDirectives, CodeStyle.PreprocessorIndent.PREPROCESSOR_INDENT.name());
        typeCharactersInText(
                "#ifdef AAA\n" +
                "    int a;\n" +
                "  #endi|\n",
                "f",
                "#ifdef AAA\n" +
                "    int a;\n" +
                "#endif|\n"
                );
    }

    public void testIZ_196567() {
        setDefaultsOptions();
        typeCharactersInText(
            "struct list_head {\n" +
            "    struct list_head *prev, *next;\n" +
            "};\n" +
            "\n" +
            "int foo(int a, int b) {\n" +
            "}\n" +
            "\n" +
            "int boo(int a, |int b) {\n" +
            "}\n", // "Incorrect identing IZ:196567 Text editor fails to honor indentation rules in certain cases.",
            "\n",
            "struct list_head {\n" +
            "    struct list_head *prev, *next;\n" +
            "};\n" +
            "\n" +
            "int foo(int a, int b) {\n" +
            "}\n" +
            "\n" +
            "int boo(int a, \n"+
            "        |int b) {\n" +
            "}\n");
    }
    
    public void testIZ_196567_2() {
        setDefaultsOptions();
        typeCharactersInText(
            "struct list_head foo(int a, int b){\n" +
            "    list_head ret,|int b;\n" +
            "    return ret;\n" +
            "}\n", // "Incorrect identing IZ:196567 Text editor fails to honor indentation rules in certain cases.",
            "\n",
            "struct list_head foo(int a, int b){\n" +
            "    list_head ret,\n" +
            "            |int b;\n" +
            "    return ret;\n" +
            "}\n");
    }

    public void testIndentMacroDefinition() {
        setDefaultsOptions();
        typeCharactersInText(
                "#define foobar(foo, bar) do {    \\\n"
                + "    int f = (foo);        \\\n"
                + "    int b = (bar);        \\\n"
                + "    f += b;            \\|\n"
                + "\n", // "Incorrect new-line indent",
                "\n",
                "#define foobar(foo, bar) do {    \\\n"
                + "    int f = (foo);        \\\n"
                + "    int b = (bar);        \\\n"
                + "    f += b;            \\\n"
                + "    |\n"
                + "\n"
                );
    }

    public void testEnterAfterLambdaHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceLambda, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        typeCharactersInText(
                "for_each(v.begin(), v.end(),\n" +
                "         [] (int val) {|\n",
                "\n",
                "for_each(v.begin(), v.end(),\n" +
                "         [] (int val) {\n"+
                "             |\n"+
                "           }\n"
                );
    }

    public void testEnterAfterLambdaHalf_1() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceLambda, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        typeCharactersInText(
                "for_each(v.begin(), v.end(),\n" +
                "         [] (int val)|\n",
                "\n",
                "for_each(v.begin(), v.end(),\n" +
                "         [] (int val)\n"+
                "           |\n"
                );
    }

    public void testEnterAfterLambdaHalf_2() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceLambda, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        typeCharactersInText(
                "for_each(v.begin(), v.end(),\n" +
                "         [] (int val)\n" +
                "           |\n",
                "{",
                "for_each(v.begin(), v.end(),\n" +
                "         [] (int val)\n"+
                "           {|\n"
                );
    }

    public void testEnterAfterLambdaHalf_3() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBraceLambda, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        typeCharactersInText(
                "for_each(v.begin(), v.end(),\n" +
                "         [] (int val)\n" +
                "           |\n",
                "{",
                "for_each(v.begin(), v.end(),\n" +
                "         [] (int val)\n"+
                "           {|\n"
                );
    }
    
    public void test2258589() {
        setDefaultsOptions();
        typeCharactersInText(
                "void foo() {\n" 
              + "    return;\n" 
              + "}\n"
              + "\n"
              + "/*static*/ void boo() {|\n"
              + "    return;\n"
              + "}\n",
                "\n",
                "void foo() {\n" 
              + "    return;\n" 
              + "}\n"
              + "\n"
              + "/*static*/ void boo() {\n"
              + "    |\n"
              + "    return;\n"
              + "}\n"
                );
    }

    public void testIZ_245131() {
        setDefaultsOptions();
        typeCharactersInText(
            "void foo(int i){\n" +
            "    i++;\n" +
            "label:|\n" +
            "}\n",
            "\n",
            "void foo(int i){\n" +
            "    i++;\n" +
            "label:\n" +
            "    |\n" +
            "}\n");
    }
    
    public void testIZ_245131_2() {
        setDefaultsOptions();
        typeCharactersInText(
            "void foo(int i){\n" +
            "label:|\n" +
            "}\n",
            "\n",
            "void foo(int i){\n" +
            "label:\n" +
            "    |\n" +
            "}\n");
    }
    
    public void testIZ_269428() {
        setDefaultsOptions();
        typeCharactersInText(
            "enum class test : char\n" +
            "{\n" +
            "    valueA = 'a',|\n" +
            "    valueB = 'b',\n" +
            "    valueC\n" +
            "};\n",
            "\n",
            "enum class test : char\n" +
            "{\n" +
            "    valueA = 'a',\n" +
            "    |\n" +
            "    valueB = 'b',\n" +
            "    valueC\n" +
            "};\n");
    }
}
