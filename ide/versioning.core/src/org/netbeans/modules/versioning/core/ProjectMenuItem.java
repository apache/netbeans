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

import java.awt.EventQueue;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.awt.DynamicMenuContent;
import org.openide.windows.TopComponent;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider.VersioningSystem;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.diff.PatchAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;

/**
 * Appears in a project's popup menu.
 * 
 * @author Maros Sandor
 */
public class ProjectMenuItem extends AbstractAction implements Presenter.Popup {
    
    private static final Logger LOG = Logger.getLogger(ProjectMenuItem.class.getName());
    private static final boolean SYNC_MENU = Boolean.getBoolean("versioning.syncpopupmenu"); //NOI18N

    @Override
    public JMenuItem getPopupPresenter() {
        return new DynamicDummyItem();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // dummy, not used
    }

    private JComponent [] createItems() {
        List<JComponent> popups = new ArrayList<JComponent>();            
        if(!VersioningManager.isInitialized()) {            
            popups.add(NoVCSMenuItem.createInitializingMenu(NbBundle.getMessage(VersioningMainMenu.class, "CTL_MenuItem_VersioningMenu")));            
            popups.add(NoVCSMenuItem.createInitializingMenu(NbBundle.getMessage(VersioningMainMenu.class, "CTL_MenuItem_LocalHistory")));            
        } else {
            Node [] nodes = getActivatedNodes();
            if (nodes.length > 0) {
                Set<VCSFileProxy> rootFiles = getRootFilesForProjectNodes(nodes);
                Set<VersioningSystem> owners = getOwnersForProjectNodes(rootFiles);
                if (owners.size() != 1) {
                    return new JComponent[0];
                }
                VersioningSystem owner = owners.iterator().next();
                VersioningSystem localHistory = getLocalHistory(rootFiles);

                if (owner == null || owner.getVCSAnnotator() != null) {
                    // prepare a lazy menu, it's items will be properly created at the time the menu is expanded
                    JMenu menu = new LazyMenu(nodes, owner);
                    popups.add(menu);
                }
                if(localHistory != null && localHistory.getVCSAnnotator() != null) {
                    // prepare a lazy menu for the local history, it's items will be properly created at the time the menu is expanded
                    JMenu menu = new LazyMenu(nodes, localHistory);
                    popups.add(menu);
                }
            }
        }
        return popups.toArray(new JComponent[0]);        
    }

    private VersioningSystem getLocalHistory (Set<VCSFileProxy> rootFiles) {
        VersioningSystem owner = null;
        for (VCSFileProxy file : rootFiles) {
            VersioningSystem fileOwner = VersioningManager.getInstance().getLocalHistory(file);
            if (owner != null) {
                if (fileOwner != null && fileOwner != owner) return null;
            } else {
                owner = fileOwner;
            }
        }
        return owner;
    }
    
    private Set<VersioningSystem> getOwnersForProjectNodes (Set<VCSFileProxy> rootFiles) {
        Set<VersioningSystem> owners = new HashSet<VersioningSystem>(2);
        boolean someUnversioned = false;
        for (VCSFileProxy file : rootFiles) {
            VersioningSystem fileOwner = VersioningManager.getInstance().getOwner(file);
            if (fileOwner == null) {
                // some root file is unversioned
                someUnversioned = true;
            } else {
                owners.add(fileOwner);
            }
        }
        if (owners.isEmpty() && someUnversioned) {
            // all rootfiles were unversioned, return a null owner for them
            owners.add(null);
        }
        return owners;
    }
    
    private Action [] createVersioningSystemActions (VersioningSystem vs, Node[] nodes, boolean displayConnectAction) {
        VCSContext ctx = VCSContext.forNodes(nodes);
        Action [] actions = null;
        if (displayConnectAction && ctx.getRootFiles().size() == 1) {
            // we have only one root. If it's disconnected, display only the Connect action instead of other actions (import, init etc. do not make sense)
            VCSFileProxy root = vs.getTopmostManagedAncestor(ctx.getRootFiles().iterator().next());
            if (root != null) {
                if (VersioningConfig.getDefault().isDisconnected(vs, root)) {
                    // repository is indeed disconnected, display only Connect action
                    String displayName = vs.getMenuLabel();
                    actions = new Action[] { new VersioningMainMenu.ConnectAction(vs, root, NbBundle.getMessage(ProjectMenuItem.class, "CTL_ConnectAction.name.vcs", displayName)) }; //NOI18N
                }
            }
        }
        if (actions == null) {
            // repository is connected or the context not yet versioned
            // see issue #231229    
//            if (vs instanceof DelegatingVCS) {
//                actions = ((DelegatingVCS) vs).getInitActions(ctx);
//            } else {
                VCSAnnotator an = vs.getVCSAnnotator();
                if (an == null) return null; 
                actions = an.getActions(ctx, VCSAnnotator.ActionDestination.PopupMenu);
//            }
        }
        return actions;
    }

