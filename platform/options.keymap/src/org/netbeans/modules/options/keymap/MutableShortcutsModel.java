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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.TextAction;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.core.options.keymap.api.KeyStrokeUtils;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.api.ShortcutsFinder;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.Utilities;

import static org.netbeans.modules.options.keymap.KeymapModel.getKeymapManagerInstances;

/**
 * Wrapper around the {@link KeymapModel}. This wrapper uses human-readable keystroke names,
 * and support local modifications. Once the modifications are {#link #apply applied}, they are
 * written to the shared storage. The underlying ShortcutsFinder (if any) is also
 * refreshed. The model is NOT thread-safe.
 * <p/>
 * The model should be cloned from the global ShortcutsFinder, then the caller may choose
 * to apply() the changes, or simply discard the entire data structure.
 * 
 * @author Svata Dedic
 */
class MutableShortcutsModel extends ShortcutsFinderImpl implements ShortcutsFinder.Writer {
    /**
     * Current profile
     */
    private volatile String              currentProfile;

    /**
     * Key: category name. Value = pair of List&lt;ShortcutAction>. The 1st List
     * holds all actions for the category AND subcategories, the 2nd List holds
     * list of actions in the category only. Initialized lazily by {@link #getItems}
     */
    private Map<String, List<Object>[]> categoryToActionsCache = 
            new HashMap<String, List<Object>[]> ();
    
    /**
     * Profiles, which has been modified. All keybindings are searched in this Map
     * first.
     */
    private volatile Map<String, Map<ShortcutAction, Set<String>>> modifiedProfiles = 
            new HashMap<String, Map<ShortcutAction, Set<String>>> ();
    
    private volatile Set<String> revertedProfiles = new HashSet<String>();
    
    private volatile Set<ShortcutAction> revertedActions = new HashSet<ShortcutAction>();
    
    /**
     * Set of profiles to be deleted
     */
    private volatile Set<String> deletedProfiles = new HashSet<String> ();
    
    /**
     * Global ShortcutsFinder to reset when the keymap is changed.
     */
    @NullAllowed
    private ShortcutsFinder master;
    
    private volatile boolean dirty;
    
    private List<ChangeListener> chListeners;
    
    public MutableShortcutsModel(@NonNull KeymapModel model, ShortcutsFinder master) {
        super(model);
        this.master = master == null ? Lookup.getDefault().lookup(ShortcutsFinder.class) : master;
    }
    
    String getProfileDisplayName(String id) {
        String s = model.getProfileName(id);
        return s != null ? s : id;
    }
    
    List<String> getProfiles () {
        Set<String> result = new HashSet<String> (model.getProfiles ());
        result.addAll (modifiedProfiles.keySet ());
        List<String> r = new ArrayList<String> (result);
        Collections.sort (r);
        return r;
    }
    
    boolean isChangedProfile(String profile) {
        return modifiedProfiles.containsKey(profile);
    }
    
    boolean isCustomProfile (String profile) {
        return model.isCustomProfile (profile);
    }
    
    boolean deleteOrRestoreProfile (String profile) {
        boolean ret;
        
        synchronized (this) {
            if (model.isCustomProfile (profile)) {
                deletedProfiles.add (profile);
                modifiedProfiles.remove (profile);
                clearShortcuts(profile);
                setDirty();
                ret = true;
            } else {
                modifiedProfiles.remove(profile);
                revertedProfiles.add(profile);
                clearShortcuts(profile);
                setDirty();
                ret = false;
            }
        }
        if (!isDirty()) {
            fireChanged();
        }
        return ret;
    }
    
    public void addChangeListener(ChangeListener l) {
        if (chListeners == null) {
            chListeners = new LinkedList<ChangeListener>();
        }
        chListeners.add(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        if (chListeners != null) {
            chListeners.remove(l);
        }
    }
    
    protected void fireChanged() {
        if (chListeners == null || chListeners.isEmpty()) {
            return;
        }
        ChangeListener[] ll = chListeners.toArray(new ChangeListener[0]);
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : ll) {
            l.stateChanged(e);
        }
    }
    
