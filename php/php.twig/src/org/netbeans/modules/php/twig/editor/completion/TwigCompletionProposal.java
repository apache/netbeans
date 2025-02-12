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
package org.netbeans.modules.php.twig.editor.completion;

import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.php.twig.editor.completion.TwigCompletionContextFinder.CompletionContext;
import org.netbeans.modules.php.twig.editor.parsing.TwigParserResult;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class TwigCompletionProposal implements CompletionProposal {
    private final TwigElement element;
    private final CompletionRequest request;

    public TwigCompletionProposal(final TwigElement element, final CompletionRequest request) {
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

    static class TagCompletionProposal extends TwigCompletionProposal {

        public TagCompletionProposal(TwigElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        @NbBundle.Messages("TagRhs=Tag")
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.TagRhs();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.TAG;
        }

    }

    static class FilterCompletionProposal extends TwigCompletionProposal {

        private static final ImageIcon ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/php/twig/resources/filter.png", false); //NOI18N

        public FilterCompletionProposal(TwigElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        @NbBundle.Messages("FilterRhs=Filter")
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.FilterRhs();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.RULE;
        }

        @Override
        public ImageIcon getIcon() {
            return ICON;
        }

    }

    static class FunctionCompletionProposal extends TwigCompletionProposal {

        public FunctionCompletionProposal(TwigElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        @NbBundle.Messages("FunctionRhs=Function")
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.FunctionRhs();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public String getSortText() {
            return "10" + getName(); //NOI18N
        }

    }

    static class TestCompletionProposal extends TwigCompletionProposal {

        public TestCompletionProposal(TwigElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        @NbBundle.Messages("TestRhs=Test")
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.TestRhs();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.TEST;
        }

        @Override
        public String getSortText() {
            return "20" + getName(); //NOI18N
        }

    }

    static class OperatorCompletionProposal extends TwigCompletionProposal {

        private static final ImageIcon ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/php/twig/resources/twig-logo.png", false); //NOI18N

        public OperatorCompletionProposal(TwigElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        @NbBundle.Messages("OperatorRhs=Operator")
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.OperatorRhs();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public ImageIcon getIcon() {
            return ICON;
        }

    }

    public static class CompletionRequest {
        public int anchorOffset;
        public String prefix;
        public TwigParserResult parserResult;
        public CompletionContext context;
    }

}
