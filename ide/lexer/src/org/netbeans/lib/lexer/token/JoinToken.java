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

import java.util.List;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.lexer.WrapTokenId;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Token consisting of multiple parts.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class JoinToken<T extends TokenId> extends PropertyToken<T> {

    private List<PartToken<T>> joinedParts; // 32 bytes (28-super + 4)
    
    private int completeLength; // 36 bytes
    
    /**
     * Number of ETLs spanned including empty ETLs except a first part.
     */
    private int extraTokenListSpanCount; // 40 bytes

    public JoinToken(WrapTokenId<T> wid, int length, TokenPropertyProvider<T> propertyProvider, PartType partType) {
        super(wid, length, propertyProvider, partType);
    }

    @Override
    public List<PartToken<T>> joinedParts() {
        return joinedParts;
    }
    
    public void setJoinedParts(List<PartToken<T>> joinedParts, int extraTokenListSpanCount) {
        assert (joinedParts != null) : "joinedParts expected to be non-null";
        this.joinedParts = joinedParts;
        for (PartToken partToken : joinedParts) {
            completeLength += partToken.length();
        }
        this.extraTokenListSpanCount = extraTokenListSpanCount;
    }

    public PartToken<T> lastPart() {
        return joinedParts.get(joinedParts.size() - 1);
    }

    public int extraTokenListSpanCount() {
        return extraTokenListSpanCount;
    }

    @Override
    public int offset(TokenHierarchy<?> tokenHierarchy) {
        return joinedParts.get(0).offset(tokenHierarchy);
    }
    
    public int endOffset() {
        PartToken<T> partToken = joinedParts.get(joinedParts.size() - 1);
        return partToken.offset(null) + partToken.length();
    }

    @Override
    public int length() {
        return completeLength;
    }

    @Override
    public CharSequence text() {
        return new JoinTokenText<T>(joinedParts, completeLength);
    }

    @Override
    public boolean isRemoved() {
        // Check whether last part of token is removed - this needs to be improved
        // for the case when token is just partially recreated.
        return lastPart().isRemoved();
    }

    @Override
    public StringBuilder dumpInfo(StringBuilder sb, TokenHierarchy<?> tokenHierarchy,
            boolean dumpTokenText, boolean dumpRealOffset, int indent
    ) {
        sb = super.dumpInfo(sb, tokenHierarchy, dumpTokenText, dumpRealOffset, indent);
        sb.append(", ").append(joinedParts.size()).append(" parts");
        int digitCount = String.valueOf(joinedParts.size() - 1).length();
        for (int i = 0; i < joinedParts.size(); i++) {
            sb.append('\n');
            ArrayUtilities.appendSpaces(sb, indent + 2);
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            joinedParts.get(i).dumpInfo(sb, tokenHierarchy, dumpTokenText, dumpRealOffset, indent + 4);
        }
        return sb;
    }

    public StringBuilder dumpText(StringBuilder sb, CharSequence inputSourceText) {
        for (int i = 0; i < joinedParts.size(); i++) {
            joinedParts.get(i).dumpText(sb, inputSourceText);
        }
        return sb;
    }

    @Override
    protected String dumpInfoTokenType() {
        return "JoiT"; // NOI18N
    }

}
