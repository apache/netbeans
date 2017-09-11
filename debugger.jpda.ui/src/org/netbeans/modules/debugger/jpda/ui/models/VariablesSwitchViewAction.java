/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
    
    private final static class SwitchViewAction extends AbstractAction implements Presenter.Popup {
        
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
