/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
