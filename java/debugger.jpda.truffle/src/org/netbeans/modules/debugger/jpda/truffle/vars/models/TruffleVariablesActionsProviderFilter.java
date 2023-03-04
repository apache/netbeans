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

package org.netbeans.modules.debugger.jpda.truffle.vars.models;

import java.net.URL;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/LocalsView",  types=NodeActionsProviderFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/ResultsView", types=NodeActionsProviderFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/ToolTipView", types=NodeActionsProviderFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM+"/WatchesView", types=NodeActionsProviderFilter.class),
})
public class TruffleVariablesActionsProviderFilter implements NodeActionsProviderFilter {

    private final RequestProcessor rp;

    public TruffleVariablesActionsProviderFilter(ContextProvider contextProvider) {
        this.rp = ((JPDADebuggerImpl) contextProvider.lookupFirst(null, JPDADebugger.class)).getRequestProcessor();
    }

    @NbBundle.Messages("CTL_GoToSource=Go to source")
    private final Action GO_TO_VALUE_SOURCE_ACTION = Models.createAction (
        Bundle.CTL_GoToSource(),
        new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object node) {
                TruffleVariable var = (TruffleVariable) node;
                return var.hasValueSource();
            }
            @Override
            public void perform (final Object[] nodes) {
                TruffleVariable var = (TruffleVariable) nodes[0];
                rp.post(() -> showSource(var.getValueSource()));
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
        
    @NbBundle.Messages("CTL_GoToTypeSource=Go to type")
    private final Action GO_TO_TYPE_SOURCE_ACTION = Models.createAction (
        Bundle.CTL_GoToTypeSource(),
        new Models.ActionPerformer () {
            @Override
            public boolean isEnabled (Object node) {
                TruffleVariable var = (TruffleVariable) node;
                return var.hasTypeSource();
            }
            @Override
            public void perform (final Object[] nodes) {
                TruffleVariable var = (TruffleVariable) nodes[0];
                rp.post(() -> showSource(var.getTypeSource()));
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );

    @Override
    public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
        if (node instanceof TruffleVariable) {
            TruffleVariable var = (TruffleVariable) node;
            rp.post(() -> {
                SourcePosition source = var.getValueSource();
                if (source == null) {
                    source = var.getTypeSource();
                }
                showSource(source);
            });
        } else {
            original.performDefaultAction(node);
        }
    }

    @Override
    public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
        UnknownTypeException originalUTEx = null;
        Action [] actions;
        try {
            actions = original.getActions (node);
        } catch (UnknownTypeException utex) {
            originalUTEx = utex;
            actions = new Action[0];
        }
        if (node instanceof TruffleVariable) {
            TruffleVariable var = (TruffleVariable) node;
            boolean hasValueSource = var.hasValueSource();
            boolean hasTypeSource = var.hasTypeSource();
            if (hasValueSource || hasTypeSource) {
                int l = actions.length;
                if (hasValueSource) {
                    l++;
                }
                if (hasTypeSource) {
                    l++;
                }
                Action[] newActions = new Action[l];
                System.arraycopy(actions, 0, newActions, 0, actions.length);
                l = actions.length;
                if (hasValueSource) {
                    newActions[l++] = GO_TO_VALUE_SOURCE_ACTION;
                }
                if (hasTypeSource) {
                    newActions[l++] = GO_TO_TYPE_SOURCE_ACTION;
                }
                actions = newActions;
            }
        } else if (originalUTEx != null) {
            throw originalUTEx;
        }
        return actions;
    }

    @NbBundle.Messages({"# {0} - The file path", "MSG_NoSourceFile=Cannot find source file {0}."})
    private void showSource(SourcePosition source) {
        if (source == null) {
            return ;
        }
        URL url = source.getSource().getUrl();
        int lineNumber = source.getStartLine();
        SwingUtilities.invokeLater (() -> {
            boolean success = EditorContextBridge.getContext().showSource(url.toExternalForm(), lineNumber, null);
            if (!success) {
                NotifyDescriptor d = new NotifyDescriptor.Message(Bundle.MSG_NoSourceFile(url.toExternalForm()), NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(d);
            }
        });
    }
}
