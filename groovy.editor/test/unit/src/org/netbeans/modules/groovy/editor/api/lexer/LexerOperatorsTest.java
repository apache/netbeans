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
 *
 * @author Martin Janicek
 */
public class LexerOperatorsTest extends GroovyTestBase {

    public LexerOperatorsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }
    
    public void testRegexpFindOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("\"123\" =~ \"1\"");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"123\"");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.REGEX_FIND, "=~");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"1\"");
    }
    
    public void testRegexpMatchOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("\"1\" ==~ /\\d+/");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"1\"");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.REGEX_MATCH, "==~");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        // Not sure if Lexer is returning correct token here, shouldn't this be something like REGEXP_LITERAL ??
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "/\\d+/");
    }
    
    public void testElvisOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("abc?:");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.ELVIS_OPERATOR, "?:");
    }
     
    public void testIdentityOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("0 <=> 1");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.NUM_INT, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.COMPARE_TO, "<=>");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.NUM_INT, "1");
    }
    
    public void testSafeNavigationOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("abc?.");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.OPTIONAL_DOT, "?.");
    }
    
    public void testMethodReferenceOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("abc.&");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.MEMBER_POINTER, ".&");
    }
    
    public void testSpreadOperatorOnMap() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("foo(1:\"a\", *:x)");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.LPAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.NUM_INT, "1");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.COLON, ":");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STRING_LITERAL, "\"a\"");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.COMMA, ",");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.WHITESPACE, " ");
        
        // Not sure why Star + Colon, I would expected Spread_Map_Arg token instead
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.STAR, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.COLON, ":");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.RPAREN, ")");
    }
    
    public void testJavaFieldOverrideOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("abc.@");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.DOT, ".");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.AT, "@");
    }
    
    // Interestingly enough no token is comming from Groovy Lexer for Spread Java Field  
    // Instead of such expected behavior there are two tokens SPREAD_DOT + AT
    public void testSpreadJavaFieldOverrideOperator() {
        TokenSequence<GroovyTokenId> ts = createTokenSequenceFor("abc*.@");
        
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.IDENTIFIER, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.SPREAD_DOT, "*.");
        LexerTestUtilities.assertNextTokenEquals(ts, GroovyTokenId.AT, "@");
    }
    
    private TokenSequence<GroovyTokenId> createTokenSequenceFor(String text) {
        return TokenHierarchy.create(text, GroovyTokenId.language()).tokenSequence(GroovyTokenId.language());
    }
}
