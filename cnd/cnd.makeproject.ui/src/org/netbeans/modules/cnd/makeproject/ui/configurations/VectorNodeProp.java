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
import org.netbeans.modules.cnd.makeproject.ui.utils.DirectoryChooserPanel;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;

abstract public class VectorNodeProp extends PropertySupport<List> {

    private final VectorConfiguration<String> vectorConfiguration;
    private final BooleanConfiguration inheritValues;
    private final FSPath baseDir;
    private final String[] texts;
    private final boolean addPathPanel;
    private final int onlyFolder;
    private final HelpCtx helpCtx;

    public VectorNodeProp(VectorConfiguration<String> vectorConfiguration, BooleanConfiguration inheritValues, FSPath baseDir, 
            String[] texts, boolean addPathPanel, int onlyFolder, HelpCtx helpCtx) {
        super(texts[0], List.class, texts[1], texts[2], true, true);
        this.vectorConfiguration = vectorConfiguration;
        this.inheritValues = inheritValues;
        this.baseDir = baseDir;
        this.texts = texts;
        this.addPathPanel = addPathPanel;
        this.onlyFolder = onlyFolder;
        this.helpCtx = helpCtx;
    }

    @Override
    public String getHtmlDisplayName() {
        if (vectorConfiguration.getModified()) {
            return "<b>" + getDisplayName(); // NOI18N
        } else {
            return null;
        }
    }

    @Override
    public List getValue() {
        return vectorConfiguration.getValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue(List v) {
        vectorConfiguration.setValue(v);
    }

    @Override
    public void restoreDefaultValue() {
        vectorConfiguration.reset();
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public boolean isDefaultValue() {
        return vectorConfiguration.getValue().isEmpty();
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        ArrayList<String> clone = new ArrayList<>();
        clone.addAll(vectorConfiguration.getValue());
        return new DirectoriesEditor(clone);
    }

    abstract protected List<String> convertToList(String text);
    abstract protected String convertToString(List<String> list);

    private class DirectoriesEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private final List<String> value;
        private PropertyEnv env;

        public DirectoriesEditor(List<String> value) {
            this.value = value;
        }
        @Override
        public void setAsText(String text) {
            super.setValue(VectorNodeProp.this.convertToList(text.trim()));
        }

        @Override
        public String getAsText() {
            return VectorNodeProp.this.convertToString(value);
        }

        @Override
        public java.awt.Component getCustomEditor() {
            String text = null;
            if (inheritValues != null) {
                text = texts[3];
            }
            return new DirectoryChooserPanel(baseDir, value, addPathPanel, inheritValues, text, this, env, VectorNodeProp.this.onlyFolder, helpCtx);
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
