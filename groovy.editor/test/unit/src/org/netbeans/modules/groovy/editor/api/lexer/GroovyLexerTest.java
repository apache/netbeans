/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.api.lexer;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.groovy.editor.test.GroovyTestBase;

/**
 * Although this test class might not be necessary it is often not obvious what
 * is the meaning of some Groovy Lexer tokens. For such cases this should help
 * to get better idea.
 * 
 * @author Martin Janicek
 */
public class GroovyLexerTest extends GroovyTestBase {

    public GroovyLexerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }
    
    public void testCharInDeclaration() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("a=\'x\'");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.ASSIGN, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\'x\'");
    }
    
    public void testStringInDeclaration() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("a = \"string\"");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.ASSIGN, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"string\"");
    }
    
    public void testMultilineString() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor(
            "def text = \"\"\"\\\n" +
            "hello there\n" +
            "how are you?\n" +
            "\"\"\"");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LITERAL_def, "def");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "text");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.ASSIGN, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, 
            "\"\"\"\\\n" +
            "hello there\n" +
            "how are you?\n" +
            "\"\"\"");
    }
    
    public void testGString() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor(
            "def text = \"hello there ${name}, how are you?\"");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LITERAL_def, "def");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "text");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.ASSIGN, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"hello there $");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LBRACE, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "name");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.RBRACE, "}");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, ", how are you?\"");
    }
    
    public void testLineComment() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("// def =\"string\"");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LINE_COMMENT, "// def =\"string\"");
    }
    
    // Groovy supports special line comment started with #!
    public void testLineComment_SH_comment() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("#! def =\"string\"");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LINE_COMMENT, "#! def =\"string\"");
    }
    
    public void testBlockComment() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor(
                "/*"
                + "Testing block comment\n"
                + ".. is it work?\n"
                + "*/");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.BLOCK_COMMENT,
                "/*"
                + "Testing block comment\n"
                + ".. is it work?\n"
                + "*/");
    }
    
    public void testDocumentation() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor(
                "/**"
                + "Some method description.\n"
                + "@param whatever\n"
                + "@return whaaat?"
                + "*/");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.BLOCK_COMMENT,
                "/**"
                + "Some method description.\n"
                + "@param whatever\n"
                + "@return whaaat?"
                + "*/");
    }
    
    public void testForLoop() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("for (int i = 0; i < 10; i++) {");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LITERAL_for, "for");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LITERAL_int, "int");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "i");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.ASSIGN, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.NUM_INT, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.SEMI, ";");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "i");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LT, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.NUM_INT, "10");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.SEMI, ";");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "i");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.INC, "++");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.RPAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LBRACE, "{");
    }
    
    private TokenSequence<GroovyTokenId> createTokenSequenceFor(String text) {
        return TokenHierarchy.create(text, GroovyTokenId.language()).tokenSequence(GroovyTokenId.language());
    }
}
