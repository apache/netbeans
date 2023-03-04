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
package org.netbeans.modules.php.dbgp.models;

import java.awt.Dialog;
import javax.swing.Action;
import org.netbeans.modules.php.dbgp.models.nodes.VariableNode;
import org.netbeans.modules.php.dbgp.ui.LocalFilterPanel;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.text.Line;
import org.openide.util.NbBundle;

/**
 * @author ads
 *
 */
public class LocalViewActionProvider implements NodeActionsProviderFilter {

    private static final String FILTERS_LABEL = "CTL_Filters_Label";    // NOI18N

    private static final String GO_TO_SOURCE_LABEL
        = "CTL_Breakpoint_GoToSource_Label";                            // NOI18N

    private static final String FIXED_WATCH_LABEL
        = "CTL_Create_Fixed_Watch";                                     // NOI18N

    private static final String DIALOG_TITLE   = "TTL_LocalFilter";     // NOI18N

    @Override
    public Action[] getActions ( NodeActionsProvider original , Object node )
            throws UnknownTypeException
    {
        Action[] actions;
        try {
            actions = original.getActions(node);
        }
        catch ( UnknownTypeException exception ) {
            actions = new Action[0];
        }
        //makes little sense goto source action to me - disabled (#159550)
        /*if(node instanceof VariableNode) {
            // TODO : add new action : create fixed watch
            Action[] newActions = new Action [actions.length + 2];
            newActions [0] = GO_TO_SOURCE_ACTION;
            newActions [1] = null;
            System.arraycopy (actions, 0, newActions, 2, actions.length);
            actions = newActions;
        }*/
        Action[] newActions = new Action[ actions.length + 1];
        newActions [0] = EDIT_FILTERS_ACTION;
        System.arraycopy (actions, 0, newActions, 1, actions.length);
        actions = newActions;
        return actions;
    }

    @Override
    public void performDefaultAction( NodeActionsProvider original , Object node)
        throws UnknownTypeException
    {
        //makes little sense goto source action to me - disabled (#159550)
        /*if (node instanceof VariableNode) {
            goToSource((VariableNode) node);
        } else {
            original.performDefaultAction(node);
        }*/
    }

    private static void goToSource(VariableNode node ) {
        Line line = node.findDeclarationLine();
        if (line != null) {
            line.show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS);
        }
    }

    private static void editFilters(){
        LocalFilterPanel panel = new LocalFilterPanel();
        DialogDescriptor descriptor = new DialogDescriptor(panel,
                NbBundle.getMessage(LocalViewActionProvider.class, DIALOG_TITLE));
            Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setVisible(true);
            dialog.dispose();

            if (descriptor.getValue() != org.openide.DialogDescriptor.OK_OPTION) {
                return;
            }
            VariablesModelFilter.setFilters( panel.getSelectedTypes() );
    }


    private static void createFixedWatch(  Object[] nodes ) {
        // TODO Auto-generated method stub

    }

    private static final Action GO_TO_SOURCE_ACTION = Models.createAction(
            NbBundle.getBundle(LocalViewActionProvider.class).getString(
                    GO_TO_SOURCE_LABEL),
            new GoToSourcePerformer(),
            Models.MULTISELECTION_TYPE_EXACTLY_ONE
        );

    private static final Action FIXED_WATCH_ACTION = Models.createAction(
            NbBundle.getBundle(LocalViewActionProvider.class).getString(
                    FIXED_WATCH_LABEL),
                    new Models.ActionPerformer () {
                        @Override
                        public boolean isEnabled(Object node) {
                            return true;
                        }
                        @Override
                        public void perform(Object[] nodes) {
                            createFixedWatch( nodes );
                        }
                    },
                    Models.MULTISELECTION_TYPE_EXACTLY_ONE
        );

    private static final Action EDIT_FILTERS_ACTION = Models.createAction (
            NbBundle.getBundle(LocalViewActionProvider.class).getString(
                    FILTERS_LABEL),
                    new Models.ActionPerformer () {
                        @Override
                        public boolean isEnabled(Object node) {
                            return true;
                        }
                        @Override
                        public void perform(Object[] nodes) {
                            editFilters();
                        }
                },
                Models.MULTISELECTION_TYPE_ANY
        );

    private static class GoToSourcePerformer implements Models.ActionPerformer {

        /* (non-Javadoc)
         * @see org.netbeans.spi.viewmodel.Models.ActionPerformer#isEnabled(java.lang.Object)
         */
        @Override
        public boolean isEnabled( Object arg ) {
            return true;
        }

        /* (non-Javadoc)
         * @see org.netbeans.spi.viewmodel.Models.ActionPerformer#perform(java.lang.Object[])
         */
        @Override
        public void perform( Object[] nodes ) {
            goToSource((VariableNode) nodes [0]);
        }

    }

}
