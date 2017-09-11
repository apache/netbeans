/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
