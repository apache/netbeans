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
package org.netbeans.modules.textmate.lexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.IToken;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.core.grammar.StackElement;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.TokenFactory;

public class TextmateLexer implements Lexer<TextmateTokenId>{

    private static final Object DO_NOT_RESUME_HERE = new Object();

    private final LexerInput li;
    private final TokenFactory<TextmateTokenId> factory;
    private final IGrammar grammar;
    private int lineLen;
    private int currentOffset;
    private List<IToken> lineTokens;
    private int currentIdx;
    private StackElement state;
    private boolean forceReadLine;

    public TextmateLexer(LexerInput li, Object state, TokenFactory<TextmateTokenId> factory, IGrammar grammar) {
        this.li = li;
        this.factory = factory;
        this.grammar = grammar;
        if (state instanceof IntralineState) {
            IntralineState istate = (IntralineState) state;
            this.lineLen = istate.lineLen;
            this.currentOffset = istate.currentOffset;
            this.lineTokens = istate.lineTokens;
            this.currentIdx = istate.currentIdx;
            this.state = istate.state;
            this.forceReadLine = true;
        } else {
            this.state = (StackElement) state;
        }
    }

    @Override
    public Token<TextmateTokenId> nextToken() {
        if (currentOffset >= lineLen || forceReadLine) {
            //read next line:
            int read;
            while ((read = li.read()) != LexerInput.EOF && read != '\n');
            if (!forceReadLine) {
                if (li.readLength() != 0) {
                    lineLen = li.readText().length();
                    currentOffset = 0;
                    ITokenizeLineResult tokenized = grammar.tokenizeLine(li.readText().toString(), state);
                    lineTokens = new ArrayList<>(Arrays.asList(tokenized.getTokens()));
                    currentIdx = 0;
                    state = tokenized.getRuleStack();
                } else {
                    lineTokens = null;
                }
            }
            forceReadLine = false;
        }
        if (lineTokens != null && currentIdx < lineTokens.size()) {
            IToken current = lineTokens.get(currentIdx);
            if (currentOffset < current.getStartIndex()) {
                int len = current.getStartIndex() - currentOffset;
                currentOffset += len;
                return factory.createToken(TextmateTokenId.UNTOKENIZED, lineLen - currentOffset);
            } else if (currentOffset == current.getStartIndex()) {
                currentIdx++;
                int len = current.getEndIndex() - current.getStartIndex();
                len = Math.min(len, lineLen -  currentOffset); //XXX: untested, unclear when this happens (Rust)
                currentOffset += len;
                List<String> categories = Collections.unmodifiableList(new ArrayList<>(current.getScopes()));
                return factory.createPropertyToken(TextmateTokenId.TEXTMATE, len, (token, key) -> {
                    if ("categories".equals(key)) {
                        return categories;
                    } else {
                        return null;
                    }
                });
            }
        }
        if (currentOffset < lineLen) {
            int len = lineLen - currentOffset;
            currentOffset += len;
            return factory.createToken(TextmateTokenId.UNTOKENIZED, len);
        }
        return null;
    }
    
    @Override
    public Object state() {
        return lineLen != currentOffset ? new IntralineState(lineLen, currentOffset, lineTokens, currentIdx, state) : this.state;
    }

    @Override
    public void release() {}
    
    private static final class IntralineState {
        private int lineLen;
        private int currentOffset;
        private List<IToken> lineTokens;
        private int currentIdx;
        private StackElement state;

        public IntralineState(int lineLen, int currentOffset, List<IToken> lineTokens, int currentIdx, StackElement state) {
            this.lineLen = lineLen;
            this.currentOffset = currentOffset;
            this.lineTokens = lineTokens;
            this.currentIdx = currentIdx;
            this.state = state;
        }

    }

}
