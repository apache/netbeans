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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.VectorConfiguration;
import org.netbeans.modules.cnd.makeproject.ui.utils.StringListPanel;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;

abstract public class StringListNodeProp extends PropertySupport<List> {

    private final VectorConfiguration<String> configuration;
    private final BooleanConfiguration inheritValues;
    private final String[] texts;
    private final boolean addPathPanel;
    private final HelpCtx helpCtx;

    public StringListNodeProp(VectorConfiguration<String> configuration, BooleanConfiguration inheritValues, String[] texts, boolean addPathPanel, HelpCtx helpCtx) {
        super(texts[0], List.class, texts[1], texts[2], true, true);
        this.configuration = configuration;
        this.inheritValues = inheritValues;
        this.texts = texts;
        this.addPathPanel = addPathPanel;
        this.helpCtx = helpCtx;
    }

    @Override
    public String getHtmlDisplayName() {
        if (configuration.getModified()) {
            return "<b>" + getDisplayName(); // NOI18N
        } else {
            return null;
        }
    }

    @Override
    public List getValue() {
        return configuration.getValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue(List v) {
        configuration.setValue(v);
    }

    @Override
    public void restoreDefaultValue() {
        configuration.reset();
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public boolean isDefaultValue() {
        return configuration.getValue().isEmpty();
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        ArrayList<String> clone = new ArrayList<>();
        clone.addAll(configuration.getValue());
        return new StringEditor(clone);
    }

    abstract protected List<String> convertToList(String text);
    abstract protected String convertToString(List<String> list);

    private class StringEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private final List<String> value;
        private PropertyEnv env;

        public StringEditor(List<String> value) {
            this.value = value;
        }

        @Override
        public void setAsText(String text) {
            super.setValue(StringListNodeProp.this.convertToList(text.trim()));
        }

        @Override
        public String getAsText() {
            return StringListNodeProp.this.convertToString(value);
        }

        @Override
        public java.awt.Component getCustomEditor() {
            String text = null;
            if (inheritValues != null) {
                text = texts[4];
            }
            return new StringListPanel(texts[3], value, addPathPanel, inheritValues, text, this, env, helpCtx);
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
