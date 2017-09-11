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

package org.netbeans.lib.lexer;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.TextToken;


/**
 * Token list used for immutable inputs.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class BatchTokenList<T extends TokenId>
extends ArrayList<TokenOrEmbedding<T>> implements TokenList<T> {
    
    private static boolean maintainLAState;
    
    /**
     * Check whether lookaheads and states are stored for testing purposes.
     */
    public static boolean isMaintainLAState() {
        return maintainLAState;
    }
    
    public static void setMaintainLAState(boolean maintainLAState) {
        BatchTokenList.maintainLAState = maintainLAState;
    }
    
    private final TokenHierarchyOperation<?,T> tokenHierarchyOperation;
    
    private final CharSequence inputSourceText;
    
    private final Language<T> language;

    private final LanguagePath languagePath;
    
    private final Set<T> skipTokenIds;
    
    private final InputAttributes inputAttributes;
    
    /**
     * Lexer input used for lexing of the input.
     */
    private LexerInputOperation<T> lexerInputOperation;

    private LAState laState;
    
    
    public BatchTokenList(TokenHierarchyOperation<?,T> tokenHierarchyOperation, CharSequence inputText,
    Language<T> language, Set<T> skipTokenIds, InputAttributes inputAttributes) {
        this.tokenHierarchyOperation = tokenHierarchyOperation;
        this.inputSourceText = inputText;
        this.language = language;
        this.languagePath = LanguagePath.get(language);
        this.skipTokenIds = skipTokenIds;
        this.inputAttributes = inputAttributes;
        if (TokenList.LOG.isLoggable(Level.FINE)) { // Maintain lookaheads and states when in test environment
            laState = LAState.empty();
        }
        this.lexerInputOperation = createLexerInputOperation();
    }

    protected LexerInputOperation<T> createLexerInputOperation() {
        return new TextLexerInputOperation<T>(this);
    }

    @Override
    public TokenList<?> rootTokenList() {
        return this; // this list should always be the root list of the token hierarchy
    }
    
    @Override
    public CharSequence inputSourceText() {
        return inputSourceText;
    }

    @Override
    public TokenHierarchyOperation<?,?> tokenHierarchyOperation() {
        return tokenHierarchyOperation;
    }

    @Override
    public Language<T> language() {
        return language;
    }
    
    @Override
    public LanguagePath languagePath() {
        return languagePath;
    }
    
    @Override
    public int tokenCount() {
        if (lexerInputOperation != null) { // still lexing
            tokenOrEmbeddingImpl(Integer.MAX_VALUE);
        }
        return size();
    }
    
    @Override
    public int tokenCountCurrent() {
        return size();
    }

    @Override
    public int tokenOffset(AbstractToken<T> token) {
        int rawOffset = token.rawOffset();
        // Children offsets should be absolute
        return rawOffset;
    }

    @Override
    public int tokenOffset(int index) {
        AbstractToken<T> token = existingToken(index);
        int offset;
        if (token.isFlyweight()) {
            offset = 0;
            while (--index >= 0) {
                token = existingToken(index);
                offset += token.length();
                if (!token.isFlyweight()) {
                    offset += token.offset(null);
                    break;
                }
            }
        } else { // non-flyweight offset
            offset = token.offset(null);
        }
        return offset;
    }

    @Override
    public int[] tokenIndex(int offset) {
        return LexerUtilsConstants.tokenIndexLazyTokenCreation(this, offset);
    }

    @Override
    public TokenOrEmbedding<T> tokenOrEmbedding(int index) {
        return tokenOrEmbeddingImpl(index);
    }
    
    private TokenOrEmbedding<T> tokenOrEmbeddingImpl(int index) {
        while (lexerInputOperation != null && index >= size()) {
            AbstractToken<T> token = lexerInputOperation.nextToken();
            if (token != null) { // lexer returned valid token
                if (!token.isFlyweight())
                    token.setTokenList(this);
                add(token);
                if (laState != null) { // maintaining lookaheads and states
                    laState = laState.add(lexerInputOperation.lookahead(),
                            lexerInputOperation.lexerState());
                }
            } else { // no more tokens from lexer
                lexerInputOperation.release();
                lexerInputOperation = null;
                trimToSize();
            }
        }
        return (index < size()) ? get(index) : null;
    }
    
    private AbstractToken<T> existingToken(int index) {
        return get(index).token();
    }

    @Override
    public int lookahead(int index) {
        return (laState != null) ? laState.lookahead(index) : -1;
    }

    @Override
    public Object state(int index) {
        return (laState != null) ? laState.state(index) : null;
    }

    @Override
    public int startOffset() {
        return 0;
    }

    @Override
    public int endOffset() {
        int cntM1 = tokenCount() - 1;
        if (cntM1 >= 0)
            return tokenOffset(cntM1) + tokenOrEmbeddingImpl(cntM1).token().length();
        return 0;
    }

    @Override
    public boolean isRemoved() {
        return false;
    }

    @Override
    public int modCount() {
        return LexerUtilsConstants.MOD_COUNT_IMMUTABLE_INPUT; // immutable input
    }
    
    @Override
    public AbstractToken<T> replaceFlyToken(
    int index, AbstractToken<T> flyToken, int offset) {
        TextToken<T> nonFlyToken = ((TextToken<T>)flyToken).createCopy(this, offset);
        set(index, nonFlyToken);
        return nonFlyToken;
    }

    @Override
    public void setTokenOrEmbedding(int index, TokenOrEmbedding<T> t) {
        set(index, t);
    }

    @Override
    public InputAttributes inputAttributes() {
        return inputAttributes;
    }
    
    @Override
    public boolean isContinuous() {
        return (skipTokenIds == null);
    }
    
    @Override
    public Set<T> skipTokenIds() {
        return skipTokenIds;
    }

    @Override
    public StringBuilder dumpInfo(StringBuilder sb) {
        return sb;
    }

    @Override
    public String dumpInfoType() {
        return "BTL";
    }

}
