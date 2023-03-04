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
package org.netbeans.modules.css.lib.api.properties;

import org.netbeans.modules.css.lib.api.properties.Token;
import java.util.Arrays;
import java.util.List;
import junit.framework.AssertionFailedError;
import org.netbeans.modules.css.lib.CssTestBase;

/**
 *
 * @author marekfukala
 */
public class TokenizerTest extends CssTestBase {

    public TokenizerTest(String name) {
        super(name);
    }
    
    public void testMoveNextAndPrevious() {
        Tokenizer t = new Tokenizer("a b c");
        
        assertEquals(-1, t.tokenIndex());
        assertEquals(3, t.tokensCount());
        
        assertFalse(t.movePrevious());
        
        assertTrue(t.moveNext());
        assertFalse(t.movePrevious());
        
        assertEquals(-1, t.tokenIndex());
        assertEquals("a", t.token().image().toString());

        assertTrue(t.moveNext());
        assertEquals(1, t.tokenIndex());
        assertEquals("b", t.token().image().toString());
        
        assertTrue(t.moveNext());
        assertEquals(2, t.tokenIndex());
        assertEquals("c", t.token().image().toString());
        
        assertFalse(t.moveNext());
        
        
    }
    public void testMoveToIndex() {
        Tokenizer t = new Tokenizer("a b c");

        assertEquals(-1, t.tokenIndex());
        t.move(0);
        
        assertEquals(0,t.tokenIndex());
        assertEquals("a", t.token().image().toString());
        
        t.move(2);
        assertEquals(2,t.tokenIndex());
        assertEquals("c", t.token().image().toString());
        
    }
    
    public void testEmptyInput() {
        Tokenizer t = new Tokenizer("");

        assertEquals(-1, t.tokenIndex());
        assertFalse(t.moveNext());
        assertNull(t.token());
        
        t.move(0);
        assertFalse(t.moveNext());
        
        assertNull(t.token());
    }
    
    public void testEndOfInput() {
        Tokenizer t = new Tokenizer("x");

        assertTrue(t.moveNext());
        assertNotNull(t.token());
        
        assertFalse(t.moveNext());
        assertNotNull(t.token()); //still points to the last token
        
    }

    public void testFillList() {
        Tokenizer t = new Tokenizer("bla , ble bli,blo,,blu bly,oh/eh");

        assertTokens(t,
                "bla", ",", "ble", "bli", ",", "blo", ",", ",", "blu", "bly",
                ",", "oh", "/", "eh");
    }

    public void testFillListWithQuotedValues() {
        Tokenizer t = new Tokenizer("'Times New Roman',serif");
        assertTokens(t,
                "'Times New Roman'", ",", "serif");
    }

    public void testFillListWithBraces() {
        Tokenizer t = new Tokenizer("rect(20,30,40)");
        assertTokens(t,
                "rect", "(", "20", ",", "30", ",", "40", ")");
    }

    public void testFillListWithNewLine() {
        Tokenizer t = new Tokenizer("marek jitka \n");
        assertTokens(t,
                "marek", "jitka");

    }

    public void testFillListWithURL() {
        Tokenizer t = new Tokenizer("url(http://www.redballs.com/redball.png)");
        assertTokens(t,
                "url(http://www.redballs.com/redball.png)");
    }
    
    public void testTokenizeMinusLen() {
        Tokenizer t = new Tokenizer("-60px");
        assertTokens(t,
                "-", "60px");
    }

    private void assertTokens(Tokenizer tokenizer, String... expected) {
        List<String> exp = Arrays.asList(expected);
        while (tokenizer.moveNext()) {
            Token token = tokenizer.token();
            String t = token.image().toString();
            if (exp.size() > tokenizer.tokenIndex()) {
                String et = exp.get(tokenizer.tokenIndex());
                assertEquals(et, t);
            } else {
                throw new AssertionFailedError(
                        String.format("Unexpected tokens: %s",
                        dumpList(tokenizer.tokensList().subList(tokenizer.tokenIndex(), tokenizer.tokensCount()))));
            }
        }


        if (tokenizer.tokensCount() < exp.size()) {
            throw new AssertionFailedError(
                    String.format("Missing expected tokens: %s", dumpList(exp.subList(tokenizer.tokensCount(), exp.size()))));
        }


    }
}
