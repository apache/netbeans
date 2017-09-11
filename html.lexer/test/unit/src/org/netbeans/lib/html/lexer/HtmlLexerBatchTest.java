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

package org.netbeans.lib.html.lexer;

import junit.framework.TestCase;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test HTML lexer analyzis
 *
 * @author Marek Fukala
 */
public class HtmlLexerBatchTest extends TestCase {

    public HtmlLexerBatchTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testJspTags() {
        String text = "<jsp:useBean name=\"pkg.myBean\"/><!--comment-->abc&gt;def<tag attr=\"value\"></tag>";
                
        TokenHierarchy<?> hi = TokenHierarchy.create(text, HTMLTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_OPEN_SYMBOL, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_OPEN, "jsp:useBean");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.WS, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.ARGUMENT, "name");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.VALUE, "\"pkg.myBean\"");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_CLOSE_SYMBOL, "/>");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.BLOCK_COMMENT, "<!--comment-->");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TEXT, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.CHARACTER, "&gt;");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TEXT, "def");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_OPEN_SYMBOL, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_OPEN, "tag");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.WS, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.ARGUMENT, "attr");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.OPERATOR, "=");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.VALUE, "\"value\"");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_CLOSE_SYMBOL, ">");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_OPEN_SYMBOL, "</");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_CLOSE, "tag");
        LexerTestUtilities.assertNextTokenEquals(ts, HTMLTokenId.TAG_CLOSE_SYMBOL, ">");
    }
    
}
