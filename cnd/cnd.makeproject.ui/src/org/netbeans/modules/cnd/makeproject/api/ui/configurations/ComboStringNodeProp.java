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
package org.netbeans.modules.cnd.makeproject.api.ui.configurations;

import java.awt.Component;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.ComboStringConfiguration;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;

public class ComboStringNodeProp extends Node.Property {

    private final ComboStringConfiguration comboStringConfiguration;
    private final String name;
    private final String description;
    private final boolean canWrite;
    StringEditor intEditor = null;

    @SuppressWarnings("unchecked")
    public ComboStringNodeProp(ComboStringConfiguration comboStringConfiguration, boolean canWrite, String name, String description) {
        super(String.class);
        this.comboStringConfiguration = comboStringConfiguration;
        this.canWrite = canWrite;
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getShortDescription() {
        return description;
    }

    @Override
    public String getHtmlDisplayName() {
        if (comboStringConfiguration.getModified()) {
            return "<b>" + getDisplayName(); // NOI18N
        } else {
            return null;
        }
    }

    @Override
    public Object getValue() {
        return comboStringConfiguration.getValue();
    }

    @Override
    public void setValue(Object v) {
        comboStringConfiguration.setValue((String) v);
        comboStringConfiguration.getPicklist().addElement((String) v);
    }

    @Override
    public void restoreDefaultValue() {
        comboStringConfiguration.reset();
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public boolean isDefaultValue() {
        return !comboStringConfiguration.getModified();
    }

    @Override
    public boolean canWrite() {
        return true;
    }

//    public void setCanWrite(boolean canWrite) {
//        this.canWrite = canWrite;
//    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        if (intEditor == null) {
            intEditor = new StringEditor();
        }
        return intEditor;
    }

    protected class StringEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private PropertyEnv env;

        @Override
        public void attachEnv(PropertyEnv env) {
            this.env = env;
            env.getFeatureDescriptor().setValue("canEditAsText", Boolean.TRUE); // NOI18N
        }

        @Override
        public String getJavaInitializationString() {
            return getAsText();
        }

        @Override
        public String getAsText() {
            return comboStringConfiguration.getValue();
        }

        @Override
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            super.setValue(text);
        }

        @Override
        public String[] getTags() {
            return comboStringConfiguration.getPicklist().getElementsDisplayName();
        }

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public Component getCustomEditor() {
            return new StringPanel(this, env);
        }
    }
}
