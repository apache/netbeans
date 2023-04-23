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

package org.netbeans.modules.editor.lib2.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.spi.editor.AbstractEditorAction;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;


/**
 * Various utility methods for declarative editor action registrations.
 */
public final class EditorActionUtilities {

    // -J-Dorg.netbeans.modules.editor.lib2.actions.EditorActionUtilities.level=FINEST
    private static final Logger LOG = Logger.getLogger(EditorActionUtilities.class.getName());

    private static Map<String,Map<String,KeyStroke>> mimeType2actionName2KeyStroke;

    private static Map<String,Boolean> mimeType2ListenerPresent = new HashMap<String, Boolean>();

    private static SearchableEditorKit globalActionsKit;

    private static final Map<EditorKit,SearchableEditorKit> kit2searchable = new WeakHashMap<EditorKit,SearchableEditorKit>();
    
    static final int LARGE_ICON_SIZE = 24;
    static final String LARGE_ICON_SIZE_STRING = "24"; // NOI18N

    private EditorActionUtilities() {
        // No instances
    }
    
    public static void resetCaretMagicPosition(JTextComponent component) {
        Caret caret;
        if (component != null && (caret = component.getCaret()) != null) {
            caret.setMagicCaretPosition(null);
        }
    }

    public static boolean isUseLargeIcon(JComponent c) {
        Object prefIconSize = c.getClientProperty("PreferredIconSize"); //NOI18N
        return (prefIconSize instanceof Integer) && (((Integer) prefIconSize).intValue() >= LARGE_ICON_SIZE);
    }
    
    public static Icon getIcon(Action a, boolean large) {
        return large ? getLargeIcon(a) : getSmallIcon(a);
    }

    public static Icon getSmallIcon(Action a) {
        return (Icon) a.getValue(Action.SMALL_ICON);
    }

    public static Icon getLargeIcon(Action a) {
        return (Icon) a.getValue(Action.LARGE_ICON_KEY);
    }

    public static Icon createSmallIcon(Action a) {
        String iconBase = (String) a.getValue(AbstractEditorAction.ICON_RESOURCE_KEY);
        if (iconBase != null) {
            return ImageUtilities.loadImageIcon(iconBase, true);
        }
        return null;
    }

    public static Icon createLargeIcon(Action a) {
        String iconBase = (String) a.getValue(AbstractEditorAction.ICON_RESOURCE_KEY);
        if (iconBase != null) {
            iconBase += LARGE_ICON_SIZE_STRING;
            return ImageUtilities.loadImageIcon(iconBase, true);
        }
        return null;
    }

    static void updateButtonIcons(AbstractButton button, Icon icon, boolean useLargeIcon, String iconResource) {
        button.setIcon(icon);

        if (iconResource != null) {
            String base = iconResource;
            String suffix = "";
            int dotIndex;
            if ((dotIndex = iconResource.lastIndexOf('.')) >= 0) {
                suffix = iconResource.substring(dotIndex);
                base = iconResource.substring(0, dotIndex);
            }
            if (useLargeIcon) {
                base += LARGE_ICON_SIZE_STRING;
            }

            Icon pressedIcon = ImageUtilities.loadImageIcon(base + "_pressed" + suffix, true); // NOI18N
            if (pressedIcon != null) {
                button.setPressedIcon(pressedIcon);
            }
            Icon rolloverIcon = ImageUtilities.loadImageIcon(base + "_rollover" + suffix, true); // NOI18N
            if (rolloverIcon != null) {
                button.setRolloverIcon(rolloverIcon);
            }
            Icon disabledIcon = ImageUtilities.loadImageIcon(base + "_disabled" + suffix, true); // NOI18N
            if (disabledIcon != null) {
                button.setDisabledIcon(disabledIcon);
            } else { // Make disabled icon from regular icon
                button.setDisabledIcon(ImageUtilities.createDisabledIcon(icon));
            }
            Icon selectedIcon = ImageUtilities.loadImageIcon(base + "_selected" + suffix, true); // NOI18N
            if (selectedIcon != null) {
                button.setSelectedIcon(selectedIcon);
            }
            Icon rolloverSelectedIcon = ImageUtilities.loadImageIcon(base + "_rolloverSelected" + suffix, true); // NOI18N
            if (rolloverSelectedIcon != null) {
                button.setRolloverSelectedIcon(rolloverSelectedIcon);
            }
            Icon disabledSelectedIcon = ImageUtilities.loadImageIcon(base + "_disabledSelected" + suffix, true); // NOI18N
            if (disabledSelectedIcon != null) {
                button.setDisabledSelectedIcon(disabledSelectedIcon);
            }
        }
    }

