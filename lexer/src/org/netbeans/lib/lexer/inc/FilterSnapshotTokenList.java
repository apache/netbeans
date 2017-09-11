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
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.LexerUtilsConstants;
import org.netbeans.lib.lexer.TokenHierarchyOperation;
import org.netbeans.lib.lexer.TokenList;
import org.netbeans.lib.lexer.TokenOrEmbedding;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Filtering token list for token hierarchy snapshots.
 * <br/>
 * It holds an offset diff between offset of a token related to particular snapshot
 * and a "natural" offset of a token (related to null token hierarchy).
 * <br/>
 * For non-snapshots it's always zero.
 * <br/>
 * It's used for token sequences over embedded token lists because
 * they are not SnapshotTokenList (only used for root token list) instances
 * so the embedded token lists need an extra relocation.
 *
 * <p>
 * It also ensures that the modCount will be -1 to eliminate up-to-date checking
 * for snapshot embedded branches.
 * </p>
 *
 * <p>
 * This list assumes single-threaded use only.
 * </p>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class FilterSnapshotTokenList<T extends TokenId> implements TokenList<T> {
    
    /** Original token list. */
    private TokenList<T> tokenList;
    
    /**
     * Difference of the offsets retrieved from tokenList.offset(index)
     * from the reality - there is a non-zero shift because of the snapshot use.
     */
    private int tokenOffsetDiff;
    
    public FilterSnapshotTokenList(TokenList<T> tokenList, int tokenOffsetDiff) {
        this.tokenList = tokenList;
        this.tokenOffsetDiff = tokenOffsetDiff;
    }
    
    public TokenList delegate() {
        return tokenList;
    }
    
    public int tokenOffsetDiff() {
        return tokenOffsetDiff;
    }
    
    @Override
    public TokenOrEmbedding<T> tokenOrEmbedding(int index) {
        return tokenList.tokenOrEmbedding(index);
    }

    @Override
    public AbstractToken<T> replaceFlyToken(int index, AbstractToken<T> flyToken, int offset) {
        return tokenList.replaceFlyToken(index, flyToken, offset);
    }

    @Override
    public int tokenOffset(int index) {
        return tokenOffsetDiff + tokenList.tokenOffset(index);
    }

    @Override
    public int modCount() {
        return LexerUtilsConstants.MOD_COUNT_IMMUTABLE_INPUT;
    }

    @Override
    public int tokenCount() {
        return tokenList.tokenCount();
    }

    @Override
    public int tokenCountCurrent() {
        return tokenList.tokenCountCurrent();
    }

    @Override
    public Language<T> language() {
        return tokenList.language();
    }

    @Override
    public LanguagePath languagePath() {
        return tokenList.languagePath();
    }

    @Override
    public int tokenOffset(AbstractToken<T> token) {
        return tokenList.tokenOffset(token);
    }

    @Override
    public int[] tokenIndex(int offset) {
        return LexerUtilsConstants.tokenIndexBinSearch(this, offset, tokenCount());
    }

    public char charAt(int offset) {
        throw new IllegalStateException("Unexpected call.");
    }

    @Override
    public void setTokenOrEmbedding(int index, TokenOrEmbedding<T> t) {
        tokenList.setTokenOrEmbedding(index, t);
    }

    @Override
    public TokenList<?> rootTokenList() {
        return tokenList.rootTokenList();
    }

    @Override
    public CharSequence inputSourceText() {
        return rootTokenList().inputSourceText();
    }

    @Override
    public TokenHierarchyOperation<?,?> tokenHierarchyOperation() {
        return tokenList.tokenHierarchyOperation();
    }
    
    @Override
    public InputAttributes inputAttributes() {
        return tokenList.inputAttributes();
    }

    @Override
    public int lookahead(int index) {
        // Can be used by LexerTestUtilities.lookahead()
        return tokenList.lookahead(index);
    }

    @Override
    public Object state(int index) {
        return tokenList.state(index);
    }

    @Override
    public boolean isContinuous() {
        return tokenList.isContinuous();
    }

    @Override
    public Set<T> skipTokenIds() {
        return tokenList.skipTokenIds();
    }
    
    @Override
    public int startOffset() {
        return tokenOffsetDiff + tokenList.startOffset();
    }

    @Override
    public int endOffset() {
        return tokenOffsetDiff + tokenList.endOffset();
    }

    @Override
    public boolean isRemoved() {
        return false;
    }

    @Override
    public StringBuilder dumpInfo(StringBuilder sb) {
        return sb;
    }

    @Override
    public String dumpInfoType() {
        return "FilterSnapshotTL";
    }

}
