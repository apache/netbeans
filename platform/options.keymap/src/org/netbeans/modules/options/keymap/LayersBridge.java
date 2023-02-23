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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.openide.ErrorManager;

import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * Bridge to old layers based system.
 *
 * @author Jan Jancura
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.core.options.keymap.spi.KeymapManager.class)
public class LayersBridge extends KeymapManager implements KeymapManager.WithRevert {
    
    /**
     * Extension for DataObjects, which cause an action to be removed from the parent (general) keymap.
     */
    private static final String EXT_REMOVED = "removed"; // NOI18N
    
    private static final Logger LOG = Logger.getLogger(LayersBridge.class.getName());
    
    static final String         KEYMAPS_FOLDER = "Keymaps";
    private static final String SHORTCUTS_FOLDER = "Shortcuts";
    
    private static final String LAYERS_BRIDGE = "LayersBridge";
    
    /** Map (GlobalAction > DataObject). */
    private Map<GlobalAction, DataObject> actionToDataObject = 
            new HashMap<GlobalAction, DataObject> ();
    /** Map (String (folderName) > Set (GlobalAction)). */
    private Map<String, Set<ShortcutAction>> categoryToActions;
    /** Set (GlobalAction). */
    private Map<GlobalAction, GlobalAction> actions = new HashMap<GlobalAction, GlobalAction> ();
    
    /**
     * Listener attached to action foldes, which will schedule rebuild. The rebuild
     * is delayed to avoid excessive rebuilding after module with more actions is enabled/disabled.
     */
    private final FileChangeListener fileListener = new FileChangeAdapter() {
        @Override
        public void fileDeleted(FileEvent fe) {
            scheduleRebuild();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            scheduleRebuild();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            scheduleRebuild();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            scheduleRebuild();
        }
    };
    
    /**
     * Listens on action folders, provokes rebuild
     */
    private final FileChangeListener  weakFolderL = WeakListeners.create(FileChangeListener.class, fileListener, null);
    
    public LayersBridge() {
        super(LAYERS_BRIDGE);
    }
    
    private void scheduleRebuild() {
        synchronized (this) {
            categoryToActions = null;
        }
    }

    /**
     * Returns Map (String (folderName) > Set (GlobalAction)).
     */
    public synchronized Map<String, Set<ShortcutAction>> getActions () {
        if (categoryToActions == null) {
            categoryToActions = new HashMap<String, Set<ShortcutAction>> ();
            initActions ("Actions", null);               // NOI18N
            categoryToActions.remove ("Hidden");                       // NOI18N
            categoryToActions = Collections.unmodifiableMap (categoryToActions);
        }
        return categoryToActions;
    }
    
    /**
     * FileObjects the bridge used to load actions the last time. Null for the first
     * initialization. Used to detach listeners.
     */
    // @GuardedBy(this)
    private Collection<FileObject>  loadedFromFolders;

    private void initActions (String folder, String category) {
        if (loadedFromFolders != null) {
            for (FileObject f : loadedFromFolders) {
                f.removeFileChangeListener(weakFolderL);
            }
        }
        FileObject fo = FileUtil.getConfigFile(folder);
        if (fo == null) return;
        DataFolder root = DataFolder.findFolder (fo);
        if (loadedFromFolders == null) {
            // the root must exist all the time, attach just once:
            root.getPrimaryFile().addFileChangeListener(weakFolderL);
        }
        Enumeration<DataObject> en = root.children ();
        Collection<FileObject> newFolders = new ArrayList<>(7);
        while (en.hasMoreElements ()) {
            DataObject dataObject = en.nextElement ();
            if (dataObject instanceof DataFolder) {
                initActions ((DataFolder) dataObject, null, category, newFolders);
            }
        }
        this.loadedFromFolders = newFolders;
    }
    
