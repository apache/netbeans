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

package org.netbeans.lib.lexer.inc;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.JoinLexerInputOperation;
import org.netbeans.lib.lexer.JoinTokenList;

/**
 * Lexer input operation over multiple joined sections (embedded token lists).
 * <br/>
 * It produces regular tokens (to be added directly into ETL represented by
 * {@link #activeTokenList()} and also special {@link #JoinToken} instances
 * in case a token spans boundaries of multiple ETLs.
 * <br/>
 * It can either work over JoinTokenList directly or, during a modification,
 * it simulates that certain token lists are already removed/added to underlying token list.
 * <br/>
 * 
 * {@link #recognizedTokenLastInTokenList()} gives information whether the lastly
 * produced token ends right at boundary of the activeTokenList.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

class MutableJoinLexerInputOperation<T extends TokenId> extends JoinLexerInputOperation<T> {
    
    private final TokenListListUpdate<T> tokenListListUpdate;

    MutableJoinLexerInputOperation(JoinTokenList<T> joinTokenList, int relexJoinIndex, Object lexerRestartState,
            int activeTokenListIndex, int relexOffset, TokenListListUpdate<T> tokenListListUpdate
    ) {
        super(joinTokenList, relexJoinIndex, lexerRestartState, activeTokenListIndex, relexOffset);
        this.tokenListListUpdate = tokenListListUpdate;
    }

    @Override
    public EmbeddedTokenList<?,T> tokenList(int tokenListIndex) {
        return tokenListListUpdate.afterUpdateTokenList((JoinTokenList<T>) tokenList, tokenListIndex);
    }

    @Override
    protected int tokenListCount() {
        return tokenListListUpdate.afterUpdateTokenListCount((JoinTokenList<T>) tokenList);
    }

    @Override
    public String toString() {
        return super.toString() + ", tokenListListUpdate: " + tokenListListUpdate; // NOI18N
    }

}
