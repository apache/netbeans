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
package org.netbeans.modules.lsp.client.spi.friend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.tm4e.core.internal.parser.PropertySettable.ArrayList;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Exceptions;

//based on the VS Code API's "LanguageConfiguration" (MIT)
//TODO: the language-configuration.json is more broad, should be enhanced to support language-configuration.json:
public final class LanguageConfiguration {
    public final @NullAllowed CommentRule comments;
    public final @NullAllowed CharacterPair[] brackets;
    public final @NullAllowed RegExp wordPattern;
    public final @NullAllowed IndentationRule indentationRules;
    public final @NullAllowed OnEnterRule[] onEnterRules;
    public final @NullAllowed AutoClosingPair[] autoClosingPairs;
    public final @NullAllowed String autoCloseBefore;
    public final @NullAllowed CharacterPair[] surroundingPairs;

    private LanguageConfiguration(CommentRule comments, CharacterPair[] brackets, RegExp wordPattern, IndentationRule indentationRules, OnEnterRule[] onEnterRules, AutoClosingPair[] autoClosingPairs, String autoCloseBefore, CharacterPair[] surroundingPairs) {
        this.comments = comments;
        this.brackets = brackets;
        this.wordPattern = wordPattern;
        this.indentationRules = indentationRules;
        this.onEnterRules = onEnterRules;
        this.autoClosingPairs = autoClosingPairs;
        this.autoCloseBefore = autoCloseBefore;
        this.surroundingPairs = surroundingPairs;
    }
    
    public static LanguageConfiguration from(CommentRule comments, CharacterPair[] brackets, RegExp wordPattern, IndentationRule indentationRules, OnEnterRule[] onEnterRules, AutoClosingPair[] autoClosingPairs, String autoCloseBefore, CharacterPair[] surroundingPairs) {
        return new LanguageConfiguration(comments, brackets, wordPattern, indentationRules, onEnterRules, autoClosingPairs, autoCloseBefore, surroundingPairs);
    }

    public static final class RegExp {
        public final @NonNull String regExp;

        public RegExp(String regExp) {
            this.regExp = regExp;
        }

        public static RegExp from(String regExp) {
            return new RegExp(regExp);
        }
        
    }
    public static final class CharacterPair {
        public final String first;
        public final String second;

        private CharacterPair(String first, String second) {
            this.first = first;
            this.second = second;
        }

        public static CharacterPair from(String first, String second) {
            return new CharacterPair(first, second);
        }
    }

    public static final class LineComment {
        public final @NonNull String comment;
        public final boolean noIdent;

        private LineComment(String comment, boolean noIdent) {
            this.comment = comment;
            this.noIdent = noIdent;
        }

        public static LineComment from(String comment) {
            return from(comment, false);
        }

        public static LineComment from(String comment, boolean noIdent) {
            return new LineComment(comment, noIdent);
        }
    }

    public static final class CommentRule {
        public final @NullAllowed LineComment lineComment;
        public final @NullAllowed CharacterPair blockComment;

        private CommentRule(LineComment lineComment, CharacterPair blockComment) {
            this.lineComment = lineComment;
            this.blockComment = blockComment;
        }

        public static CommentRule from(LineComment lineComment, CharacterPair blockComment) {
            return new CommentRule(lineComment, blockComment);
        }
    }

    public static final class IndentationRule {
        public @NullAllowed RegExp decreaseIndentPattern;
        public @NullAllowed RegExp increaseIndentPattern;
        public @NullAllowed RegExp indentNextLinePattern;
        public @NullAllowed RegExp unIndentedLinePattern;

        private IndentationRule(RegExp decreaseIndentPattern, RegExp increaseIndentPattern, RegExp indentNextLinePattern, RegExp unIndentedLinePattern) {
            this.decreaseIndentPattern = decreaseIndentPattern;
            this.increaseIndentPattern = increaseIndentPattern;
            this.indentNextLinePattern = indentNextLinePattern;
            this.unIndentedLinePattern = unIndentedLinePattern;
        }
        
        public static IndentationRule from(RegExp decreaseIndentPattern, RegExp increaseIndentPattern, RegExp indentNextLinePattern, RegExp unIndentedLinePattern) {
            return new IndentationRule(decreaseIndentPattern, increaseIndentPattern, indentNextLinePattern, unIndentedLinePattern);
        }
    }
    
    public static enum IndentAction {
        None,
        Indent,
        IndentOutdent,
        Outdent
    }

    public static final class EnterAction {
        public final @NonNull IndentAction indentAction;
        public final @NullAllowed String appendText;
        public final @NullAllowed Integer removeText;

        private EnterAction(IndentAction indentAction, String appendText, Integer removeText) {
            this.indentAction = indentAction;
            this.appendText = appendText;
            this.removeText = removeText;
        }

        public static EnterAction from(IndentAction indentAction, String appendText, Integer removeText) {
            return new EnterAction(indentAction, appendText, removeText);
        }
    }

