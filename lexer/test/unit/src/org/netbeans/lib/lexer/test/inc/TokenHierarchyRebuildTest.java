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

import java.util.ConcurrentModificationException;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.lang.TestTokenId;
import org.netbeans.spi.lexer.MutableTextInput;

/**
 *
 * @author Miloslav Metelka
 */
public class TokenHierarchyRebuildTest extends NbTestCase {
    
    /** Creates a new instance of DocumentUpdateTest */
    public TokenHierarchyRebuildTest(String name) {
        super(name);
    }
    
    public void testRebuild() throws Exception {
        final Document doc = new ModificationTextDocument();
        doc.putProperty(Language.class,TestTokenId.language());
        doc.insertString(0, "abc def", null);
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<?> ts;
        Token<?> t;
        String tText;
        ((AbstractDocument)doc).readLock();
        try {
            ts = hi.tokenSequence();
            assertTrue(ts.moveNext());
            t = ts.token();
            assertNotNull(t);
            tText = t.text().toString();
            LexerTestUtilities.initLastTokenHierarchyEventListening(doc);
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        ((ModificationTextDocument)doc).runAtomic(new Runnable() {
            @Override
            public void run() {
                MutableTextInput input = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                assertNotNull(input);
                input.tokenHierarchyControl().rebuild();
                LexerTestUtilities.initLastDocumentEventListening(doc);
            }
        });
        
        // Check the fired token hierarchy event
        int docLen = doc.getLength();
        TokenHierarchyEvent evt = LexerTestUtilities.getLastTokenHierarchyEvent(doc);
        assertEquals(0, evt.affectedStartOffset());
        // Extra newline contained in DocumentUtilities.getText(doc) being relexed
        assertEquals(docLen+1, evt.affectedEndOffset());
        
        ((AbstractDocument)doc).readLock();
        try { // ts should no longer work
            ts.moveNext();
            fail("ConcurrentModificationException not thrown.");
        } catch (ConcurrentModificationException e) {
            // Expected
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
        TokenHierarchy<?> hi2 = TokenHierarchy.get(doc);
        assertSame(hi, hi2);
        ((AbstractDocument)doc).readLock();
        try {
            TokenSequence<?> ts2 = hi2.tokenSequence();
            assertTrue(ts2.moveNext());
            Token<?> t2  = ts2.token();

            assertNotSame(t, t2);
            assertSame(t.id(), t2.id());
            assertTrue(TokenUtilities.equals(tText, t2.text()));
        } finally {
            ((AbstractDocument)doc).readUnlock();
        }
        
    }
    
}
