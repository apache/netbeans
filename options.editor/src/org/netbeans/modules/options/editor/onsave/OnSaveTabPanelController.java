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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.options.editor.onsave;

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
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.modules.options.indentation.ProxyPreferences;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 * @author Miloslav Metelka
 */
@OptionsPanelController.SubRegistration(
    location=OptionsDisplayer.EDITOR,
    id="OnSave",
    displayName="#CTL_OnSave_DisplayName",
    keywords="#KW_OnSave",
    keywordsCategory="Editor/OnSave",
    position=730
)
public final class OnSaveTabPanelController extends OptionsPanelController {

    private static final Logger LOG = Logger.getLogger(OnSaveTabPanelController.class.getName());

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private PreferencesFactory pf;
    
    private OnSaveTabPanel panel;
    
    private OnSaveTabSelector selector;

    private boolean changed = false;

    public OnSaveTabPanelController() {
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
            selector = new OnSaveTabSelector(pf);
            panel.setSelector(selector);
            fire = changed;
            changed = false;
        }

        if (fire) {
            pcs.firePropertyChange(PROP_CHANGED, true, false);
        }
    }

    @Override
    public void applyChanges() {
        boolean fire;

        synchronized (this) {
            LOG.fine("applyChanges"); //NOI18N

            pf.applyChanges();

            // Make sure that on-save settings that are only accessible for
            // 'all languages' are not overriden by particular languages (mime types)
            for(String mimeType : EditorSettings.getDefault().getAllMimeTypes()) {
                LOG.fine("Cleaning up '" + mimeType + "' preferences"); //NOI18N
                Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
//                prefs.remove(SimpleValueNames.COMPLETION_PAIR_CHARACTERS); //NOI18N
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

    @Override
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

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
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
        Preferences prefs = selector.getPreferences(mimeType);
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
                if (key.equals(SimpleValueNames.ON_SAVE_REMOVE_TRAILING_WHITESPACE) || key.equals(SimpleValueNames.ON_SAVE_REFORMAT)) {
                    saved = "never"; // NOI18N
                } else if (key.equals(SimpleValueNames.ON_SAVE_USE_GLOBAL_SETTINGS)) {
                    saved = "true"; // NOI18N
                } else {
                    saved = selector.getSavedValue(mimeType, key);
                }
            }
            isChanged |= current == null ? saved != null : !current.equals(saved);
            if (isChanged) { // no need to iterate further
                return true;
            }
        }
        return isChanged;
    }

    @Override
    public HelpCtx getHelpCtx() {
        PreferencesCustomizer c = selector == null ? null : selector.getSelectedCustomizer();
        HelpCtx ctx = c == null ? null : c.getHelpCtx();
	return ctx != null ? ctx : new HelpCtx("netbeans.optionsDialog.editor.onSave"); //NOI18N
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        if (panel == null) {
            panel = new OnSaveTabPanel();
        }
        return panel;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
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
            for(String mimeType : mimeTypePreferences.keySet()) {
                ProxyPreferences pp = mimeTypePreferences.get(mimeType);
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
            for(String mimeType : mimeTypePreferences.keySet()) {
                ProxyPreferences pp = mimeTypePreferences.get(mimeType);
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