    static String insertBeforeSuffix(String path, String toInsert) {
        int dotIndex;
        if ((dotIndex = path.lastIndexOf('.')) >= 0) {
            path = path.substring(0, dotIndex) + toInsert + path.substring(dotIndex);
        } else {
            path += toInsert;
        }
        return path;
    }

    public static String getKeyMnemonic(MultiKeyBinding binding) {
        return getKeyMnemonic(binding.getKeyStrokeList());
    }
    
    /**
     * Get mnemonic text for a keystroke.
     *
     * @param key a keystroke
     * @return mnemonic of the keystroke.
     */
    public static String getKeyMnemonic(KeyStroke key) {
        return Actions.keyStrokeToString(key);
    }

    public static String getKeyMnemonic(List<KeyStroke> keys) {
        StringBuilder sb = new StringBuilder(40);
        for (KeyStroke key : keys) {
            if (sb.length() > 0) {
                sb.append(' '); //NOI18N
            }
            sb.append(Actions.keyStrokeToString(key));
        }
        return sb.toString();
    }

    public static SearchableEditorKit getGlobalActionsKit() {
        synchronized (EditorActionUtilities.class) {
            if (globalActionsKit == null) {
                globalActionsKit = new SearchableEditorKitImpl("");
            }
            return globalActionsKit;
        }
    }
    
    public static EditorKit getKit(String mimeType) {
        Lookup.Result<EditorKit> result = MimeLookup.getLookup(mimeType).lookupResult(EditorKit.class);
        Iterator<? extends EditorKit> instancesIterator = result.allInstances().iterator();
        EditorKit kit = instancesIterator.hasNext() ? instancesIterator.next() : null;
        return kit;
    }

    /**
     * Register an instance of searchable kit explicitly for the given kit.
     * Used by BaseKit for explicit registration.
     * @param kit non-null kit.
     * @param searchableKit non-null searchable kit.
     */
    public static void registerSearchableKit(EditorKit kit, SearchableEditorKit searchableKit) {
        synchronized (kit2searchable) {
            kit2searchable.put(kit, searchableKit);
        }
    }

    /**
     * Get an editor action in a constant time (wrap a kit with a SearchableEditorKit if necessary).
     *
     * @param kit non-null kit.
     * @param actionName non-null action name.
     * @return action's instance or null.
     */
    public static Action getAction(EditorKit kit, String actionName) {
        return getSearchableKit(kit).getAction(actionName);
    }

    /**
     * Get searchable editor kit for the given kit.
     * @param kit non-null kit.
     * @return non-null searchable kit.
     */
    public static SearchableEditorKit getSearchableKit(EditorKit kit) {
        SearchableEditorKit searchableKit;
        if (kit instanceof SearchableEditorKit) {
            searchableKit = ((SearchableEditorKit)kit);
        } else {
            synchronized (kit2searchable) {
                searchableKit = kit2searchable.get(kit);
                if (searchableKit == null) {
                    searchableKit = new DefaultSearchableKit(kit);
                    registerSearchableKit(kit, searchableKit);
                }
            }
        }
        return searchableKit;
    }

