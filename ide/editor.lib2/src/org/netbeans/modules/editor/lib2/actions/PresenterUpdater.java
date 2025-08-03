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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.AbstractEditorAction;
import org.openide.awt.Mnemonics;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * Handler servicing menu, popup menu and toolbar presenters of a typical editor action.
 *
 * @author Miloslav Metelka
 */
public final class PresenterUpdater implements PropertyChangeListener, ActionListener {
    
    public static JMenuItem createMenuPresenter(Action a) {
        return (JMenuItem) new PresenterUpdater(MENU, a).presenter;
    }

    public static JMenuItem createPopupPresenter(Action a) {
        return (JMenuItem) new PresenterUpdater(POPUP, a).presenter;
    }

    public static AbstractButton createToolbarPresenter(Action a) {
        return new PresenterUpdater(TOOLBAR, a).presenter;
    }
    
    // -J-Dorg.netbeans.modules.editor.lib2.actions.PresenterUpdater.level=FINE
    private static final Logger LOG = Logger.getLogger(PresenterUpdater.class.getName()); // NOI18N

    private static final int MENU = 0;
    private static final int POPUP = 1;
    private static final int TOOLBAR = 2;

    private static final boolean ON_MAC = Utilities.isMac();

    private final int type;

    private final String actionName;

    /**
     * Action for which this presenter is constructed.
     */
    private final Action action;

    /**
     * For menu presenters hold action of last focused editor component.
     * <br>
     * Hold reference strongly but may be changed to weak reference in case
     * even actions of last activated text component are desired to be released.
     */
    private Action contextAction;

    final AbstractButton presenter;
    
    private final boolean useActionSelectedProperty;

    private final Set<Action> listenedContextActions; // Actions to which weak listeners have been attached

    private boolean updatesPending;

    /**
     * When menu is not active then this property is set to true.
     */
    private boolean presenterActive;
    
