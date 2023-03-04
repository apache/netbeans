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

package org.netbeans.modules.editor.macros;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.netbeans.modules.editor.macros.storage.ui.MacrosModel;
import org.netbeans.modules.editor.settings.storage.spi.support.StorageSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Vita Stejskal
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.core.options.keymap.spi.KeymapManager.class)
public final class MacrosKeymapManager extends KeymapManager {

    private MacrosModel model = null;
    private Map<String, Set<ShortcutAction>> macroActionsMap = null;
    private Map<ShortcutAction, Set<String>> macroShortcutsMap = null;
    
    public MacrosKeymapManager() {
        super(MacrosKeymapManager.class.getName());
    }
    
    public @Override Map<String, Set<ShortcutAction>> getActions() {
        // <category, ShortcutAction>
        if (macroActionsMap == null) {
            List<MacrosModel.Macro> allMacrosList = getModel().getAllMacros();
            Set<ShortcutAction> macroActions = new HashSet<ShortcutAction>(allMacrosList.size());
            macroShortcutsMap = new HashMap<ShortcutAction, Set<String>>(allMacrosList.size());
            for(MacrosModel.Macro macro : allMacrosList) {
                // Crate ShortcutAction for macro
                macroActions.add(macro);
                
                // Compute all macro's shortcuts
                Set<String> shortcuts = new HashSet<String>();
                for(MultiKeyBinding mkb : macro.getShortcuts()) {
                    // keyStrokesToString uses $, but it's an API and I cannot change its behaviour
                    String shortcut = StorageSupport.keyStrokesToString(mkb.getKeyStrokeList(), true);
                    assert shortcut != null;
                    // Keymaps work with " " as the keystroke keyseparator
                    shortcuts.add(shortcut.replace("$", " "));
                }
                if (shortcuts.size() > 0) {
                    macroShortcutsMap.put(macro, shortcuts);
                }
            }
            
            macroActionsMap = new HashMap<String, Set<ShortcutAction>>();
            macroActionsMap.put(NbBundle.getMessage(MacrosKeymapManager.class, "Macros_Keymap_Category"), macroActions); //NOI18N
        }
        
        return macroActionsMap;
    }

    public @Override void refreshActions() {
        macroActionsMap = null;
    }

    public @Override Map<ShortcutAction, Set<String>> getKeymap(String profileName) {
        // <ShortcutAction, Set<shortcut>>
        getActions();
        return macroShortcutsMap;
    }

    public @Override Map<ShortcutAction, Set<String>> getDefaultKeymap(String profileName) {
        // <ShortcutAction, Set<shortcut>>
        return getKeymap(profileName);
    }

    public @Override void saveKeymap(String profileName, Map<ShortcutAction, Set<String>> actionToShortcuts) {
        // <ShortcutAction, Set<shortcut>>
        
        for(ShortcutAction shortcutAction : actionToShortcuts.keySet()) {
            if (!(shortcutAction instanceof MacrosModel.Macro)) {
                continue;
            }
            
            MacrosModel.Macro macro = (MacrosModel.Macro) shortcutAction;
            Set<String> shortcuts = actionToShortcuts.get(shortcutAction);
            macro.setShortcuts(shortcuts);
        }
        
        getModel().save();
        macroActionsMap = null;
    }

    public @Override List<String> getProfiles() {
        return null;
    }

    public @Override String getCurrentProfile() {
        return null;
    }

    public @Override void setCurrentProfile(String profileName) {
        // no-op
    }

    public @Override void deleteProfile(String profileName) {
        // no-op
    }

    public @Override boolean isCustomProfile(String profileName) {
        return false;
    }

    private MacrosModel getModel() {
        if (model == null) {
            model = MacrosModel.get();
            if (!model.isLoaded()) {
                model.load();
            }
        }
        return model;
    }
}
