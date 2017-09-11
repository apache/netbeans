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

import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 *
 * @author Jan Lahoda
 */
public class JavadocLexerTest extends NbTestCase {

    public JavadocLexerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public void testNextToken1() {
        String text = "@param aaa <code>aaa</code> xyz {@link org.Aaa#aaa()}";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "aaa");
        assertTrue((Boolean) ts.token().getProperty("javadoc-identifier"));
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "<code>");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "aaa");
        assertNull((Boolean) ts.token().getProperty("javadoc-identifier"));
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "</code>");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "xyz");
        assertNull((Boolean) ts.token().getProperty("javadoc-identifier"));
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " {");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@link");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "org");
        assertNull((Boolean) ts.token().getProperty("javadoc-identifier"));
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.DOT, ".");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "Aaa");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HASH, "#");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "aaa");
        assertNull((Boolean) ts.token().getProperty("javadoc-identifier"));
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "()}");
    }

    public void testNextToken2() {
        String text = "abc @foo xyz\n@deprecated";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "abc");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " @");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "foo");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "xyz");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@deprecated");
    }

    public void testNextBrokenHTML1() {
        String text = "<code\n @param";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "<code");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "\n ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@param");
    }

    public void testNextBrokenHTML2() {
        String text = "<code\n * @param\n<code\n ** @param";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "<code");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "\n * ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@param");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "<code\n ** @param");
    }


    public void testNextBrokenHTML3() {
        String text = "<code <code\n @param";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "<code ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.HTML_TAG, "<code");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "\n ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@param");
    }

    public void test233097() {
        String text = "{@code Foo<Bar>}";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@code");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "Foo");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "Bar");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, ">");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "}");
    }

    public void test233097b() {
        String text = "{@code null}";
        
        TokenHierarchy<?> hi = TokenHierarchy.create(text, JavadocTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "{");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.TAG, "@code");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.IDENT, "null");
        LexerTestUtilities.assertNextTokenEquals(ts, JavadocTokenId.OTHER_TEXT, "}");
    }

//    public void testModification1() throws Exception {
//        PlainDocument doc = new PlainDocument();
//        doc.putProperty(Language.class, JavadocTokenId.language());
//        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
//        
//        {
//            TokenSequence<?> ts = hi.tokenSequence();
//            ts.moveStart();
//            assertFalse(ts.moveNext());
//        }
//        
//        doc.insertString(0, "@", null);
//    }
}
