/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion.cplusplus.ext;

import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndTokenProcessor;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.completion.impl.xref.ReferencesSupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.support.Interrupter;

/**
 * Macro expanded token processor.
 *
 */
public final class CsmExpandedTokenProcessor implements CndTokenProcessor<Token<TokenId>>, MacroCallback {

    private final CndTokenProcessor<Token<TokenId>> tp;
    private final Document doc;
    private final int lastOffset;
    private boolean inMacro;
    private int skipTill = -1;
    private final CsmFile file;
    private List<CsmReference> macros;

    private CsmExpandedTokenProcessor(Document doc, CsmFile file, CndTokenProcessor<Token<TokenId>> tp, int offset, List<CsmReference> macros) {
        this.tp = tp;
        this.doc = doc;
        this.lastOffset = offset;
        this.file = file;
        this.macros = macros;
    }

    public static CndTokenProcessor<Token<TokenId>> create(CsmFile file, Document doc, CndTokenProcessor<Token<TokenId>> tp, int offset) {
        if (doc != null) {
            if (file == null) {
                file = CsmUtilities.getCsmFile(doc, true, false);
            }
            if (file != null) {
                List<CsmReference> macros = CsmFileInfoQuery.getDefault().getMacroUsages(file, doc, Interrupter.DUMMY);
                if (macros != null) {
                    return create(doc, file, tp, offset, macros);
                }
            }
        }
        return tp;
    }

    private static CndTokenProcessor<Token<TokenId>> create(Document doc, CsmFile file, CndTokenProcessor<Token<TokenId>> tp, int offset, List<CsmReference> macros) {
        CsmMacroExpansion.expand(doc, file, 0, 0, true);
        return new CsmExpandedTokenProcessor(doc, file, tp, offset, macros);
    }

    @Override
    public void start(int startOffset, int firstTokenOffset, int lastOffset) {
        tp.start(startOffset, firstTokenOffset, lastOffset);
    }

    @Override
    public void end(int offset, int lastTokenOffset) {
        tp.end(offset, lastTokenOffset);
    }

    @Override
    public boolean isStopped() {
        return tp.isStopped();
    }

    @Override
    public boolean isMacroExpansion() {
        return inMacro;
    }

    public boolean isMacro(Token<TokenId> token, int tokenOffset) {
        return CndLexerUtilities.isCppIdentifierStart(token.text().charAt(0)) && ReferencesSupport.findMacro(macros, tokenOffset) != null;
    }

    @Override
    public boolean token(Token<TokenId> token, int tokenOffset) {
        // Additional logic only for macros
        if (skipTill <= tokenOffset) {
            skipTill = -1;
        }
        if (skipTill < 0 && (isMacro(token, tokenOffset) || inMacro)) {
            TokenSequence<TokenId> expTS = null;
            String expansion = CsmMacroExpansion.expand(doc, file, tokenOffset, tokenOffset + token.length(), false);
            if (expansion != null) {
                if (expansion.equals("")) { // NOI18N
                    if (lastOffset == -1 || tokenOffset + token.length() < lastOffset) {
                        return false;
                    }
                } else if (inMacro) {
                    if (tp instanceof CsmCompletionTokenProcessor) {
                        List<CsmCompletionExpression> stack = ((CsmCompletionTokenProcessor)tp).getStack();
                        boolean errorState = ((CsmCompletionTokenProcessor)tp).isErrorState();
                        if (errorState && stack.isEmpty()) {
                            ((CsmCompletionTokenProcessor)tp).setLastSeparatorOffset(tokenOffset);
                        }
                    }
                    inMacro = false;
                } else {
                    inMacro = true;
                    int[] span = CsmMacroExpansion.getMacroExpansionSpan(doc, tokenOffset, false);
                    if (span[0] < lastOffset && lastOffset <= span[1]) {
                        // skip expansion of this macro
                        skipTill = span[1];
                        inMacro = false;
                        return tp.token(token, tokenOffset);
                    }
                    TokenHierarchy<String> hi = TokenHierarchy.create(expansion, CndLexerUtilities.getLanguage(doc));
                    List<TokenSequence<?>> tsList = hi.embeddedTokenSequences(tokenOffset + token.length(), true);
                    // Go from inner to outer TSes
                    for (int i = tsList.size() - 1; i >= 0; i--) {
                        TokenSequence<?> ts = tsList.get(i);
                        final Language<?> lang = ts.languagePath().innerLanguage();
                        if (CndLexerUtilities.isCppLanguage(lang, false)) {
                            @SuppressWarnings("unchecked") // NOI18N
                            TokenSequence<TokenId> uts = (TokenSequence<TokenId>) ts;
                            expTS = uts;
                        }
                    }
                    if (expTS != null) {
                        expTS.moveStart();
                        if (expTS.moveNext()) {
                            boolean res;
                            Token<TokenId> expToken = expTS.token();
                            if (!expTS.moveNext()) {
                                if (expToken.text().toString().equals(token.text().toString()) &&
                                        expToken.id().equals(token.id())) {
                                    res = tp.token(token, tokenOffset);
                                } else {
                                    res = tp.token(expToken, tokenOffset);
                                }
                            } else {
                                res = tp.token(expToken, tokenOffset);
                                res = tp.token(expTS.token(), tokenOffset);
                                while (expTS.moveNext()) {
                                    res = tp.token(expTS.token(), tokenOffset);
                                }
                            }
                            return res;
                        }
                    }
                }
            }
        }
        if (!isWhitespace(token)) {
            inMacro = false;
        }
        return tp.token(token, tokenOffset);
    }

    private boolean isWhitespace(Token<TokenId> docToken) {
        final TokenId id = docToken.id();
        if(id instanceof CppTokenId) {
            switch ((CppTokenId)id) {
                case NEW_LINE:
                case WHITESPACE:
                case ESCAPED_WHITESPACE:
                case ESCAPED_LINE:
                    return true;
            }
        }
        return false;
    }
}
