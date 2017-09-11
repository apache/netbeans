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

package org.netbeans.lib.lexer.test.state;

import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class StateLexerIncTest extends TestCase {
    
    public StateLexerIncTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void test() throws Exception {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        InputAttributes attrs = new InputAttributes();
        doc.putProperty(InputAttributes.class, attrs);
        doc.putProperty(Language.class, StateTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts;
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            // Extra newline at the end of document returned in DocumentUtilities.getText(doc) is lexed too
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, StateTokenId.ERROR, "\n", 0);
            assertFalse(ts.moveNext());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        // Insert text into document
        String text = "abc";
        doc.insertString(0, text, null);

        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, StateTokenId.A, "a", 0);
            assertEquals(LexerTestUtilities.lookahead(ts), 0);
            assertEquals(LexerTestUtilities.state(ts), StateLexer.AFTER_A);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, StateTokenId.BMULTI, "b", 1);
            assertEquals(LexerTestUtilities.state(ts), StateLexer.AFTER_B);
            assertEquals(LexerTestUtilities.lookahead(ts), 1);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, StateTokenId.ERROR, "c", 2);
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts, StateTokenId.ERROR, "\n", 3);
            assertEquals(LexerTestUtilities.state(ts), null);
            assertFalse(ts.moveNext());

            LexerTestUtilities.incCheck(doc, false);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        // Should relex "b" so restart state should be AFTER_A
        attrs.setValue(StateTokenId.language(), "restartState", StateLexer.AFTER_A, true);
        doc.insertString(2, "b", null);
        
    }

}
