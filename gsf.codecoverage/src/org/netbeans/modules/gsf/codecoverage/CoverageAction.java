/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.gsf.codecoverage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

// TODO  -  ShowMenu extends AbstractAction implements DynamicMenuContent {
// See ShowMenu.java in mercurial for simpler way to do this
public final class CoverageAction extends AbstractAction implements ContextAwareAction {
    private static final int ACTION_TOGGLE_COLLECT = 1;
    private static final int ACTION_TOGGLE_AGGREGATION = 2;
    private static final int ACTION_CLEAR_RESULTS = 3;
    private static final int ACTION_SHOW_REPORT = 4;
    private static final int ACTION_TOGGLE_EDITORBAR = 5;
    private Action configureAction;
    private Action[] extraActions;

    public CoverageAction(Action configureAction, Action[] extraActions) {
        super();
        this.configureAction = configureAction;
        this.extraActions = extraActions;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false : "Action should never be called without a context";
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextAction(actionContext, configureAction, extraActions);
    }

    /**
     * Create the submenu.
     */
    private static JMenu createMenu(Project project, Action configureAction, Action[] extraActions) {
        return new LazyMenu(project, configureAction, extraActions);
    }

    /** Build up a nested menu of migration tasks for the given project */
    static void buildMenu(JMenu menu, Project project, Action configureAction, Action[] extraActions) {
        boolean enabled = true;
        if (configureAction != null && configureAction.isEnabled()) {
            enabled = false;

            JMenuItem menuitem =
                    new JMenuItem((String) configureAction.getValue(Action.NAME));
            menuitem.addActionListener(configureAction);
            menu.add(menuitem);

            menu.addSeparator();
            // Disable all the other actions
            enabled = false;

        }
        CoverageManagerImpl manager = CoverageManagerImpl.getInstance();


        boolean selected = manager.isEnabled(project);
        JMenuItem menuitem = new JCheckBoxMenuItem(NbBundle.getMessage(CoverageAction.class, "LBL_CollectCoverageAction"), selected);
        menuitem.addActionListener(new CoverageItemHandler(project, ACTION_TOGGLE_COLLECT));
        if (!enabled) {
            menuitem.setEnabled(false);
        }
        menu.add(menuitem);

        CoverageProvider provider = CoverageManagerImpl.getProvider(project);
        if (provider == null) {
            return;
        }
        boolean on = provider.isEnabled();
        if (provider.supportsAggregation()) {
            menu.addSeparator();
            boolean aggregating = manager.isAggregating(project);
            menuitem = new JCheckBoxMenuItem(NbBundle.getMessage(CoverageAction.class, "LBL_AggregateResults"), aggregating);
            menuitem.addActionListener(new CoverageItemHandler(project, ACTION_TOGGLE_AGGREGATION));
            if (!enabled || !on) {
                menuitem.setEnabled(false);
            }
            menu.add(menuitem);
        }

        menuitem = new JMenuItem(NbBundle.getMessage(CoverageAction.class, "LBL_ClearResultsAction"));
        menuitem.addActionListener(new CoverageItemHandler(project, ACTION_CLEAR_RESULTS));
        if (!enabled || !on) {
            menuitem.setEnabled(false);
        }
        menu.add(menuitem);

        menu.addSeparator();

        menuitem = new JMenuItem(NbBundle.getMessage(CoverageAction.class,
                "LBL_ShowReportAction"));
        menuitem.addActionListener(new CoverageItemHandler(project, ACTION_SHOW_REPORT));
        //menuitem.setToolTipText(target.getDescription());
        if (!enabled || !on) {
            menuitem.setEnabled(false);
        }
        menu.add(menuitem);
        menu.addSeparator();

        menuitem = new JCheckBoxMenuItem(NbBundle.getMessage(CoverageAction.class, "LBL_ShowEditorBar"),
                manager.getShowEditorBar());
        menuitem.addActionListener(new CoverageItemHandler(project, ACTION_TOGGLE_EDITORBAR));
        if (!enabled || !on) {
            menuitem.setEnabled(false);
        }
        menu.add(menuitem);

        menu.add(menuitem);

        if (extraActions != null && extraActions.length > 0) {
            menu.addSeparator();
            for (Action action : extraActions) {
                String name = (String) action.getValue(Action.NAME);
                // JDK6 only - Action.SELECTED_KEY
                //Boolean sel = (Boolean) action.getValue(Action.SELECTED_KEY);
                String SELECTED_KEY = "SwingSelectedKey";
                Boolean sel = (Boolean) action.getValue(SELECTED_KEY);
                if (sel != null) { // NOI18N
                    menuitem = new JCheckBoxMenuItem(name, sel);
                } else {
                    menuitem = new JMenuItem(name);
                }
                menuitem.addActionListener(configureAction);
                if (!enabled || !on) {
                    menuitem.setEnabled(false);
                }
                menu.add(menuitem);
            }
        }
    }

