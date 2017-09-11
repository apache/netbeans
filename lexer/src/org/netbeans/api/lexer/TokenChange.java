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
