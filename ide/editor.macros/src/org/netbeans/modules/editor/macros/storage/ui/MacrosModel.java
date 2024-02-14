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

package org.netbeans.modules.editor.macros.storage.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.core.options.keymap.api.KeyStrokeUtils;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.modules.editor.macros.MacroDialogSupport;
import org.netbeans.modules.editor.macros.MacroShortcutsInjector;
import org.netbeans.modules.editor.macros.MacrosKeymapManager;
import org.netbeans.modules.editor.macros.storage.MacroDescription;
import org.netbeans.modules.editor.macros.storage.MacrosStorage;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Vita Stejskal
 */
public final class MacrosModel {

    private static final Logger LOG = Logger.getLogger(MacrosModel.class.getName());
    
    public static final int NAME_COLUMN_IDX = 0;
    public static final int SHORTCUTS_COLUMN_IDX = 1;
    public static final int COLUMN_COUNT = 2;
    
    public static synchronized MacrosModel get() {
        MacrosModel model = ref == null ? null : ref.get();
        if (model == null) {
            model = new MacrosModel();
            ref = new WeakReference<MacrosModel>(model);
        }
        return model;
    }
    
    public void load() {
        EditorSettingsStorage<String, MacroDescription> ess = EditorSettingsStorage.<String, MacroDescription>get(MacrosStorage.ID);
        
        mimeType2Macros = new HashMap<MimePath, Map<String, Macro>>();
        allMacrosList = new ArrayList<Macro>();
        
        Set<String> allMimeTypes = new HashSet<String>();
        allMimeTypes.add(""); //NOI18N
        allMimeTypes.addAll(EditorSettings.getDefault().getAllMimeTypes());
        
        for(String mt : allMimeTypes) {
            MimePath mimeType = MimePath.parse(mt);
            Map<String, MacroDescription> macros;
            try {
                macros = ess.load(mimeType, null, false);
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, "Can't load macros for '" + mimeType + "'", ioe); //NOI18N
                continue;
            }
            
            Map<String, Macro> map = mimeType2Macros.get(mimeType);
            if (map == null) {
                map = new HashMap<String, Macro>(macros.size());
                mimeType2Macros.put(mimeType, map);
            }
            
            for(Map.Entry<String, MacroDescription> entry : macros.entrySet()) {
                String macroName = entry.getKey();
                MacroDescription md = entry.getValue();
                Macro macro = new Macro(this, mimeType, md);
                
                map.put(macroName, macro);
                allMacrosList.add(macro);
            }
        }
        
        allMacrosList.sort(MACRO_COMPARATOR);

        loaded = true;
        tableModel.fireTableDataChanged();
        setChanged(false);
    }
    
    public void save() {
        EditorSettingsStorage<String, MacroDescription> ess = EditorSettingsStorage.<String, MacroDescription>get(MacrosStorage.ID);
        
        for(Map.Entry<MimePath, Map<String, Macro>> mimeType2MacrosEntry : mimeType2Macros.entrySet()) {
            MimePath mimeType = mimeType2MacrosEntry.getKey();
            Map<String, Macro> map = mimeType2MacrosEntry.getValue();

            Map<String, MacroDescription> macros = new HashMap<String, MacroDescription>(map.size());
            for(Map.Entry<String, Macro> macroEntry : map.entrySet()) {
                String macroName = macroEntry.getKey();
                Macro macro = macroEntry.getValue();
                macros.put(macroName, macro.getMacroDescription());
            }
            
            try {
                ess.save(mimeType, null, false, macros);
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, "Can't save macros for '" + mimeType + "'", ioe); //NOI18N
            }
        }
        
        setChanged(false);
        
        MacroShortcutsInjector.refreshShortcuts();
    }
    
    public boolean isLoaded() {
        return loaded;
    }
    
    public boolean isChanged() {
        return changed;
    }
    
    public TableModel getTableModel() {
        return tableModel;
    }
    
