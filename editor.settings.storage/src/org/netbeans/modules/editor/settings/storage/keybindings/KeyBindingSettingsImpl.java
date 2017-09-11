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

package org.netbeans.modules.editor.settings.storage.keybindings;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.modules.editor.settings.storage.EditorSettingsImpl;
import org.netbeans.modules.editor.settings.storage.ProfilesTracker;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.netbeans.modules.editor.settings.storage.api.KeyBindingSettingsFactory;
import org.openide.util.Utilities;

/**
 * KeyBindings settings are represented by List of keybindings.
 * The List contains the instances of {@link MultiKeyBinding}.
 * <br>
 * Instances of this class should be retrieved from the {@link org.netbeans.api.editor.mimelookup.MimeLookup}
 * for a given mime-type.
 * <br>
 * <font color="red">This class must NOT be extended by any API clients</font>
 *
 * @author Jan Jancura
 */
public final class KeyBindingSettingsImpl extends KeyBindingSettingsFactory {

    private static final Logger LOG = Logger.getLogger(KeyBindingSettingsImpl.class.getName());
    
    private static final Map<MimePath, WeakReference<KeyBindingSettingsImpl>> INSTANCES =
        new WeakHashMap<MimePath, WeakReference<KeyBindingSettingsImpl>>();
    
    public static synchronized KeyBindingSettingsImpl get(MimePath mimePath) {
        WeakReference<KeyBindingSettingsImpl> reference = INSTANCES.get(mimePath);
        KeyBindingSettingsImpl result = reference == null ? null : reference.get();
        
        if (result == null) {
            result = new KeyBindingSettingsImpl(mimePath);
            INSTANCES.put(mimePath, new WeakReference<KeyBindingSettingsImpl>(result));
        }
        
        return result;
    }
    
    private MimePath mimePath;
    private PropertyChangeSupport   pcs;
    private KeyBindingSettingsImpl baseKBS;
    private Listener                listener;
    
    private String logActionName = null;
    
    /**
     * Construction prohibited for API clients.
     */
    private KeyBindingSettingsImpl (MimePath mimePath) {
        this.mimePath = mimePath;
        pcs = new PropertyChangeSupport (this);
        
        // init logging
        String myClassName = KeyBindingSettingsImpl.class.getName ();
        String value = System.getProperty(myClassName);
        if (value != null) {
            if (!value.equals("true")) {
                logActionName = System.getProperty(myClassName);
            }
        } else if (mimePath.size() == 1) {
            logActionName = System.getProperty(myClassName + '.' + mimePath.getMimeType(0));
        }
    }
    
    private boolean init = false;
    private void init () {
        if (init) return;
        init = true;
        if (mimePath.size() > 0) {
            baseKBS = get(MimePath.EMPTY);
        }
        listener = new Listener(this, baseKBS);
    }

    /**
     * Translates profile's display name to its Id. If the profile's display name
     * can't be translated this method will simply return the profile's display name
     * without translation.
     */
    private String getInternalKeymapProfile (String profile) {
        ProfilesTracker tracker = ProfilesTracker.get(KeyMapsStorage.ID, EditorSettingsImpl.EDITORS_FOLDER);
        ProfilesTracker.ProfileDescription pd = tracker.getProfileByDisplayName(profile);
        return pd == null ? profile : pd.getId();
    }
    
    /**
     * Gets the keybindings list, where items are instances of {@link MultiKeyBinding}
     *
     * @return List of {@link MultiKeyBinding}
     */
    public List<MultiKeyBinding> getKeyBindings() {
        return getKeyBindings(EditorSettingsImpl.getInstance().getCurrentKeyMapProfile());
    }
    
    /**
     * Gets the keybindings list, where items are instances of {@link MultiKeyBinding}
     *
     * @return List of {@link MultiKeyBinding}
     */
    public List<MultiKeyBinding> getKeyBindings(String profile) {
	profile = getInternalKeymapProfile(profile);
        return Collections.unmodifiableList(new ArrayList<MultiKeyBinding>(getShortcuts(profile, false).values()));
    }

    private Map<Collection<KeyStroke>, MultiKeyBinding> getShortcuts(String profile, boolean defaults) {
        EditorSettingsStorage<Collection<KeyStroke>, MultiKeyBinding> ess = EditorSettingsStorage.get(KeyMapsStorage.ID);
        try {
            return ess.load(mimePath, profile, defaults);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
            return Collections.<Collection<KeyStroke>, MultiKeyBinding>emptyMap();
        }
    }
    
    /**
     * Returns default keybindings list for given keymap name, where items 
     * are instances of {@link MultiKeyBinding}.
     *
     * @return List of {@link MultiKeyBinding}
     */
    public List<MultiKeyBinding> getKeyBindingDefaults(String profile) {
	profile = getInternalKeymapProfile(profile);
        return Collections.unmodifiableList(new ArrayList<MultiKeyBinding>(getShortcuts(profile, true).values()));
    }
    
