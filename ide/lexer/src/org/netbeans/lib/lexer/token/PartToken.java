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

package org.netbeans.lib.lexer.token;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.TokenOrEmbedding;
import org.netbeans.lib.lexer.WrapTokenId;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Part of a {@link JoinToken}.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class PartToken<T extends TokenId> extends PropertyToken<T> {

    private TokenOrEmbedding<T> joinTokenOrEmbedding; // 32 bytes (28-super + 4)
    
    private final int partTokenIndex; // Index of this part inside 
    
    private final int partTextOffset; // Offset of this part's text among all parts that comprise the complete token

    public PartToken(WrapTokenId<T> wid, int length, TokenPropertyProvider<T> propertyProvider, PartType partType,
            TokenOrEmbedding<T> joinToken, int partTokenIndex, int partTextOffset
    ) {
        super(wid, length, propertyProvider, partType);
        setJoinTokenOrEmbedding(joinToken);
        this.partTokenIndex = partTokenIndex;
        this.partTextOffset = partTextOffset;
    }

    @Override
    public JoinToken<T> joinToken() {
        return (JoinToken<T>)joinTokenOrEmbedding.token();
    }
    
    public boolean isLastPart() {
        return (joinToken().lastPart() == this);
    }
    
    public TokenOrEmbedding<T> joinTokenOrEmbedding() {
        return joinTokenOrEmbedding;
    }
    
    public void setJoinTokenOrEmbedding(TokenOrEmbedding<T> joinTokenOrEmbedding) {
        this.joinTokenOrEmbedding = joinTokenOrEmbedding;
    }

    public int partTokenIndex() {
        return partTokenIndex;
    }

    public int partTextOffset() {
        return partTextOffset;
    }

    public int partTextEndOffset() {
        return partTextOffset + length();
    }

    @Override
    protected String dumpInfoTokenType() {
        return "ParT[" + (partTokenIndex+1) + "/" + joinToken().joinedParts().size() + "]"; // NOI18N
    }

}
