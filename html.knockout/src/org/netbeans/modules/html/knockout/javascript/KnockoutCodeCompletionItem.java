/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
