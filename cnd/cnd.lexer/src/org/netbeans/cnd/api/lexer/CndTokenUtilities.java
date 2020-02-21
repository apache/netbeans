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
package org.netbeans.cnd.api.lexer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 */
public class CndTokenUtilities {

    private CndTokenUtilities() {
    }

    /**
     * method should be called under document read lock
     * @param doc
     * @param offset
     * @return
     */
    public static boolean isInPreprocessorDirective(Document doc, int offset) {
        AbstractDocument aDoc = (AbstractDocument)doc;
        aDoc.readLock();
        try {
            TokenSequence<TokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequenceWithoutEmbeddings(doc, offset);
            if (cppTokenSequence != null) {
                return cppTokenSequence.token().id() == CppTokenId.PREPROCESSOR_DIRECTIVE;
            }
        } finally {
            aDoc.readUnlock();
        }
        return false;
    }

    /**
     * method should be called under document read lock
     * @param doc
     * @param offset
     * @return
     */
    public static boolean isInProCDirective(Document doc, int offset) {
        AbstractDocument aDoc = (AbstractDocument)doc;
        aDoc.readLock();
        try {
            TokenSequence<TokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequenceWithoutEmbeddings(doc, offset);
            if (cppTokenSequence != null) {
                return cppTokenSequence.token().id() == CppTokenId.PROC_DIRECTIVE;
            }
        } finally {
            aDoc.readUnlock();
        }
        return false;
    }
    
