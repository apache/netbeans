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
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.lexer;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.Filter;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 */
public final class PreprocLexer extends CndLexer {
    private static final int INIT               = 0;
    private static final int DIRECTIVE_NAME     = INIT + 1;
    private static final int EXPRESSION         = DIRECTIVE_NAME + 1;
    private static final int INCLUDE_DIRECTIVE  = EXPRESSION + 1;
    private static final int PRAGMA             = INCLUDE_DIRECTIVE + 1;
    private static final int OMP                = PRAGMA + 1;
    private static final int OTHER              = OMP + 1;

    // shift is the number of bits enough to mask all states
    private static final int SHIFT = 3;
    private static final int MASK  = 0x7; // ~((~0) << SHIFT)
     
    private int state = INIT;
    private final Filter<CppTokenId> preprocFilter;
    private final Filter<CppTokenId> ompFilter;
    private final Filter<CppTokenId> keywordsFilter;

    public PreprocLexer(Filter<CppTokenId> defaultFilter, LexerRestartInfo<CppTokenId> info) {
        super(info);
        this.preprocFilter = CndLexerUtilities.getPreprocFilter();
        this.ompFilter = CndLexerUtilities.getOmpFilter();
        @SuppressWarnings("unchecked")
        Filter<CppTokenId> filter = (Filter<CppTokenId>) info.getAttributeValue(CndLexerUtilities.LEXER_FILTER);
        this.keywordsFilter = filter != null ? filter : defaultFilter;
        fromState((Integer) info.state()); // last line in contstructor
    }

    @Override
    public Object state() {
        Integer baseState = super.getState();
        int baseValue = baseState == null ? 0 : baseState.intValue();
        int value = (baseValue << SHIFT) | state;
        return Integer.valueOf(value);
    }
   
    private void fromState(Integer state) {
        if (state == null) {
            this.state = INIT;
            super.setState(null);
        } else {
            this.state = state.intValue() & MASK;
            super.setState(state.intValue() >> SHIFT);
        }
    }

    @Override
    protected Token<CppTokenId> finishSharp() {
        if (state == INIT) { 
            // the first sharp in preprocessor directive has own id            
            return token(CppTokenId.PREPROCESSOR_START);
        }
        return super.finishSharp();
    }

    @Override
    protected Token<CppTokenId> finishPercent() {
        if (state == INIT) {
            if (read(true) == ':') {
                // the first %: in preprocessor directive has own id
                return token(CppTokenId.PREPROCESSOR_START_ALT);
            }
            backup(1);
        }
        return super.finishPercent();
    }

    @SuppressWarnings("fallthrough")
    @Override
    protected Token<CppTokenId> finishDblQuote() {
        if (state == INCLUDE_DIRECTIVE) {
            while (true) { // user include literal
                switch (read(true)) {
                    case '"': // NOI18N
                        return token(CppTokenId.PREPROCESSOR_USER_INCLUDE);
                    case '\r':
                        consumeNewline();
                    case '\n':
                    case EOF:
                        return tokenPart(CppTokenId.PREPROCESSOR_USER_INCLUDE, PartType.START);
                }
            }              
        }
        return super.finishDblQuote();
    }

    @SuppressWarnings("fallthrough")
    @Override
    protected Token<CppTokenId> finishLT() {
        if (state == INCLUDE_DIRECTIVE) {
            while (true) { // system include literal
                switch (read(true)) {
                    case '>': // NOI18N
                        return token(CppTokenId.PREPROCESSOR_SYS_INCLUDE);
                    case '\r':
                        consumeNewline();
                    case '\n':
                    case EOF:
                        return tokenPart(CppTokenId.PREPROCESSOR_SYS_INCLUDE, PartType.START);
                }
            }              
        }        
        return super.finishLT();
    }
    
    @SuppressWarnings("fallthrough")
    @Override
    protected CppTokenId getKeywordOrIdentifierID(CharSequence text) {
        CppTokenId id = null;
        switch (state) {
            case DIRECTIVE_NAME:
                id = preprocFilter.check(text);
                break;
            case EXPRESSION:
                if (TokenUtilities.textEquals(CppTokenId.PREPROCESSOR_DEFINED.fixedText(), text)) {
                    id = CppTokenId.PREPROCESSOR_DEFINED;
                    break;
                }
                // nobreak
            case PRAGMA:
                id = ompFilter.check(text) == CppTokenId.PRAGMA_OMP_START ? CppTokenId.PRAGMA_OMP_START : null;
                break;
            case OMP:
                id = ompFilter.check(text);
                id = (id != null) ? id : keywordsFilter.check(text);
                break;
            case OTHER:
                id = keywordsFilter.check(text);
                break;
        }
        return id != null ? id : CppTokenId.PREPROCESSOR_IDENTIFIER;
    }

    @Override
    protected void postTokenCreate(CppTokenId id) {
        assert id != null;
        switch (state) { // change state of lexer
            case INIT:
                assert id == CppTokenId.PREPROCESSOR_START || id == CppTokenId.PREPROCESSOR_START_ALT:
                    "in INIT state only CppTokenId.PREPROCESSOR_START(_ALT) is possible: " + id; //NOI18N
                state = DIRECTIVE_NAME;
                break;
            case DIRECTIVE_NAME:
                if (!CppTokenId.WHITESPACE_CATEGORY.equals(id.primaryCategory()) &&
                           !CppTokenId.COMMENT_CATEGORY.equals(id.primaryCategory())) {
                    switch (id) {
                        case PREPROCESSOR_IF:
                        case PREPROCESSOR_ELIF:
                            state = EXPRESSION;
                            break;
                        case PREPROCESSOR_INCLUDE:
                        case PREPROCESSOR_INCLUDE_NEXT:
                            state = INCLUDE_DIRECTIVE;
                            break;
                        case PREPROCESSOR_PRAGMA:
                            state = PRAGMA;
                            break;
                        default:
                            state = OTHER;
                    }
                } else {
                    // do not change state
                }
                break;
            case PRAGMA:
                if (!CppTokenId.WHITESPACE_CATEGORY.equals(id.primaryCategory()) &&
                           !CppTokenId.COMMENT_CATEGORY.equals(id.primaryCategory())) {
                    switch (id) {
                        case PRAGMA_OMP_START:
                            state = OMP;
                            break;
                        default:
                            state = OTHER;
                    }
                }
                break;
            case INCLUDE_DIRECTIVE:                
            case EXPRESSION:                
            case OMP:
            case OTHER:                
                // do not change state
        }
    }
}
