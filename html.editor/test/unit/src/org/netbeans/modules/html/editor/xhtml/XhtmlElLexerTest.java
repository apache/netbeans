/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.xhtml;

import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.html.editor.test.TestBase;

/**
 *
 * @author marekfukala
 */
public class XhtmlElLexerTest extends TestBase {
    
    public XhtmlElLexerTest(String name) {
        super(name);
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new XhtmlElLanguage();
    }

    @Override
    protected String getPreferredMimeType() {
        return "text/xhtml";
    }
    
    public void testLexer() throws BadLocationException {
        BaseDocument doc = createDocument();
        doc.insertString(0, "<div> #{bean.prop} </div>", null);
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<XhtmlElTokenId> ts = th.tokenSequence(XhtmlElTokenId.language());
        
        assertToken("<div> ", XhtmlElTokenId.HTML, ts);
        assertToken("#{bean.prop}", XhtmlElTokenId.EL, ts);
        assertToken(" </div>\n", XhtmlElTokenId.HTML, ts);
        
    }
    
    public void testSingleQuotedClosingCurlyBracket() throws BadLocationException {
        BaseDocument doc = createDocument();
        doc.insertString(0, "<div> #{'text}text'} </div>", null);
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<XhtmlElTokenId> ts = th.tokenSequence(XhtmlElTokenId.language());
        
        assertToken("<div> ", XhtmlElTokenId.HTML, ts);
        assertToken("#{'text}text'}", XhtmlElTokenId.EL, ts);
        assertToken(" </div>\n", XhtmlElTokenId.HTML, ts);
        
    }
    
    public void testSingleQuotedClosingCurlyBracketWithEscape() throws BadLocationException {
        BaseDocument doc = createDocument();
        doc.insertString(0, "<div> #{'te\\'xt}text'} </div>", null);
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<XhtmlElTokenId> ts = th.tokenSequence(XhtmlElTokenId.language());
        
        assertToken("<div> ", XhtmlElTokenId.HTML, ts);
        assertToken("#{'te\\'xt}text'}", XhtmlElTokenId.EL, ts);
        assertToken(" </div>\n", XhtmlElTokenId.HTML, ts);
        
    }
    
    public void testDoubleQuotedClosingCurlyBracketWithEscape() throws BadLocationException {
        BaseDocument doc = createDocument();
        doc.insertString(0, "<div> #{\"te\\\"xt}text\"} </div>", null);
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<XhtmlElTokenId> ts = th.tokenSequence(XhtmlElTokenId.language());
        
        assertToken("<div> ", XhtmlElTokenId.HTML, ts);
        assertToken("#{\"te\\\"xt}text\"}", XhtmlElTokenId.EL, ts);
        assertToken(" </div>\n", XhtmlElTokenId.HTML, ts);
        
    }
    
    public void testDoubleQuotedClosingCurlyBracket() throws BadLocationException {
        BaseDocument doc = createDocument();
        doc.insertString(0, "<div> #{\"text}text\"} </div>", null);
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<XhtmlElTokenId> ts = th.tokenSequence(XhtmlElTokenId.language());
        
        assertToken("<div> ", XhtmlElTokenId.HTML, ts);
        assertToken("#{\"text}text\"}", XhtmlElTokenId.EL, ts);
        assertToken(" </div>\n", XhtmlElTokenId.HTML, ts);
        
    }

    public void testSetData() throws BadLocationException {
        BaseDocument doc = createDocument();
        doc.insertString(0, "<div> #{{'one', 2}} </div>", null);
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<XhtmlElTokenId> ts = th.tokenSequence(XhtmlElTokenId.language());

        assertToken("<div> ", XhtmlElTokenId.HTML, ts);
        assertToken("#{{'one', 2}}", XhtmlElTokenId.EL, ts);
        assertToken(" </div>\n", XhtmlElTokenId.HTML, ts);
    }

    public void testMapData() throws BadLocationException {
        BaseDocument doc = createDocument();
        doc.insertString(0, "<div>#{{'one' : 1, 'two' : 2}}</div>", null);
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<XhtmlElTokenId> ts = th.tokenSequence(XhtmlElTokenId.language());

        assertToken("<div>", XhtmlElTokenId.HTML, ts);
        assertToken("#{{'one' : 1, 'two' : 2}}", XhtmlElTokenId.EL, ts);
        assertToken("</div>\n", XhtmlElTokenId.HTML, ts);
    }

     private void assertToken(String expectedImage, XhtmlElTokenId expectedType, TokenSequence<XhtmlElTokenId> ts) {
        assertTrue(ts.moveNext());
        Token<XhtmlElTokenId> token = ts.token();
        assertNotNull(token);
        assertEquals(expectedImage, token.text().toString());
        assertEquals(expectedType, token.id());
    }
    
    
}
