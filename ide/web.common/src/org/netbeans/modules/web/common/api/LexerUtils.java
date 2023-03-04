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
package org.netbeans.modules.web.common.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.util.Mutex;
import org.openide.util.Parameters;

/**
 *
 * @author marekfukala
 */
public class LexerUtils {
    
     /**
     * Forces to rebuild the document's {@link TokenHierarchy}.
     * 
     * @since 1.62
     * 
     * @param doc a swing document
     */
    public  static void rebuildTokenHierarchy(final Document doc) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                AtomicLockDocument nbdoc = LineDocumentUtils.as(doc, AtomicLockDocument.class);
                Runnable rebuild = new Runnable() {
                    @Override
                    public void run() {
                        MutableTextInput mti = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                        if (mti != null) {
                            mti.tokenHierarchyControl().rebuild();
                        }
                    }
                };
                if (nbdoc != null) {
                    nbdoc.runAtomic(rebuild);
                } else {
                    rebuild.run();
                }
            }
        });
    }

    /**
     * Note: The input text must contain only \n as line terminators. This is
     * compatible with the netbeans document which never contains \r\n line
     * separators.
     *
     * @param text
     * @param offset
     * @return line offset, starting with zero.
     */
    public static int getLineOffset(CharSequence text, int offset) throws BadLocationException {
        if (text == null) {
            throw new NullPointerException();
        }

        if (offset < 0 || offset > text.length()) {
            throw new BadLocationException("The given offset is out of bounds <0, " + text.length() + ">", offset); //NOI18N
        }
        int line = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\r') {
                throw new IllegalArgumentException("The input text cannot contain carriage return char \\r"); //NOI18N
            }
            if (i == offset) {
                return line;
            }
            if (c == '\n') {
                line++;
            }
        }

        //for position just at the length of the text
        return line;
    }

    /**
     * Note: The input text must contain only \n as line terminators. This is
     * compatible with the netbeans document which never contains \r\n line
     * separators.
     *
     * @param text
     * @param line line number
     * @return offset of the beginning of the line
     */
    public static int getLineBeginningOffset(CharSequence text, int line) throws BadLocationException {
        if (text == null) {
            throw new NullPointerException();
        }

        if (line < 0) {
            throw new IllegalArgumentException("Line number must be >= 0!");
        }
        int linecount = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\r') {
                throw new IllegalArgumentException("The input text cannot contain carriage return char \\r"); //NOI18N
            }
            if (linecount == line) {
                return i;
            }
            if (c == '\n') {
                linecount++;
            }
        }

        //for position just at the length of the text
        return text.length();
    }
    
    /**
     * Finds line beginning and end in the given {@link CharSequence}
     * 
     * @since 1.63
     * 
     * @param text the input text
     * @param offset offset in the input text
     * @return two item array containing line start/end offsets or null if the position is invalid.
     */
    public static int[] findLineBoundaries(CharSequence text, int offset) {
        int l = text.length();
        if (offset == -1 || offset > l) {
            return null;
        }
        // the position is at the end of file, after a newline.
        if (offset == l && l >= 1 && text.charAt(l - 1) == '\n') {
            return new int[]{l - 1, l};
        }
        int min = offset;
        while (min > 1 && text.charAt(min - 1) != '\n') {
            min--;
        }
        int max = offset;
        while (max < l && text.charAt(max) != '\n') {
            max++;
        }
        return new int[]{min, max};
    }

    public static Token followsToken(TokenSequence ts, TokenId searchedId, boolean backwards, boolean repositionBack, TokenId... skipIds) {
        return followsToken(ts, Collections.singletonList(searchedId), backwards, repositionBack, skipIds);
    }

    public static Token followsToken(TokenSequence ts, Collection<? extends TokenId> searchedIds, boolean backwards, boolean repositionBack, TokenId... skipIds) {
        return followsToken(ts, searchedIds, backwards, repositionBack, false, skipIds);
    }

    /**
     * Checks if the {@link TokenSequence} contains a specific token in a
     * choosen direction from the current token sequence index.
     *
     * @since 1.56
     *
     * @param ts the token sequence to operate on
     * @param searchedIds list of the searched token ids
     * @param backwards should the tokens be searched backwards (true) or
     * forward (false).
     * @param repositionBack repositions the token sequence to the original
     * index if set to true
     * @param includeCurrentToken if true the current token is also taken into
     * account when searching for the tokens
     * @param skipIds list of token ids which should be skipped when searching.
     * Any other token ids will break the search.
     * @return token of the type from the searchedIds list or null if either no
     * token is found or there's an unexpected token type in the search
     * direction.
     */
    public static Token followsToken(TokenSequence ts, Collection<? extends TokenId> searchedIds,
            boolean backwards, boolean repositionBack, boolean includeCurrentToken,
            TokenId... skipIds) {
        Collection<TokenId> skip = Arrays.asList(skipIds);
        int index = ts.index();
        //if the current token is to be included, then do not move to next/previous token in the first loop
        try {
            while (includeCurrentToken || (backwards ? ts.movePrevious() : ts.moveNext())) {
                includeCurrentToken = false; //disable the flag after first loop
                Token token = ts.token();
                TokenId id = token.id();
                if (searchedIds.contains(id)) {
                    return token;
                }
                if (!skip.contains(id)) {
                    break;
                }
            }
        } finally {
            if (repositionBack) {
                int idx = ts.moveIndex(index);
                boolean moved = ts.moveNext();

                assert idx == 0 && moved;

            }
        }

        return null;
    }

    /**
     * returns top most joined html token seuence for the document at the
     * specified offset.
     */
    public static TokenSequence getJoinedTokenSequence(Document doc, int offset, Language language) {
        return getTokenSequence(doc, offset, language, true);
    }

    public static TokenSequence getTokenSequence(Document doc, int offset, Language language, boolean joined) {
        return getTokenSequence(TokenHierarchy.get(doc), offset, language, joined);
    }

    /**
     * Gets instance of {@link TokenSequence} for the given
     * {@link TokenHierarchy} and offset.
     *
     * @since 1.55
     * @param th
     * @param offset
     * @param language
     * @param joined
     * @return
     */
    public static TokenSequence getTokenSequence(TokenHierarchy th, int offset, Language language, boolean joined) {
        TokenSequence ts = th.tokenSequence();
        if (ts == null) {
            return null;
        }
        ts.move(offset);

        while (ts.moveNext() || ts.movePrevious()) {
            if (ts.language() == language) {
                return ts;
            }

            ts = ts.embeddedJoined();

            if (ts == null) {
                break;
            }

            //position the embedded ts so we can search deeper
            ts.move(offset);
        }

        return null;

    }

    /**
     * Trims the given {@link CharSequence} as {@link String#trim()} does.
     *
     * @since 1.26
     */
    public static CharSequence trim(CharSequence chs) {
        if (chs == null) {
            throw new NullPointerException();
        }
        if (chs.length() == 0) {
            return chs;
        }

        int wsPrefixLen = 0;
        for (int i = 0; i < chs.length(); i++) {
            char c = chs.charAt(i);
            if (Character.isWhitespace(c)) {
                wsPrefixLen++;
            } else {
                break;
            }
        }
        int wsPostfixLen = 0;
        for (int i = chs.length() - 1; i >= wsPrefixLen; i--) {
            char c = chs.charAt(i);
            if (Character.isWhitespace(c)) {
                wsPostfixLen++;
            } else {
                break;
            }
        }

        return chs.subSequence(wsPrefixLen, chs.length() - wsPostfixLen);

    }

    /**
     * @param optimized - first sequence is lowercase, one call to
     * Character.toLowerCase() only
     */
    public static boolean equals(CharSequence text1, CharSequence text2, boolean ignoreCase, boolean optimized) {
        Parameters.notNull("text1", text1);
        Parameters.notNull("text2", text2);
        if (text1.length() != text2.length()) {
            return false;
        } else {
            //compare content
            for (int i = 0; i < text1.length(); i++) {
                char ch1 = ignoreCase && !optimized ? Character.toLowerCase(text1.charAt(i)) : text1.charAt(i);
                char ch2 = ignoreCase ? Character.toLowerCase(text2.charAt(i)) : text2.charAt(i);
                if (ch1 != ch2) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * @param optimized - first sequence is lowercase, one call to
     * Character.toLowerCase() only
     */
    public static boolean startsWith(CharSequence text1, CharSequence prefix, boolean ignoreCase, boolean optimized) {
        if (text1.length() < prefix.length()) {
            return false;
        } else {
            return equals(text1.subSequence(0, prefix.length()), prefix, ignoreCase, optimized);
        }
    }

    /**
     * @since 1.21
     * @param optimized - first sequence is lowercase, one call to
     * Character.toLowerCase() only
     */
    public static boolean endsWith(CharSequence text1, CharSequence prefix, boolean ignoreCase, boolean optimized) {
        if (text1.length() < prefix.length()) {
            return false;
        } else {
            return equals(text1.subSequence(text1.length() - prefix.length(), text1.length()), prefix, ignoreCase, optimized);
        }
    }

}
