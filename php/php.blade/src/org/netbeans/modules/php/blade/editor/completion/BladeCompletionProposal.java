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
package org.netbeans.modules.php.blade.editor.completion;

import java.util.Collections;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.php.blade.csl.elements.ClassElement;
import org.netbeans.modules.php.blade.editor.ResourceUtilities;
import org.netbeans.modules.php.blade.syntax.annotation.Directive;
import org.netbeans.modules.php.blade.syntax.annotation.Tag;

import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;

/**
 *
 * @author bogdan
 */
public class BladeCompletionProposal implements CompletionProposal {

    //@StaticResource
    final CompletionRequest request;
    protected final ElementHandle element;
    final String previewValue;
    protected Directive directive;

    public BladeCompletionProposal(ElementHandle element, CompletionRequest request, String previewValue) {
        this.element = element;
        this.request = request;
        this.previewValue = previewValue;
    }

    public BladeCompletionProposal(ElementHandle element, CompletionRequest request, Directive directive) {
        this.element = element;
        this.request = request;
        this.previewValue = directive.name();
        this.directive = directive;
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
    public String getSortText() {
        return getName();
    }

    @Override
    public int getSortPrioOverride() {
        return 0;
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatter.name(getKind(), true);
        formatter.appendHtml("<font>");
        formatter.appendHtml("<b>");
        formatter.appendText(previewValue);
        formatter.appendHtml("</b>");
        formatter.appendHtml("</font>");
        formatter.name(getKind(), false);
        return formatter.getText();
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
    public String getCustomInsertTemplate() {
        return null;
    }

    @Override
    public String getInsertPrefix() {
        StringBuilder template = new StringBuilder();
        template.append(getName());
        return template.toString();

    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        FileObject file = null;
        if (element != null) {
            file = element.getFileObject();
        }
        if (file != null) {
            formatter.reset();
            formatter.appendText(" ");
            formatter.appendText(file.getName());
        }
        return formatter.getText();
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.CONSTRUCTOR;
    }

    @Override
    public boolean isSmart() {
        return true;
    }

    public static class PhpElementItem extends BladeCompletionProposal {

        public PhpElementItem(ElementHandle element, CompletionRequest request, String previewValue) {
            super(element, request, previewValue);
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            FileObject file = null;
            if (this.getElement() != null) {
                file = this.getElement().getFileObject();
            }
            if (file != null) {
                formatter.reset();
                formatter.appendText(" ");
                formatter.appendText(file.getNameExt());
            }
            return formatter.getText();
        }
    }

    public static class NamespaceItem extends PhpElementItem {

        public NamespaceItem(ElementHandle element, CompletionRequest request, String previewValue) {
            super(element, request, previewValue);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.PACKAGE;
        }

        @Override
        public int getSortPrioOverride() {
            return -50;//priority
        }
    }

    public static class DirectiveItem extends BladeCompletionProposal {

        public DirectiveItem(ElementHandle element, CompletionRequest request, String previewValue) {
            super(element, request, previewValue);
        }

    }

    public static class ClassItem extends PhpElementItem {

        protected String namespace = null;
        
        public ClassItem(ClassElement element, CompletionRequest request, String previewValue) {
            super(element, request, previewValue);
            this.namespace = element.getNamespace();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (namespace != null && namespace.length() > 0) {
                return namespace;
            }
            return super.getRhsHtml(formatter);
        }

        @Override
        public int getSortPrioOverride() {
            return 10;//priority
        }

        @Override
        public String getCustomInsertTemplate() {
            if (namespace != null && namespace.length() > 0) {
                return "\\" + namespace + "\\" + element.getName();
            }
            return element.getName();
        }
    }

    public static class FunctionItem extends PhpElementItem {

        protected final String namespace;

        public FunctionItem(ElementHandle element, CompletionRequest request, String previewValue) {
            super(element, request, previewValue);
            this.namespace = null;
        }

        public FunctionItem(ElementHandle element, CompletionRequest request,
                String namespace,
                String previewValue) {
            super(element, request, previewValue);
            this.namespace = namespace;
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }

        @Override
        public int getSortPrioOverride() {
            return 20;//priority
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (namespace != null && namespace.length() > 0) {
                return namespace;
            }
            return super.getRhsHtml(formatter);
        }

    }

    public static class ConstantItem extends PhpElementItem {

        public ConstantItem(ElementHandle element, CompletionRequest request, String previewValue) {
            super(element, request, previewValue);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTANT;
        }

    }

    public static class VariableItem extends BladeCompletionProposal {

        public VariableItem(ElementHandle element, CompletionRequest request, String previewValue) {
            super(element, request, previewValue);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

    }

    public static class BladeVariableItem extends BladeCompletionProposal {

        public BladeVariableItem(ElementHandle element, CompletionRequest request, String previewValue) {
            super(element, request, previewValue);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return "blade";
        }
    }

    public static class CompletionRequest {

        public int anchorOffset;
        public int carretOffset;
        public String prefix;
    }

    public static class BladeTag extends BladeCompletionProposal {

        protected Tag tag;

        public BladeTag(ElementHandle element, CompletionRequest request, Tag tag) {
            super(element, request, "");
            this.tag = tag;
        }

        @Override
        public String getCustomInsertTemplate() {
            return tag.openTag() + " ${cursor} " + tag.closeTag();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return tag.openTag() + " " + tag.closeTag();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return tag.description();
        }

        @Override
        public int getSortPrioOverride() {
            return 0;
        }
    }

    public static class DirectiveProposal extends BladeCompletionProposal {

        public DirectiveProposal(ElementHandle element, CompletionRequest request, Directive directive) {
            super(element, request, directive);
        }

        public DirectiveProposal(ElementHandle element, CompletionRequest request, String previewValue) {
            super(element, request, previewValue);
        }

        @Override
        public ImageIcon getIcon() {
            String path = ResourceUtilities.ICON_BASE + "icons/at.png";//NOI18N
            return ImageUtilities.loadImageIcon(path, false);
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (this.directive == null) {
                return null;
            }

            if (directive.description().isEmpty() && !this.directive.since().isEmpty()) {
                return "v" + this.directive.since();
            }
            return this.directive.description();
        }

    }

    public static class CustomDirective extends DirectiveProposal {

        public CustomDirective(ElementHandle element, CompletionRequest request, String preview) {
            super(element, request, preview);
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (this.getElement().getFileObject() != null) {
                return this.getElement().getFileObject().getNameExt();
            }
            return "custom directive";
        }

    }

    public static class InlineDirective extends DirectiveProposal {

        public InlineDirective(ElementHandle element, CompletionRequest request, Directive directive) {
            super(element, request, directive);
        }

    }

    public static class DirectiveWithArg extends InlineDirective {

        public DirectiveWithArg(ElementHandle element, CompletionRequest request, Directive directive) {
            super(element, request, directive);
        }

        @Override
        public String getCustomInsertTemplate() {
            String template = getName() + "($$${arg})";
            switch (getName()) {
                case "@include":
                case "@extends":
                    template = getName() + "('${path}')";
                    break;
            }
            return template;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return getName() + "()";
        }
    }

    public static class BlockDirective extends DirectiveProposal {

        public BlockDirective(ElementHandle element, CompletionRequest request, Directive directive) {
            super(element, request, directive);
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return getName() + " ... " + directive.endtag();
        }

        @Override
        public String getCustomInsertTemplate() {
            return getName() + "\n    ${selection} ${cursor}\n" + directive.endtag();
        }

    }

    public static class BlockDirectiveWithArg extends DirectiveProposal {

        public BlockDirectiveWithArg(ElementHandle element, CompletionRequest request, Directive directive) {
            super(element, request, directive);
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return getName() + "() ... " + directive.endtag();
        }

        @Override
        public String getCustomInsertTemplate() {
            String template = getName() + "($$${arg})\n    ${cursor}\n" + directive.endtag();

            switch (getName()) {
                case "@foreach": // NOI18N
                    template = getName() + "($$${array} as $$${item})\n    ${selection}${cursor}\n" + directive.endtag();
                    break;
                case "@section": // NOI18N
                case "@session": // NOI18N
                    template = getName() + "('${id}')\n    ${cursor}\n" + directive.endtag();
                    break;
            }

            return template;
        }

    }
}
