/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.lexer.inc;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.lexer.LAState;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.TokenOrEmbedding;

/**
 * Description of the change in a token list.
 * <br/>
 * The change is expressed as a list of removed tokens
 * plus the current list and index and number of the tokens
 * added to the current list.
 * <br/>
 * Some of the information that needs to be exported into TokenChange
 * is synced in a tokenChangeInfo that this class manages.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class TokenListChange<T extends TokenId> {
    
    private static final TokenOrEmbedding<?>[] EMPTY_TOKENS = {};
    
    public static <T extends TokenId> TokenListChange<T> createEmptyChange(MutableTokenList<T> tokenList) {
        TokenListChange<T> change = new TokenListChange<T>(tokenList);
        // Leave matchIndex at 0 (no replaced tokens)
        change.setRemovedTokensEmpty();
        return change;
    }

    public static <T extends TokenId> TokenListChange<T> createRebuildChange(MutableTokenList<T> tokenList) {
        TokenListChange<T> change = new TokenListChange<T>(tokenList);
        // Signal all tokens removed => IncTokenList.replaceTokens() will physically remove them
        change.matchIndex = tokenList.tokenCountCurrent();
        return change;
    }

    private final TokenChangeInfo<T> tokenChangeInfo; // 12 bytes (8-super + 4)
    
    /**
     * The list may store either tokens or branches as well.
     */
    private List<TokenOrEmbedding<T>> addedTokenOrEmbeddings; // 16 bytes

    private LAState laState; // 20 bytes
    
    int removedEndOffset; // 24 bytes
    
    protected int matchIndex; // 28 bytes
    
    protected int matchOffset; // 32 bytes
    
    protected int addedEndOffset; // 36 bytes

    boolean parentChangeIsBoundsChange; // 40 bytes
    
    public TokenListChange(MutableTokenList<T> tokenList) {
        tokenChangeInfo = new TokenChangeInfo<T>(tokenList);
    }

    public void setParentChangeIsBoundsChange(boolean parentChangeIsBoundsChange) {
        this.parentChangeIsBoundsChange = parentChangeIsBoundsChange;
    }

    public boolean parentChangeIsBoundsChange() {
        return parentChangeIsBoundsChange;
    }

    public TokenChangeInfo<T> tokenChangeInfo() {
        return tokenChangeInfo;
    }
    
    public MutableTokenList<T> tokenList() {
        return (MutableTokenList<T>)tokenChangeInfo.currentTokenList();
    }
    
    public void setMatchIndex(int matchIndex) {
        this.matchIndex = matchIndex;
    }

    public void setMatchOffset(int matchOffset) {
        this.matchOffset = matchOffset;
    }

    public int increaseMatchIndex() {
        matchOffset += tokenList().tokenOrEmbeddingDirect(matchIndex++).token().length();
        return matchOffset;
    }

    public LanguagePath languagePath() {
        return tokenList().languagePath();
    }
    
    public int index() {
        return tokenChangeInfo.index();
    }

    public void setIndex(int index) {
        tokenChangeInfo.setIndex(index);
    }
    
    public int offset() {
        return tokenChangeInfo.offset();
    }

    public void setOffset(int offset) {
        tokenChangeInfo.setOffset(offset);
        addedEndOffset = offset;
    }

    public int removedTokenCount() {
        return matchIndex - index();
    }

    public int removedEndOffset() {
        return matchOffset; // In after-mod coordinates
    }

    public int addedEndOffset() {
        return addedEndOffset;
    }
    
    public void setAddedEndOffset(int addedEndOffset) {
        this.addedEndOffset = addedEndOffset;
    }

    public void addToken(AbstractToken<T> token, int lookahead, Object state) {
        if (addedTokenOrEmbeddings == null) {
            addedTokenOrEmbeddings = new ArrayList<TokenOrEmbedding<T>>(2);
            laState = LAState.empty();
        }
        addedTokenOrEmbeddings.add(token);
        laState = laState.add(lookahead, state);
        addedEndOffset += token.length();
    }
    
    public List<TokenOrEmbedding<T>> addedTokenOrEmbeddings() {
        return addedTokenOrEmbeddings;
    }
    
    public int addedTokenOrEmbeddingsCount() {
        return (addedTokenOrEmbeddings != null) ? addedTokenOrEmbeddings.size() : 0;
    }
    
    public AbstractToken<T> removeLastAddedToken() {
        int lastIndex = addedTokenOrEmbeddings.size() - 1;
        AbstractToken<T> token = addedTokenOrEmbeddings.remove(lastIndex).token();
        laState.remove(lastIndex, 1);
        matchIndex--;
        int tokenLength = token.length();
        matchOffset -= tokenLength;
        addedEndOffset -= tokenLength;
        return token;
    }
    
    public AbstractToken<T> addedToken(int index) {
        return addedTokenOrEmbeddings.get(0).token();
    }
    
    public void syncAddedTokenCount() {
        tokenChangeInfo.setAddedTokenCount(addedTokenOrEmbeddings.size());
    }

    public void setRemovedTokens(TokenOrEmbedding<T>[] removedTokensOrBranches) {
        tokenChangeInfo.setRemovedTokenList(new RemovedTokenList<T>(
                tokenChangeInfo.currentTokenList().rootTokenList(),
                languagePath(), removedTokensOrBranches));
    }
    
    public void setRemovedTokensEmpty() {
        @SuppressWarnings("unchecked")
        TokenOrEmbedding<T>[] empty = (TokenOrEmbedding<T>[]) EMPTY_TOKENS;
        setRemovedTokens(empty);
    }

    public boolean isBoundsChange() {
        return tokenChangeInfo.isBoundsChange();
    }
    
    public void markBoundsChange() {
        tokenChangeInfo.markBoundsChange();
    }
    
    public LAState laState() {
        return laState;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('"').append(languagePath().mimePath());
        sb.append("\", ind=").append(index());
        sb.append(", off=").append(offset());
        sb.append(", maInd=").append(matchIndex);
        sb.append(", maOff=").append(matchOffset);
        sb.append(", Add:").append(addedTokenOrEmbeddingsCount());
        sb.append(", tCnt=").append(tokenList().tokenCountCurrent());
        if (isBoundsChange()) {
            sb.append(", BoChan");
        }
        return sb.toString();
    }
    
    public String toStringMods(int indent) {
        StringBuilder sb = new StringBuilder();
        TokenList<T> removedTL = tokenChangeInfo.removedTokenList();
        if (removedTL != null && removedTL.tokenCount() > 0) {
            int digitCount = ArrayUtilities.digitCount(removedTL.tokenCount() - 1);
            for (int i = 0; i < removedTL.tokenCount(); i++) {
                sb.append('\n');
                ArrayUtilities.appendSpaces(sb, indent);
                sb.append("Rem[");
                ArrayUtilities.appendIndex(sb, i, digitCount);
                sb.append("]: ");
                LexerUtilsConstants.appendTokenInfo(sb, removedTL, i, null, false, 0, true);
            }
        }
        if (addedTokenOrEmbeddings() != null) {
            int digitCount = ArrayUtilities.digitCount(addedTokenOrEmbeddings().size() - 1);
            for (int i = 0; i < addedTokenOrEmbeddings().size(); i++) {
                sb.append('\n');
                ArrayUtilities.appendSpaces(sb, indent);
                sb.append("Add[");
                ArrayUtilities.appendIndex(sb, i, digitCount);
                sb.append("]: ");
                LexerUtilsConstants.appendTokenInfo(sb, addedTokenOrEmbeddings.get(i),
                        laState.lookahead(i), laState.state(i), null, false, 0, true);
            }
        }
        return sb.toString();
    }
}
