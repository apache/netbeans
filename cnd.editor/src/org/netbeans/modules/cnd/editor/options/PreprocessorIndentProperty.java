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
public class PreprocessorIndentProperty extends PropertySupport.ReadWrite<CodeStyle.PreprocessorIndent> {

    private final CodeStyle.Language language;
    private final String optionID;
    private final PreviewPreferences preferences;
    private CodeStyle.PreprocessorIndent state;
    private PropertyEditor editor;

    public PreprocessorIndentProperty(CodeStyle.Language language, PreviewPreferences preferences, String optionID) {
        super(optionID, CodeStyle.PreprocessorIndent.class, getString("LBL_"+optionID), getString("HINT_"+optionID)); // NOI18N
        this.language = language;
        this.optionID = optionID;
        this.preferences = preferences;
        init();
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(BracePlacementProperty.class, key);
    }

    private void init() {
        state = CodeStyle.PreprocessorIndent.valueOf(getPreferences().get(optionID, getDefault().name()));
    }

    private CodeStyle.PreprocessorIndent getDefault(){
        return CodeStyle.PreprocessorIndent.valueOf((String) EditorOptions.getDefault(
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
    public CodeStyle.PreprocessorIndent getValue() {
        return state;
    }

    @Override
    public void setValue(CodeStyle.PreprocessorIndent v) {
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
            editor = new PreprocessorIndentEditor();
        }
        return editor;
    }

    private static class PreprocessorIndentEditor extends PropertyEditorSupport {
        @Override
        public String[] getTags() {
            try {
                CodeStyle.PreprocessorIndent[] values = CodeStyle.PreprocessorIndent.values();
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
                CodeStyle.PreprocessorIndent[] values = CodeStyle.PreprocessorIndent.values();
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
            CodeStyle.PreprocessorIndent e = (CodeStyle.PreprocessorIndent) getValue();
            return e != null ? CodeStyle.PreprocessorIndent.class.getName().replace('$', '.') + '.' + e.name() : "null"; // NOI18N
        }
    }
}
