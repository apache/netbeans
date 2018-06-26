/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
        private static final ImageIcon MACRO_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/php/latte/resources/macro_cc_icon.png")); //NOI18N

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
        private static final ImageIcon HELPER_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/php/latte/resources/helper_cc_icon.png")); //NOI18N

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
        private static final ImageIcon KEYWORD_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/php/latte/resources/latte_cc_icon.png")); //NOI18N
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
        private static final ImageIcon CONTROL_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/php/latte/resources/latte_cc_icon.png")); //NOI18N

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
