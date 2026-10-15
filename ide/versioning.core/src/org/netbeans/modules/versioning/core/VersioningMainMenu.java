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
package org.netbeans.modules.versioning.core;

import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.windows.TopComponent;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.core.spi.VCSContext;

import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import java.awt.event.ActionEvent;
import java.util.*;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Top level main Versioning menu.
 * 
 * @author Maros Sandor
 */
public class VersioningMainMenu extends AbstractAction implements DynamicMenuContent {

    @Override
    public void actionPerformed(ActionEvent e) {
        // does nothing, this is a popup menu
    }

    @Override
    public JComponent[] getMenuPresenters() {
        return createMenu();
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] items) {
        return createMenu();
    }
    
    @NbBundle.Messages("CTL_OtherVCS.menu=Other &VCS")
    private JComponent[] createMenu() {
        List<JComponent> items = new ArrayList<JComponent>(20);
        if(!VersioningManager.isInitialized()) {
            items.add(NoVCSMenuItem.createInitializingMenu(NbBundle.getMessage(VersioningMainMenu.class, "CTL_MenuItem_VersioningMenu")));
            items.add(Utils.createJSeparator());
            items.add(NoVCSMenuItem.createInitializingMenu(NbBundle.getMessage(VersioningMainMenu.class, "CTL_MenuItem_LocalHistory")));
        } else {
        
            final VCSContext ctx = VCSContext.forNodes(TopComponent.getRegistry().getActivatedNodes());
            List<VersioningSystem> systems = Arrays.asList(VersioningManager.getInstance().getVersioningSystems());
            VersioningSystem [] vs = VersioningManager.getInstance().getOwners(ctx);

            Collection<? extends Action> additionalItems = Lookups.forPath("Actions/Versioning/Additional").lookupAll(Action.class);
            boolean ownerMenuDisplayed = false;
            if (vs.length == 1) {
                if (vs[0].getVCSAnnotator() != null) {
                    List<JComponent> systemItems = actionsToItems(vs[0].getVCSAnnotator().getActions(ctx, VCSAnnotator.ActionDestination.MainMenu));
                    items.addAll(systemItems);
                    ownerMenuDisplayed = true;
                    items.add(Utils.createJSeparator());
                }
                for (Action a : additionalItems) {
                    items.add(Utils.toMenuItem(a));
                }
                items.addAll(actionsToItems(appendAdditionalActions(ctx, vs[0], new Action[0])));
            } else if (vs.length > 1) {
                JMenuItem dummy = new JMenuItem("<multiple systems>");
                dummy.setEnabled(false);
                items.add(dummy);
                for (Action a : additionalItems) {
                    items.add(Utils.toMenuItem(a));
                }
            } else {
                for (Action a : additionalItems) {
                    items.add(Utils.toMenuItem(a));
                }
            }
            items.add(Utils.createJSeparator());

            systems.sort(new ByDisplayNameComparator());

            VersioningSystem localHistory = null;
            boolean accepted = false;
            List<JMenu> vcsSubmenus = new ArrayList<JMenu>(systems.size());
            for (final VersioningSystem system : systems) {
                if(!system.accept(ctx) || vs.length == 1 && vs[0] == system) { // skip already displayed VCS
                    continue;
                }
                if (system.isLocalHistory()) {
                    localHistory = system;
                } else if (!"".equals(system.getMenuLabel())) { //NOI18N
                    accepted = true;
                    JMenu menu = createVersioningSystemMenu(system, true);
                    vcsSubmenus.add(menu);
                }
            }
            if (ownerMenuDisplayed) {
                JMenu menu = new JMenu(Bundle.CTL_OtherVCS_menu());
                Mnemonics.setLocalizedText(menu, Bundle.CTL_OtherVCS_menu());
                for (JMenu submenu : vcsSubmenus) {
                    menu.add(submenu);
                }
                vcsSubmenus.clear();
                vcsSubmenus.add(menu);
            }
            items.addAll(vcsSubmenus);
            accepted |= ownerMenuDisplayed;
            if(!accepted) {
                items.add(NoVCSMenuItem.createNoVcsMenu(NbBundle.getMessage(VersioningMainMenu.class, "CTL_MenuItem_VersioningMenu")));
                return items.toArray(new JComponent[0]);
            }

            if (localHistory != null) {
                items.add(Utils.createJSeparator());
                items.add(createVersioningSystemMenu(localHistory, false));
            }
        }
        return items.toArray(new JComponent[0]);
    }

    private JMenu createVersioningSystemMenu(final VersioningSystem system, final boolean isRegularVCS) {
        final JMenu menu = new JMenu();
        String menuText = 
                VersioningManager.getInstance().isLocalHistory(system) ? 
                NbBundle.getMessage(VersioningMainMenu.class, "CTL_LocalHistoryMenuNameLoc") : 
                system.getMenuLabel();
        Mnemonics.setLocalizedText(menu, menuText);
        menu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                if (menu.getItemCount() != 0) return;
                // context should be cached while the menu is displayed
                VCSContext ctx = VCSContext.forNodes(TopComponent.getRegistry().getActivatedNodes());
                constructMenu(menu, system, ctx, isRegularVCS);
            }
    
            @Override
            public void menuDeselected(MenuEvent e) {
            }
    
            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
        return menu;
    }

    private void constructMenu (JMenu menu, VersioningSystem system, VCSContext ctx, boolean isRegularVCS) {
        Action[] actions = null;
        if (system.getVCSAnnotator() != null) {
            actions = system.getVCSAnnotator().getActions(ctx, VCSAnnotator.ActionDestination.MainMenu);
        }
        if (isRegularVCS) {
            actions = appendAdditionalActions(ctx, system, actions);
        }
        if(actions != null && actions.length > 0) {
            List<JComponent> systemItems = actionsToItems(actions);
            for (JComponent systemItem : systemItems) {
                menu.add(systemItem);
            }
        }
    }

    private static List<JComponent> actionsToItems(Action[] actions) {
        List<JComponent> items = new ArrayList<JComponent>(actions.length);
        for (Action action : actions) {
            if (action == null) {
                items.add(Utils.createJSeparator());
            } else {
                if (action instanceof DynamicMenuContent) {
                    DynamicMenuContent dmc = (DynamicMenuContent) action;
                    JComponent [] components = dmc.getMenuPresenters();
                    items.addAll(Arrays.asList(components));
                } else {
                    JMenuItem item = Utils.toMenuItem(action);
                    items.add(item);
                }
            }
        }
        return items;
    }

    static final class ByDisplayNameComparator implements Comparator<VersioningSystem> {
        @Override
        public int compare(VersioningSystem a, VersioningSystem b) {
            return a.getDisplayName().compareTo(b.getDisplayName());
        }
    }
    
    static class ConnectAction extends AbstractAction {
        private final VCSFileProxy root;
        private final VersioningSystem vs;

        public ConnectAction (VersioningSystem vs, VCSFileProxy root, String name) {
            super(name == null ? NbBundle.getMessage(VersioningMainMenu.class, "CTL_ConnectAction.name") : name); //NOI18N
            this.vs = vs;
            this.root = root;
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            VersioningConfig.getDefault().connectRepository(vs, root);
            VersioningManager.getInstance().versionedRootsChanged();
        }
    }

    // should be available only from the main menu
    private static class DisconnectAction extends AbstractAction {
        private final VCSFileProxy root;
        private final VersioningSystem vs;

        public DisconnectAction (VersioningSystem vs, VCSFileProxy root) {
            super(NbBundle.getMessage(VersioningMainMenu.class, "CTL_DisconnectAction.name")); //NOI18N
            this.vs = vs;
            this.root = root;
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(VersioningMainMenu.class, "MSG_ConnectAction.confirmation.text", new Object[] { root.getName(), vs.getDisplayName() }), //NOI18N
                    NbBundle.getMessage(VersioningMainMenu.class, "LBL_ConnectAction.confirmation.title"), //NOI18N
                    NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
            if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
                VersioningConfig.getDefault().disconnectRepository(vs, root);
                VersioningManager.getInstance().versionedRootsChanged();
            }
        }
    }

    /**
     * appends connect/disconnect actions to given actions
     * @param ctx
     * @param system
     * @param actions initial actions
     * @return enhanced actions
     */
    private Action[] appendAdditionalActions (VCSContext ctx, VersioningSystem system, Action[] actions) {
        if (ctx.getRootFiles().size() == 1) {
            // can connect or disconnect just one root
            VCSFileProxy root = system.getTopmostManagedAncestor(ctx.getRootFiles().iterator().next());
            if (root != null) {
                Action a;
                // adding connect/disconnect actions to the main menu
                if (VersioningConfig.getDefault().isDisconnected(system, root)) {
                    actions = new Action[] { new ConnectAction(system, root, null) };
                } else {
                    actions = actions == null ? new Action[2] : Arrays.copyOf(actions, actions.length + 1);
                    actions[actions.length - 1] = new DisconnectAction(system, root);
                }
            }
        }
        return actions;
    }
}
