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

package org.netbeans.api.lexer;

import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.inc.TokenChangeInfo;

/**
 * Token change describes modification on one level of a token hierarchy.
 * <br/>
 * If there is only one token that was modified
 * and there was a language embedding in that token then
 * most of the embedded tokens can usually be retained.
 * This defines an embedded change accessible by {@link #embeddedChange(int)}.
 * <br/>
 * There may possibly be multiple levels of the embedded changes.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenChange<T extends TokenId> {
    
    private final TokenChangeInfo<T> info;
    
    TokenChange(TokenChangeInfo<T> info) {
        this.info = info;
    }

    /**
     * Get number of embedded changes contained in this change.
     *
     * @return >=0 number of embedded changes.
     */
    public int embeddedChangeCount() {
        return info.embeddedChanges().length;
    }
    
    /**
     * Get embedded change at the given index.
     *
     * @param index 0 &lt;= index &lt;= embeddedChangeCount() index of the embedded change.
     * @return non-null embedded token change.
     */
    public TokenChange<?> embeddedChange(int index) {
        return info.embeddedChanges()[index];
    }

    /**
     * Get the language describing token ids
     * used by tokens contained in this token change.
     */
    public Language<T> language() {
        return LexerUtilsConstants.innerLanguage(languagePath());
    }
    
    /**
     * Get the complete language path of the tokens contained
     * in this token sequence (containing outer language levels as well).
     */
    public LanguagePath languagePath() {
        return info.currentTokenList().languagePath();
    }

    /**
     * Get index of the first token being modified.
     */
    public int index() {
        return info.index();
    }
    
    /**
     * Get offset of the first token that was modified.
     * <br/>
     * If there were any added/removed tokens then this is a start offset
     * of the first added/removed token.
     */
    public int offset() {
        return info.offset();
    }
    
    /**
     * Get number of removed tokens contained in this token change.
     */
    public int removedTokenCount() {
        TokenList<?> rtl = info.removedTokenList();
        return (rtl != null) ? rtl.tokenCount() : 0;
    }
    
    /**
     * Create token sequence over the removed tokens.
     *
     * <p>
     * There is no analogy of this method for the added tokens.
     * The {@link #currentTokenSequence()} may be used for exploration
     * of the current token sequence at this level.
     * </p>
     *
     * @return token sequence over the removed tokens
     *  or null if there were no removed tokens.
     */
    public TokenSequence<T> removedTokenSequence() {
        return new TokenSequence<T>(info.removedTokenList());
    }
 
    /**
     * Get number of the tokens added by this token change.
     */
    public int addedTokenCount() {
        return info.addedTokenCount();
    }
    
    /**
     * Get the token sequence that corresponds to the current state
     * of the token hierarchy.
     * <br/>
     * The token sequence will be positioned at the {@link #index()}.
     * <br/>
     * If this is an embedded token change then this method returns
     * the token sequence at the corresponding embedded level.
     */
    public TokenSequence<T> currentTokenSequence() {
        TokenSequence<T> ts = new TokenSequence<T>(info.currentTokenList());
        ts.moveIndex(index());
        return ts;
    }
    
    /**
     * Whether this change only modifies bounds of a single token.
     * <br/>
     * This flag is only set if there was a single token removed and a new single token
     * added with the same token id in terms of this change.
     * <br/>
     * For bounds changes the affected offsets of the event will only
     * cover the modified characters (not the modified tokens boundaries).
     */
    public boolean isBoundsChange() {
        return info.isBoundsChange();
    }
    

    /**
     * Used by package-private accessor.
     */
    TokenChangeInfo<T> info() {
        return info;
    }

    @Override
    public String toString() {
        return "index=" + index() + ", offset=" + offset() + // NOI18N
                "+T:" + addedTokenCount() + " -T:" + removedTokenCount() + // NOI18N
                " ECC:" + embeddedChangeCount() + (isBoundsChange() ? ", BC" : ""); // NOI18N
    }

}
