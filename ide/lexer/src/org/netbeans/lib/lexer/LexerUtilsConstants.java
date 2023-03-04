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
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.lexer.inc.IncTokenList;
import org.netbeans.lib.lexer.inc.MutableTokenList;
import org.netbeans.lib.lexer.inc.SnapshotTokenList;
import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.token.TextToken;
import org.netbeans.spi.lexer.LanguageEmbedding;

/**
 * Various utility methods and constants in lexer module.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class LexerUtilsConstants {
    
    /**
     * Maximum allowed number of consecutive flyweight tokens.
     * <br>
     * High number of consecutive flyweight tokens
     * would degrade performance of offset
     * finding.
     */
    public static final int MAX_FLY_SEQUENCE_LENGTH = 5;
    
    /**
     * Token list's modCount for the case when the source input is unmodifiable.
     */
    public static final int MOD_COUNT_IMMUTABLE_INPUT = -1;
    
    /**
     * ModCount when the particular token list was removed from the token hierarchy.
     */
    public static final int MOD_COUNT_REMOVED = -2;
    
    /**
     * Initial size of a buffer for copying a text of a Reader.
     */
    public static final int READER_TEXT_BUFFER_SIZE = 16384;
    
    public static final AbstractToken<?> SKIP_TOKEN
        = new TextToken<TokenId>(new WrapTokenId<TokenId>(
            new TokenIdImpl("skip-token-id; special id of TokenFactory.SKIP_TOKEN; " + // NOI18N
                    " It should never be part of token sequence", 0, null)), // NOI18N
            "" // empty skip token text NOI18N
        );
    
    /**
     * Initial embedded token list's modCount prior it was synced
     * with the root token list's modCount.
     */
    public static final int MOD_COUNT_EMBEDDED_INITIAL = -3;

    public static void tokenLengthZeroOrNegative(int tokenLength) {
        if (tokenLength == 0) {
            throw new IllegalArgumentException(
                "Tokens with zero length are not supported by the framework." // NOI18N
              + " Fix the lexer." // NOI18N
            );
        } else { // tokenLength < 0
            throw new IllegalArgumentException(
                "Negative token length " + tokenLength // NOI18N
            );
        }
    }

    public static void throwFlyTokenProhibited() {
        throw new IllegalStateException("Flyweight token created but prohibited." // NOI18N
                + " Lexer needs to check lexerInput.isFlyTokenAllowed()."); // NOI18N
    }

    public static void throwBranchTokenFlyProhibited(AbstractToken token) {
        throw new IllegalStateException("Language embedding cannot be created" // NOI18N
                + " for flyweight token=" + token // NOI18N
                + "\nFix the lexer to not create flyweight token instance when"
                + " language embedding exists for the token."
        );
    }
    
    public static void checkValidBackup(int count, int maxCount) {
        if (count > maxCount) {
            throw new IndexOutOfBoundsException("Cannot backup " // NOI18N
                    + count + " characters. Maximum: " // NOI18N
                    + maxCount + '.');
        }
    }
    
    /**
     * Returns the most embedded language in the given language path.
     * <br/>
     * The method casts the resulting language to the generic type requested by the caller.
     */
    public static <T extends TokenId> Language<T> innerLanguage(LanguagePath languagePath) {
        @SuppressWarnings("unchecked")
        Language<T> l = (Language<T>)languagePath.innerLanguage();
        return l;
    }
    
    /**
     * Returns language hierarchy of the most embedded language in the given language path.
     * <br/>
     * The method casts the resulting language hierarchy to the generic type requested by the caller.
     */
    public static <T extends TokenId> LanguageHierarchy<T> innerLanguageHierarchy(LanguagePath languagePath) {
        Language<T> language = innerLanguage(languagePath);
        return LexerApiPackageAccessor.get().languageHierarchy(language);
    }
    
    /**
     * Returns language operation of the most embedded language in the given language path.
     * <br/>
     * The method casts the resulting language operation to the generic type requested by the caller.
     */
    public static <T extends TokenId> LanguageOperation<T> innerLanguageOperation(LanguagePath languagePath) {
        Language<T> language = innerLanguage(languagePath);
        return LexerApiPackageAccessor.get().languageOperation(language);
    }
    
    public static <T extends TokenId> LanguageOperation<T> languageOperation(Language<T> language) {
        return LexerApiPackageAccessor.get().languageOperation(language);
    }
    
    public static <T extends TokenId> LanguageHierarchy<T> languageHierarchy(Language<T> language) {
        return LexerApiPackageAccessor.get().languageHierarchy(language);
    }
    
    /**
     * Find the language embedding for the given parameters.
     * <br/>
     * First the <code>LanguageHierarchy.embedding()</code> method is queried
     * and if no embedding is found then the <code>LanguageProvider.findLanguageEmbedding()</code>.
     */
    public static <T extends TokenId> LanguageEmbedding<?>
    findEmbedding(LanguageHierarchy<T> languageHierarchy, AbstractToken<T> token,
    LanguagePath languagePath, InputAttributes inputAttributes) {
        LanguageEmbedding<?> embedding =
                LexerSpiPackageAccessor.get().embedding(
                languageHierarchy, token, languagePath, inputAttributes);

        if (embedding == null) {
            // try language embeddings registered in Lookup
            embedding = LanguageManager.getInstance().findLanguageEmbedding(
                    token, languagePath, inputAttributes);
        }
        
        return embedding;
    }
    
    public static int maxLanguagePathSize(Set<LanguagePath> paths) {
        int maxPathSize = 0;
        for (LanguagePath lp : paths) {
            maxPathSize = Math.max(lp.size(), maxPathSize);
        }
        return maxPathSize;
    }
    
    public static Object languageOrArrayAdd(Object languageOrArray, Language language) {
        if (languageOrArray == null) {
            return language;
        } else if (languageOrArray.getClass() == Language.class) {
            return (languageOrArray != language)
                    ? new Language[] { (Language) languageOrArray, language }
                    : languageOrArray; // Already contained
        } else {
            Language[] languageArray = (Language[]) languageOrArray;
            for (int i = languageArray.length - 1; i >= 0; i--) {
                if (languageArray[i] == language) { // Language is final
                    return languageOrArray; // Already contains
                }
            }
            Language[] ret = new Language[languageArray.length + 1];
            System.arraycopy(languageArray, 0, ret, 0, languageArray.length);
            ret[languageArray.length] = language;
            return ret;
        }
    }
    
    public static Object languageOrArrayRemove(Object languageOrArray, Language language) {
        if (languageOrArray == null) {
            return null;
        } else if (languageOrArray.getClass() == Language.class) {
            return (languageOrArray == language) ? null : languageOrArray;
        } else {
            Language[] languageArray = (Language[]) languageOrArray;
            for (int i = languageArray.length - 1; i >= 0; i--) {
                if (languageArray[i] == language) { // Language is final
                    Language[] ret = new Language[languageArray.length - 1];
                    System.arraycopy(languageArray, 0, ret, 0, i);
                    System.arraycopy(languageArray, i + 1, ret, i, languageArray.length - i - 1);
                    return ret;
                }
            }
            return languageOrArray; // Not contained
        }
    }
    
    public static int languageOrArraySize(Object languageOrArray) {
        if (languageOrArray == null) {
            return 0;
        } else if (languageOrArray.getClass() == Language.class) {
            return 1;
        } else {
            return ((Language[]) languageOrArray).length;
        }
    }
    
    public static boolean languageOrArrayContains(Object languageOrArray, Language language) {
        if (languageOrArray == null) {
            return false;
        } else if (languageOrArray.getClass() == Language.class) {
            return (Language) languageOrArray == language;
        } else {
            Language[] languageArray = (Language[]) languageOrArray;
            for (int i = languageArray.length - 1; i >= 0; i--) {
                if (languageArray[i] == language) { // Language is final
                    return true;
                }
            }
            return false;
        }
    }
    
    public static Language languageOrArrayGet(Object languageOrArray, int index) {
        if (languageOrArray != null) {
            if (languageOrArray.getClass() == Language.class) {
                if (index == 0) {
                    return (Language) languageOrArray;
                }
            } else {
                Language[] languageArray = (Language[]) languageOrArray;
                if (index >= 0 && index < languageArray.length) {
                    return languageArray[index];
                }
            }
        }
        throw new IndexOutOfBoundsException("Invalid index=" + index + // NOI18N
                ", length=" + languageOrArraySize(languageOrArray)); // NOI18N
    }
    
    /**
     * Get index of the token that "contains" the given offset.
     * If the offset is beyond the existing tokens the method asks
     * for next tokens by <code>tokenList.tokenOrEmbedding()</code>.
     * 
     * @param offset offset for which the token index should be found.
     * @return array of two items where the [0] is token's index and [1] is its offset.
     *  <br/>
     *  If offset &gt;= last-token-end-offset then [0] contains token-count and
     *  [1] conains last-token-end-offset.
     *  <br/>
     *  [0] may contain -1 to indicate that there are no tokens in the token list
     *  ([1] then contains zero).
     */
    public static int[] tokenIndexLazyTokenCreation(TokenList<?> tokenList, int offset) {
        // Token count in the list may change as possibly other threads
        // keep asking for tokens. Root token list impls create tokens lazily
        // when asked by clients.
        // The intent is to not force creation of all token (because of using a binary search)
        // so first a last token is checked whether it covers the requested offset.
        int tokenCount = tokenList.tokenCountCurrent(); // presently created token count
        if (tokenCount == 0) { // no tokens yet -> attempt to create at least one
            if (tokenList.tokenOrEmbedding(0) == null) { // really no tokens at all
                return new int[] { -1, 0 };
            }
            // Re-get the present token count (could be created a chunk of tokens at once)
            tokenCount = tokenList.tokenCountCurrent();
        }

        // tokenCount surely >0
        int prevTokenOffset = tokenList.tokenOffset(tokenCount - 1);
        if (offset > prevTokenOffset) { // may need to create further tokens if they do not exist
            // Force token list to create subsequent tokens
            // Cannot subtract offset by each token's length because
            // there may be gaps between tokens due to token id filter use.
            int tokenLength = tokenList.tokenOrEmbedding(tokenCount - 1).token().length();
            while (offset >= prevTokenOffset + tokenLength) { // above present token
                TokenOrEmbedding<?> tokenOrEmbedding = tokenList.tokenOrEmbedding(tokenCount);
                if (tokenOrEmbedding != null) {
                    AbstractToken<?> t = tokenOrEmbedding.token();
                    if (t.isFlyweight()) { // need to use previous tokenLength
                        prevTokenOffset += tokenLength;
                    } else { // non-flyweight token - retrieve offset
                        prevTokenOffset = tokenList.tokenOffset(tokenCount);
                    }
                    tokenLength = t.length();
                    tokenCount++;

                } else { // no more tokens => position behind last token
                    return new int[] { tokenCount, prevTokenOffset + tokenLength };
                }
            }
            return new int[] { tokenCount - 1, prevTokenOffset };
        }
        // Now do a regular binary search
        return tokenIndexBinSearch(tokenList, offset, tokenCount);
    }
    
    /**
     * Get index of the token that "contains" the given offset by using binary search
     * in existing tokens.
     * 
     * @param offset offset for which the token index should be found.
     * @return array of two items where the [0] is token's index and [1] is its offset.
     *  <br/>
     *  If offset &gt;= last-token-end-offset then [0] contains token-count and
     *  [1] conains last-token-end-offset.
     *  <br/>
     *  [0] may contain -1 to indicate that there are no tokens in the token list
     *  ([1] then contains zero).
     */
    public static int[] tokenIndexBinSearch(TokenList<?> tokenList, int offset, int tokenCount) {
        // The offset is within the currently recognized tokens
        // Use binary search
        int low = 0;
        int high = tokenCount - 1;
        int mid = -1;
        int midStartOffset = -1;
        while (low <= high) {
            mid = (low + high) >>> 1;
            midStartOffset = tokenList.tokenOffset(mid);
            
            if (midStartOffset < offset) {
                low = mid + 1;
            } else if (midStartOffset > offset) {
                high = mid - 1;
            } else {
                // Token starting exactly at offset found
                return new int[] { mid, midStartOffset}; // right at the token begining
            }
        }
        
        // Not found exactly and high + 1 == low => high < low
        // BTW there may be gaps between tokens; if offset is in gap then position to lower token
        if (high >= 0) { // could be -1
            if (low == tokenCount) { // Could be beyond end of last token
                AbstractToken<?> t = tokenList.tokenOrEmbedding(high).token();
                // Use current midStartOffset
                if (offset >= midStartOffset + t.length()) { // beyond end of last token
                    // Offset in the gap above the "high" token
                    high++;
                    midStartOffset += t.length();
                } else if (mid != high) {
                    midStartOffset = tokenList.tokenOffset(high);
                }
            } else if (mid != high) {
                midStartOffset = tokenList.tokenOffset(high);
            }
        } else { // high == -1 => mid == 0
            if (tokenCount == 0) { // Need to return -1
                return new int[] { -1, 0 };
            }
            high = 0;
            // Use current midStartOffset
        }
        return new int[] { high, midStartOffset };
    }

    public static int updatedStartOffset(EmbeddedTokenList<?,?> etl, int rootModCount, TokenHierarchyEventInfo eventInfo) {
        etl.updateModCount();
        int startOffset = etl.startOffset();
        return (etl.isRemoved() && startOffset > eventInfo.modOffset())
                ? Math.max(startOffset - eventInfo.removedLength(), eventInfo.modOffset())
                : startOffset;
    }

    public static <T extends TokenId> StringBuilder appendTokenList(StringBuilder sb, TokenList<T> tokenList) {
        return appendTokenList(sb, tokenList, -1, 0, Integer.MAX_VALUE, true, 0, true);
    }

    public static <T extends TokenId> StringBuilder appendTokenListIndented(
        StringBuilder sb, TokenList<T> tokenList, int indent
    ) {
        return appendTokenList(sb, tokenList, -1, 0, Integer.MAX_VALUE, false, indent, true);
    }

    public static <T extends TokenId> StringBuilder appendTokenList(StringBuilder sb,
            TokenList<T> tokenList, int currentIndex, int startIndex, int endIndex,
            boolean appendEmbedded, int indent, boolean dumpTokenText
    ) {
        if (sb == null) {
            sb = new StringBuilder(200);
        }
        TokenHierarchy<?> tokenHierarchy;
        if (tokenList instanceof SnapshotTokenList) {
            tokenHierarchy = ((SnapshotTokenList<T>)tokenList).snapshot().tokenHierarchy();
        } else {
            tokenHierarchy = null;
        }

        endIndex = Math.min(tokenList.tokenCountCurrent(), endIndex);
        int digitCount = ArrayUtilities.digitCount(endIndex - 1);
        for (int i = Math.max(startIndex, 0); i < endIndex; i++) {
            ArrayUtilities.appendSpaces(sb, indent);
            sb.append((i == currentIndex) ? '*' : 'T');
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            try {
                appendTokenInfo(sb, tokenList, i, tokenHierarchy,
                        appendEmbedded, indent, dumpTokenText);
            } catch (IndexOutOfBoundsException e) { // Fallback that allows to grab at least partial info
                sb.append("<IOOBE occurred!!!>\n");
                break; // Do not dump further info
            }
            sb.append('\n');
        }
        return sb;
    }
    
    public static boolean statesEqual(Object state1, Object state2) {
        return (state1 == null && state2 == null)
            || (state1 != null && state1.equals(state2));
    }
    
    public static String idToString(TokenId id) {
        return id.name() + '[' + id.ordinal() + ']'; // NOI18N;
    }
    
    public static <T extends TokenId> void appendTokenInfo(StringBuilder sb,
            TokenList<T> tokenList, int index,
            TokenHierarchy tokenHierarchy, boolean appendEmbedded, int indent,
            boolean dumpTokenText
    ) {
        appendTokenInfo(sb, tokenList.tokenOrEmbedding(index),
                tokenList.lookahead(index), tokenList.state(index),
                tokenHierarchy, appendEmbedded, indent, dumpTokenText);
    }

    public static <T extends TokenId> void appendTokenInfo(StringBuilder sb,
            TokenOrEmbedding<T> tokenOrEmbedding, int lookahead, Object state,
            TokenHierarchy<?> tokenHierarchy, boolean appendEmbedded, int indent,
            boolean dumpTokenText
    ) {
        if (tokenOrEmbedding == null) {
            sb.append("<NULL-TOKEN>");
        } else { // regular token
            EmbeddedTokenList<T, ?> etl = tokenOrEmbedding.embedding();
            AbstractToken<T> token = tokenOrEmbedding.token();
            token.dumpInfo(sb, tokenHierarchy, dumpTokenText, true, indent);
            appendLAState(sb, lookahead, state);
            sb.append(", ");
            appendIdentityHashCode(sb, token);

            // Check for embedding and if there is one dump it
            if (etl != null) {
                indent += 4;
                int index = 0;
                do {
                    sb.append('\n');
                    ArrayUtilities.appendSpaces(sb, indent);
                    sb.append("  Embedding[").append(index).append("]: \"");
                    sb.append(etl.languagePath().mimePath()).append("\", ");
                    LexerUtilsConstants.appendIdentityHashCode(sb, etl);
                    sb.append("\n");
                    if (appendEmbedded) {
                        appendTokenList(sb, etl, -1, 0, Integer.MAX_VALUE, appendEmbedded, indent, true);
                    }
                    etl = etl.nextEmbeddedTokenList();
                    index++;
                } while (etl != null);
            } 
        }
    }
    
    public static void appendIdentityHashCode(StringBuilder sb, Object o) {
        sb.append('@').append(Integer.toHexString(System.identityHashCode(o)));
    }
    
    public static void appendLAState(StringBuilder sb, TokenList<?> tokenList, int index) {
        appendLAState(sb, tokenList.lookahead(index), tokenList.state(index));
    }

    public static void appendLAState(StringBuilder sb, int lookahead, Object state) {
        if (lookahead > 0) {
            sb.append(", la=");
            sb.append(lookahead);
        }
        if (state != null) {
            sb.append(", st=");
            sb.append(state);
        }
    }

    public static String checkConsistencyTokenList(TokenList<?> tokenList, boolean checkEmbedded) {
        int tokenCountCurrent = tokenList.tokenCountCurrent();
        boolean continuous = tokenList.isContinuous();
        // To obtain up-to-date startOffset() a EC.updateStatus() needs to be called.
        // Of course this may affect a testing in case a missing EC.updateStatus()
        //   is a reason of failure.
        if (tokenList instanceof EmbeddedTokenList) {
            ((EmbeddedTokenList<?,?>)tokenList).updateModCount();
        }
        if (tokenList instanceof IncTokenList) {
            String error = ((IncTokenList<?>)tokenList).checkConsistency();
            if (error != null) {
                return error;
            }
        }
        
        int startOffset = tokenList.startOffset();
        int lastOffset = startOffset;
        for (int i = 0; i < tokenCountCurrent; i++) {
            TokenOrEmbedding<?> tokenOrEmbedding = tokenList.tokenOrEmbedding(i);
            if (tokenOrEmbedding == null) {
                tokenOrEmbedding = tokenList.tokenOrEmbedding(i); // Repeat operation (place bkpt here for debugging)
                return dumpContext("Null token", tokenList, i); // NOI18N
            }
            AbstractToken<?> token = tokenOrEmbedding.token();
            if (token.isRemoved()) {
                return dumpContext("Token is removed", tokenList, i); // NOI18N
            }
            // Check whether tokenList.startOffset() corresponds to the start of first token
            if (i == 0 && continuous && tokenCountCurrent > 0 && !token.isFlyweight()) {
                if (token.offset(null) != tokenList.startOffset()) {
                    return dumpContext("firstToken.offset()=" + token.offset(null) + // NOI18N
                            " != tokenList.startOffset()=" + tokenList.startOffset(), // NOI18N
                            tokenList, i);
                }
            }
            if (!token.isFlyweight() && token.tokenList() != tokenList && !(tokenList instanceof JoinTokenList)) {
                return dumpContext("Invalid token.tokenList()=" + token.tokenList(), // NOI18N
                        tokenList, i);
            }
            if (token.text() == null) {
                return dumpContext("Null token.text()", tokenList, i); // NOI18N
            }
            if (token.text().toString() == null) {
                return dumpContext("Null token.text().toString()", tokenList, i); // NOI18N
            }
            int offset = (token.isFlyweight()) ? lastOffset : token.offset(null);
            if (offset < 0) {
                return dumpContext("Token offset=" + offset + " < 0", tokenList, i); // NOI18N // NOI18N
            }
            if (offset < lastOffset) {
                return dumpContext("Token offset=" + offset + " < lastOffset=" + lastOffset, // NOI18N
                        tokenList, i);
            }
            if (offset > lastOffset && continuous) {
                return dumpContext("Gap between tokens; offset=" + offset + ", lastOffset=" + lastOffset, // NOI18N
                        tokenList, i);
            }
            lastOffset = offset + token.length();
            EmbeddedTokenList<?,?> etl = tokenOrEmbedding.embedding();
            if (etl != null && checkEmbedded) {
                while (etl != null) {
                    String error = checkConsistencyTokenList(etl, checkEmbedded);
                    if (error != null)
                        return error;
                    etl = etl.nextEmbeddedTokenList();
                }
            }
        }
        // Check that last offset ended at TL.endOffset() for continuous TLs
        if (tokenList instanceof MutableTokenList && ((MutableTokenList<?>)tokenList).isFullyLexed()) {
            int endOffset = tokenList.endOffset();
            // Check that non-empty continuous TL has tokens.
            // The check can't be applied to non-continuous TLs since e.g. a JTL consiting
            // of two ETLs <100,100> and <105,105> will contain no tokens.
            if (tokenList.isContinuous() && startOffset != endOffset && tokenCountCurrent == 0) {
                String msg = "Non-empty " + tokenList.dumpInfoType() + // NOI18N
                        " <" + startOffset + "," + endOffset + "> " + // NOI18N
                        " does not contain any tokens"; // NOI18N
                return dumpContext(msg, tokenList, 0); // NOI18N
            }
            if (continuous && lastOffset != endOffset) {
                return dumpContext("lastOffset=" + lastOffset + " != endOffset=" + endOffset, // NOI18N
                        tokenList, tokenCountCurrent);
            }
        }
        return null;
    }
    
    private static String dumpContext(String msg, TokenList<?> tokenList, int index) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        sb.append(" at index="); // NOI18N
        sb.append(index);
        sb.append(" of tokenList with ");
        appendIdentityHashCode(sb, tokenList);
        sb.append(" of language-path "); // NOI18N
        sb.append(tokenList.languagePath().mimePath());
        sb.append(", ").append(tokenList.getClass());
        sb.append('\n');
        LexerUtilsConstants.appendTokenList(sb, tokenList, index, index - 2, index + 3, false, 0, true);
        return sb.toString();
    }
    
    private LexerUtilsConstants() {
        // no instances
    }

}
