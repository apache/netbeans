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
package org.netbeans.modules.php.blade.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.antlr.v4.runtime.Token;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.php.blade.syntax.BladeDirectivesUtils;
import org.netbeans.modules.php.blade.syntax.BladeTagsUtils;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrLexer;
import static org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrLexer.*;
import org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrUtils;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 * brace matcher - block directives : @if @endif .. - output echo statements {{
 * }} {!! !!}
 *
 * @author bogdan
 */
public class BladeBracesMatcher implements BracesMatcher {

    public enum BraceDirectionType {
        END_TO_START, START_TO_END, CUSTOM_START_TO_END, CURLY_END_TO_START, CURLY_START_TO_END, STOP, NONE
    };
    private final MatcherContext context;
    private Token originToken;
    private BraceDirectionType currentDirection = BraceDirectionType.NONE;

    private BladeBracesMatcher(MatcherContext context) {
        this.context = context;
    }

    @Override
    public int[] findOrigin() throws InterruptedException, BadLocationException {
        int[] result = null;
        originToken = null;
        BaseDocument document = (BaseDocument) context.getDocument();
        document.readLock();
        try {
            Token currentToken = BladeAntlrUtils.getToken(context.getDocument(), context.getSearchOffset());

            if (currentToken == null) {
                return result;
            }

            if (!shouldLookForBraceMatch(currentToken)) {
                return result;
            }

            originToken = currentToken;
            int start = currentToken.getStartIndex();
            int end = currentToken.getStopIndex();

            String tokenText = originToken.getText();
            
            if (!tokenText.startsWith("@") // NOI18N
                    && !tokenText.startsWith("{") // NOI18N
                    && !tokenText.endsWith("}")) // NOI18N
            {
                return result;
            }

            currentDirection = findBraceDirectionType(tokenText, currentToken);

            if (currentDirection.equals(BraceDirectionType.STOP)) {
                return result;
            }

            result = new int[]{start, end + 1};
        } finally {
            document.readUnlock();
        }
        return result;
    }

    @Override
    public int[] findMatches() throws InterruptedException, BadLocationException {
        if (originToken == null) {
            return null;
        }

        String tokenText = originToken.getText();

        return switch (currentDirection) {
            case CURLY_START_TO_END -> findCloseTag();
            case CURLY_END_TO_START -> findOpenTag();
            case START_TO_END -> findDirectiveEnd(tokenText);
            case CUSTOM_START_TO_END -> findCustomDirectiveEnd(tokenText);
            case END_TO_START -> findOriginForDirectiveEnd(tokenText);
            case NONE, STOP -> null;
        };
    }

    private static boolean shouldLookForBraceMatch(@NonNull Token currentToken) {
        return switch (currentToken.getType()) {
            case HTML, PHP_EXPRESSION, AT, BLADE_COMMENT, ERROR -> false;
            default -> true;
        };
    }

    private BraceDirectionType findBraceDirectionType(String tokenText, Token token) {
        boolean isCloseTag = Arrays.asList(BladeTagsUtils.outputCloseTags()).indexOf(tokenText) >= 0;

        if (isCloseTag) {
            return BraceDirectionType.CURLY_END_TO_START;
        }

        boolean isStartTag = Arrays.asList(BladeTagsUtils.outputStartTags()).indexOf(tokenText) >= 0;

        if (isStartTag) {
            return BraceDirectionType.CURLY_START_TO_END;
        }
        
        if (tokenText.startsWith(BladeDirectivesUtils.END_DIRECTIVE_PREFIX) ||
                tokenText.equals(BladeDirectivesUtils.DIRECTIVE_SHOW)) {
            return BraceDirectionType.END_TO_START;
        }

        if (BladeDirectivesUtils.directiveStart2EndPair(tokenText) != null) {
            return BraceDirectionType.START_TO_END;
        }

        if (token.getType() == BladeAntlrLexer.D_CUSTOM
                || token.getType() == BladeAntlrLexer.D_UNKNOWN) {
            return BraceDirectionType.CUSTOM_START_TO_END;
        }

        return BraceDirectionType.STOP;
    }

