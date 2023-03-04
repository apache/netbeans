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
package org.netbeans.modules.options.editor.completion;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.options.indentation.ProxyPreferences;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 * @author Dusan Balek
 */
@OptionsPanelController.SubRegistration(
    location=OptionsDisplayer.EDITOR,
    id="CodeCompletion",
    displayName="#CTL_CodeCompletion_DisplayName",
    keywords="#KW_CodeCompletion",
    keywordsCategory="Editor/CodeCompletion",
//    toolTip="#CTL_CodeCompletion_ToolTip",
    position=250
)
public final class CodeCompletionOptionsPanelController extends OptionsPanelController {

    private static final Logger LOG = Logger.getLogger(CodeCompletionOptionsPanelController.class.getName());
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private PreferencesFactory pf;
    private CodeCompletionOptionsPanel panel;
    private CodeCompletionOptionsSelector selector;
    private boolean changed = false;

    public CodeCompletionOptionsPanelController() {
    }

    public void update() {
        boolean fire;

        synchronized (this) {
            LOG.fine("update"); //NOI18N
            if (pf != null) {
                pf.destroy();
            }
            pf = new PreferencesFactory(new Callable() {
                public Object call() {
                    notifyChanged(true);
                    return null;
                }
            });
            selector = new CodeCompletionOptionsSelector(pf);
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

            // Make sure that completions settings that are only accessible for
            // 'all languages' are not overriden by particular languages (mime types)
            for(String mimeType : EditorSettings.getDefault().getAllMimeTypes()) {
                LOG.fine("Cleaning up '" + mimeType + "' preferences"); //NOI18N
                Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
                prefs.remove(SimpleValueNames.COMPLETION_PAIR_CHARACTERS); //NOI18N
                prefs.remove(SimpleValueNames.COMPLETION_AUTO_POPUP);
                prefs.remove(SimpleValueNames.JAVADOC_AUTO_POPUP);
                prefs.remove(SimpleValueNames.JAVADOC_POPUP_NEXT_TO_CC);
                prefs.remove(SimpleValueNames.SHOW_DEPRECATED_MEMBERS);
                prefs.remove(SimpleValueNames.COMPLETION_INSTANT_SUBSTITUTION);
                prefs.remove(SimpleValueNames.COMPLETION_CASE_SENSITIVE);
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

    public void cancel() {
        boolean fire;

        synchronized (this) {
            LOG.fine("cancel"); //NOI18N

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
        synchronized (this) {
            return changed;
        }
    }

    private void firePrefsChanged() {
        boolean isChanged = false;
        for (String mimeType : pf.getAccessedMimeTypes()) {
            isChanged |= arePrefsChanged(mimeType);
            if (isChanged) { // no need to iterate further
                changed = true;
                return;
            }
        }
        changed = isChanged;
    }

    private boolean arePrefsChanged(String mimeType) {
        boolean isChanged = false;
        Preferences prefs = pf.getPreferences(mimeType);
        Preferences savedPrefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        HashSet<String> hashSet = new HashSet<String>();
        try {
            hashSet.addAll(Arrays.asList(prefs.keys()));
            hashSet.addAll(Arrays.asList(savedPrefs.keys()));
        } catch (BackingStoreException ex) {
            return false;
        }
        for (String key : hashSet) {
            String current = prefs.get(key, null);
            String saved = savedPrefs.get(key, null);
            if (saved == null) {
                saved = selector.getSavedValue(mimeType, key);
            }
            isChanged |= current == null ? saved != null : !current.equals(saved);
            if (isChanged) { // no need to iterate further
                return true;
            }
        }
        return isChanged;
    }

    public HelpCtx getHelpCtx() {
//        PreferencesCustomizer c = selector == null ? null : selector.getSelectedCustomizer();
//        HelpCtx ctx = c == null ? null : c.getHelpCtx();
	return new HelpCtx("netbeans.optionsDialog.editor.codeCompletion"); //NOI18N
    }

    public JComponent getComponent(Lookup masterLookup) {
        if (panel == null) {
            panel = new CodeCompletionOptionsPanel();
        }
        return panel;
    }

    @Override
    protected void setCurrentSubcategory(String subpath) {
        if (selector != null && selector.getMimeTypes().contains(subpath)) {
            selector.setSelectedMimeType(subpath);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

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

    static final class PreferencesFactory implements PreferenceChangeListener, NodeChangeListener {

        public PreferencesFactory(Callable callback) {
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

        public Preferences getPreferences(String mimeType) {
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
                pp.addPreferenceChangeListener(weakPrefL);
                pp.addNodeChangeListener(weakNodeL);
                mimeTypePreferences.put(mimeType, pp);
                LOG.fine("getPreferences('" + mimeType + "')"); //NOI18N
            }

            return pp;
        }

//        public boolean isKeyOverridenForMimeType(String key, String mimeType) {
//            EditorSettingsStorage<String, TypedValue> storage = EditorSettingsStorage.<String, TypedValue>get("Preferences"); //NOI18N
//            try {
//                Map<String, TypedValue> mimePathLocalPrefs = storage.load(MimePath.parse(mimeType), null, false);
//                return mimePathLocalPrefs.containsKey(key);
//            } catch (IOException ioe) {
//                LOG.log(Level.WARNING, null, ioe);
//                return false;
//            }
//        }

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

    } // End of MimeLookupPreferencesFactory class
}
