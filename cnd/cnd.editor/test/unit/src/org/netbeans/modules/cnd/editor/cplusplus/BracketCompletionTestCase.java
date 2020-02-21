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

import org.junit.Ignore;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.options.EditorOptions;

/**
 * Class was taken from java
 * Links point to java IZ.
 * C/C++ specific tests begin from testSystemInclude
 */
public class BracketCompletionTestCase extends EditorBase  {

    public BracketCompletionTestCase(String testMethodName) {
        super(testMethodName);
    }

    // ------- Tests for raw strings -------------
    
    @Ignore
    public void testModifyRawStringInPPDirective() {
        // #241929 - AssertionError at org.netbeans.modules.cnd.lexer.CppStringLexer.nextToken
        setDefaultsOptions();
        typeCharactersInText("#define R|\"\"", "'", "#define R'\"\"");
    } 
    
    public void testSimpleQuoteInRawString() throws Exception {
        setDefaultsOptions();
        typeCharactersInText("R|", "\"", "R\"|()\"");
    }

    public void testLeftParenInRawStringStartDelim() throws Exception {
        setDefaultsOptions();
        typeCharactersInText("  R\"|()\"", "(", "  R\"(|)\"");
    }

    public void testRawStringDelete() throws Exception {
        setDefaultsOptions();
        typeCharactersInText("  R|\"()\"", "\f", "  R");
        typeCharactersInText("  R\"|()\"", "\f", "  R");
        typeCharactersInText("  R\"|()\"", "\b", "  R");
        typeCharactersInText("  R\"(|)\"", "\f", "  R");
        typeCharactersInText("  R\"(|)\"", "\b", "  R");
        typeCharactersInText("  R\"()|\"", "\f", "  R");
        typeCharactersInText("  R\"()|\"", "\b", "  R");
    }

    public void testRawStringNewLine() throws Exception {
        setDefaultsOptions();
        typeCharactersInText("  R\"XYZ(te|xt)XYZ\"", "\n", "  R\"XYZ(te\n|xt)XYZ\"");
    }
    
    public void testRawStringDelStartDelimeter() throws Exception {
        setDefaultsOptions();
        typeCharactersInText("  R\"|XYZ()XYZ\"", "\f", "  R\"|YZ()YZ\"");
        typeCharactersInText("  R\"X|YZ()XYZ\"", "\b", "  R\"|YZ()YZ\"");
        typeCharactersInText("  R\"X|YZ()XYZ\"", "\f", "  R\"X|Z()XZ\"");
        typeCharactersInText("  R\"XY|Z()XYZ\"", "\b", "  R\"X|Z()XZ\"");
        typeCharactersInText("  R\"XY|Z()XYZ\"", "\f", "  R\"XY|()XY\"");
        typeCharactersInText("  R\"XYZ|()XYZ\"", "\b", "  R\"XY|()XY\"");
    }

    public void testRawStringDelEndDelimeter() throws Exception {
        setDefaultsOptions();
        typeCharactersInText("  R\"XYZ()|XYZ\"", "\f", "  R\"YZ()|YZ\"");
        typeCharactersInText("  R\"XYZ()X|YZ\"", "\b", "  R\"YZ()|YZ\"");
        typeCharactersInText("  R\"XYZ()X|YZ\"", "\f", "  R\"XZ()X|Z\"");
        typeCharactersInText("  R\"XYZ()XY|Z\"", "\b", "  R\"XZ()X|Z\"");
        typeCharactersInText("  R\"XYZ()XY|Z\"", "\f", "  R\"XY()XY|\"");
        typeCharactersInText("  R\"XYZ()XYZ|\"", "\b", "  R\"XY()XY|\"");
    }

    public void testLeftParenInRawStringStartDelim2() throws Exception {
        setDefaultsOptions();
        typeCharactersInText("  R\"XXX|()XXX\"", "(", "  R\"XXX(|)XXX\"");
    }

