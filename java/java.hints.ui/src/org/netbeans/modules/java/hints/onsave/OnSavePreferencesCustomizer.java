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
package org.netbeans.modules.java.hints.onsave;

import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.util.HelpCtx;

/**
 *
 * @author lahvac
 */
public class OnSavePreferencesCustomizer implements PreferencesCustomizer {

    private final Preferences preferences;
    private OnSaveCustomizer component;

    public OnSavePreferencesCustomizer(Preferences preferences) {
        this.preferences = preferences;
    }
    
    @Override
    public String getId() {
        return "java-hints-on-save";
    }

    @Override
    public String getDisplayName() {
        return "Java";
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public JComponent getComponent() {
        if (component == null) {
            component = new OnSaveCustomizer(preferences);
        }
        return component;
    }
    
    public static final class FactoryImpl implements Factory {

        @Override
        public PreferencesCustomizer create(Preferences preferences) {
            return new OnSavePreferencesCustomizer(preferences);
        }
        
    }
    
    public static final class CustomCustomizerImpl extends CustomCustomizer {
        @Override
        public String getSavedValue(PreferencesCustomizer customCustomizer, String key) {
            if (customCustomizer instanceof OnSavePreferencesCustomizer) {
                return ((OnSaveCustomizer) customCustomizer.getComponent()).getSavedValue(key);
            }
            return null;
        }
    }
    
}
