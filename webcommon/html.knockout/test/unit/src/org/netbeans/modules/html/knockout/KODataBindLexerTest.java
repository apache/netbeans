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
package org.netbeans.modules.html.knockout;

import org.netbeans.modules.html.knockout.api.KODataBindTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author marekfukala
 */
public class KODataBindLexerTest extends NbTestCase {

    public KODataBindLexerTest(String testName) {
        super(testName);
    }

    public void testSimple() {
        checkTokens("key:value", "key|KEY", ":|COLON", "value|VALUE");
        checkTokens("key:  value", "key|KEY", ":|COLON", "  value|VALUE");
        checkTokens("key:  1 + 1", "key|KEY", ":|COLON", "  1 + 1|VALUE");
        checkTokens("key:  1 + 1  ", "key|KEY", ":|COLON", "  1 + 1  |VALUE");
    }
    
    public void testWSAfterKey() {
        checkTokens("key  :value", "key|KEY", "  |WS", ":|COLON", "value|VALUE");
        checkTokens("key  :   value", "key|KEY", "  |WS", ":|COLON", "   value|VALUE");
    }
    
    public void testMorePairs() {
        checkTokens("key:value,key:value2", "key|KEY", ":|COLON", "value|VALUE", ",|COMMA", "key|KEY", ":|COLON", "value2|VALUE");
        checkTokens("key:value, key:value2", "key|KEY", ":|COLON", "value|VALUE", ",|COMMA", " |WS", "key|KEY", ":|COLON", "value2|VALUE");
    }
    
    public void testErrorCases() {
        checkTokens("key:value,", "key|KEY", ":|COLON", "value|VALUE", ",|COMMA"); //no syntax error, just missing second key pair
        
        //errors:
        checkTokens(":,", ":|ERROR", ",|ERROR"); 
        checkTokens("key:,", "key|KEY", ":|COLON", ",|COMMA"); 
        
    }
    
    public void testCommaInParens() {
        checkTokens("key: function(p1, p2, p3), key:val",
                "key|KEY", ":|COLON", " function(p1, p2, p3)|VALUE", ",|COMMA", " |WS", "key|KEY", ":|COLON", "val|VALUE");
        
    }
    
    public void testCommaInCurlyBrackets() {
        checkTokens("text: name, attr: {id: linkId, href : linkTarget}",
                "text|KEY", ":|COLON", " name|VALUE", ",|COMMA", " |WS", "attr|KEY", ":|COLON", " {id: linkId, href : linkTarget}|VALUE");
    }

    public void testCommaInSquareBrackets() {
        checkTokens("foreach: [a, b, c]",
                "foreach|KEY", ":|COLON", " [a, b, c]|VALUE");
    }
    
    public void testCommaInString() {
        checkTokens("key: \"one,two\", key:val",
                "key|KEY", ":|COLON", " \"one,two\"|VALUE", ",|COMMA", " |WS", "key|KEY", ":|COLON", "val|VALUE");
        
        checkTokens("key: 'one,two', key:val",
                "key|KEY", ":|COLON", " 'one,two'|VALUE", ",|COMMA", " |WS", "key|KEY", ":|COLON", "val|VALUE");
    }

    public void testCommaInStringWithEscape() {
        checkTokens("key: \"one\\\"two\", key:val",
                "key|KEY", ":|COLON", " \"one\\\"two\"|VALUE", ",|COMMA", " |WS", "key|KEY", ":|COLON", "val|VALUE");
        
        checkTokens("key: 'one\\'two', key:val",
                "key|KEY", ":|COLON", " 'one\\'two'|VALUE", ",|COMMA", " |WS", "key|KEY", ":|COLON", "val|VALUE");
        
    }
    
    public static void checkTokens(String text, String... descriptions) {
        TokenHierarchy<String> th = TokenHierarchy.create(text, KODataBindTokenId.language());
        TokenSequence<KODataBindTokenId> ts = th.tokenSequence(KODataBindTokenId.language());
        checkTokens(ts, descriptions);
    }

    public static void checkTokens(TokenSequence<KODataBindTokenId> ts, String... descriptions) {
        ts.moveStart();
        for (String descr : descriptions) {
            //parse description
            int slashIndex = descr.indexOf('|');
            assert slashIndex >= 0;

            String image = descr.substring(0, slashIndex);
            String id = descr.substring(slashIndex + 1);

            assertTrue(ts.moveNext());
            Token<KODataBindTokenId> t = ts.token();
            assertNotNull(t);

            if (image.length() > 0) {
                assertEquals(image, t.text().toString());
            }

            if (id.length() > 0) {
                assertEquals(id, t.id().name());
            }
        }

        StringBuilder b = new StringBuilder();
        while (ts.moveNext()) {
            Token t = ts.token();
            b.append("\"");
            b.append(t.text());
            b.append('|');
            b.append(t.id().name());
            b.append("\"");
            b.append(", ");
        }
        assertTrue("There are some tokens left: " + b.toString(), b.length() == 0);
    }
}
