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

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;

/**
 * Action for "Select In (Projects | Files | Favorites)" item in matching
 * object's pop-up menu.
 *
 * @author jhavlin
 */
public class SelectInAction extends NodeAction implements Presenter.Popup {

    private static final String SEL_IN_PROJECTS
            = "org.netbeans.modules.project.ui.SelectInProjects";       //NOI18N
    private static final String SEL_IN_FILES
            = "org.netbeans.modules.project.ui.SelectInFiles";          //NOI18N
    private static final String SEL_IN_FAVS
            = "org.netbeans.modules.favorites.Select";                  //NOI18N

    @Override
    protected void performAction(Node[] activatedNodes) {
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes.length == 1
                && (getAction(SEL_IN_FILES) != null
                || getAction(SEL_IN_PROJECTS) != null
                || getAction(SEL_IN_FAVS) != null);
    }

    @NbBundle.Messages("SelectInAction.name=Select In")
    @Override
    public String getName() {
        return Bundle.SelectInAction_name();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @NbBundle.Messages({
        "SelectIn.Projects=Projects",
        "SelectIn.Files=Files",
        "SelectIn.Favorites=Favorites"
    })
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu submenu = new JMenu(this);
        addActionItem(submenu, SEL_IN_PROJECTS, Bundle.SelectIn_Projects());
        addActionItem(submenu, SEL_IN_FILES, Bundle.SelectIn_Files());
        addActionItem(submenu, SEL_IN_FAVS, Bundle.SelectIn_Favorites());
        return submenu;
    }

    /**
     * Add item for an action into a submenu (it the action exists).
     *
     * @param parent Parent to add the new item into.
     * @param action ID of the action for {@link #getAction(String)}
     * @param displayName Display name for the action.
     */
    private void addActionItem(JMenu parent,
            String action, String displayName) {

        Action a = getAction(action);
        if (a != null) {
            JMenuItem item = new JMenuItem(a);
            item.setText(displayName);
            item.setIcon(null);
            parent.add(item);
        }
    }

    /**
     * Get action by id in "Window/SelectDocumentNode" category.
     */
    private Action getAction(String name) {
        return Actions.forID("Window/SelectDocumentNode", name);        //NOI18N
    }
}
