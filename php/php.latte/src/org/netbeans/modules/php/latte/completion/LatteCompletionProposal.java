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
package org.netbeans.modules.php.latte.completion;

import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.php.latte.parser.LatteParserResult;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class LatteCompletionProposal implements CompletionProposal {
    private final LatteElement element;
    private final CompletionRequest request;

    public LatteCompletionProposal(final LatteElement element, final CompletionRequest request) {
        this.element = element;
        this.request = request;
    }

    @Override
    public int getAnchorOffset() {
        return request.anchorOffset;
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
        return element.getName();
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
        return getName().startsWith(request.prefix);
    }

    @Override
    public int getSortPrioOverride() {
        return 0;
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatter.name(getKind(), true);
        formatter.appendText(getName());
        formatter.name(getKind(), false);
        element.formatParameters(formatter);
        return formatter.getText();
    }

    @Override
    public String getCustomInsertTemplate() {
        return element.getTemplate();
    }

    abstract static class MacroCompletionProposal extends LatteCompletionProposal {
        private static final ImageIcon MACRO_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/php/latte/resources/macro_cc_icon.png", false); //NOI18N

        public MacroCompletionProposal(LatteElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public ImageIcon getIcon() {
            return MACRO_ICON;
        }

    }

    static class StartMacroCompletionProposal extends MacroCompletionProposal {

        public StartMacroCompletionProposal(LatteElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        @NbBundle.Messages("MacroRhs=Macro")
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.MacroRhs();
        }

        @Override
        public int getSortPrioOverride() {
            return 50;
        }

    }

    static class EndMacroCompletionProposal extends MacroCompletionProposal {

        public EndMacroCompletionProposal(LatteElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        @NbBundle.Messages("EndMacroRhs=End Macro")
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.EndMacroRhs();
        }

        @Override
        public int getSortPrioOverride() {
            return 100;
        }

    }

    static class HelperCompletionProposal extends LatteCompletionProposal {
        private static final ImageIcon HELPER_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/php/latte/resources/helper_cc_icon.png", false); //NOI18N

        public HelperCompletionProposal(LatteElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        @NbBundle.Messages("HelperRhs=Helper")
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.HelperRhs();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.RULE;
        }

        @Override
        public ImageIcon getIcon() {
            return HELPER_ICON;
        }

    }

    static class KeywordCompletionProposal extends LatteCompletionProposal {
        private static final ImageIcon KEYWORD_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/php/latte/resources/latte_cc_icon.png", false); //NOI18N
        public KeywordCompletionProposal(LatteElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        @NbBundle.Messages("KeywordRhs=Keyword")
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.KeywordRhs();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public ImageIcon getIcon() {
            return KEYWORD_ICON;
        }

    }

    abstract static class IteratorItemCompletionProposal extends LatteCompletionProposal {

        public IteratorItemCompletionProposal(LatteElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        @NbBundle.Messages("IteratorRhs=Iterator")
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.IteratorRhs();
        }

        @Override
        public ImageIcon getIcon() {
            return null;
        }

    }

    static class IteratorFieldItemCompletionProposal extends IteratorItemCompletionProposal {

        public IteratorFieldItemCompletionProposal(LatteElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.FIELD;
        }

    }

    static class IteratorMethodItemCompletionProposal extends IteratorItemCompletionProposal {

        public IteratorMethodItemCompletionProposal(LatteElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

    }

    abstract static class VariableCompletionProposal extends LatteCompletionProposal {
        public VariableCompletionProposal(LatteElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

    }

    static class DefaultVariableCompletionProposal extends VariableCompletionProposal {
        public DefaultVariableCompletionProposal(LatteElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        @NbBundle.Messages("DefaultVariableRhs=Default Variable")
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.DefaultVariableRhs();
        }

        @Override
        public int getSortPrioOverride() {
            return 40;
        }

    }

    static class UserVariableCompletionProposal extends VariableCompletionProposal {
        public UserVariableCompletionProposal(LatteElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        @NbBundle.Messages("UserVariableRhs=User Variable")
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.UserVariableRhs();
        }

        @Override
        public int getSortPrioOverride() {
            return 15;
        }

    }

    static class ControlCompletionProposal extends LatteCompletionProposal {
        private static final ImageIcon CONTROL_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/php/latte/resources/latte_cc_icon.png", false); //NOI18N

        public ControlCompletionProposal(LatteElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        @NbBundle.Messages("ControlRhs=Control")
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.ControlRhs();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.OTHER;
        }

        @Override
        public ImageIcon getIcon() {
            return CONTROL_ICON;
        }

    }

    public static class CompletionRequest {
        public int anchorOffset;
        public String prefix;
        public LatteParserResult parserResult;
    }

}
