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

import org.netbeans.lib.lexer.TokenOrEmbedding;
import java.util.List;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.lexer.EmbeddedTokenList;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.WrapTokenId;

/**
 * Abstract token is base class of all token implementations used in the lexer module.
 * <br/>
 * Two descendants of AbstractToken:
 * <ul>
 *   <li>{@link DefaultToken} - by default does not contain a text but points
 *       into a text storage of its token list instead. It may however cache
 *       its text as string in itself.
 *       <ul>
 *           <li></li>
 *       </ul>
 *   </li>
 *   <li>{@link TextToken} - contains text that it represents; may act as flyweight token.
 *       {@link CustomTextToken} allows a token to have a custom text independent
 *       of text of an actual storage.
 *   </li>
 * 
 * 
 *
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class AbstractToken<T extends TokenId> extends Token<T>
implements TokenOrEmbedding<T> {
    
    private WrapTokenId<T> wid; // 12 bytes (8-super + 4)

    protected TokenList<T> tokenList; // 16 bytes
    
    protected int rawOffset; // 20 bytes

    /**
     * @id non-null token id.
     */
    public AbstractToken(WrapTokenId<T> wid) {
        assert (wid != null);
        this.wid = wid;
    }
    
    AbstractToken(WrapTokenId<T> wid, int rawOffset) {
        this.wid = wid;
        setRawOffset(rawOffset);
    }
    
    /**
     * Get identification of this token.
     *
     * @return non-null identification of this token.
     */
    @Override
    public final T id() {
        return wid.id();
    }

    public WrapTokenId<T> wid() {
        return wid;
    }

    public void setWid(WrapTokenId<T> wid) {
        assert (rawOffset != -1) : "Attempt to set wid=" + wid + " on flyweight token."; // NOI18N
        this.wid = wid;
    }

    /**
     * Get token list to which this token delegates its operation.
     */
    public final TokenList<T> tokenList() {
        return tokenList;
    }

    /**
     * Release this token from being attached to its parent token list.
     */
    public final void setTokenList(TokenList<T> tokenList) {
        this.tokenList = tokenList;
    }

    /**
     * Get raw offset of this token.
     * <br/>
     * Raw offset must be preprocessed before obtaining the real offset.
     */
    public final int rawOffset() {
        return rawOffset;
    }

    /**
     * Set raw offset of this token.
     *
     * @param rawOffset new raw offset.
     */
    public final void setRawOffset(int rawOffset) {
//        if (rawOffset < -1) { // -1 is default value
//            throw new IllegalArgumentException("Invalid rawOffset=" + rawOffset);
//        }
        this.rawOffset = rawOffset;
    }

    @Override
    public final boolean isFlyweight() {
        return (rawOffset == -1);
    }

    public final void makeFlyweight() {
        setRawOffset(-1);
    }
    
    @Override
    public PartType partType() {
        return PartType.COMPLETE;
    }

    @Override
    public boolean isCustomText() {
        return false;
    }

    @Override
    public int offset(TokenHierarchy<?> tokenHierarchy) {
        TokenList<T> tList = tokenList;
        if (tList != null) {
            TokenList<?> rootTokenList = tList.rootTokenList();
            synchronized (rootTokenList) {
                if (tList.getClass() == EmbeddedTokenList.class) { // Sync status first
                    ((EmbeddedTokenList)tList).updateModCount();
                }
                return tList.tokenOffset(this);
            }
        }
        return rawOffset; // Covers the case of flyweight token that will return -1
//        if (tokenHierarchy != null) {
//            return LexerApiPackageAccessor.get().tokenHierarchyOperation(
//                    tokenHierarchy).tokenOffset(this, tokenList, rawOffset);
//        } else {
//            return (tokenList != null)
//                ? tokenList.childTokenOffset(rawOffset)
//                : rawOffset;
//        }
    }

    @Override
    public boolean hasProperties() {
        return false;
    }
    
    @Override
    public Object getProperty(Object key) {
        return null;
    }

    @Override
    public Token<T> joinToken() {
        return null;
    }

    @Override
    public List<? extends Token<T>> joinedParts() {
        return null;
    }

    // Implements TokenOrEmbedding
    @Override
    public final AbstractToken<T> token() {
        return this;
    }
    
    // Implements TokenOrEmbedding
    @Override
    public final EmbeddedTokenList<T,?> embedding() {
        return null;
    }

    @Override
    public boolean isRemoved() {
        TokenList<T> tList = tokenList;
        if (tList != null) { // Flyweight tokens classify as removed
            return (tList.getClass() == EmbeddedTokenList.class) &&
                    tList.isRemoved(); // ETL's impl should allow non-synced access
        }
        return !isFlyweight();
    }
    
    public String dumpInfo() {
        return dumpInfo(null, null, true, true, 0).toString();
    }

    /**
     * Dump various information about this token
     * into a string for debugging purporses.
     * <br>
     * A regular <code>toString()</code> usually just returns
     * a text of the token to satisfy acting of the token instance
     * as <code>CharSequence</code>.
     *
     * @param tokenHierarchy <code>null</code> should be passed
     *  (the parameter is reserved for future use when token hierarchy snapshots will be implemented).
     * @param dumpText whether text should be dumped (not for TokenListUpdater
     *  when text is already shifted).
     * @param dumpRealOffset whether real offset should be dumped or whether raw offset should be used.
     * @return dump of the thorough token information. If token's text is longer
     *  than 400 characters it will be shortened.
     */
    public StringBuilder dumpInfo(StringBuilder sb, TokenHierarchy<?> tokenHierarchy,
            boolean dumpText, boolean dumpRealOffset, int indent
    ) {
        if (sb == null) {
            sb = new StringBuilder(50);
        }
        if (dumpText) {
            try {
                CharSequence text = text();
                if (text != null) {
                    sb.append('"');
                    dumpTextImpl(sb, text);
                    sb.append('"');
                } else {
                    sb.append("<null-text>"); // NOI18N
                }
            } catch (NullPointerException e) {
                sb.append("NPE in Token.text()!!!"); // NOI18N
            }
            sb.append(' ');
        }
        if (isFlyweight()) {
            sb.append("F(").append(length()).append(')');
        } else {
            int offset = dumpRealOffset ? offset(tokenHierarchy) : rawOffset();
            sb.append('<').append(offset); // NOI18N
            sb.append(",").append(offset + length()).append('>'); // NOI18N
        }
        sb.append(' ').append(wid != null ? id().name() + '[' + id().ordinal() + ']' : "<null-id>"); // NOI18N
        sb.append(" ").append(dumpInfoTokenType());
        return sb;
    }
    
    public StringBuilder dumpText(StringBuilder sb, CharSequence inputSourceText) {
        assert (tokenList == null) : "Should only be called for tokens not yet added to a token-list";
        int length = length();
        if (sb == null) {
            sb = new StringBuilder(length + 10); // some chars may be dumped as two chars
        }
        CharSequence text;
        if (isFlyweight()) {
            text = text();
        } else { // non-flyweight
            // not in token-list rawOffset is real offset
            text = inputSourceText.subSequence(rawOffset, rawOffset + length);
        }
        dumpTextImpl(sb, text);
        return sb;
    }

    private void dumpTextImpl(StringBuilder sb, CharSequence text) {
        int textLength = text.length();
        for (int i = 0; i < textLength; i++) {
            if (textLength > 400 && i >= 200 && i < textLength - 200) {
                i = textLength - 200;
                sb.append(" ...<TEXT-SHORTENED>... "); // NOI18N
                continue;
            }
            try {
                CharSequenceUtilities.debugChar(sb, text.charAt(i));
            } catch (IndexOutOfBoundsException e) {
                // For debugging purposes it's better than to completely fail
                sb.append("IOOBE at index=").append(i).append("!!!"); // NOI18N
                break;
            }
        }
    }

    protected String dumpInfoTokenType() {
        return "AbsT"; // NOI18N "AbstractToken"
    }

}
