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
package org.netbeans.api.html.lexer;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.html.lexer.HtmlLexer;
import org.netbeans.lib.html.lexer.HtmlLexerTest;
import org.netbeans.lib.html.lexer.HtmlPlugins;

/**
 *
 * @author marekfukala
 */
public class HtmlLexerPluginTest extends NbTestCase {

    public HtmlLexerPluginTest(String name) {
        super(name);
    }

    public void testResolverRegistered() {
        HtmlPlugins exprs = HtmlPlugins.getDefault();
        assertNotNull(exprs);
        
        String[] closeDelimiters = exprs.getCloseDelimiters();
        assertNotNull(closeDelimiters);
        assertEquals(1, closeDelimiters.length);
        assertEquals("}}", closeDelimiters[0]);
        
        String[] openDelimiters = exprs.getOpenDelimiters();
        assertNotNull(openDelimiters);
        assertEquals("{{", openDelimiters[0]);
        
        String[] mimes = exprs.getMimeTypes();
        assertNotNull(mimes);
        assertEquals("text/javascript", mimes[0]);
        
    }
    
    public void testELInText() {
        HtmlLexerTest.checkTokens("A{{X}}B",
                "A|TEXT", "{{|EL_OPEN_DELIMITER", "X|EL_CONTENT", "}}|EL_CLOSE_DELIMITER", "B|TEXT");
        
        HtmlLexerTest.checkTokens("A{{X{Y}}B",
                "A|TEXT", "{{|EL_OPEN_DELIMITER", "X{Y|EL_CONTENT", "}}|EL_CLOSE_DELIMITER", "B|TEXT");
        
        HtmlLexerTest.checkTokens("A{{X}Y}}B",
                "A|TEXT", "{{|EL_OPEN_DELIMITER", "X}Y|EL_CONTENT", "}}|EL_CLOSE_DELIMITER", "B|TEXT");
        
    }

    public void testELInTags() {
        HtmlLexerTest.checkTokens("<div>{{X}}</div>",
               "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", ">|TAG_CLOSE_SYMBOL", "{{|EL_OPEN_DELIMITER", 
               "X|EL_CONTENT", "}}|EL_CLOSE_DELIMITER", "</|TAG_OPEN_SYMBOL", "div|TAG_CLOSE", ">|TAG_CLOSE_SYMBOL");
    }
    
    public void testELInClassAttrValue() {
        HtmlLexerTest.checkTokens("<div class=\"{{expr}}\">",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "class|ARGUMENT", "=|OPERATOR", 
                "\"|VALUE_CSS", "{{|EL_OPEN_DELIMITER", "expr|EL_CONTENT", "}}|EL_CLOSE_DELIMITER", "\"|VALUE_CSS", ">|TAG_CLOSE_SYMBOL");
        
        HtmlLexerTest.checkTokens("<div class=\"pre{{expr}}post\">",
                 "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "class|ARGUMENT", 
                 "=|OPERATOR", "\"pre|VALUE_CSS", "{{|EL_OPEN_DELIMITER", "expr|EL_CONTENT", 
                 "}}|EL_CLOSE_DELIMITER", "post\"|VALUE_CSS", ">|TAG_CLOSE_SYMBOL");
        
        HtmlLexerTest.checkTokens("<div class=\"pre{{expr}}post{{next}}\">",
                 "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "class|ARGUMENT", 
                 "=|OPERATOR", "\"pre|VALUE_CSS", "{{|EL_OPEN_DELIMITER", "expr|EL_CONTENT", 
                 "}}|EL_CLOSE_DELIMITER", "post|VALUE_CSS", "{{|EL_OPEN_DELIMITER", 
                 "next|EL_CONTENT", "}}|EL_CLOSE_DELIMITER", "\"|VALUE_CSS", ">|TAG_CLOSE_SYMBOL");
    }
    