    public static Lookup.Result<Action> createActionsLookupResult(String mimeType) {
        if (!MimePath.validate(mimeType)) {
            throw new IllegalArgumentException("Ïnvalid mimeType=\"" + mimeType + "\"");
        }
        Lookup lookup = Lookups.forPath(getPath(mimeType, "Actions"));
        return lookup.lookupResult(Action.class);
    }

    private static String getPath(String mimeType, String subFolder) {
        StringBuilder path = new StringBuilder(50);
        path.append("Editors/");
        if (mimeType.length() > 0) {
            path.append('/').append(mimeType);
        }
        if (subFolder.length() > 0) {
            path.append('/').append(subFolder);
        }
        return path.toString();
    }

    public static Preferences getPreferences(Map<String,?> attrs) {
        String mimeType = (String) attrs.get(AbstractEditorAction.MIME_TYPE_KEY);
        if (mimeType != null) {
            mimeType = "";
        }
        Lookup mimeLookup = MimeLookup.getLookup(mimeType);
        return (mimeLookup != null) ? mimeLookup.lookup(Preferences.class) : null;
    }

    public static Preferences getGlobalPreferences() {
        Lookup globalMimeLookup = MimeLookup.getLookup(MimePath.EMPTY);
        return (globalMimeLookup != null) ? globalMimeLookup.lookup(Preferences.class) : null;
    }

