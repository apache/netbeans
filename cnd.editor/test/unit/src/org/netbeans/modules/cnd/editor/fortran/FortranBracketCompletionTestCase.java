/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
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
