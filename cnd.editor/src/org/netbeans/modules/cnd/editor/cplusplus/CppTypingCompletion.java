/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppStringTokenId;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor.Context;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;
import org.openide.util.CharSequences;

/**
 *
 */
public class CppTypingCompletion {
    private static final String EMPTY_RAW_STRING = "\"()\""; // NOI18N
    private static final String EMPTY_RAW_STRING_WITHOUT_CHARACTER = "\")\""; // NOI18N

    private CppTypingCompletion() {}

    static final class ExtraText {
        private final int caretPosition;
        private final int textPosition;
        private final String extraText;

        public ExtraText(int caretPosition) {
            this.caretPosition = caretPosition;
            this.textPosition = -1;
            this.extraText = null;
        }

        public ExtraText(int caretPosition, int textPosition, String text) {
            this.caretPosition = caretPosition;
            this.textPosition = textPosition;
            this.extraText = text;
        }

        public int getCaretPosition() {
            return caretPosition;
        }

        public String getExtraText() {
            return extraText;
        }

        public int getExtraTextPostion() {
            return textPosition;
        }
    }

    static ExtraText checkRawStringRemove(Context context) throws BadLocationException {
        BaseDocument doc = (BaseDocument) context.getDocument();
        int dotPos = context.getOffset();
        TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, dotPos, true, false);
        if (ts == null) {
            return null;
        }
        if (ts.move(dotPos) != 0 && ts.moveNext()) {
            Token<TokenId> token = ts.token();
            if (token.id() == CppTokenId.RAW_STRING_LITERAL) {
                // remove delimeter inside raw string should be symmetrical
                @SuppressWarnings("unchecked")
                TokenSequence<CppStringTokenId> es = (TokenSequence<CppStringTokenId>) ts.embedded();
                RawStringContext rsContext = calculateRawStringContext(es, dotPos);
                if (rsContext.emptyString) {
                    // remove of empty string is expected
                    return new ExtraText(rsContext.firstQuoteOffset, rsContext.firstQuoteOffset, EMPTY_RAW_STRING_WITHOUT_CHARACTER);
                }
                if (rsContext.contextTokenId != null && !rsContext.emptyDelimeter) {
                    CppStringTokenId id = rsContext.contextTokenId;
                    if (context.isBackwardDelete()) {
                        if (id == CppStringTokenId.START_DELIMETER ||
                            id == CppStringTokenId.START_DELIMETER_PAREN) {
                            return new ExtraText(dotPos, rsContext.matchingDelimeterSymbolOffset-2, context.getText());
                        } else if (id == CppStringTokenId.LAST_QUOTE ||
                                id == CppStringTokenId.END_DELIMETER) {
                            return new ExtraText(dotPos, rsContext.matchingDelimeterSymbolOffset-1, context.getText());
                        }
                    } else {
                        if (id == CppStringTokenId.START_DELIMETER) {
                            return new ExtraText(dotPos, rsContext.matchingDelimeterSymbolOffset-1, context.getText());
                        } else if (id == CppStringTokenId.END_DELIMETER) {
                            return new ExtraText(dotPos, rsContext.matchingDelimeterSymbolOffset, context.getText());
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     *
     * @param context
     * @return -1 if not handled, otherwise caret position to shift after insert
     */
    static ExtraText checkRawStringInsertion(TypedTextInterceptor.MutableContext context) {
        String text = context.getText();
        if (text.length() != 1) {
            return null;
        }
        BaseDocument doc = (BaseDocument) context.getDocument();
        int dotPos = context.getOffset();
        TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, dotPos, true, false);
        if (ts == null) {
            return null;
        }
        ExtraText rawStringTypingInfo = null;
        char typedChar = text.charAt(0);
        int offsetInToken = ts.move(dotPos);
        // special check for start of raw strings
        if (typedChar == '"') {
            if (offsetInToken == 0) { // between tokens
                if (ts.movePrevious()) { // move to previous possible ID token
                    Token<TokenId> tokenAtDot = ts.token();
                    if (tokenAtDot != null && (CppTokenId.IDENTIFIER == tokenAtDot.id() || CppTokenId.PREPROCESSOR_IDENTIFIER == tokenAtDot.id())) {
                        CharSequence tokText = tokenAtDot.text();
                        if (CppStringTokenId.PREFIX_R.fixedText().contentEquals(tokText)
                                || CppStringTokenId.PREFIX_LR.fixedText().contentEquals(tokText)
                                || CppStringTokenId.PREFIX_UR.fixedText().contentEquals(tokText)
                                || CppStringTokenId.PREFIX_u8R.fixedText().contentEquals(tokText)) {
                            // this is start of raw string, need to close it, otherwise it will continue
                            // till the end of document
                            context.setText(EMPTY_RAW_STRING, 1);
                            rawStringTypingInfo = new ExtraText(dotPos + 1);
                        }
                    }
                    if (rawStringTypingInfo == null) {
                        ts.move(dotPos);
                    }
                }
            }
        }
        if (rawStringTypingInfo == null && ts.moveNext()) {
            Token<TokenId> token = ts.token();
            if (token.id() == CppTokenId.RAW_STRING_LITERAL) {
                // typing inside raw string delimeter should be symmetrical
                @SuppressWarnings("unchecked")
                TokenSequence<CppStringTokenId> es = (TokenSequence<CppStringTokenId>) ts.embedded();
                RawStringContext rsContext = calculateRawStringContext(es, dotPos);
                if (rsContext.contextTokenId != null) {
                    CppStringTokenId id = rsContext.contextTokenId;
                    if (typedChar == '(' && id == CppStringTokenId.START_DELIMETER_PAREN) {
                        // eat (
                        context.setText("", 0);
                        return new ExtraText(dotPos + 1);
                    } else if (typedChar == '"' && id == CppStringTokenId.LAST_QUOTE) {
                        // eat closing "
                        context.setText("", 0);
                        return new ExtraText(dotPos + 1);
                    } else if (typedChar == ')' && id == CppStringTokenId.END_DELIMETER_PAREN &&
                            rsContext.emptyDelimeter) {
                        // eat closing ) when no delimeters specified
                        context.setText("", 0);
                        return new ExtraText(dotPos + 1);
                    } else if (rsContext.matchingDelimeterSymbolOffset != -1) {
                        context.setText("", 0);
                        return new ExtraText(dotPos, rsContext.matchingDelimeterSymbolOffset, "" + typedChar);
                    }
                }
            }
        }
        return rawStringTypingInfo;
    }

    private static final class RawStringContext {
        private final int firstQuoteOffset;
        private final int matchingOffset;
        private final int matchingDelimeterSymbolOffset;
        private final Token<CppStringTokenId> contextToken;
        private final CppStringTokenId contextTokenId;
        private final boolean emptyStartDelimeter;
        private final boolean emptyEndDelimeter;
        private final boolean emptyDelimeter;
        private final boolean emptyString;

        public RawStringContext(Token<CppStringTokenId> contextToken,
                int firstQuoteOffset, boolean emptyString,
                int matchingOffset, int matchingDelimeterSymbolOffset,
                boolean emptyStartDelimeter, boolean emptyEndDelimeter) {
            this.firstQuoteOffset = firstQuoteOffset;
            this.emptyString = emptyString;
            this.matchingOffset = matchingOffset;
            this.matchingDelimeterSymbolOffset = matchingDelimeterSymbolOffset;
            this.contextToken = contextToken;
            this.contextTokenId = contextToken == null ? null : contextToken.id();
            this.emptyStartDelimeter = emptyStartDelimeter;
            this.emptyEndDelimeter = emptyEndDelimeter;
            this.emptyDelimeter = emptyStartDelimeter && emptyEndDelimeter;
        }

        @Override
        public String toString() {
            return "RawStringContext{" + "matchingOffset=" + matchingOffset + // NOI18N
                    ", matchingDelimeterSymbolOffset=" + matchingDelimeterSymbolOffset + // NOI18N
                    ", contextToken=" + contextToken + ", contextTokenId=" + // NOI18N
                    contextTokenId + ", emptyStartDelimeter=" + emptyStartDelimeter + // NOI18N
                    ", emptyEndDelimeter=" + emptyEndDelimeter + // NOI18N
                    ", emptyDelimeter=" + emptyDelimeter + '}'; // NOI18N
        }
    }

    private static RawStringContext calculateRawStringContext(final TokenSequence<CppStringTokenId> es, final int dotPos) {
        RawStringContext out = null;
        Token<CppStringTokenId> firstQuote = null;
        int firstQuoteOffset = -1;
        Token<CppStringTokenId> startDelim = null;
        int startDelimOffset = -1;
        Token<?> startDelimParen = null;
        int startDelimParenOffset = -1;
        Token<CppStringTokenId> endDelimParen = null;
        int endDelimParenOffset = -1;
        Token<CppStringTokenId> endDelim = null;
        int endDelimOffset = -1;
        Token<CppStringTokenId> lastQuote = null;
        int lastQuoteOffset = -1;
        Token<CppStringTokenId> contextToken = null;
        int matchingOffset = -1;
        int matchingDelimSymbolOffset = -1;
        es.moveStart();
        Outer:
        while (es.moveNext()) {
            @SuppressWarnings("unchecked")
            Token<CppStringTokenId> cur = es.token();
            int curOffset = es.offset();
            switch (cur.id()) {
                case FIRST_QUOTE:
                    assert firstQuote == null;
                    firstQuote = cur;
                    firstQuoteOffset = curOffset;
                    if (curOffset == dotPos) {
                        contextToken = cur;
                    }
                    break;
                case START_DELIMETER:
                    assert startDelim == null;
                    startDelim = cur;
                    startDelimOffset = curOffset;
                    if (curOffset <= dotPos && dotPos < curOffset + cur.length()) {
                        contextToken = cur;
                    }
                    break;
                case START_DELIMETER_PAREN:
                    assert startDelimParen == null;
                    startDelimParen = cur;
                    startDelimParenOffset = curOffset;
                    if (curOffset == dotPos) {
                        contextToken = cur;
                    }
                    break;
                case PREFIX_L:
                case PREFIX_R:
                case PREFIX_U:
                case PREFIX_u:
                case PREFIX_u8:
                case PREFIX_LR:
                case PREFIX_UR:
                case PREFIX_uR:
                case PREFIX_u8R:
                    // skip prefix
                    break;
                default:
                    break Outer;
            }
        }
        CppStringTokenId contextTokenId = contextToken == null ? null : contextToken.id();
        es.moveEnd();
        Outer:
        while (es.movePrevious()) {
            @SuppressWarnings("unchecked")
            Token<CppStringTokenId> cur = es.token();
            int curOffset = es.offset();
            switch (cur.id()) {
                case LAST_QUOTE:
                    assert lastQuote == null;
                    lastQuote = cur;
                    lastQuoteOffset = curOffset;
                    // last quote matches with start delimeter paren
                    if (curOffset == dotPos) {
                        contextToken = cur;
                        if (startDelimParenOffset != -1) {
                            matchingDelimSymbolOffset = matchingOffset = startDelimParenOffset;
                        }
                    } else if (contextTokenId == CppStringTokenId.START_DELIMETER_PAREN) {
                        matchingDelimSymbolOffset = matchingOffset = curOffset;
                    }
                    break;
                case END_DELIMETER:
                    assert endDelim == null;
                    endDelim = cur;
                    endDelimOffset = curOffset;
                    // end delimeter matches with start delimeter
                    if (curOffset <= dotPos && dotPos < curOffset + cur.length()) {
                        contextToken = cur;
                        if (startDelimOffset != -1) {
                            assert startDelimOffset != -1;
                            assert matchingOffset == -1;
                            matchingDelimSymbolOffset = matchingOffset = startDelimOffset + (dotPos - curOffset);
                        }
                    } else if (contextTokenId == CppStringTokenId.START_DELIMETER) {
                        assert startDelimOffset != -1;
                        assert matchingOffset == -1;
                        assert dotPos >= startDelimOffset;
                        matchingDelimSymbolOffset = matchingOffset = curOffset + (dotPos - startDelimOffset);
                    }
                    break;
                case END_DELIMETER_PAREN:
                    assert endDelimParen == null;
                    endDelimParen = cur;
                    endDelimParenOffset = curOffset;
                    if (curOffset == dotPos) {
                        contextToken = cur;
                        if (startDelimParenOffset != -1) {
                            matchingOffset = startDelimParenOffset + 1;
                        }
                    }
                    break;
                default:
                    break Outer;
            }
        }
        if ((startDelim == null && endDelim == null) ||
            (startDelim != null && endDelim != null && CharSequences.comparator().compare(startDelim.text(), endDelim.text()) == 0)) {
            // only if both delimeters are empty or both have the same text
            return new RawStringContext(contextToken, 
                    firstQuoteOffset, firstQuoteOffset + EMPTY_RAW_STRING.length()-1 == lastQuoteOffset,
                    matchingOffset, matchingDelimSymbolOffset, startDelim == null, endDelim == null);
        } else {
            return new RawStringContext(contextToken, firstQuoteOffset, false, matchingOffset, -1, startDelim == null, endDelim == null);
        }
    }

}
