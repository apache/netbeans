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

import java.awt.Component;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.ui.customizer.DevelopmentHostCustomizer;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public class DevelopmentHostNodeProp extends Node.Property {

    private final DevelopmentHostConfiguration configuration;
    private final boolean canWrite;
    private final String name;
    private final String displayName;
    private final String description;

    @SuppressWarnings("unchecked")
    public DevelopmentHostNodeProp(DevelopmentHostConfiguration configuration, boolean canWrite, String name, String displayName, String description) {
        super(Integer.class);
        this.configuration = configuration;
        this.canWrite = canWrite;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        setValue("title", NbBundle.getMessage(DevelopmentHostNodeProp.class, "DLG_TITLE_Connect")); // NOI18N
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getShortDescription() {
        return description;
    }

    @Override
    public String getHtmlDisplayName() {
        if (configuration.getModified()) {
            return "<b>" + getDisplayName() + "</b>"; // NOI18N
        } else {
            return null;
        }
    }

    @Override
    public Object getValue() {
        return configuration.getValue();
    }

    @Override
    public Object getValue(String attributeName) {
        if (attributeName.equals("canAutoComplete")) { //NOI18N
            return Boolean.FALSE;
        }
        return super.getValue(attributeName);
    }        
    
    @Override
    public void setValue(Object value) {
        configuration.setValue((String) value, true);
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
        return !configuration.getModified();
    }

    @Override
    public boolean canWrite() {
        return canWrite;
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new IntEditor();
    }

    private class IntEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private PropertyEnv env;

        @Override
        public String getJavaInitializationString() {
            return getAsText();
        }

        @Override
        public String getAsText() {
            return configuration.getDisplayName(true);
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            super.setValue(text);
        }

        @Override
        public void setValue(Object value) {
            super.setValue(value);
        }


        @Override
        public String[] getTags() {
            List<String> l = new ArrayList<>();
            ServerList.getRecords().forEach((record) -> {
                l.add(record.getDisplayName());
            });
            return l.toArray(new String[l.size()]);
        }

        @Override
        public boolean supportsCustomEditor() {
            return canWrite;
        }

        @Override
        public Component getCustomEditor() {
            return new DevelopmentHostCustomizer(configuration, this, env);
        }

        @Override
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
    }
}
