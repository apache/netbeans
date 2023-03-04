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

package org.netbeans.modules.editor.document;

import javax.swing.text.BadLocationException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.editor.document.implspi.CharClassifier;

/**
 * Search the text for various properties.
 *
 * @author Miloslav Metelka
 */
public class TextSearchUtils {
    
    public static final CharClassifier DEFAULT_CLASSIFIER = new CharClassifier() {

        @Override
        public boolean isIdentifierPart(char ch) {
            return Character.isLetterOrDigit(ch);
        }

        @Override
        public boolean isWhitespace(char ch) {
            return Character.isWhitespace(ch);
        }
    };
    
    private TextSearchUtils() {
        // No instances
    }

    public static int getWordStart(@NonNull CharSequence text, @NonNull CharClassifier classifier, int offset)
    throws BadLocationException {
        int limitOffset = 0;
        boolean inIdentifier = false;
        boolean inPunct = false;
        for (int i = offset - 1; i >= limitOffset; i--) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                // If first char skip right below it
                return (i == offset - 1) ? i : i + 1;
            }

            if (classifier.isWhitespace(ch)) {
                if (inIdentifier || inPunct) {
                    return i + 1;
                }
            } else { // non-WS char
                boolean identifierChar = classifier.isIdentifierPart(ch);
                if (inIdentifier) {
                    if (!identifierChar) { // Start of ident
                        return i + 1;
                    }
                }
                if (inPunct) {
                    if (identifierChar) { // Identifier after punct
                        return i + 1;
                    }
                }
                if (identifierChar) {
                    inIdentifier = true; // might be in WS as well but does not matter
                } else {
                    inPunct = true;
                }
            }
        }
        return limitOffset;
    }

    public static int getWordEnd(@NonNull CharSequence text, @NonNull CharClassifier classifier, int offset) {
        int limitOffset = text.length();
        boolean inWhitespace = false;
        boolean inIdentifier = false;
        boolean inPunct = false;
        for (int i = offset; i < limitOffset; i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                // If first char and offset is not at EOF skip right above it
                return (i == offset && i + 1 < limitOffset) ? i + 1 : i;
            }
            if (classifier.isWhitespace(ch)) {
                if (inIdentifier) { // End of identifier
                    return i;
                }
                inWhitespace = true;
            } else {
                if (inWhitespace) { // non-WS char after WS
                    return i;
                }
                boolean identifierChar = classifier.isIdentifierPart(ch);
                if (inIdentifier) {
                    if (!identifierChar) { // End of ident
                        return i;
                    }
                }
                if (inPunct) {
                    if (identifierChar) { // Identifier after punct
                        return i;
                    }
                }
                if (identifierChar) {
                    inIdentifier = true;
                } else {
                    inPunct = true;
                }
            }
        }
        return limitOffset;
    }

    /**
     * Get the word at given offset.
     * @param doc document to operate on
     * @param wordStartOffset offset of word start.
     * @return word starting at offset.
     */
    public static CharSequence getWord(@NonNull CharSequence text, @NonNull CharClassifier classifier, int wordStartOffset)
    throws BadLocationException
    {
        int wordEnd = getWordEnd(text, classifier, wordStartOffset);
        return text.subSequence(wordStartOffset, wordEnd);
    }

    public static int getNextWordStart(@NonNull CharSequence text, @NonNull CharClassifier classifier, int offset) {
        int limitOffset = text.length();
        boolean inWhitespace = false;
        boolean inIdentifier = false;
        boolean inPunct = false;
        for (int i = offset; i < limitOffset; i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                return (i != offset) ? i : i + 1;
            }
            if (classifier.isWhitespace(ch)) {
                inWhitespace = true; // Skip WS after identifier
            } else {
                if (inWhitespace) { // non-WS char after WS
                    return i;
                }
                boolean identifierChar = classifier.isIdentifierPart(ch);
                if (inIdentifier) {
                    if (!identifierChar) { // End of ident
                        return i;
                    }
                }
                if (inPunct) {
                    if (identifierChar) { // Identifier after punct
                        return i;
                    }
                }
                if (identifierChar) {
                    inIdentifier = true;
                } else {
                    inPunct = true;
                }
            }
        }
        return limitOffset;
    }

    public static int getPreviousWordEnd(@NonNull CharSequence text, @NonNull CharClassifier classifier, int offset) {
        int limitOffset = 0;
        boolean inWhitespace = false;
        boolean inIdentifier = false;
        boolean inPunct = false;
        for (int i = offset - 1; i >= limitOffset; i--) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                // If first char skip right below it
                return (i == offset - 1) ? i : i + 1;
            }
            if (classifier.isWhitespace(ch)) {
                inWhitespace = true; // Current impl skips WS after identifier
            } else {
                if (inWhitespace) { // non-WS char after WS
                    return i + 1;
                }
                boolean identifierChar = classifier.isIdentifierPart(ch);
                if (inIdentifier) {
                    if (!identifierChar) { // Start of ident
                        return i + 1;
                    }
                }
                if (inPunct) {
                    if (identifierChar) { // Identifier after punct
                        return i + 1;
                    }
                }
                if (identifierChar) {
                    inIdentifier = true;
                } else {
                    inPunct = true;
                }
            }
        }
        return limitOffset;
    }

    /**
     * Get start of a previous word.
     * 
     * @param text non-null text to search.
     * @param classifier non-null character classifier.
     * @param offset >= 0 offset in text.
     * @return previous word start offset.
     * @since 1.4
     */
    public static int getPreviousWordStart(@NonNull CharSequence text, @NonNull CharClassifier classifier, int offset) {
        int limitOffset = 0;
        boolean inWhitespace = false;
        boolean inIdentifier = false;
        boolean inPunct = false;
        for (int i = offset - 1; i >= limitOffset; i--) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                // If first char skip right below it
                return (i == offset - 1) ? i : i + 1;
            }
            if (classifier.isWhitespace(ch)) {
                if (inIdentifier || inPunct) {
                    return i + 1;
                }
                inWhitespace = true; // Current impl skips WS after identifier
            } else {
                boolean identifierChar = classifier.isIdentifierPart(ch);
                if (inWhitespace) { // non-WS char in front of WS
                    inWhitespace = false;
                    if (identifierChar) {
                        // Search for identifier start
                        inIdentifier = true;
                        continue;
                    } else {
                        inPunct = true;
                        continue;
                    }
                }
                if (inIdentifier) {
                    if (!identifierChar) { // Start of ident
                        return i + 1;
                    }
                }
                if (inPunct) {
                    if (identifierChar) { // Identifier after punct
                        return i + 1;
                    }
                }
                if (identifierChar) {
                    inIdentifier = true;
                } else {
                    inPunct = true;
                }
            }
        }
        return limitOffset;
    }

    /**
     * Get first whitespace character in text in forward direction.
     *
     * @param text text to operate on.
     * @param offset offset of first character to examine for WS.
     * @param limitOffset offset above the last character to examine for WS.
     * @return offset of the next WS character or -1 if not found.
     */
    public static int getNextWhitespace(@NonNull CharSequence text, @NonNull CharClassifier classifier, int offset, int limitOffset) {
        for (int i = offset; i < limitOffset; i++) {
            char ch = text.charAt(i);
            if (classifier.isWhitespace(ch)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get first whitespace character in text in backward direction.
     *
     * @param text text to operate on.
     * @param offset offset above first character to examine for WS.
     * @param limitOffset offset of the last character (in backward direction) to examine for WS.
     * @return offset of the previous WS character or -1 if not found.
     */
    public static int getPreviousWhitespace(@NonNull CharSequence text, @NonNull CharClassifier classifier, int offset, int limitOffset) {
        for (int i = offset - 1; i >= limitOffset; i--) {
            char ch = text.charAt(i);
            if (classifier.isWhitespace(ch)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Get first non-whitespace character in text in forward direction.
     *
     * @param text text to operate on.
     * @param offset offset of first character to examine for WS.
     * @param limitOffset offset above the last character to examine for WS.
     * @return offset of the next non-WS character or -1 if not found.
     */
    public static int getNextNonWhitespace(@NonNull CharSequence text, @NonNull CharClassifier classifier, int offset, int limitOffset) {
        for (int i = offset; i < limitOffset; i++) {
            char ch = text.charAt(i);
            if (!classifier.isWhitespace(ch)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get first non-whitespace character in text in backward direction.
     *
     * @param text text to operate on.
     * @param offset offset above first character to examine for WS.
     * @param limitOffset offset of the last character (in backward direction) to examine for WS.
     * @return offset of the previous non-WS character or -1 if not found.
     */
    public static int getPreviousNonWhitespace(@NonNull CharSequence text, @NonNull CharClassifier classifier, int offset, int limitOffset) {
        for (int i = offset - 1; i >= limitOffset; i--) {
            char ch = text.charAt(i);
            if (!classifier.isWhitespace(ch)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get offset of first non-newline character in forward direction.
     *
     * @param text text to operate on.
     * @param offset offset of first character to examine.
     * @param limitOffset offset above the last character to examine.
     * @return offset of the next non-newline character or -1 if not found.
     */
    public static int getNextNonNewline(@NonNull CharSequence text, int offset, int limitOffset) {
        for (int i = offset; i < limitOffset; i++) {
            if (text.charAt(i) != '\n') {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get offset of first non-newline character in backward direction.
     *
     * @param text text to operate on.
     * @param offset offset above first character to examine.
     * @param limitOffset offset of the last character (in backward direction) to examine.
     * @return offset of the previous non-WS character or -1 if not found.
     */
    public static int getPreviousNonNewline(@NonNull CharSequence text, int offset, int limitOffset) {
        for (int i = offset - 1; i >= limitOffset; i--) {
            if (text.charAt(i) != '\n') {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Check if line at offset is empty.
     *
     * @param text text to operate on.
     * @param offset offset anywhere on the line.
     * @return true if line contains just newline.
     */
    public static boolean isLineEmpty(@NonNull CharSequence text, int offset) {
        if (text.charAt(offset) != '\n') {
            return false;
        }
        if (offset > 0 && text.charAt(offset - 1) != '\n') {
            return false;
        }
        return true;
    }
    
}
