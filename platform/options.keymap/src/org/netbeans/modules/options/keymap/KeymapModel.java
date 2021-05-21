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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * There are 3 areas of information loaded using SPI:
 * <ul>
 * <li>profile definitions: set of profiles + current profile
 * <li>actions: all actions registered in the system for keymap configuration
 * <li>keymaps: the actual keymaps (action-keystroke) for individual profiles
 * </ul>
 * Out of those 3, actions loading takes most of the time, as action instances 
 * must be created. At least action initialization should NOT happen in the
 * awt thread.
 * <p/>
 * Each of these areas area loaded by a "loader" (AL, KL, PL), which does all the processing
 * and keeps the loaded data. In the case of multiple initial requests, the loading may
 * happen multiple times, but only one loader will win and register itself in the member
 * variable. The code was originally written to run in a RP, but more simple synchronization
 * was chosen.
 * <p/>
 * Note: the class actually does not hold any own data; all data it serves are collected from
 * KeymapManager instances, and change/deleteProfile will immediately write the changes to those
 * Managers. So it's not necessary to keep multiple instances of KeymapModel - it only takes 
 * initialization time. Therefore {@link KeymapModel#create} should be used preferrably to constructor,
 * so multiple instances in the future are supported.
 *
 * @author Jan Jancura, Svata Dedic
 */
public class KeymapModel {
    
    /* package */ static final RequestProcessor RP = new RequestProcessor(KeymapModel.class);
    
    private static final Logger LOG = Logger.getLogger(KeymapModel.class.getName ());
    private static final Logger UI_LOG = Logger.getLogger("org.netbeans.ui.options"); // NOI18N
                                    
    private static volatile List<KeymapManager> managers = null;
    
    private volatile static KeymapModel INSTANCE;
    
    static KeymapModel create() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        synchronized (KeymapModel.class) {
            return INSTANCE = new KeymapModel();
        }
    }
    
    /**
     * @return All the registered implementations.
     */
    public static Collection<? extends KeymapManager> getKeymapManagerInstances() {
        if (managers != null) {
            return managers;
        }
        
        final Lookup.Result r = Lookup.getDefault().lookupResult(KeymapManager.class);
        ArrayList<KeymapManager> al = new ArrayList<KeymapManager>(r.allInstances());
        al.trimToSize();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Dumping registered KeymapManagers: ");
            for (KeymapManager m : al) {
                LOG.fine("    KeymapManager: " + s2s(m));
            }
            LOG.fine("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }
        synchronized (KeymapModel.class) {
            if (managers == null) {
                managers = al;
                
                r.addLookupListener(new LookupListener() {
                    @Override
                    public void resultChanged(LookupEvent ev) {
                        synchronized (KeymapModel.class) {
                            managers = null;
                            r.removeLookupListener(this);
                        }
                        if (INSTANCE != null) {
                            INSTANCE.refreshActions();
                        }
                    }
                });
            }
        }
        return al;
    }
    
    // actions .................................................................
    
    public Set<String> getActionCategories () {
        AL data = ensureActionsLoaded();
        return data.categories;
    }

    /**
     * Data for set of all actions
     */
    private volatile AL actionData;
    
    /**
     * Data for keymap profiles
     */
    private volatile PL profileData;
    
    /**
     * The keymaps themselves
     */
    private volatile KL keymapData;
    
    /**
     * Loads and processes action data from the underlying KeymapManagers
     */
    private class AL implements Runnable {
        private volatile AL current;
        
        private Set<String> categories;
        
        private Set<String> allActionIds = new HashSet<String>();

        private Set<String> duplicateIds = new HashSet<String>();

        private Map<String,Set<ShortcutAction>> categoryToActions = 
            new HashMap<String,Set<ShortcutAction>>();

        private Map<ShortcutAction,CompoundAction> sharedActions = 
                new HashMap<ShortcutAction,CompoundAction>();

        public void run() {
            if ((current = actionData) != null) {
                return;
            }
            List<Map<String, Set<ShortcutAction>>> mgrActions = new ArrayList<Map<String, Set<ShortcutAction>>>();
            Set<String> categoryIds = new HashSet<String>();
            Map<String, Set<ShortcutAction>> cats = new HashMap<String, Set<ShortcutAction>>();
            Collection<? extends KeymapManager> mgrInstances = getKeymapManagerInstances();
            for (KeymapManager m : mgrInstances) {
                Map<String, Set<ShortcutAction>> a = m.getActions();
                mgrActions.add(a);
                categoryIds.addAll(a.keySet());
            }

            Set<String> allIds = new HashSet<String>();
            Set<String> duplIds = new HashSet<String>();
            categoryIds.add(""); // NOI18N
            for (String category : categoryIds) {
                Iterator<? extends KeymapManager> mgrIt = mgrInstances.iterator();
                Set<ShortcutAction> actions = new HashSet<ShortcutAction>();
                for (Map<String, Set<ShortcutAction>> aa : mgrActions) {
                    Set<ShortcutAction> s = aa.get(category);
                    KeymapManager mgr = mgrIt.next();
                    if (s != null) {
                        actions = mergeActions(actions, s, mgr.getName(), sharedActions);
                    }
                }
                findDuplicateIds(category, actions, allIds, duplIds);
                cats.put(category, actions);
            }

            this.allActionIds = allIds;
            this.duplicateIds = duplIds;
            this.categoryToActions = cats;
            categories = categoryIds;
            
            synchronized (KeymapModel.this) {
                if (actionData != null) {
                    this.current = actionData;
                } else {
                    this.current = actionData = this;
                }
            }
        }

    }
    
    /**
     * Loads profile-related information: set of profiles + the current profile
     */
    private class PL implements Runnable { 
        private volatile PL current;
        private Map<String, String> profilesMap = new HashMap<String, String>();
        private String currentProfile;
        private Map<String, Boolean> customProfiles = new HashMap<String, Boolean>();
        
        public void run() {
            if ((current = profileData) != null) {
                return;
            }
            for (KeymapManager m : getKeymapManagerInstances()) {
                List<String> l = m.getProfiles();
                if (currentProfile == null) {
                    currentProfile = m.getCurrentProfile();
                }
                if (l != null && profilesMap.isEmpty()) {
                    for(String name : l) {
                        profilesMap.put(m.getProfileDisplayName(name), name);
                        customProfiles.put(name, Boolean.TRUE.equals(customProfiles.get(name)) || 
                                m.isCustomProfile(name));
                    }
                }
            }
            if (currentProfile == null) {
                currentProfile = "NetBeans"; // NOI18N
            }
            synchronized (KeymapModel.this) {
                if (profileData == null) {
                    current = profileData = this;
                } else {
                    current = profileData;
                }
            }
        }
    }
    
    private class KL implements Runnable {
        private AL      actionData;
        private String  profile;
        private volatile KL      current;
        
        /**
         * Map (String (profile) > Map (ShortcutAction > Set (String (shortcut AS-M)))).
         */
        private volatile Map<String, Map<ShortcutAction,Set<String>>> keyMaps = 
                new HashMap<String, Map<ShortcutAction,Set<String>>>();

        /**
         * Map (String (keymap name) > Map (ShortcutAction > Set (String (shortcut AS-M)))).
         */
        private Map<String,Map<ShortcutAction,Set<String>>> keyMapDefaults = 
                new HashMap<String,Map<ShortcutAction,Set<String>>>();
        
        public KL(AL actionData, String profile) {
            this.actionData = actionData;
            this.profile = profile;
        }

        public void run() {
            current = keymapData;
            if (current != null && current.keyMaps.get(profile) != null) {
                return;
            }
            Map<ShortcutAction,Set<String>>  res;
            Map<ShortcutAction,Set<String>>  defRes;
            res = new HashMap<ShortcutAction,Set<String>>();
            defRes = new HashMap<ShortcutAction,Set<String>>();

            for (KeymapManager m : getKeymapManagerInstances()) {
                Map<ShortcutAction,Set<String>> mm = m.getKeymap(profile);
                res = mergeShortcuts(res, mm, actionData.sharedActions);

                mm = m.getDefaultKeymap(profile);
                defRes = mergeShortcuts(defRes, mm, actionData.sharedActions);
            }
            
            synchronized (this) {
                if (keymapData != null && keymapData.keyMaps.get(profile) != null) {
                    current = keymapData;
                } else {
                    if (keymapData != null) {
                        keyMaps.putAll(keymapData.keyMaps);
                        keyMapDefaults.putAll(keymapData.keyMapDefaults);
                    }
                    keyMaps.put(profile, res);
                    keyMapDefaults.put(profile, defRes);
                    keymapData = current = this;
                }
            }
        }
    }
    
    private PL ensureProfilesLoaded() {
        PL p = profileData;
        if (p == null) {
            waitFinished(p = new PL());
        }
        return p.current;
    }
    
    private KL ensureKeymapsLoaded(String forProfile) {
        KL k = keymapData;
        if (k == null || k.keyMaps.get(forProfile) == null) {
            waitFinished(k = new KL(ensureActionsLoaded(), forProfile));
        }
        return k.current;
    }
    
    /**
     * Map (String (category name) > Set (ShortcutAction)).
     */
    
    // @GuardedBy(this)
    private static void findDuplicateIds(String category, Collection<ShortcutAction> actions, Set<String> allActionIds, Set<String> duplicateIds) {
        for (ShortcutAction sa : actions) {
            String id = sa.getId();
            
            if (!allActionIds.add(id)) {
                duplicateIds.add(id);
                continue;
            }
            // also check fallback - the classname:
            id = LayersBridge.getOrigActionClass(sa);
            if (id != null && !allActionIds.add(id)) {
                duplicateIds.add(id);
            } 
        }
    }

    /**
     * Returns List (ShortcutAction) of all global and editor actions.
     */
    public Set<ShortcutAction> getActions(final String category) {
        AL al = ensureActionsLoaded();
        Set<ShortcutAction>  actions = al.categoryToActions.get(category);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Category '" + category + "' actions (" + actions.size() + "), KeymapModel=" + this + ":"); //NOI18N
            for(ShortcutAction sa : actions) {
                LOG.fine("    id='" + sa.getId() + "', did='" + sa.getDelegatingActionId() + ", " + s2s(sa)); //NOI18N
            }
            LOG.fine("---------------------------"); //NOI18N
        }
        return actions;
    }
    
    boolean isDuplicateId(String id) {
        AL al = ensureActionsLoaded();
        if (!al.duplicateIds.contains(id)) {
            return false;
        }
        LOG.log(Level.WARNING, "Duplicate action ID used: {0}", new Object[] { id });
        
        return true;
    }

    /**
     * Clear action caches.
     */
    public void refreshActions () {
        synchronized (this) {
            actionData = null;
            profileData = null;
            keymapData = null;
        }

        for (KeymapManager m : getKeymapManagerInstances()) {
            synchronized(LOCK) {
                m.refreshActions();
            }
        }
    }
    
    // keymaps .................................................................
    
    public String getCurrentProfile () {
        return ensureProfilesLoaded().currentProfile;
    }
    
    public void setCurrentProfile (String profileName) {
        String prev = getCurrentProfile();
        if (!prev.equals(profileName)) {
            LogRecord rec = new LogRecord(Level.CONFIG, "KEYMAP_SET_PROFILE"); // NOI18N
            rec.setParameters(new Object[]{ profileName, prev });
            rec.setResourceBundle(NbBundle.getBundle(KeymapModel.class));
            rec.setResourceBundleName(KeymapModel.class.getPackage().getName() + ".Bundle");
            rec.setLoggerName(UI_LOG.getName());
            UI_LOG.log(rec);
        }
        
        final String profile = displayNameToName(profileName);
        
        waitFinished(new Runnable() {
            public void run() {
                for (KeymapManager m : getKeymapManagerInstances()) {
                    m.setCurrentProfile(profile);
                }
                profileData = null;
            }
        });
    }
    
    /**
     * Reverts an action in KeymapManagers, which support the revert
     * operation. 
     * 
     * @param ac
     * @throws IOException 
     */
    public void revertActions(final Collection<ShortcutAction> ac) throws IOException {
        final IOException[] exc = new IOException[1];
        
        waitFinished(new Runnable() {
            public void run() {
                try {
                    for (KeymapManager m : getKeymapManagerInstances()) {
                        if (m instanceof KeymapManager.WithRevert) {
                            try {
                                ((KeymapManager.WithRevert)m).revertActions(getCurrentProfile(), ac);
                            } catch (IOException ex) {
                                exc[0] = ex;
                                return;
                            }
                        } else {
                            Map<ShortcutAction, Set<String>> actions = m.getDefaultKeymap(getCurrentProfile());
                            Map<ShortcutAction, Set<String>> keymap = new HashMap<ShortcutAction, Set<String>>(m.getKeymap(getCurrentProfile()));
                            for (ShortcutAction a : ac) {
                                Set<String> defKeys = actions.get(a);
                                if (defKeys == null) {
                                    keymap.remove(a);
                                } else {
                                    keymap.put(a, defKeys);
                                }
                            }
                            m.saveKeymap(getCurrentProfile(), keymap);
                        }
                    }
                } finally {
                    synchronized (KeymapModel.this) {
                        keymapData = null;
                    }
                }
            }
        });
        if (exc[0] != null) {
            throw exc[0];
        }
    }
    
    public void revertProfile(final String profileName) throws IOException {
        final IOException[] exc = new IOException[1];
        
        waitFinished(new Runnable() {
            public void run() {
                try {
                    for (KeymapManager m : getKeymapManagerInstances()) {
                        if (m instanceof KeymapManager.WithRevert) {
                            try {
                                ((KeymapManager.WithRevert)m).revertProfile(profileName);
                            } catch (IOException ex) {
                                exc[0] = ex;
                                return;
                            }
                        } else {
                            m.saveKeymap(profileName, m.getDefaultKeymap(profileName));
                        }
                    }
                } finally {
                    synchronized (KeymapModel.this) {
                        profileData = null;
                        keymapData = null;
                    }
                }
            }
        });
        if (exc[0] != null) {
            throw exc[0];
        }
    }
    
    public List<String> getProfiles () {
        return new ArrayList<String>(getProfilesMap().keySet());
    }
    
    public boolean isCustomProfile (String profile) {
        profile = displayNameToName(profile);
        Boolean b = ensureProfilesLoaded().customProfiles.get(profile);
        return b == null || b.booleanValue();
    }
    
    /**
     * Returns Map (ShortcutAction > Set (String (shortcut))).
     */
    public Map<ShortcutAction,Set<String>> getKeymap (String profile) {
        profile = displayNameToName(profile);
        return ensureKeymapsLoaded(profile).keyMaps.get(profile);
    }
    
    /**
     * Returns Map (ShortcutAction > Set (String (shortcut))).
     */
    public Map<ShortcutAction, Set<String>> getKeymapDefaults(String profile) {
        profile = displayNameToName(profile);
        return ensureKeymapsLoaded(profile).keyMapDefaults.get(profile);
    }
    
    public void deleteProfile(String profile) {
        profile = displayNameToName(profile);
        for (KeymapManager m : getKeymapManagerInstances()) {
            synchronized(LOCK) {
                m.deleteProfile(profile);
            }
        }
    }
    
    /**
     * Defines new shortcuts for some actions in given keymap.
     * Map (ShortcutAction > Set (String (shortcut AS-M P)).
     */
    public void changeKeymap(String profileName, Map<ShortcutAction,Set<String>> actionToShortcuts) {
        final String profile = displayNameToName(profileName);
        
        log ("changeKeymap.actionToShortcuts", actionToShortcuts.entrySet ());

        // 1) mix changes with current keymap and put them to cached current shortcuts
        final Map<ShortcutAction,Set<String>> m = 
                new HashMap<ShortcutAction,Set<String>>(getKeymap(profile));
        m.putAll (actionToShortcuts);
        
        waitFinished(new Runnable() {
            public void run() {
                KL k = keymapData;
                if (k != null) {
                    Map<String, Map<ShortcutAction,Set<String>>> newMap = new HashMap<>();
                    newMap.putAll(k.keyMaps);
                    newMap.put(profile, m);
                    k.keyMaps = newMap;
                }
                log ("changeKeymap.m", m.entrySet ());
                for (KeymapManager km : getKeymapManagerInstances()) {
                    km.saveKeymap(profile, m);
                }
            }
        });
    }
    
    
    // private methods .........................................................
    
    private void log(String name, Collection items) {
        if (!LOG.isLoggable(Level.FINE)) return;
        
        LOG.fine(name);
        for(Iterator i = items.iterator(); i.hasNext(); ) {
            Object item = i.next();
            LOG.fine("  " + item); //NOI18N
        }
    }
    
    /**
     * Merges editor actions and layers actions. Creates CompoundAction for
     * actions like Copy, registerred to both contexts.
     */
    /* package-test */ static Set<ShortcutAction> mergeActions (
        Collection<ShortcutAction> res, Collection<ShortcutAction> adding, String name, 
        Map<ShortcutAction, CompoundAction> sharedActions) {
        
        Set<ShortcutAction> result = new HashSet<ShortcutAction>();
        Map<String,ShortcutAction> idToAction = new HashMap<String,ShortcutAction>();
        Map<String,ShortcutAction> delegateIdToAction = new HashMap<String,ShortcutAction>();
        for (ShortcutAction action: res) {
            String id = action.getId();
            idToAction.put(id, action);
            String delegate = action.getDelegatingActionId();
            if (delegate != null) {
                delegateIdToAction.put(delegate, action);
            }
        }
        
        for (ShortcutAction action : adding) {
            String id = action.getId();

            if (delegateIdToAction.containsKey(id)) {
                ShortcutAction origAction = delegateIdToAction.remove(id);
                idToAction.remove(origAction.getId());
                KeymapManager origActionKeymapManager = findOriginator(origAction);
                Map<String, ShortcutAction> ss = new HashMap<String, ShortcutAction>();
                ss.put(origActionKeymapManager.getName(), origAction);
                ss.put(name,action);
                CompoundAction compoundAction = new CompoundAction(ss);
                result.add(compoundAction);
                sharedActions.put(origAction, compoundAction);
                sharedActions.put(action, compoundAction);
                continue;
            }
            String delegatingId = action.getDelegatingActionId();
            if (idToAction.containsKey(delegatingId)) {
                ShortcutAction origAction = idToAction.remove(delegatingId);
                KeymapManager origActionKeymapManager = findOriginator(origAction);
                Map<String, ShortcutAction> ss = new HashMap<String, ShortcutAction>();
                ss.put(origActionKeymapManager.getName(), origAction);
                ss.put(name,action);
                CompoundAction compoundAction = new CompoundAction(ss);
                result.add(compoundAction);
                sharedActions.put(origAction, compoundAction);
                sharedActions.put(action, compoundAction);
                continue;
            }
            ShortcutAction old = idToAction.get(id);
            if (old != null) {
                if (old instanceof CompoundAction) {
                    ((CompoundAction)old).addAction(name, action);
                    sharedActions.put(action, (CompoundAction)old);
                } else {
                    idToAction.remove(id);
                    ShortcutAction origAction = old;
                    KeymapManager origActionKeymapManager = findOriginator(origAction);
                    Map<String, ShortcutAction> ss = new HashMap<String, ShortcutAction>();
                    ss.put(origActionKeymapManager.getName(), origAction);
                    ss.put(name,action);
                    CompoundAction compoundAction = new CompoundAction(ss);
                    // must remove
                    result.remove(origAction);
                    result.add(compoundAction);
                    sharedActions.put(origAction, compoundAction);
                    sharedActions.put(action, compoundAction);
                }
                continue;
            }
            if (!sharedActions.containsKey(action)) {
                result.add(action);
            }
        }
        result.addAll(idToAction.values());
        return result;
    }
    
    static Collection<ShortcutAction> filterSameScope(Set<ShortcutAction> actions, ShortcutAction anchor) {
        KeymapManager mgr = findOriginator(anchor);
        if (mgr == null) {
            return Collections.emptyList();
        }
        Collection<ShortcutAction> sameActions = null;
        
        for (ShortcutAction sa : actions) {
            KeymapManager m2 = findOriginator(sa);
            if (mgr == m2) {
                if (sameActions == null) {
                    sameActions = new LinkedList<ShortcutAction>();
                }
                sameActions.add(sa);
            }
        }
        return sameActions == null ? Collections.emptyList() : sameActions;
    }

    /**
     * Tries to determince where the action originates.
     */
    private static KeymapManager findOriginator(ShortcutAction a) {
        for (KeymapManager km : getKeymapManagerInstances()) {
            if (a.getKeymapManagerInstance(km.getName()) != null) {
                return km;
            }
        }
        return null;
    }
    
    private static Map<ShortcutAction,Set<String>> mergeShortcuts (
        Map<ShortcutAction,Set<String>> res,
        Map<ShortcutAction,Set<String>> adding,
        Map<ShortcutAction, CompoundAction> sharedActions) {

        for (ShortcutAction action : adding.keySet()) {
            Set<String> shortcuts = adding.get(action);
            if (shortcuts.isEmpty()) {
                continue;
            }
            if (sharedActions.containsKey (action)) {
                action = sharedActions.get(action);
            }
            res.put(action, shortcuts);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Action='" + action.getId() + "' (" + s2s(action) + ") shortcuts: " + shortcuts);
            }
        }
        return res;
    }

    private AL ensureActionsLoaded() {
        AL al = actionData;
        if (al != null) {
            return al;
        }
        al = new AL();
        waitFinished(al);
        return al.current;
    }

    private String displayNameToName(String keymapDisplayName) {
        String name = getProfilesMap().get(keymapDisplayName);
        return name == null ? keymapDisplayName : name;
    }
    
    String getProfileName(String id) {
        Map<String, String> m = getProfilesMap();
        for (Map.Entry<String, String> e :m.entrySet()) {
            if (e.getValue().equals(id)) {
                return e.getKey();
            }
        }
        return null;
    }
    
    static final Object LOCK = new String("Keymap lock");

    /**
     * All calls to KeymapManagers should go through this method. Keymap managers themselves are not
     * too synchronized, that keeps them relatively simple.
     * 
     * @param r A the action to execute
     */
    static void waitFinished(Runnable r) {
        synchronized (LOCK) {
            r.run();
        }
        /*
          if RP is ever needed to initialize the actions
        if (RP.isRequestProcessorThread()) {
            r.run(); 
        } else {
            RP.post(r).waitFinished();
        }
        */
    }
    
    private Map<String, String> getProfilesMap() {
        return ensureProfilesLoaded().profilesMap;
    }
    
    public KeymapModel() {
//        System.out.println("\n\n\n~~~ Dumping all actions in all categories:");
//        TreeSet<String> categories = new TreeSet<String>(getActionCategories());
//        for(String category : categories) {
//            System.out.println("Category='" + category + "'");
//            TreeMap<String, ShortcutAction> actions = new TreeMap<String, ShortcutAction>();
//            for(ShortcutAction sa : getActions(category)) {
//                assert sa != null : "ShortcutAction must not be null";
//                assert sa.getId() != null : "Action Id must not be null";
//
//                if (actions.containsKey(sa.getId())) {
//                    System.out.println("! Duplicate action detected: '" + sa.getId()
//                        + "', delegatingId='" + sa.getDelegatingActionId()
//                        //+ "', displayName='" + sa.getDisplayName()
//                        + "', " + s2s(sa));
//                }
//
//                actions.put(sa.getId(), sa);
//            }
//
//            for(String id : actions.keySet()) {
//                ShortcutAction sa = actions.get(id);
//                System.out.println("Id='" + sa.getId()
//                    + "', delegatingId='" + sa.getDelegatingActionId()
//                    //+ "', displayName='" + sa.getDisplayName()
//                    + "', " + s2s(sa));
//            }
//        }
//        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n\n");

        // HACK - loads all actions. othervise during second open of Options
        // Dialog (after cancel) map of sharedActions is not initialized.
    }

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }
}
