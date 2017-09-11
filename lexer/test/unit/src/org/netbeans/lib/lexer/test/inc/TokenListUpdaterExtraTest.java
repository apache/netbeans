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

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.lexer.lang.TestSaveTokensInLATokenId;
import org.netbeans.lib.lexer.lang.TestTokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 * Tests of token list behavior in certain special situations.
 *
 * @author mmetelka
 */
public class TokenListUpdaterExtraTest extends TestCase {
    
    public TokenListUpdaterExtraTest(String testName) {
        super(testName);
    }

    public void testSaveTokensWithinLookahead() throws Exception {
        Document doc = new ModificationTextDocument();
        String text = "aabc";
        doc.insertString(0, text, null);
        
        doc.putProperty(Language.class, TestSaveTokensInLATokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            ts.moveEnd(); // Force creation of all tokens
            LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
            
        doc.remove(1, 1);

        ((AbstractDocument)doc).readLock();
        try {
            TokenHierarchyEvent evt = LexerTestUtilities.getLastTokenHierarchyEvent(doc);
            TokenChange<?> change = evt.tokenChange();
            assertEquals(1, change.addedTokenCount());
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
    }

    public void testEmbeddedModInStartSkipLength() throws Exception {
        Document doc = commentDoc();
        doc.insertString(1, "*", null);
    }
    
    public void testEmbeddedModInEndSkipLength()  throws Exception {
        Document doc = commentDoc();
        doc.insertString(7, "*", null);
    }
    
    private Document commentDoc() throws BadLocationException {
        Document doc = new ModificationTextDocument();
        // Assign a language to the document
        String text = "/**abc*/";
        doc.insertString(0, text, null);
        
        doc.putProperty(Language.class,TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            assertNotNull(ts.embedded());
            return doc;
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }

    public void testTokenCountWasCalledInUpdater() throws Exception {
        Document doc = new ModificationTextDocument();
        String text = "+/* */";
        doc.insertString(0, text, null);
        
        doc.putProperty(Language.class, TestTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts;
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.PLUS, "+", -1);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }

        doc.remove(1, 3); // Remove "/* "
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            ts.moveEnd();
            // Extra ending '\n' of the document returned by DocumentUtilities.getText(doc) and lexed
            assertTrue(ts.movePrevious());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.WHITESPACE, "\n", -1);
            assertTrue(ts.movePrevious());
            LexerTestUtilities.assertTokenEquals(ts,TestTokenId.DIV, "/", -1);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
    }


}
