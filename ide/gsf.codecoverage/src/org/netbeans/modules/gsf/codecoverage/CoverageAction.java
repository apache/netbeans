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
    private final Action configureAction;
    private final Action[] extraActions;

    public CoverageAction(Action configureAction, Action[] extraActions) {
        super();
        this.configureAction = configureAction;
        this.extraActions = extraActions;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false : "Action should never be called without a context";
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextAction(actionContext, configureAction, extraActions);
    }

    /**
     * Create the submenu.
     */
    private static JMenu createMenu(Project project, Action configureAction, Action[] extraActions) {
        return new LazyMenu(project, configureAction, extraActions);
    }

    /**
     * Build up a nested menu of migration tasks for the given project
     */
    static void buildMenu(JMenu menu, Project project, Action configureAction, Action[] extraActions) {
        boolean enabled = true;
        if (configureAction != null && configureAction.isEnabled()) {
            enabled = false;

            JMenuItem menuitem
                = new JMenuItem((String) configureAction.getValue(Action.NAME));
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
        private final Action configureAction;
        private final Action[] extraActions;

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

        @Override
        public void actionPerformed(ActionEvent e) {
            assert false : "Action should not be called directly";
        }

        @Override
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
        private final Action configureAction;
        private final Action[] extraActions;

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

        @Override
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
