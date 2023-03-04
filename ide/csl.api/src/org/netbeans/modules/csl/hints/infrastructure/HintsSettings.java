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
package org.netbeans.modules.csl.hints.infrastructure;

import java.util.prefs.Preferences;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.HintsProvider.HintsManager;
import org.netbeans.modules.csl.api.Rule.UserConfigurableRule;
import org.openide.util.NbPreferences;

/**
 *
 * @author Petr Hrebejk
 * @author Jan Lahoda
 */
public class HintsSettings {

    // Only used for categories (disabled state in options dialog)
    static final HintSeverity SEVERITY_DEFAUT = HintSeverity.WARNING;
    static final boolean IN_TASK_LIST_DEFAULT = true;
    
    static final String ENABLED_KEY = "enabled";         // NOI18N
    static final String SEVERITY_KEY = "severity";       // NOI18N
    static final String IN_TASK_LIST_KEY = "inTaskList"; // NOI18N
    
    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    
    private HintsSettings() {
    }
 
    public static String getCurrentProfileId() {
        return DEFAULT_PROFILE;
    }
    
    /** Gets preferences node, which stores the options for given hint. It is not
     * necessary to override this method unless you want to create some special
     * behavior. The default implementation will create the the preferences node
     * by calling <code>NbPreferences.forModule(this.getClass()).node(profile).node(getId());</code>
     * @profile Profile to get the node for. May be null for current profile
     * @return Preferences node for given hint.
     */
    public static Preferences getPreferences(GsfHintsManager manager, UserConfigurableRule rule, String profile) { 
        profile = profile == null ? HintsSettings.getCurrentProfileId() : profile;
        return NbPreferences.forModule(HintsSettings.class).node(profile).node(manager.getId() + rule.getId());
    }
    
    public static HintSeverity getSeverity(GsfHintsManager manager, UserConfigurableRule rule) {
        return getSeverity(rule, getPreferences(manager, rule, getCurrentProfileId()));        
    }
    
    /** For current profile
     */ 
    public static boolean isEnabled(GsfHintsManager manager, UserConfigurableRule hint ) {
        Preferences p = getPreferences(manager, hint, HintsSettings.getCurrentProfileId());
        return isEnabled(manager, hint, p);
    }
    
    /** For current profile
     */ 
    public static boolean isShowInTaskList(GsfHintsManager manager, UserConfigurableRule hint ) {
        Preferences p = getPreferences(manager, hint, HintsSettings.getCurrentProfileId());
        return isShowInTaskList(hint, p);
    }
    
      
    public static boolean isEnabled(HintsManager manager, UserConfigurableRule hint, Preferences preferences ) {        
        return preferences.getBoolean(ENABLED_KEY, hint.getDefaultEnabled());
    }
    
    public static void setEnabled( Preferences p, boolean value ) {
        p.putBoolean(ENABLED_KEY, value);
    }
      
    public static boolean isShowInTaskList(UserConfigurableRule hint, Preferences preferences ) {
        return preferences.getBoolean(IN_TASK_LIST_KEY, hint.showInTasklist());
    }
    
    public static void setShowInTaskList( Preferences p, boolean value ) {
        p.putBoolean(IN_TASK_LIST_KEY, value);
    }
      
    public static HintSeverity getSeverity(UserConfigurableRule hint, Preferences preferences ) {
        String s = preferences.get(SEVERITY_KEY, null );
        return s == null ? hint.getDefaultSeverity() : HintSeverity.valueOf(s);
    }
    
    public static void setSeverity( Preferences p, HintSeverity severity ) {
        p.put(SEVERITY_KEY, severity.name());
    }
}
