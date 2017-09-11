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

package org.netbeans.lib.java.lexer;

import java.util.EnumSet;
import junit.framework.TestCase;
import org.netbeans.api.java.lexer.JavaStringTokenId;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class JavaLexerBatchTest extends TestCase {

    public JavaLexerBatchTest(String testName) {
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

    public void testComments() {
        String text = "/*ml-comment*//**//***//**\n*javadoc-comment*//* a";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.BLOCK_COMMENT, "/*ml-comment*/");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.BLOCK_COMMENT, "/**/");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.JAVADOC_COMMENT, "/***/");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.JAVADOC_COMMENT, "/**\n*javadoc-comment*/");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.BLOCK_COMMENT, "/* a");
        assertEquals(PartType.START, ts.token().partType());
    }
    
    public void testIdentifiers() {
        String text = "a ab aB2 2a x\nyZ\r\nz";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "ab");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "aB2");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "2");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "yZ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, "\r\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "z");
    }
    
    public void testCharLiterals() {
        String text = "'' 'a''' '\\'' '\\\\' '\\\\\\'' '\\n' 'a";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL, "''");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL, "'a'");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL, "''");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL, "'\\''");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL, "'\\\\'");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL, "'\\\\\\''");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL, "'\\n'");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR_LITERAL, "'a");
        assertEquals(PartType.START, ts.token().partType());
    }
    
    public void testStringLiterals() {
        String text = "\"\" \"a\"\"\" \"\\\"\" \"\\\\\" \"\\\\\\\"\" \"\\n\" \"a";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"a\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"\\\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"\\\\\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"\\\\\\\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"\\n\"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"a");
        assertEquals(PartType.START, ts.token().partType());
    }
    
    public void testNumberLiterals() {
        String text = "0 00 09 1 12 0L 1l 12L 0x1 0xf 0XdE 0Xbcy" + 
                " 09.5 1.5f 2.5d 6d 7e3 6.1E-7f 0xa.5dp+12d .3 0x4l 0x5L";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "00");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "09");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "1");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "12");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LONG_LITERAL, "0L");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LONG_LITERAL, "1l");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LONG_LITERAL, "12L");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0x1");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0xf");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0XdE");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0Xbc");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "y");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOUBLE_LITERAL, "09.5");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.FLOAT_LITERAL, "1.5f");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOUBLE_LITERAL, "2.5d");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOUBLE_LITERAL, "6d");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOUBLE_LITERAL, "7e3");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.FLOAT_LITERAL, "6.1E-7f");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOUBLE_LITERAL, "0xa.5dp+12d");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOUBLE_LITERAL, ".3");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LONG_LITERAL, "0x4l");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LONG_LITERAL, "0x5L");
    }
    
    public void testOperators() {
        String text = "^ ^= % %= * *= / /= = ==";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CARET, "^");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CARETEQ, "^=");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.PERCENT, "%");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.PERCENTEQ, "%=");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STAR, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STAREQ, "*=");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.SLASH, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.SLASHEQ, "/=");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.EQ, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.EQEQ, "==");
    }

    public void testKeywords() {
        String text = "abstract assert boolean break byte case catch char class const continue " +
            "default do double else enum extends final finally float for goto if " +
            "implements import instanceof int interface long native new package " +
            "private protected public return short static strictfp super switch " +
            "synchronized this throw throws transient try void volatile while " +
            "null true false";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.ABSTRACT, "abstract");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.ASSERT, "assert");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.BOOLEAN, "boolean");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.BREAK, "break");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.BYTE, "byte");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CASE, "case");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CATCH, "catch");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CHAR, "char");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CLASS, "class");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CONST, "const");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.CONTINUE, "continue");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DEFAULT, "default");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DO, "do");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOUBLE, "double");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.ELSE, "else");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.ENUM, "enum");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.EXTENDS, "extends");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.FINAL, "final");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.FINALLY, "finally");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.FLOAT, "float");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.FOR, "for");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.GOTO, "goto");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IF, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IMPLEMENTS, "implements");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IMPORT, "import");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INSTANCEOF, "instanceof");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT, "int");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INTERFACE, "interface");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LONG, "long");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.NATIVE, "native");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.NEW, "new");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.PACKAGE, "package");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.PRIVATE, "private");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.PROTECTED, "protected");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.PUBLIC, "public");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.RETURN, "return");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.SHORT, "short");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STATIC, "static");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRICTFP, "strictfp");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.SUPER, "super");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.SWITCH, "switch");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.SYNCHRONIZED, "synchronized");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.THIS, "this");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.THROW, "throw");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.THROWS, "throws");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.TRANSIENT, "transient");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.TRY, "try");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.VOID, "void");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.VOLATILE, "volatile");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHILE, "while");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");

        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.NULL, "null");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.TRUE, "true");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.FALSE, "false");
    }

    public void testNonKeywords() {
        String text = "abstracta assertx b br car dou doubl finall im i ifa inti throwsx";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "abstracta");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "assertx");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "b");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "br");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "car");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "dou");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "doubl");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "finall");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "im");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "i");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "ifa");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "inti");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "throwsx");
    }
    
    public void testEmbedding() {
        String text = "ddx \"d\\t\\br\" /** @see X */";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavaTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "ddx");
        assertEquals(0, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        assertEquals(3, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\"d\\t\\br\"");
        assertEquals(4, ts.offset());
        
        TokenSequence<?> es = ts.embedded();
        
        LexerTestUtilities.assertNextTokenEquals(es, JavaStringTokenId.TEXT, "d");
        assertEquals(5, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, JavaStringTokenId.TAB, "\\t");
        assertEquals(6, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, JavaStringTokenId.BACKSPACE, "\\b");
        assertEquals(8, es.offset());
        LexerTestUtilities.assertNextTokenEquals(es, JavaStringTokenId.TEXT, "r");
        assertEquals(10, es.offset());
        
        assertFalse(es.moveNext());
        
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, " ");
        assertEquals(12, ts.offset());
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.JAVADOC_COMMENT, "/** @see X */");
        assertEquals(13, ts.offset());
        
        TokenSequence<?> ds = ts.embedded();
        
        LexerTestUtilities.assertNextTokenEquals(ds, JavadocTokenId.OTHER_TEXT, " ");
        assertEquals(16, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, JavadocTokenId.TAG, "@see");
        assertEquals(17, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, JavadocTokenId.OTHER_TEXT, " ");
        assertEquals(21, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, JavadocTokenId.IDENT, "X");
        assertEquals(22, ds.offset());
        LexerTestUtilities.assertNextTokenEquals(ds, JavadocTokenId.OTHER_TEXT, " ");
        assertEquals(23, ds.offset());
        
        assertFalse(ds.moveNext());
        
        assertFalse(ts.moveNext());
    }

    public void xtestExoticIdentifiers() {//Support for exotic identifiers has been removed 6999438
        String text = "a #\" \" #\"\\\"\"";
        InputAttributes attr = new InputAttributes();
        attr.setValue(JavaTokenId.language(), "version", Integer.valueOf(7), true);
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, JavaTokenId.language(), EnumSet.of(JavaTokenId.WHITESPACE), attr);
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "#\" \"");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "#\"\\\"\"");
    }

    public void testNoExoticIdentifiers() {
        String text = "a #\" \"";
        InputAttributes attr = new InputAttributes();
        attr.setValue(JavaTokenId.language(), "version", Integer.valueOf(5), true);
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, JavaTokenId.language(), EnumSet.of(JavaTokenId.WHITESPACE), attr);
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.ERROR, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.STRING_LITERAL, "\" \"");
    }

    public void testInterferenceBraceIdent() {
        String text = "() -> {A::a();}";
        InputAttributes attr = new InputAttributes();
        attr.setValue(JavaTokenId.language(), "version", Integer.valueOf(7), true);
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, JavaTokenId.language(), EnumSet.of(JavaTokenId.WHITESPACE), attr);
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.ARROW, "->");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LBRACE, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "A");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.COLONCOLON, "::");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.SEMICOLON, ";");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.RBRACE, "}");

        assertFalse(ts.moveNext());
    }

    public void testBinaryLiterals() {
        String text = "0b101 0B101 0b101l 0b101L";
        InputAttributes attr = new InputAttributes();
        attr.setValue(JavaTokenId.language(), "version", Integer.valueOf(7), true);
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, JavaTokenId.language(), EnumSet.of(JavaTokenId.WHITESPACE), attr);
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0b101");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0B101");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LONG_LITERAL, "0b101l");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LONG_LITERAL, "0b101L");
    }

    public void testNoBinaryLiterals() {
        String text = "0b101 0B101 0b101l 0b101L";
        InputAttributes attr = new InputAttributes();
        attr.setValue(JavaTokenId.language(), "version", Integer.valueOf(5), true);
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, JavaTokenId.language(), EnumSet.of(JavaTokenId.WHITESPACE), attr);
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "b101");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "B101");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "b101l");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "b101L");
    }

    public void testUnderscoresInLiterals() {
        String text = "_12 1_2 12_ 0_12 01_2 0x1_2";
        InputAttributes attr = new InputAttributes();
        attr.setValue(JavaTokenId.language(), "version", Integer.valueOf(7), true);
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, JavaTokenId.language(), EnumSet.of(JavaTokenId.WHITESPACE), attr);
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "_12");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "1_2");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "12");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "_");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0_12");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "01_2");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT_LITERAL, "0x1_2");
    }
    
    public void testUnicode() {
        String text = "//\\u000Aint\\u0020\\u002E\\uuuuuu002E\\u000A";
        InputAttributes attr = new InputAttributes();
        attr.setValue(JavaTokenId.language(), "version", Integer.valueOf(7), true);
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, JavaTokenId.language(), EnumSet.noneOf(JavaTokenId.class), attr);
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.LINE_COMMENT, "//\\u000A");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.INT, "int");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, "\\u0020");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOT, "\\u002E");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.DOT, "\\uuuuuu002E");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.WHITESPACE, "\\u000A");
    }
    
    public void testBrokenUnicode() {
        String text = "\\u000X\\u00";
        InputAttributes attr = new InputAttributes();
        attr.setValue(JavaTokenId.language(), "version", Integer.valueOf(7), true);
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, JavaTokenId.language(), EnumSet.noneOf(JavaTokenId.class), attr);
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.ERROR, "\\");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "u000X");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.ERROR, "\\");
        LexerTestUtilities.assertNextTokenEquals(ts, JavaTokenId.IDENTIFIER, "u00");
    }
}