    public void testQuoteInRawStringEndDelim() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(" R\"delim()delim|\"  ", "\"", " R\"delim()delim\"|  ");
    }

    public void testQuoteInRawStringEndDelim2() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(" R\"()|\"  ", "\"", " R\"()\"|  ");
    }

    public void testRightParenInRawStringWithEmptyDelim() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(" R\"(|)\"  ", ")", " R\"()|\"  ");
    }

    public void testRightParenInRawStringBeforeEndDelim() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(" R\"ddd(|)ddd\"  ", ")", " R\"ddd()|)ddd\"  ");
    }

    public void testTypeInRawStringStartDelim() throws Exception {
        setDefaultsOptions();
        typeCharactersInText("    R\"del|im()delim\"  ", "U",  "    R\"delU|im()delUim\"  ");
        typeCharactersInText("    R\"delim|()delim\"  ", "E",  "    R\"delimE|()delimE\"  ");
        typeCharactersInText("    R\"|delim()delim\"  ", "S",  "    R\"S|delim()Sdelim\"  ");
    }

    public void testType2InRawStringStartDelim() throws Exception {
        setDefaultsOptions();
        typeCharactersInText("    R\"del|im()delim\"  ", "UU", "    R\"delUU|im()delUUim\"  ");
        typeCharactersInText("    R\"delim|()delim\"  ", "EE", "    R\"delimEE|()delimEE\"  ");
        typeCharactersInText("    R\"|delim()delim\"  ", "SS", "    R\"SS|delim()SSdelim\"  ");
    }

    public void testTypeInRawStringEndDelim() throws Exception {
        setDefaultsOptions();
        typeCharactersInText("    R\"delim()del|im\"  ", "U", "    R\"delUim()delU|im\"  ");
        typeCharactersInText("    R\"delim()delim|\"  ", "A", "    R\"delimA()delimA|\"  ");
        typeCharactersInText("    R\"delim()|delim\"  ", "B", "    R\"Bdelim()B|delim\"  ");
    }

    public void test2TypeInRawStringEndDelim() throws Exception {
        setDefaultsOptions();
        typeCharactersInText("    R\"delim()del|im\"  ", "UU", "    R\"delUUim()delUU|im\"  ");
        typeCharactersInText("    R\"delim()delim|\"  ", "AA", "    R\"delimAA()delimAA|\"  ");
        typeCharactersInText("    R\"delim()|delim\"  ", "BB", "    R\"BBdelim()BB|delim\"  ");
    }

    // ------- Test of . to -> completion
    public void testThisDotToArrow() {
        setDefaultsOptions();
        typeCharactersInText("this|", ".", "this->|");
    }

    // ------- Tests for completion of right parenthesis ')' -------------
    
    public void testRightParenSimpleMethodCall() {
        setDefaultsOptions();
        typeCharactersInText("m()|)", ")", "m())|");
    }
    
    public void testRightParenSwingInvokeLaterRunnable() {
        setDefaultsOptions();
        typeCharactersInText(
                "SwingUtilities.invokeLater(new Runnable()|))",
                ")",
                "SwingUtilities.invokeLater(new Runnable())|)");
    }

    public void testRightParenSwingInvokeLaterRunnableRun() {
        setDefaultsOptions();
        typeCharactersInText(
            "SwingUtilities.invokeLater(new Runnable() {\n"
          + "    public void run()|)\n"
          + "})",
          ")",
            "SwingUtilities.invokeLater(new Runnable() {\n"
          + "    public void run())|\n"
          + "})"
        );
    }
    
    public void testRightParenIfMethodCall() {
        setDefaultsOptions();
        typeCharactersInText(
            " if (a()|) + 5 > 6) {\n"
          + " }",
          ")",
            " if (a())| + 5 > 6) {\n"
          + " }"
        );
    }

    public void testNotSkipRPAREN() throws Exception {
        // #225194 - incorrect skipping of right parent 
        setDefaultsOptions();
        typeCharactersInText(
                " if ((n == 0|) {\n"
                + " }",
                ")",
                " if ((n == 0)) {\n"
                + " }");
    }
    
    public void testRightParenSkipInPP() throws Exception {
        // extra fix for #225194 - incorrect skipping of right parent 
        setDefaultsOptions();
        typeCharactersInText("#define A(|) 1\n", ")", "#define A()| 1\n");
    }
    
    public void testRightParenNoSkipNonBracketChar() {
        setDefaultsOptions();
        typeCharactersInText("m()| ", ")", "m())| ");
    }

    public void testRightParenNoSkipDocEnd() {
        setDefaultsOptions();
        typeCharactersInText("m()|", ")", "m())|");
    }

    
    // ------- Tests for completion of right brace '}' -------------
    
    public void testAddRightBraceIfLeftBrace() {
        setDefaultsOptions();
        typeCharactersInText("if (true) {|", "\n", "if (true) {\n    |\n}");
    }

    public void testAddRightBraceIfLeftBraceWhiteSpace() {
        setDefaultsOptions();
        typeCharactersInText("if (true) { \t|\n",
                "\n",
                  "if (true) { \t\n"
                + "    |\n"
                + "}\n");
    }
    
    public void testAddRightBraceIfLeftBraceLineComment() {
        setDefaultsOptions();
        typeCharactersInText("if (true) { // line-comment|\n",
                "\n",
                "if (true) { // line-comment\n"
                + "    |\n"
                + "}\n");
    }

    public void testAddRightBraceIfLeftBraceBlockComment() {
        setDefaultsOptions();
        typeCharactersInText("if (true) { /* block-comment */|\n",
                "\n",
                "if (true) { /* block-comment */\n"
                + "    |\n"
                + "}\n");
    }

    public void testAddRightBraceIfLeftBraceAlreadyPresent() {
        setDefaultsOptions();
        typeCharactersInText(
            "if (true) {|\n"
          + "}",
            "\n",
            "if (true) {\n" +
            "    |\n"
          + "}"
        );
    }

    public void testAddRightBraceCaretInComment() {
        setDefaultsOptions();
        typeCharactersInText(
            "if (true) { /* in-block-comment |\n",
            "\n",
            "if (true) { /* in-block-comment \n"
          + "             * |\n"
        );
    }
    
    public void testSimpleAdditionOfOpeningParenthesisAfterWhile () throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
            "while |",
            "(",
            "while (|)"
        );
    }

    // ------- Tests for completion of quote (") -------------
    public void testSimpleQuoteInEmptyDoc () throws Exception {
        setDefaultsOptions();
        typeCharactersInText("|", "\"", "\"|\"");
    }

    public void testSimpleQuoteAtBeginingOfDoc () throws Exception {
        setDefaultsOptions();
        typeCharactersInText("|  ", "\"", "\"|\"  ");
    }

    public void testSimpleQuoteAtEndOfDoc () throws Exception {
        setDefaultsOptions();
        typeCharactersInText("  |", "\"", "  \"|\"");
    }
    
    public void testSimpleQuoteInWhiteSpaceArea () throws Exception {
        setDefaultsOptions();
        typeCharactersInText("  |  ", "\"", "  \"|\"  ");
    }
    
    public void testQuoteAtEOL () throws Exception {
        setDefaultsOptions();
        typeCharactersInText("  |\n", "\"", "  \"|\"\n");
    }
    
    public void testQuoteWithUnterminatedStringLiteral() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "  \"unterminated string| \n", "\"",
                "  \"unterminated string\"| \n");
    }

    public void testQuoteAtEOLWithUnterminatedStringLiteral() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "  \"unterminated string |\n", "\"",
                "  \"unterminated string \"|\n");
    }

    public void testQuoteInsideStringLiteral() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "  \"stri|ng literal\" ", "\"",
                "  \"stri\"|ng literal\" ");
    }

    public void testQuoteInsideEmptyParentheses() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                " printf(|) ", "\"",
                " printf(\"|\") ");
    }

    public void testQuoteInsideNonEmptyParentheses() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                " printf(|some text) ", "\"",
                " printf(\"|some text) ");
    }

    public void testQuoteInsideNonEmptyParenthesesBeforeClosingParentheses() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                " printf(i+|) ", "\"",
                " printf(i+\"|\") ");
    }

    public void testQuoteInsideNonEmptyParenthesesBeforeClosingParenthesesAndUnterminatedStringLiteral() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                " printf(\"unterminated string literal |); ", "\"",
                " printf(\"unterminated string literal \"|); ");
    }

    public void testQuoteBeforePlus() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                " printf(|+\"string literal\"); ", "\"",
                " printf(\"|\"+\"string literal\"); ");
    }

    public void testQuoteBeforeComma() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "String s[] = new String[]{|,\"two\"};", "\"",
                "String s[] = new String[]{\"|\",\"two\"};");
    }

    public void testQuoteBeforeBrace() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "String s[] = new String[]{\"one\",|};", "\"",
                "String s[] = new String[]{\"one\",\"|\"};");
    }

    public void testQuoteBeforeSemicolon() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "String s = \"\" + |;", "\"",
                "String s = \"\" + \"|\";");
    }

    public void testQuoteBeforeSemicolonWithWhitespace() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "String s = \"\" +| ;", "\"",
                "String s = \"\" +\"|\" ;");
    }

    public void testQuoteAfterEscapeSequence() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "\\|", "\"",
                "\\\"|");
    }

    /**
     * issue #69524
     */
    public void testQuoteEaten() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "|",
                "\"\"",
                "\"\"|");
    }

    /**
     * issue #69935
     */
    public void testQuoteInsideComments() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "/** |\n */",
                "\"",
                "/** \"|\n */");
    }

    /**
     * issue #71880
     */
    public void testQuoteAtTheEndOfLineCommentLine() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "// test line comment |\n", "\"",
                "// test line comment \"|\n");
    }

    // ------- Tests for completion of single quote (') -------------        
    public void testSingleQuoteInEmptyDoc() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "|",
                "'",
                "'|'");
    }

    public void testSingleQuoteAtBeginingOfDoc() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "|  ",
                "'",
                "'|'  ");
    }

    public void testSingleQuoteAtEndOfDoc() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "  |",
                "'",
                "  '|'");
    }

    public void testSingleQuoteInWhiteSpaceArea() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "  |  ",
                "'",
                "  '|'  ");
    }

    public void testSingleQuoteAtEOL() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "  |\n",
                "'",
                "  '|'\n");
    }

    public void testSingleQuoteWithUnterminatedCharLiteral() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "  '| \n",
                "'",
                "  ''| \n");
    }

    public void testSingleQuoteAtEOLWithUnterminatedCharLiteral() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "  ' |\n",
                "'",
                "  ' '|\n");
    }

    public void testSingleQuoteInsideCharLiteral() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "  '| ' ",
                "'",
                "  ''| ' ");
    }

    public void testSingleQuoteInsideEmptyParentheses() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                " printf(|) ",
                "'",
                " printf('|') ");
    }

    public void testSingleQuoteInsideNonEmptyParentheses() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                " printf(|some text) ",
                "'",
                " printf('|some text) ");
    }

    public void testSingleQuoteInsideNonEmptyParenthesesBeforeClosingParentheses() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                " printf(i+|) ",
                "'",
                " printf(i+'|') ");
    }

    public void testSingleQuoteInsideNonEmptyParenthesesBeforeClosingParenthesesAndUnterminatedCharLiteral() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                " printf(' |); ",
                "'",
                " printf(' '|); ");
    }

    public void testSingleQuoteBeforePlus() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                " printf(|+\"string literal\"); ",
                "'",
                " printf('|'+\"string literal\"); ");
    }

    public void testSingleQuoteBeforeComma() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "String s[] = new String[]{|,\"two\"};",
                "'",
                "String s[] = new String[]{'|',\"two\"};");
    }

    public void testSingleQuoteBeforeBrace() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "String s[] = new String[]{\"one\",|};",
                "'",
                "String s[] = new String[]{\"one\",'|'};");
    }

    public void testSingleQuoteBeforeSemicolon() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "String s = \"\" + |;",
                "'",
                "String s = \"\" + '|';");
    }

    public void testsingleQuoteBeforeSemicolonWithWhitespace() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "String s = \"\" +| ;",
                "'",
                "String s = \"\" +'|' ;");
    }

    public void testSingleQuoteAfterEscapeSequence() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "\\|",
                "'",
                "\\'|");
    }

    /**
     * issue #69524
     */
    public void testSingleQuoteEaten() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "|", "''",
                "''|");
    }

    /**
     * issue #69935
     */
    public void testSingleQuoteInsideComments() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "/* |\n */",
                "'",
                "/* \'|\n */");
    }

    /**
     * issue #71880
     */
    public void testSingleQuoteAtTheEndOfLineCommentLine() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "// test line comment |\n",
                "'",
                "// test line comment \'|\n");
    }

    public void testNoGT() {
        setDefaultsOptions();
        typeCharactersInText(
                "if (a |)\n",
                "<",
                "if (a <|)\n");
    }
    
    public void testNoGTInString() {
        setDefaultsOptions();
        typeCharactersInText(
                "\"hello |\"\n",
                "<",
                "\"hello <|\"\n");
    }
    
    public void testSystemInclude() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "#include |\n",
                "<",
                "#include <|>\n");
    }

    public void testSystemIncludeEOF() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "#include |",
                "<",
                "#include <|>");
    }
    
    public void testSkipSystemInclude() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "#include <math.h|>\n",
                ">",
                "#include <math.h>|\n");
    }
    
    public void testNotSkipSystemInclude() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "#include <math|.h>\n",
                ">",
                "#include <math>|.h>\n");
    }
    
    public void testUserInclude() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "#include |\n", "\"",
                "#include \"|\"\n");
    }

    public void testUserIncludeEOF() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "#include |", "\"",
                "#include \"|\"");
    }
    
    public void testSkipUserInclude() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "#include \"h.h|\"\n",
                "\"",
                "#include \"h.h\"|\n");
    }
    
    public void testArray() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "int a|\n",
                "[",
                "int a[|]\n");
    }
    
    public void testRightBracePreprocessor() {
        setDefaultsOptions();
        typeCharactersInText(
            "void foo(){\n" +
            "#if A\n" +
            "    if (a){\n" +
            "#else\n" +
            "    if (b){|\n" +
            "#endif\n" +
            "    }\n" +
            "}",
            "\n",
              "void foo(){\n"
            + "#if A\n"
            + "    if (a){\n"
            + "#else\n"
            + "    if (b){\n"
            + "        |\n"
            + "#endif\n"
            + "    }\n"
            + "}"
        );
    }

    public void testRightBracePreprocessor2() {
        setDefaultsOptions();
        typeCharactersInText(
            "void foo(){\n" +
            "#if A\n" +
            "    if (a){|\n" +
            "#else\n" +
            "    if (b){\n" +
            "#endif\n" +
            "    }\n" +
            "}",
            "\n",
            "void foo(){\n" +
            "#if A\n" +
            "    if (a){\n" +
            "        |\n" +
            "#else\n" +
            "    if (b){\n" +
            "#endif\n" +
            "    }\n" +
            "}"
        );
    }

    public void testRightBracePreprocessor3() {
        setDefaultsOptions();
        typeCharactersInText(
            "void foo(){\n" +
            "#if A\n" +
            "    if (a){|\n" +
            "#else\n" +
            "    if (b){\n" +
            "#endif\n" +
            "//    }\n" +
            "}",
            "\n",
            "void foo(){\n" +
            "#if A\n" +
            "    if (a){\n" +
            "        |\n" +
            "    }\n" +
            "#else\n" +
            "    if (b){\n" +
            "#endif\n" +
            "//    }\n" +
            "}"
        );
    }

    public void testRightBracePreprocessor4() {
        setDefaultsOptions();
        typeCharactersInText(
            "void foo(){\n" +
            "#if A\n" +
            "    if (a){\n" +
            "#else\n" +
            "    if (b){\n" +
            "#endif\n" +
            "    if (b){|\n" +
            "    }\n" +
            "}",
            "\n",
            "void foo(){\n" +
            "#if A\n" +
            "    if (a){\n" +
            "#else\n" +
            "    if (b){\n" +
            "#endif\n" +
            "    if (b){\n" +
            "        |\n" +
            "    }\n" +
            "    }\n" +
            "}"
        );
    }

    public void testRightBracePreprocessor5() {
        setDefaultsOptions();
        typeCharactersInText(
            "void foo(){\n" +
            "#define PAREN {\n" +
            "    if (b){|\n" +
            "    }\n" +
            "}",
            "\n",
            "void foo(){\n" +
            "#define PAREN {\n" +
            "    if (b){\n" +
            "        |\n" +
            "    }\n" +
            "}"
        );
    }
    
    public void testIZ102091() throws Exception {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP, getDocument())).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE.name());
        typeCharactersInText (
            "if(i)\n"+
            "    |", 
            "{", 
            "if(i)\n"+
            "{|"
        );
    }
    
    public void testColonAfterPublic() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
            "class A{\n" +
            "    public|\n" +
            "}\n",
            ":", // "Colon After Public"
            "class A{\n" +
            "public:\n" +
            "}\n"
        );
    }
    
    public void testIdentFunctionName()  throws Exception {
        setDefaultsOptions("GNU");
        typeCharactersInText(
            "tree\n" +
            "        |",
            "d", // Incorrect identing of main",
            "tree\n" +
            "d|"
            );
    }
    
    // test line break
    
    public void testBreakLineInString1() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "char* a = \"|\"",
                "\n", // "Incorrect identing of main",
                "char* a = \"\"\n" +
                "\"|\"");
    }    

    public void testBreakLineInString2() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "           char* a = \"\\|\"",
                "\n",
                "           char* a = \"\\\n" +
                "|\"");
    }     

    public void testBreakLineInString2_1() throws Exception {
        setDefaultsOptions();
        // TODO: second line should be in the first column after fixing bug in indentation
        typeCharactersInText(
                "           char* a = \"\\\\|\"",
                "\n",
                "           char* a = \"\\\\\"\n" +
                "           \"|\"");
    }

    public void testBreakLineInString3() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "             char* a = \"\\|",
                "\n",
                "             char* a = \"\\\n" +
                "|");
    }    

    public void testBreakLineInString31() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "             char* a = \"\\|\n",
                "\n",
                "             char* a = \"\\\n" +
                "|\n");
    }

    public void testBreakLineInString4() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "             char* a = \"\\|\"",
                "\n",
                "             char* a = \"\\\n" +
                "|\"");
    }

    public void testBreakLineInString41() throws Exception {
        setDefaultsOptions();
        typeCharactersInText(
                "             char* a = \"\\|\"\n",
                "\n",
                "             char* a = \"\\\n" +
                "|\"\n");
    }

    public void testBreakLineAfterLCurly() {
        setDefaultsOptions();
        typeCharactersInText(
                "void foo() {|",
                "\n",
                "void foo() {\n" +
                "    |\n" +
                "}");
    }
    
    public void testBreakLineAfterLCurly2() {
        setDefaultsOptions();
        typeCharactersInText(
                "struct A {|",
                "\n",
                "struct A {\n" +
                "    |\n" +
                "};");
    }

    public void testBlockCommentAutoCompletion() throws Exception {
        setDefaultsOptions();
        typeCharactersInText("#define A\n/|\nvoid foo() {\n}\n", "*", "#define A\n/*|*/\nvoid foo() {\n}\n");
        typeCharactersInText("#define A\n/|    \nvoid foo() {\n}\n", "*", "#define A\n/*|*/    \nvoid foo() {\n}\n");
        typeCharactersInText("#define A\n   /|    \nvoid foo() {\n}\n", "*", "#define A\n   /*|*/    \nvoid foo() {\n}\n");
        
        typeCharactersInText("int a;\n   /|    \nvoid foo() {\n}\n", "*", "int a;\n   /*|*/    \nvoid foo() {\n}\n");
        
        typeCharactersInText("int a; /|\nvoid foo() {\n}\n", "*", "int a; /*|\nvoid foo() {\n}\n");
        typeCharactersInText("int a; /|    \nvoid foo() {\n}\n", "*", "int a; /*|    \nvoid foo() {\n}\n");

        typeCharactersInText("int a;\n/*void| foo() {\n}\n", "*", "int a;\n/*void*| foo() {\n}\n");
        typeCharactersInText("int a;\n/*|void foo() {\n}\n", "*", "int a;\n/**|void foo() {\n}\n");
    }
}