    protected String getCurrentProfile () {
        if (currentProfile == null) {
            return model.getCurrentProfile();
        } else {
            return currentProfile;
        }
    }
    
    void setCurrentProfile (String currentKeymap) {
        this.currentProfile = currentKeymap;
        setDirty();
    }
    
    void setDirty() {
        boolean old = this.dirty;
        this.dirty = isDirty();
        if (old != dirty) {
            fireChanged();
        }
    }
    
    private boolean isDirty() {
        boolean isChanged = !getCurrentProfile().equals(model.getCurrentProfile());
        if (isChanged) { // currently selected profile is modified, so no need to iterate futher
            return true;
        }
        for (KeymapManager m : getKeymapManagerInstances()) {
            List<String> profiles = m.getProfiles();
            if (profiles != null) {
                if(!modifiedProfiles.isEmpty()) {
                    isChanged |= !profiles.containsAll(modifiedProfiles.keySet());
                }
                if (isChanged) { // one or more profiles have been dublicated, so no need to iterate futher
                    return true;
                }
                if(!deletedProfiles.isEmpty()) {
                    isChanged |= profiles.containsAll(deletedProfiles);
                }
                if (isChanged) { // one or more profiles have been deleted, so no need to iterate futher
                    return true;
                }
                for (String profile : profiles) {
                    Map<ShortcutAction, Set<String>> saved = m.getKeymap(profile);
                    Map<ShortcutAction, Set<String>> current = modifiedProfiles.get(profile);
                    if(current != null) {
                        for(Map.Entry<ShortcutAction, Set<String>> entry : current.entrySet()) {
                            Set<String> savedShortcut = saved.get(entry.getKey());
                            Set<String> currentShortcut = current.get(entry.getKey());
                            isChanged |= savedShortcut == null ? !currentShortcut.isEmpty() : !savedShortcut.equals(currentShortcut);
                            if (isChanged) { // a shortcut is found to be modified, so no need to iterate futher
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    synchronized void cloneProfile (String newProfileName) {
        Map<ShortcutAction, Set<String>> result = new HashMap<ShortcutAction, Set<String>> ();
        cloneProfile ("", result);
        modifiedProfiles.put (newProfileName, result);
        // just in case, if the profile was deleted, then created anew
        deletedProfiles.remove(newProfileName);
        setDirty();
    }
    
    private void cloneProfile (
        String category,        // name of currently resolved category
        Map<ShortcutAction, Set<String>> result
    ) {
        Iterator it = getItems (category).iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            String[] shortcuts = getShortcuts ((ShortcutAction) o);
            result.put ((ShortcutAction)o, new HashSet<String> (Arrays.asList (shortcuts)));
        }
    }
    
    public ShortcutAction findActionForShortcut (String shortcut) {
        return findActionForShortcut (shortcut, "", false, null, "");
    }
    
    /**
     * Filters the actions and retains only those which come from the same KeymapManager
     * as the 'anchor' action. Actions from the same keymap manager are typically not allowed
     * to have the same key binding
     * 
     * @param actions actions to filter
     * @param anchor action that identifies the KeymapManager
     * @return filtered action list, as a new collection
     */
    Collection<ShortcutAction> filterSameScope(Set<ShortcutAction> actions, ShortcutAction anchor) {
        return KeymapModel.filterSameScope(actions, anchor);
    }

    /**
     * Finds action with conflicting shortcut (or a prefix, for a multi-keybinding)
     * for a shortcut
     * @param shortcut the shortcut to look for
     * @return action with same shortcut, or shortcutprefix. If the prefix is same
     * but the rest of multi-keybinding is different, returns <code>null</code> (no conflict).
     */
    Set<ShortcutAction> findActionForShortcutPrefix(String shortcut) {
        Set<ShortcutAction> set = new HashSet<ShortcutAction>();
        if (shortcut.length() == 0) {
            return set;
        }
        //has to work with multi-keybinding properly,
        //ie. not allow 'Ctrl+J' and 'Ctrl+J X' at the same time
        if (shortcut.contains(" ")) {
            findActionForShortcut(shortcut.substring(0, shortcut.lastIndexOf(' ')), "", true, set, shortcut);
        } else {
            findActionForShortcut(shortcut, "", true, set, shortcut);
        }
        return set;
    }
    
    private ShortcutAction findActionForShortcut (String shortcut, String category, boolean prefixSearch, Set<ShortcutAction> set, String completeMultikeySC) {
        //search in modified profiles first
        Map<ShortcutAction, Set<String>> map = modifiedProfiles.get(getCurrentProfile());
        if (map != null) {
            for (Map.Entry<ShortcutAction, Set<String>> entry : map.entrySet()) {
                for (String sc : entry.getValue()) {
                    ShortcutAction action = entry.getKey();
                    // special hack for macros; the RunMacro action gets all macro shortcuts assinged
                    if (isImpliedAction(action)) {
                        continue;
                    }
                    if (prefixSearch) {
                        if (sc.equals(shortcut) || (sc.startsWith(completeMultikeySC) && shortcut.equals(completeMultikeySC) && sc.contains(" "))) {
                            set.add(entry.getKey());
                        }
                    } else if (sc.equals(shortcut)) {
                        return entry.getKey();
                    }
                }
            }
        }

        Iterator it = getItems (category).iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            ShortcutAction action = (ShortcutAction) o;
            // special hack for macros; the RunMacro action gets all macro shortcuts assinged
            if (isImpliedAction(action)) {
                continue;
            }
            String[] shortcuts = getShortcuts (action);
            int i, k = shortcuts.length;
            for (i = 0; i < k; i++) {
                if (prefixSearch) {
                    if (shortcuts[i].equals(shortcut) || (shortcuts[i].startsWith(completeMultikeySC) && shortcut.equals(completeMultikeySC) && shortcuts[i].contains(" "))) {
                        set.add(action);
                    }
                } else if (shortcuts[i].equals(shortcut)) {
                    return action;
                }
            }

        }
        return null;
    }

    protected ShortcutAction findActionForId (String actionId, String category, boolean delegate) {
        // check whether the ID is not a duplicate one -> no action found:
        Iterator it = getItems (category).iterator ();
        while (it.hasNext ()) {
            Object o = it.next ();
            String id;
            
            if (delegate) {
                // fallback for issue #197068 - try to find actions also by their classname:
                id = LayersBridge.getOrigActionClass((ShortcutAction)o);
            } else {
                id = ((ShortcutAction) o).getId ();
            }
            if (id != null && actionId.equals (id)) { 
                return (ShortcutAction) o;
            }
        }
        return null;
    }
    
    protected Map<ShortcutAction,Set<String>> getKeymap (String profile) {
        Map<ShortcutAction,Set<String>> base;
        
        if (revertedProfiles.contains(profile)) {
            base = model.getKeymapDefaults(profile);
        } else {
            base = super.getKeymap(profile);
        }
        Map<ShortcutAction,Set<String>> p = modifiedProfiles.get(profile);
        if (p != null) {
            base = new HashMap<ShortcutAction,Set<String>>(base);
            base.putAll(p);
        }
        return base;
    }
    
    public String[] getShortcuts (ShortcutAction action) {
        String profile = getCurrentProfile();
        Map<ShortcutAction,Set<String>> p = modifiedProfiles.get(profile);
        if (p != null) {
            // find it in modified shortcuts
            Map<ShortcutAction, Set<String>> actionToShortcuts = p;
            if (actionToShortcuts.containsKey (action)) {
                Set<String> s = actionToShortcuts.get (action);
                return s.toArray (new String [0]);
            }
        }
        return super.getShortcuts(action);
    }

    /**
     * Set of all shortcuts used by current profile (including modifications)
     * In case there is a multikey keybinding used, its prefix is included
     * @return set of shortcuts
     */
    public Set<String> getAllCurrentlyUsedShortcuts() {
        Set<String> set = new LinkedHashSet<String>();
        //add modified shortcuts, if any
        String profile = getCurrentProfile();
        
        Set<ShortcutAction> processed = new HashSet<ShortcutAction>();
        Map<ShortcutAction, Set<String>> modMap = modifiedProfiles.get(profile);
        if (modMap != null) {
            processed.addAll(modMap.keySet());
            for (Map.Entry<ShortcutAction, Set<String>> entry : modMap.entrySet()) {
                for (String sc : entry.getValue()) {
                    set.add(sc);
                    if (sc.contains(" ")) { // NOI18N
                        set.add(sc.substring(0, sc.indexOf(' ')));
                    }
                }
            }
        }
        //add default shortcuts
        for (Map.Entry<ShortcutAction, Set<String>> entry : getProfileMap(profile).entrySet()) {
            // ignore entries, which are going to be overriden by modifiedProfiles.
            if (processed.contains(entry.getKey())) {
                continue;
            }
            for (String sc : entry.getValue()) {
                    set.add(sc);
                    if (sc.contains(" ")) {
                        set.add(sc.substring(0, sc.indexOf(' ')));
                    }
                }
        }

        return set;
    }

    void addShortcut (ShortcutAction action, String shortcut) {
        // delete old shortcut
        ShortcutAction act = findActionForShortcut (shortcut);
        Set<String> s = new LinkedHashSet<String> ();
        s.addAll (Arrays.asList (getShortcuts (action)));
        s.add (shortcut);
        setShortcuts (action, s);
    }

    /**
     * Reverts shortcuts. If there is a conflict between the restored shortucts and other
     * actions, the method will do nothing unless 'force' is true, and returns collection of conflicting actions.
     * Return value of null indicates successful change.
     * 
     * @param action action to revert
     * @param force if true, does not check conflicts; used after user confirmation
     * @return {@code null} for success, or collection of conflicting actions 
     */
    synchronized Collection<ShortcutAction> revertShortcutsToDefault(ShortcutAction action, boolean force) {
        if (model.isCustomProfile(getCurrentProfile())) {
            return null;
        }
        Map<ShortcutAction, Set<String>> m = model.getKeymapDefaults (getCurrentProfile());
        m = convertFromEmacs(m);
        Set<String> shortcuts = m.get(action);
        if (shortcuts == null) {
            shortcuts = Collections.<String>emptySet(); //this action has no default shortcut
        }
        //lets search for conflicting SCs
        Set<ShortcutAction> conflictingActions = new HashSet<ShortcutAction>();
        for(String sc : shortcuts) {
            ShortcutAction ac = findActionForShortcut(sc);
            if (ac != null && !ac.equals(action)) {
                conflictingActions.add(ac);
            }
        }
        // retain only conflicting actions from the same keymap manager
        Collection<ShortcutAction> filtered = KeymapModel.filterSameScope(conflictingActions, action);
        if (!filtered.isEmpty() && !force) {
            return conflictingActions;
        }
        revertedActions.add(action);
        setShortcuts(action, shortcuts);
        for (ShortcutAction a : filtered) {
            String[] ss = getShortcuts(a);
            Set<String> newSs = new HashSet<String>(Arrays.asList(ss));
            newSs.removeAll(shortcuts);
            setShortcuts(a, newSs);
        }
        return null;
    }

    public synchronized void setShortcuts (ShortcutAction action, Set<String> shortcuts) {
        Map<ShortcutAction, Set<String>> actionToShortcuts = modifiedProfiles.get (getCurrentProfile());
        if (actionToShortcuts == null) {
            actionToShortcuts = new HashMap<ShortcutAction, Set<String>> ();
            modifiedProfiles.put (getCurrentProfile(), actionToShortcuts);
        }
        actionToShortcuts.put (action, shortcuts);
        setDirty();
    }

    public void removeShortcut (ShortcutAction action, String shortcut) {
        Set<String> s = new LinkedHashSet<String> (Arrays.asList (getShortcuts (action)));
        s.remove (shortcut);
        setShortcuts(action, s);
    }
    
    /**
     * Simple guard against scheduling multiple tasks in advance. Also guards
     * against reentrancy.
     */
    private volatile boolean applyInProgress = false;
    
    public void apply () {
        postApply();
    }
    
    private Map<String, Map<ShortcutAction, Set<String>>> cloneProfileMap(Map<String, Map<ShortcutAction, Set<String>>> profiles) {
        Map<String, Map<ShortcutAction, Set<String>>> result = new HashMap<String, Map<ShortcutAction, Set<String>>>(profiles.size());
        for (Map.Entry<String, Map<ShortcutAction, Set<String>>> e : profiles.entrySet()) {
            result.put(e.getKey(), new HashMap<ShortcutAction, Set<String>>(e.getValue()));
        }
        return result;
    }
    
    /* test only */ synchronized Task postApply() {
        if (applyInProgress) {
            return null;
        }
        applyInProgress = true;
        
        final Set<String> revertedProfiles = new HashSet<String>(this.revertedProfiles);
        final Set<ShortcutAction> revertedActions = new HashSet<ShortcutAction>(this.revertedActions);
        final Map<String, Map<ShortcutAction, Set<String>>> modifiedProfiles = 
            cloneProfileMap(this.modifiedProfiles);
        final Set<String> deletedProfiles = new HashSet<String> (this.deletedProfiles);
        final String currentProfile = this.currentProfile;
        
        return RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                for (String profile : revertedProfiles) {
                    try {
                        model.revertProfile(profile);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                if (!revertedActions.isEmpty()) {
                    try {
                        model.revertActions(revertedActions);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                for (Map.Entry<String, Map<ShortcutAction, Set<String>>> entry: modifiedProfiles.entrySet()) {
                    String profile = entry.getKey ();
                    Map<ShortcutAction, Set<String>> actionToShortcuts = entry.getValue();
                    actionToShortcuts = convertToEmacs (actionToShortcuts);
                    model.changeKeymap (
                        profile, 
                        actionToShortcuts
                    );
                }
                for (String profile: deletedProfiles) {
                    model.deleteProfile (profile);
                }
                String prof = currentProfile;
                if (prof == null) {
                    prof = model.getCurrentProfile();
                }
                model.setCurrentProfile (prof);
                
                clearState();
                model = new KeymapModel ();
                applyInProgress = false;
                clearCache();
                
                if (master != null) {
                    master.refreshActions();
                }
            }
        });
    }
    
    public boolean isChanged () {
        return dirty;
    }
    
    private synchronized void clearState() {
        modifiedProfiles = new HashMap<String, Map<ShortcutAction, Set<String>>> ();
        deletedProfiles = new HashSet<String> ();
        revertedActions = new HashSet<ShortcutAction>();
        revertedProfiles = new HashSet<String>();
        currentProfile = null;
        dirty = false;
    }
    
    public void cancel () {
        clearState();
    }

    Map<String, Map<ShortcutAction, Set<String>>> getModifiedProfiles() {
        return modifiedProfiles;
    }

    Set<String> getDeletedProfiles() {
        return deletedProfiles;
    }

    void setModifiedProfiles(Map<String, Map<ShortcutAction, Set<String>>> mp) {
        this.modifiedProfiles = mp;
    }

    void setDeletedProfiles(Set<String> dp) {
        this.deletedProfiles = dp;
    }
    
    /**
     * Converts Map (ShortcutAction > Set (String (shortcut Alt+Shift+P))) to 
     * Map (ShortcutAction > Set (String (shortcut AS-P))).
     */
    private static Map<ShortcutAction, Set<String>> convertToEmacs (Map<ShortcutAction, Set<String>> shortcuts) {
        Map<ShortcutAction, Set<String>> result = new HashMap<ShortcutAction, Set<String>> ();
        for (Map.Entry<ShortcutAction, Set<String>> entry: shortcuts.entrySet()) {
            ShortcutAction action = entry.getKey();
            Set<String> newSet = new HashSet<String> ();
            for (String s: entry.getValue()) {
                if (s.length () == 0) continue;
                KeyStroke[] ks = getKeyStrokes (s, " ");
                if (ks == null) 
                    continue; // unparsable shortcuts ignorred
                StringBuffer sb = new StringBuffer (
                    Utilities.keyToString (ks [0], true)
                );
                int i, k = ks.length;
                for (i = 1; i < k; i++)
                    sb.append (' ').append (Utilities.keyToString (ks [i], true));
                newSet.add (sb.toString ());
            }
            result.put (action, newSet);
        }
        return result;
    }
    
    /** 
     * Returns multi keystroke for given text representation of shortcuts
     * (like Alt+A B). Returns null if text is not parsable, and empty array
     * for empty string.
     */
    private static KeyStroke[] getKeyStrokes (String keyStrokes, String delim) {
        if (keyStrokes.length () == 0) return new KeyStroke [0];
        StringTokenizer st = new StringTokenizer (keyStrokes, delim);
        List<KeyStroke> result = new ArrayList<KeyStroke> ();
        while (st.hasMoreTokens ()) {
            String ks = st.nextToken ().trim ();
            KeyStroke keyStroke = KeyStrokeUtils.getKeyStroke (ks);
            if (keyStroke == null) return null; // text is not parsable 
            result.add (keyStroke);
        }
        return result.toArray (new KeyStroke [0]);
    }
    
    public Set<String> getCategories() {
        return model.getActionCategories();
    }

    /**
     * Returns actions in the category and subcategories
     * @param category
     * @return 
     */
    public List<Object/*Union2<String,ShortcutAction>*/> getItems (String category) {
        return getItems(category, true);
    }
    
    /**
     * Returns list of actions in the given category, and optionally in the sub-categories.
     * 
     * @param category
     * @param prefix
     * @return 
     */
    public List<Object/*Union2<String,ShortcutAction>*/> getItems (String category, boolean prefix) {
        List<ShortcutAction>[] result = (List<ShortcutAction>[])(List[])categoryToActionsCache.get (category);
        if (result == null) {
            List<ShortcutAction> allActions = new ArrayList<ShortcutAction>();
            List<ShortcutAction> thisActions = Collections.emptyList();
            
            Set<String> filtered = new HashSet<String>(model.getActionCategories());
            for (Iterator<String> it = filtered.iterator(); it.hasNext(); ) {
                String cat = it.next();
                if (!cat.startsWith(category)) {
                    it.remove();
                } else if (category.length() > 0 && cat.length() > category.length() && cat.charAt(category.length()) != '/') {
                    it.remove();
                }
            }
            for (String c : filtered) {
                Collection<ShortcutAction> act = model.getActions(c);
                allActions.addAll(act);
                if (c.length() == category.length()) {
                    thisActions = new ArrayList<ShortcutAction>(act);
                }
            }
            Collections.<ShortcutAction>sort (allActions, new KeymapViewModel.ActionsComparator ());
            if (!thisActions.isEmpty()) {
                Collections.<ShortcutAction>sort (thisActions, new KeymapViewModel.ActionsComparator ());
            }
            result = new List[] { allActions , thisActions };
            ((Map)categoryToActionsCache).put (category, result);
        }
        return (List)(prefix ? result[0] : result[1]);
    }

    boolean differsFromDefault(String profile) {
        if (modifiedProfiles.containsKey(profile)) {
            return true;
        }
        if (revertedProfiles.contains(profile)) {
            return false;
        }
        return !model.getKeymapDefaults(profile).equals(model.getKeymap(profile));
    }
}
