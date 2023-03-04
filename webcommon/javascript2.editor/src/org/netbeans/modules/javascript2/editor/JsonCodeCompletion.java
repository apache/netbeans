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
package org.netbeans.modules.javascript2.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.types.spi.ParserResult;

/**
 *
 * @author Dusan Balek
 */
public class JsonCodeCompletion implements CodeCompletionHandler {

    private static final Logger LOGGER = Logger.getLogger(JsonCodeCompletion.class.getName());
    private boolean caseSensitive;

    @Override
    public CodeCompletionResult complete(CodeCompletionContext context) {
        final CancelSupport cancelSupport = CancelSupport.getDefault();
        if (cancelSupport.isCancelled()) {
            return CodeCompletionResult.NONE;
        }

        long start = System.currentTimeMillis();

        Document doc = (Document) context.getParserResult().getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return CodeCompletionResult.NONE;
        }

        ParserResult info = (ParserResult) context.getParserResult();

        TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
        if (th == null) {
            return CodeCompletionResult.NONE;
        }
        TokenSequence<JsTokenId> ts = th.tokenSequence(JsTokenId.jsonLanguage());
        if (ts == null) {
            return CodeCompletionResult.NONE;
        }

        String pref = context.getPrefix();
        int caretOffset = context.getParserResult().getSnapshot().getEmbeddedOffset(context.getCaretOffset());
        final int anchor = caretOffset - pref.length();

        ts.move(anchor);
        if (!ts.moveNext() && !ts.movePrevious()){
            return CodeCompletionResult.NONE;
        }

        final Model model = Model.getModel(info, false);
        if (model == null) {
            return CodeCompletionResult.NONE;
        }

        this.caseSensitive = context.isCaseSensitive();
        final List<CompletionProposal> resultList = new ArrayList<>();

        final Token<? extends JsTokenId> token = ts.token();
        final JsTokenId tokenId = token.id();
        ts.movePrevious();
        final Token<? extends JsTokenId> prevToken = LexUtilities.findPreviousNonWsNonComment(ts);
        final JsTokenId prevTokenId = prevToken.id();

        if (prevTokenId == JsTokenId.BRACKET_LEFT_CURLY || prevTokenId == JsTokenId.OPERATOR_COMMA) {
            if (tokenId == JsTokenId.STRING || tokenId == JsTokenId.WHITESPACE) {
                Set<String> keys = new HashSet<>();
                findKeys(model.getGlobalObject(), caretOffset, keys);
                for (final String key : keys) {
                    if (startsWith(key, pref)) {
                        resultList.add(new CompletionProposal() {
                            @Override
                            public int getAnchorOffset() {
                                return anchor;
                            }

                            @Override
                            public ElementHandle getElement() {
                                return null;
                            }

                            @Override
                            public String getName() {
                                return key;
                            }

                            @Override
                            public String getInsertPrefix() {
                                return tokenId == JsTokenId.STRING ? key : '\"' + key + '\"';
                            }

                            @Override
                            public String getSortText() {
                                return key;
                            }

                            @Override
                            public String getLhsHtml(HtmlFormatter formatter) {
                                formatter.appendText(key);
                                return formatter.getText();
                            }

                            @Override
                            public String getRhsHtml(HtmlFormatter formatter) {
                                return null;
                            }

                            @Override
                            public ElementKind getKind() {
                                return ElementKind.PROPERTY;
                            }

                            @Override
                            public ImageIcon getIcon() {
                                return null;
                            }

                            @Override
                            public Set<Modifier> getModifiers() {
                                return Collections.emptySet();
                            }

                            @Override
                            public boolean isSmart() {
                                return false;
                            }

                            @Override
                            public int getSortPrioOverride() {
                                return 1;
                            }

                            @Override
                            public String getCustomInsertTemplate() {
                                return null;
                            }
                        });
                    }
                }
            }
        }


        long end = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Counting JSON CC took {0}ms ",  (end - start));

