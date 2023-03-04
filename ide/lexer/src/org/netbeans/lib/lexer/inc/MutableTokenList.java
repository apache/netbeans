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
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.TokenOrEmbedding;

/**
 * Token list that allows mutating by token list mutator.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface MutableTokenList<T extends TokenId> extends TokenList<T> {

    /**
     * Return token or branch token list at the requested index
     * but do not lazily initialize tokens if they are missing at the given index.
     * Also do not perform any checks regarding index validity
     * - only items below {@link #tokenCountCurrent()} will be requested.
     */
    TokenOrEmbedding<T> tokenOrEmbeddingDirect(int index);

    /**
     * Create lexer input operation used for relexing of the input.
     */
    LexerInputOperation<T> createLexerInputOperation(
    int tokenIndex, int relexOffset, Object relexState);
    
    /**
     * Check whether the whole input was tokenized or not.
     * <br/>
     * Incremental algorithm uses this information to determine
     * whether it should relex the input till the end or not.
     */
    boolean isFullyLexed();
    
    /**
     * Update the token list by replacing tokens according to the given change.
     */
    void replaceTokens(TokenListChange<T> change, TokenHierarchyEventInfo eventInfo, boolean modInside);

}
