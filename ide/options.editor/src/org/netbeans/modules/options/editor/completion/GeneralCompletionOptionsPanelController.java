/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.options.editor.completion;

import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.util.HelpCtx;

/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
public final class GeneralCompletionOptionsPanelController implements  PreferencesCustomizer {

    private Preferences preferences;
    private GeneralCompletionOptionsPanel generalCompletionOptionsPanel;

    public GeneralCompletionOptionsPanelController(Preferences p) {
        this.preferences = p;
    }

    public String getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("netbeans.optionsDialog.editor.codeCompletion"); //NOI18N
    }

    public JComponent getComponent() {
        if (generalCompletionOptionsPanel == null) {
            generalCompletionOptionsPanel = new GeneralCompletionOptionsPanel(preferences);
        }
        return generalCompletionOptionsPanel;
    }
    
    public static final class CustomCustomizerImpl extends PreferencesCustomizer.CustomCustomizer {
        @Override
        public String getSavedValue(PreferencesCustomizer customCustomizer, String key) {
            if (customCustomizer instanceof GeneralCompletionOptionsPanelController) {
                return ((GeneralCompletionOptionsPanel) customCustomizer.getComponent()).getSavedValue(key);
            }
            return null;
        }
    }
}