    private JMenuItem createmenuItem(Action action) {
        JMenuItem item;
        if (action instanceof Presenter.Menu) {
            item = ((Presenter.Menu) action).getMenuPresenter();
        } else if (action instanceof Presenter.Popup) {
            item = ((Presenter.Popup) action).getPopupPresenter();
        } else {
            item = new JMenuItem();
            Actions.connect(item, action, true);
        }
        return item;
    }

    private Node[] getActivatedNodes() {
        return TopComponent.getRegistry().getActivatedNodes();
    }

    private class DynamicDummyItem extends JMenuItem implements DynamicMenuContent {
        @Override
        public JComponent[] getMenuPresenters() {
            return createItems();
        }

        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return createItems();
        }
    }

    private Set<VCSFileProxy> getRootFilesForProjectNodes (Node[] nodes) {
        Set<VCSFileProxy> rootFiles = new HashSet<VCSFileProxy>(nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            Project project =  node.getLookup().lookup(Project.class);
            if (project != null) {
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                for (int j = 0; j < sourceGroups.length; j++) {
                    SourceGroup sourceGroup = sourceGroups[j];
                    FileObject srcRootFo = sourceGroup.getRootFolder();
                    VCSFileProxy rootFile = VCSFileProxy.createFileProxy(srcRootFo);
                    if (rootFile == null) {
                        continue;
                    }
                    rootFiles.add(rootFile);
                }
                continue;
            }
        }
        return rootFiles;
    }

    /**
     * Items for this popup menu are created when really needed, that is at the time when the menu is expanded.
     */
    private class LazyMenu extends JMenu {
        private final Node[] nodes;
        private final VersioningSystem owner;
        boolean initialized; // create only once, prevents recreating items when user repeatedly expends and collapses the menu

        private LazyMenu(Node[] nodes, VersioningSystem owner) {
            // owner == null ? 'default versioning menu' : 'specific menu of a versioning system'
            super(owner == null ? NbBundle.getMessage(ProjectMenuItem.class, "CTL_MenuItem_VersioningMenu") : Utils.getSystemMenuName(owner));
            this.nodes = nodes;
            this.owner = owner;
        }

        @Override
        @NbBundle.Messages("LBL_ProjectPopupMenu_Initializing=Initializing...")
        public JPopupMenu getPopupMenu() {
            if (nodes != null && !initialized) {
                initialized = true;
                if (SYNC_MENU) {
                    Action[] actions = getActions();
                    addVersioningSystemItems(actions);
                    if (owner == null) {
                        addNoVCSMenu(actions);
                    }
                } else {
                    // clear created items
                    super.removeAll();
                    JMenuItem item = new JMenuItem(Bundle.LBL_PopupMenu_Initializing());
                    item.setEnabled(false);
                    add(item);
                    Utils.postParallel(new Runnable() {
                        @Override
                        public void run () {
                            final Action[] actions = getActions();
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run () {
                                    JPopupMenu popup = getPopupMenu();
                                    boolean display = popup.isVisible();
                                    popup.setVisible(false);
                                    removeAll();
                                    if (isShowing()) {
                                        addVersioningSystemItems(actions);
                                        if (owner == null) {
                                            addNoVCSMenu(actions);
                                        }
                                        popup.setVisible(display);
                                    }
                                }
                            });
                        }
                    });
                }
            }
            return super.getPopupMenu();
        }

        private void addNoVCSMenu (Action[] actions) throws MissingResourceException {
            if (actions != null && actions.length > 0) {
                addSeparator();
                add(createmenuItem(SystemAction.get(PatchAction.class)));
            } else {
                JMenuItem item = new JMenuItem();
                Mnemonics.setLocalizedText(item, NbBundle.getMessage(VersioningMainMenu.class, "LBL_NoneAvailable"));  // NOI18N
                item.setEnabled(false);
                add(item);
            }
        }

        private boolean addVersioningSystemItems (Action[] actions) {
            if (actions != null && actions.length > 0) {
                for (Action action : actions) {
                    if (action == null) {
                        add(Utils.createJSeparator());
                    } else {
                        add(createmenuItem(action));
                    }
                }
                return true;
            }
            return false;
        }

        private Action[] getActions () {
            Action[] actions;
            if (owner == null) {
                // default Versioning menu (Import into...)
                List<VersioningSystem> vcs = new ArrayList<VersioningSystem>(Arrays.asList(VersioningManager.getInstance().getVersioningSystems()));
                vcs.sort(new VersioningMainMenu.ByDisplayNameComparator());
                List<Action> allvsActions = new ArrayList<Action>(50);
                for (VersioningSystem vs : vcs) {
                    if (vs.isLocalHistory()) {
                        continue;
                    }
                    Action[] vsActions = createVersioningSystemActions(vs, nodes, true);
                    if (vsActions != null) {
                        allvsActions.addAll(Arrays.asList(vsActions));
                    }
                }
                actions = allvsActions.toArray(new Action[0]);
            } else {
                // specific versioning system menu
                actions = createVersioningSystemActions(owner, nodes, false);
            }
            return actions;
        }
    }
}