    /**
     * method should be called under document read lock and token processor must be
     * very fast to prevent document blocking. If startOffset is less than lastOffset,
     * then process tokens in backward direction, otherwise in forward
     * @param tp
     * @param doc
     * @param startOffset
     * @param lastOffset
     */
    public static void processTokens(CndTokenProcessor<Token<TokenId>> tp, Document doc, int startOffset, int lastOffset) {
        TokenSequence<TokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, startOffset, false, lastOffset < startOffset);
        if (cppTokenSequence == null) {
            // check if it is C/C++ document at all
            TokenHierarchy<Document> hi = TokenHierarchy.get(doc);
            TokenSequence<?> ts = hi.tokenSequence();
            if (ts != null) {
                if (CndLexerUtilities.isCppLanguage(ts.language(), true)) {
                    tp.start(startOffset, startOffset, lastOffset);
                    // just emulate finish
                    tp.end(lastOffset, lastOffset);
                    return;
                }
            }            
            return;
        }
        processTokens(tp, cppTokenSequence, startOffset, lastOffset);
    }
    
    /**
     * If startOffset is less than lastOffset,
     * then process tokens in backward direction, otherwise in forward
     * @param tp
     * @param doc
     * @param startOffset
     * @param lastOffset
     */
    public static void processTokens(CndTokenProcessor<Token<TokenId>> tp, TokenSequence<TokenId> cppTokenSequence, int startOffset, int lastOffset) {
        processTokens(tp, cppTokenSequence, startOffset, lastOffset, 0);
    }    
    
    /**
     * If startOffset is less than lastOffset,
     * then process tokens in backward direction, otherwise in forward.
     * Tokens could be shifted via offsetShift parameter.
     * 
     * @param tp
     * @param doc
     * @param startOffset
     * @param lastOffset
     * @param offsetShift - shift of expression offsets from token sequence offsets
     */
    public static void processTokens(CndTokenProcessor<Token<TokenId>> tp, TokenSequence<TokenId> cppTokenSequence, int startOffset, int lastOffset, int offsetShift) {
        int shift = cppTokenSequence.move(shiftOffset(startOffset, -offsetShift));
        tp.start(startOffset, startOffset - shift, lastOffset);
        if (processTokensImpl(tp, cppTokenSequence, startOffset, lastOffset, offsetShift, shift != 0)) {
            tp.end(lastOffset, shiftOffset(cppTokenSequence.offset(), offsetShift));
        } else {
            tp.end(lastOffset, lastOffset);
        }
    }    

    public static <T extends TokenId> TokenItem<T> createTokenItem(TokenSequence<T> ts) {
        return TokenItemImpl.create(ts);
    }

    public static <T extends TokenId> TokenItem<T> createTokenItem(Token<T> token, int tokenOffset) {
        return TokenItemImpl.create(token, tokenOffset);
    }

    /**
     * method should be called under document read lock
     * @param doc
     * @param offset
     * @return
     */
    public static TokenItem<TokenId> getFirstNonWhiteBwd(Document doc, int offset) {
        SkipTokenProcessor tp = new SkipTokenProcessor(Collections.<TokenId>emptySet(), skipWSCategories, true);
        processTokens(tp, doc, offset, 0);
        return tp.getTokenItem();
    }

    /**
     * method should be called under read lock
     * Return the position of the last command separator before the given position.
     * @param doc documetn
     * @param pos position in document
     * @return position of the last command separator before the given position or
     * 
     */
    public static int getLastCommandSeparator(Document doc, int pos) throws BadLocationException {
        if (pos < 0 || pos > doc.getLength()) {
            throw new BadLocationException("position is out of range[" + 0 + "-" + doc.getLength() + "]", pos); // NOI18N
        }
        if (pos == 0) {
            return 0;
        }
        AbstractDocument aDoc = (AbstractDocument)doc;
        aDoc.readLock();
        try {
            TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, pos, true, true);
            if (ts == null) {
                return 0;
            }
            CppTokenId prev = null;
            int bracesDepth = 0;
            do {
                final TokenId id = ts.token().id();
                if(id instanceof CppTokenId) {
                        switch ((CppTokenId)id) {
                            case SEMICOLON:
                                if (bracesDepth <= 0) {
                                    return ts.offset();
                                }
                                prev = (CppTokenId) id;
                                break;
                                
                            case LBRACE:
                                if (bracesDepth > 0) {
                                    bracesDepth--;
                                } else {
                                    return ts.offset();
                                }
                                prev = (CppTokenId) id;
                                break;
                                   
                                
                            case RBRACE:
                                if (bracesDepth > 0) {
                                    bracesDepth++;
                                } else if (canBeNextToLambdaToken(prev)) {
                                    bracesDepth++;
                                } else if (canBeNextToUniformInitilaizerToken(prev)) {
                                    bracesDepth++;
                                } else {
                                    return ts.offset();
                                }
                                prev = (CppTokenId) id;
                                break;
                                
                            case WHITESPACE:
                            case NEW_LINE:
                            case LINE_COMMENT:
                            case DOXYGEN_LINE_COMMENT:
                            case BLOCK_COMMENT:
                            case DOXYGEN_COMMENT:
                            case ESCAPED_LINE:
                                break;
                                
                            default:
                                prev = (CppTokenId) id;
                                break;
                        }
                    }
            } while (ts.movePrevious());
            ts.moveStart();
            if (ts.moveNext()) {
                return ts.offset();
            }
        } finally {
            aDoc.readUnlock();
        }
        return 0;
    }

    /**
     * method should be called under read lock
     * move token sequence to the position of preprocessor keyword
     * @param ts token sequence
     * @return true if successfuly moved, false if no preprocessor keyword in given sequence
     * of sequence is null, or not preprocessor directive sequence
     */
    public static boolean moveToPreprocKeyword(TokenSequence<TokenId> ts) {
        if (ts != null) {
            ts.moveStart();
            ts.moveNext();
            if (!ts.moveNext()) {// skip start #
                return false;
            }
            if (shiftToNonWhite(ts, false)) {
            final TokenId id = ts.token().id();
                if(id instanceof CppTokenId) {
                    switch ((CppTokenId)id) {
                        case PREPROCESSOR_DEFINE:
                        case PREPROCESSOR_ELIF:
                        case PREPROCESSOR_ELSE:
                        case PREPROCESSOR_ENDIF:
                        case PREPROCESSOR_ERROR:
                        case PREPROCESSOR_IDENT:
                        case PREPROCESSOR_IF:
                        case PREPROCESSOR_IFDEF:
                        case PREPROCESSOR_IFNDEF:
                        case PREPROCESSOR_INCLUDE:
                        case PREPROCESSOR_INCLUDE_NEXT:
                        case PREPROCESSOR_LINE:
                        case PREPROCESSOR_PRAGMA:
                        case PREPROCESSOR_UNDEF:
                        case PREPROCESSOR_WARNING:
                            return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * method should be called under read lock
     * move sequence to the first not whitespace category token if needed
     * @param ts token sequence to move
     * @param backward if <code>true</code> move backward, otherwise move forward
     * @return true if successfuly moved, false otherwise
     */
    public static boolean shiftToNonWhite(TokenSequence<TokenId> ts, boolean backward) {
        do {
            final TokenId id = ts.token().id();
            if(id instanceof CppTokenId) {
                switch ((CppTokenId)id) {
                    case WHITESPACE:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case LINE_COMMENT:
                    case ESCAPED_LINE:
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                        break;
                    default:
                        return true;
                }
            }
        } while (backward ? ts.movePrevious() : ts.moveNext());
        return false;
    }
    
    /**
     * method should be called under document read lock
     * returns offsetable token on interested offset
     * @param cppTokenSequence token sequence
     * @param offset interested offset
     * @return returns ofssetable token, but if offset is in the beginning of whitespace
     * or comment token, then it returns previous token
     */
    public static TokenItem<TokenId> getTokenCheckPrev(Document doc, int offset) {
        return getTokenImpl(doc, offset, true, true);
    }

    /**
     * method should be called under document read lock
     * @param doc
     * @param offset
     * @param tokenizePP
     * @return
     */
    public static TokenItem<TokenId> getToken(Document doc, int offset, boolean tokenizePP) {
        return getTokenImpl(doc, offset, tokenizePP, false);
    }

    private static TokenItem<TokenId> getTokenImpl(Document doc, int offset, boolean tokenizePP, boolean checkPrevious) {
        TokenSequence<TokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, offset, tokenizePP, false);
        if (cppTokenSequence == null) {
            return null;
        }
        TokenItem<TokenId> offsetToken = getTokenImpl(cppTokenSequence, offset, checkPrevious);
        return offsetToken;
    }

    private static TokenItem<TokenId> getTokenImpl(TokenSequence<TokenId> cppTokenSequence, int offset, boolean checkPrevious) {
        if (cppTokenSequence == null) {
            return null;
        }
        int shift = cppTokenSequence.move(offset);
        TokenItem<TokenId> offsetToken = null;
        boolean checkPrev = false;
        if (cppTokenSequence.moveNext()) {
            if(cppTokenSequence.token().id() instanceof TokenId) {
            offsetToken = TokenItemImpl.create(cppTokenSequence);
            if (checkPrevious && (shift == 0)) {
                String category = offsetToken.id().primaryCategory();
                if (CppTokenId.WHITESPACE_CATEGORY.equals(category) ||
                        CppTokenId.COMMENT_CATEGORY.equals(category) ||
                        CppTokenId.SEPARATOR_CATEGORY.equals(category) ||
                        CppTokenId.OPERATOR_CATEGORY.equals(category)) {
                    checkPrev = true;
                }
            }
            }
        }
        if (checkPrev && cppTokenSequence.movePrevious()) {
            offsetToken = TokenItemImpl.create(cppTokenSequence);
        }
        return offsetToken;
    }

    private static boolean processTokensImpl(CndTokenProcessor<Token<TokenId>> tp, TokenSequence<TokenId> cppTokenSequence, int startOffset, int lastOffset, int offsetShift, boolean adjust) {
        boolean processedToken = false;
        boolean bwd = (lastOffset < startOffset);
        boolean adjustOnFirstIteration = adjust;
        // in forward direction move to next, otherwise move to previous token
        while (!tp.isStopped()) {
            // for backward mode, we need token as well
            boolean moved;
            if (adjustOnFirstIteration || !bwd) {
                moved = cppTokenSequence.moveNext();
            } else {
                moved = cppTokenSequence.movePrevious();
            }
            if (!moved) {
                break;
            }
            adjustOnFirstIteration = false;
            Token<TokenId> token = cppTokenSequence.token();
            // check finish condition
            if (bwd) {
                if (shiftOffset(cppTokenSequence.offset() + token.length(), offsetShift) < lastOffset) {
                    break;
                }
            } else {
                if (shiftOffset(cppTokenSequence.offset(), offsetShift) >= lastOffset) {
                    break;
                }
            }
            if (tp.token(token, shiftOffset(cppTokenSequence.offset(), offsetShift))) {
                // process embedding
                @SuppressWarnings("unchecked")
                TokenSequence<TokenId> embedded = (TokenSequence<TokenId>) cppTokenSequence.embedded();
                if (embedded != null) {
                    int shift = 0;
                    if (shiftOffset(cppTokenSequence.offset(), offsetShift) < startOffset) {
                        shift = embedded.move(shiftOffset(startOffset, -offsetShift));
                    }
                    processedToken |= processTokensImpl(tp, embedded, startOffset, lastOffset, offsetShift, shift != 0);
                }
            } else {
                processedToken = true;
            }
        }
        return processedToken;
    }
    
    private static int shiftOffset(int offset, int shift) {
        return offset + shift;
    }
    
    private static boolean canBeNextToLambdaToken(CppTokenId id) {
        return id == CppTokenId.LPAREN 
            || id == CppTokenId.COMMA
            || id == CppTokenId.RPAREN;
    }
    
    private static boolean canBeNextToUniformInitilaizerToken(CppTokenId id) {
        return id == CppTokenId.DOT
            || id == CppTokenId.COMMA 
            || id == CppTokenId.LPAREN
            || id == CppTokenId.RPAREN;
    }

    private static class SkipTokenProcessor extends CndAbstractTokenProcessor<Token<TokenId>> {

        private boolean stopped = false;
        private TokenItem<TokenId> tokenItem = null;
        private Token<TokenId> lastToken = null;
        private final Set<TokenId> skipTokenIds;
        private final Set<String> skipTokenCategories;
        private final boolean processPP;

        public SkipTokenProcessor(Set<TokenId> skipTokenIds, Set<String> skipTokenCategories, boolean processPP) {
            this.skipTokenIds = skipTokenIds;
            this.skipTokenCategories = skipTokenCategories;
            this.processPP = processPP;
        }

        @Override
        public boolean token(Token<TokenId> token, int tokenOffset) {
            lastToken = token;
            if (token.id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
                return processPP;
            }
            if (!skipTokenIds.contains(token.id()) && !skipTokenCategories.contains(token.id().primaryCategory())) {
                stopped = true;
            }
            return false;
        }

        @Override
        public boolean isStopped() {
            return stopped;
        }

        public TokenItem<TokenId> getTokenItem() {
            return tokenItem;
        }

        @Override
        public void end(int offset, int lastTokenOffset) {
            super.end(offset, lastTokenOffset);
            if (lastToken != null) {
                tokenItem = TokenItemImpl.create(lastToken, lastTokenOffset);
            }
        }
    }

    private static final class TokenItemImpl<T extends TokenId> extends TokenItem.AbstractItem<T> implements Comparable<TokenItem.AbstractItem<T>> {
        private final Token<T> token;

        private TokenItemImpl(Token<T> token, int offset) {
            super(token.id(), token.partType(), offset, token.text());
            this.token = token;
        }

        private static <T extends TokenId> TokenItem<T> create(TokenSequence<T> ts) {
            return new TokenItemImpl<T>(ts.token(), ts.offset());
        }

        private static <T extends TokenId> TokenItem<T> create(Token<T> token, int offset) {
            return new TokenItemImpl<T>(token, offset);
        }

        @Override
        public int hashCode() {
            return this.token.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TokenItemImpl<T> other = (TokenItemImpl<T>) obj;
            return this.token == other.token;
        }

        @Override
        public String toString() {
            return "TokenItemImpl{" + "token=" + token + '}' + super.toString(); // NOI18N
        }

        @Override
        public int compareTo(AbstractItem<T> o) {
            return this.offset() - o.offset();
        }
    }
    private static final Set<String> skipWSCategories = new HashSet<String>(1);


    static {
        skipWSCategories.add(CppTokenId.WHITESPACE_CATEGORY);
    }
}
