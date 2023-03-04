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
package org.netbeans.modules.groovy.debug;

import javax.swing.Action;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle.Messages;

/**
 * @author Martin Grebac
 * @author Martin Adamek
 */
@DebuggerServiceRegistration(path="BreakpointsView", types=NodeActionsProviderFilter.class)
public class GroovyBreakpointActionsProvider implements NodeActionsProviderFilter {

    @Messages("LBL_Action_Go_To_Source=Go to Source")
    private static final Action GO_TO_SOURCE_ACTION = Models.createAction(
            Bundle.LBL_Action_Go_To_Source(),
            new Models.ActionPerformer() {
                @Override
                public boolean isEnabled(Object node) {
                    return true;
                }

                @Override
                public void perform(Object[] nodes) {
                    goToSource((LineBreakpoint) nodes[0]);
                }
            },
            Models.MULTISELECTION_TYPE_EXACTLY_ONE);
    
    @Messages("LBL_Action_Customize=Customize")
    private static final Action CUSTOMIZE_ACTION = Models.createAction(
            Bundle.LBL_Action_Customize(),
            new Models.ActionPerformer() {
                @Override
                public boolean isEnabled(Object node) {
                    return false;
                }

                @Override
                public void perform(Object[] nodes) {
//                customize ((Breakpoint) nodes [0]);
                }
            },
            Models.MULTISELECTION_TYPE_EXACTLY_ONE);

    @Override
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (!(node instanceof LineBreakpoint)) {
            return original.getActions(node);
        }

        Action[] oas = original.getActions(node);
        Action[] as = new Action[oas.length + 3];
        as[0] = GO_TO_SOURCE_ACTION;
        as[1] = null;
        System.arraycopy(oas, 0, as, 2, oas.length);
        as[as.length - 1] = CUSTOMIZE_ACTION;
        return as;
    }

    @Override
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof LineBreakpoint) {
            goToSource((LineBreakpoint) node);
        } else {
            original.performDefaultAction(node);
        }
    }

    public void addModelListener(ModelListener l) {
    }

    public void removeModelListener(ModelListener l) {
    }

    private static void goToSource(LineBreakpoint b) {
        Context.showSource(b);
    }
}