    /**
     * The particular instance of this action for a given project.
     */
    private static final class ContextAction extends AbstractAction implements Presenter.Popup {
        private final Project project;
        private Action configureAction;
        private Action[] extraActions;

        public ContextAction(Lookup lkp, Action configureAction, Action[] extraActions) {
            super(NbBundle.getMessage(CoverageAction.class, "LBL_CodeCoverage"));
            this.configureAction = configureAction;
            this.extraActions = extraActions;

            Collection<? extends Project> apcs = lkp.lookupAll(Project.class);

            if (apcs.size() == 1) {
                project = apcs.iterator().next();
            } else {
                project = null;
            }

            super.setEnabled(project != null);
        }

        public void actionPerformed(ActionEvent e) {
            assert false : "Action should not be called directly";
        }

        public JMenuItem getPopupPresenter() {
            if (project != null) {
                return createMenu(project, configureAction, extraActions);
            } else {
                return new Actions.MenuItem(this, false);
            }
        }

        @Override
        public void setEnabled(boolean b) {
            assert false : "No modifications to enablement status permitted";
        }
    }

    private static final class LazyMenu extends JMenu {
        private final Project project;
        private boolean initialized = false;
        private Action configureAction;
        private Action[] extraActions;

        public LazyMenu(Project project, Action configureAction, Action[] extraActions) {
            super(NbBundle.getMessage(CoverageAction.class, "LBL_CodeCoverage"));
            this.project = project;
            this.configureAction = configureAction;
            this.extraActions = extraActions;
        }

        @Override
        public JPopupMenu getPopupMenu() {
            if (!initialized) {
                initialized = true;
                super.removeAll();

                buildMenu(this, project, configureAction, extraActions);
            }

            return super.getPopupMenu();
        }
    }

    /**
     * Action handler for a menu item representing one target.
     */
    private static final class CoverageItemHandler implements ActionListener {
        private final Project project;
        private final int action;

        public CoverageItemHandler(Project project, int action) {
            this.project = project;
            this.action = action;
        }

        public void actionPerformed(ActionEvent ev) {
            CoverageManagerImpl manager = CoverageManagerImpl.getInstance();
            switch (action) {
                case ACTION_TOGGLE_COLLECT: {
                    boolean enabled = ((JCheckBoxMenuItem) ev.getSource()).isSelected();
                    manager.setEnabled(project, enabled);
                    break;
                }
                case ACTION_TOGGLE_AGGREGATION: {
                    boolean enabled = ((JCheckBoxMenuItem) ev.getSource()).isSelected();
                    manager.setAggregating(project, enabled);
                    break;
                }
                case ACTION_SHOW_REPORT: {
                    manager.showReport(project);
                    break;
                }

                case ACTION_CLEAR_RESULTS: {
                    manager.clear(project);
                    break;
                }

                case ACTION_TOGGLE_EDITORBAR: {
                    boolean enabled = ((JCheckBoxMenuItem) ev.getSource()).isSelected();
                    manager.setShowEditorBar(enabled);
                    break;
                }
            }
        }
    }
}
