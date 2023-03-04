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
package org.netbeans.modules.javascript2.requirejs.editor;

import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.javascript2.requirejs.ConfigOption;
import org.netbeans.modules.javascript2.requirejs.RequireJsDataProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class RequireJsCompletionItem implements CompletionProposal {

    private final int anchor;
    private final ElementHandle element;

    public RequireJsCompletionItem(ElementHandle element, final String name, final int anchor) {
        this.element = element;
        this.anchor = anchor;
    }

    protected String getText() {
        return getName();
    }

    @Override
    public int hashCode() {
        return getText().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RequireJsCompletionItem)) {
            return false;
        }

        RequireJsCompletionItem remote = (RequireJsCompletionItem) o;

        return getText().equals(remote.getText());
    }

    @Override
    public ImageIcon getIcon() {
        return EditorUtils.getRequireJsIcon();
    }

    @Override
    public int getAnchorOffset() {
        return this.anchor;
    }

    @Override
    public ElementHandle getElement() {
        return this.element;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getInsertPrefix() {
        return getName();
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        return getText();
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return "";
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
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
        return -1000;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }

    public static class PluginNameCompletionItem extends RequireJsCompletionItem {

        public PluginNameCompletionItem(final String name, final int anchor) {
            super(new PluginNameHandle(name), name, anchor);
        }

        @Override
        public String getInsertPrefix() {
            return getName() + '!'; //NOI18N
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            return getName() + '!';
        }

        
        private static class PluginNameHandle extends SimpleHandle.DocumentationHandle {

            public PluginNameHandle(final String name) {
                super(name, ElementKind.OTHER);
            }

            @Override
            @NbBundle.Messages("pluginNameDoc=Loader Plugin")
            public String getDocumentation() {
                return Bundle.pluginNameDoc();
            }
        }

    }

    public static class PropertyNameCompletionItem extends RequireJsCompletionItem {

        private final ConfigOption.OptionType type;

        public PropertyNameCompletionItem(final String name, ConfigOption.OptionType type, final int anchor) {
            super(new PropertyHandle(name), name, anchor);
            this.type = type;
        }

        @Override
        public String getInsertPrefix() {
            return getName() + ": "; //NOI18N
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder sb = new StringBuilder();
            sb.append(getName()).append(": "); // NOI18N
            switch (type) {
                case STRING:
                    sb.append("'${cursor}'"); // NOI18N
                    break;
                case OBJECT:
                    sb.append("{\n${cursor}\n}"); // NOI18N
                    break;
                case ARRAY:
                    sb.append("[\n${cursor}\n]"); // NOI18N
                    break;
                case BOOLEAN:
                    sb.append("${false}${cursor}"); // NOI18N
                    break; 
                default:
                    sb.append("${cursor}"); // NOI18N
            }
            return sb.toString();
        }

        private static class PropertyHandle extends SimpleHandle.DocumentationHandle {

            public PropertyHandle(final String name) {
                super(name, ElementKind.PROPERTY);
            }

            @Override
            public String getDocumentation() {
                return RequireJsDataProvider.getDefault().getDocFocOption(getName());
            }

        }
    }

}
