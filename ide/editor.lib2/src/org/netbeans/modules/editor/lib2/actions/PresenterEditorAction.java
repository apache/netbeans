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

package org.netbeans.modules.editor.lib2.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.EditorUtilities;
import org.openide.awt.Actions;
import org.openide.util.WeakSet;
import org.openide.util.actions.Presenter;

/**
 * Action that represents a named editor action in main menu, popup menu
 * and editor toolbar.
 * <br>
 * The actions are registered into "Editors/ActionPresenters" regardless
 * of the mime-type for which the actions get created.
 */
public final class PresenterEditorAction extends TextAction
        implements Presenter.Menu, Presenter.Popup, Presenter.Toolbar, PropertyChangeListener
{

    /**
     * Boolean action property displayed by the checkbox menu item.
     */
    private static final String SELECTED_KEY = "SwingSelectedKey"; // [TODO] Replace with "Action.SELECTED_KEY" on 1.6

    // -J-Dorg.netbeans.modules.editor.lib2.actions.PresenterEditorAction.level=FINEST
    private static final Logger LOG = Logger.getLogger(PresenterEditorAction.class.getName());

    /**
     * Currently active editor component's editor kit reference.
     */
    private static Reference<SearchableEditorKit> activeKitRef;

    private static boolean activeKitLastFocused;

    private static final Set<PresenterEditorAction> presenterActions = new WeakSet<PresenterEditorAction>();

    private static final ChangeListener actionsChangeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            synchronized (PresenterEditorAction.class) {
                refreshActiveKitActions(activeKit(), true);
            }
        }
    };

    public static SearchableEditorKit activeKit() {
        synchronized (PresenterEditorAction.class) {
            return (activeKitRef != null) ? activeKitRef.get() : null;
        }
    }

    static {
        EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
                    JTextComponent focusedTextComponent = (JTextComponent) evt.getNewValue();
                    SearchableEditorKit oldActiveKit = activeKit();
                    EditorKit kit = null;
                    if (focusedTextComponent != null) {
                        TextUI ui = focusedTextComponent.getUI();
                        if (ui != null) {
                            kit = ui.getEditorKit(focusedTextComponent);
                        }
                    }

                    synchronized (PresenterEditorAction.class) {
                        SearchableEditorKit newActiveKit = (kit != null)
                                ? EditorActionUtilities.getSearchableKit(kit)
                                : null;
                        boolean kitChanged;
                        if (newActiveKit != oldActiveKit) {
                            if (oldActiveKit != null) {
                                oldActiveKit.removeActionsChangeListener(actionsChangeListener);
                            }
                            activeKitRef = (newActiveKit != null)
                                    ? new WeakReference<SearchableEditorKit>(newActiveKit)
                                    : null;
                            if (newActiveKit != null) {
                                newActiveKit.addActionsChangeListener(actionsChangeListener);
                            }
                            kitChanged = true;
                        } else {
                            kitChanged = false;
                        }
                        boolean focusChanged = (activeKitLastFocused == false);
                        activeKitLastFocused = true;
                        if (focusChanged || kitChanged) {
                            refreshActiveKitActions(newActiveKit, kitChanged);
                        }
                    }

                } else if (EditorRegistry.FOCUS_LOST_PROPERTY.equals(evt.getPropertyName())) {
                    synchronized (PresenterEditorAction.class) {
                        boolean newActiveKitLastFocused = (EditorRegistry.lastFocusedComponent() != null);
                        if (newActiveKitLastFocused != activeKitLastFocused) {
                            activeKitLastFocused = newActiveKitLastFocused;
                            for (PresenterEditorAction a : presenterActions) {
                                a.refreshActiveKitAction(null, false);
                            }
                        }
                    }
                }
            }
        });
        EditorActionUtilities.getGlobalActionsKit().addActionsChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                SearchableEditorKit globalKit = EditorActionUtilities.getGlobalActionsKit();
                synchronized (PresenterEditorAction.class) {
                    for (PresenterEditorAction a : presenterActions) {
                        a.refreshGlobalAction(globalKit);
                    }
                }
            }
        });
    }

    static void registerAction(PresenterEditorAction action) {
        synchronized (PresenterEditorAction.class) {
            presenterActions.add(action);
        }
    }

    private static void refreshActiveKitActions(SearchableEditorKit activeKit, boolean kitChanged) {
        for (PresenterEditorAction a : presenterActions) {
            a.refreshActiveKitAction(activeKit, kitChanged);
        }
    }

    public static Action create(Map<String,?> attrs) {
        String actionName = (String)attrs.get(Action.NAME);
        if (actionName == null) {
            throw new IllegalArgumentException("Null Action.NAME attribute for attrs: " + attrs); // NOI18N
        }
        return new PresenterEditorAction(actionName);
    }

    private final String actionName;

    private JMenuItem menuPresenter;

    private JMenuItem popupPresenter;

    private Component toolBarPresenter;

    private Action globalKitAction;

    private Action activeKitAction;

    private PresenterEditorAction(String actionName) {
        super(actionName);
        this.actionName = actionName;
        refreshGlobalAction(EditorActionUtilities.getGlobalActionsKit());
        refreshActiveKitAction(activeKit(), true);
        registerAction(this);
    }

    @Override
    public void putValue(String key, Object newValue) {
        if (actionName != null && Action.NAME.equals(key)) {
            throw new IllegalArgumentException("PresenterEditorAction(\"" + actionName + // NOI18N
                    "\"): putValue(Action.NAME,newName) prohibited."); // NOI18N
        }
        super.putValue(key, newValue);
    }

    public void actionPerformed(ActionEvent evt) {
        // Find the right action for the corresponding editor kit
        JTextComponent component = getTextComponent(evt);
        if (component != null) {
            TextUI ui = component.getUI();
            if (ui != null) {
                EditorKit kit = ui.getEditorKit(component);
                if (kit != null) {
                    Action action = EditorUtilities.getAction(kit, actionName);
                    if (action != null) {
                        action.actionPerformed(evt);
                    } else {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Action '" + actionName + "' not found in editor kit " + kit + '\n'); // NOI18N
                        }
                    }
                }
            }
        }
    }

    public JMenuItem getMenuPresenter() {
        if (menuPresenter == null) {
            menuPresenter = createMenuItem(false);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("getMenuPresenter() for action=" + actionName + " returns " + menuPresenter); // NOI18N
        }
        return menuPresenter;
    }

    public JMenuItem getPopupPresenter() {
        if (popupPresenter == null) {
            popupPresenter = createMenuItem(true);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("getPopupPresenter() for action=" + actionName + " returns " + popupPresenter); // NOI18N
        }
        return popupPresenter;
    }

    public Component getToolbarPresenter() {
        if (toolBarPresenter == null) {
            toolBarPresenter = new JButton(this);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("getToolbarPresenter() for action=" + actionName + " returns " + toolBarPresenter); // NOI18N
        }
        return toolBarPresenter;
    }

    @Override
    public Object getValue(String key) {
        Object value = super.getValue(key);
        if (value == null) {
            if (!"instanceCreate".equals(key)) { // Return null for this key
                Action action = activeKitAction;
                if (action != null) {
                    value = action.getValue(key);
                }
                if (value == null) {
                    action = globalKitAction;
                    if (action != null) {
                        value = action.getValue(key);
                    }
                }
            }
        }
        return value;
    }

    @Override
    public boolean isEnabled() {
        return (activeKitAction != null) ? activeKitAction.isEnabled() && activeKitLastFocused : false;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (SELECTED_KEY.equals(propertyName)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("propertyChange() of SELECTED_KEY for action " + actionName);
            }
            updateSelectedInPresenters();
        }
        // Re-fire the property change
        firePropertyChange(propertyName, null, null);
    }
    
    void refreshGlobalAction(SearchableEditorKit kit) {
        Action newAction = (kit != null) ? kit.getAction(actionName) : null;
        if (newAction != globalKitAction) {
            if (globalKitAction != null) {
                globalKitAction.removePropertyChangeListener(this);
            }
            ActionPropertyRefresh propertyRefresh = new ActionPropertyRefresh();
            propertyRefresh.before();
            globalKitAction = newAction;
            propertyRefresh.after();
            if (globalKitAction != null) {
                globalKitAction.addPropertyChangeListener(this);
            }
        }
    }

    void refreshActiveKitAction(SearchableEditorKit kit, boolean kitChanged) {
        if (kitChanged) {
            Action newAction = (kit != null) ? kit.getAction(actionName) : null;
            if (newAction != activeKitAction) {
                if (activeKitAction != null) {
                    activeKitAction.removePropertyChangeListener(this);
                }
                ActionPropertyRefresh propertyRefresh = new ActionPropertyRefresh();
                propertyRefresh.before();
                activeKitAction = newAction;
                propertyRefresh.after();
                if (activeKitAction != null) {
                    activeKitAction.addPropertyChangeListener(this);
                }
            }
        }
        // Always fire "enabled" change. If problematic add a local copy of "activeKitFocused" flag.
        boolean newEnabled = isEnabled();
        firePropertyChange("enabled", !newEnabled, newEnabled);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("\"" + actionName + "\".refreshActiveKitAction(): activeKitFocused=" + // NOI18N
                    activeKitLastFocused + ", newEnabled=" + newEnabled + ", kitChanged=" + kitChanged + '\n'); // NOI18N
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.INFO, "", new Exception());
            }
        }
    }

    private void updateSelectedInPresenters() {
        if (isCheckBox()) {
            boolean selected = isSelected();
            if (menuPresenter instanceof JCheckBoxMenuItem) {
                ((JCheckBoxMenuItem)menuPresenter).setSelected(selected);
            }
            if (popupPresenter instanceof JCheckBoxMenuItem) {
                ((JCheckBoxMenuItem)popupPresenter).setSelected(selected);
            }
        }
    }

    private boolean isSelected() {
        return Boolean.TRUE.equals(getValue(SELECTED_KEY));
    }

    private JMenuItem createMenuItem(boolean isPopup) {
        final JMenuItem menuItem;
        if (isCheckBox()) {
            menuItem = new JCheckBoxMenuItem();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Create checkbox menu item for action " + actionName + ", selected=" + isSelected());
            }
            menuItem.setSelected(isSelected());
            menuItem.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent evt) {
                    boolean checkboxSelected = ((JCheckBoxMenuItem)evt.getSource()).isSelected();
                    boolean actionSelected = isSelected();
                    if (checkboxSelected != actionSelected) {
                        Action action = activeKitAction;
                        if (action != null) {
                            action.putValue(SELECTED_KEY, checkboxSelected);
                        } else {
                            action = globalKitAction;
                            if (action != null) {
                                action.putValue(SELECTED_KEY, checkboxSelected);
                            }
                        }
                    }
                }
            });
        } else { // Regular menu item
            menuItem = new JMenuItem();
        }
        Actions.connect(menuItem, this, isPopup);
        return menuItem;
    }

    private boolean isCheckBox() {
        String presenterType = (String) getValue("PresenterType");
        return "CheckBox".equals(presenterType);
    }

    private final class ActionPropertyRefresh {

        private boolean checkBox;

        ActionPropertyRefresh() {
            this.checkBox = isCheckBox();
        }

        private boolean selected;

        void before() {
            if (checkBox) {
                selected = isSelected();
            }
        }

        void after() {
            if (checkBox) {
                if (selected != isSelected()) {
                    firePropertyChange(SELECTED_KEY, selected, !selected);
                }
            }
        }

    }
}
