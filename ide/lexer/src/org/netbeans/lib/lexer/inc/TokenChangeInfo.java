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

import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.LexerApiPackageAccessor;
import org.netbeans.lib.lexer.TokenList;

/**
 * Description of the change in a token list.
 * <br/>
 * The change is expressed as a list of removed tokens
 * plus the current list and index and number of the tokens
 * added to the current list.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenChangeInfo<T extends TokenId> {
    
    private static final TokenChange<?>[] EMPTY_EMBEDDED_CHANGES
            = (TokenChange<?>[])new TokenChange[0];

    private TokenChange<?>[] embeddedChanges = EMPTY_EMBEDDED_CHANGES;
    
    private final TokenList<T> currentTokenList;
    
    private RemovedTokenList<T> removedTokenList;
    
    private int addedTokenCount;

    private int index;

    private int offset;
    
    private boolean boundsChange;


    public TokenChangeInfo(TokenList<T> currentTokenList) {
        this.currentTokenList = currentTokenList;
    }

    public TokenChange<?>[] embeddedChanges() {
        return embeddedChanges;
    }
    
    public void addEmbeddedChange(TokenChangeInfo<?> change) {
        TokenChange<?>[] tmp = (TokenChange<?>[])
                new TokenChange[embeddedChanges.length + 1];
        System.arraycopy(embeddedChanges, 0, tmp, 0, embeddedChanges.length);
        tmp[embeddedChanges.length] = LexerApiPackageAccessor.get().createTokenChange(change);
        embeddedChanges = tmp;
    }
    
    public int index() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
    public int offset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public RemovedTokenList<T> removedTokenList() {
        return removedTokenList;
    }
    
    public void setRemovedTokenList(RemovedTokenList<T> removedTokenList) {
        this.removedTokenList = removedTokenList;
    }
    
    public int addedTokenCount() {
        return addedTokenCount;
    }

    public void setAddedTokenCount(int addedTokenCount) {
        this.addedTokenCount = addedTokenCount;
    }
    
    public void updateAddedTokenCount(int diff) {
        addedTokenCount += diff;
    }

    public TokenList<T> currentTokenList() {
        return currentTokenList;
    }
    
    public boolean isBoundsChange() {
        return boundsChange;
    }
    
    public void markBoundsChange() {
        this.boundsChange = true;
    }
    
}
