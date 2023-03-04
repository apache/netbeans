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

package org.netbeans.modules.editor;

import java.awt.Component;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.ToolBarUI;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.impl.ToolbarActionsProvider;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib2.actions.EditorActionUtilities;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.ToolbarWithOverflow;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Editor toolbar component.
 * <br>
 * Toolbar contents are obtained by merging of
 * Editors/mime-type/Toolbars/Default
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

/* package */ final class NbEditorToolBar extends ToolbarWithOverflow {

    // -J-Dorg.netbeans.modules.editor.NbEditorToolBar.level=FINE
    private static final Logger LOG = Logger.getLogger(NbEditorToolBar.class.getName());
    
    /** Flag for testing the sorting support by debugging messages. */
    private static final boolean debugSort
        = Boolean.getBoolean("netbeans.debug.editor.toolbar.sort"); // NOI18N

    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);
    
    // An empty lookup. Can't use Lookup.EMPTY as this can be returned by clients.
    private static final Lookup NO_ACTION_CONTEXT = Lookups.fixed();
    
    private FileChangeListener moduleRegListener;

    /** Shared mouse listener used for setting the border painting property
     * of the toolbar buttons and for invoking the popup menu.
     */
    private static final MouseListener sharedMouseListener
        = new org.openide.awt.MouseUtils.PopupMouseAdapter() {
            public @Override void mouseEntered(MouseEvent evt) {
                Object src = evt.getSource();
                
                if (src instanceof AbstractButton) {
                    AbstractButton button = (AbstractButton)evt.getSource();
                    if (button.isEnabled()) {
                        button.setContentAreaFilled(true);
                        button.setBorderPainted(true);
                    }
                }
            }
            
            public @Override void mouseExited(MouseEvent evt) {
                Object src = evt.getSource();
                if (src instanceof AbstractButton)
                {
                    AbstractButton button = (AbstractButton)evt.getSource();
                    removeButtonContentAreaAndBorder(button);
                }
            }
            
            protected void showPopup(MouseEvent evt) {
            }
        };
       

    
    /** Text component for which the toolbar gets constructed. */
    private Reference componentRef;
    
    private boolean presentersAdded;

    private boolean addListener = true;
    
    private static final String NOOP_ACTION_KEY = "noop-action-key"; //NOI18N
    private static final Action NOOP_ACTION = new NoOpAction();
    
    private final Lookup.Result<KeyBindingSettings> lookupResult;
    private final LookupListener keybindingsTracker = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            refreshToolbarButtons();
        }
    };
    
    private final Preferences preferences;
    private final PreferenceChangeListener prefsTracker = new PreferenceChangeListener() {
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();
            if (settingName == null || SimpleValueNames.TOOLBAR_VISIBLE_PROP.equals(settingName)) {
                refreshToolbarButtons();
            }
        }
    };
    
    public NbEditorToolBar(JTextComponent component) {
        this.componentRef = new WeakReference(component);
        
        setFloatable(false);
        //mkleint - instead of here, assign the border in CloneableEditor and MultiView module.
//        // special border installed by core or no border if not available
//        Border b = (Border)UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
//        setBorder(b);
        addMouseListener(sharedMouseListener);

        installModulesInstallationListener();
        installNoOpActionMappings();
        
        lookupResult = MimeLookup.getLookup(DocumentUtilities.getMimeType(component)).lookupResult(KeyBindingSettings.class);
        lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, keybindingsTracker, lookupResult));
        
        String mimeType = DocumentUtilities.getMimeType(component);
        preferences = MimeLookup.getLookup(mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType)).lookup(Preferences.class);
        preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefsTracker, preferences));
        
        refreshToolbarButtons();
        setBorderPainted(true);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // In GTK L&F the border of the toolbar looks raised. It does not help to set null border
        // nor setting EmptyBorder helps. Curent solution is to set one-pixel LineBorder
        // which overwrites the "raising line".
        setBorder(new LineBorder(getBackground(), 1));
    }

    // issue #69642
    private void installNoOpActionMappings(){
        InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        // cut
        KeyStroke[] keys = findEditorKeys(DefaultEditorKit.cutAction, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
        for (int i = 0; i < keys.length; i++) {
            im.put(keys[i], NOOP_ACTION_KEY);
        }
        // copy
        keys = findEditorKeys(DefaultEditorKit.copyAction, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
        for (int i = 0; i < keys.length; i++) {
            im.put(keys[i], NOOP_ACTION_KEY);
        }
        // delete
        keys = findEditorKeys(DefaultEditorKit.deleteNextCharAction, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)); //NOI18N
        for (int i = 0; i < keys.length; i++) {
            im.put(keys[i], NOOP_ACTION_KEY);
        }
        // paste
        keys = findEditorKeys(DefaultEditorKit.pasteAction, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
        for (int i = 0; i < keys.length; i++) {
            im.put(keys[i], NOOP_ACTION_KEY);
        }
        
        getActionMap().put(NOOP_ACTION_KEY, NOOP_ACTION);
    }
    
    /** See issue #57773 for details. Toolbar should be updated with possible changes after
       module install/uninstall */
    private void installModulesInstallationListener(){
        moduleRegListener = new FileChangeAdapter() {
            public @Override void fileChanged(FileEvent fe) {
                //some module installed/uninstalled. Refresh toolbar content
                Runnable r = new Runnable() {
                    public void run() {
                        if (isToolbarVisible()) {
                            checkPresentersRemoved();
                            checkPresentersAdded();                                
                        }
                    }
                 };
                Utilities.runInEventDispatchThread(r);
            }
        };

        FileObject moduleRegistry = FileUtil.getConfigFile("Modules"); //NOI18N

        if (moduleRegistry !=null){
            moduleRegistry.addFileChangeListener(
                FileUtil.weakFileChangeListener(moduleRegListener, moduleRegistry));
        }
    }

    public @Override String getUIClassID() {
        //For GTK and Aqua look and feels, we provide a custom toolbar UI -
        //but we cannot override this globally or it will cause problems for
        //the form editor & other things
        if (UIManager.get("Nb.Toolbar.ui") != null) { //NOI18N
            return "Nb.Toolbar.ui"; //NOI18N
        } else {
            return super.getUIClassID();
        }
    }
    
    public @Override String getName() {
        //Required for Aqua L&F toolbar UI
        return "editorToolbar"; // NOI18N
    }
    
    public @Override void setUI(ToolBarUI ui){
        addListener = false;
        super.setUI(ui);
        addListener = true;
    }
    
    public @Override synchronized void addMouseListener(MouseListener l) {
        if (addListener) {
            super.addMouseListener(l);
        }
    }
    
    public @Override synchronized void addMouseMotionListener(MouseMotionListener l) {
        if (addListener) {
            super.addMouseMotionListener(l);
        }
    }
    
    private boolean isToolbarVisible() {
        return preferences.getBoolean(SimpleValueNames.TOOLBAR_VISIBLE_PROP, EditorPreferencesDefaults.defaultToolbarVisible);
    }
    
    private void refreshToolbarButtons() {
	final JTextComponent c = getComponent();
        final boolean visible = isToolbarVisible();
        
        Runnable r = new Runnable() {
            public void run() {
                if (visible) {
                    checkPresentersAdded();
                    if (c != null) { //#62487
                        installNoOpActionMappings();
                        Map<String, MultiKeyBinding> keybsMap = getKeyBindingMap();

                        Component comps[] = getComponents();
                        for (int i = 0; i < comps.length; i++) {
                            Component comp = comps[i];
                            if (comp instanceof JButton) {
                                JButton button = (JButton) comp;
                                Action action = button.getAction();
                                if (action == null) {
                                    continue;
                                }
                                String actionName = (String) action.getValue(Action.NAME);
                                if (actionName == null) {
                                    continue;
                                }

                                String tooltipText = button.getToolTipText();
                                if (tooltipText != null) {
                                    int index = tooltipText.indexOf("("); //NOI18N
                                    if (index > 0) {
                                        tooltipText = tooltipText.substring(0, index - 1);
                                    }
                                }

                                MultiKeyBinding mkb = keybsMap.get(actionName);
                                if (mkb != null) {
                                    button.setToolTipText(tooltipText + " (" + // NOI18N
                                            EditorActionUtilities.getKeyMnemonic(mkb) + ")"); // NOI18N
                                } else {
                                    button.setToolTipText(tooltipText);
                                }
                            }
                        }
                    }
                } else {
                    checkPresentersRemoved();
                }
                setVisible(visible);
            }
        };
        
        Utilities.runInEventDispatchThread(r);
    }
    
    private void checkPresentersAdded() {
        if (!presentersAdded) {
            presentersAdded = true;
            addPresenters();
        }
    }
    
    private void checkPresentersRemoved() {
        presentersAdded = false;        
        removeAll();
    }    

    private Map<String, MultiKeyBinding> getKeyBindingMap() {
        Map<String, MultiKeyBinding> map = new HashMap<String, MultiKeyBinding>();
        List<? extends MultiKeyBinding> list = getKeyBindingList();
        
        for(MultiKeyBinding mkb : list){
            map.put(mkb.getActionName(), mkb);
        }
        
        return map;
    }
    
    private List<? extends MultiKeyBinding> getKeyBindingList() {
        Collection<? extends KeyBindingSettings> c = lookupResult.allInstances();
        if (!c.isEmpty()) {
            KeyBindingSettings kbs = c.iterator().next();
            return kbs.getKeyBindings();
        } else {
            return Collections.<MultiKeyBinding>emptyList();
        }
    }

    public static void initKeyBindingList(String mimeType) {
        Collection<? extends KeyBindingSettings> c = MimeLookup.getLookup(mimeType).lookupAll(KeyBindingSettings.class);
        if (!c.isEmpty()) {
            // just do something with the collection
            c.iterator().next();
        }
    }
    
    private JTextComponent getComponent() {
	return (JTextComponent)componentRef.get();
    }
    
    private ToolbarItemHider hider;
    
    void attachHidingComponent(Component c) {
        if (hider == null) {
            hider = new ToolbarItemHider();
        }
        c.addPropertyChangeListener("enabled", hider); // NOI18N
    }
    
    private class ToolbarItemHider implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("enabled".equals(evt.getPropertyName())) { // NOI18N
                hideShowPresenter((JComponent)evt.getSource());
            }
        }
    }
    
    private void hideShowPresenter(JComponent jc) {
        boolean vis = jc.isEnabled();
        jc.setVisible(vis);
        // check surroundings and hide possible adjacent separators
        Component[] comps = getComponents();
        int idx = getComponentIndex(jc);
        if (idx == -1) {
            return;
        }
        int sepBefore;
        for (sepBefore = idx - 1; sepBefore >= 0; sepBefore--) {
            Component c = comps[sepBefore];
            if (c.isVisible()) {
                if (!(c instanceof JSeparator)) {
                    break;
                }
            }
        }
        int sepAfter;
        final int l = comps.length;
        for (sepAfter = idx + 1; sepAfter < l; sepAfter++) {
            Component c = comps[sepAfter];
            if (c.isVisible()) {
                if (!(c instanceof JSeparator)) {
                    break;
                }
            }
        }
        boolean first = sepBefore >= 0;
        // hide all JSeparators except one between sepBefore + 1 and sepAfter - 1
        for (int i = sepBefore + 1; i < sepAfter; i++) {
            Component c = comps[i];
            if (c instanceof JSeparator) {
                c.setVisible(first);
                first = false;
            }
        }
    }
    
    /** Add the presenters (usually buttons) for the contents of the toolbar
     * contained in the base and mime folders.
     * @param baseFolder folder that corresponds to "text/base"
     * @param mimeFolder target mime type folder.
     * @param toolbar toolbar being constructed.
     */
    private void addPresenters() {
        JTextComponent c = getComponent();
        String mimeType = c == null ? null : NbEditorUtilities.getMimeType(c);
        Reference<JTextComponent> cRef = new WeakReference<>(c);
        
        if (mimeType == null) {
            return; // Probably no component or it's not loaded properly
        }

        List<? extends MultiKeyBinding> keybindings = null;
        Lookup actionContext = null;
        List items = ToolbarActionsProvider.getToolbarItems(mimeType);
        
        // COMPAT: The ToolbarsActionsProvider treats 'text/base' in a special way. It
        // will list only items registered for this particular mime type, but won't
        // inherit anything else. The 'text/base' is normally empty, but could be
        // used by some legacy code.
        List oldTextBaseItems = ToolbarActionsProvider.getToolbarItems("text/base"); //NOI18N
        if (oldTextBaseItems.size() > 0) {
            items = new ArrayList(items);
            items.add(new JSeparator());
            items.addAll(oldTextBaseItems);
        }
        List<JComponent>    processHiding = new ArrayList<>();
        
        for(Object item : items) {
            LOG.log(Level.FINE, "Adding item {0}", item); //NOI18N

            if (item == null || item instanceof JSeparator) {
                addSeparator();
                continue;
            }
            
            if (item instanceof String) {
                EditorKit kit = c.getUI().getEditorKit(c);
                if (kit instanceof BaseKit) {
                    Action a = ((BaseKit) kit).getActionByName((String) item);
                    if (a != null) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "Item {0} converted to an editor action {1}", new Object [] { item, s2s(a) }); //NOI18N
                        }
                        item = a;
                    } else {
                        // unknown action
                        continue;
                    }
                }
            }
            
            if (item instanceof ContextAwareAction) {
                if (actionContext == null) {
                    Lookup context = createActionContext(c);
                    actionContext = context == null ? NO_ACTION_CONTEXT : context;
                }
                
                if (actionContext != NO_ACTION_CONTEXT) {
                    Action caa = ((ContextAwareAction) item).createContextAwareInstance(actionContext);
                    
                    // use the context aware instance only if it implements Presenter.Toolbar
                    // or is a Component else fall back to the original object
                    if (caa instanceof Presenter.Toolbar || caa instanceof Component) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "Item {0} converted to a context-aware Action {1}", new Object [] { s2s(item), s2s(caa) }); //NOI18N
                        }
                        item = caa;
                    }
                }
            }
            
            Action ai = item instanceof Action ? (Action)item : null;
            
            if (item instanceof Presenter.Toolbar) {
                Component presenter = ((Presenter.Toolbar) item).getToolbarPresenter();
                if (presenter != null) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "Item {0} converted to a Presenter.Toolbar {1}", new Object [] { s2s(item), s2s(presenter) }); //NOI18N
                    }
                    if (presenter instanceof JComponent) {
                        // Since toolbars do not appear to always GC in current setup put a weak reference to component into a client property (not a strong ref)
                        ((JComponent)presenter).putClientProperty(JTextComponent.class, cRef);
                    }
                    item = presenter;
                }
            }
            
            boolean hideWhenDisabled = ai != null && (ai.getValue(DynamicMenuContent.HIDE_WHEN_DISABLED) == Boolean.TRUE) &&
                        !ai.isEnabled();
            
            if (item instanceof Component) {
                add((Component)item);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Adding component {0}", s2s(item)); //NOI18N
                }
            } else if (item instanceof Action) {
                // Wrap action to execute on the proper text component
                // because the default fallback in TextAction.getTextComponent()
                // might not work properly if the focus was switched
                // to e.g. a JTextField and then toolbar was clicked.
                Action a = new WrapperAction(componentRef, (Action) item);
                
                // Try to find an icon if not present
                updateIcon(a);

                // Add the action and let the JToolbar to creat a presenter for it
                item = add(a);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Adding action {0} as {1}", new Object [] { s2s(a), s2s(item) }); //NOI18N
                }
            } else {
                // Some sort of crappy item -> ignore
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Ignoring item {0}", s2s(item)); //NOI18N
                }
                continue;
            }
            if (hideWhenDisabled) {
                JComponent jc = (JComponent)item;
                attachHidingComponent(jc);
                if (!jc.isEnabled()) {
                    processHiding.add(jc);
                }
            }
            if (item instanceof AbstractButton) {
                AbstractButton button = (AbstractButton)item;
                processButton(button);
                
                if (keybindings == null) {
                    List<? extends MultiKeyBinding> l = getKeyBindingList();
                    keybindings = l == null ? Collections.<MultiKeyBinding>emptyList() : l;
                }
                updateTooltip(button, keybindings);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Special treatment for button {0}", s2s(item)); //NOI18N
                }
            }
        }
        for (JComponent jc : processHiding) {
            hideShowPresenter(jc);
        }
    }
    
    // XXX: this is actually wierd, because it changes the action's properties
    // perhaps we should just update the presenter, but should not touch the
    // action itself
    private static void updateIcon(Action a) {
        Object icon = a.getValue(Action.SMALL_ICON);
        if (icon == null) {
            String resourceId = (String)a.getValue(BaseAction.ICON_RESOURCE_PROPERTY);
            if (resourceId != null) {
                ImageIcon img = ImageUtilities.loadImageIcon(resourceId, true);
                if (img != null) {
                    a.putValue(Action.SMALL_ICON, img);
                }
            }
        }
    }

    private static void updateTooltip(AbstractButton b, List<? extends MultiKeyBinding> keybindings) {
        Action a = b.getAction();
        String actionName = a == null ? null : (String) a.getValue(Action.NAME);
        
        if (actionName == null) {
            // perhaps no action at all
            return;
        }
        
        for (MultiKeyBinding mkb : keybindings) {
            if (actionName.equals(mkb.getActionName())) {
                b.setToolTipText(b.getToolTipText() + " (" + // NOI18N
                        EditorActionUtilities.getKeyMnemonic(mkb) + ")"); // NOI18N
                break; // multiple shortcuts ?
            }
        }
    }
    
    /**
     * Not private because of the tests.
     */
    static Lookup createActionContext(JTextComponent c) {
        Lookup nodeLookup = null;
        DataObject dobj = (c != null) ? NbEditorUtilities.getDataObject(c.getDocument()) : null;
        if (dobj != null && dobj.isValid()) {
            nodeLookup = dobj.getNodeDelegate().getLookup();
        }

        Lookup ancestorLookup = null;
        for (java.awt.Component comp = c; comp != null; comp = comp.getParent()) {
            if (comp instanceof Lookup.Provider) {
                Lookup lookup = ((Lookup.Provider)comp).getLookup ();
                if (lookup != null) {
                    ancestorLookup = lookup;
                    break;
                }
            }
        }

        Lookup componentLookup = Lookups.singleton(c);
        if (nodeLookup == null && ancestorLookup == null) {
            return componentLookup;
        } else if (nodeLookup == null) {
            return new ProxyLookup(new Lookup[] { ancestorLookup, componentLookup });
        } else if (ancestorLookup == null) {
            return new ProxyLookup(new Lookup[] { nodeLookup, componentLookup });
        }
        assert nodeLookup != null && ancestorLookup != null;

        Node node = (Node)nodeLookup.lookup(Node.class);
        boolean ancestorLookupContainsNode = ancestorLookup.lookup(
                new Lookup.Template(Node.class)).allInstances().contains(node);

        if (ancestorLookupContainsNode) {
            return new ProxyLookup(new Lookup[] { ancestorLookup, componentLookup });
        } else {
            return new ProxyLookup(new Lookup[] { nodeLookup, ancestorLookup, componentLookup });
        }
    }

    private void processButton(AbstractButton button) {
        removeButtonContentAreaAndBorder(button);
        button.setMargin(BUTTON_INSETS);
        if (button instanceof AbstractButton) {
            button.addMouseListener(sharedMouseListener);
        }
        //fix of issue #69642. Focus shouldn't stay in toolbar
        button.setFocusable(false);
    }

    private static void removeButtonContentAreaAndBorder(AbstractButton button) {
        boolean canRemove = true;
        if (button instanceof JToggleButton) {
            canRemove = !button.isSelected();
        }
        if (canRemove) {
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
        }
    }

    /** Attempt to find the editor keystroke for the given action. */
    private KeyStroke[] findEditorKeys(String editorActionName, KeyStroke defaultKey) {
        KeyStroke[] ret = new KeyStroke[] { defaultKey };
        JTextComponent comp = getComponent();
        if (editorActionName != null && comp != null) {
            TextUI textUI = comp.getUI();
            Keymap km = comp.getKeymap();
            if (textUI != null && km != null) {
                EditorKit kit = textUI.getEditorKit(comp);
                if (kit instanceof BaseKit) {
                    Action a = ((BaseKit)kit).getActionByName(editorActionName);
                    if (a != null) {
                        KeyStroke[] keys = km.getKeyStrokesForAction(a);
                        if (keys != null && keys.length > 0) {
                            ret = keys;
                        } else {
                            // try kit's keymap
                            Keymap km2 = ((BaseKit)kit).getKeymap();
                            KeyStroke[] keys2 = km2.getKeyStrokesForAction(a);
                            if (keys2 != null && keys2.length > 0) {
                                ret = keys2;
                            }                            
                        }
                    }
                }
            }
        }
        return ret;
    }

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }

    /** No operation action  - do nothing when invoked 
     *  issue #69642
     */
    private static final class NoOpAction extends AbstractAction{
        public NoOpAction(){
        }
        public void actionPerformed(ActionEvent e) {
        }
    } // End of NoOpAction class
    
    private static final class WrapperAction implements Action {
        
        private final Reference componentRef;
        
        private final Action delegate;
        
        WrapperAction(Reference componentRef, Action delegate) {
            this.componentRef = componentRef;
            assert (delegate != null);
            this.delegate = delegate;
        }
        
        public Object getValue(String key) {
            return delegate.getValue(key);
        }

        public void putValue(String key, Object value) {
            delegate.putValue(key, value);
        }

        public void setEnabled(boolean b) {
            delegate.setEnabled(b);
        }

        public boolean isEnabled() {
            return delegate.isEnabled();
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            delegate.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            delegate.removePropertyChangeListener(listener);
        }

        public void actionPerformed(ActionEvent e) {
            JTextComponent c = (JTextComponent)componentRef.get();
            if (c != null) { // Override action event to text component
                e = new ActionEvent(c, e.getID(), e.getActionCommand());
            }
            delegate.actionPerformed(e);
        }
    } // End of WrapperAction class
}
