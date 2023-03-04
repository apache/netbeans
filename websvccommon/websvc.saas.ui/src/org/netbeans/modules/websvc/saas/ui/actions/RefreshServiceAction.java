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
package org.netbeans.modules.websvc.saas.ui.actions;

import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * Action that refreshes a web service from its original location.
 * 
 * @author quynguyen
 * @author Jan Stola
 */
public class RefreshServiceAction extends NodeAction {

    @Override
    protected boolean enable(Node[] nodes) {
        boolean enabled = true;
        for (Node node : nodes) {
            Saas saas = node.getLookup().lookup(Saas.class);
            if (saas == null || saas.getState() == Saas.State.INITIALIZING) {
                enabled = false;
                break;
            }
        }
        return enabled;
    }

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected String iconResource() {
        return "org/netbeans/modules/websvc/saas/ui/resources/ActionIcon.gif"; // NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RefreshServiceAction.class, "REFRESH"); // NOI18N
    }

    @Override
    protected void performAction(final Node[] nodes) {
        String msg = NbBundle.getMessage(RefreshServiceAction.class, "WS_REFRESH");
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
        Object response = DialogDisplayer.getDefault().notify(d);
        if (NotifyDescriptor.YES_OPTION.equals(response)) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    for (Node node : nodes) {
                        Saas saas = node.getLookup().lookup(Saas.class);
                        try {
                            SaasServicesModel.getInstance().refreshService(saas);
                        } catch (Exception ex) {
                            NotifyDescriptor.Message msg = new NotifyDescriptor.Message(ex.getMessage());
                            DialogDisplayer.getDefault().notify(msg);
                        }
                    }
                }
            });
        }
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
