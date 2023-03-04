/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.lexer;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.CustomTextToken;
import org.netbeans.lib.lexer.token.DefaultToken;
import org.netbeans.lib.lexer.token.PropertyToken;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Implementation of the functionality related to lexer input.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class LexerInputOperation<T extends TokenId> {

    // -J-Dorg.netbeans.lib.lexer.LexerInputOperation.level=FINE
    static final Logger LOG = Logger.getLogger(LexerInputOperation.class.getName());
    // -J-Dorg.netbeans.spi.lexer.LexerInput.level=FINE
    static final Logger LexerInputLOG = Logger.getLogger(LexerInput.class.getName());
    
    protected final TokenList<T> tokenList;
    
    /**
     * Current reading index which usually corresponds to real offset.
     * <br/>
     * It should be set to its initial value in the constructor by descendants.
     */
    protected int readOffset;
    
    /**
     * A value that designates a start of a token being currently recognized.
     */
    protected int tokenStartOffset;

    /**
     * Maximum index from which the char was fetched for current
     * (or previous) tokens recognition.
     * <br>
     * The index is updated lazily - only when EOF is reached
     * and when backup() is called.
     */
    private int lookaheadOffset;
    
    /**
     * Token length computed by assignTokenLength().
     */
    protected int tokenLength;
    
    protected Lexer<T> lexer;
    
    protected final LanguageOperation<T> languageOperation;

    
    /**
     * How many flyweight tokens were created in a row.
     */
    private int flyTokenSequenceLength;
    
    protected final WrapTokenIdCache<T> wrapTokenIdCache;
    
    public LexerInputOperation(TokenList<T> tokenList, int tokenIndex, Object lexerRestartState) {
        this.tokenList = tokenList;
        LanguagePath languagePath = tokenList.languagePath();
        Language<T> language = tokenList.language();
        this.languageOperation = LexerUtilsConstants.languageOperation(language);
        
        // Determine flyTokenSequenceLength setting
        while (--tokenIndex >= 0 && tokenList.tokenOrEmbedding(tokenIndex).token().isFlyweight()) {
            flyTokenSequenceLength++;
        }

        LanguageHierarchy<T> languageHierarchy = LexerApiPackageAccessor.get().languageHierarchy(
                LexerUtilsConstants.<T>innerLanguage(languagePath));
        TokenFactory<T> tokenFactory = LexerSpiPackageAccessor.get().createTokenFactory(this);
        LexerInput lexerInput = LexerSpiPackageAccessor.get().createLexerInput(this);

        LexerRestartInfo<T> info = LexerSpiPackageAccessor.get().createLexerRestartInfo(
                lexerInput, tokenFactory, lexerRestartState,
                languagePath, tokenList.inputAttributes());
        lexer = LexerSpiPackageAccessor.get().createLexer(languageHierarchy, info);

        wrapTokenIdCache = tokenList.tokenHierarchyOperation().getWrapTokenIdCache(language);

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.INFO, "LexerInputOperation created for " +
                    tokenList.tokenHierarchyOperation().inputSource(), new Exception()); // NOI18N
        }
    }

    /**
     * Read a character or return LexerInput.EOF.
     *
     * @param offset offset among characters that were read from this lexer input operation (zero
     *  offset is first character read from this lexer input operation).
     * @return character at the given offset or LexerInput.EOF.
     */
    public abstract int read(int offset);

    /**
     * Read a character that was already requested by {@link #read(int)} in the past.
     *
     * @param offset offset among characters that were read from this lexer input operation (zero
     *  offset is first character read from this lexer input operation).
     * @return character at the given offset.
     */
    public abstract char readExisting(int offset);

    /**
     * Fill appropriate data like token list and offset into a non-flyweight token.
     * <br/>
     * This method should also move over the token's characters by increasing
     * starting offset of the token and possibly other related variables.
     * 
     * @param token non-null non-flyweight token.
     */
    protected abstract void fillTokenData(AbstractToken<T> token);
    
    public final int read() {
        int c = read(readOffset++);
        if (c == LexerInput.EOF) {
            lookaheadOffset = readOffset; // count EOF char into lookahead
            readOffset--; // readIndex must not include EOF
        }
        return c;
    }
    
    public final int readLength() {
        return readOffset - tokenStartOffset;
    }
    
    public final char readExistingAtIndex(int index) {
        return readExisting(tokenStartOffset + index);
    }
    
    public final void backup(int count) {
        if (lookaheadOffset < readOffset) {
            lookaheadOffset = readOffset;
        }
        readOffset -= count;
    }
    
    /**
     * Get last recognized token's lookahead.
     * The method should only be used after fetching of the token.
     * 
     * @return extra characters need for token's recognition >= 0.
     */
    public final int lookahead() {
        return Math.max(lookaheadOffset, readOffset) - tokenStartOffset;
    }

    public AbstractToken<T> nextToken() {
        if (lexer == null) {
            return null;
        }
        while (true) {
            AbstractToken<T> token = (AbstractToken<T>)lexer.nextToken();
            if (token == null) {
                checkLexerInputFinished();
                return null;
            }
//            if (LOG.isLoggable(Level.FINE)) {
//                LOG.fine("NEXT-TOKEN: off=" + tokenStartOffset + ", len=" + tokenLength // NOI18N
//                        + ", " + token.dumpInfo() + '\n');
//            }
            // Check if token id of the new token belongs to the language
            Language<T> language = languageOperation.language();
            // Check that the id belongs to the language
            if (!isSkipToken(token) && !language.tokenIds().contains(token.id())) {
                String msgPrefix = "Invalid TokenId=" + token.id()
                        + " returned from lexer="
                        + lexer + " for language=" + language + ":\n";
                if (token.id().ordinal() > language.maxOrdinal()) {
                    throw new IllegalStateException(msgPrefix +
                            "Language.maxOrdinal()=" + language.maxOrdinal() + " < " + token.id().ordinal());
                } else { // Ordinal ok but different id with that ordinal contained in language
                    throw new IllegalStateException(msgPrefix +
                            "Language contains no or different tokenId with ordinal="
                            + token.id().ordinal() + ": " + language.tokenId(token.id().ordinal()));
                }
            }
            // Skip token's chars
            tokenStartOffset += tokenLength;
            if (!isSkipToken(token))
                return token;
        } // Continue to fetch non-skip token
    }

    /**
     * Used by token list updater after nextToken() to determine start offset of a token 
     * to be recognized next. Overriden for join token lists since join tokens
     * may span multiple ETLs.
     * 
     * @return start offset of a next token that would be recognized.
     */
    public int lastTokenEndOffset() {
        return tokenStartOffset;
    }

    public AbstractToken<T> getFlyweightToken(T id, String text) {
        if (text.length() > readLength()) {
            throw new IllegalArgumentException("getFlyweightToken(): Creating token " + // NOI18N
                    " for unread characters: text=\"" + // NOI18N
                    CharSequenceUtilities.debugText(text) + "\"; text.length()=" + // NOI18N
                    text.length() + " > readLength()=" + readLength()); // NOI18N
        }
        // Compare each recognized char with the corresponding char in text
        if (LOG.isLoggable(Level.FINE)) {
            for (int i = 0; i < text.length(); i++) {
                if (text.charAt(i) != readExistingAtIndex(i)) {
                    throw new IllegalArgumentException("Flyweight text in " + // NOI18N
                            "TokenFactory.getFlyweightToken(" + id + ", \"" + // NOI18N
                            CharSequenceUtilities.debugText(text) + "\") " + // NOI18N
                            "differs from recognized text: '" + // NOI18N
                            CharSequenceUtilities.debugChar(readExisting(i)) +
                            "' != '" + CharSequenceUtilities.debugChar(text.charAt(i)) + // NOI18N
                            "' at index=" + i // NOI18N
                    );
                }
            }
        }

        logTokenContent("getFlyweightToken", id, text.length());
        assignTokenLength(text.length());
        AbstractToken<T> token;
        if ((token = checkSkipToken(id)) == null) {
            if (isFlyTokenAllowed()) {
                token = languageOperation.getFlyweightToken(wrapTokenIdCache.plainWid(id), text);
                flyTokenSequenceLength++;
            } else { // Create regular token
                token = createDefaultTokenInstance(id);
                fillTokenData(token);
                flyTokenSequenceLength = 0;
            }
        }
        return token;
    }
    
    private AbstractToken<T> checkSkipToken(T id) {
        if (isSkipTokenId(id)) {
            // Prevent fly token occurrence after skip token to have a valid offset
            flyTokenSequenceLength = LexerUtilsConstants.MAX_FLY_SEQUENCE_LENGTH;
            return skipToken();
        }
        return null;
    }

    private void checkTokenIdNonNull(T id) {
        if (id == null) {
            throw new IllegalArgumentException("Token id must not be null. Fix lexer " + lexer); // NOI18N
        }
    }

    public AbstractToken<T> createToken(T id, int length) {
        checkTokenIdNonNull(id);
        logTokenContent("createToken", id, length);
        assignTokenLength(length);
        AbstractToken<T> token;
        if ((token = checkSkipToken(id)) == null) {
            token = createDefaultTokenInstance(id);
            fillTokenData(token);
            flyTokenSequenceLength = 0;
        }
        return token;
    }

    private void logTokenContent(String opName, T id, int length) {
        if (LexerInputLOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(100);
            sb.append("TokenFactory.").append(opName).append("(");
            sb.append(id).append(", ").append(length);
            sb.append("): \"");
            for (int i = 0; i < length; i++) {
                CharSequenceUtilities.debugChar(sb, readExistingAtIndex(i));
            }
            sb.append("\", st=").append(lexer.state()).append('\n');
            LexerInputLOG.fine(sb.toString());
        }
    }

    protected AbstractToken<T> createDefaultTokenInstance(T id) {
        return new DefaultToken<T>(wrapTokenIdCache.plainWid(id), tokenLength);
    }

    public AbstractToken<T> createToken(T id, int length, PartType partType) {
        if (partType == null)
            throw new IllegalArgumentException("partType must be non-null");
        if (partType == PartType.COMPLETE)
            return createToken(id, length);

        checkTokenIdNonNull(id);
        return createPropertyToken(id, length, null, partType);
    }

    public AbstractToken<T> createPropertyToken(T id, int length,
    TokenPropertyProvider<T> propertyProvider, PartType partType) {
        if (partType == null)
            partType = PartType.COMPLETE;

        logTokenContent("createPropertyToken", id, length);
        assignTokenLength(length);
        AbstractToken<T> token;
        if ((token = checkSkipToken(id)) == null) {
            token = createPropertyTokenInstance(id, propertyProvider, partType);
            fillTokenData(token);
            flyTokenSequenceLength = 0;
        }
        return token;
    }

    protected AbstractToken<T> createPropertyTokenInstance(T id,
    TokenPropertyProvider<T> propertyProvider, PartType partType) {
        return new PropertyToken<T>(wrapTokenIdCache.plainWid(id), tokenLength, propertyProvider, partType);
    }

    public AbstractToken<T> createCustomTextToken(T id, int length, CharSequence customText) {
        logTokenContent("createCustomTextToken", id, length);
        assignTokenLength(length);
        AbstractToken<T> token;
        if ((token = checkSkipToken(id)) == null) {
            token = createCustomTextTokenInstance(id, customText);
            fillTokenData(token);
            flyTokenSequenceLength = 0;
        }
        return token;
    }
    
    protected AbstractToken<T> createCustomTextTokenInstance(T id, CharSequence customText) {
        return new CustomTextToken<T>(wrapTokenIdCache.plainWid(id), customText, tokenLength);
    }

    public boolean isSkipTokenId(T id) {
        Set<T> skipTokenIds = tokenList.skipTokenIds();
        return (skipTokenIds != null && skipTokenIds.contains(id));
    }

    protected final int tokenLength() {
        return tokenLength;
    }

    public void assignTokenLength(int tokenLength) {
        if (tokenLength > readLength()) {
            throw new IndexOutOfBoundsException("tokenLength=" + tokenLength // NOI18N
                    + " > " + readLength() + ". " + lexer.getClass() + // NOI18N
                    " implementation must be fixed to create all tokens with a proper token length value."); // NOI18N
        }
        if (tokenLength <= 0) {
            throw new IndexOutOfBoundsException("tokenLength=" + tokenLength +
                    " <= 0. " + lexer.getClass() + // NOI18N
                    " implementation must be fixed to create all tokens with a proper token length value."); // NOI18N
        }
        this.tokenLength = tokenLength;
    }
    
    public final Object lexerState() {
        return lexer.state();
    }

    protected boolean isFlyTokenAllowed() {
        return (flyTokenSequenceLength < LexerUtilsConstants.MAX_FLY_SEQUENCE_LENGTH);
    }
    
    public final boolean isSkipToken(AbstractToken<T> token) {
        return (token == LexerUtilsConstants.SKIP_TOKEN);
    }
    
    @SuppressWarnings("unchecked")
    public final AbstractToken<T> skipToken() {
        return (AbstractToken<T>)LexerUtilsConstants.SKIP_TOKEN;
    }

    /**
     * Release the underlying lexer. This method can be called multiple times.
     */
    public final void release() {
        if (lexer != null) {
            lexer.release();
            lexer = null;
        }
    }
    
    /**
     * Check that there are no more characters to be read from the given
     * lexer input operation.
     */
    private void checkLexerInputFinished() {
        if (read() != LexerInput.EOF || readLength() > 0) {
            StringBuilder sb = new StringBuilder(100);
            int readLen = readLength();
            sb.append("Lexer ").append(lexer); // NOI18N
            sb.append("\n  returned null token but lexerInput.readLength()="); // NOI18N
            sb.append(readLen);
            sb.append("\n  lexer-state: ").append(lexer.state());
            sb.append("\n  ").append(this); // NOI18N
            sb.append("\n  Chars: \"");
            for (int i = 0; i < readLen; i++) {
                sb.append(CharSequenceUtilities.debugChar(readExistingAtIndex(i)));
            }
            sb.append("\" - these characters need to be tokenized."); // NOI18N
            sb.append("\nFix the lexer to not return null token in this state."); // NOI18N
            throw new IllegalStateException(sb.toString());
        }
    }

    @Override
    public String toString() {
        return "tokenStartOffset=" + tokenStartOffset + ", readOffset=" + readOffset + // NOI18N
                ", lookaheadOffset=" + lookaheadOffset;
    }

}
