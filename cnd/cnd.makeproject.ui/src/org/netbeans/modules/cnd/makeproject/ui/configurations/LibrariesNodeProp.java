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
import java.util.List;
import java.util.ArrayList;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibrariesConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.PropertySupport;

public class LibrariesNodeProp extends PropertySupport<List> {

    private final LibrariesConfiguration configuration;
    private final Project project;
    private final MakeConfiguration conf;
    private final FSPath baseDir;
    private final String[] texts;

    public LibrariesNodeProp(LibrariesConfiguration configuration, Project project, MakeConfiguration conf, FSPath baseDir, String[] texts) {
        super(texts[0], List.class, texts[1], texts[2], true, true);
        this.configuration = configuration;
        this.project = project;
        this.conf = conf;
        this.baseDir = baseDir;
        this.texts = texts;
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
        ArrayList<LibraryItem> clone = new ArrayList<>();
        clone.addAll(configuration.getValue());
        return new DirectoriesEditor(clone);
    }

    @Override
    public Object getValue(String attributeName) {
        if (attributeName.equals("canEditAsText")) // NOI18N
        {
            return Boolean.FALSE;
        }
        return super.getValue(attributeName);
    }

    private class DirectoriesEditor extends PropertyEditorSupport implements ExPropertyEditor {

        private final List<LibraryItem> value;
        private PropertyEnv env;

        public DirectoriesEditor(List<LibraryItem> value) {
            this.value = value;
        }

        @Override
        public void setAsText(String text) {
        }

        @Override
        public String getAsText() {
            boolean addSep = false;
            StringBuilder ret = new StringBuilder();
            for (int i = 0; i < value.size(); i++) {
                if (addSep) {
                    ret.append(", "); // NOI18N
                }
                ret.append(value.get(i).toString());
                addSep = true;
            }
            return ret.toString();
        }

        @Override
        public java.awt.Component getCustomEditor() {
            return new LibrariesPanel(project, conf, baseDir, value, this, env);
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
