/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.css.lib.api.properties;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author marekfukala
 */
public class TokenAcceptorsTest extends NbTestCase {
    
    public TokenAcceptorsTest(String name) {
        super(name);
    }
    
    public void testBasic() {
        TokenAcceptor ta = TokenAcceptor.getAcceptor("length");
        assertNotNull(ta);
    }

    public void testAccepts() {
        TokenAcceptor ta = TokenAcceptor.getAcceptor("identifier");
        assertTrue(ta.accepts(getToken("hello")));
        assertTrue(ta.accepts(getToken("_hello")));
        assertTrue(ta.accepts(getToken("hel_lo")));
        assertTrue(ta.accepts(getToken("-hello")));
        assertTrue(ta.accepts(getToken("hel-lo")));
        assertTrue(ta.accepts(getToken("hello23")));
        assertTrue(ta.accepts(getToken("\u0080hello")));
        assertTrue(ta.accepts(getToken("hel\u0090o")));
        assertTrue(ta.accepts(getToken("hel\\uffbbo")));
        assertTrue(ta.accepts(getToken("hel\\no")));
        
        assertFalse(ta.accepts(getToken("0hello")));
        
    }
    
    public void testLengthAcceptor() {
        TokenAcceptor ta = TokenAcceptor.getAcceptor(TokenAcceptor.Length.class);
        assertTrue(ta.accepts(getToken("1rem")));
        assertTrue(ta.accepts(getToken("2em")));
        assertTrue(ta.accepts(getToken("8vmin")));
        assertTrue(ta.accepts(getToken("1cqw")));
        assertTrue(ta.accepts(getToken("2cqh")));
        assertTrue(ta.accepts(getToken("3cqi")));
        assertTrue(ta.accepts(getToken("4cqb")));
        assertTrue(ta.accepts(getToken("5cqmin")));
        assertTrue(ta.accepts(getToken("6cqmax")));
    }
    
    private Token getToken(String tokenImg) {
        Tokenizer t = new Tokenizer(tokenImg);
        return t.token();
    }
    
}
