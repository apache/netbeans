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

import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.css.lib.api.CssTokenId;

/**
 * Splits the input source to tokens.
 * 
 * Internally it uses nb lexer.
 * 
 * XXX possibly expose the lexer's token instead of the wrappers
 *
 * @author mfukala@netbeans.org
 */
public final class Tokenizer {
    
    private List<Token> tokens;
    private int currentToken;
    
    private Tokenizer(List<Token> tokens, CharSequence input) {
        this.tokens = tokens;
    }
    
    public Tokenizer(CharSequence input) {
        this(tokenize(input), input);
        reset();
    }
    
    public List<Token> tokensList() {
        return tokens;
    }
    
    public int tokenIndex() {
        return currentToken;
    }
    
    public int tokensCount() {
        return tokens.size();
    }
    
    public void move(int tokenIndex) {
        currentToken = tokenIndex;
    }
    
    public void reset() {
        currentToken = -1;
    }
    
    public Token token() {
        if(currentToken == -1) {
            currentToken = 0; //position at the beginning
        }
        if(currentToken >= tokens.size()) {
            return null;
        }
        
        return tokens.get(currentToken);
    }
    
    public boolean moveNext() {
        if(currentToken < tokens.size() - 1) {
            currentToken++;
            return true;
        } else {
            return false;
        }
    }
    
    public boolean movePrevious() {
        if(currentToken >= 0) {
            currentToken--;
            return currentToken != -1;
        } else {
            return false;
        }
    }

    private static List<Token> tokenize(CharSequence input) {
        List<Token> stack = new LinkedList<>();
        TokenHierarchy<CharSequence> th = TokenHierarchy.create(input, CssTokenId.language());
        TokenSequence<CssTokenId> ts = th.tokenSequence(CssTokenId.language());
        ts.moveStart();
        while(ts.moveNext()) {
            org.netbeans.api.lexer.Token<CssTokenId> t = ts.token();
            switch(t.id()) {
                case WS:
                case NL:
                    continue; //ignore WS
            }
            stack.add(new Token(t.id(), ts.offset(), t.length(), input));
        }
        return stack;
    }
    
}
