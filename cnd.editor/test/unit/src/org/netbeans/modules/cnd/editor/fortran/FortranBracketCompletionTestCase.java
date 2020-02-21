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
package org.netbeans.modules.cnd.editor.fortran;

/**
 *
 */
public class FortranBracketCompletionTestCase extends FortranEditorBase {
    
    public FortranBracketCompletionTestCase(String testMethodName) {
        super(testMethodName);
    }

    // ------- Tests for completion of quote (") -------------
    public void testSimpleQuoteInEmptyDoc () throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText("|", "\"", "\"|\"");
    }

    // ------- Tests for completion of quote (") -------------
    public void testSimpleQuoteInEmptyDoc2() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText("|", "'", "'|'");
    }
    
    public void testSimpleQuoteAtBeginingOfDoc () throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText("|  ", "\"", "\"|\"  ");
    }
    
    public void testSimpleQuoteAtBeginingOfDoc2 () throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText("|  ", "'", "'|'  ");
    }

    public void testSimpleQuoteAtEndOfDoc () throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText("  |", "\"", "  \"|\"");
    }

    public void testSimpleQuoteAtEndOfDoc2 () throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText("  |", "'", "  '|'");
    }
    
    public void testSimpleQuoteInWhiteSpaceArea () throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText("  |  ", "\"", "  \"|\"  ");
    }
    
    public void testSimpleQuoteInWhiteSpaceArea2 () throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText("  |  ", "'", "  '|'  ");
    }
    
    public void testQuoteAtEOL () throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText("  |\n", "\"", "  \"|\"\n");
    }

    public void testQuoteAtEOL2 () throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText("  |\n", "'", "  '|'\n");
    }
    
    public void testQuoteWithUnterminatedStringLiteral() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "  \"unterminated string| \n", "\"",
                "  \"unterminated string\"| \n");
    }

    public void testQuoteWithUnterminatedStringLiteral2() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "  'unterminated string| \n", "'",
                "  'unterminated string'| \n");
    }

    public void testQuoteAtEOLWithUnterminatedStringLiteral() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "  \"unterminated string |\n", "\"",
                "  \"unterminated string \"|\n");
    }
    
    public void testQuoteAtEOLWithUnterminatedStringLiteral2() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "  'unterminated string |\n", "'",
                "  'unterminated string '|\n");
    }

    public void testQuoteInsideStringLiteral() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "  \"stri|ng literal\" ", "\"",
                "  \"stri\"|ng literal\" ");
    }

    public void testQuoteInsideStringLiteral2() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "  'stri|ng literal' ", "'",
                "  'stri\'|ng literal' ");
    }

    public void testQuoteInsideEmptyParentheses() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                " printf(|) ", "\"",
                " printf(\"|\") ");
    }

    public void testQuoteInsideEmptyParentheses2() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                " printf(|) ", "'",
                " printf('|') ");
    }

    public void testQuoteInsideNonEmptyParentheses() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                " printf(|some text) ", "\"",
                " printf(\"|some text) ");
    }

    public void testQuoteInsideNonEmptyParentheses2() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                " printf(|some text) ", "'",
                " printf('|some text) ");
    }

    public void testQuoteInsideNonEmptyParenthesesBeforeClosingParentheses() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                " printf(i+|) ", "\"",
                " printf(i+\"|\") ");
    }

    public void testQuoteInsideNonEmptyParenthesesBeforeClosingParentheses2() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                " printf(i+|) ", "'",
                " printf(i+'|') ");
    }

    public void testQuoteInsideNonEmptyParenthesesBeforeClosingParenthesesAndUnterminatedStringLiteral() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                " printf(\"unterminated string literal |); ", "\"",
                " printf(\"unterminated string literal \"|); ");
    }

    public void testQuoteInsideNonEmptyParenthesesBeforeClosingParenthesesAndUnterminatedStringLiteral2() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                " printf('unterminated string literal |); ", "'",
                " printf('unterminated string literal '|); ");
    }

    public void testQuoteBeforePlus() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                " printf(|+\"string literal\"); ", "\"",
                " printf(\"|\"+\"string literal\"); ");
    }