    public void testELInAttrValue() {
        //check in non-class attribute which is special
        HtmlLexerTest.checkTokens("<div xxx=\"pre{{expr}}post{{next}}\">",
                 "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "xxx|ARGUMENT", 
                 "=|OPERATOR", "\"pre|VALUE", "{{|EL_OPEN_DELIMITER", "expr|EL_CONTENT", 
                 "}}|EL_CLOSE_DELIMITER", "post|VALUE", "{{|EL_OPEN_DELIMITER", 
                 "next|EL_CONTENT", "}}|EL_CLOSE_DELIMITER", "\"|VALUE", ">|TAG_CLOSE_SYMBOL");
        
    }
    
    public void testELInJSAttrValue() {
        //check in non-class attribute which is special
        HtmlLexerTest.checkTokens("<div onclick=\"pre{{expr}}post{{next}}\">",
                "<|TAG_OPEN_SYMBOL", "div|TAG_OPEN", " |WS", "onclick|ARGUMENT", 
                "=|OPERATOR", "\"pre|VALUE_JAVASCRIPT", "{{|EL_OPEN_DELIMITER", 
                "expr|EL_CONTENT", "}}|EL_CLOSE_DELIMITER", "post|VALUE_JAVASCRIPT", 
                "{{|EL_OPEN_DELIMITER", "next|EL_CONTENT", "}}|EL_CLOSE_DELIMITER", 
                "\"|VALUE_JAVASCRIPT", ">|TAG_CLOSE_SYMBOL");
        
    }
    
    public void testInjectCustomEmbeddingIntoAttribute() {
        TokenHierarchy th = TokenHierarchy.create("<div ng-click=\"alert()\">click me!</div>", HTMLTokenId.language());
        TokenSequence ts = th.tokenSequence();
        ts.moveStart();

        assertTrue(ts.moveNext());
        assertEquals("<", ts.token().text().toString());
        assertEquals(HTMLTokenId.TAG_OPEN_SYMBOL, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals("div", ts.token().text().toString());
        assertEquals(HTMLTokenId.TAG_OPEN, ts.token().id());

        assertTrue(ts.moveNext());
        assertEquals(" ", ts.token().text().toString());
        assertEquals(HTMLTokenId.WS, ts.token().id());
        
        assertTrue(ts.moveNext());
        assertEquals("ng-click", ts.token().text().toString());
        assertEquals(HTMLTokenId.ARGUMENT, ts.token().id());
        
        assertTrue(ts.moveNext());
        assertEquals("=", ts.token().text().toString());
        assertEquals(HTMLTokenId.OPERATOR, ts.token().id());

        //now check the value token
        assertTrue(ts.moveNext());
        Token<HTMLTokenId> token = ts.token();
        assertEquals("\"alert()\"", token.text().toString());
        assertEquals(HTMLTokenId.VALUE, token.id());
        
        String mimetype = (String)token.getProperty(HtmlLexer.ATTRIBUTE_VALUE_EMBEDDING_MIMETYPE_TOKEN_PROPERTY_KEY);
        assertNotNull(mimetype);
        assertEquals("text/javascript", mimetype);
        
//        //check if the javascript embedding was really created
//        TokenSequence embedded = ts.embedded(JsTokenId.javascriptLanguage());
//        assertNotNull(embedded);
//        assertEquals("text/javascript", embedded.language().mimeType());
//        
    }

    @MimeRegistration(mimeType = "text/html", service = HtmlLexerPlugin.class)
    public static class TestPlugin extends HtmlLexerPlugin {

        @Override
        public String getOpenDelimiter() {
            return "{{";
        }

        @Override
        public String getCloseDelimiter() {
            return "}}";
        }

        @Override
        public String getContentMimeType() {
            return "text/javascript";
        }

        @Override
        public String createAttributeEmbedding(String elementName, String attributeName) {
            assertNotNull(elementName);
            assertNotNull(attributeName);
            
            if(attributeName.equals("ng-click")) {
                return "text/javascript";
            }
            
            return null;
        }
    }
}