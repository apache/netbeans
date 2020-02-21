/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
