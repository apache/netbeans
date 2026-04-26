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
package org.netbeans.modules.php.blade.editor.braces;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.antlr.v4.runtime.Token;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import org.netbeans.modules.php.blade.editor.FileSystemUtils;
import org.netbeans.modules.php.blade.editor.directives.CustomDirectives;
import org.netbeans.modules.php.blade.editor.directives.CustomDirectives.CustomDirective;
import org.netbeans.modules.php.blade.editor.lexer.BladeLexerUtils;
import org.netbeans.modules.php.blade.editor.lexer.BladeTokenId;
import org.netbeans.modules.php.blade.syntax.BladeDirectivesUtils;
import static org.netbeans.modules.php.blade.syntax.BladeDirectivesUtils.END_DIRECTIVE_PREFIX;
import org.netbeans.modules.php.blade.syntax.BladeTagsUtils;
import org.netbeans.modules.php.blade.syntax.antlr4.utils.BaseBladeAntlrUtils;
import static org.netbeans.modules.php.blade.syntax.BladeTagsUtils.*;
import org.netbeans.modules.php.blade.syntax.antlr4.utils.BladeAntlrLexerUtils;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.lexer.antlr4.AntlrTokenSequence;

/**
 * brace matcher - block directives : @if @endif .. - output echo statements {{
 * }} {!! !!}
 *
 * @author bogdan
 */
public class BladeBracesMatcher implements BracesMatcher {

    public enum BraceDirectionType {
        BLOCK_DIRECTIVE_END_TO_START, BLOCK_DIRECTIVE_START_TO_END,
        CUSTOM_START_TO_END, CUSTOM_END_TO_START,
        CURLY_END_TO_START, CURLY_START_TO_END, STOP
    }
    private final MatcherContext context;
    private String tokenText;
    private int tokenOffset;
    private BraceDirectionType currentDirection;

    private BladeBracesMatcher(MatcherContext context) {
        this.context = context;
    }

    @Override
    public int[] findOrigin() throws InterruptedException, BadLocationException {
        int[] result = null;
        tokenText = null;
        tokenOffset = context.getSearchOffset();
        BaseDocument document = (BaseDocument) context.getDocument();
        document.readLock();
        try {
            TokenHierarchy<?> th = TokenHierarchy.get(document);
            org.netbeans.api.lexer.Token<BladeTokenId> token = BladeLexerUtils.getBladeToken(th, context.getSearchOffset());

            if (token == null) {
                return result;
            }

            BladeTokenId id = token.id();
            tokenOffset = token.offset(th);

            switch (id) {
                case BLADE_DIRECTIVE -> {
                    tokenText = token.text().toString().trim();
                    currentDirection = findDirectiveBlockDirectionType(tokenText);
                    if (currentDirection.equals(BraceDirectionType.STOP)) {
                        return result;
                    }
                    return new int[]{tokenOffset, tokenOffset + token.length()};
                }
                case BLADE_ECHO_DELIMITOR -> {
                    tokenText = token.text().toString().trim();
                    currentDirection = isStartTag(tokenText) ? BraceDirectionType.CURLY_START_TO_END : BraceDirectionType.CURLY_END_TO_START;
                    return new int[]{tokenOffset, tokenOffset + token.length()};
                }
                case BLADE_CUSTOM_DIRECTIVE -> {
                    tokenText = token.text().toString().trim();
                    Project projectOwner = FileSystemUtils.getProjectOwner(document);
                    if (projectOwner == null) {
                        return result;
                    }
                    CustomDirectives customDirectives = CustomDirectives.getInstance(projectOwner);
                    if (customDirectives == null) {
                        return result;
                    }
                    for (List<CustomDirective> directiveCollection : customDirectives.getCustomDirectives().values()) {
                        for (CustomDirective customDirective : directiveCollection) {
                            if (customDirective.isBlockDirective()
                                    && tokenText.equals(customDirective.getName())) {
                                currentDirection = BraceDirectionType.CUSTOM_START_TO_END;
                                return new int[]{tokenOffset, tokenOffset + token.length()};
                            }
                        }
                    }
                    return result;
                }
            }
        } finally {
            document.readUnlock();
        }
        return result;
    }

    @Override
    public int[] findMatches() throws InterruptedException, BadLocationException {
        int[] result = null;

        if (tokenText == null) {
            return result;
        }

        return switch (currentDirection) {
            case BLOCK_DIRECTIVE_START_TO_END -> findBlockDirectiveEnd(tokenText);
            case BLOCK_DIRECTIVE_END_TO_START -> findBlockDirectiveStart(tokenText);
            case CUSTOM_START_TO_END -> findCustomDirectiveEnd(tokenText);
            case CUSTOM_END_TO_START -> findCustomDirectiveStart(tokenText);
            case CURLY_START_TO_END -> findCloseTag();
            case CURLY_END_TO_START -> findOpenTag();
            default -> result;
        };
    }

    private int[] findBlockDirectiveEnd(String directive) {
        String[] endings = BladeDirectivesUtils.blockDirectiveEndings(directive);

        if (endings == null) {
            return null;
        }

        Set<String> openingDirectives = BladeDirectivesUtils.blockDirectiveOpeningsSet(endings);
        Set<String> endDirectives = new HashSet<>(Arrays.asList(endings));

        int searchOffset = tokenOffset + tokenText.length() + 1;
        AntlrTokenSequence ats = BladeAntlrLexerUtils.getTokens(context.getDocument());

        Token endToken = BaseBladeAntlrUtils.findForward(ats,
                searchOffset,
                endDirectives,
                openingDirectives);

        return outputRange(endToken);
    }

