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
package org.netbeans.modules.options.keymap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.TextAction;
import org.netbeans.core.options.keymap.api.KeyStrokeUtils;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.api.ShortcutsFinder;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service= ShortcutsFinder.class)
public class ShortcutsFinderImpl implements ShortcutsFinder {
    protected KeymapModel model;
    
    // Map (String (keymapName) > Map (ShortcutAction > Set (String (shortcut Ctrl+F)))).
    private volatile Map<String, Map<ShortcutAction, Set<String>>> shortcutsCache = 
            Collections.emptyMap();

    public ShortcutsFinderImpl() {
        this(KeymapModel.create());
    }

    public ShortcutsFinderImpl(KeymapModel model) {
        this.model = model;
    }

    @Override
    public ShortcutAction findActionForShortcut(String sc) {
        for (String c : model.getActionCategories()) {
            for (ShortcutAction action : model.getActions(c)) {
                String[] shortcuts = getShortcuts (action);
                int i, k = shortcuts.length;
                for (i = 0; i < k; i++) {
                    if (shortcuts[i].equals(sc)) {
                        if (isImpliedAction(action)) { // NOI18N
                            continue;
                        }
                        return action;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Determines if the action is "implied" by some other settings. Such action will not be
     * returned as conflicts to the user when searching for a shortcut.
     * @param action
     * @return 
     */
    protected boolean isImpliedAction(ShortcutAction action) {
        // special hack for macros; the RunMacro action gets all macro shortcuts assinged
        return action != null && "run-macro".equals(action.getId()); // NOI18N
    }

    @Override
    public ShortcutAction findActionForId(String actionId) {
        if (model.isDuplicateId(actionId)) {
            return null;
        }
        ShortcutAction ac = findActionForId(actionId, "", false); // NOI18N
        if (ac == null) {
            ac = findActionForId(actionId, "", true); // NOI18N
        }
        return ac;
    }

    protected ShortcutAction findActionForId (String actionId, String category, boolean delegate) {
        if (!category.isEmpty()) {
            throw new IllegalArgumentException();
        }
        for (String c : model.getActionCategories()) {
            for (ShortcutAction action : model.getActions(c)) {
                String id;

                if (delegate) {
                    // fallback for issue #197068 - try to find actions also by their classname:
                    id = LayersBridge.getOrigActionClass(action);
                } else {
                    id = action.getId ();
                }
                if (id != null && actionId.equals (id)) { 
                    return action;
                }
            }
        }
        return null;
    }

    @Override
    public String showShortcutsDialog() {
        final ShortcutsDialog d = new ShortcutsDialog ();
        d.init(this);
        final DialogDescriptor descriptor = new DialogDescriptor (
            d,
            loc ("Add_Shortcut_Dialog"),
            true,
            new Object[] {
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.CANCEL_OPTION
            },
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.DEFAULT_ALIGN,
            null, 
            d.getListener()
        );
        descriptor.setClosingOptions (new Object[] {
            DialogDescriptor.OK_OPTION,
            DialogDescriptor.CANCEL_OPTION
        });
        descriptor.setAdditionalOptions (new Object [] {
            d.getBClear(), d.getBTab()
        });
        descriptor.setValid(d.isShortcutValid());
        d.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() == null || ShortcutsDialog.PROP_SHORTCUT_VALID.equals(evt.getPropertyName())) {
                    descriptor.setValid(d.isShortcutValid());
                }
            }
        });
        
        DialogDisplayer.getDefault ().notify (descriptor);
        if (descriptor.getValue () == DialogDescriptor.OK_OPTION)
            return d.getTfShortcut().getText ();
        return null;
    }
    
    protected String getCurrentProfile() {
        return model.getCurrentProfile();
    }

    @Override
    public String[] getShortcuts(ShortcutAction action) {
        String currentProfile = getCurrentProfile();
        Map<ShortcutAction, Set<String>> profileMap = getProfileMap(currentProfile);
        Set<String> shortcuts = profileMap.get (action);
        if (shortcuts == null) {
            return new String [0];
        }
        return shortcuts.toArray (new String [0]);
    }
    
    protected void clearShortcuts(String profile) {
        shortcutsCache.remove(profile);
    }
    
    protected Map<ShortcutAction,Set<String>> getKeymap (String profile) {
        return model.getKeymap (profile);
    }
    
    /**
     * Provides mapping of actions to their (non modified) shortcuts for a profile
     * @param profile given profile
     * @return the mapping
     */
    protected Map<ShortcutAction, Set<String>> getProfileMap(String profile) {
        Map<ShortcutAction, Set<String>> res = shortcutsCache.get(profile);
        if (res == null) {
            // read profile and put it to cache
            Map<ShortcutAction, Set<String>> profileMap = convertFromEmacs (getKeymap(profile));
            synchronized (this) {
                res = shortcutsCache.get(profile);
                if (res == null) {
                    Map<String, Map<ShortcutAction, Set<String>>> m = new HashMap<>(shortcutsCache);
                    m.put(profile, profileMap);
                    shortcutsCache = m;
                    res = profileMap;
                }
            }
        }
        return res;
    }

    @Override
    public void refreshActions() {
        clearCache();
        model.refreshActions ();
    }
    
    protected void clearCache() {
        shortcutsCache = Collections.emptyMap();
    }

    @Override
    public void setShortcuts(ShortcutAction action, Set<String> shortcuts) {
        throw new UnsupportedOperationException("Finder must be cloned first");
    }

    @Override
    public void apply() {
        throw new UnsupportedOperationException("Finder must be cloned first");
    }
    
    protected static String loc (String key) {
        return NbBundle.getMessage (KeymapPanel.class, key);
    }
    
    /**
     * Converts Map (ShortcutAction > Set (String (shortcut AS-P))) to 
     * Map (ShortcutAction > Set (String (shortcut Alt+Shift+P))).
     */
    protected static Map<ShortcutAction, Set<String>> convertFromEmacs (Map<ShortcutAction, Set<String>> emacs) {
        Map<ShortcutAction, Set<String>> result = new HashMap<ShortcutAction, Set<String>> ();
        for (Map.Entry<ShortcutAction, Set<String>> entry: emacs.entrySet()) {
            ShortcutAction action = entry.getKey();
            Set<String> shortcuts = new LinkedHashSet<String> ();
            for (String emacsShortcut: entry.getValue()) {
                KeyStroke[] keyStroke = Utilities.stringToKeys (emacsShortcut);
                shortcuts.add (KeyStrokeUtils.getKeyStrokesAsText (keyStroke, " "));
            }
            result.put (action, shortcuts);
        }
        return result;
    }
    
    public ShortcutsFinder.Writer localCopy() {
        MutableShortcutsModel local = new MutableShortcutsModel(model, this);
        model.getActionCategories();
        model.getKeymap(model.getCurrentProfile());
        return local;
    }
    
}
