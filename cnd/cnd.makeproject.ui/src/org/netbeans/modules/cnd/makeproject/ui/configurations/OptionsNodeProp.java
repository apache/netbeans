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
