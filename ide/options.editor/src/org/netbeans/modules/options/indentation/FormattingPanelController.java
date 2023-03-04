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
package org.netbeans.modules.options.indentation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeEvent;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.netbeans.modules.editor.settings.storage.spi.TypedValue;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * This is used in Tools-Options, but not in project properties customizer.
 * 
 * @author Dusan Balek
 */
@OptionsPanelController.SubRegistration(
    id="Formatting",
    displayName="org.netbeans.modules.options.editor.Bundle#CTL_Formating_DisplayName",
//    tooltip="org.netbeans.modules.options.editor.Bundle#CTL_Formating_ToolTip",
    keywords="org.netbeans.modules.options.editor.Bundle#KW_Formatting",
    keywordsCategory="Editor/Formatting",
    position=200,
    location=OptionsDisplayer.EDITOR
)
public final class FormattingPanelController extends OptionsPanelController {

    public static final String OVERRIDE_GLOBAL_FORMATTING_OPTIONS = "FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS"; //NOI18N

    // ------------------------------------------------------------------------
    // OptionsPanelController implementation
    // ------------------------------------------------------------------------

    public FormattingPanelController() {
    }

    public void update() {
        boolean fire;
        
        synchronized (this) {
            LOG.fine("update"); //NOI18N
            if (pf != null) {
                pf.destroy();
            }
            pf = new MimeLookupPreferencesFactory(new Callable() {
                public Object call() {
                    notifyChanged(true);
                    return null;
                }
            });
            selector = new CustomizerSelector(pf, true, null);
            panel.setSelector(selector);
            fire = changed;
            changed = false;
        }
        
        if (fire) {
            pcs.firePropertyChange(PROP_CHANGED, true, false);
        }
    }
    
    public void applyChanges() {
        boolean fire;
        
        synchronized (this) {
            LOG.fine("applyChanges"); //NOI18N

            pf.applyChanges();
            for(String mimeType : pf.getAccessedMimeTypes()) {
                for(PreferencesCustomizer c : selector.getCustomizers(mimeType)) {
                    if (c instanceof CustomizerSelector.WrapperCustomizer) {
                        ((CustomizerSelector.WrapperCustomizer) c).applyChanges();
                    }
                }
            }

            // Find mimeTypes that do not have a customizer
            Set<String> mimeTypes = new HashSet<String>(EditorSettings.getDefault().getAllMimeTypes());
            mimeTypes.removeAll(selector.getMimeTypes());

            // and make sure that they do NOT override basic settings from All Languages
            for(String mimeType : mimeTypes) {
                Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
                EditorSettingsStorage<String, TypedValue> storage = EditorSettingsStorage.<String, TypedValue>get("Preferences"); //NOI18N
                for(String key : BASIC_SETTINGS_NAMES) {
                    try {
                        Map<String, TypedValue> mimePathLocalPrefs = storage.load(MimePath.parse(mimeType), null, false);
                        Map<String, TypedValue> moduleMimePathLocalPrefs = storage.load(MimePath.parse(mimeType), null, true);
                        
                        if (mimePathLocalPrefs.containsKey(key) || moduleMimePathLocalPrefs.containsKey(key)) {
                            TypedValue value = moduleMimePathLocalPrefs.get(key);
                            if (value != null) {
                                if (value.getJavaType().equals(Integer.class.getName())) {
                                    prefs.putInt(key, Integer.parseInt(value.getValue()));
                                } else if (value.getJavaType().equals(Boolean.class.getName())) {
                                    prefs.putBoolean(key, Boolean.parseBoolean(value.getValue()));
                                } else {
                                    prefs.put(key, value.getValue());
                                }
                            } else {
                                prefs.remove(key);
                            }
                        }
                    } catch (IOException ioe) {
                        LOG.log(Level.WARNING, null, ioe);
                    }
                }
            }

            pf.destroy();
            pf = null;
            panel.setSelector(null);
            selector = null;
            
            fire = changed;
            changed = false;
        }
        
        if (fire) {
            pcs.firePropertyChange(PROP_CHANGED, true, false);
        }

        // XXX: just use whatever value, it's ignored anyway, this is here in order
        // to fire property change events on documents, which are then intercepted by
        // the new view hierarchy (DocumentView)
        SwingUtilities.invokeLater(new Runnable() {
            public @Override void run() {
                JTextComponent lastFocused = EditorRegistry.lastFocusedComponent();
                if (lastFocused != null) {
                    lastFocused.getDocument().putProperty(SimpleValueNames.TEXT_LINE_WRAP, ""); //NOI18N
                    lastFocused.getDocument().putProperty(SimpleValueNames.TAB_SIZE, ""); //NOI18N
                    lastFocused.getDocument().putProperty(SimpleValueNames.TEXT_LIMIT_WIDTH, ""); //NOI18N
                }
                for(JTextComponent jtc : EditorRegistry.componentList()) {
                    if (lastFocused == null || lastFocused != jtc) {
                        jtc.getDocument().putProperty(SimpleValueNames.TEXT_LINE_WRAP, ""); //NOI18N
                        jtc.getDocument().putProperty(SimpleValueNames.TAB_SIZE, ""); //NOI18N
                        jtc.getDocument().putProperty(SimpleValueNames.TEXT_LIMIT_WIDTH, ""); //NOI18N
                    }
                }
            }
        });
    }

