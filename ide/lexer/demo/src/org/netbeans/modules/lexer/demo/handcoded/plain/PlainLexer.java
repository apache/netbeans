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

package org.netbeans.modules.lexer.demo.handcoded.plain;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Lexer;
import org.netbeans.api.lexer.LexerInput;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.Token;

/**
 * Lexer that recognizes PlainLanguage.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

final class PlainLexer implements Lexer {

    private static final PlainLanguage language = PlainLanguage.get();

    private LexerInput lexerInput;

    public PlainLexer() {
    }

    public Object getState() {
        return null;
    }

    public void restart(LexerInput input, Object state) {
        this.lexerInput = input;
    }

    public Token nextToken() {
        int ch = lexerInput.read();
        while (ch != LexerInput.EOF && ch != '\n') {
            ch = lexerInput.read();
        }
        
        return (lexerInput.getReadLength() > 0) // read one or more chars
            ? lexerInput.createToken(PlainLanguage.TEXT)
            : null; // was immediate EOF on input
    }
    
}
