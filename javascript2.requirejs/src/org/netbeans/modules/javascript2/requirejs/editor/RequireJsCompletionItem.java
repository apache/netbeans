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
