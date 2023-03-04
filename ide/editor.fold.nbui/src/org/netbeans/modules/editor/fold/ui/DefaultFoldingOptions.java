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
package org.netbeans.modules.editor.fold.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.OverridePreferences;
import org.openide.util.WeakListeners;

/**
 * Default implementation of folding options. The dialog will only display
 * checboxes, for each of the registered FoldTypes. FoldTypes are displayed
 * in same order, as they are enumerated by FoldUtilities.getFoldTypes().
 * <p/>
 * The dialog will read and set preference values {@code code-folding-collapse-X},
 * where X is obtained from {@link FoldType#code()}.
 *
 * @author sdedic
 */
public final class DefaultFoldingOptions extends javax.swing.JPanel 
implements PreferenceChangeListener, ChangeListener, CustomizerWithDefaults, ItemListener {
    
    private static final Logger LOG = Logger.getLogger(DefaultFoldingOptions.class.getName());
    
    public static final String COLLAPSE_PREFIX = FoldUtilitiesImpl.PREF_COLLAPSE_PREFIX;
    
    public static final String PREF_OVERRIDE_DEFAULTS = FoldUtilitiesImpl.PREF_OVERRIDE_DEFAULTS;
    
    private static Set<FoldType> LEGACY_FOLD_TYPES = new HashSet<FoldType>();
    
    static {
        LEGACY_FOLD_TYPES.add(FoldType.CODE_BLOCK);
        LEGACY_FOLD_TYPES.add(FoldType.INITIAL_COMMENT);
        LEGACY_FOLD_TYPES.add(FoldType.DOCUMENTATION);
        LEGACY_FOLD_TYPES.add(FoldType.TAG);
        LEGACY_FOLD_TYPES.add(FoldType.MEMBER);
        LEGACY_FOLD_TYPES.add(FoldType.NESTED);
        LEGACY_FOLD_TYPES.add(FoldType.IMPORT);
    }
    
    /**
     * Mimetype for which these options work
     */
    private String mimeType;
    
    /**
     * Transient preferences, contains current values + defaults.
     */
    private Preferences preferences;

    /**
     * Preferences for the super mimetype (or "")
     */
    private Preferences defaultPrefs;
    
    /**
     * Fold types registered for this MIME type. For "" type, the fold types
     * are filtered for those, which are actually used by any of the registered MIME types
     */
    private Collection<? extends FoldType> types;
    
    /**
     * Checkbox controls for collapse-* options
     */
    private Collection<JCheckBox> controls = new ArrayList<JCheckBox>();

    private PreferenceChangeListener weakL;
    
    private Collection<String>    parentFoldTypes;
    
    private boolean isChanged= false;
    
    /**
     * Creates new form DefaultFoldingOptions
     */
    public DefaultFoldingOptions(String mime, Preferences preferences) {
        initComponents();
	 
        VerticalFlowLayout vfl = new VerticalFlowLayout();
        localSwitchboard.setLayout(vfl);
        
        vfl = new VerticalFlowLayout();
        localSwitchboard.setLayout(vfl);

        this.mimeType = mime;
        this.preferences = preferences;
        
        String parentMime = MimePath.parse(mime).getInheritedType();
        if (parentMime != null) {
            parentFoldTypes = new HashSet<String>(13);
            for (FoldType ft : FoldUtilities.getFoldTypes(parentMime).values()) {
                parentFoldTypes.add(ft.code());
            }
        } else {
            parentFoldTypes = Collections.emptyList();
        }
    }

    @Override
    public void setDefaultPreferences(Preferences pref) {
        if (this.defaultPrefs != null) {
            defaultPrefs.removePreferenceChangeListener(weakL);
        }
        // anomaly: if the local Preference contains 'removedKey' for a preference
        // which has been turned to default, it blocks all change propagations.
        this.defaultPrefs = pref;
        if (pref != null) {
            weakL = WeakListeners.create(PreferenceChangeListener.class, this, pref);
            pref.addPreferenceChangeListener(weakL);
        }
    }
    
    private static String k(FoldType ft) {
        return COLLAPSE_PREFIX + ft.code();
    }
    
    private JCheckBox createCheckBox(FoldType ft) {
        return new JCheckBox();
    }
    
    /**
     * Filters types to contain only mimetypes, which are actually used in 
     * one or more mimetypes. Used only for "" mimetype (defaults).
     */
    private void filterUsedMimeTypes() {
        Set<String> mimeTypes = EditorSettings.getDefault().getAllMimeTypes();
        Set<String> codes = new HashSet<String>();
        for (String mt : mimeTypes) {
            Collection<? extends FoldType> fts = FoldUtilities.getFoldTypes(mt).values();
            for (FoldType ft : fts) {
                codes.add(ft.code());
                if (ft.parent() != null) {
                    codes.add(ft.parent().code());
                }
            }
        }
        for (Iterator<? extends FoldType> it = types.iterator(); it.hasNext();) {
            FoldType ft = it.next();
            if (LEGACY_FOLD_TYPES.contains(ft)) {
                continue;
            }
            if (!codes.contains(ft.code())) {
                it.remove();
            }
        }
    }
    
    private void load() {
        types = new ArrayList<FoldType>(FoldUtilities.getFoldTypes(mimeType).values());
        if ("".equals(mimeType)) { // NOI18N
            filterUsedMimeTypes();
        }

        boolean override = isCollapseRedefined();
        boolean currentOverride = 
                isDefinedLocally(PREF_OVERRIDE_DEFAULTS) ? !preferences.getBoolean(PREF_OVERRIDE_DEFAULTS, true) : false;
        if (override != currentOverride) {
            updateOverrideChanged();
        }
        
        for (FoldType ft : types) {
            String name = ft.getLabel();
            
            JCheckBox cb = createCheckBox(ft);
            cb.setText(name);
            cb.putClientProperty("id", ft.code()); // NOI18N
            cb.putClientProperty("type", ft); // NOI18N
            localSwitchboard.add(cb);
            controls.add(cb);
            cb.addItemListener(this);
        }
        
        // watch out for preferences
        this.preferences.addPreferenceChangeListener(this);
        updateEnabledState();
    }
    
    /**
     * Checks whether some of the collapse- options is redefined for this MIME type.
     * If so, the checkbox will be selected even though the override option is not
     * present.
     * 
     * @return 
     */
    private boolean isCollapseRedefined() {
        for (FoldType ft : types) {
            String pref = k(ft);
            if (((OverridePreferences)preferences).isOverriden(pref)) {
               if (defaultPrefs == null || 
                    (parentFoldTypes.contains(ft.code()) || (ft.parent() != null && parentFoldTypes.contains(ft.parent().code())))) {
                    return true;
               }
            }
        }
        return false;
    }
    
    private boolean loaded;

    @Override
    public void addNotify() {
        super.addNotify();
        if (!loaded) {
            load();
            updateEnabledState();
            updateValueState();
            loaded = true;
        }
    }

    @Override
    public void preferenceChange(final PreferenceChangeEvent evt) {
        // the inherited legacy 'method' fold setting in "" mime type is changed in the same Swing event
        // as the real 'member' setting, so updating in the next event should read the already changed value.
        // similar for javadoc and inner classes.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireChanged(updateCheckers(evt));
            }
        });
    }
    
    private void updateValueState() {
        ignoreStateChange = true;
        for (JCheckBox cb : controls) {
            FoldType ft = (FoldType)cb.getClientProperty("type"); // NOI18N
            String k = COLLAPSE_PREFIX + ft.code();
            boolean val = isCollapseEnabled(ft);
            cb.setSelected(val);
        }
        ignoreStateChange = false;
    }
    
    private void updateEnabledState() {
        boolean foldEnable = preferences.getBoolean(SimpleValueNames.CODE_FOLDING_ENABLE, true);
        boolean useDefaults = preferences.getBoolean(FoldUtilitiesImpl.PREF_OVERRIDE_DEFAULTS, true);
        
        for (JComponent c : controls) {
            FoldType ft = (FoldType)c.getClientProperty("type"); // NOI18N
            boolean enable = true;
            if (defaultPrefs != null && useDefaults) {
                enable = !isDefinedDefault(ft);
            }
            enable &= foldEnable;
            c.setEnabled(enable);
        }
    }
    
    boolean isChanged() {
        return isChanged;
    }

    private void fireChanged(String pk) {
        if (pk == null) {
            isChanged = false;
            return;
        }
        Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        boolean changed = preferences.getBoolean(SimpleValueNames.CODE_FOLDING_ENABLE, true) != prefs.getBoolean(SimpleValueNames.CODE_FOLDING_ENABLE, true)
                || preferences.getBoolean(FoldUtilitiesImpl.PREF_OVERRIDE_DEFAULTS, true) != prefs.getBoolean(FoldUtilitiesImpl.PREF_OVERRIDE_DEFAULTS, true)
                || preferences.getBoolean(FoldUtilitiesImpl.PREF_CONTENT_PREVIEW, true) != prefs.getBoolean(FoldUtilitiesImpl.PREF_CONTENT_PREVIEW, true)
                || preferences.getBoolean(FoldUtilitiesImpl.PREF_CONTENT_SUMMARY, true) != prefs.getBoolean(FoldUtilitiesImpl.PREF_CONTENT_SUMMARY, true);
        for (JCheckBox cb : controls) {
            changed |= isFoldTypeChanged((FoldType) cb.getClientProperty("type"), prefs); // NOI18N
        }
        isChanged = changed;
    }

    private boolean isFoldTypeChanged(FoldType ft, Preferences prefs) {
        if (defaultPrefs == null) {
            return preferences.getBoolean(k(ft), ft.parent() == null ? false : preferences.getBoolean(k(ft.parent()), false))
                    != prefs.getBoolean(k(ft), ft.parent() == null ? false : prefs.getBoolean(k(ft.parent()), false));
        } else {
            String k = k(ft);
            return preferences.getBoolean(k, defaultPrefs.getBoolean(k, ft.parent() == null ? false : preferences.getBoolean(k(ft.parent()), false)))
                    != prefs.getBoolean(k, defaultPrefs.getBoolean(k, ft.parent() == null ? false : prefs.getBoolean(k(ft.parent()), false)));
        }
    }
    
    private String updateCheckers(PreferenceChangeEvent evt) {
        String pk = evt.getKey();
        if (pk != null) {
            if (pk.equals(SimpleValueNames.CODE_FOLDING_ENABLE)) {
                updateEnabledState();
                return pk;
            }
            if (pk.equals(PREF_OVERRIDE_DEFAULTS)) {
                updateOverrideChanged();
            } else if (!pk.startsWith(COLLAPSE_PREFIX)) {
                return pk;
            }
        } else {
            updateEnabledState();
        }
        String c = pk == null ? null : pk.substring(COLLAPSE_PREFIX.length());
        for (JCheckBox cb : controls) {
            FoldType ft = (FoldType)cb.getClientProperty("type"); // NOI18N
            FoldType ftp = ft.parent();
            if (c == null || ft.code().equals(c) || (ftp != null && ftp.code().equals(c))) {
                updateChecker(pk, cb, ft);
                return pk;
            }
        }
        return pk;
    }
    
    private boolean isCollapseEnabled(FoldType ft) {
        if (defaultPrefs == null) {
            return preferences.getBoolean(k(ft),
                    ft.parent() == null ? false
                    : preferences.getBoolean(k(ft.parent()), false));
        } else {
            String k = k(ft);
            return preferences.getBoolean(k,
                    defaultPrefs.getBoolean(k,
                    ft.parent() == null ? false
                    : preferences.getBoolean(k(ft.parent()), false)));
        }
    }
    
    private void updateOverrideChanged() {
        boolean en = !preferences.getBoolean(FoldUtilitiesImpl.PREF_OVERRIDE_DEFAULTS, true);
        if (defaultPrefs == null) {
            return;
        }
        if (en) {
            // persist all foldtype settings. Effective (inherited) settings will be read, and 
            // hopefully persisted.
            for (FoldType ft : types) {
                if (!isDefinedDefault(ft) && isDefinedLocally(k(ft))) {
                    continue;
                }
                preferences.putBoolean(k(ft), 
			   defaultPrefs.getBoolean(k(ft),
	                     ft.parent() == null ? false :
	                 defaultPrefs.getBoolean(k(ft.parent()), false))
		  );
            }
        } else {
            // remove all local definitions, which also have a default
            for (FoldType ft : types) {
                if (isDefinedDefault(ft) && isDefinedLocally(k(ft))) {
                    preferences.remove(k(ft));
                }
            }
        }
        updateEnabledState();
        updateValueState();
    }
    
    private boolean isDefinedDefault(FoldType ft) {
        return parentFoldTypes.contains(ft.code()) ||
            (ft.parent() != null && parentFoldTypes.contains(ft.parent().code()));
    }
    
    private boolean isDefinedLocally(String prefKey) {
        return !(preferences instanceof OverridePreferences) ||
                ((OverridePreferences)preferences).isOverriden(prefKey);
    }
    
    private void updateChecker(String prefKey, JCheckBox cb, FoldType ft) {
        if (lastChangedCB == cb) {
            // ignore
            lastChangedCB = null;
            return;
        }
        boolean val = isCollapseEnabled(ft);
        ignoreStateChange = true;
        LOG.log(Level.FINE, "Updating checker: " + prefKey + ", setSelected " + val); // NOI18N
        cb.setSelected(val);
        ignoreStateChange = false;
    }
    
    private boolean ignoreStateChange;
    
    private JCheckBox lastChangedCB;

    @Override
    public void itemStateChanged(final ItemEvent e) {
        if (ignoreStateChange) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updatePref(e);
            }
        });
    }
    
    private void updatePref(ItemEvent e) {
        JCheckBox cb = (JCheckBox)e.getSource();
        FoldType ft = (FoldType)cb.getClientProperty("type"); // NOI18N
        
        String prefKey = COLLAPSE_PREFIX + ft.code();
        lastChangedCB = cb;
        LOG.log(Level.FINE, "Updating preference: " + prefKey + ", value = " + cb.isSelected()); // NOI18N
        preferences.putBoolean(prefKey, cb.isSelected());
        
        if (!"".equals(mimeType)) {
            return;
        }
        // legacy setting support in transient prefs:
        String propagate = FoldOptionsController.LEGACY_SETTINGS_MAP.get(ft.code());
        if (propagate != null) {
            prefKey = COLLAPSE_PREFIX + propagate;
            LOG.log(Level.FINE, "Updating LEGACY preference: " + prefKey + ", value = " + cb.isSelected()); // NOI18N
            preferences.putBoolean(prefKey, cb.isSelected());
        }
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        if (ignoreStateChange) {
            return;
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        collapseContainer = new javax.swing.JPanel();
        localSwitchboard = new javax.swing.JPanel();

        collapseContainer.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DefaultFoldingOptions.class, "DefaultFoldingOptions.collapseContainer.border.title"))); // NOI18N
        collapseContainer.setLayout(new java.awt.BorderLayout());

        localSwitchboard.setLayout(null);
        collapseContainer.add(localSwitchboard, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(collapseContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(collapseContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel collapseContainer;
    private javax.swing.JPanel localSwitchboard;
    // End of variables declaration//GEN-END:variables
}
