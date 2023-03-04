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

import javax.swing.Action;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                             types={NodeActionsProviderFilter.class},
                             position=20000)
public class ResultsSwitchViewAction implements NodeActionsProviderFilter {
    
    static final String ID = "ResultsView";
    private static final String treeNodeFormat =
            "{DefaultLocalsColumn} = ({"+Constants.LOCALS_TYPE_COLUMN_ID+"}) "+"{"+Constants.LOCALS_VALUE_COLUMN_ID+"}"; // NOI18N
    
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
        return VariablesSwitchViewAction.getSwitchViewAction(ID, treeNodeFormat);
    }

}