    /**
     * Get single-key accelerator for a given declared action.
     * Only a single-key accelerators are supported.
     */
    public static KeyStroke getAccelerator(FileObject fo) {
        if (fo == null) {
            throw new IllegalArgumentException("Must be called with non-null fileObject"); // NOI18N
        }
        boolean fineLoggable = LOG.isLoggable(Level.FINE);
        String path = fo.getParent().getPath();
        String actionName = (String) fo.getAttribute(Action.NAME);
        KeyStroke ks = null;
        if (path.startsWith("Editors/")) {
            path = path.substring(7); // Leave ending '/' to support "Editors/Actions"
            if (path.endsWith("/Actions")) {
                path = path.substring(0, path.length() - 8);
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                String mimeType = path;
                if (!MimePath.validate(mimeType)) {
                    LOG.info("Invalid mime-type='" + mimeType + "' of action's fileObject=" + fo); // NOI18N
                }
                ks = getAccelerator(mimeType, actionName);
            } else if (fineLoggable) {
                LOG.fine("No \"/Actions\" at end of mime-type='" + path +
                    "' of action's fileObject=" + fo); // NOI18N
            }
        } else if (fineLoggable) {
            LOG.fine("No \"Editors/\" at begining of mime-type='" + path + // NOI18N
                    "' of action's fileObject=" + fo); // NOI18N
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Accelerator for action \"" + actionName + "\" is " + ks);
        }
        return ks;
    }

    /**
     * Get single-key accelerator for a given declared action.
     * <br>
     * Unfortunately currently there's no easy way to display multi-keybinding in menu-item
     * (there's just JMenuItem.setAccelerator() and its impl is L&F-based)
     * so just display single-keystroke accelerators.
     */
    public static KeyStroke getAccelerator(String mimeType, String actionName) {
        KeyStroke ks = null;
        if (actionName != null) {
            synchronized (EditorActionUtilities.class) {
                if (mimeType2actionName2KeyStroke == null) {
                    mimeType2actionName2KeyStroke = new HashMap<String,Map<String,KeyStroke>>();
                }
                Map<String,KeyStroke> actionName2KeyStrokeList = mimeType2actionName2KeyStroke.get(mimeType);
                if (actionName2KeyStrokeList == null) {
                    actionName2KeyStrokeList = new HashMap<String,KeyStroke>();
                    Lookup.Result<KeyBindingSettings> result = MimeLookup.getLookup(mimeType).lookupResult(
                            KeyBindingSettings.class);
                    Collection<? extends KeyBindingSettings> instances = result.allInstances();
                    if (!instances.isEmpty()) {
                        KeyBindingSettings kbs = instances.iterator().next();
                        for (MultiKeyBinding kb : kbs.getKeyBindings()) {
                            if (!actionName2KeyStrokeList.containsKey(kb.getActionName())
                                && kb.getKeyStrokeCount() == 1)
                            {
                                actionName2KeyStrokeList.put(kb.getActionName(), kb.getKeyStroke(0));
                            }
                        }
                    }
                    mimeType2actionName2KeyStroke.put(mimeType, actionName2KeyStrokeList);
                    // Ensure listening on changes in keybinding settings
                    if (!Boolean.TRUE.equals(mimeType2ListenerPresent.get(mimeType))) {
                        mimeType2ListenerPresent.put(mimeType, true);
                        result.addLookupListener(KeyBindingSettingsListener.INSTANCE);
                    }
                }
                ks = actionName2KeyStrokeList.get(actionName);
            }
        }
        return ks;
    }
    
    /**
     * Create an instance of an empty action which may be used as a marker action
     * in various situations.
     */
    public static Action createEmptyAction() {
        return new EmptyAction();
    }
    
    public static String getGlobalActionDisplayName(Map<String,?> attrs) {
        return (String) getGlobalActionProperty(attrs, AbstractEditorAction.DISPLAY_NAME_KEY);
    }

    public static String getGlobalActionShortDescription(Map<String,?> attrs) {
        return (String) getGlobalActionProperty(attrs, Action.SHORT_DESCRIPTION);
    }

    public static String getGlobalActionIconResource(Map<String,?> attrs) {
        return (String) getGlobalActionProperty(attrs, AbstractEditorAction.ICON_RESOURCE_KEY);
    }

    public static String getGlobalActionMenuText(Map<String,?> attrs) {
        return (String) getGlobalActionProperty(attrs, AbstractEditorAction.MENU_TEXT_KEY);
    }

    public static String getGlobalActionPopupText(Map<String,?> attrs) {
        return (String) getGlobalActionProperty(attrs, AbstractEditorAction.POPUP_TEXT_KEY);
    }

    public static Object getGlobalActionProperty(Map<String,?> attrs, String key) {
        Object value = null;
        String actionName = (String) attrs.get(Action.NAME);
        SearchableEditorKit globalKit = getGlobalActionsKit();
        if (globalKit != null) {
            Action a = globalKit.getAction(actionName);
            if (a != null) {
                value = a.getValue(key);
            }
        }
        return value;
    }

    private static final class KeyBindingSettingsListener implements LookupListener {
        
        static final KeyBindingSettingsListener INSTANCE = new KeyBindingSettingsListener();

        private KeyBindingSettingsListener() {
        }

        public void resultChanged(LookupEvent ev) {
            synchronized (EditorActionUtilities.class) {
                mimeType2actionName2KeyStroke = null;
                LOG.fine("mimeType2actionName2KeyStroke cleared."); // NOI18N
            }
        }

    }

    private static final class DefaultSearchableKit implements SearchableEditorKit {
        
        private final Map<String,Reference<Action>> name2actionRef = new WeakHashMap<String,Reference<Action>>();

        DefaultSearchableKit(EditorKit kit) {
            for (Action action : kit.getActions()) {
                if (action != null) {
                    name2actionRef.put((String)action.getValue(Action.NAME), new WeakReference<Action>(action));
                }
            }
        }

        @Override
        public Action getAction(String actionName) {
            Reference<Action> actionRef = name2actionRef.get(actionName);
            return (actionRef != null) ? actionRef.get() : null;
        }

        @Override
        public void addActionsChangeListener(ChangeListener listener) {
        }

        @Override
        public void removeActionsChangeListener(ChangeListener listener) {
        }

    }

    /**
     * @see #createEmptyAction()
     */
    private static final class EmptyAction extends TextAction {
        
        EmptyAction() {
            super("empty-action"); // NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }

    }

}
