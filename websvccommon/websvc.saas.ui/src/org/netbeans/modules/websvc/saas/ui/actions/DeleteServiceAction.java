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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 * Deletes a user-defined web service.
 * 
 * @author  nam
 */
public class DeleteServiceAction extends NodeAction {

    @Override
    protected boolean enable(Node[] nodes) {
        for (Node n : nodes) {
            Saas saas = n.getLookup().lookup(Saas.class);
            if (saas == null || !saas.isUserDefined()) {
                return false;
            }
        }
        return true;
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
        return NbBundle.getMessage(DeleteServiceAction.class, "DELETE"); // NOI18N
    }

    @Override
    protected void performAction(Node[] nodes) {
        final List<Saas> saases = new ArrayList<Saas>();
        for (Node n : nodes) {
            Saas saas = n.getLookup().lookup(Saas.class);
            saases.add(saas);
        }

        String msg = NbBundle.getMessage(this.getClass(), "WS_DELETE", saases); // NOI18N
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
        Object response = DialogDisplayer.getDefault().notify(d);
        if (NotifyDescriptor.YES_OPTION.equals(response)) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    for (Saas saas : saases) {
                        SaasServicesModel.getInstance().removeService(saas);
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
