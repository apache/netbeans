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
package org.openide.actions;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;
import org.openide.windows.Workspace;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.openide.awt.Mnemonics;


/** Switch to a different workspace.
* @see Workspace#activate
* @author Ales Novak
 * @deprecated No longer used since there are no more workspaces.
*/
@Deprecated
@SuppressWarnings({"unchecked", "deprecation"})
public class WorkspaceSwitchAction extends CallableSystemAction {
    public String getName() {
        return NbBundle.getBundle(WorkspaceSwitchAction.class).getString("WorkspacesItems");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(WorkspaceSwitchAction.class);
    }

    public JMenuItem getMenuPresenter() {
        // beware, we shouldn't cache menu intstance, because getMenuPresenter
        // can be legally called several times and menu component cannot be
        // contained in more than one component hierarchy
        JMenu menu = new org.openide.awt.JMenuPlus();
        Mnemonics.setLocalizedText(menu, getName());
        menu.setHorizontalTextPosition(JMenu.RIGHT);
        menu.setHorizontalAlignment(JMenu.LEFT);
        menu.setIcon(getIcon());
        HelpCtx.setHelpIDString(menu, WorkspaceSwitchAction.class.getName());

        final WindowManager pool = WindowManager.getDefault();

        final Hashtable<ActionListener, Workspace> menu2Workspace = new Hashtable<>(10);

        // ^ maps listener on workspace
        final Hashtable<Workspace, JRadioButtonMenuItem> workspace2Menu = new Hashtable<>(10);

        // ^ maps workspace to menuitem
        final Hashtable<Workspace, ActionListener> workspace2Listener = new Hashtable<>(10);

        // ^ maps workspace to action listener
        final Workspace[] currentDeskRef = new Workspace[1];
        currentDeskRef[0] = pool.getCurrentWorkspace();

        // attach all workspaces
        Workspace[] workspaces = pool.getWorkspaces();

        for (int i = 0; i < workspaces.length; i++) {
            attachWorkspace(workspaces[i], currentDeskRef, workspace2Menu, menu2Workspace, workspace2Listener, menu);
        }

        // check on currently active workspace
        JRadioButtonMenuItem curItem = workspace2Menu.get(currentDeskRef[0]);

        if (curItem != null) {
            curItem.setSelected(true);
        }

        // listen to the changes in workspaces
        pool.addPropertyChangeListener(
            getWorkspacePoolListener(workspace2Menu, menu2Workspace, workspace2Listener, currentDeskRef, menu)
        );

        return menu;
    }

    /** Not implemented. May only be used in a menu presenter, with the children performing the action. */
    public void performAction() {
        assert false;
    }

