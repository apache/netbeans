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
package org.netbeans.modules.lsp.client.bindings.configuration;

import java.util.Map;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.lsp.client.spi.friend.LanguageConfiguration;
import org.netbeans.modules.lsp.client.spi.friend.LanguageConfiguration.CharacterPair;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

public class BracesMatcherFactoryImpl implements BracesMatcherFactory {

    @Override
    public BracesMatcher createMatcher(MatcherContext context) {
        String mimeType = NbEditorUtilities.getMimeType(context.getDocument());
        LanguageConfiguration lc = MimeLookup.getLookup(mimeType).lookup(LanguageConfiguration.class);
        if (lc != null && lc.brackets != null) {
            String[] bracketPairs = new String[2 * lc.brackets.length];
            int i = 0;
            for (CharacterPair p : lc.brackets) {
                bracketPairs[i++] = p.first;
                bracketPairs[i++] = p.second;
            }
            return new StringMatcher(context, -1, -1, bracketPairs);
        }
        return null;
    }

    //an adjusted copy of: ide/editor.bracesmatching/src/org/netbeans/spi/editor/bracesmatching/support/CharacterMatcher.java
    private static final class StringMatcher implements BracesMatcher {

        private static final Logger LOG = Logger.getLogger(StringMatcher.class.getName());

        private final MatcherContext context;
        private final String[] matchingPairs;
        private final int lowerBound;
        private final int upperBound;

        private int originOffset;
        private String originalBrace;
        private String matchingBrace;
        private boolean backward;

        public StringMatcher(MatcherContext context, int lowerBound, int upperBound, String... matchingPairs) {
            this.context = context;
            this.lowerBound = lowerBound == -1 ? Integer.MIN_VALUE : lowerBound;
            this.upperBound = upperBound == -1 ? Integer.MAX_VALUE : upperBound;

            assert matchingPairs.length % 2 == 0 : "The matchingPairs parameter must contain even number of Strings."; //NOI18N
            this.matchingPairs = matchingPairs;
        }

        // -----------------------------------------------------
        // BracesMatcher implementation
        // -----------------------------------------------------

        public int [] findOrigin() throws BadLocationException {
            ((AbstractDocument) context.getDocument()).readLock();
            try {
                int result [] = findChar(
                    context.getDocument(),
                    context.getSearchOffset(),
                    context.isSearchingBackward() ?
                        Math.max(context.getLimitOffset(), lowerBound) :
                        Math.min(context.getLimitOffset(), upperBound),
                    matchingPairs
                );

                if (result != null) {
                    originOffset = result[0];
                    originalBrace = matchingPairs[result[1]];
                    matchingBrace = matchingPairs[result[1] + result[2]];
                    backward = result[2] < 0;
                    return new int [] { originOffset, originOffset + originalBrace.length() };
                } else {
                    return null;
                }
            } finally {
                ((AbstractDocument) context.getDocument()).readUnlock();
            }
        }

        public int [] findMatches() throws BadLocationException {
            ((AbstractDocument) context.getDocument()).readLock();
            try {
                int offset = matchChar(
                    context.getDocument(),
                    backward ? originOffset : originOffset + 1,
                    backward ?
                        Math.max(0, lowerBound) :
                        Math.min(context.getDocument().getLength(), upperBound),
                    originalBrace,
                    matchingBrace
                );

                return offset != -1 ? new int [] { offset, offset + matchingBrace.length() } : null;
            } finally {
                ((AbstractDocument) context.getDocument()).readUnlock();
            }
        }
    }

    //copied and adjusted from: ide/editor.bracesmatching/src/org/netbeans/spi/editor/bracesmatching/support/BracesMatcherSupport.java
    public static int [] findChar(Document document, int offset, int limit, String... pairs) throws BadLocationException {
        assert pairs.length % 2 == 0 : "The pairs parameter must contain even number of String."; //NOI18N

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
                if (detectOrigin(result, text.array, text.offset + i, pairs)) {
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
                if (detectOrigin(result, text.array, text.offset + i, pairs)) {
                    result[0] = offset + i;
                    return result;
                }
            }
        }

        return null;
    }

    public static int matchChar(Document document, int offset, int limit, String origin, String matching) throws BadLocationException {
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
                if (matches(text.array, text.offset + i, origin)) {
                    count++;
                } else if (matches(text.array, text.offset + i, matching)) {
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
                if (matches(text.array, text.offset + i, origin)) {
                    count++;
                } else if (matches(text.array, text.offset + i, matching)) {
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

    private static boolean detectOrigin(int [] results, char[] text, int startOffset, String... pairs) {
        int cnt = pairs.length / 2;

        for(int idx = 0; idx < 2; idx++) {
            NEXT_CANDIDATE: for(int i = 0; i < cnt; i++) {
                int i2 = 2 * i + idx;

                if (matches(text, startOffset, pairs[i2])) {
                    results[1] = i2;
                    results[2] = idx == 0 ? 1 : -1;
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean matches(char[] text, int startOffset, String bracket) {
        //TODO: don't run out of text!
        //TODO: cancellation here as well?
        for (int bracketOffset = 0; bracketOffset < bracket.length(); bracketOffset++) {
            if (text[startOffset + bracketOffset] != bracket.charAt(bracketOffset)) {
                return false;
            }
        }

        return true;
    }
}
