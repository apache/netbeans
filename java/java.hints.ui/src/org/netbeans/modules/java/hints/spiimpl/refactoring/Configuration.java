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
package org.netbeans.modules.java.hints.spiimpl.refactoring;

import java.util.prefs.Preferences;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.spiimpl.RulesManager;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import static org.netbeans.modules.java.hints.spiimpl.options.HintsSettings.createPreferencesBasedHintsSettings;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Becicka
 */
public class Configuration {
    
    private String displayName;
    private String id;
    
    Configuration(String id, String displayName) {
        this.displayName = displayName;
        this.id = id;
        Preferences prefs = NbPreferences.forModule(this.getClass()).node(id());
        prefs.put("display.name", displayName);
    }
    
    private static final String PREFERENCES_LOCATION = "org/netbeans/modules/java/hints";
    public HintsSettings getSettings() {
        return createPreferencesBasedHintsSettings(NbPreferences.root().node(PREFERENCES_LOCATION).node(id), false, Severity.VERIFIER);
    }
    
    public String getDisplayName() {
        return displayName;
        
    }
    
    public String id() {
        return id;
    }

    public void setDisplayName(String displayName) {
        Preferences oldPrefs = NbPreferences.forModule(this.getClass()).node(id());
        oldPrefs.put("display.name", displayName);
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public void enable(String hintId) {
        for (HintMetadata hm : RulesManager.getInstance().readHints(null, null, null).keySet()) {
            if (hintId.equals(hm.id)) {
                getSettings().setEnabled(hm, true);
                return ;
            }
        }
    }
}
