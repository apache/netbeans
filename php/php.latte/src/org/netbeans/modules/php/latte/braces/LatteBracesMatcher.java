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

package org.netbeans.modules.php.latte.braces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.latte.lexer.LatteMarkupTokenId;
import org.netbeans.modules.php.latte.lexer.LatteTopTokenId;
import org.netbeans.modules.php.latte.utils.LatteLexerUtils;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteBracesMatcher implements BracesMatcher {
    private static final List<Matcher> MATCHERS = new ArrayList<>();
    static {
        MATCHERS.add(new StartEndMacroMatcher("define")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("ifCurrent")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("for")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("foreach")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("while")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("first")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("last")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("sep")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("capture")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("cache")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("syntax")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("_")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("block")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("form")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("label")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("snippet")); //NOI18N
        MATCHERS.add(new StartEndMacroMatcher("translate")); //NOI18N
        MATCHERS.add(new IfMatcher());
        MATCHERS.add(new IfsetMatcher());
        MATCHERS.add(new EndIfMatcher());
        MATCHERS.add(new EndIfsetMatcher());
        MATCHERS.add(new ElseMatcher());
        MATCHERS.add(new ElseIfMatcher());
        MATCHERS.add(new ElseIfsetMatcher());
    }
    private final MatcherContext context;

    private LatteBracesMatcher(MatcherContext context) {
        this.context = context;
    }

    @Override
    public int[] findOrigin() throws InterruptedException, BadLocationException {
        int[] result = null;
        BaseDocument document = (BaseDocument) context.getDocument();
        document.readLock();
        try {
            result = findOriginUnderLock();
        } finally {
            document.readUnlock();
        }
        return result;
    }

    private int[] findOriginUnderLock() {
        int[] result = null;
        TokenSequence<? extends LatteMarkupTokenId> ts = LatteLexerUtils.getLatteMarkupTokenSequence(context.getDocument(), context.getSearchOffset());
        if (ts != null) {
            result = findOriginInSequence(ts);
        }
        return result;
    }

    private int[] findOriginInSequence(TokenSequence<? extends LatteMarkupTokenId> ts) {
        int[] result = null;
        ts.move(context.getSearchOffset());
        if (ts.moveNext()) {
            Token<? extends LatteMarkupTokenId> currentToken = ts.token();
            if (currentToken != null) {
                LatteMarkupTokenId currentTokenId = currentToken.id();
                if ((currentTokenId == LatteMarkupTokenId.T_MACRO_START || currentTokenId == LatteMarkupTokenId.T_MACRO_END)
                        && !isUnderscoreTranslationTag(ts)) {
                    result = new int[] {ts.offset(), ts.offset() + currentToken.length()};
                }
            }
        }
        return result;
    }

    private boolean isUnderscoreTranslationTag(TokenSequence<? extends LatteMarkupTokenId> ts) {
        // check {_'text'}
        // see: https://latte.nette.org/en/tags#toc-translation
        int originalOffset = ts.offset();
        boolean isTranslationTag = false;
        if (TokenUtilities.textEquals("_", ts.token().text())) { // NOI18N
            while(ts.moveNext()) {
                Token<? extends LatteMarkupTokenId> token = ts.token();
                if (token == null) {
                    break;
                }
                if (token.id() == LatteMarkupTokenId.T_WHITESPACE) {
                    continue;
                }
                if (token.id() == LatteMarkupTokenId.T_STRING) {
                    isTranslationTag = true;
                }
                break;
            }
        }
        ts.move(originalOffset);
        ts.moveNext();
        return isTranslationTag;
    }

    @Override
    public int[] findMatches() throws InterruptedException, BadLocationException {
        int[] result = null;
        BaseDocument document = (BaseDocument) context.getDocument();
        document.readLock();
        try {
            result = findMatchesUnderLock();
        } finally {
            document.readUnlock();
        }
        return result;
    }

    private int[] findMatchesUnderLock() {
        int[] result = null;
        TokenSequence<? extends LatteTopTokenId> topTs = LatteLexerUtils.getLatteTopTokenSequence(context.getDocument(), context.getSearchOffset());
        if (topTs != null) {
            result = findMatchesInTopSequence(topTs);
        }
        return result;
    }

    private int[] findMatchesInTopSequence(TokenSequence<? extends LatteTopTokenId> topTs) {
        assert topTs != null;
        int[] result = null;
        topTs.move(context.getSearchOffset());
        topTs.moveNext();
        TokenSequence<LatteMarkupTokenId> ts = topTs.embeddedJoined(LatteMarkupTokenId.language());
        if (ts != null) {
            result = findMatchesInEmbeddedSequence(topTs, ts);
        }
        return result;
    }

    private int[] findMatchesInEmbeddedSequence(TokenSequence<? extends LatteTopTokenId> topTs, TokenSequence<LatteMarkupTokenId> embeddedTs) {
        int[] result = null;
        embeddedTs.move(context.getSearchOffset());
        if (embeddedTs.moveNext()) {
            Token<? extends LatteMarkupTokenId> currentToken = embeddedTs.token();
            if (currentToken != null) {
                result = processMatchers(currentToken, topTs);
            }
        }
        return result;
    }

    private int[] processMatchers(Token<? extends LatteMarkupTokenId> currentToken, TokenSequence<? extends LatteTopTokenId> topTs) {
        int[] result = null;
        for (Matcher matcher : MATCHERS) {
            if (matcher.matches(currentToken)) {
                result = matcher.findMatches(currentToken, topTs);
                break;
            }
        }
        return result;
    }

    private static int[] createMatches(List<OffsetRange> offsetRanges) {
        int[] result = null;
        if (!offsetRanges.isEmpty()) {
            int resultSize = offsetRanges.size() * 2;
            result = new int[resultSize];
            for (int i = 0, j = 0; i < offsetRanges.size(); i++, j += 2) {
                result[j] = offsetRanges.get(i).getStart();
                result[j + 1] = offsetRanges.get(i).getEnd();
            }
        }
        return result;
    }

    private interface Matcher {

        boolean matches(Token<? extends LatteMarkupTokenId> token);

        int[] findMatches(Token<? extends LatteMarkupTokenId> token, TokenSequence<? extends LatteTopTokenId> topTs);

    }

    private static final class StartEndMacroMatcher implements Matcher {
        private static final String END = "/"; //NOI18N
        private final String macroName;

        public StartEndMacroMatcher(String macroName) {
            assert macroName != null;
            this.macroName = macroName;
        }

        @Override
        public boolean matches(Token<? extends LatteMarkupTokenId> token) {
            assert token != null;
            return (token.id() == LatteMarkupTokenId.T_MACRO_START || token.id() == LatteMarkupTokenId.T_MACRO_END)
                    && (macroName.equals(token.text().toString()) || (END + macroName).equals(token.text().toString()));
        }

        @Override
        public int[] findMatches(Token<? extends LatteMarkupTokenId> token, TokenSequence<? extends LatteTopTokenId> topTs) {
            assert token != null;
            assert topTs != null;
            int[] result = null;
            String tagText = token.text().toString();
            if (tagText.equals(macroName)) {
                List<OffsetRange> offsetRanges = LatteLexerUtils.findForwardMatching(
                        topTs,
                        LatteLexerUtils.LatteTokenTextImpl.create(LatteMarkupTokenId.T_MACRO_START, macroName),
                        LatteLexerUtils.LatteTokenTextImpl.create(LatteMarkupTokenId.T_MACRO_END, END + macroName));
                result = createMatches(offsetRanges);
            } else if (tagText.equals(END + macroName)) {
                List<OffsetRange> offsetRanges = LatteLexerUtils.findBackwardMatching(
                        topTs,
                        LatteLexerUtils.LatteTokenTextImpl.create(LatteMarkupTokenId.T_MACRO_END, END + macroName),
                        LatteLexerUtils.LatteTokenTextImpl.create(LatteMarkupTokenId.T_MACRO_START, macroName));
                result = createMatches(offsetRanges);
            }
            return result;
        }

    }

    private abstract static class MultiMatcher implements Matcher {
        protected static final LatteLexerUtils.LatteTokenText IF_TOKEN = LatteLexerUtils.LatteTokenTextImpl.create(LatteMarkupTokenId.T_MACRO_START, "if"); //NOI18N
        protected static final LatteLexerUtils.LatteTokenText IFSET_TOKEN = LatteLexerUtils.LatteTokenTextImpl.create(LatteMarkupTokenId.T_MACRO_START, "ifset"); //NOI18N
        protected static final LatteLexerUtils.LatteTokenText ELSE_IF_TOKEN = LatteLexerUtils.LatteTokenTextImpl.create(LatteMarkupTokenId.T_MACRO_START, "elseif"); //NOI18N
        protected static final LatteLexerUtils.LatteTokenText ELSE_IFSET_TOKEN = LatteLexerUtils.LatteTokenTextImpl.create(LatteMarkupTokenId.T_MACRO_START, "elseifset"); //NOI18N
        protected static final LatteLexerUtils.LatteTokenText ELSE_TOKEN = LatteLexerUtils.LatteTokenTextImpl.create(LatteMarkupTokenId.T_MACRO_START, "else"); //NOI18N
        protected static final LatteLexerUtils.LatteTokenText END_IF_TOKEN = LatteLexerUtils.LatteTokenTextImpl.create(LatteMarkupTokenId.T_MACRO_END, "/if"); //NOI18N
        protected static final LatteLexerUtils.LatteTokenText END_IFSET_TOKEN = LatteLexerUtils.LatteTokenTextImpl.create(LatteMarkupTokenId.T_MACRO_END, "/ifset"); //NOI18N

        @Override
        public boolean matches(Token<? extends LatteMarkupTokenId> token) {
            return matchingToken().matches(token);
        }

        protected abstract LatteLexerUtils.LatteTokenText matchingToken();

    }

    private static final class IfMatcher extends MultiMatcher {

        @Override
        protected LatteLexerUtils.LatteTokenText matchingToken() {
            return IF_TOKEN;
        }

        @Override
        public int[] findMatches(Token<? extends LatteMarkupTokenId> token, TokenSequence<? extends LatteTopTokenId> topTs) {
            assert token != null;
            assert topTs != null;
            List<OffsetRange> offsetRanges = LatteLexerUtils.findForwardMatching(
                    topTs,
                    IF_TOKEN,
                    END_IF_TOKEN,
                    Arrays.asList(ELSE_IF_TOKEN, ELSE_TOKEN));
            return createMatches(offsetRanges);
        }

    }

    private static final class IfsetMatcher extends MultiMatcher {

        @Override
        protected LatteLexerUtils.LatteTokenText matchingToken() {
            return IFSET_TOKEN;
        }

        @Override
        public int[] findMatches(Token<? extends LatteMarkupTokenId> token, TokenSequence<? extends LatteTopTokenId> topTs) {
            assert token != null;
            assert topTs != null;
            List<OffsetRange> offsetRanges = LatteLexerUtils.findForwardMatching(
                    topTs,
                    IFSET_TOKEN,
                    END_IFSET_TOKEN,
                    Arrays.asList(ELSE_IFSET_TOKEN, ELSE_TOKEN));
            return createMatches(offsetRanges);
        }

    }

    private static final class EndIfMatcher extends MultiMatcher {

        @Override
        protected LatteLexerUtils.LatteTokenText matchingToken() {
            return END_IF_TOKEN;
        }

        @Override
        public int[] findMatches(Token<? extends LatteMarkupTokenId> token, TokenSequence<? extends LatteTopTokenId> topTs) {
            assert token != null;
            assert topTs != null;
            List<OffsetRange> offsetRanges = LatteLexerUtils.findBackwardMatching(
                    topTs,
                    END_IF_TOKEN,
                    IF_TOKEN,
                    Arrays.asList(ELSE_IF_TOKEN, ELSE_TOKEN));
            return createMatches(offsetRanges);
        }

    }

    private static final class EndIfsetMatcher extends MultiMatcher {

        @Override
        protected LatteLexerUtils.LatteTokenText matchingToken() {
            return END_IFSET_TOKEN;
        }

        @Override
        public int[] findMatches(Token<? extends LatteMarkupTokenId> token, TokenSequence<? extends LatteTopTokenId> topTs) {
            assert token != null;
            assert topTs != null;
            List<OffsetRange> offsetRanges = LatteLexerUtils.findBackwardMatching(
                    topTs,
                    END_IFSET_TOKEN,
                    IFSET_TOKEN,
                    Arrays.asList(ELSE_IFSET_TOKEN, ELSE_TOKEN));
            return createMatches(offsetRanges);
        }

    }

    private static final class ElseMatcher extends MultiMatcher {

        @Override
        protected LatteLexerUtils.LatteTokenText matchingToken() {
            return ELSE_TOKEN;
        }

        @Override
        public int[] findMatches(Token<? extends LatteMarkupTokenId> token, TokenSequence<? extends LatteTopTokenId> topTs) {
            assert token != null;
            assert topTs != null;
            List<OffsetRange> offsetRanges = LatteLexerUtils.findBackwardMatching(
                    topTs,
                    END_IF_TOKEN,
                    IF_TOKEN,
                    Arrays.asList(ELSE_IF_TOKEN, ELSE_TOKEN));
            offsetRanges.addAll(LatteLexerUtils.findForwardMatching(
                    topTs,
                    IF_TOKEN,
                    END_IF_TOKEN,
                    Arrays.asList(LatteLexerUtils.LatteTokenText.NONE)));
            if (offsetRanges.isEmpty()) { // possibly an ELSE in "IFSET -> /IFSET" macro
                offsetRanges.addAll(LatteLexerUtils.findBackwardMatching(
                        topTs,
                        END_IFSET_TOKEN,
                        IFSET_TOKEN,
                        Arrays.asList(ELSE_IFSET_TOKEN, ELSE_TOKEN)));
                offsetRanges.addAll(LatteLexerUtils.findForwardMatching(
                        topTs,
                        IFSET_TOKEN,
                        END_IFSET_TOKEN,
                        Arrays.asList(LatteLexerUtils.LatteTokenText.NONE)));
            }
            return createMatches(offsetRanges);
        }

    }

    private static final class ElseIfMatcher extends MultiMatcher {

        @Override
        protected LatteLexerUtils.LatteTokenText matchingToken() {
            return ELSE_IF_TOKEN;
        }

        @Override
        public int[] findMatches(Token<? extends LatteMarkupTokenId> token, TokenSequence<? extends LatteTopTokenId> topTs) {
            assert token != null;
            assert topTs != null;
            List<OffsetRange> offsetRanges = LatteLexerUtils.findBackwardMatching(
                    topTs,
                    END_IF_TOKEN,
                    IF_TOKEN,
                    Arrays.asList(ELSE_IF_TOKEN, ELSE_TOKEN));
            offsetRanges.addAll(LatteLexerUtils.findForwardMatching(
                    topTs,
                    IF_TOKEN,
                    END_IF_TOKEN,
                    Arrays.asList(ELSE_IF_TOKEN, ELSE_TOKEN)));
            return createMatches(offsetRanges);
        }

    }

    private static final class ElseIfsetMatcher extends MultiMatcher {

        @Override
        protected LatteLexerUtils.LatteTokenText matchingToken() {
            return ELSE_IFSET_TOKEN;
        }

        @Override
        public int[] findMatches(Token<? extends LatteMarkupTokenId> token, TokenSequence<? extends LatteTopTokenId> topTs) {
            assert token != null;
            assert topTs != null;
            List<OffsetRange> offsetRanges = LatteLexerUtils.findBackwardMatching(
                    topTs,
                    END_IFSET_TOKEN,
                    IFSET_TOKEN,
                    Arrays.asList(ELSE_IFSET_TOKEN, ELSE_TOKEN));
            offsetRanges.addAll(LatteLexerUtils.findForwardMatching(
                    topTs,
                    IFSET_TOKEN,
                    END_IFSET_TOKEN,
                    Arrays.asList(ELSE_IFSET_TOKEN, ELSE_TOKEN)));
            return createMatches(offsetRanges);
        }

    }

    public static final class Factory implements BracesMatcherFactory {

        @Override
        public BracesMatcher createMatcher(MatcherContext context) {
            return new LatteBracesMatcher(context);
        }

    }

}
