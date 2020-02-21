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

import junit.framework.TestCase;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndLexerUtilities.FortranFormat;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test several lexer inputs.
 *
 */
public class FortranLexerBatchTestCase extends TestCase {

    public FortranLexerBatchTestCase(String testName) {
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

    protected InputAttributes getLexerAttributes(FortranFormat format) {
        InputAttributes lexerAttrs = new InputAttributes();
        lexerAttrs.setValue(FortranTokenId.languageFortran(), CndLexerUtilities.FORTRAN_MAXIMUM_TEXT_WIDTH, 132, true);
        lexerAttrs.setValue(FortranTokenId.languageFortran(), CndLexerUtilities.FORTRAN_FREE_FORMAT, format, true);
        return lexerAttrs;
    }

    public void testAloneBackSlash() {
        String text = "\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FIXED));
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        assertFalse("No more tokens", ts.moveNext());
    }

    public void testComments() {
        String text = "!abc\ncabc";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FIXED));
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.LINE_COMMENT_FREE, "!abc");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.LINE_COMMENT_FIXED, "cabc");
    }

    public void testContinuation() {
        String text = "     1100";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FIXED));
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, "     ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.LINE_CONTINUATION_FIXED, "1");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "100");
    }

    public void testContinuation2() {
        String text = "     DO";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FIXED));
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, "     ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.LINE_CONTINUATION_FIXED, "D");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "O");
    }

    public void testIdentifiers() {
        String text = "a ab aB2 2a x\nyZ\r\nz";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FREE));
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "ab");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "aB2");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "2");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "yZ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, "\r");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "z");
    }

    public void testApostropheChar() {
        String text = "id'";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FIXED));
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "id");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.APOSTROPHE_CHAR, "'");
    }

    public void testStringLiterals() {
        String text = "\"\" \"a\"\"\" \"\\\"\" \"\\\\\" \"\\\\\\\"\" \"\\n\" \"a";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FIXED));
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"a\"");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"\\\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"\\\\\"");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"\\\\\\\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"\\n\"");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"a");
    }

    public void testNumberLiterals() {
        String text = "0 00 09 1 12" +
                " o'7' b'101' z'A1' 1.1 1e1 1d1";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FREE));
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "00");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "09");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "1");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "12");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_OCTAL, "o'7'");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_BINARY, "b'101'");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_HEX, "z'A1'");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1.1");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1e1");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1d1");
    }

    public void testNumberLiterals2() {
        String text = "12345 09 1 12" +
                " o'7' b'101' z'A1' 1.1 1e1 1d1";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FIXED));
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "12345");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "09");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "1");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "12");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_OCTAL, "o'7'");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_BINARY, "b'101'");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_HEX, "z'A1'");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1.1");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1e1");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1d1");
    }

    public void testRealLiterals() {
        String text = "1.23e9 1.23e+9 1.23e-9 "+
                      "1.23d9 1.23d+9 1.23d-9 "+
                      "1.23q9 1.23q+9 1.23q-9 "+
                      "1.23e9_4 1.23e+9_8 1.23e-9_16 ";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FIXED));
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1.23e9");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1.23e+9");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1.23e-9");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1.23d9");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1.23d+9");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1.23d-9");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1.23q9");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1.23q+9");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1.23q-9");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1.23e9_4");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1.23e+9_8");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1.23e-9_16");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
    }

    public void testOperators() {
        String text = "** * / + - // == /= < <= > >=";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FREE));
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_POWER, "**");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_MUL, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_DIV, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_PLUS, "+");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_MINUS, "-");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_CONCAT, "//");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_LOG_EQ, "==");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_NOT_EQ, "/=");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_LT, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_LT_EQ, "<=");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_GT, ">");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_GT_EQ, ">=");
        assertFalse("No more tokens", ts.moveNext());
    }

    public void testOperators2() {
        String text = "\t** * / + - // == /= < <= > >=";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FIXED));
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, "\t");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_POWER, "**");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_MUL, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_DIV, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_PLUS, "+");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_MINUS, "-");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_CONCAT, "//");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_LOG_EQ, "==");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_NOT_EQ, "/=");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_LT, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_LT_EQ, "<=");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_GT, ">");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_GT_EQ, ">=");
        assertFalse("No more tokens", ts.moveNext());
    }

    private String getAllKeywords() {
        return "allocatable allocate apostrophe assignment backspace\n" +
                "bind block blockdata call case character close common complex contains\n" +
                " continue cycle data deallocate default dimension do double\n" +
                "doubleprecision elemental else elseif elsewhere end endassociate endblock\n" +
                "endblockdata enddo endenum end endfile endforall endfunction endif\n" +
                "endinterface endmap endmodule endprogram endselect endstructure endsubroutine\n" +
                "endtype endunion endwhere entry eor equivalance err exist exit external\n" +
                "file file forall form format formatted function go goto if implicit in\n" +
                "include inout inquire integer intent interface intrinsic iostat kind len\n" +
                "logical map module name named namelist nextrec nml none nullify number\n" +
                "only open opened operator optional out pad parameter pointer position precision\n" +
                "print private procedure program public pure quote read read\n" +
                "readwrite real rec recl recursive result return rewind save select selectcase\n" +
                "selecttype sequence sequential size size stat status stop structure\n" +
                "subroutine target then to type unformatted union use where while write write";
    }

    public void testKeywords() {
        String text = getAllKeywords();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FREE));
        TokenSequence<?> ts = hi.tokenSequence();

        //    CndLexerUnitTest.dumpTokens(ts, "ts");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ALLOCATABLE, "allocatable");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ALLOCATE, "allocate");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_APOSTROPHE, "apostrophe");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ASSIGNMENT, "assignment");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_BACKSPACE, "backspace");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_BIND, "bind");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_BLOCK, "block");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_BLOCKDATA, "blockdata");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_CALL, "call");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_CASE, "case");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_CHARACTER, "character");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_CLOSE, "close");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_COMMON, "common");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_COMPLEX, "complex");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_CONTAINS, "contains");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_CONTINUE, "continue");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_CYCLE, "cycle");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_DATA, "data");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_DEALLOCATE, "deallocate");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_DEFAULT, "default");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_DIMENSION, "dimension");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_DO, "do");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_DOUBLE, "double");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_DOUBLEPRECISION, "doubleprecision");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ELEMENTAL, "elemental");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ELSE, "else");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ELSEIF, "elseif");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ELSEWHERE, "elsewhere");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_END, "end");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDASSOCIATE, "endassociate");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDBLOCK, "endblock");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDBLOCKDATA, "endblockdata");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDDO, "enddo");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDENUM, "endenum");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_END, "end");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDFILE, "endfile");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDFORALL, "endforall");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDFUNCTION, "endfunction");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDIF, "endif");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDINTERFACE, "endinterface");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDMAP, "endmap");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDMODULE, "endmodule");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDPROGRAM, "endprogram");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDSELECT, "endselect");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDSTRUCTURE, "endstructure");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDSUBROUTINE, "endsubroutine");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDTYPE, "endtype");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDUNION, "endunion");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENDWHERE, "endwhere");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ENTRY, "entry");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "eor");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_EQUIVALENCE, "equivalance");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "err");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "exist");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_EXIT, "exit");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_EXTERNAL, "external");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "file");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "file");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_FORALL, "forall");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "form");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_FORMAT, "format");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "formatted");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_FUNCTION, "function");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_GO, "go");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_GOTO, "goto");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_IF, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_IMPLICIT, "implicit");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_IN, "in");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INCLUDE, "include");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INOUT, "inout");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INQUIRE, "inquire");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INTEGER, "integer");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INTENT, "intent");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INTERFACE, "interface");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INTRINSIC, "intrinsic");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "iostat");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_KIND, "kind");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_LEN, "len");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_LOGICAL, "logical");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_MAP, "map");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_MODULE, "module");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "name");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "named");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_NAMELIST, "namelist");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "nextrec");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "nml");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_NONE, "none");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_NULLIFY, "nullify");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "number");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_ONLY, "only");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_OPEN, "open");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "opened");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_OPERATOR, "operator");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_OPTIONAL, "optional");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_OUT, "out");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "pad");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_PARAMETER, "parameter");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_POINTER, "pointer");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "position");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_PRECISION, "precision");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_PRINT, "print");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_PRIVATE, "private");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_PROCEDURE, "procedure");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_PROGRAM, "program");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_PUBLIC, "public");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_PURE, "pure");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_QUOTE, "quote");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_READ, "read");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_READ, "read");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "readwrite");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_REAL, "real");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "rec");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "recl");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_RECURSIVE, "recursive");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_RESULT, "result");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_RETURN, "return");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_REWIND, "rewind");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_SAVE, "save");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_SELECT, "select");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_SELECTCASE, "selectcase");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_SELECTTYPE, "selecttype");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_SEQUENCE, "sequence");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "sequential");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "size");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "size");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_STAT, "stat");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "status");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_STOP, "stop");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_STRUCTURE, "structure");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_SUBROUTINE, "subroutine");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_TARGET, "target");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_THEN, "then");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_TO, "to");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_TYPE, "type");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "unformatted");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_UNION, "union");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_USE, "use");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_WHERE, "where");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_WHILE, "while");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_WRITE, "write");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_WRITE, "write");

        assertFalse("No more tokens", ts.moveNext());
    }
    public void testKeywordExtensions() {
        String text = "int\n" +
                "short\n" +
                "long\n" +
                "signed\n" +
                "unsigned\n" +
                "size_t\n" +
                "int8_t\n" +
                "int16_t\n" +
                "int32_t\n" +
                "int64_t\n" +
                "int_least8_t\n" +
                "int_least16_t\n" +
                "int_least32_t\n" +
                "int_least64_t\n" +
                "int_fast8_t\n" +
                "int_fast16_t\n" +
                "int_fast32_t\n" +
                "int_fast64_t\n" +
                "intmax_t\n" +
                "intptr_t\n" +
                "float\n" +
                "double\n" +
                "_Complex\n" +
                "_Bool\n" +
                " char";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FREE));
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INT, "int");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_SHORT, "short");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_LONG, "long");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_SIGNED, "signed");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_UNSIGNED, "unsigned");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_SIZE_T, "size_t");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INT8_T, "int8_t");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INT16_T, "int16_t");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INT32_T, "int32_t");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INT64_T, "int64_t");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INT_LEAST8_T, "int_least8_t");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INT_LEAST16_T, "int_least16_t");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INT_LEAST32_T, "int_least32_t");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INT_LEAST64_T, "int_least64_t");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INT_FAST8_T, "int_fast8_t");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INT_FAST16_T, "int_fast16_t");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INT_FAST32_T, "int_fast32_t");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INT_FAST64_T, "int_fast64_t");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INTMAX_T, "intmax_t");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_INTPTR_T, "intptr_t");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_FLOAT, "float");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_DOUBLE, "double");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "_Complex");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.IDENTIFIER, "_Bool");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.FortranTokenId.KW_CHAR, "char");

        assertFalse("No more tokens", ts.moveNext());
    }

    public void testNonKeywords() {
        String text = "asma autos b br car dou doubl finall im i ifa inti throwx ";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FREE));
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "asma");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "autos");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "b");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "br");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "car");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "dou");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "doubl");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "finall");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "im");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "i");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "ifa");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "inti");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "throwx");
    }
    public void testNonKeywords2() {
        String text = "\tasma autos b br car dou doubl finall im i ifa inti throwx ";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FIXED));
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, "\t");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "asma");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "autos");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "b");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "br");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "car");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "dou");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "doubl");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "finall");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "im");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "i");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "ifa");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "inti");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "throwx");
    }

    public void testTab() {
        String text = "\tprint *";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes(FortranFormat.FIXED));
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, "\t");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_PRINT, "print");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_MUL, "*");
    }

}