    private PresenterUpdater(int type, Action action) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null"); // NOI18N
        }
        this.type = type;
        this.actionName = (String) action.getValue(Action.NAME);
        this.action = action;
        if (type == TOOLBAR) {
            presenter = new JButton();
            useActionSelectedProperty = false;
        } else { // MENU or POPUP
            useActionSelectedProperty = (action.getValue(AbstractEditorAction.PREFERENCES_KEY_KEY) != null);
            if (useActionSelectedProperty) {
                presenter = new LazyJCheckBoxMenuItem();
                presenter.setSelected(isActionSelected());
            } else {
                presenter = new LazyJMenuItem();
            }
        }

        action.addPropertyChangeListener(WeakListeners.propertyChange(this, action));
        if (type == MENU) {
            listenedContextActions = Collections.newSetFromMap(new WeakHashMap<>());
            EditorRegistryWatcher.get().registerPresenterUpdater(this); // Includes notification of active component
        } else {
            listenedContextActions = null;
        }

        presenter.addActionListener(this);
        updatePresenter(null); // Not active yet => mark updates pending
    }
    
    public String getActionName() {
        return actionName;
    }

    private boolean isActionSelected() {
        return Boolean.TRUE.equals(activeAction().getValue(Action.SELECTED_KEY));
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) { // evt may be null
        Action a = (Action) evt.getSource();
        // React to property changes from both action and contextAction
        // since some of presenter's properties may default from contextAction's property
        // to action's property value.
        if (a != action && a != contextAction) {
            return;
        }
        updatePresenter(evt.getPropertyName());
    }

    void presenterActivated() {
        if (!presenterActive) {
            presenterActive = true;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Presenter for " + action + " activated. updatesPending=" + updatesPending + '\n'); // NOI18N
            }
            if (updatesPending) {
                updatesPending = false;
                updatePresenter(null);
            }
        }
    }

    void presenterDeactivated() {
        presenterActive = false;
    }

    private void updatePresenter(String propName) {
        // For menu items do lazy update (only when they become visible (measured)
        // since they are being updated by an active component's action properties.
        if (!ON_MAC && type == MENU && !presenterActive) {
            updatesPending = true;

            // Invalidate presenter
            if (SwingUtilities.isEventDispatchThread()) {
                presenter.invalidate();
            } else { // Include non-AWT version for completness
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        presenter.invalidate();
                    }
                });
            }

            return;
        }

        Action cAction = contextAction;
        // Enabled status
        if ((propName == null) || "enabled".equals(propName)) { // NOI18N
            boolean enabled = (cAction != null ? cAction : action).isEnabled();
            presenter.setEnabled(enabled);
        }

        // Text
        if ((propName == null) || AbstractEditorAction.DISPLAY_NAME_KEY.equals(propName) ||
                AbstractEditorAction.MENU_TEXT_KEY.equals(propName) ||
                AbstractEditorAction.POPUP_TEXT_KEY.equals(propName))
        {
            if (type != TOOLBAR) {
                String text = null;
                if (type == POPUP) {
                    if (cAction != null) {
                        text = (String) cAction.getValue(AbstractEditorAction.POPUP_TEXT_KEY);
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("POPUP_TEXT_KEY for context " + cAction + ": \"" + text + "\"\n"); // NOI18N
                        }
                    }
                    if (text == null) {
                        text = (String) action.getValue(AbstractEditorAction.POPUP_TEXT_KEY);
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("POPUP_TEXT_KEY for action " + action + ": \"" + text + "\"\n"); // NOI18N
                        }
                    }

                }
                if (text == null && cAction != null) {
                    text = (String) cAction.getValue(AbstractEditorAction.MENU_TEXT_KEY);
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("MENU_TEXT_KEY for context " + cAction + ": \"" + text + "\"\n"); // NOI18N
                    }
                }
                if (text == null) {
                    text = (String) action.getValue(AbstractEditorAction.MENU_TEXT_KEY);
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("MENU_TEXT_KEY for " + action + ": \"" + text + "\"\n"); // NOI18N
                    }
                }

                if (text != null) {
                    Mnemonics.setLocalizedText(presenter, text);
                    presenter.getAccessibleContext().setAccessibleName(text);
                } else {
                    if (cAction != null) {
                        text = (String) cAction.getValue(AbstractEditorAction.DISPLAY_NAME_KEY);
                    }
                    if (text == null) {
                        text = (String) action.getValue(AbstractEditorAction.DISPLAY_NAME_KEY);
                    }
                    if (text != null) {
                        presenter.setText(text); // Do not handle '&' chars
                        presenter.getAccessibleContext().setAccessibleName(text);
                    }
                }
            }
        }

        // Icon(s)
        if ((propName == null) || Action.SMALL_ICON.equals(propName)
                || Action.LARGE_ICON_KEY.equals(propName) ||
                AbstractEditorAction.ICON_RESOURCE_KEY.equals(propName))
        {
            Icon icon = null;
            if (isMenuItem()) {
                if (cAction != null && Boolean.TRUE.equals(cAction.getValue(AbstractEditorAction.NO_ICON_IN_MENU))) {
                    return;
                }
                if (cAction != null) {
                    icon = EditorActionUtilities.getSmallIcon(cAction);
                }

                if (icon == null && Boolean.TRUE.equals(action.getValue(AbstractEditorAction.NO_ICON_IN_MENU))) {
                    return;
                }
                if (icon == null) {
                    icon = EditorActionUtilities.getSmallIcon(action);
                }
                if (icon != null) {
                    presenter.setIcon(icon);
                }
            } else { // toolbar
                boolean useLargeIcon = EditorActionUtilities.isUseLargeIcon(presenter);
                if (useLargeIcon) {
                    if (cAction != null) {
                        icon = EditorActionUtilities.getLargeIcon(cAction);
                    }
                    if (icon == null) {
                        icon = EditorActionUtilities.getLargeIcon(action);
                    }
                }
                if (icon == null) { // useLargeIcon is false or no large icon present => use small icon
                    useLargeIcon = false;
                    if (cAction != null) {
                        icon = EditorActionUtilities.getSmallIcon(cAction);
                    }
                    if (icon == null) {
                        icon = EditorActionUtilities.getSmallIcon(action);
                    }
                }
                if (icon != null) {
                    String iconResource = null;
                    if (cAction != null) {
                        iconResource = (String) cAction.getValue(AbstractEditorAction.ICON_RESOURCE_KEY);
                    }
                    if (iconResource == null) {
                        iconResource = (String) action.getValue(AbstractEditorAction.ICON_RESOURCE_KEY);
                    }
                    EditorActionUtilities.updateButtonIcons(presenter, icon, useLargeIcon, iconResource);
                }
            }
        }

        // Accelerator
        if ((propName == null) ||
                Action.ACCELERATOR_KEY.equals(propName) ||
                AbstractEditorAction.MULTI_ACCELERATOR_LIST_KEY.equals(propName)
        ) {
            if (isMenuItem()) {
                Action a = (cAction != null) ? cAction : action;
                @SuppressWarnings("unchecked")
                List<List<KeyStroke>> mkbList = (List<List<KeyStroke>>) a.getValue(AbstractEditorAction.MULTI_ACCELERATOR_LIST_KEY);
                KeyStroke accelerator = null;
                if (mkbList != null && mkbList.size() > 0) {
                    List<KeyStroke> firstMkb = mkbList.get(0);
                    accelerator = firstMkb.get(0);
                } else {
                    accelerator = (KeyStroke) a.getValue(Action.ACCELERATOR_KEY);
                }
                ((JMenuItem) presenter).setAccelerator(accelerator);
            }
        }

        // ToolTip
        if ((propName == null) ||
                Action.ACCELERATOR_KEY.equals(propName) ||
                AbstractEditorAction.MULTI_ACCELERATOR_LIST_KEY.equals(propName) ||
                Action.SHORT_DESCRIPTION.equals(propName)
        ) {
            if (type == TOOLBAR) {
                String toolTipText = null;
                if (cAction != null) {
                    toolTipText = (String) cAction.getValue(Action.SHORT_DESCRIPTION);
                }
                if (toolTipText == null) {
                    toolTipText = (String) action.getValue(Action.SHORT_DESCRIPTION);
                }
                if (toolTipText == null) {
                    toolTipText = "";
                }
                if (toolTipText.length() > 0) {
                    Action a = (cAction != null) ? cAction : action;
                    @SuppressWarnings("unchecked")
                    List<List<KeyStroke>> mkbList = (List<List<KeyStroke>>) a.getValue(AbstractEditorAction.MULTI_ACCELERATOR_LIST_KEY);
                    if (mkbList != null && mkbList.size() > 0) {
                        List<KeyStroke> firstMkb = mkbList.get(0);
                        toolTipText += " (" + EditorActionUtilities.getKeyMnemonic(firstMkb) + ")"; // NOI18N
                    } else {
                        KeyStroke accelerator = (KeyStroke) a.getValue(Action.ACCELERATOR_KEY);
                        if (accelerator != null) {
                            toolTipText += " (" + EditorActionUtilities.getKeyMnemonic(accelerator) + ")"; // NOI18N
                        }
                    }
                }
                presenter.setToolTipText(toolTipText);
            }
        }

        // Selected (for checkbox)
        if (useActionSelectedProperty && (propName == null || Action.SELECTED_KEY.equals(propName))) {
            if (isMenuItem()) {
                presenter.setSelected(isActionSelected());
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        // If text component is set into the presenter, then run the actions
        // like if they are invoked from the component
        Object jtcOrRef = presenter.getClientProperty(JTextComponent.class);
        JTextComponent c = (JTextComponent)
                ((jtcOrRef instanceof Reference)
                    ? ((Reference)jtcOrRef).get()
                    : jtcOrRef);
        if (c != null) {
            evt = new ActionEvent(c, evt.getID(), evt.getActionCommand(), evt.getWhen(), evt.getModifiers());
        }
        activeAction().actionPerformed(evt);
    }

    public void setActiveAction(Action a) {
        if (a == action) { // In this case there is in fact no extra context
            a = null;
        }
        synchronized (this) {
            if (a != contextAction) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("setActiveAction(): from " + contextAction + " to " + a + "\n"); // NOI18N
                }
                contextAction = a;
                if (a != null && !listenedContextActions.contains(a)) {
                    listenedContextActions.add(a);
                    a.addPropertyChangeListener(WeakListeners.propertyChange(this, a));
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("setActiveAction(): started listening on " + a + "\n"); // NOI18N
                    }
                }
                updatePresenter(null); // Update presenter completely
            }
        }
    }

    private Action activeAction() {
        return (contextAction != null) ? contextAction : action;
    }

    private boolean isMenuItem() {
        return (type == MENU || type == POPUP);
    }

    private final class LazyJMenuItem extends JMenuItem {

        @Override
        public void removeNotify() {
            super.removeNotify();
            presenterDeactivated();
        }

        @Override
        public Dimension getPreferredSize() {
            presenterActivated(); // Update properties (addNotify() is too late)
            return super.getPreferredSize();
        }

    }
    
    private final class LazyJCheckBoxMenuItem extends JCheckBoxMenuItem {

        @Override
        public void removeNotify() {
            super.removeNotify();
            presenterDeactivated();
        }

        @Override
        public Dimension getPreferredSize() {
            presenterActivated(); // Update properties (addNotify() is too late)
            return super.getPreferredSize();
        }

    }

}
