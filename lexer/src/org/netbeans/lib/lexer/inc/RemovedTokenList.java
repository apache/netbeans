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

import java.util.Set;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.TextToken;
import org.netbeans.lib.lexer.TokenOrEmbedding;

/**
 * Token list implementation holding added or removed tokens from a list.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class RemovedTokenList<T extends TokenId> implements TokenList<T> {
    
    private final TokenList<?> rootTokenList;
    
    private final LanguagePath languagePath;
    
    private final TokenOrEmbedding<T>[] tokenOrEmbeddings;
    
    private int removedTokensStartOffset;
    
    public RemovedTokenList(TokenList<?> rootTokenList, LanguagePath languagePath, TokenOrEmbedding<T>[] tokensOrBranches) {
        this.rootTokenList = rootTokenList;
        this.languagePath = languagePath;
        this.tokenOrEmbeddings = tokensOrBranches;
    }

    @Override
    public Language<T> language() {
        return LexerUtilsConstants.innerLanguage(languagePath);

    }

    @Override
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    @Override
    public TokenOrEmbedding<T> tokenOrEmbedding(int index) {
        return (index < tokenOrEmbeddings.length) ? tokenOrEmbeddings[index] : null;
    }

    public TokenOrEmbedding<T>[] tokenOrEmbeddings() {
        return tokenOrEmbeddings;
    }

    @Override
    public int lookahead(int index) {
        return -1;
    }

    @Override
    public Object state(int index) {
        return null;
    }

    @Override
    public int tokenOffset(int index) {
        Token<?> token = existingToken(index);
        if (token.isFlyweight()) {
            int offset = 0;
            while (--index >= 0) {
                token = existingToken(index);
                offset += token.length();
                if (!token.isFlyweight()) {
                    // Return from here instead of break; - see code after while()
                    return offset + token.offset(null);
                }
            }
            // might remove token sequence starting with flyweight
            return removedTokensStartOffset + offset;

        } else { // non-flyweight offset
            return token.offset(null);
        }
    }

    @Override
    public int[] tokenIndex(int offset) {
        return LexerUtilsConstants.tokenIndexBinSearch(this, offset, tokenCountCurrent());
    }

    private Token<T> existingToken(int index) {
        return tokenOrEmbeddings[index].token();
    }

    @Override
    public AbstractToken<T> replaceFlyToken(
    int index, AbstractToken<T> flyToken, int offset) {
        TextToken<T> nonFlyToken = ((TextToken<T>)flyToken).createCopy(this, offset);
        tokenOrEmbeddings[index] = nonFlyToken;
        return nonFlyToken;
    }

    @Override
    public int tokenCount() {
        return tokenCountCurrent();
    }

    @Override
    public int tokenCountCurrent() {
        return tokenOrEmbeddings.length;
    }

    @Override
    public int modCount() {
        return LexerUtilsConstants.MOD_COUNT_IMMUTABLE_INPUT;
    }
    
    @Override
    public int tokenOffset(AbstractToken<T> token) {
        int rawOffset = token.rawOffset();
        // Offsets of contained tokens are absolute
        return rawOffset;
    }
    
    public char charAt(int offset) {
        throw new IllegalStateException("Querying of text for removed tokens not supported"); // NOI18N
    }

    @Override
    public void setTokenOrEmbedding(int index, TokenOrEmbedding<T> t) {
        throw new IllegalStateException("Branching of removed tokens not supported"); // NOI18N
    }
    
    @Override
    public TokenList<?> rootTokenList() {
        return rootTokenList;
    }

    @Override
    public CharSequence inputSourceText() {
        return null;
    }

    @Override
    public TokenHierarchyOperation<?,?> tokenHierarchyOperation() {
        return null;
    }
    
    @Override
    public InputAttributes inputAttributes() {
        return null;
    }

    @Override
    public int startOffset() {
        if (tokenCountCurrent() > 0 || tokenCount() > 0)
            return tokenOffset(0);
        return 0;
    }

    @Override
    public int endOffset() {
        int cntM1 = tokenCount() - 1;
        if (cntM1 >= 0)
            return tokenOffset(cntM1) + tokenOrEmbedding(cntM1).token().length();
        return 0;
    }

    @Override
    public boolean isRemoved() {
        return true; // Collects tokens removed from TH
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public Set<T> skipTokenIds() {
        return null;
    }

    @Override
    public StringBuilder dumpInfo(StringBuilder sb) {
        return sb;
    }

    @Override
    public String dumpInfoType() {
        return "RemovedTL";
    }

    @Override
    public String toString() {
        return LexerUtilsConstants.appendTokenList(null, this).toString();
    }
    
}
