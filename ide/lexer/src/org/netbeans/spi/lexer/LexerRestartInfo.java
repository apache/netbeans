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

package org.netbeans.spi.lexer;

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;

/**
 * Lexer restart info contains all the necessary information for restarting
 * of a lexer mainly the lexer input, state and token factory.
 * 
 * <p>
 * When lexing embedded sections if {@link LanguageEmbedding#joinSections()}
 * returns true then the {@link #state()} will return state after
 * the last token of a corresponding previous section (with the same language path).
 * </p>
 *
 * @author Miloslav Metelka
 */

public final class LexerRestartInfo<T extends TokenId> {

    private final LexerInput input;
    
    private final TokenFactory<T> tokenFactory;
    
    private final Object state;
    
    private final LanguagePath languagePath;
    
    private final InputAttributes inputAttributes;
    
    LexerRestartInfo(LexerInput input,
    TokenFactory<T> tokenFactory, Object state,
    LanguagePath languagePath, InputAttributes inputAttributes) {
        this.input = input;
        this.tokenFactory = tokenFactory;
        this.state = state;
        this.languagePath = languagePath;
        this.inputAttributes = inputAttributes;
    }
    
    /**
     * Get lexer input from which the lexer should read characters.
     */
    public LexerInput input() {
        return input;
    }

    /**
     * Get token factory through which the lexer should produce tokens.
     */
    public TokenFactory<T> tokenFactory() {
        return tokenFactory;
    }
    
    /**
     * Get state from which the lexer should start lexing.
     */
    public Object state() {
        return state;
    }
    
    /**
     * Get language path at which the lexer operates.
     */
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    /**
     * Get supplementary information about particular input source
     * or null if there are no extra attributes.
     */
    public InputAttributes inputAttributes() {
        return inputAttributes;
    }
    
    /**
     * Get value of an attribute or null if the attribute is not set
     * or if there are no attributes at all.
     */
    public Object getAttributeValue(Object key) {
        return (inputAttributes != null)
                ? inputAttributes.getValue(languagePath, key)
                : null;
    }
    
}