        if (!resultList.isEmpty()) {
            return new DefaultCompletionResult(resultList, false);
        }
        return CodeCompletionResult.NONE;
    }

    @Override
    public String document(
            org.netbeans.modules.csl.spi.ParserResult info,
            ElementHandle element) {
        return null;
    }

    @Override
    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }

    @Override
    public String getPrefix(
            org.netbeans.modules.csl.spi.ParserResult info,
            int caretOffset,
            boolean upToOffset) {
        String prefix = "";
        Document doc = (Document) info.getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return null;
        }
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(info.getSnapshot(), caretOffset, JsTokenId.jsonLanguage());
        if (ts == null) {
            return null;
        }
        int offset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }
        if (ts.offset() == offset) {
            // We're looking at the offset to the RIGHT of the caret
            // and here I care about what's on the left
            ts.movePrevious();
        }
        Token<? extends JsTokenId> token = ts.token();
        if (token != null && token.id() != JsTokenId.EOL) {
            JsTokenId id = token.id();
            if (id == JsTokenId.STRING && offset < ts.offset() + token.length()) {
                prefix = token.text().toString();
                prefix = prefix.substring(1, prefix.length() - 1);
                if (upToOffset) {
                    int prefixIndex = getPrefixIndexFromSequence(prefix.substring(0, offset - ts.offset() - 1));
                    prefix = prefix.substring(prefixIndex, offset - ts.offset() - 1);
                }
            }
            if (id == JsTokenId.KEYWORD_FALSE || id == JsTokenId.KEYWORD_TRUE || id == JsTokenId.KEYWORD_NULL) {
                prefix = token.text().toString();
                if (upToOffset) {
                    if (offset - ts.offset() >= 0) {
                        prefix = prefix.substring(0, offset - ts.offset());
                    }
                }
            }
            if (id.isError()) {
                prefix = token.text().toString();
                //if (upToOffset) {
                    prefix = prefix.substring(0, offset - ts.offset());
                //}
            }
        }
        LOGGER.log(Level.FINE, String.format("Prefix for cc: %s", prefix));
        return prefix;
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        return QueryType.NONE;
    }

    @Override
    public String resolveTemplateVariable(
            String variable,
            org.netbeans.modules.csl.spi.ParserResult info,
            int caretOffset,
            String name,
            Map parameters) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document doc, int selectionBegin, int selectionEnd) {
        return null;
    }

    @Override
    public ParameterInfo parameters(
            org.netbeans.modules.csl.spi.ParserResult info,
            int caretOffset,
            CompletionProposal proposal) {
        return ParameterInfo.NONE;
    }

    /** XXX - Once the JS framework support becomes plugable, should be moved to jQueryCompletionHandler getPrefix() */
    private int getPrefixIndexFromSequence(String prefix) {
        int spaceIndex = prefix.lastIndexOf(" ") + 1; //NOI18N
        int dotIndex = prefix.lastIndexOf(".") + 1; //NOI18N
        int hashIndex = prefix.lastIndexOf("#") + 1; //NOI18N
        int bracketIndex = prefix.lastIndexOf("[") + 1; //NOI18N
        int columnIndex = prefix.lastIndexOf(":") + 1; //NOI18N
        int parenIndex = prefix.lastIndexOf("(") + 1; //NOI18N
        // for file code completion
        int slashIndex = prefix.lastIndexOf('/') + 1; //NOI18N
        return (Math.max(0, Math.max(hashIndex, Math.max(dotIndex, Math.max(parenIndex,Math.max(columnIndex, Math.max(bracketIndex, Math.max(spaceIndex, slashIndex))))))));
    }

    private boolean startsWith(String theString, String prefix) {
        if (prefix == null || prefix.length() == 0) {
            return true;
        }
        return caseSensitive ? theString.startsWith(prefix)
                : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    private boolean findKeys(JsObject object, int offset, Set<String> keys) {
        boolean containsOffset = false;
        for (Map.Entry<String, ? extends JsObject> entry : object.getProperties().entrySet()) {
            containsOffset = findKeys(entry.getValue(), offset, keys);
            if (entry.getValue().containsOffset(offset) && !containsOffset) {
                containsOffset = true;
                continue;
            }
            if (!entry.getKey().isEmpty() && !entry.getValue().isAnonymous()) {
                keys.add(entry.getKey());
            }
        }
        return containsOffset;
    }
}
