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
package org.netbeans.modules.html.knockout.javascript;

import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.html.knockout.KOUtils;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author Roman Svitanic
 */
public class KnockoutCodeCompletionItem implements CompletionProposal {

    static final String KNOCKOUT_LABEL = "Knockout"; //NOI18N
    private final int anchorOffset;
    private final ElementHandle element;

    public KnockoutCodeCompletionItem(final ElementHandle element, final int anchorOffset) {
        this.anchorOffset = anchorOffset;
        this.element = element;
    }

    @Override
    public int getAnchorOffset() {
        return anchorOffset;
    }

    @Override
    public ElementHandle getElement() {
        return element;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getInsertPrefix() {
        return element.getName();
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        return element.getName();
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return KNOCKOUT_LABEL;
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return element.getModifiers();
    }

    @Override
    public boolean isSmart() {
        return false;
    }

    @Override
    public int getSortPrioOverride() {
        return 20;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }

    static class KOComponentItem extends KnockoutCodeCompletionItem {

        private final ElementHandle element;
        private final CodeCompletionContext context;

        public KOComponentItem(ElementHandle element, CodeCompletionContext ccContext) {
            super(element, ccContext.getCaretOffset());
            this.element = element;
            this.context = ccContext;
        }

        @Override
        public ImageIcon getIcon() {
            return KOUtils.KO_ICON;
        }

        @Override
        public int getAnchorOffset() {
            return context.getCaretOffset() - context.getPrefix().length();
        }

        @Override
        public String getCustomInsertTemplate() {
            String result = element.getName();
            TokenHierarchy th = TokenHierarchy.get(context.getParserResult().getSnapshot().getSource().getDocument(true));
            if (th != null) {
                TokenSequence<JsTokenId> ts = LexerUtils.getTokenSequence(th, context.getCaretOffset(), JsTokenId.javascriptLanguage(), false);
                if (ts != null) {
                    int diff = ts.move(context.getCaretOffset());
                    if (diff == 0 && ts.movePrevious() || ts.moveNext()) {
                        Token<JsTokenId> token = ts.token();
                        JsTokenId id = token.id();
                        if (id == JsTokenId.UNKNOWN && ts.movePrevious()) {
                            token = ts.token();
                            id = token.id();
                        }

                        boolean isInString = (id == JsTokenId.STRING_BEGIN || id == JsTokenId.STRING);
                        if (!isInString) {
                            result = '\'' + result + '\'';
                        }
                    }
                }
            }
            return result;
        }
    }

    static class KOComponentOptionItem extends KOComponentItem {

        private final ElementHandle element;

        public KOComponentOptionItem(ElementHandle element, CodeCompletionContext ccContext) {
            super(element, ccContext);
            this.element = element;
        }

        @Override
        public String getCustomInsertTemplate() {
            return element.getName() + ": ";
        }
    }

    static class KOComponentOptionConfigItem extends KOComponentItem {

        private final ElementHandle element;

        public KOComponentOptionConfigItem(ElementHandle element, CodeCompletionContext ccContext) {
            super(element, ccContext);
            this.element = element;
        }

        @Override
        public String getCustomInsertTemplate() {
            return element.getName() + ": { }";
        }
    }
}
