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
package org.netbeans.modules.cnd.lexer;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppStringTokenId;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * based on JavaStringLexerText
 * 
 */
public class CppStringLexerTestCase extends NbTestCase {

    public CppStringLexerTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testZero() {
        String text = "\\0";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppStringTokenId.languageSingle());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.OCTAL_ESCAPE, "\\0");

        assertFalse("No more tokens", ts.moveNext());
    }
    
    public void testNextToken1() {
        String text = "t";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppStringTokenId.languageDouble());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.TEXT, "t");
    }

    public void testNextToken2() {
        String text = "\"\\e\\t\\a\\b\\t \\tabc\\rsddfdsffffffffff\\uuuuAbcD\\377\"";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppStringTokenId.languageDouble());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.ANSI_COLOR, "\\e");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.TAB, "\\t");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.BELL, "\\a");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.BACKSPACE, "\\b");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.TAB, "\\t");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.TAB, "\\t");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.TEXT, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.CR, "\\r");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.TEXT, "sddfdsffffffffff");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.UNICODE_ESCAPE, "\\uuuuAbcD");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.OCTAL_ESCAPE, "\\377");
        LexerTestUtilities.assertNextTokenEquals(ts, CppStringTokenId.LAST_QUOTE, "\"");
    }

    public void testRawTokens() {
        // uR"*(This is a "raw UTF-16" string.)*)*"
        String text = "uR\"*(This is a \"raw UTF-16\" \\string.)*)*\"";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, CppStringTokenId.languageRawString());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.PREFIX_uR, "uR");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.FIRST_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.START_DELIMETER, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.START_DELIMETER_PAREN, "(");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, "This is a ");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.DOUBLE_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, "raw UTF-16");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.DOUBLE_QUOTE, "\"");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, " \\string.");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.TEXT, ")*");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.END_DELIMETER_PAREN, ")");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.END_DELIMETER, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, org.netbeans.cnd.api.lexer.CppStringTokenId.LAST_QUOTE, "\"");

        assertFalse("No more tokens", ts.moveNext());
//        CndLexerUnitTest.dumpTokens(ts, "ts");
    }
}
