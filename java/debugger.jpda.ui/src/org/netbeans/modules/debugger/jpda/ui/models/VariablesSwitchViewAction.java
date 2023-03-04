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
package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                             types={NodeActionsProviderFilter.class},
                             position=20000)
public class VariablesSwitchViewAction implements NodeActionsProviderFilter {
    
    static final String ID = "LocalsView";
    static final String treeNodeFormat =
            "{DefaultLocalsColumn} = ({"+Constants.LOCALS_TYPE_COLUMN_ID+"}) "+"{"+Constants.LOCALS_VALUE_COLUMN_ID+"}"; // NOI18N
    private static final String VIEW_PREFERENCES_NAME = "view_preferences"; // NOI18N
    private static final String VIEW_TYPE = "view_type";                    // NOI18N
    private static final String VIEW_TREE_DISPLAY_FORMAT = "view_tree_display_format"; // NOI18N
    private static final String VIEW_TYPE_TABLE = "table";                  // NOI18N
    private static final String VIEW_TYPE_TREE = "tree";                    // NOI18N
    private static final String IS_SHOW_WATCHES = "show_watches"; // NOI18N
    private static final String IS_SHOW_EVALUTOR_RESULT = "show_evaluator_result"; // NOI18N
    private static final String VAR_PREFERENCES_NAME = "variables_view"; // NOI18N
    
    private static final Map<String, SwitchViewAction> switchActions = new HashMap<String, SwitchViewAction>();

    private Action switchViewAction;

    @Override
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        original.performDefaultAction(node);
    }

    @Override
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        Action[] actions = original.getActions(node);
        int n = actions.length;
        Action[] newActions = new Action[n+1];
        System.arraycopy(actions, 0, newActions, 0, n);
        if (switchViewAction == null) {
            switchViewAction = getSwitchViewAction();
        }
        newActions[n] = switchViewAction;
        return newActions;
    }
    
    static Action getSwitchViewAction() {
        return getSwitchViewAction(ID, treeNodeFormat);
    }

    private static boolean isWatchesViewNested() {
        Preferences preferences = NbPreferences.forModule(ContextProvider.class).node(VAR_PREFERENCES_NAME); // NOI18N
        return preferences.getBoolean(IS_SHOW_WATCHES, true);
    }

    private static boolean isResultsViewNested() {
        Preferences preferences = NbPreferences.forModule(ContextProvider.class).node(VAR_PREFERENCES_NAME); // NOI18N
        return preferences.getBoolean(IS_SHOW_EVALUTOR_RESULT, true);
    }

    static synchronized Action getSwitchViewAction(String viewId, String treeNodeFormat) {
        SwitchViewAction a = switchActions.get(viewId);
        if (a == null) {
            a = new SwitchViewAction(viewId, treeNodeFormat);
            switchActions.put(viewId, a);
        }
        return a;
    }
    
    private static synchronized SwitchViewAction getSwitchViewAction(String viewId) {
        SwitchViewAction a = switchActions.get(viewId);
        return a;
    }
    
    private static final class SwitchViewAction extends AbstractAction implements Presenter.Popup {
        
        private String id;
        private String treeNodeFormat;
        private Preferences preferences;
        
        SwitchViewAction(String id, String treeNodeFormat) {
            this.id = id;
            this.treeNodeFormat = treeNodeFormat;
            preferences = NbPreferences.forModule(ContextProvider.class).node(VIEW_PREFERENCES_NAME).node(id); // NOI18N
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            String type = preferences.get(VIEW_TYPE, null);
            String toType;
            if (type == null || type.equals(VIEW_TYPE_TABLE)) {
                toType = VIEW_TYPE_TREE;
                preferences.put(VIEW_TYPE, toType);
                preferences.put(VIEW_TREE_DISPLAY_FORMAT, treeNodeFormat);
            } else {
                toType = VIEW_TYPE_TABLE;
                preferences.put(VIEW_TYPE, toType);
                preferences.remove(VIEW_TREE_DISPLAY_FORMAT);
            }
            checkNested(toType);
        }
        
        private void onViewAs(String type, boolean checkForNested) {
            preferences.put(VIEW_TYPE, type);
            if (type.equals(VIEW_TYPE_TREE)) {
                preferences.put(VIEW_TREE_DISPLAY_FORMAT, treeNodeFormat);
            } else {
                preferences.remove(VIEW_TREE_DISPLAY_FORMAT);
            }
            if (checkForNested) {
                checkNested(type);
            }
        }
        
        private void checkNested(String type) {
            if (id.equals(WatchesSwitchViewAction.ID) && isWatchesViewNested()) {
                ((SwitchViewAction) VariablesSwitchViewAction.getSwitchViewAction()).onViewAs(type, false);
                if (isResultsViewNested()) {
                    ((SwitchViewAction) ResultsSwitchViewAction.getSwitchViewAction()).onViewAs(type, false);
                }
            }
            if (id.equals(ResultsSwitchViewAction.ID) && isResultsViewNested()) {
                ((SwitchViewAction) VariablesSwitchViewAction.getSwitchViewAction()).onViewAs(type, false);
                if (isWatchesViewNested()) {
                    ((SwitchViewAction) WatchesSwitchViewAction.getSwitchViewAction()).onViewAs(type, false);
                }
            }
            if (id.equals(VariablesSwitchViewAction.ID)) {
                if (isWatchesViewNested()) {
                    ((SwitchViewAction) WatchesSwitchViewAction.getSwitchViewAction()).onViewAs(type, false);
                }
                if (isResultsViewNested()) {
                    ((SwitchViewAction) ResultsSwitchViewAction.getSwitchViewAction()).onViewAs(type, false);
                }
            }
        }
        
        @Override
        public JMenuItem getPopupPresenter() {
            JMenu viewAsPopup = new JMenu(NbBundle.getMessage(SwitchViewAction.class, "CTL_ViewAs_Popup"));
            JRadioButtonMenuItem tableView = new ViewAsMenuItem(VIEW_TYPE_TABLE);
            JRadioButtonMenuItem treeView = new ViewAsMenuItem(VIEW_TYPE_TREE);
            String type = preferences.get(VIEW_TYPE, null);
            if (type == null || type.equals(VIEW_TYPE_TABLE)) {
                tableView.setSelected(true);
            } else {
                treeView.setSelected(true);
            }
            viewAsPopup.add(tableView);
            viewAsPopup.add(treeView);
            return viewAsPopup;
        }
        
        private class ViewAsMenuItem extends JRadioButtonMenuItem {

            public ViewAsMenuItem(final String type) {
                super(new AbstractAction(NbBundle.getMessage(NumericDisplayFilter.class, "CTL_View_"+type)) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            onViewAs(type, true);
                        }
                    });
            }

        }

    };
        
    
    
}
