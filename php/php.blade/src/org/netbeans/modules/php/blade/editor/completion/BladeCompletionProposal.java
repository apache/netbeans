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
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.php.blade.csl.elements.ClassElement;
import org.netbeans.modules.php.blade.editor.ResourceUtilities;
import org.netbeans.modules.php.blade.syntax.BladeDirectivesUtils;
import org.netbeans.modules.php.blade.syntax.annotation.Directive;
import org.netbeans.modules.php.blade.syntax.annotation.Tag;

import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;

/**
 *
 * @author bogdan
 */
public class BladeCompletionProposal implements CompletionProposal {

    private final ElementHandle element;
    private final String previewValue;
    private int anchorOffset;
    private Directive directive;

    public BladeCompletionProposal(ElementHandle element, int anchorOffset, String previewValue) {
        this.element = element;
        this.anchorOffset = anchorOffset;
        this.previewValue = previewValue;
    }

    public BladeCompletionProposal(ElementHandle element, int anchorOffset, Directive directive) {
        this.element = element;
        this.anchorOffset = anchorOffset;
        this.previewValue = directive.name();
        this.directive = directive;
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
        formatter.appendHtml("<font>"); // NOI18N
        formatter.appendHtml("<b>"); // NOI18N
        formatter.appendText(previewValue);
        formatter.appendHtml("</b>"); // NOI18N
        formatter.appendHtml("</font>"); // NOI18N
        formatter.name(getKind(), false); // NOI18N
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
            formatter.appendText(" "); // NOI18N
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
    
    public Directive getDirective(){
        return directive;
    }

    public static class PhpElementItem extends BladeCompletionProposal {

        public PhpElementItem(ElementHandle element, int anchorOffset, String previewValue) {
            super(element, anchorOffset, previewValue);
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            FileObject file = null;
            if (this.getElement() != null) {
                file = this.getElement().getFileObject();
            }
            if (file != null) {
                formatter.reset();
                formatter.appendText(" "); // NOI18N
                formatter.appendText(file.getNameExt());
            }
            return formatter.getText();
        }
    }

    public static class NamespaceItem extends PhpElementItem {

        public NamespaceItem(ElementHandle element, int anchorOffset, String previewValue) {
            super(element, anchorOffset, previewValue);
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

        public DirectiveItem(ElementHandle element, int anchorOffset, String previewValue) {
            super(element, anchorOffset, previewValue);
        }

    }

    public static class ClassItem extends PhpElementItem {

        private String namespace = null;
        
        public ClassItem(ClassElement element, int anchorOffset, String previewValue) {
            super(element, anchorOffset, previewValue);
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
                return "\\" + namespace + "\\" + getElement().getName(); // NOI18N
            }
            return getElement().getName();
        }
    }

    public static class FunctionItem extends PhpElementItem {

        protected final String namespace;

        public FunctionItem(ElementHandle element, int anchorOffset, String previewValue) {
            super(element, anchorOffset, previewValue);
            this.namespace = null;
        }

        public FunctionItem(ElementHandle element, int anchorOffset,
                String namespace,
                String previewValue) {
            super(element, anchorOffset, previewValue);
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

        public ConstantItem(ElementHandle element, int anchorOffset, String previewValue) {
            super(element, anchorOffset, previewValue);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTANT;
        }

    }

    public static class VariableItem extends BladeCompletionProposal {

        public VariableItem(ElementHandle element, int anchorOffset, String previewValue) {
            super(element, anchorOffset, previewValue);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

    }

    public static class BladeVariableItem extends BladeCompletionProposal {

        public BladeVariableItem(ElementHandle element, int anchorOffset, String previewValue) {
            super(element, anchorOffset, previewValue);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return "blade"; // NOI18N
        }
    }

    public static class BladeTag extends BladeCompletionProposal {

        private Tag tag;

        public BladeTag(ElementHandle element, int anchorOffset, Tag tag) {
            super(element, anchorOffset, ""); // NOI18N
            this.tag = tag;
        }

        @Override
        public String getCustomInsertTemplate() {
            return tag.openTag() + " ${cursor} " + tag.closeTag(); // NOI18N
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

        public DirectiveProposal(ElementHandle element, int anchorOffset, Directive directive) {
            super(element, anchorOffset, directive);
        }

        public DirectiveProposal(ElementHandle element, int anchorOffset, String previewValue) {
            super(element, anchorOffset, previewValue);
        }

        @Override
        public ImageIcon getIcon() {
            String path = ResourceUtilities.DIRECTIVE_ICON;
            return ImageUtilities.loadImageIcon(path, false);
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (this.getDirective() == null) {
                return null;
            }

            if (getDirective().description().isEmpty() && !getDirective().since().isEmpty()) {
                return "v" + getDirective().since(); // NOI18N
            }
            return getDirective().description();
        }

    }

    public static class CustomDirective extends DirectiveProposal {

        public CustomDirective(ElementHandle element, int anchorOffset, String preview) {
            super(element, anchorOffset, preview);
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (this.getElement().getFileObject() != null) {
                return this.getElement().getFileObject().getNameExt();
            }
            return "custom directive"; // NOI18N
        }

    }

    public static class InlineDirective extends DirectiveProposal {

        public InlineDirective(ElementHandle element, int anchorOffset, Directive directive) {
            super(element, anchorOffset, directive);
        }

    }

    public static class DirectiveWithArg extends InlineDirective {

        public DirectiveWithArg(ElementHandle element, int anchorOffset, Directive directive) {
            super(element, anchorOffset, directive);
        }

        @Override
        public String getCustomInsertTemplate() {
            return switch (getName()) {
                case BladeDirectivesUtils.DIRECTIVE_INCLUDE, BladeDirectivesUtils.DIRECTIVE_EXTENDS -> getName() + "('${path}')"; // NOI18N
                default -> getName() + "($$${arg})"; // NOI18N
            };
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return getName() + "()"; // NOI18N
        }
    }

    public static class BlockDirective extends DirectiveProposal {

        public BlockDirective(ElementHandle element, int anchorOffset, Directive directive) {
            super(element, anchorOffset, directive);
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return getName() + " ... " + getDirective().endtag(); // NOI18N
        }

        @Override
        public String getCustomInsertTemplate() {
            return getName() + "\n    ${selection} ${cursor}\n" + getDirective().endtag(); // NOI18N
        }

    }

    public static class BlockDirectiveWithArg extends DirectiveProposal {

        public BlockDirectiveWithArg(ElementHandle element, int anchorOffset, Directive directive) {
            super(element, anchorOffset, directive);
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return getName() + "() ... " + getDirective().endtag(); // NOI18N
        }

        @Override
        public String getCustomInsertTemplate() {
            return switch (getName()) {
                case BladeDirectivesUtils.DIRECTIVE_FOREACH -> // NOI18N
                    getName() + "($$${array} as $$${item})\n    ${selection}${cursor}\n" + getDirective().endtag(); // NOI18N
                case BladeDirectivesUtils.DIRECTIVE_SECTION, BladeDirectivesUtils.DIRECTIVE_SESSION -> // NOI18N
                    getName() + "('${id}')\n    ${cursor}\n" + getDirective().endtag(); // NOI18N
                default -> getName() + "($$${arg})\n    ${cursor}\n" + getDirective().endtag(); // NOI18N
            };
        }

    }
}
