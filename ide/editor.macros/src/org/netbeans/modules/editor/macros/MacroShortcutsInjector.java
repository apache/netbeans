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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.modules.editor.macros.storage.MacroDescription;
import org.netbeans.modules.editor.macros.storage.MacrosStorage;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.netbeans.modules.editor.settings.storage.spi.StorageFilter;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.editor.settings.storage.spi.StorageFilter.class)
public final class MacroShortcutsInjector extends StorageFilter<Collection<KeyStroke>, MultiKeyBinding> implements PropertyChangeListener {
    /**
     * How many times the Injector will try to load the macros from the editor storage.
     * Because of issue #223802, editor storage may not be yet initialized when the Injector is run.
     */
    private static final int MAX_RETRY_COUNT = 2;
    
    public static void refreshShortcuts() {
        Collection<? extends MacroShortcutsInjector> injectors = Lookup.getDefault().lookupAll(MacroShortcutsInjector.class);
        if (injectors.size() == 0) {
            LOG.warning("No MacroShortcutsInjector found in default Lookup"); //NOI18N
            return;
        } else if (injectors.size() > 1) {
            LOG.warning("Too many MacroShortcutsInjector instances found in default Lookup:"); //NOI18N
            for(MacroShortcutsInjector msi : injectors) {
                LOG.warning("  " + msi); //NOI18N
            }
        }

        injectors.iterator().next().notifyChanges();
//        assert instance != null;
//        LOG.fine("Shortcuts refresh forced, notifying 'Keybindings' storage..."); //NOI18N
//        instance.notifyChanges();
    }
    
    public MacroShortcutsInjector() {
        super("Keybindings"); //NOI18N
    }
    
    // ------------------------------------------------------------------------
    // StorageFilter implementation
    // ------------------------------------------------------------------------
    
    @Override
    public void afterLoad(Map<Collection<KeyStroke>, MultiKeyBinding> map, MimePath mimePath, String profile, boolean defaults) {
        Map<String, MacroDescription> macros = new HashMap<String, MacroDescription>();
        if (!collectMacroActions(mimePath, macros)) {
            return;
        }
        
        for(MacroDescription macro : macros.values()) {
            List<? extends MultiKeyBinding> shortcuts = macro.getShortcuts();
            for(MultiKeyBinding shortcut : shortcuts) {
                Collection<KeyStroke> keys = shortcut.getKeyStrokeList();

                // A macro shortcut never replaces shortcuts for ordinary editor actions
                if (!map.containsKey(keys)) {
                    map.put(keys, shortcut);
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("afterLoad: injecting " + keys + " for macro '" //NOI18N
                            + macro.getName() + "'; mimePath='" + mimePath.getPath() + "'"); //NOI18N
                    }
                } else {
                    LOG.warning("Shortcut " + keys + " is bound to '" + map.get(keys).getActionName() //NOI18N
                        + "' for '" + mimePath.getPath() + "' and will not be assigned to '" + macro.getName() + "' macro!"); //NOI18N
                }
            }
        }
    }

    @Override
    public void beforeSave(Map<Collection<KeyStroke>, MultiKeyBinding> map, MimePath mimePath, String profile, boolean defaults) {
        Set<Collection<KeyStroke>> keysToFilterOut = new HashSet<Collection<KeyStroke>>();
        
        for(Map.Entry<Collection<KeyStroke>,MultiKeyBinding> entry : map.entrySet()) {
            Collection<KeyStroke> keys = entry.getKey();
            MultiKeyBinding shortcut = entry.getValue();
            if (shortcut.getActionName().equals(MacroDialogSupport.RunMacroAction.runMacroAction)) {
                keysToFilterOut.add(keys);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("beforeSave: filtering out macro shortcut " + keys + "; mimePath='" + mimePath.getPath() + "'"); //NOI18N
                }
            }
        }
        
        map.keySet().removeAll(keysToFilterOut);
    }

    // ------------------------------------------------------------------------
    // PropertyChangeListener implementation
    // ------------------------------------------------------------------------
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null || EditorSettingsStorage.PROP_DATA.equals(evt.getPropertyName())) {
            LOG.fine("Macros storage changed, notifying 'Keybindings' storage...");
            notifyChanges();
        }
    }
    
    // ------------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(MacroShortcutsInjector.class.getName());
    
    private EditorSettingsStorage<String, MacroDescription> storage = null;
    
    /**
     * Counter of attempted refreshes
     */
    private int retryCount;
    
    private RequestProcessor.Task delayed;
    
    private boolean collectMacroActions(MimePath mimePath, Map<String, MacroDescription> macros) {
        if (storage == null) {
            storage = EditorSettingsStorage.<String, MacroDescription>find(MacrosStorage.ID);
            if (storage == null) {
                synchronized (this) {
                    if (delayed != null) {
                        return false;
                    }
                    if (retryCount++ < MAX_RETRY_COUNT) {
                        delayed = RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                synchronized (MacroShortcutsInjector.this) {
                                    delayed = null;
                                }
                                refreshShortcuts();
                            }
                        }, 500);
                    } else {
                        LOG.warning("Could not create macro shortcuts, editor settings storage not initialized yet.");
                    }
                }
                return false;
            }
            storage.addPropertyChangeListener(WeakListeners.propertyChange(this, storage));
        }

        try {
            macros.putAll(storage.load(mimePath, null, false));
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }
        return true;
    }

}
