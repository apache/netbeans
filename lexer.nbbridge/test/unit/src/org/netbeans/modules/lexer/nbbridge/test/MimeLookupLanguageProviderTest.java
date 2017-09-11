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

package org.netbeans.modules.lexer.nbbridge.test;

import java.util.Collection;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.lexer.nbbridge.test.simple.SimplePlainTokenId;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author vita
 */
public class MimeLookupLanguageProviderTest extends NbTestCase{
    
    /** Creates a new instance of MimeLookupLanguageProviderTest */
    public MimeLookupLanguageProviderTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        // Initialize the module system
        Collection<? extends ModuleInfo> infos = Lookup.getDefault().<ModuleInfo>lookupAll(ModuleInfo.class);
    }
    
    public void testFindLanguageForMT() {
        Document doc = new PlainDocument();
        doc.putProperty("mimeType", "text/x-simple-char");
        
        TokenHierarchy th = TokenHierarchy.get(doc);
        assertNotNull("Can't find token hierarchy for a text/x-simple-char document", th);
        
        Language lang = th.tokenSequence().language();
        assertNotNull("Can't find language for text/x-simple-char", lang);
        assertEquals("Wrong language", "text/x-simple-char", lang.mimeType());
    }

    public void testLanguagesEmbeddingMapMT() throws Exception {
        Document doc = new PlainDocument();
        doc.putProperty("mimeType", "text/x-simple-plain");
        // All words have to be longer than 3 characters
        doc.insertString(0, "Hello 1234 0xFF00", SimpleAttributeSet.EMPTY);
        
        TokenHierarchy th = TokenHierarchy.get(doc);
        assertNotNull("Can't find token hierarchy for a text/x-simple-plain document", th);
        
        TokenSequence seq = th.tokenSequence();
        Language lang = seq.language();
        assertNotNull("Can't find language for text/x-simple-plain", lang);
        assertEquals("Wrong language", "text/x-simple-plain", lang.mimeType());
        
        for(int i = 0; i < seq.tokenCount(); i++) {
            seq.moveIndex(i);
            assertTrue(seq.moveNext());
            Token token = seq.token();
            
            if (token.id() == SimplePlainTokenId.WORD) {
                TokenSequence embeddedSeq = seq.embedded();
                assertNotNull("Can't find embedded token sequence", embeddedSeq);

                Language embeddedLang = embeddedSeq.language();
                assertNotNull("Can't find language of the embedded sequence", embeddedLang);
                assertEquals("Wrong language of the embedded sequence", "text/x-simple-char", embeddedLang.mimeType());
                
                embeddedSeq.moveStart();
                assertTrue("Embedded sequence has no tokens (moveFirst)", embeddedSeq.moveNext());
                assertEquals("Wrong startSkipLength", 1, embeddedSeq.offset() - seq.offset());
                
                embeddedSeq.moveEnd();
                assertTrue("Embedded sequence has no tokens (moveLast)", embeddedSeq.movePrevious());
                assertEquals("Wrong endSkipLength", 2, 
                    (seq.offset() + seq.token().length()) - (embeddedSeq.offset() + embeddedSeq.token().length()));
            }
        }
    }
}
