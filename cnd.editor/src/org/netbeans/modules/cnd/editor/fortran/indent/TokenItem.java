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

package org.netbeans.modules.cnd.editor.fortran.indent;

import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import static org.netbeans.cnd.api.lexer.FortranTokenId.*;

/**
 *
 */
public final class TokenItem {

    private final int index;
    private final FortranTokenId tokenId;
    protected final TokenSequence<FortranTokenId> tokenSeq;
    private final String text;

    public TokenItem(TokenSequence<FortranTokenId> ts) {
        index = ts.index();
        tokenId = ts.token().id();
        this.tokenSeq = ts;
        text = ts.token().text().toString();
    }

    public TokenSequence<FortranTokenId> getTokenSequence() {
        return tokenSeq;
    }
    
    private void go() {
        tokenSeq.moveIndex(index);
        tokenSeq.moveNext();
    }

    public FortranTokenId getTokenID() {
        return tokenId;
    }

    public int index() {
        return index;
    }

    public String getImage() {
        return text;
    }

    public TokenItem getNext() {
        go();
        while (tokenSeq.moveNext()) {
            if (tokenSeq.token().id() != PREPROCESSOR_DIRECTIVE) {
                return new TokenItem(tokenSeq);
            }
        }
        return null;
    }

    public TokenItem getPrevious() {
        go();
        while (tokenSeq.movePrevious()) {
            if (tokenSeq.token().id() != PREPROCESSOR_DIRECTIVE) {
                return new TokenItem(tokenSeq);
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
