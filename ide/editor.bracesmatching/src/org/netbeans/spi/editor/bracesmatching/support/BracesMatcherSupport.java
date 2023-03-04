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
package org.netbeans.spi.editor.bracesmatching.support;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 * Some useful implementations of <code>BracesMatcher</code>.
 * 
 * @author Vita Stejskal
 */
public final class BracesMatcherSupport {

    private static final char [] DEFAULT_CHARS = new char [] { '(', ')', '[', ']', '{', '}', '<', '>' }; //NOI18N
   
    /**
     * Creates the default <code>BracesMatcher</code> implementation. The default
     * matcher is used when no other matcher is available. The default matcher
     * is basically a character matcher, which looks for the following character
     * pairs: <code>'(', ')', '[', ']', '{', '}', '&lt;', '&gt;'</code>.
     * 
     * @param context The context for the matcher.
     * @param lowerBound The start offset of the area where the created matcher
     *   should search. Can be <code>-1</code> for no restriction.
     * @param upperBound The end offset of the area where the crated matcher
     *   should search. Can be <code>-1</code> for no restriction.
     * 
     * @return The default matcher.
     * @see #characterMatcher
     */
    public static BracesMatcher defaultMatcher(MatcherContext context, int lowerBound, int upperBound) {
        return new CharacterMatcher(context, lowerBound, upperBound, DEFAULT_CHARS);
    }
    
    /**
     * Creates <code>BracesMatcher</code> for finding character pairs.
     * 
     * <p>The character matcher looks for characters passed in as an
     * array of paired characters that match each other. Any character from
     * the array can be detected as the original character (area). The other
     * character from the pair will then be used to search for the matching area.
     * 
     * <p>The characters in each pair have to be listed in a specific order.
     * The order determines where the matching character should lay in text
     * relatively to the position of the original character. When the first character
     * is detected as the original character the matcher will search for the
     * matching character (ie. the second character from the pair) in the forward
     * direction (ie. towards the end of a document). Similarily when the second
     * character is detected as the original character the matcher will search
     * for the matching character (ie. the first character in the pair) in the
     * backward direction towards the beginning of a document.
     * 
     * <p>In other words each pair should contain the 'opening' character first
     * and the 'closing' character second. For example, when searching for curely
     * braces they should be listed in the following order
     * <code>char [] braces = new char [] { '{', '}' }</code>.
     * 
     * <p>The created matcher can be further restricted to search in a certain
     * area only. This might be useful for restricting the search to a particular
     * lexical token in text (eg. a string literal, javadoc comment, etc.).
     * 
     * @param context The context for the matcher.
     * @param lowerBound The start offset of the area where the created matcher
     *   should search. Can be <code>-1</code> for no restriction.
     * @param upperBound The end offset of the area where the crated matcher
     *   should search. Can be <code>-1</code> for no restriction.
     * @param matchingPairs The array with pairs of matching characters. There
     *   should always be an even number of elements in the array.
     * 
     * @return The character matcher.
     */
    public static BracesMatcher characterMatcher(MatcherContext context, int lowerBound, int upperBound, char... matchingPairs) {
        return new CharacterMatcher(context, lowerBound, upperBound, matchingPairs);
    }
    
    /**
     * Finds a character in a document. This methods will scan a document area between
     * the <code>offset</code> and <code>limit</code> offsets to search for characters
     * passed in the <code>pairs</code> parameter.
     * 
     * <p>The <code>offset</code> parameter determines the position in the document
     * where searching should start. The method will search from this position
     * towards the position specified by the <code>limit</code> parameter. That means
     * that if <code>limit &lt; offset</code> the search will be done in the backward
     * direction; while if <code>limit &gt; offset</code> the method will search
     * in the forward direction.
     * 
     * <p>The pairs array should always contain an even number of chacters that
     * match each other, eg. <code>char [] { '(', ')' }</code>. It is recommended
     * to pass the 'opening' character first and the 'closing' character second.
     * 
     * <p>If some of the <code>pairs</code> characters is found in the specified
     * area of the document, the method will return an array of exactly three
     * integers. The numbers returned have the following meaning.
     * 
     * <ul>
     * <li><code>int[0]</code> - offset in the document where the character
     * was found.
     * <li><code>int[1]</code> - index in the <code>pairs</code> array of the
     * character that was found.
     * <li><code>int[2]</code> - flag, indicating whether the search for the
     * matching character should be done bacwkard or forward from the offset returned
     * in <code>int[0]</code>. The value of this flag is either <code>-1</code>
     * for bacward search or <code>+1</code> for forward search. The value can
     * also be used for determining the index of the matching character in the
     * <code>pairs</code> array simply by adding it to <code>int[1]</code>.
     * </ul>
     * 
     * <div class="nonnormative">
     * The code below demonstrates how the original and the matching characters
     * and the direction in which the matching character should lay can be determined
     * from the return value.
     * 
     * <pre>
     * int offset = result[0];
     * char original = pairs[result[1]];
     * char matching = pairs[result[1] + result[2]];
     * boolean backward = result[2] &lt; 0;
     * </pre>
     * </div>
     * 
     * @param document The document to scan.
     * @param offset The offset in the document to start searching at.
     * @param limit The offset in the document to search towards.
     * @param pairs The pairs of matching characters to search for.
     * 
     * @return The search results as an array of three integers or <code>null</code>
     *   if none of the <code>pairs</code> characters was found in the specified
     *   area of the document.
     * @throws javax.swing.text.BadLocationException If the offsets are incorrect.
     */
    public static int [] findChar(Document document, int offset, int limit, char... pairs) throws BadLocationException {
        assert pairs.length % 2 == 0 : "The pairs parameter must contain even number of characters."; //NOI18N
        
        boolean backward = limit < offset;
        int lookahead = backward ? offset - limit : limit - offset;
        int [] result = new int [3];
        
        if (backward) {
            // check the character at the left from the caret
            Segment text = new Segment();
            document.getText(offset - lookahead, lookahead, text);

            for(int i = lookahead - 1; i >= 0; i--) {
                if (MatcherContext.isTaskCanceled()) {
                    return null;
                }
                if (detectOrigin(result, text.array[text.offset + i], pairs)) {
                    result[0] = offset - (lookahead - i);
                    return result;
                }
            }
        } else {
            // check the character at the right from the caret
            Segment text = new Segment();
            document.getText(offset, lookahead, text);

            for(int i = 0 ; i < lookahead; i++) {
                if (MatcherContext.isTaskCanceled()) {
                    return null;
                }
                if (detectOrigin(result, text.array[text.offset + i], pairs)) {
                    result[0] = offset + i;
                    return result;
                }
            }
        }
        
        return null;
    }

