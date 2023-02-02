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
package org.netbeans.core.output2.ui;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.netbeans.core.output2.NbIOProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jhavlin
 */
@ServiceProvider(service = KeymapManager.class)
public class OutputKeymapManager extends KeymapManager {

    private static final Logger LOG = Logger.getLogger(
            OutputKeymapManager.class.getName());
    private static final String CATEGORY_NAME = NbBundle.getMessage(
            NbIOProvider.class, "OpenIDE-Module-Name");                 //NOI18N
    /**
     * ID of actions in keymap settings panel.
     */
    public static final String CLEAR_ACTION_ID =
            "output-window-clear";                                      //NOI18N
    public static final String FILTER_ACTION_ID =
            "output-window-filter";                                     //NOI18N
    public static final String LARGER_FONT_ACTION_ID =
            "output-window-larger-font";                                //NOI18N
    public static final String SMALLER_FONT_ACTION_ID =
            "output-window-smaller-font";                               //NOI18N
    public static final String CLOSE_ACTION_ID =
            "output-window-close";                                      //NOI18N
    public static final String OUTPUT_SETTINGS_ACTION_ID =
            "output-window-settings";                                   //NOI18N
    public static final String SAVE_AS_ACTION_ID =
            "output-window-save-as";                                    //NOI18N
    public static final String WRAP_ACTION_ID =
            "output-window-wrap";                                       //NOI18N
    /**
     * Constants for persistence.
     */
    public static final String STORAGE_DIR =
            "org-netbeans-core-output2/actions/";                       //NOI18N
    public static final String SHORTCUT_PREFIX = "sc";                  //NOI18N
    /**
     * Actions
     */
    private final OutWinShortCutAction wrap = new OutWinShortCutAction(
            WRAP_ACTION_ID, "ACTION_WRAP");                             //NOI18N
    private final OutWinShortCutAction clear = new OutWinShortCutAction(
            CLEAR_ACTION_ID, "ACTION_CLEAR");                           //NOI18N
    private final OutWinShortCutAction filter = new OutWinShortCutAction(
            FILTER_ACTION_ID, "ACTION_FILTER");                         //NOI18N
    private final OutWinShortCutAction largerFont = new OutWinShortCutAction(
            LARGER_FONT_ACTION_ID, "ACTION_LARGER_FONT");               //NOI18N
    private final OutWinShortCutAction smallerFont = new OutWinShortCutAction(
            SMALLER_FONT_ACTION_ID, "ACTION_SMALLER_FONT");             //NOI18N
    private final OutWinShortCutAction closeWindow = new OutWinShortCutAction(
            CLOSE_ACTION_ID, "ACTION_CLOSE");                           //NOI18N
    private final OutWinShortCutAction fontType = new OutWinShortCutAction(
            OUTPUT_SETTINGS_ACTION_ID, "ACTION_SETTINGS");              //NOI18N
    private final OutWinShortCutAction saveAs = new OutWinShortCutAction(
            SAVE_AS_ACTION_ID, "ACTION_SAVEAS");                        //NOI18N
    /**
     * Map of keymaps. Keys are profile names.
     */
    Map<String, Map<ShortcutAction, Set<String>>> keymaps =
            new HashMap<String, Map<ShortcutAction, Set<String>>>();
    /**
     * The default keymap. Used if keys for a profile are not set.
     */
    Map<ShortcutAction, Set<String>> defaultKeymap =
            new HashMap<ShortcutAction, Set<String>>();
    /**
     * List of all actions.
     */
    private final Set<OutWinShortCutAction> allActions =
            new HashSet<OutWinShortCutAction>();
    /**
     * Map of actions of categories. There is only one category in this case.
     */
    Map<String, Set<ShortcutAction>> actions =
            new HashMap<String, Set<ShortcutAction>>();

    public OutputKeymapManager() {
        super("OutputWindowKeymapManager");                             //NOI18N
        actions = new HashMap<String, Set<ShortcutAction>>();
        Collections.addAll(allActions, wrap, clear, filter, largerFont,
                smallerFont, closeWindow, fontType, saveAs);
        Set<ShortcutAction> set = new HashSet<ShortcutAction>();
        set.addAll(allActions);
        actions.put(CATEGORY_NAME, set);
        fillDefaultKeyMap();
        loadShortCuts();
    }

    private void fillDefaultKeyMap() {
        for (OutWinShortCutAction a : allActions) {
            String dflt = a.getDefaultShortcut();
            defaultKeymap.put(a, (dflt != null && !dflt.isEmpty())
                    ? Collections.singleton(dflt)
                    : Collections.<String>emptySet());
        }
    }

    @Override
    public Map<String, Set<ShortcutAction>> getActions() {
        return actions;
    }

    @Override
    public void refreshActions() {
    }