    public void cancel() {
        boolean fire;
        
        synchronized (this) {
            LOG.fine("cancel"); //NOI18N

            for(String mimeType : pf.getAccessedMimeTypes()) {
                for(PreferencesCustomizer c : selector.getCustomizers(mimeType)) {
                    if (c instanceof CustomizerSelector.WrapperCustomizer) {
                        ((CustomizerSelector.WrapperCustomizer) c).cancel();
                    }
                }
            }
                
            pf.destroy();
            pf = null;
            panel.setSelector(null);
            selector = null;
            
            fire = changed;
            changed = false;
        }
        
        if (fire) {
            pcs.firePropertyChange(PROP_CHANGED, true, false);
        }
    }
    
    public boolean isValid() {
        return true;
    }
    
    public boolean isChanged() {
        return changed || areCNDPrefsChanged();
    }
    
    private boolean areCNDPrefsChanged() {
        boolean isChanged = false;
        if(pf == null || selector == null) {
            return isChanged;
        }
        for (String mimeType : pf.getAccessedMimeTypes()) {
            for (PreferencesCustomizer c : selector.getCustomizers(mimeType)) {
                if (c instanceof CustomizerSelector.WrapperCustomizer) {
                    isChanged |= ((CustomizerSelector.WrapperCustomizer) c).isChanged();
                    if (isChanged) { // no need to iterate further
                        return true;
                    }
                }
            }
        }
        return isChanged;
    }
    
    private void firePrefsChanged() {
        boolean isChanged = false;
        for (String mimeType : pf.getAccessedMimeTypes()) {
            for (PreferencesCustomizer c : selector.getCustomizers(mimeType)) {
                if (c instanceof CustomizerSelector.WrapperCustomizer) {
                    isChanged |= ((CustomizerSelector.WrapperCustomizer) c).isChanged();
                    continue;
                }
                isChanged |= arePrefsChanged(mimeType, c);
                if (isChanged) { // no need to iterate further
                    changed = true;
                    return;
                }
            }
        }
        changed = isChanged;
    }

    private boolean arePrefsChanged(String mimeType, PreferencesCustomizer c) {
        boolean isChanged = false;
        Preferences prefs = selector.getCustomizerPreferences(c);
        Preferences savedPrefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        HashSet<String> hashSet = new HashSet<String>();
        try {
            hashSet.addAll(Arrays.asList(prefs.keys()));
            hashSet.addAll(Arrays.asList(savedPrefs.keys()));
        } catch (BackingStoreException ex) {
            return false;
        }
        for (String key : hashSet) {
            if (key.equals(FormattingPanelController.OVERRIDE_GLOBAL_FORMATTING_OPTIONS)) {
                continue;
            }
            isChanged |= (prefs.get(key, null) == null ? savedPrefs.get(key, null) != null : !prefs.get(key, null).equals(savedPrefs.get(key, null)))
                    || (prefs.get(key, null) == null ? savedPrefs.get(key, null) != null : !prefs.get(key, null).equals(savedPrefs.get(key, null)));
        }
        return isChanged;
    }
    
