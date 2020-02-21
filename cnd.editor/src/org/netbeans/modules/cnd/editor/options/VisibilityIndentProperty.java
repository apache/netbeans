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

package org.netbeans.modules.cnd.editor.options;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 *
 */
public class VisibilityIndentProperty extends PropertySupport.ReadWrite<CodeStyle.VisibilityIndent> {

    private final CodeStyle.Language language;
    private final String optionID;
    private final PreviewPreferences preferences;
    private CodeStyle.VisibilityIndent state;
    private PropertyEditor editor;

    public VisibilityIndentProperty(CodeStyle.Language language, PreviewPreferences preferences, String optionID) {
        super(optionID, CodeStyle.VisibilityIndent.class, getString("LBL_"+optionID), getString("HINT_"+optionID)); // NOI18N
        this.language = language;
        this.optionID = optionID;
        this.preferences = preferences;
        init();
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(BracePlacementProperty.class, key);
    }

    private void init() {
        state = CodeStyle.VisibilityIndent.valueOf(getPreferences().get(optionID, getDefault().name()));
    }

    private CodeStyle.VisibilityIndent getDefault(){
        return CodeStyle.VisibilityIndent.valueOf((String) EditorOptions.getDefault(
                getPreferences().getLanguage(), getPreferences().getStyleId(), optionID));
    }

    private PreviewPreferences getPreferences() {
        return preferences;
    }

    @Override
    public String getHtmlDisplayName() {
        if (!isDefaultValue()) {
            return "<b>" + getDisplayName(); // NOI18N
        }
        return null;
    }

    @Override
    public CodeStyle.VisibilityIndent getValue() {
        return state;
    }

    @Override
    public void setValue(CodeStyle.VisibilityIndent v) {
        state = v;
        getPreferences().put(optionID, state.name());
    }

    @Override
    public void restoreDefaultValue() {
        setValue(getDefault());
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public boolean isDefaultValue() {
        return getDefault().equals(getValue());
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        if (editor == null) {
            editor = new VisibilityIndentEditor();
        }
        return editor;
    }

    private static class VisibilityIndentEditor extends PropertyEditorSupport {
        @Override
        public String[] getTags() {
            try {
                CodeStyle.VisibilityIndent[] values = CodeStyle.VisibilityIndent.values();
                String[] tags = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    tags[i] = values[i].toString();
                }
                return tags;
            } catch (Exception x) {
                throw new AssertionError(x);
            }
        }

        @Override
        public String getAsText() {
            Object o = getValue();
            return o != null ? o.toString() : ""; // NOI18N
        }

        @Override
        public void setAsText(String text) {
            if (text.length() > 0) {
                CodeStyle.VisibilityIndent[] values = CodeStyle.VisibilityIndent.values();
                for (int i = 0; i < values.length; i++) {
                    if (values[i].toString().equals(text)) {
                        setValue(values[i]);
                        return;
                    }
                }
            }
            setValue(null);
        }

        @Override
        public String getJavaInitializationString() {
            CodeStyle.VisibilityIndent e = (CodeStyle.VisibilityIndent) getValue();
            return e != null ? CodeStyle.VisibilityIndent.class.getName().replace('$', '.') + '.' + e.name() : "null"; // NOI18N
        }
    }
}