    /**
     * Gets the keybindings list, where items are instances of 
     * {@link MultiKeyBinding}.
     *
     * @return List of {@link MultiKeyBinding}
     */
    public void setKeyBindings (
        String profile, 
        List<MultiKeyBinding> keyBindings
    ) {
        init ();
        profile = getInternalKeymapProfile(profile);
        EditorSettingsStorage<Collection<KeyStroke>, MultiKeyBinding> ess = EditorSettingsStorage.get(KeyMapsStorage.ID);

        try {
            if (keyBindings == null) {
                ess.delete(mimePath, profile, false);
            } else {
                Map<Collection<KeyStroke>, MultiKeyBinding> shortcuts = new HashMap<Collection<KeyStroke>, MultiKeyBinding>();
                for(MultiKeyBinding mkb : keyBindings) {
                    shortcuts.put(mkb.getKeyStrokeList(), mkb);
                }

                listener.removeListeners(); // ???
                ess.save(mimePath, profile, false, shortcuts);
                listener.addListeners();

                pcs.firePropertyChange (null, null, null);
            }
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }
    }
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be registerred
     */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be unregisterred
     */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }    
    
    // other methods ...........................................................
    
    private void log (String text, Collection keymap) {
        if (!LOG.isLoggable(Level.FINE)) {
            return;
        }
        if (text.length() != 0) {
            if (mimePath.size() == 1) {
                text += " " + mimePath.getMimeType(0);
            }
            text += " " + EditorSettingsImpl.getInstance().getCurrentKeyMapProfile();
        }
        if (keymap == null) {
            LOG.fine(text + " : null");
            return;
        }
        LOG.fine(text);
        Iterator it = keymap.iterator ();
        while (it.hasNext ()) {
            Object mkb = it.next ();
            if (logActionName == null || !(mkb instanceof MultiKeyBinding)) {
                LOG.fine("  " + mkb);
            } else if (mkb instanceof MultiKeyBinding &&
                logActionName.equals(((MultiKeyBinding) mkb).getActionName ()))
            {
                LOG.fine("  " + mkb);
            }
        }
    }

    public Object createInstanceForLookup() {
        init ();
        
        // 1) get real profile
	String profile = getInternalKeymapProfile(EditorSettingsImpl.getInstance().getCurrentKeyMapProfile());
        
        Map<Collection<KeyStroke>, MultiKeyBinding> allShortcuts = new HashMap<Collection<KeyStroke>, MultiKeyBinding>();

        // Add base shortcuts
        if (baseKBS != null) {
            Map<Collection<KeyStroke>, MultiKeyBinding> baseShortcuts = baseKBS.getShortcuts(profile, false);
            allShortcuts.putAll(baseShortcuts);
        }

        // Add local shortcuts
        Map<Collection<KeyStroke>, MultiKeyBinding> localShortcuts = getShortcuts(profile, false);
        allShortcuts.putAll(localShortcuts);
        
        // Prepare the result
        List<MultiKeyBinding> result = new ArrayList<MultiKeyBinding>(allShortcuts.values());
        
        return new Immutable(result);
    }
    
    private static final class Listener extends WeakReference<KeyBindingSettingsImpl> implements PropertyChangeListener, Runnable {
        
        private final KeyBindingSettingsFactory baseKBS;
        private final EditorSettingsStorage<Collection<KeyStroke>, MultiKeyBinding> storage;
        
        public Listener (
            KeyBindingSettingsImpl kb,
            KeyBindingSettingsFactory baseKBS
        ) {
            super(kb, Utilities.activeReferenceQueue());
            this.baseKBS = baseKBS;
            this.storage = EditorSettingsStorage.get(KeyMapsStorage.ID);

            addListeners ();
        }
        
        private KeyBindingSettingsImpl getSettings () {
            KeyBindingSettingsImpl r = get ();
            if (r != null) return r;
            removeListeners ();
            return null;
        }
        
        private void addListeners () {
            EditorSettingsImpl.getInstance().addPropertyChangeListener(
                EditorSettings.PROP_CURRENT_KEY_MAP_PROFILE,
                this
            );
            storage.addPropertyChangeListener(this);
            if (baseKBS != null) {
                baseKBS.addPropertyChangeListener(this);
            }
        }
        
        private void removeListeners () {
            if (baseKBS != null) {
                baseKBS.removePropertyChangeListener(this);
            }
            storage.removePropertyChangeListener(this);
            EditorSettingsImpl.getInstance().removePropertyChangeListener(
                EditorSettings.PROP_CURRENT_KEY_MAP_PROFILE,
                this
            );
        }
        
        public void propertyChange (PropertyChangeEvent evt) {
            KeyBindingSettingsImpl r = getSettings ();
            if (r == null) return;
            r.log ("refresh2", Collections.EMPTY_SET);
            r.pcs.firePropertyChange (null, null, null);
        }

        public void run() {
            removeListeners();
        }
    }
    
    /* package */ static final class Immutable extends KeyBindingSettings {
        private List<MultiKeyBinding> keyBindings;
        
        public Immutable(List<MultiKeyBinding> keyBindings) {
            this.keyBindings = keyBindings;
        }
        
        public List<MultiKeyBinding> getKeyBindings() {
            return Collections.unmodifiableList(keyBindings);
        }
    }
    
}
