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

package org.netbeans.modules.groovy.editor.api.completion.util;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;

/**
 * Holder class for the context of a given completion.
 * This means the two surrounding Lexer-tokens before and after the completion point are stored here.
 *
 * @author Martin Janicek
 */
public class CompletionSurrounding {

    // b2    b1      |       a1        a2
    // class MyClass extends BaseClass {
    public Token<GroovyTokenId> beforeLiteral;
    public Token<GroovyTokenId> before2;
    public Token<GroovyTokenId> before1;
    public Token<GroovyTokenId> active;
    public Token<GroovyTokenId> after1;
    public Token<GroovyTokenId> after2;
    public Token<GroovyTokenId> afterLiteral;
    public TokenSequence<GroovyTokenId> ts; // we keep the sequence with us.

    
    public CompletionSurrounding(
        Token<GroovyTokenId> beforeLiteral,
        Token<GroovyTokenId> before2,
        Token<GroovyTokenId> before1,
        Token<GroovyTokenId> active,
        Token<GroovyTokenId> after1,
        Token<GroovyTokenId> after2,
        Token<GroovyTokenId> afterLiteral,
        TokenSequence<GroovyTokenId> ts) {

        this.beforeLiteral = beforeLiteral;
        this.before2 = before2;
        this.before1 = before1;
        this.active = active;
        this.after1 = after1;
        this.after2 = after2;
        this.afterLiteral = afterLiteral;
        this.ts = ts;
    }
}