    /**
     * Searches for a matching character. This method will search <code>document</code>
     * in the area between <code>offset</code> and <code>limit</code> for
     * the <code>matching</code> character. The method will automatically skip
     * any additional pairs of <code>original</code> - <code>matching</code> characters
     * in the searched area.
     * 
     * <p>The <code>offset</code> parameter determines the position in the document
     * where searching should start. The method will search from this position
     * towards the position specified by the <code>limit</code> parameter. That means
     * that if <code>limit &lt; offset</code> the search will be done in the backward
     * direction; while if <code>limit &gt; offset</code> the method will search
     * in the forward direction.
     * 
     * <div class="nonnormative">This method in combination with <code>findChar</code>
     * can be used for creating a character matcher as demonstrated below.
     * 
     * <pre>
     * int originOffset;
     * char originalChar;
     * char matchingChar;
     * boolean backward;
     * 
     * int [] findOrigin() {
     *   int result[] = findChar(doc, offset, limit, PAIRS);
     *   if (result != null) {
     *     originOffset = result[0];
     *     originalChar = PAIRS[result[1]];
     *     matchingChar = PAIRS[result[1] + result[2]];
     *     backward = result[2] &lt; 0;
     * 
     *     return new int [] { originOffset, originOffset + 1 };
     *   } else {
     *     return null;
     *   }
     * }
     * 
     * int [] findMatches() {
     *   int offset = matchCharacter(
     *     doc, 
     *     backward ? originOffset : originOffset + 1, 
     *     backward ? 0 : doc.getLength(), 
     *     originalChar, 
     *     matchingChar);
     * 
     *   return offset != -1 ? new int [] { offset, offset + 1 } : null;
     * }
     * </pre>
     * </div>
     * 
     * @param document The document to search in.
     * @param offset The offset in the document to start seacrhing at.
     * @param limit The offset in the document to search towards.
     * @param origin The original character.
     * @param matching The matching character. This is the character we are searching for.
     * 
     * @return The offset of the matching character or <code>-1</code> if the matching
     *   character can't be found in the specified area of the document.
     * @throws javax.swing.text.BadLocationException If the offsets are invalid.
     */
    public static int matchChar(Document document, int offset, int limit, char origin, char matching) throws BadLocationException {
        boolean backward = limit < offset;
        int lookahead = backward ? offset - limit : limit - offset;
        
        if (backward) {
            // check the character at the left from the caret
            Segment text = new Segment();
            document.getText(offset - lookahead, lookahead, text);

            int count = 0;
            for(int i = lookahead - 1; i >= 0; i--) {
                if (MatcherContext.isTaskCanceled()) {
                    return -1;
                }
                if (origin == text.array[text.offset + i]) {
                    count++;
                } else if (matching == text.array[text.offset + i]) {
                    if (count == 0) {
                        return offset - (lookahead - i);
                    } else {
                        count--;
                    }
                }
            }
        } else {
            // check the character at the right from the caret
            Segment text = new Segment();
            document.getText(offset, lookahead, text);

            int count = 0;
            for(int i = 0 ; i < lookahead; i++) {
                if (MatcherContext.isTaskCanceled()) {
                    return -1;
                }
                if (origin == text.array[text.offset + i]) {
                    count++;
                } else if (matching == text.array[text.offset + i]) {
                    if (count == 0) {
                        return offset + i;
                    } else {
                        count--;
                    }
                }
            }
        }
        
        return -1;
    }
    
    // -----------------------------------------------------
    // private implementation
    // -----------------------------------------------------
    
    private static boolean detectOrigin(int [] results, char ch, char... pairs) {
        int cnt = pairs.length / 2;
        
        for(int idx = 0; idx < 2; idx++) {
            for(int i = 0; i < cnt; i++) {
                int i2 = 2 * i + idx;
                
                if (ch == pairs[i2]) {
                    results[1] = i2;
                    results[2] = idx == 0 ? 1 : -1;
                    return true;
                }
            }
        }        
        
        return false;
    }
    
    // Used from the layer
    private static BracesMatcherFactory defaultMatcherFactory() {
        return new BracesMatcherFactory() {
            public BracesMatcher createMatcher(MatcherContext context) {
                return defaultMatcher(context, -1, -1);
            }
        };
    }

    // Preventing instantiation
    private BracesMatcherSupport() {
    }
}
