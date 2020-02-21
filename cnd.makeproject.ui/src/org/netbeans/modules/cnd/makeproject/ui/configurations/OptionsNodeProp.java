/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.OptionsConfiguration;
import org.netbeans.modules.cnd.makeproject.spi.configurations.AllOptionsProvider;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectOptionsFormat;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.PropertySupport;

public class OptionsNodeProp extends PropertySupport<String> {

    private final OptionsConfiguration commandLineConfiguration;
    private final BooleanConfiguration inheritValues;
    private final AllOptionsProvider optionsProvider;
    private final AbstractCompiler compiler;
    private String delimiter = ""; // NOI18N
    private final String[] texts;

    public OptionsNodeProp(OptionsConfiguration commandLineConfiguration, BooleanConfiguration inheritValues, AllOptionsProvider optionsProvider, AbstractCompiler compiler, String delimiter, String[] texts) {
        super("ID", String.class, texts[0], texts[1], true, true); // NOI18N
        this.commandLineConfiguration = commandLineConfiguration;
        this.inheritValues = inheritValues;
        this.optionsProvider = optionsProvider;
        this.compiler = compiler;
        if (delimiter != null) {
            this.delimiter = delimiter;
        }
        this.texts = texts;
    }

    @Override
    public String getHtmlDisplayName() {
        if (commandLineConfiguration.getModified()) {
            return "<b>" + getDisplayName(); // NOI18N
        } else {
            return null;
        }
    }

    @Override
    public String getValue() {
        return commandLineConfiguration.getValue();
    }

    @Override
    public void setValue(String v) {
        String s = MakeProjectOptionsFormat.reformatWhitespaces(v);
        commandLineConfiguration.setValue(s);
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new CommandLinePropEditor();
    }

    /*
    public Object getValue(String attributeName) {
    if (attributeName.equals("canEditAsText")) // NOI18N
    return Boolean.FALSE;
    else
    return super.getValue(attributeName);
    }
     */
    @Override
    public void restoreDefaultValue() {
        commandLineConfiguration.optionsReset();
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public boolean isDefaultValue() {
        return !commandLineConfiguration.getModified();
    }

    private class CommandLinePropEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private PropertyEnv env;

        @Override
        public void setAsText(String text) {
            StringBuilder newText = new StringBuilder();
            if (delimiter.length() > 0) {
                // Remove delimiter
                StringTokenizer st = new StringTokenizer(text, delimiter);
                while (st.hasMoreTokens()) {
                    newText.append(st.nextToken());
                }
            } else {
                newText.append(text);
            }
            super.setValue(newText.toString());
        }

        @Override
        public String getAsText() {
            String s = (String) super.getValue();
            return MakeProjectOptionsFormat.reformatWhitespaces(s, "", delimiter); // NOI18N
        }

        @Override
        public java.awt.Component getCustomEditor() {
            OptionsEditorPanel commandLineEditorPanel = new OptionsEditorPanel(texts, inheritValues, this, env);
            commandLineEditorPanel.setAllOptions(optionsProvider.getAllOptions(compiler));
            commandLineEditorPanel.setAdditionalOptions((String) super.getValue());
            return commandLineEditorPanel;
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }
}