    private void initActions (
        DataFolder folder, 
        String folderName, 
        String category, 
        Collection<FileObject> folders
    ) {
        
        // 1) reslove name
        String name = folder.getName ();
        if (category != null)
            name = category;
        else {
            String bundleName = (String) folder.getPrimaryFile ().getAttribute 
                ("SystemFileSystem.localizingBundle");
            if (bundleName != null)
                try {
                    name = NbBundle.getBundle (bundleName).getString (
                        folder.getPrimaryFile ().getPath ()
                    );
                } catch (MissingResourceException ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
            if (folderName != null) 
                name = folderName + '/' + name;
        }
        folders.add(folder.getPrimaryFile());
        // watch out for changes
        folder.getPrimaryFile().addFileChangeListener(weakFolderL);
        Enumeration en = folder.children ();
        while (en.hasMoreElements ()) {
            DataObject dataObject = (DataObject) en.nextElement ();
            if (dataObject instanceof DataFolder) {
                initActions ((DataFolder) dataObject, name, category, folders);
                continue;
            }
            GlobalAction action = createAction (dataObject, name, dataObject.getPrimaryFile().getName(), false);
            if (action == null) continue;
            if (actions.containsKey (action)) continue;
            actions.put (action, action);
            
            // add to actions (Map (String (folderName) > Set (GlobalAction))).
            Set<ShortcutAction> a = categoryToActions.get (name);
            if (a == null) {
                a = new HashSet<ShortcutAction> ();
                categoryToActions.put (name, a);
            }
            a.add (action);
            
            while (dataObject instanceof DataShadow)
                dataObject = ((DataShadow) dataObject).getOriginal ();
            
            actionToDataObject.put (action, dataObject);
        }
    }
    
    private volatile List<String> keymapNames;
    private volatile Map<String, String> keymapDisplayNames;

    private void refreshKeymapNames() {
        DataFolder root = getRootFolder(KEYMAPS_FOLDER, null);
        Enumeration en = root.children(false);
        List<String> names = new ArrayList<String>();
        Map<String, String> displayNames = new HashMap<String, String>();
        while (en.hasMoreElements()) {
            FileObject f = ((DataObject) en.nextElement()).getPrimaryFile();
            if (f.isFolder()) {
                String name = f.getNameExt();
                String displayName;

                try {
                    displayName = f.getFileSystem().getDecorator().annotateName(name, Collections.singleton(f));
                } catch (FileStateInvalidException fsie) {
                    // ignore
                    displayName = name;
                }
                names.add(name);
                displayNames.put(name, displayName);
            }
        }
        if (names.isEmpty()) {
            names.add("NetBeans"); //NOI18N
        }
        synchronized (this) {
            this.keymapNames = names;
            this.keymapDisplayNames = displayNames;
        }
    }

    public List<String> getProfiles() {
        List<String> names = keymapNames;
        if (names == null) {
            refreshKeymapNames();
            // keymapNames is not erased
            names = keymapNames;
        }
        return Collections.unmodifiableList(names);
    }
    
    public @Override String getProfileDisplayName(String profileName) {
        Map<String, String> m = keymapDisplayNames;
        if (m == null) {
            refreshKeymapNames();
            m = keymapDisplayNames;
        }
        String displayName = m.get(profileName);
        return displayName == null ? profileName : displayName;
    }
    
    /** Profile to Map of GlobalAction to set of shortcuts. */
    private Map<String, Map<ShortcutAction, Set<String>>> keymaps = 
            new HashMap<String, Map<ShortcutAction, Set<String>>> ();
    
    /**
     * The base keymap, shared for all profiles. Used as a baseline when generating
     * 'removed' instructions for a profile.
     */
    private volatile Map<ShortcutAction, Set<String>> baseKeyMap;
    
    /**
     * Returns Map (GlobalAction > Set (String (shortcut))).
     */
    public Map<ShortcutAction, Set<String>> getKeymap (String profile) {
        if (!keymaps.containsKey (profile)) {
            DataFolder root = getRootFolder (SHORTCUTS_FOLDER, null);
            Map<ShortcutAction, Set<String>> m = readKeymap (root);
            root = getRootFolder (KEYMAPS_FOLDER, profile);
            overrideWithKeyMap(m, readKeymap(root), profile);
            m.remove(REMOVED);
            keymaps.put (profile, m);
        }
        return Collections.unmodifiableMap (keymaps.get (profile));
    }
        
    private Map<ShortcutAction, Set<String>> getBaseKeyMap() {
        if (baseKeyMap == null) {
            DataFolder root = getRootFolder (SHORTCUTS_FOLDER, null);
            Map<ShortcutAction, Set<String>> m = readKeymap (root);
            baseKeyMap = m;
        }
        return baseKeyMap;
    }
    
    /**
     * Overrides the base shortcut map with contents of the Keymap. If keymap specifies
     * a shortcut which is already used in the 'base', the shortcut mapping is removed from the base
     * and only the keymap mapping will prevail.
     * 
     * @param base base keymap
     * @param keyMap override keymap, from the profile
     * @return 
     */
    private Map<ShortcutAction, Set<String>> overrideWithKeyMap(Map<ShortcutAction, Set<String>> base,
            Map<ShortcutAction, Set<String>> keyMap, String profile) {
        Set<String> overrideKeyStrokes = new HashSet<String>();
        Map<String, ShortcutAction> shortcuts = null;
        
        for (Set<String> strSet : keyMap.values()) {
            overrideKeyStrokes.addAll(strSet);
        }

        for (Iterator<Map.Entry<ShortcutAction,Set<String>>> it = base.entrySet().iterator();
                it.hasNext();) {
            Map.Entry<ShortcutAction,Set<String>> en = it.next();
            Set<String> keys = en.getValue();

            if (LOG.isLoggable(Level.FINER)) {
                for (String s : keys) {
                    if (overrideKeyStrokes.contains(s)) {
                        if (shortcuts == null) {
                            shortcuts = shortcutToAction(keyMap);
                        }
                        ShortcutAction sa = shortcuts.get(s);
                        if (!sa.getId().equals(en.getKey().getId())) {
                            LOG.finer("[" + profile + "] change keybinding " + s + " from " + en.getKey().getId() + " to " + sa.getId());
                        }
                    }
                }
            }
            
            keys.removeAll(overrideKeyStrokes);
            if (keys.isEmpty()) {
                it.remove();
            }
        }
        base.putAll(keyMap);
        return base;
    }
    
    /** Map (String (profile) > Map (GlobalAction > Set (String (shortcut)))). */
    private Map<String, Map<ShortcutAction, Set<String>>> keymapDefaults = 
            new HashMap<String, Map<ShortcutAction, Set<String>>> ();
    
    /**
     * Returns Map (GlobalAction > Set (String (shortcut))).
     */
    public synchronized Map<ShortcutAction, Set<String>> getDefaultKeymap (String profile) {
        if (!keymapDefaults.containsKey (profile)) {
            DataFolder root = getRootFolder (SHORTCUTS_FOLDER, null);
            Map<ShortcutAction, Set<String>> m = readKeymap (root, true);
            overrideWithKeyMap(m, readOriginalKeymap(root), profile);
            root = getRootFolder (KEYMAPS_FOLDER, profile);
            overrideWithKeyMap(m, readKeymap(root, true), profile);
            overrideWithKeyMap(m, readOriginalKeymap(root), profile);
            m.remove(REMOVED);
            keymapDefaults.put (profile, m);
        }
        return Collections.unmodifiableMap (keymapDefaults.get (profile));
    }
    
    DataObject getDataObject (Object action) {
        return actionToDataObject.get (action);
    }
    
    /**
     * Placeholder, which indicates shortcut(s) that should be removed. Must be used
     * only internally !
     */
    private static final GlobalAction REMOVED = new GlobalAction(null, null, "<removed>") {
        { 
            name = ""; // NOI18N
        }
    };
    
    public void revertProfile(final String profile) throws IOException {
        final DataFolder root = getRootFolder (KEYMAPS_FOLDER, profile);
        if (root == null) {
            return;
        }
        final Collection<Callable> reverts = (Collection<Callable>)root.getPrimaryFile().getAttribute("revealEntries");
        root.getPrimaryFile().getFileSystem().runAtomicAction(new AtomicAction() {
            public void run() throws IOException {
                for (Callable c : reverts) {
                    try {
                        c.call();
                    } catch (IOException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        throw new IOException("Unexpected error", ex);
                    }
                }
                FileObject[] ch = root.getPrimaryFile().getChildren();
                for (FileObject f : ch) {
                    if (f.canRevert()) {
                        f.revert();
                    }
                }
                //root.getPrimaryFile().revert();
                root.getPrimaryFile().refresh();
            }
        });
        keymaps.remove(profile);
    }
    
    /** 
     * Reads original keymap entries, which are now replaced or deleted by the user's
     * writable layer.
     * 
     * @param root
     * @return 
     */
    private Map<ShortcutAction, Set<String>> readOriginalKeymap(DataFolder root) {
        Map<ShortcutAction, Set<String>> keymap = 
                new HashMap<ShortcutAction, Set<String>> ();
        if (root == null) return keymap;

        FileObject fo = root.getPrimaryFile();
        Collection<FileObject> entries = (Collection<FileObject>)fo.getAttribute("revealEntries");
        Set<String> names = new HashSet<String>();
        for (FileObject f : entries) {
            try {
                GlobalAction action;
                
                names.add(f.getName());
                if (EXT_REMOVED.equals(f.getExt()) && FileUtil.findBrother(f, "shadow") == null) {
                    action = REMOVED;
                } else {
                    FileObject orig = DataShadow.findOriginal(f);
                    if (orig == null) {
                        continue;
                    }
                    DataObject dataObject = DataObject.find(orig);

                    if (dataObject instanceof DataFolder) continue;
                    action = createActionWithLookup (dataObject, null, f.getName(), false);
                }
                if (action == null) continue;
                String shortcut = f.getName();

                LOG.log(Level.FINEST, "Overriden action {0}: {1}, by {2}", new Object[] {
                    action.getId(),
                    shortcut,
                    f.getPath()
                });
                Set<String> s = keymap.get (action);
                if (s == null) {
                    s = new HashSet<String> ();
                    keymap.put (action, s);
                }
                s.add (shortcut);
            } catch (IOException ex) {
                // handle somehow
            }
        }
        return keymap;
    }
    
    public void revertActions(String profile, Collection<ShortcutAction> actions) throws IOException {
        Map<ShortcutAction, Set<String>> defaultKeyMap = getDefaultKeymap(profile);
        Map<ShortcutAction, Set<String>> keyMap = getKeymap(profile);
        
        DataFolder root = getRootFolder (KEYMAPS_FOLDER, profile);
        if (root == null) {
            return;
        }
        final FileObject fo = root.getPrimaryFile();
        final Collection<FileObject> entries = (Collection<FileObject>)fo.getAttribute("revealEntries");
        final Set<String> keys = new HashSet<String>();
        final Set<String> discard = new HashSet<String>();
        
        for (ShortcutAction ac : actions) {
            Set<String> sc = defaultKeyMap.get(ac);
            if (sc != null) {
                keys.addAll(sc);
            }
            sc = keyMap.get(ac);
            if (sc != null) {
                discard.addAll(sc);
            }
        }

        fo.getFileSystem().runAtomicAction(new AtomicAction() {
            public void run() throws IOException {
                if (keys != null) {
                    for (FileObject f : entries) {
                        if (keys.remove(f.getName())) {
                            try {
                                ((Callable)f).call();
                            } catch (IOException ex) {
                                throw ex;
                            } catch (Exception ex) {
                                throw new IOException("Cannot revert", ex);
                            }
                            
                        }
                    }
                    // for remaining keys, try to discover the .remove mask
                    for (Iterator<String> it = keys.iterator(); it.hasNext(); ) {
                        String s = it.next();
                        FileObject mask = fo.getFileObject(s, "removed");
                        if (mask != null) {
                            mask.delete();
                            it.remove();
                        }
                    }
                }
                for (String s : discard) {
                    FileObject ex = fo.getFileObject(s, "shadow");
                    if (ex != null) {
                        ex.delete();
                    }
                }
            }
        });
    }
    
    private Map<ShortcutAction, Set<String>> readKeymap (DataFolder root) {
        return readKeymap(root, false);
    }
    
    /**
     * Read keymap from one folder Map (GlobalAction > Set (String (shortcut))).
     */
    private Map<ShortcutAction, Set<String>> readKeymap (DataFolder root, boolean ignoreUserRemoves) {
        LOG.log(Level.FINEST, "Reading keymap from: {0}", root);
        Map<ShortcutAction, Set<String>> keymap = 
                new HashMap<ShortcutAction, Set<String>> ();
        if (root == null) return keymap;
        Enumeration<DataObject> en = root.children (false);
        while (en.hasMoreElements ()) {
            DataObject dataObject = en.nextElement ();
            if (dataObject instanceof DataFolder) continue;
            GlobalAction action = createActionWithLookup (dataObject, null, dataObject.getPrimaryFile().getName(), ignoreUserRemoves);
            if (action == null) continue;
            String shortcut = dataObject.getPrimaryFile().getName().toUpperCase();
            
            LOG.log(Level.FINEST, "Action {0}: {1}, by {2}", new Object[] {
                action.getId(),
                shortcut,
                dataObject.getPrimaryFile().getPath()
            });
            Set<String> s = keymap.get (action);
            if (s == null) {
                s = new HashSet<String> ();
                keymap.put (action, s);
            }
            s.add (shortcut);
        }
        return keymap;
    }

    @Override
    public void deleteProfile (String profile) {
        FileObject root = FileUtil.getConfigFile(KEYMAPS_FOLDER);
        if (root == null) return;
        root = root.getFileObject (profile);
        if (root == null) return;
        try {
            root.delete ();
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
    
    // actionToShortcuts Map (GlobalAction > Set (String (shortcut))
    @Override
    public void saveKeymap (String profile, Map<ShortcutAction, Set<String>> actionToShortcuts) {
        // discard our cached copy first
        keymaps.remove(profile);
        keymapDefaults.remove(profile);
        // 1) get / create Keymaps/Profile folder
        DataFolder defaultFolder = getRootFolder(SHORTCUTS_FOLDER, null);
        DataFolder folder = getRootFolder (KEYMAPS_FOLDER, profile);
        if (folder == null) {
            folder = getRootFolder (KEYMAPS_FOLDER, null);
            try {
                folder = DataFolder.create (folder, profile);
            } catch (IOException ex) {
                ErrorManager.getDefault ().notify (ex);
                return;
            }
        }
        saveKeymap (defaultFolder, folder, actionToShortcuts);
    }
    
    private void saveKeymap (DataFolder defaultMap, DataFolder folder, Map<ShortcutAction, Set<String>> actionToShortcuts) {
        LOG.log(Level.FINEST, "Saving keymap to: {0}", folder.getPrimaryFile().getPath());
        // hack: initialize the actions map first
  	getActions();
        // 2) convert to: Map (String (shortcut AC-C X) > GlobalAction)
        Map<String, ShortcutAction> shortcutToAction = shortcutToAction (actionToShortcuts);
        
        Set<String> definedShortcuts = new HashSet<String>(shortcutToAction.keySet());
        
        // 3) delete obsolete DataObjects
        FileObject targetDir = folder.getPrimaryFile();

        Enumeration en = folder.children ();
        while (en.hasMoreElements ()) {
            DataObject dataObject = (DataObject) en.nextElement ();
            if (dataObject.getPrimaryFile().getExt().equals(EXT_REMOVED)) {
                continue;
            }
            GlobalAction a1 = (GlobalAction) shortcutToAction.get (dataObject.getName ());
            if (a1 != null) {
                GlobalAction action = createAction (dataObject, null, dataObject.getPrimaryFile().getName(), false);
                if (action == null) {
                    LOG.log(Level.FINEST, "Broken action shortcut will be removed: {0}, will replace by {1}", new Object[] { dataObject.getName(), a1.getId() });
                } else if (action.equals (a1)) {
                    // shortcut already saved
                    LOG.log(Level.FINEST, "Found same binding: {0} -> {1}", new Object[] { dataObject.getName(), action.getId()});
                    shortcutToAction.remove (dataObject.getName ());
                    continue;
                }
            }
            // obsolete shortcut. 
            try {
                LOG.log(Level.FINEST, "Removing obsolete binding: {0}", dataObject.getName());
                dataObject.delete ();
            } catch (IOException ex) {
                ex.printStackTrace ();
            }
        }
        
        Set<String> defaultNames = new HashSet<String>();
        // 4) add new shortcuts
        en = defaultMap.children();
        while (en.hasMoreElements()) {
            DataObject dataObject = (DataObject)en.nextElement();
            GlobalAction ga = (GlobalAction)shortcutToAction.get(dataObject.getName());
            if (ga == null) {
                continue;
            }
            GlobalAction action = createAction(dataObject, null, dataObject.getPrimaryFile().getName(), false);
            if (ga.equals(action)) {
                LOG.log(Level.FINEST, "Leaving default shortcut: {0}", dataObject.getName());
                defaultNames.add(dataObject.getName());
            }
        }
        
        Iterator it = shortcutToAction.keySet ().iterator ();
        while (it.hasNext ()) {
            String shortcut = (String) it.next ();
            // check whether the DO does not already exist:
            GlobalAction action = (GlobalAction) shortcutToAction.get (shortcut);
            DataObject dataObject = actionToDataObject.get (action);
            if (dataObject == null) {
                 if (System.getProperty ("org.netbeans.optionsDialog") != null)
                     System.out.println ("No original DataObject specified! Not possible to create shadow1. " + action);
                 continue;
            }
            FileObject f = targetDir.getFileObject(shortcut, EXT_REMOVED);
            try {
                if (f != null) {
                    f.delete();
                }
                if (defaultNames.contains(shortcut)) {
                    continue;
                }
                DataShadow.create (folder, shortcut, dataObject);
                // remove the '.remove' file, if it exists:
            } catch (IOException ex) {
                ex.printStackTrace ();
                continue;
            }
        }

        // 5, mask out DataObjects from the global keymap, which are NOT present in this profile:
        if (defaultMap != null) {
            en = defaultMap.children();
            while (en.hasMoreElements()) {
                DataObject dataObject = (DataObject) en.nextElement ();
                if (definedShortcuts.contains(dataObject.getName())) {
                    continue;
                }
                try {
                    FileObject pf = dataObject.getPrimaryFile();
                    // If the shortcut is ALSO defined in 'parent' folder,
                    // we cannot just 'delete' it, but also mask the parent by adding 'removed' file.
                    if (targetDir.getFileObject(pf.getName(), EXT_REMOVED) == null) {
                        LOG.log(Level.FINEST, "Masking out binding: {0}", pf.getName());
                        folder.getPrimaryFile().createData(pf.getName(), EXT_REMOVED);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace ();
                }
            }
        }
        
    }    
    
    private static DataFolder getExistingProfile(String profile) {
        FileObject root = FileUtil.getConfigRoot ();
        FileObject fo1 = root.getFileObject(KEYMAPS_FOLDER);
        if (fo1 == null) {
            return null;
        }
        FileObject fo2 = fo1.getFileObject(profile);
        if (fo2 == null) {
            return null;
        }
        return DataFolder.findFolder(fo2);
    }

    private static DataFolder getRootFolder (String name1, String name2) {
        FileObject root = FileUtil.getConfigRoot ();
        FileObject fo1 = root.getFileObject (name1);
        try {
            if (fo1 == null) fo1 = root.createFolder (name1);
            if (fo1 == null) return null;
            if (name2 == null) return DataFolder.findFolder (fo1);
            FileObject fo2 = fo1.getFileObject (name2);
            if (fo2 == null) fo2 = fo1.createFolder (name2);
            if (fo2 == null) return null;
            return DataFolder.findFolder (fo2);
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
            return null;
        }
    }
    
    private GlobalAction createActionWithLookup(DataObject dataObject, String prefix, String name, boolean ignoreUserRemoves) {
        GlobalAction a = createAction(dataObject, prefix, name, ignoreUserRemoves);
        if (a == null) {
            return null;
        }
        GlobalAction b = actions.get(a);
        return b == null ? a : b;
    }

    /**
     * Returns instance of GlobalAction encapsulating action, or null.
     */
    private GlobalAction createAction (DataObject dataObject, String prefix, String name, boolean ignoreUserRemoves) {
        InstanceCookie ic = dataObject.getCookie(InstanceCookie.class);
        // handle any non-IC file as instruction to remove the action
        FileObject pf = dataObject.getPrimaryFile();
        if (ignoreUserRemoves && pf.canRevert()) {
            return null;
        }
        if (ic == null) {
            if (!EXT_REMOVED.equals(pf.getExt())) {
                LOG.log(Level.WARNING, "Invalid shortcut: {0}", dataObject);
                return null;
            }
            // ignore the 'remove' file, if there's a shadow (= real action) present
            if (FileUtil.findBrother(pf, "shadow") != null) {
                // handle redefinition + removal: ignore the removal.
                return null;
            }
            return REMOVED;
        }
        try {
            Object action = ic.instanceCreate ();
            if (action == null) return null;
            if (!(action instanceof Action)) return null;
            return createAction((Action) action, prefix, name);
        } catch (Exception ex) {
            ex.printStackTrace ();
            return null;
        }
    }

    // hack: hardcoded OpenIDE impl class name + field
    private static final String OPENIDE_DELEGATE_ACTION = "org.openide.awt.GeneralAction$DelegateAction"; // NOI18N
    private static volatile Field KEY_FIELD;

    /**
     * Hack, which allows to somehow extract actionId from OpenIDE actions. Public API
     * does not exist for this.
     * 
     * @param a
     * @param prefix
     * @param name
     * @return 
     */
    private static GlobalAction createAction(Action a, String prefix, String name) {
        String id = name;
        
        try {
            if (a.getClass().getName().equals(OPENIDE_DELEGATE_ACTION)) {
                if (KEY_FIELD == null) {
                    Class c = a.getClass();
                    // #220683: the field must be first made accessible before assingment to global variable.
                    Field f = c.getSuperclass().getDeclaredField("key"); // NOI18N
                    f.setAccessible(true);
                    KEY_FIELD = f;
                }
                String key = (String)KEY_FIELD.get(a);
                if (key != null) {
                    id = key;
                }
            }
        } catch (NoSuchFieldException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return new GlobalAction(a, prefix, id);
    }
    
    /**
     * converts: actionToShortcuts: Map (ShortcutAction > Set (String (shortcut AC-C X)))
     * to: Map (String (shortcut AC-C X) > GlobalAction).
     * removes all non GlobalAction actions.
     */
    static Map<String, ShortcutAction> shortcutToAction (Map<ShortcutAction, Set<String>> actionToShortcuts) {
        Map<String, ShortcutAction> shortcutToAction = new HashMap<String, ShortcutAction> ();
        for (Map.Entry<ShortcutAction, Set<String>> entry: actionToShortcuts.entrySet()) {
            ShortcutAction action = entry.getKey();
            Set<String> shortcuts = entry.getValue();
            action = action != null ? action.getKeymapManagerInstance(LAYERS_BRIDGE) : null; // #161164
            if (!(action instanceof GlobalAction)) continue;
            for (String multiShortcut: shortcuts) {
                shortcutToAction.put (multiShortcut, action);
            }
        }
        return shortcutToAction;
    }
    
    public void refreshActions() {
        refreshKeymapNames();
    }
    
    /** The name of the default profile. */
    private static final String DEFAULT_PROFILE = "NetBeans"; //NOI18N
    private static final String FATTR_CURRENT_KEYMAP_PROFILE = "currentKeymap";      // NOI18N

    private String cachedProfile;
    
    public String getCurrentProfile() {
        if (cachedProfile == null) {
            FileObject fo = FileUtil.getConfigFile (KEYMAPS_FOLDER);
            if (fo != null) {
                Object o = fo.getAttribute (FATTR_CURRENT_KEYMAP_PROFILE);
                if (o instanceof String) {
                    cachedProfile = (String) o;
                }
            }
            if (cachedProfile == null) {
                cachedProfile = DEFAULT_PROFILE;
            }
        }
        return cachedProfile;
    }

    public void setCurrentProfile(String profileName) {
        // cached mainly because of tests; the physical storage is implemented by EditorsBridge.
        this.cachedProfile = profileName;
        // Persist the change
        try {
            FileObject fo = FileUtil.getConfigFile (KEYMAPS_FOLDER);
            if (fo == null) {
                fo = FileUtil.getConfigRoot ().createFolder (KEYMAPS_FOLDER);
            }
            fo.setAttribute (FATTR_CURRENT_KEYMAP_PROFILE, profileName);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Can't persist change in current keymap profile.", ex); //NOI18N
        }
    }

    /**
     * Custom profile is present if:
     * a/ the profile's folder can be reverted (=> was materialized on writable layer)
     * b/ there's no "reveal" entry for it in its parent => was not present
     * @param profileName
     * @return C
     */
    public boolean isCustomProfile(String profileName) {
        DataFolder profileFolder = getExistingProfile(profileName);
        if (profileFolder == null) {
            return true;
        }
        FileObject f = profileFolder.getPrimaryFile();
        if (!f.canRevert()) {
            return false;
        }
        FileObject parentF = profileFolder.getPrimaryFile().getParent();
        if (parentF == null) {
            // very very unlikely
            return true;
        }
        Collection<FileObject> col = (Collection<FileObject>)parentF.getAttribute("revealEntries");
        if (col == null) {
            return true;
        }
        for (FileObject f2 : col) {
            if (f2.getNameExt().equals(profileName)) {
                return false;
            }
        }
        return true;
    }
    
    /* package */ static String getOrigActionClass(ShortcutAction sa) {
        if (!(sa instanceof GlobalAction)) {
            return null;
        }
        GlobalAction ga = (GlobalAction)sa;
        return ga.action == null ? null : ga.action.getClass().getName();
    }
    
    private static class GlobalAction implements ShortcutAction {
        private Action action;
        String name;
        private String id;
        
        /**
         * 
         * @param a the action to be delegated to
         * @param prefix prefix for the name, e.g. category where the action is defined
         * @param n name / id of the action, usually a declaring filename
         */
        private GlobalAction (Action a, String prefix, String n) {
            action = a;
            /*
            if (prefix != null) {
                this.id = prefix + "/" + n; // NOI18N
            } else {
                this.id = n;
            }
            */
            this.id = n;
        }
        
        public String getDisplayName () {
            if (name == null) {
                try {
                    name = (String) action.getValue (Action.NAME);
                } catch (MissingResourceException ex) {
                    // this is a common plugin error, which would be otherwise attributed to 
                    // actions component. Print a warning and blame the real originator of the bug
                    LOG.log(Level.WARNING, "Missing resources for action {0}, class {1}: {2}", new Object[] {
                        id,
                        action.getClass().getName(),
                        ex.getLocalizedMessage()
                    });
                    // name remains null, action will be filtered out from the display.
                }
                if (name == null) {
                    name = ""; // #185619: not intended for presentation in this dialog
                }
                name = name.replace ("&", "").trim (); // NOI18N
            }
            return name;
        }
        
        public String getId () {
            if (id == null) 
                id = action.getClass ().getName ();
            return id;
        }
        
        public String getDelegatingActionId () {
            return null;
        }
        
        @Override
        public boolean equals (Object o) {
            if (!(o instanceof GlobalAction)) return false;
            return ((GlobalAction) o).action == action || ((GlobalAction) o).action.equals (action);
        }
        
        @Override
        public int hashCode () {
            return action == null ? 111 : action.hashCode ();
        }
        
        @Override
        public String toString () {
            return "GlobalAction[" + getDisplayName()+ ":" + id + "]";
        }
    
        public ShortcutAction getKeymapManagerInstance(String keymapManagerName) {
            if (LAYERS_BRIDGE.equals(keymapManagerName)) {
                return this;
            }
            return null;
        }
    }
}