    private int[] findBlockDirectiveStart(String directive) {
        String[] openings = BladeDirectivesUtils.blockDirectiveOpenings(directive);

        if (openings == null) {
            if (!directive.startsWith(END_DIRECTIVE_PREFIX)) {
                return null;
            }
            Project projectOwner = FileSystemUtils.getProjectOwner(context.getDocument());
            if (projectOwner == null) {
                return null;
            }
            CustomDirectives customDirectives = CustomDirectives.getInstance(projectOwner);
            if (customDirectives == null) {
                return null;
            }
            String startTag = BladeDirectivesUtils.AT + directive.substring(END_DIRECTIVE_PREFIX.length()); // NOI18N
            for (List<CustomDirective> directiveCollection : customDirectives.getCustomDirectives().values()) {
                for (CustomDirective customDirective : directiveCollection) {
                    if (customDirective.isBlockDirective()
                            && startTag.equals(customDirective.getName())) {
                        openings = new String[]{startTag};
                        break;
                    }
                }
            }
            if (openings == null) {
                return null;
            }
        }

        Set<String> endDirectivesForBalance = BladeDirectivesUtils.blockDirectiveEndingsSet(openings);
        Set<String> openDirectives = new HashSet<>(Arrays.asList(openings));

        int searchOffset = tokenOffset - 1;
        AntlrTokenSequence ats = BladeAntlrLexerUtils.getTokens(context.getDocument());

        Token startToken = BaseBladeAntlrUtils.findBackward(ats,
                searchOffset,
                openDirectives,
                endDirectivesForBalance);

        return outputRange(startToken);
    }

    public int[] findOpenTag() {
        AntlrTokenSequence ats = BladeAntlrLexerUtils.getTokens(context.getDocument());
        String matchText = tokenText.equals(CONTENT_TAG_CLOSE) ? CONTENT_TAG_OPEN : RAW_TAG_OPEN;
        int searchOffset = tokenOffset + tokenText.length() + 1;
        Token startToken = BladeAntlrLexerUtils.findBackward(ats,
                searchOffset,
                new HashSet<>(Arrays.asList(matchText)),
                new HashSet<>());

        return outputRange(startToken);
    }

    public int[] findCloseTag() {
        AntlrTokenSequence ats = BladeAntlrLexerUtils.getTokens(context.getDocument());
        String matchText = tokenText.equals(CONTENT_TAG_OPEN) ? CONTENT_TAG_CLOSE : RAW_TAG_CLOSE;
        int searchOffset = tokenOffset - 1;
        Token endToken = BladeAntlrLexerUtils.findForward(ats,
                searchOffset,
                new HashSet<>(Arrays.asList(matchText)),
                new HashSet<>());

        return outputRange(endToken);
    }

    public int[] findCustomDirectiveEnd(String directive) {
        String[] pair = new String[]{BladeDirectivesUtils.END_DIRECTIVE_PREFIX + directive.substring(1)};
        Set<String> stopDirectives = new HashSet<>(Arrays.asList(pair));
        Set<String> startDirectiveForBalance = new HashSet<>();
        startDirectiveForBalance.add(directive);

        AntlrTokenSequence ats = BladeAntlrLexerUtils.getTokens(context.getDocument());
        int searchOffset = tokenOffset - 1;
        Token endToken = BaseBladeAntlrUtils.findForward(ats,
                searchOffset,
                stopDirectives,
                startDirectiveForBalance);

        return outputRange(endToken);
    }

    public int[] findCustomDirectiveStart(String directive) {
        int endTextLength = BladeDirectivesUtils.END_DIRECTIVE_PREFIX.length();
        String[] pair = new String[]{directive.substring(endTextLength)};
        Set<String> stopDirectives = new HashSet<>(Arrays.asList(pair));
        Set<String> startDirectiveForBalance = new HashSet<>();
        startDirectiveForBalance.add(directive);

        AntlrTokenSequence ats = BladeAntlrLexerUtils.getTokens(context.getDocument());
        int searchOffset = tokenOffset + tokenText.length() + 1;
        Token endToken = BaseBladeAntlrUtils.findBackward(ats,
                searchOffset,
                stopDirectives,
                startDirectiveForBalance);

        return outputRange(endToken);
    }

    private boolean isStartTag(String tag) {
        return Arrays.asList(BladeTagsUtils.outputStartTags()).indexOf(tag) >= 0;
    }

    private BraceDirectionType findDirectiveBlockDirectionType(String tokenText) {
        if (tokenText.startsWith(BladeDirectivesUtils.END_DIRECTIVE_PREFIX)
                || tokenText.equals(BladeDirectivesUtils.DIRECTIVE_SHOW)
                || tokenText.equals(BladeDirectivesUtils.DIRECTIVE_ELSEIF)
                || tokenText.equals(BladeDirectivesUtils.DIRECTIVE_ELSE)) {
            return BraceDirectionType.BLOCK_DIRECTIVE_END_TO_START;
        } else if (BladeDirectivesUtils.blockDirectiveEndings(tokenText) != null) {
            return BraceDirectionType.BLOCK_DIRECTIVE_START_TO_END;
        }

        return BraceDirectionType.STOP;
    }

    private int[] outputRange(Token token) {
        if (token != null) {
            String rangeTokenText = token.getText().trim();
            int start = token.getStartIndex();
            int end = start + rangeTokenText.length();
            return new int[]{start, end};
        }

        return null;
    }

    @MimeRegistration(service = BracesMatcherFactory.class, mimeType = BladeLanguage.MIME_TYPE, position = 0)
    public static final class Factory implements BracesMatcherFactory {

        @Override
        public BracesMatcher createMatcher(MatcherContext context) {
            return new BladeBracesMatcher(context);
        }

    }
}
