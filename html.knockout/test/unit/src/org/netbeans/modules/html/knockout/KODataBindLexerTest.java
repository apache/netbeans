/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