    /** creates new actionlistener for given menuitem */
    private java.awt.event.ActionListener createActionListener(
        final JRadioButtonMenuItem menuItem, final Workspace[] currentDeskRef, final Hashtable menu2Workspace,
        final Hashtable workspace2Menu
    ) {
        return new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    Workspace desk = (Workspace) menu2Workspace.get(this);

                    if (desk == null) {
                        return;
                    }

                    if (workspace2Menu.get(desk) == null) {
                        return;
                    }

                    ((JRadioButtonMenuItem) workspace2Menu.get(desk)).setSelected(true);

                    if (desk == currentDeskRef[0]) {
                        return;
                    }

                    // deactivate old if present
                    if (currentDeskRef[0] != null) {
                        ((JRadioButtonMenuItem) workspace2Menu.get(currentDeskRef[0])).setSelected(false);
                    }

                    currentDeskRef[0] = desk;
                    desk.activate();
                }
            };
    }

    /** creates propertychangelistener that listens on current workspace */
    private PropertyChangeListener getWorkspacePoolListener(
        final Hashtable workspace2Menu, final Hashtable menu2Workspace, final Hashtable workspace2Listener,
        final Workspace[] currentDeskRef, final JMenu menu
    ) {
        PropertyChangeListener pcl1 = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent che) {
                    if (che.getPropertyName().equals(WindowManager.PROP_CURRENT_WORKSPACE)) {
                        Workspace newDesk = (Workspace) che.getNewValue();

                        if (currentDeskRef[0] == newDesk) {
                            return;
                        }

                        JRadioButtonMenuItem menu2 = ((JRadioButtonMenuItem) workspace2Menu.get(currentDeskRef[0]));

                        if (menu2 != null) {
                            menu2.setSelected(false);
                        }

                        currentDeskRef[0] = newDesk;
                        menu2 = ((JRadioButtonMenuItem) workspace2Menu.get(newDesk));

                        if (menu2 != null) {
                            menu2.setSelected(true);
                        }
                    } else if (che.getPropertyName().equals(WindowManager.PROP_WORKSPACES)) {
                        Workspace[] newWorkspaces = (Workspace[]) che.getNewValue();
                        Workspace[] oldWorkspaces = (Workspace[]) che.getOldValue();

                        /*for (int i = 0; i < oldWorkspaces.length; i++) {
                          System.out.println ("Old Value["+i+"]= "+oldWorkspaces[i].getName());
                        }
                        for (int i = 0; i < newWorkspaces.length; i++) {
                          System.out.println ("New Value["+i+"]= "+newWorkspaces[i].getName());
                        }*/
                        List<Workspace> newList = Arrays.asList(newWorkspaces);
                        List<Workspace> oldList = Arrays.asList(oldWorkspaces);

                        // remove old
                        for (int i = 0; i < oldWorkspaces.length; i++) {
                            if (newList.indexOf(oldWorkspaces[i]) < 0) {
                                detachWorkspace(
                                    oldWorkspaces[i], workspace2Menu, menu2Workspace, workspace2Listener, menu
                                );
                            }
                        }

                        // attach new
                        for (int i = 0; i < newWorkspaces.length; i++) {
                            if (oldList.indexOf(newWorkspaces[i]) < 0) {
                                attachWorkspace(
                                    newWorkspaces[i], currentDeskRef, workspace2Menu, menu2Workspace, workspace2Listener,
                                    menu
                                );
                            }
                        }
                    }
                }
            };

        return pcl1;
    }

    /** Initializes listeners atc to the given workspace */
    void attachWorkspace(
        Workspace workspace, Workspace[] currentDeskRef, Hashtable workspace2Menu, Hashtable menu2Workspace,
        Hashtable workspace2Listener, JMenu menu
    ) {
        // bugfix #6116 - change from getName() to getDisplayName()
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem();
        Mnemonics.setLocalizedText(menuItem, workspace.getDisplayName());
        HelpCtx.setHelpIDString(menuItem, WorkspaceSwitchAction.class.getName());

        ActionListener listener = createActionListener(menuItem, currentDeskRef, menu2Workspace, workspace2Menu);
        menuItem.addActionListener(listener);
        menu2Workspace.put(listener, workspace);
        workspace2Listener.put(workspace, listener);
        workspace2Menu.put(workspace, menuItem);
        workspace.addPropertyChangeListener(createNameListener(menuItem));
        menu.add(menuItem);
    }

    /** Frees all listeners etc from given workspace. */
    void detachWorkspace(
        Workspace workspace, Hashtable workspace2Menu, Hashtable menu2Workspace, Hashtable workspace2Listener,
        JMenu menu
    ) {
        JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem) workspace2Menu.get(workspace);
        workspace2Menu.remove(workspace);
        menu2Workspace.remove(workspace2Listener.get(workspace));
        workspace2Listener.remove(workspace);
        menu.remove(menuItem);
    }

    /** creates new PropertyChangeListener that listens for "name" property... */

    // NOI18N
    private PropertyChangeListener createNameListener(final JRadioButtonMenuItem item) {
        return new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                    if (ev.getPropertyName().equals("name")) {
                        item.setText((String) ev.getNewValue());
                    }
                }
            };
    }
}
