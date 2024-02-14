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

package org.netbeans.modules.options.editor.keymap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.KeyBindingSettingsFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/**
 * @author Jan Jancura
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.core.options.keymap.spi.KeymapManager.class)
public final class EditorBridge extends KeymapManager {

    private static final Logger LOG = Logger.getLogger(EditorBridge.class.getName());

    private static final String EDITOR_BRIDGE = "EditorBridge"; //NOI18N

    public EditorBridge() {
        super(EDITOR_BRIDGE);
    }

    private Map<String, Set<ShortcutAction>> actions;

    public Map<String, Set<ShortcutAction>> getActions() {
        if (actions == null) {
            Map<String, String> categories = readCategories();
            actions = new HashMap<String, Set<ShortcutAction>>();
            final Map<String, EditorAction> tmpEditorActionsMap = Collections.unmodifiableMap(getEditorActionsMap());
            for (EditorAction action : tmpEditorActionsMap.values()) {
                String category = categories.get(action.getId());
                if (category == null) {
                    category = NbBundle.getMessage(EditorBridge.class, "CTL_Other"); // NOI18N
                }
                Set<ShortcutAction> a = actions.get(category);
                if (a == null) {
                    a = new HashSet<ShortcutAction>();
                    actions.put(category, a);
                }
                a.add(action);

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Action='" + action.getId() + "' -> Category='" + category + "'"); //NOI18N
                }
            }
            actions.remove("Hidden"); // NOI18N
        }
        return actions;
    }

    public void refreshActions() {
        editorActionsMap = null;
        actions = null;
        actionNameToMimeTypes = new HashMap<String, Set<String>>();
    }

    public String getCurrentProfile() {
        return getEditorSettings().getCurrentKeyMapProfile();
    }

    public void setCurrentProfile(String profile) {
        getEditorSettings().setCurrentKeyMapProfile(profile);
    }

    public boolean isCustomProfile(String profile) {
        return getEditorSettings().isCustomKeymapProfile(profile);
    }

    public Map<ShortcutAction, Set<String>> getKeymap(String profile) {
        Map<ShortcutAction, Set<String>> result = new HashMap<ShortcutAction, Set<String>>();
        readKeymap(profile, null, false, result);
        for (String mimeType : getEditorSettings().getMimeTypes()) {
            readKeymap(profile, mimeType, false, result);
        }
        return Collections.unmodifiableMap(result);
    }

    public Map<ShortcutAction, Set<String>> getDefaultKeymap(String profile) {
        Map<ShortcutAction, Set<String>> result = new HashMap<ShortcutAction, Set<String>>();
        readKeymap(profile, null, true, result);
        for (String mimeType : getEditorSettings().getMimeTypes()) {
            readKeymap(profile, mimeType, true, result);
        }
        return Collections.unmodifiableMap(result);
    }

    public void deleteProfile(String profile) {
        KeyBindingSettingsFactory kbs = getKeyBindingSettings(null);
        kbs.setKeyBindings(profile, null);
    }

    /**
     * Saves actionToShortcuts Map (GlobalAction > Set (String (shortcut)).
     * Ignores all non EditorAction actions.
     */
    public void saveKeymap(String profile, Map<ShortcutAction, Set<String>> actionToShortcuts) {

        Set<String> allMimes = new HashSet<String>(getEditorSettings().getMimeTypes());
        allMimes.add(null); // NOI18N
        
        // all knon action IDs for EditorActions (those which are handled by this Bridge)
        Set<String> actionIds = new HashSet<String>(actionToShortcuts.size());
        for(ShortcutAction action : actionToShortcuts.keySet()) {
            action = action.getKeymapManagerInstance(EDITOR_BRIDGE);
            if (!(action instanceof EditorAction)) {
                continue;
            }
            actionIds.add(((EditorAction)action).getId());
        }
        
        // each MIME entry contains action > MKB list mapping. Initialize to all actions,
        // them remove individual action ids which are defined in the new keybinding map
        Map<String, Object> undefinedActions = new HashMap<String, Object>(allMimes.size());
        
        for (String s : allMimes) {
            KeyBindingSettingsFactory f = getKeyBindingSettings(s);
            List<MultiKeyBinding> mkbs = f.getKeyBindings(profile);

            Map<String, List<MultiKeyBinding>> actionToMkb = new HashMap<String, List<MultiKeyBinding>>();
            for (MultiKeyBinding mkb : mkbs) {
                String an = mkb.getActionName();
                if (!actionIds.contains(an)) {
                    // do not attempt to preserve missing actions; if the action is not present at all,
                    // the user had removed it.
                    continue;
                }
                List<MultiKeyBinding> bindings = actionToMkb.get(an);
                if (bindings == null) {
                    bindings = new ArrayList<>(2);
                    actionToMkb.put(an, bindings);
                }
                bindings.add(mkb);
            }
            
            undefinedActions.put(s, actionToMkb);
        }
        
        // 1)
        // convert actionToShortcuts: Map (ShortcutAction > Set (String (shortcut AS-M)))
        // to mimeTypeToKeyBinding: Map (String (mimetype) > List (MultiKeyBinding)).
        Map<String, List<MultiKeyBinding>> mimeTypeToKeyBinding = new HashMap<String, List<MultiKeyBinding>>(); // editor shortcuts
        for(Map.Entry<ShortcutAction, Set<String>> entry : actionToShortcuts.entrySet()) {
            ShortcutAction action = entry.getKey();
            Set<String> shortcuts = entry.getValue();

            action = action.getKeymapManagerInstance(EDITOR_BRIDGE);
            if (!(action instanceof EditorAction)) {
                continue;
            }
            
            EditorAction editorAction = (EditorAction) action;
            Set<String> mimeTypesPre = getMimeTypes(editorAction);

            assert mimeTypesPre != null : "Cannot find MIME types for action " + editorAction; // NOI18N

            Set<String> mimeTypes = new LinkedHashSet<String>(mimeTypesPre.size() + 1);
            mimeTypes.add(null);
            mimeTypes.addAll(mimeTypesPre);
            
            List<MultiKeyBinding> nullBindings = new ArrayList<MultiKeyBinding>();
            
            for (String shortcut : shortcuts) {
                MultiKeyBinding mkb = new MultiKeyBinding(stringToKeyStrokes2(shortcut), editorAction.getId());
                boolean presentInDefault = mimeTypesPre.contains(null);
                
                for (String mimeType : mimeTypes) {
                    // check whether the action thing already exists in the specific MIME map:
                    Map<String, List<MultiKeyBinding>> mimeActions = (Map<String, List<MultiKeyBinding>>)undefinedActions.get(mimeType);
                    boolean exists = mimeActions != null && mimeActions.get(editorAction.getId()) != null;
                    
                    if (mimeType != null) {
                        if (!exists) {
                            if (presentInDefault) {
                                // the action -> mkb is already defined in the null Mimetype, 
                                // do not duplicate
                                continue;
                            }
                            if (nullBindings.contains(mkb)) {
                                // the root MIME defines the action, but its EKit does not handle it
                                // == a common action binding for many MIMEs. Still do not duplicate, if the
                                // mkb is the same.
                                continue;
                            }
                        }
                    } else {
                        if (!presentInDefault && !exists) {
                            // do not define in default iff not already defined and its editorKit does not know it
                            continue;
                        }
                        nullBindings.add(mkb);
                        presentInDefault = true;
                    }
                    List<MultiKeyBinding> l = mimeTypeToKeyBinding.get(mimeType);
                    if (l == null) {
                        l = new ArrayList<MultiKeyBinding>();
                        mimeTypeToKeyBinding.put(mimeType, l);
                    }
                    l.add(mkb);
                }
            }
            for (String mimeType : mimeTypes) {
                Map m = (Map)undefinedActions.get(mimeType);
                if (m != null) {
                    // action is defined for the MIME, remove from the leftover list
                    m.remove(editorAction.getId());
                }
            }
        }
        
        // merge in bindings for unknown actions:
        for (String mimeType : keyBindingSettings.keySet()) {
            Map<String, List<MultiKeyBinding>> actionToMkb = (Map<String, List<MultiKeyBinding>>)undefinedActions.get(mimeType);
            List<MultiKeyBinding> l = mimeTypeToKeyBinding.get(mimeType);
            if (l == null) {
                mimeTypeToKeyBinding.put(mimeType, l = new ArrayList<MultiKeyBinding>(5));
            }
            for (List<MultiKeyBinding> keys : actionToMkb.values()) {
                l.addAll(keys);
            }
        }

        // 2) save all shortcuts
        for (Map.Entry<String, KeyBindingSettingsFactory> entry : keyBindingSettings.entrySet()) {
            String mimeType = entry.getKey();
            KeyBindingSettingsFactory kbs = entry.getValue();
            kbs.setKeyBindings(profile, mimeTypeToKeyBinding.get(mimeType));
        }
    }


    // private methods .........................................................
    /** Map (String (mimeType) > Set (String (action name))). */
    private Map<String, EditorAction> editorActionsMap;
    /** Map (ShortcutAction > Set (String (mimeType))). */
    private Map<String, Set<String>> actionNameToMimeTypes = new HashMap<String, Set<String>>();

    /**
     * Returns map of all editor actions.
     * Map (String (mimeType) > Set (String (action name)))
     */
    /* test */ Map<String, EditorAction> getEditorActionsMap() {
        if (editorActionsMap == null) {
            editorActionsMap = new LinkedHashMap<String, EditorAction>();
            initActionMap(null, null);
            Map<String, EditorAction> emptyMimePathActions = new HashMap<String, EditorAction>(editorActionsMap);
            
            for (String mimeType : getEditorSettings().getMimeTypes()) {
                initActionMap(mimeType, emptyMimePathActions);
            }
        }
        return editorActionsMap;
    }

    private Set<String> getMimeTypes(EditorAction a) {
        getEditorActionsMap(); // initialization
        return actionNameToMimeTypes.get(a.getId());
    }

    /**
     * Loads editor actions for given mimeType to editorActionsMap.
     */
    private void initActionMap(String mimeType, Map<String, EditorAction> emptyMimePathActions) {

        // 1) get EditorKit
        EditorKit editorKit = null;
        if (mimeType == null) {
            editorKit = BaseKit.getKit(NbEditorKit.class);
        } else {
            Lookup mimeLookup = MimeLookup.getLookup(MimePath.parse(mimeType));
            editorKit = mimeLookup.lookup(EditorKit.class);
        }
        if (editorKit == null) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.fine("EditorKit not found for: " + mimeType); //NOI18N
            }
            return;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading actions for '" + (mimeType == null ? "" : mimeType) + "' using " + editorKit); //NOI18N
        }

        // 2) copy actions from EditorKit to actionMap
        Action[] as = editorKit.getActions();
        for (int i = 0; i < as.length; i++) {
            Object isHidden = as[i].getValue(BaseAction.NO_KEYBINDING);
            if (isHidden instanceof Boolean && ((Boolean) isHidden).booleanValue()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("! Action '" + as[i].getValue(Action.NAME) + "' is hidden, ignoring"); //NOI18N
                }
                continue; // ignore hidden actions
            }
            
            EditorAction action = new EditorAction(as [i]);
            String id = action.getId();

            // filter out actions inherited from an empty mime path (all editors actions)
            if (emptyMimePathActions != null && emptyMimePathActions.containsKey(id)) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.finest("Action '" + id + "' was already listed among all alnguages actions, skipping"); //NOI18N
                }
                continue;
            }
            
            getEditorActionsMap().put(id, action);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Action '" + id + "' loaded for '" + (mimeType == null ? "" : mimeType) + "'"); //NOI18N
            }

            Set<String> s = actionNameToMimeTypes.get(id);
            if (s == null) {
                // LinkedHS ensures that 'null' mime which is initialized 1st will be
                // the 1st enumerated from getMimeTypes(). This invariant is used in saveKeyMap
                s = new LinkedHashSet<String>();
                actionNameToMimeTypes.put(id, s);
            }
            s.add(mimeType);
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Actions for '" + (mimeType == null ? "" : mimeType) + "' loaded successfully"); //NOI18N
        }
    }

    private EditorSettings editorSettings;

    private EditorSettings getEditorSettings() {
        if (editorSettings == null) {
            editorSettings = EditorSettings.getDefault();
        }
        return editorSettings;
    }

    private final Map<String, KeyBindingSettingsFactory> keyBindingSettings = new HashMap<String, KeyBindingSettingsFactory>();
    private static final String [] EMPTY = new String[0];
    
    private KeyBindingSettingsFactory getKeyBindingSettings(String mimeType) {
        KeyBindingSettingsFactory kbs = keyBindingSettings.get(mimeType);
        if (kbs == null) {
            kbs = EditorSettings.getDefault().getKeyBindingSettings(
                mimeType == null ? EMPTY : new String[] { mimeType });

            keyBindingSettings.put(mimeType, kbs);
            getListener().add(kbs);
        }
        return kbs;
    }

    private Listener listener;

    private Listener getListener() {
        if (listener == null) {
            listener = new Listener(this);
        }
        return listener;
    }

    private static class Listener implements PropertyChangeListener {

        private Reference<EditorBridge> model;
        private Set<KeyBindingSettingsFactory> factories = new HashSet<KeyBindingSettingsFactory>();

        Listener(EditorBridge model) {
            this.model = new WeakReference<EditorBridge>(model);
        }

        void add(KeyBindingSettingsFactory kbsf) {
            this.factories.add(kbsf);
            kbsf.addPropertyChangeListener(this);
        }

        private EditorBridge getModel() {
            EditorBridge m = model.get ();
            if (m != null) {
                return m;
            }
            for (KeyBindingSettingsFactory kbsf : factories) {
                kbsf.removePropertyChangeListener(this);
            }
            factories = new HashSet<KeyBindingSettingsFactory>();
            return null;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            EditorBridge m = getModel();
            if (m == null) {
                return;
                //model.keyMaps = new HashMap ();
            }
        }
    }

    /**
     * Reads keymap for given mimetype and profile to given map
     * Map (ShortcutAction > Set (String (shortcut)))
     */
    private void readKeymap(String profile, String mimeType, boolean defaults, Map<ShortcutAction, Set<String>> map) {
        // 1) get list of MultiKeyBindings
        KeyBindingSettingsFactory kbs = getKeyBindingSettings(mimeType);
        if (kbs == null) {
            return;
        }
        List<MultiKeyBinding> keyBindings = defaults ? kbs.getKeyBindingDefaults(profile) : kbs.getKeyBindings(profile);
        if (keyBindings == null) {
            return;
        }
        // 2) create Map (String (action name) > Set (String (shortcut)))
        Map<String, Set<String>> actionNameToShortcuts = convertKeymap(keyBindings);

        // 3) create Map (EditorAction > Set (String (shortcut)))
        for (Map.Entry<String, Set<String>> entry : actionNameToShortcuts.entrySet()) {
            String actionName = entry.getKey();
            Set<String> keyStrokes = entry.getValue();
            ShortcutAction action = (ShortcutAction) getEditorActionsMap ().get (actionName);
            if (action == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("action not found " + actionName); //NOI18N
                }
                continue;
            }
            Set<String> s = map.get(action);
            if (s == null) {
                s = new HashSet<String>();
                map.put(action, s);
            }
            s.addAll(keyStrokes);
        }
    }

    /**
     * create Map (String (action name) > Set (String (shortcut AS-M)))
     *
     * @param keyBindings list of MultiKeyBindings
     */
    private static Map<String, Set<String>> convertKeymap(List<MultiKeyBinding> keyBindings) {
        Map<String, Set<String>> actionNameToShortcuts = new HashMap<String, Set<String>>();

        for (int i = 0; i < keyBindings.size(); i++) {
            MultiKeyBinding mkb = keyBindings.get(i);
            StringBuilder sb = new StringBuilder();

            for (int j = 0; j < mkb.getKeyStrokeCount(); j++) {
                if (j > 0) {
                    sb.append(' '); //NOI18N
                }
                sb.append(Utilities.keyToString(mkb.getKeyStrokeList().get(j), true));
            }

            Set<String> keyStrokes = actionNameToShortcuts.get(mkb.getActionName());
            if (keyStrokes == null) {
                keyStrokes = new HashSet<String>();
                actionNameToShortcuts.put(mkb.getActionName(), keyStrokes);
            }
            keyStrokes.add(sb.toString());
        }

        return actionNameToShortcuts;
    }

    private static Map<String, String> readCategories() {
        Map<String, String> result = new HashMap<String, String>();
        FileObject fo = FileUtil.getConfigFile("OptionsDialog/Actions"); //NOI18N
        if (fo == null) {
            return result;
        }
        FileObject[] categories = fo.getChildren();
        for (int i = 0; i < categories.length; i++) {
            String categoryName = categories[i].getName();
            String bundleName = (String) categories [i].getAttribute 
                ("SystemFileSystem.localizingBundle"); //NOI18N
            if (bundleName != null) {
                try {
                    categoryName = NbBundle.getBundle(bundleName).getString(categories[i].getPath());
                } catch (MissingResourceException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
            FileObject[] actions = categories[i].getChildren();
            for (int j = 0; j < actions.length; j++) {
                if (actions[j].getExt().length() > 0) {
                    continue;
                }
                String actionName = actions[j].getName();
                result.put(actionName, categoryName);
            }
        }
        return result;
    }

    public List<String> getProfiles() {
        return null;
    }

    private static KeyStroke[] stringToKeyStrokes2(String key) {
        List<KeyStroke> result = new ArrayList<KeyStroke>();

        for (StringTokenizer st = new StringTokenizer(key, " "); st.hasMoreTokens();) { //NOI18N
            String ks = st.nextToken().trim();
            KeyStroke keyStroke = Utilities.stringToKey(ks);

            if (keyStroke == null) {
                LOG.warning("'" + ks + "' is not a valid keystroke"); //NOI18N
                return null;
            }

            result.add(keyStroke);
        }

        return result.toArray(new KeyStroke[0]);
    }

    private static final class EditorAction implements ShortcutAction {

        private Action action;
        private String name;
        private String id;
        private String delegaitngActionId;

        public EditorAction(Action a) {
            action = a;
        }

        public String getDisplayName() {
            if (name == null) {
                try {
                    name = (String) action.getValue (Action.SHORT_DESCRIPTION);
                } catch (MissingResourceException mre) {
                    Throwable t = new Throwable("The action " + action + " crashed when accessing its short description.", mre); //NOI18N
                    LOG.log(Level.WARNING, null, t);
                    name = null;
                }
                if (name == null) {
                    LOG.warning("The action " + action + " doesn't provide short description, using its name."); //NOI18N
                    name = getId();
                }
                name = name.replace("&", "").trim(); //NOI18N
            }
            return name;
        }

        public String getId() {
            if (id == null) {
                id = (String) action.getValue (Action.NAME);
                assert id != null : "Actions must have name, offending action: " + action; //NOI18N
            }
            return id;
        }

        public String getDelegatingActionId() {
            if (delegaitngActionId == null) {
                delegaitngActionId = (String) action.getValue(NbEditorKit.SYSTEM_ACTION_CLASS_NAME_PROPERTY);
                if (delegaitngActionId != null) {
                    delegaitngActionId = delegaitngActionId.replace(".", "-");
                }
            }
            return delegaitngActionId;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof EditorAction)) {
                return false;
            }
            return ((EditorAction) o).getId().equals(getId());
        }

        @Override
        public int hashCode() {
            return getId().hashCode();
        }

        @Override
        public String toString() {
            return "EditorAction[" + getDisplayName() + ":" + getId() + "]"; //NOI18N
        }

        public ShortcutAction getKeymapManagerInstance(String keymapManagerName) {
            if (EDITOR_BRIDGE.equals(keymapManagerName)) {
                return this;
            } else {
                return null;
            }
        }

        public Action getRealAction() {
            return action;
        }
    } // End of EditorAction
}