    private int[] findOpenTag() {
        int matchTokenType = BladeAntlrUtils.getTagPairTokenType(originToken.getType());
        List<Integer> skipableTokenTypes = new ArrayList<>();
        skipableTokenTypes.add(HTML);
        Token startToken = BladeAntlrUtils.findBackwardWithStop(context.getDocument(),
                originToken,
                matchTokenType,
                skipableTokenTypes);

        if (startToken != null) {
            int start = startToken.getStartIndex();
            int end = startToken.getStopIndex();
            return new int[]{start, end + 1};
        }

        return null;
    }

    private int[] findCloseTag() {
        int matchTokenType = BladeAntlrUtils.getTagPairTokenType(originToken.getType());
        List<Integer> skipableTokenTypes = new ArrayList<>();
        skipableTokenTypes.add(HTML);
        Token endToken = BladeAntlrUtils.findForwardWithStop(context.getDocument(),
                originToken,
                matchTokenType,
                skipableTokenTypes);

        if (endToken != null) {
            int start = endToken.getStartIndex();
            int end = endToken.getStopIndex();
            return new int[]{start, end + 1};
        }

        return null;
    }

    @CheckForNull
    private int[] findDirectiveEnd(String directive) {
        String[] pair = BladeDirectivesUtils.directiveStart2EndPair(directive);
        if (pair == null){
            return null;
        }
        List<String> startDirectiveForBalance = new ArrayList<>();
        List<String> stopDirectives = Arrays.asList(pair);

        for (String endDirective : pair) {
            String[] startDirectives = BladeDirectivesUtils.directiveEnd2StartPair(endDirective);

            if (startDirectives != null) {
                startDirectiveForBalance.addAll(Arrays.asList(startDirectives));
            }
        }

        Token endToken = BladeAntlrUtils.findForward(context.getDocument(),
                originToken,
                stopDirectives,
                startDirectiveForBalance);
        
        if (endToken != null) {
            int start = endToken.getStartIndex();
            int end = endToken.getStopIndex();
            return new int[]{start, end + 1};
        }

        return null;
    }

    @CheckForNull
    private int[] findCustomDirectiveEnd(String directive) {
        String endPrefix = BladeDirectivesUtils.END_DIRECTIVE_PREFIX;
        String[] pair = new String[]{endPrefix + directive.substring(1)};
        List<String> stopDirectives = Arrays.asList(pair);
        List<String> startDirectiveForBalance = Arrays.asList(new String[]{directive});

        Token endToken = BladeAntlrUtils.findForward(context.getDocument(),
                originToken,
                stopDirectives,
                startDirectiveForBalance);

        if (endToken != null) {
            int start = endToken.getStartIndex();
            int end = endToken.getStopIndex();
            return new int[]{start, end + 1};
        }

        return null;
    }

    @CheckForNull
    private int[] findOriginForDirectiveEnd(String directive) {
        String[] pair = BladeDirectivesUtils.directiveEnd2StartPair(directive);
        List<String> endDirectivesForBalance = new ArrayList<>();
        List<String> openDirectives = Arrays.asList(pair);

        for (String startDirective : pair) {
            String[] endDirectives = BladeDirectivesUtils.directiveStart2EndPair(startDirective);

            if (endDirectives != null) {
                endDirectivesForBalance.addAll(Arrays.asList(endDirectives));
            }
        }

        Token startToken = BladeAntlrUtils.findBackward(context.getDocument(),
                originToken,
                openDirectives,
                endDirectivesForBalance);

        if (startToken != null) {
            int start = startToken.getStartIndex();
            int end = startToken.getStopIndex();
            return new int[]{start, end + 1};
        }

        return null;
    }

    @MimeRegistration(service = BracesMatcherFactory.class, mimeType = BladeLanguage.MIME_TYPE, position = 110)
    public static final class Factory implements BracesMatcherFactory {

        @Override
        public BracesMatcher createMatcher(MatcherContext context) {
            return new BladeBracesMatcher(context);
        }

    }
}
