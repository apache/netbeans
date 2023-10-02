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

package org.netbeans.modules.cnd.lexer;

import java.util.Collections;
import junit.framework.TestCase;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppStringTokenId;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.DoxygenTokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.modules.cnd.utils.CndLanguageStandards.CndLanguageStandard;

/**
 * Test several lexer inputs.
 *
 */
public class CppLexerBatchTestCase extends TestCase {

    public CppLexerBatchTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    public void testAloneBackSlash() {
        String text = "\\\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_LINE, "\\\n");
        assertFalse("No more tokens", ts.moveNext());
    }

    public void testAloneBackSlashInPP() {
        doTestAloneBackSlashInPP(true);
        doTestAloneBackSlashInPP(false);
    }
    
    private void doTestAloneBackSlashInPP(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "include \"\\\n\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_DIRECTIVE, startText + "include \"\\\n\n");
        TokenSequence<?> ep = ts.embedded();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_INCLUDE, "include");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_USER_INCLUDE, "\"\\\n\n");

        assertFalse("No more tokens", ep.moveNext());
        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testPreprocEmbedding() {
        doTestPreprocEmbedding(true);
        doTestPreprocEmbedding(false);
    }

    private void doTestPreprocEmbedding(boolean altStartToken) {
        String startText = altStartToken ? "%:" : "#";
        String text = startText + "define C 1 \"/*\" /* \n@see C */";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_DIRECTIVE, startText + "define C 1 \"/*\" /* \n@see C */");

        TokenSequence<?> ep = ts.embedded();
        if (altStartToken) {
            LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_START_ALT, "%:");
        } else {
            LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_START, "#");
        }
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_DEFINE, "define");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_IDENTIFIER, "C");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.INT_LITERAL, "1");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.STRING_LITERAL, "\"/*\"");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.BLOCK_COMMENT, "/* \n@see C */");
        assertFalse("No more tokens", ep.moveNext());

        assertFalse("No more tokens", ts.moveNext());

    }

    public void testComments() {
        String text = "/// doxygen line comment\n/*ml-comment*//**//***//*! doxygen*//**\n*doxygen-comment*//* a";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOXYGEN_LINE_COMMENT, "/// doxygen line comment");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BLOCK_COMMENT, "/*ml-comment*/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BLOCK_COMMENT, "/**/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOXYGEN_COMMENT, "/***/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOXYGEN_COMMENT, "/*! doxygen*/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOXYGEN_COMMENT, "/**\n*doxygen-comment*/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BLOCK_COMMENT, "/* a");
        assertEquals(PartType.START, ts.token().partType());
    }

    public void testIdentifiers() {
        String text = "a ab aB2 2a x\nyZ\r\nz";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "ab");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "aB2");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "2");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "yZ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\r\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "z");
        assertFalse("No more tokens", ts.moveNext());
    }

    public void testStrangeIdentifiers() {
        String text = "$a àà";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "$a");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "àà");
        assertFalse("No more tokens", ts.moveNext());
    }

    public void testCharLiterals() {
        String text = "'' 'a''' '\\'' '\\\\' '\\\\\\'' '\\n' 'a";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "''");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "'a'");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "''");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "'\\''");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "'\\\\'");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "'\\\\\\''");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "'\\n'");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR_LITERAL, "'a");
        assertEquals(PartType.START, ts.token().partType());
    }

    public void testStringLiterals() {
        String text = "\"\" \"a\"\"\" \"\\\"\" \"\\\\\" \"\\\\\\\"\" \"\\n\" \"a";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"a\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"\\\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"\\\\\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"\\\\\\\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"\\n\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"a");
        assertEquals(PartType.START, ts.token().partType());
    }

    public void testUTF16Strings() {
        // u"This is a bigger Unicode Character: \u2018."
        String text = "u\"This is a bigger Unicode Character: \\u2018.\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.STRING_LITERAL, "u\"This is a bigger Unicode Character: \\u2018.\"");
        TokenSequence<?> ep = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_u, "u");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, "This is a bigger Unicode Character: ");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.UNICODE_ESCAPE, "\\u2018");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, ".");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.LAST_QUOTE, "\"");
        assertFalse("No more tokens", ep.moveNext());

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testUTF32Strings() {
        // U"This is a Unicode Character: \u2018."
        String text = "U\"This is a Unicode Character: \\u2018.\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.STRING_LITERAL, "U\"This is a Unicode Character: \\u2018.\"");
        TokenSequence<?> ep = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_U, "U");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, "This is a Unicode Character: ");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.UNICODE_ESCAPE, "\\u2018");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, ".");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.LAST_QUOTE, "\"");
        assertFalse("No more tokens", ep.moveNext());

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testUTF8trings() {
        // u8"This is a Unicode Character: \u2018."
        String text = "u8\"This is a Unicode Character: \\u2018.\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.STRING_LITERAL, "u8\"This is a Unicode Character: \\u2018.\"");
        TokenSequence<?> ep = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_u8, "u8");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, "This is a Unicode Character: ");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.UNICODE_ESCAPE, "\\u2018");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, ".");
        LexerTestUtilities.assertNextTokenEquals(ep, org.netbeans.cnd.api.lexer.CppStringTokenId.LAST_QUOTE, "\"");
        assertFalse("No more tokens", ep.moveNext());

        assertFalse("No more tokens", ts.moveNext());
    }

    public void test_RawStrings() {
        // R"delimeter(The String Data \ Stuff " )delimeterff )delimeter"
        String text = "R\"delimeter(The String Data \\ Stuff \" )delimeterff )delimeter\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.RAW_STRING_LITERAL, "R\"delimeter(The String Data \\ Stuff \" )delimeterff )delimeter\"");
        assertFalse("No more tokens", ts.moveNext());
    }

    public void test_RawUTF8Strings() {
        // u8R"XXX(I'm a "raw UTF-8" XXX string.)XXX"
        String text = "u8R\"XXX(I'm a \"raw UTF-8\" XXX string.)XXX\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.RAW_STRING_LITERAL, "u8R\"XXX(I'm a \"raw UTF-8\" XXX string.)XXX\"");
        assertFalse("No more tokens", ts.moveNext());
    }

    public void test_RawUTF16Strings() {
        // uR"*(This is a "raw UTF-16" string.)*)*"
        String text = "uR\"*(This is a \"raw UTF-16\" string.)*)*\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.RAW_STRING_LITERAL, "uR\"*(This is a \"raw UTF-16\" string.)*)*\"");
        assertFalse("No more tokens", ts.moveNext());
    }

    public void test_RawUTF32Strings() {
        // UR".(This is a "raw UTF-32" string.)."
        String text = "UR\".(This is a \"raw UTF-32\" string.).\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.RAW_STRING_LITERAL, "UR\".(This is a \"raw UTF-32\" string.).\"");
        assertFalse("No more tokens", ts.moveNext());
    }

    public void test_RawWStrings() {
        // LR".(This is a "raw W" string.)."
        String text = "LR\".(This is a \"raw W\" string.).\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.RAW_STRING_LITERAL, "LR\".(This is a \"raw W\" string.).\"");
        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void test_RawString() {
        // UR".(This is a "raw UTF-32" string.)."
        String text = "R\"()\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.RAW_STRING_LITERAL, "R\"()\"");
        TokenSequence<?> es = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_R, "R");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.START_DELIMETER_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.END_DELIMETER_PAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.LAST_QUOTE, "\"");

        assertFalse("No more tokens", es.moveNext());
        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void test_IncompleteRawString_1() {
        String text = "R\"\" after raw string";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.RAW_STRING_LITERAL, "R\"\"");
        
        TokenSequence<?> es = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_R, "R");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.LAST_QUOTE, "\"");
        assertFalse("No more tokens", es.moveNext());
        
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.IDENTIFIER, "after");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.IDENTIFIER, "raw");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.IDENTIFIER, "string");

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void test_IncompleteRawString_2() {
        String text = "u8R\" not complete";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.RAW_STRING_LITERAL, "u8R\"");

        TokenSequence<?> es = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_u8R, "u8R");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.FIRST_QUOTE, "\"");
        assertFalse("No more tokens", es.moveNext());

        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.ALTERNATE_NOT, "not");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.IDENTIFIER, "complete");
        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void test_IncompleteRawString_3() {
        String text = "UR\"( not complete";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.RAW_STRING_LITERAL, "UR\"( not complete");
        TokenSequence<?> es = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_UR, "UR");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.START_DELIMETER_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, " not complete");

        assertFalse("No more tokens", es.moveNext());
        assertFalse("No more tokens", ts.moveNext());
        
    }
    
    public void test_IncompleteRawString_4() {
        String text = "UR\"( not complete\" other text";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.RAW_STRING_LITERAL, "UR\"( not complete\" other text");
        TokenSequence<?> es = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_UR, "UR");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.START_DELIMETER_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, " not complete");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.DOUBLE_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, " other text");

        assertFalse("No more tokens", es.moveNext());
        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void test_IncompleteRawString_5() {
        String text = "LR\"( not complete\" other text";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.RAW_STRING_LITERAL, "LR\"( not complete\" other text");
        TokenSequence<?> es = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_LR, "LR");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.START_DELIMETER_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, " not complete");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.DOUBLE_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, " other text");

        assertFalse("No more tokens", es.moveNext());
        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void test_IncompleteNoDelimRawString_6() {
        String text = "R'\"\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.IDENTIFIER, "R");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.CHAR_LITERAL, "'\"\"");
        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void test_IncompleteNoDelimRawString_6_1() {
        String text = "#define R'\"\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_DIRECTIVE, "#define R'\"\"");  
        
        TokenSequence<?> es = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_DEFINE, "define");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_IDENTIFIER, "R");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppTokenId.CHAR_LITERAL, "'\"\"");
        assertFalse("No more tokens", es.moveNext());

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void test_IncompleteNoDelimRawString_6_2() {
        String text = "R\"no_delim_text\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.RAW_STRING_LITERAL, "R\"no_delim_text\"");
        TokenSequence<?> es = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_R, "R");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, "no_delim_text");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.LAST_QUOTE, "\"");
        assertFalse("No more tokens", es.moveNext());
        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void test_IncompleteNoDelimRawString_6_2_1() {
        String text = "R\"\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.RAW_STRING_LITERAL, "R\"\"");
        TokenSequence<?> es = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_R, "R");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.LAST_QUOTE, "\"");
        assertFalse("No more tokens", es.moveNext());
        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void test_IncompleteNoDelimRawString_6_2_2() {
        String text = "#define R\"\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_DIRECTIVE, "#define R\"\"");  
        
        TokenSequence<?> es = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_DEFINE, "define");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppTokenId.RAW_STRING_LITERAL, "R\"\"");
        
        TokenSequence<?> rs = es.embedded();
        LexerTestUtilities.assertNextTokenEquals(rs, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_R, "R");
        LexerTestUtilities.assertNextTokenEquals(rs, org.netbeans.cnd.api.lexer.CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(rs, org.netbeans.cnd.api.lexer.CppStringTokenId.LAST_QUOTE, "\"");
        assertFalse("No more tokens", rs.moveNext());
        
        assertFalse("No more tokens", es.moveNext());

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testNumberLiterals() {
        String text = "0 00 09 1 12 0L 1l 12L 0LL 1ll 0x1 0xf 0XdE 0Xbcy" +
                " 09.5 1.5f 1.6F 6u 7U 7e3 6.1E-7f .3 3614090360UL 3614090360ul 0xffffffffull" +
                " 0x1234l 0x1234L 0.0747474774773784e-4L 0.0747474774773784e-4l 500lu 0x8000000000000000LLU";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "00");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "09");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "1");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "12");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG_LITERAL, "0L");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG_LITERAL, "1l");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG_LITERAL, "12L");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG_LONG_LITERAL, "0LL");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG_LONG_LITERAL, "1ll");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "0x1");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "0xf");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "0XdE");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "0Xbc");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "y");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOUBLE_LITERAL, "09.5");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.FLOAT_LITERAL, "1.5f");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.FLOAT_LITERAL, "1.6F");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNSIGNED_LITERAL, "6u");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNSIGNED_LITERAL, "7U");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOUBLE_LITERAL, "7e3");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.FLOAT_LITERAL, "6.1E-7f");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOUBLE_LITERAL, ".3");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNSIGNED_LONG_LITERAL, "3614090360UL");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNSIGNED_LONG_LITERAL, "3614090360ul");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNSIGNED_LONG_LONG_LITERAL, "0xffffffffull");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG_LITERAL, "0x1234l");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LONG_LITERAL, "0x1234L");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOUBLE_LITERAL, "0.0747474774773784e-4L");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOUBLE_LITERAL, "0.0747474774773784e-4l");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNSIGNED_LONG_LITERAL, "500lu");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.UNSIGNED_LONG_LONG_LITERAL, "0x8000000000000000LLU");
        assertFalse("No more tokens", ts.moveNext());
    }

    public void testOperators() {
        String text = "= > < ! ~ ? : == <= >= != && || ++ -- + - * / & | ^ % << >> += -= *= /= &= |= ^= %= <<= >>=";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.EQ, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.GT, ">");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LT, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NOT, "!");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.TILDE, "~");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.QUESTION, "?");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.COLON, ":");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.EQEQ, "==");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LTEQ, "<=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.GTEQ, ">=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NOTEQ, "!=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.AMPAMP, "&&");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BARBAR, "||");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PLUSPLUS, "++");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.MINUSMINUS, "--");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PLUS, "+");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.MINUS, "-");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STAR, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SLASH, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.AMP, "&");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BAR, "|");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CARET, "^");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PERCENT, "%");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LTLT, "<<");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.GTGT, ">>");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PLUSEQ, "+=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.MINUSEQ, "-=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STAREQ, "*=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SLASHEQ, "/=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.AMPEQ, "&=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BAREQ, "|=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CARETEQ, "^=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PERCENTEQ, "%=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LTLTEQ, "<<=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.GTGTEQ, ">>=");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testLineContinuation() {
        String text = "  #de\\\n" +
                      "fine A\\\r" +
                      "AA 1 // comment \\\n" +
                      " comment-again\n" +
                      "ch\\\n" +
                      "ar* a = \"str\\\n" +
                      "0\"\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "  ");
        assertEquals(0, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_DIRECTIVE, "#de\\\nfine A\\\rAA 1 // comment \\\n comment-again\n");
        assertEquals(2, ts.offset());
        TokenSequence<?> ep = ts.embedded();

        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_START, "#");
        assertEquals(2, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_DEFINE, "de\\\nfine");
        assertEquals(3, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        assertEquals(11, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_IDENTIFIER, "A\\\rAA");
        assertEquals(12, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        assertEquals(17, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.INT_LITERAL, "1");
        assertEquals(18, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        assertEquals(19, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.LINE_COMMENT, "// comment \\\n comment-again");
        assertEquals(20, ep.offset());
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.NEW_LINE, "\n");
        assertEquals(47, ep.offset());
        assertFalse("No more tokens", ep.moveNext());

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CHAR, "ch\\\nar");
        assertEquals(48, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STAR, "*");
        assertEquals(54, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        assertEquals(55, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "a");
        assertEquals(56, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        assertEquals(57, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.EQ, "=");
        assertEquals(58, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        assertEquals(59, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"str\\\n0\"");
        assertEquals(60, ts.offset());

        TokenSequence<?> es = ts.embedded();

        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.TEXT, "str\\\n0");
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.LAST_QUOTE, "\"");
        assertEquals(67, es.offset());

        assertFalse("No more tokens", es.moveNext());

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        assertEquals(68, ts.offset());
        assertFalse("No more tokens", ts.moveNext());

    }

    public void testLineContinuationAfterSlash() {
        String text = "\n" +
                      "    /\\\n\n" +
                      "#define /\\\n    \n\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, "    ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SLASH, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_LINE, "\\\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.PREPROCESSOR_DIRECTIVE, "#define /\\\n    \n");

        TokenSequence<?> ep = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_START, "#");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.PREPROCESSOR_DEFINE, "define");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.SLASH, "/");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.ESCAPED_LINE, "\\\n");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.WHITESPACE, "    ");
        LexerTestUtilities.assertNextTokenEquals(ep, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ep.moveNext());

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        assertFalse("No more tokens", ts.moveNext());
    }

    private enum L {
        C,
        C11,
        C23,
        CPP,
        CPP11,
        CPP17,
        CPP20,
        CPP23,
        BOTH
    }

    private static final class T {
        final TokenId id;
        final String t;
        final L l;

        private static T T(TokenId id, String t, L l) {
            return new T(id, t, l);
        }
        private T(TokenId id, String t, L l) {
            this.id = id;
            this.t = t;
            this.l = l;
        }
    }

    public void assertNextTokenEquals(TokenSequence<?> ts, T t, L current) {
        if (t.l == L.BOTH) {
            LexerTestUtilities.assertNextTokenEquals(ts, t.id, t.t);
        } else if (t.l == current) {
            LexerTestUtilities.assertNextTokenEquals(ts, t.id, t.t);
        } else {
            if (current == L.CPP11 && t.id == CppTokenId.IDENTIFIER) {
                Filter<CppTokenId> keywordsFilter = (Filter<CppTokenId>) 
                        CndLexerUtilities.getFilter(CppTokenId.languageCpp(), CndLanguageStandard.CPP11);
                CppTokenId filtered = keywordsFilter.check(t.t);
                if (filtered == null) {
                    filtered = CppTokenId.IDENTIFIER;
                }
                LexerTestUtilities.assertNextTokenEquals(ts, filtered, t.t);
            } else {
                LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, t.t);
            }
        }
    }

    private T[] getAllKW() {
        return new T[] {
            T.T(CppTokenId.ALIGNAS, "alignas", L.CPP11),
            T.T(CppTokenId.ALIGNOF, "alignof", L.CPP11),
            T.T(CppTokenId.ALTERNATE_AND, "and", L.CPP),
            T.T(CppTokenId.ALTERNATE_AND_EQ, "and_eq", L.CPP),
            T.T(CppTokenId.ASM, "asm", L.BOTH), // not standard in C, but accepted by GCC
            T.T(CppTokenId.AUTO, "auto", L.BOTH),
            T.T(CppTokenId.ALTERNATE_BITAND, "bitand", L.CPP),
            T.T(CppTokenId.BOOL, "bool", L.CPP),
            T.T(CppTokenId.BREAK, "break", L.BOTH),
            T.T(CppTokenId.CASE, "case", L.BOTH),
            T.T(CppTokenId.CATCH, "catch", L.CPP),
            T.T(CppTokenId.CHAR, "char", L.BOTH),
            T.T(CppTokenId.CHAR8_T, "char8_t", L.CPP20),
            T.T(CppTokenId.CHAR16_T, "char16_t", L.CPP11),
            T.T(CppTokenId.CHAR32_T, "char32_t", L.CPP11),
            T.T(CppTokenId.CLASS, "class", L.CPP),
            T.T(CppTokenId.ALTERNATE_COMPL, "compl", L.CPP),
            T.T(CppTokenId.CONCEPT, "concept", L.CPP20),
            T.T(CppTokenId.CONST, "const", L.BOTH),
            T.T(CppTokenId.CONSTEVAL, "consteval", L.CPP20),
            T.T(CppTokenId.CONSTEXPR, "constexpr", L.CPP11),
            T.T(CppTokenId.CONSTINIT, "constinit", L.CPP20),
            T.T(CppTokenId.CONST_CAST, "const_cast", L.CPP),
            T.T(CppTokenId.CONTINUE, "continue", L.BOTH),
            T.T(CppTokenId.CO_AWAIT, "co_await", L.CPP20),
            T.T(CppTokenId.CO_RETURN, "co_return", L.CPP20),
            T.T(CppTokenId.CO_YIELD, "co_yield", L.CPP20),
            T.T(CppTokenId.DECLTYPE, "decltype", L.CPP11),
            T.T(CppTokenId.DEFAULT, "default", L.BOTH),
            T.T(CppTokenId.DELETE, "delete", L.CPP),
            T.T(CppTokenId.DO, "do", L.BOTH),
            T.T(CppTokenId.DOUBLE, "double", L.BOTH),
            T.T(CppTokenId.DYNAMIC_CAST, "dynamic_cast", L.CPP),
            T.T(CppTokenId.ELSE, "else", L.BOTH),
            T.T(CppTokenId.ENUM, "enum", L.BOTH),
            T.T(CppTokenId.EXPLICIT, "explicit", L.CPP),
            T.T(CppTokenId.EXTERN, "extern", L.BOTH),
            T.T(CppTokenId.EXPORT, "export", L.CPP),
            T.T(CppTokenId.FINAL, "final", L.CPP11),
            T.T(CppTokenId.FINALLY, "finally", L.CPP),
            T.T(CppTokenId.FLOAT, "float", L.BOTH),
            T.T(CppTokenId.FOR, "for", L.BOTH),
            //T.T(CppTokenId.FORTRAN, "fortran", L.C), // unsupported keyword
            T.T(CppTokenId.FRIEND, "friend", L.CPP),
            T.T(CppTokenId.GOTO, "goto", L.BOTH),
            T.T(CppTokenId.IF, "if", L.BOTH),
            T.T(CppTokenId.IMPORT, "import", L.CPP20),
            T.T(CppTokenId.INLINE, "inline", L.BOTH),
            T.T(CppTokenId.INT, "int", L.BOTH),
            T.T(CppTokenId.LONG, "long", L.BOTH),
            T.T(CppTokenId.MODULE, "module", L.CPP20),
            T.T(CppTokenId.MUTABLE, "mutable", L.CPP),
            T.T(CppTokenId.NAMESPACE, "namespace", L.CPP),
            T.T(CppTokenId.NEW, "new", L.CPP),
            T.T(CppTokenId.NOEXCEPT, "noexcept", L.CPP11),
            T.T(CppTokenId.ALTERNATE_NOT, "not", L.CPP),
            T.T(CppTokenId.ALTERNATE_NOT_EQ, "not_eq", L.CPP),
            T.T(CppTokenId.NULLPTR, "nullptr", L.CPP11),
            T.T(CppTokenId.OPERATOR, "operator", L.CPP),
            T.T(CppTokenId.ALTERNATE_OR, "or", L.CPP),
            T.T(CppTokenId.ALTERNATE_OR_EQ, "or_eq", L.CPP),
            T.T(CppTokenId.OVERRIDE, "override", L.CPP11),
            T.T(CppTokenId.PRIVATE, "private", L.CPP),
            T.T(CppTokenId.PROTECTED, "protected", L.CPP),
            T.T(CppTokenId.PUBLIC, "public", L.CPP),
            T.T(CppTokenId.REGISTER, "register", L.BOTH),
            T.T(CppTokenId.REINTERPRET_CAST, "reinterpret_cast", L.CPP),
            T.T(CppTokenId.REQUIRES, "requires", L.CPP20),
            T.T(CppTokenId.RESTRICT, "restrict", L.C),
            T.T(CppTokenId.RETURN, "return", L.BOTH),
            T.T(CppTokenId.SHORT, "short", L.BOTH),
            T.T(CppTokenId.SIGNED, "signed", L.BOTH),
            T.T(CppTokenId.SIZEOF, "sizeof", L.BOTH),
            T.T(CppTokenId.STATIC, "static", L.BOTH),
            T.T(CppTokenId.STATIC_ASSERT, "static_assert", L.CPP11),
            T.T(CppTokenId.STATIC_CAST, "static_cast", L.CPP),
            T.T(CppTokenId.STRUCT, "struct", L.BOTH),
            T.T(CppTokenId.SHORT, "short", L.BOTH),
            T.T(CppTokenId.SWITCH, "switch", L.BOTH),
            T.T(CppTokenId.TEMPLATE, "template", L.CPP),
            T.T(CppTokenId.THIS, "this", L.CPP),
            T.T(CppTokenId.THREAD_LOCAL, "thread_local", L.CPP11),
            T.T(CppTokenId.THROW, "throw", L.CPP),
            T.T(CppTokenId.TRY, "try", L.CPP),
            T.T(CppTokenId.TYPEDEF, "typedef", L.BOTH),
            T.T(CppTokenId.TYPEID, "typeid", L.CPP),
            T.T(CppTokenId.TYPENAME, "typename", L.CPP),
            T.T(CppTokenId.TYPEOF, "typeof", L.BOTH),
            T.T(CppTokenId.TYPEOF_UNQUAL, "typeof_unqual", L.C23),
            T.T(CppTokenId.UNION, "union", L.BOTH),
            T.T(CppTokenId.UNSIGNED, "unsigned", L.BOTH),
            T.T(CppTokenId.USING, "using", L.CPP),
            T.T(CppTokenId.VIRTUAL, "virtual", L.CPP),
            T.T(CppTokenId.VOID, "void", L.BOTH),
            T.T(CppTokenId.VOLATILE, "volatile", L.BOTH),
            T.T(CppTokenId.WCHAR_T, "wchar_t", L.CPP),
            T.T(CppTokenId.WHILE, "while", L.BOTH),
            T.T(CppTokenId.ALTERNATE_XOR, "xor", L.CPP),
            T.T(CppTokenId.ALTERNATE_XOR_EQ, "xor_eq", L.CPP),
            T.T(CppTokenId._ALIGNAS, "_Alignas", L.C11),
            T.T(CppTokenId._ALIGNOF, "_Alignof", L.C11),
            T.T(CppTokenId._ATOMIC, "_Atomic", L.C11),
            T.T(CppTokenId._BITINT, "_BitInt", L.C23),
            T.T(CppTokenId._BOOL, "_Bool", L.C),
            T.T(CppTokenId._COMPLEX, "_Complex", L.C),
            T.T(CppTokenId._DECIMAL32, "_Decimal32", L.C23),
            T.T(CppTokenId._DECIMAL64, "_Decimal64", L.C23),
            T.T(CppTokenId._DECIMAL128, "_Decimal128", L.C23),
            T.T(CppTokenId._GENERIC, "_Generic", L.C11),
            T.T(CppTokenId._IMAGINARY, "_Imaginary", L.C),
            T.T(CppTokenId._NORETURN, "_Noreturn", L.C11),
            //T.T(CppTokenId._PRAGMA, "_Pragma", L.C), C98 and C++11 --- can't test for that?
            T.T(CppTokenId._STATIC_ASSERT, "_Static_assert", L.C11),
            T.T(CppTokenId._THREAD_LOCAL, "_Thread_local", L.C11),
            T.T(CppTokenId.IDENTIFIER, "null", L.BOTH),
            T.T(CppTokenId.TRUE, "true", L.CPP),
            T.T(CppTokenId.FALSE, "false", L.CPP),
        };
    }
    
    
    public void testCppKeywords() {
        StringBuilder buf = new StringBuilder();
        for(T t : getAllKW()) {
            buf.append(t.t).append(' ');
        }
        String text = buf.toString();
        InputAttributes attrs = new InputAttributes();
        Language<CppTokenId> language = CppTokenId.languageCpp(); 
        attrs.setValue(language, CndLexerUtilities.LEXER_FILTER, CndLexerUtilities.getFilter(language, CndLanguageStandard.CPP98), true);  // NOI18N
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, language, 
                Collections.<CppTokenId>emptySet(), attrs);
        TokenSequence<?> ts = hi.tokenSequence();
        for(T t : getAllKW()) {
            assertNextTokenEquals(ts, t, L.CPP);
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        }
    }

    public void testCKeywords() {
        StringBuilder buf = new StringBuilder();
        for(T t : getAllKW()) {
            buf.append(t.t).append(' ');
        }
        String text = buf.toString();
        InputAttributes attrs = new InputAttributes();
        Language<CppTokenId> language = CppTokenId.languageC(); 
        attrs.setValue(language, CndLexerUtilities.LEXER_FILTER, CndLexerUtilities.getFilter(language, CndLanguageStandard.C89), true);  // NOI18N
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, language, 
                Collections.<CppTokenId>emptySet(), attrs);
        TokenSequence<?> ts = hi.tokenSequence();
        for(T t : getAllKW()) {
            assertNextTokenEquals(ts, t, L.C);
            LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        }
    }
    
   public void testNonKeywords() {
        String text = "asma autos b br car dou doubl finall im i ifa inti throwx ";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "asma");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "autos");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "b");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "br");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "car");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "dou");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "doubl");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "finall");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "im");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "i");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "ifa");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "inti");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "throwx");
    }

    public void testEmbedding() {
        String text = "ddx \"d\\t\\br\" /** @see X */ L\"Lex\" L2";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "ddx");
        assertEquals(0, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        assertEquals(3, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"d\\t\\br\"");
        assertEquals(4, ts.offset());

        TokenSequence<?> es = ts.embedded();

        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.FIRST_QUOTE, "\"");
        assertEquals(4, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.TEXT, "d");
        assertEquals(5, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.TAB, "\\t");
        assertEquals(6, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.BACKSPACE, "\\b");
        assertEquals(8, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.TEXT, "r");
        assertEquals(10, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.LAST_QUOTE, "\"");
        assertEquals(11, es.offset());

        assertFalse(es.moveNext());

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        assertEquals(12, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOXYGEN_COMMENT, "/** @see X */");
        assertEquals(13, ts.offset());

        TokenSequence<?> ds = ts.embedded();

        LexerTestUtilities.assertNextTokenEquals(ds, DoxygenTokenId.OTHER_TEXT, " ");
        assertEquals(16, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, DoxygenTokenId.TAG, "@see");
        assertEquals(17, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, DoxygenTokenId.OTHER_TEXT, " ");
        assertEquals(21, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, DoxygenTokenId.IDENT, "X");
        assertEquals(22, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, DoxygenTokenId.OTHER_TEXT, " ");
        assertEquals(23, ds.offset());

        assertFalse(ds.moveNext());

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        assertEquals(26, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "L\"Lex\"");
        assertEquals(27, ts.offset());

        es = ts.embedded();

        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.PREFIX_L, "L");
        assertEquals(27, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.FIRST_QUOTE, "\"");
        assertEquals(28, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.TEXT, "Lex");
        assertEquals(29, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, CppStringTokenId.LAST_QUOTE, "\"");
        assertEquals(32, es.offset());

        assertFalse(es.moveNext());

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        assertEquals(33, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "L2");

        assertFalse(ts.moveNext());
    }

    public void testStrings() {
        // IZ#144221: IDE highlights L' ' and L" " as wrong code
        String text = "L\"\\x20\\x9\\xD\\xA\" L'\"' L'\\x20'";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();      
        
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "L\"\\x20\\x9\\xD\\xA\"");  
        TokenSequence<?> es = ts.embedded();

        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_L, "L");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.HEX_ESCAPE, "\\x20");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.HEX_ESCAPE, "\\x9");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.HEX_ESCAPE, "\\xD");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.HEX_ESCAPE, "\\xA");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.LAST_QUOTE, "\"");
        assertFalse("No more tokens", es.moveNext());

        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.CHAR_LITERAL, "L'\"'");
        es = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_L, "L");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.SINGLE_QUOTE, "'");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.DOUBLE_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.SINGLE_QUOTE, "'");
        assertFalse("No more tokens", es.moveNext());
        
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppTokenId.CHAR_LITERAL, "L'\\x20'");
        es = ts.embedded();
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_L, "L");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.SINGLE_QUOTE, "'");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.HEX_ESCAPE, "\\x20");
        LexerTestUtilities.assertNextTokenEquals(es, org.netbeans.cnd.api.lexer.CppStringTokenId.SINGLE_QUOTE, "'");
        assertFalse("No more tokens", es.moveNext());
        
        assertFalse("No more tokens", ts.moveNext());
    }

    public void testSpecial() {
        String text = "\\ ... $ @";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BACK_SLASH, "\\");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ELLIPSIS, "...");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "$");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.AT, "@");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testSeparators() {
        String text = "( ) { } [ ] ; , . .* :: -> ->*";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LBRACE, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RBRACE, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LBRACKET, "[");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.RBRACKET, "]");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SEMICOLON, ";");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOT, ".");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.DOTMBR, ".*");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SCOPE, "::");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ARROW, "->");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ARROWMBR, "->*");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testComment2() {
        // IZ#83566: "*/" string is highlighted as error
        String text = "const/*    */int*/*       */i = 0; */";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.CONST, "const");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BLOCK_COMMENT, "/*    */");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT, "int");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STAR, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BLOCK_COMMENT, "/*       */");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "i");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.EQ, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT_LITERAL, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SEMICOLON, ";");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INVALID_COMMENT_END, "*/");

        assertFalse("No more tokens", ts.moveNext());

    }
}