    public HelpCtx getHelpCtx() {
        PreferencesCustomizer c = selector == null ? null : selector.getSelectedCustomizer();
        HelpCtx ctx = c == null ? null : c.getHelpCtx();
	return ctx != null ? ctx : new HelpCtx("netbeans.optionsDialog.editor.identation"); //NOI18N
    }
    
    public synchronized JComponent getComponent(Lookup masterLookup) {
        if (panel == null) {
            panel = new FormattingPanel();
        }
        return panel;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
	pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
	pcs.removePropertyChangeListener(l);
    }

    // ------------------------------------------------------------------------
    // private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(FormattingPanelController.class.getName());

    private static String [] BASIC_SETTINGS_NAMES = new String [] {
        SimpleValueNames.EXPAND_TABS,
        SimpleValueNames.INDENT_SHIFT_WIDTH,
        SimpleValueNames.SPACES_PER_TAB,
        SimpleValueNames.TAB_SIZE,
        SimpleValueNames.TEXT_LIMIT_WIDTH,
        SimpleValueNames.TEXT_LINE_WRAP,
    };

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private MimeLookupPreferencesFactory pf;
    private CustomizerSelector selector;
    private FormattingPanel panel;
    private boolean changed = false;

    private void notifyChanged(boolean changed) {
        boolean fire;
        
        synchronized (this) {
            if (this.changed != changed) {
                this.changed = changed;
                fire = true;
            } else {
                fire = false;
            }
        }
        
        if (fire) {
            pcs.firePropertyChange(PROP_CHANGED, !changed, changed);
        }
        firePrefsChanged();
    }

    private static final class MimeLookupPreferencesFactory implements CustomizerSelector.PreferencesFactory, PreferenceChangeListener, NodeChangeListener {

        public MimeLookupPreferencesFactory(Callable callback) {
            this.callback = callback;
        }
        
        public Set<? extends String> getAccessedMimeTypes() {
            return mimeTypePreferences.keySet();
        }

        public void applyChanges() {
            for(Map.Entry<String, ProxyPreferences> entry : mimeTypePreferences.entrySet()) {
                String mimeType = entry.getKey();
                ProxyPreferences pp = entry.getValue();

                pp.silence();

                if (mimeType.length() > 0) {
                    // there can be no tabs-and-indents customizer
                    //assert pp.get(OVERRIDE_GLOBAL_FORMATTING_OPTIONS, null) != null;

                    if (!pp.getBoolean(OVERRIDE_GLOBAL_FORMATTING_OPTIONS, false)) {
                        // remove the basic settings if a language is not overriding the 'all languages' values
                        for(String key : BASIC_SETTINGS_NAMES) {
                            pp.remove(key);
                        }
                    }
                    pp.remove(OVERRIDE_GLOBAL_FORMATTING_OPTIONS);
                } else {
                    assert pp.get(OVERRIDE_GLOBAL_FORMATTING_OPTIONS, null) == null;
                }

                try {
                    LOG.fine("    flushing pp for '" + mimeType + "'"); //NOI18N
                    pp.flush();
                } catch (BackingStoreException ex) {
                    LOG.log(Level.WARNING, "Can't flush preferences for '" + mimeType + "'", ex); //NOI18N
                }
            }
        }

