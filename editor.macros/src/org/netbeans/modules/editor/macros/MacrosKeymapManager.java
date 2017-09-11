/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