    @Override
    public Map<ShortcutAction, Set<String>> getKeymap(String profileName) {
        Map<ShortcutAction, Set<String>> km = keymaps.get(profileName);
        if (km == null) {
            km = new HashMap<ShortcutAction, Set<String>>(defaultKeymap);
            keymaps.put(profileName, km);
        }
        return km;
    }

    @Override
    public Map<ShortcutAction, Set<String>> getDefaultKeymap(
            String profileName) {
        return defaultKeymap;
    }

    @Override
    public void saveKeymap(String profileName,
            Map<ShortcutAction, Set<String>> actionToShortcuts) {

        Map<ShortcutAction, Set<String>> newShortcuts =
                new HashMap<ShortcutAction, Set<String>>();
        keymaps.put(profileName, newShortcuts);
        for (OutWinShortCutAction owsa : allActions) {
            Set<String> shortcuts = actionToShortcuts.get(owsa);
            if (shortcuts == null) {
                shortcuts = Collections.<String>emptySet();
            }
            newShortcuts.put(owsa, shortcuts);
        }
        storeShortCuts(profileName);
    }

    @Override
    public List<String> getProfiles() {
        return null;
    }

    @Override
    public String getCurrentProfile() {
        return null;
    }

    @Override
    public void setCurrentProfile(String profileName) {
    }

    @Override
    public void deleteProfile(String profileName) {
    }

    @Override
    public boolean isCustomProfile(String profileName) {
        return false;
    }

    private class OutWinShortCutAction implements ShortcutAction {

        private String id;
        private String bundleKey;
        private String displayName;
        private String defaultShortcut;

        public OutWinShortCutAction(String id, String bundleKey) {
            this.id = id;
            this.bundleKey = bundleKey;
            this.displayName = NbBundle.getMessage(
                    NbIOProvider.class, bundleKey);
            String nbKeysBundleKey = Utilities.isMac()
                    ? bundleKey + ".accel.mac" //NOI18N
                    : bundleKey + ".accel";                             //NOI18N
            String nbKeys = NbBundle.getMessage(NbIOProvider.class,
                    nbKeysBundleKey);
            this.defaultShortcut = nbKeys;
        }

        @Override
        public String getId() {
            return id;
        }

        public String getBundleKey() {
            return bundleKey;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        public String getDefaultShortcut() {
            return defaultShortcut;
        }

        @Override
        public String getDelegatingActionId() {
            return null;
        }

        @Override
        public ShortcutAction getKeymapManagerInstance(
                String keymapManagerName) {
            return null;
        }
    }

    private void storeShortCuts(String profileName) {
        FileObject root = FileUtil.getConfigRoot();
        try {
            FileObject actionsDir = FileUtil.createFolder(
                    root, STORAGE_DIR + profileName);
            for (OutWinShortCutAction a : allActions) {
                FileObject data = actionsDir.getFileObject(a.getId());
                if (data == null) {
                    data = actionsDir.createData(a.getId());
                } else if (data.isFolder()) {
                    throw new IOException(data + " is a folder.");      //NOI18N
                }
                Enumeration<String> atts = data.getAttributes();
                while (atts.hasMoreElements()) {
                    String attName = atts.nextElement();
                    data.setAttribute(attName, null);
                }
                int index = 1;
                if (keymaps.get(profileName).get(a) == null) {
                    continue;
                }
                for (String shortCut : keymaps.get(profileName).get(a)) {
                    data.setAttribute(SHORTCUT_PREFIX + index++, shortCut);
                }
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Cannot create folder", e);          //NOI18N
        }
    }

    private void loadShortCuts() {
        FileObject root = FileUtil.getConfigRoot();
        FileObject actionsDir = root.getFileObject(STORAGE_DIR);
        if (actionsDir == null) {
            return;
        }
        for (FileObject profileDir : actionsDir.getChildren()) {
            if (!profileDir.isFolder()) {
                continue;
            }
            Map<ShortcutAction, Set<String>> keymap =
                    new HashMap<ShortcutAction, Set<String>>();
            keymaps.put(profileDir.getName(), keymap);
            for (OutWinShortCutAction a : allActions) {
                FileObject actionFile = profileDir.getFileObject(a.getId());
                if (actionFile == null || !actionFile.isData()) {
                    keymap.put(a, Collections.<String>emptySet());
                    continue;
                }
                Enumeration<String> atts = actionFile.getAttributes();
                Set<String> strokes = new HashSet<>();
                while (atts.hasMoreElements()) {
                    String att = atts.nextElement();
                    if (att.startsWith(SHORTCUT_PREFIX)) {
                        strokes.add((String) actionFile.getAttribute(att));
                    }
                }
                keymap.put(a, strokes);
            }
        }
    }
}
