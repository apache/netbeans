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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.lexer;

import junit.framework.TestCase;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 */
public class EscapeLineTestCase extends TestCase {

    public EscapeLineTestCase(String testName) {
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

    public void testLineComment() {
        String text =  "//comm\\\n" + 
                       "ent\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.LINE_COMMENT, "//comm\\\nent");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    } 
    
    public void testBlockComment() {
        String text =  "/*comm\\\n" + 
                       "ent\n" +
                       "*/\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.BLOCK_COMMENT, "/*comm\\\nent\n*/");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testString() {
        String text =  "\"string\\\n" + 
                       "\" \"str\\\n" +
                       "ing\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"string\\\n\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.STRING_LITERAL, "\"str\\\ning\"");

        assertFalse("No more tokens", ts.moveNext());
    } 
    
    public void test1() {
        String text =  "in\\\n" + 
                       "t\\\n" + 
                       " a\\\r" + 
                       "\\\r\n" +
                       "; \\\r a\n" +
                       " \\\n b\n" +
                       "  \\\r\n c";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppTokenId.languageCpp());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.INT, "in\\\nt");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_LINE, "\\\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_LINE, "\\\r");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_LINE, "\\\r\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.SEMICOLON, ";");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_WHITESPACE, " \\\r ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_WHITESPACE, " \\\n ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "b");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.ESCAPED_WHITESPACE, "  \\\r\n ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppTokenId.IDENTIFIER, "c");

        assertFalse("No more tokens", ts.moveNext());
    }
   
}
