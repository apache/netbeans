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
package org.netbeans.modules.languages.neon.completion;

import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.languages.neon.parser.NeonParser.NeonParserResult;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class NeonCompletionProposal implements CompletionProposal {
    private final NeonElement element;
    private final CompletionRequest request;

    public NeonCompletionProposal(NeonElement element, CompletionRequest request) {
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
        return formatter.getText();
    }

    @Override
    public String getCustomInsertTemplate() {
        return element.getTemplate();
    }

    public static class CompletionRequest {
        public int anchorOffset;
        public String prefix;
        public NeonParserResult parserResult;
    }

    static class ServiceConfigOptCompletionProposal extends NeonCompletionProposal {

        public ServiceConfigOptCompletionProposal(NeonElement serviceDefinitionSwitch, CompletionRequest request) {
            super(serviceDefinitionSwitch, request);
        }

        @Override
        @NbBundle.Messages("ConfigOptRhs=Config")
        public String getRhsHtml(HtmlFormatter formatter) {
            return Bundle.ConfigOptRhs();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.ATTRIBUTE;
        }
    }

    static class TypeCompletionProposal extends NeonCompletionProposal {

        public TypeCompletionProposal(NeonElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            ElementHandle elementHandle = getElement();
            assert elementHandle != null;
            return ((NeonElement) elementHandle).getType();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

    }

    static class MethodCompletionProposal extends NeonCompletionProposal {

        public MethodCompletionProposal(NeonElement element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            ElementHandle elementHandle = getElement();
            assert elementHandle != null;
            return ((NeonElement) elementHandle).getType();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

    }

}