    public static final class OnEnterRule {
        public final @NonNull RegExp beforeText;
        public final @NullAllowed RegExp afterText;
        public final @NullAllowed RegExp previousLineText;
        public final @NonNull EnterAction action;

        private OnEnterRule(RegExp beforeText, RegExp afterText, RegExp previousLineText, EnterAction action) {
            this.beforeText = beforeText;
            this.afterText = afterText;
            this.previousLineText = previousLineText;
            this.action = action;
        }
        
        public static OnEnterRule from(RegExp beforeText, RegExp afterText, RegExp previousLineText, EnterAction action) {
            return new OnEnterRule(beforeText, afterText, previousLineText, action);
        }
    }

    public static enum SyntaxTokenType {
        Other,
        Comment,
        String,
        RegEx
    }
    
    public static final class AutoClosingPair {
        public @NonNull String open;
        public @NonNull String close;
        public @NullAllowed SyntaxTokenType[] notIn;

        private AutoClosingPair(String open, String close, SyntaxTokenType[] notIn) {
            this.open = open;
            this.close = close;
            this.notIn = notIn;
        }
        
        public static AutoClosingPair from(String open, String close, SyntaxTokenType[] notIn) {
            return new AutoClosingPair(open, close, notIn);
        }
    }

    //the language-configuration.json often does not fully follow the JSON spec,
    //be lenient in parsing:
    private static final JsonMapper MAPPER = JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
                .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
                .build();
    public static LanguageConfiguration from(String source) {
        //TODO resilience against "incorrect" config:
        Map<String, Object> config;
        try {
            config = MAPPER.readValue(source, HashMap.class);
        } catch (JsonProcessingException ex) {
            Exceptions.printStackTrace(ex);
            config = Map.of();
        }
        CommentRule comments = null;
        Map<String, Object> commentsConfig = (Map<String, Object>) config.get("comments");
        if (commentsConfig != null) {
            LineComment lineComment = null;
            Object lineCommentConfig = commentsConfig.get("lineComment");
            if (lineCommentConfig instanceof String comment) {
                lineComment = LineComment.from(comment);
            } else if (lineCommentConfig instanceof Map vals) {
                lineComment = LineComment.from((String) vals.get("comment"), (boolean) vals.getOrDefault("noIndent", false));
            }
            List<String> commentsBlockConfig = (List<String>) commentsConfig.get("blockComment");
            CharacterPair blockComment = null;

            if (commentsBlockConfig != null) {
                blockComment = CharacterPair.from(commentsBlockConfig.get(0), commentsBlockConfig.get(1));
            }
            comments = CommentRule.from(lineComment, blockComment);
        }
        CharacterPair[] braces = gatherPairs(config.get("brackets"));
        AutoClosingPair[] autoClosingPairs = null;
        List<Object> autoClosingPairsConfig = (List<Object>) config.get("autoClosingPairs");
        if (autoClosingPairsConfig != null) {
            List<AutoClosingPair> autoClosingPairsList = new ArrayList<>();
            for (Object conf : autoClosingPairsConfig) {
                if (conf instanceof Map<?, ?> map) {
                    SyntaxTokenType[] notIn = null;
                    if (map.get("notIn") instanceof List<?> notInList) {
                        notIn = notInList.stream().map(key -> switch ((String) key) {
                            case "string" -> SyntaxTokenType.String;
                            case "comment" -> SyntaxTokenType.Comment;
                            case "regex" -> SyntaxTokenType.RegEx;
                            default -> SyntaxTokenType.Other;
                        }).toArray(SyntaxTokenType[]::new);
                    }
                    autoClosingPairsList.add(new AutoClosingPair((String) map.get("open"), (String) map.get("close"), notIn));
                } else if (conf instanceof List<?> pair && pair.size() == 2) {
                    autoClosingPairsList.add(new AutoClosingPair((String) pair.get(0), (String) pair.get(1), null));
                }
            }
            autoClosingPairs = autoClosingPairsList.toArray(AutoClosingPair[]::new);
        }
        String autoCloseBefore = null;
        if (config.get("autoCloseBefore") instanceof String autoCloseBeforeValue) {
            autoCloseBefore = autoCloseBeforeValue;
        }
        CharacterPair[] surroundingPairs = gatherPairs(config.get("surroundingPairs"));
        return LanguageConfiguration.from(comments, braces, null, null, null, autoClosingPairs, autoCloseBefore, surroundingPairs);
    }

    private static CharacterPair[] gatherPairs(Object pairsConfigObj) {
        CharacterPair[] result = null;

        if (pairsConfigObj instanceof List<?> pairsConfig) {
            List<CharacterPair> bracesList = new ArrayList<>();
            for (Object conf : pairsConfig) {
                if (conf instanceof List<?> pair) {
                    bracesList.add(new CharacterPair((String) pair.get(0), (String) pair.get(1)));
                }
            }
            result = bracesList.toArray(CharacterPair[]::new);
        }

        return result;
    }
}
