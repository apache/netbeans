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

import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

public class BooleanNodeProp extends PropertySupport<Boolean> {

    private final CodeStyle.Language language;
    private final String optionID;
    private PreviewPreferences preferences;
    private boolean state;

    public BooleanNodeProp(CodeStyle.Language language, PreviewPreferences preferences, String optionID) {
        super(optionID, Boolean.class, getString("LBL_" + optionID), getString("HINT_" + optionID), true, true); // NOI18N
        this.language = language;
        this.optionID = optionID;
        this.preferences = preferences;
        init();
    }

    // create read only property
    public BooleanNodeProp(CodeStyle.Language language, PreviewPreferences preferences, String optionID, boolean state) {
        super(optionID, Boolean.class, getString("LBL_" + optionID), getString("HINT_" + optionID), true, false); // NOI18N
        this.language = language;
        this.optionID = optionID;
        this.preferences = preferences;
        this.state = state;
    }

    private static String getString(String key) {
        return NbBundle.getMessage(BooleanNodeProp.class, key);
    }

    private void init() {
        state = getPreferences().getBoolean(optionID, getDefault());
    }

    private boolean getDefault(){
        return (Boolean) EditorOptions.getDefault(
                getPreferences().getLanguage(), getPreferences().getStyleId(), optionID);
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
    public Boolean getValue() {
        return Boolean.valueOf(state);
    }

    @Override
    public void setValue(Boolean v) {
        state = v;
        getPreferences().putBoolean(optionID, state);
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
        return getDefault() == getValue().booleanValue();
    }
}
