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
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.ModificationTextDocument;
import org.netbeans.lib.lexer.lang.TestStringTokenId;
import org.netbeans.lib.lexer.lang.TestTokenId;

/**
 *
 * @author Jan Lahoda
 */
public class DocumentUpdateTest extends NbTestCase {
    
    /** Creates a new instance of DocumentUpdateTest */
    public DocumentUpdateTest(String name) {
        super(name);
    }
    
    public void testUpdate1() throws Exception {
        Document d = new ModificationTextDocument();
        
        d.putProperty(Language.class,TestTokenId.language());
        
        d.insertString(0, "\"\\t\\b\\t test\"", null);
        
        TokenHierarchy<?> h = TokenHierarchy.get(d);
        
        ((AbstractDocument)d).readLock();
        try {
            h.tokenSequence().tokenCount();

            TokenSequence<?> s = h.tokenSequence();

            assertTrue(s.moveNext());

            s.embedded();
        } finally {
            ((AbstractDocument)d).readUnlock();
        }

        d.insertString(5, "t", null);
    }
    
    public void testUpdate2() throws Exception {
        Document d = new ModificationTextDocument();
        
        d.putProperty(Language.class,TestTokenId.language());
        
        d.insertString(0, "\"\\t\\b\\b\\t sfdsffffffffff\"", null);
        
        TokenHierarchy<?> h = TokenHierarchy.get(d);
        
        ((AbstractDocument)d).readLock();
        try {
            h.tokenSequence().tokenCount();

            TokenSequence<?> s = h.tokenSequence();

            assertTrue(s.moveNext());

            s.embedded();
        } finally {
            ((AbstractDocument)d).readUnlock();
        }
        
        d.insertString(10, "t", null);
    }
    
    public void testUpdate3() throws Exception {
        Document d = new ModificationTextDocument();
        
        d.putProperty(Language.class,TestTokenId.language());
        
        d.insertString(0, "\"t\"", null);
        
        TokenHierarchy<?> h = TokenHierarchy.get(d);
        
        ((AbstractDocument)d).readLock();
        try {
            h.tokenSequence().tokenCount();

            TokenSequence<?> s = h.tokenSequence();

            assertTrue(s.moveNext());

            assertNotNull(s.embedded());
        } finally {
            ((AbstractDocument)d).readUnlock();
        }
        
        d.insertString(1, "\\", null);
        
        ((AbstractDocument)d).readLock();
        try {
        
            LexerTestUtilities.assertNextTokenEquals(h.tokenSequence(),TestTokenId.STRING_LITERAL, "\"\\t\"");

            TokenSequence<?> s = h.tokenSequence();

            assertTrue(s.moveNext());

            TokenSequence<?> e = s.embedded();

            assertNotNull(e);

            assertTrue(e.moveNext());

            assertEquals(e.token().id(),TestStringTokenId.TAB);
        } finally {
            ((AbstractDocument)d).readUnlock();
        }
    }
    
}
