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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.api.lexer;

import org.netbeans.lib.lexer.lang.TestTokenId;
import org.netbeans.lib.lexer.lang.TestJavadocTokenId;
import junit.framework.TestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.simple.*;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class InputAttributesTest extends TestCase {

    public InputAttributesTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    LanguagePath simpleLP = LanguagePath.get(TestTokenId.language());
    LanguagePath jdLP = LanguagePath.get(TestJavadocTokenId.language());
    LanguagePath nestedJDLP  = LanguagePath.get(simpleLP,TestJavadocTokenId.language());

    public void testGetSetValue() {
        InputAttributes attrs = new InputAttributes();
        assertNull(attrs.getValue(simpleLP, "version"));
        attrs.setValue(simpleLP, "version", Integer.valueOf(1), false);
        assertEquals(attrs.getValue(simpleLP, "version"), Integer.valueOf(1));
        
        attrs = new InputAttributes();
        attrs.setValue(simpleLP, "version", Integer.valueOf(1), true);
        assertEquals(attrs.getValue(simpleLP, "version"), Integer.valueOf(1));
    }

    public void testInheritance() {
        InputAttributes attrs = new InputAttributes();
        attrs.setValue(jdLP, "version", Integer.valueOf(1), false);
        assertNull(attrs.getValue(nestedJDLP, "version"));
        
        attrs = new InputAttributes();
        attrs.setValue(jdLP, "version", Integer.valueOf(1), true);
        assertEquals(attrs.getValue(nestedJDLP, "version"), Integer.valueOf(1));
    }

    public void testLexerInputAttributes() {
        String text = "public static private";

        // Default version recognizes "static" keyword
        TokenHierarchy<?> hi = TokenHierarchy.create(text,TestTokenId.language());
        TokenSequence<?> ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PUBLIC, "public", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 6);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.STATIC, "static", 7);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 13);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PRIVATE, "private", 14);
        assertFalse(ts.moveNext());

        // Version 1 recognizes "static" as identifier
        InputAttributes attrs = new InputAttributes();
        attrs.setValue(TestTokenId.language(), "version", Integer.valueOf(1), false);
        hi = TokenHierarchy.create(text, false,TestTokenId.language(), null, attrs);
        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PUBLIC, "public", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 6);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "static", 7);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, " ", 13);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PRIVATE, "private", 14);
        assertFalse(ts.moveNext());
    }
    
}
