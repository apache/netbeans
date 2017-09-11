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

package org.netbeans.lib.lexer.test.inc;

import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.lang.TestTokenId;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class TokenHierarchySnapshotTest extends TestCase {
    
    public TokenHierarchySnapshotTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testSnapshot() throws Exception {
        if (true)
            return; // Snapshots will no longer function after #87014
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        assertNotNull("Null token hierarchy for document", hi);
        TokenSequence<?> ts = hi.tokenSequence();
        assertFalse(ts.moveNext());
        
        // Insert text into document
        String text = "abc+def-xyz";
        doc.insertString(0, text, null);

        ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "abc", 0);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", 3);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "def", 4);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.MINUS, "-", 7);
        assertTrue(ts.moveNext());
        LexerTestUtilities.assertTokenEquals(ts,TestTokenId.IDENTIFIER, "xyz", 8);
        assertFalse(ts.moveNext());
        LexerTestUtilities.incCheck(doc, false);

        // Create snapshot1 and check hierarchy
        String hi1text = doc.getText(0, doc.getLength());
        TokenHierarchy<?> hi1 = TokenHierarchy.create(hi1text,TestTokenId.language());
//        TokenHierarchy<?> snapshot1 = hi.createSnapshot();
//        assertEquals(snapshot1.snapshotOf(), hi);
//        assertFalse(snapshot1.isSnapshotReleased());
//
//        // Check that all the non-fly tokens are mutable
//        ts = snapshot1.tokenSequence();
//        assertEquals(0, ts.moveIndex(0));
//        assertTrue(ts.moveNext());
//        
//        LexerTestUtilities.assertTokenSequencesEqual(hi1.tokenSequence(), hi1,
//                snapshot1.tokenSequence(), snapshot1, false);
//
//        doc.insertString(4, "+", null);
//
//        // Check that the snapshot token sequence can be further operated.
//        assertEquals(0, ts.moveIndex(0));
//        assertTrue(ts.moveNext());
//        assertNotNull(ts.token());
//
//        // Check that the tokens except '+' are live
//        ts = snapshot1.tokenSequence();
//
//        LexerTestUtilities.assertTokenSequencesEqual(hi1.tokenSequence(), hi1,
//                snapshot1.tokenSequence(), snapshot1, true);
//
//        // Create snapshot2 and check hierarchy
//        String hi2text = doc.getText(0, doc.getLength());
//        TokenHierarchy<?> hi2 = TokenHierarchy.create(hi2text,TestTokenId.language());
//        TokenHierarchy<?> snapshot2 = hi.createSnapshot();
//        assertEquals(snapshot2.snapshotOf(), hi);
//
//        // Check that all the non-fly tokens are mutable
//        ts = snapshot2.tokenSequence();
//        assertEquals(0, ts.moveIndex(0));
//        assertTrue(ts.moveNext());
//
//        LexerTestUtilities.assertTokenSequencesEqual(hi2.tokenSequence(), hi2,
//                snapshot2.tokenSequence(), snapshot2, false);
//
//        doc.remove(8, 1);
//
//        LexerTestUtilities.assertTokenSequencesEqual(hi1.tokenSequence(), hi1,
//                snapshot1.tokenSequence(), snapshot1, false);
//        LexerTestUtilities.assertTokenSequencesEqual(hi2.tokenSequence(), hi2,
//                snapshot2.tokenSequence(), snapshot2, false);
    }
    
}