// XXX: currently not used
//    public Macro getMacroByName(MimePath mimeType, String macroName) {
//        assert mimeType != null : "The mimeType parameter must not be null"; //NOI18N
//        assert macroName != null : "The macroName parameter must not be null"; //NOI18N
//        
//        Macro macro = null;
//        Map<String, Macro> map = mimeType2Macros.get(mimeType);
//        if (map != null) {
//            macro = map.get(macroName);
//        }
//        
//        return macro;
//    }

    public String validateMacroName(String macroName) {
        if (macroName == null || macroName.trim().length() == 0) {
            return NbBundle.getMessage(MacrosModel.class, "CTL_Empty_Macro_Name"); //NOI18N
        }

        Map<String, Macro> map = mimeType2Macros.get(MimePath.EMPTY);
        if (map.containsKey(macroName)) {
            return NbBundle.getMessage(MacrosModel.class, "CTL_Duplicate_Macro_Name"); //NOI18N
        }
        
        return null;
    }
    
    public Macro getMacroByIndex(int tableRow) {
        return allMacrosList.get(tableRow);
    }
    
    public Macro createMacro(MimePath mimeType, String name) {
        Macro macro = new Macro(this, mimeType, name, "", Collections.<MultiKeyBinding>emptyList());
        
        Map<String, Macro> map = mimeType2Macros.get(mimeType);
        if (map == null) {
            map = new HashMap<String, Macro>();
            mimeType2Macros.put(mimeType, map);
        }
        
        if (map.containsKey(name)) {
            throw new IllegalArgumentException("Can't create macro, the name is already used: '" + name + "'"); //NOI18N
        }
        
        map.put(name, macro);
        allMacrosList.add(macro);
        
        tableModel.fireTableRowsInserted(allMacrosList.size() - 1, allMacrosList.size() - 1);
        fireChanged();
        
        return macro;
    }
    
    public void deleteMacro(int tableRow) {
        Macro macro = allMacrosList.get(tableRow);
        macro.setShortcuts(Collections.<String>emptySet());
        allMacrosList.remove(tableRow);
        Map<String, Macro> map = mimeType2Macros.get(macro.getMimeType());
        map.remove(macro.getName());
        
        tableModel.fireTableRowsDeleted(tableRow, tableRow);
        fireChanged();
    }

    public List<Macro> getAllMacros() {
        return Collections.unmodifiableList(allMacrosList);
    }
    
    public static final class Macro implements ShortcutAction {
        
        public static final String PROP_CODE = "Macro.PROP_CODE"; //NOI18N
        public static final String PROP_SHORTCUTS = "Macro.PROP_SHORTCUTS"; //NOI18N
        
        public MimePath getMimeType() {
            return mimeType;
        }

        public String getName() {
            return macroDescription != null ? macroDescription.getName() : name;
        }

        public String getCode() {
            return macroDescription != null ? macroDescription.getCode() : code;
        }

        public void setCode(String code) {
            if (Utilities.compareObjects(getCode(), code)) {
                return;
            }
            
            cloneOnModify();
            
            String oldCode = this.code;
            this.code = code;

            pcs.firePropertyChange(PROP_CODE, oldCode, code);
            model.fireChanged();
        }

        public List<? extends MultiKeyBinding> getShortcuts() {
            return macroDescription != null ? macroDescription.getShortcuts() : shortcuts;
        }

        // shortcuts is in Ctrl+A form
        public void setShortcut(String shortcut) {
            KeyStroke[] strokes = KeyStrokeUtils.getKeyStrokes(shortcut);
            if (strokes == null) {
                LOG.warning("Could not decode keystrokes from: " + shortcut);
                return;
            }
            List<? extends MultiKeyBinding> list = Collections.singletonList(
                new MultiKeyBinding(strokes, 
                    MacroDialogSupport.RunMacroAction.runMacroAction)); //NOI18N
            
            List<? extends MultiKeyBinding> current = getShortcuts();
            // ignores changes iff just order of shortcuts has been changed;
            // this is consistent with key binding storage.
            if (current != null && list != null &&
                current.size() == list.size() &&
                current.containsAll(list)) {
                return;
            }
            
            cloneOnModify();
            
            List<? extends MultiKeyBinding> oldShortcuts = this.shortcuts;
            this.shortcuts = list;
            
            pcs.firePropertyChange(PROP_SHORTCUTS, oldShortcuts, shortcuts);
            model.fireTableModelChange(this, SHORTCUTS_COLUMN_IDX);
            model.fireChanged();
        }

        // shortcuts are in M-A form, delimited by SPACE
        public void setShortcuts(Set<String> shortcuts) {
            List<MultiKeyBinding> list = new ArrayList<MultiKeyBinding>(shortcuts.size());
            
            for (String shortcut : shortcuts) {
                KeyStroke[] keys = Utilities.stringToKeys(shortcut);
                if (keys == null) {
                    LOG.warning("Could not decode keystrokes from: " + shortcut);
                } else {
                    list.add(new MultiKeyBinding(keys, MacroDialogSupport.RunMacroAction.runMacroAction)); //NOI18N
                }
            }
            
            if (Utilities.compareObjects(new HashSet<MultiKeyBinding>(getShortcuts()), new HashSet<MultiKeyBinding>(list))) {
                return;
            }
            
            cloneOnModify();
            
            List<? extends MultiKeyBinding> oldShortcuts = this.shortcuts;
            this.shortcuts = list;
            
            pcs.firePropertyChange(PROP_SHORTCUTS, oldShortcuts, shortcuts);
            model.fireTableModelChange(this, SHORTCUTS_COLUMN_IDX);
            model.fireChanged();
        }
        
        // -----------------------------------------------------------------
        // ShortcutAction implementation
        // -----------------------------------------------------------------
        
        public String getDisplayName() {
            String languageName = EditorSettings.getDefault().getLanguageName(mimeType.getPath()).toLowerCase();
            if (mimeType == MimePath.EMPTY) {
                return NbBundle.getMessage(MacrosModel.class, "MacroAction_DisplayName_1", getName(), languageName); //NOI18N
            } else {
                return NbBundle.getMessage(MacrosModel.class, "MacroAction_DisplayName_2", getName(), languageName); //NOI18N
            }
        }

        public String getId() {
            return getName() + "@" + mimeType.getPath(); //NOI18N
        }

        public String getDelegatingActionId() {
            return null;
        }

        public ShortcutAction getKeymapManagerInstance(String keymapManagerName) {
            if (keymapManagerName != null && keymapManagerName.equals(MacrosKeymapManager.class.getName())) {
                return this;
            } else {
                return null;
            }
        }

        // -----------------------------------------------------------------
        // private implementation
        // -----------------------------------------------------------------
        
        private final MacrosModel model;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final MimePath mimeType;
        
        private MacroDescription macroDescription;
        
        private String name;
        private String code;
        private List<? extends MultiKeyBinding> shortcuts;

        private Macro(MacrosModel model, MimePath mimeType, String name, String code, List<? extends MultiKeyBinding> shortcuts) {
            this.model = model;
            this.mimeType = mimeType;
            this.name = name;
            this.code = code;
            this.shortcuts = shortcuts;
        }

        private Macro(MacrosModel model, MimePath mimeType, MacroDescription macroDescription) {
            this.model = model;
            this.mimeType = mimeType;
            this.macroDescription = macroDescription;
        }
        
        private void cloneOnModify() {
            if (macroDescription != null) {
                this.name = macroDescription.getName();
                this.code = macroDescription.getCode();
                this.shortcuts = macroDescription.getShortcuts();
                this.macroDescription = null;
            }
        }
        
        private MacroDescription getMacroDescription() {
            return macroDescription != null ? macroDescription : new MacroDescription(name, code, null, shortcuts);
        }
    } // End of Macro class
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    // --------------------------------------------------------------
    // private implementation
    // --------------------------------------------------------------
    
    private Map<MimePath, Map<String, Macro>> mimeType2Macros = new HashMap<MimePath, Map<String, MacrosModel.Macro>>();
    private List<Macro> allMacrosList = new ArrayList<Macro>();
    private final MacrosTableModel tableModel = new MacrosTableModel();
    private boolean changed = false;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean loaded = false;
    private static Reference<MacrosModel> ref = null;
    
    private static final Comparator<Macro> MACRO_COMPARATOR = new Comparator<MacrosModel.Macro>() {
        public int compare(Macro o1, Macro o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };
    
    private MacrosModel() {
        // no-op
    }

    private void fireTableModelChange(Macro m, int columnIndex) {
        assert columnIndex >= 0 && columnIndex < COLUMN_COUNT;
        
	//wrong assumption that list will be ordered in the same way as table
//        int rowIndex = allMacrosList.indexOf(m);
//        assert rowIndex != -1;
        
//        tableModel.fireTableCellUpdated(rowIndex, columnIndex);
	tableModel.fireTableDataChanged();
    }
    
    private void fireChanged() {
        EditorSettingsStorage<String, MacroDescription> ess = EditorSettingsStorage.<String, MacroDescription>get(MacrosStorage.ID);

        for (Map.Entry<MimePath, Map<String, Macro>> entry : mimeType2Macros.entrySet()) {
            MimePath mimeType = entry.getKey();
            Map<String, Macro> map = entry.getValue();
            Map<String, MacroDescription> current = new HashMap<>(map.size());
            for (String macroName : map.keySet()) {
                current.put(macroName, map.get(macroName).getMacroDescription());
            }
            try {
                if (!ess.load(mimeType, null, false).equals(current)) {
                    setChanged(true);
                    return;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        setChanged(false);
    }

    private void setChanged(boolean changed) {
        if (this.changed == changed) {
            return;
        }
        this.changed = changed;
        pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, !changed, changed);
    }
    
    private final class MacrosTableModel extends AbstractTableModel {

        public int getColumnCount() {
            return COLUMN_COUNT;
        }

        public int getRowCount() {
            return allMacrosList.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case NAME_COLUMN_IDX: // name
                    return allMacrosList.get(rowIndex).getName();
                case SHORTCUTS_COLUMN_IDX: // shortcuts
                    StringBuilder sb = new StringBuilder();
                    for(int j = 0; j < allMacrosList.get(rowIndex).getShortcuts().size(); j++) {
                        MultiKeyBinding mkb = allMacrosList.get(rowIndex).getShortcuts().get(j);
                        List<KeyStroke> list = mkb.getKeyStrokeList();
                        KeyStroke[] arr = list.toArray(new KeyStroke[0]);
                        sb.append(KeyStrokeUtils.getKeyStrokesAsText(arr, " ")); // NOI18N
                        if (j + 1 < allMacrosList.get(rowIndex).getShortcuts().size()) {
                            sb.append(", "); //NOI18N
                        }
                    }
                    return sb.toString();
                default:
                    throw new ArrayIndexOutOfBoundsException("Invalid column index: " + columnIndex); //NOI18N
            }
        }

        public @Override Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        public @Override String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case NAME_COLUMN_IDX: // name
                    return NbBundle.getMessage(MacrosModel.class, "MacrosTable_Name_Column_Title"); //NOI18N
                case SHORTCUTS_COLUMN_IDX: // shortcut
                    return NbBundle.getMessage(MacrosModel.class, "MacrosTable_Shortcut_Column_Title"); //NOI18N
                default:
                    throw new ArrayIndexOutOfBoundsException("Invalid column index: " + columnIndex); //NOI18N
            }
        }

        public @Override boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    } // End of MacrosTableModel class
}
