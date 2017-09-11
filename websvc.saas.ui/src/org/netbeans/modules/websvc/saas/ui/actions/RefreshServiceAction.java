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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
