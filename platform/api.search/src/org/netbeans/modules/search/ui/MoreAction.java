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
package org.netbeans.modules.search.ui;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import org.netbeans.modules.search.MatchingObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.ActionPresenterProvider;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;

/**
 * Action for creating "Other" submenu in Actions pop-up menu.
 *
 * @author jhavlin
 */
public class MoreAction extends NodeAction implements Presenter.Popup {

    private static final KeyStroke DELETE_KS
            = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);

    @Override
    protected void performAction(Node[] activatedNodes) {
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    @NbBundle.Messages("MoreAction.name=More")
    @Override
    public String getName() {
        return Bundle.MoreAction_name();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        Lookup l = Utilities.actionsGlobalContext();
        Collection<? extends MatchingObject> matchingObjects
                = l.lookupAll(MatchingObject.class);
        LinkedHashSet<Action> commonActions = new LinkedHashSet<>();
        boolean first = true;
        for (MatchingObject mo : matchingObjects) {
            DataObject dob = mo.getDataObject();
            if (dob != null) {
                Node nodeDelegate = dob.getNodeDelegate();
                Collection<Action> dobActions = Arrays.asList(
                        nodeDelegate.getActions(false));
                if (first) {
                    commonActions.addAll(dobActions);
                    first = false;
                } else {
                    commonActions.retainAll(dobActions);
                }
            }
        }
        return actionsToMenu(commonActions, l);
    }

    /**
     * Most of code copied from Utilities.actionsToPopup.
     */
    private JMenuItem actionsToMenu(Set<Action> actions, Lookup lookup) {
        // keeps actions for which was menu item created already (do not add them twice)
        Set<Action> counted = new HashSet<>();
        // components to be added (separators are null)
        List<Component> components = new ArrayList<>();

        for (Action action : actions) {
            if (action != null && counted.add(action)) {
                // switch to replacement action if there is some
                if (action instanceof ContextAwareAction) {
                    Action contextAwareAction = ((ContextAwareAction) action).createContextAwareInstance(
                            lookup);
                    if (contextAwareAction == null) {
                        Logger.getLogger(Utilities.class.getName()).log(
                                Level.WARNING,
                                "ContextAwareAction.createContextAwareInstance(lookup) returns null. That is illegal!" + " action={0}, lookup={1}",
                                new Object[]{action, lookup});
                    } else {
                        action = contextAwareAction;
                    }
                }

                JMenuItem item;
                if (action instanceof Presenter.Popup) {
                    item = ((Presenter.Popup) action).getPopupPresenter();
                    if (item == null) {
                        Logger.getLogger(Utilities.class.getName()).log(
                                Level.WARNING,
                                "findContextMenuImpl, getPopupPresenter returning null for {0}",
                                action);
                        continue;
                    }
                } else {
                    // We need to correctly handle mnemonics with '&' etc.
                    item = ActionPresenterProvider.getDefault().createPopupPresenter(
                            action);
                }
                if (!canBeEnabledLater(action) && !action.isEnabled()) {
                    continue;
                }
                for (Component c : ActionPresenterProvider.getDefault().convertComponents(
                        item)) {
                    if (!(c instanceof JSeparator)) {
                        components.add(c);
                    }
                }
            }
        }
        // Now create actual menu. Strip adjacent, leading, and trailing separators.
        JMenu menu = new JMenu(this);
        boolean nonempty = false; // has anything been added yet?
        boolean pendingSep = false; // should there be a separator before any following item?
        for (Component c : components) {
            try {
                if (c == null) {
                    pendingSep = nonempty;
                } else {
                    removeDeleteAccelerator(c);
                    nonempty = true;
                    if (pendingSep) {
                        pendingSep = false;
                        menu.addSeparator();
                    }
                    menu.add(c);
                }
            } catch (RuntimeException ex) {
                Exceptions.attachMessage(ex, "Current component: " + c); // NOI18N
                Exceptions.attachMessage(ex, "List of components: " + components); // NOI18N
                Exceptions.attachMessage(ex, "List of actions: " + actions); // NOI18N
                Exceptions.printStackTrace(ex);
            }
        }
        return menu;
    }

    /**
     * Some actions are enabled asynchronously, e.g. cut action, so they should
     * be added to the list even if disabled right now.
     *
     * @param a The action.
     * @return True if the action can be enabled later (i.e. it is updated
     * asynchronously), false otherwise.
     */
    private static boolean canBeEnabledLater(Action a) {
        Object key = a.getValue("key");                                 //NOI18N
        return "cut-to-clipboard".equals(key)                           //NOI18N
                || "copy-to-clipboard".equals(key);                     //NOI18N
    }

    /**
     * Remove DELETE accelerator from Delete action, because it is used by Hide
     * action in this context.
     */
    private void removeDeleteAccelerator(Component item) {
        if (item instanceof JMenuItem) {
            JMenuItem mItem = (JMenuItem) item;
            if (DELETE_KS.equals(mItem.getAccelerator())) {
                mItem.setAccelerator(null);
            }
        }
    }
}
