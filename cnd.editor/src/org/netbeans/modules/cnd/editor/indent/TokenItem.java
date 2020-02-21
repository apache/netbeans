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

package org.netbeans.modules.cnd.editor.indent;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;

/**
 *
 */
public final class TokenItem {

    private final int index;
    private final TokenId tokenId;
    protected final TokenSequence<TokenId> tokenSeq;
    private final boolean skipPP;

    public TokenItem(TokenSequence<TokenId> ts, boolean skipPP) {
        index = ts.index();
        tokenId = ts.token().id();
        this.tokenSeq = ts;
        this.skipPP = skipPP;
    }

    public TokenSequence<TokenId> getTokenSequence() {
        return tokenSeq;
    }

    public boolean isSkipPP(){
        return skipPP;
    }
    
    private void go() {
        tokenSeq.moveIndex(index);
        tokenSeq.moveNext();
    }

    public TokenId getTokenID() {
        return tokenId;
    }

    public CppTokenId getTokenPPID() {
        TokenSequence<CppTokenId> prep = tokenSeq.embedded(CppTokenId.languagePreproc());
        if (prep == null){
            return CppTokenId.PREPROCESSOR_START;
        }
        prep.moveStart();
        while (prep.moveNext()) {
            if (!(prep.token().id() == CppTokenId.WHITESPACE ||
                    prep.token().id() == CppTokenId.PREPROCESSOR_START ||
                    prep.token().id() == CppTokenId.PREPROCESSOR_START_ALT)) {
                break;
            }
        }
        Token<CppTokenId> directive = null;
        if (prep.token() != null) {
            directive = prep.token();
        }
        if (directive != null) {
             switch (directive.id()) {
                case PREPROCESSOR_DIRECTIVE:
                case PREPROCESSOR_IF:
                case PREPROCESSOR_IFDEF:
                case PREPROCESSOR_IFNDEF:
                case PREPROCESSOR_ELSE:
                case PREPROCESSOR_ELIF:
                case PREPROCESSOR_ENDIF:
                case PREPROCESSOR_DEFINE:
                case PREPROCESSOR_UNDEF:
                case PREPROCESSOR_INCLUDE:
                case PREPROCESSOR_INCLUDE_NEXT:
                case PREPROCESSOR_LINE:
                case PREPROCESSOR_IDENT:
                case PREPROCESSOR_PRAGMA:
                case PREPROCESSOR_WARNING:
                case PREPROCESSOR_ERROR:
                case PREPROCESSOR_DEFINED:
                    return directive.id();
                default:
                     break;
            }
        }
        return CppTokenId.PREPROCESSOR_START;
    }

    public int index() {
        return index;
    }

    public TokenItem getNext() {
        go();
        while (tokenSeq.moveNext()) {
            if (!skipPP || tokenSeq.token().id() != CppTokenId.PREPROCESSOR_DIRECTIVE) {
                return new TokenItem(tokenSeq, skipPP);
            }
        }
        return null;
    }

    public TokenItem getPrevious() {
        go();
        while (tokenSeq.movePrevious()) {
            if (!skipPP || tokenSeq.token().id() != CppTokenId.PREPROCESSOR_DIRECTIVE) {
                return new TokenItem(tokenSeq, skipPP);
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TokenItem) {
            return ((TokenItem) obj).index == index;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + this.index;
        hash = 43 * hash + (this.tokenId != null ? this.tokenId.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return tokenId+"("+index+")"; // NOI18N
    }
}