//    public void testQuoteBeforePlus2() throws Exception {
//        setDefaultsOptions(true);
//        typeCharactersInText(
//                " printf(|+'string literal'); ", "'",
//                " printf('|'+'string literal'); ");
//    }

    public void testQuoteBeforeComma() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "String s[] = new String[]{|,\"two\"};", "\"",
                "String s[] = new String[]{\"|\",\"two\"};");
    }

    public void testQuoteBeforeBrace() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "String s[] = new String[]{\"one\",|};", "\"",
                "String s[] = new String[]{\"one\",\"|\"};");
    }

    public void testQuoteBeforeSemicolon() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "String s = \"\" + |;", "\"",
                "String s = \"\" + \"|\";");
    }

    public void testQuoteBeforeSemicolonWithWhitespace() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "String s = \"\" +| ;", "\"",
                "String s = \"\" +\"|\" ;");
    }

    public void testQuoteAfterEscapeSequence() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "\\|", "\"",
                "\\\"|");
    }

    public void testQuoteEaten() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "'|'", "'",
                "''|");
    }

    public void testQuoteEaten2() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "'1|'", "'",
                "'1'|");
    }

    public void testQuoteEaten3() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "\"1|\"", "\"",
                "\"1\"|");
    }

    public void testQuoteEaten4() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "\"|\"", "\"",
                "\"\"|");
    }

    public void testQuoteAtTheEndOfLineCommentLine() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "! test line comment |\n", "\"",
                "! test line comment \"|\n");
    }

    // ------- Tests for completion of single quote (') -------------        
    public void testSingleQuoteInEmptyDoc() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "|",
                "'",
                "'|'");
    }

    public void testSingleQuoteAtBeginingOfDoc() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "|  ",
                "'",
                "'|'  ");
    }

    public void testSingleQuoteAtEndOfDoc() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "  |",
                "'",
                "  '|'");
    }

    public void testSingleQuoteInWhiteSpaceArea() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "  |  ",
                "'",
                "  '|'  ");
    }

    public void testSingleQuoteAtEOL() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "  |\n",
                "'",
                "  '|'\n");
    }

    public void testSingleQuoteWithUnterminatedCharLiteral() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "  '| \n",
                "'",
                "  ''| \n");
    }

    public void testSingleQuoteAtEOLWithUnterminatedCharLiteral() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "  ' |\n",
                "'",
                "  ' '|\n");
    }

    public void testSingleQuoteInsideCharLiteral() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "  '| ' ",
                "'",
                "  ''| ' ");
    }

    public void testSingleQuoteInsideEmptyParentheses() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                " printf(|) ",
                "'",
                " printf('|') ");
    }

    public void testSingleQuoteInsideNonEmptyParentheses() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                " printf(|some text) ",
                "'",
                " printf('|some text) ");
    }

    public void testSingleQuoteInsideNonEmptyParenthesesBeforeClosingParentheses() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                " printf(i+|) ",
                "'",
                " printf(i+'|') ");
    }

    public void testSingleQuoteInsideNonEmptyParenthesesBeforeClosingParenthesesAndUnterminatedCharLiteral() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                " printf(' |); ",
                "'",
                " printf(' '|); ");
    }

    public void testSingleQuoteBeforePlus() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                " printf(|+\"string literal\"); ",
                "'",
                " printf('|'+\"string literal\"); ");
    }

    public void testSingleQuoteBeforeComma() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "String s[] = new String[]{|,\"two\"};",
                "'",
                "String s[] = new String[]{'|',\"two\"};");
    }

    public void testSingleQuoteBeforeBrace() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "String s[] = new String[]{\"one\",|};",
                "'",
                "String s[] = new String[]{\"one\",'|'};");
    }

    public void testSingleQuoteBeforeSemicolon() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "String s = \"\" + |;",
                "'",
                "String s = \"\" + '|';");
    }

    public void testsingleQuoteBeforeSemicolonWithWhitespace() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "String s = \"\" +| ;",
                "'",
                "String s = \"\" +'|' ;");
    }

    public void testSingleQuoteAfterEscapeSequence() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "\\|",
                "'",
                "\\'|");
    }

    public void testArray() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "int a|\n",
                "(",
                "int a(|)\n");
    }

    public void testArray2() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "int a(|)\n",
                ")",
                "int a()|\n");
    }

    public void testArray3() throws Exception {
        setDefaultsOptions(true);
        typeCharactersInText(
                "int a(1|)\n",
                ")",
                "int a(1)|\n");
    }
}