        public void destroy() {
            // destroy all proxy preferences
            for(Map.Entry<String, ProxyPreferences> entry : mimeTypePreferences.entrySet()) {
                String mimeType = entry.getKey();
                ProxyPreferences pp = entry.getValue();
                pp.removeNodeChangeListener(weakNodeL);
                pp.removePreferenceChangeListener(weakPrefL);
                pp.destroy();
                LOG.fine("destroying pp for '" + mimeType + "'"); //NOI18N
            }

            // reset the cache
            mimeTypePreferences.clear();
        }

        // ------------------------------------------------------------------------
        // CustomizerSelector.PreferencesFactory implementation
        // ------------------------------------------------------------------------

        public Preferences getPreferences(final String mimeType) {
            ProxyPreferences pp = mimeTypePreferences.get(mimeType);
            try {
                // clean up the cached ProxyPreferences instance that has been removed in the meantime
                if (pp != null && !pp.nodeExists("")) { //NOI18N
                    pp = null;
                }
            } catch (BackingStoreException bse) {
                // ignore
            }

            if (pp == null) {
                Preferences p = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
                pp = ProxyPreferences.getProxyPreferences(this, p);
                if (mimeType.length() > 0) {
                    final ProxyPreferences finalPP = pp;
                    PROCESSOR.post(new Runnable() {
                        public void run() {
                            boolean overriden = isKeyOverridenForMimeType(SimpleValueNames.EXPAND_TABS, mimeType)
                                    || isKeyOverridenForMimeType(SimpleValueNames.INDENT_SHIFT_WIDTH, mimeType)
                                    || isKeyOverridenForMimeType(SimpleValueNames.SPACES_PER_TAB, mimeType)
                                    || isKeyOverridenForMimeType(SimpleValueNames.TAB_SIZE, mimeType)
                                    || isKeyOverridenForMimeType(SimpleValueNames.TEXT_LIMIT_WIDTH, mimeType)
                                    || isKeyOverridenForMimeType(SimpleValueNames.TEXT_LINE_WRAP, mimeType);
                            finalPP.putBoolean(OVERRIDE_GLOBAL_FORMATTING_OPTIONS, overriden);
                        }
                    }, 1000);
                }               
                pp.addPreferenceChangeListener(weakPrefL);
                pp.addNodeChangeListener(weakNodeL);
                mimeTypePreferences.put(mimeType, pp);
                LOG.fine("getPreferences(" + mimeType + ")"); //NOI18N
            }
            
            return pp;
        }

        public boolean isKeyOverridenForMimeType(String key, String mimeType) {
            EditorSettingsStorage<String, TypedValue> storage = EditorSettingsStorage.<String, TypedValue>get("Preferences"); //NOI18N
            try {
                Map<String, TypedValue> mimePathLocalPrefs = storage.load(MimePath.parse(mimeType), null, false);
                return mimePathLocalPrefs.containsKey(key);
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
                return false;
            }
        }

        // ------------------------------------------------------------------------
        // PreferenceChangeListener implementation
        // ------------------------------------------------------------------------

        public void preferenceChange(PreferenceChangeEvent evt) {
            try { callback.call(); } catch (Exception e) { /* ignore */ }
        }

        // ------------------------------------------------------------------------
        // NodeChangeListener implementation
        // ------------------------------------------------------------------------

        public void childAdded(NodeChangeEvent evt) {
            try { callback.call(); } catch (Exception e) { /* ignore */ }
        }

        public void childRemoved(NodeChangeEvent evt) {
            try { callback.call(); } catch (Exception e) { /* ignore */ }
        }

        // ------------------------------------------------------------------------
        // private implementation
        // ------------------------------------------------------------------------

        private final Map<String, ProxyPreferences> mimeTypePreferences = new HashMap<String, ProxyPreferences>();
        private final PreferenceChangeListener weakPrefL = WeakListeners.create(PreferenceChangeListener.class, this, null);
        private final NodeChangeListener weakNodeL = WeakListeners.create(NodeChangeListener.class, this, null);
        private final Callable callback;
        private final RequestProcessor PROCESSOR = new RequestProcessor(MimeLookupPreferencesFactory.class); // NOI18N

    } // End of MimeLookupPreferencesFactory class
}
